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
import ij.process.StackProcessor;

// TODO: Auto-generated Javadoc
/** The plugin implements the Image/Stacks/Tools/Montage to Stack command.
	It creates a w*h image stack from an wxh image montage.
	This is the opposite of what the "Make Montage" command does.
	2010.04.20,TF: Final stack can be cropped to remove border around frames.
*/
public class StackMaker implements PlugIn {
	
	/** The b. */
	private static int w=2, h=2, b=0;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		if (imp.getStackSize()>1)
			{IJ.error("This command requires a montage"); return;}
		GenericDialog gd = new GenericDialog("Stack Maker");
		gd.addNumericField("Images_per_row: ", w, 0);
		gd.addNumericField("Images_per_column: ", h, 0);
		gd.addNumericField("Border width: ", b, 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		w = (int)gd.getNextNumber();
		h = (int)gd.getNextNumber();
		b = (int)gd.getNextNumber();
		ImageStack stack = makeStack(imp.getProcessor(), w, h, b);
		new ImagePlus("Stack", stack).show();
	}
	
	/**
	 * Make stack.
	 *
	 * @param ip the ip
	 * @param w the w
	 * @param h the h
	 * @param b the b
	 * @return the image stack
	 */
	public ImageStack makeStack(ImageProcessor ip, int w, int h, int b) {
		int stackSize = w*h;
		int width = ip.getWidth()/w;
		int height = ip.getHeight()/h;
		ImageStack stack = new ImageStack(width, height);
		for (int y=0; y<h; y++)
			for (int x=0; x<w; x++) {
				ip.setRoi(x*width, y*height, width, height);
				stack.addSlice(null, ip.crop());
			}
		if (b>0) { 
			int cropwidth = width-b-b/2;
			int cropheight = height-b-b/2;
			StackProcessor sp = new StackProcessor(stack,ip); 
			stack = sp.crop(b, b, cropwidth, cropheight);
		}
		return stack;
	}	 
}
