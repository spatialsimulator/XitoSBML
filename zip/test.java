import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class test {
	private ImagePlus image;
	private int width;
	private int height;
	private int depth;
	private int lwidth;
	private int lheight;
	private int ldepth;
	private int[] mask;
	private HashMap<Integer, Byte> hashPix = new HashMap<Integer, Byte>();
	private byte[] pixels;
	private int[] invert;
	private ImageStack altimage;
	
	ImagePlus fill(ImagePlus image){
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.depth = image.getStackSize();
		this.image = image;

		copyMat();
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				//System.out.print(pixels[h*width+w]&0xFF);
			}
			System.out.println();
		}
		System.out.println();
		invertMat();
		label();
		if(checkHole()){
			while(checkHole()){	
				fillHole();
				hashPix.clear();
			//			invertMat();
			label();
			}
			setStack();
			image.setStack(altimage);
			image.updateImage();
		}
		
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				System.out.print(pixels[h*width+w]&0xFF);
			}
			System.out.println();
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
	
	 private void invertMat(){
		lwidth = width + 2;
		lheight = height + 2;
		if(depth < 3) ldepth = depth;
		else 			ldepth = depth + 2;
		
		invert = new int[lwidth * lheight * ldepth]; 
		mask = new int[lwidth * lheight * ldepth];
		if (ldepth > depth) {
			for (int d = 0; d < ldepth; d++) {
				for (int h = 0; h < lheight; h++) {
					for (int w = 0; w < lwidth; w++) {
						if (d == 0 || d == ldepth - 1 || h == 0 || h == lheight - 1 || w == 0 || w == lwidth - 1) {
							invert[d * lheight * lwidth + h * lwidth + w] = 1;
							mask[d * lheight * lwidth + h * lwidth + w] = 1;
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
							mask[d * lheight * lwidth + h * lwidth + w] = 1;
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
	
	 int labelCount;
	public void label(){
		hashPix.put(1, (byte)0);
		labelCount = 2;
		if (ldepth > depth) {
			for (int d = 1; d < ldepth - 1; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1 && pixels[(d-1) * height * width + (h-1) * width + w - 1] == 0) {
							mask[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}else{
							mask[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[(d-1) * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}else{
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (invert[d * lheight * lwidth + h * lwidth + w] == 1 && pixels[d * height * width + (h-1) * width + w - 1] == 0) {
							mask[d * lheight * lwidth + h * lwidth + w] = setLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}else{
							mask[d * lheight * lwidth + h * lwidth + w] = setbackLabel(w, h, d, pixels[d * height * width + (h-1) * width + w - 1]);
						}
					}
				}
			}
		}
	}
	
	public boolean checkHole(){
		if(Collections.frequency(hashPix.values(), (byte) 0) > 1){
			System.out.println(hashPix.toString());
			return true;
		}		
		return false;
	}
	
	public void fillHole(){
		for(Entry<Integer, Byte> e : hashPix.entrySet()){
			if(!e.getKey().equals(1) && e.getValue().equals((byte)0)){
				fill(e.getKey());
			}		
		}

	}
	
	public void fill(int index){
		if (ldepth > depth) {
			for (int d = 1; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == index ) {
							pixels[(d-1) * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, index);
						}
					}
				}
			}
		} else {
			for (int d = 0; d < ldepth; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == index ) {
							pixels[d * height * width + (h-1) * width + w - 1] = checkAdjacentsLabel(w, h, d, index);
						}
					}
				}
			}
		}
	}
	
	public byte checkAdjacentsLabel(int w, int h, int d, int index){
		List<Byte> adjVal = new ArrayList<Byte>();
			//check right
			if(mask[d * lheight * lwidth + h * lwidth + w + 1] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w + 1]));
			
			//check left			
			if(mask[d * lheight * lwidth + h * lwidth + w - 1] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]));
			
			//check down
			if(mask[d * lheight * lwidth + (h+1) * lwidth + w ] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h+1) * lwidth + w]));

			//check up
			if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != index)
				adjVal.add(hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]));

			//check above
			if(d != depth - 1 && mask[(d+1) * lheight * lwidth + h * lwidth + w] != index)
				adjVal.add(hashPix.get(mask[(d+1) * lheight * lwidth + h * lwidth + w]));
			
			//check below
			if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != index)
				adjVal.add(hashPix.get(mask[(d - 1) * lheight * lwidth + h * lwidth + w]));
			
			if(adjVal.isEmpty())
				return 0;
		
			int max = 0;
			int count = 0;
			int freq, temp; Byte val = 0;
			for(int n = 0 ; n < adjVal.size() ; n++){
				val = adjVal.get(n);
				if(val == 0)
					continue;
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
	
	private int setLabel(int  w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();
		//check left			
		if(mask[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]) == (byte)0)
			adjVal.add(mask[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]) == (byte)0)
			adjVal.add(mask[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(mask[(d-1) * lheight * lwidth + h * lwidth + w]) == (byte)0)
			adjVal.add(mask[(d-1) * lheight * lwidth + h * lwidth + w]);
		
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

	
	private int setbackLabel(int  w , int h, int d, byte pixVal){
		List<Integer> adjVal = new ArrayList<Integer>();

		//check left			
		if(mask[d * lheight * lwidth + h * lwidth + w - 1] != 0 && hashPix.get(mask[d * lheight * lwidth + h * lwidth + w - 1]) != (byte)0)
			adjVal.add(mask[d * lheight * lwidth + h * lwidth + w - 1]);

		//check up
		if(mask[d * lheight * lwidth + (h-1) * lwidth + w ] != 0 && hashPix.get(mask[d * lheight * lwidth + (h-1) * lwidth + w]) != (byte)0)
			adjVal.add(mask[d * lheight * lwidth + (h-1) * lwidth + w]);

		//check below
		if(d != 0 && mask[(d-1) * lheight * lwidth + h * lwidth + w] != 0 && hashPix.get(mask[(d-1) * lheight * lwidth + h * lwidth + w]) != (byte)0)
			adjVal.add(mask[(d-1) * lheight * lwidth + h * lwidth + w]);
		
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
						if (mask[d * lheight * lwidth + h * lwidth + w] == before)
							mask[d * lheight * lwidth + h * lwidth + w] = after;					
						}
				}
			}
		}else{
			for (int d = 0; d <= dEnd; d++) {
				for (int h = 1; h < lheight - 1; h++) {
					for (int w = 1; w < lwidth - 1; w++) {
						if (mask[d * lheight * lwidth + h * lwidth + w] == before)
							mask[d * lheight * lwidth + h * lwidth + w] = after;
					}
				}
			}
		}

	}
	
}
