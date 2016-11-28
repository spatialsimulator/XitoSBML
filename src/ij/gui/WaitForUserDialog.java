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
import ij.plugin.frame.RoiManager;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


// TODO: Auto-generated Javadoc
/**
* This is a non-modal dialog box used to ask the user to perform some task
* while a macro or plugin is running. It implements the waitForUser() macro
* function. It is based on Michael Schmid's Wait_For_User plugin.
*/
public class WaitForUserDialog extends Dialog implements ActionListener, KeyListener {
	
	/** The button. */
	protected Button button;
	
	/** The label. */
	protected MultiLineLabel label;
	
	/** The yloc. */
	static protected int xloc=-1, yloc=-1;
	
	/** The esc pressed. */
	private boolean escPressed;
	
	/**
	 * Instantiates a new wait for user dialog.
	 *
	 * @param title the title
	 * @param text the text
	 */
	public WaitForUserDialog(String title, String text) {
		super(IJ.getInstance(), title, false);
		if (text!=null && text.startsWith("IJ: "))
			text = text.substring(4);
		label = new MultiLineLabel(text, 175);
		if (!IJ.isLinux()) label.setFont(new Font("SansSerif", Font.PLAIN, 14));
		if (IJ.isMacOSX()) {
			RoiManager rm = RoiManager.getInstance();
			if (rm!=null) rm.runCommand("enable interrupts");
		}
        GridBagLayout gridbag = new GridBagLayout(); //set up the layout
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        c.insets = new Insets(6, 6, 0, 6); 
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        add(label,c); 
		button = new Button("  OK  ");
		button.addActionListener(this);
		button.addKeyListener(this);
        c.insets = new Insets(2, 6, 6, 6); 
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.EAST;
        add(button, c);
		setResizable(false);
		addKeyListener(this);
		pack();
		if (xloc==-1)
			GUI.center(this);
		else
			setLocation(xloc, yloc);
		if (IJ.isJava16())
			setAlwaysOnTop(true);
	}
	
	/**
	 * Instantiates a new wait for user dialog.
	 *
	 * @param text the text
	 */
	public WaitForUserDialog(String text) {
		this("Action Required", text);
	}

	/* (non-Javadoc)
	 * @see java.awt.Dialog#show()
	 */
	public void show() {
		super.show();
		//IJ.beep();
		synchronized(this) {  //wait for OK
			try {wait();}
			catch(InterruptedException e) {return;}
		}
	}
	
    /**
     * Close.
     */
    public void close() {
        synchronized(this) { notify(); }
        xloc = getLocation().x;
        yloc = getLocation().y;
		dispose();
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		close();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) { 
		int keyCode = e.getKeyCode(); 
		IJ.setKeyDown(keyCode); 
		if (keyCode==KeyEvent.VK_ENTER || keyCode==KeyEvent.VK_ESCAPE) {
			escPressed = keyCode==KeyEvent.VK_ESCAPE;
			close();
		}
	}
	
	/**
	 * Esc pressed.
	 *
	 * @return true, if successful
	 */
	public boolean escPressed() {
		return escPressed;
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
	
	/**
	 *  Returns a reference to the 'OK' button.
	 *
	 * @return the button
	 */
	public Button getButton() {
		return button;
	}

}
