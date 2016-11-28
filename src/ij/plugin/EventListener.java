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
import ij.CommandListener;
import ij.Executer;
import ij.IJ;
import ij.IJEventListener;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.RoiListener;
import ij.gui.Toolbar;

// TODO: Auto-generated Javadoc
/**
 *  This plugin implements the Plugins/Utilities/Monitor Events command.
 * 	By implementing the IJEventListener, CommandListener, ImageListener
 * 	and RoiListener interfaces, it is able to monitor foreground and background
 * 	color changes, tool switches, Log window closings, command executions, image
 * 	window openings, closings and updates, and ROI changes.
 *
 * @see EventEvent
 */
public class EventListener implements PlugIn, IJEventListener, ImageListener, RoiListener, CommandListener {

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		IJ.addEventListener(this);
		Executer.addCommandListener(this);
		ImagePlus.addImageListener(this);
		Roi.addRoiListener(this);
		IJ.log("EventListener started");
	}
	
	/* (non-Javadoc)
	 * @see ij.IJEventListener#eventOccurred(int)
	 */
	public void eventOccurred(int eventID) {
		switch (eventID) {
			case IJEventListener.FOREGROUND_COLOR_CHANGED:
				String c = Integer.toHexString(Toolbar.getForegroundColor().getRGB());
				c = "#"+c.substring(2);
				IJ.log("Changed foreground color to "+c);
				break;
			case IJEventListener.BACKGROUND_COLOR_CHANGED:
				c = Integer.toHexString(Toolbar.getBackgroundColor().getRGB());
				c = "#"+c.substring(2);
				IJ.log("Changed background color to "+c);
				break;
			case IJEventListener.TOOL_CHANGED:
				String name = IJ.getToolName();
				IJ.log("Switched to the "+name+(name.endsWith("Tool")?"":" tool"));
				break;
			case IJEventListener.COLOR_PICKER_CLOSED:
				IJ.log("Color picker closed");
				break;
			case IJEventListener.LOG_WINDOW_CLOSED:
				IJ.removeEventListener(this);
				Executer.removeCommandListener(this);
				ImagePlus.removeImageListener(this);
				Roi.removeRoiListener(this);
				IJ.showStatus("Log window closed; EventListener stopped");
				break;
		}
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageOpened(ij.ImagePlus)
	 */
	// called when an image is opened
	public void imageOpened(ImagePlus imp) {
		IJ.log("Opened \""+imp.getTitle()+"\"");
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageClosed(ij.ImagePlus)
	 */
	// Called when an image is closed
	public void imageClosed(ImagePlus imp) {
		IJ.log("Closed \""+imp.getTitle()+"\"");
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageUpdated(ij.ImagePlus)
	 */
	// Called when an image's pixel data is updated
	public void imageUpdated(ImagePlus imp) {
		IJ.log("Updated \""+imp.getTitle()+"\"");
	}
	
	/* (non-Javadoc)
	 * @see ij.CommandListener#commandExecuting(java.lang.String)
	 */
	public String commandExecuting(String command) {
		IJ.log("Executed \""+command+"\" command");
		return command;
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.RoiListener#roiModified(ij.ImagePlus, int)
	 */
	public  void roiModified(ImagePlus img, int id) {
		String type = "UNKNOWN";
		switch (id) {
			case CREATED: type="CREATED"; break;
			case MOVED: type="MOVED"; break;
			case MODIFIED: type="MODIFIED"; break;
			case EXTENDED: type="EXTENDED"; break;
			case COMPLETED: type="COMPLETED"; break;
			case DELETED: type="DELETED"; break;
		}
		IJ.log("ROI Modified: "+(img!=null?img.getTitle():"")+", "+type);
	}


}
