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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

// TODO: Auto-generated Javadoc
/** This plugin implements the File/Page Setup and File/Print commands. */
public class Printer implements PlugInFilter, Printable {
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The scaling. */
	private static double scaling = 100.0;
	
	/** The draw border. */
	private static boolean drawBorder;
	
	/** The center. */
	private static boolean center = true;
	
	/** The label. */
	private static boolean label;
	
	/** The print selection. */
	private static boolean printSelection;
	
	/** The rotate. */
	private static boolean rotate;
	
	/** The actual size. */
	private static boolean actualSize;
	
	/** The font size. */
	private static int fontSize = 12;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("setup"))
			{pageSetup(); return DONE;}
		this.imp = imp;
		IJ.register(Printer.class);
		return DOES_ALL+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		print(imp);
	}
	
	/**
	 * Page setup.
	 */
	void pageSetup() {
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		boolean isRoi = roi!=null && roi.isArea();
		GenericDialog gd = new GenericDialog("Page Setup");
		gd.addNumericField("Scale:", scaling, 0, 3, "%");
		gd.addCheckbox("Draw border", drawBorder);
		gd.addCheckbox("Center on page", center);
		gd.addCheckbox("Print title", label);
		if (isRoi)
			gd.addCheckbox("Selection only", printSelection);
		gd.addCheckbox("Rotate 90"+IJ.degreeSymbol, rotate);
		gd.addCheckbox("Print_actual size", actualSize);
		if (imp!=null)
			gd.enableYesNoCancel(" OK ", "Print");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		scaling = gd.getNextNumber();
		if (scaling<5.0) scaling = 5;
		drawBorder = gd.getNextBoolean();
		center = gd.getNextBoolean();
		label = gd.getNextBoolean();
		if (isRoi)
			printSelection = gd.getNextBoolean();
		else
			printSelection = false;
		rotate = gd.getNextBoolean();
		actualSize = gd.getNextBoolean();
		if (!gd.wasOKed() && imp!=null) {
			this.imp = imp;
			print(imp);
		}
	}

	/**
	 * Prints the.
	 *
	 * @param imp the imp
	 */
	void print(ImagePlus imp) {
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(this);
		//pj.pageDialog(pj.defaultPage());
		if (IJ.macroRunning() || pj.printDialog()) {
			imp.startTiming();
			try {pj.print(); }
			catch (PrinterException e) {
				IJ.log(""+e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		if (pageIndex != 0) return NO_SUCH_PAGE;
		Roi roi = imp.getRoi();
		ImagePlus imp2 = imp;
		if (imp2.getOverlay()!=null && !imp2.getHideOverlay()) {
			imp2.deleteRoi();
			imp2 = imp2.flatten();
		}
		ImageProcessor ip = imp2.getProcessor();
		if (printSelection && roi!=null && roi.isArea() )
			ip.setRoi(roi);
			ip = ip.crop();
		if (rotate)
			ip = ip.rotateLeft();
		//new ImagePlus("ip", ip.duplicate()).show();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int margin = 0;
		if (drawBorder) margin = 1;
		double scale = scaling/100.0;
		int dstWidth = (int)(width*scale);
		int dstHeight = (int)(height*scale);
		int pageX = (int)pf.getImageableX();
		int pageY = (int)pf.getImageableY();
		int dstX = pageX+margin;
		int dstY = pageY+margin;
		Image img = ip.createImage();
		double pageWidth = pf.getImageableWidth()-2*margin;
		double pageHeight = pf.getImageableHeight()-2*margin;
		if (label && pageWidth-dstWidth<fontSize+5) {
			dstY += fontSize+5;
			pageHeight -= fontSize+5;
		}
		if (actualSize) {
			Calibration cal = imp.getCalibration();
			int unitIndex = ImageProperties.getUnitIndex(cal.getUnit());
			if (unitIndex!=ImageProperties.OTHER_UNIT) {
				double unitsPerCm = ImageProperties.getUnitsPerCm(unitIndex);
				double widthInCm = width*cal.pixelWidth/unitsPerCm;
				double heightInCm = height*cal.pixelHeight/unitsPerCm;
				dstWidth = (int)((widthInCm*(72*0.3937))*scale);
				dstHeight = (int)((heightInCm*(72*0.3937))*scale);
			}
			if (center && dstWidth<pageWidth && dstHeight<pageHeight) {
				dstX += (pageWidth-dstWidth)/2;
				dstY += (pageHeight-dstHeight)/2;
			}
		} else if (dstWidth>pageWidth || dstHeight>pageHeight) {
			// scale to fit page
			double hscale = pageWidth/dstWidth;
			double vscale = pageHeight/dstHeight;
			double scale2 = hscale<=vscale?hscale:vscale;
			dstWidth = (int)(dstWidth*scale2);
			dstHeight = (int)(dstHeight*scale2);
		} else if (center) {
			dstX += (pageWidth-dstWidth)/2;
			dstY += (pageHeight-dstHeight)/2;
		}
		g.drawImage(img, 
			dstX, dstY, dstX+dstWidth, dstY+dstHeight,
			0, 0, width, height, 
			null);
		if (drawBorder)
			g.drawRect(dstX-1, dstY-1, dstWidth+1, dstHeight+1);
		if (label) {
			g.setFont(new Font("SanSerif", Font.PLAIN, fontSize));
			g.setColor(Color.black);
			g.drawString(imp.getTitle(), pageX+5, pageY+fontSize);
		}
		return PAGE_EXISTS;
	}

}
