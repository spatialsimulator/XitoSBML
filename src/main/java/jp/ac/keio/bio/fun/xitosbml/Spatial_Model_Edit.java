package jp.ac.keio.bio.fun.xitosbml;

import jp.ac.keio.bio.fun.xitosbml.xitosbml.MainModelEdit;


/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public class Spatial_Model_Edit extends Spatial_SBML {

	/* (non-Javadoc)
	 * @see Spatial_SBML#run(java.lang.String)
	 */
	@Override
	public void run(String args) {
			new MainModelEdit().run(args);		
	}
}
