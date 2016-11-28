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
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * This plugin is used by the Image/Stacks/Tools/Make Substack
 * command to create substacks of hyperstacks.
 *  
 * @author Curtis Rueden
 */
public class SubHyperstackMaker implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(final String arg) {
		// verify input image is appropriate
		final ImagePlus input = WindowManager.getCurrentImage();
		if (input == null) {
			IJ.showMessage("No image open.");
			return;
		}
		if (input.getStackSize() == 1) {
			IJ.showMessage("Image is not a stack.");
			return;
		}
		final int cCount = input.getNChannels();
		final int zCount = input.getNSlices();
		final int tCount = input.getNFrames();
		final boolean hasC = cCount > 1;
		final boolean hasZ = zCount > 1;
		final boolean hasT = tCount > 1;

		// prompt for C, Z and T ranges
		final GenericDialog gd = new GenericDialog("Subhyperstack Maker");
		gd.addMessage("Enter a range (e.g. 2-14), a range with increment\n"
			+ "(e.g. 1-100-2) or a list (e.g. 7,9,25,27)", null, Color.darkGray);
		if (hasC) gd.addStringField("Channels:", "1-" + cCount, 40);
		if (hasZ) gd.addStringField("Slices:", "1-" + zCount, 40);
		if (hasT) gd.addStringField("Frames:", "1-" + tCount, 40);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		final String cString = hasC ? gd.getNextString() : "1";
		final String zString = hasZ ? gd.getNextString() : "1";
		final String tString = hasT ? gd.getNextString() : "1";

		// compute subhyperstack
		final ImagePlus output =
			makeSubhyperstack(input, cString, zString, tString);

		// display result
		output.show();
	}

	/**
	 * Make subhyperstack.
	 *
	 * @param input the input
	 * @param cString the c string
	 * @param zString the z string
	 * @param tString the t string
	 * @return the image plus
	 */
	public static ImagePlus makeSubhyperstack(final ImagePlus input,
		final String cString, final String zString, final String tString)
	{
		final ArrayList<Integer> cList = parseList(cString, input.getNChannels());
		final ArrayList<Integer> zList = parseList(zString, input.getNSlices());
		final ArrayList<Integer> tList = parseList(tString, input.getNFrames());
		return makeSubhyperstack(input, cList, zList, tList);
	}

	/**
	 * Make subhyperstack.
	 *
	 * @param input the input
	 * @param cList the c list
	 * @param zList the z list
	 * @param tList the t list
	 * @return the image plus
	 */
	public static ImagePlus makeSubhyperstack(final ImagePlus input,
		final List<Integer> cList, final List<Integer> zList,
		final List<Integer> tList)
	{
		// validate inputs
		if (cList.size() == 0) {
			throw new IllegalArgumentException("Must specify at least one channel");
		}
		if (zList.size() == 0) {
			throw new IllegalArgumentException("Must specify at least one slice");
		}
		if (tList.size() == 0) {
			throw new IllegalArgumentException("Must specify at least one frame");
		}

		final ImageStack inputStack = input.getImageStack();

		final int cCount = input.getNChannels();
		final int zCount = input.getNSlices();
		final int tCount = input.getNFrames();

		for (final int c : cList)
			check("C", c, cCount);
		for (final int z : zList)
			check("Z", z, zCount);
		for (final int t : tList)
			check("T", t, tCount);

		// create output image
		final String title = WindowManager.getUniqueName(input.getTitle());
		ImagePlus output =
			input.createHyperStack(title, cList.size(), zList.size(), tList.size(),
				input.getBitDepth());
		final ImageStack outputStack = output.getImageStack();

		// add specified planes to subhyperstack
		int oc = 0, oz, ot;
		for (final int c : cList) {
			oc++;
			oz = 0;
			for (final int z : zList) {
				oz++;
				ot = 0;
				for (final int t : tList) {
					ot++;
					final int i = input.getStackIndex(c, z, t);
					final int oi = output.getStackIndex(oc, oz, ot);
					final String label = inputStack.getSliceLabel(i);
					final ImageProcessor ip = inputStack.getProcessor(i);
					outputStack.setSliceLabel(label, oi);
					outputStack.setPixels(ip.getPixels(), oi);
				}
			}
		}

		// propagate composite image settings, if appropriate
		if (input instanceof CompositeImage) {
			final CompositeImage compositeInput = (CompositeImage) input;
			final CompositeImage compositeOutput =
				new CompositeImage(output, compositeInput.getMode());
			oc = 0;
			for (final int c : cList) {
				oc++;
				final LUT table = compositeInput.getChannelLut(c);
				compositeOutput.setChannelLut(table, oc);
				compositeOutput.setPositionWithoutUpdate(oc, 1, 1);
				compositeInput.setPositionWithoutUpdate(c, 1, 1);
				final double min = compositeInput.getDisplayRangeMin();
				final double max = compositeInput.getDisplayRangeMax();
				compositeOutput.setDisplayRange(min, max);
			}
			output = compositeOutput;
		}

		// return result
		return output;
	}

	/**
	 * Check.
	 *
	 * @param name the name
	 * @param index the index
	 * @param count the count
	 */
	private static void
		check(final String name, final int index, final int count)
	{
		if (index < 1 || index > count) {
			throw new IllegalArgumentException("Invalid " + name + " index: " +
				index);
		}
	}

	/**
	 * Parses the list.
	 *
	 * @param planeString the plane string
	 * @param count the count
	 * @return the array list
	 */
	private static ArrayList<Integer> parseList(final String planeString,
		int count)
	{
		final ArrayList<Integer> list = new ArrayList<Integer>();
		for (final String token : planeString.split("\\s*,\\s*")) {
			final int dash1 = token.indexOf("-");
			final int dash2 = token.lastIndexOf("-");
			if (dash1 < 0) {
				// single number
				final int index;
				try {
					index = Integer.parseInt(token);
				}
				catch (final NumberFormatException exc) {
					throw new IllegalArgumentException("Invalid number: " + token);
				}
				if (index < 1 || index > count) {
					throw new IllegalArgumentException("Invalid number: " + token);
				}
				list.add(Integer.parseInt(token));
			}
			else {
				// range, with or without increment
				final int min, max, step;
				try {
					min = Integer.parseInt(token.substring(0, dash1));
					if (dash1 == dash2) {
						// range (e.g. 2-14)
						max = Integer.parseInt(token.substring(dash1 + 1));
						step = 1;
					}
					else {
						// range with increment (e.g. 1-100-2)
						max = Integer.parseInt(token.substring(dash1 + 1, dash2));
						step = Integer.parseInt(token.substring(dash2 + 1));
					}
				}
				catch (final NumberFormatException exc) {
					throw new IllegalArgumentException("Invalid range: " + token);
				}
				if (min < 1 || min > max || max > count || step < 1) {
					throw new IllegalArgumentException("Invalid range: " + token);
				}
				for (int index = min; index <= max; index += step) {
					list.add(index);
				}
			}
		}
		return list;
	}

}
