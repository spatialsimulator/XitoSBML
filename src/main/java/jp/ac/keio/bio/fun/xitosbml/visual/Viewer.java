package jp.ac.keio.bio.fun.xitosbml.visual;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3d;

import customnode.CustomMesh;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij3d.Content;
import ij3d.ContentNode;
import ij3d.Image3DUniverse;
import isosurface.MeshGroup;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;

/**
 * The class Viewer, which visualizes a SpatialImage object on ImageJ 3D Viewer.
 * This class also contains some methods to handle ImageJ image object
 * (ImagePlus). Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class Viewer {

	/** The Image3DUniverse, which is used in ImageJ 3D Viewer. */
	private Image3DUniverse univ;

	/** The colors. */
	private Vector<Color3f> colors = new Vector<Color3f>();

	/** The width of an image. */
	private int width;

	/** The height of an image. */
	private int height;

	/** The depth of an image. */
	private int depth;

	/** The hashmap of sampled value of spatial image. */
	private HashMap<String, Integer> hashDoms;

	/** The hashmap of ImageJ image object (ImagePlus). */
	private HashMap<String, ImagePlus> hashImg = new HashMap<String, ImagePlus>();

	/** The raw data of spatial image in 1D array. */
	private byte[] rawMat;

	/** The hashmap of vertices. */
	private HashMap<String, List<Point3d>> hashVertices = new HashMap<String, List<Point3d>>();

	/** The hashmap of boundary. */
	private HashMap<String, Point3d> hashBound = new HashMap<String, Point3d>();

	/**
	 * Visualize SpatialImage object on ImageJ 3D Viewer
	 *
	 * @param spImg the The SpatialImage, which is a class for handling spatial
	 *              image in XitoSBML.
	 */
	public void view(SpatialImage spImg) {
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
	 * Sets images to Image3DUniverse, and show the 3D space with ImageJ 3D Viewer.
	 */
	private void setImages() {
		int i = 0;
		for (Entry<String, ImagePlus> e : hashImg.entrySet()) {
			Content c = univ.addMesh(e.getValue(), colors.get(i++), e.getKey(), 0, new boolean[] { true, true, true },
					1);
			c.setShaded(false);
		}
		univ.getSelected();
		univ.show();
	}

	/**
	 * Find points in the 3D universe. If the 3D space contains MethGroup, then add
	 * all the vertices of the mesh to hashmap of vertices. Minimum and Maximum
	 * boundary is also assigned as points.
	 */
	public void findPoints() {
		for (Entry<String, ImagePlus> e : hashImg.entrySet()) {
			Content c = univ.getContent(e.getKey());
			ContentNode node = c.getContent();

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
	 * Sets the maximum boundary to the hashmap of boundary.
	 *
	 * @param p the new maximum boundary
	 */
	private void setMaxBound(Point3d p) {
		if (!hashBound.containsKey("max"))
			hashBound.put("max", p);
		else {
			Point3d tempMax = hashBound.get("max");
			if (tempMax.x < p.x)
				tempMax.x = p.x;
			if (tempMax.y < p.y)
				tempMax.y = p.y;
			if (tempMax.z < p.z)
				tempMax.z = p.z;
		}
	}

	/**
	 * Sets the minimum boundary to the hashmap of boundary.
	 *
	 * @param p the new minimum boundary
	 */
	private void setMinBound(Point3d p) {
		if (!hashBound.containsKey("min"))
			hashBound.put("min", p);
		else {
			Point3d tempMin = hashBound.get("min");
			if (tempMin.x > p.x)
				tempMin.x = p.x;
			if (tempMin.y > p.y)
				tempMin.y = p.y;
			if (tempMin.z > p.z)
				tempMin.z = p.z;
		}
	}

	/**
	 * Sets the colors.
	 *
	 * @param size the new colors
	 */
	private void setColors(int size) {
		for (int i = 0; i <= size; i++) {
			switch (i) {
			case 0:
				colors.add(new Color3f(1f, 0, 0));
				break;
			case 1:
				colors.add(new Color3f(0, 1f, 0));
				break;
			case 2:
				colors.add(new Color3f(0, 0, 1f));
				break;
			case 3:
				colors.add(new Color3f(1f, 1f, 0));
				break;
			case 4:
				colors.add(new Color3f(0, 1f, 1f));
				break;
			case 5:
				colors.add(new Color3f(1f, 0, 1f));
				break;
			default:
				colors.add(new Color3f(0, 0, 0));
				break;
			}
		}
	}

	/**
	 * Split images to each domain images, and then convert it to label image, which
	 * is a binarised image of each domain.
	 */
	private void separateImg() {
		for (Entry<String, Integer> e : hashDoms.entrySet()) {
			if (e.getValue().equals(0))
				continue;
			ImagePlus tempimg = new ImagePlus();
			tempimg.setStack(createLabelImage(e.getValue()));
			hashImg.put(e.getKey(), tempimg);
		}
	}

	/**
	 * Creates the label image. If a pixel contains an identical value with given
	 * pixVal, then replace its value to 255. This procedure will create a binarised
	 * image, which represents the domain with white pixels.
	 *
	 * @param pixVal the pixel value of domain
	 * @return the image stack
	 */
	private ImageStack createLabelImage(int pixVal) {
		byte[] pixels = new byte[width * height * depth];
		for (int d = 0; d < depth; d++) {
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					if (rawMat[d * height * width + h * width + w] == (byte) pixVal)
						pixels[d * height * width + h * width + w] = (byte) 255;
				}
			}
		}
		return setStack(pixels);
	}

	/**
	 * Sets the stack with given 1D array of pixel values.
	 *
	 * @param pixels the 1D array of pixel values, which represents the 3D space
	 * @return the image stack
	 */
	private ImageStack setStack(byte[] pixels) {
		ImageStack imstack = new ImageStack(width, height);
		for (int d = 0; d < depth; d++) {
			byte[] matrix = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			imstack.addSlice(new ByteProcessor(width, height, matrix, null));
		}
		return imstack;
	}

	/**
	 * Gets the hashmap of vertices.
	 *
	 * @return the hashmap of vertices
	 */
	public HashMap<String, List<Point3d>> gethashVertices() {
		return hashVertices;
	}

	/**
	 * Gets the hashmap of boundary.
	 *
	 * @return the hashmap of boundary
	 */
	public HashMap<String, Point3d> gethashBound() {
		return hashBound;
	}

}
