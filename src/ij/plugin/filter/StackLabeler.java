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
import ij.Macro;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.measure.Measurements;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.Tools;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;


// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Stacks/Label command. */
public class StackLabeler implements ExtendedPlugInFilter, DialogListener {
	
	/** The Constant formats. */
	private static final String[] formats = {"0", "0000", "00:00", "00:00:00", "Text","Label"};
	
	/** The Constant LABEL. */
	private static final int NUMBER=0, ZERO_PADDED_NUMBER=1, MIN_SEC=2, HOUR_MIN_SEC=3, TEXT=4, LABEL=5;
	
	/** The format. */
	private static int format = (int)Prefs.get("label.format", NUMBER);
	
	/** The flags. */
	private int flags = DOES_ALL;
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The x. */
	private static int x = 5;
	
	/** The y. */
	private static int y = 20;
	
	/** The font size. */
	private static int fontSize = 18;
	
	/** The max width. */
	private int maxWidth;
	
	/** The font. */
	private Font font;
	
	/** The start. */
	private static double start = 0;
	
	/** The interval. */
	private static double interval = 1;
	
	/** The text. */
	private static String text = "";
	
	/** The decimal places. */
	private static int decimalPlaces = 0;
	
	/** The use overlay. */
	private static boolean useOverlay;
	
	/** The use text tool font. */
	private static boolean useTextToolFont;
	
	/** The field width. */
	private int fieldWidth;
	
	/** The color. */
	private Color color;
	
	/** The default last frame. */
	private int firstFrame, lastFrame, defaultLastFrame;
	
	/** The overlay. */
	private Overlay overlay;
	
	/** The base overlay. */
	private Overlay baseOverlay;
	
	/** The previewing. */
	private boolean previewing; 
	
	/** The virtual stack. */
	private boolean virtualStack; 
	
	/** The yoffset. */
	private int yoffset;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		if (imp!=null) {
			virtualStack = imp.getStack().isVirtual();
			if (virtualStack) useOverlay = true;
			baseOverlay = imp.getOverlay();
			flags += virtualStack?0:DOES_STACKS;
			firstFrame=1; lastFrame=defaultLastFrame=imp.getStackSize();
		}
		this.imp = imp;
		return flags;
	}

    /* (non-Javadoc)
     * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
     */
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		ImageProcessor ip = imp.getProcessor();
		Rectangle roi = ip.getRoi();
		if (roi.width<ip.getWidth() || roi.height<ip.getHeight()) {
			x = roi.x;
			y = roi.y+roi.height;
			fontSize = (int) ((roi.height - 1.10526)/0.934211);	
			if (fontSize<7) fontSize = 7;
			if (fontSize>80) fontSize = 80;
		}
		if (IJ.macroRunning()) {
			format = NUMBER;
			decimalPlaces = 0;
		    interval=1;
			text = "";
			start = 0;
			useOverlay = false;
			useTextToolFont = false;
			String options = Macro.getOptions();
			if (options!=null) {
				if (options.indexOf("interval=0")!=-1 && options.indexOf("format=")==-1)
					format = TEXT;
				if (options.indexOf(" slice=")!=-1) {
					options = options.replaceAll(" slice=", " range=");
					Macro.setOptions(options);
				}
			}
		}
		if (format<0||format>LABEL) format = NUMBER;
		int defaultLastFrame = imp.getStackSize();
		if (imp.isHyperStack()) {
			if (imp.getNFrames()>1)
				defaultLastFrame = imp.getNFrames();
			else if (imp.getNSlices()>1)
				defaultLastFrame = imp.getNSlices();
		}
		GenericDialog gd = new GenericDialog("Label Stacks");
		gd.setInsets(2, 5, 0);
		gd.addChoice("Format:", formats, formats[format]);
		gd.addStringField("Starting value:", IJ.d2s(start,decimalPlaces));
		gd.addStringField("Interval:", ""+IJ.d2s(interval,decimalPlaces));
		gd.addNumericField("X location:", x, 0);
		gd.addNumericField("Y location:", y, 0);
		gd.addNumericField("Font size:", fontSize, 0);
		gd.addStringField("Text:", text, 10);
        addRange(gd, "Range:", 1, defaultLastFrame);
		gd.setInsets(10,20,0);
        gd.addCheckbox(" Use overlay", useOverlay);
        gd.addCheckbox(" Use_text tool font", useTextToolFont);
        gd.addPreviewCheckbox(pfr);
        gd.addHelp(IJ.URL+"/docs/menus/image.html#label");
        gd.addDialogListener(this);
        previewing = true;
		gd.showDialog();
		previewing = false;
		if (gd.wasCanceled())
        	return DONE;
        else
        	return flags;
    }

	/**
	 * Adds the range.
	 *
	 * @param gd the gd
	 * @param label the label
	 * @param start the start
	 * @param end the end
	 */
	void addRange(GenericDialog gd, String label, int start, int end) {
		gd.addStringField(label, start+"-"+end);
	}
	
	/**
	 * Gets the range.
	 *
	 * @param gd the gd
	 * @param start the start
	 * @param end the end
	 * @return the range
	 */
	double[] getRange(GenericDialog gd, int start, int end) {
		String[] range = Tools.split(gd.getNextString(), " -");
		double d1 = Tools.parseDouble(range[0]);
		double d2 = range.length==2?Tools.parseDouble(range[1]):Double.NaN;
		double[] result = new double[2];
		result[0] = Double.isNaN(d1)?1:(int)d1;
		result[1] = Double.isNaN(d2)?end:(int)d2;
		if (result[0]<start) result[0] = start;
		if (result[1]>end) result[1] = end;
		if (result[0]>result[1]) {
			result[0] = start;
			result[1] = end;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		format = gd.getNextChoiceIndex();
		start = Tools.parseDouble(gd.getNextString());
 		String str = gd.getNextString();
 		interval = Tools.parseDouble(str);
		x = (int)gd.getNextNumber();
		y = (int)gd.getNextNumber();
		fontSize = (int)gd.getNextNumber();
		text = gd.getNextString();
		double[] range = getRange(gd, 1, defaultLastFrame);
		useOverlay = gd.getNextBoolean();
		useTextToolFont = gd.getNextBoolean();
		if (virtualStack) useOverlay = true;
		firstFrame=(int)range[0]; lastFrame=(int)range[1];
		int index = str.indexOf(".");
		if (index!=-1)
			decimalPlaces = str.length()-index-1;
		else
			decimalPlaces = 0;
		if (gd.invalidNumber()) return false;
		if (useTextToolFont)
			font = new Font(TextRoi.getFont(), TextRoi.getStyle(), fontSize);
		else
			font = new Font("SansSerif", Font.PLAIN, fontSize);
		if (y<fontSize) y = fontSize+5;
		ImageProcessor ip = imp.getProcessor();
		ip.setFont(font);
		int size = defaultLastFrame;
		maxWidth = ip.getStringWidth(getString(size, interval, format));
		fieldWidth = 1;
		if (size>=10) fieldWidth = 2;
		if (size>=100) fieldWidth = 3;
		if (size>=1000) fieldWidth = 4;
		if (size>=10000) fieldWidth = 5;
		Prefs.set("label.format", format);
        return true;
    }
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		int image = ip.getSliceNumber();
		int n = image - 1;
		if (imp.isHyperStack()) n = updateIndex(n);
		if (virtualStack) {
			int nSlices = imp.getStackSize();
			if (previewing) nSlices = 1;
			for (int i=1; i<=nSlices; i++) {
				image=i; n=i-1;
				if (imp.isHyperStack()) n = updateIndex(n);
				drawLabel(ip, image, n);
			}
		} else {
			if (previewing && overlay!=null) {
				imp.setOverlay(baseOverlay);
				overlay = null;
			}
			drawLabel(ip, image, n);
		}
	}
	
	/**
	 * Update index.
	 *
	 * @param n the n
	 * @return the int
	 */
	int updateIndex(int n) {
		if (imp.getNFrames()>1)
			return (int)(n*((double)(imp.getNFrames())/imp.getStackSize()));
		else if (imp.getNSlices()>1)
			return (int)(n*((double)(imp.getNSlices())/imp.getStackSize()));
		else
			return n;
	}
	
	/**
	 * Draw label.
	 *
	 * @param ip the ip
	 * @param image the image
	 * @param n the n
	 */
	void drawLabel(ImageProcessor ip, int image, int n) {
		String s = getString(n, interval, format);
		ip.setFont(font);
		int textWidth = ip.getStringWidth(s);
		if (color==null) {
			color = Toolbar.getForegroundColor();
			if ((color.getRGB()&0xffffff)==0) {
				ip.setRoi(x, y-fontSize, maxWidth+textWidth, fontSize);
				double mean = ImageStatistics.getStatistics(ip, Measurements.MEAN, null).mean;
				if (mean<50.0 && !ip.isInvertedLut()) color=Color.white;
				ip.resetRoi();
			}
		}
		int frame = image;
		if (imp.isHyperStack()) {
			int[] pos = imp.convertIndexToPosition(image);
			if (imp.getNFrames()>1)
				frame = pos[2];
			else if (imp.getNSlices()>1)
				frame = pos[1];
		}
		if (useOverlay) {
			if (image==1) {
				overlay = new Overlay();
				if (baseOverlay!=null) {
					for (int i=0; i<baseOverlay.size(); i++)
						overlay.add(baseOverlay.get(i));
				}
				Roi roi = imp.getRoi();
				Rectangle r = roi!=null?roi.getBounds():null;
				yoffset = r!=null?r.height:fontSize;
			}
			if (frame>=firstFrame&&frame<=lastFrame) {
				int xloc = format==LABEL?x:x+maxWidth-textWidth;
				Roi roi = new TextRoi(xloc, y-yoffset, s, font);
				roi.setStrokeColor(color);
				roi.setNonScalable(true);
				roi.setPosition(image);
				overlay.add(roi);
			}
			if (image==imp.getStackSize()||previewing)
				imp.setOverlay(overlay);
		} else if (frame>=firstFrame&&frame<=lastFrame) {
			ip.setColor(color); 
			ip.setAntialiasedText(fontSize>=18);
			int xloc = format==LABEL?x:x+maxWidth-textWidth;
			ip.moveTo(xloc, y);
			ip.drawString(s);
		}
	}
	
	/**
	 * Gets the string.
	 *
	 * @param index the index
	 * @param interval the interval
	 * @param format the format
	 * @return the string
	 */
	String getString(int index, double interval, int format) {
		double time = start + (index+1-firstFrame)*interval;
		int itime = (int)Math.floor(time);
		int sign = 1;
		if (itime < 0) sign = -1;
		itime = itime*sign;
		String str = "";
		switch (format) {
			case NUMBER: str=IJ.d2s(time, decimalPlaces)+" "+text; break;
			case ZERO_PADDED_NUMBER:
				if (decimalPlaces==0)
					str=zeroFill((int)time); 
				else
					str=IJ.d2s(time, decimalPlaces);
				str = text +" " + str;
				break;
			case MIN_SEC:
				str=pad((int)Math.floor((itime/60)%60))+":"+pad(itime%60)+" "+text;
				if (sign == -1) str = "-"+str;
				break;
			case HOUR_MIN_SEC:
				str=pad((int)Math.floor(itime/3600))+":"+pad((int)Math.floor((itime/60)%60))+":"+pad(itime%60)+" "+text;
				if (sign == -1) str = "-"+str;
				break;
			case TEXT: 
				str=text; 
				break;
			case LABEL:
				if (0<=index && index<imp.getStackSize()) {
					str = imp.getStack().getShortSliceLabel(index+1);
					str = str==null?"null slice label ("+(index+1)+")":str;
				} else
					str="void";
				break;
		}
		return str;
	}

	/**
	 * Pad.
	 *
	 * @param n the n
	 * @return the string
	 */
	String pad(int n) {
		String str = ""+n;
		if (str.length()==1) str="0"+str;
		return str;
	}
	
	/**
	 * Zero fill.
	 *
	 * @param n the n
	 * @return the string
	 */
	String  zeroFill(int n) {
		String str = ""+n;
		while (str.length()<fieldWidth)
			str = "0" + str;
		return str;
	}
		
	/* (non-Javadoc)
	 * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
	 */
	public void setNPasses (int nPasses) {}

}
