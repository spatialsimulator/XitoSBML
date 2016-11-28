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
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;


// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Translate command. */
public class Translator implements ExtendedPlugInFilter, DialogListener {
	
	/** The flags. */
	private int flags = DOES_ALL|PARALLELIZE_STACKS;
	
	/** The x offset. */
	private static double xOffset = 15;
	
	/** The y offset. */
	private static double yOffset = 15;
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The pfr. */
	private PlugInFilterRunner pfr;
	
	/** The interpolation method. */
	private static int interpolationMethod = ImageProcessor.NONE;
	
	/** The methods. */
	private String[] methods = ImageProcessor.getInterpolationMethods();

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return flags;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		ip.setInterpolationMethod(interpolationMethod);
		ip.translate(xOffset, yOffset);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
	 */
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int digits = xOffset==(int)xOffset&&yOffset==(int)yOffset?1:3;
		if (IJ.isMacro())
			interpolationMethod = ImageProcessor.NONE;
		gd = new GenericDialog("Translate");
		gd.addNumericField("X offset (pixels): ", xOffset, digits, 8, "");
		gd.addNumericField("Y offset (pixels): ", yOffset, digits, 8, "");
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled())
			return DONE;
		return IJ.setupDialog(imp, flags);
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		xOffset = gd.getNextNumber();
		yOffset = gd.getNextNumber();
		interpolationMethod = gd.getNextChoiceIndex();
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) IJ.error("Offset is invalid.");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
	 */
	public void setNPasses(int nPasses) {
	}

}

