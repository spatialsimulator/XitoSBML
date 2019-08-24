package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.HashMap;
import java.util.Iterator;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

/**
 * The class CreateImage, which creates composite image.
 * The composite image is stored in two kinds of objects
 * (raw data as 1D array, and stack of Images).
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class CreateImage {
	
	/** The hashmap of domain images. */
	private HashMap<String, ImagePlus> hashDomFile;
	
	/** The hashmap of sampled value of spatial image. */
	private HashMap<String, Integer> hashSampledValue;
	
	/** The ImageJ image object of composite image. */
	private ImagePlus compoImg;
	
	/** The byte array (raw data) of composite image. */
	private byte[] compoMat;
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth ;

	/** The composite image converted to stack of images. */
	private ImageStack altimage;


	/**
	 * Instantiates a new CreateImage object with given hashmap of domain images and
	 * hashmap of sampled value of spatial image.
	 *
	 * @param hashDomFile the hashmap of domain images
	 * @param hashSampledValue the hashmap of sampled value of spatial image.
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
	 * Composite image. All images included in hashDomFile (hashmap of domain images)
	 * will be composed to compoMat[], which is a raw data (1D array).
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
	 * Creates and returns the raw data (1D array) of given image.
     *
     * @param image the ImageJ image object
     * @return the raw data (1D array) of given image.
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
     * Composite a given image (imgMat) to compoMat array.
	 * The pixel value of given image (imgMat[i]) will be assigned to
	 * compoMat[i], if the pixel value of given image is larger than
	 * compoMat[i].
     *
     * @param img the ImageJ image object
     * @param imgMat the raw date (1D array) of an image
     * @param name the name of an image
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
    * Check whether the pixel value of composite image is smaller than the given pixel value.
	* Both composite image pixel and given pixel values are treated as 8bit value.
    *
    * @param compoVal the value of a composite image
    * @param pixVal the value of a pixel
    * @return true, if compoVal(8bit) is smaller than pixVal(8bit)
    */
   private boolean checkVal(byte compoVal , byte pixVal){
	   return (compoVal & 0xFF) < (pixVal & 0xFF);
   }
    
	/**
	 * Convert raw data (1D array) of composite image to altimage, which is an image stack object.
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
	 * Gets the composite image.
	 *
	 * @return the composite image (ImageJ image object)
	 */
	public ImagePlus getCompoImg() {
		return compoImg;
	}

	/**
	 * Sets the composite image.
	 *
	 * @param compoImg the new composite image (ImageJ image object)
	 */
	public void setCompoImg(ImagePlus compoImg) {
		this.compoImg = compoImg;
	}
}
