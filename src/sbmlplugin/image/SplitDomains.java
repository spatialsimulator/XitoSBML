package sbmlplugin.image;

import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 27, 2015
 */
public class SplitDomains {
	private int width;
	private int height;
	private int depth;
	private byte[] raw;
	private ImageStack altStack;
	private byte cytVal;
	private byte delTarget;
	
	public SplitDomains(SpatialImage spImg, String targetDomain){
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.raw = spImg.getRaw();
		
		createDomainToCheck(spImg.getHashSampledValue(), targetDomain);
		checkDomain();
		createNewStack();
	}

	private void createDomainToCheck(HashMap<String, Integer> hashSampledValue, String targetDomain){
		cytVal =  hashSampledValue.get("Cytosol").byteValue();
		delTarget = hashSampledValue.get(targetDomain).byteValue();
	}
	
	private void checkDomain(){
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if(	delTarget == (raw[d * height * width + h * width + w])){
						checkAdjacents(w,h,d, raw[d * height * width + h * width + w]);
					}
				}
			}
		}
	}
	
	//assumes its in cytosol
	private void checkAdjacents(int w, int h, int d, byte pixVal) {
		List<Byte> adjVal = new ArrayList<Byte>();

		// check left
		if (w != 0 && raw[d * height * width + h * width + w - 1] != cytVal && raw[d * height * width + h * width + w - 1] != pixVal)
			adjVal.add(raw[d * height * width + h * width + w - 1]);

		// check right
		if (w != width - 1 && raw[d * height * width + h * width + w + 1] != cytVal && raw[d * height * width + h * width + w + 1] != pixVal)
			adjVal.add(raw[d * height * width + h * width + w + 1]);

		// check up
		if (h != 0 && raw[d * height * width + (h - 1) * width + w] != cytVal && raw[d * height * width + (h - 1) * width + w] != pixVal)
			adjVal.add(raw[d * height * width + (h - 1) * width + w]);

		// check down
		if (h != height - 1 && raw[d * height * width + (h + 1) * width + w] != cytVal && raw[d * height * width + (h - 1) * width + w] != pixVal)
			adjVal.add(raw[d * height * width + (h + 1) * width + w]);

		// check below
		if (d != 0 && raw[(d - 1) * height * width + h * width + w] != cytVal && raw[(d - 1) * height * width + h * width + w] != pixVal)
			adjVal.add(raw[(d - 1) * height * width + h * width + w]);

		// check above
		if (d < depth - 1 && raw[(d + 1) * height * width + h * width + w] != cytVal && raw[(d + 1) * height * width + h * width + w] != pixVal)
			adjVal.add(raw[(d + 1) * height * width + h * width + w]);

		
		if (adjVal.isEmpty())
			return;
		
		else
			raw[d * height * width + h * width + w] = cytVal;
	}
	
	private void createNewStack(){
		altStack = new ImageStack(width, height);
		
		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
	}
	
	public ImageStack getStackImage(){
		return altStack;
	}
}
