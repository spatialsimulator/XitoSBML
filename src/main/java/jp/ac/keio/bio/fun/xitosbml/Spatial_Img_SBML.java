package jp.ac.keio.bio.fun.xitosbml;

import jp.ac.keio.bio.fun.xitosbml.xitosbml.MainImgSpatial;
import ij.IJ;
import ij.ImageJ;

// TODO: Auto-generated Javadoc
/**
 * The Class Spatial_Img_SBML.
 */
public class Spatial_Img_SBML extends Spatial_SBML {

	/* (non-Javadoc)
	 * @see sbmlplugin.Spatial_SBML#run(java.lang.String)
	 */
	public void run(String args) {   
	  if (args.equals("about")) {
	    showAbout();
	    return;
	  } else {
			new MainImgSpatial().run(args);	
	  }
	}
	
	public void showAbout() {
    IJ.showMessage("XitoSBML",
        "XitoSBML " + this.version + ": Spatial SBML Plugin for ImageJ\n"
        + "Copyright (C) 2014-2017 Funahashi Lab. Keio University.\n \n"
        + "XitoSBML is an ImageJ plugin which creates Spatial SBML model\n"
        + "from segmented images. XitoSBML is not just a converter,\n"
        + "but also a spatial model editor so that users can add\n"
        + "molecules(species), reactions and advection/diffusion coefficients\n"
        + "to the converted Spatial SBML model.\n"
        + "More information is available at\n \n"
        + "    "+"https://github.com/spatialsimulator/XitoSBML"
        );
  }

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