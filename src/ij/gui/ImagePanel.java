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
import ij.ImagePlus;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Panel;

// TODO: Auto-generated Javadoc
/** This class is used by GenericDialog to add images to dialogs. */
public class ImagePanel extends Panel {
	
	/** The img. */
	private ImagePlus img;
	
	/** The height. */
	private int width, height;
	 
	/**
	 * Instantiates a new image panel.
	 *
	 * @param img the img
	 */
	ImagePanel(ImagePlus img) {
		this.img = img;
		width = img.getWidth();
		height = img.getHeight();
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		g.drawImage(img.getProcessor().createImage(), 0, 0, null);
	}

}
