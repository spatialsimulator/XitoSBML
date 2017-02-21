package xitosbml.image;

import ij.ImagePlus;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Nov 2, 2015
 */
public class ImgProcessUtil {

    /**
     * Copy mat.
     *
     * @param ip the ip
     * @return the byte[]
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
     * Gets the raw.
     *
     * @param ip the ip
     * @return the raw
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
     * Gets the label mat.
     *
     * @param ip the ip
     * @return the label mat
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
