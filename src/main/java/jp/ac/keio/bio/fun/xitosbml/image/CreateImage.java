package jp.ac.keio.bio.fun.xitosbml.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.HashMap;
import java.util.Iterator;

// TODO: Auto-generated Javadoc
/**
 * The Class CreateImage.
 */
public class CreateImage {
	
	/** The hash dom file. */
	private HashMap<String, ImagePlus> hashDomFile;
	
	/** The hash sampled value. */
	private HashMap<String, Integer> hashSampledValue;
	
	/** The compo img. */
	private ImagePlus compoImg;
	
	/** The compo mat. */
	private byte[] compoMat;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The depth. */
	private int depth ;
	
	/** The altimage. */
	private ImageStack altimage;
	

	/**
	 * Instantiates a new creates the image.
	 *
	 * @param hashDomFile the hash dom file
	 * @param hashSampledValue the hash sampled value
	 */
	public CreateImage(HashMap<String, ImagePlus> hashDomFile, HashMap<String, Integer> hashSampledValue) {
		this.hashSampledValue = hashSampledValue;
		this.hashDomFile = hashDomFile;
		ImagePlus img = hashDomFile.values().iterator().next();
		width = img.getWidth();
		height = img.getHeight();
		depth = img.getStackSize();
		compositeImage();
		System.out.println("width " + width + " height "+ height + " depth " + depth);
		replaceMat();
		compoImg = new ImagePlus("Combined_Image", altimage);
		compoImg.setFileInfo(img.getFileInfo());
		compoImg.setCalibration(img.getCalibration());
		System.out.println("fileinfo " + compoImg.getOriginalFileInfo());
	}
	
	/**
	 * Composite image.
	 */
	private void compositeImage(){
		Iterator<String> domNames = hashDomFile.keySet().iterator();
		compoMat = new byte[width*height*depth];
		ImagePlus temp;
		byte[] tempMat;
		String imgName;
		
		while(domNames.hasNext()){
			imgName = domNames.next();
			temp = hashDomFile.get(imgName);
			tempMat = getMat(temp);
			cmpImg(temp, tempMat, imgName);
		}
	}

    /**
     * Gets the mat.
     *
     * @param image the image
     * @return the mat
     */
    private byte[] getMat(ImagePlus image){
    	byte[] slice = null;   
    	byte[] pixels = new byte[width * height * depth];

		if (image.isInvertedLut()) {
			image.getProcessor().invertLut();
		}
    	ImageStack stack = image.getStack();
    	
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[]) stack.getPixels(i);
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, height * width);
    	} 
    	

    	return pixels;
    }

    /**
     * Cmp img.
     *
     * @param img the img
     * @param imgMat the img mat
     * @param name the name
     */
    private void cmpImg(ImagePlus img, byte[] imgMat, String name){
    	int max = imgMat.length;
    	System.out.println("cmpImg " + name);
    	byte pixVal = hashSampledValue.get(name).byteValue();
    	for(int i = 0 ; i < max ; i++){
    		if(imgMat[i] != 0 && (compoMat[i] == 0 || checkVal(compoMat[i],pixVal))){
    			compoMat[i] = pixVal;
    		}		
    	}
    }

   /**
    * Check val.
    *
    * @param compoVal the compo val
    * @param pixVal the pix val
    * @return true, if successful
    */
   private boolean checkVal(byte compoVal , byte pixVal){
	   return (compoVal & 0xFF) < (pixVal & 0xFF);
   }
    
	/**
	 * Replace mat.
	 */
	private void replaceMat(){
		altimage = new ImageStack(width, height);
		byte[] slice;

		for (int i = 1; i <= depth; i++) {
			slice = new byte[width * height];
			System.arraycopy(compoMat, (i - 1) * height * width, slice, 0, slice.length);
			altimage.addSlice(new ByteProcessor(width, height, slice, null));
		}
	}
    
	/**
	 * Gets the compo img.
	 *
	 * @return the compo img
	 */
	public ImagePlus getCompoImg() {
		return compoImg;
	}

	/**
	 * Sets the compo img.
	 *
	 * @param compoImg the new compo img
	 */
	public void setCompoImg(ImagePlus compoImg) {
		this.compoImg = compoImg;
	}
}
