/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package sbmlplugin.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;

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
		ListOfGeometryDefinitions logd = geometry.getListOfGeometryDefinitions();
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

		if(gd.isSampledFieldGeometry()){
			SampledFieldGeometryData sfgd = new SampledFieldGeometryData(gd, geometry);
			return sfgd.getSpatialImage();
		}else if(gd.isAnalyticGeometry()){
			AnalyticGeometryData agd = new AnalyticGeometryData(gd, geometry, minCoord, maxCoord, dispCoord);
			return agd.getSpatialImage();
		}else if(gd.isParametricGeometry()){
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
		ListOfCoordinateComponents locc = geometry.getListOfCoordinateComponents();
		dimension = (int) locc.size();
		for(int i = 0 ; i < locc.size(); i++){
			CoordinateComponent cc = locc.get(i);
			switch (cc.getType()){
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_X:
				minCoord.setX((float) cc.getBoundaryMin().getValue()); maxCoord.setX((float) cc.getBoundaryMax().getValue());
				break;
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Y:
				minCoord.setY((float) cc.getBoundaryMin().getValue()); maxCoord.setY((float) cc.getBoundaryMax().getValue());
				break;
			case libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Z:
				minCoord.setZ((float) cc.getBoundaryMin().getValue()); maxCoord.setZ((float) cc.getBoundaryMax().getValue());
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
		ListOfDomainTypes lodt = geometry.getListOfDomainTypes();
		for(int i = 0 ; i < lodt.size(); i++){
			DomainType d = lodt.get(i);
			if(d.getSpatialDimensions() == dimension)
				domList.add(d.getId());
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
