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
package sbmlplugin.sbmlplugin;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.util.HashMap;
import java.util.Map.Entry;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ReqSBasePlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;

import sbmlplugin.gui.ParamAndSpecies;
import sbmlplugin.image.CreateImage;
import sbmlplugin.image.Filler;
import sbmlplugin.image.ImageEdit;
import sbmlplugin.image.ImageExplorer;
import sbmlplugin.image.Interpolater;
import sbmlplugin.image.SpatialImage;
import sbmlplugin.visual.DomainStruct;
import sbmlplugin.visual.Viewer;


public abstract class MainSpatial implements PlugIn{

	protected SBMLDocument document;
	protected Model model;
	protected SBMLNamespaces sbmlns; 
	protected SpatialPkgNamespaces spatialns;
	protected SpatialModelPlugin spatialplugin;
	protected ReqSBasePlugin reqplugin;
	private ImageExplorer imgexp;
	private HashMap<String, Integer> hashDomainTypes;
	protected HashMap<String, Integer> hashSampledValue;
	protected Viewer viewer;
	protected SpatialImage spImg;
	
	protected void gui() {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer> ();
		imgexp = new ImageExplorer(hashDomainTypes,hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					
				}
			}
		}
	}
	
	protected void computeImg(){
		Interpolater interpolater = new Interpolater();
		HashMap<String, ImagePlus> hashDomFile = imgexp.getDomFile();
		interpolater.interpolate(hashDomFile);
		Filler fill = new Filler();

		for(Entry<String, ImagePlus> e : hashDomFile.entrySet())
			hashDomFile.put(e.getKey(), fill.fill(e.getValue()));
		
		CreateImage creIm = new CreateImage(imgexp.getDomFile(), hashSampledValue);
		spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		//showStep(spImg);
		ImagePlus img = fill.fill(spImg);
		spImg.setImage(img);
		//showStep(spImg);
		//ImageBorder imgBorder = new ImageBorder(spImg);
		//spImg.updateImage(imgBorder.getStackImage());

		//showStep(spImg);
		new ImageEdit(spImg);
		//showStep(spImg);
	}
	
	protected void visualize (SpatialImage spImg){
		viewer = new Viewer();
		viewer.view(spImg);
	}
	
	protected void addParaAndSpecies(){
		ListOfParameters lop = model.getListOfParameters();
		ListOfSpecies los = model.getListOfSpecies();
		ParamAndSpecies pas = new ParamAndSpecies(model);
		
		while(lop.size() == 0 || los.size() == 0 || pas.isRunning()){
			synchronized(lop){
				synchronized(los){
					
				}
			}
		}
	}
	
	protected void showDomainStructure(){
		spatialplugin = (SpatialModelPlugin)model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		new DomainStruct().show(g);	
	}
	
	protected void showStep(SpatialImage spImg){
		visualize(spImg);
	}
}