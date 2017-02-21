package xitosbml;


import xitosbml.xitosbml.MainImgSpatial;

// TODO: Auto-generated Javadoc
/**
 * The Class Spatial_Img_SBML.
 */
public class Spatial_Img_SBML extends Spatial_SBML {

	/* (non-Javadoc)
	 * @see sbmlplugin.Spatial_SBML#run(java.lang.String)
	 */
	public void run(String args) {   
		if(checkJgraph() && check3Dviewer()) 
			new MainImgSpatial().run(args);	
	}
}