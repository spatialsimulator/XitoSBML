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
import ij.Menus;
import ij.text.TextWindow;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** Lists ImageJ commands or keyboard shortcuts in a text window. */
public class CommandLister implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (arg.equals("shortcuts"))
			listShortcuts();
		else
			listCommands();
	}
	
	/**
	 * List commands.
	 */
	public void listCommands() {
		Hashtable commands = Menus.getCommands();
		Vector v = new Vector();
		for (Enumeration en=commands.keys(); en.hasMoreElements();) {
			String command = (String)en.nextElement();
			v.addElement(command+"\t"+(String)commands.get(command));
		}
		showList("Commands", "Command\tPlugin", v);
	}

	/**
	 * List shortcuts.
	 */
	public void listShortcuts() {
		Hashtable shortcuts = Menus.getShortcuts();
		Vector v = new Vector();
		addShortcutsToVector(shortcuts, v);
		Hashtable macroShortcuts = Menus.getMacroShortcuts();
		addShortcutsToVector(macroShortcuts, v);
		showList("Keyboard Shortcuts", "Hot Key\tCommand", v);
	}
	
	/**
	 * Adds the shortcuts to vector.
	 *
	 * @param shortcuts the shortcuts
	 * @param v the v
	 */
	void addShortcutsToVector(Hashtable shortcuts, Vector v) {
		for (Enumeration en=shortcuts.keys(); en.hasMoreElements();) {
			Integer key = (Integer)en.nextElement();
			int keyCode = key.intValue();
			boolean upperCase = false;
			if (keyCode>=200+65 && keyCode<=200+90) {
				upperCase = true;
				keyCode -= 200;
			}
			String shortcut = KeyEvent.getKeyText(keyCode);
			if (!upperCase && shortcut.length()==1) {
				char c = shortcut.charAt(0);
				if (c>=65 && c<=90)
					c += 32;
				char[] chars = new char[1];
				chars[0] = c;
				shortcut = new String(chars);
			}
			if (shortcut.length()>1)
				shortcut = " " + shortcut; 
			v.addElement(shortcut+"\t"+(String)shortcuts.get(key));
		}
	}

	/**
	 * Show list.
	 *
	 * @param title the title
	 * @param headings the headings
	 * @param v the v
	 */
	void showList(String title, String headings, Vector v) {
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		ArrayList list2 = new ArrayList();
		for (int i=0; i<list.length; i++)
			list2.add(list[i]);
		TextWindow tw = new TextWindow(title, headings, list2, 600, 500);
	}
}
