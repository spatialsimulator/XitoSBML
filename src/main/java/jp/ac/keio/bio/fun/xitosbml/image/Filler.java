package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;


/**
 * The class Filler, which provides several morphological operations for filling holes in the image.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class Filler {
	
	/** The ImageJ image object. */
	private ImagePlus image;
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/** The width of an image including padding. */
	private int lwidth;
	
	/** The height of an image including padding. */
	private int lheight;
	
	/** The depth of an image including padding. */
	private int ldepth;
	
	/** The mask which stores the label of each pixel. */
	private int[] mask;
	
	/**
	 * The hashmap of pixel value. &lt;labelnumber, pixel value&gt;.
	 * The domain which has pixel value = 0 will have a label = 1.
	 */
	private HashMap<Integer, Byte> hashPix = new HashMap<Integer, Byte>();		// label number, pixel value
	
	/** The raw data (1D byte array) of the image. */
	private byte[] pixels;
	
	/** The raw data (1D int array) of inverted the image. */
	private int[] invert;
	
	/**
	 * Fill a hole in the given image (ImagePlus object) by morphology operation,
	 * and returns the filled image.
	 *
	 * @param image the ImageJ image object
	 * @return the filled ImageJ image (ImagePlus) object
	 */
	public ImagePlus fill(ImagePlus image){
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.depth = image.getStackSize();
		this.image = image;

		pixels = ImgProcessUtil.copyMat(image);
		invertMat();
		label();
		if (checkHole()) {
			while (checkHole()) {
				fillHole();
				hashPix.clear();
				label();
			}
			ImageStack stack = createStack();
			image.setStack(stack);
			image.updateImage();
		}
		return image;
	}

	/**
	 * Fill a hole in the given image ({@link SpatialImage} object) by morphology operation,
	 * and returns the filled image as ImageJ image object.
	 *
	 * @param spImg the SpatialImage object
	 * @return the filled ImageJ image (ImagePlus) object
	 */
	public ImagePlus fill(SpatialImage spImg){
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.image = spImg.getImage();
		this.pixels = spImg.getRaw();
		invertMat();
		label();
		if(checkHole()){
			while(checkHole()){	
				fillHole();
				hashPix.clear();
				label();
			}
			ImageStack stack = createStack();
			image.setStack(stack);
			image.updateImage();
		}
		
		return image;
	}
	
	/**
	 * Creates the stack of images from raw data (1D array) of image (pixels[]),
	 * and returns the stack of images.
	 *
	 * @return the stack of images
	 */
	private ImageStack createStack(){
		ImageStack altimage = new ImageStack(width, height);
		for(int d = 0 ; d < depth ; d++){
			byte[] matrix = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			altimage.addSlice(new ByteProcessor(width,height,matrix,null));
		}
		return altimage;
	}
	
	/**
	 * Create an inverted 1D array of an image (invert[]) from 1D array of an image (pixels[]).
	 * Each pixel value will be inverted (0 -> 1, otherwise -> 0). For example, the Black and White
	 * binary image will be converted to a White and Black binary image.
	 */
 	private void invertMat(){
		lwidth = width + 2;
		lheight = height + 2;
		if(depth < 3) ldepth = depth;
		else 			ldepth = depth + 2;
		
		invert = new int[lwidth * lheight * ldepth]; 
		mask = new int[lwidth * lheight * ldepth];
		if (ldepth > depth) {  // 3D image
			for (int d = 0; d < ldepth; d++) {
				for (int h = 0; h < lheight; h++) {
					for (int w = 0; w < lwidth; w++) {
						if (d == 0 || d == ldepth - 1 || h == 0 || h == lheight - 1 || w == 0 || w == lwidth - 1) {
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
							mask[d * lheight * lwidth + h * lwidth + w] = 1;
							continue;
						}

						if (pixels[(d - 1) * height * width + (h - 1) * width + w - 1] == 0)
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
						else
							invert[d * lheight * lwidth + h * lwidth + w] = 0;
					}
				}
			}
		} else { // 2D image
			for (int d = 0; d < ldepth; d++) {
				for (int h = 0; h < lheight; h++) {
					for (int w = 0; w < lwidth; w++) {
						if(h == 0 || h == lheight - 1 || w == 0 || w == lwidth - 1){
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
							mask[d * lheight * lwidth + h * lwidth + w] = 1;
							continue;
						}
							
						if (pixels[d * height * width + (h - 1) * width + w - 1] == 0)
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
						else
							invert[d * lheight * lwidth + h * lwidth + w] = 0;	
					}
				}
			}
		}
	 }
	
	/** The label count. */
	private int labelCount;
	
	/**
	 * Assign a label (label number) to each pixel.
     * The label number will be stored in mask[] array.
	 * The domain which has pixel value = 0 will have a label = 1.
	 */
	public void label(){
		hashPix.put(1, (byte)0);
		labelCount = 2;
		if (ldepth > depth) {
			for (int d = 1; d < ldepth - 1; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1 && pixels[(d-1) * height * width + (h-1) * width + w - 1] == 0) {
							mask[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}else{
							mask[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}else{
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1 && pixels[d * height * width + (h-1) * width + w - 1] == 0) {
							mask[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}else{
							mask[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Check whether a hole exists in the hashmap of pixels (HashMap&lt;label number, pixel value&gt;).
	 *
	 * @return true, if a hole exists
	 */
	public boolean checkHole(){
		if(Collections.frequency(hashPix.values(), (byte) 0) > 1)
			return true;
		else		
			return false;
	}
	
	/**
	 * Fill a hole in the hashmap of pixels (HashMap&lt;label number, pixel value&gt; by morphology operation.
	 * The fill operation will be applied to each domain (which has unique label number).
	 */
	public void fillHole(){
		for(Entry<Integer, Byte> e : hashPix.entrySet()){
			if(!e.getKey().equals(1) && e.getValue().equals((byte)0)){
				fill(e.getKey());
			}
		}
	}
	
	/**
	 * Fill a hole in the hashmap of pixels (HashMap&lt;label number, pixel value&gt; by morphology operation.
     * The hole will be filled with the pixel value of adjacent pixel.
	 *
	 * @param labelNum the label number
	 */
	public void fill(int labelNum){
		if (ldepth > depth) { // 3D image
			for (int d = 1; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == labelNum ) {
							pixels[(d-1) * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, labelNum);
						}
					}
				}
			}
		} else { // 2D image
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == labelNum ) {
							pixels[d * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, labelNum);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Check adjacent pixels whether it contains the given label (labelNum).
     * If all the adjacent pixels have same label with given label, then return 0.
	 * If the adjacent pixels contain different labels, then returns the pixel
	 * value of most enclosing adjacent domain.
	 *
	 * @param w the x offset
	 * @param h the y offset
	 * @param d the z offset
	 * @param labelNum the label number
	 * @return the pixel value of most enclosing adjacent domain if different domain exists, otherwise 0
	 */
	public byte checkAdjacentsLabel(int w, int h, int d, int labelNum){
		List<Byte> adjVal = new ArrayList<Byte>();
			//check right
			if(mask[d * lheight * lwidth + h * lwidth + w + 1] != labelNum)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w + 1]));
			
			//check left			
			if(mask[d * lheight * lwidth + h * lwidth + w - 1] != labelNum)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]));
			
			//check down
			if(mask[d * lheight * lwidth + (h+1) * lwidth + w ] != labelNum)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h+1) * lwidth + w]));

			//check up
			if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != labelNum)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]));

			//check above
			if(d != depth - 1 && mask[(d+1) * lheight * lwidth + h * lwidth + w] != labelNum)
				adjVal.add(hashPix.get(mask[(d+1) * lheight * lwidth + h * lwidth + w]));
			
			//check below
			if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != labelNum)
				adjVal.add(hashPix.get(mask[(d - 1) * lheight * lwidth + h * lwidth + w]));
			
			if(adjVal.isEmpty())
				return 0;
		
			int max = 0;
			int count = 0;
			int freq, temp; Byte val = 0;
			for(int n = 0 ; n < adjVal.size() ; n++){
				val = adjVal.get(n);
				if(val == 0)
					continue;
				freq = Collections.frequency(adjVal, val);
				temp = val & 0xFF;
				if(freq > count){
					max = temp;
					count = freq;
				}
					
				if(freq == count && max < temp){
					max = temp;
					count = freq;
				}
			}
		return (byte) max;
	}
	
	/**
	 * Sets the label of the given pixel (x offset, y offset, z offset) with its pixel value.
	 * Checks whether the adjacent pixel has the zero pixel value, and its label is already assigned.
	 *
	 * @param w the x offset
	 * @param h the y offset
	 * @param d the z offset
	 * @param pixVal the pixel value
	 * @return the label as integer value
	 */
	private int setLabel(int w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();
		//check left			
		if(mask[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]) == (byte)0)
			adjVal.add(mask[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]) == (byte)0)
			adjVal.add(mask[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(mask[(d-1) * lheight * lwidth + h * lwidth + w]) == (byte)0)
			adjVal.add(mask[(d-1) * lheight * lwidth + h * lwidth + w]);
		
		if(adjVal.isEmpty()){
			hashPix.put(labelCount, pixVal);
			return labelCount++;
		}
			
		Collections.sort(adjVal);
		
		//if all element are same or list has only one element 
		if(Collections.frequency(adjVal, adjVal.get(0)) == adjVal.size())
			return adjVal.get(0);
		
		int min = adjVal.get(0);
		for(int i = 1; i < adjVal.size(); i++){
			if(min == adjVal.get(i))
				continue;
			
			rewriteLabel(d, min, adjVal.get(i));
			hashPix.remove(adjVal.get(i));
		}
			return min;
	}

	/**
	 * Sets back the label of the given pixel (x offset, y offset, z offset) with its pixel value.
	 * Checks whether the adjacent pixel has the non-zero pixel value, and its label is already assigned.
	 *
	 * @param w the x offset
	 * @param h the y offset
	 * @param d the z offset
	 * @param pixVal the pixel value
	 * @return the label as integer value
	 */
	private int setbackLabel(int w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();
		//check left
		if(mask[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]) != (byte)0)
			adjVal.add(mask[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]) != (byte)0)
			adjVal.add(mask[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(mask[(d-1) * lheight * lwidth + h * lwidth + w]) != (byte)0)
			adjVal.add(mask[(d-1) * lheight * lwidth + h * lwidth + w]);
		
		if(adjVal.isEmpty()){
			hashPix.put(labelCount, pixVal);
			return labelCount++;
		}
			
		Collections.sort(adjVal);
	
		//if all element are same or list has only one element 
		if(Collections.frequency(adjVal, adjVal.get(0)) == adjVal.size())
			return adjVal.get(0);
		
		int min = adjVal.get(0);
		for(int i = 1; i < adjVal.size(); i++){
			if(min == adjVal.get(i))
				continue;
			
			hashPix.remove(adjVal.get(i));
			rewriteLabel(d, min, adjVal.get(i));
		}
		return min;
	}
	
	/**
	 * Replace the label of pixels in the spatial image which has "before" to "after".
	 *
	 * @param dEnd the end of the depth
	 * @param after the label to set by this replacement
	 * @param before the label to be replaced
	 */
	private void rewriteLabel(int dEnd, int after, int before){
		if (ldepth > depth) {
			for (int d = 1; d <= dEnd; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == before)
							mask[d * lheight * lwidth + h * lwidth + w] = after;					
						}
				}
			}
		}else{
			for (int d = 0; d <= dEnd; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == before)
							mask[d * lheight * lwidth + h * lwidth + w] = after;
					}
				}
			}
		}

	}
	
}
