import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import customnode.CustomMesh;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij3d.Content;
import ij3d.ContentNode;
import ij3d.Image3DUniverse;
import isosurface.MeshGroup;


public class Viewer {
	Image3DUniverse univ;
	private Vector<Color3f> colors = new Vector<Color3f>();
	int width;
	int height;
	int depth;
	HashMap<String, Integer> hashDoms;
	HashMap<String, ImagePlus> hashImg = new HashMap<String, ImagePlus>();
	byte[] rawMat;
	HashMap<String, List<Point3f>> hashVertices = new HashMap<String, List<Point3f>>();
	
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
			Content c = univ.addMesh(e.getValue(), colors.get(i++), e.getKey(), 0, new boolean[] {true,true,true}, 1);
			c.setShaded(false);
			ContentNode node = c.getContent();
			//各メッシュの頂点を取ってくる、メッシュは三角形を形成するので、
			if(node instanceof MeshGroup){
				 CustomMesh cm  = ((MeshGroup)node).getMesh();
				 List<Point3f> vertices = cm.getMesh();
				 hashVertices.put(c.getName(), vertices);
			}
		}
		univ.getSelected();
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
	
	HashMap<String, List<Point3f>> gethashVertices(){
		return hashVertices;
	}
}
