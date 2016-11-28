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
import ij.gui.Arrow;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.gui.Toolbar;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;

// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Arrow Tool command. */
public class ArrowToolOptions implements PlugIn, DialogListener {
	
	/** The color name. */
	private String colorName;
	
	/** The gd. */
	private static GenericDialog gd;
	
	/** The Constant LOC_KEY. */
	private static final String LOC_KEY = "arrows.loc";

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public void run(String arg) {
 		if (gd!=null && gd.isVisible())
 			gd.toFront();
 		else
			arrowToolOptions();
	}
				
	/**
	 * Arrow tool options.
	 */
	void arrowToolOptions() {
		if (!Toolbar.getToolName().equals("arrow"))
			IJ.setTool("arrow");
		double width = Arrow.getDefaultWidth();
		double headSize = Arrow.getDefaultHeadSize();
		Color color = Toolbar.getForegroundColor();
		colorName = Colors.colorToString2(color);
		int style = Arrow.getDefaultStyle();
		gd = new NonBlockingGenericDialog("Arrow Tool");
		gd.addSlider("Width:", 1, 50, (int)width);
		gd.addSlider("Size:", 0, 50, headSize);
		gd.addChoice("Color:", Colors.getColors(colorName), colorName);
		gd.addChoice("Style:", Arrow.styles, Arrow.styles[style]);
		gd.addCheckbox("Outline", Arrow.getDefaultOutline());
		gd.addCheckbox("Double head", Arrow.getDefaultDoubleHeaded());
		gd.addCheckbox("Keep after adding to overlay", Prefs.keepArrowSelections);
		gd.addDialogListener(this);
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null) {
			gd.centerDialog(false);
			gd.setLocation (loc);
		}
		gd.showDialog();
		Prefs.saveLocation(LOC_KEY, gd.getLocation());
	}

	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		double width2 = gd.getNextNumber();
		double headSize2 = gd.getNextNumber();
		String colorName2 = gd.getNextChoice();
		int style2 = gd.getNextChoiceIndex();
		boolean outline2 = gd.getNextBoolean();
		boolean doubleHeaded2 = gd.getNextBoolean();
		Prefs.keepArrowSelections = gd.getNextBoolean();
		if (colorName!=null && !colorName2.equals(colorName)) {
			Color color = Colors.decode(colorName2, null);
			Toolbar.setForegroundColor(color);
		}
		colorName = colorName2;
		Arrow.setDefaultWidth(width2);
		Arrow.setDefaultHeadSize(headSize2);
		Arrow.setDefaultStyle(style2);
		Arrow.setDefaultOutline(outline2);
		Arrow.setDefaultDoubleHeaded(doubleHeaded2);
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return true;
		Roi roi = imp.getRoi();
		if (roi==null) return true;
		if (roi instanceof Arrow) {
			Arrow arrow = (Arrow)roi;
			roi.setStrokeWidth((float)width2);
			arrow.setHeadSize(headSize2);
			arrow.setStyle(style2);
			arrow.setOutline(outline2);
			arrow.setDoubleHeaded(doubleHeaded2);
			imp.draw();
		}
		Prefs.set(Arrow.STYLE_KEY, style2);
		Prefs.set(Arrow.WIDTH_KEY, width2);
		Prefs.set(Arrow.SIZE_KEY, headSize2);
		Prefs.set(Arrow.OUTLINE_KEY, outline2);
		Prefs.set(Arrow.DOUBLE_HEADED_KEY, doubleHeaded2);
		return true;
	}
	
} 
