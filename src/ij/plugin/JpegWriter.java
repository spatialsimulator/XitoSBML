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
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

// TODO: Auto-generated Javadoc
/** The File/Save As/Jpeg command (FileSaver.saveAsJpeg() method) 
      uses this plugin to save images in JPEG format. */
public class JpegWriter implements PlugIn {
	
	/** The Constant DEFAULT_QUALITY. */
	public static final int DEFAULT_QUALITY = 75;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		imp.startTiming();
		saveAsJpeg(imp,arg,FileSaver.getJpegQuality());
		IJ.showTime(imp, imp.getStartTime(), "JpegWriter: ");
	}

	/**
	 *  Thread-safe method.
	 *
	 * @param imp the imp
	 * @param path the path
	 * @param quality the quality
	 * @return the string
	 */
	public static String save(ImagePlus imp, String path, int quality) {
		imp.startTiming();
		String error = (new JpegWriter()).saveAsJpeg(imp, path, quality);
		IJ.showTime(imp, imp.getStartTime(), "JpegWriter: ");
		return error;
	}

	/**
	 * Save as jpeg.
	 *
	 * @param imp the imp
	 * @param path the path
	 * @param quality the quality
	 * @return the string
	 */
	String saveAsJpeg(ImagePlus imp, String path, int quality) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		int biType = BufferedImage.TYPE_INT_RGB;
		boolean overlay = imp.getOverlay()!=null && !imp.getHideOverlay();
		if (imp.getProcessor().isDefaultLut() && !imp.isComposite() && !overlay)
			biType = BufferedImage.TYPE_BYTE_GRAY;
		BufferedImage bi = new BufferedImage(width, height, biType);
		String error = null;
		try {
			Graphics g = bi.createGraphics();
			Image img = imp.getImage();
			if (overlay)
				img = imp.flatten().getImage();
			g.drawImage(img, 0, 0, null);
			g.dispose();            
			Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = (ImageWriter)iter.next();
			File f = new File(path);
			String originalPath = null;
			boolean replacing = f.exists();
			if (replacing) {
				originalPath = path;
				path += ".temp";
				f = new File(path);
			}
			ImageOutputStream ios = ImageIO.createImageOutputStream(f);
			writer.setOutput(ios);
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(param.MODE_EXPLICIT);
			param.setCompressionQuality(quality/100f);
			if (quality == 100)
				param.setSourceSubsampling(1, 1, 0, 0);
			IIOImage iioImage = new IIOImage(bi, null, null);
			writer.write(null, iioImage, param);
			ios.close();
			writer.dispose();
			if (replacing) {
				File f2 = new File(originalPath);
				boolean ok = f2.delete();
				if (ok) f.renameTo(f2);
			}
		} catch (Exception e) {
			error = ""+e;
			IJ.error("Jpeg Writer", ""+error);
		}
		return error;
	}

	/**
	 * Sets the quality.
	 *
	 * @param jpegQuality the new quality
	 * @deprecated replaced by FileSaver.setJpegQuality()
	 */
	public static void setQuality(int jpegQuality) {
		FileSaver.setJpegQuality(jpegQuality);
	}

	/**
	 * Gets the quality.
	 *
	 * @return the quality
	 * @deprecated replaced by FileSaver.getJpegQuality()
	 */
	public static int getQuality() {
		return FileSaver.getJpegQuality();
	}

}
