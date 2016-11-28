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

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.Prefs;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/** This class implements the Analyze/Plot Profile command.
* @author Michael Schmid
* @author Wayne Rasband
*/
public class PlotWindow extends ImageWindow implements ActionListener, 
	ClipboardOwner, ImageListener, RoiListener, Runnable {

	/** Display points using a circle 5 pixels in diameter. */
	public static final int CIRCLE = 0;
	/** Display points using an X-shaped mark. */
	public static final int X = 1;
	/** Display points using an box-shaped mark. */
	public static final int BOX = 3;
	/** Display points using an tiangular mark. */
	public static final int TRIANGLE = 4;
	/** Display points using an cross-shaped mark. */
	public static final int CROSS = 5;
	/** Connect points with solid lines. */
	public static final int LINE = 2;

	/** The Constant WIDTH. */
	private static final int WIDTH = 450;
	
	/** The Constant HEIGHT. */
	private static final int HEIGHT = 200;
	
	/** The Constant PLOT_WIDTH. */
	private static final String PLOT_WIDTH = "pp.width";
	
	/** The Constant PLOT_HEIGHT. */
	private static final String PLOT_HEIGHT = "pp.height";
	
	/** The Constant OPTIONS. */
	private static final String OPTIONS = "pp.options";
	
	/** The Constant SAVE_X_VALUES. */
	private static final int SAVE_X_VALUES = 1;
	
	/** The Constant AUTO_CLOSE. */
	private static final int AUTO_CLOSE = 2;
	
	/** The Constant LIST_VALUES. */
	private static final int LIST_VALUES = 4;
	
	/** The Constant INTERPOLATE. */
	private static final int INTERPOLATE = 8;
	
	/** The Constant NO_GRID_LINES. */
	private static final int NO_GRID_LINES = 16;

	/** The live. */
	private Button list, save, copy, live;
	
	/** The coordinates. */
	private Label coordinates;
	
	/** The default directory. */
	private static String defaultDirectory = null;
	
	/** The options. */
	private static int options;
	
	/** The default digits. */
	private int defaultDigits = -1;
	
	/** The mark size. */
	private int markSize = 5;
	
	/** The static plot. */
	private static Plot staticPlot;
	
	/** The plot. */
	private Plot plot;
	
	/** The blank label. */
	private String blankLabel = "                      ";
	
	/** The plot maker. */
	private PlotMaker plotMaker;
	
	/** The src imp. */
	private ImagePlus srcImp;		// the source image for live plotting
	
	/** The bg thread. */
	private Thread bgThread;		// thread for plotting (in the background)
	
	/** The do update. */
	private boolean doUpdate;	// tells the background thread to update

	
	/** Save x-values only. To set, use Edit/Options/
		Profile Plot Options. */
	public static boolean saveXValues;
	
	/** Automatically close window after saving values. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean autoClose;
	
	/** The width of the plot in pixels. */
	public static int plotWidth = WIDTH;

	/** The height of the plot in pixels. */
	public static int plotHeight = HEIGHT;

	/** Display the XY coordinates in a separate window. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean listValues;

	/** Interpolate line profiles. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean interpolate;

	/**  Add grid lines to plots. */
	public static boolean noGridLines;

	// static initializer
	static {
		options = Prefs.getInt(OPTIONS, SAVE_X_VALUES);
		saveXValues = (options&SAVE_X_VALUES)!=0;
		autoClose = (options&AUTO_CLOSE)!=0;
		listValues = (options&LIST_VALUES)!=0;
		plotWidth = Prefs.getInt(PLOT_WIDTH, WIDTH);
		plotHeight = Prefs.getInt(PLOT_HEIGHT, HEIGHT);
		interpolate = (options&INTERPOLATE)==0; // 0=true, 1=false
		noGridLines = (options&NO_GRID_LINES)!=0; 
   }

 	/**
	  * Instantiates a new plot window.
	  *
	  * @param title the title
	  * @param xLabel the x label
	  * @param yLabel the y label
	  * @param xValues the x values
	  * @param yValues the y values
	  * @deprecated replaced by the Plot class.
	  */
	public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		super(createImage(title, xLabel, yLabel, xValues, yValues));
		plot = staticPlot;
	}

 	/**
	  * Instantiates a new plot window.
	  *
	  * @param title the title
	  * @param xLabel the x label
	  * @param yLabel the y label
	  * @param xValues the x values
	  * @param yValues the y values
	  * @deprecated replaced by the Plot class.
	  */
	public PlotWindow(String title, String xLabel, String yLabel, double[] xValues, double[] yValues) {
		this(title, xLabel, yLabel, Tools.toFloat(xValues), Tools.toFloat(yValues));
	}
	
	/**
	 *  Creates a PlotWindow from a Plot object.
	 *
	 * @param plot the plot
	 */
	PlotWindow(Plot plot) {
		super(plot.getImagePlus());
		this.plot = plot;
		draw();
		//addComponentListener(this);
	}

	/**
	 *  Called by the constructor to generate the image the plot will be drawn on.
	 * 		This is a static method because constructors cannot call instance methods.
	 *
	 * @param title the title
	 * @param xLabel the x label
	 * @param yLabel the y label
	 * @param xValues the x values
	 * @param yValues the y values
	 * @return the image plus
	 */
	static ImagePlus createImage(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		staticPlot = new Plot(title, xLabel, yLabel, xValues, yValues);
		return new ImagePlus(title, staticPlot.getBlankProcessor());
	}
	
	/**
	 *  Sets the x-axis and y-axis range.
	 *
	 * @param xMin the x min
	 * @param xMax the x max
	 * @param yMin the y min
	 * @param yMax the y max
	 */
	public void setLimits(double xMin, double xMax, double yMin, double yMax) {
		plot.setLimits(xMin, xMax, yMin, yMax);
	}

	/** Adds a set of points to the plot or adds a curve if shape is set to LINE.
	* @param x			the x-coodinates
	* @param y			the y-coodinates
	* @param shape		CIRCLE, X, BOX, TRIANGLE, CROSS or LINE
	*/
	public void addPoints(float[] x, float[] y, int shape) {
		plot.addPoints(x, y, shape);
	}

	/**
	 *  Adds a set of points to the plot using double arrays.
	 * 		Must be called before the plot is displayed.
	 *
	 * @param x the x
	 * @param y the y
	 * @param shape the shape
	 */
	public void addPoints(double[] x, double[] y, int shape) {
		addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
	}
	
	/**
	 *  Adds vertical error bars to the plot.
	 *
	 * @param errorBars the error bars
	 */
	public void addErrorBars(float[] errorBars) {
		plot.addErrorBars(errorBars);
	}

	/**
	 *  Draws a label.
	 *
	 * @param x the x
	 * @param y the y
	 * @param label the label
	 */
	public void addLabel(double x, double y, String label) {
		plot.addLabel(x, y, label);
	}
	
	/**
	 *  Changes the drawing color. The frame and labels are
	 * 		always drawn in black.
	 *
	 * @param c the new color
	 */
	public void setColor(Color c) {
		plot.setColor(c);
	}

	/**
	 *  Changes the line width.
	 *
	 * @param lineWidth the new line width
	 */
	public void setLineWidth(int lineWidth) {
		plot.setLineWidth(lineWidth);
	}

	/**
	 *  Changes the font.
	 *
	 * @param font the font
	 */
	public void changeFont(Font font) {
		plot.changeFont(font);
	}

	/** Displays the plot. */
	public void draw() {
		Panel buttons = new Panel();
		int hgap = IJ.isMacOSX()?1:5;
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT,hgap,0));
		list = new Button(" List ");
		list.addActionListener(this);
		buttons.add(list);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		copy = new Button("Copy...");
		copy.addActionListener(this);
		buttons.add(copy);
		if (plot!=null && plot.getPlotMaker()!=null) {
			live = new Button("Live");
			live.addActionListener(this);
			buttons.add(live);
		}		
		coordinates = new Label("X=12345678, Y=12345678"); 
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		coordinates.setBackground(new Color(220, 220, 220));
		buttons.add(coordinates);
		add(buttons);
		plot.draw();
		pack();
		coordinates.setText(blankLabel);
		ImageProcessor ip = plot.getProcessor();
		if ((ip instanceof ColorProcessor) && (imp.getProcessor() instanceof ByteProcessor))
			imp.setProcessor(null, ip);
		else
			imp.updateAndDraw();
		if (listValues)
			showList();
	}

	/**
	 * Gets the digits.
	 *
	 * @param n1 the n 1
	 * @param n2 the n 2
	 * @return the digits
	 */
	int getDigits(double n1, double n2) {
		if (Math.round(n1)==n1 && Math.round(n2)==n2)
			return 0;
		else {
			n1 = Math.abs(n1);
			n2 = Math.abs(n2);
			double n = n1<n2&&n1>0.0?n1:n2;
			double diff = Math.abs(n2-n1);
			if (diff>0.0 && diff<n) n = diff;			
			int digits = 1;
			if (n<10.0) digits = 2;
			if (n<0.01) digits = 3;
			if (n<0.001) digits = 4;
			if (n<0.0001) digits = 5;
			return digits;
		}
	}

	/**
	 *  Updates the graph X and Y values when the mouse is moved.
	 * 		Overrides mouseMoved() in ImageWindow. 
	 *
	 * @param x the x
	 * @param y the y
	 * @see ij.gui.ImageWindow#mouseMoved
	 */
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		if (plot!=null && plot.frame!=null && coordinates!=null) {
			String coords = plot.getCoordinates(x,y) + blankLabel;
			coordinates.setText(coords.substring(0, blankLabel.length()));
		}
	}
			
	/**
	 *  shows the data of the backing plot in a Textwindow with columns.
	 */
	void showList(){
		ResultsTable rt = getResultsTable();
		rt.show("Plot Values");
		if (autoClose) {
			imp.changes=false;
			close();
		}
	}
	
	/**
	 *  Returns the plot values as a ResultsTable.
	 *
	 * @return the results table
	 */
	public ResultsTable getResultsTable() {
		ResultsTable rt = new ResultsTable();
		rt.showRowNumbers(false);
		String[] headings = getHeadings();
		int max = 0;
		
		/** find the longest x-value data set */
		float[] column;
		for(int i = 0; i<plot.storedData.size(); i+=2){
			column = (float[])plot.storedData.get(i);
			int s = column.length;
			max = s>max?s:max;
		}
		
		// store values that will be saved
		ArrayList data = new ArrayList(plot.storedData);
		boolean ex_test = false;
		boolean ey_test = false;
		
		// includes vertical error bars
		if (plot.errorBars !=null)
			data.add(2, plot.errorBars);
			
		// includes horizontal error bars
		if (plot.xErrorBars !=null)
			data.add(3, plot.xErrorBars);
					
		boolean skipDuplicates = skipDuplicateXColumns();
		int n = data.size();
		for (int i=0; i<max; i++) {
			ey_test = plot.errorBars!=null;
			ex_test = plot.xErrorBars!=null;
			for (int j=0; j<n;) {
				if (saveXValues && !(j>1&&skipDuplicates)) {
					column = (float[])data.get(j);
					if (i<column.length)
						rt.setValue(headings[j], i, column[i]);
					else
						rt.setValue(headings[j], i, "");
				}
				j++;
				column = (float[])data.get(j);
				if (i<column.length)
					rt.setValue(headings[j], i, column[i]);
				else
					rt.setValue(headings[j], i, "");
				j++;
				if (ey_test){
					column = (float[])data.get(j);
					if (i<column.length)
						rt.setValue(headings[j], i, column[i]);
					else
						rt.setValue(headings[j], i, "");

					j++;
					ey_test=false;
				}
				if (ex_test){
					column = (float[])data.get(j);
					if (i<column.length)
						rt.setValue(headings[j], i, column[i]);
					else
						rt.setValue(headings[j], i, "");

					j++;
					ex_test=false;
				}
			}
		}
		int nColumns = rt.getLastColumn() + 1;
		for (int i=0; i<nColumns; i++)
			rt.setDecimalPlaces(i, getPrecision(rt.getColumn(i)));
		return rt;
	}
	
	/**
	 * Skip duplicate X columns.
	 *
	 * @return true, if successful
	 */
	private boolean skipDuplicateXColumns() {
		ArrayList data = plot.storedData;
		int sets = data.size()/2;
		if (sets<2)
			return false;
		float[] x0 = (float[])data.get(0);
		for (int i=1; i<sets; i++) {
			if (!equals(x0,(float[])data.get(i*2)))
				return false;
		}
		return true;
	}
	
	/**
	 * Equals.
	 *
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @return true, if successful
	 */
	private boolean equals(float[] a1, float[] a2) {
		if (a1.length!=a2.length)
			return false;
		for (int i=0; i<a1.length; i++) {
			if (a1[i]!=a2[i])
				return false;
		}
		return true;
	}

	/**
	 * Gets the headings.
	 *
	 * @return the headings
	 */
	private String[] getHeadings() {
		ArrayList headings = new ArrayList();
		int sets = plot.storedData.size()/2;
		if (saveXValues || sets>1) {
			if (sets==1) {
				headings.add("X");
				headings.add("Y");
			} else {
				headings.add("X0");
				headings.add("Y0");
			}
		} else {
			headings.add("X0");
			headings.add("Y0");
		}
		if (plot.errorBars!=null) {
			if (plot.xErrorBars!=null)
				headings.add("Y_ERR");
			else
				headings.add("ERR");
		}
		if (plot.xErrorBars!=null)
			headings.add("X_ERR");
		for (int j = 1; j<sets; j++) {
			if (saveXValues || sets>1) {
				headings.add("X"+j);
				headings.add("Y" + j);
			} else
				headings.add("Y" + j);
		}
		return (String[])headings.toArray(new String[headings.size()]);
	}
		
	/**
	 *  creates the data that fills the showList() function values.
	 *
	 * @return the values as string
	 */
	private String getValuesAsString(){
		ResultsTable rt = getResultsTable();
		int n = rt.size();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<rt.size(); i++) {
			sb.append(rt.getRowAsString(i));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Save as text.
	 */
	void saveAsText() {
		SaveDialog sd = new SaveDialog("Save as Text", "Values", ".txt");
		String name = sd.getFileName();
		if (name==null) return;
		String directory = sd.getDirectory();
		PrintWriter pw = null;
		try {
			FileOutputStream fos = new FileOutputStream(directory+name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		IJ.wait(250);  // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");
		pw.print(getValuesAsString());
		pw.close();
		if (autoClose)
			{imp.changes=false; close();}
	}
		
	/**
	 * Copy to clipboard.
	 */
	void copyToClipboard() {
		Clipboard systemClipboard = null;
		try {systemClipboard = getToolkit().getSystemClipboard();}
		catch (Exception e) {systemClipboard = null; }
		if (systemClipboard==null)
			{IJ.error("Unable to copy to Clipboard."); return;}
		IJ.showStatus("Copying plot values...");
		int xdigits = 0;
		if (saveXValues)
			xdigits = getPrecision(plot.xValues);
		int ydigits = xdigits;
		if (ydigits==0)
			ydigits = getPrecision(plot.yValues);
		CharArrayWriter aw = new CharArrayWriter(plot.nPoints*4);
		PrintWriter pw = new PrintWriter(aw);
		for (int i=0; i<plot.nPoints; i++) {
			if (saveXValues)
				pw.print(IJ.d2s(plot.xValues[i],xdigits)+"\t"+IJ.d2s(plot.yValues[i],ydigits)+"\n");
			else
				pw.print(IJ.d2s(plot.yValues[i],ydigits)+"\n");
		}
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
		if (autoClose)
			{imp.changes=false; close();}
	}
	
	/**
	 * Gets the precision.
	 *
	 * @param values the values
	 * @return the precision
	 */
	int getPrecision(float[] values) {
		int setDigits = Analyzer.getPrecision();
		int measurements = Analyzer.getMeasurements();
		boolean scientificNotation = (measurements&Measurements.SCIENTIFIC_NOTATION)!=0;
		int minDecimalPlaces = 4;
		if (scientificNotation) {
			if (setDigits<minDecimalPlaces)
				setDigits = minDecimalPlaces;
			return -setDigits;
		}
		int digits = minDecimalPlaces;
		if (setDigits>digits)
			digits = setDigits;
		boolean realValues = false;
		for (int i=0; i<values.length; i++) {
			if ((int)values[i]!=values[i] && !Float.isNaN(values[i])) {
				realValues = true;
				break;
			}
		}
		if (!realValues)
			digits = 0;
		return digits;
	}
		
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==live)
			toggleLiveProfiling();
		else if (b==list)
			showList();
		else if (b==save)
			saveAsText();
		else
			copyToClipboard();
	}
	
	/**
	 * Gets the x values.
	 *
	 * @return the x values
	 */
	public float[] getXValues() {
		return plot.xValues;
	}

	/**
	 * Gets the y values.
	 *
	 * @return the y values
	 */
	public float[] getYValues() {
		return plot.yValues;
	}
		
	/**
	 *  Draws a new plot in this window.
	 *
	 * @param plot the plot
	 */
	public void drawPlot(Plot plot) {
		this.plot = plot;
		if (imp!=null) {
			imp.setProcessor(null, plot.getProcessor());	
			ImagePlus plotImp = plot.getImagePlus();
			Calibration plotCal = plotImp.getCalibration();
			imp.setCalibration(plotCal);
		}
	}
	
	/**
	 *  Called once when ImageJ quits.
	 *
	 * @param prefs the prefs
	 */
	public static void savePreferences(Properties prefs) {
		double min = ProfilePlot.getFixedMin();
		double max = ProfilePlot.getFixedMax();
		if (plotWidth!=WIDTH || plotHeight!=HEIGHT) {
			prefs.put(PLOT_WIDTH, Integer.toString(plotWidth));
			prefs.put(PLOT_HEIGHT, Integer.toString(plotHeight));
		}
		int options = 0;
		if (saveXValues) options |= SAVE_X_VALUES;
		if (autoClose && !listValues) options |= AUTO_CLOSE;
		if (listValues) options |= LIST_VALUES;
		if (!interpolate) options |= INTERPOLATE; // true=0, false=1
		if (noGridLines) options |= NO_GRID_LINES; 
		prefs.put(OPTIONS, Integer.toString(options));
	}
	
	/**
	 * Toggle live profiling.
	 */
	private void toggleLiveProfiling() {
		boolean liveMode = live.getForeground()==Color.red;
		if (liveMode)
			removeListeners();
		else
			enableLiveProfiling();
	}

	/**
	 * Enable live profiling.
	 */
	private void enableLiveProfiling() {
		if (plotMaker==null)
			plotMaker = plot!=null?plot.getPlotMaker():null;
		if (plotMaker!=null && bgThread==null) {
			srcImp = plotMaker.getSourceImage();
			if (srcImp==null)
				return;
			bgThread = new Thread(this, "Live Profiler");
			bgThread.setPriority(Math.max(bgThread.getPriority()-3, Thread.MIN_PRIORITY));
			bgThread.start();
			imageUpdated(srcImp);
		}
		createListeners();
		if (srcImp!=null)
			imageUpdated(srcImp);
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.RoiListener#roiModified(ij.ImagePlus, int)
	 */
	public synchronized void roiModified(ImagePlus img, int id) {
		if (IJ.debugMode) IJ.log("PlotWindow.roiModified: "+img+"  "+id);
		if (img==srcImp) {
			doUpdate=true;
			notify();
		}
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
	// This method is called if the source image content is changed
	public synchronized void imageUpdated(ImagePlus imp) {
		if (imp==srcImp) { 
			doUpdate = true;
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
			plotMaker = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	// the background thread for live plotting.
	public void run() {
		while (true) {
			IJ.wait(50);	//delay to make sure the roi has been updated
			Plot plot = plotMaker.getPlot();
			if (doUpdate && plot!=null) {
				this.plot = plot;
				ImageProcessor ip = plot.getProcessor();
				if (ip!=null && imp!=null)
					imp.setProcessor(null, ip);
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
		if (IJ.debugMode) IJ.log("PlotWindow.createListeners");
		if (srcImp==null)
			return;
		ImagePlus.addImageListener(this);
		Roi.addRoiListener(this);
		Font font = live.getFont();
		live.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		live.setForeground(Color.red);
	}
	
	/**
	 * Removes the listeners.
	 */
	private void removeListeners() {
		if (IJ.debugMode) IJ.log("PlotWindow.removeListeners");
		if (srcImp==null)
			return;
		ImagePlus.removeImageListener(this);
		Roi.removeRoiListener(this);
		Font font = live.getFont();
		live.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		live.setForeground(Color.black);
	}
	
}


