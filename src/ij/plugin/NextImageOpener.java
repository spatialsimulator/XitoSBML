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
/**
This plugin, written by Jon Harmon, implements the File/Open Next command.
It opens the "next" image in a directory, where "next" can be the
succeeding or preceeding image in the directory list.
Press shift-o to open the succeeding image or 
alt-shift-o to open the preceeding image.
It can leave the previous file open, or close it.
You may contact the author at Jonathan_Harman at yahoo.com
This code was modified from Image_Browser by Albert Cardona
*/

package ij.plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.YesNoCancelDialog;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.io.Opener;

import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Class NextImageOpener.
 */
public class NextImageOpener implements PlugIn {

	/** The forward. */
	boolean forward = true; // default browse direction is forward
	
	/** The close current. */
	boolean closeCurrent = true; //default behavior is to close current window
	
	/** The imp 0. */
	ImagePlus imp0;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		/* get changes to defaults */
		if (arg.equals("backward") || IJ.altKeyDown()) forward = false;
		if (arg.equals("backwardsc")) {
			forward = false;
			closeCurrent = false;
		}
		if (arg.equals("forwardsc")) {
			forward = true;
			closeCurrent = false;
		}
				
		// get current image; displays error and aborts if no image is open
 		imp0 = IJ.getImage();
 		// get current image directory
 		String currentPath = getDirectory(imp0);
		if (IJ.debugMode) IJ.log("OpenNext.currentPath:" + currentPath);
		if (currentPath==null) {
			IJ.error("Next Image", "Directory information for \""+imp0.getTitle()+"\" not found.");
			return;
		}
		// get the next name (full path)
		//long start = System.currentTimeMillis();
		String nextPath = getNext(currentPath, getName(imp0), forward);
		//IJ.log("time: "+(System.currentTimeMillis()-start));
		if (IJ.debugMode) IJ.log("OpenNext.nextPath:" + nextPath);
		// open
		if (nextPath != null) {
			String rtn = open(nextPath);
			if (rtn==null)
				open(getNext(currentPath, (new File(nextPath)).getName(), forward));
		}
	}
	
	/**
	 * Gets the directory.
	 *
	 * @param imp the imp
	 * @return the directory
	 */
	String getDirectory(ImagePlus imp) {
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi==null) return null;
		String dir = fi.openNextDir;
		if (dir==null) dir = fi.directory;
		return dir;
	}

	/**
	 * Gets the name.
	 *
	 * @param imp the imp
	 * @return the name
	 */
	String getName(ImagePlus imp) {
		String name = imp.getTitle();
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi!=null) {
			if (fi.openNextName!=null)
				name = fi.openNextName;
			else if (fi.fileName!=null)
				name = fi.fileName;
		}
		return name;
	}
	
	/**
	 * Open.
	 *
	 * @param nextPath the next path
	 * @return the string
	 */
	String open(String nextPath) {
		ImagePlus imp2 = IJ.openImage(nextPath);
		if (imp2==null) return null;
		String newTitle = imp2.getTitle();
		if (imp0.changes) {
			String msg;
			String name = imp0.getTitle();
			if (name.length()>22)
				msg = "Save changes to\n" + "\"" + name + "\"?";
			else
				msg = "Save changes to \"" + name + "\"?";
			YesNoCancelDialog d = new YesNoCancelDialog(imp0.getWindow(), "ImageJ", msg);
			if (d.cancelPressed())
				return "Canceled";
			else if (d.yesPressed()) {
				FileSaver fs = new FileSaver(imp0);
				if (!fs.save())
					return "Canceled";
			}
			imp0.changes = false;
		}
		if (imp2.isComposite() || imp2.isHyperStack()) {
			imp2.show();
			imp0.close();
			imp0 = imp2;
		} else {
			imp0.setStack(newTitle, imp2.getStack());
			imp0.setCalibration(imp2.getCalibration());
			imp0.setFileInfo(imp2.getOriginalFileInfo());
			imp0.setProperty ("Info", imp2.getProperty ("Info"));
			imp0.setOverlay(imp2.getOverlay());
			ImageWindow win = imp0.getWindow();
			if (win!=null) win.repaint();
		}
		return "ok";
	}

	/**
	 *  gets the next image name in a directory list.
	 *
	 * @param path the path
	 * @param imageName the image name
	 * @param forward the forward
	 * @return the next
	 */
	String getNext(String path, String imageName, boolean forward) {
		File dir = new File(path);
		if (!dir.isDirectory()) return null;
		String[] names = dir.list();
		ij.util.StringSorter.sort(names);
		int thisfile = -1;
		for (int i=0; i<names.length; i++) {
			if (names[i].equals(imageName)) {
				thisfile = i;
				break;
			}
		}
		if (IJ.debugMode) IJ.log("OpenNext.thisfile:" + thisfile);
		if(thisfile == -1) return null;// can't find current image
		
		// make candidate the index of the next file
		int candidate = thisfile + 1;
		if (!forward) candidate = thisfile - 1;
		if (candidate<0) candidate = names.length - 1;
		if (candidate==names.length) candidate = 0;
		// keep on going until an image file is found or we get back to beginning
		while (candidate!=thisfile) {
			String nextPath = path + names[candidate];
			if (IJ.debugMode) IJ.log("OpenNext: "+ candidate + "  " + names[candidate]);
			File nextFile = new File(nextPath);
			boolean canOpen = true;
			if (names[candidate].startsWith(".") || nextFile.isDirectory())
				canOpen = false;
			if (canOpen) {
				Opener o = new Opener();
				int type = o.getFileType(nextPath);
				if (type==Opener.UNKNOWN || type==Opener.JAVA_OR_TEXT
				||  type==Opener.ROI ||  type==Opener.TEXT)
					canOpen = false;
			}
			if (canOpen)
					return nextPath;
			else {// increment again
				if (forward)
					candidate = candidate + 1;
				else
					candidate = candidate - 1;
				if (candidate<0) candidate = names.length - 1;
				if (candidate == names.length) candidate = 0;
			}
			
		}
		if (IJ.debugMode) IJ.log("OpenNext: Search failed");
		return null;
	}

}
