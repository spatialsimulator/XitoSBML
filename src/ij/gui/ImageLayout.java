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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Scrollbar;

// TODO: Auto-generated Javadoc
/** This is a custom layout manager that supports resizing of zoomed
images. It's based on FlowLayout, but with vertical and centered flow. */
public class ImageLayout implements LayoutManager {

    /** The hgap. */
    int hgap = ImageWindow.HGAP;
    
    /** The vgap. */
    int vgap = ImageWindow.VGAP;
	
	/** The ic. */
	ImageCanvas ic;

    /**
     *  Creates a new ImageLayout with center alignment.
     *
     * @param ic the ic
     */
    public ImageLayout(ImageCanvas ic) {
    	this.ic = ic;
    }

    /**
     *  Not used by this class.
     *
     * @param name the name
     * @param comp the comp
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     *  Not used by this class.
     *
     * @param comp the comp
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     *  Returns the preferred dimensions for this layout.
     *
     * @param target the target
     * @return the dimension
     */
    public Dimension preferredLayoutSize(Container target) {
		Dimension dim = new Dimension(0,0);
		int nmembers = target.getComponentCount();
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
			Dimension d = m.getPreferredSize();
			dim.width = Math.max(dim.width, d.width);
			if (i>0) dim.height += vgap;
			dim.height += d.height;
		}
		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right + hgap*2;
		dim.height += insets.top + insets.bottom + vgap*2;
		return dim;
    }

    /**
     *  Returns the minimum dimensions for this layout.
     *
     * @param target the target
     * @return the dimension
     */
    public Dimension minimumLayoutSize(Container target) {
		return preferredLayoutSize(target);
    }

    /**
     *  Centers the elements in the specified column, if there is any slack.
     *
     * @param target the target
     * @param x the x
     * @param y the y
     * @param width the width
     * @param height the height
     * @param nmembers the nmembers
     */
    private void moveComponents(Container target, int x, int y, int width, int height, int nmembers) {
    	int x2 = 0;
	    y += height / 2;
		for (int i=0; i<nmembers; i++) {
		    Component m = target.getComponent(i);
		    Dimension d = m.getSize();
		    if (i==0 || d.height>60)
		    	x2 = x + (width - d.width)/2;
			m.setLocation(x2, y);
			y += vgap + d.height;
		}
    }

    /**
     *  Lays out the container and calls ImageCanvas.resizeCanvas()
     * 		to adjust the image canvas size as needed.
     *
     * @param target the target
     */
    public void layoutContainer(Container target) {
		Insets insets = target.getInsets();
		int nmembers = target.getComponentCount();
		Dimension d;
		int extraHeight = 0;
		for (int i=1; i<nmembers; i++) {
			Component m = target.getComponent(i);
			d = m.getPreferredSize();
			extraHeight += d.height+vgap;
		}
		d = target.getSize();
		int preferredImageWidth = d.width - (insets.left + insets.right + hgap*2);
		int preferredImageHeight = d.height - (insets.top + insets.bottom + vgap*2 + extraHeight);
		ic.resizeCanvas(preferredImageWidth, preferredImageHeight);
		int maxwidth = d.width - (insets.left + insets.right + hgap*2);
		int maxheight = d.height - (insets.top + insets.bottom + vgap*2);
		Dimension psize = preferredLayoutSize(target);
		int x = insets.left + hgap + (d.width - psize.width)/2;
		int y = 0;
		int colw = 0;
		
		for (int i=0; i<nmembers; i++) {
			Component m = target.getComponent(i);
			d = m.getPreferredSize();
			if ((m instanceof ScrollbarWithLabel) || (m instanceof Scrollbar)) {
				int scrollbarWidth = target.getComponent(0).getPreferredSize().width;
				Dimension minSize = m.getMinimumSize();
				if (scrollbarWidth<minSize.width) scrollbarWidth = minSize.width;
				m.setSize(scrollbarWidth, d.height);
			} else
				m.setSize(d.width, d.height);
			if (y > 0) y += vgap;
			y += d.height;
			colw = Math.max(colw, d.width);
		}
		moveComponents(target, x, insets.top + vgap, colw, maxheight - y, nmembers);
    }
    
}
