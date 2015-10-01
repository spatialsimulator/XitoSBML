package sbmlplugin;
import sbmlplugin.util.PluginConstants;
import ij.IJ;
import ij.plugin.PlugIn;


/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public abstract class Spatial_SBML implements PlugIn {
	static boolean isRunning = false;
	String title = "Export segmented image to Spatial SBML";
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);

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
        if(version != null && Float.parseFloat(version) >= PluginConstants.VIEWERVERSION)
                return true;
        IJ.error("Please Update 3D Viewer");
        return false;
	}
}
