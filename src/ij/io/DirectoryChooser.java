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
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import ij.util.Java2;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFileChooser;

// TODO: Auto-generated Javadoc
/** This class displays a dialog box that allows the user can select a directory. */ 
 public class DirectoryChooser {
 	
	 /** The directory. */
	 private String directory;
 	
	 /** The title. */
	 private String title;
 
 	/**
	  *  Display a dialog using the specified title.
	  *
	  * @param title the title
	  */
 	public DirectoryChooser(String title) {
 		this.title = title;
		if (IJ.isMacOSX() && !IJ.isJava17())
			getDirectoryUsingFileDialog(title);
 		else {
			String macroOptions = Macro.getOptions();
			if (macroOptions!=null)
				directory = Macro.getValue(macroOptions, title, null);
			if (directory==null) {
 				if (EventQueue.isDispatchThread())
 					getDirectoryUsingJFileChooserOnThisThread(title);
 				else
 					getDirectoryUsingJFileChooser(title);
 			}
 		}
 	}
 	
	/**
	 * Gets the directory using J file chooser.
	 *
	 * @param title the title
	 * @return the directory using J file chooser
	 */
	// runs JFileChooser on event dispatch thread to avoid possible thread deadlocks
 	void getDirectoryUsingJFileChooser(final String title) {
		Java2.setSystemLookAndFeel();
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle(title);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					String defaultDir = OpenDialog.getDefaultDirectory();
					if (defaultDir!=null) {
						File f = new File(defaultDir);
						if (IJ.debugMode)
							IJ.log("DirectoryChooser,setSelectedFile: "+f);
						chooser.setSelectedFile(f);
					}
					chooser.setApproveButtonText("Select");
					if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						directory = file.getAbsolutePath();
						if (!directory.endsWith(File.separator))
							directory += File.separator;
						OpenDialog.setDefaultDirectory(directory);
					}
				}
			});
		} catch (Exception e) {}
	}
 
	/**
	 * Gets the directory using J file chooser on this thread.
	 *
	 * @param title the title
	 * @return the directory using J file chooser on this thread
	 */
	// Choose a directory using JFileChooser on the current thread
 	void getDirectoryUsingJFileChooserOnThisThread(final String title) {
		Java2.setSystemLookAndFeel();
		try {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(title);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			String defaultDir = OpenDialog.getDefaultDirectory();
			if (defaultDir!=null) {
				File f = new File(defaultDir);
				if (IJ.debugMode)
					IJ.log("DirectoryChooser,setSelectedFile: "+f);
				chooser.setSelectedFile(f);
			}
			chooser.setApproveButtonText("Select");
			if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				directory = file.getAbsolutePath();
				if (!directory.endsWith(File.separator))
					directory += File.separator;
				OpenDialog.setDefaultDirectory(directory);
			}
		} catch (Exception e) {}
	}

 	/**
	  * Gets the directory using file dialog.
	  *
	  * @param title the title
	  * @return the directory using file dialog
	  */
	 // On Mac OS X, we can select directories using the native file open dialog
 	void getDirectoryUsingFileDialog(String title) {
 		boolean saveUseJFC = Prefs.useJFileChooser;
 		Prefs.useJFileChooser = false;
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		String dir=null, name=null;
		String defaultDir = OpenDialog.getDefaultDirectory();
		if (defaultDir!=null) {
			File f = new File(defaultDir);
			dir = f.getParent();
			name = f.getName();
		}
		if (IJ.debugMode)
			IJ.log("DirectoryChooser: dir=\""+dir+"\",  file=\""+name+"\"");
		OpenDialog od = new OpenDialog(title, dir, name);
		if (od.getDirectory()==null)
			directory = null;
		else
			directory = od.getDirectory() + od.getFileName() + "/";
		if (directory!=null)
			OpenDialog.setDefaultDirectory(directory);
		System.setProperty("apple.awt.fileDialogForDirectories", "false");
 		Prefs.useJFileChooser = saveUseJFC;
	}

 	/**
	  *  Returns the directory selected by the user.
	  *
	  * @return the directory
	  */
 	public String getDirectory() {
		if (IJ.debugMode)
			IJ.log("DirectoryChooser.getDirectory: "+directory);
		if (Recorder.record && !IJ.isMacOSX())
			Recorder.recordPath(title, directory);
 		return directory;
 	}
 	
    /**
     *  Sets the default directory presented in the dialog.
     *
     * @param dir the new default directory
     */
    public static void setDefaultDirectory(String dir) {
    	if (dir==null || (new File(dir)).isDirectory())
			OpenDialog.setDefaultDirectory(dir);
    }

	//private void setSystemLookAndFeel() {
	//	try {
	//		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	//	} catch(Throwable t) {}
	//}

}
