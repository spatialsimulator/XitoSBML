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
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;

import java.awt.Rectangle;

// TODO: Auto-generated Javadoc
/** Implements the Flip and Rotate commands in the Image/Transform submenu. */
public class Transformer implements PlugInFilter {
	
	/** The imp. */
	ImagePlus imp;
	
	/** The arg. */
	String arg;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (arg.equals("fliph") || arg.equals("flipv"))
			return IJ.setupDialog(imp, DOES_ALL+NO_UNDO);
		else
			return DOES_ALL+NO_UNDO+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		Calibration cal = imp.getCalibration();
		boolean transformOrigin = cal.xOrigin!=0 || cal.yOrigin!=0;
		if (arg.equals("fliph")) {
			ip.flipHorizontal();
			Rectangle r = ip.getRoi();
			if (transformOrigin && r.x==0 && r.y==0 && r.width==ip.getWidth() && r.height==ip.getHeight())
				cal.xOrigin = imp.getWidth()-1 - cal.xOrigin;
			return;
		}
		if (arg.equals("flipv")) {
			ip.flipVertical();
			Rectangle r = ip.getRoi();
			if (transformOrigin && r.x==0 && r.y==0 && r.width==ip.getWidth() && r.height==ip.getHeight())
	    		cal.yOrigin = imp.getHeight()-1 - cal.yOrigin;
			return;
		}
		if (arg.equals("right") || arg.equals("left")) {
	    	StackProcessor sp = new StackProcessor(imp.getStack(), ip);
	    	ImageStack s2 = null;
			if (arg.equals("right")) {
	    		s2 = sp.rotateRight();
	    		if (transformOrigin) {
	    			double xOrigin = imp.getWidth()-1 - cal.yOrigin;
	    			double yOrigin = cal.xOrigin;
	    			cal.xOrigin = xOrigin;
	    			cal.yOrigin = yOrigin;
	    		}
	    	} else {
	    		s2 = sp.rotateLeft();
	    		if (transformOrigin) {
	    			double xOrigin = cal.yOrigin;
	    			double yOrigin = imp.getHeight()-1 - cal.xOrigin;
	    			cal.xOrigin = xOrigin;
	    			cal.yOrigin = yOrigin;
	    		}
	    	}
	    	imp.setStack(null, s2);
	    	double pixelWidth = cal.pixelWidth;
	    	cal.pixelWidth = cal.pixelHeight;
	    	cal.pixelHeight = pixelWidth;
			if (!cal.getXUnit().equals(cal.getYUnit())) {
				String xUnit = cal.getXUnit();
				cal.setXUnit(cal.getYUnit());
				cal.setYUnit(xUnit);
			}
			return;
		}
	}
	
}
