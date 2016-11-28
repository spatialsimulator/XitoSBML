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
import java.awt.Color;
import java.awt.Rectangle;

// TODO: Auto-generated Javadoc
/** This class does bit blitting of 16-bit images. */
public class ShortBlitter implements Blitter {

	/** The ip. */
	private ShortProcessor ip;
	
	/** The height. */
	private int width, height;
	
	/** The pixels. */
	private short[] pixels;
	
	/* (non-Javadoc)
	 * @see ij.process.Blitter#setTransparentColor(java.awt.Color)
	 */
	public void setTransparentColor(Color c) {
	}

	/**
	 *  Constructs a ShortBlitter from a ShortProcessor.
	 *
	 * @param ip the ip
	 */
	public ShortBlitter(ShortProcessor ip) {
		this.ip = ip;
		width = ip.getWidth();
		height = ip.getHeight();
		pixels = (short[])ip.getPixels();
	}

	/**
	 *  Copies the 16-bit image in 'ip' to (x,y) using the specified mode.
	 *
	 * @param ip the ip
	 * @param xloc the xloc
	 * @param yloc the yloc
	 * @param mode the mode
	 */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		Rectangle r1, r2;
		int srcIndex, dstIndex;
		int xSrcBase, ySrcBase;
		short[] srcPixels;
		
		int srcWidth = ip.getWidth();
		int srcHeight = ip.getHeight();
		r1 = new Rectangle(srcWidth, srcHeight);
		r1.setLocation(xloc, yloc);
		r2 = new Rectangle(width, height);
		if (!r1.intersects(r2))
			return;
		srcPixels = (short [])ip.getPixels();
//new ij.ImagePlus("srcPixels", new ShortProcessor(srcWidth, srcHeight, srcPixels, null)).show();
		r1 = r1.intersection(r2);
		xSrcBase = (xloc<0)?-xloc:0;
		ySrcBase = (yloc<0)?-yloc:0;
		int src, dst;
		for (int y=r1.y; y<(r1.y+r1.height); y++) {
			srcIndex = (y-yloc)*srcWidth + (r1.x-xloc);
			dstIndex = y * width + r1.x;
			switch (mode) {
				case COPY: case COPY_INVERTED: case COPY_TRANSPARENT:
					for (int i=r1.width; --i>=0;)
						pixels[dstIndex++] = srcPixels[srcIndex++];
					break;
				case COPY_ZERO_TRANSPARENT:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						if (src==0)
							dst = pixels[dstIndex];
						else
							dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case ADD:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&0xffff)+(pixels[dstIndex]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case AVERAGE:
					for (int i=r1.width; --i>=0;) {
						dst = ((srcPixels[srcIndex++]&0xffff)+(pixels[dstIndex]&0xffff))/2;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case DIFFERENCE:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&0xffff)-(srcPixels[srcIndex++]&0xffff);
						if (dst<0) dst = -dst;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case SUBTRACT:
					for (int i=r1.width; --i>=0;) {
						dst = (pixels[dstIndex]&0xffff)-(srcPixels[srcIndex++]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MULTIPLY:
					for (int i=r1.width; --i>=0;) {
						dst = (srcPixels[srcIndex++]&0xffff)*(pixels[dstIndex]&0xffff);
						if (dst<0) dst = 0;
						if (dst>65535) dst = 65535;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case DIVIDE:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						if (src==0)
							dst = 65535;
						else
							dst = (pixels[dstIndex]&0xffff)/src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case AND:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]&pixels[dstIndex]&0xffff;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case OR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]|pixels[dstIndex];
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case XOR:
					for (int i=r1.width; --i>=0;) {
						dst = srcPixels[srcIndex++]^pixels[dstIndex];
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MIN:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						dst = pixels[dstIndex]&0xffff;
						if (src<dst) dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
				case MAX:
					for (int i=r1.width; --i>=0;) {
						src = srcPixels[srcIndex++]&0xffff;
						dst = pixels[dstIndex]&0xffff;
						if (src>dst) dst = src;
						pixels[dstIndex++] = (short)dst;
					}
					break;
			}
		}
	}
}
