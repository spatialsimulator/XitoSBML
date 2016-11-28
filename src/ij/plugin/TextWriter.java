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
import ij.WindowManager;
import ij.plugin.frame.Editor;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.Frame;

// TODO: Auto-generated Javadoc
/** This plugin implements the File/Save As/Text command. What it does
	is save the contents of TextWindows (e.g., "Log" and "Results"). */
public class TextWriter implements PlugIn {
    
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		saveText();
	}
	
	/**
	 * Save text.
	 */
	void saveText() {
		Frame frame = WindowManager.getFrontWindow();
		if (frame!=null && (frame instanceof TextWindow)) {
			TextPanel tp = ((TextWindow)frame).getTextPanel();
			tp.saveAs("");
		} else if (frame!=null && (frame instanceof Editor)) {
			Editor ed = (Editor)frame;
			ed.saveAs();
		} else {
			IJ.error("Save As Text",
				"This command requires a TextWindow, such\n"
				+ "as the \"Log\" window, or an Editor window. Use\n"
				+ "File>Save>Text Image to save an image as text.");
		}
	}
	
}

