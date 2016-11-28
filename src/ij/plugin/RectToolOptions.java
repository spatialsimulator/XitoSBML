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
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.gui.Toolbar;

import java.awt.AWTEvent;
import java.awt.Color;

// TODO: Auto-generated Javadoc
/** This plugin implements the rounded rectangle tool dialog box. */
public class RectToolOptions implements PlugIn, DialogListener {
	
	/** The fill color name. */
	private String strokeColorName, fillColorName;
	
	/** The gd. */
	private static GenericDialog gd;
	
	/** The default stroke width. */
	private static double defaultStrokeWidth = 0.0;
	
	/** The default stroke color. */
	private static Color defaultStrokeColor;

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public void run(String arg) {
 		if (gd!=null && gd.isVisible())
 			gd.toFront();
 		else
			rectToolOptions();
	}
				
	/**
	 * Rect tool options.
	 */
	void rectToolOptions() {
		if (defaultStrokeColor==null)
			defaultStrokeColor = Roi.getColor();
		Color strokeColor = defaultStrokeColor;
		Color fillColor = null;
		if (defaultStrokeWidth==0.0)
			defaultStrokeWidth = 1.0;
		double strokeWidth = defaultStrokeWidth;
		int cornerDiameter = (int)Prefs.get(Toolbar.CORNER_DIAMETER, 20);
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		if (roi!=null && (roi.getType()==Roi.RECTANGLE)) {
			strokeColor = roi.getStrokeColor();
			if (strokeColor==null)
				strokeColor = Roi.getColor();
			fillColor = roi.getFillColor();
			strokeWidth = roi.getStrokeWidth();
			cornerDiameter = roi.getCornerDiameter();
		}
		String strokec = Colors.colorToString(strokeColor);
		String fillc = Colors.colorToString(fillColor);

		gd = new NonBlockingGenericDialog("Rounded Rectangle Tool");
		gd.addSlider("Stroke width:", 1, 25, (int)strokeWidth);
		gd.addSlider("Corner diameter:", 0, 200, cornerDiameter);
		gd.addStringField("Color: ", strokec);
		gd.addStringField("Fill color: ", fillc);
		gd.addDialogListener(this);
		gd.showDialog();
	}

	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		double strokeWidth2 = gd.getNextNumber();
		int cornerDiameter2 = (int)gd.getNextNumber();
		String strokec2 = gd.getNextString();
		String fillc2 = gd.getNextString();
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		Color strokeColor2 = Colors.decode(strokec2, defaultStrokeColor);
		if (roi!=null && (roi.getType()==Roi.RECTANGLE)) {
			roi.setStrokeWidth((int)strokeWidth2);
			roi.setCornerDiameter((int)(cornerDiameter2));
			strokeColor2 = Colors.decode(strokec2, roi.getStrokeColor());
			Color fillColor = Colors.decode(fillc2, roi.getFillColor());
			roi.setStrokeColor(strokeColor2);
			roi.setFillColor(fillColor);
		}
		defaultStrokeWidth = strokeWidth2;
		defaultStrokeColor = strokeColor2;
		Toolbar.setRoundRectArcSize(cornerDiameter2);
		if (cornerDiameter2>0) {
			if (!Toolbar.getToolName().equals("roundrect"))
				IJ.setTool("roundrect");
		}
		return true;
	}
	
	/**
	 * Gets the default stroke color.
	 *
	 * @return the default stroke color
	 */
	public static Color getDefaultStrokeColor() {
		return defaultStrokeColor;
	}
	
	/**
	 * Gets the default stroke width.
	 *
	 * @return the default stroke width
	 */
	public static float getDefaultStrokeWidth() {
		return (float)defaultStrokeWidth;
	}
	
} 
