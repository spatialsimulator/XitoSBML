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
package ij.plugin.tool;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.Toolbar;
import ij.plugin.Colors;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.Vector;

// TODO: Auto-generated Javadoc
//Version history
// 2012-07-14 shift to confine horizontally or vertically, ctrl-shift to resize
// 2012-07-22 options allow width=0; width&transparency range checking, alt for BG, CTRL to pick color

/**
 * The Class OverlayBrushTool.
 */
public class OverlayBrushTool extends PlugInTool implements Runnable {
	
	/** The Constant IDLE. */
	private final static int UNCONSTRAINED=0, HORIZONTAL=1, VERTICAL=2, DO_RESIZE=3, RESIZED=4, IDLE=5; //mode flags
	
	/** The width key. */
	private static String WIDTH_KEY = "obrush.width";
	
	/** The Constant LOC_KEY. */
	private static final String LOC_KEY = "obrush.loc";
	
	/** The width. */
	private float width = (float)Prefs.get(WIDTH_KEY, 5);
	
	/** The transparency. */
	private int transparency;
	
	/** The stroke. */
	private BasicStroke stroke;
	
	/** The path. */
	private GeneralPath path;
	
	/** The mode. */
	private int mode;  //resizing brush or motion constrained horizontally or vertically
	
	/** The y start. */
	private float xStart, yStart;
	
	/** The old width. */
	private float oldWidth = width;
	
	/** The new path. */
	private boolean newPath;
	
	/** The options. */
	private Options options;
	
	/** The gd. */
	private GenericDialog gd;

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mousePressed(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mousePressed(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		float x = (float)ic.offScreenXD(e.getX());
		float y = (float)ic.offScreenYD(e.getY());
		xStart = x;
		yStart = y;
		oldWidth = width;
		int ctrlMask = IJ.isMacintosh() ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
		int resizeMask = InputEvent.SHIFT_MASK | ctrlMask;
		if ((e.getModifiers() & resizeMask) == resizeMask) {
			mode = DO_RESIZE;
			return;
		} else if ((e.getModifiers() & ctrlMask) != 0) {  //Pick the color from image or overlay
			//Limitiation: no sub-pixel accuracy here.
			//Don't use awt.robot to pick the color, it is influenced by screen color calibration
			int[] rgbValues = imp.flatten().getPixel((int)x,(int)y);
			Color color = new Color(rgbValues[0],rgbValues[1],rgbValues[2]);
			boolean altKeyDown = (e.getModifiers() & InputEvent.ALT_MASK) != 0;
			if (altKeyDown)
				Toolbar.setBackgroundColor(color);
			else {
				Toolbar.setForegroundColor(color);
				if (gd != null)
					options.setColor(color);
			}
			mode = IDLE;
			return;
		}
		mode = UNCONSTRAINED;	//prepare drawing
		path = new GeneralPath();
		path.moveTo(x, y);
		newPath = true;
		stroke = new BasicStroke(width, BasicStroke.CAP_ROUND/*CAP_BUTT*/, BasicStroke.JOIN_ROUND);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseDragged(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseDragged(ImagePlus imp, MouseEvent e) {
		if (mode == IDLE) return;
		ImageCanvas ic = imp.getCanvas();
		float x = (float)ic.offScreenXD(e.getX());
		float y = (float)ic.offScreenYD(e.getY());
		Overlay overlay = imp.getOverlay();
		if (overlay==null)
			overlay = new Overlay();
		if (mode == DO_RESIZE || mode == RESIZED) {
			changeBrushSize((float)(x-xStart), imp);
			return;
		}
		if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) { //shift constrains
			if (mode == UNCONSTRAINED) {	//first movement with shift down determines direction
				if (Math.abs(x-xStart) > Math.abs(y-yStart))
					mode = HORIZONTAL;
				else if (Math.abs(x-xStart) < Math.abs(y-yStart))
					mode = VERTICAL;
				else return; //constraint direction still unclear
			}
			if (mode == HORIZONTAL)
				y = yStart;
			else if (mode == VERTICAL)
				x = xStart;
		} else {
			xStart = x;
			yStart = y;
			mode = UNCONSTRAINED;
		}
		path.lineTo(x, y);
		ShapeRoi roi = new ShapeRoi(path);
		boolean altKeyDown = (e.getModifiers() & InputEvent.ALT_MASK) != 0;
		Color color = altKeyDown ? Toolbar.getBackgroundColor() : Toolbar.getForegroundColor();
		float red = (float)(color.getRed()/255.0);
		float green = (float)(color.getGreen()/255.0);
		float blue = (float)(color.getBlue()/255.0);
		float alpha = (float)((100-transparency)/100.0);
		roi.setStrokeColor(new Color(red, green, blue, alpha));
		roi.setStroke(stroke);
		if (newPath) {
			overlay.add(roi);
			newPath = false;
		} else {
			overlay.remove(overlay.size()-1);
			overlay.add(roi);
		}
		imp.setOverlay(overlay);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseReleased(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseReleased(ImagePlus imp, MouseEvent e) {
		if (mode == RESIZED) {
			Overlay overlay = imp.getOverlay();
			overlay.remove(overlay.size()-1); //delete brush resizing circle
			imp.setOverlay(overlay);
			Prefs.set(WIDTH_KEY, width);
			if (gd!=null)
				options.setWidth(width);
		} else if (newPath)		// allow drawing a single dot
			mouseDragged(imp, e);
	}

	/**
	 * Change brush size.
	 *
	 * @param deltaWidth the delta width
	 * @param imp the imp
	 */
	private void changeBrushSize(float deltaWidth, ImagePlus imp) {
		if (deltaWidth!=0) {
			Overlay overlay = imp.getOverlay();
			width = oldWidth + deltaWidth;
			if (width < 0) width = 0;
			Roi circle = new OvalRoi(xStart-width/2, yStart-width/2, width, width);
			circle.setStrokeColor(Color.red);
			overlay = imp.getOverlay();
			if (overlay==null)
				overlay = new Overlay();
			if (mode == RESIZED)
				overlay.remove(overlay.size()-1);
			overlay.add(circle);
			imp.setOverlay(overlay);
		}
		IJ.showStatus("Overlay Brush width: "+IJ.d2s(width));
		mode = RESIZED;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#showOptionsDialog()
	 */
	public void showOptionsDialog() {
		Thread thread = new Thread(this, "Brush Options");
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getToolName()
	 */
	public String getToolName() {
		return "Overlay Brush Tool";
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getToolIcon()
	 */
	public String getToolIcon() {
		return "C037La077Ld098L6859L4a2fL2f4fL3f99L5e9bL9b98L6888L5e8dL888cC123P2f7f9ebdcaf70";
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		new Options();
	}

	/**
	 * The Class Options.
	 */
	class Options implements DialogListener {

		/**
		 * Instantiates a new options.
		 */
		Options() {
			if (gd != null) {
				gd.toFront();
				return;
			}
			options = this;
			if (IJ.debugMode) IJ.log("Options: true");
			showDialog();
		}

		/**
		 * Sets the width.
		 *
		 * @param width the new width
		 */
		//set 'width' textfield and adjust scrollbar
		void setWidth(float width) {
			Vector numericFields = gd.getNumericFields();
			TextField widthField  = (TextField)numericFields.elementAt(0);
			widthField.setText(IJ.d2s(width,1));
			Vector sliders = gd.getSliders();
			Scrollbar sb = (Scrollbar)sliders.elementAt(0);
			sb.setValue((int)(width+0.5f));
		}

		/**
		 * Sets the color.
		 *
		 * @param c the new color
		 */
		void setColor(Color c) {
			String name = Colors.getColorName(c, "");
			if (name.length() > 0) {
				Vector choices = gd.getChoices();
				Choice ch = (Choice)choices.elementAt(0);
				ch.select(name);
			}
		}

		/**
		 * Show dialog.
		 */
		public void showDialog() {
			Color color = Toolbar.getForegroundColor();
			String colorName = Colors.colorToString2(color);
			gd = new NonBlockingGenericDialog("Overlay Brush Options");
			gd.addSlider("Brush width (pixels):", 0, 50, width);
			gd.addSlider("Transparency (%):", 0, 100, transparency);
			gd.addChoice("Color:", Colors.getColors(colorName), colorName);
			gd.setInsets(10, 0, 0);
			String ctrlString = IJ.isMacintosh()? "CMD":"CTRL";
			gd.addMessage("SHIFT for horizontal or vertical lines\n"+
					"ALT to draw in background color\n"+
					ctrlString+"-SHIFT-drag to change brush width\n"+
					ctrlString+"-(ALT) click to change foreground (background) color\n"+
					"or use this dialog or the Color Picker (shift-k).", null, Color.darkGray);
			gd.hideCancelButton();
			gd.addHelp("");
			gd.setHelpLabel("Undo");
			gd.setOKLabel("Close");
			gd.addDialogListener(this);
			Point loc = Prefs.getLocation(LOC_KEY);
			if (loc!=null) {
				gd.centerDialog(false);
				gd.setLocation (loc);
			}
			gd.showDialog();
			Prefs.saveLocation(LOC_KEY, gd.getLocation());
			if (IJ.debugMode) IJ.log("Options: false");
			gd = null;
		}

		/* (non-Javadoc)
		 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
		 */
		public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
			if (e!=null && e.toString().contains("Undo")) {
				ImagePlus imp = WindowManager.getCurrentImage();
				if (imp==null) return true;
				Overlay overlay = imp.getOverlay();
				if (overlay!=null && overlay.size()>0) {
					overlay.remove(overlay.size()-1);
					imp.draw();
				}
				return true;
			}
			width = (float)gd.getNextNumber();
			if (gd.invalidNumber() || width<0)
				width = (float)Prefs.get(WIDTH_KEY, 5);
			transparency = (int)gd.getNextNumber();
			if (gd.invalidNumber() || transparency<0 || transparency>100)
				transparency = 100;
			String colorName = gd.getNextChoice();
			Color color = Colors.decode(colorName, null);
			Toolbar.setForegroundColor(color);
			Prefs.set(WIDTH_KEY, width);
			return true;
		}

	}
}