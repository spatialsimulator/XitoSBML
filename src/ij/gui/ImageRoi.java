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
import ij.io.FileSaver;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;

// TODO: Auto-generated Javadoc
/** An ImageRoi is an Roi that overlays an image. 
* @see ij.ImagePlus#setOverlay(ij.gui.Overlay)
*/
public class ImageRoi extends Roi {
	
	/** The img. */
	private Image img;
	
	/** The composite. */
	private Composite composite;
	
	/** The opacity. */
	private double opacity = 1.0;
	
	/** The angle. */
	private double angle = 0.0;
	
	/** The zero transparent. */
	private boolean zeroTransparent;
	
	/** The ip. */
	private ImageProcessor ip;

	/**
	 *  Creates a new ImageRoi from a BufferedImage.
	 *
	 * @param x the x
	 * @param y the y
	 * @param bi the bi
	 */
	public ImageRoi(int x, int y, BufferedImage bi) {
		super(x, y, bi.getWidth(), bi.getHeight());
		img = bi;
		setStrokeColor(Color.black);
	}

	/**
	 *  Creates a new ImageRoi from a ImageProcessor.
	 *
	 * @param x the x
	 * @param y the y
	 * @param ip the ip
	 */
	public ImageRoi(int x, int y, ImageProcessor ip) {
		super(x, y, ip.getWidth(), ip.getHeight());
		img = ip.createImage();
		this.ip = ip;
		setStrokeColor(Color.black);
	}
		
	/* (non-Javadoc)
	 * @see ij.gui.Roi#draw(java.awt.Graphics)
	 */
	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;						
		double mag = getMagnification();
		int sx2 = screenX(x+width);
		int sy2 = screenY(y+height);
		Composite saveComposite = null;
		if (composite!=null) {
			saveComposite = g2d.getComposite();
			g2d.setComposite(composite);
		}
		Image img2 = img;
		if (angle!=0.0) {
			ImageProcessor ip = new ColorProcessor(img);
			ip.setInterpolate(true);
			ip.setBackgroundValue(0.0);
			ip.rotate(angle);
			if (zeroTransparent)
				ip = makeZeroTransparent(ip, true);
			img2 = ip.createImage();
		}
		g.drawImage(img2, screenX(x), screenY(y), sx2, sy2, 0, 0, img.getWidth(null), img.getHeight(null), null);
		if (composite!=null) g2d.setComposite(saveComposite);
		if (isActiveOverlayRoi() && !overlay)
			super.draw(g);
 	}
 	 	
	/**
	 *  Sets the composite mode.
	 *
	 * @param composite the new composite
	 */
	public void setComposite(Composite composite) {
		this.composite = composite;
	}
	
	/**
	 *  Sets the composite mode using the specified opacity (alpha), in the 
	 * 	     range 0.0-1.0, where 0.0 is fully transparent and 1.0 is fully opaque.
	 *
	 * @param opacity the new opacity
	 */
	public void setOpacity(double opacity) {
		if (opacity<0.0) opacity = 0.0;
		if (opacity>1.0) opacity = 1.0;
		this.opacity = opacity;
		if (opacity!=1.0)
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)opacity);
		else
			composite = null;
	}
	
	/**
	 *  Returns a serialized version of the image.
	 *
	 * @return the serialized image
	 */
	public byte[] getSerializedImage() {
		ImagePlus imp = new ImagePlus("",img);
		return new FileSaver(imp).serialize();
	}

	/**
	 *  Returns the current opacity.
	 *
	 * @return the opacity
	 */
	public double getOpacity() {
		return opacity;
	}

	/**
	 * Rotate.
	 *
	 * @param angle the angle
	 */
	public void rotate(double angle) {
		this.angle += angle;
	}

	/**
	 * Sets the angle.
	 *
	 * @param angle the new angle
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	/**
	 * Sets the zero transparent.
	 *
	 * @param zeroTransparent the new zero transparent
	 */
	public void setZeroTransparent(boolean zeroTransparent) {
		if (this.zeroTransparent!=zeroTransparent) {
			ip = makeZeroTransparent(new ColorProcessor(img), zeroTransparent);
			img = ip.createImage();
		}
		this.zeroTransparent = zeroTransparent;
	}
	
	/**
	 * Gets the zero transparent.
	 *
	 * @return the zero transparent
	 */
	public boolean getZeroTransparent() {
		return zeroTransparent;
	}

	/**
	 * Make zero transparent.
	 *
	 * @param ip the ip
	 * @param transparent the transparent
	 * @return the image processor
	 */
	private ImageProcessor makeZeroTransparent(ImageProcessor ip, boolean transparent) {
		if (transparent) {
			ip.setColorModel(new DirectColorModel(32,0x00ff0000,0x0000ff00,0x000000ff,0xff000000));
			for (int x=0; x<width; x++) {
				for (int y=0; y<height; y++) {
					double v = ip.getPixelValue(x, y);
					if (v>1)
						ip.set(x, y, ip.get(x,y)|0xff000000); // set alpha bits
					else
						ip.set(x, y, ip.get(x,y)&0xffffff); // clear alpha bits
				}
			}
		}
		return ip;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#clone()
	 */
	public synchronized Object clone() {
		ImagePlus imp = new ImagePlus("", img);
		ImageRoi roi2 = new ImageRoi(x, y, imp.getProcessor());
		roi2.setOpacity(getOpacity());
		roi2.setZeroTransparent(zeroTransparent);
		return roi2;
	}
	
	/**
	 * Gets the processor.
	 *
	 * @return the processor
	 */
	public ImageProcessor getProcessor() {
		if (ip!=null)
			return ip;
		else {
			ip = new ColorProcessor(img);
			return ip;
		}
	}

	/**
	 * Sets the processor.
	 *
	 * @param ip the new processor
	 */
	public void setProcessor(ImageProcessor ip) {
		img = ip.createImage();
		this.ip = ip;
	}

}