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
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// TODO: Auto-generated Javadoc
/** Displays the ImageJ Channels window. */
public class Channels extends PlugInDialog implements PlugIn, ItemListener, ActionListener {

	/** The modes. */
	private static String[] modes = {"Composite", "Color", "Grayscale"};
	
	/** The menu items. */
	private static String[] menuItems = {"Make Composite", "Convert to RGB", "Split Channels", "Merge Channels...",
		"Edit LUT...", "-", "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Grays"};

	/** The more label. */
	private static String moreLabel = "More "+'\u00bb';
	
	/** The choice. */
	//private String[] title = {"Red", "Green", "Blue"};
	private Choice choice;
	
	/** The checkbox. */
	private Checkbox[] checkbox;
	
	/** The more button. */
	private Button moreButton;
	
	/** The instance. */
	private static Channels instance;
	
	/** The id. */
	private int id;
	
	/** The location. */
	private static Point location;
	
	/** The pm. */
	private PopupMenu pm;

	/**
	 * Instantiates a new channels.
	 */
	public Channels() {
		super("Channels");
		if (instance!=null) {
			instance.toFront();
			return;
		}
		WindowManager.addWindow(this);
		instance = this;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		int y = 0;
		c.gridx = 0;
		c.gridy = y++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		int margin = 32;
		if (IJ.isMacOSX())
			margin = 20;
		c.insets = new Insets(10, margin, 10, margin);
		choice = new Choice();
		for (int i=0; i<modes.length; i++)
			choice.addItem(modes[i]);
		choice.select(0);
		choice.addItemListener(this);
		add(choice, c);

		CompositeImage ci = getImage();
		int nCheckBoxes = ci!=null?ci.getNChannels():3;
		if (nCheckBoxes>CompositeImage.MAX_CHANNELS)
			nCheckBoxes = CompositeImage.MAX_CHANNELS;
		checkbox = new Checkbox[nCheckBoxes];
		for (int i=0; i<nCheckBoxes; i++) {
			checkbox[i] = new Checkbox("Channel "+(i+1), true);
			c.insets = new Insets(0, 25, i<nCheckBoxes-1?0:10, 5);
			c.gridy = y++;
			add(checkbox[i], c);
			checkbox[i].addItemListener(this);
		}

		c.insets = new Insets(0, 15, 10, 15);
		c.fill = GridBagConstraints.NONE;
		c.gridy = y++;
		moreButton = new Button(moreLabel);
		moreButton.addActionListener(this);
		add(moreButton, c);
		update();

		pm=new PopupMenu();
		for (int i=0; i<menuItems.length; i++)
			addPopupItem(menuItems[i]);
		add(pm);

		addKeyListener(IJ.getInstance());  // ImageJ handles keyboard shortcuts
		setResizable(false);
		pack();
		if (location==null) {
			GUI.center(this);
			location = getLocation();
		} else
			setLocation(location);
		show();
	}
	
	/**
	 * Update.
	 */
	public void update() {
		CompositeImage ci = getImage();
		if (ci==null || checkbox==null)
			return;
		int n = checkbox.length;
		int nChannels = ci.getNChannels();
		if (nChannels!=n && nChannels<=CompositeImage.MAX_CHANNELS) {
			instance = null;
			location = getLocation();
			close();
			new Channels();
			return;
		}
		boolean[] active = ci.getActiveChannels();
		for (int i=0; i<checkbox.length; i++)
			checkbox[i].setState(active[i]);
		int index = 0;
		switch (ci.getMode()) {
			case IJ.COMPOSITE: index=0; break;
			case IJ.COLOR: index=1; break;
			case IJ.GRAYSCALE: index=2; break;
		}
		choice.select(index);
	}
	
	/**
	 * Update channels.
	 */
	public static void updateChannels() {
		if (instance!=null)
			instance.update();
	}
	
	/**
	 * Adds the popup item.
	 *
	 * @param s the s
	 */
	void addPopupItem(String s) {
		MenuItem mi=new MenuItem(s);
		mi.addActionListener(this);
		pm.add(mi);
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	CompositeImage getImage() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null || !imp.isComposite())
			return null;
		else
			return (CompositeImage)imp;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		if (!imp.isComposite()) {
			int channels = imp.getNChannels();
			if (channels==1 && imp.getStackSize()<=4)
				channels = imp.getStackSize();
			if (imp.getBitDepth()==24 || (channels>1&&channels<CompositeImage.MAX_CHANNELS)) {
				GenericDialog gd = new GenericDialog(imp.getTitle());
				gd.addMessage("Convert to multi-channel composite image?");
				gd.showDialog();
				if (gd.wasCanceled())
					return;
				else
					IJ.doCommand("Make Composite");					
			} else {
				IJ.error("Channels", "A composite image is required (e.g., "+moreLabel+" Open HeLa Cells),\nor create one using "+moreLabel+" Make Composite.");
				return;
			}
		}
		if (!imp.isComposite()) return;
		CompositeImage ci = (CompositeImage)imp;
		Object source = e.getSource();
		if (source==choice) {
			int index = ((Choice)source).getSelectedIndex();
			switch (index) {
				case 0: ci.setMode(IJ.COMPOSITE); break;
				case 1: ci.setMode(IJ.COLOR); break;
				case 2: ci.setMode(IJ.GRAYSCALE); break;
			}
			ci.updateAndDraw();
			if (Recorder.record) {
				String mode = null;
				if (Recorder.scriptMode()) {
					switch (index) {
						case 0: mode="IJ.COMPOSITE"; break;
						case 1: mode="IJ.COLOR"; break;
						case 2: mode="IJ.GRAYSCALE"; break;
					}
					Recorder.recordCall("imp.setDisplayMode("+mode+");");
				} else {
					switch (index) {
						case 0: mode="composite"; break;
						case 1: mode="color"; break;
						case 2: mode="grayscale"; break;
					}
					Recorder.record("Stack.setDisplayMode", mode);
				}
			}
		} else if (source instanceof Checkbox) {
			for (int i=0; i<checkbox.length; i++) {
				Checkbox cb = (Checkbox)source;
				if (cb==checkbox[i]) {
					if (ci.getMode()==IJ.COMPOSITE) {
						boolean[] active = ci.getActiveChannels();
						active[i] = cb.getState();
						if (Recorder.record) {
							String str = "";
							for (int c=0; c<ci.getNChannels(); c++)
								str += active[c]?"1":"0";
							if (Recorder.scriptMode())
								Recorder.recordCall("imp.setActiveChannels(\""+str+"\");");
							else
								Recorder.record("Stack.setActiveChannels", str);
						}
					} else {
						imp.setPosition(i+1, imp.getSlice(), imp.getFrame());
						if (Recorder.record) {
							if (Recorder.scriptMode())
								Recorder.recordCall("imp.setC("+(i+1)+");");
							else
								Recorder.record("Stack.setChannel", i+1);
						}
					}
					ci.updateAndDraw();
					return;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command==null) return;
		if (command.equals(moreLabel)) {
			Point bloc = moreButton.getLocation();
			pm.show(this, bloc.x, bloc.y);
		} else if (command.equals("Convert to RGB"))
			IJ.doCommand("Stack to RGB");
		else
			IJ.doCommand(command);
	}
	
	/**
	 *  Obsolete; always returns null.
	 *
	 * @return single instance of Channels
	 */
	public static Frame getInstance() {
		return null;
	}
		
	/* (non-Javadoc)
	 * @see ij.plugin.frame.PlugInDialog#close()
	 */
	public void close() {
		super.close();
		instance = null;
		location = getLocation();
	}
	
}
