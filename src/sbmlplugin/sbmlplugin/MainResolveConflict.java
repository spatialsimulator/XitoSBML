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

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 28, 2015
 */
public class MainResolveConflict extends MainSBaseSpatial {
	
	private String targetDomain;
	private Geometry geometry;
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
			IJ.error("No targete domain found");
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
		save();
		visualize(spImg);
	}

	private void renewModelData(){
		spImg.createHashDomainTypes();
		new ImageEdit(spImg);
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
		sbmlexp.createGeometryElements();
	}
	
	private void removeMembrane(Set<Integer> adjacentToTargetSet){
		for(Entry<String, Integer> e : hashSampledValue.entrySet()){
			if(adjacentToTargetSet.contains(e.getValue()))
				removeMembraneFromModel(targetDomain, e.getKey());
		}
	}
	
	private void removeMembraneFromModel(String dom1, String dom2) {
		removeFromListOf(dom1, dom2, geometry.getListOfDomainTypes());
		removeFromListOf(dom1, dom2, geometry.getListOfDomains());
		removeFromListOf(dom1, dom2, geometry.getListOfAdjacentDomains());
	}
	
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

	private void getTargetDomains() {
		TargetDomainChooser tdc = new TargetDomainChooser(model);
		while (tdc.getTargetDomain() == null){
			synchronized (model) {
				
			}
		}	
		targetDomain = tdc.getTargetDomain();
	}
}
