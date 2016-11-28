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
import ij.gui.GenericDialog;
import ij.measure.Calibration;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Stacks/Tools/Grouped Z Project command. */

public class GroupedZProjector implements PlugIn {
	
	/** The method. */
	private static int method = ZProjector.AVG_METHOD;
	
	/** The group size. */
	private int groupSize;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		int size = imp.getStackSize();
		if (size==1) {
			IJ.error("Z Project", "This command requires a stack");
			return;
		}
		if (imp.isHyperStack()) {
			new ZProjector().run("");
			return;
		}
		if (!showDialog(imp))
			return;
		ImagePlus imp2 = groupZProject(imp, method, groupSize);
		imp2.setCalibration(imp.getCalibration());
		Calibration cal = imp2.getCalibration();
		cal.pixelDepth *= groupSize;
		if (imp!=null)
			imp2.show();
	}
	
	/**
	 * Group Z project.
	 *
	 * @param imp the imp
	 * @param method the method
	 * @param groupSize the group size
	 * @return the image plus
	 */
	public ImagePlus groupZProject(ImagePlus imp, int method, int groupSize) {
		if (method<0 || method>=ZProjector.METHODS.length)
			return null;
		imp.setDimensions(1, groupSize, imp.getStackSize()/groupSize);
		ZProjector zp = new ZProjector(imp);
		zp.setMethod(method);
		zp.setStartSlice(1);
		zp.setStopSlice(groupSize);
		zp.doHyperStackProjection(true);
		return zp.getProjection();
	}
	
	/**
	 * Show dialog.
	 *
	 * @param imp the imp
	 * @return true, if successful
	 */
	boolean showDialog(ImagePlus imp) {
		int size = imp.getStackSize();
		GenericDialog gd = new GenericDialog("Z Project");
		gd.addChoice("Projection method:", ZProjector.METHODS, ZProjector.METHODS[method]);
		gd.addNumericField("Group size:", size, 0);
		String factors = "Valid factors: ";
		int i = 1, count = 0;
		while (i <= size && count<10) {
			if (size % i == 0) {
				count++; factors +=	 " "+ i +",";
			}
			i++;
		}
		gd.setInsets(10,0,0);
		gd.addMessage(factors+"...");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		method = gd.getNextChoiceIndex();
		groupSize = (int)gd.getNextNumber();
		if (groupSize<1 || groupSize>size || (size%groupSize)!=0) {
			IJ.error("ZProject", "Group size must divide evenly into the stack size.");
			return false;
		}
		return true;
	}
	
}