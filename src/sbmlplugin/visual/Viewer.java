<<<<<<< 4d33b61e0fa721df60cd16c0d511242814b35393:src/sbmlplugin/visual/Viewer.java
/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package sbmlplugin.visual;

=======
package visual;
>>>>>>> modified viewer to avoid redundancy, erased all warnings, need a way to visualize when importing an existing model:src/visual/Viewer.java
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij3d.Content;
import ij3d.ContentNode;
import ij3d.Image3DUniverse;
<<<<<<< 4d33b61e0fa721df60cd16c0d511242814b35393:src/sbmlplugin/visual/Viewer.java
=======
import image.SpatialImage;
>>>>>>> modified viewer to avoid redundancy, erased all warnings, need a way to visualize when importing an existing model:src/visual/Viewer.java
import isosurface.MeshGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import sbmlplugin.image.SpatialImage;
import customnode.CustomMesh;


public class Viewer {
	private Image3DUniverse univ;
	private Vector<Color3f> colors = new Vector<Color3f>();
	private int width;
	private	int height;
	private int depth;
	private HashMap<String, Integer> hashDoms;
	private HashMap<String, ImagePlus> hashImg = new HashMap<String, ImagePlus>();
	private byte[] rawMat;
	private HashMap<String, List<Point3f>> hashVertices = new HashMap<String, List<Point3f>>();
	private HashMap<String, Point3d> hashBound = new HashMap<String, Point3d>();
	
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
	
	private void setImages(){
		int i = 0;
		for(Entry<String, ImagePlus> e : hashImg.entrySet()){
			Content c = univ.addMesh(e.getValue(), colors.get(i++), e.getKey(), 0, new boolean[] {true,true,true}, 1);
			c.setShaded(false);
<<<<<<< 4d33b61e0fa721df60cd16c0d511242814b35393:src/sbmlplugin/visual/Viewer.java
<<<<<<< cdc200734e26e34f1f6ffe80745713b23e13e0ba:src/sbmlplugin/visual/Viewer.java
=======
>>>>>>> modified viewer to avoid redundancy, erased all warnings, need a way to visualize when importing an existing model:src/visual/Viewer.java
		}
		univ.getSelected();
		univ.show();
	}
	
	public void findPoints() {
		for (Entry<String, ImagePlus> e : hashImg.entrySet()) {
			Content c = univ.getContent(e.getKey());
<<<<<<< 4d33b61e0fa721df60cd16c0d511242814b35393:src/sbmlplugin/visual/Viewer.java
			ContentNode node = c.getContent();
			// 各メッシュの頂点を取ってくる、メッシュは三角形を形成する
			if (node instanceof MeshGroup) {
				CustomMesh cm = ((MeshGroup) node).getMesh();
				@SuppressWarnings("unchecked")
				List<Point3f> vertices = cm.getMesh();
				hashVertices.put(c.getName(), vertices);
=======
			
			ContentNode node = c.getContent();
			//各メッシュの頂点を取ってくる、メッシュは三角形を形成する
			if(node instanceof MeshGroup){
				 CustomMesh cm  = ((MeshGroup)node).getMesh();
				 List<Point3f> vertices = cm.getMesh();
				 hashVertices.put(c.getName(), vertices);
>>>>>>> created new feature to add parameter and species to existing model:src/visual/Viewer.java
=======
			ContentNode node = c.getContent();
			// 各メッシュの頂点を取ってくる、メッシュは三角形を形成する
			if (node instanceof MeshGroup) {
				CustomMesh cm = ((MeshGroup) node).getMesh();
				@SuppressWarnings("unchecked")
				List<Point3f> vertices = cm.getMesh();
				hashVertices.put(c.getName(), vertices);
>>>>>>> modified viewer to avoid redundancy, erased all warnings, need a way to visualize when importing an existing model:src/visual/Viewer.java
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
<<<<<<< 4d33b61e0fa721df60cd16c0d511242814b35393:src/sbmlplugin/visual/Viewer.java
	
	private void setMaxBound(Point3d p){
=======
	void setMaxBound(Point3d p){
>>>>>>> modified viewer to avoid redundancy, erased all warnings, need a way to visualize when importing an existing model:src/visual/Viewer.java
		if(!hashBound.containsKey("max")) hashBound.put("max", p);
		else{
			Point3d tempMax = hashBound.get("max");
			if(tempMax.x < p.x) tempMax.x = p.x;
			if(tempMax.y < p.y) tempMax.y = p.y;
			if(tempMax.z < p.z) tempMax.z = p.z;
		}
	}
	
	private void setMinBound(Point3d p){
		if(!hashBound.containsKey("min")) hashBound.put("min", p);
		else{
			Point3d tempMin = hashBound.get("min");
			if(tempMin.x > p.x) tempMin.x = p.x;
			if(tempMin.y > p.y) tempMin.y = p.y;
			if(tempMin.z > p.z) tempMin.z = p.z;
		}
	}
	
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
	
	private void separateImg(){
		for(Entry<String, Integer> e : hashDoms.entrySet()){
			if(e.getValue().equals(0))
				continue;
			ImagePlus tempimg = new ImagePlus();
			tempimg.setStack(createLabelImage(e.getValue()));
			hashImg.put(e.getKey(), tempimg);
		}
	}

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
	
	private ImageStack setStack(byte[] pixels){
		ImageStack imstack = new ImageStack(width, height);
		for(int d = 0 ; d < depth ; d++){
			byte matrix[] = new byte[width * height];
			System.arraycopy(pixels, d * height * width, matrix, 0, matrix.length);
			imstack.addSlice(new ByteProcessor(width,height,matrix,null));
		}
		return imstack;
	}
	
	public HashMap<String, List<Point3f>> gethashVertices(){
		return hashVertices;
	}

	public HashMap<String, Point3d> gethashBound(){
		return hashBound;
	}

}
