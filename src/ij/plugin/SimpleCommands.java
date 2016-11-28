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
import ij.Undo;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

// TODO: Auto-generated Javadoc
/** This plugin implements the Plugins/Utilities/Unlock, Image/Rename
	and Plugins/Utilities/Search commands. */
public class SimpleCommands implements PlugIn {
	
	/** The search arg. */
	static String searchArg;
    
    /** The choices. */
    private static String[] choices = {"Locked Image", "Clipboard", "Undo Buffer"};
    
    /** The choice index. */
    private static int choiceIndex = 0;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (arg.equals("search"))
			search();
		else if (arg.equals("import")) 
			Opener.openResultsTable("");
		else if (arg.equals("table")) 
			Opener.openTable("");
		else if (arg.equals("rename"))
			rename();
		else if (arg.equals("reset"))
			reset();
		else if (arg.equals("about"))
			aboutPluginsHelp();
		else if (arg.equals("install"))
			installation();
		else if (arg.equals("set"))
			setSliceLabel();
		else if (arg.equals("remove"))
			removeStackLabels();
		else if (arg.equals("itor"))
			imageToResults();
		else if (arg.equals("rtoi"))
			resultsToImage();
		else if (arg.equals("display"))
			IJ.runMacroFile("ij.jar:ShowAllLuts", null);
		else if (arg.equals("fonts"))
			showFonts();
	}
	
	/**
	 * Show fonts.
	 */
	private synchronized void showFonts() {
		Thread t = new Thread(new Runnable() {
			public void run() {IJ.runPlugIn("ij.plugin.Text", "");}
		});
		t.start();
	}

	/**
	 * Reset.
	 */
	private void reset() {
		GenericDialog gd = new GenericDialog("");
		gd.addChoice("Reset:", choices, choices[choiceIndex]);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		choiceIndex = gd.getNextChoiceIndex();
		switch (choiceIndex) {
			case 0: unlock(); break;
			case 1: resetClipboard(); break;
			case 2: resetUndo(); break;
		}
	}
	
	/**
	 * Unlock.
	 */
	private void unlock() {
		ImagePlus imp = IJ.getImage();
		boolean wasUnlocked = imp.lockSilently();
		if (wasUnlocked)
			IJ.showStatus("\""+imp.getTitle()+"\" is not locked");
		else {
			IJ.showStatus("\""+imp.getTitle()+"\" is now unlocked");
			IJ.beep();
		}
		imp.unlock();
	}

	/**
	 * Reset clipboard.
	 */
	private void resetClipboard() {
		ImagePlus.resetClipboard();
		IJ.showStatus("Clipboard reset");
	}
	
	/**
	 * Reset undo.
	 */
	private void resetUndo() {
		Undo.setup(Undo.NOTHING, null);
		IJ.showStatus("Undo reset");
	}
	
	/**
	 * Rename.
	 */
	private void rename() {
		ImagePlus imp = IJ.getImage();
		GenericDialog gd = new GenericDialog("Rename");
		gd.addStringField("Title:", imp.getTitle(), 30);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else
			imp.setTitle(gd.getNextString());
	}
		
	/**
	 * Search.
	 */
	private void search() {
		searchArg = IJ.runMacroFile("ij.jar:Search", searchArg);
	}
		
	/**
	 * Installation.
	 */
	private void installation() {
		String url = IJ.URL+"/docs/install/";
		if (IJ.isMacintosh())
			url += "osx.html";
		else if (IJ.isWindows())
			url += "windows.html";
		else if (IJ.isLinux())
			url += "linux.html";
		IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
	}
	
	/**
	 * About plugins help.
	 */
	private void aboutPluginsHelp() {
		IJ.showMessage("\"About Plugins\" Submenu", 
			"Plugins packaged as JAR files can add entries\n"+
			"to this submenu. There is an example at\n \n"+
			IJ.URL+"/plugins/jar-demo.html");
	}
	
	/**
	 * Sets the slice label.
	 */
	private void setSliceLabel() {
		ImagePlus imp = IJ.getImage();
		int size = imp.getStackSize();
		if (size==1) {
			IJ.error("Stack required");
			return;
		}
		ImageStack stack = imp.getStack();
		int n = imp.getCurrentSlice();
		String label = stack.getSliceLabel(n);
		String label2 = label;
		if (label2==null)
			label2 = "";
		GenericDialog gd = new GenericDialog("Set Slice Label ("+n+")");
		gd.addStringField("Label:", label2, 30);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		label2 = gd.getNextString();
		if (label2!=label) {
			stack.setSliceLabel(label2, n);
			imp.repaintWindow();
		}
	}

	/**
	 * Removes the stack labels.
	 */
	private void removeStackLabels() {
		ImagePlus imp = IJ.getImage();
		int size = imp.getStackSize();
		if (size==1)
			IJ.error("Stack required");
		else {
			ImageStack stack = imp.getStack();
			for (int i=1; i<=size; i++)
				stack.setSliceLabel(null, i);
			imp.repaintWindow();
		}
	}
	
	/**
	 * Image to results.
	 */
	private void imageToResults() {
		ImagePlus imp = IJ.getImage();
		ImageProcessor ip = imp.getProcessor();
		ResultsTable rt = ResultsTable.createTableFromImage(ip);
		rt.showRowNumbers(false);
		rt.show("Results");
	}
	
	/**
	 * Results to image.
	 */
	private void resultsToImage() {
		ResultsTable rt = ResultsTable.getResultsTable();
		if (rt==null || rt.getCounter()==0) {
			IJ.error("Results to Image", "The Results table is empty");
			return;
		}
		ImageProcessor ip = rt.getTableAsImage();
		if (ip==null) return;
		new ImagePlus("Results Table", ip).show();
	}

}
