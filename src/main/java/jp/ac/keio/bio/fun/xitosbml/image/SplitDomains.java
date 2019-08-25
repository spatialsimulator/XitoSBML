package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ij.ImageStack;
import ij.process.ByteProcessor;

/**
 * The class SplitDomain, which provides several image processing filters to split
 * domains with given pixel value (sampled value).
 * Date Created: Aug 27, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SplitDomains {
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/** The raw data of spatial image in 1D array. */
	private byte[] raw;
	
	/** The stack of images which has safe border. */
	private ImageStack altStack;
	
	/** The sampled value of cytosol. */
	private byte cytVal;
	
	/** The del target. */
	private byte delTarget;
	
	/** The hashset of target of adjacent pixels. */
	private Set<Integer> adjacentToTargetSet = new HashSet<Integer>();
	
	/** The target of adjacent pixel. */
	private byte adjacentToTarget;
	
	/**
	 * Instantiates a new split domains with given SpatialImage.
	 *
	 * @param spImg the SpatialImage object
	 * @param targetDomain the target domain
	 */
	public SplitDomains(SpatialImage spImg, String targetDomain){
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.raw = spImg.getRaw();
		
		createDomainToCheck(spImg.getHashSampledValue(), targetDomain);
		checkDomain();
		createNewStack();
	}

	/**
	 * Creates the domain to be checked. The deletion target and the adjacent pixels will be set.
	 *
	 * @param hashSampledValue the hashmap of sampled value. HashMap&lt;String, Integer&gt;
	 * @param targetDomain the target domain
	 */
	private void createDomainToCheck(HashMap<String, Integer> hashSampledValue, String targetDomain){
		cytVal =  hashSampledValue.get("Cytosol").byteValue();
		String[] memName = targetDomain.split("_");
		
		delTarget = hashSampledValue.get(getSmallerDom(memName, hashSampledValue)).byteValue();
		adjacentToTarget = hashSampledValue.get(getBiggerDom(memName, hashSampledValue)).byteValue();
	}
	
	/**
	 * Gets the domain which has smaller sampled value.
	 *
	 * @param domNames the array which contains domain names
	 * @param hashSampledValue the hashmap of sampled value. HashMap&lt;String, Integer&gt;
	 * @return the domain which has smaller sampled value.
	 */
	private String getSmallerDom(String[] domNames, HashMap<String, Integer> hashSampledValue){
		String dom1 = domNames[0];
		String dom2 = domNames[1];
		
		if( hashSampledValue.get(dom1) > hashSampledValue.get(dom2))		
			return dom2;
		
		else 
			return dom1;
	}
	
	/**
	 * Gets the domain which has bigger sampled value.
	 *
	 * @param domNames the array which contains domain names
	 * @param hashSampledValue the hashmap of sampled value. HashMap&lt;String, Integer&gt;
	 * @return the domain which has bigger sampled value.
	 */
	private String getBiggerDom(String[] domNames, HashMap<String, Integer> hashSampledValue){
		String dom1 = domNames[0];
		String dom2 = domNames[1];
		
		if( hashSampledValue.get(dom1) > hashSampledValue.get(dom2))		
			return dom2;
		
		else 
			return dom1;
	}
	
	/**
	 * Check domain. If a pixel contain a value of deletion target, then delete the pixel (set to the value of
	 * cytosol) and add adjacent pixels to the list of target.
	 */
	private void checkDomain(){
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if(	delTarget == (raw[d * height * width + h * width + w])){
						checkAdjacents(w,h,d, delTarget);
					}
				}
			}
		}
	}
	
	/**
	 * Check adjacent pixel is not a cytosol and has different pixel value with the given pixel value (deletion target).
	 * If the adjacent pixel has different value, then add the adjacent pixel value to the list of
	 * target, and update the pixel value of deletion target to the pixel value of cytosol (which means, the pixel will
	 * be deleted by this process).
	 *
	 * @param w the x offset
	 * @param h the y offset
	 * @param d the z offset
	 * @param pixVal the pixel value
	 */
	private void checkAdjacents(int w, int h, int d, byte pixVal) {
		List<Byte> adjVal = new ArrayList<Byte>();

		// check left
		if (w != 0 && raw[d * height * width + h * width + w - 1] != cytVal && raw[d * height * width + h * width + w - 1] != pixVal)
			adjVal.add(raw[d * height * width + h * width + w - 1]);

		// check right
		if (w != width - 1 && raw[d * height * width + h * width + w + 1] != cytVal && raw[d * height * width + h * width + w + 1] != pixVal)
			adjVal.add(raw[d * height * width + h * width + w + 1]);

		// check up
		if (h != 0 && raw[d * height * width + (h - 1) * width + w] != cytVal && raw[d * height * width + (h - 1) * width + w] != pixVal)
			adjVal.add(raw[d * height * width + (h - 1) * width + w]);

		// check down
		if (h != height - 1 && raw[d * height * width + (h + 1) * width + w] != cytVal && raw[d * height * width + (h + 1) * width + w] != pixVal)
			adjVal.add(raw[d * height * width + (h + 1) * width + w]);

		// check below
		if (d != 0 && raw[(d - 1) * height * width + h * width + w] != cytVal && raw[(d - 1) * height * width + h * width + w] != pixVal)
			adjVal.add(raw[(d - 1) * height * width + h * width + w]);

		// check above
		if (d < depth - 1 && raw[(d + 1) * height * width + h * width + w] != cytVal && raw[(d + 1) * height * width + h * width + w] != pixVal)
			adjVal.add(raw[(d + 1) * height * width + h * width + w]);

		
		if (adjVal.isEmpty())
			return;
		
		else{
			listToSet(adjVal);
			raw[d * height * width + h * width + w] = cytVal;
		}
	}
	
	/**
     * Add adjacent pixels to target set.
	 *
	 * @param adjVal the list of adjacent pixel values
	 */
	private void listToSet(List<Byte> adjVal){
		for(Byte b : adjVal)
			adjacentToTargetSet.add(b & 0xFF);	
	}
	
	/**
	 * Creates the new stack of split images.
	 */
	private void createNewStack(){
		altStack = new ImageStack(width, height);
		
		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
	}
	
	/**
	 * Gets the stack of split images.
	 *
	 * @return the stack image
	 */
	public ImageStack getStackImage(){
		return altStack;
	}

	/**
	 * Gets the hashset of target of adjacent pixels.
	 *
	 * @return the hashset of target of adjacent pixels
	 */
	public Set<Integer> getAdjacentToTargetList() {
		return adjacentToTargetSet;
	}
}
