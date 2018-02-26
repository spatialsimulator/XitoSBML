package jp.ac.keio.bio.fun.xitosbml;
import ij.plugin.PlugIn;


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
	
	/** Version info. */
	String version = "1.1.0";
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);
}
