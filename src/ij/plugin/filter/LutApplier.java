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
import ij.ImageStack;
import ij.Undo;
import ij.plugin.frame.ContrastAdjuster;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Lookup Tables/Apply LUT command. */
public class LutApplier implements PlugInFilter {
	
	/** The imp. */
	ImagePlus imp;
	
	/** The max. */
	int min, max;
	
	/** The canceled. */
	boolean canceled;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		int baseOptions = DOES_8G+DOES_8C+DOES_RGB+SUPPORTS_MASKING;
		if (imp!=null && imp.getType()==ImagePlus.COLOR_RGB)
			return baseOptions+NO_UNDO;
		else
			return baseOptions;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		apply(imp, ip);
	}
	
	/**
	 * Apply.
	 *
	 * @param imp the imp
	 * @param ip the ip
	 */
	void apply(ImagePlus imp, ImageProcessor ip) {
        if (ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
            imp.unlock();
			IJ.runPlugIn("ij.plugin.Thresholder", "skip");
            return;
        }
		min = (int)ip.getMin();
		max = (int)ip.getMax();
		if (min==0 && max==255) {
				IJ.error("Apply LUT", "The display range must first be updated\n"
                +"using Image>Adjust>Brightness/Contrast\n"
                +"or threshold levels defined using\n"
                +"Image>Adjust>Threshold.");
				return;
		}
		if (imp.getType()==ImagePlus.COLOR_RGB) {
			if (imp.getStackSize()>1)
				applyRGBStack(imp);
			else {
				ip.reset();
				Undo.setup(Undo.TRANSFORM, imp);
				ip.setMinAndMax(min, max);
				//ip.snapshot();
			}
			if (canceled) ip.reset();
			resetContrastAdjuster();
			return;
		}
		ip.resetMinAndMax();
		int[] table = new int[256];
		for (int i=0; i<256; i++) {
			if (i<=min)
				table[i] = 0;
			else if (i>=max)
				table[i] = 255;
			else
				table[i] = (int)(((double)(i-min)/(max-min))*255);
		}
		if (imp.getStackSize()>1) {
			ImageStack stack = imp.getStack();
			
			int flags = IJ.setupDialog(imp, 0);
			if (flags==PlugInFilter.DONE)
				{ip.setMinAndMax(min, max); return;}
			if (flags==PlugInFilter.DOES_STACKS) {
				new StackProcessor(stack, ip).applyTable(table);
				Undo.reset();
			} else
				ip.applyTable(table);
		} else
			ip.applyTable(table);
		resetContrastAdjuster();
	}
	
	/**
	 * Reset contrast adjuster.
	 */
	void resetContrastAdjuster() {
		ContrastAdjuster.update();
	}

	/**
	 * Apply RGB stack.
	 *
	 * @param imp the imp
	 */
	void applyRGBStack(ImagePlus imp) {
		int current = imp.getCurrentSlice();
		int n = imp.getStackSize();
		if (!IJ.showMessageWithCancel("Update Entire Stack?",
		"Apply brightness and contrast settings\n"+
		"to all "+n+" slices in the stack?\n \n"+
		"NOTE: There is no Undo for this operation.")) {
			canceled = true;
			return;
		}
		for (int i=1; i<=n; i++) {
			if (i!=current) {
				imp.setSlice(i);
				ImageProcessor ip = imp.getProcessor();
				ip.setMinAndMax(min, max);
				IJ.showProgress((double)i/n);
			}
		}
		imp.setSlice(current);
	}
	
}
