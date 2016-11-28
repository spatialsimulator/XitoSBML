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
import ij.plugin.Colors;
import ij.util.Java2;
import ij.util.Tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Vector;


 // TODO: Auto-generated Javadoc
/** Displays a dialog that allows the user to select a color using three sliders. */
public class ColorChooser implements TextListener, AdjustmentListener {
	
	/** The sliders. */
	Vector colors, sliders;
	
	/** The panel. */
	ColorPanel panel;
	
	/** The initial color. */
	Color initialColor;
	
	/** The blue. */
	int red, green, blue;
	
	/** The use HSB. */
	boolean useHSB;
	
	/** The title. */
	String title;
	
	/** The frame. */
	Frame frame;

	/**
	 *  Constructs a ColorChooser using the specified title and initial color.
	 *
	 * @param title the title
	 * @param initialColor the initial color
	 * @param useHSB the use HSB
	 */
	public ColorChooser(String title, Color initialColor, boolean useHSB) {
		this(title, initialColor, useHSB, null);
	}
	
	/**
	 * Instantiates a new color chooser.
	 *
	 * @param title the title
	 * @param initialColor the initial color
	 * @param useHSB the use HSB
	 * @param frame the frame
	 */
	public ColorChooser(String title, Color initialColor, boolean useHSB, Frame frame) {
		this.title = title;
		if (initialColor==null) initialColor = Color.black;
		this.initialColor = initialColor;
		red = initialColor.getRed();
		green = initialColor.getGreen();
		blue = initialColor.getBlue();
		this.useHSB = useHSB;
		this.frame = frame;
	}

	/**
	 *  Displays a color selection dialog and returns the color selected by the user.
	 *
	 * @return the color
	 */
	public Color getColor() {
		GenericDialog gd = frame!=null?new GenericDialog(title, frame):new GenericDialog(title);
		gd.addSlider("Red:", 0, 255, red);
		gd.addSlider("Green:", 0, 255, green);
		gd.addSlider("Blue:", 0, 255, blue);
		panel = new ColorPanel(initialColor);
		gd.addPanel(panel, GridBagConstraints.CENTER, new Insets(10, 0, 0, 0));
		colors = gd.getNumericFields();
		for (int i=0; i<colors.size(); i++)
			((TextField)colors.elementAt(i)).addTextListener(this);
		sliders = gd.getSliders();
		for (int i=0; i<sliders.size(); i++)
			((Scrollbar)sliders.elementAt(i)).addAdjustmentListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) return null;
		int red = (int)gd.getNextNumber();
		int green = (int)gd.getNextNumber();
		int blue = (int)gd.getNextNumber();
		return new Color(red, green, blue);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.TextListener#textValueChanged(java.awt.event.TextEvent)
	 */
	public void textValueChanged(TextEvent e) {
		int red = (int)Tools.parseDouble(((TextField)colors.elementAt(0)).getText());
		int green = (int)Tools.parseDouble(((TextField)colors.elementAt(1)).getText());
		int blue = (int)Tools.parseDouble(((TextField)colors.elementAt(2)).getText());
		if (red<0) red=0; if (red>255) red=255;
		if (green<0) green=0; if (green>255) green=255;
		if (blue<0) blue=0; if (blue>255) blue=255;
		panel.setColor(new Color(red, green, blue));
		panel.repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		Object source = e.getSource();
		for (int i=0; i<sliders.size(); i++) {
			if (source==sliders.elementAt(i)) {
				Scrollbar sb = (Scrollbar)source;
				TextField tf = (TextField)colors.elementAt(i);
			}
		}
	}

}

class ColorPanel extends Panel {
	static final int WIDTH=150, HEIGHT=50;
	static Font font = new Font("Monospaced", Font.PLAIN, 18);
	Color c;
	 
	ColorPanel(Color c) {
		this.c = c;
	}

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	void setColor(Color c) {
		this.c = c;
	}

	public Dimension getMinimumSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	public void paint(Graphics g) {
		g.setColor(c);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		int intensity = (c.getRed()+c.getGreen()+c.getBlue())/3;
		Color c2 = intensity<128?Color.white:Color.black;
		g.setColor(c2);
		g.setFont(font);
		Java2.setAntialiasedText(g, true);
		String s = Colors.colorToString(c);
		g.drawString(s, 5, HEIGHT-5);
		g.setColor(Color.black);
		g.drawRect(0, 0, WIDTH-1, HEIGHT-1);
	}

}
