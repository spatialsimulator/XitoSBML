package sbmlplugin.geometry;

import ij.ImagePlus;

import java.util.HashMap;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class ImageGeometryData extends AbstractData {
	protected Geometry g;
	protected  HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	protected ImagePlus img = new ImagePlus();
	protected byte raw[];
	
	ImageGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd);
		this.g = g;
	}

	abstract void getSampledValues();
	abstract void createImage();
	abstract SpatialImage getSpatialImage();
}
