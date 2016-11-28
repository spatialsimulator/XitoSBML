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
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.util.Tools;

import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Overlay/Labels command. */
public class OverlayLabels implements PlugIn, DialogListener {
	
	/** The Constant fontSizes. */
	private static final String[] fontSizes = {"7", "8", "9", "10", "12", "14", "18", "24", "28", "36", "48", "72"};
	
	/** The default overlay. */
	private static Overlay defaultOverlay = new Overlay();
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The overlay. */
	private Overlay overlay;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The show labels. */
	private boolean showLabels;
	
	/** The show names. */
	private boolean showNames;
	
	/** The draw backgrounds. */
	private boolean drawBackgrounds;
	
	/** The color name. */
	private String colorName;
	
	/** The font size. */
	private int fontSize;
	
	/** The bold. */
	private boolean bold;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		imp = WindowManager.getCurrentImage();
		overlay = null;
		if (imp!=null) {
			ImageCanvas ic = imp.getCanvas();
			if (ic!=null)
				overlay = ic.getShowAllList();
			if (overlay==null)
				overlay = imp.getOverlay();
		}
		if (overlay==null)
			overlay = defaultOverlay;
		showDialog();
		if (!gd.wasCanceled()) {
			defaultOverlay.drawLabels(overlay.getDrawLabels());
			defaultOverlay.drawNames(overlay.getDrawNames());
			defaultOverlay.drawBackgrounds(overlay.getDrawBackgrounds());
			defaultOverlay.setLabelColor(overlay.getLabelColor());
			defaultOverlay.setLabelFont(overlay.getLabelFont());
		}
	}
	
	/**
	 * Show dialog.
	 */
	public void showDialog() {
		showLabels = overlay.getDrawLabels();
		showNames = overlay.getDrawNames();
		drawBackgrounds = overlay.getDrawBackgrounds();
		colorName = Colors.getColorName(overlay.getLabelColor(), "white");
		fontSize = 12;
		Font font = overlay.getLabelFont();
		if (font!=null) {
			fontSize = font.getSize();
			bold = font.getStyle()==Font.BOLD;
		}
		gd = new GenericDialog("Labels");
		gd.addChoice("Color:", Colors.colors, colorName);
		gd.addChoice("Font size:", fontSizes, ""+fontSize);
		gd.addCheckbox("Show labels", showLabels);
		gd.addCheckbox("Use names as labels", showNames);
		gd.addCheckbox("Draw backgrounds", drawBackgrounds);
		gd.addCheckbox("Bold", bold);
		gd.addDialogListener(this);
		gd.showDialog();
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (gd.wasCanceled()) return false;
		String colorName2 = colorName;
		boolean showLabels2 = showLabels;
		boolean showNames2 = showNames;
		boolean drawBackgrounds2 = drawBackgrounds;
		boolean bold2 = bold;
		int fontSize2 = fontSize;
		colorName = gd.getNextChoice();
		fontSize = (int)Tools.parseDouble(gd.getNextChoice(), 12);
		showLabels = gd.getNextBoolean();
		showNames = gd.getNextBoolean();
		drawBackgrounds = gd.getNextBoolean();
		bold = gd.getNextBoolean();
		boolean colorChanged = !colorName.equals(colorName2);
		boolean sizeChanged = fontSize!=fontSize2;
		boolean changes = showLabels!=showLabels2 || showNames!=showNames2
			|| drawBackgrounds!=drawBackgrounds2 || colorChanged || sizeChanged
			|| bold!=bold2;
		if (changes) {
			if (showNames || colorChanged || sizeChanged) {
				showLabels = true;
				Vector checkboxes = gd.getCheckboxes();
				((Checkbox)checkboxes.elementAt(0)).setState(true);
			}
			overlay.drawLabels(showLabels);
			overlay.drawNames(showNames);
			overlay.drawBackgrounds(drawBackgrounds);
			Color color = Colors.getColor(colorName, Color.white);
			overlay.setLabelColor(color);
			if (sizeChanged || bold || bold!=bold2)
				overlay.setLabelFont(new Font("SansSerif", bold?Font.BOLD:Font.PLAIN, fontSize));
			if (imp!=null) {
				Overlay o = imp.getOverlay();
				if (o==null) {
					ImageCanvas ic = imp.getCanvas();
					if (ic!=null)
						o = ic.getShowAllList();
				}
				if (o!=null)
					imp.draw();
			}
		}
		return true;
	}

	/**
	 *  Creates an empty Overlay that has the current label settings.
	 *
	 * @return the overlay
	 */
	public static Overlay createOverlay() {
		return defaultOverlay.duplicate();
	}

}
