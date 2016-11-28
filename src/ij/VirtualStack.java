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
package ij;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Font;
import java.awt.image.ColorModel;
import java.io.File;

// TODO: Auto-generated Javadoc
/** This class represents an array of disk-resident images. */
public class VirtualStack extends ImageStack {
	
	/** The Constant INITIAL_SIZE. */
	private static final int INITIAL_SIZE = 100;
	
	/** The path. */
	private String path;
	
	/** The n slices. */
	private int nSlices;
	
	/** The names. */
	private String[] names;
	
	/** The labels. */
	private String[] labels;
	
	/** The bit depth. */
	private int bitDepth;
	
	/** Default constructor. */
	public VirtualStack() { }

	/**
	 *  Creates a new, empty virtual stack.
	 *
	 * @param width the width
	 * @param height the height
	 * @param cm the cm
	 * @param path the path
	 */
	public VirtualStack(int width, int height, ColorModel cm, String path) {
		super(width, height, cm);
		this.path = path;
		names = new String[INITIAL_SIZE];
		labels = new String[INITIAL_SIZE];
		//IJ.log("VirtualStack: "+path);
	}

	 /**
 	 *  Adds an image to the end of the stack.
 	 *
 	 * @param name the name
 	 */
	public void addSlice(String name) {
		if (name==null) 
			throw new IllegalArgumentException("'name' is null!");
		nSlices++;
	   //IJ.log("addSlice: "+nSlices+"	"+name);
	   if (nSlices==names.length) {
			String[] tmp = new String[nSlices*2];
			System.arraycopy(names, 0, tmp, 0, nSlices);
			names = tmp;
			tmp = new String[nSlices*2];
			System.arraycopy(labels, 0, tmp, 0, nSlices);
			labels = tmp;
		}
		names[nSlices-1] = name;
	}

   /**
    *  Does nothing.
    *
    * @param sliceLabel the slice label
    * @param pixels the pixels
    */
	public void addSlice(String sliceLabel, Object pixels) {
	}

	/**
	 *  Does nothing..
	 *
	 * @param sliceLabel the slice label
	 * @param ip the ip
	 */
	public void addSlice(String sliceLabel, ImageProcessor ip) {
	}
	
	/**
	 *  Does noting.
	 *
	 * @param sliceLabel the slice label
	 * @param ip the ip
	 * @param n the n
	 */
	public void addSlice(String sliceLabel, ImageProcessor ip, int n) {
	}

	/**
	 *  Deletes the specified slice, were 1<=n<=nslices.
	 *
	 * @param n the n
	 */
	public void deleteSlice(int n) {
		if (n<1 || n>nSlices)
			throw new IllegalArgumentException("Argument out of range: "+n);
			if (nSlices<1)
				return;
			for (int i=n; i<nSlices; i++)
				names[i-1] = names[i];
			names[nSlices-1] = null;
			nSlices--;
		}
	
	/** Deletes the last slice in the stack. */
	public void deleteLastSlice() {
		if (nSlices>0)
			deleteSlice(nSlices);
	}
	   
   /**
    *  Returns the pixel array for the specified slice, were 1<=n<=nslices.
    *
    * @param n the n
    * @return the pixels
    */
	public Object getPixels(int n) {
		ImageProcessor ip = getProcessor(n);
		if (ip!=null)
			return ip.getPixels();
		else
			return null;
	}		
	
	 /**
 	 *  Assigns a pixel array to the specified slice,
 	 * 		were 1<=n<=nslices.
 	 *
 	 * @param pixels the pixels
 	 * @param n the n
 	 */
	public void setPixels(Object pixels, int n) {
	}

   /**
    *  Returns an ImageProcessor for the specified slice,
    * 		were 1<=n<=nslices. Returns null if the stack is empty.
    *
    * @param n the n
    * @return the processor
    */
	public ImageProcessor getProcessor(int n) {
		//IJ.log("getProcessor: "+n+"  "+names[n-1]+"  "+bitDepth);
		Opener opener = new Opener();
		opener.setSilentMode(true);
		IJ.redirectErrorMessages(true);
		ImagePlus imp = opener.openImage(path, names[n-1]);
		IJ.redirectErrorMessages(false);
		ImageProcessor ip = null;
		int depthThisImage = 0;
		if (imp!=null) {
			int w = imp.getWidth();
			int h = imp.getHeight();
			int type = imp.getType();
			ColorModel cm = imp.getProcessor().getColorModel();
			String info = (String)imp.getProperty("Info");
			if (info!=null && !(info.startsWith("Software")||info.startsWith("ImageDescription")))
				labels[n-1] = info;
			depthThisImage = imp.getBitDepth();
			ip = imp.getProcessor();
			ip.setOverlay(imp.getOverlay());
		} else {
			File f = new File(path, names[n-1]);
			String msg = f.exists()?"Error opening ":"File not found: ";
			ip = new ByteProcessor(getWidth(), getHeight());
			ip.invert();
			int size = getHeight()/20;
			if (size<9) size=9;
			Font font = new Font("Helvetica", Font.PLAIN, size);
			ip.setFont(font);
			ip.setAntialiasedText(true);
			ip.setColor(0);
			ip.drawString(msg+names[n-1], size, size*2);
			depthThisImage = 8;
		}
		if (depthThisImage!=bitDepth) {
			switch (bitDepth) {
				case 8: ip=ip.convertToByte(true); break;
				case 16: ip=ip.convertToShort(true); break;
				case 24:  ip=ip.convertToRGB(); break;
				case 32: ip=ip.convertToFloat(); break;
			}
		}
		if (ip.getWidth()!=getWidth() || ip.getHeight()!=getHeight()) {
			ImageProcessor ip2 = ip.createProcessor(getWidth(), getHeight());
			ip2.insert(ip, 0, 0);
			ip = ip2;
		}
		return ip;
	 }
 
	/**
	 *  Currently not implemented.
	 *
	 * @param n the n
	 * @return the int
	 */
	public int saveChanges(int n) {
		return -1;
	}

	 /**
 	 *  Returns the number of slices in this stack.
 	 *
 	 * @return the size
 	 */
	public int getSize() {
		return nSlices;
	}

	/**
	 *  Returns the label of the Nth image.
	 *
	 * @param n the n
	 * @return the slice label
	 */
	public String getSliceLabel(int n) {
		String label = labels[n-1];
		if (label==null)
			return names[n-1];
		else if (label.length()<=60)
			return label;
		else
			return names[n-1]+"\n"+label;
	}
	
	/**
	 *  Returns null.
	 *
	 * @return the image array
	 */
	public Object[] getImageArray() {
		return null;
	}

   /**
    *  Does nothing.
    *
    * @param label the label
    * @param n the n
    */
	public void setSliceLabel(String label, int n) {
	}

	/**
	 *  Always return true.
	 *
	 * @return true, if is virtual
	 */
	public boolean isVirtual() {
		return true;
	}

   /** Does nothing. */
	public void trim() {
	}
	
	/**
	 *  Returns the path to the directory containing the images.
	 *
	 * @return the directory
	 */
	public String getDirectory() {
		return path;
	}
		
	/**
	 *  Returns the file name of the specified slice, were 1<=n<=nslices.
	 *
	 * @param n the n
	 * @return the file name
	 */
	public String getFileName(int n) {
		return names[n-1];
	}
	
	/**
	 *  Sets the bit depth (8, 16, 24 or 32).
	 *
	 * @param bitDepth the new bit depth
	 */
	public void setBitDepth(int bitDepth) {
		this.bitDepth = bitDepth;
	}

	/**
	 *  Returns the bit depth (8, 16, 24 or 32), or 0 if the bit depth is not known.
	 *
	 * @return the bit depth
	 */
	public int getBitDepth() {
		return bitDepth;
	}
	
	/**
	 * Sort dicom.
	 *
	 * @param strings the strings
	 * @param info the info
	 * @param maxDigits the max digits
	 * @return the image stack
	 */
	public ImageStack sortDicom(String[] strings, String[] info, int maxDigits) {
		int n = getSize();
		String[] names2 = new String[n];
		for (int i=0; i<n; i++)
			names2[i] = names[i];
		for (int i=0; i<n; i++) {
			int slice = (int)Tools.parseDouble(strings[i].substring(strings[i].length()-maxDigits), 0.0);
			if (slice==0) return null;
			names[i] = names2[slice-1];
			labels[i] = info[slice-1];
		}
		return this;
	}

} 

