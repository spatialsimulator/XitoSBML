import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.vecmath.Color3f;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij3d.Content;
import ij3d.Image3DUniverse;


public class viewer {
	Image3DUniverse univ;
	private Vector<Color3f> colors = new Vector<Color3f>();
	int width;
	int height;
	int depth;
	HashMap<String, Integer> hashDoms;
	HashMap<String, ImagePlus> hashImg = new HashMap<String, ImagePlus>();
	byte[] rawMat;
	
	void view(SpatialImage spImg ){
		univ = new Image3DUniverse();
		this.width = spImg.width;
		this.height = spImg.height;
		this.depth = spImg.depth;
		this.hashDoms = spImg.hashSampledValue;
		this.rawMat = spImg.raw;
		separateImg();
		setColors(hashImg.size());
		setImages();
	}
	
	void setImages(){
		int i = 0;
		for(Entry<String, ImagePlus> e : hashImg.entrySet()){
			Content c = univ.addMesh(e.getValue(), colors.get(i++), e.getKey(), 0, new boolean[] {true,true,true}, 3);
			c.setShaded(false);
		}
		univ.show();
	}
	
	void setColors(int size){
		for(int i = 0 ; i <= size; i++){
			switch(i){
				case 0:
					colors.add(new Color3f(1f,0,0));
					break;
				case 1:
					colors.add(new Color3f(0,1f,0));
					break;
				case 2:
					colors.add(new Color3f(0,0,1f));
					break;
				case 4:
					colors.add(new Color3f(1f,1f,0));
					break;
				case 5:
					colors.add(new Color3f(0,1f,1f));
					break;
				case 6:
					colors.add(new Color3f(1f,0,1f));
					break;
				default:	
					colors.add(new Color3f(0,0,0));
					break;
			}
		}
	}
	
	void separateImg(){
		for(Entry<String, Integer> e : hashDoms.entrySet()){
			if(e.getValue().equals(0))
				continue;
			ImagePlus tempimg = new ImagePlus();
			tempimg.setStack(createLabelImage(e.getValue()));
			hashImg.put(e.getKey(), tempimg);
		}
	}

	ImageStack createLabelImage(int pixVal){
		byte[] pixels = new byte[width*height*depth];
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if(rawMat[d * height * width + h * width + w] == (byte)pixVal)
						pixels[d * height * width + h * width + w] = (byte)255;
				}
			}
		}
		return setStack(pixels);
	}
	
	private ImageStack setStack(byte[] pixels){
		ImageStack imstack = new ImageStack(width, height);
		for(int d = 0 ; d < depth ; d++){
			byte matrix[] = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			imstack.addSlice(new ByteProcessor(width,height,matrix,null));
		}
		return imstack;
	}
}
