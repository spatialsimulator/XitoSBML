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
package ij.measure;
import ij.ImagePlus;
import ij.plugin.filter.Analyzer;

// TODO: Auto-generated Javadoc
/** Calibration objects contain an image's spatial and density calibration data. */
   
public class Calibration implements Cloneable {

	/** The Constant EXP_RECOVERY. */
	public static final int STRAIGHT_LINE=0,POLY2=1,POLY3=2,POLY4=3,
		EXPONENTIAL=4,POWER=5,LOG=6,RODBARD=7,GAMMA_VARIATE=8, LOG2=9, RODBARD2=10,
		EXP_WITH_OFFSET=11, GAUSSIAN=12, EXP_RECOVERY=13;
	
	/** The Constant CUSTOM. */
	public static final int NONE=20, UNCALIBRATED_OD=21, CUSTOM=22;
	
	/** The Constant DEFAULT_VALUE_UNIT. */
	public static final String DEFAULT_VALUE_UNIT = "Gray Value";
	
	/** The Constant UNKNOWN. */
	private static final int UNKNOWN = 0;

	/**  Pixel width in 'unit's. */
	public double pixelWidth = 1.0;
	
	/**  Pixel height in 'unit's. */
	public double pixelHeight = 1.0;
	
	/**  Pixel depth in 'unit's. */
	public double pixelDepth = 1.0;
	
	/**  Frame interval in 'timeUnit's. */
	public double frameInterval;

	/**  Frame rate in frames per second. */
	public double fps;

	/**  Loop back and forth when animating stack. */
	private static boolean loopBackAndForth;
	
	/** The loop. */
	public boolean loop = loopBackAndForth;

	/** X origin in pixels. */
	public double xOrigin;

	/** Y origin in pixels. */
	public double yOrigin;

	/** Z origin in pixels. */
	public double zOrigin;

	/** Plugin writers can use this string to store information about the
		image. This string is saved in the TIFF header if it is not longer
		than 64 characters and it contains no '=' or '\n' characters. */
	public String info;

	/**  Calibration function coefficients. */
	private double[] coefficients;
		
	/** The unit. */
	/* Default distance unit (e.g. 'cm', 'inch') */
	private String unit = "pixel";
	
	/** The yunit. */
	/* Y distance unit */
	private String yunit;

	/** The zunit. */
	/* Z distance unit */
	private String zunit;

	/** The units. */
	/* Distance units (e.g. 'microns', 'inches') */
	private String units;

	/** The value unit. */
	/* Pixel value unit (e.g. 'gray level', 'OD') */
	private String valueUnit = DEFAULT_VALUE_UNIT;

	/** The time unit. */
	/* Unit of time (e.g. 'sec', 'msec') */
	private String timeUnit = "sec";

	/** The function. */
	/* Calibration function ID */
	private int function = NONE;

	/** The c table. */
	/* Calibration table */
	private float[] cTable;
	
	/** The inverted lut. */
	private boolean invertedLut;
	
	/** The bit depth. */
	private int bitDepth = UNKNOWN;
	
	/** The zero clip. */
	private boolean zeroClip;
	
	/** The invert Y. */
	private boolean invertY;

	/**
	 *  Constructs a new Calibration object using the default values.
	 *
	 * @param imp the imp
	 */ 
	public Calibration(ImagePlus imp) {
		if (imp!=null) {
			bitDepth = imp.getBitDepth();
			invertedLut = imp.isInvertedLut();
		}
	}
	
	/** Constructs a new Calibration object using the default values.
		For density calibration, the image is assumed to be 8-bits. */ 
	public Calibration() {
	}
	
	/**
	 *  Returns true if this image is spatially calibrated.
	 *
	 * @return true, if successful
	 */
	public boolean scaled() {
		return pixelWidth!=1.0 || pixelHeight!=1.0 || pixelDepth!=1.0 || !unit.equals("pixel");
	}
	
   	/**
	    *  Sets the default length unit (e.g. "mm", "inch").
	    *
	    * @param unit the new unit
	    */
 	public void setUnit(String unit) {
 		if (unit==null || unit.equals(""))
 			this.unit = "pixel";
 		else {
 			if (unit.equals("um")) unit = "\u00B5m";
 			this.unit = unit;
 		}
 		units = null;
 	}
 	
   	/**
	    *  Sets the X length unit.
	    *
	    * @param unit the new x unit
	    */
 	public void setXUnit(String unit) {
		setUnit(unit);
	}

   	/**
	    *  Sets the Y length unit.
	    *
	    * @param unit the new y unit
	    */
 	public void setYUnit(String unit) {
 		yunit = unit;
	}

   	/**
	    *  Sets the Z length unit.
	    *
	    * @param unit the new z unit
	    */
 	public void setZUnit(String unit) {
 		zunit = unit;
	}

 	/**
	  *  Returns the default length unit (e.g. "micron", "inch").
	  *
	  * @return the unit
	  */
 	public String getUnit() {
 		return unit;
 	}
 	
 	/**
	  *  Returns the X length unit.
	  *
	  * @return the x unit
	  */
 	public String getXUnit() {
 		return unit;
 	}

 	/**
	  *  Returns the Y length unit, or the default unit if 'yunit' is null.
	  *
	  * @return the y unit
	  */
 	public String getYUnit() {
 		return yunit!=null?yunit:unit;
 	}

 	/**
	  *  Returns the Z length unit, or the default unit if 'zunit' is null.
	  *
	  * @return the z unit
	  */
 	public String getZUnit() {
 		return zunit!=null?zunit:unit;
 	}

	/**
	 *  Returns the plural form of the length unit (e.g. "microns", "inches").
	 *
	 * @return the units
	 */
 	public String getUnits() {
 		if (units==null) {
  			if (unit.equals("pixel"))
 				units = "pixels";
 			else if (unit.equals("micron"))
 				units = "microns";
  			else if (unit.equals("inch"))
 				units = "inches";
			else
 				units = unit;
 		}
 		return units;
 	}
 	
   	/**
	    *  Sets the time unit (e.g. "sec", "msec").
	    *
	    * @param unit the new time unit
	    */
 	public void setTimeUnit(String unit) {
 		if (unit==null || unit.equals(""))
 			timeUnit = "sec";
 		else
 			timeUnit = unit;
 	}

 	/**
	  *  Returns the distance unit (e.g. "sec", "msec").
	  *
	  * @return the time unit
	  */
 	public String getTimeUnit() {
 		return timeUnit;
 	}

 	/**
	  *  Converts a x-coodinate in pixels to physical units (e.g. mm).
	  *
	  * @param x the x
	  * @return the x
	  */
 	public double getX(double x) {
 		return (x-xOrigin)*pixelWidth;
 	}
 	
  	/**
	   *  Converts a y-coordinate in pixels to physical units (e.g. mm).
	   *
	   * @param y the y
	   * @return the y
	   */
 	public double getY(double y) {
 		return (y-yOrigin)*pixelHeight;
 	}
 	
 	/**
	  *  Converts a y-coordinate in pixels to physical units (e.g. mm),
	  *  		taking into account the invertY and global "Invert Y Coordinates" flags.
	  *
	  * @param y the y
	  * @param imageHeight the image height
	  * @return the y
	  */
 	public double getY(double y, int imageHeight) {
 		if (invertY || (Analyzer.getMeasurements()&Measurements.INVERT_Y)!=0) {
			if (yOrigin!=0.0)
				return (yOrigin-y)*pixelHeight;
			else
				return (imageHeight-y-1)*pixelHeight;
		} else
   			return (y-yOrigin)*pixelHeight;
	}

  	/**
	   *  Converts a z-coordinate in pixels to physical units (e.g. mm).
	   *
	   * @param z the z
	   * @return the z
	   */
 	public double getZ(double z) {
 		return (z-zOrigin)*pixelDepth;
 	}
 	
 	/**
	  *  Converts a x-coordinate in physical units to pixels.
	  *
	  * @param x the x
	  * @return the raw X
	  */
 	public double getRawX(double x) {
 		return x/pixelWidth + xOrigin;
 	}
 	
   	/**
	    *  Converts a y-coodinate in physical units to pixels.
	    *
	    * @param y the y
	    * @return the raw Y
	    */
 	public double getRawY(double y) {
  		return y/pixelHeight + yOrigin;
	}
 	
 	/**
	  *  Converts a y-coodinate in physical units to pixels,
	  *  		taking into account the 'invertY' flag.
	  *
	  * @param y the y
	  * @param imageHeight the image height
	  * @return the raw Y
	  */
 	public double getRawY(double y, int imageHeight) {
 		if (invertY || (Analyzer.getMeasurements()&Measurements.INVERT_Y)!=0) {
			if (yOrigin!=0.0)
				return yOrigin-y/pixelHeight;
			else
				return imageHeight -y/pixelHeight - 1;
		} else
   			return y/pixelHeight + yOrigin;
	}

	//public double getX(int x) {return getX((double)x);}
 	//public double getY(int y) {return getY((double)y);}
 	//public double getZ(int z) {return getZ((double)z);}
 	
  	/**
	 *  Sets the calibration function,  coefficient table and unit (e.g. "OD").
	 *
	 * @param function the function
	 * @param coefficients the coefficients
	 * @param unit the unit
	 */
 	public void setFunction(int function, double[] coefficients, String unit) {
 		setFunction(function, coefficients, unit, false);
 	}
 	
 	/**
	  * Sets the function.
	  *
	  * @param function the function
	  * @param coefficients the coefficients
	  * @param unit the unit
	  * @param zeroClip the zero clip
	  */
	 public void setFunction(int function, double[] coefficients, String unit, boolean zeroClip) {
 		if (function==NONE)
 			{disableDensityCalibration(); return;}
 		if (coefficients==null && function>=STRAIGHT_LINE && function<=EXP_RECOVERY)
 			return;
 		this.function = function;
 		this.coefficients = coefficients;
 		this.zeroClip = zeroClip;
 		if (unit!=null)
 			valueUnit = unit;
 		cTable = null;
 	}

 	/**
	  *  Disables the density calibation if the specified image has a differenent bit depth.
	  *
	  * @param imp the new image
	  */
 	public void setImage(ImagePlus imp) {
 		if (imp==null)
 			return;
 		int type = imp.getType();
 		int newBitDepth = imp.getBitDepth();
 		if (newBitDepth==16 && imp.getLocalCalibration().isSigned16Bit()) {
			double[] coeff = new double[2]; coeff[0] = -32768.0; coeff[1] = 1.0;
 			setFunction(Calibration.STRAIGHT_LINE, coeff, DEFAULT_VALUE_UNIT);
		} else if ((newBitDepth!=bitDepth&&bitDepth!=UNKNOWN) || type==ImagePlus.GRAY32 || type==ImagePlus.COLOR_RGB) {
			String saveUnit = valueUnit;
			disableDensityCalibration();
			if (type==ImagePlus.GRAY32) valueUnit = saveUnit;
		}
 		bitDepth = newBitDepth;
 	}
 	
 	/**
	  * Disable density calibration.
	  */
	 public void disableDensityCalibration() {
		function = NONE;
		coefficients = null;
		cTable = null;
		valueUnit = DEFAULT_VALUE_UNIT;
 	}
 	
	/**
	 *  Returns the value unit.
	 *
	 * @return the value unit
	 */
 	public String getValueUnit() {
 		return valueUnit;
 	}
 	
	/**
	 *  Sets the value unit.
	 *
	 * @param unit the new value unit
	 */
 	public void setValueUnit(String unit) {
 		if (unit!=null)
 			valueUnit = unit;
 	}

 	/**
	  *  Returns the calibration function coefficients.
	  *
	  * @return the coefficients
	  */
 	public double[] getCoefficients() {
 		return coefficients;
 	}

 	/**
	  *  Returns true if this image is density calibrated.
	  *
	  * @return true, if successful
	  */
	public boolean calibrated() {
		return function!=NONE;
	}
	
	/**
	 *  Returns the calibration function ID.
	 *
	 * @return the function
	 */
 	public int getFunction() {
 		return function;
 	}
 	
	/**
	 *  Returns the calibration table. With 8-bit images,
	 * 		the table has a length of 256. With 16-bit images,
	 * 		the length is 65536.
	 *
	 * @return the c table
	 */
 	public float[] getCTable() {
 		if (cTable==null)
 			makeCTable();
 		return cTable;
 	}
 	
	/**
	 *  Sets the calibration table. With 8-bit images, the table must 
	 * 		have a length of 256. With 16-bit images, it must be 65536.
	 *
	 * @param table the table
	 * @param unit the unit
	 */
 	public void setCTable(float[] table, String unit) {
 		if (table==null) {
 			disableDensityCalibration();
 			return;
 		}
 		if (bitDepth==UNKNOWN) {
 			if (table.length==256)
 				bitDepth = 8;
 			else if (table.length==65536)
 				bitDepth = 16;
 		}
 		if (bitDepth==16 && table.length!=65536)
 			throw new IllegalArgumentException("Table.length!=65536");
 		cTable = table;
 		function = CUSTOM;
 		coefficients = null;
 		zeroClip = false;
 		if (unit!=null) valueUnit = unit;
 	}

 	/**
	  * Make C table.
	  */
	 void makeCTable() {
 		if (bitDepth==16)
 			{make16BitCTable(); return;}
 		if (bitDepth==UNKNOWN)
 			bitDepth = 8;
 		if (bitDepth!=8)
 			return;
 		if (function==UNCALIBRATED_OD) {
 			cTable = new float[256];
			for (int i=0; i<256; i++)
				cTable[i] = (float)od(i);
		} else if (function>=STRAIGHT_LINE && function<=EXP_RECOVERY && coefficients!=null) {
 			cTable = new float[256];
 			double value;
 			for (int i=0; i<256; i++) {
				value = CurveFitter.f(function, coefficients, i);
				if (zeroClip && value<0.0)
					cTable[i] = 0f;
				else
					cTable[i] = (float)value;
			}
		} else
 			cTable = null;
  	}

 	/**
	  * Make 16 bit C table.
	  */
	 void make16BitCTable() {
		if (function>=STRAIGHT_LINE && function<=EXP_RECOVERY && coefficients!=null) {
 			cTable = new float[65536];
 			for (int i=0; i<65536; i++)
				cTable[i] = (float)CurveFitter.f(function, coefficients, i);
		} else
 			cTable = null;
  	}

	/**
	 * Od.
	 *
	 * @param v the v
	 * @return the double
	 */
	double od(double v) {
		if (invertedLut) {
			if (v==255.0) v = 254.5;
			return 0.434294481*Math.log(255.0/(255.0-v));
		} else {
			if (v==0.0) v = 0.5;
			return 0.434294481*Math.log(255.0/v);
		}
	}
	
  	/**
	   *  Converts a raw pixel value to a density calibrated value.
	   *
	   * @param value the value
	   * @return the c value
	   */
 	public double getCValue(int value) {
		if (function==NONE)
			return value;
		if (function>=STRAIGHT_LINE && function<=EXP_RECOVERY && coefficients!=null) {
			double v = CurveFitter.f(function, coefficients, value);
			if (zeroClip && v<0.0)
				return 0.0;
			else
				return v;
		}
		if (cTable==null)
			makeCTable();
 		if (cTable!=null && value>=0 && value<cTable.length)
 			return cTable[value];
 		else
 			return value;
 	}
 	 	
  	/**
	   *  Converts a raw pixel value to a density calibrated value.
	   *
	   * @param value the value
	   * @return the c value
	   */
 	public double getCValue(double value) {
		if (function==NONE)
			return value;
		else {
			if (function>=STRAIGHT_LINE && function<=EXP_RECOVERY && coefficients!=null) {
				double 	v = CurveFitter.f(function, coefficients, value);
				if (zeroClip && v<0.0)
					return 0.0;
				else
					return v;
			} else
				return getCValue((int)value);
		}
 	}
 	
  	/**
	   *  Converts a density calibrated value into a raw pixel value.
	   *
	   * @param value the value
	   * @return the raw value
	   */
 	public double getRawValue(double value) {
		if (function==NONE)
			return value;
		if (function==STRAIGHT_LINE && coefficients!=null && coefficients.length==2 && coefficients[1]!=0.0)
			return (value-coefficients[0])/coefficients[1];
		if (cTable==null)
			makeCTable();
		float fvalue = (float)value;
		float smallestDiff = Float.MAX_VALUE;
		float diff;
		int index = 0;
		for (int i=0; i<cTable.length; i++) {
			diff = fvalue - cTable[i];
			if (diff<0f) diff = -diff;
			if (diff<smallestDiff) {
				smallestDiff = diff;
				index = i;
			}
		}
 		return index;
 	}
 	 	
	/**
	 *  Returns a clone of this object.
	 *
	 * @return the calibration
	 */
	public Calibration copy() {
		return (Calibration)clone();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}

	/**
	 *  Compares two Calibration objects for equality.
	 *
	 * @param cal the cal
	 * @return true, if successful
	 */
 	public boolean equals(Calibration cal) {
 		if (cal==null)
 			return false;
 		boolean equal = true;
 		if (cal.pixelWidth!=pixelWidth || cal.pixelHeight!=pixelHeight || cal.pixelDepth!=pixelDepth)
 			equal = false;
 		if (!cal.unit.equals(unit))
 			equal = false;
 		if (!cal.valueUnit.equals(valueUnit) || cal.function!=function)
 			equal = false;
 		return equal;
 	}
 	
  	/**
	   *  Returns true if this is a signed 16-bit image.
	   *
	   * @return true, if is signed 16 bit
	   */
 	public boolean isSigned16Bit() {
		return (bitDepth==16 && function>=STRAIGHT_LINE && function<=EXP_RECOVERY && coefficients!=null
			&& coefficients[0]==-32768.0 && coefficients[1]==1.0);
 	}
 	
 	/** Sets up a calibration function that subtracts 32,768 from pixel values. */
 	public void setSigned16BitCalibration() {
		double[] coeff = new double[2];
		coeff[0] = -32768.0;
		coeff[1] = 1.0;
		setFunction(STRAIGHT_LINE, coeff, "Gray Value");
 	}

 	/**
	  *  Returns true if zero clipping is enabled.
	  *
	  * @return true, if successful
	  */
 	public boolean zeroClip() {
 		return zeroClip;
 	}
 	
 	/**
	  *  Sets the 'invertY' flag.
	  *
	  * @param invertYCoordinates the new invert Y
	  */
 	public void setInvertY(boolean invertYCoordinates) {
 		invertY = invertYCoordinates;
 	}
 	
 	/**
	  *  Set the default state of the animation "Loop back and forth" flag.
	  *
	  * @param loop the new loop back and forth
	  */
	public static void setLoopBackAndForth(boolean loop) {
 		loopBackAndForth = loop;
 	}
 	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return
    		"w=" + pixelWidth
			+ ", h=" + pixelHeight
			+ ", d=" + pixelDepth
			+ ", unit=" + unit
			+ ", f=" + function
 			+ ", nc=" + (coefficients!=null?""+coefficients.length:"null")
 			+ ", table=" + (cTable!=null?""+cTable.length:"null")
			+ ", vunit=" + valueUnit;
   }
}

