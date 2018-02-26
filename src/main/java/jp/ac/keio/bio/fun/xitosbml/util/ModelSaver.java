package jp.ac.keio.bio.fun.xitosbml.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.TidySBMLWriter;

import ij.IJ;
import ij.io.SaveDialog;
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
			
		IJ.log("filename = " + name);
		
		setAnnotation();
		
		try {		
			if(name.contains(".xml"))	
				name = name.substring(0, name.indexOf('.'));
			document.getModel().setId(name);
			SBMLWriter.write(document, new File(path + "/" + name + ".xml"), ' ', (short) 2); 
		} catch(NullPointerException e) {
			System.out.println("SBML document was not saved");
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		try {
			String docStr = new TidySBMLWriter().writeSBMLToString(document);
	        IJ.log(docStr);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

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
		IJ.log("hostname = " + id);
		
		String annot = "This " + model.getId() + " model is created";
		
		if(!id.equals("")) {
		  if (id.contains(".")) {
		    id = id.substring(0,  id.indexOf("."));
		  }
			annot = annot.concat(" by " + id);
		}
			
		Calendar date = new GregorianCalendar();
		annot = annot.concat(" in " + date.getTime());
		

		try {
			document.setNotes(annot);
			model.setNotes("This model has been built using XitoSBML-" + this.getClass().getPackage().getImplementationVersion() + " implemented by Kaito Ii and Akira Funahashi "
					+ "from Funahashi Lab. Keio University, Japan with substantial contributions from Kota Mashimo, Mitsunori Ozeki, and Noriko Hiroi");
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
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
