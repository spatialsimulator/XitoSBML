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

	// TODO: Auto-generated Javadoc
/**
	 *  Plugins that implement this interface are notified when ImageJ
	 * 		is about to run a menu command. There is an example plugin at
	 * 		http://imagej.nih.gov/ij/plugins/download/misc/Command_Listener.java
	 *
	 * @see CommandEvent
	 */
	public interface CommandListener {

	/**
	 * Command executing.
	 *
	 * @param command the command
	 * @return the string
	 */
	/*	The method is called when ImageJ is about to run a menu command, 
		where 'command' is the name of the command. Return this string 
		and ImageJ will run the command, return a different command name
		and ImageJ will run that command, or return null to not run a command.
	*/
	public String commandExecuting(String command);

}
