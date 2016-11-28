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
package ij.macro;
import ij.IJ;
import ij.ImageJ;
import ij.WindowManager;
import ij.plugin.frame.Editor;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// TODO: Auto-generated Javadoc
/** This class implements the text editor's Macros/Find Functions command.
It was written by jerome.mutterer at ibmp.fr, and is based on Mark Longair's CommandFinder plugin.
*/
public class FunctionFinder implements TextListener,  WindowListener, KeyListener, ItemListener, ActionListener {
	
	/** The dialog. */
	private Dialog dialog;
	
	/** The prompt. */
	private TextField prompt;
	
	/** The functions. */
	private List functions;
	
	/** The close button. */
	private Button insertButton, infoButton, closeButton;
	
	/** The commands. */
	private String [] commands;
	
	/** The editor. */
	private Editor editor;
	
	/**
	 * Instantiates a new function finder.
	 *
	 * @param editor the editor
	 */
	public FunctionFinder(Editor editor) {
		this.editor = editor;
		String exists = IJ.runMacro("return File.exists(getDirectory('macros')+'functions.html');");
		if (exists=="0")	{
			String installLocalMacroFunctionsFile = "functions = File.openUrlAsString('"+IJ.URL+"/developer/macro/functions.html');\n"+
					"f = File.open(getDirectory('macros')+'functions.html');\n"+
					"print (f, functions);\n"+
					"File.close(f);";
			try { IJ.runMacro(installLocalMacroFunctionsFile);
			} catch (Throwable e) { IJ.error("Problem downloading functions.html"); return;}
		}
		String f = IJ.runMacro("return File.openAsString(getDirectory('macros')+'functions.html');");
		String [] l = f.split("\n");
		commands= new String [l.length];
		int c=0;
		for (int i=0; i<l.length; i++) {
			String line = l[i];
			if (line.startsWith("<b>")) {
				commands[c]=line.substring(line.indexOf("<b>")+3,line.indexOf("</b>"));
				c++;
			}
		}
		if (c==0) {
			IJ.error("ImageJ/macros/functions.html is corrupted");
			return;
		}
		
		ImageJ imageJ = IJ.getInstance();
		dialog = new Dialog(imageJ, "Built-in Functions");
		dialog.setLayout(new BorderLayout());
		dialog.addWindowListener(this);
		Panel northPanel = new Panel();
		prompt = new TextField("", 32);
		prompt.addTextListener(this);
		prompt.addKeyListener(this);
		northPanel.add(prompt);
		dialog.add(northPanel, BorderLayout.NORTH);
		functions = new List(12);
		functions.addKeyListener(this);
		populateList("");
		dialog.add(functions, BorderLayout.CENTER);
		Panel buttonPanel = new Panel();
		//panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		insertButton = new Button("Insert");
		insertButton.addActionListener(this);
		buttonPanel.add(insertButton);
		infoButton = new Button("Info");
		infoButton.addActionListener(this);
		buttonPanel.add(infoButton);
		closeButton = new Button("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		
		Frame frame = WindowManager.getFrontWindow();
		if (frame==null) return;
		java.awt.Point posi=frame.getLocationOnScreen();
		int initialX = (int)posi.getX() + 38;
		int initialY = (int)posi.getY() + 84;
		dialog.setLocation(initialX,initialY);
		dialog.setVisible(true);
		dialog.toFront();
	}

	/**
	 * Instantiates a new function finder.
	 */
	public FunctionFinder() {
		this(null);
	}

	/**
	 * Populate list.
	 *
	 * @param matchingSubstring the matching substring
	 */
	public void populateList(String matchingSubstring) {
		String substring = matchingSubstring.toLowerCase();
		functions.removeAll();
		try {
			for(int i=0; i<commands.length; ++i) {
				String commandName = commands[i];
				if (commandName.length()==0)
					continue;
				String lowerCommandName = commandName.toLowerCase();
				if( lowerCommandName.indexOf(substring) >= 0 ) {
					functions.add(commands[i]);
				}
			}
		} catch (Exception e){}
	}
	
	/**
	 * Ed paste.
	 *
	 * @param arg the arg
	 */
	public void edPaste(String arg) {
		Frame frame = editor;
		if (frame!=null && !frame.isVisible())
			frame = null;
		if (frame==null) {
			frame = WindowManager.getFrontWindow();
			if (!(frame instanceof Editor))
				return;
		}
		try {
			TextArea ta = ((Editor)frame).getTextArea();
			int start = ta.getSelectionStart( );
			int end = ta.getSelectionEnd( );
			try {
				ta.replaceRange(arg.substring(0,arg.length()), start, end);
			} catch (Exception e) { }
			if (IJ.isMacOSX())
				ta.setCaretPosition(start+arg.length());
		} catch (Exception e) { }
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		populateList(prompt.getText());
	}
	
	/**
	 * Run from label.
	 *
	 * @param listLabel the list label
	 */
	protected void runFromLabel(String listLabel) {
		edPaste(listLabel);
		dialog.dispose();
	}
	
	/**
	 * Close.
	 */
	public void close() {
		dialog.dispose();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		int items = functions.getItemCount();
		Object source = ke.getSource();
		if (source==prompt) {
			if (key==KeyEvent.VK_ENTER) {
				if (1==items) {
					String selected = functions.getItem(0);
					edPaste(selected);
				}
			} else if (key==KeyEvent.VK_UP) {
				functions.requestFocus();
				if(items>0)
					functions.select(functions.getItemCount()-1);
			} else if (key==KeyEvent.VK_ESCAPE) {
				dialog.dispose();
			} else if (key==KeyEvent.VK_DOWN)  {
				functions.requestFocus();
				if (items>0)
					functions.select(0);
			}
		} else if (source==functions) {
			if (key==KeyEvent.VK_ENTER) {
				String selected = functions.getSelectedItem();
				if (selected!=null)
					edPaste(selected);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent ke) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent ke) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.TextListener#textValueChanged(java.awt.event.TextEvent)
	 */
	public void textValueChanged(TextEvent te) {
		populateList(prompt.getText());
	}
		
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==insertButton) {
			int index = functions.getSelectedIndex();
			if (index>=0) {
				String selected = functions.getItem(index);
				edPaste(selected);
			}
		} else if (b==infoButton) {
			String url = IJ.URL+"/developer/macro/functions.html";
			int index = functions.getSelectedIndex();
			if (index>=0) {
				String selected = functions.getItem(index);
				int index2 = selected.indexOf("(");
				if (index2==-1)
					index2 = selected.length();
				url = url + "#" + selected.substring(0, index2);
			}
			IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
		} else if (b==closeButton)
			dialog.dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		dialog.dispose();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) { }
}

