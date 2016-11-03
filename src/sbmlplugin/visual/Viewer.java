package sbmlplugin.visual;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij3d.Content;
import ij3d.ContentNode;
import ij3d.Image3DUniverse;
import isosurface.MeshGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import sbmlplugin.image.SpatialImage;
import customnode.CustomMesh;


// TODO: Auto-generated Javadoc
/**
 * The Class Viewer.
 */
public class Viewer {
	
	/** The univ. */
	private Image3DUniverse univ;
	
	/** The colors. */
	private Vector<Color3f> colors = new Vector<Color3f>();
	
	/** The width. */
	private int width;
	
	/** The height. */
	private	int height;
	
	/** The depth. */
	private int depth;
	
	/** The hash doms. */
	private HashMap<String, Integer> hashDoms;
	
	/** The hash img. */
	private HashMap<String, ImagePlus> hashImg = new HashMap<String, ImagePlus>();
	
	/** The raw mat. */
	private byte[] rawMat;
	
	/** The hash vertices. */
	private HashMap<String, List<Point3d>> hashVertices = new HashMap<String, List<Point3d>>();
	
	/** The hash bound. */
	private HashMap<String, Point3d> hashBound = new HashMap<String, Point3d>();
	
	/**
	 * View.
	 *
	 * @param spImg the sp img
	 */
	public void view(SpatialImage spImg){
		univ = new Image3DUniverse();
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.hashDoms = spImg.getHashSampledValue();
		this.rawMat = spImg.getRaw();
		separateImg();
		setColors(hashImg.size());
		setImages();
	}
	
	/**
	 * Sets the images.
	 */
	private void setImages(){
		int i = 0;
		for(Entry<String, ImagePlus> e : hashImg.entrySet()){
			Content c = univ.addMesh(e.getValue(), colors.get(i++), e.getKey(), 0, new boolean[] {true,true,true}, 1);
			c.setShaded(false);
		}
		univ.getSelected();
		univ.show();
	}
	
	/**
	 * Find points.
	 */
	public void findPoints() {
		for (Entry<String, ImagePlus> e : hashImg.entrySet()) {
			Content c = univ.getContent(e.getKey());
			ContentNode node = c.getContent();
			// 各メッシュの頂点を取ってくる、メッシュは三角形を形成する
			if (node instanceof MeshGroup) {
				CustomMesh cm = ((MeshGroup) node).getMesh();
				@SuppressWarnings("unchecked")
				List<Point3d> vertices = cm.getMesh();
				hashVertices.put(c.getName(), vertices);
			}
			// get min max coordinates
			Point3d p = new Point3d();
			c.getContent().getMax(p);
			setMaxBound(p);
			p = new Point3d();
			c.getContent().getMin(p);
			setMinBound(p);
		}
	}
	
	/**
	 * Sets the max bound.
	 *
	 * @param p the new max bound
	 */
	private void setMaxBound(Point3d p){
		if(!hashBound.containsKey("max")) hashBound.put("max", p);
		else{
			Point3d tempMax = hashBound.get("max");
			if(tempMax.x < p.x) tempMax.x = p.x;
			if(tempMax.y < p.y) tempMax.y = p.y;
			if(tempMax.z < p.z) tempMax.z = p.z;
		}
	}
	
	/**
	 * Sets the min bound.
	 *
	 * @param p the new min bound
	 */
	private void setMinBound(Point3d p){
		if(!hashBound.containsKey("min")) hashBound.put("min", p);
		else{
			Point3d tempMin = hashBound.get("min");
			if(tempMin.x > p.x) tempMin.x = p.x;
			if(tempMin.y > p.y) tempMin.y = p.y;
			if(tempMin.z > p.z) tempMin.z = p.z;
		}
	}
	
	/**
	 * Sets the colors.
	 *
	 * @param size the new colors
	 */
	private void setColors(int size){
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
				case 3:
					colors.add(new Color3f(1f,1f,0));
					break;
				case 4:
					colors.add(new Color3f(0,1f,1f));
					break;
				case 5:
					colors.add(new Color3f(1f,0,1f));
					break;
				default:	
					colors.add(new Color3f(0,0,0));
					break;
			}
		}
	}
	
	/**
	 * Separate img.
	 */
	private void separateImg(){
		for(Entry<String, Integer> e : hashDoms.entrySet()){
			if(e.getValue().equals(0))
				continue;
			ImagePlus tempimg = new ImagePlus();
			tempimg.setStack(createLabelImage(e.getValue()));
			hashImg.put(e.getKey(), tempimg);
		}
	}

	/**
	 * Creates the label image.
	 *
	 * @param pixVal the pix val
	 * @return the image stack
	 */
	private ImageStack createLabelImage(int pixVal){
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
	
	/**
	 * Sets the stack.
	 *
	 * @param pixels the pixels
	 * @return the image stack
	 */
	private ImageStack setStack(byte[] pixels){
		ImageStack imstack = new ImageStack(width, height);
		for(int d = 0 ; d < depth ; d++){
			byte matrix[] = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			imstack.addSlice(new ByteProcessor(width,height,matrix,null));
		}
		return imstack;
	}
	
	/**
	 * Gets the hash vertices.
	 *
	 * @return the hash vertices
	 */
	public HashMap<String, List<Point3d>> gethashVertices(){
		return hashVertices;
	}

	/**
	 * Gets the hash bound.
	 *
	 * @return the hash bound
	 */
	public HashMap<String, Point3d> gethashBound(){
		return hashBound;
	}

}
