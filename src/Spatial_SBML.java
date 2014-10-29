
/**
 *
 */
//imageJ package
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

import java.util.ArrayList;


/**
 * @author Akira Funahashi
 *
 */
public class Spatial_SBML implements PlugIn {
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";
	ArrayList<Integer> labelList;
    int width;
    int height;
    int depth;
    
	public void run(String args) {   
		ImagePlus image = WindowManager.getCurrentImage();
		
		if (checkJgraph() && checkFormat(image)) {
			width = image.getWidth(); // obtain width of image
			height = image.getHeight(); // obtain height of image
			depth = image.getStackSize(); // obtain number of slices
			IJ.log("w: " + width + " h: " + height + " d: " + depth);
			new mainSpatial().run(args);
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
	
	public boolean checkFormat(ImagePlus image){
		if(image.getBitDepth() == 8)
				return true;
				
		IJ.error("Image must be 8-bit grayscale");
		return false;
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
