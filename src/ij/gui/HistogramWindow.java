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
package ij.gui;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.LookUpTable;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

// TODO: Auto-generated Javadoc
/** This class is an extended ImageWindow that displays histograms. */
public class HistogramWindow extends ImageWindow implements Measurements, ActionListener, 
	ClipboardOwner, ImageListener, RoiListener, Runnable {
	
	/** The Constant WIN_WIDTH. */
	static final int WIN_WIDTH = 300;
	
	/** The Constant WIN_HEIGHT. */
	static final int WIN_HEIGHT = 240;
	
	/** The Constant HIST_WIDTH. */
	static final int HIST_WIDTH = 256;
	
	/** The Constant HIST_HEIGHT. */
	static final int HIST_HEIGHT = 128;
	
	/** The Constant BAR_HEIGHT. */
	static final int BAR_HEIGHT = 12;
	
	/** The Constant XMARGIN. */
	static final int XMARGIN = 20;
	
	/** The Constant YMARGIN. */
	static final int YMARGIN = 10;
	
	/** The Constant BLUE. */
	static final int INTENSITY=0, RED=1, GREEN=2, BLUE=3;
	
	/** The stats. */
	protected ImageStatistics stats;
	
	/** The histogram. */
	protected long[] histogram;
	
	/** The lut. */
	protected LookUpTable lut;
	
	/** The frame. */
	protected Rectangle frame = null;
	
	/** The rgb. */
	protected Button list, save, copy, log, live, rgb;
	
	/** The count. */
	protected Label value, count;
	
	/** The default directory. */
	protected static String defaultDirectory = null;
	
	/** The decimal places. */
	protected int decimalPlaces;
	
	/** The digits. */
	protected int digits;
	
	/** The new max count. */
	protected long newMaxCount;
	
	/** The plot scale. */
	protected int plotScale = 1;
	
	/** The log scale. */
	protected boolean logScale;
	
	/** The cal. */
	protected Calibration cal;
	
	/** The y max. */
	protected int yMax;
	
	/** The n bins. */
	public static int nBins = 256;
		
	/** The src image ID. */
	private int srcImageID;			// ID of source image
	
	/** The src imp. */
	private ImagePlus srcImp;		// source image for live histograms
	
	/** The bg thread. */
	private Thread bgThread;		// thread background drawing
	
	/** The do update. */
	private boolean doUpdate;	// tells background thread to update
	
	/** The channel. */
	private int channel;				// RGB channel
	
	/** The blank label. */
	private String blankLabel;
	
	/** The stack histogram. */
	private boolean stackHistogram;
	    
	/**
	 *  Displays a histogram using the title "Histogram of ImageName".
	 *
	 * @param imp the imp
	 */
	public HistogramWindow(ImagePlus imp) {
		super(NewImage.createRGBImage("Histogram of "+imp.getShortTitle(), WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, 256, 0.0, 0.0);
	}

	/**
	 *  Displays a histogram using the specified title and number of bins. 
	 * 		Currently, the number of bins must be 256 expect for 32 bit images.
	 *
	 * @param title the title
	 * @param imp the imp
	 * @param bins the bins
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins) {
		super(NewImage.createRGBImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, bins, 0.0, 0.0);
	}

	/**
	 *  Displays a histogram using the specified title, number of bins and histogram range.
	 * 		Currently, the number of bins must be 256 and the histogram range range must be the 
	 * 		same as the image range expect for 32 bit images.
	 *
	 * @param title the title
	 * @param imp the imp
	 * @param bins the bins
	 * @param histMin the hist min
	 * @param histMax the hist max
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins, double histMin, double histMax) {
		super(NewImage.createRGBImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, bins, histMin, histMax);
	}

	/**
	 *  Displays a histogram using the specified title, number of bins, histogram range and yMax.
	 *
	 * @param title the title
	 * @param imp the imp
	 * @param bins the bins
	 * @param histMin the hist min
	 * @param histMax the hist max
	 * @param yMax the y max
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins, double histMin, double histMax, int yMax) {
		super(NewImage.createRGBImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		this.yMax = yMax;
		showHistogram(imp, bins, histMin, histMax);
	}

	/**
	 *  Displays a histogram using the specified title and ImageStatistics.
	 *
	 * @param title the title
	 * @param imp the imp
	 * @param stats the stats
	 */
	public HistogramWindow(String title, ImagePlus imp, ImageStatistics stats) {
		super(NewImage.createRGBImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		//IJ.log("HistogramWindow: "+stats.histMin+"  "+stats.histMax+"  "+stats.nBins);
		this.yMax = stats.histYMax;
		showHistogram(imp, stats);
	}

	/**
	 *  Draws the histogram using the specified title and number of bins.
	 * 		Currently, the number of bins must be 256 expect for 32 bit images.
	 *
	 * @param imp the imp
	 * @param bins the bins
	 */
	public void showHistogram(ImagePlus imp, int bins) {
		showHistogram(imp, bins, 0.0, 0.0);
	}

	/**
	 *  Draws the histogram using the specified title, number of bins and histogram range.
	 * 		Currently, the number of bins must be 256 and the histogram range range must be 
	 * 		the same as the image range expect for 32 bit images.
	 *
	 * @param imp the imp
	 * @param bins the bins
	 * @param histMin the hist min
	 * @param histMax the hist max
	 */
	public void showHistogram(ImagePlus imp, int bins, double histMin, double histMax) {
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		if (channel!=INTENSITY && imp.getType()==ImagePlus.COLOR_RGB) {
			ColorProcessor cp = (ColorProcessor)imp.getProcessor();
			ImageProcessor ip = cp.getChannel(channel, null);
			ImagePlus imp2 = new ImagePlus("", ip);
			imp2.setRoi(imp.getRoi());
			stats = imp2.getStatistics(AREA+MEAN+MODE+MIN_MAX, bins, histMin, histMax);
		} else
			stats = imp.getStatistics(AREA+MEAN+MODE+MIN_MAX+(limitToThreshold?LIMIT:0), bins, histMin, histMax);
		showHistogram(imp, stats);
	}

	/**
	 *  Draws the histogram using the specified title and ImageStatistics.
	 *
	 * @param imp the imp
	 * @param stats the stats
	 */
	public void showHistogram(ImagePlus imp, ImageStatistics stats) {
		stackHistogram = stats.stackStatistics;
		if (list==null)
			setup(imp);
		this.stats = stats;
		cal = imp.getCalibration();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		imp.getMask();
		histogram = stats.getHistogram();
		if (limitToThreshold && histogram.length==256) {
			ImageProcessor ip = imp.getProcessor();
			if (ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
				int lower = scaleDown(ip, ip.getMinThreshold());
				int upper = scaleDown(ip, ip.getMaxThreshold());
				for (int i=0; i<lower; i++)
					histogram[i] = 0L;
				for (int i=upper+1; i<256; i++)
					histogram[i] = 0L;
			}
		}
		lut = imp.createLut();
		int type = imp.getType();
		boolean fixedRange = type==ImagePlus.GRAY8 || type==ImagePlus.COLOR_256 || type==ImagePlus.COLOR_RGB;
		ImageProcessor ip = this.imp.getProcessor();
		ip.setColor(Color.white);
		ip.resetRoi();
		ip.fill();
		ImageProcessor srcIP = imp.getProcessor();
		drawHistogram(imp, ip, fixedRange, stats.histMin, stats.histMax);
		this.imp.updateAndDraw();
	}

	/**
	 * Sets the up.
	 *
	 * @param imp the new up
	 */
	private void setup(ImagePlus imp) {
		boolean isRGB = imp.getType()==ImagePlus.COLOR_RGB;
 		Panel buttons = new Panel();
 		int hgap = IJ.isMacOSX()||isRGB?1:5;
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT,hgap,0));
		int trim = IJ.isMacOSX()?6:0;
		list = new TrimmedButton("List", trim);
		list.addActionListener(this);
		buttons.add(list);
		copy = new TrimmedButton("Copy", trim);
		copy.addActionListener(this);
		buttons.add(copy);
		log = new TrimmedButton("Log", trim);
		log.addActionListener(this);
		buttons.add(log);
		if (!stackHistogram) {
			live = new TrimmedButton("Live", trim);
			live.addActionListener(this);
			buttons.add(live);
		}
		if (imp!=null && isRGB && !stackHistogram) {
			rgb = new TrimmedButton("RGB", trim);
			rgb.addActionListener(this);
			buttons.add(rgb);
		}
		if (!(IJ.isMacOSX()&&isRGB)) {
			Panel valueAndCount = new Panel();
			valueAndCount.setLayout(new GridLayout(2,1,0,0));
			blankLabel = IJ.isMacOSX()?"           ":"                ";
			value = new Label(blankLabel);
			Font font = new Font("Monospaced", Font.PLAIN, 12);
			value.setFont(font);
			valueAndCount.add(value);
			count = new Label(blankLabel);
			count.setFont(font);
			valueAndCount.add(count);
			buttons.add(valueAndCount);
		}
		add(buttons);
		pack();
    }
    
	/**
	 * Setup.
	 */
	public void setup() {setup(null);}

	/* (non-Javadoc)
	 * @see ij.gui.ImageWindow#mouseMoved(int, int)
	 */
	public void mouseMoved(int x, int y) {
		if (value==null || count==null)
			return;
		if ((frame!=null)  && x>=frame.x && x<=(frame.x+frame.width)) {
			x = x - frame.x;
			if (x>255) x = 255;
			int index = (int)(x*((double)histogram.length)/HIST_WIDTH);
			String vlabel=null, clabel=null;
			if (blankLabel.length()==11) // OS X
				{vlabel=" "; clabel=" ";}
			else
				{vlabel=" value="; clabel=" count=";}
			String v = vlabel+d2s(cal.getCValue(stats.histMin+index*stats.binSize))+blankLabel;
			String c = clabel+histogram[index]+blankLabel;
			int len = vlabel.length() + blankLabel.length();
			value.setText(v.substring(0,len));
			count.setText(c.substring(0,len));
		} else {
			value.setText(blankLabel);
			count.setText(blankLabel);
		}
	}
	
	/**
	 * Draw histogram.
	 *
	 * @param ip the ip
	 * @param fixedRange the fixed range
	 */
	protected void drawHistogram(ImageProcessor ip, boolean fixedRange) {
		drawHistogram(null, ip, fixedRange, 0.0, 0.0);
	}

	/**
	 * Draw histogram.
	 *
	 * @param imp the imp
	 * @param ip the ip
	 * @param fixedRange the fixed range
	 * @param xMin the x min
	 * @param xMax the x max
	 */
	void drawHistogram(ImagePlus imp, ImageProcessor ip, boolean fixedRange, double xMin, double xMax) {
		int x, y;
		long maxCount2 = 0;
		int mode2 = 0;
		long saveModalCount;
		    	
		ip.setColor(Color.black);
		ip.setLineWidth(1);
		decimalPlaces = Analyzer.getPrecision();
		digits = cal.calibrated()||stats.binSize!=1.0?decimalPlaces:0;
		saveModalCount = histogram[stats.mode];
		for (int i = 0; i<histogram.length; i++) {
 			if ((histogram[i] > maxCount2) && (i != stats.mode)) {
				maxCount2 = histogram[i];
				mode2 = i;
  			}
  		}
		newMaxCount = histogram[stats.mode];
		if ((newMaxCount>(maxCount2 * 2)) && (maxCount2 != 0)) {
			newMaxCount = (int)(maxCount2 * 1.5);
  			//histogram[stats.mode] = newMaxCount;
		}
		if (logScale || IJ.shiftKeyDown() && !liveMode())
			drawLogPlot(yMax>0?yMax:newMaxCount, ip);
		drawPlot(yMax>0?yMax:newMaxCount, ip);
		histogram[stats.mode] = saveModalCount;
 		x = XMARGIN + 1;
		y = YMARGIN + HIST_HEIGHT + 2;
		if (imp==null)
			lut.drawUnscaledColorBar(ip, x-1, y, 256, BAR_HEIGHT);
		else
			drawAlignedColorBar(imp, xMin, xMax, ip, x-1, y, 256, BAR_HEIGHT);
		y += BAR_HEIGHT+15;
  		drawText(ip, x, y, fixedRange);
  		srcImageID = imp.getID();
	}
       
	/**
	 * Draw aligned color bar.
	 *
	 * @param imp the imp
	 * @param xMin the x min
	 * @param xMax the x max
	 * @param ip the ip
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 */
	void drawAlignedColorBar(ImagePlus imp, double xMin, double xMax, ImageProcessor ip, int x, int y, int width, int height) {
		ImageProcessor ipSource = imp.getProcessor();
		float[] pixels = null;
		ImageProcessor ipRamp = null;
		if (ipSource instanceof ColorProcessor) {
			ipRamp = new FloatProcessor(width, height);
			if (channel==RED)
				ipRamp.setColorModel(LUT.createLutFromColor(Color.red));
			else if (channel==GREEN)
				ipRamp.setColorModel(LUT.createLutFromColor(Color.green));
			else if (channel==BLUE)
				ipRamp.setColorModel(LUT.createLutFromColor(Color.blue));
			pixels = (float[])ipRamp.getPixels();
		} else
			pixels = new float[width*height];
		for (int j=0; j<height; j++) {
			for(int i=0; i<width; i++)
				pixels[i+width*j] = (float)(xMin+i*(xMax-xMin)/(width - 1));
		}
		if (!(ipSource instanceof ColorProcessor)) {
			ColorModel cm = null;
			if (imp.isComposite())
				cm = ((CompositeImage)imp).getChannelLut();
			else if (ipSource.getMinThreshold()==ImageProcessor.NO_THRESHOLD)
				cm = ipSource.getColorModel();
			else
				cm = ipSource.getCurrentColorModel();
			ipRamp = new FloatProcessor(width, height, pixels, cm);
		}
		double min = ipSource.getMin();
		double max = ipSource.getMax();
		ipRamp.setMinAndMax(min,max);
		ImageProcessor bar = null;
		if (ip instanceof ColorProcessor)
			bar = ipRamp.convertToRGB();
		else
			bar = ipRamp.convertToByte(true);
		ip.insert(bar, x,y);
		ip.setColor(Color.black);
		ip.drawRect(x-1, y, width+2, height);
	}

	/**
	 *  Scales a threshold level to the range 0-255.
	 *
	 * @param ip the ip
	 * @param threshold the threshold
	 * @return the int
	 */
	int scaleDown(ImageProcessor ip, double threshold) {
		double min = ip.getMin();
		double max = ip.getMax();
		if (max>min)
			return (int)(((threshold-min)/(max-min))*255.0);
		else
			return 0;
	}

	/**
	 * Draw plot.
	 *
	 * @param maxCount the max count
	 * @param ip the ip
	 */
	void drawPlot(long maxCount, ImageProcessor ip) {
		if (maxCount==0) maxCount = 1;
		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.drawRect(frame.x-1, frame.y, frame.width+2, frame.height+1);
		if (histogram.length<=HIST_WIDTH) {
			int index, y;
			for (int i=0; i<HIST_WIDTH; i++) {
				index = (int)(i*(double)histogram.length/HIST_WIDTH); 
				y = (int)(((double)HIST_HEIGHT*(double)histogram[index])/maxCount);
				if (y>HIST_HEIGHT) y = HIST_HEIGHT;
				ip.drawLine(i+XMARGIN, YMARGIN+HIST_HEIGHT, i+XMARGIN, YMARGIN+HIST_HEIGHT-y);
			}
		} else {
			double xscale = (double)HIST_WIDTH/histogram.length; 
			for (int i=0; i<histogram.length; i++) {
				long value = histogram[i];
				if (value>0L) {
					int y = (int)(((double)HIST_HEIGHT*(double)value)/maxCount);
					if (y>HIST_HEIGHT) y = HIST_HEIGHT;
					int x = (int)(i*xscale)+XMARGIN;
					ip.drawLine(x, YMARGIN+HIST_HEIGHT, x, YMARGIN+HIST_HEIGHT-y);
				}
			}
		}
	}
		
	/**
	 * Draw log plot.
	 *
	 * @param maxCount the max count
	 * @param ip the ip
	 */
	void drawLogPlot (long maxCount, ImageProcessor ip) {
		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.drawRect(frame.x-1, frame.y, frame.width+2, frame.height+1);
		double max = Math.log(maxCount);
		ip.setColor(Color.gray);
		if (histogram.length<=HIST_WIDTH) {
			int index, y;
			for (int i = 0; i<HIST_WIDTH; i++) {
				index = (int)(i*(double)histogram.length/HIST_WIDTH); 
				y = histogram[index]==0?0:(int)(HIST_HEIGHT*Math.log(histogram[index])/max);
				if (y>HIST_HEIGHT)
					y = HIST_HEIGHT;
				ip.drawLine(i+XMARGIN, YMARGIN+HIST_HEIGHT, i+XMARGIN, YMARGIN+HIST_HEIGHT-y);
			}
		} else {
			double xscale = (double)HIST_WIDTH/histogram.length; 
			for (int i=0; i<histogram.length; i++) {
				long value = histogram[i];
				if (value>0L) {
					int y = (int)(HIST_HEIGHT*Math.log(value)/max);
					if (y>HIST_HEIGHT) y = HIST_HEIGHT;
					int x = (int)(i*xscale)+XMARGIN;
					ip.drawLine(x, YMARGIN+HIST_HEIGHT, x, YMARGIN+HIST_HEIGHT-y);
				}
			}
		}
		ip.setColor(Color.black);
	}
		
	/**
	 * Draw text.
	 *
	 * @param ip the ip
	 * @param x the x
	 * @param y the y
	 * @param fixedRange the fixed range
	 */
	void drawText(ImageProcessor ip, int x, int y, boolean fixedRange) {
		ip.setFont(new Font("SansSerif",Font.PLAIN,12));
		ip.setAntialiasedText(true);
		double hmin = cal.getCValue(stats.histMin);
		double hmax = cal.getCValue(stats.histMax);
		double range = hmax-hmin;
		if (fixedRange&&!cal.calibrated()&&hmin==0&&hmax==255)
			range = 256;
		ip.drawString(d2s(hmin), x - 4, y);
		ip.drawString(d2s(hmax), x + HIST_WIDTH - getWidth(hmax, ip) + 10, y);
        
		double binWidth = range/stats.nBins;
		binWidth = Math.abs(binWidth);
		boolean showBins = binWidth!=1.0 || !fixedRange;
		int col1 = XMARGIN + 5;
		int col2 = XMARGIN + HIST_WIDTH/2;
		int row1 = y+25;
		if (showBins) row1 -= 8;
		int row2 = row1 + 15;
		int row3 = row2 + 15;
		int row4 = row3 + 15;
		long count = stats.longPixelCount>0?stats.longPixelCount:stats.pixelCount;
		String modeCount = " (" + stats.maxCount + ")";
		if (modeCount.length()>12) modeCount = "";
		ip.drawString("Count: " + count, col1, row1);
		ip.drawString("Mean: " + d2s(stats.mean), col1, row2);
		ip.drawString("StdDev: " + d2s(stats.stdDev), col1, row3);
		ip.drawString("Mode: " + d2s(stats.dmode) + modeCount, col2, row3);
		ip.drawString("Min: " + d2s(stats.min), col2, row1);
		ip.drawString("Max: " + d2s(stats.max), col2, row2);
		
		if (showBins) {
			ip.drawString("Bins: " + d2s(stats.nBins), col1, row4);
			ip.drawString("Bin Width: " + d2s(binWidth), col2, row4);
		}
	}

	/*
	String d2s(double d) {
		if (d==Double.MAX_VALUE||d==-Double.MAX_VALUE)
			return "0";
		else if (Double.isNaN(d))
			return("NaN");
		else if (Double.isInfinite(d))
			return("Infinity");
		else if ((int)d==d)
			return ResultsTable.d2s(d,0);
		else
			return ResultsTable.d2s(d,decimalPlaces);
	}
	*/
	
	/**
	 * D 2 s.
	 *
	 * @param d the d
	 * @return the string
	 */
	private String d2s(double d) {
		if ((int)d==d)
			return IJ.d2s(d, 0);
		else
    		return IJ.d2s(d, 3, 8);
    }
	
	/**
	 * Gets the width.
	 *
	 * @param d the d
	 * @param ip the ip
	 * @return the width
	 */
	int getWidth(double d, ImageProcessor ip) {
		return ip.getStringWidth(d2s(d));
	}
	
	/**
	 *  Returns the histogram values as a ResultsTable.
	 *
	 * @return the results table
	 */
	public ResultsTable getResultsTable() {
		ResultsTable rt = new ResultsTable();
		rt.showRowNumbers(false);
		String vheading = stats.binSize==1.0?"value":"bin start";
		if (cal.calibrated() && !cal.isSigned16Bit()) {
			for (int i=0; i<stats.nBins; i++) {
				rt.setValue("level", i, i);
				rt.setValue(vheading, i, cal.getCValue(stats.histMin+i*stats.binSize));
				rt.setValue("count", i, histogram[i]);
			}
			rt.setDecimalPlaces(0, 0);
			rt.setDecimalPlaces(1, digits);
			rt.setDecimalPlaces(2, 0);
		} else {
			for (int i=0; i<stats.nBins; i++) {
				rt.setValue(vheading, i, cal.getCValue(stats.histMin+i*stats.binSize));
				rt.setValue("count", i, histogram[i]);
			}
			rt.setDecimalPlaces(0, digits);
			rt.setDecimalPlaces(1, 0);
		}
		return rt;
	}

	/**
	 * Show list.
	 */
	protected void showList() {
		ResultsTable rt = getResultsTable();
		rt.show(getTitle());
	}
	
	/**
	 * Copy to clipboard.
	 */
	protected void copyToClipboard() {
		Clipboard systemClipboard = null;
		try {systemClipboard = getToolkit().getSystemClipboard();}
		catch (Exception e) {systemClipboard = null; }
		if (systemClipboard==null)
			{IJ.error("Unable to copy to Clipboard."); return;}
		IJ.showStatus("Copying histogram values...");
		CharArrayWriter aw = new CharArrayWriter(stats.nBins*4);
		PrintWriter pw = new PrintWriter(aw);
		for (int i=0; i<stats.nBins; i++)
			pw.print(ResultsTable.d2s(cal.getCValue(stats.histMin+i*stats.binSize), digits)+"\t"+histogram[i]+"\n");
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
	}
	
	/**
	 * Replot.
	 */
	void replot() {
		ImageProcessor ip = this.imp.getProcessor();
		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.setColor(Color.white);
		ip.setRoi(frame.x-1, frame.y, frame.width+2, frame.height);
		ip.fill();
		ip.resetRoi();
		ip.setColor(Color.black);
		if (logScale) {
			drawLogPlot(yMax>0?yMax:newMaxCount, ip);
			drawPlot(yMax>0?yMax:newMaxCount, ip);
		} else
			drawPlot(yMax>0?yMax:newMaxCount, ip);
		this.imp.updateAndDraw();
	}
	
	/*
	void rescale() {
		Graphics g = img.getGraphics();
		plotScale *= 2;
		if ((newMaxCount/plotScale)<50) {
			plotScale = 1;
			frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
			g.setColor(Color.white);
			g.fillRect(frame.x, frame.y, frame.width, frame.height);
			g.setColor(Color.black);
		}
		drawPlot(newMaxCount/plotScale, g);
		//ImageProcessor ip = new ColorProcessor(img);
		//this.imp.setProcessor(null, ip);
		this.imp.setImage(img);
	}
	*/
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==live)
			toggleLiveMode();
		else if (b==rgb)
			changeChannel();
		else if (b==list)
			showList();
		else if (b==copy)
			copyToClipboard();
		else if (b==log) {
			logScale = !logScale;
			replot();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
	/**
	 * Gets the histogram.
	 *
	 * @return the histogram
	 */
	public int[] getHistogram() {
		int[] hist = new int[histogram.length];
		for (int i=0; i<histogram.length; i++)
			hist[i] = (int)histogram[i];
		return hist;
	}

	/**
	 * Gets the x values.
	 *
	 * @return the x values
	 */
	public double[] getXValues() {
		double[] values = new double[stats.nBins];
		for (int i=0; i<stats.nBins; i++)
			values[i] = cal.getCValue(stats.histMin+i*stats.binSize);
		return values;
	}

	/**
	 * Toggle live mode.
	 */
	private void toggleLiveMode() {
		if (liveMode())
			removeListeners();
		else
			enableLiveMode();
	}
	
	/**
	 * Change channel.
	 */
	private void changeChannel() {
		ImagePlus imp = WindowManager.getImage(srcImageID);
		if (imp==null || imp.getType()!=ImagePlus.COLOR_RGB) {
			channel = INTENSITY;
			return;
		} else {
			channel++;
			if (channel>BLUE) channel=INTENSITY;
			showHistogram(imp, 256);
			String name = this.imp.getTitle();
			if (name.startsWith("Red ")) name=name.substring(4);
			else if (name.startsWith("Green ")) name=name.substring(6);
			else if (name.startsWith("Blue ")) name=name.substring(5);
			switch (channel) {
				case INTENSITY: this.imp.setTitle(name); break;
				case RED: this.imp.setTitle("Red "+name); break;
				case GREEN: this.imp.setTitle("Green "+name); break;
				case BLUE: this.imp.setTitle("Blue "+name); break;
			}
		}
	}

	/**
	 * Live mode.
	 *
	 * @return true, if successful
	 */
	private boolean liveMode() {
		return live!=null && live.getForeground()==Color.red;
	}

	/**
	 * Enable live mode.
	 */
	private void enableLiveMode() {
		if (bgThread==null) {
			srcImp = WindowManager.getImage(srcImageID);
			if (srcImp==null) return;
			bgThread = new Thread(this, "Live Histogram");
			bgThread.setPriority(Math.max(bgThread.getPriority()-3, Thread.MIN_PRIORITY));
			bgThread.start();
			imageUpdated(srcImp);
		}
		createListeners();
		if (srcImp!=null)
			imageUpdated(srcImp);
	}
	
	/* (non-Javadoc)
	 * @see ij.ImageListener#imageOpened(ij.ImagePlus)
	 */
	// Unused
	public void imageOpened(ImagePlus imp) {
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageUpdated(ij.ImagePlus)
	 */
	// This listener is called if the source image content is changed
	public synchronized void imageUpdated(ImagePlus imp) {
		if (imp==srcImp) { 
			doUpdate = true;
			notify();
		}
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.RoiListener#roiModified(ij.ImagePlus, int)
	 */
	public synchronized void roiModified(ImagePlus img, int id) {
		if (img==srcImp) {
			doUpdate=true;
			notify();
		}
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageClosed(ij.ImagePlus)
	 */
	// If either the source image or this image are closed, exit
	public void imageClosed(ImagePlus imp) {
		if (imp==srcImp || imp==this.imp) {
			if (bgThread!=null)
				bgThread.interrupt();
			bgThread = null;
			removeListeners();
			srcImp = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	// the background thread for live plotting.
	public void run() {
		while (true) {
			if (doUpdate && srcImp!=null) {
				if (srcImp.getRoi()!=null)
					IJ.wait(50);	//delay to make sure the roi has been updated
				if (srcImp!=null) {
					if (srcImp.getBitDepth()==16 && ImagePlus.getDefault16bitRange()!=0)
						showHistogram(srcImp, 256, 0, Math.pow(2,ImagePlus.getDefault16bitRange())-1);
					else
						showHistogram(srcImp, 256);
				}
			}
			synchronized(this) {
				if (doUpdate) {
					doUpdate = false;		//and loop again
				} else {
					try {wait();}	//notify wakes up the thread
					catch(InterruptedException e) { //interrupted tells the thread to exit
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Creates the listeners.
	 */
	private void createListeners() {
		if (srcImp==null)
			return;
		ImagePlus.addImageListener(this);
		Roi.addRoiListener(this);
		if (live!=null) {
			Font font = live.getFont();
			live.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
			live.setForeground(Color.red);
		}
	}
	
	/**
	 * Removes the listeners.
	 */
	private void removeListeners() {
		if (srcImp==null)
			return;
		ImagePlus.removeImageListener(this);
		Roi.removeRoiListener(this);
		if (live!=null) {
			Font font = live.getFont();
			live.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
			live.setForeground(Color.black);
		}
	}

}

