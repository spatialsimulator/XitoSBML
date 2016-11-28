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
import ij.macro.MacroRunner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

// TODO: Auto-generated Javadoc
/** This is modal or non-modal dialog box that displays HTML formated text. */
public class HTMLDialog extends JDialog implements ActionListener, KeyListener, HyperlinkListener {
	
	/** The escape pressed. */
	private boolean escapePressed;
	
	/** The editor pane. */
	private JEditorPane editorPane;
	
	/** The modal. */
	private boolean modal = true;

	/**
	 * Instantiates a new HTML dialog.
	 *
	 * @param title the title
	 * @param message the message
	 */
	public HTMLDialog(String title, String message) {
		super(ij.IJ.getInstance(), title, true);
		init(message);
	}

	/**
	 * Instantiates a new HTML dialog.
	 *
	 * @param parent the parent
	 * @param title the title
	 * @param message the message
	 */
	public HTMLDialog(Dialog parent, String title, String message) {
		super(parent, title, true);
		init(message);
	}

	/**
	 * Instantiates a new HTML dialog.
	 *
	 * @param title the title
	 * @param message the message
	 * @param modal the modal
	 */
	public HTMLDialog(String title, String message, boolean modal) {
		super(ij.IJ.getInstance(), title, modal);
		this.modal = modal;
		init(message);
	}
	
	/**
	 * Inits the.
	 *
	 * @param message the message
	 */
	private void init(String message) {
		ij.util.Java2.setSystemLookAndFeel();
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		if (message==null) message = "";
		editorPane = new JEditorPane("text/html","");
		editorPane.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		editorPane.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("body{font-family:Verdana,sans-serif; font-size:11.5pt; margin:5px 10px 5px 10px;}"); //top right bottom left
		styleSheet.addRule("h1{font-size:18pt;}");
		styleSheet.addRule("h2{font-size:15pt;}");
		styleSheet.addRule("dl dt{font-face:bold;}");
		editorPane.setText(message);    //display the html text with the above style
		editorPane.getActionMap().put("insert-break", new AbstractAction(){
				public void actionPerformed(ActionEvent e) {}		
		}); //suppress beep on <ENTER> key
		JScrollPane scrollPane = new JScrollPane(editorPane);
		container.add(scrollPane);
		JButton button = new JButton("OK");
		button.addActionListener(this);
		button.addKeyListener(this);
		editorPane.addKeyListener(this);
		editorPane.addHyperlinkListener(this);
		JPanel panel = new JPanel();
		panel.add(button);
		container.add(panel, "South");
		setForeground(Color.black);
		pack();
		Dimension screenD = IJ.getScreenSize();
		Dimension dialogD = getSize();
		int maxWidth = (int)(Math.min(0.70*screenD.width, 800)); //max 70% of screen width, but not more than 800 pxl
		if (maxWidth>400 && dialogD.width>maxWidth)
			dialogD.width = maxWidth;
		if (dialogD.height > 0.80*screenD.height && screenD.height>400)  //max 80% of screen height
			dialogD.height = (int)(0.80*screenD.height);
		setSize(dialogD);
		GUI.center(this);
		if (!modal) WindowManager.addWindow(this);
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
		ij.IJ.setKeyDown(keyCode);
		escapePressed = keyCode==KeyEvent.VK_ESCAPE;
		if (keyCode==KeyEvent.VK_C) {
			if (editorPane.getSelectedText()==null || editorPane.getSelectedText().length()==0)
				editorPane.selectAll();
			editorPane.copy();
			editorPane.select(0,0);
		} else if (keyCode==KeyEvent.VK_ENTER || keyCode==KeyEvent.VK_W || escapePressed)
			dispose();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		ij.IJ.setKeyUp(keyCode);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}
	
	/**
	 * Escape pressed.
	 *
	 * @return true, if successful
	 */
	public boolean escapePressed() {
		return escapePressed;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String url = e.getDescription(); //getURL does not work for relative links within document such as "#top"
			if (url==null) return;
			if (url.startsWith("#"))
				editorPane.scrollToReference(url.substring(1));
			else {
				String macro = "run('URL...', 'url="+url+"');";
				new MacroRunner(macro);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (!modal) WindowManager.removeWindow(this);
	}

}
