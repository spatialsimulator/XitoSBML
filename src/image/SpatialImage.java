package image;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.HashMap;


public class SpatialImage {
	private byte[] raw;
	private int width;
	private int height;
	private int depth;
	private ImagePlus img;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValue;
	private HashMap<String,Integer> hashDomainNum;
	private ArrayList<ArrayList<String>> adjacentsList;
	public String title;
	
	public SpatialImage(HashMap<String, Integer> hashSampledValue, HashMap<String, Integer> hashDomainTypes, ImagePlus img){
		this.setWidth(img.getWidth());
		this.setHeight(img.getHeight());
		this.setDepth(img.getImageStackSize());
		this.img = img;
		this.setHashSampledValue(hashSampledValue);
		this.setHashDomainTypes(hashDomainTypes);
		setRawImage();
		IJ.log( "If domain exist on the border, Image arrays may be modified\n");
		
		fixBorder();
		boolean hasSafeBorder = isBorderSafe(); 	//depth = 0 or top/bottom slice does not have object
		updateImg(hasSafeBorder);
		
	}	
	
	public void setRawImage(){
		byte[] slice = null;   
		setRaw(new byte[getWidth() * getHeight() * getDepth()]);
    	ImageStack stack = img.getStack();
    	
    	for(int i = 1 ; i <= getDepth() ; i++){
        	slice = (byte[]) stack.getPixels(i);
        	System.arraycopy(slice, 0, getRaw(), (i-1) * getHeight() * getWidth(), getHeight() * getWidth());
    	} 
		
	}
	
	public void setImage(ImagePlus image){
		this.img = image;
		setRawImage();
	}

	public ImagePlus getImage(){
		return img;
	}
	
	public void sethashDomainNum(HashMap<String,Integer> hashDomainNum){
		this.setHashDomainNum(hashDomainNum);
	}
	
	public void setadjacentsList(ArrayList<ArrayList<String>> adjacentsList){
		this.setAdjacentsList(adjacentsList);
	}


	private void fixBorder() {
		int init = 0, end = getDepth();
		
		for (int d = init; d < end; d++) {
			for (int h = 0; h < getHeight(); h++) {
				for (int w = 0; w < getWidth(); w++) {
					if (h == 0 || h == getHeight() - 1 || w == 0 || w == getWidth() - 1) {
						getRaw()[d * getHeight() * getWidth() + h * getWidth() + w] = 0;
					}
				}
			}

		}
	}

	private boolean isBorderSafe(){
		boolean safez = true;
		if(getDepth() != 1)	safez = checkTopBottom();
		return safez;
	}
	
	private boolean checkTopBottom(){
		int bottomSlice = (getDepth() - 1) * getWidth() * getHeight();
		
		for(int h = 0 ; h < getHeight() ; h++){
			for(int w = 0 ; w < getWidth() ; w++){
				if(getRaw()[bottomSlice + getWidth() * h + w] != 0 || getRaw()[getWidth() * h + w] != 0)
					return false;
			}
		}
		
		return true;
	}
	
	private void updateImg(boolean hasSafeBorder){
		ImageStack altStack = new ImageStack(getWidth(), getHeight());
		
		if(!hasSafeBorder) addBlackSlice(altStack);
		
		for(int i = 1 ; i <= getDepth() ; i++){
			byte[] slice = new byte[getHeight() * getWidth()];
			System.arraycopy(getRaw(), (i-1) * getHeight() * getWidth(), slice, 0, getHeight() * getWidth());
			altStack.addSlice(new ByteProcessor(getWidth(),getHeight(),slice,null));
    	} 
		if(!hasSafeBorder) addBlackSlice(altStack);
		
		img.setStack(altStack);
		img.updateImage();
		if(!hasSafeBorder) setDepth(getDepth() + 2);
		if(!hasSafeBorder) setRawImage();
	}
	
	private void addBlackSlice(ImageStack is){
		byte[] blackSlice = new byte[getWidth() * getHeight()];
		is.addSlice(new ByteProcessor(getWidth(),getHeight(),blackSlice,null));
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public HashMap<String, Integer> getHashSampledValue() {
		return hashSampledValue;
	}

	public void setHashSampledValue(HashMap<String, Integer> hashSampledValue) {
		this.hashSampledValue = hashSampledValue;
	}

	public byte[] getRaw() {
		return raw;
	}

	public void setRaw(byte[] raw) {
		this.raw = raw;
	}

	public HashMap<String, Integer> getHashDomainTypes() {
		return hashDomainTypes;
	}

	public void setHashDomainTypes(HashMap<String, Integer> hashDomainTypes) {
		this.hashDomainTypes = hashDomainTypes;
	}

	public HashMap<String,Integer> getHashDomainNum() {
		return hashDomainNum;
	}

	public void setHashDomainNum(HashMap<String,Integer> hashDomainNum) {
		this.hashDomainNum = hashDomainNum;
	}

	public ArrayList<ArrayList<String>> getAdjacentsList() {
		return adjacentsList;
	}

	public void setAdjacentsList(ArrayList<ArrayList<String>> adjacentsList) {
		this.adjacentsList = adjacentsList;
	}
}
