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
import ij.ImagePlus;
import ij.gui.Toolbar;
import ij.macro.Program;
import ij.plugin.PlugIn;

import java.awt.event.MouseEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class PlugInTool.
 */
public abstract class PlugInTool implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		Toolbar.addPlugInTool(this);
	}
	
	/**
	 * Mouse pressed.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mousePressed(ImagePlus imp, MouseEvent e) {e.consume();}

	/**
	 * Mouse released.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseReleased(ImagePlus imp, MouseEvent e) {e.consume();}

	/**
	 * Mouse clicked.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseClicked(ImagePlus imp, MouseEvent e) {e.consume();}

	/**
	 * Mouse dragged.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseDragged(ImagePlus imp, MouseEvent e) {e.consume();}
	
	/**
	 * Mouse moved.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseMoved(ImagePlus imp, MouseEvent e) { }
	
	/**
	 * Mouse entered.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseEntered(ImagePlus imp, MouseEvent e) {e.consume();}

	/**
	 * Mouse exited.
	 *
	 * @param imp the imp
	 * @param e the e
	 */
	public void mouseExited(ImagePlus imp, MouseEvent e) {e.consume();}
	
	/**
	 * Show popup menu.
	 *
	 * @param e the e
	 * @param tb the tb
	 */
	public void showPopupMenu(MouseEvent e, Toolbar tb) { }

	/**
	 *  Return the tool name.
	 *
	 * @return the tool name
	 */
	public String getToolName() {
		return getClass().getName().replace('_', ' ');
	}
	
	/**
	 *  Return the string encoding of the tool icon. See
	 * 		http://rsb.info.nih.gov/ij/developer/macro/macros.html#icons
	 * 		The default icon is the first letter of the tool name.
	 *
	 * @return the tool icon
	 */
	public String getToolIcon() {
		String letter = getToolName();
		if (letter!=null && letter.length()>0)
			letter = letter.substring(0,1);
		else
			letter = "P";
		return "C037T5f16"+letter;
	}
	
	/**
	 * Show options dialog.
	 */
	public void showOptionsDialog() {
	}

	/**
	 *  These methods are overridden by MacroToolRunner.
	 *
	 * @param name the name
	 */
	public void runMacroTool(String name) { }
	
	/**
	 * Run menu tool.
	 *
	 * @param name the name
	 * @param command the command
	 */
	public void runMenuTool(String name, String command) { }
	
	/**
	 * Gets the macro program.
	 *
	 * @return the macro program
	 */
	public Program getMacroProgram() {return null;}

}
