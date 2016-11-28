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
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.plugin.Straightener;
import ij.plugin.frame.Recorder;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


// TODO: Auto-generated Javadoc
/** This class represents a straight line selection. */
public class Line extends Roi {

	/** The y 2. */
	public int x1, y1, x2, y2;	// the line
	
	/** The y 2 d. */
	public double x1d, y1d, x2d, y2d;	// the line using sub-pixel coordinates
	
	/** The y 2 R. */
	protected double x1R, y1R, x2R, y2R;  // the line, relative to base of bounding rect
	
	/** The y handle offset. */
	private double xHandleOffset, yHandleOffset;
	
	/** The startyd. */
	protected double startxd, startyd;
	
	/** The width changed. */
	static boolean widthChanged;
	
	/** The draw offset. */
	private boolean drawOffset;
	
	/** The dragged. */
	private boolean dragged;
	
	/** The mouse up count. */
	private int mouseUpCount;

	/**
	 *  Creates a new straight line selection using the specified
	 * 		starting and ending offscreen integer coordinates.
	 *
	 * @param ox1 the ox 1
	 * @param oy1 the oy 1
	 * @param ox2 the ox 2
	 * @param oy2 the oy 2
	 */
	public Line(int ox1, int oy1, int ox2, int oy2) {
		this((double)ox1, (double)oy1, (double)ox2, (double)oy2);
	}

	/**
	 *  Creates a new straight line selection using the specified
	 * 		starting and ending offscreen double coordinates.
	 *
	 * @param ox1 the ox 1
	 * @param oy1 the oy 1
	 * @param ox2 the ox 2
	 * @param oy2 the oy 2
	 */
	public Line(double ox1, double oy1, double ox2, double oy2) {
		super((int)ox1, (int)oy1, 0, 0);
		type = LINE;
		x1d=ox1; y1d=oy1; x2d=ox2; y2d=oy2; 
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		x=(int)Math.min(x1d,x2d); y=(int)Math.min(y1d,y2d);
		x1R=x1d-x; y1R=y1d-y; x2R=x2d-x; y2R=y2d-y;
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		if (!(this instanceof Arrow) && lineWidth>1)
			updateWideLine(lineWidth);
		updateClipRect();
		oldX=x; oldY=y; oldWidth=width; oldHeight=height;
		state = NORMAL;
	}

	/**
	 *  Starts the process of creating a new user-generated straight line
	 * 		selection. 'sx' and 'sy' are screen coordinates that specify
	 * 		the start of the line. The user will determine the end of the line
	 * 		interactively using rubber banding.
	 *
	 * @param sx the sx
	 * @param sy the sy
	 * @param imp the imp
	 */
	public Line(int sx, int sy, ImagePlus imp) {
		super(sx, sy, imp);
		startxd = ic.offScreenXD(sx);
		startyd = ic.offScreenYD(sy);
		x1R = x2R = startxd - startX;
		y1R = y2R = startyd - startY;
		type = LINE;
		if (!(this instanceof Arrow) && lineWidth>1)
			updateWideLine(lineWidth);
		drawOffset = Prefs.subPixelResolution;
	}

	/**
	 * Instantiates a new line.
	 *
	 * @param ox1 the ox 1
	 * @param oy1 the oy 1
	 * @param ox2 the ox 2
	 * @param oy2 the oy 2
	 * @param imp the imp
	 * @deprecated replaced by Line(int, int, int, int)
	 */
	public Line(int ox1, int oy1, int ox2, int oy2, ImagePlus imp) {
		this(ox1, oy1, ox2, oy2);
		setImage(imp);
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#grow(int, int)
	 */
	protected void grow(int sx, int sy) { //mouseDragged
		drawLine(sx, sy);
		dragged = true;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		drawLine(e.getX(), e.getY());
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#handleMouseUp(int, int)
	 */
	protected void handleMouseUp(int screenX, int screenY) {
		mouseUpCount++;
		if (Prefs.enhancedLineTool && mouseUpCount==1 && !dragged)
			return;
		state = NORMAL;
		if (imp==null) return;
		imp.draw(clipX-5, clipY-5, clipWidth+10, clipHeight+10);
		if (Recorder.record) {
			String method = (this instanceof Arrow)?"makeArrow":"makeLine";
			Recorder.record(method, x1, y1, x2, y2);
		}
		if (getLength()==0.0)
			imp.deleteRoi();
	}

	/**
	 * Draw line.
	 *
	 * @param sx the sx
	 * @param sy the sy
	 */
	protected void drawLine(int sx, int sy) {
		double xend = ic!=null?ic.offScreenXD(sx):sx;
		double yend = ic!=null?ic.offScreenYD(sy):sy;
		if (xend<0.0) xend=0.0; if (yend<0.0) yend=0.0;
		if (xend>xMax) xend=xMax; if (yend>yMax) yend=yMax;
		double xstart=x+x1R, ystart=y+y1R;
		if (constrain) {
		    int i=0;
	        double dy = Math.abs(yend-ystart);
	        double dx = Math.abs(xend-xstart);
	        double comp = dy / dx;
	        
	        for(;i<PI_SEARCH.length; i++) {
	            if(comp < PI_SEARCH[i]) {
	                break;
	            }
	        }
	        
	        if(i < PI_SEARCH.length) {
	            if(yend > ystart) {
	                yend = ystart + dx*PI_MULT[i];
	            } else {
	                yend = ystart - dx*PI_MULT[i];
	            }
	        } else {
	            xend = xstart;
	        }
		}
		x=(int)Math.min(x+x1R,xend); y=(int)Math.min(y+y1R,yend);
		x1R=xstart-x; y1R=ystart-y;
		x2R=xend-x; y2R=yend-y;
		if (IJ.controlKeyDown()) {
			x1R=(int)Math.round(x1R); y1R=(int)Math.round(y1R);
			x2R=(int)Math.round(x2R); y2R=(int)Math.round(y2R);
		}
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		if (width<1) width=1; if (height<1) height=1;
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX=x; oldY=y;
		oldWidth=width; oldHeight=height;
	}
	
	/**  Used for angle searches in line ROI creation. */
	private static final double[] PI_SEARCH = {Math.tan(Math.PI/8), Math.tan((3*Math.PI)/8)};
	
	/** The Constant PI_MULT. */
	private static final double[] PI_MULT = {0, Math.tan((2*Math.PI)/8)};

	/* (non-Javadoc)
	 * @see ij.gui.Roi#move(int, int)
	 */
	void move(int sx, int sy) {
		int xNew = ic.offScreenX(sx);
		int yNew = ic.offScreenY(sy);
		x += xNew - startxd;
		y += yNew - startyd;
		clipboard=null;
		startxd = xNew;
		startyd = yNew;
		updateClipRect();
		if (ignoreClipRect)
			imp.draw();
		else
			imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight=height;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#moveHandle(int, int)
	 */
	protected void moveHandle(int sx, int sy) {
		double offset = getOffset(-0.5);
		double ox = ic.offScreenXD(sx)+offset;
		double oy = ic.offScreenYD(sy)+offset;
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		double length = Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d));
		switch (activeHandle) {
			case 0:
                double dx = ox-x1d;
                double dy = oy-y1d;
                x1d=ox;
                y1d=oy;
                if(center){
                    x2d -= dx;
                    y2d -= dy;
                }
				if(aspect){
					double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
					double xcd = x1d+(x2d-x1d)/2;
					double ycd = y1d+(y2d-y1d)/2;
					
					if(center){
						x1d=xcd-ratio*(xcd-x1d);
						x2d=xcd+ratio*(x2d-xcd);
						y1d=ycd-ratio*(ycd-y1d);
						y2d=ycd+ratio*(y2d-ycd);
					} else {
						x1d=x2d-ratio*(x2d-x1d);
						y1d=y2d-ratio*(y2d-y1d);
					}
					
				}
                break;
			case 1:
                dx = ox-x2d;
                dy = oy-y2d;
                x2d=ox;
                y2d=oy;
                if(center){
                    x1d -= dx;
                    y1d -= dy;
                }
				if(aspect){
					double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
					double xcd = x1d+(x2d-x1d)/2;
					double ycd = y1d+(y2d-y1d)/2;
					
					if(center){
						x1d=xcd-ratio*(xcd-x1d);
						x2d=xcd+ratio*(x2d-xcd);
						y1d=ycd-ratio*(ycd-y1d);
						y2d=ycd+ratio*(y2d-ycd);
					} else {
						x2d=x1d+ratio*(x2d-x1d);
						y2d=y1d+ratio*(y2d-y1d);
					}
										
				}
                break;
			case 2:
				dx = ox-(x1d+(x2d-x1d)/2);
				dy = oy-(y1d+(y2d-y1d)/2);
				x1d+=dx; y1d+=dy; x2d+=dx; y2d+=dy;
				if (getStrokeWidth()>1) {
					x1d+=xHandleOffset; y1d+=yHandleOffset; 
					x2d+=xHandleOffset; y2d+=yHandleOffset;
				}
				break;
		}
		if (constrain) {
			double dx = Math.abs(x1d-x2d);
			double dy = Math.abs(y1d-y2d);
			double xcd = Math.min(x1d,x2d)+dx/2;
			double ycd = Math.min(y1d,y2d)+dy/2;
			
			//double ratio = length/(Math.sqrt((x2d-x1d)*(x2d-x1d) + (y2d-y1d)*(y2d-y1d)));
			if (activeHandle==0) {
				if (dx>=dy) {
					if(aspect){
						if(x2d>x1d) x1d=x2d-length;
						else x1d=x2d+length;
					}
					y1d = y2d;
					if(center){
						y1d=y2d=ycd;
						if(aspect){
							if(xcd>x1d) {
								x1d=xcd-length/2;
								x2d=xcd+length/2;
							}
							else{
								x1d=xcd+length/2;
								x2d=xcd-length/2;
							}
						}
					}
				}else {
					if(aspect){
						if(y2d>y1d) y1d=y2d-length;
						else y1d=y2d+length;
					}
					x1d = x2d;
					if(center){
						x1d=x2d=xcd;
						if(aspect){
							if(ycd>y1d) {
								y1d=ycd-length/2;
								y2d=ycd+length/2;
							}
							else{
								y1d=ycd+length/2;
								y2d=ycd-length/2;
							}
						}
					}
				}
			} else if (activeHandle==1) {
				if (dx>=dy) {
					if(aspect){
						if(x1d>x2d) x2d=x1d-length;
						else x2d=x1d+length;
					}
					y2d= y1d;
					if(center){
						y1d=y2d=ycd;
						if(aspect){
							if(xcd>x1d) {
								x1d=xcd-length/2;
								x2d=xcd+length/2;
							}
							else{
								x1d=xcd+length/2;
								x2d=xcd-length/2;
							}
						}
					}
				} else {
					if(aspect){
						if(y1d>y2d) y2d=y1d-length;
						else y2d=y1d+length;
					}
					x2d = x1d;
					if(center){
						x1d=x2d=xcd;
						if(aspect){
							if(ycd>y1d) {
								y1d=ycd-length/2;
								y2d=ycd+length/2;
							}
							else{
								y1d=ycd+length/2;
								y2d=ycd-length/2;
							}
						}
					}
				}
			}
		}
		x=(int)Math.min(x1d,x2d); y=(int)Math.min(y1d,y2d);
		x1R=x1d-x; y1R=y1d-y;
		x2R=x2d-x; y2R=y2d-y;
		width=(int)Math.abs(x2R-x1R); height=(int)Math.abs(y2R-y1R);
		updateClipRect();
		imp.draw(clipX, clipY, clipWidth, clipHeight);
		oldX = x;
		oldY = y;
		oldWidth = width;
		oldHeight = height;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#mouseDownInHandle(int, int, int)
	 */
	protected void mouseDownInHandle(int handle, int sx, int sy) {
		state = MOVING_HANDLE;
		activeHandle = handle;
		if (getStrokeWidth()<=3)
			ic.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	/**
	 *  Draws this line on the image.
	 *
	 * @param g the g
	 */
	public void draw(Graphics g) {
		Color color =  strokeColor!=null? strokeColor:ROIColor;
		boolean isActiveOverlayRoi = !overlay && isActiveOverlayRoi();
		if (isActiveOverlayRoi) {
			if (color==Color.cyan)
				color = Color.magenta;
			else
				color = Color.cyan;
		}
		double x = getXBase();
		double y = getYBase();
		g.setColor(color);
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		x1=(int)x1d; y1=(int)y1d; x2=(int)x2d; y2=(int)y2d;
		double offset = getOffset(0.5);
		int sx1 = screenXD(x1d+offset);
		int sy1 = screenYD(y1d+offset);
		int sx2 = screenXD(x2d+offset);
		int sy2 = screenYD(y2d+offset);
		int sx3 = sx1 + (sx2-sx1)/2;
		int sy3 = sy1 + (sy2-sy1)/2;
		Graphics2D g2d = (Graphics2D)g;
		if (stroke!=null && !isActiveOverlayRoi) 
			g2d.setStroke(getScaledStroke());
		g.drawLine(sx1, sy1, sx2, sy2);
		if (wideLine && !overlay) {
			g2d.setStroke(onePixelWide);
			g.setColor(getColor());
			g.drawLine(sx1, sy1, sx2, sy2);
		}
		if (!overlay) {
			int size2 = HANDLE_SIZE/2;
			mag = getMagnification();
			handleColor = strokeColor!=null?strokeColor:ROIColor;
			drawHandle(g, sx1-size2, sy1-size2);
			handleColor=Color.white;
			drawHandle(g, sx2-size2, sy2-size2);
			drawHandle(g, sx3-size2, sy3-size2);
		}
		if (state!=NORMAL)
			IJ.showStatus(imp.getLocationAsString(x2,y2)+", angle=" + IJ.d2s(getAngle()) + ", length=" + IJ.d2s(getLength()));
		if (updateFullWindow)
			{updateFullWindow = false; imp.draw();}
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Roi#getAngle()
	 */
	public double getAngle() {
		return getFloatAngle(x1d, y1d, x2d, y2d);
	}

	/**
	 *  Returns the length of this line.
	 *
	 * @return the length
	 */
	public double getLength() {
		if (imp==null || IJ.altKeyDown())
			return getRawLength();
		else {
			Calibration cal = imp.getCalibration();
			return Math.sqrt((x2d-x1d)*cal.pixelWidth*(x2d-x1d)*cal.pixelWidth
				+ (y2d-y1d)*cal.pixelHeight*(y2d-y1d)*cal.pixelHeight);
		}
	}

	/**
	 *  Returns the length of this line in pixels.
	 *
	 * @return the raw length
	 */
	public double getRawLength() {
		return Math.sqrt((x2d-x1d)*(x2d-x1d)+(y2d-y1d)*(y2d-y1d));
	}

	/**
	 *  Returns the pixel values along this line.
	 *
	 * @return the pixels
	 */
	public double[] getPixels() {
			double[] profile;
			if (getStrokeWidth()<=1) {
				ImageProcessor ip = imp.getProcessor();
				profile = ip.getLine(x1d, y1d, x2d, y2d);
			} else {
				ImageProcessor ip2 = (new Straightener()).rotateLine(imp,(int)getStrokeWidth());
				if (ip2==null) return new double[0];
				int width = ip2.getWidth();
				int height = ip2.getHeight();
				profile = new double[width];
				double[] aLine;
				ip2.setInterpolate(false);
				for (int y=0; y<height; y++) {
					aLine = ip2.getLine(0, y, width-1, y);
					for (int i=0; i<width; i++)
						profile[i] += aLine[i];
				}
				for (int i=0; i<width; i++)
					profile[i] /= height;
			}
			return profile;
	}
	
	/**
	 *  Returns, as a Polygon, the two points that define this line.
	 *
	 * @return the points
	 */
	public Polygon getPoints() {
		Polygon p = new Polygon();
		p.addPoint((int)Math.round(x1d), (int)Math.round(y1d));
		p.addPoint((int)Math.round(x2d), (int)Math.round(y2d));
		return p;
	}

	/**
	 *  Returns, as a FloatPolygon, the two points that define this line.
	 *
	 * @return the float points
	 */
	public FloatPolygon getFloatPoints() {
		FloatPolygon p = new FloatPolygon();
		p.addPoint((float)x1d, (float)y1d);
		p.addPoint((float)x2d, (float)y2d);
		return p;
	}

	/**
	 *  If the width of this line is less than or equal to one, returns the
	 * starting and ending coordinates as a 2-point Polygon, or, if
	 * the width is greater than one, returns an outline of the line as
	 * a 4-point Polygon.
	 *
	 * @return the polygon
	 * @see #getFloatPolygon
	 * @see #getPoints
	 */
	public Polygon getPolygon() {
		FloatPolygon p = getFloatPolygon();
		return new Polygon(toIntR(p.xpoints), toIntR(p.ypoints), p.npoints);
	}

	/**
	 *  If the width of this line is less than or equal to one, returns the
	 * starting and ending coordinates as a 2-point FloatPolygon, or, if
	 * the width is greater than one, returns an outline of the line as
	 * a 4-point FloatPolygon.
	 *
	 * @return the float polygon
	 * @see #getFloatPoints
	 */
	public FloatPolygon getFloatPolygon() {
		double x = getXBase();
		double y = getYBase();
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		FloatPolygon p = new FloatPolygon();
		if (getStrokeWidth()<=1) {
			p.addPoint((float)x1d, (float)y1d);
			p.addPoint((float)x2d, (float)y2d);
		} else {
			double angle = Math.atan2(y1d-y2d, x2d-x1d);
			double width2 = getStrokeWidth()/2.0;
			double p1x = x1d + Math.cos(angle+Math.PI/2d)*width2;
			double p1y = y1d - Math.sin(angle+Math.PI/2d)*width2;
			double p2x = x1d + Math.cos(angle-Math.PI/2d)*width2;
			double p2y = y1d - Math.sin(angle-Math.PI/2d)*width2;
			double p3x = x2d + Math.cos(angle-Math.PI/2d)*width2;
			double p3y = y2d - Math.sin(angle-Math.PI/2d)*width2;
			double p4x = x2d + Math.cos(angle+Math.PI/2d)*width2;
			double p4y = y2d - Math.sin(angle+Math.PI/2d)*width2;
			p.addPoint((float)p1x, (float)p1y);
			p.addPoint((float)p2x, (float)p2y);
			p.addPoint((float)p3x, (float)p3y);
			p.addPoint((float)p4x, (float)p4y);
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#drawPixels(ij.process.ImageProcessor)
	 */
	public void drawPixels(ImageProcessor ip) {
		ip.setLineWidth(1);
		double x = getXBase();
		double y = getYBase();
		x1d=x+x1R; y1d=y+y1R; x2d=x+x2R; y2d=y+y2R;
		double offset = getOffset(0.5);
		if (getStrokeWidth()<=1) {
			ip.moveTo((int)(x1d+offset), (int)(y1d+offset));
			ip.lineTo((int)(x2d+offset), (int)(y2d+offset));
		} else {
			Polygon p = null;
			if (offset>0.0) {
				FloatPolygon fp = getFloatPolygon();
				for (int i=0; i<fp.npoints; i++) {
					fp.xpoints[i] += offset;
					fp.ypoints[i] += offset;
				}
				p = new Polygon(toIntR(fp.xpoints), toIntR(fp.ypoints), fp.npoints);
			} else
				p = getPolygon();
			ip.drawPolygon(p);
			updateFullWindow = true;
		}
	}

	/* (non-Javadoc)
	 * @see ij.gui.Roi#contains(int, int)
	 */
	public boolean contains(int x, int y) {
		if (getStrokeWidth()>1) {
			if ((x==x1&&y==y1) || (x==x2&&y==y2))
				return true;
			else
				return getPolygon().contains(x,y);
		} else
			return false;
	}
		
	/* (non-Javadoc)
	 * @see ij.gui.Roi#handleMouseDown(int, int)
	 */
	protected void handleMouseDown(int sx, int sy) {
		super.handleMouseDown(sx, sy);
		startxd = ic.offScreenXD(sx);
		startyd = ic.offScreenYD(sy);
	}

	/**
	 *  Returns a handle number if the specified screen coordinates are  
	 * 		inside or near a handle, otherwise returns -1.
	 *
	 * @param sx the sx
	 * @param sy the sy
	 * @return the int
	 */
	public int isHandle(int sx, int sy) {
		int size = HANDLE_SIZE+5;
		if (getStrokeWidth()>1) size += (int)Math.log(getStrokeWidth());
		int halfSize = size/2;
		double offset = getOffset(0.5);
		int sx1 = ic.screenXD(x+x1R+offset) - halfSize;
		int sy1 = ic.screenYD(y+y1R+offset) - halfSize;
		int sx2 = ic.screenXD(x+x2R+offset) - halfSize;
		int sy2 = ic.screenYD(y+y2R+offset) - halfSize;
		int sx3 = sx1 + (sx2-sx1)/2-1;
		int sy3 = sy1 + (sy2-sy1)/2-1;
		if (sx>=sx1&&sx<=sx1+size&&sy>=sy1&&sy<=sy1+size) return 0;
		if (sx>=sx2&&sx<=sx2+size&&sy>=sy2&&sy<=sy2+size) return 1;
		if (sx>=sx3&&sx<=sx3+size+2&&sy>=sy3&&sy<=sy3+size+2) return 2;
		return -1;
	}
	
	/**
	 * Gets the offset.
	 *
	 * @param value the value
	 * @return the offset
	 */
	private double getOffset(double value) {
		return getDrawOffset()&&getMagnification()>1.0&&!(this instanceof Arrow)?value:0.0;
	}

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public static int getWidth() {
		return lineWidth;
	}

	/**
	 * Sets the width.
	 *
	 * @param w the new width
	 */
	public static void setWidth(int w) {
		if (w<1) w = 1;
		int max = 500;
		if (w>max) {
			ImagePlus imp2 = WindowManager.getCurrentImage();
			if (imp2!=null) {
				max = Math.max(max, imp2.getWidth());
				max = Math.max(max, imp2.getHeight());
			}
			if (w>max) w = max;
		}
		lineWidth = w;
		widthChanged = true;
	}
		
	/* (non-Javadoc)
	 * @see ij.gui.Roi#setStrokeWidth(float)
	 */
	public void setStrokeWidth(float width) {
		super.setStrokeWidth(width);
		if (getStrokeColor()==Roi.getColor())
			wideLine = true;
	}
	
	/**
	 *  Return the bounding rectangle of this line.
	 *
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		int xmin = (int)Math.round(Math.min(x1d, x2d));
		int ymin = (int)Math.round(Math.min(y1d, y2d));
		int w = (int)Math.round(Math.abs(x2d - x1d));
		int h = (int)Math.round(Math.abs(y2d - y1d));
		return new Rectangle(xmin, ymin, w, h);
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Roi#clipRectMargin()
	 */
	protected int clipRectMargin() {
		return 4;
	}

	/**
	 *  Nudge end point of line by one pixel.
	 *
	 * @param key the key
	 */
	public void nudgeCorner(int key) {
		if (ic==null) return;
		double inc = 1.0/ic.getMagnification();
		switch(key) {
			case KeyEvent.VK_UP: y2R-=inc; break;
			case KeyEvent.VK_DOWN: y2R+=inc; break;
			case KeyEvent.VK_LEFT: x2R-=inc; break;
			case KeyEvent.VK_RIGHT: x2R+=inc; break;
		}
		grow(ic.screenXD(x+x2R), ic.screenYD(y+y2R));
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Roi#getDrawOffset()
	 */
	public boolean getDrawOffset() {
		return drawOffset;
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.Roi#setDrawOffset(boolean)
	 */
	public void setDrawOffset(boolean drawOffset) {
		this.drawOffset = drawOffset;
	}

	/**
	 *  Always returns true.
	 *
	 * @return true, if successful
	 */
	public boolean subPixelResolution() {
		return true;
	}

}
