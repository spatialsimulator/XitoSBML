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

import java.awt.AWTEventMulticaster;
import java.awt.Adjustable;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;


// TODO: Auto-generated Javadoc
/** This class, based on Joachim Walter's Image5D package, adds "c", "z" labels 
	 and play-pause icons (T) to the stack and hyperstacks dimension sliders.
 * @author Joachim Walter
 */
public class ScrollbarWithLabel extends Panel implements Adjustable, AdjustmentListener {
	
	/** The bar. */
	Scrollbar bar;
	
	/** The icon. */
	private Icon icon;
	
	/** The stack window. */
	private StackWindow stackWindow;
	
	/** The adjustment listener. */
	transient AdjustmentListener adjustmentListener;
	
	/**
	 * Instantiates a new scrollbar with label.
	 */
	public ScrollbarWithLabel() {
	}

	/**
	 * Instantiates a new scrollbar with label.
	 *
	 * @param stackWindow the stack window
	 * @param value the value
	 * @param visible the visible
	 * @param minimum the minimum
	 * @param maximum the maximum
	 * @param label the label
	 */
	public ScrollbarWithLabel(StackWindow stackWindow, int value, int visible, int minimum, int maximum, char label) {
		super(new BorderLayout(2, 0));
		this.stackWindow = stackWindow;
		bar = new Scrollbar(Scrollbar.HORIZONTAL, value, visible, minimum, maximum);
		GUI.fix(bar);
		icon = new Icon(label);
		add(icon, BorderLayout.WEST);
		add(bar, BorderLayout.CENTER);
		bar.addAdjustmentListener(this);
		addKeyListener(IJ.getInstance()); 
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension dim = new Dimension(0,0);
		int width = bar.getPreferredSize().width;
		Dimension minSize = getMinimumSize();
		if (width<minSize.width) width = minSize.width;		
		int height = bar.getPreferredSize().height;
		dim = new Dimension(width, height);
		return dim;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Container#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return new Dimension(80, 15);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
	 */
	/* Adds KeyListener also to all sub-components.
	 */
	public synchronized void addKeyListener(KeyListener l) {
		super.addKeyListener(l);
		bar.addKeyListener(l);
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#removeKeyListener(java.awt.event.KeyListener)
	 */
	/* Removes KeyListener also from all sub-components.
	 */
	public synchronized void removeKeyListener(KeyListener l) {
		super.removeKeyListener(l);
		bar.removeKeyListener(l);
	}

	/* (non-Javadoc)
	 * @see java.awt.Adjustable#addAdjustmentListener(java.awt.event.AdjustmentListener)
	 */
	/* 
	 * Methods of the Adjustable interface
	 */
	public synchronized void addAdjustmentListener(AdjustmentListener l) {
		if (l == null) {
			return;
		}
		adjustmentListener = AWTEventMulticaster.add(adjustmentListener, l);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getBlockIncrement()
	 */
	public int getBlockIncrement() {
		return bar.getBlockIncrement();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getMaximum()
	 */
	public int getMaximum() {
		return bar.getMaximum();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getMinimum()
	 */
	public int getMinimum() {
		return bar.getMinimum();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getOrientation()
	 */
	public int getOrientation() {
		return bar.getOrientation();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getUnitIncrement()
	 */
	public int getUnitIncrement() {
		return bar.getUnitIncrement();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getValue()
	 */
	public int getValue() {
		return bar.getValue();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#getVisibleAmount()
	 */
	public int getVisibleAmount() {
		return bar.getVisibleAmount();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#removeAdjustmentListener(java.awt.event.AdjustmentListener)
	 */
	public synchronized void removeAdjustmentListener(AdjustmentListener l) {
		if (l == null) {
			return;
		}
		adjustmentListener = AWTEventMulticaster.remove(adjustmentListener, l);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setBlockIncrement(int)
	 */
	public void setBlockIncrement(int b) {
		bar.setBlockIncrement(b);		 
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setMaximum(int)
	 */
	public void setMaximum(int max) {
		bar.setMaximum(max);		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setMinimum(int)
	 */
	public void setMinimum(int min) {
		bar.setMinimum(min);		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setUnitIncrement(int)
	 */
	public void setUnitIncrement(int u) {
		bar.setUnitIncrement(u);		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setValue(int)
	 */
	public void setValue(int v) {
		bar.setValue(v);		
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Adjustable#setVisibleAmount(int)
	 */
	public void setVisibleAmount(int v) {
		bar.setVisibleAmount(v);		
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#setFocusable(boolean)
	 */
	public void setFocusable(boolean focusable) {
		super.setFocusable(focusable);
		bar.setFocusable(focusable);
	}
		
	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	/*
	 * Method of the AdjustmenListener interface.
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (bar != null && e.getSource() == bar) {
			AdjustmentEvent myE = new AdjustmentEvent(this, e.getID(), e.getAdjustmentType(), 
					e.getValue(), e.getValueIsAdjusting());
			AdjustmentListener listener = adjustmentListener;
			if (listener != null) {
				listener.adjustmentValueChanged(myE);
			}
		}
	}
		
	/**
	 * Update play pause icon.
	 */
	public void updatePlayPauseIcon() {
		icon.repaint();
	}
	
	
	/**
	 * The Class Icon.
	 */
	class Icon extends Canvas implements MouseListener {
		
		/** The Constant HEIGHT. */
		private static final int WIDTH = 12, HEIGHT=14;
		
		/** The stroke. */
		private BasicStroke stroke = new BasicStroke(2f);
		
		/** The type. */
		private char type;
		
		/** The image. */
		private Image image;

		/**
		 * Instantiates a new icon.
		 *
		 * @param type the type
		 */
		public Icon(char type) {
			addMouseListener(this);
			addKeyListener(IJ.getInstance()); 
			setSize(WIDTH, HEIGHT);
			this.type = type;
		}
		
		/**
		 *  Overrides Component getPreferredSize().
		 *
		 * @return the preferred size
		 */
		public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		}
				
		/* (non-Javadoc)
		 * @see java.awt.Canvas#update(java.awt.Graphics)
		 */
		public void update(Graphics g) {
			paint(g);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Canvas#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (type=='t')
				drawPlayPauseButton(g2d);
			else
				drawLetter(g);
		}
		
		/**
		 * Draw letter.
		 *
		 * @param g the g
		 */
		private void drawLetter(Graphics g) {
			g.setFont(new Font("SansSerif", Font.PLAIN, 14));
			g.setColor(Color.black);
			g.drawString(String.valueOf(type), 2, 12);
		}

		/**
		 * Draw play pause button.
		 *
		 * @param g the g
		 */
		private void drawPlayPauseButton(Graphics2D g) {
			if (stackWindow.getAnimate()) {
				g.setColor(Color.black);
				g.setStroke(stroke);
				g.drawLine(3, 3, 3, 11);
				g.drawLine(8, 3, 8, 11);
			} else {
				g.setColor(Color.darkGray);
				GeneralPath path = new GeneralPath();
				path.moveTo(3f, 2f);
				path.lineTo(10f, 7f);
				path.lineTo(3f, 12f);
				path.lineTo(3f, 2f);
				g.fill(path);
			}
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			if (type!='t') return;
			int flags = e.getModifiers();
			if ((flags&(Event.ALT_MASK|Event.META_MASK|Event.CTRL_MASK))!=0)
				IJ.doCommand("Animation Options...");
			else
				IJ.doCommand("Start Animation [\\]");
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {}
	
	} // StartStopIcon class

	
}
