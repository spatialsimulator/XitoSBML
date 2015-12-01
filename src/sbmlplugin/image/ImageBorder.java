package sbmlplugin.image;

import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 18, 2015
 */
public class ImageBorder {
	private int width;
	private int height;
	private int depth;
	private byte[] raw;
	private boolean hasSafeBorder = true;
	private ImageStack altStack;
	
	public ImageBorder(SpatialImage spImg){
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.raw = spImg.getRaw();
		
		IJ.log( "If domain exist on the border, Image arrays may be modified\n");
		
		fixBorder();						// blackens all outside pixel
		hasSafeBorder = isBorderSafe(); 	//depth = 0 or top/bottom slice does not have object
		createNewStack(hasSafeBorder);
	}
	
	private void fixBorder() {
		int init = 0, end = depth;
		
		for (int d = init; d < end; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if (h == 0 || h == height - 1 || w == 0 || w == width - 1) {
						raw[d * height * width + h * width + w] = 0;
					}
				}
			}
		}
		
	}

	private boolean isBorderSafe(){
		boolean safez = true;
		if(depth != 1)	safez = checkTopBottom();
		return safez;
	}
	
	private boolean checkTopBottom(){
		int bottomSlice = (depth - 1) * width * height;
		
		for(int h = 0 ; h < height ; h++){
			for(int w = 0 ; w < width ; w++){
				if(raw[bottomSlice + width * h + w] != 0 || raw[width * h + w] != 0)
					return false;
			}
		}
		
		return true;
	}
	
	private void createNewStack(boolean hasSafeBorder){
		altStack = new ImageStack(width, height);
		
		if(!hasSafeBorder) addBlackSlice(altStack);
		
		for(int i = 1 ; i <= depth ; i++){
			byte[] slice = new byte[height * width];
			System.arraycopy(raw, (i-1) * height * width, slice, 0, height * width);
			altStack.addSlice(new ByteProcessor(width,height,slice,null));
    	} 
		if(!hasSafeBorder) addBlackSlice(altStack);
	
		if(!hasSafeBorder) depth += 2;
		
	}
	
	private void addBlackSlice(ImageStack is){
		byte[] blackSlice = new byte[width * height];
		is.addSlice(new ByteProcessor(width,height,blackSlice,null));
	}
	
	public ImageStack getStackImage(){
		return altStack;
	}
}
