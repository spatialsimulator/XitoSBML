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

import ij.macro.Interpreter;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

// TODO: Auto-generated Javadoc
/**
 * This is the progress bar that is displayed in the lower right hand corner of
 * the ImageJ window. Use one of the static IJ.showProgress() methods to display
 * and update the progress bar.
 */
public class ProgressBar extends Canvas {

    /** The canvas height. */
    private int canvasWidth, canvasHeight;
    
    /** The height. */
    private int x, y, width, height;
    
    /** The last time. */
    private long lastTime = 0;
    
    /** The show bar. */
    private boolean showBar;
    
    /** The batch mode. */
    private boolean batchMode;

    /** The bar color. */
    private Color barColor = Color.gray;
    
    /** The fill color. */
    private Color fillColor = new Color(204, 204, 255);
    
    /** The background color. */
    private Color backgroundColor = ij.ImageJ.backgroundColor;
    
    /** The frame brighter. */
    private Color frameBrighter = backgroundColor.brighter();
    
    /** The frame darker. */
    private Color frameDarker = backgroundColor.darker();
    
    /** The dual display. */
    private boolean dualDisplay = false;
    
    /** The slow X. */
    private double slowX = 0.0;//box
    
    /** The fast X. */
    private double fastX = 0.0;//dot

    /**
     * This constructor is called once by ImageJ at startup.
     *
     * @param canvasWidth the canvas width
     * @param canvasHeight the canvas height
     */
    public ProgressBar(int canvasWidth, int canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        x = 3;
        y = 5;
        width = canvasWidth - 8;
        height = canvasHeight - 7;
    }

    /**
     * Fill 3 D rect.
     *
     * @param g the g
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     */
    void fill3DRect(Graphics g, int x, int y, int width, int height) {
        g.setColor(fillColor);
        g.fillRect(x + 1, y + 1, width - 2, height - 2);
        g.setColor(frameDarker);
        g.drawLine(x, y, x, y + height);
        g.drawLine(x + 1, y, x + width - 1, y);
        g.setColor(frameBrighter);
        g.drawLine(x + 1, y + height, x + width, y + height);
        g.drawLine(x + width, y, x + width, y + height - 1);
    }

    /**
     * Updates the progress bar, where abs(progress) should run from 0 to 1.
     * If abs(<code>progress</code>) == 1 the bar is erased. The bar is updated only
     * if more than 90 ms have passed since the last call. Does nothing if the
     * ImageJ window is not present.
     * @param progress Length of the progress bar to display (0...1). 
     * Using <code>progress</code> with negative sign (0 .. -1) will regard subsequent calls with
     * positive argument as sub-ordinate processes that are displayed as moving dot.
     */
    public void show(double progress) {
        show(progress, false);
    }

    /**
     * Updates the progress bar, where abs(progress) should run from 0 to 1.
     * @param progress Length of the progress bar to display (0...1). 
     * @param showInBatchMode show progress bar in batch mode macros?
     */
    public void show(double progress, boolean showInBatchMode) {
        boolean finished = false;
        if (progress <= - 1) {
            finished = true;
        }
        if (!dualDisplay && progress >= 1) {
            finished = true;
        }

        if (!finished) {
            if (progress < 0) {
                slowX = -progress;
                fastX = 0.0;
                dualDisplay = true;
            } else if (dualDisplay) {
                fastX = progress;
            }
            if (!dualDisplay) {
                slowX = progress;
            }
        }
        if (!showInBatchMode && (batchMode || Interpreter.isBatchMode())) {
            return;
        }
        if (finished) {//clear the progress bar
            slowX = 0.0;
            fastX = 0.0;
            showBar = false;
            dualDisplay = false;
            repaint();
            return;
        }
        long time = System.currentTimeMillis();
        if (time - lastTime < 90 && progress != 1.0) {
            return;
        }
        lastTime = time;
        showBar = true;
        repaint();
    }

    /**
     * Updates the progress bar, where the length of the bar is set to
     * (<code>(abs(currentIndex)+1)/abs(finalIndex)</code> of the maximum bar
     * length. Use a negative <code>currentIndex</code> to show subsequent
     * plugin calls as moving dot. The bar is erased if
     * <code>currentIndex&gt;=finalIndex-1</code> or <code>finalIndex == 0</code>.
     *
     * @param currentIndex the current index
     * @param finalIndex the final index
     */
    public void show(int currentIndex, int finalIndex) {
        boolean wasNegative = currentIndex < 0;
        double progress = ((double) Math.abs(currentIndex) + 1.0) / Math.abs(finalIndex);
        if (wasNegative) {
            progress = -progress;
        }
        if (finalIndex == 0) {
            progress = -1;
        }
        show(progress);
    }

    /* (non-Javadoc)
     * @see java.awt.Canvas#update(java.awt.Graphics)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /* (non-Javadoc)
     * @see java.awt.Canvas#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (showBar) {
            fill3DRect(g, x - 1, y - 1, width + 1, height + 1);

            drawBar(g);
        } else {
            g.setColor(backgroundColor);
            g.fillRect(0, 0, canvasWidth, canvasHeight);
        }
    }

    /**
     * Draw bar.
     *
     * @param g the g
     */
    void drawBar(Graphics g) {
        int barEnd = (int) (width * slowX);
        g.setColor(barColor);
        g.fillRect(x, y, barEnd, height);
        if (dualDisplay && fastX > 0) {
            int dotPos = (int) (width * fastX);
            g.setColor(Color.BLACK);
            if (dotPos > 1 && dotPos < width - 7) {
                g.fillOval(dotPos, y + 3, 7, 7);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return new Dimension(canvasWidth, canvasHeight);
    }

    /**
     * Sets the batch mode.
     *
     * @param batchMode the new batch mode
     */
    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

}
