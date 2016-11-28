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
package ij.plugin.filter;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.Random;

// TODO: Auto-generated Javadoc
/** Implements ImageJ's Process/Noise/Salt and Pepper command. */
public class SaltAndPepper implements PlugInFilter {

	/** The r. */
	Random r = new Random();

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		return IJ.setupDialog(imp, DOES_8G+DOES_8C+SUPPORTS_MASKING);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		add(ip, 0.05);
	}

	/**
	 * Rand.
	 *
	 * @param min the min
	 * @param max the max
	 * @return the int
	 */
	public int rand(int min, int max) {
		return min + r.nextInt(max-min);
	}

	/**
	 * Adds the.
	 *
	 * @param ip the ip
	 * @param percent the percent
	 */
	public void add(ImageProcessor ip, double percent) {
		Rectangle roi = ip.getRoi();
		int n = (int)(percent*roi.width*roi.height);
		byte[] pixels = (byte[])ip.getPixels();
		int rx, ry;
		int width = ip.getWidth();
		int xmin = roi.x;
		int xmax = roi.x+roi.width;
		int ymin = roi.y;
		int ymax = roi.y+roi.height;
		for (int i=0; i<n/2; i++) {
			rx = rand(xmin, xmax);
			ry = rand(ymin, ymax);
			pixels[ry*width+rx] = (byte)255;
			rx = rand(xmin, xmax);
			ry = rand(ymin, ymax);
			pixels[ry*width+rx] = (byte)0;
		}
	}
}

