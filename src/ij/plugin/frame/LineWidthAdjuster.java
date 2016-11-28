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
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.util.Tools;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;

// TODO: Auto-generated Javadoc
/** Adjusts the width of line selections.  */
public class LineWidthAdjuster extends PlugInFrame implements PlugIn,
	Runnable, AdjustmentListener, TextListener, ItemListener {

	/** The Constant LOC_KEY. */
	public static final String LOC_KEY = "line.loc";
	
	/** The slider range. */
	int sliderRange = 300;
	
	/** The slider. */
	Scrollbar slider;
	
	/** The value. */
	int value;
	
	/** The set text. */
	boolean setText;
	
	/** The instance. */
	static LineWidthAdjuster instance; 
	
	/** The thread. */
	Thread thread;
	
	/** The done. */
	boolean done;
	
	/** The tf. */
	TextField tf;
	
	/** The checkbox. */
	Checkbox checkbox;

	/**
	 * Instantiates a new line width adjuster.
	 */
	public LineWidthAdjuster() {
		super("Line Width");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}		
		WindowManager.addWindow(this);
		instance = this;
		slider = new Scrollbar(Scrollbar.HORIZONTAL, Line.getWidth(), 1, 1, sliderRange+1);
		slider.setFocusable(false); // prevents blinking on Windows
				
		Panel panel = new Panel();
		int margin = IJ.isMacOSX()?5:0;
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c  = new GridBagConstraints();
		panel.setLayout(grid);
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1;
		c.ipadx = 100;
		c.insets = new Insets(margin, 15, margin, 5);
		c.anchor = GridBagConstraints.CENTER;
		grid.setConstraints(slider, c);
		panel.add(slider);
		c.ipadx = 0;  // reset
		c.gridx = 1;
		c.insets = new Insets(margin, 5, margin, 15);
		tf = new TextField(""+Line.getWidth(), 4);
		tf.addTextListener(this);
		grid.setConstraints(tf, c);
    	panel.add(tf);
		
		c.gridx = 2;
		c.insets = new Insets(margin, 25, margin, 5);
		checkbox = new Checkbox("Spline Fit", isSplineFit());
		checkbox.addItemListener(this);
		panel.add(checkbox);
		
		add(panel, BorderLayout.CENTER);
		slider.addAdjustmentListener(this);
		slider.setUnitIncrement(1);
		
		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		setResizable(false);
		show();
		thread = new Thread(this, "LineWidthAdjuster");
		thread.start();
		setup();
		addKeyListener(IJ.getInstance());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		value = slider.getValue();
		setText = true;
		notify();
	}

    /* (non-Javadoc)
     * @see java.awt.event.TextListener#textValueChanged(java.awt.event.TextEvent)
     */
    public  synchronized void textValueChanged(TextEvent e) {
        int width = (int)Tools.parseDouble(tf.getText(), -1);
		//IJ.log(""+width);
        if (width==-1) return;
        if (width<0) width=1;
        if (width!=Line.getWidth()) {
			slider.setValue(width);
        	value = width;
        	notify();
        }
    }
	
	/**
	 * Setup.
	 */
	void setup() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	// Separate thread that does the potentially time-consuming processing 
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
				if (done) return;
				Line.setWidth(value);
				if (setText) tf.setText(""+value);
				setText = false;
				updateRoi();
			}
		}
	}
	
	/**
	 * Update roi.
	 */
	private static void updateRoi() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			Roi roi = imp.getRoi();
			if (roi!=null && roi.isLine()) {
				roi.updateWideLine(Line.getWidth());
				imp.draw();
				return;
			}
		}
		if (Roi.previousRoi==null) return;
		int id = Roi.previousRoi.getImageID();
		if (id>=0) return;
		imp = WindowManager.getImage(id);
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi!=null && roi.isLine()) {
			roi.updateWideLine(Line.getWidth());
			imp.draw();
		}
	}
	
	/**
	 * Checks if is spline fit.
	 *
	 * @return true, if is spline fit
	 */
	boolean isSplineFit() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return false;
		Roi roi = imp.getRoi();
		if (roi==null) return false;
		if (!(roi instanceof PolygonRoi)) return false;
		return ((PolygonRoi)roi).isSplineFit();
	}

    /** Overrides close() in PlugInFrame. */
	public void close() {
		super.close();
		instance = null;
		done = true;
		Prefs.saveLocation(LOC_KEY, getLocation());
		synchronized(this) {notify();}
	}

    /* (non-Javadoc)
     * @see ij.plugin.frame.PlugInFrame#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent e) {
    	super.windowActivated(e);
    	checkbox.setState(isSplineFit());
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		boolean selected = e.getStateChange()==ItemEvent.SELECTED;
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{checkbox.setState(false); return;};
		Roi roi = imp.getRoi();
		if (roi==null || !(roi instanceof PolygonRoi))
			{checkbox.setState(false); return;};
		int type = roi.getType();
		if (type==Roi.FREEROI || type==Roi.FREELINE)
			{checkbox.setState(false); return;};;
		PolygonRoi poly = (PolygonRoi)roi;
		boolean splineFit = poly.isSplineFit();
		if (selected && !splineFit)
			{poly.fitSpline(); imp.draw();}
		else if (!selected && splineFit)
			{poly.removeSplineFit(); imp.draw();}
	}
	
	/**
	 * Update.
	 */
	public static void update() {
		if (instance==null) return;
		instance.checkbox.setState(instance.isSplineFit());
		int sliderWidth = instance.slider.getValue();
		int lineWidth = Line.getWidth();
		if (lineWidth!=sliderWidth && lineWidth<=200) {
			instance.slider.setValue(lineWidth);
			instance.tf.setText(""+lineWidth);
		}
	}
	
} 

