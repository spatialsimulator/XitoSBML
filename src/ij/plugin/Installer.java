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
import ij.Menus;
import ij.gui.GenericDialog;
import ij.io.PluginClassLoader;
import ij.util.StringSorter;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** Implements the Plugins/Shortcuts/Install... command. */
public class Installer implements PlugIn {

	/** The menus. */
	private static String[] menus = {"Shortcuts", "Plugins", "Import", "Save As",
		"Filters", "Tools", "Utilities"};
	
	/** The Constant TITLE. */
	private static final String TITLE = "Installer";
	
	/** The command. */
	private static String command = "";
	
	/** The shortcut. */
	private static String shortcut = "";
	
	/** The default plugin. */
	private static String defaultPlugin = "";
	
	/** The menu str. */
	private static String menuStr = menus[0];

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		installPlugin();
	}

	/**
	 * Install plugin.
	 */
	void installPlugin() {
		String[] plugins = getPlugins();
		if (plugins==null || plugins.length==0) {
			IJ.error("No plugins found");
			return;
		}
		GenericDialog gd = new GenericDialog("Install Plugin", IJ.getInstance());
		gd.addChoice("Plugin:", plugins, defaultPlugin);
		gd.addChoice("Menu:", menus, menuStr);
		gd.addStringField("Command:", command, 16);
		gd.addStringField("Shortcut:", shortcut, 3);
		gd.addStringField("Argument:", "", 12);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		String plugin = gd.getNextChoice();
		menuStr = gd.getNextChoice();
		command = gd.getNextString();
		shortcut = gd.getNextString();
		String argument = gd.getNextString();
		IJ.register(Installer.class);
		defaultPlugin = plugin;
		if (command.equals("")) {
			IJ.showMessage(TITLE, "Command required");
			return;
		}
		if (shortcut.length()>1)
			shortcut = shortcut.replace('f','F');
		char menu = ' ';
		if (menuStr.equals(menus[0]))
			menu = Menus.SHORTCUTS_MENU;
		else if (menuStr.equals(menus[1]))
			menu = Menus.PLUGINS_MENU;
		else if (menuStr.equals(menus[2]))
			menu = Menus.IMPORT_MENU;
		else if (menuStr.equals(menus[3]))
			menu = Menus.SAVE_AS_MENU;
		else if (menuStr.equals(menus[4]))
			menu = Menus.FILTERS_MENU;
		else if (menuStr.equals(menus[5]))
			menu = Menus.TOOLS_MENU;
		else if (menuStr.equals(menus[6]))
			menu = Menus.UTILITIES_MENU;
		if (!argument.equals(""))
			plugin += "(\"" + argument +"\")";
		int err = Menus.installPlugin(plugin,menu,command,shortcut,IJ.getInstance());
		switch (err) {
			case Menus.COMMAND_IN_USE:
				IJ.showMessage(TITLE, "The command \""+command+"\" \nis already being used.");
				return;
			case Menus.INVALID_SHORTCUT:
				IJ.showMessage(TITLE, "The shortcut must be a single character or \"F1\"-\"F12\".");
				return;
			case Menus.SHORTCUT_IN_USE:
				IJ.showMessage("The \""+shortcut+"\" shortcut is already being used.");
				return;
			default:
				command = "";
				shortcut = "";
				break;
		}
		if (!plugin.endsWith(")"))
			installAbout(plugin);
	}
	
	/**
	 * Install about.
	 *
	 * @param plugin the plugin
	 */
	void installAbout(String plugin) {
		boolean hasShowAboutMethod=false;
		PluginClassLoader loader = new PluginClassLoader(Menus.getPlugInsPath());
		try {
			Class c = loader.loadClass(plugin);
			Method m = c.getDeclaredMethod("showAbout", new Class[0]);
			if (m!=null)
				hasShowAboutMethod = true;
		}
		catch (Exception e) {}
		//IJ.write("installAbout: "+plugin+" "+hasShowAboutMethod);
		if (hasShowAboutMethod)
			Menus.installPlugin(plugin+"(\"about\")",Menus.ABOUT_MENU,plugin+"...","",IJ.getInstance());
	}
	
	/**
	 * Gets the plugins.
	 *
	 * @return the plugins
	 */
	String[] getPlugins() {
		String path = Menus.getPlugInsPath();
		if (path==null)
			return null;
		File f = new File(path);
		String[] list = f.list();
		if (list==null) return null;
		Vector v = new Vector();
		for (int i=0; i<list.length; i++) {
			String className = list[i];
			boolean isClassFile = className.endsWith(".class");
			if (className.indexOf('_')>=0 && isClassFile && className.indexOf('$')<0 ) {
				className = className.substring(0, className.length()-6); 
				v.addElement(className);
			} else {
				if (!isClassFile)
					getSubdirectoryPlugins(path, className, v);
			}
		}
		list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		return list;
	}
	
	/**
	 *  Looks for plugins in a subdirectorie of the plugins directory.
	 *
	 * @param path the path
	 * @param dir the dir
	 * @param v the v
	 * @return the subdirectory plugins
	 */
	void getSubdirectoryPlugins(String path, String dir, Vector v) {
		//IJ.write("getSubdirectoryPlugins: "+path+dir);
		if (dir.endsWith(".java"))
			return;
		File f = new File(path, dir);
		if (!f.isDirectory())
			return;
		String[] list = f.list();
		if (list==null)
			return;
		dir += "/";
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			if (name.indexOf('_')>=0 && name.endsWith(".class") && name.indexOf('$')<0 ) {
				name = name.substring(0, name.length()-6); // remove ".class"
				v.addElement(name);
				//IJ.write("File: "+f+"/"+name);
			}
		}
	}

}
