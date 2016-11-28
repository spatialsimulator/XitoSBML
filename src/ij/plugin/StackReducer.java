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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Stacks/Tools/Reduce command. */
public class StackReducer implements PlugIn {
	
	/** The imp. */
	ImagePlus imp;
	
	/** The factor. */
	private static int factor = 2;
	
	/** The reduce slices. */
	private boolean hyperstack, reduceSlices;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		ImageStack stack = imp.getStack();
		int size = stack.getSize();
		if (size==1 || (imp.getNChannels()==size&&imp.isComposite()))
			{IJ.error("Stack or hyperstack required"); return;}
		if (!showDialog(stack))
			return;
		if (hyperstack)
			reduceHyperstack(imp, factor, reduceSlices);
		else
			reduceStack(imp, factor);
	}

	/**
	 * Show dialog.
	 *
	 * @param stack the stack
	 * @return true, if successful
	 */
	public boolean showDialog(ImageStack stack) {
		hyperstack = imp.isHyperStack();
		boolean showCheckbox = false;
		if (hyperstack && imp.getNSlices()>1 && imp.getNFrames()>1)
			showCheckbox = true;
		else if (hyperstack && imp.getNSlices()>1)
			reduceSlices = true;
		int n = stack.getSize();
		GenericDialog gd = new GenericDialog("Reduce Size");
		gd.addNumericField("Reduction Factor:", factor, 0);
		if (showCheckbox)
			gd.addCheckbox("Reduce in Z-Dimension", false);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		factor = (int) gd.getNextNumber();
		if (showCheckbox)
			reduceSlices = gd.getNextBoolean();
		return true;
	}
	
	/**
	 * Reduce stack.
	 *
	 * @param imp the imp
	 * @param factor the factor
	 */
	public void reduceStack(ImagePlus imp, int factor) {
		ImageStack stack = imp.getStack();
		boolean virtual = stack.isVirtual();
		int n = stack.getSize();
		ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
		for (int i=1; i<=n; i+=factor) {
			if (virtual) IJ.showProgress(i, n);
			stack2.addSlice(stack.getSliceLabel(i), stack.getProcessor(i));
		}
		imp.setStack(null, stack2);
		if (virtual) {
			IJ.showProgress(1.0);
			imp.setTitle(imp.getTitle());
		}
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) cal.pixelDepth *= factor;
	}
	
	/**
	 * Reduce hyperstack.
	 *
	 * @param imp the imp
	 * @param factor the factor
	 * @param reduceSlices the reduce slices
	 */
	public void reduceHyperstack(ImagePlus imp, int factor, boolean reduceSlices) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int zfactor = reduceSlices?factor:1;
		int tfactor = reduceSlices?1:factor;
		ImageStack stack = imp.getStack();
		ImageStack stack2 = new ImageStack(imp.getWidth(), imp.getHeight());
		boolean virtual = stack.isVirtual();
		int slices2 = slices/zfactor + ((slices%zfactor)!=0?1:0);
		int frames2 = frames/tfactor + ((frames%tfactor)!=0?1:0);
		int n = channels*slices2*frames2;
		int count = 1;
		for (int t=1; t<=frames; t+=tfactor) {
			for (int z=1; z<=slices; z+=zfactor) {
				for (int c=1; c<=channels; c++) {
					int i = imp.getStackIndex(c, z, t);
					IJ.showProgress(i, n);
					ImageProcessor ip = stack.getProcessor(imp.getStackIndex(c, z, t));
					//IJ.log(count++ +"  "+i+" "+c+" "+z+" "+t);
					stack2.addSlice(stack.getSliceLabel(i), ip);
				}
			}
		}
		imp.setStack(stack2, channels, slices2, frames2);
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) cal.pixelDepth *= zfactor;
		if (virtual) imp.setTitle(imp.getTitle());
		IJ.showProgress(1.0);
	}

}
