package sbmlplugin;

import ij.plugin.PlugIn;

import javax.swing.JOptionPane;

import visual.DomainStruct;


/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: May 12, 2015
 */
public class MainImgSpatial extends MainSpatial implements PlugIn{

	@Override
	public void run(String arg) {
		createSBMLDoc();
		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
		visualize(spImg);
		sbmlexp.createGeometryElements();
		
		//add species and parameter here
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addParaAndSpecies();

		sbmlexp.addCoordParameter();
		save(sbmlexp);
		
		new DomainStruct().show(model);	
	}
}
