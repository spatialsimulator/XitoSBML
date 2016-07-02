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
	protected Point3f minCoord = new Point3f();
	protected Point3f maxCoord = new Point3f();
	protected Point3f dispCoord = new Point3f();		//displacement from original coordinates to modified coordinate
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
		ListOf<GeometryDefinition> logd = geometry.getListOfGeometryDefinitions();
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
	
	protected void getDomainTypes(){
		ListOf<DomainType> lodt = geometry.getListOfDomainTypes();
		for(int i = 0 ; i < lodt.size(); i++){
			DomainType d = lodt.get(i);
			if(d.getSpatialDimensions() == dimension)
				domList.add(d.getSpatialId());
		}
	}

	public ArrayList<SpatialImage> getSpImgList() {
		createspImgList();
		return spImgList;
	}

}
