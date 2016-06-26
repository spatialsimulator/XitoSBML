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

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;

import sbmlplugin.image.SpatialImage;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 25, 2015
 */
public abstract class ImageGeometryData extends AbstractData {
	
	/** The g. */
	protected Geometry g;
	
	/** The hash sampled value. */
	protected  HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	
	/** The img. */
	protected ImagePlus img = new ImagePlus();
	
	/** The raw. */
	protected byte raw[];
	
	/**
	 * Instantiates a new image geometry data.
	 *
	 * @param gd the gd
	 * @param g the g
	 */
	ImageGeometryData(GeometryDefinition gd, Geometry g) {
		super(gd);
		this.g = g;
	}

	/**
	 * Gets the sampled values.
	 *
	 * @return the sampled values
	 */
	abstract void getSampledValues();
	
	/**
	 * Creates the image.
	 */
	abstract void createImage();
	
	/**
	 * Gets the spatial image.
	 *
	 * @return the spatial image
	 */
	public abstract SpatialImage getSpatialImage();
}
