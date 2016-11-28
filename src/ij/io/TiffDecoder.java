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
import ij.util.Tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
Decodes single and multi-image TIFF files. The LZW decompression
code was contributed by Curtis Rueden.
*/
public class TiffDecoder {

	/** The Constant NEW_SUBFILE_TYPE. */
	// tags
	public static final int NEW_SUBFILE_TYPE = 254;
	
	/** The Constant IMAGE_WIDTH. */
	public static final int IMAGE_WIDTH = 256;
	
	/** The Constant IMAGE_LENGTH. */
	public static final int IMAGE_LENGTH = 257;
	
	/** The Constant BITS_PER_SAMPLE. */
	public static final int BITS_PER_SAMPLE = 258;
	
	/** The Constant COMPRESSION. */
	public static final int COMPRESSION = 259;
	
	/** The Constant PHOTO_INTERP. */
	public static final int PHOTO_INTERP = 262;
	
	/** The Constant IMAGE_DESCRIPTION. */
	public static final int IMAGE_DESCRIPTION = 270;
	
	/** The Constant STRIP_OFFSETS. */
	public static final int STRIP_OFFSETS = 273;
	
	/** The Constant ORIENTATION. */
	public static final int ORIENTATION = 274;
	
	/** The Constant SAMPLES_PER_PIXEL. */
	public static final int SAMPLES_PER_PIXEL = 277;
	
	/** The Constant ROWS_PER_STRIP. */
	public static final int ROWS_PER_STRIP = 278;
	
	/** The Constant STRIP_BYTE_COUNT. */
	public static final int STRIP_BYTE_COUNT = 279;
	
	/** The Constant X_RESOLUTION. */
	public static final int X_RESOLUTION = 282;
	
	/** The Constant Y_RESOLUTION. */
	public static final int Y_RESOLUTION = 283;
	
	/** The Constant PLANAR_CONFIGURATION. */
	public static final int PLANAR_CONFIGURATION = 284;
	
	/** The Constant RESOLUTION_UNIT. */
	public static final int RESOLUTION_UNIT = 296;
	
	/** The Constant SOFTWARE. */
	public static final int SOFTWARE = 305;
	
	/** The Constant DATE_TIME. */
	public static final int DATE_TIME = 306;
	
	/** The Constant ARTEST. */
	public static final int ARTEST = 315;
	
	/** The Constant HOST_COMPUTER. */
	public static final int HOST_COMPUTER = 316;
	
	/** The Constant PREDICTOR. */
	public static final int PREDICTOR = 317;
	
	/** The Constant COLOR_MAP. */
	public static final int COLOR_MAP = 320;
	
	/** The Constant TILE_WIDTH. */
	public static final int TILE_WIDTH = 322;
	
	/** The Constant SAMPLE_FORMAT. */
	public static final int SAMPLE_FORMAT = 339;
	
	/** The Constant JPEG_TABLES. */
	public static final int JPEG_TABLES = 347;
	
	/** The Constant METAMORPH1. */
	public static final int METAMORPH1 = 33628;
	
	/** The Constant METAMORPH2. */
	public static final int METAMORPH2 = 33629;
	
	/** The Constant IPLAB. */
	public static final int IPLAB = 34122;
	
	/** The Constant NIH_IMAGE_HDR. */
	public static final int NIH_IMAGE_HDR = 43314;
	
	/** The Constant META_DATA_BYTE_COUNTS. */
	public static final int META_DATA_BYTE_COUNTS = 50838; // private tag registered with Adobe
	
	/** The Constant META_DATA. */
	public static final int META_DATA = 50839; // private tag registered with Adobe
	
	/** The Constant UNSIGNED. */
	//constants
	static final int UNSIGNED = 1;
	
	/** The Constant SIGNED. */
	static final int SIGNED = 2;
	
	/** The Constant FLOATING_POINT. */
	static final int FLOATING_POINT = 3;

	/** The Constant SHORT. */
	//field types
	static final int SHORT = 3;
	
	/** The Constant LONG. */
	static final int LONG = 4;

	/** The Constant MAGIC_NUMBER. */
	// metadata types
	static final int MAGIC_NUMBER = 0x494a494a;  // "IJIJ"
	
	/** The Constant INFO. */
	static final int INFO = 0x696e666f;  // "info" (Info image property)
	
	/** The Constant LABELS. */
	static final int LABELS = 0x6c61626c;  // "labl" (slice labels)
	
	/** The Constant RANGES. */
	static final int RANGES = 0x72616e67;  // "rang" (display ranges)
	
	/** The Constant LUTS. */
	static final int LUTS = 0x6c757473;  // "luts" (channel LUTs)
	
	/** The Constant ROI. */
	static final int ROI = 0x726f6920;  // "roi " (ROI)
	
	/** The Constant OVERLAY. */
	static final int OVERLAY = 0x6f766572;  // "over" (overlay)
	
	/** The directory. */
	private String directory;
	
	/** The name. */
	private String name;
	
	/** The url. */
	private String url;
	
	/** The in. */
	protected RandomAccessStream in;
	
	/** The debug mode. */
	protected boolean debugMode;
	
	/** The little endian. */
	private boolean littleEndian;
	
	/** The d info. */
	private String dInfo;
	
	/** The ifd count. */
	private int ifdCount;
	
	/** The meta data counts. */
	private int[] metaDataCounts;
	
	/** The tiff metadata. */
	private String tiffMetadata;
	
	/** The photo interp. */
	private int photoInterp;
		
	/**
	 * Instantiates a new tiff decoder.
	 *
	 * @param directory the directory
	 * @param name the name
	 */
	public TiffDecoder(String directory, String name) {
		this.directory = directory;
		this.name = name;
	}

	/**
	 * Instantiates a new tiff decoder.
	 *
	 * @param in the in
	 * @param name the name
	 */
	public TiffDecoder(InputStream in, String name) {
		directory = "";
		this.name = name;
		url = "";
		this.in = new RandomAccessStream(in);
	}

	/**
	 * Gets the int.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	final int getInt() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		int b4 = in.read();
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}
	
	/**
	 * Gets the unsigned int.
	 *
	 * @return the unsigned int
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	final long getUnsignedInt() throws IOException {
		return (long)getInt()&0xffffffffL;
	}

	/**
	 * Gets the short.
	 *
	 * @return the short
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	final int getShort() throws IOException {
		int b1 = in.read();
		int b2 = in.read();
		if (littleEndian)
			return ((b2<<8) + b1);
		else
			return ((b1<<8) + b2);
	}

    /**
     * Read long.
     *
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    final long readLong() throws IOException {
    	if (littleEndian)
        	return ((long)getInt()&0xffffffffL) + ((long)getInt()<<32);
        else
			return ((long)getInt()<<32) + ((long)getInt()&0xffffffffL);
        	//return in.read()+(in.read()<<8)+(in.read()<<16)+(in.read()<<24)+(in.read()<<32)+(in.read()<<40)+(in.read()<<48)+(in.read()<<56);
    }

    /**
     * Read double.
     *
     * @return the double
     * @throws IOException Signals that an I/O exception has occurred.
     */
    final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

	/**
	 * Open image file header.
	 *
	 * @return the long
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	long OpenImageFileHeader() throws IOException {
	// Open 8-byte Image File Header at start of file.
	// Returns the offset in bytes to the first IFD or -1
	// if this is not a valid tiff file.
		int byteOrder = in.readShort();
		if (byteOrder==0x4949) // "II"
			littleEndian = true;
		else if (byteOrder==0x4d4d) // "MM"
			littleEndian = false;
		else {
			in.close();
			return -1;
		}
		int magicNumber = getShort(); // 42
		long offset = ((long)getInt())&0xffffffffL;
		return offset;
	}
		
	/**
	 * Gets the value.
	 *
	 * @param fieldType the field type
	 * @param count the count
	 * @return the value
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	int getValue(int fieldType, int count) throws IOException {
		int value = 0;
		int unused;
		if (fieldType==SHORT && count==1) {
			value = getShort();
			unused = getShort();
		} else
			value = getInt();
		return value;
	}	
	
	/**
	 * Gets the color map.
	 *
	 * @param offset the offset
	 * @param fi the fi
	 * @return the color map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getColorMap(long offset, FileInfo fi) throws IOException {
		byte[] colorTable16 = new byte[768*2];
		long saveLoc = in.getLongFilePointer();
		in.seek(offset);
		in.readFully(colorTable16);
		in.seek(saveLoc);
		fi.lutSize = 256;
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		int j = 0;
		if (littleEndian) j++;
		int sum = 0;
		for (int i=0; i<256; i++) {
			fi.reds[i] = colorTable16[j];
			sum += fi.reds[i];
			fi.greens[i] = colorTable16[512+j];
			sum += fi.greens[i];
			fi.blues[i] = colorTable16[1024+j];
			sum += fi.blues[i];
			j += 2;
		}
		if (sum!=0 && fi.fileType==FileInfo.GRAY8)
			fi.fileType = FileInfo.COLOR8;
	}
	
	/**
	 * Gets the string.
	 *
	 * @param count the count
	 * @param offset the offset
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	byte[] getString(int count, long offset) throws IOException {
		count--; // skip null byte at end of string
		if (count<=3)
			return null;
		byte[] bytes = new byte[count];
		long saveLoc = in.getLongFilePointer();
		in.seek(offset);
		in.readFully(bytes);
		in.seek(saveLoc);
		return bytes;
	}

	/**
	 *  Save the image description in the specified FileInfo. ImageJ
	 * 		saves spatial and density calibration data in this string. For
	 * 		stacks, it also saves the number of images to avoid having to
	 * 		decode an IFD for each image.
	 *
	 * @param description the description
	 * @param fi the fi
	 */
	public void saveImageDescription(byte[] description, FileInfo fi) {
        String id = new String(description);
        if (!id.startsWith("ImageJ"))
			saveMetadata(getName(IMAGE_DESCRIPTION), id);
		if (id.length()<7) return;
		fi.description = id;
        int index1 = id.indexOf("images=");
        if (index1>0) {
            int index2 = id.indexOf("\n", index1);
            if (index2>0) {
                String images = id.substring(index1+7,index2);
                int n = (int)Tools.parseDouble(images, 0.0);
                if (n>1) fi.nImages = n;
            }
        }
	}

	/**
	 * Save metadata.
	 *
	 * @param name the name
	 * @param data the data
	 */
	public void saveMetadata(String name, String data) {
		if (data==null) return;
        String str = name+": "+data+"\n";
        if (tiffMetadata==null)
        	tiffMetadata = str;
        else
        	tiffMetadata += str;
	}

	/**
	 * Decode NIH image header.
	 *
	 * @param offset the offset
	 * @param fi the fi
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void decodeNIHImageHeader(int offset, FileInfo fi) throws IOException {
		long saveLoc = in.getLongFilePointer();
		
		in.seek(offset+12);
		int version = in.readShort();
		
		in.seek(offset+160);
		double scale = in.readDouble();
		if (version>106 && scale!=0.0) {
			fi.pixelWidth = 1.0/scale;
			fi.pixelHeight = fi.pixelWidth;
		} 

		// spatial calibration
		in.seek(offset+172);
		int units = in.readShort();
		if (version<=153) units += 5;
		switch (units) {
			case 5: fi.unit = "nanometer"; break;
			case 6: fi.unit = "micrometer"; break;
			case 7: fi.unit = "mm"; break;
			case 8: fi.unit = "cm"; break;
			case 9: fi.unit = "meter"; break;
			case 10: fi.unit = "km"; break;
			case 11: fi.unit = "inch"; break;
			case 12: fi.unit = "ft"; break;
			case 13: fi.unit = "mi"; break;
		}

		// density calibration
		in.seek(offset+182);
		int fitType = in.read();
		int unused = in.read();
		int nCoefficients = in.readShort();
		if (fitType==11) {
			fi.calibrationFunction = 21; //Calibration.UNCALIBRATED_OD
			fi.valueUnit = "U. OD";
		} else if (fitType>=0 && fitType<=8 && nCoefficients>=1 && nCoefficients<=5) {
			switch (fitType) {
				case 0: fi.calibrationFunction = 0; break; //Calibration.STRAIGHT_LINE
				case 1: fi.calibrationFunction = 1; break; //Calibration.POLY2
				case 2: fi.calibrationFunction = 2; break; //Calibration.POLY3
				case 3: fi.calibrationFunction = 3; break; //Calibration.POLY4
				case 5: fi.calibrationFunction = 4; break; //Calibration.EXPONENTIAL
				case 6: fi.calibrationFunction = 5; break; //Calibration.POWER
				case 7: fi.calibrationFunction = 6; break; //Calibration.LOG
				case 8: fi.calibrationFunction = 10; break; //Calibration.RODBARD2 (NIH Image)
			}
			fi.coefficients = new double[nCoefficients];
			for (int i=0; i<nCoefficients; i++) {
				fi.coefficients[i] = in.readDouble();
			}
			in.seek(offset+234);
			int size = in.read();
			StringBuffer sb = new StringBuffer();
			if (size>=1 && size<=16) {
				for (int i=0; i<size; i++)
					sb.append((char)(in.read()));
				fi.valueUnit = new String(sb);
			} else
				fi.valueUnit = " ";
		}
			
		in.seek(offset+260);
		int nImages = in.readShort();
		if(nImages>=2 && (fi.fileType==FileInfo.GRAY8||fi.fileType==FileInfo.COLOR8)) {
			fi.nImages = nImages;
			fi.pixelDepth = in.readFloat();	//SliceSpacing
			int skip = in.readShort();		//CurrentSlice
			fi.frameInterval = in.readFloat();
			//ij.IJ.write("fi.pixelDepth: "+fi.pixelDepth);
		}
			
		in.seek(offset+272);
		float aspectRatio = in.readFloat();
		if (version>140 && aspectRatio!=0.0)
			fi.pixelHeight = fi.pixelWidth/aspectRatio;
		
		in.seek(saveLoc);
	}
	
	/**
	 * Dump tag.
	 *
	 * @param tag the tag
	 * @param count the count
	 * @param value the value
	 * @param fi the fi
	 */
	void dumpTag(int tag, int count, int value, FileInfo fi) {
		long lvalue = ((long)value)&0xffffffffL;
		String name = getName(tag);
		String cs = (count==1)?"":", count=" + count;
		dInfo += "    " + tag + ", \"" + name + "\", value=" + lvalue + cs + "\n";
		//ij.IJ.log(tag + ", \"" + name + "\", value=" + value + cs + "\n");
	}

	/**
	 * Gets the name.
	 *
	 * @param tag the tag
	 * @return the name
	 */
	String getName(int tag) {
		String name;
		switch (tag) {
			case NEW_SUBFILE_TYPE: name="NewSubfileType"; break;
			case IMAGE_WIDTH: name="ImageWidth"; break;
			case IMAGE_LENGTH: name="ImageLength"; break;
			case STRIP_OFFSETS: name="StripOffsets"; break;
			case ORIENTATION: name="Orientation"; break;
			case PHOTO_INTERP: name="PhotoInterp"; break;
			case IMAGE_DESCRIPTION: name="ImageDescription"; break;
			case BITS_PER_SAMPLE: name="BitsPerSample"; break;
			case SAMPLES_PER_PIXEL: name="SamplesPerPixel"; break;
			case ROWS_PER_STRIP: name="RowsPerStrip"; break;
			case STRIP_BYTE_COUNT: name="StripByteCount"; break;
			case X_RESOLUTION: name="XResolution"; break;
			case Y_RESOLUTION: name="YResolution"; break;
			case RESOLUTION_UNIT: name="ResolutionUnit"; break;
			case SOFTWARE: name="Software"; break;
			case DATE_TIME: name="DateTime"; break;
			case ARTEST: name="Artest"; break;
			case HOST_COMPUTER: name="HostComputer"; break;
			case PLANAR_CONFIGURATION: name="PlanarConfiguration"; break;
			case COMPRESSION: name="Compression"; break; 
			case PREDICTOR: name="Predictor"; break; 
			case COLOR_MAP: name="ColorMap"; break; 
			case SAMPLE_FORMAT: name="SampleFormat"; break; 
			case JPEG_TABLES: name="JPEGTables"; break; 
			case NIH_IMAGE_HDR: name="NIHImageHeader"; break; 
			case META_DATA_BYTE_COUNTS: name="MetaDataByteCounts"; break; 
			case META_DATA: name="MetaData"; break; 
			default: name="???"; break;
		}
		return name;
	}

	/**
	 * Gets the rational.
	 *
	 * @param loc the loc
	 * @return the rational
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	double getRational(long loc) throws IOException {
		long saveLoc = in.getLongFilePointer();
		in.seek(loc);
		double numerator = getUnsignedInt();
		double denominator = getUnsignedInt();
		in.seek(saveLoc);
		if (denominator!=0.0)
			return numerator/denominator;
		else
			return 0.0;
	}
	
	/**
	 * Open IFD.
	 *
	 * @return the file info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	FileInfo OpenIFD() throws IOException {
	// Get Image File Directory data
		int tag, fieldType, count, value;
		int nEntries = getShort();
		if (nEntries<1 || nEntries>1000)
			return null;
		ifdCount++;
		if ((ifdCount%50)==0 && ifdCount>0)
			ij.IJ.showStatus("Opening IFDs: "+ifdCount);
		FileInfo fi = new FileInfo();
		fi.fileType = FileInfo.BITMAP;  //BitsPerSample defaults to 1
		for (int i=0; i<nEntries; i++) {
			tag = getShort();
			fieldType = getShort();
			count = getInt();
			value = getValue(fieldType, count);
			long lvalue = ((long)value)&0xffffffffL;
			if (debugMode && ifdCount<10) dumpTag(tag, count, value, fi);
			//ij.IJ.write(i+"/"+nEntries+" "+tag + ", count=" + count + ", value=" + value);
			//if (tag==0) return null;
			switch (tag) {
				case IMAGE_WIDTH: 
					fi.width = value;
					fi.intelByteOrder = littleEndian;
					break;
				case IMAGE_LENGTH: 
					fi.height = value;
					break;
 				case STRIP_OFFSETS:
					if (count==1)
						fi.stripOffsets = new int[] {value};
					else {
						long saveLoc = in.getLongFilePointer();
						in.seek(lvalue);
						fi.stripOffsets = new int[count];
						for (int c=0; c<count; c++)
							fi.stripOffsets[c] = getInt();
						in.seek(saveLoc);
					}
					fi.offset = count>0?fi.stripOffsets[0]:value;
					if (count>1 && (((long)fi.stripOffsets[count-1])&0xffffffffL)<(((long)fi.stripOffsets[0])&0xffffffffL))
						fi.offset = fi.stripOffsets[count-1];
					break;
				case STRIP_BYTE_COUNT:
					if (count==1)
						fi.stripLengths = new int[] {value};
					else {
						long saveLoc = in.getLongFilePointer();
						in.seek(lvalue);
						fi.stripLengths = new int[count];
						for (int c=0; c<count; c++) {
							if (fieldType==SHORT)
								fi.stripLengths[c] = getShort();
							else
								fi.stripLengths[c] = getInt();
						}
						in.seek(saveLoc);
					}
					break;
 				case PHOTO_INTERP:
 					photoInterp = value;
 					fi.whiteIsZero = value==0;
					break;
				case BITS_PER_SAMPLE:
						if (count==1) {
							if (value==8)
								fi.fileType = FileInfo.GRAY8;
							else if (value==16)
								fi.fileType = FileInfo.GRAY16_UNSIGNED;
							else if (value==32)
								fi.fileType = FileInfo.GRAY32_INT;
							else if (value==12)
								fi.fileType = FileInfo.GRAY12_UNSIGNED;
							else if (value==1)
								fi.fileType = FileInfo.BITMAP;
							else
								error("Unsupported BitsPerSample: " + value);
						} else if (count>1) {
							long saveLoc = in.getLongFilePointer();
							in.seek(lvalue);
							int bitDepth = getShort();
							if (bitDepth==8)
								fi.fileType = FileInfo.GRAY8;
							else if (bitDepth==16)
								fi.fileType = FileInfo.GRAY16_UNSIGNED;
							else
								error("ImageJ can only open 8 and 16 bit/channel images ("+bitDepth+")");
							in.seek(saveLoc);
						}
						break;
				case SAMPLES_PER_PIXEL:
					fi.samplesPerPixel = value;
					if (value==3 && fi.fileType==FileInfo.GRAY8)
						fi.fileType = FileInfo.RGB;
					else if (value==3 && fi.fileType==FileInfo.GRAY16_UNSIGNED)
						fi.fileType = FileInfo.RGB48;
					else if (value==4 && fi.fileType==FileInfo.GRAY8)
						fi.fileType = photoInterp==5?FileInfo.CMYK:FileInfo.ARGB;
					else if (value==4 && fi.fileType==FileInfo.GRAY16_UNSIGNED) {
						fi.fileType = FileInfo.RGB48;
						if (photoInterp==5)  //assume cmyk
							fi.whiteIsZero = true;
					}
					break;
				case ROWS_PER_STRIP:
					fi.rowsPerStrip = value;
					break;
				case X_RESOLUTION:
					double xScale = getRational(lvalue); 
					if (xScale!=0.0) fi.pixelWidth = 1.0/xScale; 
					break;
				case Y_RESOLUTION:
					double yScale = getRational(lvalue); 
					if (yScale!=0.0) fi.pixelHeight = 1.0/yScale; 
					break;
				case RESOLUTION_UNIT:
					if (value==1&&fi.unit==null)
						fi.unit = " ";
					else if (value==2) {
						if (fi.pixelWidth==1.0/72.0) {
							fi.pixelWidth = 1.0;
							fi.pixelHeight = 1.0;
						} else
							fi.unit = "inch";
					} else if (value==3)
						fi.unit = "cm";
					break;
				case PLANAR_CONFIGURATION:  // 1=chunky, 2=planar
					if (value==2 && fi.fileType==FileInfo.RGB48)
							 fi.fileType = FileInfo.RGB48_PLANAR;
					else if (value==2 && fi.fileType==FileInfo.RGB)
						fi.fileType = FileInfo.RGB_PLANAR;
					else if (value!=2 && !(fi.samplesPerPixel==1||fi.samplesPerPixel==3||fi.samplesPerPixel==4)) {
						String msg = "Unsupported SamplesPerPixel: " + fi.samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value==5)  {// LZW compression
						fi.compression = FileInfo.LZW;
						if (fi.fileType==FileInfo.GRAY12_UNSIGNED)
							error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
					} else if (value==32773)  // PackBits compression
						fi.compression = FileInfo.PACK_BITS;
					else if (value==32946 || value==8)
						fi.compression = FileInfo.ZIP;
					else if (value!=1 && value!=0 && !(value==7&&fi.width<500)) {
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						fi.compression = FileInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " +
							"compressed in this fashion ("+value+")");
					}
					break;
				case SOFTWARE: case DATE_TIME: case HOST_COMPUTER: case ARTEST:
					if (ifdCount==1) {
						byte[] bytes = getString(count, lvalue);
						String s = bytes!=null?new String(bytes):null;
						saveMetadata(getName(tag), s);
					}
					break;
				case PREDICTOR:
					if (value==2 && fi.compression==FileInfo.LZW)
						fi.compression = FileInfo.LZW_WITH_DIFFERENCING;
					break;
				case COLOR_MAP: 
					if (count==768)
						getColorMap(lvalue, fi);
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs");
					break;
				case SAMPLE_FORMAT:
					if (fi.fileType==FileInfo.GRAY32_INT && value==FLOATING_POINT)
						fi.fileType = FileInfo.GRAY32_FLOAT;
					if (fi.fileType==FileInfo.GRAY16_UNSIGNED) {
						if (value==SIGNED)
							fi.fileType = FileInfo.GRAY16_SIGNED;
						if (value==FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (fi.compression==FileInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
				case IMAGE_DESCRIPTION: 
					if (ifdCount==1) {
						byte[] s = getString(count, lvalue);
						if (s!=null) saveImageDescription(s,fi);
					}
					break;
				case ORIENTATION:
					fi.nImages = 0; // file not created by ImageJ so look at all the IFDs
					break;
				case METAMORPH1: case METAMORPH2:
					if ((name.indexOf(".STK")>0||name.indexOf(".stk")>0) && fi.compression==FileInfo.COMPRESSION_NONE) {
						if (tag==METAMORPH2)
							fi.nImages=count;
						else
							fi.nImages=9999;
					}
					break;
				case IPLAB: 
					fi.nImages=value;
					break;
				case NIH_IMAGE_HDR: 
					if (count==256)
						decodeNIHImageHeader(value, fi);
					break;
 				case META_DATA_BYTE_COUNTS: 
					long saveLoc = in.getLongFilePointer();
					in.seek(lvalue);
					metaDataCounts = new int[count];
					for (int c=0; c<count; c++)
						metaDataCounts[c] = getInt();
					in.seek(saveLoc);
					break;
 				case META_DATA: 
 					getMetaData(value, fi);
 					break;
				default:
					if (tag>10000 && tag<32768 && ifdCount>1)
						return null;
			}
		}
		fi.fileFormat = fi.TIFF;
		fi.fileName = name;
		fi.directory = directory;
		if (url!=null)
			fi.url = url;
		return fi;
	}

	/**
	 * Gets the meta data.
	 *
	 * @param loc the loc
	 * @param fi the fi
	 * @return the meta data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getMetaData(int loc, FileInfo fi) throws IOException {
		if (metaDataCounts==null || metaDataCounts.length==0)
			return;
		int maxTypes = 10;
		long saveLoc = in.getLongFilePointer();
		in.seek(loc);
		int n = metaDataCounts.length;
		int hdrSize = metaDataCounts[0];
		if (hdrSize<12 || hdrSize>804)
			{in.seek(saveLoc); return;}
		int magicNumber = getInt();
		if (magicNumber!=MAGIC_NUMBER)  // "IJIJ"
			{in.seek(saveLoc); return;}
		int nTypes = (hdrSize-4)/8;
		int[] types = new int[nTypes];
		int[] counts = new int[nTypes];
		
		if (debugMode) dInfo += "Metadata:\n";
		int extraMetaDataEntries = 0;
		for (int i=0; i<nTypes; i++) {
			types[i] = getInt();
			counts[i] = getInt();
			if (types[i]<0xffffff)
				extraMetaDataEntries += counts[i];
			if (debugMode) {
				String id = "";
				if (types[i]==INFO) id = " (Info property)";
				if (types[i]==LABELS) id = " (slice labels)";
				if (types[i]==RANGES) id = " (display ranges)";
				if (types[i]==LUTS) id = " (luts)";
				if (types[i]==ROI) id = " (roi)";
				if (types[i]==OVERLAY) id = " (overlay)";
				dInfo += "   "+i+" "+Integer.toHexString(types[i])+" "+counts[i]+id+"\n";
			}
		}
		fi.metaDataTypes = new int[extraMetaDataEntries];
		fi.metaData = new byte[extraMetaDataEntries][];
		int start = 1;
		int eMDindex = 0;
		for (int i=0; i<nTypes; i++) {
			if (types[i]==INFO)
				getInfoProperty(start, fi);
			else if (types[i]==LABELS)
				getSliceLabels(start, start+counts[i]-1, fi);
			else if (types[i]==RANGES)
				getDisplayRanges(start, fi);
			else if (types[i]==LUTS)
				getLuts(start, start+counts[i]-1, fi);
			else if (types[i]==ROI)
				getRoi(start, fi);
			else if (types[i]==OVERLAY)
				getOverlay(start, start+counts[i]-1, fi);
			else if (types[i]<0xffffff) {
				for (int j=start; j<start+counts[i]; j++) { 
					int len = metaDataCounts[j]; 
					fi.metaData[eMDindex] = new byte[len]; 
					in.readFully(fi.metaData[eMDindex], len); 
					fi.metaDataTypes[eMDindex] = types[i]; 
					eMDindex++; 
				} 
			} else
				skipUnknownType(start, start+counts[i]-1);
			start += counts[i];
		}
		in.seek(saveLoc);
	}

	/**
	 * Gets the info property.
	 *
	 * @param first the first
	 * @param fi the fi
	 * @return the info property
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getInfoProperty(int first, FileInfo fi) throws IOException {
		int len = metaDataCounts[first];
	    byte[] buffer = new byte[len];
		in.readFully(buffer, len);
		len /= 2;
		char[] chars = new char[len];
		if (littleEndian) {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
		} else {
			for (int j=0, k=0; j<len; j++)
				chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
		}
		fi.info = new String(chars);
	}

	/**
	 * Gets the slice labels.
	 *
	 * @param first the first
	 * @param last the last
	 * @param fi the fi
	 * @return the slice labels
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getSliceLabels(int first, int last, FileInfo fi) throws IOException {
		fi.sliceLabels = new String[last-first+1];
	    int index = 0;
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			if (len>0) {
				if (len>buffer.length)
					buffer = new byte[len];
				in.readFully(buffer, len);
				len /= 2;
				char[] chars = new char[len];
				if (littleEndian) {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(buffer[k++]&255 + ((buffer[k++]&255)<<8));
				} else {
					for (int j=0, k=0; j<len; j++)
						chars[j] = (char)(((buffer[k++]&255)<<8) + buffer[k++]&255);
				}
				fi.sliceLabels[index++] = new String(chars);
				//ij.IJ.log(i+"  "+fi.sliceLabels[i-1]+"  "+len);
			} else
				fi.sliceLabels[index++] = null;
		}
	}

	/**
	 * Gets the display ranges.
	 *
	 * @param first the first
	 * @param fi the fi
	 * @return the display ranges
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getDisplayRanges(int first, FileInfo fi) throws IOException {
		int n = metaDataCounts[first]/8;
		fi.displayRanges = new double[n];
		for (int i=0; i<n; i++)
			fi.displayRanges[i] = readDouble();
	}

	/**
	 * Gets the luts.
	 *
	 * @param first the first
	 * @param last the last
	 * @param fi the fi
	 * @return the luts
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getLuts(int first, int last, FileInfo fi) throws IOException {
		fi.channelLuts = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.channelLuts[index] = new byte[len];
            in.readFully(fi.channelLuts[index], len);
            index++;
		}
	}

	/**
	 * Gets the roi.
	 *
	 * @param first the first
	 * @param fi the fi
	 * @return the roi
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getRoi(int first, FileInfo fi) throws IOException {
		int len = metaDataCounts[first];
		fi.roi = new byte[len]; 
		in.readFully(fi.roi, len); 
	}

	/**
	 * Gets the overlay.
	 *
	 * @param first the first
	 * @param last the last
	 * @param fi the fi
	 * @return the overlay
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void getOverlay(int first, int last, FileInfo fi) throws IOException {
		fi.overlay = new byte[last-first+1][];
	    int index = 0;
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
			fi.overlay[index] = new byte[len];
            in.readFully(fi.overlay[index], len);
            index++;
		}
	}

	/**
	 * Error.
	 *
	 * @param message the message
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void error(String message) throws IOException {
		if (in!=null) in.close();
		throw new IOException(message);
	}
	
	/**
	 * Skip unknown type.
	 *
	 * @param first the first
	 * @param last the last
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void skipUnknownType(int first, int last) throws IOException {
	    byte[] buffer = new byte[metaDataCounts[first]];
		for (int i=first; i<=last; i++) {
			int len = metaDataCounts[i];
            if (len>buffer.length)
                buffer = new byte[len];
            in.readFully(buffer, len);
		}
	}

	/**
	 * Enable debugging.
	 */
	public void enableDebugging() {
		debugMode = true;
	}
		
	/**
	 * Gets the tiff info.
	 *
	 * @return the tiff info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public FileInfo[] getTiffInfo() throws IOException {
		long ifdOffset;
		ArrayList list = new ArrayList();
		if (in==null)
			in = new RandomAccessStream(new RandomAccessFile(new File(directory, name), "r"));
		ifdOffset = OpenImageFileHeader();
		if (ifdOffset<0L) {
			in.close();
			return null;
		}
		if (debugMode) dInfo = "\n  " + name + ": opening\n";
		while (ifdOffset>0L) {
			in.seek(ifdOffset);
			FileInfo fi = OpenIFD();
			if (fi!=null) {
				list.add(fi);
				ifdOffset = ((long)getInt())&0xffffffffL;
			} else
				ifdOffset = 0L;
			if (debugMode && ifdCount<10) dInfo += "  nextIFD=" + ifdOffset + "\n";
			if (fi!=null) {
				if (fi.nImages>1) // ignore extra IFDs in ImageJ and NIH Image stacks
					ifdOffset = 0L;
			}
		}
		if (list.size()==0) {
			in.close();
			return null;
		} else {
			FileInfo[] info = (FileInfo[])list.toArray(new FileInfo[list.size()]);
			if (debugMode) info[0].debugInfo = dInfo;
			if (url!=null) {
				in.seek(0);
				info[0].inputStream = in;
			} else
				in.close();
			if (info[0].info==null)
				info[0].info = tiffMetadata;
			FileInfo fi = info[0];
			if (fi.fileType==FileInfo.GRAY16_UNSIGNED && fi.description==null)
				fi.lutSize = 0; // ignore troublesome non-ImageJ 16-bit LUTs
			if (debugMode) {
				int n = info.length;
				fi.debugInfo += "number of IFDs: "+ n + "\n";
				fi.debugInfo += "offset to first image: "+fi.getOffset()+ "\n";
				fi.debugInfo += "gap between images: "+getGapInfo(info) + "\n";
				fi.debugInfo += "little-endian byte order: "+fi.intelByteOrder + "\n";
			}
			return info;
		}
	}
	
	/**
	 * Gets the gap info.
	 *
	 * @param fi the fi
	 * @return the gap info
	 */
	String getGapInfo(FileInfo[] fi) {
		if (fi.length<2) return "0";
		long minGap = Long.MAX_VALUE;
		long maxGap = -Long.MAX_VALUE;
		for (int i=1; i<fi.length; i++) {
			long gap = fi[i].getOffset()-fi[i-1].getOffset();
			if (gap<minGap) minGap = gap;
			if (gap>maxGap) maxGap = gap;
		}
		long imageSize = fi[0].width*fi[0].height*fi[0].getBytesPerPixel();
		minGap -= imageSize;
		maxGap -= imageSize;
		if (minGap==maxGap)
			return ""+minGap;
		else 
			return "varies ("+minGap+" to "+maxGap+")";
	}

}
