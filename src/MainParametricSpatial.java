import java.awt.Frame;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JOptionPane;

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

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij3d.Content;
import ij3d.Image3DUniverse;
import ij3d.ImageWindow3D;


public class MainParametricSpatial implements PlugIn{
	SBMLDocument document;
	Model model;
	SBMLNamespaces sbmlns;
	SpatialPkgNamespaces spatialns;
	SpatialModelPlugin spatialplugin;
	private Image3DUniverse univ;
	ReqSBasePlugin reqplugin;
	private ImagePlus image;
	private ImageExplorer imgexp;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValue;
	Viewer viewer;
	
	@Override
	public void run(String arg) {
		/*
		Frame[] f =ImageJ.getFrames();
		Frame frame = null;
		for(int i = 0 ; i < f.length ; i++){
			if(f[i].getTitle().equals("ImageJ 3D Viewer")) frame = f[i];
		}
		if(frame == null){
			IJ.error("3D Viewer not opened");
			return;
		}
		ImageWindow3D win = (ImageWindow3D) frame;
		univ = (Image3DUniverse) win.getUniverse();
		*/
		createSBMLDoc();
		gui();
		CreateImage creIm = new CreateImage(imgexp.getDomFile(), hashSampledValue, imgexp.getFileInfo());
		SpatialImage spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		new Interpolate(spImg);
		image = new Fill().fill(spImg);
		ImageEdit edit = new ImageEdit(spImg);
		//edit.checkImageBorder();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
		visualize(spImg);

		sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
		
		
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addParaAndSpecies();

		save(sbmlexp);
		//SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
	
	}

	public void createSBMLDoc(){
		sbmlns = new SBMLNamespaces(3, 1); // create SBML name space with level 3 version 1
		sbmlns.addPackageNamespace("req", 1); // add required element package
		sbmlns.addPackageNamespace("spatial", 1); // add spatial processes package
		// SBML Document
		document = new SBMLDocument(sbmlns); // construct document with namespace
		document.setPackageRequired("req", true); // set req package as required
		document.setPackageRequired("spatial", true); // set spatial package as
														// required
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
	
	public void visualize(ImagePlus img){
		univ = new Image3DUniverse();
		univ.show();
		Content c = univ.addVoltex(img);
		c.setTransparency(0.4f);
	}
	
	public void visualize (SpatialImage spImg){
		viewer = new Viewer();
		viewer.view(spImg);
	}
	
	public void addParaAndSpecies(){
		ListOfParameters lop = model.getListOfParameters();
		ListOfSpecies los = model.getListOfSpecies();
		ParamAndSpecies pas = new ParamAndSpecies(model);
		
		while(lop.size() == 0 || los.size() == 0 || !pas.wasExited()){
			synchronized(lop){
				synchronized(los){
					
				}
			}
		}
	}
	
	public void save(SpatialSBMLExporter sbmlexp){
		SaveDialog sd = new SaveDialog("Save SBML Document",image.getTitle(),".xml");
		String name = sd.getFileName();
		IJ.log(name);
		
		try{
			sbmlexp.document.getModel().setId(name);
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);  
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 			
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
        IJ.log(sbmlexp.document.toSBML());
	}
	
	void getUniv(){
	
	}
	
}
