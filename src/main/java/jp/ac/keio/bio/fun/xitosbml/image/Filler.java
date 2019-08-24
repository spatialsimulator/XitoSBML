package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;


// TODO: Auto-generated Javadoc
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
	
	/** The mask which will be used for filling holes. */
	private int[] mask;
	
	/** The hashmap of pixel value. &lt;labelnumber, pixel value&gt;. */
	private HashMap<Integer, Byte> hashPix = new HashMap<Integer, Byte>();		// label number, pixel value
	
	/** The raw data (1D byte array) of the image. */
	private byte[] pixels;
	
	/** The raw data (1D int array) of inverted the image. */
	private int[] invert;
	
	/**
	 * Fill a hall in the given image (ImagePlus object) by morphology operation,
	 * and returns the filled image.
	 *
	 * @param image the ImageJ image object
	 * @return the filled image
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
	 * Fill the given image ({@link SpatialImage} object) by morphology operation,
	 * and returns the fillled image as ImageJ image object.
	 *
	 * @param spImg the SpatialImage object
	 * @return the ImageJ image (ImagePlus) object
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
	 * Creates the stack of image from raw data (1D array) of image (pixels[]),
	 * and returns the stack of image.
	 *
	 * @return the stack of image
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
	 * TODO:
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
	 * TODO:
	 * Label.
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
	 * TODO:
	 * Fill a hole in the hashmap of pixels (HashMap&lt;label number, pixel value&gt; by morphology operation.
     * The hole will be filled with the pixel value of adjacent pixel.
	 *
	 * @param index the index
	 */
	public void fill(int index){
		if (ldepth > depth) { // 3D image
			for (int d = 1; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == index ) {
							pixels[(d-1) * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, index);
						}
					}
				}
			}
		} else { // 2D image
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == index ) {
							pixels[d * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, index);
						}
					}
				}
			}
		}
	}
	
	/**
	 * TODO:
	 * Check adjacents label.
	 *
	 * @param w the width of the image
	 * @param h the height of the image
	 * @param d the depth of the image
	 * @param index the index
	 * @return the byte
	 */
	public byte checkAdjacentsLabel(int w, int h, int d, int index){
		List<Byte> adjVal = new ArrayList<Byte>();
			//check right
			if(mask[d * lheight * lwidth + h * lwidth + w + 1] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w + 1]));
			
			//check left			
			if(mask[d * lheight * lwidth + h * lwidth + w - 1] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]));
			
			//check down
			if(mask[d * lheight * lwidth + (h+1) * lwidth + w ] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h+1) * lwidth + w]));

			//check up
			if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]));

			//check above
			if(d != depth - 1 && mask[(d+1) * lheight * lwidth + h * lwidth + w] != index)
				adjVal.add(hashPix.get(mask[(d+1) * lheight * lwidth + h * lwidth + w]));
			
			//check below
			if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != index)
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
	 * TODO:
	 * Sets the label.
	 *
	 * @param w the w
	 * @param h the h
	 * @param d the d
	 * @param pixVal the pix val
	 * @return the int
	 */
	private int setLabel(int  w , int h, int d, byte pixVal){
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
	 * TODO:
	 * Setback label.
	 *
	 * @param w the w
	 * @param h the h
	 * @param d the d
	 * @param pixVal the pix val
	 * @return the int
	 */
	private int setbackLabel(int  w , int h, int d, byte pixVal){
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
	 * TODO:
	 * Rewrite label.
	 *
	 * @param dEnd the d end
	 * @param after the after
	 * @param before the before
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
