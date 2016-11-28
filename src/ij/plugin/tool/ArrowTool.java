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
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Arrow;
import ij.gui.ImageCanvas;
import ij.gui.Roi;

import java.awt.event.MouseEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class ArrowTool.
 */
public class ArrowTool extends PlugInTool {
	
	/** The arrow. */
	Roi arrow;

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mousePressed(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mousePressed(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		int sx = e.getX();
		int sy = e.getY();
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		Roi roi = imp.getRoi();
		int handle = roi!=null?roi.isHandle(ox, oy):-1;
		if (!(roi!=null && (roi instanceof Arrow) && (handle>=0||roi.contains(ox,oy)))) {
			arrow = new Arrow(sx, sy, imp);
			imp.setRoi(arrow, false);
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseDragged(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseDragged(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		int sx = e.getX();
		int sy = e.getY();
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		Roi roi = imp.getRoi();
		if (roi!=null && (roi instanceof Arrow) && roi.contains(ox,oy))
			roi.mouseDragged(e);
		else if (arrow!=null)
			arrow.mouseDragged(e);
		e.consume();
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#mouseReleased(ij.ImagePlus, java.awt.event.MouseEvent)
	 */
	public void mouseReleased(ImagePlus imp, MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		int sx = e.getX();
		int sy = e.getY();
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		Roi roi = imp.getRoi();
		if (arrow!=null && !(roi!=null && (roi instanceof Arrow) && roi.contains(ox,oy))) {
			arrow.mouseReleased(e);
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#showOptionsDialog()
	 */
	public void showOptionsDialog() {
		IJ.doCommand("Arrow Tool...");
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getToolIcon()
	 */
	public String getToolIcon() {
		return "B10C037L0fe1L65e1La9e1L65a9C123L8586L9497La4a7Lb3b6Lc3c4";
	}

	/* (non-Javadoc)
	 * @see ij.plugin.tool.PlugInTool#getToolName()
	 */
	public String getToolName() {
		return "Arrow Tool";
	}
	
}


