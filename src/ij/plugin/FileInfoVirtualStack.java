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
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.VirtualStack;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import ij.io.TiffDecoder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.io.IOException;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/** This plugin opens a multi-page TIFF file as a virtual stack. It
	implements the File/Import/TIFF Virtual Stack command. */
public class FileInfoVirtualStack extends VirtualStack implements PlugIn {
	
	/** The info. */
	FileInfo[] info;
	
	/** The n images. */
	int nImages;
	
	/**
	 * Instantiates a new file info virtual stack.
	 */
	/* Default constructor. */
	public FileInfoVirtualStack() {}

	/**
	 * Instantiates a new file info virtual stack.
	 *
	 * @param fi the fi
	 */
	/* Constructs a FileInfoVirtualStack from a FileInfo object. */
	public FileInfoVirtualStack(FileInfo fi) {
		info = new FileInfo[1];
		info[0] = fi;
		ImagePlus imp = open();
		if (imp!=null)
			imp.show();
	}

	/**
	 * Instantiates a new file info virtual stack.
	 *
	 * @param fi the fi
	 * @param show the show
	 */
	/* Constructs a FileInfoVirtualStack from a FileInfo 
		object and displays it if 'show' is true. */
	public FileInfoVirtualStack(FileInfo fi, boolean show) {
		info = new FileInfo[1];
		info[0] = fi;
		ImagePlus imp = open();
		if (imp!=null && show)
			imp.show();
	}

	/**
	 *  Opens the specified tiff file as a virtual stack.
	 *
	 * @param path the path
	 * @return the image plus
	 */
	public static ImagePlus openVirtual(String path) {
		OpenDialog  od = new OpenDialog("Open TIFF", path);
		String name = od.getFileName();
		String  dir = od.getDirectory();
		if (name==null)
			return null;
		FileInfoVirtualStack stack = new FileInfoVirtualStack();
		stack.init(dir, name);
		return stack.open();
	}

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		OpenDialog  od = new OpenDialog("Open TIFF", arg);
		String name = od.getFileName();
		String  dir = od.getDirectory();
		if (name==null)
			return;
		init(dir, name);
		ImagePlus imp = open();
		if (imp!=null)
			imp.show();
	}
	
	/**
	 * Inits the.
	 *
	 * @param dir the dir
	 * @param name the name
	 */
	private void init(String dir, String name) {
		if (name.endsWith(".zip")) {
			IJ.error("Virtual Stack", "ZIP compressed stacks not supported");
			return;
		}
		TiffDecoder td = new TiffDecoder(dir, name);
		if (IJ.debugMode) td.enableDebugging();
		IJ.showStatus("Decoding TIFF header...");
		try {
			info = td.getTiffInfo();
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null||msg.equals("")) msg = ""+e;
			IJ.error("TiffDecoder", msg);
			return;
		}
		if (info==null || info.length==0) {
			IJ.error("Virtual Stack", "This does not appear to be a TIFF stack");
			return;
		}
		if (IJ.debugMode)
			IJ.log(info[0].debugInfo);
	}
		
	/**
	 * Open.
	 *
	 * @return the image plus
	 */
	private ImagePlus open() {
		FileInfo fi = info[0];
		int n = fi.nImages;
		if (info.length==1 && n>1) {
			info = new FileInfo[n];
			long size = fi.width*fi.height*fi.getBytesPerPixel();
			for (int i=0; i<n; i++) {
				info[i] = (FileInfo)fi.clone();
				info[i].nImages = 1;
				info[i].longOffset = fi.getOffset() + i*(size + fi.gapBetweenImages);
			}
		}
		nImages = info.length;
		FileOpener fo = new FileOpener(info[0] );
		ImagePlus imp = fo.open(false);
		if (nImages==1 && fi.fileType==FileInfo.RGB48)
			return imp;
		Properties props = fo.decodeDescriptionString(fi);
		ImagePlus imp2 = new ImagePlus(fi.fileName, this);
		imp2.setFileInfo(fi);
		if (imp!=null && props!=null) {
			setBitDepth(imp.getBitDepth());
			imp2.setCalibration(imp.getCalibration());
			imp2.setOverlay(imp.getOverlay());
			if (fi.info!=null)
				imp2.setProperty("Info", fi.info);
			int channels = getInt(props,"channels");
			int slices = getInt(props,"slices");
			int frames = getInt(props,"frames");
			if (channels*slices*frames==nImages) {
				imp2.setDimensions(channels, slices, frames);
				if (getBoolean(props, "hyperstack"))
					imp2.setOpenAsHyperStack(true);
			}
			if (channels>1 && fi.description!=null) {
				int mode = IJ.COMPOSITE;
				if (fi.description.indexOf("mode=color")!=-1)
					mode = IJ.COLOR;
				else if (fi.description.indexOf("mode=gray")!=-1)
					mode = IJ.GRAYSCALE;
				imp2 = new CompositeImage(imp2, mode);
			}
		}
		return imp2;
	}

	/**
	 * Gets the int.
	 *
	 * @param props the props
	 * @param key the key
	 * @return the int
	 */
	int getInt(Properties props, String key) {
		Double n = getNumber(props, key);
		return n!=null?(int)n.doubleValue():1;
	}

	/**
	 * Gets the number.
	 *
	 * @param props the props
	 * @param key the key
	 * @return the number
	 */
	Double getNumber(Properties props, String key) {
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Double.valueOf(s);
			} catch (NumberFormatException e) {}
		}	
		return null;
	}

	/**
	 * Gets the boolean.
	 *
	 * @param props the props
	 * @param key the key
	 * @return the boolean
	 */
	boolean getBoolean(Properties props, String key) {
		String s = props.getProperty(key);
		return s!=null&&s.equals("true")?true:false;
	}

	/**
	 *  Deletes the specified image, were 1<=n<=nImages.
	 *
	 * @param n the n
	 */
	public void deleteSlice(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (nImages<1) return;
		for (int i=n; i<nImages; i++)
			info[i-1] = info[i];
		info[nImages-1] = null;
		nImages--;
	}
	
	/**
	 *  Returns an ImageProcessor for the specified image,
	 * 		were 1<=n<=nImages. Returns null if the stack is empty.
	 *
	 * @param n the n
	 * @return the processor
	 */
	public ImageProcessor getProcessor(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		//if (n>1) IJ.log("  "+(info[n-1].getOffset()-info[n-2].getOffset()));
		info[n-1].nImages = 1; // why is this needed?
		ImagePlus imp = null;
		if (IJ.debugMode) {
			long t0 = System.currentTimeMillis();
			FileOpener fo = new FileOpener(info[n-1]);
			imp = fo.open(false);
			IJ.log("FileInfoVirtualStack: "+n+", offset="+info[n-1].getOffset()+", "+(System.currentTimeMillis()-t0)+"ms");
		} else {
			FileOpener fo = new FileOpener(info[n-1]);
			imp = fo.open(false);
		}
		if (imp!=null)
			return imp.getProcessor();
		else {
			int w=getWidth(), h=getHeight();
			IJ.log("Read error or file not found ("+n+"): "+info[n-1].directory+info[n-1].fileName);
			switch (getBitDepth()) {
				case 8: return new ByteProcessor(w, h);
				case 16: return new ShortProcessor(w, h);
				case 24: return new ColorProcessor(w, h);
				case 32: return new FloatProcessor(w, h);
				default: return null;
			}
		}
	 }
 
	 /**
 	 *  Returns the number of images in this stack.
 	 *
 	 * @return the size
 	 */
	public int getSize() {
		return nImages;
	}

	/**
	 *  Returns the label of the Nth image.
	 *
	 * @param n the n
	 * @return the slice label
	 */
	public String getSliceLabel(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (info[0].sliceLabels==null || info[0].sliceLabels.length!=nImages)
			return null;
		else
			return info[0].sliceLabels[n-1];
	}

	/* (non-Javadoc)
	 * @see ij.ImageStack#getWidth()
	 */
	public int getWidth() {
		return info[0].width;
	}
	
	/* (non-Javadoc)
	 * @see ij.ImageStack#getHeight()
	 */
	public int getHeight() {
		return info[0].height;
	}
    
}
