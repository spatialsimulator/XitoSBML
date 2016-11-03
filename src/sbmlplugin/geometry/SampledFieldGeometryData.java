package sbmlplugin.geometry;

import ij.ImageStack;
import ij.process.ByteProcessor;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.CompressionKind;
import org.sbml.jsbml.ext.spatial.DataKind;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledField;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;

import sbmlplugin.image.SpatialImage;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 26, 2015
 */
public class SampledFieldGeometryData extends ImageGeometryData {
	
	/** The sfg. */
	private SampledFieldGeometry sfg;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The depth. */
	private int depth;
	
	/**
	 * Instantiates a new sampled field geometry data.
	 *
	 * @param gd the gd
	 * @param g the g
	 */
	public SampledFieldGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd, g);
		sfg = (SampledFieldGeometry)gd;
		getSampledValues();
		createImage();
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSampledValue()
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

	
	/* (non-Javadoc)
	 * @see sbmlplugin.geometry.ImageGeometryData#createImage()
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
	 * Creates the stack.
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
	 * Gets the size.
	 *
	 * @param sf the sf
	 * @return the size
	 */
	private void getSize(SampledField sf){
		width = sf.getNumSamples1();
		height = sf.getNumSamples2();
		if(sf.isSetNumSamples3()) depth = sf.getNumSamples3();
		else					  depth = 1;
	}
	
	
	/**
	 * Gets the array.
	 *
	 * @param sf the sf
	 * @return the array
	 */
	private void getArray(SampledField sf){ //TODO string to byte array
		int length = height * width * depth;
		raw = new byte[length];
		int array[] = new int[length];
		String data;
		if(sf.getCompression() == CompressionKind.uncompressed)
			data = sf.getDataString();
		else
			data = sf.getSamples();

		intToByte(array, raw);
	}
	
	
	/**
	 * Int to byte.
	 *
	 * @param array the array
	 * @param raw the raw
	 */
	private void intToByte(int[] array, byte[] raw){		
		//TODO need to resolve when original image data is not 8 bit
		for(int i = 0, length = array.length; i < length;i++){
			raw[i] = (byte) array[i];
		}	
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSpatialImage()
	 */
	@Override
	public SpatialImage getSpatialImage() {
		return new SpatialImage(hashSampledValue, img);
	}
}
