package jp.ac.keio.bio.fun.xitosbml;

import ij.IJ;
import ij.ImageJ;
import jp.ac.keio.bio.fun.xitosbml.xitosbml.MainImgSpatial;

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 * 
 * The Class Spatial_Img_SBML, which calls "run Spatial Image SBML plugin" from ImageJ (XitoSBML).
 * This class is an implementation of XitoSBML as an ImageJ plugin.
 * The run(String) method will call jp.ac.keio.bio.fun.xitosbml.xitosbml.MainImgSpatial#run(java.lang.String).
 * Once registered in src/main/resources/plugins.config, the run() method can be called from the ImageJ menu.
 */
public class Spatial_Img_SBML extends Spatial_SBML {

  /**
   * Launch XitoSBML as ImageJ plugin.
   * See {@link jp.ac.keio.bio.fun.xitosbml.xitosbml.MainImgSpatial#run(java.lang.String)} for implementation.
   * @param arg name of the method defined in plugins.config
   */
	public void run(String arg) {
	  if (arg.equals("about")) {
	    showAbout();
	    return;
	  } else {
		  new MainImgSpatial().run(arg);
	  }
	}
	
	/**
	 * Display "About" message
	 */
	public void showAbout() {
    IJ.showMessage("XitoSBML",
        "XitoSBML " + this.version + ": Spatial SBML Plugin for ImageJ\n"
        + "Copyright (C) 2014-2019 Funahashi Lab. Keio University.\n \n"
        + "XitoSBML is an ImageJ plugin which creates Spatial SBML model\n"
        + "from segmented images. XitoSBML is not just a converter,\n"
        + "but also a spatial model editor so that users can add\n"
        + "molecules(species), reactions and advection/diffusion coefficients\n"
        + "to the converted Spatial SBML model.\n"
        + "More information is available at\n \n"
        + "    "+"https://github.com/spatialsimulator/XitoSBML"
        );
  }

	/**
	 * Example main() method which will launch ImageJ and call "run Spatial Image SBML plugin".
	 * @param args an array of command-line arguments for the application
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = Spatial_Img_SBML.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();
		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
}