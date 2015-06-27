package sbmlplugin.visual;

import java.util.ArrayList;

import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.DomainType;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfCoordinateComponents;
import org.sbml.libsbml.ListOfDomainTypes;
import org.sbml.libsbml.ListOfGeometryDefinitions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.libsbmlConstants;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public class GeometryDatas {
	protected Model model;
	protected SpatialModelPlugin spatialplugin;
	protected Geometry geometry;
	protected double xmin, xmax;
	protected double ymin, ymax;
	protected double zmin, zmax;
	protected int dimension;
	protected ArrayList<String> domList = new ArrayList<String>();
	private ArrayList<SpatialImage> spImgList = new ArrayList<SpatialImage>();
	
	public GeometryDatas(Model model){
		this.model = model;
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		geometry = spatialplugin.getGeometry();
		getCoordinates();
		getDomainTypes();
	}
	
	protected void createspImgList(){
		ListOfGeometryDefinitions logd = geometry.getListOfGeometryDefinitions();
		SpatialImage spImg; 
		for(int i = 0 ; i < logd.size() ; i++){
			GeometryDefinition gd = logd.get(i);
			spImg = getSpImgFromGeo(gd);
			if(spImg != null)
				spImgList.add(spImg);
		}
	}
	
	protected SpatialImage getSpImgFromGeo(GeometryDefinition gd){
		if(gd.isSetIsActive() && !gd.getIsActive()) return null;			//if isactive set and is false
		
		if(gd.isSampledFieldGeometry()){
			SampledFieldGeometryData sfgd = new SampledFieldGeometryData(gd, geometry);
			return sfgd.getSpatialImage();
		}else if(gd.isAnalyticGeometry()){
			
		}else if(gd.isParametricGeometry()){
			
		}else{
			System.err.println("Not able to obtain geometry \n"
					+ "This plugin is only able to visualize AnalyticGeometry, ParametricGeometry, SampledField Geometry, ");
			return null;
		}
		return null;
	}
	
	protected void getCoordinates(){
		ListOfCoordinateComponents locc = geometry.getListOfCoordinateComponents();
		dimension = (int) locc.size();
		for(int i = 0 ; i < locc.size(); i++){
			CoordinateComponent cc = locc.get(i);
			switch (cc.getType()){
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_X:
				xmin = cc.getBoundaryMin().getValue(); xmax = cc.getBoundaryMax().getValue();
				break;
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Y:
				ymin = cc.getBoundaryMin().getValue(); zmax = cc.getBoundaryMax().getValue();
				break;
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Z:
				zmin = cc.getBoundaryMin().getValue(); zmax = cc.getBoundaryMax().getValue();
				break;
			}
		}
		adjustAxis();
	}
	
	protected void adjustAxis(){
		switch(dimension){
		case 3:
			if(zmin < 0){
				zmax -= zmin;
				zmin = 0;
			}
		case 2:
			if(xmin < 0){
				xmax -= xmin;
				xmin = 0;
			}
			
			if(ymin < 0){
				ymax -= ymin;
				ymin = 0;
			}
		}
		
	}
	
	protected void getDomainTypes(){
		ListOfDomainTypes lodt = geometry.getListOfDomainTypes();
		for(int i = 0 ; i < lodt.size(); i++){
			DomainType d = lodt.get(i);
			if(d.getSpatialDimensions() == dimension)
				domList.add(d.getId());
		}
	}


	public ArrayList<SpatialImage> getSpImgList() {
		createspImgList();
		return spImgList;
	}

}
