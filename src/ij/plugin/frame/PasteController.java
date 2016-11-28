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
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.process.Blitter;

import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// TODO: Auto-generated Javadoc
/** Implements ImageJ's Paste Control window. */
public class PasteController extends PlugInFrame implements PlugIn, ItemListener {

	/** The panel. */
	private Panel panel;
	
	/** The paste mode. */
	private Choice pasteMode;
	
	/** The instance. */
	private static Frame instance;
	
	/**
	 * Instantiates a new paste controller.
	 */
	public PasteController() {
		super("Paste Control");
		if (instance!=null) {
			WindowManager.toFront(instance);
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		IJ.register(PasteController.class);
		setLayout(new FlowLayout(FlowLayout.CENTER, 2, 5));
		
		add(new Label(" Transfer Mode:"));
		pasteMode = new Choice();
		pasteMode.addItem("Copy");
		pasteMode.addItem("Blend");
		pasteMode.addItem("Difference");
		pasteMode.addItem("Transparent-white");
		pasteMode.addItem("Transparent-zero");
		pasteMode.addItem("AND");
		pasteMode.addItem("OR");
		pasteMode.addItem("XOR");
		pasteMode.addItem("Add");
		pasteMode.addItem("Subtract");
		pasteMode.addItem("Multiply");
		pasteMode.addItem("Divide");
		pasteMode.addItem("Min");
		pasteMode.addItem("Max");
		pasteMode.select("Copy");
		pasteMode.addItemListener(this);
		add(pasteMode);
		Roi.setPasteMode(Blitter.COPY);

		pack();
		GUI.center(this);
		setResizable(false);
		show();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		int index = pasteMode.getSelectedIndex();
		int mode = Blitter.COPY;
		switch (index) {
			case 0: mode = Blitter.COPY; break;
			case 1: mode = Blitter.AVERAGE; break;
			case 2: mode = Blitter.DIFFERENCE; break;
			case 3: mode = Blitter.COPY_TRANSPARENT; break;
			case 4: mode = Blitter.COPY_ZERO_TRANSPARENT; break;
			case 5: mode = Blitter.AND; break;
			case 6: mode = Blitter.OR; break;
			case 7: mode = Blitter.XOR; break;
			case 8: mode = Blitter.ADD; break;
			case 9: mode = Blitter.SUBTRACT; break;
			case 10: mode = Blitter.MULTIPLY; break;
			case 11: mode = Blitter.DIVIDE; break;
			case 12: mode = Blitter.MIN; break;
			case 13: mode = Blitter.MAX; break;
		}
		Roi.setPasteMode(mode);
		if (Recorder.record)
			Recorder.record("setPasteMode", pasteMode.getSelectedItem());
		ImagePlus imp = WindowManager.getCurrentImage();
	}
	
	/* (non-Javadoc)
	 * @see ij.plugin.frame.PlugInFrame#close()
	 */
	public void close() {
		super.close();
		instance = null;
	}
	
}
