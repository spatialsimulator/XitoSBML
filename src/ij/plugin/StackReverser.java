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
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

// TODO: Auto-generated Javadoc
/** This plugin implements the Image/Transform/Flip Z and
	Image/Stacks/Tools/Reverse commands. */
public class StackReverser implements PlugIn {
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		if (imp.getStackSize()==1) {
			IJ.error("Flip Z", "This command requires a stack");
			return;
		}
		if (imp.isHyperStack()) {
			IJ.error("Flip Z", "This command does not currently work with hyperstacks.");
			return;
		}
		flipStack(imp);
	}
	
	/**
	 * Flip stack.
	 *
	 * @param imp the imp
	 */
	public void flipStack(ImagePlus imp) {
		ImageStack stack = imp.getStack();
		int n = stack.getSize();
		if (n==1)
			return;
 		ImageStack stack2 = new ImageStack(imp.getWidth(), imp.getHeight(), n);
 		for (int i=1; i<=n; i++) {
 			stack2.setPixels(stack.getPixels(i), n-i+1);
 			stack2.setSliceLabel(stack.getSliceLabel(i), n-i+1);
 		}
 		stack2.setColorModel(stack.getColorModel());
		imp.setStack(stack2);
		if (imp.isComposite()) {
			((CompositeImage)imp).reset();
			imp.updateAndDraw();
		}
	}

}
