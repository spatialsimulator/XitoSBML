import java.util.HashMap;


public class RawSpatialImage {
	byte[] raw;
	int width;
	int height;
	int depth;
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	
	public RawSpatialImage() {
		super();
	}
	
	public RawSpatialImage(byte[] raw, int w, int h, int d, HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValue) {
		this();
		this.raw = raw;
		this.width = w;
		this.height = h;
		this.depth = d;
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValue = hashSampledValue;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
