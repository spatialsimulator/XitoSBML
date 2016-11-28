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
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;

// TODO: Auto-generated Javadoc
/** This plugin implements the Invert, Smooth, Sharpen, Find Edges, 
	and Add Noise commands. */
public class Filters implements PlugInFilter {
	
	/** The sd. */
	private static double sd = Prefs.getDouble(Prefs.NOISE_SD, 25.0);
	
	/** The arg. */
	private String arg;
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The slice. */
	private int slice;
	
	/** The canceled. */
	private boolean canceled;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (imp!=null) {
			Roi roi = imp.getRoi();
			if (roi!=null && !roi.isArea())
				imp.deleteRoi(); // ignore any line selection
		}
		int flags = IJ.setupDialog(imp, DOES_ALL-DOES_8C+SUPPORTS_MASKING);
		if ((flags&PlugInFilter.DOES_STACKS)!=0 && imp.getType()==ImagePlus.GRAY16 && imp.getStackSize()>1 && arg.equals("invert")) {
				invert16BitStack(imp);
				return DONE;
		}
		return flags;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
	
		if (arg.equals("invert")) {
	 		ip.invert();
	 		return;
	 	}
	 	
		if (arg.equals("smooth")) {
			ip.setSnapshotCopyMode(true);
	 		ip.smooth();
			ip.setSnapshotCopyMode(false);
	 		return;
	 	}
	 	
		if (arg.equals("sharpen")) {
			ip.setSnapshotCopyMode(true);
	 		ip.sharpen();
			ip.setSnapshotCopyMode(false);
	 		return;
	 	}
	 	
		if (arg.equals("edge")) {
			ip.setSnapshotCopyMode(true);
			ip.findEdges();
			ip.setSnapshotCopyMode(false);
	 		return;
		}
						
	 	if (arg.equals("add")) {
	 		ip.noise(25.0);
	 		return;
	 	}
	 	
	 	if (arg.equals("noise")) {
	 		if (canceled)
	 			return;
	 		slice++;
	 		if (slice==1) {
				GenericDialog gd = new GenericDialog("Gaussian Noise");
				gd.addNumericField("Standard Deviation:", sd, 2);
				gd.showDialog();
				if (gd.wasCanceled()) {
					canceled = true;
					return;
				}
				sd = gd.getNextNumber();
			}
	 		ip.noise(sd);
	 		IJ.register(Filters.class);
	 		return;
	 	}
        	 	
	}
	
	/**
	 * Invert 16 bit stack.
	 *
	 * @param imp the imp
	 */
	void invert16BitStack(ImagePlus imp) {
		imp.deleteRoi();
		imp.getCalibration().disableDensityCalibration();
		ImageStatistics stats = new StackStatistics(imp);
		ImageStack stack = imp.getStack();
		int nslices = stack.getSize();
		int min=(int)stats.min, range=(int)(stats.max-stats.min);
		int n = imp.getWidth()*imp.getHeight();
		for (int slice=1; slice<=nslices; slice++) {
			ImageProcessor ip = stack.getProcessor(slice);
			short[] pixels = (short[])ip.getPixels();
			for (int i=0; i<n; i++) {
				int before = pixels[i]&0xffff;
				pixels[i] = (short)(range-((pixels[i]&0xffff)-min));
			}
		}
		imp.setStack(null, stack);
		imp.setDisplayRange(0, range);
		imp.updateAndDraw();
	}
	
	/**
	 *  Returns the default standard deviation used by Process/Noise/Add Specified Noise.
	 *
	 * @return the sd
	 */
	public static double getSD() {
		return sd;
	}
	
}
