
/**
 *
 */
//imageJ package
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij3d.Image3DUniverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
