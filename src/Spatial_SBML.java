
/**
 *
 */
//imageJ package
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij3d.Content;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.sbml.libsbml.libsbml;



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
    double voxw;
    double voxy;
    double voxz;
    Image3DUniverse univ;
    
	public void run(String args) {   
		//check for jgraph
		if(!checkJgraph()){
			System.err.println("Need installation of jgraph");
		}

		ImagePlus image = WindowManager.getCurrentImage();
        width = image.getWidth();                                //obtain width of image
        height = image.getHeight();                              //obtain height of image
        depth = image.getStackSize();								//obtain number of slices
        new mainSpatial().run(args);
        /*
        hashDomainTypes = new HashMap<String, Integer>();
        hashSampledValue = new HashMap<String, Integer>();
        System.out.println("w:" + width + " h:"+ height + " d:" + depth);
        // String s = "";
        
        Interpolate interpolate = new Interpolate(image);	
        
        
        //display 3d image with 3d viewer
        univ = new Image3DUniverse();
        univ.show();
        univ.addVoltex(interpolate.getInterpolatedImage());
        
        imageEdit edit = new imageEdit(image);
        this.labelList= edit.labelList;
        this.hashLabelNum = edit.hashLabelNum;
        this.pixels = edit.pixels;
        this.matrix = edit.matrix;
        */
        //displays gui table to name domain types

//		NamePanel panel = new NamePanel(labelList, hashLabelNum, hashDomainTypes, hashSampledValue);
		/*
		while (panel.running) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} // need a better solution to make this program wait
			catch (InterruptedException e) {
			}
		}
		*/
		//gui();
		
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
		IJ.log(name);
		try{
			sbmlexp.document.getModel().setId(name);
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

	public synchronized void gui(){
		System.out.println("gui begin");
		new NamePanel(labelList, hashLabelNum, hashDomainTypes, hashSampledValue);
		try{
			System.out.println("gui wait");
			wait();
		}catch(InterruptedException e){
			
		}
		System.out.println("gui end");
		notifyAll();
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
