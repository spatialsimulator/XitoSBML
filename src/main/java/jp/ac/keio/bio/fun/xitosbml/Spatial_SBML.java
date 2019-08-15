package jp.ac.keio.bio.fun.xitosbml;
import ij.plugin.PlugIn;


/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 * 
 * The base class of this plugin (XitoSBML).
 * It inherits from the ImageJ Plugin class.
 * The methods implemented by this plugin and their implementations are
 * described in src/main/resources/plugins.config.
 */
public abstract class Spatial_SBML implements PlugIn {
	
	/** Is XitoSBML running. */
	static boolean isRunning = false;
	
	/** Title string. */
	String title = "Export segmented image to Spatial SBML";
	
	/** Version info. */
	String version = "1.1.0";
	
  /**
   * Launch XitoSBML as ImageJ plugin.
   * The methods implemented by this plugin and their implementations are
   * described in src/main/resources/plugins.config.
   */
	@Override
	public abstract void run(String arg);
}
