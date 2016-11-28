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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

// TODO: Auto-generated Javadoc
/** A modal dialog box with a one line message and
	"Don't Save", "Cancel" and "Save" buttons. */
public class SaveChangesDialog extends Dialog implements ActionListener, KeyListener {
	
	/** The save. */
	private Button dontSave, cancel, save;
	
	/** The save pressed. */
	private boolean cancelPressed, savePressed;

	/**
	 * Instantiates a new save changes dialog.
	 *
	 * @param parent the parent
	 * @param fileName the file name
	 */
	public SaveChangesDialog(Frame parent, String fileName) {
		super(parent, "Save?", true);
		setLayout(new BorderLayout());
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		Component message;
		if (fileName.startsWith("Save "))
			message = new Label(fileName);
		else {
			if (fileName.length()>22)
				message = new MultiLineLabel("Save changes to\n" + "\"" + fileName + "\"?");
			else
				message = new Label("Save changes to \"" + fileName + "\"?");
		}
		message.setFont(new Font("Dialog", Font.BOLD, 12));
		panel.add(message);
		add("Center", panel);
		
		panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));
		save = new Button("  Save  ");
		save.addActionListener(this);
		save.addKeyListener(this);
		cancel = new Button("  Cancel  ");
		cancel.addActionListener(this);
		cancel.addKeyListener(this);
		dontSave = new Button("Don't Save");
		dontSave.addActionListener(this);
		dontSave.addKeyListener(this);
		if (ij.IJ.isMacintosh()) {
			panel.add(dontSave);
			panel.add(cancel);
			panel.add(save);
		} else {
			panel.add(save);
			panel.add(dontSave);
			panel.add(cancel);
		}
		add("South", panel);
		if (ij.IJ.isMacintosh())
			setResizable(false);
		pack();
		GUI.center(this);
		show();
	}
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==cancel)
			cancelPressed = true;
		else if (e.getSource()==save)
			savePressed = true;
		closeDialog();
	}
	
	/**
	 *  Returns true if the user dismissed dialog by pressing "Cancel".
	 *
	 * @return true, if successful
	 */
	public boolean cancelPressed() {
		if (cancelPressed)
			ij.Macro.abort();
		return cancelPressed;
	}
	
	/**
	 *  Returns true if the user dismissed dialog by pressing "Save".
	 *
	 * @return true, if successful
	 */
	public boolean savePressed() {
		return savePressed;
	}
	
	/**
	 * Close dialog.
	 */
	void closeDialog() {
		//setVisible(false);
		dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode); 
		if (keyCode==KeyEvent.VK_ENTER) 
			closeDialog(); 
		else if (keyCode==KeyEvent.VK_ESCAPE) { 
			cancelPressed = true; 
			closeDialog(); 
			IJ.resetEscape();
		} 
	} 

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}

}
