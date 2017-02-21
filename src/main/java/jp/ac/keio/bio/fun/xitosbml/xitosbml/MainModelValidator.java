package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import javax.xml.stream.XMLStreamException;

import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 1, 2015
 */
public class MainModelValidator extends MainSBaseSpatial {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		try{
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		checkSBMLDocument(document);
		ModelValidator validator = new ModelValidator(document);
		validator.validate();
	}
}
