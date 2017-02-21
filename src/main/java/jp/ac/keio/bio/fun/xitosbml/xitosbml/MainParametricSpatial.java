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
package jp.ac.keio.bio.fun.xitosbml.xitosbml;
import javax.swing.JOptionPane;

import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;


// TODO: Auto-generated Javadoc
/**
 * The Class MainParametricSpatial.
 */
public class MainParametricSpatial extends MainSpatial{
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) {
		/*
		Frame[] f = ImageJ.getFrames();
		
		Frame frame = null;
		for(int i = 0 ; i < f.length ; i++){
			if(f[i].getTitle().equals("ImageJ 3D Viewer")) frame = f[i];
		}
		if(frame == null){
			IJ.error("3D Viewer not opened");
			return;
		}
		ImageWindow3D win = (ImageWindow3D) frame;
		univ = (Image3DUniverse) win.getUniverse();
		*/

		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		visualize(spImg);
		viewer.findPoints();
		sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
		
		
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addSBases();

		sbmlexp.addCoordParameter();
		document = sbmlexp.getDocument();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
	}
}
