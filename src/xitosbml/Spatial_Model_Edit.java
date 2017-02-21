package xitosbml;

import xitosbml.xitosbml.MainModelEdit;


// TODO: Auto-generated Javadoc
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
		if(checkJgraph() && check3Dviewer()) 
			new MainModelEdit().run(args);		
	}
}
