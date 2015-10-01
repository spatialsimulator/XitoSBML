package sbmlplugin.sbmlplugin;

import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbml.libsbml.ReqExtension;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialExtension;

import sbmlplugin.image.SpatialImage;
import sbmlplugin.util.PluginConstants;
import sbmlplugin.visual.Viewer;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public abstract class MainSBaseSpatial extends MainSpatial implements PlugIn{
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);

	public void checkSBMLDocument(SBMLDocument document){
		if(document == null || document.getModel() == null) throw new IllegalArgumentException("Non-supported format");
		model = document.getModel();
		if(!checkLevelAndVersion()) throw new IllegalArgumentException("Incompatible level and version");
		checkExtension();
	}
	
	protected void visualize(ArrayList<SpatialImage> spImgList){
		Iterator<SpatialImage> it = spImgList.iterator();
		Viewer viewer = new Viewer();
		while(it.hasNext()){
			viewer.view(it.next());
		}
	}
	
	protected SBMLDocument getDocument() throws NullPointerException{
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("SBML File(*.xml)", "xml"));
		int returnVal = chooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			throw new NullPointerException();
		File f = chooser.getSelectedFile();
		SBMLReader reader = new SBMLReader();
		return reader.readSBMLFromFile(f.getAbsolutePath());
	}
	
	protected boolean checkLevelAndVersion(){
		if(model.getLevel() == PluginConstants.LOWERSBMLLEVEL){
			System.err.println("Model must be level 3 to use this plugin");
			return false;
		}
		
		//add check if new verison comes up check
		return true;
	}
	
	protected void checkExtension(){
		//if(model.getLevel() == 2) return;
		
		SBMLNamespaces sbmlns = document.getSBMLNamespaces();
		if(!document.getPackageRequired("spatial")){			//check spatial
			document.setPackageRequired("spatial", true);
			sbmlns.addPackageNamespace("spatial", 1);
		}

		if(!document.getPackageRequired("req")){				//check req
			document.setPackageRequired("req", true);
			sbmlns.addPackageNamespace("req", 1);
		}
		
		// add extension if necessary
		if(!model.isPackageEnabled("spatial"))
			model.enablePackage(SpatialExtension.getXmlnsL3V1V1(), "spatial", true);

		if(!model.isPackageEnabled("req"))
			model.enablePackage(ReqExtension.getXmlnsL3V1V1(), "req", true);	
	}

}
