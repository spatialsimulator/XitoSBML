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

// TODO: Auto-generated Javadoc
/** Plugins that run scripts (e.g., BeanShell, Jython) extend this class. */
public abstract class PlugInInterpreter implements PlugIn {

	/**
	 *  Run script on separate thread.
	 *
	 * @param script the script
	 */
	public void run(String script) {
	}
	
	/**
	 *  Run script on current thread.
	 *
	 * @param script the script
	 * @param arg the arg
	 * @return the string
	 */
	public abstract String run(String script, String arg);
	
	/**
	 *  Returns the value returned by the script, if any, or null.
	 *
	 * @return the return value
	 */
	public abstract String getReturnValue();

	/**
	 *  Returns the name of this PlugInInterpreter.
	 *
	 * @return the name
	 */
	public abstract String getName();

	/**
	 *  Returns the import statements that are added to the script.
	 *
	 * @return the imports
	 */
	public abstract String getImports();
	
	/**
	 *  Returns the version of ImageJ at the time this plugin was created.
	 *
	 * @return the version
	 */
	public abstract String getVersion();

}
