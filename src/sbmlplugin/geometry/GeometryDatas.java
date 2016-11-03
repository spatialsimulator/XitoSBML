package sbmlplugin.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;

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

import sbmlplugin.image.SpatialImage;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public class GeometryDatas {
	
	/** The model. */
	protected Model model;
	
	/** The spatialplugin. */
	protected SpatialModelPlugin spatialplugin;
	
	/** The geometry. */
	protected Geometry geometry;
	
	/** The min coord. */
	protected Point3f minCoord = new Point3f();
	
	/** The max coord. */
	protected Point3f maxCoord = new Point3f();
	
	/** The disp coord. */
	protected Point3f dispCoord = new Point3f();		//displacement from original coordinates to modified coordinate
	
	/** The dimension. */
	protected int dimension;
	
	/** The dom list. */
	protected ArrayList<String> domList = new ArrayList<String>();
	
	/** The sp img list. */
	private ArrayList<SpatialImage> spImgList = new ArrayList<SpatialImage>();
	
	/**
	 * Instantiates a new geometry datas.
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
	 * Createsp img list.
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
	 * Gets the sp img from geo.
	 *
	 * @param gd the gd
	 * @return the sp img from geo
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
					+ "This plugin is only able to visualize AnalyticGeometry SampledFieldGeometry. ");
			return null;
		}
		return null;
	}
	
	/**
	 * Gets the coordinates.
	 *
	 * @return the coordinates
	 */
	protected void getCoordinates(){
		ListOf<CoordinateComponent> locc = geometry.getListOfCoordinateComponents();
		dimension = (int) locc.size();
		for(int i = 0 ; i < locc.size(); i++){
			CoordinateComponent cc = locc.get(i);
			switch (cc.getType()){
			case cartesianX:
				minCoord.setX((float) cc.getBoundaryMinimum().getValue()); maxCoord.setX((float) cc.getBoundaryMaximum().getValue());
				break;
			case cartesianY:
				minCoord.setY((float) cc.getBoundaryMinimum().getValue()); maxCoord.setY((float) cc.getBoundaryMaximum().getValue());
				break;
			case cartesianZ:
				minCoord.setZ((float) cc.getBoundaryMinimum().getValue()); maxCoord.setZ((float) cc.getBoundaryMaximum().getValue());
				break;
			}
		}
		
		adjustAxis();
	}
	
	/**
	 * Adjust axis.
	 */
	protected void adjustAxis(){
		switch(dimension){
		case 3:
			if(minCoord.getZ() < 0){
				dispCoord.setZ(-1 * minCoord.getZ());
				maxCoord.setZ(maxCoord.getZ() - minCoord.getZ());
				minCoord.setZ(0);
			}
		case 2:
			if(minCoord.getX() < 0){
				dispCoord.setX(-1 * minCoord.getX());
				maxCoord.setX(maxCoord.getX() - minCoord.getX());
				minCoord.setX(0);
			}
			
			if(minCoord.getY() < 0){
				dispCoord.setY(-1 * minCoord.getY());
				maxCoord.setY(maxCoord.getY() - minCoord.getY());
				minCoord.setY(0);
			}
			break;
		}
	}
	
	/**
	 * Gets the domain types.
	 *
	 * @return the domain types
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
	 * Gets the sp img list.
	 *
	 * @return the sp img list
	 */
	public ArrayList<SpatialImage> getSpImgList() {
		createspImgList();
		return spImgList;
	}

}
