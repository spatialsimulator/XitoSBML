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
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// TODO: Auto-generated Javadoc
/** A modal dialog box with a one line message and
	"Yes", "No" and "Cancel" buttons. */
public class YesNoCancelDialog extends Dialog implements ActionListener, KeyListener, WindowListener {
    
    /** The cancel B. */
    private Button yesB, noB, cancelB;
    
    /** The yes pressed. */
    private boolean cancelPressed, yesPressed;
	
	/** The first paint. */
	private boolean firstPaint = true;

	/**
	 * Instantiates a new yes no cancel dialog.
	 *
	 * @param parent the parent
	 * @param title the title
	 * @param msg the msg
	 */
	public YesNoCancelDialog(Frame parent, String title, String msg) {
		super(parent, title, true);
		setLayout(new BorderLayout());
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		MultiLineLabel message = new MultiLineLabel(msg);
		message.setFont(new Font("Dialog", Font.PLAIN, 12));
		panel.add(message);
		add("North", panel);
		
		panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 8));
		if (IJ.isMacintosh() && msg.startsWith("Save")) {
			yesB = new Button("  Save  ");
			noB = new Button("Don't Save");
			cancelB = new Button("  Cancel  ");
		} else {
			yesB = new Button("  Yes  ");
			noB = new Button("  No  ");
			cancelB = new Button(" Cancel ");
		}
		yesB.addActionListener(this);
		noB.addActionListener(this);
		cancelB.addActionListener(this);
		yesB.addKeyListener(this);
		noB.addKeyListener(this);
		cancelB.addKeyListener(this);
		if (IJ.isMacintosh()) {
			panel.add(noB);
			panel.add(cancelB);
			panel.add(yesB);
			setResizable(false);
		} else {
			panel.add(yesB);
			panel.add(noB);
			panel.add(cancelB);
		}
		add("South", panel);
		addWindowListener(this);
		pack();
		GUI.center(this);
		show();
	}
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==cancelB)
			cancelPressed = true;
		else if (e.getSource()==yesB)
			yesPressed = true;
		closeDialog();
	}
	
	/**
	 *  Returns true if the user dismissed dialog by pressing "Cancel".
	 *
	 * @return true, if successful
	 */
	public boolean cancelPressed() {
		return cancelPressed;
	}

	/**
	 *  Returns true if the user dismissed dialog by pressing "Yes".
	 *
	 * @return true, if successful
	 */
	public boolean yesPressed() {
		return yesPressed;
	}
	
	/**
	 * Close dialog.
	 */
	void closeDialog() {
		dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode); 
		if (keyCode==KeyEvent.VK_ENTER) {
			if (cancelB.isFocusOwner()) {
				cancelPressed = true; 
				closeDialog(); 
			} else if (noB.isFocusOwner()) {
				closeDialog(); 
			} else {
				yesPressed = true;
				closeDialog(); 
			}
		} else if (keyCode==KeyEvent.VK_Y||keyCode==KeyEvent.VK_S) {
			yesPressed = true;
			closeDialog(); 
		} else if (keyCode==KeyEvent.VK_N || keyCode==KeyEvent.VK_D) {
			closeDialog(); 
		} else if (keyCode==KeyEvent.VK_ESCAPE||keyCode==KeyEvent.VK_C) { 
			cancelPressed = true; 
			closeDialog(); 
			IJ.resetEscape();
		} 
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
     * @see java.awt.Window#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
    	super.paint(g);
      	if (firstPaint) {
    		yesB.requestFocus();
    		firstPaint = false;
    	}
    }

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		cancelPressed = true; 
		closeDialog(); 
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
