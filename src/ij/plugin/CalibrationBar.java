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
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.awt.image.IndexColorModel;

// TODO: Auto-generated Javadoc
/** This plugin implements the Analyze/Tools/Calibration Bar command.
	Bob Dougherty, OptiNav, Inc., 4/14/2002
	Based largely on HistogramWindow.java by Wayne Rasband.
	July 2002: Modified by Daniel Marsh and renamed CalibrationBar.
	January 2013: Displays calibration bar as an overlay.
*/

public class CalibrationBar implements PlugIn {
	
	/** The Constant BAR_LENGTH. */
	final static int BAR_LENGTH = 128;
	
	/** The Constant BAR_THICKNESS. */
	final static int BAR_THICKNESS = 12;
	
	/** The Constant XMARGIN. */
	final static int XMARGIN = 10;
	
	/** The Constant YMARGIN. */
	final static int YMARGIN = 10;
	
	/** The Constant WIN_HEIGHT. */
	final static int WIN_HEIGHT = BAR_LENGTH;
	
	/** The Constant BOX_PAD. */
	final static int BOX_PAD = 0;
	
	/** The n bins. */
	static int nBins = 256;
	
	/** The Constant colors. */
	static final String[] colors = {"White","Light Gray","Dark Gray","Black","Red","Green","Blue","Yellow","None"};
	
	/** The Constant locations. */
	static final String[] locations = {"Upper Right","Lower Right","Lower Left", "Upper Left", "At Selection"};
	
	/** The Constant AT_SELECTION. */
	static final int UPPER_RIGHT=0, LOWER_RIGHT=1, LOWER_LEFT=2, UPPER_LEFT=3, AT_SELECTION=4;

	/** The fill color. */
	static String fillColor = colors[0];
	
	/** The text color. */
	static String textColor = colors[3];
	
	/** The location. */
	static String location = locations[UPPER_RIGHT];
	
	/** The zoom. */
	static double zoom = 1;
	
	/** The num labels. */
	static int numLabels = 5;
	
	/** The font size. */
	static int fontSize = 12;
	
	/** The decimal places. */
	static int decimalPlaces = 0;
	
	/** The imp. */
	ImagePlus imp;
	
	/** The gd. */
	LiveDialog gd;

	/** The stats. */
	ImageStatistics stats;
	
	/** The cal. */
	Calibration cal;
	
	/** The histogram. */
	int[] histogram;
	
	/** The img. */
	Image img;
	
	/** The un insert. */
	Button setup, redraw, insert, unInsert;
	
	/** The sw. */
	Checkbox ne,nw,se,sw;
	
	/** The loc group. */
	CheckboxGroup locGroup;
	
	/** The note. */
	Label value, note;
	
	/** The new max count. */
	int newMaxCount;
	
	/** The log scale. */
	boolean logScale;
	
	/** The win width. */
	int win_width;
	
	/** The user padding. */
	int userPadding = 0;
	
	/** The font height. */
	int fontHeight = 0;
	
	/** The bold text. */
	boolean boldText;
	
	/** The flatten. */
	boolean flatten;
	
	/** The backup pixels. */
	Object backupPixels;
	
	/** The byte storage. */
	byte[] byteStorage;
	
	/** The int storage. */
	int[] intStorage;
	
	/** The short storage. */
	short[] shortStorage;
	
	/** The float storage. */
	float[] floatStorage;
	
	/** The box outline color. */
	String boxOutlineColor = colors[8];
	
	/** The bar outline color. */
	String barOutlineColor = colors[3];
	
	/** The ip. */
	ImageProcessor ip;
	
	/** The field names. */
	String[] fieldNames = null;
	
	/** The inset pad. */
	int insetPad;
	
	/** The decimal places changed. */
	boolean decimalPlacesChanged;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		imp = IJ.getImage();
		if (imp.getBitDepth()==24 || imp.getCompositeMode()==IJ.COMPOSITE) {
			IJ.error("Calibration Bar", "RGB and composite images are not supported");
			return;
		}
		if (imp.getRoi()!=null && imp.getRoi().isArea())
			location = locations[AT_SELECTION];
		else if (location.equals(locations[AT_SELECTION]))
			location = locations[UPPER_RIGHT];
		ImageCanvas ic = imp.getCanvas();
		double mag = (ic!=null)?ic.getMagnification():1.0;
		if (zoom<=1 && mag<1)
			zoom = (double) 1.0/mag;
		insetPad = imp.getWidth()/50;
		if (insetPad<4)
			insetPad = 4;
		updateColorBar();
		if (IJ.isMacro()) {
			flatten = true;
			fillColor = colors[0];
			textColor = colors[3];
			location = locations[UPPER_RIGHT];
			zoom = 1;
			numLabels = 5;
			fontSize = 12;
			decimalPlaces = 0;
		}
		if (!showDialog()) {
			imp.setOverlay(null);
			return;
		}
		updateColorBar();
		if (flatten) {
			imp.deleteRoi();
			IJ.wait(100);
			ImagePlus imp2 = imp.flatten();
			imp2.setTitle(imp.getTitle()+" with bar");
			imp.setOverlay(null);
			imp2.show();
		}
	}

	/**
	 * Update color bar.
	 */
	private void updateColorBar() {
		Roi roi = imp.getRoi();
		if (roi!=null &&  location.equals(locations[AT_SELECTION])) {
			Rectangle r = roi.getBounds();
			drawBarAsOverlay(imp, r.x, r.y);
		} else if ( location.equals(locations[UPPER_LEFT]))
			drawBarAsOverlay(imp, insetPad, insetPad);
		else if (location.equals(locations[UPPER_RIGHT])) {
			calculateWidth();
			drawBarAsOverlay(imp, imp.getWidth()-insetPad-win_width, insetPad);
		} else if (location.equals(locations[LOWER_LEFT]) )
			drawBarAsOverlay(imp, insetPad,imp.getHeight() - (int)(WIN_HEIGHT*zoom + 2*(int)(YMARGIN*zoom)) - (int)(insetPad*zoom));
		else if(location.equals(locations[LOWER_RIGHT])) {
			calculateWidth();
			drawBarAsOverlay(imp, imp.getWidth()-win_width-insetPad,
				 imp.getHeight() - (int)(WIN_HEIGHT*zoom + 2*(int)(YMARGIN*zoom)) - insetPad);
		}
		this.imp.updateAndDraw();
	}

	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	private boolean showDialog() {
		gd = new LiveDialog("Calibration Bar");
		gd.addChoice("Location:", locations, location);
		gd.addChoice("Fill color: ", colors, fillColor);
		gd.addChoice("Label color: ", colors, textColor);
		gd.addNumericField("Number of labels:", numLabels, 0);
		gd.addNumericField("Decimal places:", decimalPlaces, 0);
		gd.addNumericField("Font size:", fontSize, 0);
		gd.addNumericField("Zoom factor:", zoom, 1);
		String[] labels = {"Bold text", "Overlay"};
		boolean[] states = {boldText, !flatten};
		gd.setInsets(10, 30, 0);
		gd.addCheckboxGroup(1, 2, labels, states);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		location = gd.getNextChoice();
		fillColor = gd.getNextChoice();
		textColor = gd.getNextChoice();
		numLabels = (int)gd.getNextNumber();
		decimalPlaces = (int)gd.getNextNumber();
		fontSize = (int)gd.getNextNumber();
		zoom = (double)gd.getNextNumber();
		boldText = gd.getNextBoolean();
		flatten = !gd.getNextBoolean();
		return true;
	}

	/**
	 * Draw bar as overlay.
	 *
	 * @param imp the imp
	 * @param x the x
	 * @param y the y
	 */
	private void drawBarAsOverlay(ImagePlus imp, int x, int y) {
		Roi roi = imp.getRoi();
		if (roi!=null)
			imp.deleteRoi();
		stats = imp.getStatistics(Measurements.MIN_MAX, nBins);
		if (roi!=null)
			imp.setRoi(roi);
		histogram = stats.histogram;
		cal = imp.getCalibration();
		Overlay overlay = new Overlay();

		int maxTextWidth = addText(null, 0, 0);
		win_width = (int)(XMARGIN*zoom) + 5 + (int)(BAR_THICKNESS*zoom) + maxTextWidth + (int)((XMARGIN/2)*zoom);
		if (x==-1 && y==-1)
			return;	 // return if calculating width

		Color c = getColor(fillColor);
		if (c!=null) {
			Roi r = new Roi(x, y, win_width, (int)(WIN_HEIGHT*zoom + 2*(int)(YMARGIN*zoom)));
			r.setFillColor(c);
			overlay.add(r);
		}
		int xOffset = x;
		int yOffset = y;
		if (decimalPlaces == -1)
			decimalPlaces = Analyzer.getPrecision();
		x = (int)(XMARGIN*zoom) + xOffset;
		y = (int)(YMARGIN*zoom) + yOffset;
		addVerticalColorBar(overlay, x, y, (int)(BAR_THICKNESS*zoom), (int)(BAR_LENGTH*zoom) );
		addText(overlay, x + (int)(BAR_THICKNESS*zoom), y);
		c = getColor(boxOutlineColor);
		overlay.setIsCalibrationBar(true);
		if (imp.getCompositeMode()>0) {
			for (int i=0; i<overlay.size(); i++)
				overlay.get(i).setPosition(imp.getC(), 0, 0);
		}
		imp.setOverlay(overlay);
	}

	/**
	 * Adds the vertical color bar.
	 *
	 * @param overlay the overlay
	 * @param x the x
	 * @param y the y
	 * @param thickness the thickness
	 * @param length the length
	 */
	private void addVerticalColorBar(Overlay overlay, int x, int y, int thickness, int length) {
		int width = thickness;
		int height = length;
		byte[] rLUT,gLUT,bLUT;
		int mapSize = 0;
		java.awt.image.ColorModel cm = imp.getProcessor().getCurrentColorModel();
		if (cm instanceof IndexColorModel) {
			IndexColorModel m = (IndexColorModel)cm;
			mapSize = m.getMapSize();
			rLUT = new byte[mapSize];
			gLUT = new byte[mapSize];
			bLUT = new byte[mapSize];
			m.getReds(rLUT);
			m.getGreens(gLUT);
			m.getBlues(bLUT);
		} else {
			mapSize = 256;
			rLUT = new byte[mapSize];
			gLUT = new byte[mapSize];
			bLUT = new byte[mapSize];
			for (int i = 0; i < mapSize; i++) {
				rLUT[i] = (byte)i;
				gLUT[i] = (byte)i;
				bLUT[i] = (byte)i;
			}
		}
		double colors = mapSize;
		int start = 0;
		ImageProcessor ipOrig =imp.getProcessor();
		if (ipOrig instanceof ByteProcessor) {
			int min = (int)ipOrig.getMin();
			if (min<0) min = 0;
			int max = (int)ipOrig.getMax();
			if (max>255) max = 255;
			colors = max-min+1;
			start = min;
		}
		for (int i = 0; i<(int)(BAR_LENGTH*zoom); i++) {
			int iMap = start + (int)Math.round((i*colors)/(BAR_LENGTH*zoom));
			if (iMap>=mapSize)
				iMap =mapSize - 1;
			int j = (int)(BAR_LENGTH*zoom) - i - 1;
			Line line = new Line(x, j+y, thickness+x, j+y);
			line.setStrokeColor(new Color(rLUT[iMap]&0xff, gLUT[iMap]&0xff, bLUT[iMap]&0xff));
			line.setStrokeWidth(1.0001);
			overlay.add(line);
		}

		Color c = getColor(barOutlineColor);
		if (c!=null) {
			Roi r = new Roi(x, y, width, height);
			r.setStrokeColor(c);
			r.setStrokeWidth(1.0);
			overlay.add(r);
		}
	}

	/**
	 * Adds the text.
	 *
	 * @param overlay the overlay
	 * @param x the x
	 * @param y the y
	 * @return the int
	 */
	private int addText(Overlay overlay, int x, int y) {

		Color c = getColor(textColor);
		if (c == null)
			return 0;
		double hmin = cal.getCValue(stats.histMin);
		double hmax = cal.getCValue(stats.histMax);
		double barStep = (double)(BAR_LENGTH*zoom) ;
		if (numLabels > 2)
			barStep /= (numLabels - 1);

		int fontType = boldText?Font.BOLD:Font.PLAIN;
		Font font = null;
		if (fontSize<9)
			font = new Font("SansSerif", fontType, 9);
		else
			font = new Font("SansSerif", fontType, (int)( fontSize*zoom));
		int maxLength = 0;

		//Blank offscreen image for font metrics
		Image img = GUI.createBlankImage(128, 64);
		Graphics g = img.getGraphics();
		FontMetrics metrics = g.getFontMetrics(font);
		fontHeight = metrics.getHeight();

		for (int i = 0; i < numLabels; i++) {
			double yLabelD = (int)(YMARGIN*zoom + BAR_LENGTH*zoom - i*barStep - 1);
			int yLabel = (int)(Math.round( y + BAR_LENGTH*zoom - i*barStep - 1));
			Calibration cal = imp.getCalibration();
			//s = cal.getValueUnit();
			ImageProcessor ipOrig = imp.getProcessor();
			double min = ipOrig.getMin();
			double max = ipOrig.getMax();
			if (ipOrig instanceof ByteProcessor) {
				if (min<0) min = 0;
				if (max>255) max = 255;
			}
			double grayLabel = min + (max-min)/(numLabels-1) * i;
			if (cal.calibrated()) {
				grayLabel = cal.getCValue(grayLabel);
				double cmin = cal.getCValue(min);
				double cmax = cal.getCValue(max);
				if (!decimalPlacesChanged && decimalPlaces==0 && ((int)cmax!=cmax||(int)cmin!=cmin))
					decimalPlaces = 2;
			}
			if (overlay!=null) {
				TextRoi label = new TextRoi(d2s(grayLabel), x + 5, yLabel + fontHeight/2, font);
				label.setStrokeColor(c);
				overlay.add(label);
			}
			int iLength = metrics.stringWidth(d2s(grayLabel));
			if (iLength > maxLength)
				maxLength = iLength;
		}
		return maxLength;
	}

	/**
	 * D 2 s.
	 *
	 * @param d the d
	 * @return the string
	 */
	String d2s(double d) {
			return IJ.d2s(d,decimalPlaces);
	}

	/**
	 * Gets the font height.
	 *
	 * @return the font height
	 */
	int getFontHeight() {
		Image img = GUI.createBlankImage(64, 64); //dummy version to get fontHeight
		Graphics g = img.getGraphics();
		int fontType = boldText?Font.BOLD:Font.PLAIN;
		Font font = new Font("SansSerif", fontType, (int) (fontSize*zoom) );
		FontMetrics metrics = g.getFontMetrics(font);
		return	metrics.getHeight();
	}

	/**
	 * Gets the color.
	 *
	 * @param color the color
	 * @return the color
	 */
	Color getColor(String color) {
		Color c = Color.white;
		if (color.equals(colors[1]))
			c = Color.lightGray;
		else if (color.equals(colors[2]))
			c = Color.darkGray;
		else if (color.equals(colors[3]))
			c = Color.black;
		else if (color.equals(colors[4]))
			c = Color.red;
		else if (color.equals(colors[5]))
			c = Color.green;
		else if (color.equals(colors[6]))
			c = Color.blue;
		else if (color.equals(colors[7]))
			c = Color.yellow;
		else if (color.equals(colors[8]))
			c = null;
		return c;
	}	 

	/**
	 * Calculate width.
	 */
	void calculateWidth() {
		drawBarAsOverlay(imp, -1, -1);
	}
		
	/**
	 * The Class LiveDialog.
	 */
	class LiveDialog extends GenericDialog {

		/**
		 * Instantiates a new live dialog.
		 *
		 * @param title the title
		 */
		LiveDialog(String title) {
			super(title);
		}

		/* (non-Javadoc)
		 * @see ij.gui.GenericDialog#textValueChanged(java.awt.event.TextEvent)
		 */
		public void textValueChanged(TextEvent e) {

			if (fieldNames == null) {
				fieldNames = new String[4];
				for(int i=0;i<4;i++)
					fieldNames[i] = ((TextField)numberField.elementAt(i)).getName();
			}

			TextField tf = (TextField)e.getSource();
			String name = tf.getName();
			String value = tf.getText();

			if (value.equals(""))
				return;

			int i=0;
			boolean needsRefresh = false;

			if (name.equals(fieldNames[0])) {

				i = getValue( value ).intValue() ;
				if(i<1)
					return;
				else {
					needsRefresh = true;
					numLabels = i;
				}
			} else if (name.equals(fieldNames[1])) {
				i = getValue( value ).intValue() ;
				if (i<0)
					return;
				else {
					needsRefresh = true;
					decimalPlaces = i;
					decimalPlacesChanged = true;
				}

			} else if (name.equals(fieldNames[2])) {
				i = getValue( value ).intValue() ;
				if(i<1)
					return;
				else {
					needsRefresh = true;
					fontSize = i;

				}

			} else if (name.equals(fieldNames[3])) {
				double d = 0;
				d = getValue( "0" + value ).doubleValue() ;
				if(d<=0)
					return;
				else {
					needsRefresh = true;
					zoom = d;
				}
			}

			if (needsRefresh)
				updateColorBar();
			return;
		}

		/* (non-Javadoc)
		 * @see ij.gui.GenericDialog#itemStateChanged(java.awt.event.ItemEvent)
		 */
		public void itemStateChanged(ItemEvent e) {
			location = ( (Choice)(choice.elementAt(0)) ).getSelectedItem();
			fillColor = ( (Choice)(choice.elementAt(1)) ).getSelectedItem();
			textColor = ( (Choice)(choice.elementAt(2)) ).getSelectedItem();
			boldText = ( (Checkbox)(checkbox.elementAt(0)) ).getState();
			flatten = !( (Checkbox)(checkbox.elementAt(1)) ).getState();
			updateColorBar();
		}

	} //LiveDialog inner class

}
