import java.awt.Frame;
import java.util.Iterator;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;

import ij.IJ;
import ij.ImageJ;
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
	Image3DUniverse univ;
	
	@Override
	public void run(String arg) {
		Frame[] f =ImageJ.getFrames();
		Frame frame = null;
		for(int i = 0 ; i < f.length ; i++){
			if(f[i].getTitle().equals("ImageJ 3D Viewer")) frame = f[i];
		}
		if(frame == null){
			IJ.error("3D Viewer not opend");
			return;
		}
		ImageWindow3D win = (ImageWindow3D) frame;
		univ = (Image3DUniverse) win.getUniverse();
		
		createSBMLDoc();
		//SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
		//sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
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
	
	void getUniv(){
	
	}
	
}
