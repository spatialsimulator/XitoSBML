package image;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.HashMap;
import java.util.Iterator;

public class CreateImage {
	private HashMap<String, ImagePlus> hashDomFile;
	private HashMap<String, Integer> hashSampledValue;
	private ImagePlus compoImg;
	private byte[] compoMat;
	private int width;
	private int height;
	private int depth ;
	private ImageStack altimage;
	

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
	
		System.out.println("fileinfo " + compoImg.getOriginalFileInfo());
	}
	
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

    private byte[] getMat(ImagePlus image){
    	byte[] slice = null;   
    	byte[] pixels = new byte[width * height * depth];
    	ImageStack stack = image.getStack();
    	
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[]) stack.getPixels(i);
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, height * width);
    	} 
    	
    	//invert image 
		if (image.isInvertedLut()) {
			image.getProcessor().invertLut();
		}
    	return pixels;
    }

    private void cmpImg(ImagePlus img, byte[] imgMat, String name){
    	int max = imgMat.length;
    	byte pixVal = hashSampledValue.get(name).byteValue();
    	for(int i = 0 ; i < max ; i++){
    		if(imgMat[i] != 0 && (compoMat[i] == 0 || checkVal(compoMat[i],pixVal))){
    			compoMat[i] = pixVal;
    		}		
    	}
    }

   private boolean checkVal(byte compoVal , byte pixVal){
	   int comp = compoVal & 0xFF;
	   int pix = pixVal & 0xFF;
	  
	   if(comp < pix)
		   return true;
				  
	  return false;
   }
    
	private void replaceMat(){
		altimage = new ImageStack(width, height);
		byte[] slice;

		for (int i = 1; i <= depth; i++) {
			slice = new byte[width * height];
			System.arraycopy(compoMat, (i - 1) * height * width, slice, 0, slice.length);
			altimage.addSlice(new ByteProcessor(width, height, slice, null));
		}
	}
    
	public ImagePlus getCompoImg() {
		return compoImg;
	}

	public void setCompoImg(ImagePlus compoImg) {
		this.compoImg = compoImg;
	}
}
