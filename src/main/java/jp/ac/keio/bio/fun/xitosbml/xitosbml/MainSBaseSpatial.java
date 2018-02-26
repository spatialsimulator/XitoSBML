package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import jp.ac.keio.bio.fun.xitosbml.util.PluginConstants;
import jp.ac.keio.bio.fun.xitosbml.visual.Viewer;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
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
	
	/**
	 * Visualize.
	 *
	 * @param spImgList the sp img list
	 */
	protected void visualize(ArrayList<SpatialImage> spImgList){
		Iterator<SpatialImage> it = spImgList.iterator();
		Viewer viewer = new Viewer();
		while(it.hasNext()){
			viewer.view(it.next());
		}
	}
	
	/**
	 * Gets the document.
	 *
	 * @return the document
	 * @throws NullPointerException the null pointer exception
	 * @throws XMLStreamException the XML stream exception
	 * @throws IOException 
	 */
	protected SBMLDocument getDocument() throws NullPointerException, XMLStreamException, IOException{

		GenericDialogPlus gd = new GenericDialogPlus("File Explorer");
		gd.addFileField("Target Model", "");
		gd.showDialog();
		if(gd.wasCanceled())
			throw new NullPointerException();

		String dir = gd.getNextString();

		return SBMLReader.read(new File(dir));
	}

	/**
	 * Check SBML document.
	 *
	 * @param document the document
	 */
	public void checkSBMLDocument(SBMLDocument document){
		if(document == null || document.getModel() == null) 
			throw new IllegalArgumentException("Non-supported format file");
		model = document.getModel();
		checkLevelAndVersion();
		checkExtension();
	}
	
	/**
	 * Check level and version.
	 */
	protected void checkLevelAndVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL || model.getVersion() != PluginConstants.SBMLVERSION)
			IJ.error("Incompatible level and version");
	}
	
	/**
	 * Check extension.
	 */
	protected void checkExtension(){
		if(!document.getPackageRequired("spatial"))
			IJ.error("Could not find spatial extension");

	}

	public static void main(String[] args) {
		GenericDialogPlus gd = new GenericDialogPlus("Count Files");
		gd.addDirectoryField("Directory", "");
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		String dir = gd.getNextString();

		new File(dir);
	}
	
}
