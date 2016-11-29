package sbmlplugin.geometry;

import org.sbml.libsbml.GeometryDefinition;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class AbstractData {
	
	/** The gd. */
	GeometryDefinition gd;
	
	/** The title. */
	String title;
	
	/**
	 * Instantiates a new abstract data.
	 *
	 * @param gd the gd
	 */
	AbstractData(GeometryDefinition gd){
		this.gd = gd;
		title = gd.getId();
	}	
}
