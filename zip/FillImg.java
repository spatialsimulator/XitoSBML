import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;


public class FillImg {
	private ImagePlus image;
	private int width;
	private int height;
	private int depth;
	private int[] mask;
	private HashMap<Integer, Byte> hashPix = new HashMap<Integer, Byte>();
	private byte[] pixels;
	private ImageStack altimage;
	
	ImagePlus fill(ImagePlus image){
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.depth = image.getStackSize();
		this.image = image;
		copyMat();
		mask = new int[width * height * depth];
		label();
		
		if(checkHole()){
			while(fillHole()){
				label();
			}
			setStack();
			image.setStack(altimage);
			image.updateImage();
		}
		
		return image;
	}

	private void setStack(){
		altimage = new ImageStack(width, height);
		for(int d = 0 ; d < depth ; d++){
			byte matrix[] = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			altimage.addSlice(new ByteProcessor(width,height,matrix,null));
		}
	}
	
	 private void copyMat(){
	    	byte[] slice;   
	    	pixels = new byte[width * height * depth];
	    	for(int i = 1 ; i <= depth ; i++){
	        	slice = (byte[]) image.getStack().getPixels(i);
	        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
	        }
	    }
	
	public void label(){
		Arrays.fill(mask, 0);
		hashPix.clear();
		int num = 1;
		for (int d = 0; d < depth; d++) {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (mask[d * height * width + i * width + j] == 0) {
						hashPix.put(num, pixels[d * height * width + i * width + j]);
						recurs(j, i, d, num);
						num++;
					}
				}
			}
		}
	}
	
	public boolean checkHole(){
		if(Collections.frequency(hashPix.values(), (byte) 0) > 1){
			return true;
		}		
		return false;
	}
	
	public boolean fillHole(){
		for(Entry<Integer, Byte> e : hashPix.entrySet()){
			if(!e.getKey().equals(1) && e.getValue().equals((byte)0)){
				fill(e.getKey());
				return true;
			}		
		}
		return false;
	}
	
	public void fill(int index){
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if (mask[d * height * width + h * width + w] == index ) {
						pixels[d * height * width + h * width + w] = checkAdjacents(w, h, d, index);
					}
				}
			}
		}
	}
	
	public byte checkAdjacents(int w, int h, int d, int index){
		int max = 0;
		int count = 0;
		ArrayList<Byte> adjVal = new ArrayList<Byte>();
		
			//check right
			if(mask[d * height * width + h * width + w + 1] != index)
				adjVal.add(hashPix.get(mask[d * height * width + h * width + w + 1]));
			
			//check left			
			if(mask[d * height * width + h * width + w - 1] != index)
				adjVal.add(hashPix.get(mask[d * height * width + h * width + w - 1]));
			
			//check down
			if(mask[d * height * width + (h+1) * width + w ] != index)
				adjVal.add(hashPix.get(mask[d * height * width + (h+1) * width + w]));

			//check up
			if(mask[d * height * width + (h-1) * width + w ] != index)
				adjVal.add(hashPix.get(mask[d * height * width + (h-1) * width + w]));

			//check above
			if(d != depth - 1 && mask[(d+1) * height * width + h * width + w] != index)
				adjVal.add(hashPix.get(mask[(d+1) * height * width + h * width + w]));
			
			//check below
			if(d != 0 && mask[(d-1) * height * width + h * width + w] != index)
				adjVal.add(hashPix.get(mask[(d - 1) * height * width + h * width + w]));
			
			if(adjVal.isEmpty())
				return (byte)max;
		
			int freq = 0,temp; Byte val = 0;
			for(int n = 0 ; n < adjVal.size() ; n++){
				val = adjVal.get(n);
				freq = Collections.frequency(adjVal, val);
				temp = val & 0xFF;
				if(freq > count){
					max = temp;
					count = freq;
				}
					
				if(freq == count && max < temp){
					max = temp;
					count = freq;
				}
			}
			
		return (byte) max;	
	}
	
	private void recurs(int  w , int h, int d, final int val){
    	Stack<Integer> block = new Stack<Integer>();
		block.push(w);
		block.push(h);
		block.push(d);

		while(!block.isEmpty()){
			d = block.pop();
			h = block.pop();
			w = block.pop();
			
			//check right
			if(w != width - 1 && mask[d * height * width + h * width + w + 1] == 0 && pixels[d * height * width + h * width + w + 1] == pixels[d * height * width + h * width + w]){
				block.push(w+1);
				block.push(h);
				block.push(d);
			}

			//check left
			if(w != 0 && mask[d * height * width + h * width + w - 1] == 0 && pixels[d * height * width + h * width + w - 1] == pixels[d * height * width + h * width + w]){
				block.push(w-1);
				block.push(h);
				block.push(d);
			}

			//check down
			if(h != height - 1 && mask[d * height * width + (h+1) * width + w ] == 0 && pixels[d * height * width + (h+1) * width + w] == pixels[d * height * width + h * width + w]){
				block.push(w);
				block.push(h+1);
				block.push(d);
			}

			//check up
			if(h != 0 && mask[d * height * width + (h-1) * width + w] == 0 && pixels[d * height * width + (h-1) * width + w] == pixels[d * height * width + h * width + w]){
				block.push(w);
				block.push(h-1);
				block.push(d);
			}
			
			//check above
			if(d != depth - 1 && mask[(d+1) * height * width + h * width + w] == 0 && pixels[(d+1) * height * width + h * width + w] == pixels[d * height * width + h * width + w]){
				block.push(w);
				block.push(h);
				block.push(d+1);
			}
			
			//check below
			if(d != 0 && mask[(d-1) * height * width + h * width + w] == 0 && pixels[(d-1) * height * width + h * width + w] == pixels[d * height * width + h * width + w]){
				block.push(w);
				block.push(h);
				block.push(d-1);
			}

			mask[d * height * width + h * width + w] = val;
		}
    }

}
