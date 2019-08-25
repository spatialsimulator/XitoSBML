package jp.ac.keio.bio.fun.xitosbml.image;

import java.util.HashMap;
import java.util.Map.Entry;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.process.ByteProcessor;


/**
 * The class Interpolator, which provides interpolation operations for Z-stack images.
 * If the voxel size of each x, y and z axis is not the same, then apply interpolation
 * to the spatial image so that the voxel size of each axis will be the same.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class Interpolator {
	
	/** The voxel size of x axis. */
	private double voxx;
	
	/** The voxel size of y axis. */
	private double voxy;
	
	/** The voxel size of z axis. */
	private double voxz;
	
	/** The size of zaxis. */
	private double zaxis;
	
	/** The width of an image. */
	private int width;
	
	/** The height of an image. */
	private int height;

	/** The depth of an image. */
	private int depth;
	
	/** The interpolated stack size. */
	private int altz;
	
	/** The ImageJ image object. */
	private ImagePlus image;		//input image
	
	/** The interpolated image object. */
	private ImageStack altimage;	//output pixel image
	
	/** The raw data of spatial image in 1D array. */
	private byte[] pixels;
	
	/** The file information of the spatial image. */
	private FileInfo info;
	
	/**
	 * Instantiates a new interpolator.
	 */
	public Interpolator(){
		
	}
	
	/**
	 * Instantiates a new interpolator with given spatial image.
	 *
	 * @param spImg the spatial image
	 */
	public Interpolator(SpatialImage spImg){
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
     * Obtain following information of the given image object and sets to the corresponding variables.
	 * <ul>
     *     <li>size of the image</li>
	 *     <li>file information</li>
	 *     <li>pixel size of the image</li>
	 *     <li>size of z axis</li>
	 * </ul>
	 *
	 * @param imgPlus the ImageJ image object
	 */
	private void getInfo(ImagePlus imgPlus){
		width = imgPlus.getWidth();
		height = imgPlus.getHeight();
		depth = imgPlus.getImageStackSize();
		info = image.getOriginalFileInfo();
		voxx = info.pixelWidth;
		voxy = info.pixelHeight;
		voxz = info.pixelDepth;
		zaxis = voxz * image.getImageStackSize();
		System.out.println("voxel size " + voxx + " " + voxy + " " + voxz);
	}
	
	/**
	 * Interpolate given image object and return the interpolated image object.
	 *
	 * @param imagePlus the ImageJ image object
	 * @return the interpolated image object
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
	 * Interpolate given spatial image object and return the interpolated spatial image object.
	 *
	 * @param spImg the spatial image object
	 * @return the interpolated spatial image object
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
	 * Interpolate given set of ImageJ image objects (as hashmap of domain files, HashMap&lt;String, ImagePlus&gt;).
	 * The value of given hashmap of domain files will be replaced by the interpolated image object.
	 *
	 * @param hashdomFile the hashmap of domain file. HashMap&lt;String, ImagePlus&gt;
	 */
	public void interpolate(HashMap<String, ImagePlus> hashdomFile) {
		for(Entry<String, ImagePlus> e : hashdomFile.entrySet()){
			ImagePlus i = interpolate(e.getValue());
			hashdomFile.put(e.getKey(), i);
		}
	}
	
	/**
	 * Check if the spatial image needs interpolation.
	 * If the voxel size of each x, y and z axis is the same, then we can skip the interpolation.
	 *
	 * @return true, if spatial image needs interpolation
	 */
	private boolean needInterpolate(){
		if(depth == 1) return false;
		if(voxz > voxx || voxz > voxy)
			return true;
		
		return false;
	}

	/**
	 * The Nearest Neighbor algorithm, which is the core implementation of interpolation.
	 * There are several algorithms proposed for interpolation. For example there exist following
	 * non adaptive interpolation algorithms:
	 * <ol>
	 *     <li>Nearest Neighbour Interpolation</li>
	 *     <li>Bilinear Interpolation</li>
	 *     <li>Bicubic Interpolation</li>
	 *     <li>Re-sampling process</li>
	 * </ol>
	 * XitoSBML adopt "Nearest Neighbor Interpolation" for interpolation.
	 * Other algorithms will be implemented in the future.
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
	 * Gets the interpolated image object.
	 *
	 * @return the interpolated image as ImageJ image object
	 */
	public ImagePlus getInterpolatedImage(){
		return image;
	}

}
