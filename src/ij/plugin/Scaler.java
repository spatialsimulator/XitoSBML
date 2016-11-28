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
import ij.Undo;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import ij.util.Tools;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Scale command. */
public class Scaler implements PlugIn, TextListener, FocusListener {
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The xstr. */
	private static String xstr = "0.5";
	
	/** The ystr. */
	private static String ystr = "0.5";
	
	/** The zstr. */
	private String zstr = "1.0";
	
	/** The new height. */
	private static int newWidth, newHeight;
	
	/** The new depth. */
	private int newDepth;
    
    /** The average when downsizing. */
    private static boolean averageWhenDownsizing = true;
	
	/** The new window. */
	private static boolean newWindow = true;
	
	/** The interpolation method. */
	private static int interpolationMethod = ImageProcessor.BILINEAR;
	
	/** The methods. */
	private String[] methods = ImageProcessor.getInterpolationMethods();
	
	/** The fill with background. */
	private static boolean fillWithBackground;
	
	/** The process stack. */
	private static boolean processStack = true;
	
	/** The zscale. */
	private double xscale, yscale, zscale;
	
	/** The title. */
	private String title = "Untitled";
	
	/** The fields. */
	private Vector fields;
	
	/** The bg value. */
	private double bgValue;
	
	/** The constain aspect ratio. */
	private boolean constainAspectRatio = true;
	
	/** The depth field. */
	private TextField xField, yField, zField, widthField, heightField, depthField;
	
	/** The r. */
	private Rectangle r;
	
	/** The field with focus. */
	private Object fieldWithFocus;
	
	/** The old depth. */
	private int oldDepth;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		imp = IJ.getImage();
		Roi roi = imp.getRoi();
		if (roi!=null && !roi.isArea())
			imp.deleteRoi(); // ignore any line selection
		ImageProcessor ip = imp.getProcessor();
		if (!showDialog(ip))
			return;
		if (newDepth>0 && newDepth!=oldDepth) {
			newWindow = true;
			processStack = true;
		}
		if ((ip.getWidth()>1 && ip.getHeight()>1) || newWindow)
			ip.setInterpolationMethod(interpolationMethod);
		else
			ip.setInterpolationMethod(ImageProcessor.NONE);
		ip.setBackgroundValue(bgValue);
		imp.startTiming();
		try {
			if (newWindow && imp.getStackSize()>1 && processStack)
				createNewStack(imp, ip);
			else
				scale(ip);
		}
		catch(OutOfMemoryError o) {
			IJ.outOfMemory("Scale");
		}
		IJ.showProgress(1.0);
	}
	
	/**
	 * Creates the new stack.
	 *
	 * @param imp the imp
	 * @param ip the ip
	 */
	void createNewStack(ImagePlus imp, ImageProcessor ip) {
		int nSlices = imp.getStackSize();
		int w=imp.getWidth(), h=imp.getHeight();
		ImagePlus imp2 = imp.createImagePlus();
		Rectangle r = ip.getRoi();
		boolean crop = r.width!=imp.getWidth() || r.height!=imp.getHeight();
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(newWidth, newHeight);
		ImageProcessor ip1, ip2;
		int method = interpolationMethod;
		if (w==1 || h==1)
			method = ImageProcessor.NONE;
		for (int i=1; i<=nSlices; i++) {
			IJ.showStatus("Scale: " + i + "/" + nSlices);
			ip1 = stack1.getProcessor(i);
			String label = stack1.getSliceLabel(i);
			if (crop) {
				ip1.setRoi(r);
				ip1 = ip1.crop();
			}
			ip1.setInterpolationMethod(method);
			ip2 = ip1.resize(newWidth, newHeight, averageWhenDownsizing);
			if (ip2!=null)
				stack2.addSlice(label, ip2);
			IJ.showProgress(i, nSlices);
		}
		imp2.setStack(title, stack2);
		Calibration cal = imp2.getCalibration();
		if (cal.scaled()) {
			cal.pixelWidth *= 1.0/xscale;
			cal.pixelHeight *= 1.0/yscale;
		}
		IJ.showProgress(1.0);
		int[] dim = imp.getDimensions();
		imp2.setDimensions(dim[2], dim[3], dim[4]);
		if (imp.isComposite()) {
			imp2 = new CompositeImage(imp2, ((CompositeImage)imp).getMode());
			((CompositeImage)imp2).copyLuts(imp);
		}
		if (imp.isHyperStack())
			imp2.setOpenAsHyperStack(true);
		if (newDepth>0 && newDepth!=oldDepth) {
			Resizer resizer = new Resizer();
			resizer.setAverageWhenDownsizing(averageWhenDownsizing);
			imp2 = resizer.zScale(imp2, newDepth, interpolationMethod);
		}
		if (imp2!=null) {
			imp2.show();
			imp2.changes = true;
		}
	}

	/**
	 * Scale.
	 *
	 * @param ip the ip
	 */
	void scale(ImageProcessor ip) {
		if (newWindow) {
			Rectangle r = ip.getRoi();
			ImagePlus imp2 = imp.createImagePlus();
			imp2.setProcessor(title, ip.resize(newWidth, newHeight, averageWhenDownsizing));
			Calibration cal = imp2.getCalibration();
			if (cal.scaled()) {
				cal.pixelWidth *= 1.0/xscale;
				cal.pixelHeight *= 1.0/yscale;
			}
			imp2.show();
			imp.trimProcessor();
			imp2.trimProcessor();
			imp2.changes = true;
		} else {
			if (processStack && imp.getStackSize()>1) {
				Undo.reset();
				StackProcessor sp = new StackProcessor(imp.getStack(), ip);
				sp.scale(xscale, yscale, bgValue);
			} else {
				ip.snapshot();
				Undo.setup(Undo.FILTER, imp);
				ip.setSnapshotCopyMode(true);
				ip.scale(xscale, yscale);
				ip.setSnapshotCopyMode(false);
			}
			imp.deleteRoi();
			imp.updateAndDraw();
			imp.changes = true;
		}
	}
	
	/**
	 * Show dialog.
	 *
	 * @param ip the ip
	 * @return true, if successful
	 */
	boolean showDialog(ImageProcessor ip) {
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			if (macroOptions.indexOf(" interpolate")!=-1)
				macroOptions.replaceAll(" interpolate", " interpolation=Bilinear");
			else if (macroOptions.indexOf(" interpolation=")==-1)
				macroOptions = macroOptions+" interpolation=None";
			Macro.setOptions(macroOptions);
		}
		int bitDepth = imp.getBitDepth();
		int stackSize = imp.getStackSize();
		boolean isStack = stackSize>1;
		oldDepth = stackSize;
		if (isStack) {
			xstr = "1.0";
			ystr = "1.0";
			zstr = "1.0";
		}
		r = ip.getRoi();
		int width = newWidth;
		if (width==0) width = r.width;
		int height = (int)((double)width*r.height/r.width);
		xscale = Tools.parseDouble(xstr, 0.0);
		yscale = Tools.parseDouble(ystr, 0.0);
		zscale = 1.0;
		if (xscale!=0.0 && yscale!=0.0) {
			width = (int)(r.width*xscale);
			height = (int)(r.height*yscale);
		} else {
			xstr = "-";
			ystr = "-";
		}
		GenericDialog gd = new GenericDialog("Scale");
		gd.addStringField("X Scale:", xstr);
		gd.addStringField("Y Scale:", ystr);
		if (isStack)
			gd.addStringField("Z Scale:", zstr);
		gd.setInsets(5, 0, 5);
		gd.addStringField("Width (pixels):", ""+width);
		gd.addStringField("Height (pixels):", ""+height);
		if (isStack) {
			String label = "Depth (images):";
			if (imp.isHyperStack()) {
				int slices = imp.getNSlices();
				int frames = imp.getNFrames();
				if (slices==1&&frames>1) {
					label = "Depth (frames):";
					oldDepth = frames;
				} else {
					label = "Depth (slices):";
					oldDepth = slices;
				}
			}
			gd.addStringField(label, ""+oldDepth);
		}
		fields = gd.getStringFields();
		for (int i=0; i<fields.size(); i++) {
			((TextField)fields.elementAt(i)).addTextListener(this);
			((TextField)fields.elementAt(i)).addFocusListener(this);
		}
		xField = (TextField)fields.elementAt(0);
		yField = (TextField)fields.elementAt(1);
		if (isStack) {
			zField = (TextField)fields.elementAt(2);
			widthField = (TextField)fields.elementAt(3);
			heightField = (TextField)fields.elementAt(4);
			depthField = (TextField)fields.elementAt(5);
		} else {
			widthField = (TextField)fields.elementAt(2);
			heightField = (TextField)fields.elementAt(3);
		}
		fieldWithFocus = xField;
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		if (bitDepth==8 || bitDepth==24)
			gd.addCheckbox("Fill with background color", fillWithBackground);
		gd.addCheckbox("Average when downsizing", averageWhenDownsizing);
		boolean hyperstack = imp.isHyperStack() || imp.isComposite();
		if (isStack && !hyperstack)
			gd.addCheckbox("Process entire stack", processStack);
		gd.addCheckbox("Create new window", newWindow);
		title = WindowManager.getUniqueName(imp.getTitle());
		gd.setInsets(10, 0, 0);
		gd.addStringField("Title:", title, 12);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		xstr = gd.getNextString();
		ystr = gd.getNextString();
		xscale = Tools.parseDouble(xstr, 0.0);
		yscale = Tools.parseDouble(ystr, 0.0);
		if (isStack) {
			zstr = gd.getNextString();
			zscale = Tools.parseDouble(ystr, 0.0);
		}
		String wstr = gd.getNextString();
		newWidth = (int)Tools.parseDouble(wstr, 0);
		newHeight = (int)Tools.parseDouble(gd.getNextString(), 0);
		if (newHeight!=0 && (wstr.equals("-") || wstr.equals("0")))
				newWidth= (int)(newHeight*(double)r.width/r.height);
		if (newWidth==0 || newHeight==0) {
			IJ.error("Scaler", "Width or height is 0");
			return false;
		}
		if (xscale>0.0 && yscale>0.0) {
			newWidth = (int)(r.width*xscale);
			newHeight = (int)(r.height*yscale);
		}
		if (isStack)
			newDepth = (int)Tools.parseDouble(gd.getNextString(), 0);
		interpolationMethod = gd.getNextChoiceIndex();
		if (bitDepth==8 || bitDepth==24)
			fillWithBackground = gd.getNextBoolean();
		averageWhenDownsizing = gd.getNextBoolean();
		if (isStack && !hyperstack)
			processStack = gd.getNextBoolean();
		if (hyperstack)
			processStack = true;
		newWindow = gd.getNextBoolean();
		if (xscale==0.0) {
			xscale = (double)newWidth/r.width;
			yscale = (double)newHeight/r.height;
		}
		title = gd.getNextString();

		if (fillWithBackground) {
			Color bgc = Toolbar.getBackgroundColor();
			if (bitDepth==8)
				bgValue = ip.getBestIndex(bgc);
			else if (bitDepth==24)
				bgValue = bgc.getRGB();
		} else
			bgValue = 0.0;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.TextListener#textValueChanged(java.awt.event.TextEvent)
	 */
	public void textValueChanged(TextEvent e) {
		Object source = e.getSource();
		double newXScale = xscale;
		double newYScale = yscale;
		double newZScale = zscale;
		if (source==xField && fieldWithFocus==xField) {
			String newXText = xField.getText();
			newXScale = Tools.parseDouble(newXText,0);
			if (newXScale==0) return;
			if (newXScale!=xscale) {
				int newWidth = (int)(newXScale*r.width);
				widthField.setText(""+newWidth);
				if (constainAspectRatio) {
					yField.setText(newXText);
					int newHeight = (int)(newXScale*r.height);
					heightField.setText(""+newHeight);
				}
			}
		} else if (source==yField && fieldWithFocus==yField) {
			String newYText = yField.getText();
			newYScale = Tools.parseDouble(newYText,0);
			if (newYScale==0) return;
			if (newYScale!=yscale) {
				int newHeight = (int)(newYScale*r.height);
				heightField.setText(""+newHeight);
			}
		} else if (source==zField && fieldWithFocus==zField) {
			String newZText = zField.getText();
			newZScale = Tools.parseDouble(newZText,0);
			if (newZScale==0) return;
			if (newZScale!=zscale) {
				int nSlices = imp.getStackSize();
				if (imp.isHyperStack()) {
					int slices = imp.getNSlices();
					int frames = imp.getNFrames();
					if (slices==1&&frames>1)
						nSlices = frames;
					else
						nSlices = slices;
				}
				int newDepth= (int)(newZScale*nSlices);
				depthField.setText(""+newDepth);
			}
		} else if (source==widthField && fieldWithFocus==widthField) {
			int newWidth = (int)Tools.parseDouble(widthField.getText(), 0.0);
			if (newWidth!=0) {
				int newHeight = (int)(newWidth*(double)r.height/r.width);
				heightField.setText(""+newHeight);
				xField.setText("-");
				yField.setText("-");
				newXScale = 0.0;
				newYScale = 0.0;
			}
       } else if (source==depthField && fieldWithFocus==depthField) {
            int newDepth = (int)Tools.parseDouble(depthField.getText(), 0.0);
            if (newDepth!=0) {
                zField.setText("-");
                newZScale = 0.0;
            }
        }
		xscale = newXScale;
		yscale = newYScale;
		zscale = newZScale;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		fieldWithFocus = e.getSource();
		if (fieldWithFocus==widthField)
			constainAspectRatio = true;
		else if (fieldWithFocus==yField)
			constainAspectRatio = false;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {}

}
