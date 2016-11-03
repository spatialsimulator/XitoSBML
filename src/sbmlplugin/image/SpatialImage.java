package sbmlplugin.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point3d;

/**
 * The Class SpatialImage.
 */
public class SpatialImage {
	
	/** The raw. */
	private byte[] raw;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The depth. */
	private int depth;
	
	/** The img. */
	private ImagePlus img;
	
	/** The hash domain types. */
	private HashMap<String, Integer> hashDomainTypes;
	
	/** The hash sampled value. */
	private HashMap<String, Integer> hashSampledValue;
	
	/** The hash domain num. */
	private HashMap<String,Integer> hashDomainNum;
	
	/** The adjacents list. */
	private ArrayList<ArrayList<String>> adjacentsList;
	
	/** The title. */
	public String title;
	
	/** The unit. */
	private String unit;
	
	/** The hash dom interior pt. */
	private HashMap<String,Point3d> hashDomInteriorPt;
	
	/** The delta. */
	private Point3d delta = new Point3d();
	
	/**
	 * Instantiates a new spatial image.
	 *
	 * @param hashSampledValue the hash sampled value
	 * @param hashDomainTypes the hash domain types
	 * @param img the img
	 */
	public SpatialImage(HashMap<String, Integer> hashSampledValue, HashMap<String, Integer> hashDomainTypes, ImagePlus img){
		this.setWidth(img.getWidth());
		this.setHeight(img.getHeight());
		this.setDepth(img.getImageStackSize());
		this.img = img;
		this.setHashSampledValue(hashSampledValue);
		this.setHashDomainTypes(hashDomainTypes);
		delta.x = img.getFileInfo().pixelWidth;
		delta.y = img.getFileInfo().pixelHeight;
		delta.z = img.getFileInfo().pixelDepth;
		setUnit();
		if(img.getFileInfo().unit != null) 
			adjustUnit(img.getFileInfo().unit);
		setRawImage();
	}	
	
	/**
	 * Instantiates a new spatial image.
	 *
	 * @param hashSampledValue the hash sampled value
	 * @param img the img
	 */
	public SpatialImage(HashMap<String, Integer> hashSampledValue, ImagePlus img){	//only for model editing
		this.setWidth(img.getWidth());
		this.setHeight(img.getHeight());
		this.setDepth(img.getImageStackSize());
		this.img = img;
		this.setHashSampledValue(hashSampledValue);
		delta.x = img.getFileInfo().pixelWidth;
		delta.y = img.getFileInfo().pixelHeight;
		delta.z = img.getFileInfo().pixelDepth;
		setUnit();
		if(img.getFileInfo().unit != null) 
			adjustUnit(img.getFileInfo().unit);
		setRawImage();
	}	
	
	/**
	 * Sets the raw image.
	 */
	private void setRawImage(){
		byte[] slice = null;   
		raw = new byte[width * height * depth];
    	ImageStack stack = img.getStack(); 	
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[]) stack.getPixels(i);
        	System.arraycopy(slice, 0, getRaw(), (i-1) * getHeight() * getWidth(), getHeight() * getWidth());
    	} 
	}
	
	/**
	 * Sets the image.
	 *
	 * @param image the new image
	 */
	public void setImage(ImagePlus image){
		this.img = image;
		setRawImage();
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public ImagePlus getImage(){
		return img;
	}

	/**
	 * Update image.
	 *
	 * @param imStack the im stack
	 */
	public void updateImage(ImageStack imStack){
		depth = imStack.getSize();
		img.setStack(imStack);
		setRawImage();
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height.
	 *
	 * @param height the new height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Sets the depth.
	 *
	 * @param depth the new depth
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Gets the hash sampled value.
	 *
	 * @return the hash sampled value
	 */
	public HashMap<String, Integer> getHashSampledValue() {
		return hashSampledValue;
	}

	/**
	 * Sets the hash sampled value.
	 *
	 * @param hashSampledValue the hash sampled value
	 */
	public void setHashSampledValue(HashMap<String, Integer> hashSampledValue) {
		this.hashSampledValue = hashSampledValue;
	}

	/**
	 * Gets the raw.
	 *
	 * @return the raw
	 */
	public byte[] getRaw() {
		return raw;
	}

	/**
	 * Sets the raw.
	 *
	 * @param raw the new raw
	 */
	public void setRaw(byte[] raw) {
		this.raw = raw;
	}

	/**
	 * Gets the hash domain types.
	 *
	 * @return the hash domain types
	 */
	public HashMap<String, Integer> getHashDomainTypes() {
		return hashDomainTypes;
	}

	/**
	 * Sets the hash domain types.
	 *
	 * @param hashDomainTypes the hash domain types
	 */
	public void setHashDomainTypes(HashMap<String, Integer> hashDomainTypes) {
		this.hashDomainTypes =  hashDomainTypes;
	}
	
	/**
	 * Creates the hash domain types.
	 */
	public void createHashDomainTypes() {
		hashDomainTypes = new HashMap<String, Integer>();
		for (String s : hashSampledValue.keySet()) {
			if (s.contains("membrane"))
				hashDomainTypes.put(s, 2);
			else
				hashDomainTypes.put(s, 3);
		}
	}
	
	/**
	 * Gets the hash domain num.
	 *
	 * @return the hash domain num
	 */
	public HashMap<String, Integer> getHashDomainNum() {
		return hashDomainNum;
	}
	
	/**
	 * Sets the hash domain num.
	 *
	 * @param hashDomainNum the hash domain num
	 */
	public void setHashDomainNum(HashMap<String,Integer> hashDomainNum) {
		this.hashDomainNum = hashDomainNum;
	}

	/**
	 * Gets the adjacents list.
	 *
	 * @return the adjacents list
	 */
	public ArrayList<ArrayList<String>> getAdjacentsList() {
		return adjacentsList;
	}

	/**
	 * Sets the adjacents list.
	 *
	 * @param adjacentsList the new adjacents list
	 */
	public void setAdjacentsList(ArrayList<ArrayList<String>> adjacentsList) {
		this.adjacentsList = adjacentsList;
	}

	/**
	 * Save as image.
	 *
	 * @param path the path
	 * @param name the name
	 */
	public void saveAsImage(String path, String name){
		FileSaver fs = new FileSaver(img);
		if(name == null) return;
		if(depth > 1)
			fs.saveAsTiffStack(path + "/" + name + ".tiff");
		else
			fs.saveAsTiff(path + "/" + name + ".tiff");
	}

	/**
	 * Gets the unit.
	 *
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}
	
	/**
	 * Sets the unit.
	 */
	public void setUnit() {
		this.unit = "um";
	}

	/**
	 * Adjust unit.
	 *
	 * @param unit the unit
	 */
	//adjust img info to um
	private void adjustUnit(String unit){
		if(unit.equals("nm")){
			delta.x /= 1000;
			delta.y /= 1000;
			delta.z /= 1000;
		}
	}
	
	/**
	 * Gets the delta.
	 *
	 * @return the delta
	 */
	public Point3d getDelta(){
		return delta;
	}
	
	/**
	 * Sets the delta.
	 *
	 * @param delta the new delta
	 */
	public void setDelta(Point3d delta) {
		this.delta = delta;
	}
	
	/**
	 * Gets the hash dom interior pt.
	 *
	 * @return the hash dom interior pt
	 */
	public HashMap<String,Point3d> getHashDomInteriorPt() {
		return hashDomInteriorPt;
	}

	/**
	 * Sets the hash dom interiorpt.
	 *
	 * @param hashDomInteriorPt the hash dom interior pt
	 */
	public void setHashDomInteriorpt(HashMap<String,Point3d> hashDomInteriorPt) {
		this.hashDomInteriorPt = hashDomInteriorPt;
	}

}
