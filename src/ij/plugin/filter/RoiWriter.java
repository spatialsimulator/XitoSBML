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
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.io.SaveDialog;
import ij.process.ImageProcessor;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/** Saves the current ROI outline to a file. RoiDecoder.java 
	has a description of the file format.
	@see ij.io.RoiDecoder
	@see ij.plugin.RoiReader
*/
public class RoiWriter implements PlugInFilter {
	
	/** The imp. */
	ImagePlus imp;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+ROI_REQUIRED+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		try {
			saveRoi(imp);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = ""+e;
			IJ.error("ROI Writer", msg);
		}
	}

	/**
	 * Save roi.
	 *
	 * @param imp the imp
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void saveRoi(ImagePlus imp) throws IOException{
		Roi roi = imp.getRoi();
		if (roi==null)
			throw new IllegalArgumentException("ROI required");
		String name = roi.getName();
		if (name==null)
			name = imp.getTitle();
		SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
		name = sd.getFileName();
		if (name == null)
			return;
		String dir = sd.getDirectory();
		RoiEncoder re = new RoiEncoder(dir+name);
		re.write(roi);
		if (name.endsWith(".roi"))
			name = name.substring(0, name.length()-4);
		roi.setName(name);
	}
	
}
