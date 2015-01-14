import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ByteProcessor;


public class Interpolate {
	//original voxel size
	private double voxx;			
	private double voxy;
	private double voxz;
	//axis of whole axis
	private double zaxis;
	private int width;
	private int height;
	private int depth;
	private int altz;
	private ImagePlus image;		//input image
	private ImageStack altimage;	//output pixel image
	private byte[] pixels;
	
	Interpolate(ImagePlus image){
		this.image = image;
		width = image.getWidth();
		height = image.getHeight();
		depth = image.getStackSize();
		FileInfo info = image.getOriginalFileInfo();

		voxx = info.pixelWidth;
		voxy = info.pixelHeight;
		voxz = info.pixelDepth;
		zaxis = voxz * image.getImageStackSize();
		copyMat();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
		
		if (needInterpolate()) {
			nearestNeighbor();
			// linear();
			image.setStack(altimage);
			info.pixelDepth =  zaxis / altz;
			image.setFileInfo(info);
			System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
		}
		image.updateImage();
	}
	
    private void copyMat(){
    	byte[] slice;   
    	pixels = new byte[width * height * depth];
    	for(int i = 1 ; i <= depth ; i++){
        	slice = (byte[])image.getStack().getPixels(i); 
        	System.arraycopy(slice, 0, pixels, (i-1) * height * width, slice.length);
        }
    }
	
	private boolean needInterpolate(){
		if(voxz > voxx || voxz > voxy)
			return true;
		
		return false;
	}

	private void nearestNeighbor(){
		altz = (int) (zaxis / voxx);
		System.out.println("interpolated stack size " + altz);
		altimage = new ImageStack(width, height);
		byte matrix[];
		double xdis, ydis, zdis;
		double halfx = voxx /2, halfy = voxy /2,halfz = voxx /2;
		
		for(int d = 0 ; d < altz ; d++){
			matrix = new byte[width * height];
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					// get center
					xdis = w * voxx + halfx;	 
					ydis = h * voxy + halfy;
					zdis = d * voxx + halfz;
					
					//apply to nearest original pixel
					xdis = Math.floor(xdis / voxx);
					ydis = Math.floor(ydis / voxy);
					zdis = Math.floor(zdis / voxz);
					matrix[h * width + w] = pixels[(int) (zdis * width * height + ydis * width + xdis)];	
				}
			}
			altimage.addSlice(new ByteProcessor(width,height,matrix,null));
		}
	}
	
	private void linear(){
		altz = (int) (zaxis / voxx);
		System.out.println("interpolated stack size " + altz);
		altimage = new ImageStack(width, height);
		byte matrix[];
		double x1, y1, z1 = 0,x2, y2, z2 = 0;
		double xc, yc, zc;
		double halfx = voxx /2, halfy = voxy /2,halfz = voxx /2;
		
		for(int d = 0 ; d < altz ; d++){
			matrix = new byte[width * height];
			System.out.println(d);
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					
					// get center
					xc = w * voxx + halfx;	 
					yc = h * voxy + halfy;
					zc = d * voxx + halfz;
					
					//apply to nearest original pixel
					x1 = Math.floor(xc / voxx);
					y1 = Math.floor(yc / voxy);
					z1 = Math.floor(zc / voxz);
					z2 = Math.ceil(zc / voxz);
					
					matrix[h * width + w] = getLinear((double) pixels[(int) (z1 * width * height + y1 * width + x1)], (double) pixels[(int) (z2 * width * height + y1 * width + x1)], (zc - z1)/(z2 - z1));
					
				}
			}
			System.out.println(z1 + " " + z2);
			altimage.addSlice(new ByteProcessor(width,height,matrix,null));
		}
	}
	
	private byte getLinear(double prev, double next, double mu){
		return (byte) (mu * (next - prev));
	}
	
	public ImagePlus getInterpolatedImage(){
		return image;
	}
}
