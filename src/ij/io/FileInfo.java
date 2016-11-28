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
package ij.io;
import ij.VirtualStack;

import java.io.InputStream;

// TODO: Auto-generated Javadoc
/** This class consists of public fields that describe an image file. */
public class FileInfo implements Cloneable {

	/** 8-bit unsigned integer (0-255). */
	public static final int GRAY8 = 0;
	
	/**	16-bit signed integer (-32768-32767). Imported signed images
		are converted to unsigned by adding 32768. */
	public static final int GRAY16_SIGNED = 1;
	
	/** 16-bit unsigned integer (0-65535). */
	public static final int GRAY16_UNSIGNED = 2;
	
	/**	32-bit signed integer. Imported 32-bit integer images are
		converted to floating-point. */
	public static final int GRAY32_INT = 3;
	
	/** 32-bit floating-point. */
	public static final int GRAY32_FLOAT = 4;
	
	/** 8-bit unsigned integer with color lookup table. */
	public static final int COLOR8 = 5;
	
	/** 24-bit interleaved RGB. Import/export only. */
	public static final int RGB = 6;	
	
	/** 24-bit planer RGB. Import only. */
	public static final int RGB_PLANAR = 7;
	
	/** 1-bit black and white. Import only. */
	public static final int BITMAP = 8;
	
	/** 32-bit interleaved ARGB. Import only. */
	public static final int ARGB = 9;
	
	/** 24-bit interleaved BGR. Import only. */
	public static final int BGR = 10;
	
	/**	32-bit unsigned integer. Imported 32-bit integer images are
		converted to floating-point. */
	public static final int GRAY32_UNSIGNED = 11;
	
	/** 48-bit interleaved RGB. */
	public static final int RGB48 = 12;	

	/** 12-bit unsigned integer (0-4095). Import only. */
	public static final int GRAY12_UNSIGNED = 13;	

	/** 24-bit unsigned integer. Import only. */
	public static final int GRAY24_UNSIGNED = 14;	

	/** 32-bit interleaved BARG (MCID). Import only. */
	public static final int BARG  = 15;	

	/** 64-bit floating-point. Import only.*/
	public static final int GRAY64_FLOAT  = 16;	

	/** 48-bit planar RGB. Import only. */
	public static final int RGB48_PLANAR = 17;	

	/** 32-bit interleaved ABGR. Import only. */
	public static final int ABGR = 18;

	/** 32-bit interleaved CMYK. Import only. */
	public static final int CMYK = 19;

	/** The Constant UNKNOWN. */
	// File formats
	public static final int UNKNOWN = 0;
	
	/** The Constant RAW. */
	public static final int RAW = 1;
	
	/** The Constant TIFF. */
	public static final int TIFF = 2;
	
	/** The Constant GIF_OR_JPG. */
	public static final int GIF_OR_JPG = 3;
	
	/** The Constant FITS. */
	public static final int FITS = 4;
	
	/** The Constant BMP. */
	public static final int BMP = 5;
	
	/** The Constant DICOM. */
	public static final int DICOM = 6;
	
	/** The Constant ZIP_ARCHIVE. */
	public static final int ZIP_ARCHIVE = 7;
	
	/** The Constant PGM. */
	public static final int PGM = 8;
	
	/** The Constant IMAGEIO. */
	public static final int IMAGEIO = 9;

	/** The Constant COMPRESSION_UNKNOWN. */
	// Compression modes
	public static final int COMPRESSION_UNKNOWN = 0;
	
	/** The Constant COMPRESSION_NONE. */
	public static final int COMPRESSION_NONE= 1;
	
	/** The Constant LZW. */
	public static final int LZW = 2;
	
	/** The Constant LZW_WITH_DIFFERENCING. */
	public static final int LZW_WITH_DIFFERENCING = 3;
	
	/** The Constant JPEG. */
	public static final int JPEG = 4;
	
	/** The Constant PACK_BITS. */
	public static final int PACK_BITS = 5;
	
	/** The Constant ZIP. */
	public static final int ZIP = 6;
	
	/** The file format. */
	/* File format (TIFF, GIF_OR_JPG, BMP, etc.). Used by the File/Revert command */
	public int fileFormat;
	
	/** The file type. */
	/* File type (GRAY8, GRAY_16_UNSIGNED, RGB, etc.) */
	public int fileType;
	
	/** The file name. */
	public String fileName;
	
	/** The directory. */
	public String directory;
	
	/** The url. */
	public String url;
    
    /** The width. */
    public int width;
    
    /** The height. */
    public int height;
    
    /** The offset. */
    public int offset=0;  // Use getOffset() to read
    
    /** The n images. */
    public int nImages;
    
    /** The gap between images. */
    public int gapBetweenImages;
    
    /** The white is zero. */
    public boolean whiteIsZero;
    
    /** The intel byte order. */
    public boolean intelByteOrder;
	
	/** The compression. */
	public int compression;
    
    /** The strip offsets. */
    public int[] stripOffsets; 
    
    /** The strip lengths. */
    public int[] stripLengths;
    
    /** The rows per strip. */
    public int rowsPerStrip;
	
	/** The lut size. */
	public int lutSize;
	
	/** The reds. */
	public byte[] reds;
	
	/** The greens. */
	public byte[] greens;
	
	/** The blues. */
	public byte[] blues;
	
	/** The pixels. */
	public Object pixels;	
	
	/** The debug info. */
	public String debugInfo;
	
	/** The slice labels. */
	public String[] sliceLabels;
	
	/** The info. */
	public String info;
	
	/** The input stream. */
	public InputStream inputStream;
	
	/** The virtual stack. */
	public VirtualStack virtualStack;
	
	/** The pixel width. */
	public double pixelWidth=1.0;
	
	/** The pixel height. */
	public double pixelHeight=1.0;
	
	/** The pixel depth. */
	public double pixelDepth=1.0;
	
	/** The unit. */
	public String unit;
	
	/** The calibration function. */
	public int calibrationFunction;
	
	/** The coefficients. */
	public double[] coefficients;
	
	/** The value unit. */
	public String valueUnit;
	
	/** The frame interval. */
	public double frameInterval;
	
	/** The description. */
	public String description;
	
	/** The long offset. */
	// Use <i>longOffset</i> instead of <i>offset</i> when offset>2147483647.
	public long longOffset;  // Use getOffset() to read
	
	/** The meta data types. */
	// Extra metadata to be stored in the TIFF header
	public int[] metaDataTypes; // must be < 0xffffff
	
	/** The meta data. */
	public byte[][] metaData;
	
	/** The display ranges. */
	public double[] displayRanges;
	
	/** The channel luts. */
	public byte[][] channelLuts;
	
	/** The roi. */
	public byte[] roi;
	
	/** The overlay. */
	public byte[][] overlay;
	
	/** The samples per pixel. */
	public int samplesPerPixel;
	
	/** The open next name. */
	public String openNextDir, openNextName;
    
	/** Creates a FileInfo object with all of its fields set to their default value. */
     public FileInfo() {
    	// assign default values
    	fileFormat = UNKNOWN;
    	fileType = GRAY8;
    	fileName = "Untitled";
    	directory = "";
    	url = "";
	    nImages = 1;
		compression = COMPRESSION_NONE;
		samplesPerPixel = 1;
    }
    
    /**
     *  Returns the offset as a long.
     *
     * @return the offset
     */
    public final long getOffset() {
    	return longOffset>0L?longOffset:((long)offset)&0xffffffffL;
    }
    
	/**
	 *  Returns the number of bytes used per pixel.
	 *
	 * @return the bytes per pixel
	 */
	public int getBytesPerPixel() {
		switch (fileType) {
			case GRAY8: case COLOR8: case BITMAP: return 1;
			case GRAY16_SIGNED: case GRAY16_UNSIGNED: return 2;
			case GRAY32_INT: case GRAY32_UNSIGNED: case GRAY32_FLOAT: case ARGB: case GRAY24_UNSIGNED: case BARG: case ABGR: case CMYK: return 4;
			case RGB: case RGB_PLANAR: case BGR: return 3;
			case RGB48: case RGB48_PLANAR: return 6;
			case GRAY64_FLOAT : return 8;
			default: return 0;
		}
	}

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return
    		"name=" + fileName
			+ ", dir=" + directory
			+ ", width=" + width
			+ ", height=" + height
			+ ", nImages=" + nImages
			+ ", offset=" + getOffset()
			+ ", type=" + getType()
			+ ", byteOrder=" + (intelByteOrder?"little":"big")
			+ ", format=" + fileFormat
			+ ", url=" + url
			+ ", whiteIsZero=" + (whiteIsZero?"t":"f")
			+ ", lutSize=" + lutSize
			+ ", comp=" + compression
			+ ", ranges=" + (displayRanges!=null?""+displayRanges.length/2:"null")
			+ ", samples=" + samplesPerPixel;
    }
    
    /**
     * Gets the type.
     *
     * @return the type
     */
    private String getType() {
    	switch (fileType) {
			case GRAY8: return "byte";
			case GRAY16_SIGNED: return "short";
			case GRAY16_UNSIGNED: return "ushort";
			case GRAY32_INT: return "int";
			case GRAY32_UNSIGNED: return "uint";
			case GRAY32_FLOAT: return "float";
			case COLOR8: return "byte(lut)";
			case RGB: return "RGB";
			case RGB_PLANAR: return "RGB(p)";
			case RGB48: return "RGB48";
			case BITMAP: return "bitmap";
			case ARGB: return "ARGB";
			case ABGR: return "ABGR";
			case BGR: return "BGR";
			case BARG: return "BARG";
			case CMYK: return "CMYK";
			case GRAY64_FLOAT: return "double";
			case RGB48_PLANAR: return "RGB48(p)";
			default: return "";
    	}
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}

}
