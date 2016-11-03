package sbmlplugin.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ij.IJ;
import ij.io.SaveDialog;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.libsbml;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 3, 2015
 */
public class ModelSaver {
	
	/** The document. */
	private SBMLDocument document;
	
	/** The model. */
	private Model model;
	
	/** The path. */
	private String path;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new model saver.
	 *
	 * @param document the document
	 */
	public ModelSaver(SBMLDocument document){
		this.document = document;
		this.model = document.getModel();
	}
	
	/**
	 * Save.
	 */
	public void save(){
		SaveDialog sd = new SaveDialog("Save SBML Document", model.getId(), ".xml");
	
		path = sd.getDirectory();
		name = sd.getFileName();
			
		IJ.log(name);
		
		setAnnotation();
		
		try {		
			if(name.contains(".xml"))	
				name = name.substring(0, name.indexOf('.'));
			document.getModel().setId(name);	
			libsbml.writeSBMLToFile(document, path + "/" + name + ".xml"); 			
		} catch(NullPointerException e) {
			System.out.println("SBML document was not saved");
		}	
		
        IJ.log(document.toSBML());
	}

	/**
	 * Sets the annotation.
	 */
	private void setAnnotation(){
		String id = "";
		try {
			id = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		}
		
		String annot = "This " + model.getId() + " model is created";
		
		if(!id.equals(""))
			annot = annot.concat(" by " + id.substring(0, id.indexOf(".")));
			
		Calendar date = new GregorianCalendar();
		annot = annot.concat(" in " + date.getTime());
		
//		document.setAnnotation(annot);
//		
//		model.setAnnotation("This model has been built using Spatial SBML Plugin created by Kaito Ii and Akira Funahashi "
//				+ "from Funahashi Lab. Keio University, Japan with substantial contributions from Kota Mashimo, Mitunori Ozeki, and Noriko Hiroi");
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	
}
