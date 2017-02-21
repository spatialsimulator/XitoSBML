package xitosbml.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ByteProcessor;

import java.util.HashMap;
import java.util.Map.Entry;


// TODO: Auto-generated Javadoc
/**
 * The Class Interpolater.
 */
public class Interpolater {
	
	/** The voxx. */
	private double voxx;			
	
	/** The voxy. */
	private double voxy;
	
	/** The voxz. */
	private double voxz;
	
	/** The zaxis. */
	private double zaxis;
	
	/** The width. */
	private int width;
	
	/** The height. */
	private int height;
	
	/** The altz. */
	private int altz;
	
	/** The image. */
	private ImagePlus image;		//input image
	
	/** The altimage. */
	private ImageStack altimage;	//output pixel image
	
	/** The pixels. */
	private byte[] pixels;
	
	/** The info. */
	private FileInfo info;
	
	/**
	 * Instantiates a new interpolater.
	 */
	public Interpolater(){
		
	}
	
	/**
	 * Instantiates a new interpolater.
	 *
	 * @param spImg the sp img
	 */
	public Interpolater(SpatialImage spImg){
			this.image = spImg.getImage();
			getInfo(image);
			this.pixels = spImg.getRaw();
			
			if (needInterpolate()) {
				nearestNeighbor();
				image.setStack(altimage);
				info.pixelDepth =  zaxis / altz;
				image.setFileInfo(info);
				image.updateImage();
				System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
			}
	}
	
	/**
	 * Gets the info.
	 *
	 * @param imgPlus the img plus
	 * @return the info
	 */
	private void getInfo(ImagePlus imgPlus){
		width = imgPlus.getWidth();
		height = imgPlus.getHeight();
		imgPlus.getImageStackSize();
		info = image.getOriginalFileInfo();
		
		voxx = info.pixelWidth;
		voxy = info.pixelHeight;
		voxz = info.pixelDepth;
		zaxis = voxz * image.getImageStackSize();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
	}
	
	/**
	 * Interpolate.
	 *
	 * @param imagePlus the image plus
	 * @return the image plus
	 */
	public ImagePlus interpolate(ImagePlus imagePlus){
		this.image = imagePlus;
		getInfo(image);
		ImagePlus nImg = new ImagePlus();
		pixels = ImgProcessUtil.copyMat(imagePlus);
		
		if (needInterpolate()) {
			nearestNeighbor();
			nImg.setStack(altimage);
			nImg.setFileInfo(info);
			nImg.getFileInfo().pixelDepth =  zaxis / altz;
			nImg.updateImage();
			System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
		}else{
			nImg = (ImagePlus) imagePlus.clone();
		}

		return nImg;
	}
	
	/**
	 * Interpolate.
	 *
	 * @param spImg the sp img
	 * @return the spatial image
	 */
	public SpatialImage interpolate(SpatialImage spImg){
		this.image = spImg.getImage();
		getInfo(image);
		this.pixels = spImg.getRaw();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
		
		if (needInterpolate()) {
			nearestNeighbor();
			image.resetStack();
			image.setStack(altimage);
			info.pixelDepth =  zaxis / altz;
			image.setFileInfo(info);
			System.out.println("interpolated voxel size " + voxx + " " + voxy + " " + info.pixelDepth);
		}
		image.updateImage();
		spImg.setImage(image);
		return spImg;
	}

	/**
	 * Interpolate.
	 *
	 * @param hashdomFile the hashdom file
	 */
	public void interpolate(HashMap<String, ImagePlus> hashdomFile) {
		for(Entry<String, ImagePlus> e : hashdomFile.entrySet()){
			ImagePlus i = interpolate(e.getValue());
			hashdomFile.put(e.getKey(), i);
		}
	}
	
	/**
	 * Need interpolate.
	 *
	 * @return true, if successful
	 */
	private boolean needInterpolate(){
		if(voxz > voxx || voxz > voxy)
			return true;
		
		return false;
	}

	/**
	 * Nearest neighbor.
	 */
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
	
	/**
	 * Gets the interpolated image.
	 *
	 * @return the interpolated image
	 */
	public ImagePlus getInterpolatedImage(){
		return image;
	}

}
