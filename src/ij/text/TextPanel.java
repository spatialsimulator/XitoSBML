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
package ij.text;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.Recorder;
import ij.util.Tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.Scrollbar;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;


// TODO: Auto-generated Javadoc
/**
This is an unlimited size text panel with tab-delimited,
labeled and resizable columns. It is based on the hGrid
class at
    http://www.lynx.ch/contacts/~/thomasm/Grid/index.html.
*/
public class TextPanel extends Panel implements AdjustmentListener,
	MouseListener, MouseMotionListener, KeyListener,  ClipboardOwner,
	ActionListener, MouseWheelListener, Runnable {

	/** The Constant DOUBLE_CLICK_THRESHOLD. */
	static final int DOUBLE_CLICK_THRESHOLD = 650;
	
	/** The i grid height. */
	// height / width
	int iGridWidth,iGridHeight;
	
	/** The i Y. */
	int iX,iY;
	
	/** The s col head. */
	// data
	String[] sColHead;
	
	/** The v data. */
	Vector vData;
	
	/** The i col width. */
	int[] iColWidth;
	
	/** The i row count. */
	int iColCount,iRowCount;
	
	/** The i first row. */
	int iRowHeight,iFirstRow;
	
	/** The sb vert. */
	// scrolling
	Scrollbar sbHoriz,sbVert;
	
	/** The i sb height. */
	int iSbWidth,iSbHeight;
	
	/** The b drag. */
	boolean bDrag;
	
	/** The i col drag. */
	int iXDrag,iColDrag;
  
	/** The headings. */
	boolean headings = true;
	
	/** The title. */
	String title = "";
	
	/** The labels. */
	String labels;
	
	/** The key listener. */
	KeyListener keyListener;
	
	/** The resize cursor. */
	Cursor resizeCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
  	
	  /** The default cursor. */
	  Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/** The sel line. */
	int selStart=-1, selEnd=-1,selOrigin=-1, selLine=-1;
	
	/** The tc. */
	TextCanvas tc;
	
	/** The pm. */
	PopupMenu pm;
	
	/** The columns manually adjusted. */
	boolean columnsManuallyAdjusted;
	
	/** The mouse down time. */
	long mouseDownTime;
    
    /** The file path. */
    String filePath;
    
    /** The rt. */
    ResultsTable rt;
    
    /** The unsaved lines. */
    boolean unsavedLines;
    
    /** The search string. */
    String searchString;
    
    /** The file menu. */
    Menu fileMenu;
    
    /** The menus extended. */
    boolean menusExtended;

  
	/** Constructs a new TextPanel. */
	public TextPanel() {
		tc = new TextCanvas(this);
		setLayout(new BorderLayout());
		add("Center",tc);
		sbHoriz=new Scrollbar(Scrollbar.HORIZONTAL);
		sbHoriz.addAdjustmentListener(this);
		sbHoriz.setFocusable(false); // prevents scroll bar from blinking on Windows
		add("South", sbHoriz);
		sbVert=new Scrollbar(Scrollbar.VERTICAL);
		sbVert.addAdjustmentListener(this);
		sbVert.setFocusable(false);
		ImageJ ij = IJ.getInstance();
		if (ij!=null) {
			sbHoriz.addKeyListener(ij);
			sbVert.addKeyListener(ij);
		}
		add("East", sbVert);
		addPopupMenu();
	}
  
	/**
	 *  Constructs a new TextPanel.
	 *
	 * @param title the title
	 */
	public TextPanel(String title) {
		this();
		this.title = title;
		if (title.equals("Results")) {
			pm.addSeparator();
			addPopupItem("Clear Results");
			addPopupItem("Summarize");
			addPopupItem("Distribution...");
			addPopupItem("Set Measurements...");
			addPopupItem("Rename...");
			addPopupItem("Duplicate...");
			menusExtended = true;
		}
	}

	/**
	 * Adds the popup menu.
	 */
	void addPopupMenu() {
		pm=new PopupMenu();
		addPopupItem("Save As...");
		pm.addSeparator();
		addPopupItem("Cut");
		addPopupItem("Copy");
		addPopupItem("Clear");
		addPopupItem("Select All");
		add(pm);
	}
	
	/**
	 * Adds the popup item.
	 *
	 * @param s the s
	 */
	void addPopupItem(String s) {
		MenuItem mi=new MenuItem(s);
		mi.addActionListener(this);
		pm.add(mi);
	}

	/**
	 * 	Clears this TextPanel and sets the column headings to
	 * 	those in the tab-delimited 'headings' String. Set 'headings'
	 * 	to "" to use a single column with no headings.
	 *
	 * @param labels the new column headings
	 */
	public synchronized void setColumnHeadings(String labels) {
		boolean sameLabels = labels.equals(this.labels);
		this.labels = labels;
		if (labels.equals("")) {
			iColCount = 1;
			sColHead=new String[1];
			sColHead[0] = "";
		} else {
			if (labels.endsWith("\t"))
				this.labels = labels.substring(0, labels.length()-1);
			sColHead = Tools.split(this.labels, "\t");
        	iColCount = sColHead.length;
		}
		flush();
		vData=new Vector();
		if (!(iColWidth!=null && iColWidth.length==iColCount && sameLabels && iColCount!=1)) {
			iColWidth=new int[iColCount];
			columnsManuallyAdjusted = false;
		}
		iRowCount=0;
		resetSelection();
		adjustHScroll();
		tc.repaint();
	}
  
	/**
	 *  Returns the column headings as a tab-delimited string.
	 *
	 * @return the column headings
	 */
	public String getColumnHeadings() {
		return labels==null?"":labels;
	}
	
	/**
	 * Update column headings.
	 *
	 * @param labels the labels
	 */
	public synchronized void updateColumnHeadings(String labels) {
		this.labels = labels;
		if (labels.equals("")) {
			iColCount = 1;
			sColHead=new String[1];
			sColHead[0] = "";
		} else {
			if (labels.endsWith("\t"))
				this.labels = labels.substring(0, labels.length()-1);
			sColHead = Tools.split(this.labels, "\t");
        	iColCount = sColHead.length;
			iColWidth=new int[iColCount];
			columnsManuallyAdjusted = false;
		}
	}

	/**
	 * Sets the font.
	 *
	 * @param font the font
	 * @param antialiased the antialiased
	 */
	public void setFont(Font font, boolean antialiased) {
		tc.fFont = font;
		tc.iImage = null;
		tc.fMetrics = null;
		tc.antialiased = antialiased;
		iColWidth[0] = 0;
		if (isShowing()) updateDisplay();
	}
  
	/**
	 *  Adds a single line to the end of this TextPanel.
	 *
	 * @param text the text
	 */
	public void appendLine(String text) {
		if (vData==null)
			setColumnHeadings("");
		char[] chars = text.toCharArray();
		vData.addElement(chars);
		iRowCount++;
		if (isShowing()) {
			if (iColCount==1 && tc.fMetrics!=null) {
				iColWidth[0] = Math.max(iColWidth[0], tc.fMetrics.charsWidth(chars,0,chars.length));
				adjustHScroll();
			}
			updateDisplay();
			unsavedLines = true;
		}
	}
	
	/**
	 *  Adds one or more lines to the end of this TextPanel.
	 *
	 * @param text the text
	 */
	public void append(String text) {
		if (text==null) text="null";
		if (vData==null)
			setColumnHeadings("");
		if (text.length()==1 && text.equals("\n"))
			text = "";
		String[] lines = text.split("\n");
		for (int i=0; i<lines.length; i++)
			appendWithoutUpdate(lines[i]);
		if (isShowing()) {
			updateDisplay();
			unsavedLines = true;
		}
	}
	
	/**
	 *  Adds strings contained in an ArrayList to the end of this TextPanel.
	 *
	 * @param list the list
	 */
	public void append(ArrayList list) {
		if (list==null) return;
		if (vData==null) setColumnHeadings("");
		for (int i=0; i<list.size(); i++)
			appendWithoutUpdate((String)list.get(i));
		if (isShowing()) {
			updateDisplay();
			unsavedLines = true;
		}
	}

	/**
	 *  Adds a single line to the end of this TextPanel without updating the display.
	 *
	 * @param data the data
	 */
	public void appendWithoutUpdate(String data) {
		if (vData!=null) {
			char[] chars = data.toCharArray();
			vData.addElement(chars);
			iRowCount++;
		}
	}

	/**
	 * Update display.
	 */
	public void updateDisplay() {
		iY=iRowHeight*(iRowCount+1);
		adjustVScroll();
		if (iColCount>1 && iRowCount<=10 && !columnsManuallyAdjusted)
			iColWidth[0] = 0; // forces column width calculation
		tc.repaint();
	}

	/**
	 * Gets the cell.
	 *
	 * @param column the column
	 * @param row the row
	 * @return the cell
	 */
	String getCell(int column, int row) {
		if (column<0||column>=iColCount||row<0||row>=iRowCount)
			return null;
		return new String(tc.getChars(column, row));
	}

	/**
	 * Adjust V scroll.
	 */
	synchronized void adjustVScroll() {
		if(iRowHeight==0) return;
		Dimension d = tc.getSize();
		int value = iY/iRowHeight;
		int visible = d.height/iRowHeight;
		int maximum = iRowCount+1;
		if (visible<0) visible=0;
		if (visible>maximum) visible=maximum;
		if (value>(maximum-visible)) value=maximum-visible;
		sbVert.setValues(value,visible,0,maximum);
		iY=iRowHeight*value;
	}

	/**
	 * Adjust H scroll.
	 */
	synchronized void adjustHScroll() {
		if (iRowHeight==0) return;
		Dimension d = tc.getSize();
		int w=0;
		for (int i=0; i<iColCount; i++)
			w+=iColWidth[i];
		iGridWidth=w;
		sbHoriz.setValues(iX,d.width,0,iGridWidth);
		iX=sbHoriz.getValue();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	public void adjustmentValueChanged (AdjustmentEvent e) {
		iX=sbHoriz.getValue();
 		iY=iRowHeight*sbVert.getValue();
		tc.repaint();
 	}
    
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed (MouseEvent e) {
		int x=e.getX(), y=e.getY();
		if (e.isPopupTrigger() || e.isMetaDown())
			pm.show(e.getComponent(),x,y);
 		else if (e.isShiftDown())
			extendSelection(x, y);
		else {
 			select(x, y);
 			handleDoubleClick();
 		}
	}
	
	/**
	 * Handle double click.
	 */
	void handleDoubleClick() {
		if (selStart<0 || selStart!=selEnd || iColCount!=1) return;
		boolean doubleClick = System.currentTimeMillis()-mouseDownTime<=DOUBLE_CLICK_THRESHOLD;
		mouseDownTime = System.currentTimeMillis();
		if (doubleClick) {
			char[] chars = (char[])(vData.elementAt(selStart));
			String s = new String(chars);
			int index = s.indexOf(": ");
			if (index>-1 && !s.endsWith(": "))
				s = s.substring(index+2); // remove sequence number added by ListFilesRecursively
			if (s.indexOf(File.separator)!=-1 ||  s.indexOf(".")!=-1) {
				filePath = s;
				Thread thread = new Thread(this, "Open");
				thread.setPriority(thread.getPriority()-1);
				thread.start();
			}
		}
	}
	
    /** For better performance, open double-clicked files on 
    	separate thread instead of on event dispatch thread. */
    public void run() {
        if (filePath!=null) IJ.open(filePath);
    }

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited (MouseEvent e) {
		if(bDrag) {
			setCursor(defaultCursor);
			bDrag=false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved (MouseEvent e) {
		int x=e.getX(), y=e.getY();
		if(y<=iRowHeight) {
			int xb=x;
			x=x+iX-iGridWidth;
			int i=iColCount-1;
			for(;i>=0;i--) {
				if(x>-7 && x<7) break;
				x+=iColWidth[i];        
			}
			if(i>=0) {
				if(!bDrag) {
					setCursor(resizeCursor);
					bDrag=true;
					iXDrag=xb-iColWidth[i];
					iColDrag=i;
				}
				return;
			}
		}
		if(bDrag) {
			setCursor(defaultCursor);
			bDrag=false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged (MouseEvent e) {
		if (e.isPopupTrigger() || e.isMetaDown())
			return;
		int x=e.getX(), y=e.getY();
		if(bDrag && x<tc.getSize().width) {
			int w=x-iXDrag;
			if(w<0) w=0;
			iColWidth[iColDrag]=w;
			columnsManuallyAdjusted = true;
			adjustHScroll();
			tc.repaint();
		} else {
			extendSelection(x, y);
		}
	}

 	/* (non-Javadoc)
	  * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	  */
	 public void mouseReleased (MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked (MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered (MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent event) {
		synchronized(this) {
			int rot = event.getWheelRotation();
			sbVert.setValue(sbVert.getValue()+rot);
			iY=iRowHeight*sbVert.getValue();
			tc.repaint();
		}
	}

	/**
	 * Scroll.
	 *
	 * @param inc the inc
	 */
	private void scroll(int inc) {
		synchronized(this) {
			sbVert.setValue(sbVert.getValue()+inc);
			iY=iRowHeight*sbVert.getValue();
			tc.repaint();
		}
	}

	/**
	 *  Unused keyPressed and keyTyped events will be passed to 'listener'.
	 *
	 * @param listener the listener
	 */
	public void addKeyListener(KeyListener listener) {
		keyListener = listener;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	public void addMouseListener(MouseListener listener) {
		tc.addMouseListener(listener);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key==KeyEvent.VK_BACK_SPACE)
			clearSelection();
		else if (key==KeyEvent.VK_UP)
			scroll(-1);
		else if (key==KeyEvent.VK_DOWN)
			scroll(1);
		else if (keyListener!=null&&key!=KeyEvent.VK_S&& key!=KeyEvent.VK_C && key!=KeyEvent.VK_X
		&& key!=KeyEvent.VK_A && key!=KeyEvent.VK_F && key!=KeyEvent.VK_G)
			keyListener.keyPressed(e);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased (KeyEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped (KeyEvent e) {
		if (keyListener!=null)
			keyListener.keyTyped(e);
	}
  
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed (ActionEvent e) {
		String cmd=e.getActionCommand();
		doCommand(cmd);
	}

 	/**
	  * Do command.
	  *
	  * @param cmd the cmd
	  */
	 void doCommand(String cmd) {
 		if (cmd==null)
 			return;
		if (cmd.equals("Save As..."))
			saveAs("");
		else if (cmd.equals("Cut"))
			cutSelection();
		else if (cmd.equals("Copy"))
			copySelection();
		else if (cmd.equals("Clear"))
			clearSelection();
		else if (cmd.equals("Select All"))
			selectAll();
		else if (cmd.equals("Find..."))
			find(null);
		else if (cmd.equals("Find Next"))
			find(searchString);
		else if (cmd.equals("Rename..."))
			rename(null);
		else if (cmd.equals("Duplicate..."))
			duplicate();
		else if (cmd.equals("Summarize"))
			IJ.doCommand("Summarize");
		else if (cmd.equals("Distribution..."))
			IJ.doCommand("Distribution...");
		else if (cmd.equals("Clear Results"))
			IJ.doCommand("Clear Results");
		else if (cmd.equals("Set Measurements..."))
			IJ.doCommand("Set Measurements...");
 		else if (cmd.equals("Options..."))
			IJ.doCommand("Input/Output...");
	}
 	
 	/* (non-Javadoc)
	  * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	  */
	 public void lostOwnership (Clipboard clip, Transferable cont) {}

	/**
	 * Find.
	 *
	 * @param s the s
	 */
	private void find(String s) {
		int first = 0;
		if (s==null) {
			GenericDialog gd = new GenericDialog("Find...", getTextWindow());
			gd.addStringField("Find: ", searchString, 20);
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			s = gd.getNextString();
		} else {
			if (selEnd>=0 && selEnd<iRowCount-1)
				first = selEnd + 1;
			else {
				IJ.beep();
				return;
			}
		}
		if (s.equals(""))
			return;
		boolean found = false;
		for (int i=first; i<iRowCount; i++) {
			String line = new String((char[])(vData.elementAt(i)));
			if (line.contains(s)) {
				setSelection(i, i);
				found = true;
				first = i + 1;
				break;
			}
		}
		if (!found) {
			IJ.beep();
			first = 0;
		}
		searchString = s;
	}
	
	/**
	 * Gets the text window.
	 *
	 * @return the text window
	 */
	private TextWindow getTextWindow() {
		Component comp = getParent();
		if (comp==null || !(comp instanceof TextWindow))
			return null;
		else
			return (TextWindow)comp;
	}

	/**
	 * Rename.
	 *
	 * @param title2 the title 2
	 */
	void rename(String title2) {
		if (rt==null) return;
		if (title2!=null && title2.equals(""))
			title2 = null;
		TextWindow tw = getTextWindow();
		if (tw==null)
			return;
		if (title2==null) {
			GenericDialog gd = new GenericDialog("Rename", tw);
			gd.addStringField("Title:", getNewTitle(title), 20);
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			title2 = gd.getNextString();
		}
		String title1 = title;
		if (title!=null && title.equals("Results")) {
			IJ.setTextPanel(null);
			Analyzer.setUnsavedMeasurements(false);
			Analyzer.setResultsTable(null);
			Analyzer.resetCounter();
		}
		if (title2.equals("Results")) {
			//tw.setVisible(false);
			tw.dispose();
			WindowManager.removeWindow(tw);
			flush();
			rt.show("Results");
		} else {
			tw.setTitle(title2);
			int mbSize = tw.mb!=null?tw.mb.getMenuCount():0;
			if (mbSize>0 && tw.mb.getMenu(mbSize-1).getLabel().equals("Results"))
				tw.mb.remove(mbSize-1);
			title = title2;
		}
		Menus.updateWindowMenuItem(title1, title2);
		if (Recorder.record)
			Recorder.recordString("IJ.renameResults(\""+title2+"\");\n");
	}

	/**
	 * Duplicate.
	 */
	void duplicate() {
		if (rt==null) return;
		ResultsTable rt2 = (ResultsTable)rt.clone();
		String title2 = IJ.getString("Title:", getNewTitle(title));
		if (!title2.equals("")) {
			if (title2.equals("Results")) title2 = "Results2";
			rt2.show(title2);
		}
	}
	
	/**
	 * Gets the new title.
	 *
	 * @param oldTitle the old title
	 * @return the new title
	 */
	private String getNewTitle(String oldTitle) {
		if (oldTitle==null)
			return "Table2";
		String title2 = oldTitle;
		if (title2.endsWith("-1") || title2.endsWith("-2"))
			title2 = title2.substring(0,title.length()-2);
		String title3 = title2+"-1";
		if (title3.equals(oldTitle))
			title3 = title2+"-2";
        return title3;
	}
	
	/**
	 * Select.
	 *
	 * @param x the x
	 * @param y the y
	 */
	void select(int x,int y) {
		Dimension d = tc.getSize();
		if(iRowHeight==0 || x>d.width || y>d.height)
			return;
     	int r=(y/iRowHeight)-1+iFirstRow;
     	int lineWidth = iGridWidth;
		if (iColCount==1 && tc.fMetrics!=null && r>=0 && r<iRowCount) {
			char[] chars = (char[])vData.elementAt(r);
			lineWidth = Math.max(tc.fMetrics.charsWidth(chars,0,chars.length), iGridWidth);
		}
      	if (r>=0 && r<iRowCount && x<lineWidth) {
			selOrigin = r;
			selStart = r;
			selEnd = r;
		} else {
			resetSelection();
			selOrigin = r;
			if (r>=iRowCount)
				selOrigin = iRowCount-1;
		}
		tc.repaint();
		selLine=r;
		Interpreter interp = Interpreter.getInstance();
		if (interp!=null && title.equals("Debug"))
			interp.showArrayInspector(r);
	}

	/**
	 * Extend selection.
	 *
	 * @param x the x
	 * @param y the y
	 */
	void extendSelection(int x,int y) {
		Dimension d = tc.getSize();
		if(iRowHeight==0 || x>d.width || y>d.height)
			return;
     	int r=(y/iRowHeight)-1+iFirstRow;
     	if(r>=0 && r<iRowCount) {
			if (r<selOrigin) {
				selStart = r;
				selEnd = selOrigin;
				
			} else {
				selStart = selOrigin;
				selEnd = r;
			}
		}
		tc.repaint();
		selLine=r;
	}

    /**
     *  Converts a y coordinate in pixels into a row index.
     *
     * @param y the y
     * @return the int
     */
    public int rowIndex(int y) {
        if (y > tc.getSize().height)
        	return -1;
        else
        	return (y/iRowHeight)-1+iFirstRow;
    }

	/**
	 * 	Copies the current selection to the system clipboard. 
	 * 	Returns the number of characters copied.
	 *
	 * @return the int
	 */
	public int copySelection() {
		if (Recorder.record && title.equals("Results"))
			Recorder.record("String.copyResults");
		if (selStart==-1 || selEnd==-1)
			return copyAll();
		StringBuffer sb = new StringBuffer();
		if (Prefs.copyColumnHeaders && labels!=null && !labels.equals("") && selStart==0 && selEnd==iRowCount-1) {
			if (Prefs.noRowNumbers) {
				String s = labels;
				int index = s.indexOf("\t");
				if (index!=-1)
					s = s.substring(index+1, s.length());
				sb.append(s);
			} else
				sb.append(labels);
			sb.append('\n');
		}
		for (int i=selStart; i<=selEnd; i++) {
			char[] chars = (char[])(vData.elementAt(i));
			String s = new String(chars);
			if (s.endsWith("\t"))
				s = s.substring(0, s.length()-1);
			if (Prefs.noRowNumbers && labels!=null && !labels.equals("")) {
				int index = s.indexOf("\t");
				if (index!=-1)
					s = s.substring(index+1, s.length());
				sb.append(s);
			} else
				sb.append(s);
			if (i<selEnd || selEnd>selStart) sb.append('\n');
		}
		String s = new String(sb);
		Clipboard clip = getToolkit().getSystemClipboard();
		if (clip==null) return 0;
		StringSelection cont = new StringSelection(s);
		clip.setContents(cont,this);
		if (s.length()>0) {
			IJ.showStatus((selEnd-selStart+1)+" lines copied to clipboard");
			if (this.getParent() instanceof ImageJ)
				Analyzer.setUnsavedMeasurements(false);
		}
		return s.length();
	}
	
	/**
	 * Copy all.
	 *
	 * @return the int
	 */
	int copyAll() {
		selectAll();
		int count = selEnd - selStart + 1;
		if (count>0)
			copySelection();
		resetSelection();
		unsavedLines = false;
		return count;
	}
	
	/**
	 * Cut selection.
	 */
	void cutSelection() {
		if (selStart==-1 || selEnd==-1)
			selectAll();
		copySelection();
		clearSelection();
	}	

	/** Deletes the selected lines. */
	public void clearSelection() {
		if (selStart==-1 || selEnd==-1) {
			if (getLineCount()>0)
				IJ.error("Selection required");
			return;
		}
		if (Recorder.record)
			Recorder.recordString("IJ.deleteRows("+selStart+", "+selEnd+");\n");
		int first=selStart, last=selEnd, rows=iRowCount;
		if (selStart==0 && selEnd==(iRowCount-1)) {
			vData.removeAllElements();
			iRowCount = 0;
			if (rt!=null) {
				if (IJ.isResultsWindow() && IJ.getTextPanel()==this) {
					Analyzer.setUnsavedMeasurements(false);
					Analyzer.resetCounter();
				} else
					rt.reset();
			}
		} else {
			int rowCount = iRowCount;
			boolean atEnd = rowCount-selEnd<8;
			int count = selEnd-selStart+1;
			for (int i=0; i<count; i++) {
				vData.removeElementAt(selStart);
				iRowCount--;
			}
			if (rt!=null && rowCount==rt.getCounter()) {
				for (int i=0; i<count; i++)
					rt.deleteRow(selStart);
				rt.show(title);
				if (!atEnd) {
					iY = 0;
					tc.repaint();
				}
			}
		}
		clearOverlay(first, last, rows);
		selStart=-1; selEnd=-1; selOrigin=-1; selLine=-1; 
		adjustVScroll();
		tc.repaint();
	}
	
	/**
	 * Clear overlay.
	 *
	 * @param first the first
	 * @param last the last
	 * @param rows the rows
	 */
	private void clearOverlay(int first, int last, int rows) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			return;
		Overlay overlay = imp.getOverlay();
		if (overlay==null)
			return;
		if (overlay.size()!=rows)
			return;
		String name1 = overlay.get(0).getName();
		String name2 = overlay.get(overlay.size()-1).getName();
		if (!"1".equals(name1) || !(""+rows).equals(name2))
			return;
		int count = last-first+1;
		if (overlay.size()==count) {
			if (count==1 || IJ.showMessageWithCancel("ImageJ", "Delete the "+overlay.size()+" element overlay?  "))
				imp.setOverlay(null);
			return;
		}
		for (int i=0; i<count; i++)
			overlay.remove(first);
		for (int i=first; i<overlay.size(); i++)
			overlay.get(i).setName(""+(i+1));
		imp.draw();
	}

	/** Deletes all the lines. */
	public void clear() {
		if (vData==null) return;
		vData.removeAllElements();
		iRowCount = 0;
		selStart=-1; selEnd=-1; selOrigin=-1; selLine=-1;
		adjustVScroll();
		tc.repaint();
	}

	/** Selects all the lines in this TextPanel. */
	public void selectAll() {
		if (selStart==0 && selEnd==iRowCount-1) {
			resetSelection();
			return;
		}
		selStart = 0;
		selEnd = iRowCount-1;
		selOrigin = 0;
		tc.repaint();
		selLine=-1;
	}

	/** Clears the selection, if any. */
	public void resetSelection() {
		selStart=-1;
		selEnd=-1;
		selOrigin=-1;
		selLine=-1;
		if (iRowCount>0)
			tc.repaint();
	}
	
	/**
	 *  Creates a selection and insures that it is visible.
	 *
	 * @param startLine the start line
	 * @param endLine the end line
	 */
	public void setSelection (int startLine, int endLine) {
		if (startLine>endLine) endLine = startLine;
		if (startLine<0) startLine = 0;
		if (endLine<0) endLine = 0;
		if (startLine>=iRowCount) startLine = iRowCount-1;
		if (endLine>=iRowCount) endLine = iRowCount-1;
		selOrigin = startLine;
		selStart = startLine;
		selEnd = endLine;
		int vstart = sbVert.getValue();
		int visible = sbVert.getVisibleAmount()-1;
		if (startLine<vstart) {
			sbVert.setValue(startLine);
			iY=iRowHeight*startLine;
		} else if (endLine>=vstart+visible) {
			vstart = endLine - visible + 1;
			if (vstart<0) vstart = 0;
			sbVert.setValue(vstart);
			iY=iRowHeight*vstart;
		}
		tc.repaint();
	}
	
	

	/**
	 *  Writes all the text in this TextPanel to a file.
	 *
	 * @param pw the pw
	 */
	public void save(PrintWriter pw) {
		resetSelection();
		if (labels!=null && !labels.equals(""))
			pw.println(labels);
		for (int i=0; i<iRowCount; i++) {
			char[] chars = (char[])(vData.elementAt(i));
			String s = new String(chars);
			if (s.endsWith("\t"))
				s = s.substring(0, s.length()-1);
			pw.println(s);
		}
		unsavedLines = false;
	}

	/**
	 *  Saves all the text in this TextPanel to a file. Set
	 * 		'path' to "" to display a save as dialog. Returns
	 * 		'false' if the user cancels the save as dialog.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean saveAs(String path) {
		boolean isResults = IJ.isResultsWindow() && IJ.getTextPanel()==this;
		boolean summarized = false;
		if (isResults) {
			String lastLine = iRowCount>=2?getLine(iRowCount-2):null;
			summarized = lastLine!=null && lastLine.startsWith("Max");
		}
		String fileName = null;
		if (rt!=null && rt.getCounter()!=0 && !summarized) {
			if (path==null || path.equals("")) {
				IJ.wait(10);
				String name = isResults?"Results":title;
				SaveDialog sd = new SaveDialog("Save Results", name, Prefs.get("options.ext", ".xls"));
				fileName = sd.getFileName();
				if (fileName==null) return false;
				path = sd.getDirectory() + fileName;
			}
			rt.save(path);
			TextWindow tw = getTextWindow();
			if (fileName!=null && tw!=null && !"Results".equals(title)) {
				tw.setTitle(fileName);
				title = fileName;
			}
		} else {
			if (path.equals("")) {
				IJ.wait(10);
				boolean hasHeadings = !getColumnHeadings().equals("");
				String ext = isResults||hasHeadings?Prefs.get("options.ext", ".xls"):".txt";
				if (ext.equals(".csv")) ext = ".txt";
				SaveDialog sd = new SaveDialog("Save as Text", title, ext);
				String file = sd.getFileName();
				if (file == null) return false;
				path = sd.getDirectory() + file;
			}
			PrintWriter pw = null;
			try {
				FileOutputStream fos = new FileOutputStream(path);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				pw = new PrintWriter(bos);
			}
			catch (IOException e) {
				IJ.error("Save As>Text", e.getMessage());
				return true;
			}
			save(pw);
			pw.close();
		}
		if (isResults) {
			Analyzer.setUnsavedMeasurements(false);
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Results", path);
		} else if (rt!=null) {
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Results", path);
		} else {
			if (Recorder.record && !IJ.isMacro())
				Recorder.record("saveAs", "Text", path);
		}
		IJ.showStatus("");
		return true;
	}

	/**
	 *  Returns all the text as a string.
	 *
	 * @return the text
	 */
	public synchronized String getText() {
		if (vData==null)
			return "";
		StringBuffer sb = new StringBuffer();
		if (labels!=null && !labels.equals("")) {
			sb.append(labels);
			sb.append('\n');
		}
		for (int i=0; i<iRowCount; i++) {
			if (vData==null) break;
			char[] chars = (char[])(vData.elementAt(i));
			sb.append(chars);
			sb.append('\n');
		}
		return new String(sb);
	}
	
	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 *  Returns the number of lines of text in this TextPanel.
	 *
	 * @return the line count
	 */
	public int getLineCount() {
		return iRowCount;
	}

	/**
	 *  Returns the specified line as a string. The argument
	 * 		must be greater than or equal to zero and less than 
	 * 		the value returned by getLineCount().
	 *
	 * @param index the index
	 * @return the line
	 */
	public String getLine(int index) {
		if (index<0 || index>=iRowCount)
			throw new IllegalArgumentException("index out of range: "+index);
		return new String((char[])(vData.elementAt(index)));
	}
	
	/**
	 *  Replaces the contents of the specified line, where 'index'
	 * 		must be greater than or equal to zero and less than 
	 * 		the value returned by getLineCount().
	 *
	 * @param index the index
	 * @param s the s
	 */
	public void setLine(int index, String s) {
		if (index<0 || index>=iRowCount)
			throw new IllegalArgumentException("index out of range: "+index);
		if (vData!=null) {
			vData.setElementAt(s.toCharArray(), index);	
			tc.repaint();
		}
	}

	/**
	 *  Returns the index of the first selected line, or -1 
	 * 		if there is no slection.
	 *
	 * @return the selection start
	 */
	public int getSelectionStart() {
		return selStart;
	}

	/**
	 *  Returns the index of the last selected line, or -1 
	 * 		if there is no slection.
	 *
	 * @return the selection end
	 */
	public int getSelectionEnd() {
		return selEnd;
	}
	
	/**
	 *  Sets the ResultsTable associated with this TextPanel.
	 *
	 * @param rt the new results table
	 */
	public void setResultsTable(ResultsTable rt) {
		this.rt = rt;
		if (!menusExtended)
			extendMenus();
	}
	
	/**
	 *  Returns the ResultsTable associated with this TextPanel, or null.
	 *
	 * @return the results table
	 */
	public ResultsTable getResultsTable() {
		return rt;
	}

	/**
	 * Extend menus.
	 */
	private void extendMenus() {
		pm.addSeparator();
		addPopupItem("Rename...");
		addPopupItem("Duplicate...");
		if (fileMenu!=null) {
			fileMenu.add("Rename...");
			fileMenu.add("Duplicate...");
		}
		menusExtended = true;
	}

	/**
	 * Scroll to top.
	 */
	public void scrollToTop() {
		sbVert.setValue(0);
		iY = 0;
		for (int i=0;i<iColCount;i++)
			tc.calcAutoWidth(i);
		adjustHScroll();
		tc.repaint();
	}
        
	/**
	 * Flush.
	 */
	void flush() {
		if (vData!=null)
			vData.removeAllElements();
		vData = null;
	}
	
}
