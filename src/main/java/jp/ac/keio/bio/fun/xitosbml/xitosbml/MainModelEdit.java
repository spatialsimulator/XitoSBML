package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import ij.IJ;
import jp.ac.keio.bio.fun.xitosbml.geometry.GeometryDatas;
import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;
import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 28, 2015
 *
 * The Class MainModelEdit, which implements "run Model Editor" function.
 */
public class MainModelEdit extends MainSBaseSpatial {

	/**
	 * Overrides ij.plugin.PlugIn#run(java.lang.String)
	 * A dialog for editing the model will be displayed.
     * Users can add, modify following SBML elements and save the model through the dialog.
	 *
     * - Species
	 * - Parameter
	 * - Advection coefficient
	 * - Diffusion coefficient
	 * - Boundary condition
	 * - Reaction
	 *
     * Once the model is saved as SBML, XitoSBML will visualize the model in 3D space,
	 * and execute a syntax validation for both SBML core and spatial extension by using
	 * SBML online validator.
     * With this plugin, users can create a reaction-diffusion model in spatial SBML format.
	 * @param arg name of the method defined in plugins.config
	 */
	@Override
	public void run(String arg) {
		try {
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		} catch (Exception e) {
			IJ.error("Error: File is not an SBML Model");
			return;
		}
		
		checkSBMLDocument(document);
		
		addSBases();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
		showDomainStructure();
		GeometryDatas gData = new GeometryDatas(model);
		visualize(gData.getSpImgList());
		
		print();
		
		ModelValidator validator = new ModelValidator(document);
		validator.validate();
	}
}
