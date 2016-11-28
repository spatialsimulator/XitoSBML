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
import ij.gui.TextRoi;
import ij.gui.Toolbar;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Fonts command and 
	the dialog displayed when you double click on the text tool. */
public class Text implements PlugIn, DialogListener {
	
	/** The Constant LOC_KEY. */
	private static final String LOC_KEY = "fonts.loc";
	
	/** The Constant styles. */
	private static final String[] styles = {"Plain", "Bold", "Italic", "Bold+Italic"};
	
	/** The Constant justifications. */
	private static final String[] justifications = {"Left", "Center", "Right"};
	
	/** The gd. */
	private static GenericDialog gd;
	
	/** The font. */
	private String font = TextRoi.getFont();
	
	/** The font size. */
	private int fontSize = TextRoi.getSize();
	
	/** The style. */
	private int style = TextRoi.getStyle();
	
	/** The justification. */
	private int justification = TextRoi.getGlobalJustification();
	
	/** The angle. */
	private int angle;
	
	/** The antialiased. */
	private boolean antialiased = TextRoi.isAntialiased();
	
	/** The color. */
	private Color color = Toolbar.getForegroundColor();
	
	/** The color name. */
	private String colorName;

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public synchronized void run(String arg) {
 		if (gd!=null && gd.isVisible())
 			gd.toFront();
 		else
			showDialog();
	}
				
	/**
	 * Show dialog.
	 */
	private void showDialog() {
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		TextRoi textRoi = roi!=null&&(roi instanceof TextRoi)?(TextRoi)roi:null;
		String fillc = "None";
		TextRoi.setDefaultFillColor(null);
		TextRoi.setDefaultAngle(0.0);
		if (textRoi!=null) {
			Font font = textRoi.getCurrentFont();
			fontSize = font.getSize();
			angle = (int)textRoi.getAngle();
			style = font.getStyle();
			justification = textRoi.getJustification();
			Color c = textRoi.getStrokeColor();
			if (c!=null) color=c;
			fillc = Colors.colorToString2(textRoi.getFillColor());
			antialiased = textRoi.getAntialiased();
		}
		colorName = Colors.colorToString2(color);
		gd = new NonBlockingGenericDialog("Fonts");
		gd.addChoice("Font:", getFonts(), font);
		gd.addChoice("Style:", styles, styles[style]);
		gd.addChoice("Just:", justifications, justifications[justification]);
		gd.addChoice("Color:", Colors.getColors(colorName), colorName);
		gd.addChoice("Bkgd:", Colors.getColors("None",!"None".equals(fillc)?fillc:null), fillc);
		gd.addSlider("Size:", 9, 200, fontSize);
		gd.addSlider("Angle:", -90, 90, angle);
		gd.addCheckbox("Antialiased text", antialiased);
		Point loc = Prefs.getLocation(LOC_KEY);
		if (IJ.debugMode) {
			Dimension screen = IJ.getScreenSize();
			IJ.log("Fonts: "+loc+" "+screen);
		}
		if (loc!=null) {
			gd.centerDialog(false);
			gd.setLocation (loc);
		}
		gd.addDialogListener(this);
		gd.setOKLabel("Close");
		gd.hideCancelButton();
		gd.showDialog();
		Prefs.saveLocation(LOC_KEY, gd.getLocation());
	}

	/**
	 * Gets the fonts.
	 *
	 * @return the fonts
	 */
	String[] getFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		ArrayList names = new ArrayList();
		names.add("SansSerif");
		names.add("Serif");
		names.add("Monospaced");
		for (int i=0; i<fonts.length; i++) {
			String f = fonts[i];
			if (f.length()<=20 && !(f.equals("SansSerif")||f.equals("Serif")||f.equals("Monospaced")))
				names.add(f);
		}
		return (String[])names.toArray(new String[names.size()]);
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		TextRoi textRoi = roi!=null&&(roi instanceof TextRoi)?(TextRoi)roi:null;
		font = gd.getNextChoice();
		style = gd.getNextChoiceIndex();
		justification = gd.getNextChoiceIndex();
		String colorName2 = gd.getNextChoice();
		String fillc = gd.getNextChoice();
		fontSize = (int)gd.getNextNumber();
		angle = (int)gd.getNextNumber();
		antialiased = gd.getNextBoolean();
		if (colorName!=null && !colorName2.equals(colorName)) {
			Color color = Colors.decode(colorName2, null);
			Toolbar.setForegroundColor(color);
			colorName = colorName2;
		}
		TextRoi.setFont(font, fontSize, style, antialiased);
		TextRoi.setGlobalJustification(justification);
		Color fillColor = Colors.decode(fillc, null);
		TextRoi.setDefaultFillColor(fillColor);
		TextRoi.setDefaultAngle(angle);
		if (textRoi!=null) {
			textRoi.setAngle(angle);
			textRoi.setJustification(justification);
			textRoi.setFillColor(fillColor);
			textRoi.setAntialiased(antialiased);
		}
		return true;
	}
	
} 
