package sbmlplugin.image;

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Point3f;


// TODO: Auto-generated Javadoc
/**
 * The Class ImageEdit.
 */
public class ImageEdit {
	
	/** The label list. */
	private ArrayList<Integer> labelList;
	
	/** The hash label num. */
	private HashMap<Integer,Integer> hashLabelNum;
	
	/** The hash domain types. */
	private HashMap<String,Integer> hashDomainTypes;
	
	/** The hash sampled value. */
	private HashMap<String,Integer> hashSampledValue;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The depth. */
	private int depth;
	
	/** The size. */
	private int size;
	
	/** The pixels. */
	private byte[] pixels;
	
	/** The matrix. */
	private int[] matrix;	
	
	/** The invert. */
	private int[] invert;
	
	/** The hash pix. */
	private HashMap<Integer, Integer>  hashPix = new HashMap<Integer,Integer>(); //label + pixel vlaue
	
	/** The hash label pt. */
	private HashMap<Integer, Point3f> hashLabelPt = new HashMap<Integer,Point3f>();  //label + coordinates
    
    /** The hash dom interior pt. */
    private HashMap<String, Point3f> hashDomInteriorPt = new HashMap<String,Point3f>();  //domain name + coordinates
	
    /**
     * Instantiates a new image edit.
     *
     * @param image the image
     * @param hashDomainTypes the hash domain types
     * @param hashSampledValue the hash sampled value
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
     * Instantiates a new image edit.
     *
     * @param spImg the sp img
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
     * List val.
     */
    //create a list of unique pixel values
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
     * Domtype.
     *
     * @param num the num
     */
    public void domtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Entry<Integer, Integer> e : num.entrySet()){
    		if(!hashLabelNum.containsKey(e.getValue()))
    			hashLabelNum.put((Integer) e.getValue(), Collections.frequency(num.values(), e.getValue()));
    	}	
    }

	 /**
 	 * Invert mat.
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
	 
	/** The label count. */
	private int labelCount = 1;
	
	/**
	 * Label.
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
	 * Sets the label.
	 *
	 * @param w the w
	 * @param h the h
	 * @param d the d
	 * @param pixVal the pix val
	 * @return the int
	 */
	private int setLabel(int  w , int h, int d, int pixVal){
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
			hashLabelPt.put(labelCount, new Point3f(w,h,d));
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
	 * Rewrite label.
	 *
	 * @param dEnd the d end
	 * @param after the after
	 * @param before the before
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
    
    /** count number of domains in each domaintypes and add membrane to adjacents. */

	private HashMap<String,Integer> hashDomainNum;
    
    /**
     * Creates the membrane.
     */
    public void createMembrane(){
    	countDomain();
    	addMembrane();
    }

    /**
     * Count domain.
     */
    public void countDomain(){
    	hashDomainNum = new HashMap<String,Integer>();
    	Integer temp;
    	for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			temp = hashSampledValue.get(e.getKey());
			hashDomainNum.put(e.getKey(), Collections.frequency(hashPix.values(), temp));
		}
    }

    /** The adjacents pixel. */
    private ArrayList<ArrayList<Integer>> adjacentsPixel;
    
    /** The adjacents list. */
    private ArrayList<ArrayList<String>> adjacentsList;
    
    /**
     * Adds the membrane.
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
     * Gets the lower label.
     *
     * @param dom1 the dom 1
     * @param dom2 the dom 2
     * @return the lower label
     */
    private int getLowerLabel(int dom1, int dom2){
    	int min = Math.min(hashPix.get(dom1), hashPix.get(dom2));
    	if(min  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    }
    
    /**
     * Gets the higher label.
     *
     * @param dom1 the dom 1
     * @param dom2 the dom 2
     * @return the higher label
     */
    private int getHigherLabel(int dom1, int dom2){	
    	int max = Math.max(hashPix.get(dom1), hashPix.get(dom2));
    	if(max  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    }

	/**
	 * Checks for label.
	 *
	 * @param dom1 the dom 1
	 * @param dom2 the dom 2
	 * @return true, if successful
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
	 * Check adjacent.
	 *
	 * @param org the org
	 * @param next the next
	 * @return true, if successful
	 */
	private boolean checkAdjacent(int org, int next){
		if(matrix[org] != matrix[next] && !hasLabel(getHigherLabel(matrix[next], matrix[org]), getLowerLabel(matrix[next], matrix[org])))
			return true;
		 else 
			return false;		
	}

	/**
	 * Addmem.
	 *
	 * @param bignum the bignum
	 * @param smallnum the smallnum
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
//			if(depth > 1) hashDomainTypes.put(buf,2);
//			else hashDomainTypes.put(buf,1);
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	/**
	 * Gets the index label.
	 *
	 * @param label the label
	 * @return the index label
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
	 * Gets the key from value.
	 *
	 * @param hash the hash
	 * @param val the val
	 * @return the key from value
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
	 * Creates the dom interior pt.
	 */
	private void createDomInteriorPt(){
		for(Entry<Integer,Point3f> e : hashLabelPt.entrySet()){
			int pixelVal = hashPix.get(e.getKey());
			String domName = getKeyFromValue(hashSampledValue, pixelVal) + getIndexLabel(e.getKey());
			hashDomInteriorPt.put(domName, e.getValue());
		}
	}
}
