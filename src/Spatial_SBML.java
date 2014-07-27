
/**
 *
 */
//imageJ package
import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.sbml.libsbml.libsbml;

/**
 * @author Akira Funahashi
 *
 */
public class Spatial_SBML implements PlugInFilter {
	ImagePlus imp;                                         //contain ImageProcessor 2D image or imagestack
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";
	ArrayList<Integer> labelList;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;

	@Override
	public void run(ImageProcessor ip) {                           //process 2d pixel data

		byte[] pixels = (byte[])ip.getPixels(); 					//obtain pixels of image
        int width = ip.getWidth();                                //obtain width of image
        int height = ip.getHeight();                              //obtain height of image
        int depth = 1;
        labelList = new ArrayList<Integer>();					//value of pixels of domains
        hashDomainTypes = new HashMap<String, Integer>();
        hashSampledValue = new HashMap<String, Integer>();
        String s = "";
        
        for(int i = 0; i < pixels.length; i++) {
        	if (!hasLabel(unsignedToBytes(pixels[i]))) {                                                              //see below
        		labelList.add(new Integer(unsignedToBytes(pixels[i])));
        	}
        	s += pixels[i] + ",";                                //organize pixel value in a string
        	if (i % width == width -1) {
        		s += "\n";	
        	}
        }
        Collections.sort(labelList);                            //sort label list
        IJ.log(labelList.toString());                           //append labelList to logPanel which is a textpanel in IJ.java

        

        /*
        NamePanel name = new NamePanel(labelList);
        hashDomainTypes = name.getDomainTypes();
        hashSampledValue = name.getSampledValue(); 
        
         while(!name.exited);
         */ 
        
        // this should be deleted
        
        Integer thresEC = labelList.get(0),thresCyt = labelList.get(1), thresNuc = labelList.get(2);									//value which determines the threshold of nucleus and cytosol
        for(Integer i : labelList) {                            //for each labellist add domain data
        	if (i == thresEC) {
        		hashSampledValue.put("EC", thresEC);
        		hashDomainTypes.put("EC", 3);
        	} else if (i == thresCyt) {
        		hashSampledValue.put("Cyt", thresCyt);
        		hashDomainTypes.put("Cyt", 3);
        		hashDomainTypes.put("Cyt_EC_membrane", 2);
        	} else if (i == thresNuc) {
        		hashSampledValue.put("Nuc", thresNuc); 
        		hashDomainTypes.put("Nuc", 3);
        		hashDomainTypes.put("Cyt_Nuc_membrane", 2);
             }
        }

        RawSpatialImage ri = new RawSpatialImage(pixels, width, height, depth, hashDomainTypes, hashSampledValue);

        SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);                                 //calls sbmlexporter and create sbml document with string s

        sbmlexp.createGeometryElements();


		//save document
		SaveDialog sd = new SaveDialog("","",".xml");

		sbmlexp.document.getModel().setId(sd.getFileName().substring(0, sd.getFileName().lastIndexOf(".")));
		IJ.log(sd.getFileName());
		libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + sd.getFileName());                             //write SBML document to xml filec

 		IJ.log(s);
        IJ.log(sbmlexp.document.toSBML());
        IJ.log(labelList.toString());
	}

	public boolean hasLabel(int label) {
		for(Integer i : labelList) {
			if (i.intValue() == label) {
				return true;
			}
		}
		return false;
	}

	public static int unsignedToBytes(byte b) {
	    return b & 0xFF;
	  }

	@Override
	public int setup(String arg, ImagePlus imp) {                          //return flags specifying capability and needs of filter
		this.imp = imp;
		if (arg.equals("about")) {                    //args Defines type of filter operation
            showAbout();
            return DONE;
        }
		return DOES_8C + DOES_8G;                          //return flag word that specifies the filters capabilities
	}

	public void showAbout() {
		IJ.showMessage("Spatial SBML Exporter",
				"This plugin is just a demonstration for SBML and its Spatial Extension.\n"
						+ "Implemented by Akira Funahashi");
	}
}
