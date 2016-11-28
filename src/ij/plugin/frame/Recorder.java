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
package ij.plugin.frame;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.Arrow;
import ij.gui.GUI;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.measure.CurveFitter;
import ij.plugin.Colors;
import ij.plugin.NewPlugin;
import ij.plugin.PlugIn;
import ij.util.Tools;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/** This is ImageJ's macro recorder. */
public class Recorder extends PlugInFrame implements PlugIn, ActionListener, ImageListener, ItemListener {

	/** This variable is true if the recorder is running. */
	public static boolean record;
	
	/** Set this variable true to allow recording within IJ.run() calls. */
	public static boolean recordInMacros;

	/** The Constant JAVA. */
	private final static int MACRO=0, JAVASCRIPT=1, BEANSHELL=2, JAVA=3;
	
	/** The Constant modes. */
	private final static String[] modes = {"Macro", "JavaScript", "BeanShell", "Java"};
	
	/** The mode. */
	private Choice mode;
	
	/** The help. */
	private Button makeMacro, help;
	
	/** The file name. */
	private TextField fileName;
	
	/** The fit type str. */
	private String fitTypeStr = CurveFitter.fitList[0];
	
	/** The text area. */
	private static TextArea textArea;
	
	/** The instance. */
	private static Recorder instance;
	
	/** The command name. */
	private static String commandName;
	
	/** The command options. */
	private static String commandOptions;
	
	/** The default name. */
	private static String defaultName = "Macro.ijm";
	
	/** The record path. */
	private static boolean recordPath = true;
	
	/** The script mode. */
	private static boolean scriptMode;
	
	/** The image updated. */
	private static boolean imageUpdated;
	
	/** The image ID. */
	private static int imageID;
	
	/** The bg color set. */
	private static boolean fgColorSet, bgColorSet;
	
	/** The bb set. */
	private static boolean bbSet;

	/**
	 * Instantiates a new recorder.
	 */
	public Recorder() {
		this(true);	
	}

	/**
	 * Instantiates a new recorder.
	 *
	 * @param showFrame the show frame
	 */
	public Recorder(boolean showFrame) {
		super("Recorder");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		record = true;
		scriptMode = false;
		recordInMacros = false;
		Panel panel = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.add(new Label("  Record:"));
		mode = new Choice();
		for (int i=0; i<modes.length; i++)
			mode.addItem(modes[i]);
		mode.addItemListener(this);
		String m = Prefs.get("recorder.mode", modes[MACRO]);
		if (m.equals("Plugin")) m=modes[JAVA];
		mode.select(m);
		panel.add(mode);
		panel.add(new Label("    Name:"));
		fileName = new TextField(defaultName, 15);
		setFileName();
		panel.add(fileName);
		panel.add(new Label("   "));
		makeMacro = new Button("Create");
		makeMacro.addActionListener(this);
		panel.add(makeMacro);
		panel.add(new Label("   "));
		help = new Button("?");
		help.addActionListener(this);
		panel.add(help);
		add("North", panel);
		textArea = new TextArea("", 15, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		if (IJ.isLinux()) textArea.setBackground(Color.white);
		add("Center", textArea);
		pack();
		GUI.center(this);
		if (showFrame)
			show();
		IJ.register(Recorder.class);
		fgColorSet = bgColorSet = false;
		bbSet = false;
	}
	
	/**
	 * Record.
	 *
	 * @param method the method
	 */
	public static void record(String method) {
		if (textArea==null)
			return;
		textArea.append(method+"();\n");
	}

	/**
	 *  Starts recording a command. Does nothing if the recorder is
	 * 		not open or the command being recorded has called IJ.run().
	 *
	 * @param command the new command
	 */
	public static void setCommand(String command) {
		boolean isMacro = Thread.currentThread().getName().startsWith("Run$_");
		if (textArea==null || (isMacro&&!recordInMacros))
			return;
		commandName = command;
		commandOptions = null;
		recordPath = true;
		imageUpdated = false;
		imageID = 0;
		if (scriptMode) {
			ImagePlus imp = WindowManager.getCurrentImage();
			imageID = imp!=null?imp.getID():0;
			if (imageID!=0)
				ImagePlus.addImageListener(instance);
		}
		//IJ.log("setCommand: "+command+" "+Thread.currentThread().getName());
	}

	/**
	 *  Returns the name of the command currently being recorded, or null.
	 *
	 * @return the command
	 */
	public static String getCommand() {
		return commandName;
	}

	/**
	 * Fix path.
	 *
	 * @param path the path
	 * @return the string
	 */
	static String fixPath (String path) {
		StringBuffer sb = new StringBuffer();
		char c;
		for (int i=0; i<path.length(); i++) {
			sb.append(c=path.charAt(i));
			if (c=='\\')
				sb.append("\\");
		}
		return new String(sb);
	}
	
	/**
	 * Record.
	 *
	 * @param method the method
	 * @param arg the arg
	 */
	public static void record(String method, String arg) {
		if (IJ.debugMode) IJ.log("record: "+method+"  "+arg);
		boolean sw = method.equals("selectWindow");
		if (textArea!=null && !(scriptMode&&sw||commandName!=null&&sw)) {
			if (scriptMode && method.equals("roiManager"))
				textArea.append("rm.runCommand(imp,\""+arg+"\");\n");
			else if (scriptMode && method.equals("run"))
				textArea.append("IJ."+method+"(\""+arg+"\");\n");
			else {
				if (method.equals("setTool"))
					method = "//"+(scriptMode?"IJ.":"")+method;
				textArea.append(method+"(\""+arg+"\");\n");
			}
		}
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param arg1 the arg 1
	 * @param arg2 the arg 2
	 */
	public static void record(String method, String arg1, String arg2) {
		if (textArea==null) return;
		if (arg1.equals("Open")||arg1.equals("Save")||method.equals("saveAs"))
			arg2 = fixPath(arg2);
		if (scriptMode&&method.equals("roiManager"))
			textArea.append("rm.runCommand(\""+arg1+"\", \""+arg2+"\");\n");
		else {
			if (scriptMode && method.equals("saveAs"))
				method = "IJ." + method;
			textArea.append(method+"(\""+arg1+"\", \""+arg2+"\");\n");
		}
	}
	
	/**
	 * Record.
	 *
	 * @param method the method
	 * @param arg1 the arg 1
	 * @param arg2 the arg 2
	 * @param arg3 the arg 3
	 */
	public static void record(String method, String arg1, String arg2, String arg3) {
		if (textArea==null) return;
		textArea.append(method+"(\""+arg1+"\", \""+arg2+"\",\""+arg3+"\");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 */
	public static void record(String method, int a1) {
		if (textArea==null) return;
		textArea.append(method+"("+a1+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 */
	public static void record(String method, int a1, int a2) {
		if (textArea==null) return;
		textArea.append(method+"("+a1+", "+a2+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 */
	public static void record(String method, double a1, double a2) {
		if (textArea==null) return;
		int places = Math.abs(a1)<0.0001||Math.abs(a2)<0.0001?9:4;
		textArea.append(method+"("+IJ.d2s(a1,places)+", "+IJ.d2s(a2,places)+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param a3 the a 3
	 */
	public static void record(String method, int a1, int a2, int a3) {
		if (textArea==null) return;
		if (scriptMode&&method.endsWith("groundColor")) method = "IJ."+method;
		textArea.append(method+"("+a1+", "+a2+", "+a3+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 */
	public static void record(String method, String a1, int a2) {
		textArea.append(method+"(\""+a1+"\", "+a2+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param args the args
	 * @param a1 the a 1
	 * @param a2 the a 2
	 */
	public static void record(String method, String args, int a1, int a2) {
		if (textArea==null) return;
		textArea.append(method+"(\""+args+"\", "+a1+", "+a2+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param a3 the a 3
	 * @param a4 the a 4
	 */
	public static void record(String method, int a1, int a2, int a3, int a4) {
		if (textArea==null) return;
		if (scriptMode&&method.startsWith("make")) {
			if (method.equals("makeRectangle"))
				recordString("imp.setRoi("+a1+","+a2+","+a3+","+a4+");\n");
			else if (method.equals("makeOval"))
				recordString("imp.setRoi(new OvalRoi("+a1+","+a2+","+a3+","+a4+"));\n");
			else if (method.equals("makeLine"))
				recordString("imp.setRoi(new Line("+a1+","+a2+","+a3+","+a4+"));\n");
			else if (method.equals("makeArrow"))
				recordString("imp.setRoi(new Arrow("+a1+","+a2+","+a3+","+a4+"));\n");
		} else {
			if (method.equals("makeArrow")) {
				ImagePlus imp = WindowManager.getCurrentImage();
				Roi roi = imp!=null?imp.getRoi():null;
				if (roi!=null && (roi instanceof Line)) {
					Arrow arrow = (Arrow)roi;
					String options = Arrow.styles[arrow.getStyle()];
					if (arrow.getOutline())
						options += " outline";
					if (arrow.getDoubleHeaded())
						options += " double";
					if (arrow.getHeadSize()<=5)
						options += " small";
					else if (arrow.getHeadSize()>=15)
						options += " large";
					options = options.toLowerCase();
					int strokeWidth = (int)arrow.getStrokeWidth();
					textArea.append(method+"("+a1+", "+a2+", "+a3+", "+a4+", \""+options+"\");\n");
					if (strokeWidth!=1)
						textArea.append("Roi.setStrokeWidth("+strokeWidth+");\n");
					Color color = arrow.getStrokeColor();
					if (color!=null)
						textArea.append("Roi.setStrokeColor(\""+Colors.colorToString(color)+"\");\n");
					return;
				}
			}
			textArea.append(method+"("+a1+", "+a2+", "+a3+", "+a4+");\n");
		}
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param a3 the a 3
	 * @param a4 the a 4
	 * @param a5 the a 5
	 */
	public static void record(String method, int a1, int a2, int a3, int a4, int a5) {
		textArea.append(method+"("+a1+", "+a2+", "+a3+", "+a4+", "+a5+");\n");
	}
	
	/**
	 * Record.
	 *
	 * @param method the method
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param a3 the a 3
	 * @param a4 the a 4
	 * @param a5 the a 5
	 */
	public static void record(String method, int a1, int a2, int a3, int a4, double a5) {
		textArea.append(method+"("+a1+", "+a2+", "+a3+", "+a4+", "+IJ.d2s(a5,2)+");\n");
	}

	/**
	 * Record.
	 *
	 * @param method the method
	 * @param path the path
	 * @param args the args
	 * @param a1 the a 1
	 * @param a2 the a 2
	 * @param a3 the a 3
	 * @param a4 the a 4
	 * @param a5 the a 5
	 */
	public static void record(String method, String path, String args, int a1, int a2, int a3, int a4, int a5) {
		if (textArea==null) return;
		path = fixPath(path);
		method = "//"+method;
		textArea.append(method+"(\""+path+"\", "+"\""+args+"\", "+a1+", "+a2+", "+a3+", "+a4+", "+a5+");\n");
	}
	
	/**
	 * Record string.
	 *
	 * @param str the str
	 */
	public static void recordString(String str) {
		if (textArea!=null)
			textArea.append(str);
	}

	/**
	 * Record call.
	 *
	 * @param call the call
	 */
	public static void recordCall(String call) {
		if (IJ.debugMode) IJ.log("recordCall: "+call+"  "+commandName);
		boolean isMacro = Thread.currentThread().getName().endsWith("Macro$") && !recordInMacros;
		if (textArea!=null && scriptMode && !IJ.macroRunning() && !isMacro) {
			if (javaMode() && call.startsWith("rm.setSelected")) {
				call = call.replace("[", "new int[]{");
				call = call.replace("])", "})");
			}
			if (javaMode() && call.startsWith("rt = "))
				call = "ResultTable " + call;
			textArea.append(call+"\n");
			commandName = null;
 		}
	}
	
	/**
	 * Record call.
	 *
	 * @param className the class name
	 * @param call the call
	 */
	public static void recordCall(String className, String call) {
		recordCall(javaMode()?className+" "+call:call);
	}

	/**
	 * Record roi.
	 *
	 * @param p the p
	 * @param type the type
	 */
	public static void recordRoi(Polygon p, int type) {
		if (textArea==null) return;
		if (scriptMode)
			{recordScriptRoi(p,type); return;}
		if (type==Roi.ANGLE||type==Roi.POINT) {
			String xarr = "newArray(", yarr="newArray(";
			xarr += p.xpoints[0]+",";
			yarr += p.ypoints[0]+",";
			xarr += p.xpoints[1]+",";
			yarr += p.ypoints[1]+",";
			xarr += p.xpoints[2]+")";
			yarr += p.ypoints[2]+")";
			String typeStr= type==Roi.ANGLE?"angle":"point";
			textArea.append("makeSelection(\""+typeStr+"\","+xarr+","+yarr+");\n");
		} else {
			String method = type==Roi.POLYGON?"makePolygon":"makeLine";
			StringBuffer args = new StringBuffer();
			for (int i=0; i<p.npoints; i++) {
				args.append(p.xpoints[i]+",");
				args.append(""+p.ypoints[i]);
				if (i!=p.npoints-1) args.append(",");
			}
			textArea.append(method+"("+args.toString()+");\n");
		}
	}

	/**
	 * Record script roi.
	 *
	 * @param p the p
	 * @param type the type
	 */
	public static void recordScriptRoi(Polygon p, int type) {
		StringBuffer x = new StringBuffer();
		for (int i=0; i<p.npoints; i++) {
			x.append(p.xpoints[i]);
			if (i!=p.npoints-1) x.append(",");
		}
		String xpoints = x.toString();
		StringBuffer y = new StringBuffer();
		for (int i=0; i<p.npoints; i++) {
			y.append(p.ypoints[i]);
			if (i!=p.npoints-1) y.append(",");
		}
		String ypoints = y.toString();
		if (javaMode()) {
			textArea.append("int[] xpoints = {"+xpoints+"};\n");
			textArea.append("int[] ypoints = {"+ypoints+"};\n");
		} else {
			textArea.append("xpoints = ["+xpoints+"];\n");
			textArea.append("ypoints = ["+ypoints+"];\n");
		}
		String typeStr = "POLYGON";
		switch (type) {
			case Roi.POLYLINE: typeStr = "POLYLINE"; break;
			case Roi.ANGLE: typeStr = "ANGLE"; break;
		}
		typeStr = "Roi."+typeStr;
		if (javaMode()) {
			if (type==Roi.POINT)
				textArea.append("imp.setRoi(new PointRoi(xpoints,ypoints,"+p.npoints+"));\n");
			else
				textArea.append("imp.setRoi(new PolygonRoi(xpoints,ypoints,"+p.npoints+","+typeStr+"));\n");
		} else {
			if (type==Roi.POINT)
				textArea.append("imp.setRoi(new PointRoi(xpoints,ypoints));\n");
			else
				textArea.append("imp.setRoi(new PolygonRoi(xpoints,ypoints,"+typeStr+"));\n");
		}
	}
	
	/**
	 * Java mode.
	 *
	 * @return true, if successful
	 */
	private static boolean javaMode() {
		if (instance==null)
			return false;
		String m = instance.mode.getSelectedItem();
		return m.equals(modes[BEANSHELL]) || m.equals(modes[JAVA]);
	}
	
	/**
	 * Record option.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public static void recordOption(String key, String value) {
		if (key==null) return;
		key = trimKey(key);
		value = addQuotes(value);
		checkForDuplicate(key+"=", value);
		if (commandOptions==null)
			commandOptions = key+"="+value;
		else
			commandOptions += " "+key+"="+value;
	}

	/**
	 * Record path.
	 *
	 * @param key the key
	 * @param path the path
	 */
	public static void recordPath(String key, String path) {
		if (key==null || !recordPath) {
			recordPath = true;
			return;
		}
		key = trimKey(key);
		path = fixPath(path);
		path = addQuotes(path);
		if (commandOptions!=null && commandOptions.contains(key+"="+path))
			return; // don't record duplicate
		checkForDuplicate(key+"=", path);
		if (commandOptions==null || commandOptions==" ")
			commandOptions = key+"="+path;
		else
			commandOptions += " "+key+"="+path;
	}

	/**
	 * Record option.
	 *
	 * @param key the key
	 */
	public static void recordOption(String key) {
		if (key==null) return;
		if (commandOptions==null && key.equals(" "))
			commandOptions = " ";
		else {
			key = trimKey(key);
			checkForDuplicate(" "+key, "");
			if (commandOptions==null || commandOptions==" ")
				commandOptions = key;
			else
				commandOptions += " "+key;
		}
	}
	
	/**
	 * Check for duplicate.
	 *
	 * @param key the key
	 * @param value the value
	 */
	static void checkForDuplicate(String key, String value) {
		if (commandOptions!=null && commandName!=null && commandOptions.indexOf(key)!=-1 && (value.equals("") || commandOptions.indexOf(value)==-1)) {
			if (key.endsWith("=")) key = key.substring(0, key.length()-1);
			IJ.showMessage("Recorder", "Duplicate keyword:\n \n" 
				+ "    Command: " + "\"" + commandName +"\"\n"
				+ "    Keyword: " + "\"" + key +"\"\n"
				+ "    Value: " + value+"\n \n"
				+ "Add an underscore to the corresponding label\n"
				+ "in the dialog to make the first word unique.");
		}
	}
	
	/**
	 * Trim key.
	 *
	 * @param key the key
	 * @return the string
	 */
	static String trimKey(String key) {
		int index = key.indexOf(" ");
		if (index>-1)
			key = key.substring(0,index);
		index = key.indexOf(":");
		if (index>-1)
			key = key.substring(0,index);
		key = key.toLowerCase(Locale.US);
		return key;
	}

	/** Writes the current command and options to the Recorder window. */
	public static void saveCommand() {
		String name = commandName;
		if (name!=null) {
			if (commandOptions==null && (name.equals("Fill")||name.equals("Clear")))
				commandOptions = "slice";
			if (!fgColorSet && (name.equals("Fill")||name.equals("Draw")))
				setForegroundColor(Toolbar.getForegroundColor());
			else if (!bgColorSet && (name.equals("Clear")||name.equals("Clear Outside")))
				setBackgroundColor(Toolbar.getBackgroundColor());
			if (!bbSet && (name.equals("Make Binary")||name.equals("Convert to Mask")||name.equals("Erode")
			||name.equals("Dilate")||name.equals("Skeletonize")))
				setBlackBackground();
			if (commandOptions!=null) {
				if (name.equals("Open...") || name.equals("URL...")) {
					String s = scriptMode?"imp = IJ.openImage":"open";
					String path = strip(commandOptions);
					boolean openingLut = false;
					if (scriptMode) {
						if (isTextOrTable(commandOptions))
							s = "IJ.open";
						else if (path!=null && path.endsWith(".lut")) {
							s = "lut = Opener.openLut";
							openingLut = true;
						}
					}
					textArea.append(s+"(\""+path+"\");\n");
					ImagePlus imp = WindowManager.getCurrentImage();
					if (openingLut && imp!=null && !imp.getTitle().endsWith(".lut"))
						textArea.append("imp.setLut(lut);\n");
				} else if (name.equals("TIFF Virtual Stack...") && scriptMode) {
					String s = "imp = IJ.openVirtual";
					String path = strip(commandOptions);
					textArea.append(s+"(\""+path+"\");\n");
				} else if (isSaveAs()) {
							if (name.endsWith("..."))
									name= name.substring(0, name.length()-3);
							String path = strip(commandOptions);
							String s = scriptMode?"IJ.saveAs(imp, ":"saveAs(";
							textArea.append(s+"\""+name+"\", \""+path+"\");\n");
				} else if (name.equals("Image..."))
					appendNewImage(false);
				else if (name.equals("Hyperstack...")||name.equals("New Hyperstack..."))
					appendNewImage(true);
				else if (name.equals("Set Slice..."))
					textArea.append((scriptMode?"imp.":"")+"setSlice("+strip(commandOptions)+");\n");
				else if (name.equals("Rename..."))
					textArea.append((scriptMode?"imp.setTitle":"rename")+"(\""+strip(commandOptions)+"\");\n");
				else if (name.equals("Wand Tool..."))
					textArea.append("//run(\""+name+"\", \""+commandOptions+"\");\n");
				else if (name.equals("Results... ")&&commandOptions.indexOf(".txt")==-1)
					textArea.append((scriptMode?"IJ.":"")+"open(\""+strip(commandOptions)+"\");\n");
				else if (name.equals("Results...")) // Save As>Results
					;
				else if (name.equals("Run...")) // Plugins>Macros>Run
					;
				else {
					String prefix = "run(";
					if (scriptMode) {
						boolean addImp = imageUpdated || (WindowManager.getCurrentImage()!=null
							&&(name.equals("Properties... ")||name.equals("Fit Spline")||commandOptions.contains("save=")));
						if (commandOptions.contains("open="))
							addImp = false;
						prefix = addImp?"IJ.run(imp, ":"IJ.run(";
					}
					textArea.append(prefix+"\""+name+"\", \""+commandOptions+"\");\n");
					if (nonAscii(commandOptions))
						textArea.append("  <<warning: the options string contains one or more non-ascii characters>>\n");
				}
			} else {
				ImagePlus imp = WindowManager.getCurrentImage();
				Roi roi = imp!=null?imp.getRoi():null;
				if (name.equals("Threshold...") || name.equals("Fonts...") || name.equals("Brightness/Contrast...") || name.equals("Channels Tool..."))
					textArea.append((scriptMode?"//IJ.":"//")+"run(\""+name+"\");\n");
				else if (name.equals("Start Animation [\\]"))
					textArea.append("doCommand(\"Start Animation [\\\\]\");\n");
				else if (name.equals("Add to Manager"))
					;
				else if (roi!=null && (roi instanceof TextRoi) && (name.equals("Draw")||name.equals("Add Selection...")))
					textArea.append(((TextRoi)roi).getMacroCode(name, imp));
				else {
					if (IJ.altKeyDown() && (name.equals("Open Next")||name.equals("Plot Profile")))
						textArea.append("setKeyDown(\"alt\"); ");
					if (scriptMode) {
						boolean addImp = imageUpdated ||
							(imp!=null&&(name.equals("Select None")||name.equals("Draw")||name.equals("Fit Spline")||name.equals("Add Selection...")));
						String prefix = addImp?"IJ.run(imp, ":"IJ.run(";
						textArea.append(prefix+"\""+name+"\", \"\");\n");
					} else
						textArea.append("run(\""+name+"\");\n");
				}
			}
		}
		commandName = null;
		commandOptions = null;
		if (imageID!=0) {
			ImagePlus.removeImageListener(instance);
			imageID = 0;
		}
	}
	
	/**
	 * Non ascii.
	 *
	 * @param s the s
	 * @return true, if successful
	 */
	private static boolean nonAscii(String s) {
		int len = s!=null?s.length():0;
		for (int i=0; i<len; i++) {
			if (s.charAt(i)>127)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if is text or table.
	 *
	 * @param path the path
	 * @return true, if is text or table
	 */
	static boolean isTextOrTable(String path) {
		return path.endsWith(".txt") || path.endsWith(".csv") || path.endsWith(".xls");
	}
	
	/**
	 * Checks if is save as.
	 *
	 * @return true, if is save as
	 */
	static boolean isSaveAs() {
		return commandName.equals("Tiff...")
			|| commandName.equals("Gif...")
			|| commandName.equals("Jpeg...")
			|| commandName.equals("Text Image...")
			|| commandName.equals("ZIP...")
			|| commandName.equals("Raw Data...")
			|| commandName.equals("BMP...")
			|| commandName.equals("PNG...")
			|| commandName.equals("PGM...")
			|| commandName.equals("FITS...")
			|| commandName.equals("LUT...")
			|| commandName.equals("Selection...")
			|| commandName.equals("XY Coordinates...")
			//|| commandName.equals("Results...")
			|| commandName.equals("Text... ");
	}

	/**
	 * Append new image.
	 *
	 * @param hyperstack the hyperstack
	 */
	static void appendNewImage(boolean hyperstack) {
		String options = getCommandOptions() + " ";
		//IJ.log("appendNewImage: "+options);
		String title = Macro.getValue(options, "name", "Untitled");
		String type = Macro.getValue(options, "type", "8-bit");
		String fill = Macro.getValue(options, "fill", "");
		if (!fill.equals(""))
			type = type +" " + fill.toLowerCase();
		if (hyperstack) {
			String mode = Macro.getValue(options, "display", "");
			if (!mode.equals(""))
				type = type +" " + mode.toLowerCase() + "-mode";
			if (options.contains(" label"))
				type = type +" label";
		}
		int width = (int)Tools.parseDouble(Macro.getValue(options, "width", "512"));
		int height = (int)Tools.parseDouble(Macro.getValue(options, "height", "512"));
		String d1= ", " + (int)Tools.parseDouble(Macro.getValue(options, "slices", "1"));
		String d2="", d3="";
		if (hyperstack) {
			d1 = ", " + (int)Tools.parseDouble(Macro.getValue(options, "channels", "1"));
			d2 = ", " + (int)Tools.parseDouble(Macro.getValue(options, "slices", "1"));
			d3 = ", " + (int)Tools.parseDouble(Macro.getValue(options, "frames", "1"));
		}
		textArea.append((scriptMode?"imp = IJ.createImage":"newImage")
			+"(\""+title+"\", "+"\""+type+"\", "+width+", "+height+d1+d2+d3+");\n");
	}

	/**
	 * Strip.
	 *
	 * @param value the value
	 * @return the string
	 */
	static String strip(String value) {
		int index = value.indexOf('=');
		if (index>=0)
			value = value.substring(index+1);
		if (value.startsWith("[")) {
			int index2 = value.indexOf(']');
			if (index2==-1) index2 = value.length();
			value = value.substring(1, index2);
		} else {
			index = value.indexOf(' ');
			if (index!=-1)
				value = value.substring(0, index);
		}
		return value;
	}

	/**
	 * Adds the quotes.
	 *
	 * @param value the value
	 * @return the string
	 */
	static String addQuotes(String value) {
		int index = value.indexOf(' ');
		if (index>-1)
			value = "["+value+"]";
		return value;
	}
	
	/**
	 *  Used by GenericDialog to determine if any options have been recorded.
	 *
	 * @return the command options
	 */
	static public String getCommandOptions() {
		return commandOptions;
	}

	/**
	 * Creates the macro.
	 */
	void createMacro() {
		String text = textArea.getText();
		if (text==null || text.equals("")) {
			IJ.showMessage("Recorder", "A macro cannot be created until at least\none command has been recorded.");
			return;
		}
		Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
		if (ed==null)
			return;
		boolean java = mode.getSelectedItem().equals(modes[JAVA]);
		boolean beanshell = mode.getSelectedItem().equals(modes[BEANSHELL]);
		String name = fileName.getText();
		int dotIndex = name.lastIndexOf(".");
		if (scriptMode) { // JavaScript, BeanShell or Java
			if (dotIndex>=0) name = name.substring(0, dotIndex);
			if (text.indexOf("rm.")!=-1) {
				text = (java?"RoiManager ":"")+ "rm = RoiManager.getInstance();\n"
				+ "if (rm==null) rm = new RoiManager();\n"
				+ text;
			}
			if (text.contains("overlay.add"))
				text = (java?"Overlay ":"") + "overlay = new Overlay();\n" + text;
			if ((text.contains("imp.")||text.contains("(imp")||text.contains("overlay.add")) && !text.contains("IJ.openImage")
			&& !text.contains("IJ.openVirtual") && !text.contains("IJ.createImage"))
				text = (java?"ImagePlus ":"") + "imp = IJ.getImage();\n" + text;
			if (text.contains("overlay.add"))
				text = text + "imp.setOverlay(overlay);\n";
			if (text.indexOf("imp =")!=-1 && !(text.indexOf("IJ.getImage")!=-1||text.indexOf("IJ.saveAs")!=-1||text.indexOf("imp.close")!=-1))
				text = text + "imp.show();\n";
			if (java) {
				name += ".java";
				createPlugin(text, name);
				return;
			} else if (beanshell)
				name += ".bsh";
			else
				name += ".js";
		} else { // ImageJ macro
			if (!name.endsWith(".txt")) {
				if (dotIndex>=0) name = name.substring(0, dotIndex);
				name += ".ijm";
			}
		}
		ed.createMacro(name, text);
		fgColorSet = bgColorSet = false;
		bbSet = false;
	}
	
	/**
	 * Creates the plugin.
	 *
	 * @param text the text
	 * @param name the name
	 */
	void createPlugin(String text, String name) {
		StringTokenizer st = new StringTokenizer(text, "\n");
		int n = st.countTokens();
		boolean impDeclared = false;
		boolean lutDeclared = false;
		String line;
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<n; i++) {
			line = st.nextToken();
			if (line!=null && line.length()>3) {
				sb.append("\t\t");
				if (line.startsWith("imp =") && !impDeclared) {
					sb.append("ImagePlus ");
					impDeclared = true;
				}
				if (line.startsWith("lut =") && !lutDeclared) {
					sb.append("LUT ");
					lutDeclared = true;
				}
				sb.append(line);
				sb.append('\n');
			}
		}
		String text2 = new String(sb);
		text2 = text2.replaceAll("print", "IJ.log");
		NewPlugin np = (NewPlugin)IJ.runPlugIn("ij.plugin.NewPlugin", text2);
		Editor ed = np.getEditor();
		ed.updateClassName(ed.getTitle(), name);
		ed.setTitle(name);
	}

	/** Temporarily disables path recording. */
	public static void disablePathRecording() {
		recordPath = false;
	}
	
	/**
	 * Script mode.
	 *
	 * @return true, if successful
	 */
	public static boolean scriptMode() {
		return scriptMode;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==makeMacro)
			createMacro();
		else if (e.getSource()==help)
			showHelp();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		setFileName();
		Prefs.set("recorder.mode", mode.getSelectedItem());
	}
	
	/**
	 * Sets the file name.
	 */
	void setFileName() {
		String name = mode.getSelectedItem();
		scriptMode = !name.equals(modes[MACRO]);
		if (name.equals(modes[MACRO]))
			fileName.setText("Macro.ijm");
		else if (name.equals(modes[JAVASCRIPT]))
			fileName.setText("Script.js");
		else if (name.equals(modes[BEANSHELL]))
			fileName.setText("Script.bsh");
		else
			fileName.setText("My_Plugin.java");
		fgColorSet = bgColorSet = false;
		bbSet = false;
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageUpdated(ij.ImagePlus)
	 */
	public void imageUpdated(ImagePlus imp) {
		if (imp.getID()==imageID)
			imageUpdated = true;
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageOpened(ij.ImagePlus)
	 */
	public void imageOpened(ImagePlus imp) { }

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageClosed(ij.ImagePlus)
	 */
	public void imageClosed(ImagePlus imp) { }

    /**
     * Show help.
     */
    void showHelp() {
    	IJ.showMessage("Recorder",
			"Click \"Create\" to open recorded commands\n"  
			+"as a macro in an editor window.\n" 
			+" \n" 
			+"In the editor:\n" 
			+" \n"
			+"    Type ctrl+R (Macros>Run Macro) to\n" 
			+"    run the macro.\n"     
			+" \n"    
			+"    Use File>Save As to save it and\n" 
			+"    ImageJ's Open command to open it.\n" 
			+" \n"    
			+"    To create a command, save in the plugins\n"  
			+"    folder and run Help>Refresh Menus.\n"  
		);
    }
    
	/* (non-Javadoc)
	 * @see ij.plugin.frame.PlugInFrame#close()
	 */
	public void close() {
		super.close();
		record = false;
		textArea = null;
		commandName = null;
		instance = null;	
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		if (textArea==null)
			return "";
		else
			return textArea.getText();
	}
	
	/**
	 * Gets the single instance of Recorder.
	 *
	 * @return single instance of Recorder
	 */
	public static Recorder getInstance() {
		return instance;
	}
	
	/**
	 * Sets the foreground color.
	 *
	 * @param c the new foreground color
	 */
	public static void setForegroundColor(Color c) {
		record("setForegroundColor", c.getRed(), c.getGreen(), c.getBlue());
		fgColorSet = true;
	}
	
	/**
	 * Sets the background color.
	 *
	 * @param c the new background color
	 */
	public static void setBackgroundColor(Color c) {
		record("setBackgroundColor", c.getRed(), c.getGreen(), c.getBlue());
		bgColorSet = true;
	}
	
	/**
	 * Sets the black background.
	 */
	public static void setBlackBackground() {
		String bb = Prefs.blackBackground?"true":"false";
		if (scriptMode)
			recordString("Prefs.blackBackground = "+bb+";\n");
		else
			recordString("setOption(\"BlackBackground\", "+bb+");\n");
		bbSet = true;
	}

}
