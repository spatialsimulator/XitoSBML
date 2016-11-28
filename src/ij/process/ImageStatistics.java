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
package ij.process;
import ij.measure.Calibration;
import ij.measure.Measurements;

import java.awt.Rectangle;

// TODO: Auto-generated Javadoc
/** Statistics, including the histogram, of an image or selection. */
public class ImageStatistics implements Measurements {

	/** The histogram. */
	public int[] histogram;
	
	/** The pixel count. */
	public int pixelCount;
	
	/** The long pixel count. */
	public long longPixelCount;
	
	/** The mode. */
	public int mode;
	
	/** The dmode. */
	public double dmode;
	
	/** The area. */
	public double area;
	
	/** The min. */
	public double min;
	
	/** The max. */
	public double max;
	
	/** The mean. */
	public double mean;
	
	/** The median. */
	public double median;
	
	/** The std dev. */
	public double stdDev;
	
	/** The skewness. */
	public double skewness;
	
	/** The kurtosis. */
	public double kurtosis;
	
	/** The x centroid. */
	public double xCentroid;
	
	/** The y centroid. */
	public double yCentroid;
	
	/** The x center of mass. */
	public double xCenterOfMass;
	
	/** The y center of mass. */
	public double yCenterOfMass;
	
	/** The roi height. */
	public double roiX, roiY, roiWidth, roiHeight;
	
	/**  Uncalibrated mean. */
	public double umean;
	
	/**  Length of major axis of fitted ellipse. */
	public double major;
	
	/**  Length of minor axis of fitted ellipse. */
	public double minor;
	
	/**  Angle in degrees of fitted ellipse. */
	public double angle;
	
	/**  Bin width 1 histogram of 16-bit images. */
	public int[] histogram16;
	/** Long histogram; use getHIstogram() to retrieve. */
	protected long[] longHistogram;
	
	/** The area fraction. */
	public double areaFraction;
	
	/**  Used internally by AnalyzeParticles. */
	public int xstart, ystart;
	
	/**  Used by HistogramWindow. */
	public boolean stackStatistics;
	
	/** The hist min. */
	public double histMin;
	
	/** The hist max. */
	public double histMax;
	
	/** The hist Y max. */
	public int histYMax;
	
	/** The max count. */
	public int maxCount;
	
	/** The n bins. */
	public int nBins = 256;
	
	/** The bin size. */
	public double binSize = 1.0;
	
	/** The height. */
	protected int width, height;
	
	/** The rh. */
	protected int rx, ry, rw, rh;
	
	/** The ph. */
	protected double pw, ph;
	
	/** The cal. */
	protected Calibration cal;
	
	/** The ef. */
	EllipseFitter ef;

	
	/**
	 * Gets the statistics.
	 *
	 * @param ip the ip
	 * @param mOptions the m options
	 * @param cal the cal
	 * @return the statistics
	 */
	public static ImageStatistics getStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
		Object pixels = ip.getPixels();
		if (pixels instanceof byte[])
			return new ByteStatistics(ip, mOptions, cal);
		else if (pixels instanceof short[])
			return new ShortStatistics(ip, mOptions, cal);
		else if (pixels instanceof int[])
			return new ColorStatistics(ip, mOptions, cal);
		else if (pixels instanceof float[])
			return new FloatStatistics(ip, mOptions, cal);
		else
			throw new IllegalArgumentException("Pixels are not byte, short, int or float");
	}

	/**
	 * Gets the raw min and max.
	 *
	 * @param minThreshold the min threshold
	 * @param maxThreshold the max threshold
	 * @return the raw min and max
	 */
	void getRawMinAndMax(int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((histogram[min] == 0) && (min < 255))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((histogram[max] == 0) && (max > 0))
			max--;
		this.max = max;
	}

	/**
	 * Gets the raw statistics.
	 *
	 * @param minThreshold the min threshold
	 * @param maxThreshold the max threshold
	 * @return the raw statistics
	 */
	void getRawStatistics(int minThreshold, int maxThreshold) {
		int count;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		
		for (int i=minThreshold; i<=maxThreshold; i++) {
			count = histogram[i];
			longPixelCount += count;
			sum += (double)i*count;
			value = i;
			sum2 += (value*value)*count;
			if (count>maxCount) {
				maxCount = count;
				mode = i;
			}
		}
		pixelCount = (int)longPixelCount;
		area = longPixelCount*pw*ph;
		mean = sum/longPixelCount;
		umean = mean;
		dmode = mode;
		calculateStdDev(longPixelCount, sum, sum2);
		histMin = 0.0;
		histMax = 255.0;
	}
	
	/**
	 * Calculate std dev.
	 *
	 * @param n the n
	 * @param sum the sum
	 * @param sum2 the sum 2
	 */
	void calculateStdDev(double n, double sum, double sum2) {
		if (n>0.0) {
			stdDev = (n*sum2-sum*sum)/n;
			if (stdDev>0.0)
				stdDev = Math.sqrt(stdDev/(n-1.0));
			else
				stdDev = 0.0;
		} else
			stdDev = 0.0;
	}
		
	/**
	 * Setup.
	 *
	 * @param ip the ip
	 * @param cal the cal
	 */
	void setup(ImageProcessor ip, Calibration cal) {
		width = ip.getWidth();
		height = ip.getHeight();
		this.cal = cal;
		Rectangle roi = ip.getRoi();
		if (roi != null) {
			rx = roi.x;
			ry = roi.y;
			rw = roi.width;
			rh = roi.height;
		}
		else {
			rx = 0;
			ry = 0;
			rw = width;
			rh = height;
		}
		
		if (cal!=null) {
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		} else {
			pw = 1.0;
			ph = 1.0;
		}
		
		roiX = cal!=null?cal.getX(rx):rx;
		roiY = cal!=null?cal.getY(ry, height):ry;
		roiWidth = rw*pw;
		roiHeight = rh*ph;
	}
	
	/**
	 * Gets the centroid.
	 *
	 * @param ip the ip
	 * @return the centroid
	 */
	void getCentroid(ImageProcessor ip) {
		byte[] mask = ip.getMaskArray();
		int count=0, mi;
		double xsum=0.0, ysum=0.0;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null||mask[mi++]!=0) {
					count++;
					xsum += x;
					ysum += y;
				}
			}
		}
		xCentroid = xsum/count+0.5;
		yCentroid = ysum/count+0.5;
		if (cal!=null) {
			xCentroid = cal.getX(xCentroid);
			yCentroid = cal.getY(yCentroid, height);
		}
	}
	
	/**
	 * Fit ellipse.
	 *
	 * @param ip the ip
	 * @param mOptions the m options
	 */
	void fitEllipse(ImageProcessor ip, int mOptions) {
		ImageProcessor originalMask = null;
		boolean limitToThreshold = (mOptions&LIMIT)!=0 && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD;
		if (limitToThreshold) {
			ImageProcessor mask = ip.getMask();
			Rectangle r = ip.getRoi();
			if (mask==null) {
				mask = new ByteProcessor(r.width, r.height);
				mask.invert();
			} else {
				originalMask = mask;
				mask = mask.duplicate();
			}
			int n = r.width*r.height;
			double t1 = ip.getMinThreshold();
			double t2 = ip.getMaxThreshold();
			double value;
			for (int y=0; y<r.height; y++) {
				for (int x=0; x<r.width; x++) {
					value = ip.getf(r.x+x, r.y+y);
					if (value<t1 || value>t2)
						mask.setf(x, y, 0f);
				}
			}
			ip.setMask(mask);
		}
		if (ef==null)
			ef = new EllipseFitter();
		ef.fit(ip, this);
		if (limitToThreshold) {
			if (originalMask==null)
				ip.setMask(null);
			else
				ip.setMask(originalMask);
		}
		double psize = (Math.abs(pw-ph)/pw)<.01?pw:0.0;
		major = ef.major*psize;
		minor = ef.minor*psize;
		angle = ef.angle;
		xCentroid = ef.xCenter;
		yCentroid = ef.yCenter;
		if (cal!=null) {
			xCentroid = cal.getX(xCentroid);
			yCentroid = cal.getY(yCentroid, height);
		}
	}
	
	/**
	 * Draw ellipse.
	 *
	 * @param ip the ip
	 */
	public void drawEllipse(ImageProcessor ip) {
		if (ef!=null)
			ef.drawEllipse(ip);
	}
	
	/**
	 * Calculate median.
	 *
	 * @param hist the hist
	 * @param first the first
	 * @param last the last
	 * @param cal the cal
	 */
	void calculateMedian(int[] hist, int first, int last, Calibration cal) {
		//ij.IJ.log("calculateMedian: "+first+"  "+last+"  "+hist.length+"  "+pixelCount);
		double sum = 0;
		int i = first-1;
		double halfCount = pixelCount/2.0;
		do {
			sum += hist[++i];
		} while (sum<=halfCount && i<last);
		median = cal!=null?cal.getCValue(i):i;
	}
	
	/**
	 * Calculate area fraction.
	 *
	 * @param ip the ip
	 * @param hist the hist
	 */
	void calculateAreaFraction(ImageProcessor ip, int[] hist) {
		int sum = 0;
		int total = 0;
		int t1 = (int)Math.round(ip.getMinThreshold());
		int t2 = (int)Math.round(ip.getMaxThreshold());
		if (t1==ImageProcessor.NO_THRESHOLD) {
			for (int i=0; i<hist.length; i++)
				total += hist[i];
			sum = total - hist[0];
		} else {
			for (int i=0; i<hist.length; i++) {
				if (i>=t1 && i<=t2)
					sum += hist[i];
				total += hist[i];
			}
		}
		areaFraction = sum*100.0/total;
	}
	
	/**
	 *  Returns the histogram as an array of longs.
	 *
	 * @return the histogram
	 */
	public long[] getHistogram() {
		long[] hist = new long[histogram.length];
		for (int i=0; i<hist.length; i++) {
			if (longHistogram!=null)
				hist[i] = longHistogram[i];
			else
				hist[i] = histogram[i];
		}
		return hist;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "stats[count="+pixelCount+", mean="+mean+", min="+min+", max="+max+"]";
	}

}
