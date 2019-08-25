package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.scijava.vecmath.Point3d;

import ij.ImagePlus;


/**
 * The class ImageEdit, which provides several image processing filters to add
 * a membrane between two different domains if exists.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ImageEdit {
	
	/** The list of unique pixel values */
	private ArrayList<Integer> labelList;
	
	/** The hashmap of number of labels. HashMap&lt;Integer, Integer&gt;*/
	private HashMap<Integer,Integer> hashLabelNum;
	
	/** The hashmap of domain types. HashMap&lt;String, Integer&gt;*/
	private HashMap<String,Integer> hashDomainTypes;
	
	/** The hashmap of sampled value. HashMap&lt;String, Integer&gt;*/
	private HashMap<String,Integer> hashSampledValue;
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/** The size of an image. */
	private int size;
	
	/** The raw data (1D array) of an image. */
	private byte[] pixels;
	
	/** The 1D array of labels. */
	private int[] matrix;	
	
	/** The inverted 1D array of an image */
	private int[] invert;
	
	/** The hashmap of pixels. HashMap&lt;Integer label, Integer pixel value&gt; */
	private HashMap<Integer, Integer>  hashPix = new HashMap<Integer,Integer>(); //label + pixel value
	
	/** The hashmap of Point3d objects. HashMap&lt;Integer label, Point3d coordinate&gt; */
	private HashMap<Integer, Point3d> hashLabelPt = new HashMap<Integer,Point3d>();  //label + coordinates
    
	/** The hashmap of domain InteriorPoint of spatial image. HashMap&lt;String domain name, Point3d coordinate&gt; */
    private HashMap<String, Point3d> hashDomInteriorPt = new HashMap<String,Point3d>();  //domain name + coordinates
	
    /**
     * Instantiates a new image edit object with given image object and hashmaps of domain types and sampled value.
     *
     * @param image the ImageJ image object
     * @param hashDomainTypes the hashmap of domain types
     * @param hashSampledValue the hashmap of sampled value of spatial image
     */
    ImageEdit(ImagePlus image,HashMap<String,Integer> hashDomainTypes, HashMap<String,Integer> hashSampledValue){
    	this.width = image.getWidth();
        this.height = image.getHeight();
        this.depth = image.getStackSize();
        this.size = width * height * depth;
        this.hashDomainTypes = hashDomainTypes;
        this.hashSampledValue = hashSampledValue;
        pixels = ImgProcessUtil.copyMat(image);
        listVal();
        invertMat();
        label();
        createMembrane();
    }
    
    /**
     * Instantiates a new image edit with given spatial image object.
     *
     * @param spImg the spatial image object
     */
    public ImageEdit(SpatialImage spImg){
    	spImg.getImage();    	
        this.width = spImg.getWidth();
        this.height = spImg.getHeight();
        this.depth = spImg.getDepth();
        this.size = width * height * depth;
        this.hashDomainTypes = spImg.getHashDomainTypes();
        this.hashSampledValue = spImg.getHashSampledValue();
        this.pixels = spImg.getRaw();

        listVal();
        invertMat();
        label();
        createMembrane();
        spImg.setHashDomainNum(hashDomainNum);
        spImg.setAdjacentsList(adjacentsList);
        createDomInteriorPt();
        spImg.setHashDomInteriorpt(hashDomInteriorPt);
    }
    
    /**
	 * Create a new labelList object, which is a list of unique pixel values.
     */
    private void listVal(){
    	int temp;
    	labelList = new ArrayList<Integer>();
    	for (int i = 0 ; i < size ; i++) {
			temp = pixels[i] & 0xFF;
			if (!labelList.contains(temp)) {	
				labelList.add(new Integer(temp));
			}
		}
    	Collections.sort(labelList);
    }
    
    /**
     * Create hashmap of count number of labels and set the value with given
	 * hashmap of pixels.
     *
     * @param num the hashmap of pixels (HashMap&lt;Integer, Integer&gt;).
     */
    public void domtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Entry<Integer, Integer> e : num.entrySet()){
    		if(!hashLabelNum.containsKey(e.getValue()))
    			hashLabelNum.put((Integer) e.getValue(), Collections.frequency(num.values(), e.getValue()));
    	}	
    }

	/**
	 * Create an inverted 1D array of an image (invert[]) from 1D array of an image (pixels[]).
	 * Each pixel value will be inverted (0 -> 1, otherwise -> 0). For example, the Black and White
	 * binary image will be converted to a White and Black binary image.
	 */
 	private void invertMat(){		
		invert = new int[width * height * depth]; 
		matrix = new int[width * height * depth];
			for (int d = 0; d < depth; d++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						if (pixels[d * height * width + h * width + w] == 0)
							invert[d * height * width + h * width + w] = 1;
						else
							invert[d * height * width + h * width + w] = 0;
					}
				}
			}
	 }
	 
	/** The count number of labels. */
	private int labelCount = 1;
	
	/**
	 * Set the label of each pixel to the 1D array of labels (matrix).
	 * After the labels are set to all pixels, then count the number of labels
	 * and set to the hashmap of count number of labels.
	 */
	public void label(){
			for (int d = 0; d < depth; d++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						matrix[d * height * width + h * width + w] = setLabel(w, h, d, pixels[d * height * width + h * width + w] & 0xFF);
					}
				}
			}
		domtype(hashPix);
	}
	
	/**
	 * Sets the label of the given pixel (x offset, y offset, z offset) with its pixel value.
     * Checks whether the adjacent pixel has the same pixel value, and its label is already assigned.
	 *
	 * @param w the x offset
	 * @param h the y offset
	 * @param d the z offset
	 * @param pixVal the pixel value
	 * @return the label as integer value
	 */
	private int setLabel(int w , int h, int d, int pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();
		//check left			
		if(w != 0 && hashPix.get(matrix[d * height * width + h * width + w - 1]).equals(pixVal))
			adjVal.add(matrix[d * height * width + h * width + w - 1]);

		//check right			
		if(w != width - 1 && matrix[d * height * width + h * width + w + 1] != 0 && hashPix.get(matrix[d * height * width + h * width + w + 1]).equals(pixVal))
			adjVal.add(matrix[d * height * width + h * width + w + 1]);

		//check up
		if(h != 0 && hashPix.get(matrix[d * height * width + (h-1) * width + w]).equals(pixVal))
			adjVal.add(matrix[d * height * width + (h-1) * width + w]);

		//check down
		if(h != height - 1 && matrix[d * height * width + (h+1) * width + w ] != 0 && hashPix.get(matrix[d * height * width + (h+1) * width + w]).equals(pixVal))
			adjVal.add(matrix[d * height * width + (h+1) * width + w]);
		
		//check below
		if(d != 0 && hashPix.get(matrix[(d-1) * height * width + h * width + w]).equals(pixVal))
			adjVal.add(matrix[(d-1) * height * width + h * width + w]);

		//check above
		if(d != depth - 1 && matrix[(d+1) * height * width + h * width + w ] != 0 && hashPix.get(matrix[(d+1) * height * width + h * width + w]).equals(pixVal))
			adjVal.add(matrix[(d+1) * height * width + h * width + w]);
		
		if(adjVal.isEmpty()){
			hashPix.put(labelCount, pixVal);
			hashLabelPt.put(labelCount, new Point3d(w,h,d));
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
			hashLabelPt.remove(adjVal.get(i));
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
		for (int d = 0; d <= dEnd; d++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						if (matrix[d * height * width + h * width + w] == before)
							matrix[d * height * width + h * width + w] = after;
					}
				}
			}
	}
    
    /** count number of domains in each domain types and add membrane to adjacents. */
	private HashMap<String,Integer> hashDomainNum;
    
    /**
     * Creates the membrane.
     */
    public void createMembrane(){
    	countDomain();
    	addMembrane();
    }

    /**
     * Count number of domains in each domain types and store the number to
	 * the hashmap (hashDomainNum&lt;String domain type, Integer count number&gt;).
     */
    public void countDomain(){
    	hashDomainNum = new HashMap<String,Integer>();
    	Integer temp;
    	for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			temp = hashSampledValue.get(e.getKey());
			hashDomainNum.put(e.getKey(), Collections.frequency(hashPix.values(), temp));
		}
    }

    /** The list of adjacent pixels. */
    private ArrayList<ArrayList<Integer>> adjacentsPixel;
    
    /** The list of adjacent domains. */
    private ArrayList<ArrayList<String>> adjacentsList;
    
    /**
     * Adds a membrane between two different domains.
     */
    public void addMembrane(){
    	adjacentsPixel = new ArrayList<ArrayList<Integer>>();
        adjacentsList = new ArrayList<ArrayList<String>>();
        int lower, higher;
        //adds the membrane 					may need changes in the future
		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height - 1; i++) {
				for (int j = 0; j < width - 1; j++) {
					// right
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + i * width + j + 1)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						lower = getLowerLabel(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]);
						higher = getHigherLabel(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]);
						temp.add(higher); temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);
					}

					// down
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + (i + 1) * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						lower = getLowerLabel(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]);
						higher = getHigherLabel(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]);
						temp.add(higher); temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);
					}
					
					//above
					if ( d != depth -1 && checkAdjacent(d * height * width + i * width + j, (d + 1) * height * width + i * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						lower = getLowerLabel(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]);
						higher = getHigherLabel(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]);
						temp.add(higher); temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);	
					}
				}
			}
		}
    }
    
    /**
     * Returns the label from given two labels which has lower pixel value.
     *
     * @param dom1 the label of domain 1
	 * @param dom2 the label of domain 2
     * @return the label which has lower pixel value
     */
    private int getLowerLabel(int dom1, int dom2){
    	int min = Math.min(hashPix.get(dom1), hashPix.get(dom2));
    	if(min  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    }
    
    /**
	 * Returns the label from given two labels which has higher pixel value.
	 *
	 * @param dom1 the label of domain 1
	 * @param dom2 the label of domain 2
	 * @return the label which has higher pixel value
	 */
	private int getHigherLabel(int dom1, int dom2){
    	int max = Math.max(hashPix.get(dom1), hashPix.get(dom2));
    	if(max  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    }

	/**
	 * Checks the given two labels are already registered in adjacentsPixel.
	 *
	 * @param dom1 the label of domain 1
	 * @param dom2 the label of domain 2
	 * @return true, if the pixel value of domain 1 has higher value than that of domain 2 in adjacentsPixel
	 */
	private boolean hasLabel(int dom1, int dom2) {
		if(adjacentsPixel.isEmpty()) return false;
		
		for (ArrayList<Integer> i : adjacentsPixel) {
			if (i.get(0) == dom1 && i.get(1) == dom2) {
				return true;
			}
		}
		return false;
	}
    
	/**
	 * Check adjacent pixels have different pixel values.
	 * If the adjacent pixel has different label with origin pixel, and the origin pixel and next pixel
	 * are already registered in adjacentsPixel.
	 *
	 * @param org the index of origin pixel in an 1D array
	 * @param next the index of next pixel in an 1D array
	 * @return true, if two pixel has different label and both pixels are registered in adjacentsPixel
	 */
	private boolean checkAdjacent(int org, int next){
		if(matrix[org] != matrix[next] &&
				!hasLabel(getHigherLabel(matrix[next], matrix[org]), getLowerLabel(matrix[next], matrix[org])))
			return true;
		 else 
			return false;
	}

	/**
	 * Add a membrane between given two labels (domains).
	 *
	 * @param bignum the label of pixel which has higher value
	 * @param smallnum the label of pixel which has lower value
	 */
	private void addmem(Integer bignum, Integer smallnum){
		String big ,small;

		big = getKeyFromValue(hashSampledValue, hashPix.get(bignum));
		small = getKeyFromValue(hashSampledValue, hashPix.get(smallnum));
		String buf = big + "_" + small + "_membrane";

		ArrayList<String> adjacentDom = new ArrayList<String>();
		adjacentDom.add(big + getIndexLabel(bignum));
		adjacentDom.add(small + getIndexLabel(smallnum));
		adjacentsList.add(adjacentDom);

		if(!hashDomainTypes.containsKey(buf)){
			if(depth > 1) hashDomainTypes.put(buf,2);
			else hashDomainTypes.put(buf,1);
			hashDomainNum.put(buf,1);
		} else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	/**
	 * Gets the index of given label as string.
	 *
	 * @param label the label of the pixel
	 * @return count.toString() the index of label
	 */
	private String getIndexLabel(int label){
		Integer count = 0;
		
		for(Entry<Integer, Integer> e : hashPix.entrySet()){
			if(e.getKey().equals(label))
				break;
			if(e.getValue().equals(hashPix.get(label)))
				count++;
		}
		
		return count.toString();
	}
	
	/**
	 * Gets the key from given value.
	 *
	 * @param hash the hashmap
	 * @param val the value
	 * @return str the key which contains the given value as string
	 */
	private String getKeyFromValue(HashMap<String, Integer> hash, Integer val){
		String str = "";
		
		for(Entry<String,Integer> e :hash.entrySet()){
			if(e.getValue().equals( val ))
				str = e.getKey();
		}
		
		return str;
	}
	
	/**
	 * Creates the hashmap of domain InteriorPoint of spatial image.
	 */
	private void createDomInteriorPt(){
		for(Entry<Integer,Point3d> e : hashLabelPt.entrySet()){
			int pixelVal = hashPix.get(e.getKey());
			String domName = getKeyFromValue(hashSampledValue, pixelVal) + getIndexLabel(e.getKey());
			hashDomInteriorPt.put(domName, e.getValue());
		}
	}
}
