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
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.process.StackProcessor;
import ij.util.ThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class Filters3D.
 */
/*
 * This plugin implements most of the 3D filters in the Process/Filters submenu.
 * @author Thomas Boudier
 */
public class Filters3D implements PlugIn {
    
    /** The Constant MAXLOCAL. */
    public final static int MEAN=10, MEDIAN=11, MIN=12, MAX=13, VAR=14, MAXLOCAL=15;
	
	/** The zradius. */
	private static float xradius = 2, yradius = 2, zradius = 2;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		String name = null;
		int filter = 0;
		if (arg.equals("mean")) {
			name = "3D Mean";
			filter = MEAN;
		} else if (arg.equals("median")) {
			name = "3D Median";
			filter = MEDIAN;
		} else if (arg.equals("min")) {
			name = "3D Minimum";
			filter = MIN;
		} else if (arg.equals("max")) {
			name = "3D Maximum";
			filter = MAX;
		} else if (arg.equals("var")) {
			name = "3D Variance";
			filter = VAR;
		} else
			return;
		ImagePlus imp = IJ.getImage();
		if (imp.isComposite() && imp.getNChannels()==imp.getStackSize()) {
			IJ.error(name, "Composite color images not supported");
			return;
		}
		if (!showDialog(name))
			return;
		imp.startTiming();
		run(imp, filter, xradius, yradius, zradius);
		IJ.showTime(imp, imp.getStartTime(), "", imp.getStackSize());
	}

	/**
	 * Show dialog.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	private boolean showDialog(String name) {
		GenericDialog gd = new GenericDialog(name);
		gd.addNumericField("X radius:", xradius, 1);
		gd.addNumericField("Y radius:", yradius, 1);
		gd.addNumericField("Z radius:", zradius, 1);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		xradius = (float) gd.getNextNumber();
		yradius = (float) gd.getNextNumber();
		zradius = (float) gd.getNextNumber();
		return true;
	}

	/**
	 * Run.
	 *
	 * @param imp the imp
	 * @param filter the filter
	 * @param radX the rad X
	 * @param radY the rad Y
	 * @param radZ the rad Z
	 */
	private void run(ImagePlus imp, int filter, float radX, float radY, float radZ) {
		if (imp.isHyperStack()) {
			filterHyperstack(imp, filter, radX, radY, radZ);
			return;
		}
		ImageStack res = filter(imp.getStack(), filter, radX, radY, radZ);
		imp.setStack(res);
	}
	
	/**
	 * Filter.
	 *
	 * @param stackorig the stackorig
	 * @param filter the filter
	 * @param vx the vx
	 * @param vy the vy
	 * @param vz the vz
	 * @return the image stack
	 */
	public static ImageStack filter(ImageStack stackorig, int filter, float vx, float vy, float vz) {
	
		if (stackorig.getBitDepth()==24)
			return filterRGB(stackorig, filter, vx, vy, vz);

		// get stack info
		final ImageStack stack = stackorig;
		final float voisx = vx;
		final float voisy = vy;
		final float voisz = vz;
		final int width= stack.getWidth();
		final int height= stack.getHeight();
		final int depth= stack.getSize();
		ImageStack res = null;
		
		if ((filter==MEAN) || (filter==MEDIAN) || (filter==MIN) || (filter==MAX) || (filter==VAR)) {
			if (filter==VAR)
				res = ImageStack.create(width, height, depth, 32);
			else
				res = ImageStack.create(width, height, depth, stackorig.getBitDepth());
			IJ.showStatus("3D filtering...");
			// PARALLEL 
			final ImageStack out = res;
			final AtomicInteger ai = new AtomicInteger(0);
			final int n_cpus = Prefs.getThreads();

			final int f = filter;
			final int dec = (int) Math.ceil((double) stack.getSize() / (double) n_cpus);
			Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
			for (int ithread = 0; ithread < threads.length; ithread++) {
				threads[ithread] = new Thread() {
					public void run() {
						StackProcessor processor = new StackProcessor(stack);
						for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
							processor.filter3D(out, voisx, voisy, voisz, dec * k, dec * (k + 1), f);
						}
					}
				};
			}
			ThreadUtil.startAndJoin(threads);
		}
		return res;
	}
	
	/**
	 * Filter hyperstack.
	 *
	 * @param imp the imp
	 * @param filter the filter
	 * @param vx the vx
	 * @param vy the vy
	 * @param vz the vz
	 */
	private static void filterHyperstack(ImagePlus imp, int filter, float vx, float vy, float vz) {
		if (imp.getNDimensions()>4) {
			IJ.error("5D hyperstacks are currently not supported");
			return;
		}
		if (imp.getNChannels()==1) {
			ImageStack stack = filter(imp.getStack(), filter, vx, vy, vz);
			imp.setStack(stack);
			return;
		}
        ImagePlus[] channels = ChannelSplitter.split(imp);
        int n = channels.length;
        for (int i=0; i<n; i++) {
			ImageStack stack = filter(channels[i].getStack(), filter, vx, vy, vz);
			channels[i].setStack(stack);
		}
		ImagePlus imp2 = RGBStackMerge.mergeChannels(channels, false);
		imp.setImage(imp2);
		//if (imp.isComposite()) {
		//	CompositeImage ci = (CompositeImage)imp;
		//	ci.reset();
		//	ci.resetDisplayRanges();
		//	ci.updateAllChannelsAndDraw();
		//}
		imp.setC(1);
	}

	/**
	 * Filter RGB.
	 *
	 * @param rgb_in the rgb in
	 * @param filter the filter
	 * @param vx the vx
	 * @param vy the vy
	 * @param vz the vz
	 * @return the image stack
	 */
	private static ImageStack filterRGB(ImageStack rgb_in, int filter, float vx, float vy, float vz) {
        ImageStack[] channels = ChannelSplitter.splitRGB(rgb_in, false);
		ImageStack red = filter(channels[0], filter, vx, vy, vz);
		ImageStack green = filter(channels[1], filter, vx, vy, vz);
		ImageStack blue = filter(channels[2], filter, vx, vy, vz);
        return RGBStackMerge.mergeStacks(red, green, blue, false);
	}

}
