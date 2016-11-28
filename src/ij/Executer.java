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
package ij;
import ij.io.OpenDialog;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Recorder;
import ij.text.TextWindow;
import ij.util.Tools;

import java.awt.Menu;
import java.awt.event.KeyEvent;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;


// TODO: Auto-generated Javadoc
/** Runs ImageJ menu commands in a separate thread.*/
public class Executer implements Runnable {

	/** The previous command. */
	private static String previousCommand;
	
	/** The listener. */
	private static CommandListener listener;
	
	/** The listeners. */
	private static Vector listeners = new Vector();
	
	/** The command. */
	private String command;
	
	/** The thread. */
	private Thread thread;
	
	/**
	 *  Create an Executer to run the specified menu command
	 * 		in this thread using the active image.
	 *
	 * @param cmd the cmd
	 */
	public Executer(String cmd) {
		command = cmd;
	}

	/**
	 *  Create an Executer that runs the specified menu 
	 * 		command in a separate thread using the specified image,
	 * 		or using the active image if 'imp' is null.
	 *
	 * @param cmd the cmd
	 * @param imp the imp
	 */
	public Executer(String cmd, ImagePlus imp) {
		if (cmd.startsWith("Repeat")) {
			command = previousCommand;
			IJ.setKeyUp(KeyEvent.VK_SHIFT);		
		} else {
			command = cmd;
			if (!(cmd.equals("Undo")||cmd.equals("Close")))
				previousCommand = cmd;
		}
		IJ.resetEscape();
		thread = new Thread(this, cmd);
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		if (imp!=null)
			WindowManager.setTempCurrentImage(thread, imp);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (command==null) return;
		if (listeners.size()>0) synchronized (listeners) {
			for (int i=0; i<listeners.size(); i++) {
				CommandListener listener = (CommandListener)listeners.elementAt(i);
				command = listener.commandExecuting(command);
				if (command==null) return;
			}
		}
		try {
			if (Recorder.record) {
				Recorder.setCommand(command);
				runCommand(command);
				Recorder.saveCommand();
			} else
				runCommand(command);
			int len = command.length();
			if (len>0 && command.charAt(len-1)!=']')
				IJ.setKeyUp(IJ.ALL_KEYS);  // set keys up except for "<", ">", "+" and "-" shortcuts
		} catch(Throwable e) {
			IJ.showStatus("");
			IJ.showProgress(1, 1);
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.unlock();
			String msg = e.getMessage();
			if (e instanceof OutOfMemoryError)
				IJ.outOfMemory(command);
			else if (e instanceof RuntimeException && msg!=null && msg.equals(Macro.MACRO_CANCELED))
				; //do nothing
			else {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				String s = caw.toString();
				if (IJ.isMacintosh()) {
					if (s.indexOf("ThreadDeath")>0)
						return;
					s = Tools.fixNewLines(s);
				}
				int w=500, h=340;
				if (s.indexOf("UnsupportedClassVersionError")!=-1) {
					if (s.indexOf("version 49.0")!=-1) {
						s = e + "\n \nThis plugin requires Java 1.5 or later.";
						w=700; h=150;
					}
					if (s.indexOf("version 50.0")!=-1) {
						s = e + "\n \nThis plugin requires Java 1.6 or later.";
						w=700; h=150;
					}
					if (s.indexOf("version 51.0")!=-1) {
						s = e + "\n \nThis plugin requires Java 1.7 or later.";
						w=700; h=150;
					}
				}
				if (IJ.getInstance()!=null) {
					s = IJ.getInstance().getInfo()+"\n \n"+s;
					new TextWindow("Exception", s, w, h);
				} else
					IJ.log(s);
			}
		} finally {
			if (thread!=null)
				WindowManager.setTempCurrentImage(null);
		}
	}
	    
	/**
	 * Run command.
	 *
	 * @param cmd the cmd
	 */
	void runCommand(String cmd) {
		Hashtable table = Menus.getCommands();
		String className = (String)table.get(cmd);
		if (className!=null) {
			String arg = "";
			if (className.endsWith("\")")) {
				// extract string argument (e.g. className("arg"))
				int argStart = className.lastIndexOf("(\"");
				if (argStart>0) {
					arg = className.substring(argStart+2, className.length()-2);
					className = className.substring(0, argStart);
				}
			}
			if (IJ.shiftKeyDown() && className.startsWith("ij.plugin.Macro_Runner") && !Menus.getShortcuts().contains("*"+cmd))
    			IJ.open(IJ.getDirectory("plugins")+arg);
    		else
				IJ.runPlugIn(cmd, className, arg);
		} else { // command is not a plugin
			// is command in the Plugins>Macros menu?
			if (MacroInstaller.runMacroCommand(cmd))
				return;
			// is it in the Image>Lookup Tables menu?
			if (loadLut(cmd))
				return;
			// is it in the File>Open Recent menu?
			if (openRecent(cmd))
				return;
			IJ.error("Unrecognized command: \"" + cmd+"\"");
	 	}
    }
    
    /**
     *  Opens a .lut file from the ImageJ/luts directory and returns 'true' if successful.
     *
     * @param name the name
     * @return true, if successful
     */
    public static boolean loadLut(String name) {
		String path = IJ.getDirectory("luts")+name.replace(" ","_")+".lut";
		File f = new File(path);
		if (!f.exists()) {
			path = IJ.getDirectory("luts")+name+".lut";
			f = new File(path);
		}
		if (!f.exists()) {
			path = IJ.getDirectory("luts")+name.toLowerCase().replace(" ","_")+".lut";
			f = new File(path);
		}
		if (f.exists()) {
			String dir = OpenDialog.getLastDirectory();
			IJ.open(path);
			OpenDialog.setLastDirectory(dir);
			return true;
		}
		return false;
    }

    /**
     *  Opens a file from the File/Open Recent menu 
     *  	      and returns 'true' if successful.
     *
     * @param cmd the cmd
     * @return true, if successful
     */
    boolean openRecent(String cmd) {
		Menu menu = Menus.getOpenRecentMenu();
		if (menu==null) return false;
		for (int i=0; i<menu.getItemCount(); i++) {
			if (menu.getItem(i).getLabel().equals(cmd)) {
				IJ.open(cmd);
				return true;
			}
		}
		return false;
    }

	/**
	 *  Returns the last command executed. Returns null
	 * 		if no command has been executed.
	 *
	 * @return the command
	 */
	public static String getCommand() {
		return previousCommand;
	}
	
	/**
	 *  Adds the specified command listener.
	 *
	 * @param listener the listener
	 */
	public static void addCommandListener(CommandListener listener) {
		listeners.addElement(listener);
	}
	
	/**
	 *  Removes the specified command listener.
	 *
	 * @param listener the listener
	 */
	public static void removeCommandListener(CommandListener listener) {
		listeners.removeElement(listener);
	}
	
	/**
	 * Gets the listener count.
	 *
	 * @return the listener count
	 */
	public static int getListenerCount() {
		return listeners.size();
	}

}


