import ij.ImagePlus;
import ij.ImageStack;

import java.util.ArrayList;
import java.util.HashMap;


public class SpatialImage {
	byte[] raw;
	int width;
	int height;
	int depth;
	private ImagePlus img;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	HashMap<String,Integer> hashDomainNum;
	ArrayList<ArrayList<String>> adjacentsList;

	public SpatialImage(){
		
	}
	
	public SpatialImage(HashMap<String, Integer> hashSampledValue, HashMap<String, Integer> hashDomainTypes, ImagePlus img){
		this.width = img.getWidth();
		this.height =  img.getHeight();
		this.depth = img.getImageStackSize();
		this.img = img;
		this.hashSampledValue = hashSampledValue;
		this.hashDomainTypes = hashDomainTypes;
		getRawImage();
	}
	
	
	public void getRawImage(){
		byte[] slice = null;   
		raw = new byte[width * height * depth];
    	ImageStack stack = img.getStack();
    	
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[]) stack.getPixels(i);
        	System.arraycopy(slice, 0, raw, (i-1) * height * width, height * width);
    	} 
		
	}
	
	public void setImage(ImagePlus image){
		this.img = image;
	}

	public ImagePlus getImage(){
		return img;
	}
	
	public void sethashDomainNum(HashMap<String,Integer> hashDomainNum){
		this.hashDomainNum = hashDomainNum;
	}
	
	public void setadjacentsList(ArrayList<ArrayList<String>> adjacentsList){
		this.adjacentsList = adjacentsList;
	}

}
