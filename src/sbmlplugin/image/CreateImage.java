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
		compoImg.setCalibration(img.getCalibration());
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
	   return (compoVal & 0xFF) < (pixVal & 0xFF);
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
