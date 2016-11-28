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
import ij.Undo;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageRoi;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.Colors;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

// TODO: Auto-generated Javadoc
// Versions
// 2012-07-22 shift to confine horizontally or vertically, ctrl-shift to resize, ctrl to pick

	/**
 * The Class BrushTool.
 */
public class BrushTool extends PlugInTool implements Runnable {
	
	/** The Constant IDLE. */
	private final static int UNCONSTRAINED=0, HORIZONTAL=1, VERTICAL=2, RESIZING=3, RESIZED=4, IDLE=5; //mode flags
	
	/** The brush width key. */
	private static String BRUSH_WIDTH_KEY = "brush.width";
	
	/** The pencil width key. */
	private static String PENCIL_WIDTH_KEY = "pencil.width";
	
	/** The circle name. */
	private static String CIRCLE_NAME = "brush-tool-overlay";
	
	/** The Constant LOC_KEY. */
	private static final String LOC_KEY = "brush.loc";

	/** The width key. */
	private String widthKey;
	
	/** The width. */
	private int width;
	
	/** The ip. */
	private ImageProcessor ip;
	
	/** The mode. */
	private int mode;  //resizing brush or motion constrained horizontally or vertically
	
	/** The y start. */
	private int xStart, yStart;
	
	/** The old width. */
	private int oldWidth;
	
	/** The is pencil. */
	private boolean isPencil;
	
	/** The overlay. */
	private Overlay overlay;
	
	/** The options. */
	private Options options;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The overlay image. */
	private ImageRoi overlayImage;
	
	/** The paint on overlay. */
	private boolean paintOnOverlay;
	
	/** The brush instance. */
	private static BrushTool brushInstance;
	//private int transparency;

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#run(java.lang.String)
	 */
	public void run(String arg) {
		isPencil = "pencil".equals(arg);
		widthKey = isPencil ? PENCIL_WIDTH_KEY : BRUSH_WIDTH_KEY;
		width = (int)Prefs.get(widthKey, isPencil ? 1 : 5);
		Toolbar.addPlugInTool(this);
		if (!isPencil)
			brushInstance = this;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mousePressed(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mousePressed(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		int x = ic.offScreenX(e.getX());
		int y = ic.offScreenY(e.getY());
		xStart = x;
		yStart = y;
		checkForOverlay(imp);
		if (overlayImage!=null)
			ip = overlayImage.getProcessor();
		else
			ip = imp.getProcessor();
		int ctrlMask = IJ.isMacintosh() ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
		int resizeMask = InputEvent.SHIFT_MASK | ctrlMask;
		if ((e.getModifiers() & resizeMask) == resizeMask) {
			mode = RESIZING;
			oldWidth = width;
			return;
		} else if ((e.getModifiers() & ctrlMask) != 0) {
			boolean altKeyDown = (e.getModifiers() & InputEvent.ALT_MASK) != 0;
			ic.setDrawingColor(x, y, altKeyDown); //pick color from image (ignore overlay)
			if (!altKeyDown)
				setColor(Toolbar.getForegroundColor());
			mode = IDLE;
			return;
		}
		mode = UNCONSTRAINED;
		ip.snapshot();
		Undo.setup(Undo.FILTER, imp);
		ip.setLineWidth(width);
		if (e.isAltDown()) {
			if (overlayImage!=null)
				ip.setColor(0); //erase
			else
				ip.setColor(Toolbar.getBackgroundColor());
		} else
			ip.setColor(Toolbar.getForegroundColor());
		ip.moveTo(x, y);
		if (!e.isShiftDown()) {
			ip.lineTo(x, y);
			if (overlayImage!=null) {
				overlayImage.setProcessor(ip);
				imp.draw();
			} else
				imp.updateAndDraw();
		}
	}
	
	/**
	 * Check for overlay.
	 *
	 * @param imp the imp
	 */
	private void checkForOverlay(ImagePlus imp) {
		if (paintOnOverlay && (overlayImage==null||getOverlayImage(imp)==null)) {
			ImageProcessor overlayIP = new ColorProcessor(imp.getWidth(), imp.getHeight());
			ImageRoi imageRoi = new ImageRoi(0, 0, overlayIP);
  			//imageRoi.setOpacity(1.0-transparency/100.0);
			imageRoi.setZeroTransparent(true);
			Overlay overlay = new Overlay(imageRoi);
			imp.setOverlay(overlay);
			overlayImage = imageRoi;
			return;
		}
		overlayImage = null;
		if (!paintOnOverlay)
			return;
		overlayImage = getOverlayImage(imp);
	}

	/**
	 * Gets the overlay image.
	 *
	 * @param imp the imp
	 * @return the overlay image
	 */
	private ImageRoi getOverlayImage(ImagePlus imp) {
		Overlay overlay = imp.getOverlay();
		if (overlay==null)
			return null;
		Roi roi = overlay.size()>0?overlay.get(0):null;
		if (roi==null||!(roi instanceof ImageRoi))
			return null;
		Rectangle bounds = roi.getBounds();
		if (bounds.x!=0||bounds.y!=0||bounds.width!=imp.getWidth()||bounds.height!=imp.getHeight())
			return null;
		return (ImageRoi)roi;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseDragged(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseDragged(ImagePlus imp, MouseEvent e) {
		if (mode == IDLE) return;
		ImageCanvas ic = imp.getCanvas();
		int x = ic.offScreenX(e.getX());
		int y = ic.offScreenY(e.getY());
		if (mode == RESIZING) {
			showToolSize(x-xStart, imp);
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
		ip.lineTo(x, y);
		if (overlayImage!=null) {
			overlayImage.setProcessor(ip);
			imp.draw();
		} else
			imp.updateAndDraw();
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseReleased(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseReleased(ImagePlus imp, MouseEvent e) {
		if (mode==RESIZING) {
			if (overlay!=null && overlay.size()>0 && CIRCLE_NAME.equals(overlay.get(overlay.size()-1).getName())) {
				overlay.remove(overlay.size()-1);
				imp.setOverlay(overlay);
			}
			overlay = null;
			if (e.isShiftDown()) {
				setWidth(width);
				Prefs.set(widthKey, width);
			}
		}
	}

	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	private void setWidth(int width) {
		if (gd==null)
			return;
		Vector numericFields = gd.getNumericFields();
		TextField widthField  = (TextField)numericFields.elementAt(0);
		widthField.setText(""+width);
		Vector sliders = gd.getSliders();
		Scrollbar sb = (Scrollbar)sliders.elementAt(0);
		sb.setValue(width);
	}
			
	/**
	 * Sets the color.
	 *
	 * @param c the new color
	 */
	private void setColor(Color c) {
		if (gd==null)
			return;
		String name = Colors.colorToString2(c);
		if (name.length()>0) {
			Vector choices = gd.getChoices();
			Choice ch = (Choice)choices.elementAt(0);
			ch.select(name);
		}
	}


	/**
	 * Show tool size.
	 *
	 * @param deltaWidth the delta width
	 * @param imp the imp
	 */
	private void showToolSize(int deltaWidth, ImagePlus imp) {
		if (deltaWidth !=0) {
			width = oldWidth + deltaWidth;
			if (width<1) width=1;
			Roi circle = new OvalRoi(xStart-width/2, yStart-width/2, width, width);
			circle.setName(CIRCLE_NAME);
			circle.setStrokeColor(Color.red);
			overlay = imp.getOverlay();
			if (overlay==null)
				overlay = new Overlay();
			else if (overlay.size()>0 && CIRCLE_NAME.equals(overlay.get(overlay.size()-1).getName()))
				overlay.remove(overlay.size()-1);
			overlay.add(circle);
			imp.setOverlay(overlay);
		}
		IJ.showStatus((isPencil?"Pencil":"Brush")+" width: "+ width);
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
		if (isPencil)
			return "Pencil Tool";
		else
			return "Paintbrush Tool";
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getToolIcon()
	 */
	public String getToolIcon() {
		if (isPencil)
			return "C037L4990L90b0Lc1c3L82a4Lb58bL7c4fDb4L494fC123L5a5dL6b6cD7b";
		else
			return "C037La077Ld098L6859L4a2fL2f4fL5e9bL9b98L6888L5e8dL888cC123L8a3fL8b6d";
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
			showDialog();
		}
		
		/**
		 * Show dialog.
		 */
		public void showDialog() {
			Color color = Toolbar.getForegroundColor();
			String colorName = Colors.colorToString2(color);
			String name = isPencil?"Pencil":"Brush";
			gd = new NonBlockingGenericDialog(name+" Options");
			gd.addSlider(name+" width:", 1, 50, width);
			//gd.addSlider("Transparency (%):", 0, 100, transparency);
			gd.addChoice("Color:", Colors.getColors(colorName), colorName);
			gd.addCheckbox("Paint on overlay", paintOnOverlay);
			gd.setInsets(10, 10, 0);
			String ctrlString = IJ.isMacintosh()? "CMD":"CTRL";
			gd.addMessage("SHIFT for horizontal or vertical lines\n"+
					"ALT to draw in background color (or\n"+
					"to erase if painting on overlay)\n"+
					ctrlString+"-SHIFT-drag to change "+(isPencil ? "pencil" : "brush")+" width\n"+
					ctrlString+"-(ALT) click to change foreground\n"+
					"(background) color, or use Color Picker", null, Color.darkGray);
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
			gd = null;
		}

		/* (non-Javadoc)
		 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
		 */
		public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
			if (e!=null && e.toString().contains("Undo")) {
				ImagePlus imp = WindowManager.getCurrentImage();
				if (imp!=null) IJ.run("Undo");
				return true;
			}
			width = (int)gd.getNextNumber();
			if (gd.invalidNumber() || width<0)
				width = (int)Prefs.get(widthKey, 1);
			//transparency = (int)gd.getNextNumber();
			//if (gd.invalidNumber() || transparency<0 || transparency>100)
			//	transparency = 100;
			String colorName = gd.getNextChoice();
			paintOnOverlay = gd.getNextBoolean();
			Color color = Colors.decode(colorName, null);
			Toolbar.setForegroundColor(color);
			Prefs.set(widthKey, width);
			return true;
		}
	}
	
	/**
	 * Sets the brush width.
	 *
	 * @param width the new brush width
	 */
	public static void setBrushWidth(int width) {
		if (brushInstance!=null) {
			Color c = Toolbar.getForegroundColor();
			brushInstance.setWidth(width);
			Toolbar.setForegroundColor(c);
		}
	}

}