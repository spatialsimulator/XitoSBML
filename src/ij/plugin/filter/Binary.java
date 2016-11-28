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
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.frame.ThresholdAdjuster;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;

// TODO: Auto-generated Javadoc
/** Implements the Erode, Dilate, Open, Close, Outline, Skeletonize
    and Fill Holes commands in the Process/Binary submenu. 
    Gabriel Landini contributed the clever binary fill algorithm
    that fills holes in objects by filling the background.
    Version 2009-06-23 preview added, interations can be aborted by escape (Michael Schmid)
*/
public class Binary implements ExtendedPlugInFilter, DialogListener {
    
    /** The Constant MAX_ITERATIONS. */
    static final int MAX_ITERATIONS = 100;
    
    /** The Constant NO_OPERATION. */
    static final String NO_OPERATION = "Nothing";
    
    /** The Constant outputTypes. */
    static final String[] outputTypes = {"Overwrite", "8-bit", "16-bit", "32-bit"};
    
    /** The Constant operations. */
    static final String[] operations = {NO_OPERATION, "Erode", "Dilate", "Open", "Close", "Outline", "Fill Holes", "Skeletonize"};

    /** The iterations. */
    //parameters / options
    static int iterations = 1;      //iterations for erode, dilate, open, close
    
    /** The count. */
    static int count = 1;           //nearest neighbor count for erode, dilate, open, close
    
    /** The operation. */
    String operation = NO_OPERATION;  //for dialog; will be copied to 'arg' for actual previewing

    /** The arg. */
    String arg;
    
    /** The imp. */
    ImagePlus imp;                  //null if only setting options with no preview possibility
    
    /** The pfr. */
    PlugInFilterRunner pfr;
    
    /** The do options. */
    boolean doOptions;              //whether options dialog is required
    
    /** The previewing. */
    boolean previewing;
    
    /** The escape pressed. */
    boolean escapePressed;
    
    /** The background. */
    int foreground, background;
    
    /** The flags. */
    int flags = DOES_8G | DOES_8C | SUPPORTS_MASKING | PARALLELIZE_STACKS | KEEP_PREVIEW | KEEP_THRESHOLD;
    
    /** The n passes. */
    int nPasses;

    /* (non-Javadoc)
     * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
     */
    public int setup(String arg, ImagePlus imp) {
        this.arg = arg;
        IJ.register(Binary.class);
        doOptions = arg.equals("options");
        if (doOptions) {
            if (imp == null) return NO_IMAGE_REQUIRED;  //options dialog does not need a (suitable) image
            ImageProcessor ip = imp.getProcessor();
            if (!(ip instanceof ByteProcessor)) return NO_IMAGE_REQUIRED;
            if (!((ByteProcessor)ip).isBinary()) return NO_IMAGE_REQUIRED;
        }
        return flags;
    }

    /* (non-Javadoc)
     * @see ij.plugin.filter.ExtendedPlugInFilter#showDialog(ij.ImagePlus, java.lang.String, ij.plugin.filter.PlugInFilterRunner)
     */
    public int showDialog (ImagePlus imp, String command, PlugInFilterRunner pfr) {
        if (doOptions) {
            this.imp = imp;
            this.pfr = pfr;
            GenericDialog gd = new GenericDialog("Binary Options");
            gd.addNumericField("Iterations (1-"+MAX_ITERATIONS+"):", iterations, 0, 3, "");
            gd.addNumericField("Count (1-8):", count, 0, 3, "");
            gd.addCheckbox("Black background", Prefs.blackBackground);
            gd.addCheckbox("Pad edges when eroding", Prefs.padEdges);
            gd.addChoice("EDM output:", outputTypes, outputTypes[EDM.getOutputType()]);
            if (imp != null) {
                gd.addChoice("Do:", operations, operation);
                gd.addPreviewCheckbox(pfr);
                gd.addDialogListener(this);
                previewing = true;
            }
            gd.addHelp(IJ.URL+"/docs/menus/process.html#options");
            gd.showDialog();
            previewing = false;
            if (gd.wasCanceled()) return DONE;
            if (imp==null) {                 //options dialog only, no do/preview
                dialogItemChanged(gd, null); //read dialog result
                return DONE;
            }
            return operation.equals(NO_OPERATION) ? DONE : IJ.setupDialog(imp, flags);
        } else {   //no dialog, 'arg' is operation type
            if (!((ByteProcessor)imp.getProcessor()).isBinary()) {
                IJ.error("8-bit binary (black and white only) image required.");
                return DONE;
            }
            return IJ.setupDialog(imp, flags);
        }
    }

    /* (non-Javadoc)
     * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
     */
    public boolean dialogItemChanged (GenericDialog gd, AWTEvent e) {
        iterations = (int)gd.getNextNumber();
        count = (int)gd.getNextNumber();
        boolean bb = Prefs.blackBackground;
        Prefs.blackBackground = gd.getNextBoolean();
        if ( Prefs.blackBackground!=bb)
        	ThresholdAdjuster.update();
        Prefs.padEdges = gd.getNextBoolean();
        EDM.setOutputType(gd.getNextChoiceIndex());
        boolean isInvalid = gd.invalidNumber();
        if (iterations<1) {iterations = 1; isInvalid = true;}
        if (iterations>MAX_ITERATIONS) {iterations = MAX_ITERATIONS; isInvalid = true;}
        if (count < 1)    {count = 1; isInvalid = true;}
        if (count > 8)    {count = 8; isInvalid = true;}
        if (isInvalid) return false;
        if (imp != null) {
            operation = gd.getNextChoice();
            arg = operation.toLowerCase();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see ij.plugin.filter.ExtendedPlugInFilter#setNPasses(int)
     */
    public void setNPasses (int nPasses) {
    	this.nPasses = nPasses;
    }

    /* (non-Javadoc)
     * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
     */
    public void run (ImageProcessor ip) {
        int fg = Prefs.blackBackground ? 255 : 0;
        foreground = ip.isInvertedLut() ? 255-fg : fg;
        background = 255 - foreground;
        ip.setSnapshotCopyMode(true);
        if (arg.equals("outline"))
            outline(ip);
        else if (arg.startsWith("fill"))
            fill(ip, foreground, background);
        else if (arg.startsWith("skel")) {
            ip.resetRoi(); skeletonize(ip);
        } else if (arg.equals("erode") || arg.equals("dilate"))
            doIterations((ByteProcessor)ip, arg);
        else if (arg.equals("open")) {
            doIterations(ip, "erode");
            doIterations(ip, "dilate");
        } else if (arg.equals("close")) {
            doIterations(ip, "dilate");
            doIterations(ip, "erode");
        }
        ip.setSnapshotCopyMode(false);
        ip.setBinaryThreshold();
    }

    /**
     * Do iterations.
     *
     * @param ip the ip
     * @param mode the mode
     */
    void doIterations (ImageProcessor ip, String mode) {
        if (escapePressed) return;
        if (!previewing && iterations>1)
            IJ.showStatus(arg+"... press ESC to cancel");
        for (int i=0; i<iterations; i++) {
            if (Thread.currentThread().isInterrupted()) return;
            if (IJ.escapePressed()) {
                escapePressed = true;
                ip.reset();
                return;
            }
            if (mode.equals("erode"))
                ((ByteProcessor)ip).erode(count, background);
            else
                ((ByteProcessor)ip).dilate(count, background);
        }
    }
    
    /**
     * Outline.
     *
     * @param ip the ip
     */
    void outline(ImageProcessor ip) {
        if (Prefs.blackBackground) ip.invert();
        ((ByteProcessor)ip).outline();
        if (Prefs.blackBackground) ip.invert();
    }

    /**
     * Skeletonize.
     *
     * @param ip the ip
     */
    void skeletonize(ImageProcessor ip) {
        if (Prefs.blackBackground) ip.invert();
        boolean edgePixels = hasEdgePixels(ip);
        ImageProcessor ip2 = expand(ip, edgePixels);
        ((ByteProcessor)ip2).skeletonize();
        ip = shrink(ip, ip2, edgePixels);
        if (Prefs.blackBackground) ip.invert();
    }

    /**
     * Checks for edge pixels.
     *
     * @param ip the ip
     * @return true, if successful
     */
    boolean hasEdgePixels(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        boolean edgePixels = false;
        for (int x=0; x<width; x++) { // top edge
            if (ip.getPixel(x, 0)==foreground)
                edgePixels = true;
        }
        for (int x=0; x<width; x++) { // bottom edge
            if (ip.getPixel(x, height-1)==foreground)
                edgePixels = true;
        }
        for (int y=0; y<height; y++) { // left edge
            if (ip.getPixel(0, y)==foreground)
                edgePixels = true;
        }
        for (int y=0; y<height; y++) { // right edge
            if (ip.getPixel(width-1, y)==foreground)
                edgePixels = true;
        }
        return edgePixels;
    }
    
    /**
     * Expand.
     *
     * @param ip the ip
     * @param hasEdgePixels the has edge pixels
     * @return the image processor
     */
    ImageProcessor expand(ImageProcessor ip, boolean hasEdgePixels) {
        if (hasEdgePixels) {
            ImageProcessor ip2 = ip.createProcessor(ip.getWidth()+2, ip.getHeight()+2);
            if (foreground==0) {
                ip2.setColor(255);
                ip2.fill();
            }
            ip2.insert(ip, 1, 1);
            //new ImagePlus("ip2", ip2).show();
            return ip2;
        } else
            return ip;
    }

    /**
     * Shrink.
     *
     * @param ip the ip
     * @param ip2 the ip 2
     * @param hasEdgePixels the has edge pixels
     * @return the image processor
     */
    ImageProcessor shrink(ImageProcessor ip, ImageProcessor ip2, boolean hasEdgePixels) {
        if (hasEdgePixels) {
            int width = ip.getWidth();
            int height = ip.getHeight();
            for (int y=0; y<height; y++)
                for (int x=0; x<width; x++)
                    ip.putPixel(x, y, ip2.getPixel(x+1, y+1));
        }
        return ip;
    }

    // Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
    /**
     * Fill.
     *
     * @param ip the ip
     * @param foreground the foreground
     * @param background the background
     */
    // 21/May/2008
    void fill(ImageProcessor ip, int foreground, int background) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        FloodFiller ff = new FloodFiller(ip);
        ip.setColor(127);
        for (int y=0; y<height; y++) {
            if (ip.getPixel(0,y)==background) ff.fill(0, y);
            if (ip.getPixel(width-1,y)==background) ff.fill(width-1, y);
        }
        for (int x=0; x<width; x++){
            if (ip.getPixel(x,0)==background) ff.fill(x, 0);
            if (ip.getPixel(x,height-1)==background) ff.fill(x, height-1);
        }
        byte[] pixels = (byte[])ip.getPixels();
        int n = width*height;
        for (int i=0; i<n; i++) {
        if (pixels[i]==127)
            pixels[i] = (byte)background;
        else
            pixels[i] = (byte)foreground;
        }
    }

}
