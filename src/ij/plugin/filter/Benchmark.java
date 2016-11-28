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
import ij.process.ImageProcessor;
// TODO: Auto-generated Javadoc
/** Implements the Plugins/Utilities/Run Benchmark command. 
	results and additional benchmarks are available at 
	"http://imagej.nih.gov/ij/plugins/benchmarks.html". */
public class Benchmark implements PlugInFilter{

	/** The arg. */
	String arg;
	
	/** The imp. */
	ImagePlus imp;
	
	/** The show updates. */
	boolean showUpdates= true;
	
	/** The counter. */
	int counter;
	
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES+SNAPSHOT;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		ip.setInterpolate(false);
		for (int i=0; i <4; i++) {
			ip.invert();
			updateScreen(imp);
		}
		for (int i=0; i <4; i++) {
			ip.flipVertical();
			updateScreen(imp);
		}
		ip.flipHorizontal(); updateScreen(imp);
		ip.flipHorizontal(); updateScreen(imp);
		for (int i=0; i <6; i++) {
			ip.smooth();
			updateScreen(imp);
		}
		ip.reset();
		for (int i=0; i <6; i++) {
			ip.sharpen();
			updateScreen(imp);
		}
		ip.reset();
		ip.smooth(); updateScreen(imp);
		ip.findEdges(); updateScreen(imp);
		ip.invert(); updateScreen(imp);
		ip.autoThreshold(); updateScreen(imp);
		ip.reset();
		ip.medianFilter(); updateScreen(imp);
		for (int i=0; i <360; i +=15) {
			ip.reset();
			ip.rotate(i);
			updateScreen(imp);
		}
		double scale = 1.5;
		for (int i=0; i <8; i++) {
			ip.reset();
			ip.scale(scale, scale);
			updateScreen(imp);
			scale = scale*1.5;
		}
		for (int i=0; i <12; i++) {
			ip.reset();
			scale = scale/1.5;
			ip.scale(scale, scale);
			updateScreen(imp);
		}
		ip.reset();
		updateScreen(imp);
	}
	
	/**
	 * Update screen.
	 *
	 * @param imp the imp
	 */
	void updateScreen(ImagePlus imp) {
		if (showUpdates)
			imp.updateAndDraw();
		IJ.showStatus((counter++) + "/"+72);
	}

	/*
	void showBenchmarkResults() {
		TextWindow tw = new TextWindow("ImageJ Benchmark", "", 450, 450);
		tw.setFont(new Font("Monospaced", Font.PLAIN, 12));
		tw.append("Time in seconds needed to perform 62 image processing");
		tw.append("operations on the 512x512 \"Mandrill\" image");
		tw.append("---------------------------------------------------------");
		tw.append(" 1.6   Pentium 4/3.0, WinXP  Java 1.3.1");
		tw.append(" 2.4   PPC G5/2.0x2, MacOSX  Java 1.3.1");
		tw.append(" 3.3   Pentium 4/1.4, Win2K  IE 5.0");
		tw.append(" 5.3   Pentium 3/750, Win98  IE 5.0");
		tw.append(" 5.6   Pentium 4/1.4, Win2K  JDK 1.3");
		tw.append(" 6.0   Pentium 3/750, Win98  Netscape 4.7");
		tw.append(" 8.6   PPC G4/400, MacOS     MRJ 2.2");
		tw.append("  11   Pentium 2/400, Win95  JRE 1.1.8");
		tw.append("  14   PPC G3/300, MacOS     MRJ 2.1");
		tw.append("  38   PPC 604/132, MacOS    MRJ 2.1");
		tw.append("  89   Pentium/100, Win95    JRE 1.1.6");
		tw.append("  96   Pentium/400, Linux    Sun JDK 1.2.2 (17 with JIT)");
		tw.append("");
	}
	*/

}


