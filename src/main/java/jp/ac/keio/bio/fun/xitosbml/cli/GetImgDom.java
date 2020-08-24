package jp.ac.keio.bio.fun.xitosbml.cli;

import java.util.HashMap;
import java.util.Map.Entry;

import ij.ImagePlus;

public class GetImgDom {
	/** The hashmap of domain types. HashMap&lt;String, Integer&gt; */
	private HashMap<String, Integer> hashDomainTypes;

	/**
	 * The hashmap of sampled value of spatial image. HashMap&lt;String, Integer&gt;
	 */
	private HashMap<String, Integer> hashSampledValues;

	/** The hashmap of domain file. HashMap&lt;String, ImagePlus&gt; */
	public HashMap<String, ImagePlus> hashDomFile;

	public ImagePlus imager;

	public GetImgDom(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues,
			ImagePlus imager) {
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		this.imager = imager;

		actionPerf();

	}

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

	public HashMap<String, ImagePlus> getDomFile() {
		// importImg();
		HashMap<String, ImagePlus> hashDomFile = new HashMap<String, ImagePlus>();
		hashDomFile.put("Cytosol".toString(), this.imager);
		// System.out.println(hashDomFile.values());
		return hashDomFile;
	}

	public void actionPerf() {

		hashDomFile = getDomFile();
		hashDomainTypes = getDomainTypes();
		hashSampledValues = getSampledValues();

	}

}
