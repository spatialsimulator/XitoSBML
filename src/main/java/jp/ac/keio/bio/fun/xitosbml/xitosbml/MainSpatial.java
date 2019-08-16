package jp.ac.keio.bio.fun.xitosbml.xitosbml;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;

import ij.ImagePlus;
import ij.plugin.PlugIn;
import jp.ac.keio.bio.fun.xitosbml.image.CreateImage;
import jp.ac.keio.bio.fun.xitosbml.image.Filler;
import jp.ac.keio.bio.fun.xitosbml.image.ImageBorder;
import jp.ac.keio.bio.fun.xitosbml.image.ImageEdit;
import jp.ac.keio.bio.fun.xitosbml.image.ImageExplorer;
import jp.ac.keio.bio.fun.xitosbml.image.Interpolater;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import jp.ac.keio.bio.fun.xitosbml.pane.TabTables;
import jp.ac.keio.bio.fun.xitosbml.visual.DomainStruct;
import jp.ac.keio.bio.fun.xitosbml.visual.Viewer;


/**
 * The class MainSpatial, which creates a GUI for XitoSBML.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public abstract class MainSpatial implements PlugIn{

	/** The SBML document. */
	protected SBMLDocument document;
	
	/** The SBML model. */
	protected Model model;
	
	/** The SBML spatialplugin. */
	protected SpatialModelPlugin spatialplugin;

	/** The ImageExplorer. */
	protected ImageExplorer imgexp;
	
	/** The hashmap of domain types. */
	private HashMap<String, Integer> hashDomainTypes;
	
	/** The hashmap of sampled value. */
	protected HashMap<String, Integer> hashSampledValue;
	
	/** The viewer. */
	protected Viewer viewer;
	
	/** The SpatialImage, which is a class for handling spatial image in XitoSBML. */
	protected SpatialImage spImg;
	
	/**
	 * Create a GUI which allows users to specify the correspondence between each image
	 * and the region in the cell.
	 */
	protected void gui() {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer> ();
		imgexp = new ImageExplorer(hashDomainTypes,hashSampledValue);
		while (imgexp.isVisible()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					
				}
			}
		}
	}
	
	/**
	 * Following process is performed to an image:
	 * <ol>
	 * <li>Interpolate an image if it is a Z-stack image (3D image)</li>
	 * <li>Fill holes (blank pixels) in the image by morphology operation if exists</li>
	 * </ol>
	 *
	 * The converted image will be generated as {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage},
	 * which is a base class for representing spatial image in XitoSBML.
	 */
	protected void computeImg(){
		Interpolater interpolater = new Interpolater();
		HashMap<String, ImagePlus> hashDomFile = imgexp.getDomFile();
		interpolater.interpolate(hashDomFile);
		Filler fill = new Filler();

		for(Entry<String, ImagePlus> e : hashDomFile.entrySet())
			hashDomFile.put(e.getKey(), fill.fill(e.getValue()));
		
		CreateImage creIm = new CreateImage(imgexp.getDomFile(), hashSampledValue);
		spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		ImagePlus img = fill.fill(spImg);
		spImg.setImage(img);
		ImageBorder imgBorder = new ImageBorder(spImg);
		spImg.updateImage(imgBorder.getStackImage());

		new ImageEdit(spImg);
	}
	
	/**
	 * Visualize the spatial model with ImageJ 3D Viewer.
	 *
	 * @param spImg the SpatialImage, which is a class for handling spatial image in XitoSBML.
	 */
	protected void visualize (SpatialImage spImg){
		viewer = new Viewer();
		viewer.view(spImg);
	}
	
	/**
	 * Create a tabbed table which allows users to add the SBases to the model through GUI.
	 */
	protected void addSBases(){
		ListOf<Parameter> lop = model.getListOfParameters();
		ListOf<Species> los = model.getListOfSpecies();
		TabTables tt = new TabTables(model);
		
		while(tt.isRunning()){
			synchronized(lop){
				synchronized(los){
					
				}
			}
		}
	}
	
	/**
	 * Show inclusion relationship of domains as a graph.
	 */
	protected void showDomainStructure(){
		spatialplugin = (SpatialModelPlugin)model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		new DomainStruct().show(g);	
	}
	
	/**
	 * Visualize SpatialImage.
	 *
	 * @param spImg the SpatialImage, which is a class for handling spatial image in XitoSBML.
	 */
	protected void showStep(SpatialImage spImg){
		visualize(spImg);
	}

	/**
	 * Prints the SBML document to stdout.
	 */
	protected void print(){
		String docStr;
		try {
			docStr = new TidySBMLWriter().writeSBMLToString(document);
			System.out.println(docStr);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}