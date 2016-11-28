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
package ij.util;
import ij.IJ;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.UIManager;

// TODO: Auto-generated Javadoc
/**
This class contains static methods that use the Java 2 API. They are isolated 
here to prevent errors when ImageJ is running on Java 1.1 JVMs.
*/
public class Java2 {

	/** The look and feel set. */
	private static boolean lookAndFeelSet;

	/**
	 * Sets the antialiased.
	 *
	 * @param g the g
	 * @param antialiased the antialiased
	 */
	public static void setAntialiased(Graphics g, boolean antialiased) {
			Graphics2D g2d = (Graphics2D)g;
			if (antialiased)
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			else
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * Sets the antialiased text.
	 *
	 * @param g the g
	 * @param antialiasedText the antialiased text
	 */
	public static void setAntialiasedText(Graphics g, boolean antialiasedText) {
			Graphics2D g2d = (Graphics2D)g;
			if (antialiasedText)
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			else
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}

	/**
	 * Gets the string width.
	 *
	 * @param s the s
	 * @param fontMetrics the font metrics
	 * @param g the g
	 * @return the string width
	 */
	public static int getStringWidth(String s, FontMetrics fontMetrics, Graphics g) {
			java.awt.geom.Rectangle2D r = fontMetrics.getStringBounds(s, g);
			return (int)r.getWidth();
	}

	/**
	 * Sets the bilinear interpolation.
	 *
	 * @param g the g
	 * @param bilinearInterpolation the bilinear interpolation
	 */
	public static void setBilinearInterpolation(Graphics g, boolean bilinearInterpolation) {
			Graphics2D g2d = (Graphics2D)g;
			if (bilinearInterpolation)
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			else
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	
	/** Sets the Swing look and feel to the system look and feel (Windows only). */
	public static void setSystemLookAndFeel() {
		if (lookAndFeelSet || !IJ.isWindows()) return;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Throwable t) {}
		lookAndFeelSet = true;
		IJ.register(Java2.class);
	}

}

