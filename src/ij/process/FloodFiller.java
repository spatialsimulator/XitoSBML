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
import java.awt.Rectangle;


// TODO: Auto-generated Javadoc
/**	This class, which does flood filling, is used by the floodFill() macro function and
	by the particle analyzer
	The Wikipedia at "http://en.wikipedia.org/wiki/Flood_fill" has a good 
	description of the algorithm used here as well as examples in C and Java. 
*/
public class FloodFiller {
	
	/** The max stack size. */
	int maxStackSize = 500; // will be increased as needed
	
	/** The xstack. */
	int[] xstack = new int[maxStackSize];
	
	/** The ystack. */
	int[] ystack = new int[maxStackSize];
	
	/** The stack size. */
	int stackSize;
	
	/** The ip. */
	ImageProcessor ip;
	
	/** The max. */
	int max;
	
	/** The is float. */
	boolean isFloat;
  
	/**
	 * Instantiates a new flood filler.
	 *
	 * @param ip the ip
	 */
	public FloodFiller(ImageProcessor ip) {
		this.ip = ip;
		isFloat = ip instanceof FloatProcessor;
	}

	/**
	 *  Does a 4-connected flood fill using the current fill/draw
	 * 		value, which is defined by ImageProcessor.setValue().
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean fill(int x, int y) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		int color = ip.getPixel(x, y);
		fillLine(ip, x, x, y);
		int newColor = ip.getPixel(x, y);
		ip.putPixel(x, y, color);
		if (color==newColor) return false;
		stackSize = 0;
		push(x, y);
		while(true) {   
			x = popx(); 
			if (x ==-1) return true;
			y = popy();
			if (ip.getPixel(x,y)!=color) continue;
			int x1 = x; int x2 = x;
			while (ip.getPixel(x1,y)==color && x1>=0) x1--; // find start of scan-line
			x1++;
			while (ip.getPixel(x2,y)==color && x2<width) x2++;  // find end of scan-line                 
			x2--;
			fillLine(ip, x1,x2,y); // fill scan-line
			boolean inScanLine = false;
			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
				if (!inScanLine && y>0 && ip.getPixel(i,y-1)==color)
					{push(i, y-1); inScanLine = true;}
				else if (inScanLine && y>0 && ip.getPixel(i,y-1)!=color)
					inScanLine = false;
			}
			inScanLine = false;
			for (int i=x1; i<=x2; i++) { // find scan-lines below this one
				if (!inScanLine && y<height-1 && ip.getPixel(i,y+1)==color)
					{push(i, y+1); inScanLine = true;}
				else if (inScanLine && y<height-1 && ip.getPixel(i,y+1)!=color)
					inScanLine = false;
			}
		}        
	}
	
	/**
	 *  Does a 8-connected flood fill using the current fill/draw
	 * 		value, which is defined by ImageProcessor.setValue().
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean fill8(int x, int y) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		int color = ip.getPixel(x, y);
		int wm1=width-1;
		int hm1=height-1; 
		fillLine(ip, x, x, y);
		int newColor = ip.getPixel(x, y);
		ip.putPixel(x, y, color);
		if (color==newColor) return false;
		stackSize = 0;
		push(x, y);
		while(true) {   
			x = popx(); 
			if (x==-1) return true;
			y = popy();
			int x1 = x; int x2 = x;
			if(ip.getPixel(x1,y)==color){ 
				while (ip.getPixel(x1,y)==color && x1>=0) x1--; // find start of scan-line
				x1++;
				while (ip.getPixel(x2,y)==color && x2<width) x2++;  // find end of scan-line
				x2--;
				fillLine(ip, x1,x2,y); // fill scan-line
			} 
			if(y>0){
				if (x1>0){
					if (ip.getPixel(x1-1,y-1)==color){
						push(x1-1,y-1);
					}
				}
				if (x2<wm1){
					if (ip.getPixel(x2+1,y-1)==color){
						push(x2+1,y-1);
					}
				}
			}
			if(y<hm1){
				if (x1>0){
					if (ip.getPixel(x1-1,y+1)==color){
						push(x1-1,y+1);
					}
				}
				if (x2<wm1){
					if (ip.getPixel(x2+1,y+1)==color){
						push(x2+1,y+1);
					}
				}
			}
			boolean inScanLine = false;
			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
				if (!inScanLine && y>0 && ip.getPixel(i,y-1)==color)
					{push(i, y-1); inScanLine = true;}
				else if (inScanLine && y>0 && ip.getPixel(i,y-1)!=color)
					inScanLine = false;
			}
			inScanLine = false;
			for (int i=x1; i<=x2; i++) {// find scan-lines below this one
				if (!inScanLine && y<hm1 && ip.getPixel(i,y+1)==color)
					{push(i, y+1); inScanLine = true;}
				else if (inScanLine && y<hm1 && ip.getPixel(i,y+1)!=color)
					inScanLine = false;
			}
		}
	}
	
	/** The count. */
	int count=0;
	
	/**
	 *  This method is used by the particle analyzer to remove interior holes from particle masks.
	 *
	 * @param x the x
	 * @param y the y
	 * @param level1 the level 1
	 * @param level2 the level 2
	 * @param mask the mask
	 * @param bounds the bounds
	 */
	public void particleAnalyzerFill(int x, int y, double level1, double level2, ImageProcessor mask, Rectangle bounds) {
		//if (count>100) return;
		int width = ip.getWidth();
		int height = ip.getHeight();
		//IJ.log("ff: "+x+" "+y+" "+level1+" "+level2+" "+ip+" "+mask+" "+bounds);
		if (x==0&&y==0&&level1==0.0&&level2==255.0&&ip.getBitDepth()==8) {
			ip.fill();
			return;
		}
		mask.setColor(0);
		mask.fill();
		mask.setColor(255);
		stackSize = 0;
		push(x, y);
		while(true) {   
			x = popx(); 
			if (x ==-1) return;
			y = popy();
			if (!inParticle(x,y,level1,level2)) continue;
			int x1 = x; int x2 = x;
			while (inParticle(x1,y,level1,level2) && x1>=0) x1--; // find start of scan-line
			x1++;
			while (inParticle(x2,y,level1,level2) && x2<width) x2++;  // find end of scan-line                 
			x2--;
			fillLine(mask, x1-bounds.x, x2-bounds.x, y-bounds.y); // fill scan-line i mask
			fillLine(ip,x1,x2,y); // fill scan-line in image
			boolean inScanLine = false;
			if (x1>0) x1--; if (x2<width-1) x2++;
			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
				if (!inScanLine && y>0 && inParticle(i,y-1,level1,level2))
					{push(i, y-1); inScanLine = true;}
				else if (inScanLine && y>0 && !inParticle(i,y-1,level1,level2))
					inScanLine = false;
			}
			inScanLine = false;
			for (int i=x1; i<=x2; i++) { // find scan-lines below this one
				if (!inScanLine && y<height-1 && inParticle(i,y+1,level1,level2))
					{push(i, y+1); inScanLine = true;}
				else if (inScanLine && y<height-1 && !inParticle(i,y+1,level1,level2))
					inScanLine = false;
			}
		}        
	}
	
	/**
	 * In particle.
	 *
	 * @param x the x
	 * @param y the y
	 * @param level1 the level 1
	 * @param level2 the level 2
	 * @return true, if successful
	 */
	final boolean inParticle(int x, int y, double level1, double level2) {
		if (isFloat)
			return ip.getPixelValue(x,y)>=level1 &&  ip.getPixelValue(x,y)<=level2;
		else {
            int v = ip.getPixel(x,y);
			return v>=level1 && v<=level2;
        }
	}
	
	/**
	 * Push.
	 *
	 * @param x the x
	 * @param y the y
	 */
	final void push(int x, int y) {
		stackSize++;
		if (stackSize==maxStackSize) {
			int[] newXStack = new int[maxStackSize*2];
			int[] newYStack = new int[maxStackSize*2];
			System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
			System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
			xstack = newXStack;
			ystack = newYStack;
			maxStackSize *= 2;
		}
		xstack[stackSize-1] = x;
		ystack[stackSize-1] = y;
	}
	
	/**
	 * Popx.
	 *
	 * @return the int
	 */
	final int popx() {
		if (stackSize==0)
			return -1;
		else
            return xstack[stackSize-1];
	}

	/**
	 * Popy.
	 *
	 * @return the int
	 */
	final int popy() {
        int value = ystack[stackSize-1];
        stackSize--;
        return value;
	}

	/**
	 * Fill line.
	 *
	 * @param ip the ip
	 * @param x1 the x 1
	 * @param x2 the x 2
	 * @param y the y
	 */
	final void fillLine(ImageProcessor ip, int x1, int x2, int y) {
		if (x1>x2) {int t = x1; x1=x2; x2=t;}
		for (int x=x1; x<=x2; x++)
            ip.drawPixel(x, y);
	}

}
