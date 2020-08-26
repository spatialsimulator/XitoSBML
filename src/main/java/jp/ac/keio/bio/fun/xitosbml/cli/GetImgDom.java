package jp.ac.keio.bio.fun.xitosbml.cli;

import java.util.HashMap;
import java.util.Map.Entry;

import ij.ImagePlus;

/**
 * 
 * The class GetImgDom.
 * 
 * This class is responsible for associating the input image file with its
 * domain. It implements methods for getting the hashmap of domain types, the
 * hashmap of sampled value of spatial image, and the hashmap of domain file.
 * Date Created: August 9, 2020
 * 
 * @author Medha Bhattacharya
 * @author Akira Funahashi
 * @author Kaito Ii
 * @author Yuta Tokuoka
 *
 */

public class GetImgDom {
	/** The hashmap of domain types. HashMap&lt;String, Integer&gt; */
	private HashMap<String, Integer> hashDomainTypes;

	/**
	 * The hashmap of sampled value of spatial image. HashMap&lt;String, Integer&gt;
	 */
	private HashMap<String, Integer> hashSampledValues;

	/** The hashmap of domain file. HashMap&lt;String, ImagePlus&gt; */
	public HashMap<String, ImagePlus> hashDomFile;

	/** The input image as ImagePlus */
	public ImagePlus imager;

	/**
	 * Constructor for class GetImgDom.java.
	 * 
	 * @param hashDomainTypes   the hashmap of domain types. HashMap&lt;String,
	 *                          Integer&gt;
	 * @param hashSampledValues the hashmap of sampled value of spatial image.
	 *                          HashMap&lt;String, Integer&gt;
	 * @param imager            the ImagePlus object
	 */
	public GetImgDom(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues,
			ImagePlus imager) {
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		this.imager = imager;

		actionPerf();

	}

	/**
	 * Sets the datatable(hashmap of domain file) to the hashmap of domain types and
	 * returns the hashmap of domain types.
	 *
	 * @return the hashmap of domain types. HashMap&lt;String, Integer&gt;
	 * 
	 */
	public HashMap<String, Integer> getDomainTypes() {

		int dimension = 3;
		for (Entry<String, ImagePlus> e : hashDomFile.entrySet()) {
			if (e.getValue().getNSlices() == 1)
				dimension = 2;
			hashDomainTypes.put(e.getKey().toString(), dimension);
		}
		hashDomainTypes.put("Extracellular", dimension);
		return hashDomainTypes;
	}

	/**
	 * Sets the hashmap of sampled values and returns the hashmap of sampled values.
	 * The sampled value is calculated by the value of its domain type.
	 *
	 * @return the hashmap of sampled value of spatial image. HashMap&lt;String,
	 *         Integer&gt;
	 */
	public HashMap<String, Integer> getSampledValues() {
		int pixel = 255;
		int interval = 255 / hashDomFile.size();
		String s = "Cytosol";
		if (hashDomFile.containsKey(s)) {
			hashSampledValues.put(s, pixel);
			pixel -= interval;
		}
		hashSampledValues.put("Extracellular", 0);
		return hashSampledValues;
	}

	/*
	 * public void importImg() { HashMap<String, ImagePlus> hashDomFile = new
	 * HashMap<String, ImagePlus>(); hashDomFile.put("Cytosol".toString(),
	 * this.imager); System.out.println(hashDomFile.values()); }
	 */

	/**
	 * Gets the hashmap of domain file. HashMap&lt;String, ImagePlus&gt;
	 *
	 * @return the hashmap of domain file.
	 */
	public HashMap<String, ImagePlus> getDomFile() {
		// importImg();
		HashMap<String, ImagePlus> hashDomFile = new HashMap<String, ImagePlus>();
		hashDomFile.put("Cytosol".toString(), this.imager);
		// System.out.println(hashDomFile.values());
		return hashDomFile;
	}

	/**
	 * Assigns entries to hashDomFile, hashDomainTypes and hashSampledValues via
	 * getDomFile(), getDomainTypes() and getSampledValues() respectively.
	 */
	public void actionPerf() {
		hashDomFile = getDomFile();
		hashDomainTypes = getDomainTypes();
		hashSampledValues = getSampledValues();

	}

}
