
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
import java.util.concurrent.TimeUnit;

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
	static ArrayList<ArrayList<Integer>> adjacentsList;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	HashMap<String,Integer> hashDomainNum;
	HashMap<Integer,Integer> hashLabelNum;
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
        System.out.println(s);

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
				if(matrix[i * width + j] == 0 && pixels[i * width + j] != 0){
					label = num.get(unsignedToBytes(pixels[i * width + j]));
					matrix[i * width + j] = label;
					recurs(i,j);
					label++;
					num.remove(unsignedToBytes(pixels[i * width + j]));
					num.put( unsignedToBytes(pixels[i * width + j]),label);
				}
			}
		}
		num.remove(0);
		num.put(0, 1);		//assumes extracellular is only one
		System.out.println("matrix");
		for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
				System.out.print(matrix[i * width + j] + ",");
			}
			System.out.println();
		}

		//count number of domains in each domaintype
		hashLabelNum = new HashMap<Integer,Integer>();
		System.out.println("domain");
		for(int i = 0 ; i < labelList.size() ; i++){
			hashLabelNum.put(labelList.get(i), num.get(labelList.get(i)) % 10);
			Integer temp = num.get(labelList.get(i)) % 10;
			System.out.println(labelList.get(i).toString() + " " + temp.toString());
		}

        //displays gui table to name domain types

		NamePanel name = new NamePanel(labelList, hashLabelNum, hashDomainTypes, hashSampledValue);
		while (name.running) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} // need a better solution to make this program wait
			catch (InterruptedException e) {
			}
		}
		try {
			hashDomainTypes.toString();  // check for nullpointers
		} catch (NullPointerException e) {
			IJ.log("Domaintype name error");
			System.exit(1); // when domaintype namer has canceled exit
		}

		hashDomainNum = new HashMap<String,Integer>();
		for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			hashDomainNum.put(e.getKey(), hashLabelNum.get(hashSampledValue.get(e.getKey())));
		}

        adjacentsList = new ArrayList<ArrayList<Integer>>();	//holds which pixels are adjacents

        //adds the membrane 					may need changes in the future
		for(int i = 0 ; i < height - 1; i++){
			for(int j = 0 ; j < width - 1; j++){
				//right
				if(matrix[i * width + j] != matrix[i * width + j + 1] && !hasLabel(Math.max(matrix[i * width + j + 1], matrix[i * width + j]), Math.min(matrix[i * width + j + 1], matrix[i * width + j]))){
			        ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(Math.max(matrix[i * width + j + 1], matrix[i * width + j]));
					temp.add(Math.min(matrix[i * width + j + 1], matrix[i * width + j]));
					adjacentsList.add(temp);
					addmembrane(
							Math.max(unsignedToBytes(pixels[i * width + j + 1]),unsignedToBytes(pixels[i * width + j])),
							Math.min(unsignedToBytes(pixels[i * width + j + 1]),unsignedToBytes(pixels[i * width + j]))
							);
				}
				//down
				if(matrix[i * width + j] != matrix[(i+1) * width + j] && !hasLabel(Math.max(matrix[i * width + j], matrix[(i+1) * width + j]), Math.min(matrix[i * width + j], matrix[(i+1) * width + j]))){
			        ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(Math.max(matrix[(i+1) * width + j], matrix[i * width + j]));
					temp.add(Math.min(matrix[(i+1) * width + j], matrix[i * width + j]));
					adjacentsList.add(temp);
					addmembrane(
							Math.max(unsignedToBytes(pixels[(i+1) * width + j]),unsignedToBytes(pixels[i * width + j])),
							Math.min(unsignedToBytes(pixels[(i+1) * width + j]),unsignedToBytes(pixels[i * width + j]))
							);
				}
			}
		}

        //show graph
		graph graph = new graph();

		for (Entry<String, Integer> e : hashDomainNum.entrySet()) {
			for (int i = 0; i < e.getValue(); i++) {
				if(!e.getKey().contains("membrane")){
					graph.addVertex(e.getKey() + i);
					System.out.println("Vertex " + e.getKey() + i);
				}
			}
		}

		for(ArrayList<Integer> a : adjacentsList){
			String edge1 = new String();
			String edge2 = new String();
			for(Entry<String, Integer> e : hashSampledValue.entrySet()){
				if(e.getValue().equals(labelList.get(a.get(0)/10))){
					System.out.println(e.getKey() + (a.get(0) % 10));
					edge1 = e.getKey() + (a.get(0) % 10);
				}
				if(e.getValue().equals(labelList.get(a.get(1)/10))){
					System.out.println(e.getKey() + (a.get(1) % 10));
					edge2 = e.getKey() + (a.get(1) % 10);
				}
			}
			graph.addEdge(edge1,edge2);
		}
		graph.visualize();

        RawSpatialImage ri = new RawSpatialImage(pixels, width, height, depth, hashDomainTypes, hashSampledValue, hashDomainNum, adjacentsList);
        SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);                                 //calls sbmlexporter and create sbml document with string s
        sbmlexp.createGeometryElements();

		//save document  obtains the name of Model as well as the document name
		SaveDialog sd = new SaveDialog("","",".xml");
		try{
		sbmlexp.document.getModel().setId(sd.getFileName().substring(0, sd.getFileName().lastIndexOf(".")));
		IJ.log(sd.getFileName());
		libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + sd.getFileName());                             //write SBML document to xml filec
		}catch(NullPointerException e){
			System.exit(1);
		}
		IJ.log(s);
        IJ.log(sbmlexp.document.toSBML());
        IJ.log(labelList.toString());
	}

	//determine if the pixel value is stored in labellist
	public boolean hasLabel(int label) {
		for(Integer i : labelList) {
			if (i.intValue() == label) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasLabel(int dom1, int dom2) {
		try{
			for (ArrayList<Integer> i : adjacentsList) {
				if (i.get(0) == dom1 && i.get(1) == dom2) {
					return true;
				}
			}
		}catch(IndexOutOfBoundsException e){

		}
		return false;
	}

	public static int unsignedToBytes(byte b) {
	    return b & 0xFF;
	  }

	  //adds the membrane domain
	public void addmembrane(Integer bignum, Integer smallnum){
		String big = new String();
		String small = new String();
		for(Entry<String,Integer> e : hashSampledValue.entrySet()){
			if(e.getValue().equals(bignum)){
				big = e.getKey();
			}
			if(e.getValue().equals(smallnum)){
				small = e.getKey();
			}
		}
		String buf = big + "_" + small + "_membrane";
		if(!hashDomainTypes.containsKey(buf)){
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	public void recurs(int i, int j){
		//check right
		if(j != width - 1 && pixels[i * width + j + 1] == pixels[i * width + j] && matrix[i * width + j + 1] == 0){
			matrix[i * width + j + 1] = matrix[i * width + j];
			recurs(i,j+1);
		}

		//check left
		if(j != 0 && pixels[i * width + j - 1] == pixels[i * width + j] && matrix[i * width + j - 1] == 0){
			matrix[i * width + j - 1] = matrix[i * width + j];
			recurs(i,j-1);
		}

		//check down
		if(i != height - 1 && pixels[(i+1) * width + j] == pixels[i * width + j] && matrix[(i+1) * width + j] == 0){
			matrix[(i + 1) * width + j] = matrix[i * width + j];
			recurs(i+1,j);
		}

		//check up
		if(i != 0 && pixels[(i-1) * width + j ] == pixels[i * width + j] && matrix[(i-1) * width + j] == 0){
			matrix[(i - 1) * width + j] = matrix[i * width + j];
			recurs(i-1,j);
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
