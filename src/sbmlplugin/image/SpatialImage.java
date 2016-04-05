/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package sbmlplugin.image;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;


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
	private String unit;
	private HashMap<String,Point3f> hashDomInteriorPt;
	private Point3d delta = new Point3d();
	
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
		if(name == null) return;
		if(depth > 1)
			fs.saveAsTiffStack(path + "/" + name + ".tiff");
		else
			fs.saveAsTiff(path + "/" + name + ".tiff");
	}

	public String getUnit() {
		return unit;
	}
	
	public void setUnit() {
		this.unit = "um";
	}

	//adjust img info to um
	private void adjustUnit(String unit){
		if(unit.equals("nm")){
			delta.x /= 1000;
			delta.y /= 1000;
			delta.z /= 1000;
		}
	}
	
	public Point3d getDelta(){
		return delta;
	}
	
	public void setDelta(Point3d delta) {
		this.delta = delta;
	}
	
	public HashMap<String,Point3f> getHashDomInteriorPt() {
		return hashDomInteriorPt;
	}

	public void setHashDomInteriorpt(HashMap<String,Point3f> hashDomInteriorPt) {
		this.hashDomInteriorPt = hashDomInteriorPt;
	}

}
