
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
import java.util.Map.Entry;

import org.sbml.libsbml.SampledVolume;
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
	HashMap<Integer,Integer> hashDomainNum;
    int width;
    int height;
    int depth;
    byte[] pixels; 
    int matrix[];
	
	@Override
	public void run(ImageProcessor ip) {                           //process 2d pixel data

		pixels = (byte[])ip.getPixels(); 					//obtain pixels of image
        width = ip.getWidth();                                //obtain width of image
        height = ip.getHeight();                              //obtain height of image
        depth = 1;
        labelList = new ArrayList<Integer>();					//value of pixels of domains
        hashDomainTypes = new HashMap<String, Integer>();
        hashSampledValue = new HashMap<String, Integer>();
        String s = "";
        
        
        for(int i = 0; i < pixels.length; i++) {
        	if (!hasLabel(unsignedToBytes(pixels[i]))) {                                                              //see below
        		labelList.add(new Integer(unsignedToBytes(pixels[i])));
        	}
        	s += unsignedToBytes(pixels[i]) + ",";                                //organize pixel value in a string
        	if (i % width == width -1) {
        		s += "\n";	
        	}
        }
        Collections.sort(labelList);                            //sort label list
        IJ.log(labelList.toString());                           //append labelList to logPanel which is a textpanel in IJ.java

        //count number of objects with certain pixel value
        matrix = new int[height*width];		//identical size of matrix with image
        matrix[0] = 0;
        HashMap<Integer,Integer> num = new HashMap<Integer,Integer>();  //labels the object in a different number
        int label = 0;
        for(int i = 0 ; i < labelList.size() ; i++){
        	num.put(labelList.get(i), label);
        	label += 10;
        }
		for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
				if(matrix[i * height + j] == 0 && pixels[i * height + j] != 0){
					int var = num.get(unsignedToBytes(pixels[i * height + j]));
					matrix[i * height + j] = var;
					recurs(i,j,width,height);
					var++;
					num.remove(unsignedToBytes(pixels[i * height + j]));
					num.put( unsignedToBytes(pixels[i * height + j]),var);
				}        
			}
		}

		//count number of domains in each domaintype
		hashDomainNum = new HashMap<Integer,Integer>();
		System.out.println("domain");
		for(int i = 0 ; i < labelList.size() ; i++){
			hashDomainNum.put(labelList.get(i), num.get(labelList.get(i)) % 10);
			Integer temp = num.get(labelList.get(i)) % 10;
			System.out.println(labelList.get(i).toString() + " " + temp.toString());
		}
		
		
        /*
        NamePanel name = new NamePanel(labelList,countDomain);
        hashDomainTypes = name.getDomainTypes();
        hashSampledValue = name.getSampledValue(); 
        
         while(!name.exited);
         */ 
        
        // this should be deleted
        
        Integer thresEC = labelList.get(0),thresCyt = labelList.get(1), thresNuc = labelList.get(2);									//value which determines the threshold of nucleus and cytosol
        for(Integer i : labelList) {                            //for each labelList add domain data
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
        
        /*
        //graph
        graph graph = new graph();
        for (Entry<String, Integer> e : hashSampledValue.entrySet()) {
        	graph.addVertex(e.getKey());
        }
        
        
        graph.visualize();
        */
        
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

	public void recurs(int i, int j, int width, int height){
		//check right
		if(j != width - 1 && pixels[i * height + j + 1]== pixels[i * height + j] && matrix[i * height + j + 1] == 0){
			matrix[i * height + j + 1] = matrix[i * height + j];
			recurs(i,j+1,width,height);
		}
		
		//check left
		if(j != 0 && pixels[i * height + j - 1] == pixels[i * height + j] && matrix[i * height + j - 1] == 0){
			matrix[i * height + j - 1] = matrix[i * height + j];
			recurs(i,j-1,width,height);
		}
		
		//check down
		if(i != height - 1 && pixels[(i+1) * height + j] == pixels[i * height + j] && matrix[(i+1) * height + j] == 0){
			matrix[(i + 1) * height + j] = matrix[i * height + j];
			recurs(i+1,j,width,height);
		}
		
		//check up
		if(i != 0 && pixels[(i-1) * height + j ] == pixels[i * height + j] && matrix[(i-1) * height + j] == 0){
			matrix[(i - 1) * height + j] = matrix[i * height + j];
			recurs(i-1,j,width,height);
		}
		
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
