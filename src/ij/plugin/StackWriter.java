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
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.plugin.frame.Recorder;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/** This plugin, which saves the images in a stack as separate files, 
	implements the File/Save As/Image Sequence command. */
public class StackWriter implements PlugIn {

	/** The choices. */
	//private static String defaultDirectory = null;
	private static String[] choices = {"BMP",  "FITS", "GIF", "JPEG", "PGM", "PNG", "Raw", "Text", "TIFF",  "ZIP"};
	
	/** The static file type. */
	private static String staticFileType = "TIFF";
	
	/** The file type. */
	private String fileType = "TIFF";
	
	/** The ndigits. */
	private int ndigits = 4;
	
	/** The use labels. */
	private boolean useLabels;
	
	/** The first time. */
	private boolean firstTime = true;
	
	/** The start at. */
	private int startAt;
	
	/** The hyperstack. */
	private boolean hyperstack;
	
	/** The dim. */
	private int[] dim;
	//private static boolean startAtZero;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null || (imp!=null && imp.getStackSize()<2&&!IJ.isMacro())) {
			IJ.error("Stack Writer", "This command requires a stack.");
			return;
		}
		int stackSize = imp.getStackSize();
		String name = imp.getTitle();
		int dotIndex = name.lastIndexOf(".");
		if (dotIndex>=0)
			name = name.substring(0, dotIndex);
		hyperstack = imp.isHyperStack();
		LUT[] luts = null;
		int lutIndex = 0;
		int nChannels = imp.getNChannels();
		if (hyperstack) {
			dim = imp.getDimensions();
			if (imp.isComposite())
				luts = ((CompositeImage)imp).getLuts();
			if (firstTime && ndigits==4) {
				ndigits = 3;
				firstTime = false;
			}
		}
		
		GenericDialog gd = new GenericDialog("Save Image Sequence");
		if (!IJ.isMacro())
			fileType = staticFileType;
		gd.addChoice("Format:", choices, fileType);
		gd.addStringField("Name:", name, 12);
		if (!hyperstack)
			gd.addNumericField("Start At:", startAt, 0);
		gd.addNumericField("Digits (1-8):", ndigits, 0);
		if (!hyperstack)
			gd.addCheckbox("Use slice labels as file names", useLabels);
		gd.setSmartRecording(true);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		fileType = gd.getNextChoice();
		if (!IJ.isMacro())
			staticFileType = fileType;
		name = gd.getNextString();
		if (!hyperstack)
			startAt = (int)gd.getNextNumber();
		if (startAt<0) startAt = 0;
		ndigits = (int)gd.getNextNumber();
		if (!hyperstack)
			useLabels = gd.getNextBoolean();
		else
			useLabels = false;
		int number = 0;
		if (ndigits<1) ndigits = 1;
		if (ndigits>8) ndigits = 8;
		int maxImages = (int)Math.pow(10,ndigits);
		if (stackSize>maxImages && !useLabels && !hyperstack) {
			IJ.error("Stack Writer", "More than " + ndigits
				+" digits are required to generate \nunique file names for "+stackSize+" images.");
			return;			
		}
		String format = fileType.toLowerCase(Locale.US);
		if (format.equals("gif") && !FileSaver.okForGif(imp))
			return;
		else if (format.equals("fits") && !FileSaver.okForFits(imp))
			return;
			
		if (format.equals("text"))
			format = "text image";
		String extension = "." + format;
		if (format.equals("tiff"))
			extension = ".tif";
		else if (format.equals("text image"))
			extension = ".txt";
			
		String title = "Save Image Sequence";
		String macroOptions = Macro.getOptions();
		String directory = null;
		if (macroOptions!=null) {
			directory = Macro.getValue(macroOptions, title, null);
			if (directory!=null) {
				File f = new File(directory);
				boolean exists = f.exists();
				if (directory.indexOf(".")==-1 && !exists) {
					// Is 'directory' a macro variable?
					if (directory.startsWith("&")) directory=directory.substring(1);
					Interpreter interp = Interpreter.getInstance();
					String directory2 = interp!=null?interp.getStringVariable(directory):null;
					if (directory2!=null) directory = directory2;
				}
				if (!f.isDirectory() && (exists||directory.lastIndexOf(".")>directory.length()-5))
					directory = f.getParent();
				if (!directory.endsWith(File.separator))
					directory += File.separator;
			}
		}
		if (directory==null) {
			if (Prefs.useFileChooser && !IJ.isMacOSX()) {
				String digits = getDigits(number);
				SaveDialog sd = new SaveDialog(title, name+digits+extension, extension);
				String name2 = sd.getFileName();
				if (name2==null)
					return;
				directory = sd.getDirectory();
			} else
				directory = IJ.getDirectory(title);
		}
		if (directory==null)
			return;
		Overlay overlay = imp.getOverlay();
		boolean isOverlay = overlay!=null && !imp.getHideOverlay();
		if (!(format.equals("jpeg")||format.equals("png")))
			isOverlay = false;
		ImageStack stack = imp.getStack();
		ImagePlus imp2 = new ImagePlus();
		imp2.setTitle(imp.getTitle());
		Calibration cal = imp.getCalibration();
		int nSlices = stack.getSize();
		String path,label=null;
		imp.lock();
		for (int i=1; i<=nSlices; i++) {
			IJ.showStatus("writing: "+i+"/"+nSlices);
			IJ.showProgress(i, nSlices);
			ImageProcessor ip = stack.getProcessor(i);
			if (isOverlay) {
				imp.setSliceWithoutUpdate(i);
				ip = imp.flatten().getProcessor();
			} else if (luts!=null && nChannels>1 && hyperstack) {
				ip.setColorModel(luts[lutIndex++]);
				if (lutIndex>=luts.length) lutIndex = 0;
			}
			imp2.setProcessor(null, ip);
			String label2 = stack.getSliceLabel(i);
			if (label2!=null && label2.indexOf("\n")!=-1)
				imp2.setProperty("Info", label2);
			else {
				Properties props = imp2.getProperties();
				if (props!=null) props.remove("Info");
			}
			imp2.setCalibration(cal);
			String digits = getDigits(number++);
			if (useLabels) {
				label = stack.getShortSliceLabel(i);
				if (label!=null && label.equals("")) label = null;
				if (label!=null) label = label.replaceAll("/","-");
			}
			if (label==null)
				path = directory+name+digits+extension;
			else
				path = directory+label+extension;
			if (i==1) {
				File f = new File(path);
				if (f.exists()) {
					if (!IJ.isMacro() && !IJ.showMessageWithCancel("Overwrite files?",
						"One or more files will be overwritten if you click \"OK\".\n \n"+path)) {
						imp.unlock();
						IJ.showStatus("");
						IJ.showProgress(1.0);
						return;
					}
				}
			}
			if (Recorder.record)
				Recorder.disablePathRecording();
			if (overlay!=null && format.equals("tiff")) {
				Overlay overlay2 = overlay.duplicate();
				overlay2.crop(i, i);
				if (overlay2.size()>0) {
					for (int j=0; j<overlay2.size(); j++) {
						Roi roi = overlay2.get(j);
						int pos = roi.getPosition();
						if (pos==1)
							roi.setPosition(i);
					}
					imp2.setOverlay(overlay2);
				}
			}
			IJ.saveAs(imp2, format, path);
		}
		imp.unlock();
		if (isOverlay) imp.setSlice(1);
		IJ.showStatus("");
	}
	
	/**
	 * Gets the digits.
	 *
	 * @param n the n
	 * @return the digits
	 */
	String getDigits(int n) {
		if (hyperstack) {
			int c = (n%dim[2])+1;
			int z = ((n/dim[2])%dim[3])+1;
			int t = ((n/(dim[2]*dim[3]))%dim[4])+1;
			String cs="", zs="", ts="";
			if (dim[2]>1) {
				cs = "00000000"+c;
				cs = "_c"+cs.substring(cs.length()-ndigits);
			}
			if (dim[3]>1) {
				zs = "00000000"+z;
				zs = "_z"+zs.substring(zs.length()-ndigits);
			}
			if (dim[4]>1) {
				ts = "00000000"+t;
				ts = "_t"+ts.substring(ts.length()-ndigits);
			}
			return ts+zs+cs;
		} else {
			String digits = "00000000"+(startAt+n);
			return digits.substring(digits.length()-ndigits);
		}
	}
	
}

