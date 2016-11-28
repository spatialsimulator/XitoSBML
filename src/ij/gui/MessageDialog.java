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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// TODO: Auto-generated Javadoc
/** A modal dialog box that displays information. Based on the
	InfoDialogclass from "Java in a Nutshell" by David Flanagan. */
public class MessageDialog extends Dialog implements ActionListener, KeyListener, WindowListener {
	
	/** The button. */
	protected Button button;
	
	/** The label. */
	protected MultiLineLabel label;
	
	/** The escape pressed. */
	private boolean escapePressed;
	
	/**
	 * Instantiates a new message dialog.
	 *
	 * @param parent the parent
	 * @param title the title
	 * @param message the message
	 */
	public MessageDialog(Frame parent, String title, String message) {
		super(parent, title, true);
		setLayout(new BorderLayout());
		if (message==null) message = "";
		label = new MultiLineLabel(message);
		if (!IJ.isLinux()) label.setFont(new Font("SansSerif", Font.PLAIN, 14));
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
		panel.add(label);
		add("Center", panel);
		button = new Button("  OK  ");
		button.addActionListener(this);
		button.addKeyListener(this);
		panel = new Panel();
		panel.setLayout(new FlowLayout());
		panel.add(button);
		add("South", panel);
		if (ij.IJ.isMacintosh())
			setResizable(false);
		pack();
		GUI.center(this);
		addWindowListener(this);
		show();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode);
		escapePressed = keyCode==KeyEvent.VK_ESCAPE;
		if (keyCode==KeyEvent.VK_ENTER || escapePressed)
			dispose();
	} 
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode(); 
		IJ.setKeyUp(keyCode); 
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		dispose();
	}
	
	/**
	 * Escape pressed.
	 *
	 * @return true, if successful
	 */
	public boolean escapePressed() {
		return escapePressed;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {}
	
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

}
