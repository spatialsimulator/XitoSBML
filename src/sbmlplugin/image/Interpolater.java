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
import java.util.HashMap;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ByteProcessor;


public class Interpolater {
	//original voxel size
	private double voxx;			
	private double voxy;
	private double voxz;
	private double zaxis;
	private int width;
	private int height;
	private int depth;
	private int altz;
	private ImagePlus image;		//input image
	private ImageStack altimage;	//output pixel image
	private byte[] pixels;
	
	public Interpolater() {

	}
	
	Interpolater(SpatialImage spImg){
			this.image = spImg.getImage();
			width = spImg.getWidth();
			height = spImg.getHeight();
			depth = spImg.getDepth();
			FileInfo info = image.getOriginalFileInfo();

			voxx = info.pixelWidth;
			voxy = info.pixelHeight;
			voxz = info.pixelDepth;
			zaxis = voxz * image.getImageStackSize();
			this.pixels = spImg.getRaw();
			System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
			
			if (needInterpolate()) {
				nearestNeighbor();
				image.setStack(altimage);
				info.pixelDepth =  zaxis / altz;
				image.setFileInfo(info);
				image.updateImage();
				System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
			}
			
	}
	
	public ImagePlus interpolate(ImagePlus imagePlus){
		this.image = imagePlus;
		width = imagePlus.getWidth();
		height = imagePlus.getHeight();
		depth = imagePlus.getImageStackSize();
		FileInfo info = image.getOriginalFileInfo();
		ImagePlus nImg = new ImagePlus();
		voxx = info.pixelWidth;
		voxy = info.pixelHeight;
		voxz = info.pixelDepth;
		zaxis = voxz * image.getImageStackSize();
		copyMat();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
		
		if (needInterpolate()) {
			nearestNeighbor();
			nImg.setStack(altimage);
			nImg.setFileInfo(info);
			nImg.getFileInfo().pixelDepth =  zaxis / altz;
			nImg.updateImage();
			System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
		}else{
			nImg = (ImagePlus) imagePlus.clone();
		}

		return nImg;
	}
	

	public SpatialImage interpolate(SpatialImage spImg){
		this.image = spImg.getImage();
		width = spImg.getWidth();
		height = spImg.getHeight();
		depth = spImg.getDepth();
		FileInfo info = image.getOriginalFileInfo();

		voxx = info.pixelWidth;
		voxy = info.pixelHeight;
		voxz = info.pixelDepth;
		zaxis = voxz * image.getImageStackSize();
		this.pixels = spImg.getRaw();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
		
		if (needInterpolate()) {
			nearestNeighbor();
			image.resetStack();
			image.setStack(altimage);
			info.pixelDepth =  zaxis / altz;
			image.setFileInfo(info);
			System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
		}
		image.updateImage();
		spImg.setImage(image);
		return spImg;
	}
	
	
    private void copyMat(){
    	byte[] slice;   
    	pixels = new byte[width * height * depth];
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])image.getStack().getPixels(i); 
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
        }
    }
	
	private boolean needInterpolate(){
		if(voxz > voxx || voxz > voxy)
			return true;
		
		return false;
	}

	private void nearestNeighbor(){
		altz = (int) (zaxis / voxx);
		System.out.println("interpolated stack size " + altz);
		altimage = new ImageStack(width, height);
		byte matrix[];
		double xdis, ydis, zdis;
		double halfx = voxx /2, halfy = voxy /2,halfz = voxx /2;
		
		for(int d = 0 ; d < altz ; d++){
			matrix = new byte[width * height];
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					// get center
					xdis = w * voxx + halfx;	 
					ydis = h * voxy + halfy;
					zdis = d * voxx + halfz;
					
					//apply to nearest original pixel
					xdis = Math.floor(xdis / voxx);
					ydis = Math.floor(ydis / voxy);
					zdis = Math.floor(zdis / voxz);
					matrix[h * width + w] = pixels[(int) (zdis * width * height + ydis * width + xdis)];	
				}
			}
			altimage.addSlice(new ByteProcessor(width,height,matrix,null));
		}
	}
	
	public ImagePlus getInterpolatedImage(){
		return image;
	}

	/**
	 * @param hashdomFile
	 */
	public void interpolate(HashMap<String, ImagePlus> hashdomFile) {
		for(Entry<String, ImagePlus> e : hashdomFile.entrySet()){
			ImagePlus i = interpolate(e.getValue());
			hashdomFile.put(e.getKey(), i);
		}
	}
	
}
