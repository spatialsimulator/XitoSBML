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
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.frame.ContrastAdjuster;
import ij.plugin.frame.Recorder;
import ij.process.LUT;

import java.awt.AWTEvent;
import java.awt.Color;

// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Appearance command. */
public class AppearanceOptions implements PlugIn, DialogListener {
	
	/** The interpolate. */
	private boolean interpolate = Prefs.interpolateScaledImages;
	
	/** The open 100. */
	private boolean open100 = Prefs.open100Percent;
	
	/** The black. */
	private boolean black = Prefs.blackCanvas;
	
	/** The no border. */
	private boolean noBorder = Prefs.noBorder;
	
	/** The inverting. */
	private boolean inverting = Prefs.useInvertingLut;
	
	/** The antialiased. */
	private boolean antialiased = Prefs.antialiasedTools;
	
	/** The range index. */
	private int rangeIndex = ContrastAdjuster.get16bitRangeIndex();
	
	/** The luts. */
	private LUT[] luts = getLuts();
	
	/** The set menu size. */
	private int setMenuSize = Menus.getFontSize();
	
	/** The repainted. */
	private boolean redrawn, repainted;

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public void run(String arg) {
 		showDialog();
 	}
		
	/**
	 * Show dialog.
	 */
	void showDialog() {
		String[] ranges = ContrastAdjuster.sixteenBitRanges;
		GenericDialog gd = new GenericDialog("Appearance", IJ.getInstance());
		gd.addCheckbox("Interpolate zoomed images", Prefs.interpolateScaledImages);
		gd.addCheckbox("Open images at 100%", Prefs.open100Percent);
		gd.addCheckbox("Black canvas", Prefs.blackCanvas);
		gd.addCheckbox("No image border", Prefs.noBorder);
		gd.addCheckbox("Use inverting lookup table", Prefs.useInvertingLut);
		gd.addCheckbox("Antialiased tool icons", Prefs.antialiasedTools);
		gd.addCheckbox("Auto contrast stacks (or use shift key)", Prefs.autoContrast);
		gd.addChoice("16-bit range:", ranges, ranges[rangeIndex]);
		gd.addNumericField("Menu font size:", Menus.getFontSize(), 0, 3, "points");
        gd.addHelp(IJ.URL+"/docs/menus/edit.html#appearance");
        gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) {
			if (antialiased!=Prefs.antialiasedTools)
				Toolbar.getInstance().repaint();
			Prefs.interpolateScaledImages = interpolate;
			Prefs.open100Percent = open100;
			Prefs.blackCanvas = black;
			Prefs.noBorder = noBorder;
			Prefs.useInvertingLut = inverting;
			Prefs.antialiasedTools = antialiased;
			if (redrawn) draw();
			if (repainted) repaintWindow();
			Prefs.open100Percent = open100;
			if (rangeIndex!=ContrastAdjuster.get16bitRangeIndex()) {
				ContrastAdjuster.set16bitRange(rangeIndex);
				ImagePlus imp = WindowManager.getCurrentImage();
				Calibration cal = imp!=null?imp.getCalibration():null;
				if (imp!=null && imp.getType()==ImagePlus.GRAY16 && !cal.isSigned16Bit()) {
					imp.resetDisplayRange();
					if (rangeIndex==0 && imp.isComposite() && luts!=null)
						((CompositeImage)imp).setLuts(luts);
					imp.updateAndDraw();
				}
			}
			return;
		}
		if (setMenuSize!=Menus.getFontSize() && !IJ.isMacintosh()) {
			Menus.setFontSize(setMenuSize);
			IJ.showMessage("Appearance", "Restart ImageJ to use the new font size");
		}
		if (Prefs.useInvertingLut) {
			IJ.showMessage("Appearance",
				"The \"Use inverting lookup table\" option is set. Newly opened\n"+
				"8-bit images will use an inverting LUT (white=0, black=255).");
		}
		int range = ImagePlus.getDefault16bitRange();
		if (range>0 && Recorder.record) {
			if (Recorder.scriptMode())
				Recorder.recordCall("ImagePlus.setDefault16bitRange("+range+");");
			else
				Recorder.recordString("call(\"ij.ImagePlus.setDefault16bitRange\", "+range+");\n");
		}

	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(100);
		boolean interpolate = gd.getNextBoolean();
		Prefs.open100Percent = gd.getNextBoolean();
		boolean blackCanvas = gd.getNextBoolean();
		boolean noBorder = gd.getNextBoolean();
		Prefs.useInvertingLut = gd.getNextBoolean();
		boolean antialiasedTools = gd.getNextBoolean();
		boolean toolbarChange = antialiasedTools!=Prefs.antialiasedTools;
		Prefs.antialiasedTools = antialiasedTools;
		Prefs.autoContrast = gd.getNextBoolean();
		if (toolbarChange) Toolbar.getInstance().repaint();
		setMenuSize = (int)gd.getNextNumber();
		if (interpolate!=Prefs.interpolateScaledImages) {
			Prefs.interpolateScaledImages = interpolate;
			draw();
		}
		if (blackCanvas!=Prefs.blackCanvas) {
			Prefs.blackCanvas = blackCanvas;
			repaintWindow();
		}
		if (noBorder!=Prefs.noBorder) {
			Prefs.noBorder = noBorder;
			repaintWindow();
		}
		int rangeIndex2 = gd.getNextChoiceIndex();
		int range1 = ImagePlus.getDefault16bitRange();
		int range2 = ContrastAdjuster.set16bitRange(rangeIndex2);
		ImagePlus imp = WindowManager.getCurrentImage();
		Calibration cal = imp!=null?imp.getCalibration():null;
		if (range1!=range2 && imp!=null && imp.getType()==ImagePlus.GRAY16 && !cal.isSigned16Bit()) {
			imp.resetDisplayRange();
			if (rangeIndex2==0 && imp.isComposite() && luts!=null)
				((CompositeImage)imp).setLuts(luts);
			imp.updateAndDraw();
		}
		return true;
    }
    
    /**
     * Gets the luts.
     *
     * @return the luts
     */
    private LUT[] getLuts() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null || imp.getBitDepth()!=16 || !imp.isComposite())
			return null;
		return ((CompositeImage)imp).getLuts();
    }
    
    /**
     * Draw.
     */
    void draw() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.draw();
		redrawn = true;
    }

	/**
	 * Repaint window.
	 */
	void repaintWindow() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			ImageWindow win = imp.getWindow();
			if (win!=null) {
				if (Prefs.blackCanvas) {
					win.setForeground(Color.white);
					win.setBackground(Color.black);
				} else {
					win.setForeground(Color.black);
					win.setBackground(Color.white);
				}
				imp.repaintWindow();
			}
		}
		repainted = true;
	}
		
}
