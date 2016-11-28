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
package ij.macro;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.Menus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.plugin.frame.Editor;

import java.awt.PopupMenu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
																																																																																																																																																					   

// TODO: Auto-generated Javadoc
/** This class runs macros in a separate thread. */
public class MacroRunner implements Runnable {

	/** The macro. */
	private String macro;
	
	/** The pgm. */
	private Program pgm;
	
	/** The address. */
	private int address;
	
	/** The name. */
	private String name;
	
	/** The thread. */
	private Thread thread;
	
	/** The argument. */
	private String argument;
	
	/** The editor. */
	private Editor editor;

	/** Create a MacrRunner. */
	public MacroRunner() {
	}

	/**
	 *  Create a new object that interprets macro source in a separate thread.
	 *
	 * @param macro the macro
	 */
	public MacroRunner(String macro) {
		this(macro, (Editor)null);
	}

	/**
	 *  Create a new object that interprets macro source in debug mode if 'editor' is not null.
	 *
	 * @param macro the macro
	 * @param editor the editor
	 */
	public MacroRunner(String macro, Editor editor) {
		this.macro = macro;
		this.editor = editor;
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 *  Create a new object that interprets macro source in a 
	 * 		separate thread, and also passing a string argument.
	 *
	 * @param macro the macro
	 * @param argument the argument
	 */
	public MacroRunner(String macro, String argument) {
		this.macro = macro;
		this.argument = argument;
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 *  Create a new object that interprets a macro file using a separate thread.
	 *
	 * @param file the file
	 */
	public MacroRunner(File file) {
		int size = (int)file.length();
		if (size<=0)
			return;
		try {
			StringBuffer sb = new StringBuffer(5000);
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (true) {
				String s=r.readLine();
				if (s==null)
					break;
				else
					sb.append(s+"\n");
			}
			r.close();
			macro = new String(sb);
		}
		catch (Exception e) {
			IJ.error(e.getMessage());
			return;
		}
		thread = new Thread(this, "Macro$"); 
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 *  Create a new object that runs a tokenized macro in a separate thread.
	 *
	 * @param pgm the pgm
	 * @param address the address
	 * @param name the name
	 */
	public MacroRunner(Program pgm, int address, String name) {
		this(pgm, address, name, (String)null);
	}

	/**
	 *  Create a new object that runs a tokenized macro in a separate thread,
	 * 		passing a string argument.
	 *
	 * @param pgm the pgm
	 * @param address the address
	 * @param name the name
	 * @param argument the argument
	 */
	public MacroRunner(Program pgm, int address, String name, String argument) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		this.argument = argument;
		thread = new Thread(this, name+"_Macro$");
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 *  Create a new object that runs a tokenized macro in debug mode if 'editor' is not null.
	 *
	 * @param pgm the pgm
	 * @param address the address
	 * @param name the name
	 * @param editor the editor
	 */
	public MacroRunner(Program pgm, int address, String name, Editor editor) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		this.editor = editor;
		thread = new Thread(this, name+"_Macro$");
		thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
		thread.start();
	}

	/**
	 *  Runs tokenized macro on current thread if pgm.queueCommands is true.
	 *
	 * @param pgm the pgm
	 * @param address the address
	 * @param name the name
	 */
	public void runShortcut(Program pgm, int address, String name) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		if (pgm.queueCommands)
			run();
		else {
			thread = new Thread(this, name+"_Macro$");
			thread.setPriority(Math.max(thread.getPriority()-2, Thread.MIN_PRIORITY));
			thread.start();
		}
	}
	
	/**
	 *  Runs a tokenized macro on the current thread.
	 *
	 * @param pgm the pgm
	 * @param address the address
	 * @param name the name
	 */
	public void run(Program pgm, int address, String name) {
		this.pgm = pgm;
		this.address = address;
		this.name = name;
		this.argument = null;
		run();
	}

	/**
	 * Gets the thread.
	 *
	 * @return the thread
	 */
	public Thread getThread() {
		return thread;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Interpreter interp = new Interpreter();
		interp.argument = argument;
		if (editor!=null)
			interp.setDebugger(editor);
		try {
			if (pgm==null)
				interp.run(macro);
			else {
				if ("Popup Menu".equals(name)) {
					PopupMenu popup = Menus.getPopupMenu();
					if (popup!=null) {
						ImagePlus imp = null;
						Object parent = popup.getParent();
						if (parent instanceof ImageCanvas)
							imp = ((ImageCanvas)parent).getImage();
						if (imp!=null)
							WindowManager.setTempCurrentImage(Thread.currentThread(), imp);
					}
				}
				interp.runMacro(pgm, address, name);
			}
		} catch(Throwable e) {
			interp.abortMacro();
			IJ.showStatus("");
			IJ.showProgress(1.0);
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.unlock();
			String msg = e.getMessage();
			if (e instanceof RuntimeException && msg!=null && e.getMessage().equals(Macro.MACRO_CANCELED))
				return;
			IJ.handleException(e);
		} finally {
			if (thread!=null)
				WindowManager.setTempCurrentImage(null);
		}
	}

}

