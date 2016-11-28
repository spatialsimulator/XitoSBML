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
package ij.process;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// TODO: Auto-generated Javadoc
/** Used by the Roi classes to return float coordinate arrays and to
	determine if a point is inside or outside of spline fitted selections. */
public class FloatPolygon {
	
	/** The bounds. */
	private Rectangle bounds;
	
	/** The max Y. */
	private float minX, minY, maxX, maxY;

	/** The number of points. */
	public int npoints;

	/** The xpoints. */
	/* The array of x coordinates. */
	public float xpoints[];

	/** The ypoints. */
	/* The array of y coordinates. */
	public float ypoints[];

	/** Constructs an empty FloatPolygon. */ 
	public FloatPolygon() {
		npoints = 0;
		xpoints = new float[10];
		ypoints = new float[10];
	}

	/**
	 *  Constructs a FloatPolygon from x and y arrays.
	 *
	 * @param xpoints the xpoints
	 * @param ypoints the ypoints
	 */ 
	public FloatPolygon(float xpoints[], float ypoints[]) {
		if (xpoints.length!=ypoints.length)
			throw new IllegalArgumentException("xpoints.length!=ypoints.length");
		this.npoints = xpoints.length;
		this.xpoints = xpoints;
		this.ypoints = ypoints;
	}

	/**
	 *  Constructs a FloatPolygon from x and y arrays.
	 *
	 * @param xpoints the xpoints
	 * @param ypoints the ypoints
	 * @param npoints the npoints
	 */ 
	public FloatPolygon(float xpoints[], float ypoints[], int npoints) {
		this.npoints = npoints;
		this.xpoints = xpoints;
		this.ypoints = ypoints;
	}
		
	/* Constructs a FloatPolygon from a Polygon. 
	public FloatPolygon(Polygon polygon) {
		npoints = polygon.npoints;
		xpoints = new float[npoints];
		ypoints = new float[npoints];
		for (int i=0; i<npoints; i++) {
			xpoints[i] = polygon.xpoints[i];
			ypoints[i] = polygon.ypoints[i];
		}
	}
	*/

	/**
	 *  Returns 'true' if the point (x,y) is inside this polygon. This is a Java
	 * 	version of the remarkably small C program by W. Randolph Franklin at
	 * 	http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
	 *
	 * @param x the x
	 * @param y the y
	 * @return true, if successful
	 */
	public boolean contains(float x, float y) {
		boolean inside = false;
		for (int i=0, j=npoints-1; i<npoints; j=i++) {
			if (((ypoints[i]>y)!=(ypoints[j]>y)) &&
			(x<(xpoints[j]-xpoints[i])*(y-ypoints[i])/(ypoints[j]-ypoints[i])+xpoints[i]))
			inside = !inside;
		}
		return inside;
	}

	/**
	 * Gets the bounds.
	 *
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		if (npoints==0)
			return new Rectangle();
		if (bounds==null)
			calculateBounds(xpoints, ypoints, npoints);
		return bounds.getBounds();
	}

	/**
	 * Gets the float bounds.
	 *
	 * @return the float bounds
	 */
	public Rectangle2D.Double getFloatBounds() {
		if (npoints==0)
			return new Rectangle2D.Double();
		if (bounds==null)
			calculateBounds(xpoints, ypoints, npoints);
		return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
	}

	/**
	 * Calculate bounds.
	 *
	 * @param xpoints the xpoints
	 * @param ypoints the ypoints
	 * @param npoints the npoints
	 */
	void calculateBounds(float[] xpoints, float[] ypoints, int npoints) {
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		for (int i=0; i<npoints; i++) {
			float x = xpoints[i];
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			float y = ypoints[i];
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);
		}
		int iMinX = (int)Math.floor(minX);
		int iMinY = (int)Math.floor(minY);
		bounds = new Rectangle(iMinX, iMinY, (int)(maxX-iMinX+0.5), (int)(maxY-iMinY+0.5));
	}

	/**
	 * Adds the point.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public void addPoint(float x, float y) {
		if (npoints==xpoints.length) {
			float[] tmp = new float[npoints*2];
			System.arraycopy(xpoints, 0, tmp, 0, npoints);
			xpoints = tmp;
			tmp = new float[npoints*2];
			System.arraycopy(ypoints, 0, tmp, 0, npoints);
			ypoints = tmp;
		}
		xpoints[npoints] = x;
		ypoints[npoints] = y;
		npoints++;
		bounds = null;
	}

	/**
	 * Adds the point.
	 *
	 * @param x the x
	 * @param y the y
	 */
	public void addPoint(double x, double y) {
		addPoint((float)x, (float)y);
	}
	
	/**
	 * Duplicate.
	 *
	 * @return the float polygon
	 */
	public FloatPolygon duplicate() {
		int n = this.npoints;
		float[] xpoints = new float[n];
		float[] ypoints = new float[n];
		System.arraycopy(this.xpoints, 0, xpoints, 0, n);
		System.arraycopy(this.ypoints, 0, ypoints, 0, n);	
		return new FloatPolygon(xpoints, ypoints, n);
	}
	
	/**
	 * Gets the length.
	 *
	 * @param isLine the is line
	 * @return the length
	 */
	/* Returns the length of this polygon or line. */
	public double getLength(boolean isLine) {
		double dx, dy;
		double length = 0.0;
		for (int i=0; i<(npoints-1); i++) {
			dx = xpoints[i+1]-xpoints[i];
			dy = ypoints[i+1]-ypoints[i];
			length += Math.sqrt(dx*dx+dy*dy);
		}
		if (!isLine) {
			dx = xpoints[0]-xpoints[npoints-1];
			dy = ypoints[0]-ypoints[npoints-1];
			length += Math.sqrt(dx*dx+dy*dy);
		}
		return length;
	}
	
}
