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
import ij.gui.GenericDialog;
import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/**
 * The Class GaussianBlur3D.
 */
public class GaussianBlur3D implements PlugIn {
	
	/** The zsigma. */
	private static double xsigma=2, ysigma=2, zsigma=2;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		if (imp.isComposite() && imp.getNChannels()==imp.getStackSize()) {
			IJ.error("3D Gaussian Blur", "Composite color images not supported");
			return;
		}
		if (!showDialog())
			return;
		imp.startTiming();
		blur(imp, xsigma, ysigma, zsigma);
		IJ.showTime(imp, imp.getStartTime(), "", imp.getStackSize());
	}
	
	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("3D Gaussian Blur");
		gd.addNumericField("X sigma:", xsigma, 1);
		gd.addNumericField("Y sigma:", ysigma, 1);
		gd.addNumericField("Z sigma:", zsigma, 1);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		xsigma = gd.getNextNumber();
		ysigma = gd.getNextNumber();
		zsigma = gd.getNextNumber();
		return true;
	}
	
	/**
	 * Blur.
	 *
	 * @param imp the imp
	 * @param sigmaX the sigma X
	 * @param sigmaY the sigma Y
	 * @param sigmaZ the sigma Z
	 */
	public static void blur(ImagePlus imp, double sigmaX, double sigmaY, double sigmaZ) {
		imp.deleteRoi();
		ImageStack stack = imp.getStack();
		if (sigmaX>0.0 || sigmaY>0.0) {
			GaussianBlur gb = new GaussianBlur();
			int channels = stack.getProcessor(1).getNChannels();
			gb.setNPasses(channels*imp.getStackSize());
			for (int i=1; i<=imp.getStackSize(); i++) {
				ImageProcessor ip = stack.getProcessor(i);
				double accuracy = (imp.getBitDepth()==8||imp.getBitDepth()==24)?0.002:0.0002;
				gb.blurGaussian(ip, sigmaX, sigmaY, accuracy);
			}
		}
		if (sigmaZ>0.0) {
			if (imp.isHyperStack())
				blurHyperStackZ(imp, sigmaZ);
			else
				blurZ(stack, sigmaZ);
			imp.updateAndDraw();
		}
	}

	/**
	 * Blur Z.
	 *
	 * @param stack the stack
	 * @param sigmaZ the sigma Z
	 */
	private static void blurZ(ImageStack stack, double sigmaZ) {
		GaussianBlur gb = new GaussianBlur();
		double accuracy = (stack.getBitDepth()==8||stack.getBitDepth()==24)?0.002:0.0002;
		int w=stack.getWidth(), h=stack.getHeight(), d=stack.getSize();
		float[] zpixels = null;
		FloatProcessor fp =null;
		IJ.showStatus("Z blurring");
		gb.showProgress(false);
		int channels = stack.getProcessor(1).getNChannels();
		for (int y=0; y<h; y++) {
			IJ.showProgress(y, h-1);
			for (int channel=0; channel<channels; channel++) {
				zpixels = stack.getVoxels(0, y, 0, w, 1, d, zpixels, channel);
				if (fp==null)
					fp = new FloatProcessor(w, d, zpixels);
				//if (y==h/2) new ImagePlus("before-"+h/2, fp.duplicate()).show();
				gb.blur1Direction(fp, sigmaZ, accuracy, false, 0);
				stack.setVoxels(0, y, 0, w, 1, d, zpixels, channel);
			}
		}
		IJ.showStatus("");
	}

	/**
	 * Blur hyper stack Z.
	 *
	 * @param imp the imp
	 * @param zsigma the zsigma
	 */
	private static void blurHyperStackZ(ImagePlus imp, double zsigma) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int timePoints = imp.getNFrames();
		int nVolumes = channels*timePoints;
		for (int c=1; c<=channels; c++) {
			if (slices==1) {
				ImageStack stack = getVolume(imp, c, 1);
				blurZ(stack, zsigma);
			} else {
				for (int t=1; t<=timePoints; t++) {
					ImageStack stack = getVolume(imp, c, t);
					blurZ(stack, zsigma);
					//new ImagePlus("stack-"+c+"-"+t, stack).show();
				}
			}
		}
	}

	/**
	 * Gets the volume.
	 *
	 * @param imp the imp
	 * @param c the c
	 * @param t the t
	 * @return the volume
	 */
	private static ImageStack getVolume(ImagePlus imp, int c, int t) {
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(imp.getWidth(), imp.getHeight());
		if (imp.getNSlices()==1) {
			for (t=1; t<=imp.getNFrames(); t++) {
				int n = imp.getStackIndex(c, 1, t);
				stack2.addSlice(stack1.getProcessor(n));
			}
		} else {
			for (int z=1; z<=imp.getNSlices(); z++) {
				int n = imp.getStackIndex(c, z, t);
				stack2.addSlice(stack1.getProcessor(n));
			}
		}
		return stack2;
	}

}
