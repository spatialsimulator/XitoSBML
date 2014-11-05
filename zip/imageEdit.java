import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map.Entry;

/*
 * Label the image and count number of domains
 */
public class imageEdit {
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
    static int matrix[];	
    
    imageEdit(){
    	
    }
    
    imageEdit(ImagePlus image,HashMap<String,Integer> hashDomainTypes, HashMap<String,Integer> hashSampledValue){
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
						recurs(i,j,d);
						num.remove(pixels[d * height *  width + i * width + j] & 0xFF);
						num.put(pixels[d * height *  width + i * width + j] & 0xFF, ++label);
					}
				}
			}
		}
		num.remove(0);
		num.put(0, 1);		//assumes extracellular is only one
        countdomtype(num);
    }
    
    private void recurs(int i , int j, int d){
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
				matrix[d * height * width + i * width + j + 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j+1);
				block.push(d);
			}

			//check left
			if(j != 0 && pixels[d * height * width + i * width + j - 1] == pixels[d * height * width + i * width + j] && matrix[d * height * width + i * width + j - 1] == 0){
				matrix[d * height * width + i * width + j - 1] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j-1);
				block.push(d);
			}

			//check down
			if(i != height - 1 && pixels[d * height * width + (i+1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i+1) * width + j] == 0){
				matrix[d * height * width + (i + 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i+1);
				block.push(j);
				block.push(d);
			}

			//check up
			if(i != 0 && pixels[d * height * width + (i-1) * width + j] == pixels[d * height * width + i * width + j] && matrix[d * height * width + (i-1) * width + j] == 0){
				matrix[d * height * width + (i - 1) * width + j] = matrix[d * height * width + i * width + j];
				block.push(i-1);
				block.push(j);
				block.push(d);
			}
			
			//check above
			if(d != depth - 1 && pixels[d * height * width + i * width + j] == pixels[(d+1) * height * width + i * width + j] && matrix[(d+1) * height * width + i * width + j] == 0){
				matrix[(d+1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d+1);
			}
			
			//check below
			if(d != 0 && pixels[d * height * width + i * width + j] == pixels[(d-1) * height * width + i * width + j] && matrix[(d-1) * height * width + i * width + j] == 0){
				matrix[(d-1) * height * width + i * width + j] = matrix[d * height * width + i * width + j];
				block.push(i);
				block.push(j);
				block.push(d-1);
			}
		}
    }

    //count number of domains in each domaintype and store to hashlabelnum
    public void countdomtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Integer i : labelList)
			hashLabelNum.put(i, num.get(i) % 10);
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
		for(Entry<String,Integer> e : hashDomainTypes.entrySet())
			hashDomainNum.put(e.getKey(), hashLabelNum.get(hashSampledValue.get(e.getKey())));
    }

    
    private static ArrayList<ArrayList<Integer>> adjacentsPixel;
    ArrayList<ArrayList<String>> adjacentsList;
    public void addMembrane(){
    	System.out.println("Adding membranes ...");
        setAdjacentsPixel(new ArrayList<ArrayList<Integer>>());	//holds which pixels are adjacents
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
						getAdjacentsPixel().add(temp);
						addmem(
								Math.max(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + i * width + j + 1], matrix[d * height * width + i * width + j]));
					}

					// down
					if (checkAdjacent(d * height * width + i * width + j, d * height * width + (i + 1) * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
						getAdjacentsPixel().add(temp);
						addmem(
								Math.max(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[d * height * width + (i + 1) * width + j], matrix[d * height * width + i * width + j]));
					}
					
					//above
					if ( d != depth -1 && checkAdjacent(d * height * width + i * width + j, (d + 1) * height * width + i * width + j)) {
						ArrayList<Integer> temp = new ArrayList<Integer>(2);
						temp.add(Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						temp.add(Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
						getAdjacentsPixel().add(temp);
						addmem(
								Math.max(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]),
								Math.min(matrix[(d + 1) * height * width + i * width + j], matrix[d * height * width + i * width + j]));
					}
				}
			}
		}
    }

	private static boolean hasLabel(int dom1, int dom2) {
		if(adjacentsPixel.isEmpty()) return false;
		
		for (ArrayList<Integer> i : adjacentsPixel) {
			if (i.get(0) == dom1 && i.get(1) == dom2) {
				return true;
			}
		}
		return false;
	}
    
	private static boolean checkAdjacent(int org, int next){
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

	public void setAdjacentsPixel(ArrayList<ArrayList<Integer>> adjacentsPixel) {
		imageEdit.adjacentsPixel = adjacentsPixel;
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
