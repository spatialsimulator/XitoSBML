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
/** Plugins that acquire images or display windows should
	implement this interface. Plugins that process images 
	should implement the PlugInFilter interface. */
public interface PlugIn {

	/**
	 *  This method is called when the plugin is loaded.
	 * 		'arg', which may be blank, is the argument specified
	 * 		for this plugin in IJ_Props.txt.
	 *
	 * @param arg the arg
	 */ 
	public void run(String arg);
	
}
