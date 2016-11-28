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
import ij.WindowManager;
import ij.gui.GenericDialog;

// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Wand Tool command. */
public class WandToolOptions implements PlugIn {
	
	/** The Constant modes. */
	private static final String[] modes = {"Legacy", "4-connected", "8-connected"};
	
	/** The mode. */
	private static String mode = modes[0];
	
	/** The tolerance. */
	private static double tolerance;

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public void run(String arg) {
 		ImagePlus imp = WindowManager.getCurrentImage();
 		boolean showCheckbox = imp!=null && imp.getBitDepth()!=24 && WindowManager.getFrame("Threshold")==null;
		GenericDialog gd = new GenericDialog("Wand Tool");
		gd.addChoice("Mode: ", modes, mode);
		gd.addNumericField("Tolerance: ", tolerance, 1);
		if (showCheckbox)
			gd.addCheckbox("Enable Thresholding", false);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		mode = gd.getNextChoice();
		tolerance = gd.getNextNumber();
		if (showCheckbox) {
			if (gd.getNextBoolean()) {
				imp.deleteRoi();
				IJ.run("Threshold...");
			}
		}
	}
	
	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public static String getMode() {
		return mode;
	}

	/**
	 * Gets the tolerance.
	 *
	 * @return the tolerance
	 */
	public static double getTolerance() {
		return tolerance;
	}

}
