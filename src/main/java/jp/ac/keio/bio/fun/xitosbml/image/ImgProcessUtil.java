package jp.ac.keio.bio.fun.xitosbml.image;

import ij.ImagePlus;

/**
 * The class ImgProcessUtil, which contains several useful static methods
 * which wll convert ImageJ image object (ImagePlus) to raw data (1D byte array).
 * Date Created: Nov 2, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ImgProcessUtil {

    /**
     * Converts given image object (ImagePlus) to a raw data (1D byte array).
     *
     * @param ip the ImageJ image object
     * @return the raw data (1D byte array) of given image object
     */
    public static byte[] copyMat(ImagePlus ip){
		int width = ip.getWidth();
		int height = ip.getHeight();
		int depth = ip.getStackSize();	
    	byte[] slice;   
    	byte[] pixels = new byte[width * height * depth];
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])ip.getStack().getPixels(i);
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
        }
		return pixels;
    }
	
	/**
	 * Converts given image object (ImagePlus) to a raw data (1D byte array).
     * This method is not used in XitoSBML. Use {@link #copyMat(ImagePlus)} instead.
	 *
	 * @param ip the ImageJ image object
	 * @return the raw data (1D byte array) of given image object
	 */
	public static byte[] getRaw(ImagePlus ip){
		int width = ip.getWidth();
		int height = ip.getHeight();
		int depth = ip.getStackSize();	
		if (ip.isInvertedLut()) 
			ip.getProcessor().invertLut();
		
		
		byte[] slice;   
    	byte[] raw = new byte[width * height * depth];
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])ip.getStack().getPixels(i);
        	System.arraycopy(slice, 0, raw, (i-1) * height * width, slice.length);
        }
    	return raw;
    }
	
    /**
     * Gets the label matrix.
	 * This method is not yet implemented.
	 *
	 * @param ip the ImageJ image object
	 * @return the label matrix (1D byte array) of given image object
     */
    public static byte[] getlabelMat(ImagePlus ip){
		int width = ip.getWidth();
		int height = ip.getHeight();
		int depth = ip.getStackSize();	
    	byte[] label = new byte[width * height * depth];
    	if (ip.isInvertedLut()) 
			ip.getProcessor().invertLut();
			
    	return label;
    }
    
}
