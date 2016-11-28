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
package ij.process;
import ij.IJ;
import ij.plugin.Colors;

import java.awt.Color;
import java.awt.image.IndexColorModel;

	// TODO: Auto-generated Javadoc
/** This is an indexed color model that allows an
		lower and upper bound to be specified. */
    public class LUT extends IndexColorModel implements Cloneable {
        
        /** The max. */
        public double min, max;
	
    /**
     *  Constructs a LUT from red, green and blue byte arrays, which must have a length of 256.
     *
     * @param r the r
     * @param g the g
     * @param b the b
     */
    public LUT(byte r[], byte g[], byte b[]) {
    	this(8, 256, r, g, b);
	}
	
    /**
     *  Constructs a LUT from red, green and blue byte arrays, where 'bits' 
     *     	must be 8 and 'size' must be less than or equal to 256.
     *
     * @param bits the bits
     * @param size the size
     * @param r the r
     * @param g the g
     * @param b the b
     */
    public LUT(int bits, int size, byte r[], byte g[], byte b[]) {
    	super(bits, size, r, g, b);
	}
	
	/**
	 * Instantiates a new lut.
	 *
	 * @param cm the cm
	 * @param min the min
	 * @param max the max
	 */
	public LUT(IndexColorModel cm, double min, double max) {
		super(8, cm.getMapSize(), getReds(cm), getGreens(cm), getBlues(cm));
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Gets the reds.
	 *
	 * @param cm the cm
	 * @return the reds
	 */
	static byte[] getReds(IndexColorModel cm) {
		byte[] reds=new byte[256]; cm.getReds(reds); return reds;
	}
	
	/**
	 * Gets the greens.
	 *
	 * @param cm the cm
	 * @return the greens
	 */
	static byte[] getGreens(IndexColorModel cm) {
		byte[] greens=new byte[256]; cm.getGreens(greens); return greens;
	}
	
	/**
	 * Gets the blues.
	 *
	 * @param cm the cm
	 * @return the blues
	 */
	static byte[] getBlues(IndexColorModel cm) {
		byte[] blues=new byte[256]; cm.getBlues(blues); return blues;
	}
	
	/**
	 * Gets the bytes.
	 *
	 * @return the bytes
	 */
	public byte[] getBytes() {
		int size = getMapSize();
		if (size!=256) return null;
		byte[] bytes = new byte[256*3];
		for (int i=0; i<256; i++) bytes[i] = (byte)getRed(i);
		for (int i=0; i<256; i++) bytes[256+i] = (byte)getGreen(i);
		for (int i=0; i<256; i++) bytes[512+i] = (byte)getBlue(i);
		return bytes;
	}
	
	/**
	 * Creates the inverted lut.
	 *
	 * @return the lut
	 */
	public LUT createInvertedLut() {
		int mapSize = getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		byte[] reds2 = new byte[mapSize];
		byte[] greens2 = new byte[mapSize];
		byte[] blues2 = new byte[mapSize];	
		getReds(reds); 
		getGreens(greens); 
		getBlues(blues);
		for (int i=0; i<mapSize; i++) {
			reds2[i] = (byte)(reds[mapSize-i-1]&255);
			greens2[i] = (byte)(greens[mapSize-i-1]&255);
			blues2[i] = (byte)(blues[mapSize-i-1]&255);
		}
		return new LUT(8, mapSize, reds2, greens2, blues2);
	}
	
	/**
	 *  Creates a color LUT from a Color.
	 *
	 * @param color the color
	 * @return the lut
	 */
	public static LUT createLutFromColor(Color color) {
		byte[] rLut = new byte[256];
		byte[] gLut = new byte[256];
		byte[] bLut = new byte[256];
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		double rIncr = ((double)red)/255d;
		double gIncr = ((double)green)/255d;
		double bIncr = ((double)blue)/255d;
		for (int i=0; i<256; ++i) {
			rLut[i] = (byte)(i*rIncr);
			gLut[i] = (byte)(i*gIncr);
			bLut[i] = (byte)(i*bIncr);
		}
		return new LUT(rLut, gLut, bLut);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.image.IndexColorModel#toString()
	 */
	public  String toString() {
		return "rgb[0]="+Colors.colorToString(new Color(getRGB(0)))+", rgb[255]="
			+Colors.colorToString(new Color(getRGB(255)))+", min="+IJ.d2s(min,4)+", max="+IJ.d2s(max,4);
	}

}
