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

import ij.ImagePlus;

import java.util.HashMap;

import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class ImageGeometryData extends AbstractData {
	protected Geometry g;
	protected  HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	protected ImagePlus img = new ImagePlus();
	protected byte raw[];
	
	ImageGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd);
		this.g = g;
	}

	abstract void getSampledValues();
	abstract void createImage();
	public abstract SpatialImage getSpatialImage();
}
