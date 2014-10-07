
/**
 *
 */
//imageJ package
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij3d.Content;
import ij3d.ContentInstant;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import math3d.Point3d;

import org.sbml.libsbml.libsbml;

import vib.InterpolatedImage;
import voltex.VoltexGroup;


/**
 * @author Akira Funahashi
 *
 */
public class Spatial_SBML implements PlugIn {
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";
	ArrayList<Integer> labelList;
	static ArrayList<ArrayList<Integer>> adjacentsPixel;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	HashMap<String,Integer> hashDomainNum;
	HashMap<Integer,Integer> hashLabelNum;
	ArrayList<ArrayList<String>> adjacentsList;
    int width;
    int height;
    int depth;
    byte[] pixels;
    static int matrix[];

	public void run(String args) {                           //process 2d pixel data
		//check for jgraph
		if(!checkJgraph()){
			System.exit(1);
		}

		ImagePlus image = WindowManager.getCurrentImage();
        width = image.getWidth();                                //obtain width of image
        height = image.getHeight();                              //obtain height of image
        depth = image.getStackSize();								//obtain number of slices
        labelList = new ArrayList<Integer>();					//value of pixels of domains
        hashDomainTypes = new HashMap<String, Integer>();
        hashSampledValue = new HashMap<String, Integer>();
        byte[] slice;
        pixels = new byte[width * height * depth];
        System.out.println("w:" + width + " h:"+ height + " d:" + depth);
        // String s = "";
        for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])image.getStack().getPixels(i); 					//obtain pixels of the first stack image
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
        }

        int max = depth * height * width;
        int temps;
			for (int i = 0 ; i < max ; i++) {
				temps = unsignedToBytes(pixels[i]);
				if (!hasLabel(temps)) {					 // see below
					labelList.add(new Integer(temps));
				}
				/*
				 * s += temps + ","; //organize pixel value in a string
				 * if (i % width == width -1) { s += "\n"; }
				 */
			}
        Collections.sort(labelList);                            //sort label list
        IJ.log(labelList.toString());                           //append labelList to logPanel which is a textpanel in IJ.java

        //count number of objects with certain pixel value
        matrix = new int[height * width * depth];		//identical size of matrix with image

        System.out.println("Labeling ...");

        HashMap<Integer,Integer> num = new HashMap<Integer,Integer>();  //labels the object in a different number
        int label = 0;
        max = labelList.size();
        for(int i = 0 ; i < max ; i++){
        	num.put(labelList.get(i), label);
        	label += 10;
        }

		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (matrix[d * height *  width + i * width + j] == 0 && pixels[d * height *  width + i * width + j] != 0) {
						label = num.get(unsignedToBytes(pixels[d * height *  width + i * width + j]));
						matrix[d * height *  width + i * width + j] = label;
						recurs(i, j, d);
						num.remove(unsignedToBytes(pixels[d * height *  width + i * width + j]));
						num.put(unsignedToBytes(pixels[d * height *  width + i * width + j]), ++label);
					}
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
		//int temp;
		hashLabelNum = new HashMap<Integer,Integer>();
		for(int i = 0 ; i < max ; i++){
			hashLabelNum.put(labelList.get(i), num.get(labelList.get(i)) % 10);
			temps = num.get(labelList.get(i)) % 10;
			System.out.println(labelList.get(i).toString() + " " + temps);
		}

        //displays gui table to name domain types

		NamePanel panel = new NamePanel(labelList, hashLabelNum, hashDomainTypes, hashSampledValue);
		while (panel.running) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} // need a better solution to make this program wait
			catch (InterruptedException e) {
			}
		}

		hashDomainNum = new HashMap<String,Integer>();
		for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			hashDomainNum.put(e.getKey(), hashLabelNum.get(hashSampledValue.get(e.getKey())));
		}

		System.out.println("Adding membranes ...");
        adjacentsPixel= new ArrayList<ArrayList<Integer>>();	//holds which pixels are adjacents
        adjacentsList = new ArrayList<ArrayList<String>>();
        //adds the membrane 					may need changes in the future
		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height - 1; i++) {
				for (int j = 0; j < width - 1; j++) {
					// right
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + i * width + j + 1)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmembrane(
								Math.max(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
					}

					// down
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + (i + 1) * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmembrane(
								Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
					}
					
					//above
					if ( d != depth -1 && checkAdjacent(d * height * width + i * width + j, (d + 1) * height * width + i * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmembrane(
								Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
					}
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
				//	System.out.println("Vertex " + e.getKey() + i);
				}
			}
		}

		for(ArrayList<Integer> a : adjacentsPixel){
			String edge1 = new String();
			String edge2 = new String();
			for(Entry<String, Integer> e : hashSampledValue.entrySet()){
				if(e.getValue().equals(labelList.get(a.get(0)/10))){
					edge1 = e.getKey() + (a.get(0) % 10);
				}
				if(e.getValue().equals(labelList.get(a.get(1)/10))){
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
		SaveDialog sd = new SaveDialog("Save SBML Document",image.getTitle(),".xml");
		String name = sd.getFileName();
		sbmlexp.document.getModel().setId(name);
		IJ.log(name);
		try{
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);                             //write SBML document to xml file
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
//		IJ.log(pixels.toString()); //print matrix
        IJ.log(sbmlexp.document.toSBML());
        IJ.log(labelList.toString());
        //univ.close();
        //graph.close();
	}

	//determine if the pixel value is stored in labellist
	private boolean hasLabel(int label) {
		for(Integer i : labelList) {
			if (i.equals(label)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasLabel(int dom1, int dom2) {
		if(adjacentsPixel.isEmpty()) return false;
		
		for (ArrayList<Integer> i : adjacentsPixel) {
			if (i.get(0) == dom1 && i.get(1) == dom2) {
				return true;
			}
		}
		return false;
	}

	private static int unsignedToBytes(byte b) {
	    return b & 0xFF;
	  }


	private static boolean checkAdjacent(int org, int next){
		if(matrix[org] != matrix[next] && !hasLabel(Math.max(matrix[next], matrix[org]), Math.min(matrix[next], matrix[org]))){
			return true;
		} else {
			return false;
		}
	}

	  //adds the membrane domain
	public void addmembrane(Integer bignum, Integer smallnum){
		String big = null,small = null;

		for(Entry<String,Integer> e : hashSampledValue.entrySet()){
			if(e.getValue().equals( labelList.get(bignum / 10) )){
				big = e.getKey();
			}
			if(e.getValue().equals(labelList.get(smallnum / 10))){
				small = e.getKey();
			}
		}
		String buf = big + "_" + small + "_membrane";

		ArrayList<String> adjacentDom = new ArrayList<String>();
		adjacentDom.add(big + bignum % 10);
		adjacentDom.add(small + smallnum % 10);
		adjacentsList.add(adjacentDom);

		if(!hashDomainTypes.containsKey(buf)){
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	private void recurs(int i, int j, int d){
		Stack<Integer> block = new Stack<Integer>();
		block.push(i);
		block.push(j);
		block.push(d);

		while(!block.isEmpty()){
			d = block.pop();
			j = block.pop();
			i = block.pop();

			//check right
			if(j != width - 1 && pixels[d * height * width + i * width + j + 1] == pixels[d * height * width + i * width + j] && matrix[d * height * width + i * width + j + 1] == 0){
				matrix[d * height * width + i * width + j + 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j+1);
				block.push(d);
			}

			//check left
			if(j != 0 && pixels[d * height * width + i * width + j - 1] == pixels[d * height * width + i * width + j] && matrix[d * height * width + i * width + j - 1] == 0){
				matrix[d * height * width + i * width + j - 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j-1);
				block.push(d);
			}

			//check down
			if(i != height - 1 && pixels[d * height * width + (i+1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i+1) * width + j] == 0){
				matrix[d * height * width + (i + 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i+1);
				block.push(j);
				block.push(d);
			}

			//check up
			if(i != 0 && pixels[d * height * width + (i-1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i-1) * width + j] == 0){
				matrix[d * height * width + (i - 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i-1);
				block.push(j);
				block.push(d);
			}
			
			//check above
			if(d != depth - 1 && pixels[d * height * width + i * width + j] == pixels[(d+1) * height * width + i * width + j] && matrix[(d+1) * height * width + i * width + j] == 0){
				matrix[(d+1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d+1);
			}
			
			//check below
			if(d != 0 && pixels[d * height * width + i * width + j] == pixels[(d-1) * height * width + i * width + j] && matrix[(d-1) * height * width + i * width + j] == 0){
				matrix[(d-1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d-1);
			}
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
/*
	@Override
	public int setup(String arg, ImagePlus imp) {                          //return flags specifying capability and needs of filter
		this.imp = imp;
		if (arg.equals("about")) {                    //args Defines type of filter operation
            showAbout();
            return DONE;
        }
		return DOES_8C + DOES_8G;                          //return flag word that specifies the filters capabilities
	}
*/
	public void showAbout() {
		IJ.showMessage("Spatial SBML Exporter",
				"This plugin is just a demonstration for SBML and its Spatial Extension.\n"
						+ "Implemented by Akira Funahashi");
	}

}
