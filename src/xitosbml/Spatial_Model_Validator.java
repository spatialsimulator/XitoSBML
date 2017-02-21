package xitosbml;

import xitosbml.xitosbml.MainModelValidator;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 1, 2015
 */
public class Spatial_Model_Validator extends Spatial_SBML {
	
	/* (non-Javadoc)
	 * @see sbmlplugin.Spatial_SBML#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		new MainModelValidator().run(arg);;
	}

}
