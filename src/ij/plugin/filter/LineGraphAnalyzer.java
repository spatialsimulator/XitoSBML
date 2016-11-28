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
package ij.plugin.filter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/** Implements ImageJ's Analyze/Tools/Analyze Line Graph command. */
public class LineGraphAnalyzer implements PlugInFilter, Measurements  {
	
	/** The imp. */
	ImagePlus imp;

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		analyze(imp);
	}
	
	/**
	 *  Uses ImageJ's particle analyzer to extract a set
	 * 		of coordinate pairs from a digitized line graph.
	 *
	 * @param imp the imp
	 */
	public void analyze(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		ImageProcessor ip2 = ip.crop();
		int width = ip2.getWidth();
		int height = ip2.getHeight();
		ip2.setColor(Color.white);
		for (int i=1; i<width; i+=2) {
			ip2.moveTo(i,0);
			ip2.lineTo(i,height-1);
		}
		ip2 = ip2.rotateRight();
		ImagePlus imp2 = imp.createImagePlus();
		ip2.setThreshold(ip.getMinThreshold(), ip.getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);
		imp2.setProcessor("Temp", ip2);
		Calibration cal = imp2.getCalibration();
		double pw = cal.pixelWidth;
		double ph = cal.pixelHeight;
		cal.pixelWidth = ph;
		cal.pixelHeight = pw;
		imp2.setCalibration(cal);
		if (IJ.altKeyDown())
			imp2.show();
		int options = ParticleAnalyzer.SHOW_PROGRESS;
		int measurements = CENTROID;
		int minSize = 1;
		int maxSize = Integer.MAX_VALUE;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
		if (!pa.analyze(imp2))
			return;
		float[] y = rt.getColumn(ResultsTable.X_CENTROID);
		if (y==null)
			return;				
		float[] x = rt.getColumn(ResultsTable.Y_CENTROID);
		double[] a = Tools.getMinMax(x);
		double xmin=a[0], xmax=a[1];
		a = Tools.getMinMax(y);
		double ymin=a[0], ymax=a[1];
		
		String units = " ("+cal.getUnits()+")";
		String xLabel = "X"+units;
		String yLabel = "Y"+units;
		Plot plot = new Plot("Line Graph", xLabel, yLabel, x, y);
		plot.setLimits(0.0, width*ph, 0.0, height*pw);				
		plot.show();				
	}
	
}
