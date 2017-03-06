package jp.ac.keio.bio.fun.xitosbml.util;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 20, 2015
 */
public class PluginConstants {
	
	/** The Constant VIEWERVERSION. */
	public static final double VIEWERVERSION = 1.5;
	
	/** The Constant SBMLLEVEL. */
	public static final int SBMLLEVEL = 3;
	
	/** The Constant SBMLVERSION. */
	public static final int SBMLVERSION = 1;
	
	/** The Constant LOWERSBMLLEVEL. */
	public static final int LOWERSBMLLEVEL = 2;
	
	/** The Constant LOWERSBMLVERSION. */
	public static final int LOWERSBMLVERSION = 4;
	
	/** The Constant CDNAMESPACE. */
	public static final String CDNAMESPACE = "http://www.sbml.org/2001/ns/celldesigner";

	/** The Constant CDURI. */
	public static final String TAG_CELLDESIGNER_PREFIX = "celldesigner";

	/** The Constant TAG_START_ANNOTATION. */
	public static final String TAG_START_ANNOTATION = "<annotation xmlns:" + TAG_CELLDESIGNER_PREFIX + "=\"" + "http://www.sbml.org/2001/ns/celldesigner" + "\">";

	/** The Constant TAG_START_CELLDESIGNER. */
	public static final String TAG_START_CELLDESIGNER = "<" + TAG_CELLDESIGNER_PREFIX + ":";

	/** The Constant OUTSIDE. */
	public static final String OUTSIDE = "outside";

	/** The Constant TAG_END_ANNOTATION. */
	public final static String TAG_END_ANNOTATION = "</annotation>";

	/** The Constant TAG_END_CELLDESIGNER. */
	public final static String TAG_END_CELLDESIGNER = "</" + TAG_CELLDESIGNER_PREFIX + ":";

	public final static String TAG_CELLDESIGNER_EXTENSIONTOPLEVEL  = "extension";

	public static String addCellDesignerAnnotationTag(String str){
		
		String rtn =	TAG_START_ANNOTATION			+
		    	TAG_START_CELLDESIGNER					+
		    	TAG_CELLDESIGNER_EXTENSIONTOPLEVEL	 	+
		    	">"										+
		    	str										+
		    	TAG_END_CELLDESIGNER					+
		    	TAG_CELLDESIGNER_EXTENSIONTOPLEVEL	 	+
		    	">"										+
		    	TAG_END_ANNOTATION;
		
		return rtn;
	}

	
}
