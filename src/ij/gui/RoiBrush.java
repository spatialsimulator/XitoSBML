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
package ij.gui;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.Point;
import java.awt.Polygon;

// TODO: Auto-generated Javadoc
/** Implements the ROI Brush tool.*/
class RoiBrush implements Runnable {
	
	/** The subtract. */
	static int ADD=0, SUBTRACT=1;
	
	/** The shift. */
	static int leftClick=16, alt=9, shift=1;
	
	/** The poly. */
	private Polygon poly;
	
	/** The previous P. */
	private Point previousP;
	
	/** The mode. */
	private int mode = ADD;
 
	/**
	 * Instantiates a new roi brush.
	 */
	RoiBrush() {
		Thread thread = new Thread(this, "RoiBrush");
		thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		int size = Toolbar.getBrushSize();
		ImagePlus img = WindowManager.getCurrentImage();
		if (img==null) return;
		ImageCanvas ic = img.getCanvas();
		if (ic==null) return;
		Roi roi = img.getRoi();
		if (roi!=null && !roi.isArea())
			img.deleteRoi();
		Point p = ic.getCursorLoc();
		if (roi!=null && !roi.contains(p.x, p.y))
			mode = SUBTRACT;
		int flags;
		while (true) {
			p = ic.getCursorLoc();
			if (p.equals(previousP))
				{IJ.wait(1); continue;}
			previousP = p;
			flags = ic.getModifiers();
			if ((flags&leftClick)==0) return;
			if ((flags&shift)!=0)
				mode = ADD;
			else if ((flags&alt)!=0)
				mode = SUBTRACT;
			if (mode==ADD)
				addCircle(img, p.x, p.y, size);
			else
				subtractCircle(img, p.x, p.y, size);
		}
	}

	/**
	 * Adds the circle.
	 *
	 * @param img the img
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 */
	void addCircle(ImagePlus img, int x, int y, int width) {
		Roi roi = img.getRoi();
		Roi roi2 = roi;
		if (roi2!=null) {
			if (!(roi2 instanceof ShapeRoi))
				roi2 = new ShapeRoi(roi2);
			((ShapeRoi)roi2).or(getCircularRoi(x, y, width));
			roi2.copyAttributes(roi);
		} else
			roi2 = new OvalRoi(x-width/2, y-width/2, width, width);
		img.setRoi(roi2);
	}

	/**
	 * Subtract circle.
	 *
	 * @param img the img
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 */
	void subtractCircle(ImagePlus img, int x, int y, int width) {
		Roi roi = img.getRoi();
		Roi roi2 = roi;
		if (roi2!=null) {
			if (!(roi2 instanceof ShapeRoi))
			roi2 = new ShapeRoi(roi2);
			((ShapeRoi)roi2).not(getCircularRoi(x, y, width));
			roi2.copyAttributes(roi);
			img.setRoi(roi2);
		}
	}

    
	/**
	 * Gets the circular roi.
	 *
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @return the circular roi
	 */
	ShapeRoi getCircularRoi(int x, int y, int width) {
		if (poly==null) {
			Roi roi = new OvalRoi(x-width/2, y-width/2, width, width);
			poly = roi.getPolygon();
			for (int i=0; i<poly.npoints; i++) {
				poly.xpoints[i] -= x;
				poly.ypoints[i] -= y;
			}
		}
		return new ShapeRoi(x, y, poly);
	}

}

