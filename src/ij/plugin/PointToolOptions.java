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
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;

import java.awt.AWTEvent;
import java.awt.Color;


// TODO: Auto-generated Javadoc
/** This plugin implements the Edit/Options/Point Tool command. */
public class PointToolOptions implements PlugIn, DialogListener {
	
	/** The multipoint tool. */
	boolean multipointTool;

 	/* (non-Javadoc)
	  * @see ij.plugin.PlugIn#run(java.lang.String)
	  */
	 public void run(String arg) {
 		showDialog();
 	}
		
	/**
	 * Show dialog.
	 */
	void showDialog() {
		String options = IJ.isMacro()?Macro.getOptions():null;
		if (options!=null) {
			options = options.replace("selection=", "color=");
			options = options.replace("marker=", "size=");
			Macro.setOptions(options);
		}
		multipointTool = IJ.getToolName().equals("multipoint");
		Color sc =Roi.getColor();
		String sname = Colors.getColorName(sc, "Yellow");
		Color cc =PointRoi.getDefaultCrossColor();
		String cname = Colors.getColorName(cc, "None");
		String type = PointRoi.types[PointRoi.getDefaultType()];
		String size = PointRoi.sizes[PointRoi.getDefaultSize()];
		GenericDialog gd = new GenericDialog("Point Tool");
		gd.setInsets(5,0,2);
		gd.addChoice("Type:", PointRoi.types, type);
		gd.addChoice("Color:", Colors.getColors(), sname);
		gd.addChoice("Size:", PointRoi.sizes, size);
		if (!multipointTool) {
			gd.addCheckbox("Auto-measure", Prefs.pointAutoMeasure);
			gd.addCheckbox("Auto-next slice", Prefs.pointAutoNextSlice);
			gd.addCheckbox("Add_to overlay", Prefs.pointAddToOverlay);
			gd.addCheckbox("Add to ROI Manager", Prefs.pointAddToManager);
		}
		gd.addCheckbox("Label points", !Prefs.noPointLabels);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled()) {
		}
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		boolean redraw = false;
		// type
		int index = gd.getNextChoiceIndex();
		if (index!=PointRoi.getDefaultType()) {
			PointRoi.setDefaultType(index);
			redraw = true;
		}
		// color
		String selectionColor = gd.getNextChoice();
		Color sc = Colors.getColor(selectionColor, Color.yellow);
		if (sc!=Roi.getColor()) {
			Roi.setColor(sc);
			redraw = true;
			Toolbar.getInstance().repaint();
		}
		// size
		index = gd.getNextChoiceIndex();
		if (index!=PointRoi.getDefaultSize()) {
			PointRoi.setDefaultSize(index);
			redraw = true;
		}
		if (!multipointTool) {
			Prefs.pointAutoMeasure = gd.getNextBoolean();
			Prefs.pointAutoNextSlice = gd.getNextBoolean();
			Prefs.pointAddToOverlay = gd.getNextBoolean();
			Prefs.pointAddToManager = gd.getNextBoolean();
			if (Prefs.pointAddToOverlay)
				Prefs.pointAddToManager = false;
			if (Prefs.pointAutoNextSlice&&!Prefs.pointAddToManager)
				Prefs.pointAutoMeasure = true;
		}
		boolean noPointLabels = !gd.getNextBoolean();
		if (noPointLabels!=Prefs.noPointLabels)
			redraw = true;
		Prefs.noPointLabels = noPointLabels;
		if (redraw) {
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) {
				Roi roi = imp.getRoi();
				if (roi instanceof PointRoi)
					((PointRoi)roi).setShowLabels(!Prefs.noPointLabels);
				imp.draw();
			}
		}
		return true;
    }
    			
}
