package sbmlplugin.sbmlplugin;

import ij.IJ;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfGeometryDefinitions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SpatialModelPlugin;

import sbmlplugin.geometry.SampledFieldGeometryData;
import sbmlplugin.gui.TargetDomainChooser;
import sbmlplugin.image.SpatialImage;
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
		document = getDocment();
		checkSBMLDocument(document);
		
		gd = getActiveSampledFieldGeometry(document.getModel());
		if(gd == null){
			IJ.error("This plugin is able to resolve membrane confliction only for Sampled Field Geometry");
			return;
		}
		getTargetDomains();
		
		if(targetDomain.equals("")){
			return;
		}
		
		SampledFieldGeometryData sfgd = new SampledFieldGeometryData(gd, geometry);
		SpatialImage spImg = sfgd.getSpatialImage();
		SplitDomains sd = new SplitDomains(spImg, targetDomain);
		spImg.updateImage(sd.getStackImage());
		
		save();
		visualize(spImg);
	}

	private GeometryDefinition getActiveSampledFieldGeometry(Model model){
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		geometry = spatialplugin.getGeometry();
		ListOfGeometryDefinitions logd = geometry.getListOfGeometryDefinitions();
		
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
