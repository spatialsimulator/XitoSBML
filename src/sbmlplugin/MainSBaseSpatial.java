package sbmlplugin;

import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;

import javax.swing.JFileChooser;

import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBMLReader;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public class MainSBaseSpatial extends MainSpatial implements PlugIn{
	
	final int LEVEL2 =  2;
	final int LEVEL = 3;
	final int VERSION = 1;
	final int VERSION4 = 4;
	boolean isLevelCompatible;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		document = getDocment();
		if(document == null || document.getModel() == null) return;
		model = document.getModel();
		checkLevelAndVersion();
		checkExtension();
		
		addParaAndSpecies();
		save();
		
		IJ.log(document.toSBML());
	}

	private SBMLDocument getDocment(){
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		File f = chooser.getSelectedFile();
		SBMLReader reader = new SBMLReader();
		return reader.readSBMLFromFile(f.getAbsolutePath());
	}
	
	private void checkLevelAndVersion(){
		if(model.getLevel() == LEVEL2)			 			//level 2		change to latest level 2 version
			document.setLevelAndVersion(LEVEL2, VERSION4);
		else{}												//level 3		if new verison comes up check

	}
	
	private void checkExtension(){
		if(model.getLevel() == 2) return ;
		
		SBMLNamespaces sbmlns = document.getSBMLNamespaces();
		if(!document.getPackageRequired("spatial")){			//check spatial
			document.setPackageRequired("spatial", true);
			sbmlns.addPackageNamespace("spatial", 1);
		}

		if(!document.getPackageRequired("req")){				//check req
			document.setPackageRequired("req", true);
			sbmlns.addPackageNamespace("req", 1);
		}
	}

}
