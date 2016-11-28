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
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.util.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Memory command. */
public class Memory implements PlugIn {
	
	/** The s. */
	String s;
	
	/** The index 2. */
	int index1, index2;
	
	/** The f. */
	File f;
	
	/** The file missing. */
	boolean fileMissing;
	
	/** The sixty four bit. */
	boolean sixtyFourBit;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		changeMemoryAllocation();
		//IJ.log("setting="+getMemorySetting()/(1024*1024)+"MB");
		//IJ.log("maxMemory="+maxMemory()/(1024*1024)+"MB");
	}

	/**
	 * Change memory allocation.
	 */
	void changeMemoryAllocation() {
		IJ.maxMemory(); // forces IJ to cache old limit
		int max = (int)(getMemorySetting()/1048576L);
		boolean unableToSet = max==0;
		if (max==0) max = (int)(maxMemory()/1048576L);
		String title = "Memory "+(IJ.is64Bit()?"(64-bit)":"(32-bit)");
		GenericDialog gd = new GenericDialog(title);
		gd.addNumericField("Maximum memory:", max, 0, 5, "MB");
		gd.addNumericField("Parallel threads:", Prefs.getThreads(), 0, 5, "");
		gd.setInsets(12, 0, 0);
		gd.addCheckbox("Keep multiple undo buffers", Prefs.keepUndoBuffers);
		gd.setInsets(12, 0, 0);
		gd.addCheckbox("Run garbage collector on status bar click", !Prefs.noClickToGC);
		gd.addHelp(IJ.URL+"/docs/menus/edit.html#memory");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		int max2 = (int)gd.getNextNumber();
		Prefs.setThreads((int)gd.getNextNumber());
		Prefs.keepUndoBuffers = gd.getNextBoolean();
		Prefs.noClickToGC = !gd.getNextBoolean();
		if (gd.invalidNumber()) {
			IJ.showMessage("Memory", "The number entered was invalid.");
			return;
		}
		if (unableToSet && max2!=max)
			{showError(); return;}
		if (IJ.isMacOSX() && max2<256)
			max2 = 256;
		else if (max2<32)
			max2 = 32;
		if (max2==max) return;
		int limit = IJ.isWindows()?1600:1700;
		String OSXInfo = "";
		if (IJ.isMacOSX())
			OSXInfo = "\n \nOn Max OS X, use\n"
				+"/Applications/Utilities/Java/Java Preferences\n"
				+"to switch to a 64-bit version of Java. You may\n"
				+"also need to run \"ImageJ64\" instead of \"ImageJ\".";
		if (max2>=limit && !IJ.is64Bit()) {
			if (!IJ.showMessageWithCancel(title, 
			"Note: setting the memory limit to a value\n"
			+"greater than "+limit+"MB on a 32-bit system\n"
			+"may cause ImageJ to fail to start. The title of\n"
			+"the Edit>Options>Memory & Threads dialog\n"
			+"box changes to \"Memory (64-bit)\" when ImageJ\n"
			+"is running on a 64-bit version of Java."
			+ OSXInfo));
				return;
		}
		try {
			String s2 = s.substring(index2);
			if (s2.startsWith("g"))
				s2 = "m"+s2.substring(1);
			String s3 = s.substring(0, index1) + max2 + s2;
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter pw = new PrintWriter(fos);
			pw.print(s3);
			pw.close();
		} catch (IOException e) {
			String error = e.getMessage();
			if (error==null || error.equals("")) error = ""+e;
			String name = IJ.isMacOSX()?"Info.plist":"ImageJ.cfg";
			String msg = 
				   "Unable to update the file \"" + name + "\".\n"
				+ " \n"
				+ "\"" + error + "\"";
			IJ.showMessage("Memory", msg);
			return;
		}
		String hint = "";
		if (IJ.isWindows() && max2>640 && max2>max)
			hint = "\nDelete the \"ImageJ.cfg\" file, located in the ImageJ folder,\nif ImageJ fails to start.";
		IJ.showMessage("Memory", "The new " + max2 +"MB limit will take effect after ImageJ is restarted."+hint);		
	}
	
	/**
	 * Gets the memory setting.
	 *
	 * @return the memory setting
	 */
	public long getMemorySetting() {
		if (IJ.getApplet()!=null) return 0L;
		long max = 0L;
		if (IJ.isMacOSX()) {
			if (IJ.is64Bit())
				max = getMemorySetting("ImageJ64.app/Contents/Info.plist");
			if (max==0L) {
				max = getMemorySetting("ImageJ.app/Contents/Info.plist");
			}
		} else
			max = getMemorySetting("ImageJ.cfg");		
		return max;
	}

	/**
	 * Show error.
	 */
	void showError() {
		int max = (int)(maxMemory()/1048576L);
		String msg =
			   "ImageJ is unable to change the memory limit. For \n"
			+ "more information, refer to the installation notes at\n \n"
			+ "    "+IJ.URL+"/docs/install/\n"
			+ " \n";
		if (fileMissing) {
			if (IJ.isMacOSX())
				msg += "The ImageJ application (ImageJ.app) was not found.\n \n";
			else if (IJ.isWindows())
				msg += "ImageJ.cfg not found.\n \n";
			fileMissing = false;
		}
		if (max>0)
			msg += "Current limit: " + max + "MB";
		IJ.showMessage("Memory", msg);
	}

	/**
	 * Gets the memory setting.
	 *
	 * @param file the file
	 * @return the memory setting
	 */
	long getMemorySetting(String file) {
		String path = Prefs.getImageJDir() + file;
		if (IJ.debugMode) IJ.log("getMemorySetting: "+path);
		f = new File(path);
		if (!f.exists()) {
			fileMissing = true;
			return 0L;
		}
		long max = 0L;
		try {
			int size = (int)f.length();
			byte[] buffer = new byte[size];
			FileInputStream in = new FileInputStream(f);
			in.read(buffer, 0, size);
			s = new String(buffer, 0, size, "ISO8859_1");
			in.close();
			index1 = s.indexOf("-mx");
			if (index1==-1) index1 = s.indexOf("-Xmx");
			if (index1==-1) return 0L;
			if (s.charAt(index1+1)=='X') index1+=4; else index1+=3;
			index2 = index1;
			while (index2<s.length()-1 && Character.isDigit(s.charAt(++index2))) {}
			String s2 = s.substring(index1, index2);
			max = (long)Tools.parseDouble(s2, 0.0)*1024*1024;
			if (index2<s.length() && s.charAt(index2)=='g')
				max = max*1024L;
		}
		catch (Exception e) {
			IJ.log(""+e);
			return 0L;
		}
		return max;
	}

	/**
	 *  Returns the maximum amount of memory this JVM will attempt to use.
	 *
	 * @return the long
	 */
	public long maxMemory() {
			return Runtime.getRuntime().maxMemory();
	}
	
}
