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
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;

import java.io.File;

// TODO: Auto-generated Javadoc
/** This plugin implements the File/Batch/Measure command, 
	which measures all the images in a user-specified folder. */
	public class BatchMeasure implements PlugIn {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		String dir = IJ.getDirectory("Choose a Folder");
		if (dir==null) return;
		String[] list = (new File(dir)).list();
		if (list==null) return;
		Analyzer.setMeasurement(Measurements.LABELS, true);
		for (int i=0; i<list.length; i++) {
			if (list[i].startsWith(".")) continue;
			String path = dir + list[i];
			IJ.showProgress(i+1, list.length);
			IJ.redirectErrorMessages(true);
			ImagePlus imp = !path.endsWith("/")?IJ.openImage(path):null;
			IJ.redirectErrorMessages(false);
			if (imp!=null) {
				IJ.run(imp, "Measure", "");
				imp.close();
			} else if (!path.endsWith("/"))
				IJ.log("IJ.openImage() returned null: "+path);
		}
	}

}
