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
import ij.Prefs;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.measure.ResultsTable;
import ij.text.TextPanel;
import ij.text.TextWindow;

import java.awt.Frame;

// TODO: Auto-generated Javadoc
/** Writes measurements to a csv or tab-delimited text file. */
public class MeasurementsWriter implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String path) {
		save(path);
	}
	
	/**
	 * Save.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean save(String path) {
		Frame frame = WindowManager.getFrontWindow();
		if (frame!=null && (frame instanceof TextWindow) && !"Log".equals(frame.getTitle())) {
			TextWindow tw = (TextWindow)frame;
			return tw.getTextPanel().saveAs(path);
		} else if (IJ.isResultsWindow()) {
			TextPanel tp = IJ.getTextPanel();
			if (tp!=null) {
				if (!tp.saveAs(path))
					return false;
			}
		} else {
			ResultsTable rt = ResultsTable.getResultsTable();
			if (rt==null || rt.getCounter()==0) {
				frame = WindowManager.getFrame("Results");
				if (frame==null || !(frame instanceof TextWindow))
					return false;
				else {
					TextWindow tw = (TextWindow)frame;
					return tw.getTextPanel().saveAs(path);
				}
			}
			if (path.equals("")) {
				SaveDialog sd = new SaveDialog("Save as Text", "Results", Prefs.get("options.ext", ".xls"));
				String file = sd.getFileName();
				if (file == null) return false;
				path = sd.getDirectory() + file;
			}
			return rt.save(path);
		}
		return true;
	}

}

