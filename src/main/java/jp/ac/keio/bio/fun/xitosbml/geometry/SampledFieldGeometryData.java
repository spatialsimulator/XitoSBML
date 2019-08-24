package jp.ac.keio.bio.fun.xitosbml.geometry;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.CompressionKind;
import org.sbml.jsbml.ext.spatial.DataKind;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledField;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;

import ij.ImageStack;
import ij.process.ByteProcessor;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;


/**
 * The class SampledFieldGeometryData, which inherits ImageGeometryData and
 * implements getSampledValues() and createImage() methods. This class
 * contains following objects which are related to sampled field geometry.
 * <ul>
 *     <li>Geometry {@link org.sbml.jsbml.ext.spatial.SampledFieldGeometry}</li>
 *     <li>image size</li>
 * </ul>
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.geometry.GeometryDatas},
 * to visualize a model in 3D space.
 *
 * Date Created: Jun 26, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SampledFieldGeometryData extends ImageGeometryData {
	
	/** The sampled filed geometry object. */
	private SampledFieldGeometry sfg;
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/**
	 * Instantiates a new sampled field geometry data with given GeometryDefinition
	 * and Geometry.
	 *
	 * @param gd the GeometryDefinition
	 * @param g the Geometry
	 */
	public SampledFieldGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd, g);
		sfg = (SampledFieldGeometry)gd;
		getSampledValues();
		createImage();
	}

	/**
	 * Get sampled value from Geometry (SampledFieldGeometry) and
	 * sets its value to the hashSampledValue (hashmap of sampled value).
	 */
	@Override
	protected void getSampledValues() {											//may need to use min/max in future
		ListOf<SampledVolume> losv = sfg.getListOfSampledVolumes();
		for(int i = 0 ; i < losv.size() ; i++){
			SampledVolume sv = losv.get(i);
			if(sv.isSetSampledValue())
				hashSampledValue.put(sv.getDomainType(), (int) sv.getSampledValue());			//double to int may need to chang
		}
	}

	/**
	 * Create a stacked image from spatial image (3D).
	 * The value of each pixel corresponds to the domain.
	 * @see jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData#createImage()
	 */
	@Override
	protected void createImage(){
		ListOf<SampledField> losf = g.getListOfSampledFields();
		//TODO : be able create image with multiple sampledfield
		if(losf.size() > 1)
			System.err.println("not able to compute multiple sampledfields at this point");

		SampledField sf = losf.get(0);
		if(sf.getDataType() != DataKind.UINT8)
			System.err.println("Image data is automatically changed to 8 bit image");
		getSize(sf);
		getArray(sf);

		ImageStack is = createStack(); 
		img.setStack(is);
		img.setTitle(title);
	}
	
	/**
	 * Creates the stacked image from the raw data (1D array) of spatial image.
	 *
	 * @return the image stack
	 */
	private ImageStack createStack(){
		ImageStack stack = new ImageStack(width, height);
		byte[] slice;   
    	int length = width * height;
    	for(int i = 1 ; i <= depth ; i++){
        	slice = new byte[length];
        	System.arraycopy(raw, (i-1) * height * width, slice, 0, length);
        	stack.addSlice(new ByteProcessor(width,height,slice,null));
    	}
    	return stack;
    }
	
	/**
	 * Gets the size of geometry, and sets the height, depth
	 * of the given sampled field. These values will be obtained
	 * by the size of SampledField object.
     *
	 * @param sf the sampled field object
	 */
	private void getSize(SampledField sf){
		width = sf.getNumSamples1();
		height = sf.getNumSamples2();
		if(sf.isSetNumSamples3()) depth = sf.getNumSamples3();
		else					  depth = 1;
	}
	
	/**
	 * Create a byte array (raw), which will be used to store the value of an image.
     * Data compression is currently not supported.
	 *
	 * @param sf the sampled field object
	 */
	private void getArray(SampledField sf){
		String[] data;
		if(sf.getCompression() == CompressionKind.uncompressed)
			data = sf.getSamples().split(" ");
		else
			//TODO add uncompression for sample array
			data = sf.getSamples().split(" ");
		
		raw = stringToByte(data);
	}

	/**
     * Convert String array, which contains pixel values of sampled field as String,
	 * to 1D byte array. Currently this implementation expects that the String
	 * array only contains "-255" to "255" values.
	 *
	 * @param data the String array which contains pixel values as String
	 * @return raw the raw data (1D array) of sampled field.
	 */
	private byte[] stringToByte(String[] data){		
		raw = new byte[data.length];
		
		//TODO need to resolve when original image data is not 8 bit
		for(int i = 0, length = data.length; i < length;i++){
			raw[i] = (byte) Integer.parseInt(data[i]);
		}	
		
		return raw;
	}

	/**
	 * Create and return a new spatial image.
	 * SpatialImage object is generated with the ImagePlus object (img) and the hashmap of sampled value
	 * (pixel value of a SampledVolume).
	 * @see jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData#getSpatialImage()
	 *
	 * @return spatial image object, which is an object for handling spatial image in XitoSBML.
	 */
	@Override
	public SpatialImage getSpatialImage() {
		return new SpatialImage(hashSampledValue, img);
	}
}
