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
package ij.plugin.tool;
import ij.macro.Program;
import ij.plugin.MacroInstaller;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroToolRunner.
 */
public class MacroToolRunner extends PlugInTool {
	
	/** The installer. */
	MacroInstaller installer;

	/**
	 * Instantiates a new macro tool runner.
	 *
	 * @param installer the installer
	 */
	public MacroToolRunner(MacroInstaller installer) {
		this.installer = installer;
	}
	
	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#runMacroTool(java.lang.String)
	 */
	public void runMacroTool(String name) {
		if (installer!=null)
			installer.runMacroTool(name);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#runMenuTool(java.lang.String, java.lang.String)
	 */
	public void runMenuTool(String name, String command) {
		if (installer!=null)
			installer.runMenuTool(name, command);
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getMacroProgram()
	 */
	public Program getMacroProgram() {
		if (installer!=null)
			return installer.getProgram();
		else
			return null;
	}
	
	/**
	 * Gets the macro count.
	 *
	 * @return the macro count
	 */
	public int getMacroCount() {
		if (installer!=null)
			return installer.getMacroCount();
		else
			return 0;
	}

}


