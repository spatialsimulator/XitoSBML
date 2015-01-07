import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
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
	private int lwidth;
	private int lheight;
	private int ldepth;
    byte[] pixels;
    int[] matrix;	
    int[] invert;
	HashMap<Integer, Byte>  hashPix = new HashMap<Integer,Byte>();
    
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
        labelMat();
 //       invertMat();
   //     label();
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
    
    private void labelMat(){
      	matrix = new int[size];
    	HashMap<Integer,Integer> num = new HashMap<Integer,Integer>();  //labels the object with different numbers
        int label = 0;
        for(int i = 0 ; i < labelList.size(); i++){
        	num.put(labelList.get(i), label);
        	label += 10;
        }
        
		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (matrix[d * height *  width + i * width + j] == 0 && pixels[d * height *  width + i * width + j] != 0) {
						label = num.get(pixels[d * height *  width + i * width + j] & 0xFF);
						matrix[d * height *  width + i * width + j] = label;
						recurs(i,j,d, label);
						num.put(pixels[d * height *  width + i * width + j] & 0xFF, ++label);
					}
					if (matrix[d * height *  width + i * width + j] == 0 && pixels[d * height *  width + i * width + j] == 0) {
						label = num.get(pixels[d * height *  width + i * width + j] & 0xFF) + 1;
						matrix[d * height *  width + i * width + j] = label;
						recurs(i,j,d, label);
						num.put(pixels[d * height *  width + i * width + j] & 0xFF, ++label);
					}
				}
			}
		}
		num.put(0, num.get(0) - 1);		//assumes extracellular is only one
        countdomtype(num);
    }
    
    private void recurs(int i , int j, int d, int val){
    	Stack<Integer> block = new Stack<Integer>();
		block.push(i);
		block.push(j);
		block.push(d);

		while(!block.isEmpty()){
			d = block.pop();
			j = block.pop();
			i = block.pop();

			//check right
			if(j != width - 1 && pixels[d * height * width + i * width + j + 1] == pixels[d * height * width + i * width + j] && matrix[d * height * width + i * width + j + 1] == 0){
			//	matrix[d * height * width + i * width + j + 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j+1);
				block.push(d);
			}

			//check left
			if(j != 0 && pixels[d * height * width + i * width + j - 1] == pixels[d * height * width + i * width + j] && matrix[d * height * width + i * width + j - 1] == 0){
			//	matrix[d * height * width + i * width + j - 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j-1);
				block.push(d);
			}

			//check down
			if(i != height - 1 && pixels[d * height * width + (i+1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i+1) * width + j] == 0){
			//	matrix[d * height * width + (i + 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i+1);
				block.push(j);
				block.push(d);
			}

			//check up
			if(i != 0 && pixels[d * height * width + (i-1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i-1) * width + j] == 0){
			//	matrix[d * height * width + (i - 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i-1);
				block.push(j);
				block.push(d);
			}
			
			//check above
			if(d != depth - 1 && pixels[d * height * width + i * width + j] == pixels[(d+1) * height * width + i * width + j] && matrix[(d+1) * height * width + i * width + j] == 0){
			//	matrix[(d+1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d+1);
			}
			
			//check below
			if(d != 0 && pixels[d * height * width + i * width + j] == pixels[(d-1) * height * width + i * width + j] && matrix[(d-1) * height * width + i * width + j] == 0){
			//	matrix[(d-1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d-1);
			}
		
			matrix[d * height * width + i * width + j] = val;
		}
    }

    //count number of domains in each domaintype and store to hashlabelnum
    public void countdomtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Integer i : labelList)
			hashLabelNum.put(i, num.get(i) % 10);
    }

    
	
	private int setLabel(int  w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();
		//check left			
		if(matrix[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(matrix[d * lheight * lwidth + h * lwidth + w - 1]) == (byte)0)
			adjVal.add(matrix[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(matrix[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(matrix[d * lheight * lwidth + (h-1) * lwidth + w]) == (byte)0)
			adjVal.add(matrix[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && matrix[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(matrix[(d-1) * lheight * lwidth + h * lwidth + w]) == (byte)0)
			adjVal.add(matrix[(d-1) * lheight * lwidth + h * lwidth + w]);
		
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


	 private void invertMat(){
		lwidth = width + 2;
		lheight = height + 2;
		if(depth < 3) ldepth = depth;
		else 			ldepth = depth + 2;
		
		invert = new int[lwidth * lheight * ldepth]; 
		matrix = new int[lwidth * lheight * ldepth];
		if (ldepth > depth) {
			for (int d = 0; d < ldepth; d++) {
				for (int h = 0; h < lheight; h++) {
					for (int w = 0; w < lwidth; w++) {
						if (d == 0 || d == ldepth - 1 || h == 0 || h == lheight - 1 || w == 0 || w == lwidth - 1) {
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
							matrix[d * lheight * lwidth + h * lwidth + w] = 1;
							continue;
						}

						if (pixels[(d - 1) * height * width + (h - 1) * width + w - 1] == 0)
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
						else
							invert[d * lheight * lwidth + h * lwidth + w] = 0;
					}
				}
			}
		} else {
			for (int d = 0; d < ldepth; d++) {
				for (int h = 0; h < lheight; h++) {
					for (int w = 0; w < lwidth; w++) {
						if(h == 0 || h == lheight - 1 || w == 0 || w == lwidth - 1){
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
							matrix[d * lheight * lwidth + h * lwidth + w] = 1;
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
	 
	int labelCount = 2;
	public void label(){
		hashPix.put(1, (byte)0);

		if (ldepth > depth) {
			for (int d = 1; d < ldepth - 1; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1) {
							matrix[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}else{
							matrix[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}else{
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1) {
							matrix[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}else{
							matrix[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}
	}
	
	private int setbackLabel(int  w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();

		//check left			
		if(matrix[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(matrix[d * lheight * lwidth + h * lwidth + w - 1]) != (byte)0)
			adjVal.add(matrix[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(matrix[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(matrix[d * lheight * lwidth + (h-1) * lwidth + w]) != (byte)0)
			adjVal.add(matrix[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && matrix[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(matrix[(d-1) * lheight * lwidth + h * lwidth + w]) != (byte)0)
			adjVal.add(matrix[(d-1) * lheight * lwidth + h * lwidth + w]);
		
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
		if (ldepth > depth) {
			for (int d = 1; d <= dEnd; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (matrix[d * lheight * lwidth + h * lwidth + w] == before)
							matrix[d * lheight * lwidth + h * lwidth + w] = after;					}
				}
			}
		}else{
			for (int d = 0; d <= dEnd; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (matrix[d * lheight * lwidth + h * lwidth + w] == before)
							matrix[d * lheight * lwidth + h * lwidth + w] = after;
					}
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
			if(hashLabelNum.containsKey(temp))
				hashDomainNum.put(e.getKey(), hashLabelNum.get(temp));
			else
				hashDomainNum.put(e.getKey(), 0);
		}
		/*
    	for(Entry<String,Integer> e : hashDomainTypes.entrySet()){
			temp = hashSampledValue.get(e.getKey()).byteValue();
			hashDomainNum.put(e.getKey(), Collections.frequency(hashPix.values(), temp));
		}
		*/
    	System.out.println(hashDomainNum.toString());
    }

    
    private static ArrayList<ArrayList<Integer>> adjacentsPixel;
    ArrayList<ArrayList<String>> adjacentsList;
    public void addMembrane(){
    	adjacentsPixel = new ArrayList<ArrayList<Integer>>();
        adjacentsList = new ArrayList<ArrayList<String>>();
        //adds the membrane 					may need changes in the future
		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height - 1; i++) {
				for (int j = 0; j < width - 1; j++) {
					// right
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + i * width + j + 1)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmem(
								Math.max(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
					}

					// down
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + (i + 1) * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmem(
								Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
					}
					
					//above
					if ( d != depth -1 && checkAdjacent(d * height * width + i * width + j, (d + 1) * height * width + i * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						adjacentsPixel.add(temp);
						addmem(
								Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
					}
				}
			}
		}
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
		if(matrix[org] != matrix[next] && !hasLabel(Math.max(matrix[next], matrix[org]), Math.min(matrix[next], matrix[org]))){
			return true;
		} else {
			return false;
		}
	}

	public void addmem(Integer bignum, Integer smallnum){
		String big = null,small = null;

		for(Entry<String,Integer> e : hashSampledValue.entrySet()){
			if(e.getValue().equals( labelList.get(bignum / 10) )){
				big = e.getKey();
			}
			if(e.getValue().equals(labelList.get(smallnum / 10))){
				small = e.getKey();
			}
		}
		String buf = big + "_" + small + "_membrane";

		ArrayList<String> adjacentDom = new ArrayList<String>();
		adjacentDom.add(big + bignum % 10);
		adjacentDom.add(small + smallnum % 10);
		adjacentsList.add(adjacentDom);

		if(!hashDomainTypes.containsKey(buf)){
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
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
