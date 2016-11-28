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
import ij.IJ;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Window;

// TODO: Auto-generated Javadoc
/** This class consists of static GUI utility methods. */
public class GUI {
	
	/** The light gray. */
	private static Color lightGray = new Color(240,240,240);
	
	/** The is windows 8. */
	private static boolean isWindows8;
	
	/** The max bounds. */
	private static Rectangle maxBounds;
	
	/** The zero based max bounds. */
	private static Rectangle zeroBasedMaxBounds;
	
	/** The union of bounds. */
	private static Rectangle unionOfBounds;

	static {
		if (IJ.isWindows()) {
			String osname = System.getProperty("os.name");
			isWindows8 = osname.contains("unknown") || osname.contains("8");
		}
	}

	/**
	 *  Positions the specified window in the center of the screen.
	 *
	 * @param win the win
	 */
	public static void center(Window win) {
		if (win==null)
			return;
		Rectangle bounds = getMaxWindowBounds();
		Dimension window= win.getSize();
		if (window.width==0)
			return;
		int left = bounds.x + (bounds.width-window.width)/2;
		if (left<bounds.x) left=bounds.x;
		int top = bounds.y + (bounds.height-window.height)/4;
		if (top<bounds.y) top=bounds.y;
		win.setLocation(left, top);
	}
	
	/**
	 * Gets the max window bounds.
	 *
	 * @return the max window bounds
	 */
	public static Rectangle getMaxWindowBounds() {
		if (GraphicsEnvironment.isHeadless())
			return new Rectangle(0,0,0,0);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = ge.getMaximumWindowBounds();
		if (IJ.isLinux() && unionOfBounds==null)
			unionOfBounds = getUnionOfBounds(ge);
		zeroBasedMaxBounds = null;
		if (bounds.x>300 || bounds.equals(unionOfBounds))
			bounds = getZeroBasedMonitor(ge, bounds);
		if (bounds.x<0 || bounds.x>300 || bounds.width<300) {
			Dimension screen = IJ.getScreenSize();
			bounds = new Rectangle(0, 0, screen.width, screen.height);
		}
		if (IJ.debugMode) IJ.log("GUI.getMaxWindowBounds: "+bounds);
		maxBounds = bounds;
		return bounds;
	}

	/**
	 * Gets the zero based max bounds.
	 *
	 * @return the zero based max bounds
	 */
	public static Rectangle getZeroBasedMaxBounds() {
		if (maxBounds==null)
			getMaxWindowBounds();
		if (IJ.debugMode) IJ.log("GUI.getZeroBasedMaxBounds: "+zeroBasedMaxBounds);
		return zeroBasedMaxBounds;
	}
	
	/**
	 * Gets the union of bounds.
	 *
	 * @return the union of bounds
	 */
	public static Rectangle getUnionOfBounds() {
		if (unionOfBounds==null)
			getMaxWindowBounds();
		return unionOfBounds;
	}

	/**
	 * Gets the union of bounds.
	 *
	 * @param ge the ge
	 * @return the union of bounds
	 */
	private static Rectangle getUnionOfBounds(GraphicsEnvironment ge) {
		Rectangle virtualBounds = new Rectangle();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle bounds2 = null;
		int nMonitors = 0;
		for (int j = 0; j < gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i=0; i < gc.length; i++) {
				Rectangle bounds = gc[i].getBounds();
				if (bounds!=null && !bounds.equals(bounds2)) {
					virtualBounds = virtualBounds.union(bounds);
					nMonitors++;
				}
				bounds2 = bounds;
			}
		}
		if (nMonitors<2)
			virtualBounds = new Rectangle(0,0,1,1);
		if (IJ.debugMode) IJ.log("GUI.getUnionOfBounds: "+nMonitors+" "+virtualBounds);
		return virtualBounds;
	} 

	/**
	 * Gets the zero based monitor.
	 *
	 * @param ge the ge
	 * @param bounds the bounds
	 * @return the zero based monitor
	 */
	private static Rectangle getZeroBasedMonitor(GraphicsEnvironment ge, Rectangle bounds) {
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle bounds2 = null;
		for (int j=0; j<gs.length; j++) {
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i=0; i<gc.length; i++) {
				bounds2 = gc[i].getBounds();
				if (bounds2!=null && bounds.x==0)
					break;
			}
		}
		if (IJ.debugMode) IJ.log("GUI.getZeroBasedMonitor: "+bounds2);
		if (bounds2!=null) {
			bounds = bounds2;
			zeroBasedMaxBounds = bounds2;
		}
		return bounds;
	}

    /** The frame. */
    static private Frame frame;
    
    /**
     *  Creates a white AWT Image image of the specified size.
     *
     * @param width the width
     * @param height the height
     * @return the image
     */
    public static Image createBlankImage(int width, int height) {
        if (width==0 || height==0)
            throw new IllegalArgumentException("");
		if (frame==null) {
			frame = new Frame();
			frame.pack();
			frame.setBackground(Color.white);
		}
        Image img = frame.createImage(width, height);
        return img;
    }
    
    /**
     *  Lightens overly dark scrollbar background on Windows 8.
     *
     * @param sb the sb
     */
    public static void fix(Scrollbar sb) {
    	if (isWindows8) {
			sb.setBackground(lightGray);
		}
    }
    
}
