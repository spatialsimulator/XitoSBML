package jp.ac.keio.bio.fun.xitosbml;
import ij.plugin.PlugIn;


/**
 * The base class of this plugin (XitoSBML).
 *
 * It inherits from the ImageJ Plugin class.
 * This class has run method which will be able to launch from * 'Plugins' -&gt; 'XitoSBML'.
 * The methods implemented by this plugin are described in src/main/resources/plugins.config.
 * Date Created: Jun 17, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public abstract class Spatial_SBML implements PlugIn {
	
	/** Is XitoSBML running. */
	static boolean isRunning = false;
	
	/** Title string. */
	String title = "Export segmented image to Spatial SBML";
	
	/** Version info. */
	String version = "1.2.0";
	
  /**
   * Launch XitoSBML as ImageJ plugin.
   * The methods implemented by this plugin and their implementations are
   * described in src/main/resources/plugins.config.
   *
   * @param arg name of the method defined in plugins.config
   */
	@Override
	public abstract void run(String arg);
}
