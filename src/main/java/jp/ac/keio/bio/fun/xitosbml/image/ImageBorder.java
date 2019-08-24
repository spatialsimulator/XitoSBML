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
package jp.ac.keio.bio.fun.xitosbml.image;

import ij.ImageStack;
import ij.process.ByteProcessor;

/**
 * The class ImageBorder, which provides several image processing filters.
 * Date Created: Jun 18, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ImageBorder {
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/** The raw data (1D byte array) of the image. */
	private byte[] raw;
	
	/** The image has safe border. */
	private boolean hasSafeBorder = true;
	
	/** The stack of images which has safe border. */
	private ImageStack altStack;
	
	/**
	 * Instantiates a new ImageBorder object.
	 *
	 * @param spImg the SpatialImage object
	 */
	public ImageBorder(SpatialImage spImg){
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.raw = spImg.getRaw();
		
		fixBorder();						// blackens all outside pixel
		hasSafeBorder = isBorderSafe(); 	//depth = 0 or top/bottom slice does not have object
		createNewStack(hasSafeBorder);
		//createNewStack();
	}
	
	/**
	 * Fill with 0 for X and Y border. This method is like a zero-padding, but will not
	 * extend the image size.
	 */
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

	/**
	 * Checks if border of Z axis is safe (does not contain non zero value on the border layer).
	 *
	 * @return true, if border of Z axis is safe
	 */
	private boolean isBorderSafe(){
		boolean safez = true;
		if(depth > 1)	safez = checkTopBottom();
		return safez;
	}
	
	/**
	 * Check whether top and bottom z-stack image contains non zero value.
     * If the z-stack image contains non zero value, this means that the 3D image is not
	 * surrounded with zero values.
	 *
	 * @return true, if the top and bottom z-stack image only contains zero value
	 */
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
	
	/**
	 * Creates the new stack of images. If the border of Z axis is not safe,
	 * then add a layer which is filled with zero (border layer).
	 *
	 * @param hasSafeBorder the flag whether the border of Z axis is safe
	 */
	private void createNewStack(boolean hasSafeBorder){
		altStack = new ImageStack(width, height);
		
		if(!hasSafeBorder)
			addBlackSlice(altStack);
		
		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
		
		if(!hasSafeBorder) 
			addBlackSlice(altStack);
	
		if(!hasSafeBorder) 
			depth += 2;
		
	}
	
	/**
	 * Creates the new stack of images.
	 */
	private void createNewStack(){
		altStack = new ImageStack(width, height);
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				raw[ h * width + w] = 0;
				raw[(depth - 1) * height * width + h * width + w] = 0;
			}
		}

		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
	}
	
	/**
	 * Adds a black slice (a layer which is filled with zero (that is, the border layer)) to the stack of images.
	 *
	 * @param is the stack of images
	 */
	private void addBlackSlice(ImageStack is){
		byte[] blackSlice = new byte[width * height];
		is.addSlice(new ByteProcessor(width,height,blackSlice,null));
	}
	
	/**
	 * Gets the stack of images.
	 *
	 * @return the stack of images
	 */
	public ImageStack getStackImage(){
		return altStack;
	}
}
