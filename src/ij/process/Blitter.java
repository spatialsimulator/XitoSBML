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

// TODO: Auto-generated Javadoc
/** ImageJ bit blitting classes must implement this interface. */
public interface Blitter {

	/**  dst=src. */
	public static final int COPY = 0;
	
	/**  dst=255-src (8-bits and RGB). */
	public static final int COPY_INVERTED = 1;
	
	/** Copies with white pixels transparent. */
	public static final int COPY_TRANSPARENT = 2;
	
	/**  dst=dst+src. */
	public static final int ADD = 3;
	
	/**  dst=dst-src. */
	public static final int SUBTRACT = 4;
		
	/**  dst=src*src. */
	public static final int MULTIPLY = 5;
	
	/**  dst=dst/src. */
	public static final int DIVIDE = 6;
	
	/**  dst=(dst+src)/2. */
	public static final int AVERAGE = 7;
	
	/**  dst=abs(dst-src). */
	public static final int DIFFERENCE = 8;
	
	/**  dst=dst AND src. */
	public static final int AND = 9;
	
	/**  dst=dst OR src. */
	public static final int OR = 10;
	
	/**  dst=dst XOR src. */
	public static final int XOR = 11;
	
	/**  dst=min(dst,src). */
	public static final int MIN = 12;
	
	/**  dst=max(dst,src). */
	public static final int MAX = 13;
	
	/** Copies with zero pixels transparent. */
	public static final int COPY_ZERO_TRANSPARENT = 14;


	/**
	 *  Sets the transparent color used in the COPY_TRANSPARENT
	 * 		mode (default is Color.white).
	 *
	 * @param c the new transparent color
	 */
	public void setTransparentColor(Color c);

	/**
	 *  Copies the image in 'src' to (x,y) using the specified mode.
	 *
	 * @param src the src
	 * @param x the x
	 * @param y the y
	 * @param mode the mode
	 */
	public void copyBits(ImageProcessor src, int x, int y, int mode);

}
