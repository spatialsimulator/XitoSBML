package xitosbml.geometry;

import ij.ImagePlus;

import java.util.HashMap;

import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;

import xitosbml.image.SpatialImage;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class ImageGeometryData extends AbstractData {
	
	/** The g. */
	protected Geometry g;
	
	/** The hash sampled value. */
	protected  HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	
	/** The img. */
	protected ImagePlus img = new ImagePlus();
	
	/** The raw. */
	protected byte raw[];
	
	/**
	 * Instantiates a new image geometry data.
	 *
	 * @param gd the gd
	 * @param g the g
	 */
	ImageGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd);
		this.g = g;
	}

	/**
	 * Gets the sampled values.
	 *
	 * @return the sampled values
	 */
	abstract void getSampledValues();
	
	/**
	 * Creates the image.
	 */
	abstract void createImage();
	
	/**
	 * Gets the spatial image.
	 *
	 * @return the spatial image
	 */
	public abstract SpatialImage getSpatialImage();
}
