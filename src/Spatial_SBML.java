

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

		String varname;
		String shlibname;

		if (System.getProperty("os.name").startsWith("Mac OS")) {
			varname = "DYLD_LIBRARY_PATH"; // We're on a Mac.
			shlibname = "'libsbmlj.jnilib'";
		} else {
			varname = "LD_LIBRARY_PATH"; // We're not on a Mac.
			shlibname = "'libsbmlj.so' and/or 'libsbml.so'";
		}

		try {
			System.loadLibrary("sbmlj");
			// For extra safety, check that the jar file is in the classpath.
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (UnsatisfiedLinkError e) {
			IJ.error("Error encountered while attempting to load libSBML:");
			IJ.error("Please check the value of your " + varname
					+ " environment variable and/or"
					+ " your 'java.library.path' system property"
					+ " (depending on which one you are using) to"
					+ " make sure it list the directories needed to"
					+ " find the " + shlibname + " library file and the"
					+ " libraries it depends upon (e.g., the XML parser).");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			IJ.error("Error: unable to load the file 'libsbmlj.jar'."
					+ " It is likely that your -classpath command line "
					+ " setting or your CLASSPATH environment variable "
					+ " do not include the file 'libsbmlj.jar'.");
			e.printStackTrace();

			System.exit(1);
		} catch (SecurityException e) {
			IJ.error("Error encountered while attempting to load libSBML:");
			e.printStackTrace();
			IJ.error("Could not load the libSBML library files due to a"
					+ " security exception.\n");
			System.exit(1);
		}
	}
	
	public void run(String args) {   
		if(checkJgraph() && check3Dviewer()) 
			new MainSpatial().run(args);	
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
	
	public boolean check3Dviewer(){
		String version = ij3d.Install_J3D.getJava3DVersion();
        System.out.println("3D Viewer version = " + version);
        if(version != null && Float.parseFloat(version) >= 1.5)
                return true;
        IJ.error("Please Update 3D Viewer");
        return false;
	}
	
	public void showAbout() {
		IJ.showMessage("Spatial SBML Exporter",
				"This plugin is just a demonstration for SBML and its Spatial Extension.\n"
						+ "Implemented by Akira Funahashi");
	}

}
