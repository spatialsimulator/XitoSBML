

import java.util.ArrayList;
import java.util.HashMap;


public class RawSpatialImage {		//image data
	byte[] raw;
	int width;
	int height;
	int depth;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	HashMap<Integer,Integer> hashDomainNum;
	ArrayList<ArrayList<Integer>> adjacentsList;
	
	public RawSpatialImage() {
		super();				//call constructor of super class
	}

	public RawSpatialImage(byte[] raw, int w, int h, int d, HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValue, HashMap<Integer,Integer> hashDomainNum, ArrayList<ArrayList<Integer>> adjacentsList) {
		this();										//call constructor that is above
		this.raw = raw;
		this.width = w;
		this.height = h;
		this.depth = d;
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValue = hashSampledValue;
		this.hashDomainNum = hashDomainNum;
		this.adjacentsList = adjacentsList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
