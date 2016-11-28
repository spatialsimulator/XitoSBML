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
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

// TODO: Auto-generated Javadoc
/** This plugin implements the Plugins/Utilities/Capture Screen
    and Plugins/Utilities/Capture Image commands. */
public class ScreenGrabber implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp2 = null;
		if (arg.equals("image") || arg.equals("flatten"))
			imp2 = captureImage();
		else
			imp2 = captureScreen();
		if (imp2!=null)
			imp2.show();
	}
    
	/**
	 *  Captures the entire screen and returns it as an ImagePlus.
	 *
	 * @return the image plus
	 */
	public ImagePlus captureScreen() {
		ImagePlus imp = null;
		try {
			Robot robot = new Robot();
			Dimension dimension = IJ.getScreenSize();
			Rectangle r = new Rectangle(dimension);
			Image img = robot.createScreenCapture(r);
			if (img!=null) imp = new ImagePlus("Screenshot", img);
		} catch(Exception e) {}
		return imp;
	}

	/**
	 *  Captures the active image window and returns it as an ImagePlus.
	 *
	 * @return the image plus
	 */
	public ImagePlus captureImage() {
		ImagePlus imp = IJ.getImage();
		if (imp==null) {
			IJ.noImage();
			return null;
		}
		ImageWindow win = imp.getWindow();
		if (win==null) return null;
		win.toFront();
		IJ.wait(500);
		Point loc = win.getLocation();
		ImageCanvas ic = win.getCanvas();
		Rectangle bounds = ic.getBounds();
		loc.x += bounds.x;
		loc.y += bounds.y;
		Rectangle r = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
		ImagePlus imp2 = null;
		Image img = null;
		boolean wasHidden = ic.hideZoomIndicator(true);
		try {
			Robot robot = new Robot();
			img = robot.createScreenCapture(r);
		} catch(Exception e) { }
		ic.hideZoomIndicator(wasHidden);
		if (img!=null) {
			String title = WindowManager.getUniqueName(imp.getTitle());
			imp2 = new ImagePlus(title, img);
		}
		return imp2;
	}

}

