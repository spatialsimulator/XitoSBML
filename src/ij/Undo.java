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
/**Implements the Edit/Undo command.*/

package ij;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;

// TODO: Auto-generated Javadoc
/** This class consists of static methods and
	fields that implement ImageJ's Undo command. */
public class Undo {

	/** The Constant NOTHING. */
	public static final int NOTHING = 0;
	
	/** The Constant FILTER. */
	public static final int FILTER = 1;
	
	/** The Constant TYPE_CONVERSION. */
	public static final int TYPE_CONVERSION = 2;
	
	/** The Constant PASTE. */
	public static final int PASTE = 3;
	
	/** The Constant COMPOUND_FILTER. */
	public static final int COMPOUND_FILTER = 4;
	
	/** The Constant COMPOUND_FILTER_DONE. */
	public static final int COMPOUND_FILTER_DONE = 5;
	
	/** The Constant TRANSFORM. */
	public static final int TRANSFORM = 6;
	
	/** The Constant OVERLAY_ADDITION. */
	public static final int OVERLAY_ADDITION = 7;
	
	/** The Constant ROI. */
	public static final int ROI = 8;
	
	/** The Constant MACRO. */
	public static final int MACRO = 9;
	
	/** The what to undo. */
	private static int whatToUndo = NOTHING;
	
	/** The image ID. */
	private static int imageID;
	
	/** The ip copy. */
	private static ImageProcessor ipCopy = null;
	
	/** The imp copy. */
	private static ImagePlus impCopy;
	
	/** The cal copy. */
	private static Calibration calCopy;
	
	/** The roi copy. */
	private static Roi roiCopy;
	
	/** The display range max. */
	private static double displayRangeMin, displayRangeMax;
	
	/** The lut copy. */
	private static LUT lutCopy;
	
	/**
	 * Setup.
	 *
	 * @param what the what
	 * @param imp the imp
	 */
	public static void setup(int what, ImagePlus imp) {
		if (imp==null) {
			whatToUndo = NOTHING;
			reset();
			return;
		}
		//IJ.log(imp.getTitle() + ": set up undo (" + what + ")");
		if (what==FILTER && whatToUndo==COMPOUND_FILTER)
				return;
		if (what==COMPOUND_FILTER_DONE) {
			if (whatToUndo==COMPOUND_FILTER)
				whatToUndo = what;
			return;
		}
		whatToUndo = what;
		imageID = imp.getID();
		if (what==TYPE_CONVERSION) {
			ipCopy = imp.getProcessor();
			calCopy = (Calibration)imp.getCalibration().clone();
		} else if (what==TRANSFORM) {	
			if (!IJ.macroRunning())
				impCopy = new ImagePlus(imp.getTitle(), imp.getProcessor().duplicate());
		} else if (what==MACRO) {	
			impCopy = new ImagePlus(imp.getTitle(), imp.getProcessor().duplicate());
			whatToUndo = TRANSFORM;
		} else if (what==COMPOUND_FILTER) {
			ImageProcessor ip = imp.getProcessor();
			if (ip!=null)
				ipCopy = ip.duplicate();
			else
				ipCopy = null;
		} else if (what==OVERLAY_ADDITION) {
			impCopy = null;
			ipCopy = null;
		} else if (what==ROI) {
			impCopy = null;
			ipCopy = null;
			Roi roi = imp.getRoi();
			if (roi!=null) {
				roiCopy = (Roi)roi.clone();
				roiCopy.setImage(null);
			} else
				whatToUndo = NOTHING;
		} else {
			ipCopy = null;
			ImageProcessor ip = imp.getProcessor();
			//lutCopy = (LUT)ip.getLut().clone();
		}
	}
		
	/**
	 * Reset.
	 */
	public static void reset() {
		if (whatToUndo==COMPOUND_FILTER || whatToUndo==OVERLAY_ADDITION)
			return;
		whatToUndo = NOTHING;
		imageID = 0;
		ipCopy = null;
		impCopy = null;
		calCopy = null;
		roiCopy = null;
		lutCopy = null;
		//IJ.log("Undo: reset");
	}
	

	/**
	 * Undo.
	 */
	public static void undo() {
		ImagePlus imp = WindowManager.getCurrentImage();
		//IJ.log(imp.getTitle() + ": undo (" + whatToUndo + ")  "+(imageID!=imp.getID()));
		if (imp==null || imageID!=imp.getID()) {
			if (imp!=null && !IJ.macroRunning()) { // does image still have an undo buffer?
				ImageProcessor ip2 = imp.getProcessor();
				ip2.swapPixelArrays();
				imp.updateAndDraw();
			} else
				reset();
			return;
		}
		switch (whatToUndo) {
			case FILTER:
				ImageProcessor ip = imp.getProcessor();
				if (ip!=null) {
					if (!IJ.macroRunning()) {
						ip.swapPixelArrays();
						//IJ.log("undo-filter: "+displayRangeMin+" "+displayRangeMax);
						//ip.setMinAndMax(displayRangeMin,displayRangeMax);
						imp.updateAndDraw();
						return; // don't reset
					} else {
						ip.reset();
						imp.updateAndDraw();
					}
				}
				break;
			case TYPE_CONVERSION:
			case COMPOUND_FILTER:
			case COMPOUND_FILTER_DONE:
				if (ipCopy!=null) {
					if (whatToUndo==TYPE_CONVERSION && calCopy!=null)
						imp.setCalibration(calCopy);
					if (swapImages(new ImagePlus("",ipCopy), imp)) {
						imp.updateAndDraw();
						return;
					} else
						imp.setProcessor(null, ipCopy);
				}
				break;
			case TRANSFORM:
				if (impCopy!=null)
					imp.setProcessor(impCopy.getTitle(), impCopy.getProcessor());
				break;
			case PASTE:
				Roi roi = imp.getRoi();
				if (roi!=null)
					roi.abortPaste();
	    		break;
			case ROI:
				Roi roiCopy2 = roiCopy;
				setup(ROI, imp); // setup redo
				imp.setRoi(roiCopy2);
				return; //don't reset
			case OVERLAY_ADDITION:
				Overlay overlay = imp.getOverlay();
				if (overlay==null) 
					{IJ.beep(); return;}
				int size = overlay.size();
				if (size>0) {
					overlay.remove(size-1);
					imp.draw();
				} else {
					IJ.beep();
					return;
				}
	    		return; //don't reset
    	}
    	reset();
	}
	
	/**
	 * Swap images.
	 *
	 * @param imp1 the imp 1
	 * @param imp2 the imp 2
	 * @return true, if successful
	 */
	static boolean swapImages(ImagePlus imp1, ImagePlus imp2) {
		if (imp1.getWidth()!=imp2.getWidth() || imp1.getHeight()!=imp2.getHeight()
		|| imp1.getBitDepth()!=imp2.getBitDepth() || IJ.macroRunning())
			return false;
		ImageProcessor ip1 = imp1.getProcessor();
		ImageProcessor ip2 = imp2.getProcessor();
		double min1 = ip1.getMin();
		double max1 = ip1.getMax();
		double min2 = ip2.getMin();
		double max2 = ip2.getMax();
		ip2.setSnapshotPixels(ip1.getPixels());
		ip2.swapPixelArrays();
		ip1.setPixels(ip2.getSnapshotPixels());
		ip2.setSnapshotPixels(null);
		ip1.setMinAndMax(min2, max2);
		ip2.setMinAndMax(min1, max1);
		return true;
	}

}
