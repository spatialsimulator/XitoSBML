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
package ij.plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.OpenDialog;
import ij.io.RoiDecoder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/** Opens ImageJ, NIH Image and Scion Image for windows ROI outlines. 
	RoiDecoder.java has a description of the file format.
	@see ij.io.RoiDecoder
	@see ij.plugin.filter.RoiWriter
*/
public class RoiReader implements PlugIn {
	
	/** The traced. */
	final int polygon=0, rect=1, oval=2, line=3,freeLine=4, segLine=5, noRoi=6,freehand=7, traced=8;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open ROI...", arg);
		String dir = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return;
		try {
			openRoi(dir, name);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = ""+e;
			IJ.error("ROI Reader", msg);
		}
	}

	/**
	 * Open roi.
	 *
	 * @param dir the dir
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void openRoi(String dir, String name) throws IOException {
		String path = dir+name;
		RoiDecoder rd = new RoiDecoder(path);
		Roi roi = rd.getRoi();
		Rectangle r = roi.getBounds();
		ImagePlus img = WindowManager.getCurrentImage();
		if (img==null || img.getWidth()<(r.x+r.width) || img.getHeight()<(r.y+r.height)) {
			ImageProcessor ip =  new ByteProcessor(r.x+r.width+10, r.y+r.height+10);
			img = new ImagePlus(name, ip);
			img.show();
		}
		img.setRoi(roi);
	}

}
