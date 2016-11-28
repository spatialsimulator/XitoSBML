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

// TODO: Auto-generated Javadoc
/** Implements the commands in the Process/Shadows submenu. */
public class Shadows implements PlugInFilter {
	
	/** The arg. */
	String arg;
	
	/** The imp. */
	ImagePlus imp;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (imp!=null && imp.getStackSize()>1 && arg.equals("demo"))
			{IJ.error("This demo does not work with stacks."); return DONE;}
		return IJ.setupDialog(imp, DOES_ALL+SUPPORTS_MASKING);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		if (arg.equals("demo")) {
			IJ.resetEscape();
			while (!IJ.escapePressed()) {
				north(ip); imp.updateAndDraw(); ip.reset();
				northeast(ip); imp.updateAndDraw(); ip.reset();
				east(ip); imp.updateAndDraw(); ip.reset();
				southeast(ip); imp.updateAndDraw(); ip.reset();
				south(ip); imp.updateAndDraw(); ip.reset();
				southwest(ip); imp.updateAndDraw(); ip.reset();
				west(ip); imp.updateAndDraw(); ip.reset();
				northwest(ip); imp.updateAndDraw(); ip.reset();
			}
		}
		else if (arg.equals("north")) north(ip);
		else if (arg.equals("northeast")) northeast(ip);
		else if (arg.equals("east")) east(ip);
		else if (arg.equals("southeast")) southeast(ip);
		else if (arg.equals("south")) south(ip);
		else if (arg.equals("southwest")) southwest(ip);
		else if (arg.equals("west")) west(ip);
		else if (arg.equals("northwest")) northwest(ip);

	}
		
		
		/**
		 * North.
		 *
		 * @param ip the ip
		 */
		public void north(ImageProcessor ip) {
			int[] kernel = {1,2,1, 0,1,0,  -1,-2,-1};
			ip.convolve3x3(kernel);
		}

		/**
		 * South.
		 *
		 * @param ip the ip
		 */
		public void south(ImageProcessor ip) {
			int[] kernel = {-1,-2,-1,  0,1,0,  1,2,1};
			ip.convolve3x3(kernel);
		}

		/**
		 * East.
		 *
		 * @param ip the ip
		 */
		public void east(ImageProcessor ip) {
			int[] kernel = {-1,0,1,  -2,1,2,  -1,0,1};
			ip.convolve3x3(kernel);
		}

		/**
		 * West.
		 *
		 * @param ip the ip
		 */
		public void west(ImageProcessor ip) {
			int[] kernel = {1,0,-1,  2,1,-2,  1,0,-1};
			ip.convolve3x3(kernel);
		}

		/**
		 * Northwest.
		 *
		 * @param ip the ip
		 */
		public void northwest(ImageProcessor ip) {
			int[] kernel = {2,1,0,  1,1,-1,  0,-1,-2};
			ip.convolve3x3(kernel);
		}

		/**
		 * Southeast.
		 *
		 * @param ip the ip
		 */
		public void southeast(ImageProcessor ip) {
			int[] kernel = {-2,-1,0,  -1,1,1,  0,1,2};
			ip.convolve3x3(kernel);
		}
		
		/**
		 * Northeast.
		 *
		 * @param ip the ip
		 */
		public void northeast(ImageProcessor ip) {
			int[] kernel = {0,1,2,  -1,1,1,  -2,-1,0};
			ip.convolve3x3(kernel);
		}
		
		/**
		 * Southwest.
		 *
		 * @param ip the ip
		 */
		public void southwest(ImageProcessor ip) {
			int[] kernel = {0,-1,-2,  1,1,-1,  2,1,0};
			ip.convolve3x3(kernel);
		}
}
