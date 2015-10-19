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
package sbmlplugin.geometry;

import ij.ImageStack;
import ij.process.ByteProcessor;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfSampledFields;
import org.sbml.libsbml.ListOfSampledVolumes;
import org.sbml.libsbml.SampledField;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SampledVolume;
import org.sbml.libsbml.libsbmlConstants;

import sbmlplugin.image.SpatialImage;


/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 26, 2015
 */
public class SampledFieldGeometryData extends ImageGeometryData {
	private SampledFieldGeometry sfg;
	private int width;
	private int height;
	private int depth;
	
	/**
	 * @param gd
	 * @param g
	 */
	public SampledFieldGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd, g);
		sfg = (SampledFieldGeometry)gd;
		getSampledValues();
		createImage();
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSampledValue()
	 */
	@Override
	protected void getSampledValues() {											//may need to use min/max in future
		ListOfSampledVolumes losv = sfg.getListOfSampledVolumes();
		for(int i = 0 ; i < losv.size() ; i++){
			SampledVolume sv = losv.get(i);
			if(sv.isSetSampledValue())
				hashSampledValue.put(sv.getDomainType(), (int) sv.getSampledValue());			//double to int may need to chang
		}
	}

	
	protected void createImage(){
		ListOfSampledFields losf = g.getListOfSampledFields();
		//TODO : be able create image with multiple sampledfield
		if(losf.size() > 1)
			System.err.println("not able to compute multiple sampledfields at this point");

		SampledField sf = losf.get(0);
		if(sf.getDataType() != libsbmlConstants.SPATIAL_DATAKIND_UINT8)
			System.err.println("Image data is automatically changed to 8 bit image");
		getSize(sf);
		getArray(sf);

		ImageStack is = createStack();
		img.setStack(is);
		img.setTitle(title);
	}
	
	private ImageStack createStack(){
		ImageStack stack = new ImageStack(width, height);
		byte[] slice;   
    	int length = width * height;
    	for(int i = 1 ; i <= depth ; i++){
        	slice = new byte[length];
        	System.arraycopy(raw, (i-1) * height * width, slice, 0, length);
        	stack.addSlice(new ByteProcessor(width,height,slice,null));
    	}
    	return stack;
    }
	
	private void getSize(SampledField sf){
		width = sf.getNumSamples1();
		height = sf.getNumSamples2();
		if(sf.isSetNumSamples3()) depth = sf.getNumSamples3();
		else					  depth = 1;
	}
	
	private void getArray(SampledField sf){
		int length = height * width * depth;
		raw = new byte[length];
		int array[] = new int[length];
		

		if(sf.getCompression() == libsbmlConstants.SPATIAL_COMPRESSIONKIND_DEFLATED)
			sf.getUncompressed(array);
		else
			sf.getSamples(array);

		intToByte(array, raw);
	}
	
	
	private void intToByte(int[] array, byte[] raw){		
		//TODO need to resolve when original image data is not 8 bit
		for(int i = 0, length = array.length; i < length;i++){
			raw[i] = (byte) array[i];
		}	
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSpatialImage()
	 */
	@Override
	public SpatialImage getSpatialImage() {
		return new SpatialImage(hashSampledValue, img);
	}
}
