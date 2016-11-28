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
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.macro.MacroConstants;
import ij.macro.Program;
import ij.macro.Symbol;
import ij.macro.Tokenizer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/** Installs plugins dragged and dropped on the "ImageJ" window, or plugins,
	macros or scripts opened using the Plugins/Install command. */
public class PluginInstaller implements PlugIn {
	
	/** The Constant validExtensions. */
	public static final String[] validExtensions = {".txt",".ijm",".js",".bsh",".class",".jar",".zip",".java",".py"};

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		OpenDialog od = new OpenDialog("Install Plugin, Macro or Script...", arg);
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
				return;
		if (!validExtension(name)) {
			IJ.error("Plugin Installer", errorMessage());
			return;
		}
		String path = directory + name;
		install(path);
	}
	
	/**
	 * Install.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean install(String path) {
		boolean isURL = path.contains("://");
		String lcPath = path.toLowerCase();
		boolean isTool = lcPath.endsWith("tool.ijm") || lcPath.endsWith("tool.txt")
			|| lcPath.endsWith("tool.class") || lcPath.endsWith("tool.jar");
		boolean isMacro = lcPath.endsWith(".txt") || lcPath.endsWith(".ijm");
		byte[] data = null;
		String name = path;
		if (isURL) {
			int index = path.lastIndexOf("/");
			if (index!=-1 && index<=path.length()-1)
				name = path.substring(index+1);
			data = download(path, name);
		} else {
			File f = new File(path);
			name = f.getName();
			data = download(f);
		}
		if (data==null)
			return false;
		if (name.endsWith(".txt") && !name.contains("_"))
			name = name.substring(0,name.length()-4) + ".ijm";
		if (name.endsWith(".zip")) {
			if (!name.contains("_")) {
				IJ.error("Plugin Installer", "No underscore in file name:\n \n  "+name);
				return false;
			}
			name = name.substring(0,name.length()-4) + ".jar";
		}
		String dir = null;
		boolean isLibrary = name.endsWith(".jar") && !name.contains("_");
		if (isLibrary) {
			dir = Menus.getPlugInsPath()+"jars";
			File f = new File(dir);
			if (!f.exists()) {
				boolean ok = f.mkdir();
				if (!ok)
					dir = Menus.getPlugInsPath();
			}
		}
		if (isTool) {
			dir = Menus.getPlugInsPath()+"Tools" + File.separator;
			File f = new File(dir);
			if (!f.exists()) {
				boolean ok = f.mkdir();
				if (!ok) dir=null;
			}
			if (dir!=null && isMacro) {
				String name2 = getToolName(data);
				if (name2!=null)
					name = name2;
			}
		}
		if (dir==null) {
			SaveDialog sd = new SaveDialog("Save Plugin, Macro or Script...", Menus.getPlugInsPath(), name, null);
			String name2 = sd.getFileName();
			if (name2==null)
				return false;
			dir = sd.getDirectory();
		}
		//IJ.log(dir+"   "+Menus.getPlugInsPath());
		if (!savePlugin(new File(dir,name), data))
			return false;
		if (name.endsWith(".java"))
			IJ.runPlugIn("ij.plugin.Compiler", dir+name);
		Menus.updateImageJMenus();
		if (isTool) {
			if (isMacro)
				IJ.runPlugIn("ij.plugin.Macro_Runner", "Tools/"+name);
			else if (name.endsWith(".class")) {
				name = name.replaceAll("_"," ");
				name = name.substring(0,name.length()-6);
				IJ.run(name);
			}
		}
		return true;
	}
	
	/**
	 * Gets the tool name.
	 *
	 * @param data the data
	 * @return the tool name
	 */
	private String getToolName(byte[] data) {
		String text = new String(data);
		String name = null;
		Tokenizer tok = new Tokenizer();
		Program pgm = tok.tokenize(text);
		int[] code = pgm.getCode();
		Symbol[] symbolTable = pgm.getSymbolTable();
		for (int i=0; i<code.length; i++) {
			int token = code[i]&MacroConstants.TOK_MASK;
			if (token==MacroConstants.MACRO) {
				int nextToken = code[i+1]&MacroConstants.TOK_MASK;
				if (nextToken==MacroConstants.STRING_CONSTANT) {
					int address = code[i+1]>>MacroConstants.TOK_SHIFT;
					Symbol symbol = symbolTable[address];
					name = symbol.str;
					break;
				}
			}
		}
		if (name==null)
			return null;
		int index = name.indexOf("Tool");
		if (index==-1)
			return null;
		name = name.substring(0, index+4);
		name = name.replaceAll(" ","_");
		name = name + ".ijm";
		return name;
	}
	
	/**
	 * Save plugin.
	 *
	 * @param f the f
	 * @param data the data
	 * @return true, if successful
	 */
	boolean savePlugin(File f, byte[] data) {
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(data, 0, data.length);
			out.close();
		} catch (IOException e) {
			IJ.error("Plugin Installer", ""+e);
			return false;
		}
		return true;
	}

	/**
	 * Download.
	 *
	 * @param urlString the url string
	 * @param name the name
	 * @return the byte[]
	 */
	public static byte[] download(String urlString, String name) {
		int maxLength = 52428800; //50MB
		URL url = null;
		boolean unknownLength = false;
		byte[] data = null;;
		int n = 0;
		try {
			url = new URL(urlString);
			if (IJ.debugMode) IJ.log("Downloading: "+urlString+"  " +url);
			if (url==null)
				return null;
			URLConnection uc = url.openConnection();
			int len = uc.getContentLength();
			unknownLength = len<0;
			if (unknownLength) len = maxLength;
			if (name!=null)
				IJ.showStatus("Downloading "+url.getFile());
			InputStream in = uc.getInputStream();
			data = new byte[len];
			int lenk = len/1024;
			while (n<len) {
				int count = in.read(data, n, len-n);
				if (count<0)
					break;
				n += count;
				if (name!=null)
					IJ.showStatus("Downloading "+name+" ("+(n/1024)+"/"+lenk+"k)");
				IJ.showProgress(n, len);
			}
			in.close();
		} catch (Exception e) {
			String msg = "" + e;
			if (!msg.contains("://"))
				msg += "\n   "+urlString;
			IJ.error("Plugin Installer", msg);
			return null;
		} finally {
			IJ.showProgress(1.0);
		}
		if (name!=null) IJ.showStatus("");
		if (unknownLength) {
			byte[] data2 = data;
			data = new byte[n];
			for (int i=0; i<n; i++)
				data[i] = data2[i];
		}
		return data;
	}
	
	/**
	 * Download.
	 *
	 * @param f the f
	 * @return the byte[]
	 */
	byte[] download(File f) {
		if (!f.exists()) {
			IJ.error("Plugin Installer", "File not found: "+f);
			return null;
		}
		byte[] data = null;
		try {
			int len = (int)f.length();
			InputStream in = new BufferedInputStream(new FileInputStream(f));
			DataInputStream dis = new DataInputStream(in);
			data = new byte[len];
			dis.readFully(data);
			dis.close();
		}
		catch (Exception e) {
			IJ.error("Plugin Installer", ""+e);
			data = null;
		}
		return data;
	}
	
	/**
	 * Valid extension.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	private boolean validExtension(String name) {
		name = name.toLowerCase(Locale.US);
		boolean valid = false;
		for (int i=0; i<validExtensions.length; i++) {
			if (name.endsWith(validExtensions[i]))
				return true;
		}
		return false;
	}
	
	/**
	 * Error message.
	 *
	 * @return the string
	 */
	private String errorMessage() {
		String s = "File name must end in ";
		int len = validExtensions.length;
		for (int i=0; i<len; i++) {
			if (i==len-2)
				s += "\""+validExtensions[i]+"\" or ";
			else if (i==len-1)
				s += "\""+validExtensions[i]+"\".";
			else
				s += "\""+validExtensions[i]+"\", ";
		}
		return s;
	}
	
}
