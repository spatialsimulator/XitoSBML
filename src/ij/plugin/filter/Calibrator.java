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
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.Measurements;
import ij.measure.Minimizer;
import ij.plugin.TextReader;
import ij.plugin.frame.Fitter;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;


// TODO: Auto-generated Javadoc
/** Implements the Analyze/Calibrate command. */
public class Calibrator implements PlugInFilter, Measurements, ActionListener {

	/** The Constant NONE. */
	private static final String NONE = "None";
	
	/** The Constant INVERTER. */
	private static final String INVERTER = "Pixel Inverter";
	
	/** The Constant UNCALIBRATED_OD. */
	private static final String UNCALIBRATED_OD = "Uncalibrated OD";
	
	/** The Constant CUSTOM. */
	private static final String CUSTOM = "Custom";
	
	/** The show settings. */
	private static boolean showSettings;
	
	/** The global 2. */
	private boolean global1, global2;
    
    /** The imp. */
    private ImagePlus imp;
	
	/** The choice index. */
	private int choiceIndex;
	
	/** The functions. */
	private String[] functions;
	
	/** The n fits. */
	private	int nFits = Calibration.EXP_RECOVERY+1;   //don't set to CurveFitter.fitList.length; Calibration can't cope with it
	
	/** The curve fit error. */
	private String curveFitError;
	
	/** The spacer index. */
	private int spacerIndex = nFits+1;
	
	/** The inverter index. */
	private int inverterIndex = nFits+2;
	
	/** The od index. */
	private int odIndex = nFits+3;
	
	/** The custom index. */
	private int customIndex = nFits+4;
	
	/** The x text. */
	private static String xText = "";
	
	/** The y text. */
	private static String yText = "";
	
	/** The imported values. */
	private static boolean importedValues;
	
	/** The unit. */
	private String unit;
	
	/** The ly. */
	private double lx=0.02, ly=0.1;
	
	/** The old function. */
	private int oldFunction;
	
	/** The fit goodness. */
	private String sumResiduals, fitGoodness;
	
	/** The save. */
	private Button open, save;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The show plot flag saved. */
	private static boolean showPlotFlagSaved = true;
	
	/** The show plot flag. */
	private boolean showPlotFlag;
	
	/** The unit saved. */
	private static String unitSaved = Calibration.DEFAULT_VALUE_UNIT;
	
	/** The curve fitter. */
	private CurveFitter curveFitter;
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		//IJ.register(Calibrator.class);
		return DOES_ALL-DOES_RGB+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		global1 = imp.getGlobalCalibration()!=null;
		if (!showDialog(imp))
			return;
		if (choiceIndex==customIndex) {
			showPlot(null, null, imp.getCalibration(), null);
			return;
		} else if (imp.getType()==ImagePlus.GRAY32) {
			if (choiceIndex==0)
				imp.getCalibration().setValueUnit(unit);
			else
				IJ.error("Calibrate", "Function must be \"None\" for 32-bit images,\nbut you can change the Unit.");
		} else
			calibrate(imp);
	}

	/**
	 * Show dialog.
	 *
	 * @param imp the imp
	 * @return true, if successful
	 */
	public boolean showDialog(ImagePlus imp) {
		String defaultChoice;
		Calibration cal = imp.getCalibration();
		functions = getFunctionList(cal.getFunction()==Calibration.CUSTOM);
		int function = cal.getFunction();
		oldFunction = function;
		double[] p = cal.getCoefficients();
		unit = cal.getValueUnit();
		if (unit == Calibration.DEFAULT_VALUE_UNIT)
		    unit = unitSaved;
		if (function==Calibration.NONE)
			defaultChoice=NONE;
		else if (function<nFits&&function==Calibration.STRAIGHT_LINE&&p!=null&& p[0]==255.0&&p[1]==-1.0)
			defaultChoice=INVERTER;
		else if (function<nFits)
			defaultChoice = CurveFitter.fitList[function];
		else if (function==Calibration.UNCALIBRATED_OD)
			defaultChoice=UNCALIBRATED_OD;
		else if (function==Calibration.CUSTOM)
			defaultChoice=CUSTOM;
		else
			defaultChoice=NONE;
			
		String tmpText = getMeans();
		if (!importedValues && !tmpText.equals(""))	
			xText = tmpText;	
		gd = new GenericDialog("Calibrate...");
		gd.addChoice("Function:", functions, defaultChoice);
		gd.addStringField("Unit:", unit, 16);
		gd.addTextAreas(xText, yText, 20, 14);
		//gd.addMessage("Left column contains uncalibrated measured values,\n right column contains known values (e.g., OD).");
		gd.addPanel(makeButtonPanel(gd));
		gd.addCheckbox("Global calibration", IJ.isMacro()?false:global1);
		gd.addCheckbox("Show plot", IJ.isMacro()?false:showPlotFlagSaved);
		//gd.addCheckbox("Show Simplex Settings", showSettings);
		gd.addHelp(IJ.URL+"/docs/menus/analyze.html#cal");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		else {
			choiceIndex = gd.getNextChoiceIndex();
			unit = gd.getNextString();
			xText = gd.getNextText();
			yText = gd.getNextText();
			global2 = gd.getNextBoolean();
			showPlotFlag = gd.getNextBoolean();
			//showSettings = gd.getNextBoolean();
			showPlotFlagSaved = showPlotFlag;
			unitSaved = unit;
			return true;
		}
	}

	/**
	 *  Creates a panel containing "Open..." and "Save..." buttons.
	 *
	 * @param gd the gd
	 * @return the panel
	 */
	Panel makeButtonPanel(GenericDialog gd) {
		Panel buttons = new Panel();
    	buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		open = new Button("Open...");
		open.addActionListener(this);
		buttons.add(open);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		return buttons;
	}

    /**
     *  Calibrate an image with the function type defined previously.
     *  Sets the function to Calibration.NONE on error
     *
     * @param imp the imp
     */
	public void calibrate(ImagePlus imp) {
		Calibration cal = imp.getCalibration();
		Calibration calOrig = cal.copy();
		int function = Calibration.NONE;
		boolean is16Bits = imp.getType()==ImagePlus.GRAY16;
		double[] parameters = null;
		double[] x=null, y=null;
		boolean zeroClip=false;
		curveFitter = null;
		if (choiceIndex<=0) {
			if (oldFunction==Calibration.NONE&&!yText.equals("")&&!xText.equals("")) {
				IJ.error("Calibrate", "Please select a function");
			    return;
			}
			function = Calibration.NONE;
		} else if (choiceIndex<=nFits) {
			function = choiceIndex - 1;
			x = getData(xText);
			y = getData(yText);
			if (!validateXValues(imp, x))
				return;
			if (!cal.calibrated() || y.length!=0 || function!=oldFunction) {
				parameters = doCurveFitting(x, y, function);
				if (parameters==null) { //minimization failed
				    IJ.error(curveFitError);
				    function = Calibration.NONE;
					return;
				}
			}
			if (!is16Bits && function!=Calibration.STRAIGHT_LINE) {
				zeroClip = true;
				for (int i=0; i<y.length; i++)
					if (y[i]<0.0) zeroClip = false;
			}
		} else if (choiceIndex==inverterIndex) {
			function = Calibration.STRAIGHT_LINE;
			parameters = new double[2];
			if (is16Bits)
				parameters[0] = 65535;
			else
				parameters[0] = 255;
			parameters[1] = -1.0;
			unit = "Inverted Gray Value";
		} else if (choiceIndex==odIndex) {
			if (is16Bits) {
				IJ.error("Calibrate", "Uncalibrated OD is not supported on 16-bit images.");
				return;
			}
			function = Calibration.UNCALIBRATED_OD;
			unit = "Uncalibrated OD";
		}
		cal.setFunction(function, parameters, unit, zeroClip);
		if (!cal.equals(calOrig))
			imp.setCalibration(cal);
		imp.setGlobalCalibration(global2?cal:null);
		if (global2 || global2!=global1)
			WindowManager.repaintImageWindows();
		else
			imp.repaintWindow();
		if (global2 && global2!=global1)
			FileOpener.setShowConflictMessage(true);
		if (function!=Calibration.NONE && showPlotFlag) {
			if (curveFitter!=null)
				Fitter.plot(curveFitter, imp.getBitDepth()==8);
			else
				showPlot(x, y, cal, fitGoodness);
		}
	}
	
	/**
	 * Validate X values.
	 *
	 * @param imp the imp
	 * @param x the x
	 * @return true, if successful
	 */
	private boolean validateXValues(ImagePlus imp, double[] x) {
		int bitDepth = imp.getBitDepth();
		if (bitDepth==32 || x==null)
			return true;
		int max = 255;
		if (bitDepth==16)
			max = 65535;
		for (int i=0; i<x.length; i++) {
			if (x[i]<0 || x[i]>max) {
			    String title = (bitDepth==8?"8-bit":"16-bit") + " Calibration";
				String msg = "Measured (uncalibrated) values in the left\ncolumn must be in the range 0-";
				IJ.error(title, msg+max+".");
				return false;
			}
		}
		return true;
	}

	/**
	 * Do curve fitting.
	 *
	 * @param x the x
	 * @param y the y
	 * @param fitType the fit type
	 * @return the double[]
	 */
	double[] doCurveFitting(double[] x, double[] y, int fitType) {
		if (x.length!=y.length || y.length==0) {
			IJ.error("Calibrate",
				"To create a calibration curve, the left column must\n"
				+"contain a list of measured mean pixel values and the\n"
				+"right column must contain the same number of calibration\n"
				+"standard values. Use the Measure command to add mean\n"
				+"pixel value measurements to the left column.\n"
				+" \n"
				+"    Left column: "+x.length+" values\n"
				+"    Right column: "+y.length+" values\n"
				);
			return null;
		}
		int n = x.length;
		double xmin=0.0,xmax;
		if (imp.getType()==ImagePlus.GRAY16)
			xmax=65535.0; 
		else
			xmax=255.0;
		double[] a = Tools.getMinMax(y);
		double ymin=a[0], ymax=a[1]; 
		CurveFitter cf = new CurveFitter(x, y);
		cf.doFit(fitType, showSettings);
		if (cf.getStatus() == Minimizer.INITIALIZATION_FAILURE) {
		    curveFitError = cf.getStatusString();
		    return null;
		}
        if (IJ.debugMode) IJ.log(cf.getResultString());
		int np = cf.getNumParams();
		double[] p = cf.getParams();
		fitGoodness = IJ.d2s(cf.getRSquared(),6);
		curveFitter = cf;
		double[] parameters = new double[np];
		for (int i=0; i<np; i++)
			parameters[i] = p[i];
		return parameters;									
	}
	
	/**
	 * Show plot.
	 *
	 * @param x the x
	 * @param y the y
	 * @param cal the cal
	 * @param rSquared the r squared
	 */
	void showPlot(double[] x, double[] y, Calibration cal, String rSquared) {
		if (!showPlotFlag || !cal.calibrated())
			return;
		int xmin,xmax,range;
		float[] ctable = cal.getCTable();
		if (ctable.length==256) { //8-bit image
			xmin = 0;
			xmax = 255;
		} else {  // 16-bit image
			xmin = 0;
			xmax = 65535;
		}
		range = 256;
		float[] px = new float[range];
		float[] py = new float[range];
		for (int i=0; i<range; i++)
			px[i]=(float)((i/255.0)*xmax);
		for (int i=0; i<range; i++)
			py[i]=ctable[(int)px[i]];
		double[] a = Tools.getMinMax(py);
		double ymin = a[0];
		double ymax = a[1];
		int fit = cal.getFunction();
		String unit = cal.getValueUnit();
		Plot plot = new Plot("Calibration Function","pixel value",unit,px,py);
		plot.setLimits(xmin,xmax,ymin,ymax);
		if (x!=null&&y!=null&&x.length>0&&y.length>0)
			plot.addPoints(x, y, PlotWindow.CIRCLE);
		double[] p = cal.getCoefficients();
		if (fit<=Calibration.LOG2) {
			drawLabel(plot, CurveFitter.fList[fit]);
			ly += 0.04;
		}
		if (p!=null) {
			int np = p.length;
			drawLabel(plot, "a="+IJ.d2s(p[0],6,10));
			drawLabel(plot, "b="+IJ.d2s(p[1],6,10));
			if (np>=3)
				drawLabel(plot, "c="+IJ.d2s(p[2],6,10));
			if (np>=4)
				drawLabel(plot, "d="+IJ.d2s(p[3],6,10));
			if (np>=5)
				drawLabel(plot, "e="+IJ.d2s(p[4],6,10));
			ly += 0.04;
		}
		if (rSquared!=null)
			{drawLabel(plot, "R^2="+rSquared); rSquared=null;}
		plot.show();
	}
	
	/**
	 * Draw label.
	 *
	 * @param plot the plot
	 * @param label the label
	 */
	void drawLabel(Plot plot, String label) {
		plot.addLabel(lx, ly, label);
		ly += 0.08;
	}
	

	/**
	 * Sqr.
	 *
	 * @param x the x
	 * @return the double
	 */
	double sqr(double x) {return x*x;}

	/**
	 * Gets the function list.
	 *
	 * @param custom the custom
	 * @return the function list
	 */
	String[] getFunctionList(boolean custom) {
		int n = nFits+4;
		if (custom) n++;
		String[] list = new String[n];
		list[0] = NONE;
		for (int i=0; i<nFits; i++)
			list[1+i] = CurveFitter.fitList[i];
		list[spacerIndex] = "-";
		list[inverterIndex] = INVERTER;
		list[odIndex] = UNCALIBRATED_OD;
		if (custom) 
			list[customIndex] = CUSTOM;
		return list;
 	}
	
	/**
	 * Gets the means.
	 *
	 * @return the means
	 */
	String getMeans() {
		float[] umeans = Analyzer.getUMeans();
		int count = Analyzer.getCounter();
		if (umeans==null || count==0)
			return "";
		if (count>MAX_STANDARDS)
			count = MAX_STANDARDS;
		String s = "";
		for (int i=0; i<count; i++)
			s += IJ.d2s(umeans[i],2)+"\n";
		importedValues = false;
		return s;
	}

	/**
	 * Gets the data.
	 *
	 * @param xData the x data
	 * @return the data
	 */
	double[] getData(String xData) {
		int len = xData.length();
		StringBuffer sb = new StringBuffer(len);
		for (int i=0; i<len; i++) {
			char c = xData.charAt(i);
			if ((c>='0'&&c<='9') || c=='-'  || c=='.' || c==',' || c=='\n' || c=='\r' || c==' ')
				sb.append(c);
		}
		xData = sb.toString();

		StringTokenizer st = new StringTokenizer(xData);
		int nTokens = st.countTokens();
		if (nTokens<1)
			return new double[0];
		int n = nTokens;
		double data[] = new double[n];
		for (int i=0; i<n; i++) {
			data[i] = getNum(st);
		}
		return data;
	}
	
	/**
	 * Gets the num.
	 *
	 * @param st the st
	 * @return the num
	 */
	double getNum(StringTokenizer st) {
		Double d;
		String token = st.nextToken();
		try {d = new Double(token);}
		catch (NumberFormatException e){d = null;}
		if (d!=null)
			return(d.doubleValue());
		else
			return 0.0;
	}
	
	/**
	 * Save.
	 */
	void save() {
		TextArea ta1 = gd.getTextArea1();
		TextArea ta2 = gd.getTextArea2();
		ta1.selectAll();
		String text1 = ta1.getText();
		ta1.select(0, 0);
		ta2.selectAll();
		String text2 = ta2.getText();
		ta2.select(0, 0);
		double[] x = getData(text1);
		double[] y = getData(text2);
		SaveDialog sd = new SaveDialog("Save as Text...", "calibration", ".txt");
		String name = sd.getFileName();
		if (name == null)
			return;
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
		int n = Math.max(x.length, y.length);
		for (int i=0; i<n; i++) {
			String xs = x.length==0?"":i<x.length?""+x[i]:"0";
			String ys = y.length==0?"":i<y.length?""+y[i]:"0";
			pw.println(xs + "\t"+ ys);
		}
		pw.close();
	}
	
	/**
	 * Open.
	 */
	void open() {
		OpenDialog od = new OpenDialog("Open Calibration...", "");
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return;
		String path = directory + name;
		TextReader tr = new TextReader();
		ImageProcessor ip = tr.open(path);
		if (ip==null)
			return;
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (!((width==1||width==2)&&height>1)) {
			IJ.error("Calibrate", "This appears to not be a one or two column text file");
			return;
		}
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<height; i++) {
			sb.append(""+ip.getPixelValue(0, i));
			sb.append("\n");
		}
		String s1=null, s2=null;
		if (width==2) {
			s1 = new String(sb);
			sb = new StringBuffer();
			for (int i=0; i<height; i++) {
				sb.append(""+ip.getPixelValue(1, i));
				sb.append("\n");
			}
			s2 = new String(sb);
		} else
			s2 = new String(sb);
		if (s1!=null) {
			TextArea ta1 = gd.getTextArea1();
			ta1.selectAll();
			ta1.setText(s1);
		}
		TextArea ta2 = gd.getTextArea2();
		ta2.selectAll();
		ta2.setText(s2);
		importedValues = true;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source==save)
			save();
		else if (source==open)
			open();
	}
	
}
