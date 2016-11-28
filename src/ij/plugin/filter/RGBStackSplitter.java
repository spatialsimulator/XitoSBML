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
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/** Deprecated; replaced by ij.plugin.ChannelSplitter. */
public class RGBStackSplitter implements PlugInFilter {
	
	/** The imp. */
	ImagePlus imp;
	
	/** The blue. */
	public ImageStack red, green, blue;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		(new ChannelSplitter()).run(arg);
		return DONE;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
	}

	/**
	 *  Deprecated; replaced by ij.plugin.ChannelSplitter.
	 *
	 * @param imp the imp
	 */
	public void split(ImagePlus imp) {
		WindowManager.setTempCurrentImage(imp);
		(new ChannelSplitter()).run("");
	}

	/**
	 *  Deprecated; replaced by ChannelSplitter.splitRGB().
	 *
	 * @param rgb the rgb
	 * @param keepSource the keep source
	 */
	public void split(ImageStack rgb, boolean keepSource) {
		ImageStack[] channels = ChannelSplitter.splitRGB(rgb, keepSource);
		red = channels[0];
		green = channels[1];
		blue = channels[2];
	}
	
}



