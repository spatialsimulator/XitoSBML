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
import ij.macro.Interpreter;
import ij.plugin.frame.Recorder;
import ij.util.Java2;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

// TODO: Auto-generated Javadoc
/** This class displays a dialog window from 
	which the user can select an input file. */ 
 public class OpenDialog {

	/** The dir. */
	private String dir;
	
	/** The name. */
	private String name;
	
	/** The record path. */
	private boolean recordPath;
	
	/** The default directory. */
	private static String defaultDirectory;
	
	/** The shared frame. */
	private static Frame sharedFrame;
	
	/** The title. */
	private String title;
	
	/** The last name. */
	private static String lastDir, lastName;

	
	/**
	 *  Displays a file open dialog with 'title' as the title.
	 *
	 * @param title the title
	 */
	public OpenDialog(String title) {
		this(title, null);
	}

	/**
	 *  Displays a file open dialog with 'title' as
	 * 		the title. If 'path' is non-blank, it is
	 * 		used and the dialog is not displayed. Uses
	 * 		and updates the ImageJ default directory.
	 *
	 * @param title the title
	 * @param path the path
	 */
	public OpenDialog(String title, String path) {
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null && (path==null||path.equals(""))) {
			path = Macro.getValue(macroOptions, title, path);
			if (path==null || path.equals(""))
				path = Macro.getValue(macroOptions, "path", path);
			if ((path==null || path.equals("")) && title!=null && title.equals("Open As String"))
				path = Macro.getValue(macroOptions, "OpenAsString", path);
			path = lookupPathVariable(path);
		}
		if (path==null || path.equals("")) {
			if (Prefs.useJFileChooser)
				jOpen(title, getDefaultDirectory(), null);
			else
				open(title, getDefaultDirectory(), null);
			if (name!=null) defaultDirectory = dir;
			this.title = title;
			recordPath = true;
		} else {
			decodePath(path);
			recordPath = IJ.macroRunning();
		}
		IJ.register(OpenDialog.class);
	}
	
	/**
	 *  Displays a file open dialog, using the specified 
	 * 		default directory and file name.
	 *
	 * @param title the title
	 * @param defaultDir the default dir
	 * @param defaultName the default name
	 */
	public OpenDialog(String title, String defaultDir, String defaultName) {
		String path = null;
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null)
			path = Macro.getValue(macroOptions, title, path);
		if (path!=null)
			decodePath(path);
		else {
			if (Prefs.useJFileChooser)
				jOpen(title, defaultDir, defaultName);
			else
				open(title, defaultDir, defaultName);
			this.title = title;
			recordPath = true;
		}
	}
	
	/**
	 * Lookup path variable.
	 *
	 * @param path the path
	 * @return the string
	 */
	public static String lookupPathVariable(String path) {
		if (path!=null && path.indexOf(".")==-1 && !((new File(path)).exists())) {
			if (path.startsWith("&")) path=path.substring(1);
			Interpreter interp = Interpreter.getInstance();
			String path2 = interp!=null?interp.getStringVariable(path):null;
			if (path2!=null) path = path2;
		}
		return path;
	}

	/**
	 * J open.
	 *
	 * @param title the title
	 * @param path the path
	 * @param fileName the file name
	 */
	// Uses JFileChooser to display file open dialog box.
	void jOpen(String title, String path, String fileName) {
		Java2.setSystemLookAndFeel();
		if (EventQueue.isDispatchThread())
			jOpenDispatchThread(title, path, fileName);
		else
			jOpenInvokeAndWait(title, path, fileName);
	}
		
	// Uses the JFileChooser class to display the dialog box.
	/**
	 * J open dispatch thread.
	 *
	 * @param title the title
	 * @param path the path
	 * @param fileName the file name
	 */
	// Assumes we are running on the event dispatch thread
	void jOpenDispatchThread(String title, String path, final String fileName) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);
		File fdir = null;
		if (path!=null)
			fdir = new File(path);
		if (fdir!=null)
			fc.setCurrentDirectory(fdir);
		if (fileName!=null)
			fc.setSelectedFile(new File(fileName));
		int returnVal = fc.showOpenDialog(IJ.getInstance());
		if (returnVal!=JFileChooser.APPROVE_OPTION)
			{Macro.abort(); return;}
		File file = fc.getSelectedFile();
		if (file==null)
			{Macro.abort(); return;}
		name = file.getName();
		dir = fc.getCurrentDirectory().getPath()+File.separator;
	}

	/**
	 * J open invoke and wait.
	 *
	 * @param title the title
	 * @param path the path
	 * @param fileName the file name
	 */
	// Run JFileChooser on event dispatch thread to avoid deadlocks
	void jOpenInvokeAndWait(final String title, final String path, final String fileName) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle(title);
				File fdir = null;
				if (path!=null)
					fdir = new File(path);
				if (fdir!=null)
					fc.setCurrentDirectory(fdir);
				if (fileName!=null)
					fc.setSelectedFile(new File(fileName));
				int returnVal = fc.showOpenDialog(IJ.getInstance());
				if (returnVal!=JFileChooser.APPROVE_OPTION)
					{Macro.abort(); return;}
				File file = fc.getSelectedFile();
				if (file==null)
					{Macro.abort(); return;}
				name = file.getName();
				dir = fc.getCurrentDirectory().getPath()+File.separator;
				}
			});
		} catch (Exception e) {}
	}
	
	/**
	 * Open.
	 *
	 * @param title the title
	 * @param path the path
	 * @param fileName the file name
	 */
	// Uses the AWT FileDialog class to display the dialog box
	void open(String title, String path, String fileName) {
		Frame parent = IJ.getInstance();
		if (parent==null) {
			if (sharedFrame==null) sharedFrame = new Frame();
			parent = sharedFrame;
		}
		FileDialog fd = new FileDialog(parent, title);
		if (path!=null)
			fd.setDirectory(path);
		if (fileName!=null)
			fd.setFile(fileName);
		//GUI.center(fd);
		fd.show();
		name = fd.getFile();
		if (name==null) {
			if (IJ.isMacOSX())
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			Macro.abort();
		} else
			dir = fd.getDirectory();
	}

	/**
	 * Decode path.
	 *
	 * @param path the path
	 */
	void decodePath(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0) {
			dir = path.substring(0, i+1);
			name = path.substring(i+1);
		} else {
			dir = "";
			name = path;
		}
	}

	/**
	 *  Returns the selected directory.
	 *
	 * @return the directory
	 */
	public String getDirectory() {
		lastDir = dir;
		return dir;
	}
	
	/**
	 *  Returns the selected file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		if (name!=null) {
			if (Recorder.record && recordPath && dir!=null)
				Recorder.recordPath(title, dir+name);
			lastName = name;
		}
		return name;
	}
		
	/**
	 *  Returns the selected file path or null if the dialog was canceled.
	 *
	 * @return the path
	 */
	public String getPath() {
		if (getFileName()==null)
			return null;
		else return
			getDirectory() + getFileName();
	}

	/**
	 *  Returns the current working directory, which may be null. The
	 * 		returned string always ends with the separator character ("/" or "\").
	 *
	 * @return the default directory
	 */
	public static String getDefaultDirectory() {
		if (defaultDirectory==null)
			defaultDirectory = Prefs.getDefaultDirectory();
		return defaultDirectory;
	}

	/**
	 *  Sets the current working directory.
	 *
	 * @param defaultDir the new default directory
	 */
	public static void setDefaultDirectory(String defaultDir) {
		defaultDirectory = defaultDir;
		if (!defaultDirectory.endsWith(File.separator))
			defaultDirectory = defaultDirectory + File.separator;
	}
	
	/**
	 *  Returns the path to the last directory opened by the user
	 * 		using a file open or file save dialog, or using drag and drop. 
	 * 		Returns null if the users has not opened a file.
	 *
	 * @return the last directory
	 */
	public static String getLastDirectory() {
		return lastDir;
	}
		
	/**
	 *  Sets the path to the directory containing the last file opened by the user.
	 *
	 * @param dir the new last directory
	 */
	public static void setLastDirectory(String dir) {
		lastDir = dir;
	}

	/**
	 *  Returns the name of the last file opened by the user
	 * 		using a file open or file save dialog, or using drag and drop.
	 * 		Returns null if the users has not opened a file.
	 *
	 * @return the last name
	 */
	public static String getLastName() {
		return lastName;
	}

	/**
	 *  Sets the name of the last file opened by the user.
	 *
	 * @param name the new last name
	 */
	public static void setLastName(String name) {
		lastName = name;
	}

}
