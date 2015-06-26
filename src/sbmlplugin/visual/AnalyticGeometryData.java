package sbmlplugin.visual;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 26, 2015
 */
public class AnalyticGeometryData extends ImageGeometryData {

	/**
	 * @param gd
	 * @param g
	 */
	AnalyticGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd, g);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSampledValue()
	 */
	@Override
	void getSampledValues() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#createImage()
	 */
	@Override
	void createImage() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSpatialImage()
	 */
	@Override
	SpatialImage getSpatialImage() {
		// TODO Auto-generated method stub
		return null;
	}

}
