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
package ij.process;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.Prefs;
import ij.measure.Calibration;

import java.awt.image.ColorModel;

// TODO: Auto-generated Javadoc
/** This class converts an ImagePlus object to a different type. */
public class ImageConverter {
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The type. */
	private int type;
	
	/** The do scaling. */
	//private static boolean doScaling = Prefs.getBoolean(Prefs.SCALE_CONVERSIONS,true);
	private static boolean doScaling = true;

	/**
	 *  Constructs an ImageConverter based on an ImagePlus object.
	 *
	 * @param imp the imp
	 */
	public ImageConverter(ImagePlus imp) {
		this.imp = imp;
		type = imp.getType();
	}

	/** Converts this ImagePlus to 8-bit grayscale. */
	public synchronized void convertToGray8() {
		if (imp.getStackSize()>1) {
			new StackConverter(imp).convertToGray8();
			return;
		}
		ImageProcessor ip = imp.getProcessor();
		if (type==ImagePlus.GRAY16 || type==ImagePlus.GRAY32) {
			imp.setProcessor(null, ip.convertToByte(doScaling));
			imp.setCalibration(imp.getCalibration()); //update calibration
		} else if (type==ImagePlus.COLOR_RGB)
	    	imp.setProcessor(null, ip.convertToByte(doScaling));
		else if (ip.isPseudoColorLut()) {
			boolean invertedLut = ip.isInvertedLut();
			ip.setColorModel(LookUpTable.createGrayscaleColorModel(invertedLut));
	    	imp.updateAndDraw();
		} else {
			ip = new ColorProcessor(imp.getImage());
	    	imp.setProcessor(null, ip.convertToByte(doScaling));
	    }
	    ImageProcessor ip2 = imp.getProcessor();
		if (Prefs.useInvertingLut && ip2 instanceof ByteProcessor && !ip2.isInvertedLut()&& !ip2.isColorLut()) {
			ip2.invertLut();
			ip2.invert();
		}
	}

	/** Converts this ImagePlus to 16-bit grayscale. */
	public void convertToGray16() {
		if (type==ImagePlus.GRAY16)
			return;
		if (!(type==ImagePlus.GRAY8||type==ImagePlus.GRAY32||type==ImagePlus.COLOR_RGB))
			throw new IllegalArgumentException("Unsupported conversion");
		if (imp.getStackSize()>1) {
			new StackConverter(imp).convertToGray16();
			return;
		}
		ImageProcessor ip = imp.getProcessor();
		imp.trimProcessor();
		imp.setProcessor(null, ip.convertToShort(doScaling));
		imp.setCalibration(imp.getCalibration()); //update calibration
	}

	/** Converts this ImagePlus to 32-bit grayscale. */
	public void convertToGray32() {
		if (type==ImagePlus.GRAY32)
			return;
		if (!(type==ImagePlus.GRAY8||type==ImagePlus.GRAY16||type==ImagePlus.COLOR_RGB))
			throw new IllegalArgumentException("Unsupported conversion");
		if (imp.getStackSize()>1) {
			new StackConverter(imp).convertToGray32();
			return;
		}
		ImageProcessor ip = imp.getProcessor();
		imp.trimProcessor();
		Calibration cal = imp.getCalibration();
		imp.setProcessor(null, ip.convertToFloat());
		imp.setCalibration(cal); //update calibration
	}

	/** Converts this ImagePlus to RGB. */
	public void convertToRGB() {
		if (imp.getStackSize()>1) {
			new StackConverter(imp).convertToRGB();
			return;
		}
		ImageProcessor ip = imp.getProcessor();
		imp.setProcessor(null, ip.convertToRGB());
		imp.setCalibration(imp.getCalibration()); //update calibration
	}
	
	/** Converts an RGB image to an RGB (red, green and blue) stack. */
	public void convertToRGBStack() {
		if (type!=ImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("Image must be RGB");

		//convert to RGB Stack
		ColorProcessor cp;
		if (imp.getType()==ImagePlus.COLOR_RGB)
			cp = (ColorProcessor)imp.getProcessor();
		else
			cp = new ColorProcessor(imp.getImage());
		int width = imp.getWidth();
		int height = imp.getHeight();
		byte[] R = new byte[width*height];
		byte[] G = new byte[width*height];
		byte[] B = new byte[width*height];
		cp.getRGB(R, G, B);
		imp.trimProcessor();
		
		// Create stack and select Red channel
		ColorModel cm = LookUpTable.createGrayscaleColorModel(false);
		ImageStack stack = new ImageStack(width, height, cm);
		stack.addSlice("Red", R);
		stack.addSlice("Green", G);
		stack.addSlice("Blue", B);
		imp.setStack(null, stack);
		imp.setDimensions(3, 1, 1);
		if (imp.isComposite())
			((CompositeImage)imp).setMode(IJ.GRAYSCALE);
	}

	/** Converts an RGB image to a HSB (hue, saturation and brightness) stack. */
	public void convertToHSB() {
		if (type!=ImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("Image must be RGB");

		//convert to hue, saturation and brightness
		//IJ.showProgress(0.1);
		ColorProcessor cp;
		if (imp.getType()==ImagePlus.COLOR_RGB)
			cp = (ColorProcessor)imp.getProcessor();
		else
			cp = new ColorProcessor(imp.getImage());
		ImageStack stack = cp.getHSBStack();
		imp.trimProcessor();
		imp.setStack(null, stack);
		imp.setDimensions(3, 1, 1);
		//IJ.showProgress(1.0);
	}
	
	/** Converts a 2 or 3 slice 8-bit stack to RGB. */
	public void convertRGBStackToRGB() {
		int stackSize = imp.getStackSize();
		if (stackSize<2 || stackSize>3 || type!=ImagePlus.GRAY8)
			throw new IllegalArgumentException("2 or 3 slice 8-bit stack required");
		int width = imp.getWidth();
		int height = imp.getHeight();
		ImageStack stack = imp.getStack();
		byte[] R = (byte[])stack.getPixels(1);
		byte[] G = (byte[])stack.getPixels(2);
		byte[] B;
		if (stackSize>2)
			B = (byte[])stack.getPixels(3);
		else
			B = new byte[width*height];
		imp.trimProcessor();
		ColorProcessor cp = new ColorProcessor(width, height);
		cp.setRGB(R, G, B);
		if (imp.isInvertedLut())
			cp.invert();
		imp.setImage(cp.createImage());
		imp.killStack();
		if (IJ.isLinux())
			imp.setTitle(imp.getTitle());
	}

	/** Converts a 3-slice (hue, saturation, brightness) 8-bit stack to RGB. */
	public void convertHSBToRGB() {
		if (imp.getStackSize()!=3)
			throw new IllegalArgumentException("3-slice 8-bit stack required");
		ImageStack stack = imp.getStack();
		byte[] H = (byte[])stack.getPixels(1);
		byte[] S = (byte[])stack.getPixels(2);
		byte[] B = (byte[])stack.getPixels(3);
		int width = imp.getWidth();
		int height = imp.getHeight();
		imp.trimProcessor();
		ColorProcessor cp = new ColorProcessor(width, height);
		cp.setHSB(H, S, B);
		imp.setImage(cp.createImage());
		imp.killStack();
		if (IJ.isLinux())
			imp.setTitle(imp.getTitle());
	}
	
	/**
	 *  Converts an RGB image to 8-bits indexed color. 'nColors' must
	 * 		be greater than 1 and less than or equal to 256.
	 *
	 * @param nColors the n colors
	 */
	public void convertRGBtoIndexedColor(int nColors) {
		if (type!=ImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("Image must be RGB");
		if (nColors<2) nColors = 2;
		if (nColors>256) nColors = 256;
		
		// get RGB pixels
		IJ.showProgress(0.1);
		IJ.showStatus("Grabbing pixels");
		int width = imp.getWidth();
		int height = imp.getHeight();
		ImageProcessor ip = imp.getProcessor();
	 	ip.snapshot();
		int[] pixels = (int[])ip.getPixels();
		imp.trimProcessor();
		
		// convert to 8-bits
		long start = System.currentTimeMillis();
		MedianCut mc = new MedianCut(pixels, width, height);
		ImageProcessor ip2 = mc.convertToByte(nColors);
	    imp.setProcessor(null, ip2);
	}
	
	/**
	 *  Set true to scale to 0-255 when converting short to byte or float
	 * 		to byte and to 0-65535 when converting float to short.
	 *
	 * @param scaleConversions the new do scaling
	 */
	public static void setDoScaling(boolean scaleConversions) {
		doScaling = scaleConversions;
		IJ.register(ImageConverter.class); 
	}

	/**
	 *  Returns true if scaling is enabled.
	 *
	 * @return the do scaling
	 */
	public static boolean getDoScaling() {
		return doScaling;
	}
}
