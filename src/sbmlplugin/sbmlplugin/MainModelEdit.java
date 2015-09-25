package sbmlplugin.sbmlplugin;

import sbmlplugin.geometry.GeometryDatas;

/**
 * Spatial SBML Plugin for ImageJ
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
		document = getDocument();
		checkSBMLDocument(document);
		
		//addParaAndSpecies();
		//save();

		showDomainStructure();
		GeometryDatas gData = new GeometryDatas(model);
		visualize(gData.getSpImgList());
	}
	
}
