package jp.ac.keio.bio.fun.xitosbml.geometry;

import java.util.HashMap;

import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;

import ij.ImagePlus;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;

/**
 * The class ImageGeometryData, which is an abstract class and be used to
 * create data objects for geometry objects (AnalyticGeometryData, SampledFieldGeometryData and ParametricGeometryData).
 * The inherited class should implement getSampledValues(), createImage() and getSpatialImage() methods.
 *
 * Date Created: Jun 25, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public abstract class ImageGeometryData extends AbstractData {
	
	/** The Geometry object. */
	protected Geometry g;
	
	/** The hashmap of sampled value of spatial image.  */
	protected  HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	
	/** The ImageJ image object. */
	protected ImagePlus img = new ImagePlus();
	
	/** The raw data of spatial image in 1D array. */
	protected byte raw[];
	
	/**
	 * Instantiates a new image geometry data.
	 *
	 * @param gd the GeometryDefinition object
	 * @param g the Geometry
	 */
	ImageGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd);
		this.g = g;
	}

	/**
	 * Get sampled value from Geometry (SampledFieldGeometry or AnalyticGeometry) and
	 * sets its value to the hashSampledValue (hashmap of sampled value).
	 * AnalyticGeometry does not contain sampled value, so it will be calculated
	 * by the ordinal value of each domain.
	 */
	abstract void getSampledValues();
	
	/**
	 * Create a stacked image from spatial image (3D).
	 * The value of each pixel corresponds to the domain.
	 */
	abstract void createImage();
	
	/**
	 * Create and return a new spatial image.
	 * SpatialImage object is generated with the ImagePlus object (img) and the hashmap of sampled value
	 * (pixel value of a SampledVolume).
	 *
	 * @return spatial image object, which is an object for handling spatial image in XitoSBML.
	 */
	public abstract SpatialImage getSpatialImage();
}
