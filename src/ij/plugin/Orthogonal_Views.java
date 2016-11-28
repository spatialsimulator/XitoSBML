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
package ij.plugin;
import ij.CommandListener;
import ij.CompositeImage;
import ij.Executer;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.GeneralPath;
import java.awt.image.ColorModel;
 
// TODO: Auto-generated Javadoc
/**
 * This plugin projects dynamically orthogonal XZ and YZ views of a stack. 
 * The output images are calibrated, which allows measurements to be performed more easily. 
 * 
 * Many thanks to Jerome Mutterer for the code contributions and testing.
 * Thanks to Wayne Rasband for the code that properly handles the image magnification.
 * 		
 * @author Dimiter Prodanov
 */
public class Orthogonal_Views implements PlugIn, MouseListener, MouseMotionListener, KeyListener, ActionListener, 
	ImageListener, WindowListener, AdjustmentListener, MouseWheelListener, FocusListener, CommandListener, Runnable {

	/** The win. */
	private ImageWindow win;
	
	/** The imp. */
	private ImagePlus imp;
	
	/** The rgb. */
	private boolean rgb;
	
	/** The image stack. */
	private ImageStack imageStack;
	
	/** The hyperstack. */
	private boolean hyperstack;
	
	/** The current mode. */
	private int currentChannel, currentFrame, currentMode; 
	
	/** The canvas. */
	private ImageCanvas canvas;
	
	/** The Constant H_ZOOM. */
	private static final int H_ROI=0, H_ZOOM=1;
	
	/** The sticky. */
	private static boolean sticky=true;
	
	/** The yz ID. */
	private static int xzID, yzID;
	
	/** The instance. */
	private static Orthogonal_Views instance;
	
	/** The yz image. */
	private ImagePlus xz_image, yz_image;
	
	/**  ImageProcessors for the xz and yz images. */
	private ImageProcessor fp1, fp2;
	
	/** The az. */
	private double ax, ay, az;
	
	/** The rotate YZ. */
	private boolean rotateYZ = Prefs.rotateYZ;
	
	/** The flip XZ. */
	private boolean flipXZ = Prefs.flipXZ;
	
	/** The xy Y. */
	private int xyX, xyY;
	
	/** The cal yz. */
	private Calibration cal=null, cal_xz=new Calibration(), cal_yz=new Calibration();
	
	/** The magnification. */
	private double magnification=1.0;
	
	/** The color. */
	private Color color = Roi.getColor();
	
	/** The max. */
	private double min, max;
	
	/** The screen. */
	private Dimension screen = IJ.getScreenSize();
	
	/** The sync zoom. */
	private boolean syncZoom = true;
	
	/** The cross loc. */
	private Point crossLoc;
	
	/** The first time. */
	private boolean firstTime = true;
	
	/** The previous Y. */
	private static int previousID, previousX, previousY;
	
	/** The starting src rect. */
	private Rectangle startingSrcRect;
	
	/** The done. */
	private boolean done;
	
	/** The thread. */
	private Thread thread;

	 
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		imp = IJ.getImage();
		if (instance!=null) {
			instance.dispose();
			return;
		}
		if (imp.getStackSize()==1) {
			IJ.error("Othogonal Views", "This command requires a stack.");
			return;
		}
		hyperstack = imp.isHyperStack();
		if ((hyperstack||imp.isComposite()) && imp.getNSlices()<=1) {
			IJ.error("Othogonal Views", "This command requires a stack, or a hypertack with Z>1.");
			return;
		}
		yz_image = WindowManager.getImage(yzID);
		rgb = imp.getBitDepth()==24 || hyperstack;
		int yzBitDepth = hyperstack?24:imp.getBitDepth();
		if (yz_image==null || yz_image.getHeight()!=imp.getHeight() || yz_image.getBitDepth()!=yzBitDepth)
			yz_image = new ImagePlus();
		xz_image = WindowManager.getImage(xzID);
		//if (xz_image!=null) IJ.log(imp+"  "+xz_image+"  "+xz_image.getHeight()+"  "+imp.getHeight()+"  "+xz_image.getBitDepth()+"  "+yzBitDepth);
		if (xz_image==null || xz_image.getWidth()!=imp.getWidth() || xz_image.getBitDepth()!=yzBitDepth)
			xz_image = new ImagePlus();
		instance = this;
		ImageProcessor ip = hyperstack?new ColorProcessor(imp.getImage()):imp.getProcessor();
		min = ip.getMin();
		max = ip.getMax();
		cal=this.imp.getCalibration();
		double calx=cal.pixelWidth;
		double caly=cal.pixelHeight;
		double calz=cal.pixelDepth;
		ax=1.0;
		ay=caly/calx;
		az=calz/calx;
		win = imp.getWindow();
		canvas = win.getCanvas();
		addListeners(canvas);
		magnification= canvas.getMagnification();
		imp.deleteRoi();
		Rectangle r = canvas.getSrcRect();
		if (imp.getID()==previousID)
			crossLoc = new Point(previousX, previousY);
		else
			crossLoc = new Point(r.x+r.width/2, r.y+r.height/2);
		imageStack = getStack();
		calibrate();
		if (createProcessors(imageStack)) {
			if (ip.isColorLut() || ip.isInvertedLut()) {
				ColorModel cm = ip.getColorModel();
				fp1.setColorModel(cm);
				fp2.setColorModel(cm);				
			}
			thread = new Thread(this, "Orthogonal Views");
			thread.start();
			IJ.wait(100);
			update();
		} else
			dispose();
	}
	
	/**
	 * Gets the stack.
	 *
	 * @return the stack
	 */
	private ImageStack getStack() {
		if (imp.isHyperStack()) {
			int slices = imp.getNSlices();
			ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
			int c=imp.getChannel(), z=imp.getSlice(), t=imp.getFrame();
			for (int i=1; i<=slices; i++) {
				imp.setPositionWithoutUpdate(c, i, t);
				stack.addSlice(null, new ColorProcessor(imp.getImage()));
			}
			imp.setPosition(c, z, t);
			currentChannel = c;
			currentFrame = t;
			if (imp.isComposite())
				currentMode = ((CompositeImage)imp).getMode();
			return stack;
		} else
			return imp.getStack();
	}
 
	/**
	 * Adds the listeners.
	 *
	 * @param canvas the canvas
	 */
	private void addListeners(ImageCanvas canvas) {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		win.addWindowListener (this);  
		win.addMouseWheelListener(this);
		win.addFocusListener(this);
		ImagePlus.addImageListener(this);
		Executer.addCommandListener(this);
	}
	 
	/**
	 * Calibrate.
	 */
	private void calibrate() {
		String unit=cal.getUnit();
		double o_depth=cal.pixelDepth;
		double o_height=cal.pixelHeight;
		double o_width=cal.pixelWidth;
		cal_yz.setUnit(unit);
		if (rotateYZ) {
			cal_yz.pixelHeight=o_depth/az;
			cal_yz.pixelWidth=o_height;
		} else {
			cal_yz.pixelWidth=o_depth/az;
			cal_yz.pixelHeight=o_height;
		}
		yz_image.setCalibration(cal_yz);
		cal_xz.setUnit(unit);
		cal_xz.pixelWidth=o_width;
		cal_xz.pixelHeight=o_depth/az;
		xz_image.setCalibration(cal_xz);
	}

	/**
	 * Update magnification.
	 *
	 * @param x the x
	 * @param y the y
	 */
	private void updateMagnification(int x, int y) {
        double magnification= win.getCanvas().getMagnification();
        int z = imp.getSlice()-1;
        ImageWindow xz_win = xz_image.getWindow();
        if (xz_win==null) return;
        ImageCanvas xz_ic = xz_win.getCanvas();
        double xz_mag = xz_ic.getMagnification();
        double arat = az/ax;
		int zcoord=(int)(arat*z);
		if (flipXZ) zcoord=(int)(arat*(imp.getNSlices()-z));
        while (xz_mag<magnification) {
        	xz_ic.zoomIn(xz_ic.screenX(x), xz_ic.screenY(zcoord));
        	xz_mag = xz_ic.getMagnification();
        }
        while (xz_mag>magnification) {
        	xz_ic.zoomOut(xz_ic.screenX(x), xz_ic.screenY(zcoord));
        	xz_mag = xz_ic.getMagnification();
        }
        ImageWindow yz_win = yz_image.getWindow();
        if (yz_win==null) return;
        ImageCanvas yz_ic = yz_win.getCanvas();
        double yz_mag = yz_ic.getMagnification();
		zcoord = (int)(arat*z);
        while (yz_mag<magnification) {
        	//IJ.log(magnification+"  "+yz_mag+"  "+zcoord+"  "+y+"  "+x);
        	yz_ic.zoomIn(yz_ic.screenX(zcoord), yz_ic.screenY(y));
        	yz_mag = yz_ic.getMagnification();
        }
        while (yz_mag>magnification) {
        	yz_ic.zoomOut(yz_ic.screenX(zcoord), yz_ic.screenY(y));
        	yz_mag = yz_ic.getMagnification();
        }
	}
	
	/**
	 * Update views.
	 *
	 * @param p the p
	 * @param is the is
	 */
	void updateViews(Point p, ImageStack is) {
		if (fp1==null) return;
		updateXZView(p,is);
		
		double arat=az/ax;
		int width2 = fp1.getWidth();
		int height2 = (int)Math.round(fp1.getHeight()*az);
		if (width2!=fp1.getWidth()||height2!=fp1.getHeight()) {
			fp1.setInterpolate(true);
			ImageProcessor sfp1=fp1.resize(width2, height2);
			if (!rgb) sfp1.setMinAndMax(min, max);
			xz_image.setProcessor("XZ "+p.y, sfp1);
		} else {
			if (!rgb) fp1.setMinAndMax(min, max);
	    	xz_image.setProcessor("XZ "+p.y, fp1);
		}
			
		if (rotateYZ)
			updateYZView(p, is);
		else
			updateZYView(p, is);
				
		width2 = (int)Math.round(fp2.getWidth()*az);
		height2 = fp2.getHeight();
		String title = "YZ ";
		if (rotateYZ) {
			width2 = fp2.getWidth();
			height2 = (int)Math.round(fp2.getHeight()*az);
			title = "ZY ";
		}
		//IJ.log("updateViews "+width2+" "+height2+" "+arat+" "+ay+" "+fp2);
		if (width2!=fp2.getWidth()||height2!=fp2.getHeight()) {
			fp2.setInterpolate(true);
			ImageProcessor sfp2=fp2.resize(width2, height2);
			if (!rgb) sfp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x, sfp2);
		} else {
			if (!rgb) fp2.setMinAndMax(min, max);
			yz_image.setProcessor(title+p.x, fp2);
		}
		
		calibrate();
		if (yz_image.getWindow()==null) {
			yz_image.show();
			ImageCanvas ic = yz_image.getCanvas();
			ic.addKeyListener(this);
			//ic.addMouseListener(this);
			//ic.addMouseMotionListener(this);
			ic.setCustomRoi(true);
			//yz_image.getWindow().addMouseWheelListener(this);
			yzID = yz_image.getID();
		} else {
			ImageCanvas ic = yz_image.getWindow().getCanvas();
			//ic.addMouseListener(this);
			//ic.addMouseMotionListener(this);
			ic.setCustomRoi(true);
		}
		if (xz_image.getWindow()==null) {
			xz_image.show();
			ImageCanvas ic = xz_image.getCanvas();
			ic.addKeyListener(this);
			//ic.addMouseListener(this);
			//ic.addMouseMotionListener(this);
			ic.setCustomRoi(true);
			//xz_image.getWindow().addMouseWheelListener(this);
			xzID = xz_image.getID();
		} else {
			ImageCanvas ic = xz_image.getWindow().getCanvas();
			//ic.addMouseListener(this);
			//ic.addMouseMotionListener(this);
			ic.setCustomRoi(true);
		}
		 
	}
	
	/**
	 * Arrange windows.
	 *
	 * @param sticky the sticky
	 */
	void arrangeWindows(boolean sticky) {
		ImageWindow xyWin = imp.getWindow();
		if (xyWin==null) return;
		Point loc = xyWin.getLocation();
		if ((xyX!=loc.x)||(xyY!=loc.y)) {
			xyX =  loc.x;
			xyY =  loc.y;
 			ImageWindow yzWin =null;
 			long start = System.currentTimeMillis();
 			while (yzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				yzWin = yz_image.getWindow();
				if (yzWin==null) IJ.wait(50);
			}
			if (yzWin!=null)
 				yzWin.setLocation(xyX+xyWin.getWidth(), xyY);
			ImageWindow xzWin =null;
 			start = System.currentTimeMillis();
 			while (xzWin==null && (System.currentTimeMillis()-start)<=2500L) {
				xzWin = xz_image.getWindow();
				if (xzWin==null) IJ.wait(50);
			}
			if (xzWin!=null)
 				xzWin.setLocation(xyX,xyY+xyWin.getHeight());
 			if (firstTime) {
 				imp.getWindow().toFront();
 				if (hyperstack)
 					imp.setPosition(imp.getChannel(), imp.getNSlices()/2, imp.getFrame());
 				else
 					imp.setSlice(imp.getNSlices()/2);
 				firstTime = false;
 			}
		}
	}
	
	/**
	 * Creates the processors.
	 *
	 * @param is - used to get the dimensions of the new ImageProcessors
	 * @return true, if successful
	 */
	boolean createProcessors(ImageStack is) {
		ImageProcessor ip=is.getProcessor(1);
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize(); 
		double arat=1.0;//az/ax;
		double brat=1.0;//az/ay;
		int za=(int)(ds*arat);
		int zb=(int)(ds*brat);
		//IJ.log("za: "+za +" zb: "+zb);
		
		if (ip instanceof FloatProcessor) {
			fp1=new FloatProcessor(width,za);
			if (rotateYZ)
				fp2=new FloatProcessor(height,zb);
			else
				fp2=new FloatProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ByteProcessor) {
			fp1=new ByteProcessor(width,za);
			if (rotateYZ)
				fp2=new ByteProcessor(height,zb);
			else
				fp2=new ByteProcessor(zb,height);
			return true;
		}
		
		if (ip instanceof ShortProcessor) {
			fp1=new ShortProcessor(width,za);
			if (rotateYZ)
				fp2=new ShortProcessor(height,zb);
			else
				fp2=new ShortProcessor(zb,height);
			//IJ.log("createProcessors "+rotateYZ+"  "+height+"   "+zb+"  "+fp2);
			return true;
		}
		
		if (ip instanceof ColorProcessor) {
			fp1=new ColorProcessor(width,za);
			if (rotateYZ)
				fp2=new ColorProcessor(height,zb);
			else
				fp2=new ColorProcessor(zb,height);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Update XZ view.
	 *
	 * @param p the p
	 * @param is the is
	 */
	void updateXZView(Point p, ImageStack is) {
		int width= is.getWidth();
		int size=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		
		int y=p.y;
		// XZ
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[width*size];
			for (int i=0; i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[width*size];
			for (int i=0;i<size; i++) { 
				Object pixels=is.getPixels(i+1);
				if (flipXZ)
					System.arraycopy(pixels, width*y, newpix, width*(size-i-1), width);
				else
					System.arraycopy(pixels, width*y, newpix, width*i, width);
			}
			fp1.setPixels(newpix);
			return;
		}
		
	}
	
	/**
	 * Update YZ view.
	 *
	 * @param p the p
	 * @param is the is
	 */
	void updateYZView(Point p, ImageStack is) {
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		int x=p.x;
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int j=0;j<height;j++)
					newpix[(ds-i-1)*height + j] = pixels[x + j* width];
			}
			fp2.setPixels(newpix);
		}
		if (!flipXZ) fp2.flipVertical();
		
	}
	
	/**
	 * Update ZY view.
	 *
	 * @param p the p
	 * @param is the is
	 */
	void updateZYView(Point p, ImageStack is) {
		int width= is.getWidth();
		int height=is.getHeight();
		int ds=is.getSize();
		ImageProcessor ip=is.getProcessor(1);
		int x=p.x;
		
		if (ip instanceof FloatProcessor) {
			float[] newpix=new float[ds*height];
			for (int i=0;i<ds; i++) { 
				float[] pixels= (float[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ByteProcessor) {
			byte[] newpix=new byte[ds*height];
			for (int i=0;i<ds; i++) { 
				byte[] pixels= (byte[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ShortProcessor) {
			short[] newpix=new short[ds*height];
			for (int i=0;i<ds; i++) { 
				short[] pixels= (short[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
		if (ip instanceof ColorProcessor) {
			int[] newpix=new int[ds*height];
			for (int i=0;i<ds; i++) { 
				int[] pixels= (int[]) is.getPixels(i+1);//toFloatPixels(pixels);
				for (int y=0;y<height;y++)
					newpix[i + y*ds] = pixels[x + y* width];
			}
			fp2.setPixels(newpix);
		}
		
	}
	 
	/**
	 *  draws the crosses in the images.
	 *
	 * @param imp the imp
	 * @param p the p
	 * @param path the path
	 */
	void drawCross(ImagePlus imp, Point p, GeneralPath path) {
		int width=imp.getWidth();
		int height=imp.getHeight();
		float x = p.x;
		float y = p.y;
		path.moveTo(0f, y);
		path.lineTo(width, y);
		path.moveTo(x, 0f);
		path.lineTo(x, height);	
	}
	      
	/**
	 * Dispose.
	 */
	void dispose() {
		synchronized(this) {
			done = true;
			notify();
		}
		imp.setOverlay(null);
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
		canvas.setCustomRoi(false);
		xz_image.setOverlay(null);
		ImageWindow win1 = xz_image.getWindow();
		if (win1!=null) {
			win1.removeMouseWheelListener(this);
			ImageCanvas ic = win1.getCanvas();
			if (ic!=null) {
				ic.removeKeyListener(this);
				ic.removeMouseListener(this);
				ic.removeMouseMotionListener(this);
				ic.setCustomRoi(false);
			}
		}
		yz_image.setOverlay(null);
		ImageWindow win2 = yz_image.getWindow();
		if (win2!=null) {
			win2.removeMouseWheelListener(this);
			ImageCanvas ic = win2.getCanvas();
			if (ic!=null) {
				ic.removeKeyListener(this);
				ic.removeMouseListener(this);
				ic.removeMouseMotionListener(this);
				ic.setCustomRoi(false);
			}
		}
		ImagePlus.removeImageListener(this);
		Executer.removeCommandListener(this);
		win.removeWindowListener(this);
		win.removeMouseWheelListener(this);
		win.removeFocusListener(this);
		win.setResizable(true);
		instance = null;
		previousID = imp.getID();
		previousX = crossLoc.x;
		previousY = crossLoc.y;
		imageStack = null;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		ImageCanvas xyCanvas = imp.getCanvas();
		startingSrcRect = (Rectangle)xyCanvas.getSrcRect().clone();
		mouseDragged(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (IJ.spaceBarDown())  // scrolling?
			return;
		if (e.getSource().equals(canvas)) {
			crossLoc = canvas.getCursorLoc();
		} else if (e.getSource().equals(xz_image.getCanvas())) {
			crossLoc.x = xz_image.getCanvas().getCursorLoc().x;
			int pos = xz_image.getCanvas().getCursorLoc().y;
			int z = (int)Math.round(pos/az);
			int slice = flipXZ?imp.getNSlices()-z:z+1;
			if (hyperstack)
				imp.setPosition(imp.getChannel(), slice, imp.getFrame());
			else
				imp.setSlice(slice);
		} else if (e.getSource().equals(yz_image.getCanvas())) {
			int pos;
			if (rotateYZ) {
				crossLoc.y = yz_image.getCanvas().getCursorLoc().x;
				pos = yz_image.getCanvas().getCursorLoc().y;
			} else {
				crossLoc.y = yz_image.getCanvas().getCursorLoc().y;
				pos = yz_image.getCanvas().getCursorLoc().x;
			}
			int z = (int)Math.round(pos/az);
			if (hyperstack)
				imp.setPosition(imp.getChannel(), z+1, imp.getFrame());
			else
				imp.setSlice(z+1);
		}
		update();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		ImageCanvas ic = imp.getCanvas();
		Rectangle srcRect = ic.getSrcRect();
		if (srcRect.x!=startingSrcRect.x || srcRect.y!=startingSrcRect.y) {
			// user has scrolled xy image
			int dy = srcRect.y - startingSrcRect.y;
			ImageCanvas yzic = yz_image.getCanvas();
			Rectangle yzSrcRect =yzic.getSrcRect();
			if (rotateYZ) {
				yzSrcRect.x += dy;
				if (yzSrcRect.x<0)
					yzSrcRect.x = 0;
				if (yzSrcRect.x>yz_image.getWidth()-yzSrcRect.width)
					yzSrcRect.y = yz_image.getWidth()-yzSrcRect.width;
			} else {
				yzSrcRect.y += dy;
				if (yzSrcRect.y<0)
					yzSrcRect.y = 0;
				if (yzSrcRect.y>yz_image.getHeight()-yzSrcRect.height)
					yzSrcRect.y = yz_image.getHeight()-yzSrcRect.height;
			}
			yzic.repaint();
			int dx = srcRect.x - startingSrcRect.x;
			ImageCanvas xzic = xz_image.getCanvas();
			Rectangle xzSrcRect =xzic.getSrcRect();
			xzSrcRect.x += dx;
			if (xzSrcRect.x<0)
				xzSrcRect.x = 0;
			if (xzSrcRect.x>xz_image.getWidth()-xzSrcRect.width)
				xzSrcRect.x = xz_image.getWidth()-xzSrcRect.width;
			xzic.repaint();
		}
	}
	
	/** Refresh the output windows. */
	synchronized void update() {
		notify();
	}
	
	/**
	 * Exec.
	 */
	private void exec() {
		if (canvas==null) return;
		int width=imp.getWidth();
		int height=imp.getHeight();
		if (hyperstack) {
			int c = imp.getChannel();
			int t = imp.getFrame();
			if (c!=currentChannel || t!=currentFrame)
				imageStack = null;
			if (imp.isComposite()) {
				int mode = ((CompositeImage)imp).getMode();
				if (mode!=currentMode)
					imageStack = null;
			}
		}
		ImageStack is=imageStack;
		if (is==null)
			is = imageStack = getStack();
		double arat=az/ax;
		double brat=az/ay;
		Point p=crossLoc;
		if (p.y>=height) p.y=height-1;
		if (p.x>=width) p.x=width-1;
		if (p.x<0) p.x=0;
		if (p.y<0) p.y=0;
		updateViews(p, is);
		GeneralPath path = new GeneralPath();
		drawCross(imp, p, path);
		imp.setOverlay(path, color, new BasicStroke(1));
		canvas.setCustomRoi(true);
		updateCrosses(p.x, p.y, arat, brat);
		if (syncZoom) updateMagnification(p.x, p.y);
		arrangeWindows(sticky);
	}

	/**
	 * Update crosses.
	 *
	 * @param x the x
	 * @param y the y
	 * @param arat the arat
	 * @param brat the brat
	 */
	private void updateCrosses(int x, int y, double arat, double brat) {
		Point p;
		int z=imp.getNSlices();
		int zlice=imp.getSlice()-1;
		int zcoord=(int)Math.round(arat*zlice);
		if (flipXZ) zcoord = (int)Math.round(arat*(z-zlice));
		
		ImageCanvas xzCanvas = xz_image.getCanvas();
		p=new Point (x, zcoord);
		GeneralPath path = new GeneralPath();
		drawCross(xz_image, p, path);
		xz_image.setOverlay(path, color, new BasicStroke(1));
		if (rotateYZ) {
			if (flipXZ)
				zcoord=(int)Math.round(brat*(z-zlice));
			else
				zcoord=(int)Math.round(brat*(zlice));
			p=new Point (y, zcoord);
		} else {
			zcoord=(int)Math.round(arat*zlice);
			p=new Point (zcoord, y);
		}
		path = new GeneralPath();
		drawCross(yz_image, p, path);
		yz_image.setOverlay(path, color, new BasicStroke(1));
		IJ.showStatus(imp.getLocationAsString(crossLoc.x, crossLoc.y));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key==KeyEvent.VK_ESCAPE) {
			IJ.beep();
			dispose();
		} else if (IJ.shiftKeyDown()) {
			int width=imp.getWidth(), height=imp.getHeight();
			switch (key) {
				case KeyEvent.VK_LEFT: crossLoc.x--; if (crossLoc.x<0) crossLoc.x=0; break;
				case KeyEvent.VK_RIGHT: crossLoc.x++; if (crossLoc.x>=width) crossLoc.x=width-1; break;
				case KeyEvent.VK_UP: crossLoc.y--; if (crossLoc.y<0) crossLoc.y=0; break;
				case KeyEvent.VK_DOWN: crossLoc.y++; if (crossLoc.y>=height) crossLoc.y=height-1; break;
				default: return;
			}
			update();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ev) {
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageClosed(ij.ImagePlus)
	 */
	public void imageClosed(ImagePlus imp) {
		dispose();
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageOpened(ij.ImagePlus)
	 */
	public void imageOpened(ImagePlus imp) {
	}

	/* (non-Javadoc)
	 * @see ij.ImageListener#imageUpdated(ij.ImagePlus)
	 */
	public void imageUpdated(ImagePlus imp) {
		if (imp==this.imp) {
			ImageProcessor ip = imp.getProcessor();
			min = ip.getMin();
			max = ip.getMax();
			update();
		}
	}

	/* (non-Javadoc)
	 * @see ij.CommandListener#commandExecuting(java.lang.String)
	 */
	public String commandExecuting(String command) {
		if (command.equals("In")||command.equals("Out")) {
			ImagePlus cimp = WindowManager.getCurrentImage();
			if (cimp==null) return command;
			if (cimp==imp) {
				ImageCanvas ic = imp.getCanvas();
				if (ic==null) return null;
				int x = ic.screenX(crossLoc.x);
				int y = ic.screenY(crossLoc.y);
				if (command.equals("In")) {
					ic.zoomIn(x, y);
					if (ic.getMagnification()<=1.0) imp.repaintWindow();
				} else {
					ic.zoomOut(x, y);
					if (ic.getMagnification()<1.0) imp.repaintWindow();
				}
				xyX=crossLoc.x; xyY=crossLoc.y;
				update();
				return null;
			} else if (cimp==xz_image || cimp==yz_image) {
				syncZoom = false;
				return command;
			} else
				return command;
		} else if (command.equals("Flip Vertically")&& xz_image!=null) {
			if (xz_image==WindowManager.getCurrentImage()) {
				flipXZ = !flipXZ;
				update();
				return null;
			} else
				return command;
		} else
			return command;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		dispose();		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
		 arrangeWindows(sticky);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		update();
	}
		
	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getSource().equals(xz_image.getWindow())) {
			crossLoc.y += e.getWheelRotation();
		} else if (e.getSource().equals(yz_image.getWindow())) {
			crossLoc.x += e.getWheelRotation();
		}
		update();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		ImageCanvas ic = imp.getCanvas();
		if (ic!=null) canvas.requestFocus();
		arrangeWindows(sticky);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		arrangeWindows(sticky);
	}
	
	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public static ImagePlus getImage() {
		if (instance!=null)
			return instance.imp;
		else
			return null;
	}
	
	/**
	 * Checks if is ortho views image.
	 *
	 * @param imp the imp
	 * @return true, if is ortho views image
	 */
	public static synchronized boolean isOrthoViewsImage(ImagePlus imp) {
		if (imp==null || instance==null)
			return false;
		else
			return imp==instance.imp || imp==instance.xz_image || imp==instance.yz_image;
	}

	/**
	 * Gets the single instance of Orthogonal_Views.
	 *
	 * @return single instance of Orthogonal_Views
	 */
	public static Orthogonal_Views getInstance() {
		return instance;
	}

	/**
	 * Gets the cross loc.
	 *
	 * @return the cross loc
	 */
	public int[] getCrossLoc() {
		int[] loc = new int[3];
		loc[0] = crossLoc.x;
		loc[1] = crossLoc.y;
		loc[2] = imp.getSlice()-1;
		return loc;
	}
	
	/**
	 * Sets the cross loc.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setCrossLoc(int x, int y, int z) {
		crossLoc.setLocation(x, y);
		if (hyperstack)
			imp.setPosition(imp.getChannel(), z+1, imp.getFrame());
		else
			imp.setSlice(z+1);
		update();
	}
	
	/**
	 * Gets the XZ image.
	 *
	 * @return the XZ image
	 */
	public ImagePlus getXZImage(){
		return xz_image;
	}
	
	/**
	 * Gets the YZ image.
	 *
	 * @return the YZ image
	 */
	public ImagePlus getYZImage(){
		return yz_image;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
			}
			if (!done)
				exec();
		}
	}

}
