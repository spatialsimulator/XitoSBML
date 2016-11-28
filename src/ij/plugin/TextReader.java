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
package ij.plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;


// TODO: Auto-generated Javadoc
/** This plugin opens a tab or comma delimeted text file as an image.
	Modified to accept commas as delimiters on 4/22/08 by 
	Jay Unruh, Stowers Institute for Medical Research. */
public class TextReader implements PlugIn {
    
    /** The width. */
    int words = 0, chars = 0, lines = 0, width=1;;
    
    /** The path. */
    String directory, name, path;
    
    /** The hide error messages. */
    boolean hideErrorMessages;
    
    /** The first tok. */
    String firstTok;
    
    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String arg) {
        if (showDialog()) {
            IJ.showStatus("Opening: " + path);
            ImageProcessor ip = open(path);
            if (ip!=null)
                new ImagePlus(name, ip).show();
        }
    }
    
    /**
     * Show dialog.
     *
     * @return true, if successful
     */
    boolean showDialog() {
        OpenDialog od = new OpenDialog("Open Text Image...", null);
        directory = od.getDirectory();
        name = od.getFileName();
        if (name!=null)
            path = directory + name;
        return name!=null;
    }
    
    /**
     *  Displays a file open dialog and opens the specified text file as a float image.
     *
     * @return the image processor
     */
    public ImageProcessor open() {
        if (showDialog())
            return open(path);
        else
            return null;
    }
    
    /**
     *  Opens the specified text file as a float image.
     *
     * @param path the path
     * @return the image processor
     */
    public ImageProcessor open(String path) {
        ImageProcessor ip = null;
        try {
            words = chars = lines = 0;
            Reader r = new BufferedReader(new FileReader(path));
            countLines(r);
            r.close();
            r = new BufferedReader(new FileReader(path));
            //int width = words/lines;
            if (width*lines==0)
            	return null;
            float[] pixels = new float[width*lines];
            ip = new FloatProcessor(width, lines, pixels, null);
            read(r, width*lines, pixels);
            r.close();
            int firstRowNaNCount = 0;
            for (int i=0; i<width; i++) {
            	if (i<pixels.length && Float.isNaN(pixels[i]))
            		firstRowNaNCount++;
            }
            if (firstRowNaNCount==width && !"NaN".equals(firstTok)) { // assume first row is header
            	ip.setRoi(0, 1, width, lines-1);
            	ip = ip.crop();
            }
            ip.resetMinAndMax();
        }
        catch (IOException e) {
            String msg = e.getMessage();
            if (msg==null || msg.equals(""))
                msg = ""+e;
			IJ.showProgress(1.0);
            if (!hideErrorMessages) 
            	IJ.error("Text Reader", msg);
            ip = null;
        }
        return ip;
    }
    
    /**
     * Hide error messages.
     */
    public void hideErrorMessages() {
        hideErrorMessages = true;
    }

    /**
     *  Returns the file name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Count lines.
     *
     * @param r the r
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void countLines(Reader r) throws IOException {
        StreamTokenizer tok = new StreamTokenizer(r);
        int wordsPerLine=0, wordsInPreviousLine=0;
        tok.resetSyntax();
        tok.wordChars(43, 43);
        tok.wordChars(45, 127);
        tok.whitespaceChars(0, 42);
        tok.whitespaceChars(44, 44);
        //tok.wordChars(33, 127);
        //tok.whitespaceChars(0, ' ');
		tok.whitespaceChars(128, 255);
        tok.eolIsSignificant(true);

        while (tok.nextToken() != StreamTokenizer.TT_EOF) {
            switch (tok.ttype) {
                case StreamTokenizer.TT_EOL:
                    lines++;
                    if (wordsPerLine==0)
                        lines--;  // ignore empty lines
                    if (lines==1 && wordsPerLine>0)
                        width = wordsPerLine;
                    if (lines>1 && wordsPerLine!=0 && wordsPerLine!=wordsInPreviousLine)
                        throw new IOException("Line "+lines+ " is not the same length as the first line.");
                    if (wordsPerLine!=0)
                        wordsInPreviousLine = wordsPerLine;
                    wordsPerLine = 0;
                    if (lines%20==0 && width>1 && lines<=width)
                        IJ.showProgress(((double)lines/width)/2.0);
                    break;
                case StreamTokenizer.TT_WORD:
                    words++;
                    wordsPerLine++;
                    break;
            }
        }
        if (wordsPerLine==width) 
            lines++; // last line does not end with EOL
   }

    /**
     * Read.
     *
     * @param r the r
     * @param size the size
     * @param pixels the pixels
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void read(Reader r, int size, float[] pixels) throws IOException {
        StreamTokenizer tok = new StreamTokenizer(r);
        tok.resetSyntax();
        tok.wordChars(43, 43);
        tok.wordChars(45, 127);
        tok.whitespaceChars(0, 42);
        tok.whitespaceChars(44, 44);
        //tok.wordChars(33, 127);
        //tok.whitespaceChars(0, ' ');
        tok.whitespaceChars(128, 255);
        //tok.parseNumbers();

        int i = 0;
        int inc = size/20;
        if (inc<1)
            inc = 1;
        while (tok.nextToken() != StreamTokenizer.TT_EOF) {
            if (tok.ttype==StreamTokenizer.TT_WORD) {
                if (i==0)
                	firstTok = tok.sval;
                pixels[i++] = (float)Tools.parseDouble(tok.sval, Double.NaN);
                if (i==size)
                    break;
                if (i%inc==0)
                    IJ.showProgress(0.5+((double)i/size)/2.0);
            }
        }
        IJ.showProgress(1.0);
    }

}
