package jp.ac.keio.bio.fun.xitosbml;

import jp.ac.keio.bio.fun.xitosbml.xitosbml.MainModelValidator;

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 1, 2015
 * 
 * The Class Spatial_Model_Validator, which calls "run Model Validation" from ImageJ (XitoSBML).
 * This class is an implementation of XitoSBML as an ImageJ plugin.
 * The run(String) method will call jp.ac.keio.bio.fun.xitosbml.xitosbml.MainModelValidator#run(java.lang.String).
 * Once registered in src/main/resources/plugins.config, the run() method can be called from the ImageJ menu.
 */
public class Spatial_Model_Validator extends Spatial_SBML {
	
  /**
   * Launch XitoSBML as ImageJ plugin.
   * See {@link jp.ac.keio.bio.fun.xitosbml.xitosbml.MainModelValidator#run(java.lang.String)} for implementation.
   * @param arg name of the method defined in plugins.config
   */
	@Override
	public void run(String arg) {
		new MainModelValidator().run(arg);
	}

}
