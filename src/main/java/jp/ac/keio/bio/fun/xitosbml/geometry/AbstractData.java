package jp.ac.keio.bio.fun.xitosbml.geometry;

import org.sbml.jsbml.ext.spatial.GeometryDefinition;

/**
 * The class AbstractData, which is an abstract class and be used to
 * create data objects for geometry objects (AnalyticGeometryData, SampledFieldGeometryData and ParametricGeometryData).
 *
 * This class contains following objects which are related to geometry.
 * <ul>
 *     <li>GeometryDefinition {@link org.sbml.jsbml.ext.spatial.GeometryDefinition}</li>
 *     <li>Title (SpatialId as String)</li>
 * </ul>
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData}.
 *
 * Date Created: Jun 25, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public abstract class AbstractData {
	
	/** The GeometryDefinition object. */
	GeometryDefinition gd;
	
	/** The title (SpatialId). */
	String title;
	
	/**
	 * Instantiates a new abstract data.
	 *
	 * @param gd the GeometryDefinition object
	 */
	AbstractData(GeometryDefinition gd){
		this.gd = gd;
		title = gd.getSpatialId();
	}	
}
