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
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ImageProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;


// TODO: Auto-generated Javadoc
/** This is an Roi subclass for creating and displaying arrows. */
public class Arrow extends Line {
	
	/** The Constant STYLE_KEY. */
	public static final String STYLE_KEY = "arrow.style";
	
	/** The Constant WIDTH_KEY. */
	public static final String WIDTH_KEY = "arrow.width";
	
	/** The Constant SIZE_KEY. */
	public static final String SIZE_KEY = "arrow.size";
	
	/** The Constant DOUBLE_HEADED_KEY. */
	public static final String DOUBLE_HEADED_KEY = "arrow.double";
	
	/** The Constant OUTLINE_KEY. */
	public static final String OUTLINE_KEY = "arrow.outline";
	
	/** The Constant BAR. */
	public static final int FILLED=0, NOTCHED=1, OPEN=2, HEADLESS=3, BAR=4;
	
	/** The Constant styles. */
	public static final String[] styles = {"Filled", "Notched", "Open", "Headless", "Bar"};
	
	/** The default style. */
	private static int defaultStyle = (int)Prefs.get(STYLE_KEY, FILLED);
	
	/** The default width. */
	private static float defaultWidth = (float)Prefs.get(WIDTH_KEY, 2);
	
	/** The default head size. */
	private static double defaultHeadSize = (int)Prefs.get(SIZE_KEY, 10);  // 0-30;
	
	/** The default double headed. */
	private static boolean defaultDoubleHeaded = Prefs.get(DOUBLE_HEADED_KEY, false);
	
	/** The default outline. */
	private static boolean defaultOutline = Prefs.get(OUTLINE_KEY, false);
	
	/** The style. */
	private int style;
	
	/** The head size. */
	private double headSize = 10;  // 0-30
	
	/** The double headed. */
	private boolean doubleHeaded;
	
	/** The outline. */
	private boolean outline;
	
	/** The points. */
	private float[] points = new float[2*5];
	
	/** The path. */
	private GeneralPath path = new GeneralPath();
	
	/** The default stroke. */
	private static Stroke defaultStroke = new BasicStroke();
	
	/** The head shaft ratio. */
	double headShaftRatio;
	
	static {
		if (defaultStyle<FILLED || defaultStyle>HEADLESS)
			defaultStyle = FILLED;
	}

	/**
	 * Instantiates a new arrow.
	 *
	 * @param ox1 the ox 1
	 * @param oy1 the oy 1
	 * @param ox2 the ox 2
	 * @param oy2 the oy 2
	 */
	public Arrow(double ox1, double oy1, double ox2, double oy2) {
		super(ox1, oy1, ox2, oy2);
		setStrokeWidth(2);
	}

	/**
	 * Instantiates a new arrow.
	 *
	 * @param sx the sx
	 * @param sy the sy
	 * @param imp the imp
	 */
	public Arrow(int sx, int sy, ImagePlus imp) {
		super(sx, sy, imp);
		setStrokeWidth(defaultWidth);
		style = defaultStyle;
		headSize = defaultHeadSize;
		doubleHeaded = defaultDoubleHeaded;
		outline = defaultOutline;
		setStrokeColor(Toolbar.getForegroundColor());
	}

	/**
	 *  Draws this arrow on the image.
	 *
	 * @param g the g
	 */
	public void draw(Graphics g) {
		Shape shape2 = null;
		if (doubleHeaded) {
			flipEnds();
			shape2 = getShape();
			flipEnds();
		}
		Shape shape = getShape();
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		if (fillColor!=null) color = fillColor;
		g.setColor(color);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		AffineTransform at = g2.getDeviceConfiguration().getDefaultTransform();
		double mag = getMagnification();
		int xbase=0, ybase=0;
		if (ic!=null) {
			Rectangle r = ic.getSrcRect();
			xbase = r.x; ybase = r.y;
		}
		at.setTransform(mag, 0.0, 0.0, mag, -xbase*mag, -ybase*mag);
		if (outline) {
			float lineWidth = (float)(getOutlineWidth()*mag);
			g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g2.draw(at.createTransformedShape(shape));
			if (doubleHeaded) g2.draw(at.createTransformedShape(shape2));
			g2.setStroke(defaultStroke);
		} else  {
			g2.fill(at.createTransformedShape(shape));
			if (doubleHeaded) g2.fill(at.createTransformedShape(shape2));
		}
		if (state!=CONSTRUCTING && !overlay) {
			int size2 = HANDLE_SIZE/2;
			handleColor=Color.white;
			drawHandle(g, screenXD(x1d)-size2, screenYD(y1d)-size2);
			drawHandle(g, screenXD(x2d)-size2, screenYD(y2d)-size2);
			drawHandle(g, screenXD(x1d+(x2d-x1d)/2.0)-size2, screenYD(y1d+(y2d-y1d)/2.0)-size2);
		}
		if (state!=NORMAL && imp!=null && imp.getRoi()!=null)
			showStatus();
		if (updateFullWindow) 
			{updateFullWindow = false; imp.draw();}
	}
		
	/**
	 * Flip ends.
	 */
	private void flipEnds() {
		double tmp = x1R;
		x1R=x2R;
		x2R=tmp;
		tmp=y1R;
		y1R=y2R;
		y2R=tmp;
	}
	
	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	private Shape getPath() {
		path.reset();
		path = new GeneralPath();
		calculatePoints();
		float tailx = points[0];
		float taily = points[1];
		float headbackx = points[2*1];
		float headbacky = points[2*1+1];
		float headtipx = points[2*3];
		float headtipy = points[2*3+1];
		if (outline) {
			double dx = headtipx - tailx;
			double dy = headtipy - taily;
			double shaftLength = Math.sqrt(dx*dx+dy*dy);
			dx = headtipx - headbackx;
			dy = headtipy- headbacky;
			double headLength = Math.sqrt(dx*dx+dy*dy);
			headShaftRatio = headLength/shaftLength;
			if (headShaftRatio>1.0)
				headShaftRatio = 1.0;
			//IJ.log(headShaftRatio+" "+(int)shaftLength+" "+(int)headLength+" "+(int)tailx+" "+(int)taily+" "+(int)headtipx+" "+(int)headtipy);
		}
		path.moveTo(tailx, taily); // tail
		path.lineTo(headbackx, headbacky); // head back
		path.moveTo(headbackx, headbacky); // head back
		if (style==OPEN)
			path.moveTo(points[2 * 2], points[2 * 2 + 1]);
		else
			path.lineTo(points[2 * 2], points[2 * 2 + 1]); // left point
		path.lineTo(headtipx, headtipy); // head tip
		path.lineTo(points[2 * 4], points[2 * 4 + 1]); // right point
		path.lineTo(headbackx, headbacky); // back to the head back
		return path;
	}

	 /** Based on the method with the same name in Fiji's Arrow plugin,
	 	written by Jean-Yves Tinevez and Johannes Schindelin. */
	 private void calculatePoints() {
		double tip = 0.0;
		double base;
		double shaftWidth = getStrokeWidth();
		double length = 8+10*shaftWidth*0.5;
		length = length*(headSize/10.0);
		length -= shaftWidth*1.42;
		if (style==NOTCHED) length*=0.74;
		if (style==OPEN) length*=1.32;
		if (length<0.0 || style==HEADLESS) length=0.0;
		double x = getXBase();
		double y = getYBase();
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		double dx=x2d-x1d, dy=y2d-y1d;
		double arrowLength = Math.sqrt(dx*dx+dy*dy);
		dx=dx/arrowLength; dy=dy/arrowLength;
		if (doubleHeaded && style!=HEADLESS) {
			points[0] = (float)(x1d+dx*shaftWidth*2.0);
			points[1] = (float)(y1d+dy*shaftWidth*2.0);
		} else {
			points[0] = (float)x1d;
			points[1] = (float)y1d;
		}
        if (length>0) {
			double factor = style==OPEN?1.3:1.42;
			points[2*3] = (float)(x2d-dx*shaftWidth*factor);
			points[2*3+1] = (float)(y2d-dy*shaftWidth*factor);
			if (style==BAR) {
				points[2*3] = (float)(x2d-dx*shaftWidth*0.5);
				points[2*3+1] = (float)(y2d-dy*shaftWidth*0.5);
			}
		} else {
			points[2*3] = (float)x2d;
			points[2*3+1] = (float)y2d;
		}
		final double alpha = Math.atan2(points[2*3+1]-points[1], points[2*3]-points[0]);
		double SL = 0.0;
		switch (style) {
			case FILLED: case HEADLESS:
				tip = Math.toRadians(20.0);
				base = Math.toRadians(90.0);
				points[1*2]   = (float) (points[2*3]	- length*Math.cos(alpha));
				points[1*2+1] = (float) (points[2*3+1] - length*Math.sin(alpha));
				SL = length*Math.sin(base)/Math.sin(base+tip);;
				break;
			case NOTCHED:
				tip = Math.toRadians(20);
				base = Math.toRadians(120);
				points[1*2]   = (float) (points[2*3] - length*Math.cos(alpha));
				points[1*2+1] = (float) (points[2*3+1] - length*Math.sin(alpha));
				SL = length*Math.sin(base)/Math.sin(base+tip);;
				break;
			case OPEN:
				tip = Math.toRadians(25); //30
				points[1*2] = points[2*3];
				points[1*2+1] = points[2*3+1];
				SL = length;
				break;
			case BAR:
				tip = Math.toRadians(90); //30
				points[1*2] = points[2*3];
				points[1*2+1] = points[2*3+1];
				SL = length;
				updateFullWindow = true;
				break;       
		}
		// P2 = P3 - SL*alpha+tip
		points[2*2] = (float) (points[2*3]	- SL*Math.cos(alpha+tip));
		points[2*2+1] = (float) (points[2*3+1] - SL*Math.sin(alpha+tip));
		// P4 = P3 - SL*alpha-tip
		points[2*4]   = (float) (points[2*3]	- SL*Math.cos(alpha-tip));
		points[2*4+1] = (float) (points[2*3+1] - SL*Math.sin(alpha-tip));
 	}
 	
	/**
	 * Gets the shape.
	 *
	 * @return the shape
	 */
	private Shape getShape() {
		Shape arrow = getPath();
		BasicStroke stroke = new BasicStroke((float)getStrokeWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		Shape outlineShape = stroke.createStrokedShape(arrow);
		Area a1 = new Area(arrow);
		Area a2 = new Area(outlineShape);
		try {a1.add(a2);} catch(Exception e) {};
		return a1;
	}

	/**
	 * Gets the shape roi.
	 *
	 * @return the shape roi
	 */
	private ShapeRoi getShapeRoi() {
		Shape arrow = getPath();
		BasicStroke stroke = new BasicStroke(getStrokeWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		ShapeRoi sroi = new ShapeRoi(arrow);
		Shape outlineShape = stroke.createStrokedShape(arrow);
		sroi.or(new ShapeRoi(outlineShape));
		return sroi;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#getMask()
	 */
	public ImageProcessor getMask() {
		if (width==0 && height==0)
			return null;
		else
			return getShapeRoi().getMask();
	}

	/**
	 * Gets the outline width.
	 *
	 * @return the outline width
	 */
	private double getOutlineWidth() {
		double width = getStrokeWidth()/8.0;
		double head = headSize/7.0;
		double lineWidth = width + head + headShaftRatio;
		if (lineWidth<1.0) lineWidth = 1.0;
		//if (width<1) width=1;
		//if (head<1) head=1;
		//IJ.log(getStrokeWidth()+"  "+IJ.d2s(width,2)+"  "+IJ.d2s(head,2)+"  "+IJ.d2s(headShaftRatio,2)+"  "+IJ.d2s(lineWidth,2)+"  "+IJ.d2s(width*head,2));
		return lineWidth;
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Line#drawPixels(ij.process.ImageProcessor)
	 */
	public void drawPixels(ImageProcessor ip) {
		ShapeRoi shapeRoi = getShapeRoi();
		ShapeRoi shapeRoi2 = null;
		if (doubleHeaded) {
			flipEnds();
			shapeRoi2 = getShapeRoi();
			flipEnds();
		}
		if (outline) {
			int lineWidth = ip.getLineWidth();
			ip.setLineWidth((int)Math.round(getOutlineWidth()));
			shapeRoi.drawPixels(ip);
			if (doubleHeaded) shapeRoi2.drawPixels(ip);
			ip.setLineWidth(lineWidth);
		} else {
			ip.fill(shapeRoi);
			if (doubleHeaded) ip.fill(shapeRoi2);
		}
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Line#contains(int, int)
	 */
	public boolean contains(int x, int y) {
		return getShapeRoi().contains(x, y);
	}

	/**
	 *  Return the bounding rectangle of this arrow.
	 *
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return getShapeRoi().getBounds();
	}

	/* (non-Javadoc)
	 * @see ij.gui.Line#handleMouseDown(int, int)
	 */
	protected void handleMouseDown(int sx, int sy) {
		super.handleMouseDown(sx, sy);
		startxd = ic!=null?ic.offScreenXD(sx):sx;
		startyd = ic!=null?ic.offScreenYD(sy):sy;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Line#clipRectMargin()
	 */
	protected int clipRectMargin() {
		double mag = getMagnification();
		double arrowWidth = getStrokeWidth();
		double size = 8+10*arrowWidth*mag*0.5;
		return (int)Math.max(size*2.0, headSize);
	}
			
	/* (non-Javadoc)
	 * @see ij.gui.Roi#isDrawingTool()
	 */
	public boolean isDrawingTool() {
		return true;
	}
	
	/**
	 * Sets the default width.
	 *
	 * @param width the new default width
	 */
	public static void setDefaultWidth(double width) {
		defaultWidth = (float)width;
	}

	/**
	 * Gets the default width.
	 *
	 * @return the default width
	 */
	public static double getDefaultWidth() {
		return defaultWidth;
	}

	/**
	 * Sets the style.
	 *
	 * @param style the new style
	 */
	public void setStyle(int style) {
		this.style = style;
	}
	
	/**
	 * Sets the style.
	 *
	 * @param style the new style
	 */
	/* Set the style, where 'style' is "filled", "notched", "open", "headless" or "bar",
		plus optionial modifiers of "outline", "double", "small", "medium" and "large". */
	public void setStyle(String style) {
		style = style.toLowerCase();
		int newStyle = Arrow.FILLED;
		if (style.contains("notched"))
			newStyle = Arrow.NOTCHED;
		else if (style.contains("open"))
			newStyle = Arrow.OPEN;
		else if (style.contains("headless"))
			newStyle = Arrow.HEADLESS;
		else if (style.contains("bar"))
			newStyle = Arrow.BAR;
		setStyle(newStyle);
		setOutline(style.contains("outline"));
		setDoubleHeaded(style.contains("double"));
		if (style.contains("small"))
			setHeadSize(5);
		else if (style.contains("large"))
			setHeadSize(15);
	}

	/**
	 * Gets the style.
	 *
	 * @return the style
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * Sets the default style.
	 *
	 * @param style the new default style
	 */
	public static void setDefaultStyle(int style) {
		defaultStyle = style;
	}

	/**
	 * Gets the default style.
	 *
	 * @return the default style
	 */
	public static int getDefaultStyle() {
		return defaultStyle;
	}

	/**
	 * Sets the head size.
	 *
	 * @param headSize the new head size
	 */
	public void setHeadSize(double headSize) {
		this.headSize = headSize;
	}

	/**
	 * Gets the head size.
	 *
	 * @return the head size
	 */
	public double getHeadSize() {
		return headSize;
	}

	/**
	 * Sets the default head size.
	 *
	 * @param size the new default head size
	 */
	public static void setDefaultHeadSize(double size) {
		defaultHeadSize = size;
	}

	/**
	 * Gets the default head size.
	 *
	 * @return the default head size
	 */
	public static double getDefaultHeadSize() {
		return defaultHeadSize;
	}

	/**
	 * Sets the double headed.
	 *
	 * @param b the new double headed
	 */
	public void setDoubleHeaded(boolean b) {
		doubleHeaded = b;
	}

	/**
	 * Gets the double headed.
	 *
	 * @return the double headed
	 */
	public boolean getDoubleHeaded() {
		return doubleHeaded;
	}

	/**
	 * Sets the default double headed.
	 *
	 * @param b the new default double headed
	 */
	public static void setDefaultDoubleHeaded(boolean b) {
		defaultDoubleHeaded = b;
	}

	/**
	 * Gets the default double headed.
	 *
	 * @return the default double headed
	 */
	public static boolean getDefaultDoubleHeaded() {
		return defaultDoubleHeaded;
	}

	/**
	 * Sets the outline.
	 *
	 * @param b the new outline
	 */
	public void setOutline(boolean b) {
		outline = b;
	}

	/**
	 * Gets the outline.
	 *
	 * @return the outline
	 */
	public boolean getOutline() {
		return outline;
	}

	/**
	 * Sets the default outline.
	 *
	 * @param b the new default outline
	 */
	public static void setDefaultOutline(boolean b) {
		defaultOutline = b;
	}

	/**
	 * Gets the default outline.
	 *
	 * @return the default outline
	 */
	public static boolean getDefaultOutline() {
		return defaultOutline;
	}

}
