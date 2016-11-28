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
import ij.gui.GenericDialog;
import ij.macro.Interpreter;

import java.awt.Choice;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Startup command. */
	public class Startup implements PlugIn, ItemListener {
		
		/** The name. */
		private static String NAME = "RunAtStartup.ijm";
		
		/** The gd. */
		private GenericDialog gd;
		
		/** The Constant code. */
		private static final String[] code = {
			"[Select from list]",
			"Black background",
			"Debug mode",
			"10-bit (0-1023) range",
			"12-bit (0-4095) range"
		};
	
	/** The macro. */
	private String macro = "";
	
	/** The original length. */
	private int originalLength;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		macro = getStartupMacro();
		String macro2 = macro;
		if (!showDialog())
			return;
		if (!macro.equals(macro2)) {
			if (!runMacro(macro))
				return;
			saveStartupMacro(macro);
		}
	}
	
	/**
	 * Gets the startup macro.
	 *
	 * @return the startup macro
	 */
	public String getStartupMacro() {
		String macro = IJ.openAsString(IJ.getDirectory("macros")+NAME);
		if (macro==null || macro.startsWith("Error:"))
			return null;
		else
			return macro;
	}
		
	/**
	 * Save startup macro.
	 *
	 * @param macro the macro
	 */
	private void saveStartupMacro(String macro) {
		IJ.saveString(macro, IJ.getDirectory("macros")+NAME);
	}

	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	private boolean showDialog() {
		gd = new GenericDialog("Startup Macro");
		String text = "Macro code contained in this text area\nexecutes when ImageJ starts up.";
		Font font = new Font("SansSerif", Font.PLAIN, 14);
		gd.setInsets(5,15,0);
		gd.addMessage(text, font);
		gd.setInsets(5, 10, 0);
		gd.addTextAreas(macro, null, 12, 50);
		gd.addChoice("Add code:", code, code[0]);
		Vector choices = gd.getChoices();
		if (choices!=null) {
			Choice choice = (Choice)choices.elementAt(0);
			choice.addItemListener(this);
		}
		gd.showDialog();
		macro = gd.getNextText();
		return !gd.wasCanceled();
	}
	
	/**
	 * Run macro.
	 *
	 * @param macro the macro
	 * @return true, if successful
	 */
	private boolean runMacro(String macro) {
		Interpreter interp = new Interpreter();
		interp.run(macro, null);
		if (interp.wasError())
			return false;
		else
			return true;
	}
				
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		Choice choice = (Choice)e.getSource();
		String item = choice.getSelectedItem();
		String statement = null;
		if (item.equals(code[1]))
			statement = "setOption(\"BlackBackground\", true);\n";
		else if (item.equals(code[2]))
			statement = "setOption(\"DebugMode\", true);\n";
		else if (item.equals(code[3]))
			statement = "call(\"ij.ImagePlus.setDefault16bitRange\", 10);\n";
		else if (item.equals(code[4]))
			statement = "call(\"ij.ImagePlus.setDefault16bitRange\", 12);\n";
		if (statement!=null) {
			TextArea ta = gd.getTextArea1();
			ta.insert(statement, ta.getCaretPosition());
			if (IJ.isMacOSX()) ta.requestFocus();
		}
	}

}
