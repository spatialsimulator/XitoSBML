package sbmlplugin.visual;

import org.sbml.libsbml.GeometryDefinition;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class AbstractData {
	GeometryDefinition gd;
	String title;
	
	AbstractData(GeometryDefinition gd){
		this.gd = gd;
		title = gd.getId();
	}	
}
