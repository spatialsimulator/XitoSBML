package sbmlplugin;
import ij.IJ;
import ij.plugin.PlugIn;
import sbmlplugin.util.PluginConstants;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public abstract class Spatial_SBML implements PlugIn {
	
	/** The is running. */
	static boolean isRunning = false;
	
	/** The title. */
	String title = "Export segmented image to Spatial SBML";
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);

	/**
	 * Check jgraph.
	 *
	 * @return true, if successful
	 */
	public boolean checkJgraph(){
		try {
			Class.forName("org.jgrapht.ListenableGraph");
			return true;
		} catch (ClassNotFoundException e1) {
			IJ.error("Please Install Jgrapht");
			return false;
		}
	}
	
	/**
	 * Check 3 dviewer.
	 *
	 * @return true, if successful
	 */
	public boolean check3Dviewer(){
		String version = ij3d.Install_J3D.getJava3DVersion();
        if(version != null && Float.parseFloat(version) >= PluginConstants.VIEWERVERSION)
                return true;
        IJ.error("Please Update 3D Viewer");
        return false;
	}
}
