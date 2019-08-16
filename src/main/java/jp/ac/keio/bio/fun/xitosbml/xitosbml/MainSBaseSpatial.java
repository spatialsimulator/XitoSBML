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

/**
 * The class MainSBaseSpatial.
 *
 * The class MainSBaseSpatial, which is a class for handling spatial model in XitoSBML.
 * Main functions provided in this class are:
 * <ul>
 *  <li>visualization</li>
 *  <li>validation</li>
 * </ul>
 * Date Created: Jun 17, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public abstract class MainSBaseSpatial extends MainSpatial implements PlugIn{
	
	/**
     * Not used in this class.
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);
	
	/**
	 * Visualize the model in 3D space (even a model is 2D model).
	 * 3D visualization is done by using ImageJ 3D viewer plugin.
	 *
	 * @param spImgList list of {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage},
	 *                  which is a class for storing an image in XitoSBML
	 */
	protected void visualize(ArrayList<SpatialImage> spImgList){
		Iterator<SpatialImage> it = spImgList.iterator();
		Viewer viewer = new Viewer();
		while(it.hasNext()){
			viewer.view(it.next());
		}
	}
	
	/**
	 * Returns an SBML document from given file path.
	 *
	 * @return SBML document
	 * @throws NullPointerException the null pointer exception
	 * @throws XMLStreamException the XML stream exception
	 * @throws IOException the IOException exception
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
     * This method will check whether the SBML document has correct level, version and extension.
	 *
	 * @param document SBML document
	 */
	public void checkSBMLDocument(SBMLDocument document){
		if(document == null || document.getModel() == null) 
			throw new IllegalArgumentException("Non-supported format file");
		model = document.getModel();
		checkLevelAndVersion();
		checkExtension();
	}
	
	/**
	 * Check whether SBML level and version matches with Level 3 Version 1.
	 */
	protected void checkLevelAndVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL || model.getVersion() != PluginConstants.SBMLVERSION)
			IJ.error("Incompatible level and version");
	}
	
	/**
	 * Check whether the model contains spatial extension.
	 */
	protected void checkExtension(){
		if(!document.getPackageRequired("spatial"))
			IJ.error("Could not find spatial extension");

	}

	/**
	 * Example main() method which will launch ImageJ and call "run Spatial Image SBML plugin".
	 * @param args an array of command-line arguments for the application
	 */
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
