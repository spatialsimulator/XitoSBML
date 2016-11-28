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
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * The Class Duplicater.
 *
 * @deprecated replaced by Duplicator class
 */
public class Duplicater implements PlugInFilter {
	
	/** The imp. */
	ImagePlus imp;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
	}

	/**
	 * Duplicate stack.
	 *
	 * @param imp the imp
	 * @param newTitle the new title
	 * @return the image plus
	 */
	public ImagePlus duplicateStack(ImagePlus imp, String newTitle) {
		ImagePlus imp2 = (new Duplicator()).run(imp);
		imp2.setTitle(newTitle);
		return imp2;
	}
	
	/**
	 * Duplicate substack.
	 *
	 * @param imp the imp
	 * @param newTitle the new title
	 * @param first the first
	 * @param last the last
	 * @return the image plus
	 */
	public ImagePlus duplicateSubstack(ImagePlus imp, String newTitle, int first, int last) {
		ImagePlus imp2 = (new Duplicator()).run(imp, first, last);
		imp2.setTitle(newTitle);
		return imp2;
	}

}
