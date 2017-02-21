package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import javax.xml.stream.XMLStreamException;

import jp.ac.keio.bio.fun.xitosbml.geometry.GeometryDatas;
import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 28, 2015
 */
public class MainModelEdit extends MainSBaseSpatial {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		try {
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		checkSBMLDocument(document);
		
		addSBases();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
		showDomainStructure();
		GeometryDatas gData = new GeometryDatas(model);
		visualize(gData.getSpImgList());
		
		print();
	}
}
