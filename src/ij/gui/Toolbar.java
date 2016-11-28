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
import ij.IJEventListener;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.macro.Program;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Editor;
import ij.plugin.frame.Recorder;
import ij.plugin.tool.MacroToolRunner;
import ij.plugin.tool.PlugInTool;

import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Polygon;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/** The ImageJ toolbar. */
public class Toolbar extends Canvas implements MouseListener, MouseMotionListener, ItemListener, ActionListener {

	/** The Constant RECTANGLE. */
	public static final int RECTANGLE = 0;
	
	/** The Constant OVAL. */
	public static final int OVAL = 1;
	
	/** The Constant POLYGON. */
	public static final int POLYGON = 2;
	
	/** The Constant FREEROI. */
	public static final int FREEROI = 3;
	
	/** The Constant LINE. */
	public static final int LINE = 4;
	
	/** The Constant POLYLINE. */
	public static final int POLYLINE = 5;
	
	/** The Constant FREELINE. */
	public static final int FREELINE = 6;
	
	/** The Constant CROSSHAIR. */
	public static final int POINT = 7, CROSSHAIR = 7;
	
	/** The Constant WAND. */
	public static final int WAND = 8;
	
	/** The Constant TEXT. */
	public static final int TEXT = 9;
	
	/** The Constant UNUSED. */
	public static final int UNUSED = 10;
	
	/** The Constant MAGNIFIER. */
	public static final int MAGNIFIER = 11;
	
	/** The Constant HAND. */
	public static final int HAND = 12;
	
	/** The Constant DROPPER. */
	public static final int DROPPER = 13;
	
	/** The Constant ANGLE. */
	public static final int ANGLE = 14;
	
	/** The Constant CUSTOM1. */
	public static final int CUSTOM1 = 15;
	
	/** The Constant CUSTOM2. */
	public static final int CUSTOM2 = 16;
	
	/** The Constant CUSTOM3. */
	public static final int CUSTOM3 = 17;
	
	/** The Constant CUSTOM4. */
	public static final int CUSTOM4 = 18;
	
	/** The Constant CUSTOM5. */
	public static final int CUSTOM5 = 19;
	
	/** The Constant CUSTOM6. */
	public static final int CUSTOM6 = 20;
	
	/** The Constant CUSTOM7. */
	public static final int CUSTOM7 = 21;
	
	/** The Constant DOUBLE_CLICK_THRESHOLD. */
	public static final int DOUBLE_CLICK_THRESHOLD = 650;

	/** The Constant BRUSH_ROI. */
	public static final int OVAL_ROI=0, ELLIPSE_ROI=1, BRUSH_ROI=2;
	
	/** The Constant builtInTools. */
	private static final String[] builtInTools = {"Arrow","Brush","Developer Menu","Flood Filler",
		"LUT Menu","Overlay Brush","Pencil","Pixel Inspector","Spray Can","Stacks Menu"};
	
	/** The Constant builtInTools2. */
	private static final String[] builtInTools2 = {"Pixel Inspection Tool","Paintbrush Tool","Flood Fill Tool"};

	/** The Constant NUM_TOOLS. */
	private static final int NUM_TOOLS = 23;
	
	/** The Constant MAX_EXTRA_TOOLS. */
	private static final int MAX_EXTRA_TOOLS = 8;
	
	/** The Constant MAX_TOOLS. */
	private static final int MAX_TOOLS = NUM_TOOLS+MAX_EXTRA_TOOLS;
	
	/** The Constant NUM_BUTTONS. */
	private static final int NUM_BUTTONS = 21;
	
	/** The Constant SIZE. */
	private static final int SIZE = 28;
	
	/** The Constant GAP_SIZE. */
	private static final int GAP_SIZE = 9;
	
	/** The Constant OFFSET. */
	private static final int OFFSET = 6;
	
	/** The Constant BRUSH_SIZE. */
	private static final String BRUSH_SIZE = "toolbar.brush.size";
	
	/** The Constant CORNER_DIAMETER. */
	public static final String CORNER_DIAMETER = "toolbar.arc.size";
	
	/** The tool key. */
	public static String TOOL_KEY = "toolbar.tool";
		
	/** The ps. */
	private Dimension ps = new Dimension(SIZE*NUM_BUTTONS-(SIZE-GAP_SIZE), SIZE);
	
	/** The down. */
	private boolean[] down;
	
	/** The current. */
	private static int current;
	
	/** The previous. */
	private int previous;
	
	/** The y. */
	private int x,y;
	
	/** The y offset. */
	private int xOffset, yOffset;
	
	/** The mouse down time. */
	private long mouseDownTime;
	
	/** The g. */
	private Graphics g;
	
	/** The instance. */
	private static Toolbar instance;
	
	/** The mp previous. */
	private int mpPrevious = RECTANGLE;
	
	/** The names. */
	private String[] names = new String[MAX_TOOLS];
	
	/** The icons. */
	private String[] icons = new String[MAX_TOOLS];
	
	/** The tools. */
	private PlugInTool[] tools = new PlugInTool[MAX_TOOLS];
	
	/** The menus. */
	private PopupMenu[] menus = new PopupMenu[MAX_TOOLS];
	
	/** The n extra tools. */
	private int nExtraTools;
	
	/** The macro installer. */
	private MacroInstaller macroInstaller;
	
	/** The adding single tool. */
	private boolean addingSingleTool;
	
	/** The installing startup tool. */
	private boolean installingStartupTool;
	
	/** The do not save prefs. */
	private boolean doNotSavePrefs;
	
	/** The pc. */
	private int pc;
	
	/** The icon. */
	private String icon;
	
	/** The startup time. */
	private int startupTime;
	
	/** The switch popup. */
	private PopupMenu rectPopup, ovalPopup, pointPopup, linePopup, switchPopup;
	
	/** The round rect item. */
	private CheckboxMenuItem rectItem, roundRectItem;
	
	/** The brush item. */
	private CheckboxMenuItem ovalItem, ellipseItem, brushItem;
	
	/** The multi point item. */
	private CheckboxMenuItem pointItem, multiPointItem;
	
	/** The arrow item. */
	private CheckboxMenuItem straightLineItem, polyLineItem, freeLineItem, arrowItem;
	
	/** The current set. */
	private String currentSet = "Startup Macros";

	/** The foreground color. */
	private static Color foregroundColor = Prefs.getColor(Prefs.FCOLOR,Color.white);
	
	/** The background color. */
	private static Color backgroundColor = Prefs.getColor(Prefs.BCOLOR,Color.black);
	
	/** The oval type. */
	private static int ovalType = OVAL_ROI;
	
	/** The multi point mode. */
	private static boolean multiPointMode = Prefs.multiPointMode;
	
	/** The round rect mode. */
	private static boolean roundRectMode;
	
	/** The arrow mode. */
	private static boolean arrowMode;
	
	/** The brush size. */
	private static int brushSize = (int)Prefs.get(BRUSH_SIZE, 15);
	
	/** The arc size. */
	private static int arcSize = (int)Prefs.get(CORNER_DIAMETER, 20);
	
	/** The line type. */
	private int lineType = LINE;
	
	/** The legacy mode. */
	private static boolean legacyMode;
	
	/** The gray. */
	private Color gray = new Color(228,228,228);
	
	/** The brighter. */
	private Color brighter = gray.brighter();
	
	/** The darker. */
	private Color darker = new Color(180, 180, 180);
	
	/** The even darker. */
	private Color evenDarker = new Color(110, 110, 110);
	
	/** The triangle color. */
	private Color triangleColor = new Color(150, 0, 0);
	
	/** The tool color. */
	private Color toolColor = new Color(0, 25, 45);
	
	/**  Obsolete public constants. */
	public static final int SPARE1=UNUSED, SPARE2=CUSTOM1, SPARE3=CUSTOM2, SPARE4=CUSTOM3, 
		SPARE5=CUSTOM4, SPARE6=CUSTOM5, SPARE7=CUSTOM6, SPARE8=CUSTOM7, SPARE9=22;


	/**
	 * Instantiates a new toolbar.
	 */
	public Toolbar() {
		down = new boolean[MAX_TOOLS];
		resetButtons();
		down[0] = true;
		setForeground(Color.black);
		setBackground(ImageJ.backgroundColor);
		addMouseListener(this);
		addMouseMotionListener(this);
		instance = this;
		names[getNumTools()-1] = "\"More Tools\" menu (switch toolsets or add tools)";
		icons[getNumTools()-1] = "C900T1c13>T7c13>"; // ">>"
		addPopupMenus();
	}

	/**
	 * Adds the popup menus.
	 */
	void addPopupMenus() {
		rectPopup = new PopupMenu();
		if (Menus.getFontSize()!=0)
			rectPopup.setFont(Menus.getFont());
		rectItem = new CheckboxMenuItem("Rectangle Tool", !roundRectMode);
		rectItem.addItemListener(this);
		rectPopup.add(rectItem);
		roundRectItem = new CheckboxMenuItem("Rounded Rectangle Tool", roundRectMode);
		roundRectItem.addItemListener(this);
		rectPopup.add(roundRectItem);
		add(rectPopup);

		ovalPopup = new PopupMenu();
		if (Menus.getFontSize()!=0)
			ovalPopup.setFont(Menus.getFont());
		ovalItem = new CheckboxMenuItem("Oval selections", ovalType==OVAL_ROI);
		ovalItem.addItemListener(this);
		ovalPopup.add(ovalItem);
		ellipseItem = new CheckboxMenuItem("Elliptical selections", ovalType==ELLIPSE_ROI);
		ellipseItem.addItemListener(this);
		ovalPopup.add(ellipseItem);
		brushItem = new CheckboxMenuItem("Selection Brush Tool", ovalType==BRUSH_ROI);
		brushItem.addItemListener(this);
		ovalPopup.add(brushItem);
		add(ovalPopup);

		pointPopup = new PopupMenu();
		if (Menus.getFontSize()!=0)
			pointPopup.setFont(Menus.getFont());
		pointItem = new CheckboxMenuItem("Point Tool", !multiPointMode);
		pointItem.addItemListener(this);
		pointPopup.add(pointItem);
		multiPointItem = new CheckboxMenuItem("Multi-point Tool", multiPointMode);
		multiPointItem.addItemListener(this);
		pointPopup.add(multiPointItem);
		add(pointPopup);

		linePopup = new PopupMenu();
		if (Menus.getFontSize()!=0)
			linePopup.setFont(Menus.getFont());
		straightLineItem = new CheckboxMenuItem("Straight Line", lineType==LINE&&!arrowMode);
		straightLineItem.addItemListener(this);
		linePopup.add(straightLineItem);
		polyLineItem = new CheckboxMenuItem("Segmented Line", lineType==POLYLINE);
		polyLineItem.addItemListener(this);
		linePopup.add(polyLineItem);
		freeLineItem = new CheckboxMenuItem("Freehand Line", lineType==FREELINE);
		freeLineItem.addItemListener(this);
		linePopup.add(freeLineItem);
		arrowItem = new CheckboxMenuItem("Arrow tool", lineType==LINE&&!arrowMode);
		arrowItem.addItemListener(this);
		linePopup.add(arrowItem);
		add(linePopup);

		switchPopup = new PopupMenu();
		if (Menus.getFontSize()!=0)
			switchPopup.setFont(Menus.getFont());
		add(switchPopup);
	}
	
	/**
	 *  Returns the ID of the current tool (Toolbar.RECTANGLE, Toolbar.OVAL, etc.).
	 *
	 * @return the tool id
	 */
	public static int getToolId() {
		int id = current;
		if (legacyMode) {
			if (id==CUSTOM1)
				id=UNUSED;
			else if (id>=CUSTOM2)
				id--;
		}
		return id;
	}

	/**
	 *  Returns the ID of the tool whose name (the description displayed in the status bar)
	 * 		starts with the specified string, or -1 if the tool is not found.
	 *
	 * @param name the name
	 * @return the tool id
	 */
	public int getToolId(String name) {
		int tool =  -1;
		for (int i=0; i<getNumTools(); i++) {
			if (names[i]!=null && names[i].startsWith(name)) {
				tool = i;
				break;
			}			
		}
		return tool;
	}

	/**
	 *  Returns a reference to the ImageJ toolbar.
	 *
	 * @return single instance of Toolbar
	 */
	public static Toolbar getInstance() {
		return instance;
	}

	/**
	 * Draw buttons.
	 *
	 * @param g the g
	 */
	private void drawButtons(Graphics g) {
		if (g==null)
			return;
		if (Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		for (int i=0; i<LINE; i++)
			drawButton(g, i);
		drawButton(g, lineType);
		for (int i=POINT; i<getNumTools(); i++)
			drawButton(g, i);
	}

	/**
	 * Fill 3 D rect.
	 *
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 * @param raised the raised
	 */
	private void fill3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
		if (null==g) return;
		if (raised)
			g.setColor(gray);
		else
			g.setColor(darker);
		g.fillRect(x+1, y+1, width-2, height-2);
		g.setColor(raised ? brighter : evenDarker);
		g.drawLine(x, y, x, y + height - 1);
		g.drawLine(x + 1, y, x + width - 2, y);
		g.setColor(raised ? evenDarker : brighter);
		g.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
		g.drawLine(x + width - 1, y, x + width - 1, y + height - 2);
	}    

	/**
	 * Draw button.
	 *
	 * @param g the g
	 * @param tool the tool
	 */
	private void drawButton(Graphics g, int tool) {
		if (g==null) return;
		if (legacyMode) {
			if (tool==UNUSED)
				tool = CUSTOM1;
			else if (tool>=CUSTOM1)
				tool++;
			if ((tool==POLYLINE && lineType!=POLYLINE) || (tool==FREELINE && lineType!=FREELINE))
				return;
		}
        int index = toolIndex(tool);
        int x = index*SIZE + 1;
        if (tool>=CUSTOM1)
        	x -= SIZE-GAP_SIZE;
        if (tool!=UNUSED)
        	fill3DRect(g, x, 1, SIZE, SIZE-1, !down[tool]);
        g.setColor(toolColor);
        x = index*SIZE + OFFSET;
        if (tool>=CUSTOM1)
        	x -= SIZE-GAP_SIZE;
		int y = OFFSET;
		if (down[tool]) { x++; y++;}
		this.g = g;
		if (tool>=CUSTOM1 && tool<=getNumTools() && icons[tool]!=null) {
			drawIcon(g, tool, x, y);
			return;
		}
		switch (tool) {
			case RECTANGLE:
				xOffset = x; yOffset = y;
				if (roundRectMode)
					g.drawRoundRect(x, y+1, 17, 13, 8, 8);
				else
					g.drawRect(x, y+1, 17, 13);
				drawTriangle(16,15);
				return;
			case OVAL:
				xOffset = x; yOffset = y;
				if (ovalType==BRUSH_ROI) {
					yOffset = y - 1;
					polyline(6,4,8,2,12,1,15,2,16,4,15,7,12,8,9,11,9,14,6,16,2,16,0,13,1,10,4,9,6,7,6,4);
				} else if (ovalType==ELLIPSE_ROI) {
					yOffset = y + 1;
					polyline(11,0,13,0,14,1,15,1,16,2,17,3,17,7,12,12,11,12,10,13,8,13,7,14,4,14,3,13,2,13,1,12,1,11,0,10,0,9,1,8,1,7,6,2,7,2,8,1,10,1,11,0);
				} else
					g.drawOval(x, y+1, 17, 13);
				drawTriangle(16,15);
				return;
			case POLYGON:
				xOffset = x+1; yOffset = y+2;
				polyline(4,0,15,0,15,1,11,5,11,6,14,10,14,11,0,11,0,4,4,0);
				return;
			case FREEROI:
				xOffset = x; yOffset = y+2;
				polyline(2,0,5,0,7,3,10,3,12,0,15,0,17,2,17,5,16,8,13,10,11,11,6,11,4,10,1,8,0,6,0,2,2,0); 
				return;
			case LINE:
				xOffset = x; yOffset = y;
				if (arrowMode) {
					m(1,14); d(14,1); m(6,5); d(14,1); m(10,9); d(14,1); m(6,5); d(10,9);
				} else {
					m(0,12); d(17,3);
					drawDot(0,11); drawDot(17,2);
				}
				drawTriangle(12,14);
				return;
			case POLYLINE:
				xOffset = x; yOffset = y;
				polyline(15,6,11,2,1,2,1,3,7,9,2,14);
				drawTriangle(12,14);
				return;
			case FREELINE:
				xOffset = x; yOffset = y;
				polyline(16,4,14,6,12,6,9,3,8,3,6,7,2,11,1,11);
				drawTriangle(12,14);
				return;
			case POINT:
				xOffset = x; yOffset = y;
				if (multiPointMode) {
					drawPoint(1,3); drawPoint(9,1); drawPoint(15,5);
					drawPoint(10,11); drawPoint(2,12);
				} else {
					m(1,8); d(6,8); d(6,6); d(10,6); d(10,10); d(6,10); d(6,9);
					m(8,1); d(8,5); m(11,8); d(15,8); m(8,11); d(8,15);
					m(8,8); d(8,8);
					g.setColor(Roi.getColor());
					g.fillRect(x+7, y+7, 3, 3);
				}
				drawTriangle(14,14);
				return;
			case WAND:
				xOffset = x+2; yOffset = y+1;
				dot(4,0);  m(2,0); d(3,1); d(4,2);  m(0,0); d(1,1);
				m(0,2); d(1,3); d(2,4);  dot(0,4); m(3,3); d(13,13);
				g.setColor(Roi.getColor());
				m(1,2); d(3,2); m(2,1); d(2,3);
				return;
			case TEXT:
				xOffset = x+2; yOffset = y+1;
				m(0,13); d(3,13);
				m(1,12); d(7,0); d(12,13);
				m(11,13); d(14,13);
				m(3,8); d(10,8);
				return;
			case MAGNIFIER:
				xOffset = x+2; yOffset = y+2;
				polyline(3,0,3,0,5,0,8,3,8,5,7,6,7,7,6,7,5,8,3,8,0,5,0,3,3,0);
				polyline(8,8,9,8,13,12,13,13,12,13,8,9,8,8);
				return;
			case HAND:
				xOffset = x+1; yOffset = y+1;
				polyline(5,14,2,11,2,10,0,8,0,7,1,6,2,6,4,8,4,6,3,5,3,4,2,3,2,2,3,1,4,1,5,2,5,3);
				polyline(6,5,6,1,7,0,8,0,9,1,9,5,9,1,11,1,12,2,12,6);
				polyline(13,4,14,3,15,4,15,7,14,8,14,10,13,11,13,12,12,13,12,14);
				return;
			case DROPPER:
				xOffset = x; yOffset = y;
				g.setColor(foregroundColor);
				m(12,2); d(14,2);
				m(11,3); d(15,3);
				m(11,4); d(15,4);
				m(8,5); d(15,5);
				m(9,6); d(14,6);
				polyline(10,7,12,7,12,9);
				polyline(8,7,2,13,2,15,4,15,11,8);
				g.setColor(backgroundColor);
				polyline(-1,-1,18,-1,18,17,-1,17,-1,-1);
				return;
			case ANGLE:
				xOffset = x; yOffset = y+2;
				m(0,11); d(11,0); m(0,11); d(15,11); 
				m(10,11); d(10,8); m(9,7); d(9,6); dot(8,5);
				drawDot(11,-1); drawDot(15,10);
				return;
		}
	}
	
	/**
	 * Draw triangle.
	 *
	 * @param x the x
	 * @param y the y
	 */
	void drawTriangle(int x, int y) {
		g.setColor(triangleColor);
		xOffset+=x; yOffset+=y;
		m(0,0); d(4,0); m(1,1); d(3,1); dot(2,2);
	}
	
	/**
	 * Draw dot.
	 *
	 * @param x the x
	 * @param y the y
	 */
	void drawDot(int x, int y) {
		g.fillRect(xOffset+x, yOffset+y, 2, 2);
	}

	/**
	 * Draw point.
	 *
	 * @param x the x
	 * @param y the y
	 */
	void drawPoint(int x, int y) {
		g.setColor(toolColor);
		m(x-2,y); d(x+2,y);
		m(x,y-2); d(x,y+2);
		g.setColor(Roi.getColor());
		dot(x,y);
	}
	
	/**
	 * Draw icon.
	 *
	 * @param g the g
	 * @param tool the tool
	 * @param x the x
	 * @param y the y
	 */
	void drawIcon(Graphics g, int tool, int x, int y) {
		if (null==g) return;
		icon = icons[tool];
		if (icon==null) return;
		this.icon = icon;
		int x1, y1, x2, y2;
		pc = 0;
		while (true) {
			char command = icon.charAt(pc++);
			if (pc>=icon.length()) break;
			switch (command) {
				case 'B': x+=v(); y+=v(); break;  // reset base
				case 'R': g.drawRect(x+v(), y+v(), v(), v()); break;  // rectangle
				case 'F': g.fillRect(x+v(), y+v(), v(), v()); break;  // filled rectangle
				case 'O': g.drawOval(x+v(), y+v(), v(), v()); break;  // oval
				case 'V': case 'o': g.fillOval(x+v(), y+v(), v(), v()); break;  // filled oval
				case 'C': // set color
					int v1=v(), v2=v(), v3=v();
					int red=v1*16, green=v2*16, blue=v3*16;
					if (red>255) red=255; if (green>255) green=255; if (blue>255) blue=255;
					Color color = v1==1&&v2==2&&v3==3?foregroundColor:new Color(red,green,blue);
					g.setColor(color);
					break; 
				case 'L': g.drawLine(x+v(), y+v(), x+v(), y+v()); break; // line
				case 'D': g.fillRect(x+v(), y+v(), 1, 1); break; // dot
				case 'P': // polyline
					Polygon p = new Polygon();
					p.addPoint(x+v(), y+v());
					while (true) {
						x2=v(); if (x2==0) break;
						y2=v(); if (y2==0) break;
						p.addPoint(x+x2, y+y2);
					}
					g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
					break;
				case 'G': case 'H':// polygon or filled polygon
					p = new Polygon();
					p.addPoint(x+v(), y+v());
					while (true) {
						x2=v(); y2=v();
						if (x2==0 && y2==0 && p.npoints>2)
							break;
						p.addPoint(x+x2, y+y2);
					}
					if (command=='G')
						g.drawPolygon(p.xpoints, p.ypoints, p.npoints);
					else
						g.fillPolygon(p.xpoints, p.ypoints, p.npoints);
					break;
				case 'T': // text (one character)
					x2 = x+v()-1;
					y2 = y+v();
					int size = v()*10+v()+1;
					char[] c = new char[1];
					c[0] = pc<icon.length()?icon.charAt(pc++):'e';
					g.setFont(new Font("SansSerif", Font.PLAIN, size));
					g.drawString(new String(c), x2, y2);
					break;
				default: break;
			}
			if (pc>=icon.length()) break;
		}
		if (menus[tool]!=null && menus[tool].getItemCount()>0) { 
			xOffset = x; yOffset = y;
			drawTriangle(14, 14);
		}
	}
	
	/**
	 * V.
	 *
	 * @return the int
	 */
	int v() {
		if (pc>=icon.length()) return 0;
		char c = icon.charAt(pc++);
		switch (c) {
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			case 'a': return 10;
			case 'b': return 11;
			case 'c': return 12;
			case 'd': return 13;
			case 'e': return 14;
			case 'f': return 15;
			case 'g': return 16;
			case 'h': return 17;
			default: return 0;
		}
	}
	
	/**
	 * Show message.
	 *
	 * @param tool the tool
	 */
	private void showMessage(int tool) {
		if (tool>=UNUSED && tool<getNumTools() && names[tool]!=null) {
			String name = names[tool];
			int index = name.indexOf("Action Tool");
			if (index!=-1)
				name = name.replace("Action Tool", "Tool");
			else {
				index = name.indexOf("Menu Tool");
				if (index!=-1)
					name = name.substring(0, index+4);
			}
			IJ.showStatus(name);
			return;
		}
		String hint = " (right click to switch)";
		String hint2 = " (right click to switch; double click to configure)";
		switch (tool) {
			case RECTANGLE:
				if (roundRectMode)
					IJ.showStatus("Rectangular or *rounded rectangular* selections"+hint);
				else
					IJ.showStatus("*Rectangular* or rounded rectangular selections"+hint);
				return;
			case OVAL:
				if (ovalType==BRUSH_ROI)
					IJ.showStatus("Oval, elliptical or *brush* selections"+hint);
				else if (ovalType==ELLIPSE_ROI)
					IJ.showStatus("Oval, *elliptical* or brush selections"+hint);
				else
					IJ.showStatus("*Oval*, elliptical or brush selections"+hint);
				return;
			case POLYGON:
				IJ.showStatus("Polygon selections");
				return;
			case FREEROI:
				IJ.showStatus("Freehand selections");
				return;
			case LINE:
				if (arrowMode)
					IJ.showStatus("Straight, segmented or freehand lines, or *arrows*"+hint);
				else
					IJ.showStatus("*Straight*, segmented or freehand lines, or arrows"+hint);
				return;
			case POLYLINE:
				IJ.showStatus("Straight, *segmented* or freehand lines, or arrows"+hint);
				return;
			case FREELINE:
				IJ.showStatus("Straight, segmented or *freehand* lines, or arrows"+hint);
				return;
			case POINT:
				if (multiPointMode)
					IJ.showStatus("*Multi-point* or point"+hint2);
				else
					IJ.showStatus("*Point* or multi-point"+hint2);
				return;
			case WAND:
				IJ.showStatus("Wand (tracing) tool");
				return;
			case TEXT:
				IJ.showStatus("Text tool (double-click to configure)");
				return;
			case MAGNIFIER:
				IJ.showStatus("Magnifying glass (or use \"+\" and \"-\" keys)");
				return;
			case HAND:
				IJ.showStatus("Scrolling tool (or press space bar and drag)");
				return;
			case DROPPER:
				String fg = foregroundColor.getRed() + "," + foregroundColor.getGreen() + "," + foregroundColor.getBlue();
				String bg = backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue();
				IJ.showStatus("Color picker (" +  fg + "/"+ bg + ")");
				return;
			case ANGLE:
				IJ.showStatus("Angle tool");
				return;
			default:
				IJ.showStatus("ImageJ "+IJ.getVersion()+" / Java "+System.getProperty("java.version")+(IJ.is64Bit()?" (64-bit)":" (32-bit)"));
				return;
		}
	}

	/**
	 * M.
	 *
	 * @param x the x
	 * @param y the y
	 */
	private void m(int x, int y) {
		this.x = xOffset+x;
		this.y = yOffset+y;
	}

	/**
	 * D.
	 *
	 * @param x the x
	 * @param y the y
	 */
	private void d(int x, int y) {
		x += xOffset;
		y += yOffset;
		g.drawLine(this.x, this.y, x, y);
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Dot.
	 *
	 * @param x the x
	 * @param y the y
	 */
	private void dot(int x, int y) {
		g.fillRect(x+xOffset, y+yOffset, 1, 1);
	}
	
	/**
	 * Polyline.
	 *
	 * @param values the values
	 */
	private void polyline(int... values) {
		Polygon p = new Polygon();
		int n = values.length/2;
		for (int i=0; i<n; i++)
			p.addPoint(values[i*2]+xOffset, values[i*2+1]+yOffset);
		g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
	}

	/**
	 * Reset buttons.
	 */
	private void resetButtons() {
		for (int i=0; i<getNumTools(); i++)
			down[i] = false;
	}

	/* (non-Javadoc)
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		drawButtons(g);
	}

	/**
	 * Sets the tool.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	public boolean setTool(String name) {
		if (name==null) return false;
		if (name.indexOf(" Tool")!=-1) { // macro tool?
			for (int i=UNUSED; i<getNumTools(); i++) {
				if (name.equals(names[i])) {
					setTool(i);
					return true;
				}
			}
		}
		name = name.toLowerCase(Locale.US);
		boolean ok = true;
		if (name.indexOf("round")!=-1) {
			roundRectMode = true;
			setTool(RECTANGLE);
		} else if (name.indexOf("rect")!=-1) {
			roundRectMode = false;
			setTool(RECTANGLE);
		} else if (name.indexOf("oval")!=-1) {
			ovalType = OVAL_ROI;
			setTool(OVAL);
		} else if (name.indexOf("ellip")!=-1) {
			ovalType = ELLIPSE_ROI;
			setTool(OVAL);
		} else if (name.indexOf("brush")!=-1) {
			ovalType = BRUSH_ROI;
			setTool(OVAL);
		} else if (name.indexOf("polygon")!=-1)
			setTool(POLYGON);
		else if (name.indexOf("polyline")!=-1)
			setTool(POLYLINE);
		else if (name.indexOf("freeline")!=-1)
			setTool(FREELINE);
		else if (name.indexOf("line")!=-1) {
			arrowMode = false;
			setTool(LINE);
		} else if (name.indexOf("arrow")!=-1) {
			arrowMode = true;
			setTool(LINE);
		} else if (name.indexOf("free")!=-1)
			setTool(FREEROI);
		else if (name.indexOf("multi")!=-1) {
			multiPointMode = true;
			Prefs.multiPointMode = true;
			setTool(POINT);
		} else if (name.indexOf("point")!=-1) {
			multiPointMode = false;
			Prefs.multiPointMode = false;
			setTool(POINT);
		} else if (name.indexOf("wand")!=-1)
			setTool(WAND);
		else if (name.indexOf("text")!=-1)
			setTool(TEXT);
		else if (name.indexOf("hand")!=-1)
			setTool(HAND);
		else if (name.indexOf("zoom")!=-1)
			setTool(MAGNIFIER);
		else if (name.indexOf("dropper")!=-1||name.indexOf("color")!=-1)
			setTool(DROPPER);
		else if (name.indexOf("angle")!=-1)
			setTool(ANGLE);
		else
			ok = false;
		return ok;
	}
	
	/**
	 *  Returns the name of the current tool.
	 *
	 * @return the tool name
	 */
	public static String getToolName() {
		String name = instance.getName(current);
		if (current>=UNUSED && current<instance.getNumTools() && instance.names[current]!=null)
			name = instance.names[current];
		return name!=null?name:"";
	}

	/**
	 *  Returns the name of the specified tool.
	 *
	 * @param id the id
	 * @return the name
	 */
	String getName(int id) {
		switch (id) {
			case RECTANGLE: return roundRectMode?"roundrect":"rectangle";
			case OVAL:
				switch (ovalType) {
					case OVAL_ROI: return "oval";
					case ELLIPSE_ROI: return "ellipse";
					case BRUSH_ROI: return "brush";
				}
			case POLYGON: return "polygon";
			case FREEROI: return "freehand";
			case LINE: return arrowMode?"arrow":"line";
			case POLYLINE: return "polyline";
			case FREELINE: return "freeline";
			case ANGLE: return "angle";
			case POINT: return Prefs.multiPointMode?"multipoint":"point";
			case WAND: return "wand";
			case TEXT: return "text";
			case HAND: return "hand";
			case MAGNIFIER: return "zoom";
			case DROPPER: return "dropper";
			default: return null;
		}
		
	}
	
	/**
	 * Sets the tool.
	 *
	 * @param tool the new tool
	 */
	public void setTool(int tool) {
		if (tool==UNUSED)  //  "Unused" (blank) tool replaced with gap in 1.48h
			tool = CUSTOM1;
		else if (legacyMode && tool>=CUSTOM1)
			tool++;
		if (IJ.debugMode) IJ.log("Toolbar.setTool: "+tool);
		if ((tool==current&&!(tool==RECTANGLE||tool==OVAL||tool==POINT)) || tool<0 || tool>=getNumTools()-1)
			return;
		if (tool>=CUSTOM1&&tool<=getNumTools()-2) {
			if (names[tool]==null)
				names[tool] = "Spare tool"; // enable tool
			if (names[tool].indexOf("Action Tool")!=-1)
				return;
		}
		if (isLine(tool)) lineType = tool;
		setTool2(tool);
	}
		
	/**
	 * Sets the tool 2.
	 *
	 * @param tool the new tool 2
	 */
	private void setTool2(int tool) {
		if (!isValidTool(tool)) return;
		String previousName = getToolName();
		current = tool;
		Graphics g = this.getGraphics();
		if (g==null)
			return;
		down[current] = true;
		if (current!=previous)
			down[previous] = false;
		if (Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, previous);
		drawButton(g, current);
		if (null==g) return;
		g.dispose();
		showMessage(current);
		previous = current;
		if (Recorder.record) {
			String name = getName(current);
			if (name!=null) Recorder.record("setTool", name);
		}
		if (legacyMode)
			repaint();
		if (!previousName.equals(getToolName()))
			IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
	}
	
	/**
	 * Checks if is valid tool.
	 *
	 * @param tool the tool
	 * @return true, if is valid tool
	 */
	boolean isValidTool(int tool) {
		if (tool<0 || tool>=getNumTools())
			return false;
		if (tool>=CUSTOM1 && tool<getNumTools() && names[tool]==null)
			return false;
		return true;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 * @deprecated replaced by getForegroundColor()
	 */
	public Color getColor() {
		return foregroundColor;
	}

	/**
	 * Sets the color.
	 *
	 * @param c the new color
	 * @deprecated replaced by setForegroundColor()
	 */
	public void setColor(Color c) {
		if (c!=null) {
			foregroundColor = c;
			drawButton(this.getGraphics(), DROPPER);
		}
	}
	
	/**
	 * Gets the foreground color.
	 *
	 * @return the foreground color
	 */
	public static Color getForegroundColor() {
		return foregroundColor;
	}

	/**
	 * Sets the foreground color.
	 *
	 * @param c the new foreground color
	 */
	public static void setForegroundColor(Color c) {
		if (c==null)
			return;
		foregroundColor = c;
		IJ.notifyEventListeners(IJEventListener.FOREGROUND_COLOR_CHANGED);
		if (instance==null)
			return;
		repaintTool(DROPPER);
		for (int i=CUSTOM1; i<=instance.getNumTools()-2; i++) {
			if (instance.icons[i]!=null && instance.icons[i].contains("C123"))
				repaintTool(i);  // some of this tool's icon is drawn in the foreground color
		}
		if (!IJ.isMacro()) setRoiColor(c);
	}

	/**
	 * Gets the background color.
	 *
	 * @return the background color
	 */
	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Sets the background color.
	 *
	 * @param c the new background color
	 */
	public static void setBackgroundColor(Color c) {
		if (c!=null) {
			backgroundColor = c;
			repaintTool(DROPPER);
			IJ.notifyEventListeners(IJEventListener.BACKGROUND_COLOR_CHANGED);
		}
	}
	
	/**
	 * Sets the roi color.
	 *
	 * @param c the new roi color
	 */
	private static void setRoiColor(Color c) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi!=null && (roi.isDrawingTool())) {
			roi.setStrokeColor(c);
			imp.draw();
		}
	}

	/**
	 *  Returns the size of the selection brush tool, or 0 if the brush tool is not enabled.
	 *
	 * @return the brush size
	 */
	public static int getBrushSize() {
		if (ovalType==BRUSH_ROI)
			return brushSize;
		else
			return 0;
	}

	/**
	 *  Set the size of the selection brush tool, in pixels.
	 *
	 * @param size the new brush size
	 */
	public static void setBrushSize(int size) {
		brushSize = size;
		if (brushSize<1) brushSize = 1;
		Prefs.set(BRUSH_SIZE, brushSize);
	}
		
	/**
	 *  Returns the rounded rectangle arc size, or 0 if the rounded rectangle tool is not enabled.
	 *
	 * @return the round rect arc size
	 */
	public static int getRoundRectArcSize() {
		if (!roundRectMode)
			return 0;
		else
			return arcSize;
	}

	/**
	 *  Sets the rounded rectangle corner diameter (pixels).
	 *
	 * @param size the new round rect arc size
	 */
	public static void setRoundRectArcSize(int size) {
		if (size<=0)
			roundRectMode = false;
		else {
			arcSize = size;
			Prefs.set(CORNER_DIAMETER, arcSize);
		}
		repaintTool(RECTANGLE);
		ImagePlus imp = WindowManager.getCurrentImage();
		Roi roi = imp!=null?imp.getRoi():null;
		if (roi!=null && roi.getType()==Roi.RECTANGLE)
			roi.setCornerDiameter(roundRectMode?arcSize:0);
	}

	/**
	 *  Returns 'true' if the multi-point tool is enabled.
	 *
	 * @return the multi point mode
	 */
	public static boolean getMultiPointMode() {
		return multiPointMode;
	}

	/**
	 *  Returns the oval tool type (OVAL_ROI, ELLIPSE_ROI or BRUSH_ROI).
	 *
	 * @return the oval tool type
	 */
	public static int getOvalToolType() {
		return ovalType;
	}

	/**
	 * Gets the button size.
	 *
	 * @return the button size
	 */
	public static int getButtonSize() {
		return SIZE;
	}
	
	/**
	 * Repaint tool.
	 *
	 * @param tool the tool
	 */
	static void repaintTool(int tool) {
		if (IJ.getInstance()!=null) {
			Toolbar tb = getInstance();
			Graphics g = tb.getGraphics();
			if (IJ.debugMode) IJ.log("Toolbar.repaintTool: "+tool+" "+g);
			if (g==null) return;
			if (Prefs.antialiasedTools)
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			tb.drawButton(g, tool);
			if (g!=null) g.dispose();
		}
		//Toolbar tb = getInstance();
		//tb.repaint(tool * SIZE , 0, SIZE, SIZE);
	}
	
	/**
	 * Tool index.
	 *
	 * @param tool the tool
	 * @return the int
	 */
	// Returns the toolbar position index of the specified tool
    int toolIndex(int tool) {
    	switch (tool) {
			case RECTANGLE: return 0;
			case OVAL: return 1;
			case POLYGON: return 2;
			case FREEROI: return 3;
			case LINE: return 4;
			case POLYLINE: return 4;
			case FREELINE: return 4;
			case POINT: return 6;
			case WAND: return 7;
			case TEXT: return 8;
			case MAGNIFIER: return 9;
			case HAND: return 10;
			case DROPPER: return 11;
			case ANGLE: return 5;
			case UNUSED: return 12;
			default: return tool - 2;
		}
    }

	/**
	 * Tool ID.
	 *
	 * @param x the x
	 * @return the int
	 */
	// Returns the tool corresponding to the specified x coordinate
	private int toolID(int x) {
		if (x>SIZE*12+GAP_SIZE)
			x -= GAP_SIZE;
		int index = x/SIZE;
    	switch (index) {
			case 0: return RECTANGLE;
			case 1: return OVAL;
			case 2: return POLYGON;
			case 3: return FREEROI;
			case 4: return lineType;
			case 5: return ANGLE;
			case 6: return POINT;
			case 7: return WAND;
			case 8: return TEXT;
			case 9: return MAGNIFIER;
			case 10: return HAND;
			case 11: return DROPPER;
			default: return index + 3;
		}
    }
    
	/**
	 * In gap.
	 *
	 * @param x the x
	 * @return true, if successful
	 */
	private boolean inGap(int x) {
		return x>=(SIZE*12) && x<(SIZE*12+GAP_SIZE);
 	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		if (inGap(x))
			return;
 		int newTool = toolID(x);
		if (newTool==getNumTools()-1) {
			showSwitchPopupMenu(e);
			return;
		}
		if (!isValidTool(newTool))
			return;
		if (menus[newTool]!=null && menus[newTool].getItemCount()>0) {
            menus[newTool].show(e.getComponent(), e.getX(), e.getY());
			return;
		}
		boolean doubleClick = newTool==current && (System.currentTimeMillis()-mouseDownTime)<=DOUBLE_CLICK_THRESHOLD;
 		mouseDownTime = System.currentTimeMillis();
		if (!doubleClick) {
			mpPrevious = current;
			if (isMacroTool(newTool)) {
				String name = names[newTool];
				if (newTool==UNUSED || name.contains("Unused Tool"))
					return;
				if (name.indexOf("Action Tool")!=-1) {
					if (e.isPopupTrigger()||e.isMetaDown()) {
						name = name.endsWith(" ")?name:name+" ";
						tools[newTool].runMacroTool(name+"Options");
					} else {
						drawTool(newTool, true);
						IJ.wait(50);
						drawTool(newTool, false);
						runMacroTool(newTool);
					}
					return;
				} else {	
					name = name.endsWith(" ")?name:name+" ";
					tools[newTool].runMacroTool(name+"Selected");
				}
			}
			setTool2(newTool);
			boolean isRightClick = e.isPopupTrigger()||e.isMetaDown();
			if (current==RECTANGLE && isRightClick) {
				rectItem.setState(!roundRectMode);
				roundRectItem.setState(roundRectMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				rectPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (current==OVAL && isRightClick) {
				ovalItem.setState(ovalType==OVAL_ROI);
				ellipseItem.setState(ovalType==ELLIPSE_ROI);
				brushItem.setState(ovalType==BRUSH_ROI);
				if (IJ.isMacOSX()) IJ.wait(10);
				ovalPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (current==POINT && isRightClick) {
				pointItem.setState(!multiPointMode);
				multiPointItem.setState(multiPointMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				pointPopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (isLine(current) && isRightClick) {
				straightLineItem.setState(lineType==LINE&&!arrowMode);
				polyLineItem.setState(lineType==POLYLINE);
				freeLineItem.setState(lineType==FREELINE);
				arrowItem.setState(lineType==LINE&&arrowMode);
				if (IJ.isMacOSX()) IJ.wait(10);
				linePopup.show(e.getComponent(),x,y);
				mouseDownTime = 0L;
			}
			if (isMacroTool(current) && isRightClick) {
				String name = names[current].endsWith(" ")?names[current]:names[current]+" ";
				tools[current].runMacroTool(name+"Options");
			}
			if (isPlugInTool(current) && isRightClick) {
				tools[current].showPopupMenu(e, this);
			}
		} else { //double click
			if (isMacroTool(current)) {
				String name = names[current].endsWith(" ")?names[current]:names[current]+" ";
				tools[current].runMacroTool(name+"Options");
				return;
			}
			if (isPlugInTool(current)) {
				tools[current].showOptionsDialog();
				return;
			}
			ImagePlus imp = WindowManager.getCurrentImage();
			switch (current) {
				case RECTANGLE:
					if (roundRectMode)
						IJ.doCommand("Rounded Rect Tool...");
					break;
				case OVAL:
					showBrushDialog();
					break;
				case MAGNIFIER:
					if (imp!=null) {
						ImageCanvas ic = imp.getCanvas();
						if (ic!=null) ic.unzoom();
					}
					break;
				case LINE: case POLYLINE: case FREELINE:
					if (current==LINE && arrowMode) {
						IJ.doCommand("Arrow Tool...");
					} else
						IJ.runPlugIn("ij.plugin.frame.LineWidthAdjuster", "");
					break;
				case ANGLE:
					showAngleDialog();
					break;
				case POINT:
					IJ.doCommand("Point Tool...");
					break;
				case WAND:
					IJ.doCommand("Wand Tool...");
					break;
				case TEXT:
					IJ.run("Fonts...");
					break;
				case DROPPER:
					IJ.doCommand("Color Picker...");
					setTool2(mpPrevious);
					break;
				default:
			}
		}
	}
	
	/**
	 * Show switch popup menu.
	 *
	 * @param e the e
	 */
	void showSwitchPopupMenu(MouseEvent e) {
		String path = IJ.getDirectory("macros")+"toolsets/";
		if (path==null)
			return;
		boolean applet = IJ.getApplet()!=null;
		File f = new File(path);
		String[] list;
		if (!applet && f.exists() && f.isDirectory()) {
			list = f.list();
			if (list==null) return;
		} else
			list = new String[0];
		switchPopup.removeAll();
        path = IJ.getDirectory("macros") + "StartupMacros.txt";
		f = new File(path);
		if (!applet && f.exists())
            addItem("Startup Macros");
        else
            addItem("StartupMacros*");
 		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.startsWith(".") || name.endsWith(" Tool"))
				continue;
			if (name.endsWith(".txt")) {
				name = name.substring(0, name.length()-4);
                addItem(name);
			} else if (name.endsWith(".ijm")) {
				name = name.substring(0, name.length()-4) + " ";
                addItem(name);
			}
		}
		addPluginTools();
		addItem("Restore Startup Tools");
		addItem("Remove Custom Tools");
		addItem("Help...");
		add(ovalPopup);
		if (IJ.isMacOSX()) IJ.wait(10);
		switchPopup.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Adds the plugin tools.
	 */
	private void addPluginTools() {
		switchPopup.addSeparator();
		for (int i=0; i<builtInTools.length; i++)
			addBuiltInTool(builtInTools[i]);
		MenuBar menuBar = Menus.getMenuBar();
		if (menuBar==null)
			return;
		int n = menuBar.getMenuCount();
		Menu pluginsMenu = null;
		if (menuBar.getMenuCount()>=5)
			pluginsMenu = menuBar.getMenu(5);
		if (pluginsMenu==null || !"Plugins".equals(pluginsMenu.getLabel()))
			return;
		n = pluginsMenu.getItemCount();
		Menu toolsMenu = null;
		for (int i=0; i<n; ++i) {
			MenuItem m = pluginsMenu.getItem(i);
			if ("Tools".equals(m.getLabel()) && (m instanceof Menu)) {
				toolsMenu = (Menu)m;
				break;
			}
		}
		if (toolsMenu==null) {
			switchPopup.addSeparator();
			return;
		}
		n = toolsMenu.getItemCount();
		boolean separatorAdded = false;
		for (int i=0; i<n; ++i) {
			MenuItem m = toolsMenu.getItem(i);
			String label = m.getLabel();
			if (label!=null && (label.endsWith(" Tool")||label.endsWith(" Menu"))) {
				if (!separatorAdded) {
					switchPopup.addSeparator();
					separatorAdded = true;
				}
				addPluginTool(label);
			}
		}
		switchPopup.addSeparator();
	}

    /**
     * Adds the built in tool.
     *
     * @param name the name
     */
    private void addBuiltInTool(String name) {
		CheckboxMenuItem item = new CheckboxMenuItem(name, name.equals(currentSet));
		item.addItemListener(this);
		item.setActionCommand("Tool");
		switchPopup.add(item);
    }

    /**
     * Adds the plugin tool.
     *
     * @param name the name
     */
    private void addPluginTool(String name) {
		CheckboxMenuItem item = new CheckboxMenuItem(name, name.equals(currentSet));
		item.addItemListener(this);
		item.setActionCommand("Plugin Tool");
		switchPopup.add(item);
    }

    /**
     * Adds the item.
     *
     * @param name the name
     */
    private void addItem(String name) {
		CheckboxMenuItem item = new CheckboxMenuItem(name, name.equals(currentSet));
		item.addItemListener(this);
		switchPopup.add(item);
    }

	/**
	 * Draw tool.
	 *
	 * @param tool the tool
	 * @param drawDown the draw down
	 */
	void drawTool(int tool, boolean drawDown) {
		down[tool] = drawDown;
		Graphics g = this.getGraphics();
		if (!drawDown && Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, tool);
		if (null==g) return;
		g.dispose();
	}

	/**
	 * Checks if is line.
	 *
	 * @param tool the tool
	 * @return true, if is line
	 */
	boolean isLine(int tool) {
		return tool==LINE || tool==POLYLINE || tool==FREELINE;
	}
	
	/**
	 * Restore previous tool.
	 */
	public void restorePreviousTool() {
		setTool2(mpPrevious);
	}
	
	/**
	 * Checks if is macro tool.
	 *
	 * @param tool the tool
	 * @return true, if is macro tool
	 */
	boolean isMacroTool(int tool) {
		return tool>=CUSTOM1 && tool<getNumTools() && names[tool]!=null && (tools[tool] instanceof MacroToolRunner);
	}
	
	/**
	 * Checks if is plug in tool.
	 *
	 * @param tool the tool
	 * @return true, if is plug in tool
	 */
	boolean isPlugInTool(int tool) {
		return tool>=CUSTOM1 && tool<getNumTools() && tools[tool]!=null;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		CheckboxMenuItem item = (CheckboxMenuItem)e.getSource();
		String previousName = getToolName();
		if (item==rectItem || item==roundRectItem) {
			roundRectMode = item==roundRectItem;
			repaintTool(RECTANGLE);
			showMessage(RECTANGLE);
			ImagePlus imp = WindowManager.getCurrentImage();
			Roi roi = imp!=null?imp.getRoi():null;
			if (roi!=null && roi.getType()==Roi.RECTANGLE)
				roi.setCornerDiameter(roundRectMode?arcSize:0);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==ovalItem || item==ellipseItem || item==brushItem) {
			if (item==brushItem)
				ovalType = BRUSH_ROI;
			else if (item==ellipseItem)
				ovalType = ELLIPSE_ROI;
			else
				ovalType = OVAL_ROI;
			repaintTool(OVAL);
			showMessage(OVAL);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==pointItem || item==multiPointItem) {
			multiPointMode = item==multiPointItem;
			Prefs.multiPointMode = multiPointMode;
			repaintTool(POINT);
			showMessage(POINT);
			if (!previousName.equals(getToolName()))
				IJ.notifyEventListeners(IJEventListener.TOOL_CHANGED);
		} else if (item==straightLineItem) {
			lineType = LINE;
			arrowMode = false;
			setTool2(LINE);
			showMessage(LINE);
		} else if (item==polyLineItem) {
			lineType = POLYLINE;
			setTool2(POLYLINE);
			showMessage(POLYLINE);
		} else if (item==freeLineItem) {
			lineType = FREELINE;
			setTool2(FREELINE);
			showMessage(FREELINE);
		} else if (item==arrowItem) {
			lineType = LINE;
			arrowMode = true;
			setTool2(LINE);
			showMessage(LINE);
		} else {
			String label = item.getLabel();
			String cmd = item.getActionCommand();
			boolean isTool = cmd.equals("Tool") || cmd.equals("Plugin Tool");
			if (!(label.equals("Help...")||label.equals("Remove Custom Tools")) && !isTool && !label.endsWith("Tool") && !label.endsWith("Tool "))
				currentSet = label;
			if (isTool) {
				if (cmd.equals("Tool")) // built in tool
					installBuiltinTool(label);
				else  // plugin or macro tool in ImageJ/plugins/Tools
					IJ.run(label);
				return;
			}
			String path;
			if (label.equals("Remove Custom Tools")) {
				removeTools();
			} else if (label.equals("Restore Startup Tools")) {
				removeTools();
				installStartupMacros();
			} else if (label.equals("Help...")) {
				IJ.showMessage("Tool Switcher and Loader",
					"Use this drop down menu to switch to alternative\n"+
					"macro toolsets or to load additional plugin tools.\n"+
					"The toolsets listed in the menu are located\n"+
					"in the ImageJ/macros/toolsets folder and the\n"+
					"plugin tools are the ones installed in the\n"+
					"Plugins>Tools submenu.\n"+
					" \n"+
					"Hold the shift key down while selecting a\n"+
					"toolset to view its source code.\n"+
					" \n"+
					"More macro toolsets are available at\n"+
					"  <"+IJ.URL+"/macros/toolsets/>\n"+
					" \n"+
					"Plugin tools can be downloaded from\n"+
					"the Tools section of the Plugins page at\n"+
					"  <"+IJ.URL+"/plugins/>\n"
					);
				return;
			} else if (label.endsWith("*")) {
                // load from ij.jar
                MacroInstaller mi = new MacroInstaller();
                label = label.substring(0, label.length()-1) + ".txt";
                path = "/macros/"+label;
				if (IJ.shiftKeyDown()) {
					String macros = mi.openFromIJJar(path);
                    Editor ed = new Editor();
                    ed.setSize(350, 300);
                    ed.create(label, macros);
                	IJ.setKeyUp(KeyEvent.VK_SHIFT);
				} else {
					resetTools();
					mi.installFromIJJar(path);
				}
            } else {
                // load from ImageJ/macros/toolsets
                if (label.equals("Startup Macros")) {
                	installStartupMacros();
                	return;
                } else if (label.endsWith(" "))
                    path = IJ.getDirectory("macros")+"toolsets"+File.separator+label.substring(0, label.length()-1)+".ijm";
                else
                    path = IJ.getDirectory("macros")+"toolsets"+File.separator+label+".txt";
                try {
                    if (IJ.shiftKeyDown()) {
                        IJ.open(path);
                		IJ.setKeyUp(KeyEvent.VK_SHIFT);
                    } else
                        new MacroInstaller().run(path);
                } catch(Exception ex) {}
            }
		}
	}
	
	/**
	 * Removes the tools.
	 */
	private void removeTools() {
		removeMacroTools();
		setTool(RECTANGLE);
		currentSet = "Startup Macros";
		resetPrefs();
		if (nExtraTools>0) {
			String name = names[getNumTools()-1];
			String icon = icons[getNumTools()-1];
			nExtraTools = 0;
			names[getNumTools()-1] = name;
			icons[getNumTools()-1] = icon;
			ps = new Dimension(SIZE*NUM_BUTTONS-(SIZE-GAP_SIZE)+nExtraTools*SIZE, SIZE);
			IJ.getInstance().pack();
		}
	}
	
	/**
	 * Reset prefs.
	 */
	private void resetPrefs() {
		for (int i=0; i<7; i++) {
			String key = TOOL_KEY+(i/10)%10+i%10;
			if (!Prefs.get(key, "").equals(""))
				Prefs.set(key, "");
		}
	}
	
	/**
	 * Restore tools.
	 */
	public static void restoreTools() {
		Toolbar tb = Toolbar.getInstance();
		if (tb!=null) {
			if (tb.getToolId()>=UNUSED)
				tb.setTool(RECTANGLE);
			tb.installStartupMacros();
		}
	}

	/**
	 * Install startup macros.
	 */
	private 	void installStartupMacros() {
		resetTools();
		String path = IJ.getDirectory("macros")+"StartupMacros.txt";
		File f = new File(path);
		if (!f.exists()) {
			String path2 = IJ.getDirectory("macros")+"StartupMacros.fiji.ijm";
			f = new File(path2);
			if (!f.exists()) {
				IJ.error("StartupMacros not found:\n \n"+path);
				return;
			} else
				path = path2;
		}
		if (IJ.shiftKeyDown()) {
			IJ.open(path);
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
		} else {
			try {
				MacroInstaller mi = new MacroInstaller();
				mi.installFile(path);
			} catch
				(Exception ex) {}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		MenuItem item = (MenuItem)e.getSource();
		String cmd = e.getActionCommand();
		PopupMenu popup = (PopupMenu)item.getParent();
		int tool = -1;
		for (int i=CUSTOM1; i<getNumTools(); i++) {
			if (popup==menus[i]) {
				tool = i;
				break;
			}
		}
		if (tool==-1) return;
		if (tools[tool]!=null)
			tools[tool].runMenuTool(names[tool], cmd);
    }

	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize(){
		return ps;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#getMinimumSize()
	 */
	public Dimension getMinimumSize(){
		return ps;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		if (inGap(x))
			IJ.showStatus("");
		else
			showMessage(toolID(x));
	}

	/**
	 *  Adds a tool to the toolbar. The 'toolTip' string is displayed in the status bar
	 * 		 when the mouse is over the tool icon. The 'toolTip' string may include icon 
	 * 		(http://imagej.nih.gov/ij/developer/macro/macros.html#tools).
	 * 		Returns the tool ID, or -1 if all tool slots are in use.
	 *
	 * @param toolTip the tool tip
	 * @return the int
	 */
	public int addTool(String toolTip) {
		int index = toolTip.indexOf('-');
		boolean hasIcon = index>=0 && (toolTip.length()-index)>4;
		int tool =-1;
		for (int i=CUSTOM1; i<=getNumTools()-2; i++) {
			if (names[i]==null || toolTip.startsWith(names[i])) {
				tool = i;
				break;
			}			
		}
		if (tool==CUSTOM1)
			legacyMode = toolTip.startsWith("Select and Transform Tool"); //TrakEM2
		if (tool==-1 && (nExtraTools<MAX_EXTRA_TOOLS)) {
			nExtraTools++;
			names[getNumTools()-1] = names[getNumTools()-2];
			icons[getNumTools()-1] = icons[getNumTools()-2];
			names[getNumTools()-2] = null;
			icons[getNumTools()-2] = null;
			ps = new Dimension(SIZE*NUM_BUTTONS-(SIZE-GAP_SIZE)+nExtraTools*SIZE, SIZE);
			IJ.getInstance().pack();
			tool = getNumTools()-2;
		}
		if (tool==-1) {
			if (addingSingleTool)
				tool = getNumTools()-2;
			else
				return -1;
		}
		if (hasIcon) {
			icons[tool] = toolTip.substring(index+1);
			if (index>0 && toolTip.charAt(index-1)==' ')
				names[tool] = toolTip.substring(0, index-1);
			else
				names[tool] = toolTip.substring(0, index);
		} else {
			if (toolTip.endsWith("-"))
				toolTip = toolTip.substring(0, toolTip.length()-1);
			else if (toolTip.endsWith("- "))
				toolTip = toolTip.substring(0, toolTip.length()-2);
			names[tool] = toolTip;
		}
        if (tool==current && (names[tool].indexOf("Action Tool")!=-1||names[tool].indexOf("Unused Tool")!=-1))
        	setTool(RECTANGLE);
        if (names[tool].endsWith(" Menu Tool"))
            installMenu(tool);
        if (IJ.debugMode) IJ.log("Toolbar.addTool: "+tool+" "+toolTip);
		return tool;
	}
    
    /**
     * Install menu.
     *
     * @param tool the tool
     */
    void installMenu(int tool) {
        Program pgm = macroInstaller.getProgram();
        Hashtable h = pgm.getMenus();
        if (h==null) return;
        String[] commands = (String[])h.get(names[tool]);
        if (commands==null)
        	return;
		if (menus[tool]==null) {
			menus[tool] = new PopupMenu("");
			if (Menus.getFontSize()!=0)
				menus[tool].setFont(Menus.getFont());
			add(menus[tool] );
		} else
			menus[tool].removeAll();
        for (int i=0; i<commands.length; i++) {
			if (commands[i].equals("-"))
				menus[tool].addSeparator();
			else if (commands[i].startsWith("-"))
				menus[tool].addSeparator();
			else {
				boolean disable = commands[i].startsWith("*");
				String command = commands[i];
				if (disable)
					command = command.substring(1);
				MenuItem mi = new MenuItem(command);
				if (disable)
					mi.setEnabled(false);
				mi.addActionListener(this);
				menus[tool].add(mi);
			}
        }
        if (tool==current) setTool(RECTANGLE);
    }
    
	/**
	 *  Used by the MacroInstaller class to install a set of macro tools.
	 *
	 * @param name the name
	 * @param macroInstaller the macro installer
	 * @param id the id
	 */
	public void addMacroTool(String name, MacroInstaller macroInstaller, int id) {
		if (id==0) {
			resetTools();
			if (name.startsWith("Unused"))
				return;
 		}
		if (name.endsWith(" Built-in Tool")) {
			name = name.substring(0,name.length()-14);
			doNotSavePrefs = true;
			boolean ok = installBuiltinTool(name);
			if (!ok) {
				Hashtable commands = Menus.getCommands();
				if (commands!=null && commands.get(name)!=null)
					IJ.run(name);
			}
			doNotSavePrefs = false;
			return;
		}
		this.macroInstaller = macroInstaller;
		int tool = addTool(name);
		this.macroInstaller = null;
		if (tool!=-1)
			tools[tool] = new MacroToolRunner(macroInstaller);
	}
	
	/**
	 * Reset tools.
	 */
	private void resetTools() {
		for (int i=CUSTOM1; i<getNumTools()-1; i++) {
			names[i] = null;
			tools[i] = null;
			icons[i] = null;
			if (menus[i]!=null)
				menus[i].removeAll();
		}
	}
	
	/**
	 *  Used by the MacroInstaller class to add a macro tool to the toolbar.
	 *
	 * @param name the name
	 * @param macroInstaller the macro installer
	 */
	public void addMacroTool(String name, MacroInstaller macroInstaller) {
		String custom1Name = names[CUSTOM1];
		this.macroInstaller = macroInstaller;
		addingSingleTool = true;
		int tool = addTool(name);
		addingSingleTool = false;
		this.macroInstaller = null;
		if (tool!=-1) {
			tools[tool] = new MacroToolRunner(macroInstaller);
			if (!name.contains(" Menu Tool")) {
				if (menus[tool]!=null)
					menus[tool].removeAll();
				if (!installingStartupTool)
					setTool(tool);
				else
					installingStartupTool = false;
			}
			if ((tool-CUSTOM1)>0 || custom1Name==null)
				setPrefs(tool);
		}
	}
	
	/**
	 * Sets the prefs.
	 *
	 * @param id the new prefs
	 */
	private void setPrefs(int id) {
		if (doNotSavePrefs)
			return;
		boolean ok = isBuiltInTool(names[id]);
		String prefsName = instance.names[id];
		if (!ok) {
			String name = names[id];
			int i = name.indexOf(" (");  // remove any hint in parens
			if (i>0) {
				name = name.substring(0, i);
				prefsName=name;
			}
		}
		int index = id - CUSTOM1;
		String key = TOOL_KEY + (index/10)%10 + index%10;
		Prefs.set(key, prefsName);
	}
	
	/**
	 * Checks if is built in tool.
	 *
	 * @param name the name
	 * @return true, if is built in tool
	 */
	private boolean isBuiltInTool(String name) {
		for (int i=0; i<builtInTools2.length; i++) {
			if (name.equals(builtInTools2[i]))
				return true;
		}
		for (int i=0; i<builtInTools.length; i++) {
			if (name.startsWith(builtInTools[i]))
				return true;
		}
		return false;
	}

	/**
	 * Removes the macro tools.
	 */
	public static void removeMacroTools() {
		if (instance!=null) {
			if (instance.getToolId()>=CUSTOM1)
				instance.setTool(RECTANGLE);
			instance.resetTools();
			instance.repaint();
		}
	}

	/**
	 *  Adds a plugin tool to the first available toolbar slot,
	 * 		or to the last slot if the toolbar is full.
	 *
	 * @param tool the tool
	 */
	public static void addPlugInTool(PlugInTool tool) {
		if (instance==null) return;
		String nameAndIcon = tool.getToolName()+" - "+tool.getToolIcon();
		instance.addingSingleTool = true;
		int id = instance.addTool(nameAndIcon);
		instance.addingSingleTool = false;
		if (id!=-1) {
			instance.tools[id] = tool;
			if (instance.menus[id]!=null)
				instance.menus[id].removeAll();
			instance.repaintTool(id);	
			if (!instance.installingStartupTool)
				instance.setTool(id);
			else
				instance.installingStartupTool = false;
			instance.setPrefs(id);
		}
	}

	/**
	 * Gets the plug in tool.
	 *
	 * @return the plug in tool
	 */
	public static PlugInTool getPlugInTool() {
		PlugInTool tool = null;
		if (instance==null)
			return null;
		if (current<instance.tools.length)
			tool = instance.tools[current];
		if (tool!=null && tool instanceof MacroToolRunner)
			tool = null;
		return tool;
	}

	/**
	 * Run macro tool.
	 *
	 * @param id the id
	 */
	void runMacroTool(int id) {
		if (id<getNumTools() && tools[id]!=null)
			tools[id].runMacroTool(names[id]);
	}
	
	/**
	 * Show brush dialog.
	 */
	void showBrushDialog() {
		GenericDialog gd = new GenericDialog("Selection Brush");
		gd.addCheckbox("Enable selection brush", ovalType==BRUSH_ROI);
		gd.addNumericField("           Size:", brushSize, 0, 4, "pixels");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		if (gd.getNextBoolean())
			ovalType = BRUSH_ROI;
		brushSize = (int)gd.getNextNumber();
		if (brushSize<1) brushSize=1;
		repaintTool(OVAL);
		ImagePlus img = WindowManager.getCurrentImage();
		Roi roi = img!=null?img.getRoi():null;
		if (roi!=null && roi.getType()==Roi.OVAL && ovalType==BRUSH_ROI)
			img.deleteRoi();
		Prefs.set(BRUSH_SIZE, brushSize);
	}

	/**
	 * Show angle dialog.
	 */
	void showAngleDialog() {
		GenericDialog gd = new GenericDialog("Angle Tool");
		gd.addCheckbox("Measure reflex angle", Prefs.reflexAngle);
		gd.showDialog();
		if (!gd.wasCanceled())
			Prefs.reflexAngle = gd.getNextBoolean();
	}
	
	/**
	 * Install startup tools.
	 */
	public void installStartupTools() {
		if (IJ.debugMode) IJ.log("Toolbar.installStartupTools");
		for (int i=0; i<=6; i++) {
			String name = Prefs.get(TOOL_KEY + (i/10)%10 + i%10, "");
			if (IJ.debugMode) IJ.log("  "+i+" "+name);
			if (name.equals("")) continue;
			installingStartupTool = true;
			boolean ok = installBuiltinTool(name);
			if (!ok) {
				ok = installToolsetTool(name);
				if (!ok) {  // install tool in plugins/Tools
					if (name.endsWith("Menu Tool"))
						name = name.substring(0, name.length()-5);
					Hashtable commands = Menus.getCommands();
					if (commands!=null && commands.get(name)!=null)
						IJ.run(name);
				}
			}
			installingStartupTool = false;
		}
	}
	
	/**
	 * Install toolset tool.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	// install tool from ImageJ/macros/toolsets
	private boolean installToolsetTool(String name) {
		String path = IJ.getDirectory("macros")+"toolsets"+File.separator+name+".ijm";
		if (!((new File(path)).exists())) {
			name = name.replaceAll(" ", "_");
			path = IJ.getDirectory("macros")+"toolsets"+File.separator+name+".ijm";
		}
		String text = IJ.openAsString(path);
		if (text==null || text.startsWith("Error"))
			return false;
		new MacroInstaller().installSingleTool(text);
		return true;
	}
	
	/**
	 * Install builtin tool.
	 *
	 * @param label the label
	 * @return true, if successful
	 */
	private boolean installBuiltinTool(String label) {
		if (IJ.debugMode) IJ.log("Toolbar.installBuiltinTool: "+label);
		boolean ok = true;
		PlugInTool tool = null;
		if (label.startsWith("Arrow")) {
			tool = new ij.plugin.tool.ArrowTool();
			if (tool!=null) tool.run("");
		} else if (label.startsWith("Overlay Brush")) {
			tool = new ij.plugin.tool.OverlayBrushTool();
			if (tool!=null) tool.run("");
		} else if (label.startsWith("Pixel Inspect")) {
			tool = new ij.plugin.tool.PixelInspectionTool();
			if (tool!=null) tool.run("");
		} else if (label.startsWith("Brush")||label.startsWith("Paintbrush")) {
			tool = new ij.plugin.tool.BrushTool();
			if (tool!=null) tool.run("");
		} else if (label.startsWith("Pencil")) {
			tool = new ij.plugin.tool.BrushTool();
			if (tool!=null) tool.run("pencil");
		} else if (label.startsWith("Flood Fill")) {
			(new MacroInstaller()).installFromIJJar("/macros/FloodFillTool.txt");
		} else if (label.startsWith("Spray Can")) {
			(new MacroInstaller()).installFromIJJar("/macros/SprayCanTool.txt");
		} else if (label.startsWith("Developer Menu")) {
			(new MacroInstaller()).installFromIJJar("/macros/DeveloperMenuTool.txt");
		} else if (label.startsWith("Stacks Menu")) {
			(new MacroInstaller()).installFromIJJar("/macros/StacksMenuTool.txt");
		} else if (label.startsWith("LUT Menu")) {
			(new MacroInstaller()).installFromIJJar("/macros/LUTMenuTool.txt");
		} else
			ok = false;
		return ok;
	}
	
	/**
	 * Checks if is macro set.
	 *
	 * @param id the id
	 * @return true, if is macro set
	 */
	private boolean isMacroSet(int id) {
		if (tools[id]==null)
			return false;
		if (!(tools[id] instanceof MacroToolRunner))
			return false;
		boolean rtn = ((MacroToolRunner)tools[id]).getMacroCount()>2;
		return rtn;
	}
	
	/**
	 * Install startup macros tools.
	 *
	 * @return true, if successful
	 */
	public static boolean installStartupMacrosTools() {
		String customTool0 = Prefs.get(Toolbar.TOOL_KEY+"00", "");
		return customTool0.equals("") || Character.isDigit(customTool0.charAt(0));
	}
	
	/**
	 * Gets the num tools.
	 *
	 * @return the num tools
	 */
	public int getNumTools() {
		return NUM_TOOLS + nExtraTools;
	}

	//public void repaint() {
	//	super.repaint();
	//}

}
