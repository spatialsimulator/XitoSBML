package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 1, 2015
 *
 * The Class MainModelValidator, which implements "run Model Validation" function.
 */
public class MainModelValidator extends MainSBaseSpatial {

	/**
	 * Overrides ij.plugin.PlugIn#run(java.lang.String)
	 * A file dialog for validating the model will be displayed.
	 * Users will select a model (SBML file) through the graphical user interface,
	 * and then XitoSBML will will check whether the SBML document has correct
	 * level, version and extension. Moreover, if XitoSBML is connected to an internet,
	 * then it will send the model to the online SBML validator, which will run
	 * a semantic validation, and show the validation result.
	 *
	 * @param arg name of the method defined in plugins.config
	 */
	@Override
	public void run(String arg) {
		try{
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		} catch (XMLStreamException | IOException e) {
			e.printStackTrace();
		}

		checkSBMLDocument(document);
		ModelValidator validator = new ModelValidator(document);
		validator.validate();
	}
}
