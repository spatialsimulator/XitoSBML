package sbmlplugin.util;

import ij.IJ;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLValidator;


// TODO: Auto-generated Javadoc
/**
 * The Class ModelValidator.
 */
public class ModelValidator {
	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** The document. */
	private SBMLDocument document;
	
	/** The model. */
	private Model model;

	
	/**
	 * Instantiates a new model validator.
	 *
	 * @param document the document
	 */
	public ModelValidator(SBMLDocument document){
		this.document = document;
		this.model = document.getModel();	
	}
	
	/**
	 * Check model version.
	 *
	 * @return true, if successful
	 */
	private boolean checkModelVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL  ||  model.getVersion() != PluginConstants.SBMLVERSION) 
			IJ.log("model is not level 3 version 1");
		
		return model.getLevel() == PluginConstants.SBMLLEVEL  &&  model.getVersion() == PluginConstants.SBMLVERSION;
	}
	
	/**
	 * Check extension.
	 *
	 * @return true, if successful
	 */
	private boolean checkExtension(){
		
		if(!document.getPackageRequired("spatial"))
			IJ.log("model missing extension spatial");
		
		if(!document.getPackageRequired("req"))
			IJ.log("model missing extension req");
		
		return document.getPackageRequired("spatial") && document.getPackageRequired("req");
	}

	/**
	 * Validate.
	 */
	public void validate(){
		boolean hasRequiredAttribute = checkModelVersion() && checkExtension();
		
		if(!hasRequiredAttribute)
			return;
		
		SBMLValidator sv = new SBMLValidator();
		sv.validate(document);
		IJ.log(sv.getErrorLog().toString());
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("sampledfield_3d.xml");
		ModelValidator mv = new ModelValidator(d);
		mv.validate();
	}

}
