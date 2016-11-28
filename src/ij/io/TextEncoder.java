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
import ij.IJ;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

// TODO: Auto-generated Javadoc
/** Saves an image described by an ImageProcessor object as a tab-delimited text file. */
public class TextEncoder {

	/** The ip. */
	private ImageProcessor ip;
	
	/** The cal. */
	private Calibration cal;
	
	/** The precision. */
	private int precision;

	/**
	 *  Constructs a TextEncoder from an ImageProcessor and optional Calibration.
	 *
	 * @param ip the ip
	 * @param cal the cal
	 * @param precision the precision
	 */
	public TextEncoder (ImageProcessor ip, Calibration cal, int precision) {
		this.ip = ip;
		this.cal = cal;
		this.precision = precision;
	}

	/**
	 *  Saves the image as a tab-delimited text file.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void write(DataOutputStream out) throws IOException {
		PrintWriter pw = new PrintWriter(out);
		boolean calibrated = cal!=null && cal.calibrated();
		if (calibrated)
			ip.setCalibrationTable(cal.getCTable());
		else
			ip.setCalibrationTable(null);
		boolean intData = !calibrated && ((ip instanceof ByteProcessor) || (ip instanceof ShortProcessor));
		int width = ip.getWidth();
		int height = ip.getHeight();
		int inc = height/20;
		if (inc<1) inc = 1;
		//IJ.showStatus("Exporting as text...");
		double value;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				value = ip.getPixelValue(x,y);
				if (intData)
					pw.print((int)value);
				else
					pw.print(IJ.d2s(value, precision));
				if (x!=(width-1))
					pw.print("\t");
			}
			pw.println();
			if (y%inc==0) IJ.showProgress((double)y/height);
		}
		pw.close();
		IJ.showProgress(1.0);
		//IJ.showStatus("");
	}
	
}
