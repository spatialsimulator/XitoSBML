package sbmlplugin.util;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Sep 5, 2015
 */
public class PluginUtil {

	public static int unsignedToBytes(byte b) {
		  return b & 0xFF;
	  }

}
