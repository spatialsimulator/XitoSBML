package sbmlplugin.sbmlplugin;

import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

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

	public void checkSBMLDocument(SBMLDocument document){
		if(document == null || document.getModel() == null) 
			throw new IllegalArgumentException("Non-supported format file");
		model = document.getModel();
		checkLevelAndVersion();
		checkExtension();
	}
	
	protected void checkLevelAndVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL || model.getVersion() != PluginConstants.SBMLVERSION)
			throw new IllegalArgumentException("Incompatible level and version");
	}
	
	protected void checkExtension(){
		if(!document.getPackageRequired("spatial"))
			throw new IllegalArgumentException("Could not find spatial extension");

		if(!document.getPackageRequired("req")) 
			throw new IllegalArgumentException("Could not find req extension");

	}

}
