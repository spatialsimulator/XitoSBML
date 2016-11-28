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
import ij.io.FileSaver;
import ij.io.Opener;

import java.util.Hashtable;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/** The class contains static methods that perform macro operations. */
public class Macro {

	/** The Constant MACRO_CANCELED. */
	public static final String MACRO_CANCELED = "Macro canceled";

	// A table of Thread as keys and String as values, so  
	/** The table. */
	// Macro options are local to each calling thread.
	static private Hashtable table = new Hashtable();
	
	/** The abort. */
	static boolean abort;

	/**
	 * Open.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public static boolean open(String path) {
		if (path==null || path.equals("")) {
			Opener o = new Opener();
			return true;
		}
		Opener o = new Opener();
		ImagePlus img = o.openImage(path);
		if (img==null)
			return false;
		img.show();	
		return true;
	}

	/**
	 * Save as.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public static boolean saveAs(String path) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			return false;
		FileSaver fs = new FileSaver(imp);
		if (path==null || path.equals(""))
			return fs.saveAsTiff();
		if (imp.getStackSize()>1)
			return fs.saveAsTiffStack(path);
		else
			return fs.saveAsTiff(path);
	}

	/**
	 * Gets the name.
	 *
	 * @param path the path
	 * @return the name
	 */
	public static String getName(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0)
			return path.substring(i+1);
		else
			return path;
	}
	
	/**
	 * Gets the dir.
	 *
	 * @param path the path
	 * @return the dir
	 */
	public static String getDir(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0)
			return path.substring(0, i+1);
		else
			return "";
	}
	
	/** Aborts the currently running macro or any plugin using IJ.run(). */
	public static void abort() {
		abort = true;
		//IJ.log("Abort: "+Thread.currentThread().getName());
		if (Thread.currentThread().getName().endsWith("Macro$")) {
			table.remove(Thread.currentThread());
			throw new RuntimeException(MACRO_CANCELED);
		}
	}

	/**
	 *  If a command started using run(name, options) is running,
	 * 		and the current thread is the same thread,
	 * 		returns the options string, otherwise, returns null.
	 *
	 * @return the options
	 * @see ij.gui.GenericDialog
	 * @see ij.io.OpenDialog
	 */
	public static String getOptions() {
		String threadName = Thread.currentThread().getName();
		//IJ.log("getOptions: "+threadName+" "+Thread.currentThread().hashCode()); //ts
		if (threadName.startsWith("Run$_")||threadName.startsWith("RMI TCP")) {
			Object options = table.get(Thread.currentThread());
			return options==null?null:options+" ";
		} else
			return null;
	}

	/**
	 *  Define a set of Macro options for the current Thread.
	 *
	 * @param options the new options
	 */
	public static void setOptions(String options) {
		//IJ.log("setOptions: "+Thread.currentThread().getName()+" "+Thread.currentThread().hashCode()+" "+options); //ts
		if (options==null || options.equals(""))
			table.remove(Thread.currentThread());
		else
			table.put(Thread.currentThread(), options);
	}

	/**
	 *  Define a set of Macro options for a Thread.
	 *
	 * @param thread the thread
	 * @param options the options
	 */
	public static void setOptions(Thread thread, String options) {
		if (null==thread)
			throw new RuntimeException("Need a non-null thread instance");
		if (null==options)
			table.remove(thread);
		else
			table.put(thread, options);
	}

	/**
	 * Gets the value.
	 *
	 * @param options the options
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the value
	 */
	public static String getValue(String options, String key, String defaultValue) {
		key = trimKey(key);
        key += '=';
		int index=-1;
		do { // Require that key not be preceded by a letter
			index = options.indexOf(key, ++index);
			if (index<0) return defaultValue;
		} while (index!=0&&Character.isLetter(options.charAt(index-1)));
		options = options.substring(index+key.length(), options.length());
		if (options.charAt(0)=='\'') {
			index = options.indexOf("'",1);
			if (index<0)
				return defaultValue;
			else
				return options.substring(1, index);
		} else if (options.charAt(0)=='[') {
			index = options.indexOf("]",1);
			if (index<=1)
				return defaultValue;
			else
				return options.substring(1, index);
		} else {
			//if (options.indexOf('=')==-1) {
			//	options = options.trim();
			//	IJ.log("getValue: "+key+"  |"+options+"|");
			//	if (options.length()>0)
			//		return options;
			//	else
			//		return defaultValue;
			//}
			index = options.indexOf(" ");
			if (index<0)
				return defaultValue;
			else
				return options.substring(0, index);
		}
	}
	
	/**
	 * Trim key.
	 *
	 * @param key the key
	 * @return the string
	 */
	public static String trimKey(String key) {
		int index = key.indexOf(" ");
		if (index>-1)
			key = key.substring(0,index);
		index = key.indexOf(":");
		if (index>-1)
			key = key.substring(0,index);
		key = key.toLowerCase(Locale.US);
		return key;
	}

}

