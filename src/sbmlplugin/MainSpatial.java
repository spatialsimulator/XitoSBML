package sbmlplugin;
import gui.ParamAndSpecies;
import ij.IJ;
import ij.ImagePlus;
import ij.io.SaveDialog;
import image.CreateImage;
import image.Fill;
import image.ImageEdit;
import image.ImageExplorer;
import image.Interpolate;
import image.SpatialImage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ReqSBasePlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;
import org.sbml.libsbml.libsbml;

import visual.Viewer;


public abstract class MainSpatial{

	SBMLDocument document;
	Model model;
	SBMLNamespaces sbmlns; 
	SpatialPkgNamespaces spatialns;
	SpatialModelPlugin spatialplugin;
	ReqSBasePlugin reqplugin;
	private ImageExplorer imgexp;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValue;
	Viewer viewer;
	SpatialImage spImg;
	
	public void createSBMLDoc(){
		sbmlns = new SBMLNamespaces(3, 1); // create SBML name space with level 3 version 1
		sbmlns.addPackageNamespace("req", 1); // add required element package
		sbmlns.addPackageNamespace("spatial", 1); // add spatial processes package
		// SBML Document
		document = new SBMLDocument(sbmlns); // construct document with namespace
		document.setPackageRequired("req", true); // set req package as required
		document.setPackageRequired("spatial", true); // set spatial package as required
		model = document.createModel(); // create model using the document and return pointer
				
		// Create Spatial
		//
		// set the SpatialPkgNamespaces for Level 3 Version 1 Spatial Version 1
		//
		spatialns = new SpatialPkgNamespaces(3, 1, 1); // create spatial package
														// name space
		//
		// Get a SpatialModelPlugin object plugged in the model object.
		//
		// The type of the returned value of SBase::getPlugin() function is
		// SBasePlugin, and
		// thus the value needs to be casted for the corresponding derived
		// class.
		//
		//reqplugin = (ReqSBasePlugin) model.getPlugin("req"); // get required element plugin

		SBasePlugin basePlugin = (model.getPlugin("spatial"));
		spatialplugin = (SpatialModelPlugin) basePlugin; // get spatial plugin
		if (spatialplugin == null) {
			IJ.error("[Fatal Error] Layout Extension Level "
					+ spatialns.getLevel() + " Version "
					+ spatialns.getVersion() + " package version "
					+ spatialns.getPackageVersion() + " is not registered.");
			System.exit(1);
		}

	}
	
	public void gui() {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer> ();
		imgexp = new ImageExplorer(hashDomainTypes,hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					
				}
			}
		}
	}
	
	public void computeImg(){
		Interpolate interpolate = new Interpolate();
		HashMap<String, ImagePlus> hashDomFile = imgexp.getDomFile();
		interpolate.interpolate(hashDomFile);
		Fill fill = new Fill();
		//fill each images
		for(Entry<String, ImagePlus> e : hashDomFile.entrySet())
			hashDomFile.put(e.getKey(), fill.fill(e.getValue()));
			
		CreateImage creIm = new CreateImage(imgexp.getDomFile(), hashSampledValue);
		spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		spImg.setImage(fill.fill(spImg));
		new ImageEdit(spImg);
	}
	
	public void visualize (SpatialImage spImg){
		viewer = new Viewer();
		viewer.view(spImg);
	}
	
	public void addParaAndSpecies(){
		ListOfParameters lop = model.getListOfParameters();
		ListOfSpecies los = model.getListOfSpecies();
		ParamAndSpecies ps = new ParamAndSpecies(model);
		
		while(lop.size() == 0 || los.size() == 0 || !ps.hasExited()){
			synchronized(lop){
				synchronized(los){
					
				}
			}
		}
	}
	
	public void save(SpatialSBMLExporter sbmlexp){
		SaveDialog sd = new SaveDialog("Save SBML Document", spImg.title, ".xml");
		String name = sd.getFileName();
		IJ.log(name);
		
		try{
			sbmlexp.document.getModel().setId(name);
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);  
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 			
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
		
		setAnnotation();
        IJ.log(sbmlexp.document.toSBML());
	}
	
	private void setAnnotation(){
		String id = "";
		try {
			id = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			
		}
		
		String annot = "This " + model.getId() + " model is created";
		
		if(!id.equals(""))
			annot = annot.concat(" by " + id.substring(0, id.indexOf(".")));
		
		
		Calendar date = new GregorianCalendar();
		annot = annot.concat(" in " + date.getTime());
		
		document.setAnnotation(annot);
		
		model.setAnnotation("This model has been built using Spatial SBML Plugin created by Kaito Ii and Akira Funahashi "
				+ "from Funahashi Lab. Keio University, Japan with substantial contributions from Kota Mashimo, Mitunori Ozeki, and Noriko Hiroi");

	}
}
	

