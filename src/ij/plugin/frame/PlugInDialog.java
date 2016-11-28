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
package ij.plugin.frame;
import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import ij.WindowManager;
import ij.plugin.PlugIn;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// TODO: Auto-generated Javadoc
/**  This is a non-modal dialog that plugins can extend. */
public class PlugInDialog extends Dialog implements PlugIn, WindowListener, FocusListener {

	/** The title. */
	String title;
	
	/**
	 * Instantiates a new plug in dialog.
	 *
	 * @param title the title
	 */
	public PlugInDialog(String title) {
		super(IJ.isMacOSX()?IJ.getInstance():IJ.isJava16()?null:new Frame(),title);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.title = title;
		ImageJ ij = IJ.getInstance();
		if (IJ.isMacOSX() && ij!=null) {
			ij.toFront(); // needed for keyboard shortcuts to work
			ij.setMenuBar(Menus.getMenuBar());
		}
		addWindowListener(this);
 		addFocusListener(this);
		if (IJ.isLinux()) setBackground(ImageJ.backgroundColor);
		if (ij!=null && !IJ.isMacOSX() && IJ.isJava16()) {
			Image img = ij.getIconImage();
			if (img!=null)
				try {setIconImage(img);} catch (Exception e) {}
		}
	}
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
	}
	
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing(WindowEvent e) {
    	if (e.getSource()==this) {
    		close();
    		if (Recorder.record)
    			Recorder.record("run", "Close");
    	}
    }
    
    /** Closes this window. */
    public void close() {
		//setVisible(false);
		dispose();
		WindowManager.removeWindow(this);
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent e) {
		ImageJ ij = IJ.getInstance();
		if (IJ.isMacOSX() && ij!=null) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			ij.setMenuBar(Menus.getMenuBar());
		}
		WindowManager.setWindow(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		//IJ.log("PlugInFrame: focusGained");
		WindowManager.setWindow(this);
	}

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    public void windowOpened(WindowEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    public void windowIconified(WindowEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    public void windowDeiconified(WindowEvent e) {}
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    public void windowDeactivated(WindowEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {}
}
