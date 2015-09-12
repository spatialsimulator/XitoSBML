package sbmlplugin.image;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;

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
	}	
	
	public SpatialImage(HashMap<String, Integer> hashSampledValue, ImagePlus img){	//only for model editing
		this.setWidth(img.getWidth());
		this.setHeight(img.getHeight());
		this.setDepth(img.getImageStackSize());
		this.img = img;
		this.setHashSampledValue(hashSampledValue);
		setRawImage();
	}	
	
	private void setRawImage(){
		byte[] slice = null;   
		raw = new byte[width * height * depth];
    	ImageStack stack = img.getStack();
    	
    	for(int i = 1 ; i <= depth ; i++){
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

	public void updateImage(ImageStack imStack){
		depth = imStack.getSize();
		img.setStack(imStack);
		setRawImage();
	}
	
	public void sethashDomainNum(HashMap<String,Integer> hashDomainNum){
		this.setHashDomainNum(hashDomainNum);
	}
	
	public void setadjacentsList(ArrayList<ArrayList<String>> adjacentsList){
		this.setAdjacentsList(adjacentsList);
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
		this.hashDomainTypes =  hashDomainTypes;
	}
	
	public void createHashDomainTypes() {
		hashDomainTypes = new HashMap<String, Integer>();
		for (String s : hashSampledValue.keySet()) {
			if (s.contains("membrane"))
				hashDomainTypes.put(s, 2);
			else
				hashDomainTypes.put(s, 3);

		}
	}
	
	public HashMap<String, Integer> getHashDomainNum() {
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

	public void saveAsImage(String path, String name){
		FileSaver fs = new FileSaver(img);
		name = name.substring(0, name.indexOf('.'));
		if(depth > 1)
			fs.saveAsTiffStack(path + "/" + name + ".tiff");
		else
			fs.saveAsTiff(path + "/" + name + ".tiff");
	}

}
