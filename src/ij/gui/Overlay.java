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
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** An Overlay is a list of Rois that can be drawn non-destructively on an Image. */
public class Overlay {
	
	/** The list. */
	private Vector list;
    
    /** The label. */
    private boolean label;
    
    /** The draw names. */
    private boolean drawNames;
    
    /** The draw backgrounds. */
    private boolean drawBackgrounds;
    
    /** The label color. */
    private Color labelColor;
    
    /** The label font. */
    private Font labelFont;
    
    /** The is calibration bar. */
    private boolean isCalibrationBar;
    
    /** Constructs an empty Overlay. */
    public Overlay() {
    	list = new Vector();
    }
    
    /**
     *  Constructs an Overlay and adds the specified Roi.
     *
     * @param roi the roi
     */
    public Overlay(Roi roi) {
    	list = new Vector();
    	list.add(roi);
    }

    /**
     *  Adds an Roi to this Overlay.
     *
     * @param roi the roi
     */
    public void add(Roi roi) {
    	list.add(roi);
    }
        
    /**
     *  Adds an Roi to this Overlay.
     *
     * @param roi the roi
     */
    public void addElement(Roi roi) {
    	list.add(roi);
    }

    /**
     *  Removes the Roi with the specified index from this Overlay.
     *
     * @param index the index
     */
    public void remove(int index) {
    	list.remove(index);
    }
    
    /**
     *  Removes the specified Roi from this Overlay.
     *
     * @param roi the roi
     */
    public void remove(Roi roi) {
    	list.remove(roi);
    }

   /** Removes all the Rois in this Overlay. */
    public void clear() {
    	list.clear();
    }

    /**
     *  Returns the Roi with the specified index or null if the index is invalid.
     *
     * @param index the index
     * @return the roi
     */
    public Roi get(int index) {
    	try {
    		return (Roi)list.get(index);
    	} catch(Exception e) {
    		return null;
    	}
    }
    
    /**
     *  Returns the index of the Roi with the specified name, or -1 if not found.
     *
     * @param name the name
     * @return the index
     */
    public int getIndex(String name) {
    	if (name==null) return -1;
    	Roi[] rois = toArray();
		for (int i=rois.length-1; i>=0; i--) {
			if (name.equals(rois[i].getName()))
				return i;
		}
		return -1;
    }
    
    /**
     *  Returns 'true' if this Overlay contains the specified Roi.
     *
     * @param roi the roi
     * @return true, if successful
     */
    public boolean contains(Roi roi) {
    	return list.contains(roi);
    }

    /**
     *  Returns the number of Rois in this Overlay.
     *
     * @return the int
     */
    public int size() {
    	return list.size();
    }
    
    /**
     *  Returns on array containing the Rois in this Overlay.
     *
     * @return the roi[]
     */
    public Roi[] toArray() {
    	Roi[] array = new Roi[list.size()];
    	return (Roi[])list.toArray(array);
    }
    
    /**
     *  Sets the stroke color of all the Rois in this overlay.
     *
     * @param color the new stroke color
     */
    public void setStrokeColor(Color color) {
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++)
			rois[i].setStrokeColor(color);
	}

    /**
     *  Sets the fill color of all the Rois in this overlay.
     *
     * @param color the new fill color
     */
    public void setFillColor(Color color) {
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++)
			rois[i].setFillColor(color);
	}

	/**
	 *  Moves all the ROIs in this overlay.
	 *
	 * @param dx the dx
	 * @param dy the dy
	 */
	public void translate(int dx, int dy) {
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++) {
			Roi roi = rois[i];
			if (roi.subPixelResolution()) {
				Rectangle2D r = roi.getFloatBounds();
				roi.setLocation(r.getX()+dx, r.getY()+dy);
			} else {
				Rectangle r = roi.getBounds();
				roi.setLocation(r.x+dx, r.y+dy);
			}
		}
	}

	/**
	 *  Moves all the Rois in this overlay.
	 * Marcel Boeglin, October 2013
	 *
	 * @param dx the dx
	 * @param dy the dy
	 */
	public void translate(double dx, double dy) {
		Roi[] rois = toArray();
		boolean intArgs = (int)dx==dx && (int)dy==dy;
		for (int i=0; i<rois.length; i++) {
			Roi roi = rois[i];
			if (roi.subPixelResolution() || !intArgs) {
				Rectangle2D r = roi.getFloatBounds();
				roi.setLocation(r.getX()+dx, r.getY()+dy);
			} else {
				Rectangle r = roi.getBounds();
				roi.setLocation(r.x+(int)dx, r.y+(int)dy);
			}
		}
	}

	/**
	 * Crop.
	 *
	 * @param bounds the bounds
	 * @return the overlay
	 */
	/*
	* Duplicate the elements of this overlay which  
	* intersect with the rectangle 'bounds'.
	* Author: Wilhelm Burger
	* Author: Marcel Boeglin
	*/
	public Overlay crop(Rectangle bounds) {
		if (bounds==null)
			return duplicate();
		Overlay overlay2 = create();
		Roi[] allRois = toArray();
		for (Roi roi: allRois) {
			Rectangle roiBounds = roi.getBounds();
			if (roiBounds.width==0) roiBounds.width=1;
			if (roiBounds.height==0) roiBounds.height=1;
			if (bounds.intersects(roiBounds))
				overlay2.add((Roi)roi.clone());
		}
		int dx = bounds.x>0?bounds.x:0;
		int dy = bounds.y>0?bounds.y:0;
		if (dx>0 || dy>0)
			overlay2.translate(-dx, -dy);
		return overlay2;
	}

	/**
	 *  Removes ROIs having positions outside of the  
	 * interval defined by firstSlice and lastSlice.
	 * Marcel Boeglin, September 2013
	 *
	 * @param firstSlice the first slice
	 * @param lastSlice the last slice
	 */
	public void crop(int firstSlice, int lastSlice) {
		for (int i=size()-1; i>=0; i--) {
			Roi roi = get(i);
			int position = roi.getPosition();
			if (position>0) {
				if (position<firstSlice || position>lastSlice)
					remove(i);
				else
					roi.setPosition(position-firstSlice+1);
			}
		}
	}

	/**
	 *  Removes ROIs having a C, Z or T coordinate outside the volume
	 * defined by firstC, lastC, firstZ, lastZ, firstT and lastT.
	 * Marcel Boeglin, September 2013
	 *
	 * @param firstC the first C
	 * @param lastC the last C
	 * @param firstZ the first Z
	 * @param lastZ the last Z
	 * @param firstT the first T
	 * @param lastT the last T
	 */
	public void crop(int firstC, int lastC, int firstZ, int lastZ, int firstT, int lastT) {
		int nc = lastC-firstC+1, nz = lastZ-firstZ+1, nt = lastT-firstT+1;
		boolean toCStack = nz==1 && nt==1;
		boolean toZStack = nt==1 && nc==1;
		boolean toTStack = nc==1 && nz==1;
		Roi roi;
		int c, z, t, c2, z2, t2;
		for (int i=size()-1; i>=0; i--) {
			roi = get(i);
			c = roi.getCPosition();
			z = roi.getZPosition();
			t = roi.getTPosition();
			c2 = c-firstC+1;
			z2 = z-firstZ+1;
			t2 = t-firstT+1;
			if (toCStack)
				roi.setPosition(c2);
			else if (toZStack)
				roi.setPosition(z2);
			else if (toTStack)
				roi.setPosition(t2);
			else
				roi.setPosition(c2, z2, t2);
			if ((c2<1||c2>nc) && c>0 || (z2<1||z2>nz) && z>0 || (t2<1||t2>nt) && t>0)
				remove(i);
		}
	}

    /**
     *  Returns the bounds of this overlay.
     *
     * @return the overlay
     */
    /*
    public Rectangle getBounds() {
    	if (size()==0)
    		return new Rectangle(0,0,0,0);
    	int xmin = Integer.MAX_VALUE;
    	int xmax = -Integer.MAX_VALUE;
    	int ymin = Integer.MAX_VALUE;
    	int ymax = -Integer.MAX_VALUE;
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++) {
			Rectangle r = rois[i].getBounds();
			if (r.x<xmin) xmin = r.x;
			if (r.y<ymin) ymin = r.y;
			if (r.x+r.width>xmax) xmax = r.x+r.width;
			if (r.y+r.height>ymax) ymax = r.y+r.height;
		}
		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}
	*/

	/** Returns a new Overlay that has the same properties as this one. */
	public Overlay create() {
		Overlay overlay2 = new Overlay();
		overlay2.drawLabels(label);
		overlay2.drawNames(drawNames);
		overlay2.drawBackgrounds(drawBackgrounds);
		overlay2.setLabelColor(labelColor);
		overlay2.setLabelFont(labelFont);
		return overlay2;
	}
	
	/**
	 *  Returns a clone of this Overlay.
	 *
	 * @return the overlay
	 */
	public Overlay duplicate() {
		Roi[] rois = toArray();
		Overlay overlay2 = create();
		for (int i=0; i<rois.length; i++)
			overlay2.add((Roi)rois[i].clone());
		return overlay2;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
    	return list.toString();
    }
    
    /**
     * Draw labels.
     *
     * @param b the b
     */
    public void drawLabels(boolean b) {
    	label = b;
    }
    
    /**
     * Gets the draw labels.
     *
     * @return the draw labels
     */
    public boolean getDrawLabels() {
    	return label;
    }
    
    /**
     * Draw names.
     *
     * @param b the b
     */
    public void drawNames(boolean b) {
    	drawNames = b;
		Roi[] rois = toArray();
		for (int i=0; i<rois.length; i++)
			rois[i].setIgnoreClipRect(drawNames);
    }
    
    /**
     * Gets the draw names.
     *
     * @return the draw names
     */
    public boolean getDrawNames() {
    	return drawNames;
    }

    /**
     * Draw backgrounds.
     *
     * @param b the b
     */
    public void drawBackgrounds(boolean b) {
    	drawBackgrounds = b;
    }
    
    /**
     * Gets the draw backgrounds.
     *
     * @return the draw backgrounds
     */
    public boolean getDrawBackgrounds() {
    	return drawBackgrounds;
    }

    /**
     * Sets the label color.
     *
     * @param c the new label color
     */
    public void setLabelColor(Color c) {
    	labelColor = c;
    }
    
    /**
     * Gets the label color.
     *
     * @return the label color
     */
    public Color getLabelColor() {
    	return labelColor;
    }

    /**
     * Sets the label font.
     *
     * @param font the new label font
     */
    public void setLabelFont(Font font) {
    	labelFont = font;
    }
    
    /**
     * Gets the label font.
     *
     * @return the label font
     */
    public Font getLabelFont() {
    	//if (labelFont==null && labelFontSize!=0)
    	//	labelFont = new Font("SansSerif", Font.PLAIN, labelFontSize);
    	return labelFont;
    }

    /**
     * Sets the checks if is calibration bar.
     *
     * @param b the new checks if is calibration bar
     */
    public void setIsCalibrationBar(boolean b) {
    	this.isCalibrationBar = b;
    }
    
    /**
     * Checks if is calibration bar.
     *
     * @return true, if is calibration bar
     */
    public boolean isCalibrationBar() {
    	return isCalibrationBar;
    }

    /**
     * Sets the vector.
     *
     * @param v the new vector
     */
    void setVector(Vector v) {list = v;}
        
    /**
     * Gets the vector.
     *
     * @return the vector
     */
    Vector getVector() {return list;}
    
}
