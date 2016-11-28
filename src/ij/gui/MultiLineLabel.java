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
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**Custom component for displaying multiple lines. Based on 
   MultiLineLabel class from "Java in a Nutshell" by David Flanagan.*/
public class MultiLineLabel extends Canvas {
	
	/** The lines. */
	String[] lines;
	
	/** The num lines. */
	int num_lines;
	
	/** The margin width. */
	int margin_width = 6;
	
	/** The margin height. */
	int margin_height = 6;
	
	/** The line height. */
	int line_height;
	
	/** The line ascent. */
	int line_ascent;
	
	/** The line widths. */
	int[] line_widths;
	
	/** The max width. */
	int min_width, max_width;
    
    /**
     * Instantiates a new multi line label.
     *
     * @param label the label
     */
    // Breaks the specified label up into an array of lines.
    public MultiLineLabel(String label) {
        init(label);
    }
    

    /**
     * Instantiates a new multi line label.
     *
     * @param label the label
     * @param minimumWidth the minimum width
     */
    public MultiLineLabel(String label, int minimumWidth) {
        init(label);
        min_width = minimumWidth;
    }

    /**
     * Inits the.
     *
     * @param text the text
     */
    private void init(String text) {
        StringTokenizer t = new StringTokenizer(text, "\n");
        num_lines = t.countTokens();
        lines = new String[num_lines];
        line_widths = new int[num_lines];
        for (int i=0; i<num_lines; i++)
        	lines[i] = t.nextToken();
    }

    // Figures out how wide each line of the label
    /**
     * Measure.
     */
    // is, and how wide the widest line is.
    protected void measure() {
        FontMetrics fm = this.getFontMetrics(this.getFont());
        // If we don't have font metrics yet, just return.
        if (fm == null) return;
        line_height = fm.getHeight();
        line_ascent = fm.getAscent();
        max_width = 0;
        for(int i = 0; i < num_lines; i++) {
            line_widths[i] = fm.stringWidth(lines[i]);
            if (line_widths[i] > max_width) max_width = line_widths[i];
        }
    }
    

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        init(text);
        measure();
        repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setFont(java.awt.Font)
     */
    public void setFont(Font f) {
        super.setFont(f);
        measure();
        repaint();
    }


	// This method is invoked after our Canvas is first created
	// but before it can actually be displayed.  After we've
	// invoked our superclass's addNotify() method, we have font
	// metrics and can successfully call measure() to figure out
	/* (non-Javadoc)
	 * @see java.awt.Canvas#addNotify()
	 */
	// how big the label is.
	public void addNotify() {
		super.addNotify();
		measure();
	}
    

    // Called by a layout manager when it wants to
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    // know how big we'd like to be.  
    public Dimension getPreferredSize() {
        return new Dimension(Math.max(min_width, max_width + 2*margin_width), 
                     num_lines * line_height + 2*margin_height);
    }
    

    // Called when the layout manager wants to know
    /* (non-Javadoc)
     * @see java.awt.Component#getMinimumSize()
     */
    // the bare minimum amount of space we need to get by.
    public Dimension getMinimumSize() {
        return new Dimension(Math.max(min_width, max_width), num_lines * line_height);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Canvas#paint(java.awt.Graphics)
     */
    // Draws the label
    public void paint(Graphics g) {
        int x, y;
        Dimension d = this.getSize();
		if (!ij.IJ.isLinux()) setAntialiasedText(g);
        y = line_ascent + (d.height - num_lines * line_height)/2;
        for(int i = 0; i < num_lines; i++, y += line_height) {
            x = margin_width;
            g.drawString(lines[i], x, y);
        }
    }

	/**
	 * Sets the antialiased text.
	 *
	 * @param g the new antialiased text
	 */
	void setAntialiasedText(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

}
