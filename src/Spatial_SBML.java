
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
import java.util.Stack;
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
    int count = 0;
    
	@Override
	public void run(ImageProcessor ip) {                           //process 2d pixel data

		//check for jgraph
		if(!checkJgraph()){
			System.exit(1);
		}
		
		pixels = (byte[])ip.getPixels(); 					//obtain pixels of image
        width = ip.getWidth();                                //obtain width of image
        height = ip.getHeight();                              //obtain height of image
        depth = 1;
        System.out.println("z " + depth);
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
        for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
					matrix[i * width + j] = 0;
			}
		}
      
        System.out.println("Labeling ...");
        
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
					num.put(unsignedToBytes(pixels[i * width + j]),label);
				}
			}
		}
		
		num.remove(0);
		num.put(0, 1);		//assumes extracellular is only one
/*
		for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
				if(matrix[i * width + j] == 0 && pixels[i * width + j] == 0){
					label = num.get(unsignedToBytes(pixels[i * width + j]));
					matrix[i * width + j] = label;
					recurs(i,j);
					label++;
					num.remove(unsignedToBytes(pixels[i * width + j]));
					num.put(unsignedToBytes(pixels[i * width + j]),label);
				}
			}
		}
*/		
		//count number of domains in each domaintype
		System.out.println("calculating number of domains ...");
		
		hashLabelNum = new HashMap<Integer,Integer>();
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
		
		DomNameCheck();
		
		hashDomainNum = new HashMap<String,Integer>();
		for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			hashDomainNum.put(e.getKey(), hashLabelNum.get(hashSampledValue.get(e.getKey())));
		}

		System.out.println("Adding membranes ...");
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
							Math.max(matrix[i * width + j + 1], matrix[i * width + j]),
							Math.min(matrix[i * width + j + 1],matrix[i * width + j])
							);
				}
				//down
				if(matrix[i * width + j] != matrix[(i+1) * width + j] && !hasLabel(Math.max(matrix[i * width + j], matrix[(i+1) * width + j]), Math.min(matrix[i * width + j], matrix[(i+1) * width + j]))){
			        ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(Math.max(matrix[(i+1) * width + j], matrix[i * width + j]));
					temp.add(Math.min(matrix[(i+1) * width + j], matrix[i * width + j]));
					adjacentsList.add(temp);
					addmembrane(
							Math.max(matrix[(i + 1) * width + j ], matrix[i * width + j]),
							Math.min(matrix[(i + 1) * width + j ],matrix[i * width + j])
							);
				}
			}
		}

        //show graph
		System.out.println("Displaying hiearchal graph");
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
			System.out.println(a.get(0) + " " + a.get(1));
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

		System.out.println("Creating SBML document ...");
		
        RawSpatialImage ri = new RawSpatialImage(pixels, width, height, depth, hashDomainTypes, hashSampledValue, hashDomainNum, adjacentsList);
        SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);                                 //calls sbmlexporter and create sbml document with string s
        sbmlexp.createGeometryElements();

		//save document  obtains the name of Model as well as the document name
		SaveDialog sd = new SaveDialog("Save SBML Document","","");
		try{
		sbmlexp.document.getModel().setId(sd.getFileName());
		IJ.log(sd.getFileName());
		libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + sd.getFileName() + ".xml");                             //write SBML document to xml file
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
		IJ.log(s);
        IJ.log(sbmlexp.document.toSBML());
        IJ.log(labelList.toString());
        graph.close();
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
		if(adjacentsList.isEmpty()) return false;
		for (ArrayList<Integer> i : adjacentsList) {
			if (i.get(0) == dom1 && i.get(1) == dom2) {
				return true;
			}
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
			if(e.getValue().equals( labelList.get(bignum / 10) )){
				big = e.getKey() + bignum % 10;
				System.out.println(bignum + " big " + big);
			}
			if(e.getValue().equals(labelList.get(smallnum / 10))){
				small = e.getKey() + smallnum % 10;
				System.out.println(smallnum + " small " + small);
			}
		}
		String buf = big + "_" + small + "_membrane";
		System.out.println(buf);
		if(!hashDomainTypes.containsKey(buf)){
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	public void recurs(int i, int j){
		Stack<Integer> block = new Stack<Integer>();
		block.push(i);
		block.push(j);
		
		while(!block.isEmpty()){
			j = block.pop();
			i = block.pop();
			if(j != width - 1 && pixels[i * width + j + 1] == pixels[i * width + j] && matrix[i * width + j + 1] == 0){
				matrix[i * width + j + 1] = matrix[i * width + j];
				block.push(i);
				block.push(j+1);
			}

			//check left
			if(j != 0 && pixels[i * width + j - 1] == pixels[i * width + j] && matrix[i * width + j - 1] == 0){
				matrix[i * width + j - 1] = matrix[i * width + j];
				block.push(i);
				block.push(j-1);
			}

			//check down
			if(i != height - 1 && pixels[(i+1) * width + j] == pixels[i * width + j] && matrix[(i+1) * width + j] == 0){
				matrix[(i + 1) * width + j] = matrix[i * width + j];
				block.push(i + 1);
				block.push(j);
			}

			//check up

			if(i != 0 && pixels[(i-1) * width + j ] == pixels[i * width + j] && matrix[(i-1) * width + j] == 0){
				matrix[(i - 1) * width + j] = matrix[i * width + j];
				block.push(i -1 );
				block.push(j);
			}
		}

	}

	public void DomNameCheck(){
		if(hashDomainTypes.containsKey("")){
			IJ.error("DomainType has no name");
			System.exit(1);
		}
		
		//check number of domaintype
		if(hashDomainTypes.size() != labelList.size()){
			IJ.error("Duplicate DomainType name");
			System.exit(1);
		}
	}

	public boolean checkJgraph(){
		try {
			Class.forName("org.jgrapht.ListenableGraph");
			return true;
		} catch (ClassNotFoundException e1) {
			IJ.error("Please Install Jgrapht");
			return false;
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
