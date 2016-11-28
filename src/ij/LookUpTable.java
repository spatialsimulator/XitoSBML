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
package ij;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;

// TODO: Auto-generated Javadoc
/** This class represents a color look-up table. */
public class LookUpTable extends Object {
	
	/** The height. */
	private int width, height;
	
	/** The pixels. */
	private byte[] pixels;
	
	/** The map size. */
	private int mapSize = 0;
	
	/** The cm. */
	private ColorModel cm;
	
	/** The b LUT. */
	private byte[] rLUT, gLUT,bLUT;

	/**
	 *  Constructs a LookUpTable object from an AWT Image.
	 *
	 * @param img the img
	 */
	public LookUpTable(Image img) {
		PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
			cm = pg.getColorModel();
		}
		catch (InterruptedException e){};
		getColors(cm);
	}

	/**
	 *  Constructs a LookUpTable object from a ColorModel.
	 *
	 * @param cm the cm
	 */
	public LookUpTable(ColorModel cm) {
		this.cm = cm;
		getColors(cm);
	}
	
	/**
	 * Gets the colors.
	 *
	 * @param cm the cm
	 * @return the colors
	 */
	void getColors(ColorModel cm) {
    	if (cm instanceof IndexColorModel) {
    		IndexColorModel m = (IndexColorModel)cm;
    		mapSize = m.getMapSize();
    		rLUT = new byte[mapSize];
    		gLUT = new byte[mapSize];
    		bLUT = new byte[mapSize];
    		m.getReds(rLUT); 
    		m.getGreens(gLUT); 
    		m.getBlues(bLUT); 
    	}
	}
	
	/**
	 * Gets the map size.
	 *
	 * @return the map size
	 */
	public int getMapSize() {
		return mapSize;
	}
    
    /**
     * Gets the reds.
     *
     * @return the reds
     */
    public byte[] getReds() {
    	return rLUT;
    }

    /**
     * Gets the greens.
     *
     * @return the greens
     */
    public byte[] getGreens() {
    	return gLUT;
    }

    /**
     * Gets the blues.
     *
     * @return the blues
     */
    public byte[] getBlues() {
    	return bLUT;
    }

	/**
	 * Gets the color model.
	 *
	 * @return the color model
	 */
	public ColorModel getColorModel() {
		return cm;
	}

	/**
	 *  Returns <code>true</code> if this is a 256 entry grayscale LUT.
	 *
	 * @return true, if is grayscale
	 * @see ij.process.ImageProcessor#isColorLut
	 */
	public boolean isGrayscale() {
		boolean isGray = true;
		
		if (mapSize < 256)
			return false;
		for (int i=0; i<mapSize; i++)
			if ((rLUT[i] != gLUT[i]) || (gLUT[i] != bLUT[i]))
				isGray = false;
		return isGray;
	}
			
	/**
	 * Draw color bar.
	 *
	 * @param g the g
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 */
	public void drawColorBar(Graphics g, int x, int y, int width, int height) {
		if (mapSize == 0)
			return;
		ColorProcessor cp = new ColorProcessor(width, height);
		double scale = 256.0/mapSize;
		for (int i = 0; i<256; i++) {
			int index = (int)(i/scale);
			cp.setColor(new Color(rLUT[index]&0xff,gLUT[index]&0xff,bLUT[index]&0xff));
			cp.moveTo(i,0); cp.lineTo(i,height);
		}
		g.drawImage(cp.createImage(),x,y,null);
		g.setColor(Color.black);
		g.drawRect(x, y, width, height);
	}

	/**
	 * Draw unscaled color bar.
	 *
	 * @param ip the ip
	 * @param x the x
	 * @param y the y
	 * @param width the width
	 * @param height the height
	 */
	public void drawUnscaledColorBar(ImageProcessor ip, int x, int y, int width, int height) {
		ImageProcessor bar = null;
		if (ip instanceof ColorProcessor)
			bar = new ColorProcessor(width, height);
		else
			bar = new ByteProcessor(width, height);
		if (mapSize == 0) {  //no color table; draw a grayscale bar
			for (int i = 0; i < 256; i++) {
				bar.setColor(new Color(i, i, i));
				bar.moveTo(i, 0); bar.lineTo(i, height);
			}
		}
		else {
			for (int i = 0; i<mapSize; i++) {
				bar.setColor(new Color(rLUT[i]&0xff, gLUT[i]&0xff, bLUT[i]&0xff));
				bar.moveTo(i, 0); bar.lineTo(i, height);
			}
		}
		ip.insert(bar, x,y);
		ip.setColor(Color.black);
		ip.drawRect(x-1, y, width+2, height);
	}
			
	/**
	 * Creates the grayscale color model.
	 *
	 * @param invert the invert
	 * @return the color model
	 */
	public static ColorModel createGrayscaleColorModel(boolean invert) {
		byte[] rLUT = new byte[256];
		byte[] gLUT = new byte[256];
		byte[] bLUT = new byte[256];
		if (invert)
			for(int i=0; i<256; i++) {
				rLUT[255-i]=(byte)i;
				gLUT[255-i]=(byte)i;
				bLUT[255-i]=(byte)i;
			}
		else {
			for(int i=0; i<256; i++) {
				rLUT[i]=(byte)i;
				gLUT[i]=(byte)i;
				bLUT[i]=(byte)i;
			}
		}
		return(new IndexColorModel(8, 256, rLUT, gLUT, bLUT));
	}
	
}

