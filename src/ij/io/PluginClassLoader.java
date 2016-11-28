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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

// TODO: Auto-generated Javadoc
/** ImageJ uses this class loader to load plugins and resources from the
 * plugins directory and immediate subdirectories. This class loader will
 * also load classes and resources from JAR files.
 *
 * <p> The class loader searches for classes and resources in the following order:
 * <ol>
 *  <li> Plugins directory</li>
 *  <li> Subdirectories of the Plugins directory</li>
 *  <li> JAR and ZIP files in the plugins directory and subdirectories</li>
 * </ol>
 * <p> The class loader does not recurse into subdirectories beyond the first level.
*/
public class PluginClassLoader extends URLClassLoader {
    
    /** The path. */
    protected String path;

    /**
     * Creates a new PluginClassLoader that searches in the directory path
     * passed as a parameter. The constructor automatically finds all JAR and ZIP
     * files in the path and first level of subdirectories. The JAR and ZIP files
     * are stored in a Vector for future searches.
     * @param path the path to the plugins directory.
     */
	public PluginClassLoader(String path) {
		super(new URL[0], IJ.class.getClassLoader());
		init(path);
	}
	
	/**
	 *  This version of the constructor is used when ImageJ is launched using Java WebStart.
	 *
	 * @param path the path
	 * @param callSuper the call super
	 */
	public PluginClassLoader(String path, boolean callSuper) {
		super(new URL[0], Thread.currentThread().getContextClassLoader());
		init(path);
	}

	/**
	 * Inits the.
	 *
	 * @param path the path
	 */
	void init(String path) {
		this.path = path;

		//find all JAR files on the path and subdirectories
		File f = new File(path);
        try {
            // Add plugin directory to search path
            addURL(f.toURI().toURL());
        } catch (MalformedURLException e) {
            ij.IJ.log("PluginClassLoader: "+e);
        }
		String[] list = f.list();
		if (list==null)
			return;
		for (int i=0; i<list.length; i++) {
			if (list[i].equals(".rsrc"))
				continue;
			File f2=new File(path, list[i]);
			if (f2.isDirectory())
				addDirectory(f2);
			else 
				addJar(f2);
		}
		addDirectory(f, "jars"); // add ImageJ/jars; requested by Wilhelm Burger
	}

	/**
	 * Adds the directory.
	 *
	 * @param f the f
	 */
	private void addDirectory(File f) {
		if (IJ.debugMode) IJ.log("PluginClassLoader.addDirectory: "+f);
		try {
			// Add first level subdirectories to search path
			addURL(f.toURI().toURL());
		} catch (MalformedURLException e) {
			ij.IJ.log("PluginClassLoader: "+e);
		}
		String[] innerlist = f.list();
		if (innerlist==null)
			return;
		for (int j=0; j<innerlist.length; j++) {
			File g = new File(f,innerlist[j]);
			if (g.isFile())
				addJar(g);
		}
	}

    /**
     * Adds the jar.
     *
     * @param f the f
     */
    private void addJar(File f) {
        if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
			if (IJ.debugMode) IJ.log("PluginClassLoader.addJar: "+f);
            try {
                addURL(f.toURI().toURL());
            } catch (MalformedURLException e) {
				ij.IJ.log("PluginClassLoader: "+e);
            }
        }
    }

	/**
	 * Adds the directory.
	 *
	 * @param f the f
	 * @param name the name
	 */
	private void addDirectory(File f, String name) {
		f = f.getParentFile();
		if (f==null)
			return;
		f = new File(f, name);
		if (f==null)
			return;
		if (f.isDirectory())
			addDirectory(f);
	}

}
