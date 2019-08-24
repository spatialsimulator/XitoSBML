package jp.ac.keio.bio.fun.xitosbml.geometry;

import java.util.ArrayList;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.CoordinateComponent;
import org.sbml.jsbml.ext.spatial.DomainType;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.ParametricGeometry;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;

import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import math3d.Point3d;

/**
 * The class GeometryDatas, which contains following objects which are related to geometry.
 * <ul>
 *     <li>SBML model {@link org.sbml.jsbml.Model}</li>
 *     <li>Geometry {@link org.sbml.jsbml.ext.spatial.Geometry}</li>
 *     <li>coordinates of boundary</li>
 *     <li>dimension</li>
 *     <li>list of domains(String)</li>
 *     <li>list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}s</li>
 * </ul>
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.xitosbml.MainSBaseSpatial},
 * to visualize a model in 3D space.
 *
 * Date Created: Jun 25, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class GeometryDatas {
	
	/** The SBML model. */
	protected Model model;
	
	/** The spatialplugin. */
	protected SpatialModelPlugin spatialplugin;
	
	/** The Geometry object. Geometry might be a SampledFieldGeometry or AnalyticGeometry */
	protected Geometry geometry;
	
	/** The minimum value of the coordinate axis (boundary). */
	protected Point3d minCoord = new Point3d();
	
	/** The maximum value of the coordinate axis (boundary). */
	protected Point3d maxCoord = new Point3d();
	
	/** The displacement from original coordinates to modified coordinate. */
	protected Point3d dispCoord = new Point3d();		//displacement from original coordinates to modified coordinate
	
	/** The dimension of the geometry. */
	protected int dimension;
	
	/** The list of domains. */
	protected ArrayList<String> domList = new ArrayList<String>();
	
	/** The list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}. */
	private ArrayList<SpatialImage> spImgList = new ArrayList<SpatialImage>();
	
	/**
	 * Instantiates a new GeometryDatas object with given SBML model.
	 * The geometry, minimum values of the coordinate * axis (boundary), domain
	 * types which are stored in the model will be set to this object.
	 *
	 * @param model the model
	 */
	public GeometryDatas(Model model){
		this.model = model;
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		geometry = spatialplugin.getGeometry();
		getCoordinates();
		getDomainTypes();
	}
	
	/**
	 * Create the list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage},
	 * which is a class for handling spatial image in XitoSBML.
	 */
	protected void createspImgList(){
		ListOf<GeometryDefinition> logd = geometry.getListOfGeometryDefinitions();
		SpatialImage spImg; 
		for(int i = 0 ; i < logd.size() ; i++){
			GeometryDefinition gd = logd.get(i);
			spImg = getSpImgFromGeo(gd);
			if(spImg != null)
				spImgList.add(spImg);
		}
	}
	
	/**
	 * Gets the {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}
	 * object from given GeometryDefinition. Currently, this method
	 * supports AnalyticGeometry and SampledFieldGeometry.
	 *
	 * @param gd the JSBML GeometryDefinition object
	 * @return the SpatialImage object
	 */
	protected SpatialImage getSpImgFromGeo(GeometryDefinition gd){
		if(gd.isSetIsActive() && !gd.getIsActive()) return null;			//if isactive set and is false

		if(gd instanceof SampledFieldGeometry){
			SampledFieldGeometryData sfgd = new SampledFieldGeometryData(gd, geometry);
			return sfgd.getSpatialImage();
		}else if(gd instanceof AnalyticGeometry){
			AnalyticGeometryData agd = new AnalyticGeometryData(gd, geometry, minCoord, maxCoord, dispCoord);
			return agd.getSpatialImage();
		}else if(gd instanceof ParametricGeometry){
				//TODO 
		}else{
			System.err.println("Not able to obtain geometry \n"
					+ "This plugin is only able to visualize AnalyticGeometry and SampledFieldGeometry. ");
			return null;
		}
		return null;
	}
	
	/**
	 * Gets the minimum and maximum values of the coordinate axis (boundary)
	 * from the geometry, and then adjust the axis (reset to 0 origin).
	 *
	 */
	protected void getCoordinates(){
		ListOf<CoordinateComponent> locc = geometry.getListOfCoordinateComponents();
		dimension = (int) locc.size();
		for(int i = 0 ; i < locc.size(); i++){
			CoordinateComponent cc = locc.get(i);
			switch (cc.getType()){
			case cartesianX:
				minCoord.x = ( cc.getBoundaryMinimum().getValue()); maxCoord.x = ( cc.getBoundaryMaximum().getValue());
				break;
			case cartesianY:
				minCoord.y = ( cc.getBoundaryMinimum().getValue()); maxCoord.y = ( cc.getBoundaryMaximum().getValue());
				break;
			case cartesianZ:
				minCoord.z = ( cc.getBoundaryMinimum().getValue()); maxCoord.z = ( cc.getBoundaryMaximum().getValue());
				break;
			}
		}
		
		adjustAxis();
	}
	
	/**
	 * Adjust axis to 0 origin.
	 * This adjustment is required to properly visualized on ImageJ 3D Viewer.
	 */
	protected void adjustAxis(){
		switch(dimension){
		case 3:
			if(minCoord.z < 0){
				dispCoord.z = (-1 * minCoord.z);
				maxCoord.z = (maxCoord.z - minCoord.z);
				minCoord.z = (0);
			}
		case 2:
			if(minCoord.x < 0){
				dispCoord.x = (-1 * minCoord.x);
				maxCoord.x = (maxCoord.x - minCoord.x);
				minCoord.x = (0);
			}
			
			if(minCoord.y < 0){
				dispCoord.y = (-1 * minCoord.y);
				maxCoord.y = (maxCoord.y - minCoord.y);
				minCoord.y = (0);
			}
			break;
		}
	}
	
	/**
	 * Gets the domain types from the geometry object, and then add them
	 * to the list of domains.
	 *
	 */
	protected void getDomainTypes(){
		ListOf<DomainType> lodt = geometry.getListOfDomainTypes();
		for(int i = 0 ; i < lodt.size(); i++){
			DomainType d = lodt.get(i);
			if(d.getSpatialDimensions() == dimension)
				domList.add(d.getSpatialId());
		}
	}

	/**
	 * Gets the list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}.
	 *
	 * @return the list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}.
	 */
	public ArrayList<SpatialImage> getSpImgList() {
		createspImgList();
		return spImgList;
	}

}
