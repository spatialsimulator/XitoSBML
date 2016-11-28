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
import ij.ImageJ;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GUI;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;

// TODO: Auto-generated Javadoc
/** This plugin continuously plots ImageJ's memory utilization. 
	Click on the plot to force the JVM to do garbage collection. */
public class MemoryMonitor extends PlugInFrame {
 	
	 /** The Constant HEIGHT. */
	 private static final int WIDTH=250, HEIGHT=90;
	
	/** The Constant LOC_KEY. */
	private static final String LOC_KEY = "memory.loc";
	
	/** The instance. */
	private static MemoryMonitor instance;
	
	/** The image. */
	private Image image;
	
	/** The g. */
	private Graphics2D g;
	
	/** The frames. */
	private int frames;
	
	/** The mem. */
	private double[] mem;
	
	/** The index. */
	private int index;
	
	/** The value. */
	private long value;
 	
	 /** The default max. */
	 private double defaultMax = 15*1204*1024; // 15MB
	
	/** The max. */
	private double max = defaultMax;
	
	/** The max memory. */
	private long maxMemory = IJ.maxMemory();

	/**
	 * Instantiates a new memory monitor.
	 */
	public MemoryMonitor() {
		super("Memory");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}
		instance = this;
		WindowManager.addWindow(this);
		
		setLayout(new BorderLayout());
		Canvas ic = new Canvas();
		ic.setSize(WIDTH, HEIGHT);
		add(ic);
		setResizable(false);
		pack();
		Point loc = Prefs.getLocation(LOC_KEY);
		if (loc!=null)
			setLocation(loc);
		else
			GUI.center(this);
		image = createImage(WIDTH,HEIGHT);
		g = (Graphics2D)image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setFont(new Font("SansSerif",Font.PLAIN,12));
		Graphics icg = ic.getGraphics();
		icg.drawImage(image, 0, 0, null);
		show();
		ImageJ ij = IJ.getInstance();
		if (ij!=null) {
			addKeyListener(ij);
			ic.addKeyListener(ij);
			ic.addMouseListener(ij);
		}
		mem = new double[WIDTH+1];
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
       	while (true) {
			updatePlot();
         	addText();
			icg.drawImage(image, 0, 0, null);
        	IJ.wait(50);
       		frames++;
		}
	}
	
    /**
     * Adds the text.
     */
    void addText() {
    	double value2 = (double)value/1048576L;
    	String s = IJ.d2s(value2,value2>50?0:2)+"MB";
    	if (maxMemory>0L) {
			double percent = value*100/maxMemory;
			s += " ("+(percent<1.0?"<1":IJ.d2s(percent,0)) + "%)";
		}
		g.drawString(s, 2, 15);
		String images = ""+WindowManager.getImageCount();
		g.drawString(images, WIDTH-(5+images.length()*8), 15);
	}

	/**
	 * Update plot.
	 */
	void updatePlot() {
		double used = IJ.currentMemory();
		if (frames%10==0) value = (long)used;
		if (used>0.86*max) max *= 2.0;
		mem[index++] = used;
		if (index==mem.length) index = 0;
		double maxmax = 0.0;
		for (int i=0; i<mem.length; i++) {
			if (mem[i]>maxmax) maxmax= mem[i];
		}
		if (maxmax<defaultMax) max=defaultMax*2;
		if (maxmax<defaultMax/2) max = defaultMax;
		int index2 = index+1;
		if (index2==mem.length) index2 = 0;
		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
	 	g.setColor(Color.black);	
		double scale = HEIGHT/max;
		int x1 = 0;
		int y1 = HEIGHT-(int)(mem[index2]*scale);
		for (int x2=1; x2<WIDTH; x2++) {
			index2++;
			if (index2==mem.length) index2 = 0;
			int y2 = HEIGHT-(int)(mem[index2]*scale);
			g.drawLine(x1, y1, x2, y2);
			x1=x2; y1=y2;
		}
	}

    /* (non-Javadoc)
     * @see ij.plugin.frame.PlugInFrame#close()
     */
    public void close() {
	 	super.close();
		instance = null;
		Prefs.saveLocation(LOC_KEY, getLocation());
	}

}
