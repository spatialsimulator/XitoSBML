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
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;

// TODO: Auto-generated Javadoc
/** This plugin implements the File/Import/XY Coordinates command. It reads a
	two column text file, such as those created by File/Save As/XY Coordinates,
	as a polygon ROI. The ROI is displayed in the current image or, if the image
	is too small, in a new blank image.
*/
public class XY_Reader implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		TextReader tr = new TextReader();
		ImageProcessor ip = tr.open();
		if (ip==null)
			return;
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (width!=2 || height<3) {
			IJ.showMessage("XY Reader", "Two column text file required");
			return;
		}
		float[] x = new float[height];
		float[] y = new float[height];
		boolean allIntegers = true;
		double length = 0.0;
		for (int i=0; i<height; i++) {
			x[i] = ip.getf(0, i);
			y[i] = ip.getf(1, i);
			if ((int)x[i]!=x[i] || (int)y[i]!=y[i])
				allIntegers = false;
			if (i>0) {
				double dx = x[i] - x[i-1];
				double dy = y[i] - y[i-1];
				length += Math.sqrt(dx*dx+dy*dy);
			}
		}
		Roi roi = null;
		int type = length/x.length>10?Roi.POLYGON:Roi.FREEROI;
		if (allIntegers)
			roi = new PolygonRoi(Roi.toIntR(x), Roi.toIntR(y), height, type);
		else
			roi = new PolygonRoi(x, y, height, type);
		Rectangle r = roi.getBoundingRect();
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null || imp.getWidth()<r.x+r.width || imp.getHeight()<r.y+r.height) {
			new ImagePlus(tr.getName(), new ByteProcessor(Math.abs(r.x)+r.width+10, Math.abs(r.y)+r.height+10)).show();
			imp = WindowManager.getCurrentImage();
		}
		if (imp!=null)
			imp.setRoi(roi);
	}
	
}
