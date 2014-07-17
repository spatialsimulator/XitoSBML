/**
 * 
 */

import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.sbml.libsbml.libsbml;

/**
 * @author Akira Funahashi
 * 
 */
public class Spatial_SBML implements PlugInFilter {
	ImagePlus imp;
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";
	ArrayList<Integer> labelList;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;

	@Override
	public void run(ImageProcessor ip) {
		byte[] pixels = (byte[])ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        labelList = new ArrayList<Integer>();
        hashDomainTypes = new HashMap<String, Integer>();
        hashSampledValue = new HashMap<String, Integer>();
        String s = "";
        for(int i = 0; i < pixels.length; i++) {
        	if (!hasLabel(pixels[i])) {
        		labelList.add(new Integer(pixels[i]));
        	}
        	s += pixels[i] + ",";
        	if (i % width == width -1) {
        		s += "\n";
        	}
        }
        Collections.sort(labelList);
        IJ.log(labelList.toString());
        for(Integer i : labelList) {
        	if (i == 0) {
        		hashSampledValue.put("EC", 0);
        		hashDomainTypes.put("EC", 3);
        	} else if (i == 1) {
        		hashSampledValue.put("Cyt", 1);
        		hashDomainTypes.put("Cyt", 3);
        		hashDomainTypes.put("Cyt_EC_membrane", 2);
        	} else if (i == 5) {
        		hashSampledValue.put("Nuc", 5);
        		hashDomainTypes.put("Nuc", 3);
        		hashDomainTypes.put("Cyt_Nuc_membrane", 2);
        	}
        }
        RawSpatialImage ri = new RawSpatialImage(pixels, width, height, 1, hashDomainTypes, hashSampledValue);
        SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(hashDomainTypes, ri);
        sbmlexp.createGeometryElements();
  		IJ.log(sbmlexp.document.getModel().getId());
	  	libsbml.writeSBMLToFile(sbmlexp.document, "out.xml");
        IJ.log(s);
        IJ.log(sbmlexp.document.toSBML());
	}
	
	public boolean hasLabel(int label) {
		for(Integer i : labelList) {
			if (i.intValue() == label) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (arg.equals("about")) {
            showAbout();
            return DONE;
        }
		return DOES_8C + DOES_8G;
	}
	
	public void showAbout() {
		IJ.showMessage("Spatial SBML Exporter",
				"This plugin is just a demonstration for SBML and its Spatial Extension.\n"
						+ "Implemented by Akira Funahashi");
	}
}
