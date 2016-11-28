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
import ij.Executer;
import ij.IJ;
import ij.Menus;
import ij.gui.GenericDialog;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** Implements the Plugins/Hotkeys/Create Shortcut and Remove commands. */
public class Hotkeys implements PlugIn {

	/** The Constant TITLE. */
	private static final String TITLE = "Hotkeys";
	
	/** The command. */
	private static String command = "";
	
	/** The shortcut. */
	private static String shortcut = "";

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (arg.equals("install"))
			installHotkey();
		else if (arg.equals("remove"))
			removeHotkey();
		else {
			Executer e = new Executer(arg);
			e.run();
		}
		IJ.register(Hotkeys.class);
	}

	/**
	 * Install hotkey.
	 */
	void installHotkey() {
		String[] commands = getAllCommands();
		String[] shortcuts = getAvailableShortcuts();
		GenericDialog gd = new GenericDialog("Create Shortcut");
		gd.addChoice("Command:", commands, command);
		gd.addChoice("Shortcut:", shortcuts, shortcuts[0]);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		command = gd.getNextChoice();
		shortcut = gd.getNextChoice();
		String plugin = "ij.plugin.Hotkeys("+"\""+command+"\")";
		int err = Menus.installPlugin(plugin,Menus.SHORTCUTS_MENU,"*"+command,shortcut,IJ.getInstance());
		switch (err) {
			case Menus.COMMAND_IN_USE:
				IJ.showMessage(TITLE, "The command \"" + command + "\" is already installed.");
				break;
			case Menus.INVALID_SHORTCUT:
				IJ.showMessage(TITLE, "The shortcut must be a single character or F1-F24.");
				break;
			case Menus.SHORTCUT_IN_USE:
				IJ.showMessage("The \""+shortcut+"\" shortcut is already being used.");
				break;
			default:
				shortcut = "";
				break;
		}
	}
	
	/**
	 * Removes the hotkey.
	 */
	void removeHotkey() {
		String[] commands = getInstalledCommands();
		if (commands==null) {
			IJ.showMessage("Remove...", "No installed commands found.");
			return;
		}
		GenericDialog gd = new GenericDialog("Remove");
		gd.addChoice("Command:", commands, "");
		gd.addMessage("The command is not removed\nuntil ImageJ is restarted.");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		command = gd.getNextChoice();
		int err = Menus.uninstallPlugin(command);
		boolean removed = true;
		if(err==Menus.COMMAND_NOT_FOUND)
			removed = deletePlugin(command);
		if (removed) {
			IJ.showStatus("\""+command + "\" removed; ImageJ restart required");
		} else
			IJ.showStatus("\""+command + "\" not removed");
	}

	/**
	 * Delete plugin.
	 *
	 * @param command the command
	 * @return true, if successful
	 */
	boolean deletePlugin(String command) {
		String plugin = (String)Menus.getCommands().get(command);
		String name = plugin+".class";
		File file = new File(Menus.getPlugInsPath(), name);
		if (file==null || !file.exists())
			return false;
		else
			return IJ.showMessageWithCancel("Delete Plugin?", "Permanently delete \""+name+"\"?");
	}
	
	/**
	 * Gets the all commands.
	 *
	 * @return the all commands
	 */
	String[] getAllCommands() {
		Vector v = new Vector();
		Hashtable commandTable = Menus.getCommands();
		Hashtable shortcuts = Menus.getShortcuts();
		for (Enumeration en=commandTable.keys(); en.hasMoreElements();) {
			String cmd = (String)en.nextElement();
			if (!cmd.startsWith("*") && !cmd.startsWith(" ") && cmd.length()<35 && !shortcuts.contains(cmd))
				v.addElement(cmd);
		}
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		return list;
	}
	
	/**
	 * Gets the available shortcuts.
	 *
	 * @return the available shortcuts
	 */
	String[] getAvailableShortcuts() {
		Vector v = new Vector();
		Hashtable shortcuts = Menus.getShortcuts();
		for (char c = '0'; c<='9'; c++) {
			String shortcut = ""+c;
			if (!Menus.shortcutInUse(shortcut))
				v.add(shortcut);
		}
		for (char c = 'a'; c<='z'; c++) {
			String shortcut = ""+c;
			if (!Menus.shortcutInUse(shortcut))
				v.add(shortcut);
		}
		for (char c = 'A'; c<='Z'; c++) {
			String shortcut = ""+c;
			if (!Menus.shortcutInUse(shortcut))
				v.add(shortcut);
		}
		for (int i = 1; i<=12; i++) {
			String shortcut = "F"+i;
			if (!Menus.shortcutInUse(shortcut))
				v.add(shortcut);
		}
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		return list;
	}

	/**
	 * Gets the installed commands.
	 *
	 * @return the installed commands
	 */
	String[] getInstalledCommands() {
		Vector v = new Vector();
		Hashtable commandTable = Menus.getCommands();
		for (Enumeration en=commandTable.keys(); en.hasMoreElements();) {
			String cmd = (String)en.nextElement();
			if (cmd.startsWith("*"))
				v.addElement(cmd);
		}
		if (v.size()==0)
			return null;
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		return list;
	}
	
}
