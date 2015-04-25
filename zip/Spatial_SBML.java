

import ij.IJ;
import ij.plugin.PlugIn;


/**
 * @author Akira Funahashi
 *
 */
public class Spatial_SBML implements PlugIn {
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";

	static {
		try{
			System.loadLibrary("sbmlj");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run(String args) {   
		if(/*checkJgraph() &&*/ check3Dviewer()) 
			new MainSpatial().run(args);	
	}

	public boolean checkJgraph(){
		try {
			Class.forName("org.jgrapht");
			return true;
		} catch (ClassNotFoundException e1) {
			IJ.error("Please Install Jgrapht");
			return false;
		}
	}
	
	public boolean check3Dviewer(){
		String version = ij3d.Install_J3D.getJava3DVersion();
        System.out.println("version = " + version);
        if(version != null && Float.parseFloat(version) >= 1.5)
                return true;
        IJ.error("Please Update 3D Viewer");
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
