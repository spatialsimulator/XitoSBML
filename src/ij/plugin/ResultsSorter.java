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
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/** This plugin implements the Results Table's Sort command. */
public class ResultsSorter implements PlugIn {
	
	/** The parameter. */
	static String parameter = "Area";

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ResultsTable rt = ResultsTable.getResultsTable();
		int count = rt.getCounter();
		if (count==0) {
			IJ.error("Sort", "The \"Results\" table is empty");
			return;
		}
		String head= rt.getColumnHeadings();
		StringTokenizer t = new StringTokenizer(head, "\t");
		int tokens = t.countTokens()-1;
		String[] strings = new String[tokens];
		strings[0] = t.nextToken(); // first token is empty?
	   	for(int i=0; i<tokens; i++)
			strings[i] = t.nextToken();
		GenericDialog gd = new GenericDialog("Sort");
		gd.addChoice("Parameter: ", strings, strings[getIndex(strings)]);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		parameter = gd.getNextChoice ();
		float[] data = null;
		int index = rt.getColumnIndex(parameter);
		if (index>=0)
			data = rt.getColumn(index);
		if (data==null) {
			IJ.error("Sort", "No available results: \""+parameter+"\"");
			return;
		}
	}
	
	/**
	 * Gets the index.
	 *
	 * @param strings the strings
	 * @return the index
	 */
	private int getIndex(String[] strings) {
		for (int i=0; i<strings.length; i++) {
			if (strings[i].equals(parameter))
				return i;
		}
		return 0;
	}

}
