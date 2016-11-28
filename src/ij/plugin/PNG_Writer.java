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
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;


// TODO: Auto-generated Javadoc
/** Saves in PNG format using the ImageIO classes.  RGB images are saved
	as RGB PNGs. All other image types are saved as 8-bit PNGs. With 8-bit images,
	the value of the transparent index can be set in the Edit/Options/Input-Output dialog,
	or by calling Prefs.setTransparentIndex(index), where 0<=index<=255. */
public class PNG_Writer implements PlugIn {
    
    /** The imp. */
    ImagePlus imp;

    /* (non-Javadoc)
     * @see ij.plugin.PlugIn#run(java.lang.String)
     */
    public void run(String path) {
        imp = WindowManager.getCurrentImage();
        if (imp==null)
        	{IJ.noImage(); return;}

        if (path.equals("")) {
            SaveDialog sd = new SaveDialog("Save as PNG...", imp.getTitle(), ".png");
            String name = sd.getFileName();
            if (name==null)
                return;
            String dir = sd.getDirectory();
            path = dir + name;
        }

        try {
            writeImage(imp, path, Prefs.getTransparentIndex());
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg==null || msg.equals(""))
                msg = ""+e;
            IJ.error("PNG Writer", "An error occured writing the file.\n \n" + msg);
        }
        IJ.showStatus("");
    }

	/**
	 * Write image.
	 *
	 * @param imp the imp
	 * @param path the path
	 * @param transparentIndex the transparent index
	 * @throws Exception the exception
	 */
	public void writeImage(ImagePlus imp, String path, int transparentIndex) throws Exception {
		if (imp.getStackSize()==4 && imp.getBitDepth()==8 && "alpha".equalsIgnoreCase(imp.getStack().getSliceLabel(4)))
			writeFourChannelsWithAlpha(imp, path);
		else if (transparentIndex>=0 && transparentIndex<=255 && imp.getBitDepth()==8)
			writeImageWithTransparency(imp, path, transparentIndex);
		else if (imp.getOverlay()!=null && !imp.getHideOverlay())
			ImageIO.write(imp.flatten().getBufferedImage(), "png", new File(path));
		else if (imp.getBitDepth()==16 && !imp.isComposite() && imp.getProcessor().isDefaultLut())
			write16gs(imp, path);
        else
			ImageIO.write(imp.getBufferedImage(), "png", new File(path));
	}
	
	/**
	 * Write four channels with alpha.
	 *
	 * @param imp the imp
	 * @param path the path
	 * @throws Exception the exception
	 */
	private void writeFourChannelsWithAlpha(ImagePlus imp, String path) throws Exception {
		ImageStack stack = imp.getStack();
		int w=imp.getWidth(), h=imp.getHeight();
		ImagePlus imp2 = new ImagePlus("", new ColorProcessor(w,h));
		ColorProcessor cp = (ColorProcessor)imp2.getProcessor();
		for (int channel=1; channel<=4; channel++)
			cp.setChannel(channel, (ByteProcessor)stack.getProcessor(channel));
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = bi.getRaster();
		raster.setDataElements(0, 0, w, h, cp.getPixels());
		ImageIO.write(bi, "png", new File(path));
	}
    
	/**
	 * Write image with transparency.
	 *
	 * @param imp the imp
	 * @param path the path
	 * @param transparentIndex the transparent index
	 * @throws Exception the exception
	 */
	void writeImageWithTransparency(ImagePlus imp, String path, int transparentIndex) throws Exception {
		int width = imp.getWidth();
		int  height = imp.getHeight();
		ImageProcessor ip = imp.getProcessor();
		IndexColorModel cm = (IndexColorModel)ip.getColorModel();
		int size = cm.getMapSize();
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];	
		cm.getReds(reds); 
		cm.getGreens(greens); 
		cm.getBlues(blues);
		cm = new IndexColorModel(8, 256, reds, greens, blues, transparentIndex);
		WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
		DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
		byte[] biPixels = db.getData();
		System.arraycopy(ip.getPixels(), 0, biPixels, 0, biPixels.length);
		BufferedImage bi = new BufferedImage(cm, wr, false, null);
		ImageIO.write(bi, "png", new File(path));
	}

    /**
     * Write 16 gs.
     *
     * @param imp the imp
     * @param path the path
     * @throws Exception the exception
     */
    void write16gs(ImagePlus imp, String path) throws Exception {
		ShortProcessor sp = (ShortProcessor)imp.getProcessor();
		BufferedImage bi = sp.get16BitBufferedImage();
		File f = new File(path);
		ImageIO.write(bi, "png", f);
    }
}
