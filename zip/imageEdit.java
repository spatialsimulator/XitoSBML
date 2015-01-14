import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/*
 * Label the image and count number of domains
 */
public class ImageEdit {
	ImagePlus image;
	ArrayList<Integer> labelList;
	HashMap<Integer,Integer> hashLabelNum;
    HashMap<String,Integer> hashDomainTypes;
    HashMap<String,Integer> hashSampledValue;
	int width;
    int height;
    int depth;
    int size;
    byte[] pixels;
    int[] matrix;	
    int[] invert;
	HashMap<Integer, Integer>  hashPix = new HashMap<Integer,Integer>();
    
    ImageEdit(ImagePlus image,HashMap<String,Integer> hashDomainTypes, HashMap<String,Integer> hashSampledValue){
    	this.image = image;    	
        this.width = image.getWidth();                                //obtain width of image
        this.height = image.getHeight();                              //obtain height of image
        this.depth = image.getStackSize();								//obtain number of slices
        this.size = width * height * depth;
        this.hashDomainTypes = hashDomainTypes;
        this.hashSampledValue = hashSampledValue;
        
        copyMat();

        listVal();
        invertMat();
        label();
        createMembrane();
    }
    
    //copies the matrix into array pixel
    private void copyMat(){
    	byte[] slice;   
    	pixels = new byte[width * height * depth];
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])image.getStack().getPixels(i);
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
        }
    }
    
    //create a list of pixel value
    private void listVal(){
    	int temp;
    	labelList = new ArrayList<Integer>();
    	for (int i = 0 ; i < size ; i++) {
			temp = pixels[i] & 0xFF;  			//convert byte to int
			if (!hasLabel(temp)) {					 // see below
				labelList.add(new Integer(temp));
			}
		}
    	Collections.sort(labelList);
    	//IJ.log(labelList.toString());
    }
    
    private boolean hasLabel(int label){
    	for(Integer i : labelList){
    		if(i.equals(label))
    			return true;
    	}
    	return false;
    }
 
    //count number of domains in each domaintype and store to hashlabelnum
    public void countdomtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Integer i : labelList)
			hashLabelNum.put(i, num.get(i) % 10);
  
    System.out.println(hashLabelNum.toString());
    }

    public void domtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Entry e : num.entrySet()){
    		if(!hashLabelNum.containsKey(e.getValue()))
    			hashLabelNum.put((Integer) e.getValue(), Collections.frequency(num.values(), e.getValue()));
    	}
			
    }

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
	 
	int labelCount = 1;
	public void label(){
			for (int d = 0; d < depth; d++) {
				for (int h = 0; h < height; h++) {
					for (int w = 0; w < width; w++) {
						if (invert[d * height * width + h * width + w] == 1) {
							matrix[d * height * width + h * width + w] = setLabel(w, h, d, pixels[d * height * width + h * width + w] & 0xFF);
						}else{
							matrix[d * height * width + h * width + w] = setbackLabel(w, h, d, pixels[d * height * width + h * width + w] & 0xFF);
						}
					}
				}
			}
		domtype(hashPix);
	}
	
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

	
	private int setbackLabel(int  w , int h, int d, int pixVal){
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
    
    /**
     * count number of domains in each domaintypes and add membrane to adjacents
     */

	HashMap<String,Integer> hashDomainNum;
    public void createMembrane(){
    	countDomain();
    	addMembrane();
    }
    

    public void countDomain(){
    	hashDomainNum = new HashMap<String,Integer>();
    	Integer temp;
    	for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			temp = hashSampledValue.get(e.getKey());
			hashDomainNum.put(e.getKey(), Collections.frequency(hashPix.values(), temp));
		}
    }

    
    private ArrayList<ArrayList<Integer>> adjacentsPixel;
    ArrayList<ArrayList<String>> adjacentsList;
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
						temp.add(higher);temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);
					}

					// down
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + (i + 1) * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						lower = getLowerLabel(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]);
						higher = getHigherLabel(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]);
						temp.add(higher);temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);
					}
					
					//above
					if ( d != depth -1 && checkAdjacent(d * height * width + i * width + j, (d + 1) * height * width + i * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						lower = getLowerLabel(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]);
						higher = getHigherLabel(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]);
						temp.add(higher);temp.add(lower);
						adjacentsPixel.add(temp);
						addmem(higher,lower);	
					}
				}
			}
		}
    }
    
    private int getLowerLabel(int dom1, int dom2){
    	int min = Math.min(hashPix.get(dom1), hashPix.get(dom2));
    	
    	if(min  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    	
    }
    
    private int getHigherLabel(int dom1, int dom2){	
    	int max = Math.max(hashPix.get(dom1), hashPix.get(dom2));
    	if(max  == hashPix.get(dom1) )
    		return dom1;
    	else 
    		return dom2;
    	}
	private boolean hasLabel(int dom1, int dom2) {
		if(adjacentsPixel.isEmpty()) return false;
		
		for (ArrayList<Integer> i : adjacentsPixel) {
			if (i.get(0) == dom1 && i.get(1) == dom2) {
				return true;
			}
		}
		return false;
	}
    
	private boolean checkAdjacent(int org, int next){
		if(matrix[org] != matrix[next] && !hasLabel(getHigherLabel(matrix[next], matrix[org]), getLowerLabel(matrix[next], matrix[org]))){
			return true;
		} else {
			return false;
		}
	}

	public void addmem(Integer bignum, Integer smallnum){
		String big = null,small = null;
		for(Entry<String,Integer> e : hashSampledValue.entrySet()){
			if(e.getValue().equals( hashPix.get(bignum) )){
				big = e.getKey();
			}
			if(e.getValue().equals(hashPix.get(smallnum))){
				small = e.getKey();
			}
		}
		String buf = big + "_" + small + "_membrane";

		ArrayList<String> adjacentDom = new ArrayList<String>();
		adjacentDom.add(big + getIndexLabel(bignum));
		adjacentDom.add(small + getIndexLabel(smallnum));
		adjacentsList.add(adjacentDom);

		if(!hashDomainTypes.containsKey(buf)){
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

	private String getIndexLabel(int label){
		Integer count = 0;
		
		for(Entry e : hashPix.entrySet()){
			if(e.getKey().equals(label))
				break;
			if(e.getValue().equals(hashPix.get(label)))
				count++;
		}
		
		return count.toString();
	}
	
	
	public ArrayList<ArrayList<Integer>> getAdjacentsPixel() {
		return adjacentsPixel;
	}

	public void printPixel() {
		for (int i = 0; i < depth; i++) {
			for (int j = 0; j < height; j++) {
				for (int k = 0; k < width; k++) {
					System.out.print(pixels[i * height * width + k * width + k] & 0xFF);
				}
				System.out.println("");
			}
			System.out.println("");
		}
	}


}
