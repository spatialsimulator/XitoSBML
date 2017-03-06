package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import javax.swing.JOptionPane;

import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;
import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: May 12, 2015
 */
public class MainImgSpatial extends MainSpatial {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {

		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		model = sbmlexp.getModel();
		sbmlexp.createGeometryElements();
		//visualize(spImg);
		
		//add species and parameter here
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addSBases();
		
		sbmlexp.addCoordParameter();
		document = sbmlexp.getDocument();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
		spImg.saveAsImage(saver.getPath(), saver.getName());
		showDomainStructure();
		
		ModelValidator validator = new ModelValidator(document);
		validator.validate();
		print();
	}	
}