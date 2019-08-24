package jp.ac.keio.bio.fun.xitosbml.geometry;

import org.sbml.jsbml.ext.spatial.GeometryDefinition;

/**
 * The class ParametricGeometryData, which inherits ImageGeometryData.
 * implements getSampledValues() and createImage() methods.
 * ParametricGeometry is not supported in the current version of XitoSBML,
 * thus this code is not used anywhere.
 *
 * Date Created: Jun 26, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ParametricGeometryData extends AbstractData {

	/**
	 * Instantiates a new sampled field geometry data with given GeometryDefinition.
	 *
	 * @param gd the GeometryDefinition
	 */
	ParametricGeometryData(GeometryDefinition gd) {
		super(gd);
		// TODO Auto-generated constructor stub
	}

}
