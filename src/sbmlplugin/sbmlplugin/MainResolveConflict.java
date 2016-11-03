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

import ij.IJ;

import java.util.Map.Entry;
import java.util.Set;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfGeometryDefinitions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SpatialModelPlugin;

import sbmlplugin.geometry.SampledFieldGeometryData;
import sbmlplugin.gui.TargetDomainChooser;
import sbmlplugin.image.ImageEdit;
import sbmlplugin.image.SplitDomains;
import sbmlplugin.util.ModelSaver;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 28, 2015
 */
public class MainResolveConflict extends MainSBaseSpatial {
	
	/** The target domain. */
	private String targetDomain;
	
	/** The geometry. */
	private Geometry geometry;
	
	/** The gd. */
	private GeometryDefinition gd;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		try{
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		}
		this.model = document.getModel();
		checkSBMLDocument(document);
		gd = getActiveSampledFieldGeometry(model);
		if(gd == null){
			IJ.error("This plugin is able to resolve membrane confliction only for Sampled Field Geometry");
			return;
		}
		
		getTargetDomains();
		
		if(targetDomain.equals("")){
			IJ.error("No target domain found");
			return;
		}
		
		SampledFieldGeometryData sfgd = new SampledFieldGeometryData(gd, geometry);
		spImg = sfgd.getSpatialImage();
		this.hashSampledValue = spImg.getHashSampledValue();
		SplitDomains sd = new SplitDomains(spImg, targetDomain);
		Set<Integer> adjacentToTargetSet = sd.getAdjacentToTargetList();
		spImg.updateImage(sd.getStackImage());
		removeMembrane(adjacentToTargetSet);
		
		renewModelData();
		
		ModelSaver saver = new ModelSaver(document);
		saver.save();
		spImg.saveAsImage(saver.getPath(), saver.getName());
		visualize(spImg);
	}

	/**
	 * Renew model data.
	 */
	private void renewModelData(){
		spImg.createHashDomainTypes();
		new ImageEdit(spImg);
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		sbmlexp.createGeometryElements();
		document = sbmlexp.getDocument();
	}
	
	/**
	 * Removes the membrane.
	 *
	 * @param adjacentToTargetSet the adjacent to target set
	 */
	private void removeMembrane(Set<Integer> adjacentToTargetSet){
		for(Entry<String, Integer> e : hashSampledValue.entrySet()){
			if(adjacentToTargetSet.contains(e.getValue()))
				removeMembraneFromModel(targetDomain, e.getKey());
		}
	}
	
	/**
	 * Removes the membrane from model.
	 *
	 * @param dom1 the dom 1
	 * @param dom2 the dom 2
	 */
	private void removeMembraneFromModel(String dom1, String dom2) {
		removeFromListOf(dom1, dom2, geometry.getListOfDomainTypes());
		removeFromListOf(dom1, dom2, geometry.getListOfDomains());
		removeFromListOf(dom1, dom2, geometry.getListOfAdjacentDomains());
	}
	
	/**
	 * Removes the from list of.
	 *
	 * @param id1 the id 1
	 * @param id2 the id 2
	 * @param list the list
	 */
	private void removeFromListOf(String id1, String id2, ListOf list){
		SBase base;
		String id;
		for(long i = list.size() - 1; i > 0 ; i--){
			base = list.get(i);
			id = base.getId();
			if(id.contains(id1) && id.contains(id2))
				base.removeFromParentAndDelete();
		}
	}
	
	/**
	 * Gets the active sampled field geometry.
	 *
	 * @param model the model
	 * @return the active sampled field geometry
	 */
	private GeometryDefinition getActiveSampledFieldGeometry(Model model){
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		geometry = spatialplugin.getGeometry();
		ListOfGeometryDefinitions logd = geometry.getListOfGeometryDefinitions();
	
		//TODO multiple geometry
		for(int i = 0 ; i < logd.size() ; i++){
			GeometryDefinition gd = logd.get(i);
			if(gd.isSampledFieldGeometry() && (!gd.isSetIsActive() || gd.getIsActive()))
				return gd;
		}
		return null;
	}

	/**
	 * Gets the target domains.
	 *
	 * @return the target domains
	 */
	private void getTargetDomains() {
		TargetDomainChooser tdc = new TargetDomainChooser(model);
		while (tdc.getTargetDomain() == null){
			synchronized (model) {
				
			}
		}	
		targetDomain = tdc.getTargetDomain();
	}
}
