import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

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
	String title;
	
	public SpatialImage(HashMap<String, Integer> hashSampledValue, HashMap<String, Integer> hashDomainTypes, ImagePlus img){
		this.width = img.getWidth();
		this.height =  img.getHeight();
		this.depth = img.getImageStackSize();
		this.img = img;
		this.hashSampledValue = hashSampledValue;
		this.hashDomainTypes = hashDomainTypes;
		setRawImage();
		IJ.log( "If domain exist on the border, Image arrays may be modified\n");
		
		fixBorder();
		boolean hasSafeBorder = isBorderSafe(); 	//depth = 0 or top/bottom slice does not have object
		updateImg(hasSafeBorder);
		
	}	
	
	public void setRawImage(){
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
		System.out.println(image.getStackSize());
		setRawImage();
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


	private void fixBorder() {
		int init = 0, end = depth;
		
		for (int d = init; d < end; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if (h == 0 || h == height - 1 || w == 0 || w == width - 1) {
						raw[d * height * width + h * width + w] = 0;
					}
				}
			}

		}
	}

	private boolean isBorderSafe(){
		boolean safez = true;
		if(depth != 1)	safez = checkTopBottom();
		return safez;
	}
	
	private boolean checkTopBottom(){
		int bottomSlice = (depth - 1) * width * height;
		
		for(int h = 0 ; h < height ; h++){
			for(int w = 0 ; w < width ; w++){
				if(raw[bottomSlice + width * h + w] != 0 || raw[width * h + w] != 0)
					return false;
			}
		}
		
		return true;
	}
	
	private void updateImg(boolean hasSafeBorder){
		ImageStack altStack = new ImageStack(width, height);
		
		if(!hasSafeBorder) addBlackSlice(altStack);
		
		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.out.println(altStack.getSize());
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
		if(!hasSafeBorder) addBlackSlice(altStack);
		
		img.setStack(altStack);
		img.updateImage();
		if(!hasSafeBorder) depth +=2;
		if(!hasSafeBorder) setRawImage();
	}
	
	private void addBlackSlice(ImageStack is){
		byte[] blackSlice = new byte[width * height];
		is.addSlice(new ByteProcessor(width,height,blackSlice,null));
	}
}
