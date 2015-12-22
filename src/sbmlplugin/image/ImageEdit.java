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
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Point3f;


public class ImageEdit {
	private ArrayList<Integer> labelList;
	private HashMap<Integer,Integer> hashLabelNum;
	private HashMap<String,Integer> hashDomainTypes;
	private HashMap<String,Integer> hashSampledValue;
	private int width;
	private int height;
	private int depth;
	private int size;
	private byte[] pixels;
	private int[] matrix;	
	private int[] invert;
	private HashMap<Integer, Integer>  hashPix = new HashMap<Integer,Integer>(); //label + pixel vlaue
	private HashMap<Integer, Point3f> hashLabelPt = new HashMap<Integer,Point3f>();  //label + coordinates
    private HashMap<String, Point3f> hashDomInteriorPt = new HashMap<String,Point3f>();  //domain name + coordinates
	
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
        spImg.sethashDomainNum(hashDomainNum);
        spImg.setAdjacentsList(adjacentsList);
        createDomInteriorPt();
        spImg.setHashDomInteriorpt(hashDomInteriorPt);
    }
    
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
    
    public void domtype(HashMap<Integer,Integer> num){
    	hashLabelNum = new HashMap<Integer,Integer>();
    	for(Entry<Integer, Integer> e : num.entrySet()){
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
	 
	private int labelCount = 1;
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

	private HashMap<String,Integer> hashDomainNum;
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
    private ArrayList<ArrayList<String>> adjacentsList;
    
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
			hashDomainTypes.put(buf,2);
			hashDomainNum.put(buf,1);
		}else{
			int temp = hashDomainNum.get(buf);
			hashDomainNum.put(buf,++temp);
		}
	}

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
	
	private String getKeyFromValue(HashMap<String, Integer> hash, Integer val){
		String str = "";
		
		for(Entry<String,Integer> e :hash.entrySet()){
			if(e.getValue().equals( val ))
				str = e.getKey();
		}
		
		return str;
	}
	
	private void createDomInteriorPt(){
		for(Entry<Integer,Point3f> e : hashLabelPt.entrySet()){
			int pixelVal = hashPix.get(e.getKey());
			String domName = getKeyFromValue(hashSampledValue, pixelVal) + getIndexLabel(e.getKey());
			hashDomInteriorPt.put(domName, e.getValue());
		}
	}
}
