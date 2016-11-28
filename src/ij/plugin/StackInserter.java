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
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/** This plugin, which implements the Image/Stacks/Tools/Insert 
	command, inserts an image or stack into another image or stack. */
public class StackInserter implements PlugIn {

	/** The index 1. */
	private static int index1;
	
	/** The index 2. */
	private static int index2;
	
	/** The y. */
	private static int x, y;
			
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		int[] wList = WindowManager.getIDList();
		if (wList==null) {
			IJ.showMessage("Stack Inserter", "No windows are open.");
			return;
		}
		if (wList.length==1) {
			IJ.showMessage("Stack Inserter", "At least two windows must be open.");
			return;
		}
		String[] titles = new String[wList.length];
		for (int i=0; i<wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp!=null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}
		if (index1>=titles.length)index1 = 0;
		if (index2>=titles.length)index2 = 0;
		GenericDialog gd = new GenericDialog("Stack Inserter");
		gd.addChoice("Source: ", titles, titles[index1]);
		gd.addChoice("Destination: ", titles, titles[index2]);
		gd.addNumericField("X Location: ", 0, 0);
		gd.addNumericField("Y Location: ", 0, 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		index1 = gd.getNextChoiceIndex();
		index2 = gd.getNextChoiceIndex();
		x = (int)gd.getNextNumber();
		y = (int)gd.getNextNumber();
		String title1 = titles[index1];
		String title2 = titles[index2];
		ImagePlus imp1 = WindowManager.getImage(wList[index1]);
		ImagePlus imp2 = WindowManager.getImage(wList[index2]);
		if (imp1.getType()!= imp2.getType()) {
			IJ.showMessage("Stack Inserter", "The source and destination must be the same type.");
			return;
		}
		if (imp1== imp2) {
			IJ.showMessage("Stack Inserter", "The source and destination must be different.");
			return;
		}
		insert(imp1, imp2, x, y);
	}
	
	/**
	 * Insert.
	 *
	 * @param imp1 the imp 1
	 * @param imp2 the imp 2
	 * @param x the x
	 * @param y the y
	 */
	public void insert(ImagePlus imp1, ImagePlus imp2, int x, int y) {
		ImageStack stack1 = imp1.getStack();
		ImageStack stack2 = imp2.getStack();
		int size1 = stack1.getSize();
		int size2 = stack2.getSize();
		ImageProcessor ip1, ip2;
		for (int i=1; i<=size2; i++) {
			ip1 = stack1.getProcessor(i<=size1?i:size1);
			ip2 = stack2.getProcessor(i);
			ip2.insert(ip1, x, y);
			stack2.setPixels(ip2.getPixels(), i);
		}
		imp2.setStack(null, stack2);
	}

}
