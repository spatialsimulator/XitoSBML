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
import ij.Macro;
import ij.Undo;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;


// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Rotate/Arbitrarily command. */
public class Rotator implements ExtendedPlugInFilter, DialogListener {
	
	/** The flags. */
	private int flags = DOES_ALL|SUPPORTS_MASKING|PARALLELIZE_STACKS;
	
	/** The angle. */
	private static double angle = 15.0;
	
	/** The fill with background. */
	private static boolean fillWithBackground;
	
	/** The enlarge. */
	private static boolean enlarge;
	
	/** The grid lines. */
	private static int gridLines = 1;
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The bit depth. */
	private int bitDepth;
	
	/** The can enlarge. */
	private boolean canEnlarge;
	
	/** The is enlarged. */
	private boolean isEnlarged;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The pfr. */
	private PlugInFilterRunner pfr;
	
	/** The methods. */
	private String[] methods = ImageProcessor.getInterpolationMethods();
	
	/** The interpolation method. */
	private static int interpolationMethod = ImageProcessor.BILINEAR;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (imp!=null) {
			bitDepth = imp.getBitDepth();
			Roi roi = imp.getRoi();
			Rectangle r = roi!=null?roi.getBounds():null;
			canEnlarge = r==null || (r.x==0&&r.y==0&&r.width==imp.getWidth()&&r.height==imp.getHeight());
		}
		return flags;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		if(enlarge && gd.wasOKed()) synchronized(this) {
			if (!isEnlarged) {
				enlargeCanvas();
				isEnlarged=true;
			}
		}
		if (isEnlarged) {	//enlarging may have made the ImageProcessor invalid, also for the parallel threads
			int slice = pfr.getSliceNumber();
			if (imp.getStackSize()==1)
				ip = imp.getProcessor();
			else
				ip = imp.getStack().getProcessor(slice);
		}
		ip.setInterpolationMethod(interpolationMethod);
		if (fillWithBackground) {
			Color bgc = Toolbar.getBackgroundColor();
			if (bitDepth==8)
				ip.setBackgroundValue(ip.getBestIndex(bgc));
			else if (bitDepth==24)
				ip.setBackgroundValue(bgc.getRGB());
		} else
			ip.setBackgroundValue(0);
		ip.rotate(angle);
		if (!gd.wasOKed())
			drawGridLines(gridLines);
		if (isEnlarged && imp.getStackSize()==1) {
			imp.changes = true;
			imp.updateAndDraw();
			Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		}
	}

	/**
	 * Enlarge canvas.
	 */
	void enlargeCanvas() {
		imp.unlock();
		if (imp.getStackSize()==1)
			Undo.setup(Undo.COMPOUND_FILTER, imp);
		IJ.run("Select All");
		IJ.run("Rotate...", "angle="+angle);
		Roi roi = imp.getRoi();
		Rectangle r = roi.getBounds();
		if (r.width<imp.getWidth()) r.width = imp.getWidth();
		if (r.height<imp.getHeight()) r.height = imp.getHeight();
		IJ.showStatus("Rotate: Enlarging...");
		IJ.run("Canvas Size...", "width="+r.width+" height="+r.height+" position=Center "+(fillWithBackground?"":"zero"));
		IJ.showStatus("Rotating...");
	}

	/**
	 * Draw grid lines.
	 *
	 * @param lines the lines
	 */
	void drawGridLines(int lines) {
		ImageCanvas ic = imp.getCanvas();
		if (ic==null) return;
		if (lines==0) {ic.setDisplayList(null); return;}
		GeneralPath path = new GeneralPath();
		float width = imp.getWidth();
		float height = imp.getHeight();
		float xinc = width/lines;
		float yinc = height/lines;
		float xstart = xinc/2f;
		float ystart = yinc/2f;
		for (int i=0; i<lines; i++) {
			path.moveTo(xstart+xinc*i, 0f);
			path.lineTo(xstart+xinc*i, height);
			path.moveTo(0f, ystart+yinc*i);
			path.lineTo(width, ystart+yinc*i);
		}
		ic.setDisplayList(path, null, null);
	}
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
	 */
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			if (macroOptions.indexOf(" interpolate")!=-1)
				macroOptions.replaceAll(" interpolate", " interpolation=Bilinear");
			else if (macroOptions.indexOf(" interpolation=")==-1)
				macroOptions = macroOptions+" interpolation=None";
			Macro.setOptions(macroOptions);
		}
		gd = new GenericDialog("Rotate", IJ.getInstance());
		gd.addNumericField("Angle (degrees):", angle, (int)angle==angle?1:2);
		gd.addNumericField("Grid Lines:", gridLines, 0);
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		if (bitDepth==8 || bitDepth==24)
			gd.addCheckbox("Fill with Background Color", fillWithBackground);
		if (canEnlarge)
			gd.addCheckbox("Enlarge Image to Fit Result", enlarge);
		else
			enlarge = false;
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		drawGridLines(0);
		if (gd.wasCanceled())
			return DONE;
		if (!enlarge)
			flags |= KEEP_PREVIEW;		// standard filter without enlarge
		else if (imp.getStackSize()==1)
			flags |= NO_CHANGES;			// undoable as a "compound filter"
		return IJ.setupDialog(imp, flags);
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		angle = gd.getNextNumber();
		//only check for invalid input to "angle", don't care about gridLines
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) IJ.error("Angle is invalid.");
			return false;
		}
		gridLines = (int)gd.getNextNumber();
		interpolationMethod = gd.getNextChoiceIndex();
		if (bitDepth==8 || bitDepth==24)
			fillWithBackground = gd.getNextBoolean();
		if (canEnlarge)
			enlarge = gd.getNextBoolean();
		return true;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
	 */
	public void setNPasses(int nPasses) {
	}

}

