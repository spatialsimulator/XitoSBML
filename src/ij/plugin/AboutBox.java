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
package ij.plugin;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.URL;

// TODO: Auto-generated Javadoc
/** This plugin implements the Help/About ImageJ command by opening
	the about.jpg in ij.jar, scaling it 400% and adding some text. */
	public class AboutBox implements PlugIn {
		
		/** The Constant LARGE_FONT. */
		static final int SMALL_FONT=14, LARGE_FONT=30;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		System.gc();
		int lines = 7;
		String[] text = new String[lines];
		text[0] = "ImageJ "+ImageJ.VERSION+ImageJ.BUILD;
		text[1] = "Wayne Rasband";
		text[2] = "National Institutes of Health, USA";
		text[3] = IJ.URL;
		text[4] = "Java "+System.getProperty("java.version")+(IJ.is64Bit()?" (64-bit)":" (32-bit)");
		text[5] = IJ.freeMemory();
		text[6] = "ImageJ is in the public domain";
		ImageProcessor ip = null;
		ImageJ ij = IJ.getInstance();
		URL url = ij .getClass() .getResource("/about.jpg");
		if (url!=null) {
			Image img = null;
			try {img = ij.createImage((ImageProducer)url.getContent());}
			catch(Exception e) {}
			if (img!=null) {
				ImagePlus imp = new ImagePlus("", img);
				ip = imp.getProcessor();
			}
		}
		if (ip==null) 
			ip =  new ColorProcessor(55,45);
		ip = ip.resize(ip.getWidth()*4, ip.getHeight()*4);
		ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
		ip.setAntialiasedText(true);
		int[] widths = new int[lines];
		widths[0] = ip.getStringWidth(text[0]);
		ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
		for (int i=1; i<lines-1; i++)
			widths[i] = ip.getStringWidth(text[i]);
		int max = 0;
		for (int i=0; i<lines-1; i++) 
			if (widths[i]>max)
				max = widths[i];
		ip.setColor(new Color(255,255, 140));
		ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
		int y  = 45;
		ip.drawString(text[0], x(text[0],ip,max), y);
		ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
		y += 30;
		ip.drawString(text[1], x(text[1],ip,max), y);
		y += 18;
		ip.drawString(text[2], x(text[2],ip,max), y);
		y += 18;
		ip.drawString(text[3], x(text[3],ip,max), y);
		y += 18;
		ip.drawString(text[4], x(text[4],ip,max), y);
		if (IJ.maxMemory()>0L) {
			y += 18;
			ip.drawString(text[5], x(text[5],ip,max), y);
		}
		ip.drawString(text[6], ip.getWidth()-ip.getStringWidth(text[6])-10, ip.getHeight()-3);
		ImageWindow.centerNextImage();
		ImagePlus imp = new ImagePlus("About ImageJ", ip);
		String info = text[0] +"\n" + text[4] +"\n" + text[5];
		imp.setProperty("Info", info);
		imp.show();
	}

	/**
	 * X.
	 *
	 * @param text the text
	 * @param ip the ip
	 * @param max the max
	 * @return the int
	 */
	int x(String text, ImageProcessor ip, int max) {
		return ip.getWidth() - max + (max - ip.getStringWidth(text))/2 - 10;
	}

}
