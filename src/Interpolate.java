import ij.ImagePlus;
import ij.io.FileInfo;


public class Interpolate {
	private double voxx;
	private double voxy;
	private double voxz;
	private ImagePlus image;
	
	Interpolate(){
		
	}
	
	Interpolate(ImagePlus image){
        this.image = image;
		FileInfo info = image.getOriginalFileInfo();
        voxx = info.pixelWidth;
        voxy = info.pixelHeight;
        voxz = info.pixelDepth;
	
        if(needInterpolate()){
        	interpolation();
        }
	}
	
	private boolean needInterpolate(){
		if(voxz != voxx || voxz != voxy)
			return false;
		
		return true;
	}
	
	private void interpolation(){
		
	}
	
	//may not need it
	public ImagePlus getInterpolatedImage(){
		return image;
	}
}
