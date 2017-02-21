package xitosbml;

import xitosbml.xitosbml.MainParametricSpatial;


// TODO: Auto-generated Javadoc
/**
 * The Class Spatial_Parametric_SBML.
 */
public class Spatial_Parametric_SBML extends Spatial_SBML{

	/* (non-Javadoc)
	 * @see sbmlplugin.Spatial_SBML#run(java.lang.String)
	 */
	@Override
	public void run(String args) {
		if(check3Dviewer()) 
			new MainParametricSpatial().run(args);	
	}
}