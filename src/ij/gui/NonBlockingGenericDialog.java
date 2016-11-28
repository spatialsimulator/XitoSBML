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
package ij.gui;
import ij.IJ;
import ij.WindowManager;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

// TODO: Auto-generated Javadoc
/** This is an extension of GenericDialog that is non-model.
 *	@author Johannes Schindelin
 */
public class NonBlockingGenericDialog extends GenericDialog {
	
	/**
	 * Instantiates a new non blocking generic dialog.
	 *
	 * @param title the title
	 */
	public NonBlockingGenericDialog(String title) {
		super(title, null);
		setModal(false);
	}

	/* (non-Javadoc)
	 * @see ij.gui.GenericDialog#showDialog()
	 */
	public synchronized void showDialog() {
		super.showDialog();
		if (!IJ.macroRunning()) { // add to Window menu on event dispatch thread
			final NonBlockingGenericDialog thisDialog = this;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					WindowManager.addWindow(thisDialog);
				}
			});
		}
		try {
			wait();
		} catch (InterruptedException e) { }
	}

	/* (non-Javadoc)
	 * @see ij.gui.GenericDialog#actionPerformed(java.awt.event.ActionEvent)
	 */
	public synchronized void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (!isVisible())
			notify();
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.GenericDialog#keyPressed(java.awt.event.KeyEvent)
	 */
	public synchronized void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if (wasOKed() || wasCanceled())
			notify();
	}

    /* (non-Javadoc)
     * @see ij.gui.GenericDialog#windowClosing(java.awt.event.WindowEvent)
     */
    public synchronized void windowClosing(WindowEvent e) {
		super.windowClosing(e);
		if (wasOKed() || wasCanceled())
			notify();
    }
    
	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose() {
		super.dispose();
		WindowManager.removeWindow(this);
	}

}
