package jp.ac.keio.bio.fun.xitosbml.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.AnalyticVolume;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;

import ij.ImageStack;
import ij.process.ByteProcessor;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import math3d.Point3d;

/**
 * The class AnalyticGeometryData, which inherits ImageGeometryData and
 * implements getSampledValues() and createImage() methods. This class
 * contains following objects which are related to analytic geometry.
 * <ul>
 *     <li>Geometry {@link org.sbml.jsbml.ext.spatial.AnalyticGeometry}</li>
 *     <li>coordinates of boundary</li>
 *     <li>coordinate of displacement</li>
 *     <li>image size</li>
 *     <li>delta</li>
 * </ul>
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.geometry.GeometryDatas},
 * to visualize a model in 3D space.
 *
 * Date Created: Jun 26, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class AnalyticGeometryData extends ImageGeometryData {
	
	/** The analytic geometry object. */
	private AnalyticGeometry ag;
	
	/** The minimum value of the coordinate axis (boundary). */
	protected Point3d minCoord = new Point3d();
	
	/** The maximum value of the coordinate axis (boundary). */
	protected Point3d maxCoord = new Point3d();
	
	/** The coordinate of displacement. */
	protected Point3d dispCoord = new Point3d();
	
	/** The width of an image. */
	private int width = 32; //TODO find better way to determine image size
	
	/** The height of an image. */
	private int height;
	
	/** The depth of an image. */
	private int depth;
	
	/** The delta (x, y, z) of the 3D space. These values will be calculated
	 * by the size of 3D space and the width of an image. */
	private Point3d delta = new Point3d();
	
	/**
	 * Instantiates a new analytic geometry data with given GeometryDefinition
	 * and Geometry.
	 *
	 * @param gd the GeometryDefinition
	 * @param g the Geometry
	 * @param minCoord the minimum value of the coordinate axis (boundary)
	 * @param maxCoord the maximum value of the coordinate axis (boundary)
	 * @param dispCoord the coordinate of displacement
	 */
	AnalyticGeometryData(GeometryDefinition gd, Geometry g, Point3d minCoord, Point3d maxCoord, Point3d dispCoord) {
		super(gd, g);
		this.minCoord = minCoord;
		this.maxCoord = maxCoord;
		this.dispCoord = dispCoord;
		ag = (AnalyticGeometry) gd;
		getSampledValues();
		createImage();
	}

	/**
	 * Get sampled value from Geometry (SampledFieldGeometry or AnalyticGeometry) and
	 * sets its value to the hashSampledValue (hashmap of sampled value).
	 * AnalyticGeometry does not contain sampled value, so it will be calculated
	 * by the ordinal value of each domain.
	 * @see jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData#getSampledValues()
	 */
	@Override
	void getSampledValues() {
		ListOf<AnalyticVolume> loav = ag.getListOfAnalyticVolumes();
		int numDom = (int) loav.size();
		int intervalVal = (int) Math.floor(255 / (numDom - 1));		//divide 255 by num of domains excluding EC
		
		for(int i = 0 ; i < numDom ; i++){
			AnalyticVolume av = loav.get(i);
			hashSampledValue.put(av.getDomainType(), av.getOrdinal() * intervalVal);
		}
	}

	/**
     * Create a stacked image from spatial image (3D).
	 * The value of each pixel corresponds to the domain.
	 * @see jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData#createImage()
	 */
	@Override
	void createImage() {
		getSize();
		getArray();
		ListOf<AnalyticVolume> loav = ag.getListOfAnalyticVolumes();
		ArrayList<AnalyticVolume> orderedList = new ArrayList<AnalyticVolume>();
		orderedList = orderVolume(orderedList, loav);
		setVolumeToArray(orderedList);	
		ImageStack is = createStack();
		img.setStack(is);
		img.setTitle(title);
	}
	
	/**
	 * Creates the stacked image from the raw data (1D array) of spatial image.
	 *
	 * @return the image stack
	 */
	private ImageStack createStack(){
		ImageStack stack = new ImageStack(width, height);
		byte[] slice;   
    	int length = width * height;
    	for(int i = 1 ; i <= depth ; i++){
        	slice = new byte[length];
        	System.arraycopy(raw, (i-1) * height * width, slice, 0, length);
        	stack.addSlice(new ByteProcessor(width,height,slice,null));
    	}
    	return stack;
    }
	
	/**
	 * Sets the AnalyticVolume to 1D byte array (raw).
	 * The sub volume (domain) will written to the array in the specific order
	 * specified in orderedList.
	 * As each domain shape is represented as an equation (AST), the AST is
	 * evaluated in this method by resolveDomain() to calculate the geometry of
	 * each pixel.
	 *
	 * @param orderedList the ordered list of AnalyticVolume
	 */
	private void setVolumeToArray(ArrayList<AnalyticVolume> orderedList){

		for(int d = 0 ; d < depth ; d++){
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					for(AnalyticVolume av : orderedList){
						if(resolveDomain(av.getMath(), w, h, d) == 1){				
							raw[d * width * height + h * width + w] = (byte) (hashSampledValue.get(av.getDomainType()) & 0xFF);
							break;
						}				
					}	
				}	
			}	
		}
	}
	
	/**
	 * Evaluate AST (Abstract Syntax Tree), which represents the shape of domain
	 * as an equation. As each domain shape is represented as an equation (AST),
	 * the AST has to be evaluated to calculate the geometry of each pixel.
	 *
	 * @param ast the AST (Abstract Syntax Tree) object, which represents the shape of domain
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the evaluation result as a double value. If the equation is equality or inequality,
	 * this method will return 1 on true, otherwise 0.
	 */
	private double resolveDomain(ASTNode ast, int x, int y, int z) {
		
		if (ast.isRelational()) { // relational
			Type type = ast.getType();
			switch (type) {
			case RELATIONAL_GEQ:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) >= 0) {
					return 1;
				} else {
					return 0;
				}
			case RELATIONAL_GT:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) > 0) {
					return 1;
				} else {
					return 0;
				}
			case RELATIONAL_LEQ:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) <= 0) {
					return 1;
				} else {
					return 0;
				}
			case RELATIONAL_LT:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) < 0) {
					return 1;
				} else {
					return 0;
				}
			default:
				System.err.println("Error at relational");
				return 0;
			}
		} else if (ast.isOperator()) {// ast is operator
			Type type = ast.getType();
			switch (type) {
			case PLUS:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						+ resolveDomain(ast.getRightChild(), x, y, z);

			case MINUS:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z);

			case TIMES:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						* resolveDomain(ast.getRightChild(), x, y, z);

			case DIVIDE:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						/ resolveDomain(ast.getRightChild(), x, y, z);

			case POWER:
				return Math.pow(resolveDomain(ast.getLeftChild(), x, y, z),
						resolveDomain(ast.getRightChild(), x, y, z));
			default:
				System.err.println("Error at operator");
				return 0;
			}
		} else if (ast.isReal()) {// ast is real number
			return ast.getReal();
		} else if (ast.isInteger()) {// ast is integer
			return ast.getInteger();
		} else if (ast.isConstant()) {// ast is constant
			Type type = ast.getType();
			switch (type) {
			case CONSTANT_E:
				return Math.E;
			case CONSTANT_PI:
				return Math.PI;
			default:
				break;
			}
		} else if(ast.isFunction()) {
			if(ast.getType() == Type.FUNCTION_POWER){
				return Math.pow(resolveDomain(ast.getLeftChild(), x, y, z), resolveDomain(ast.getRightChild(), x, y, z));
			}
		} else {// variable		
			String var = ast.getName();
			if (var.equals("x")) {
				return x * delta.x - dispCoord.x;
			} else if (var.equals("y")) {
				return y * delta.y - dispCoord.y;
			} else if (var.equals("z")) {
				return z * delta.z - dispCoord.z;
			} else {
				System.err.println("can't find name");
				return 0;
			}
		}
		
		return 0;
	}
	
	/**
	 * Create ordered list of AnalyticVolume (orderedList) by given list of AnalyticVolume (loav).
	 * The order of AnalyticVolume is be determined by the value of ordinal assigned to each domain.
	 * Also, the AST (Abstract Syntax Tree) which represents the analytic geometry will be rearranged
	 * so that the spatial simulator can easily evaluate AST to create a simulation space.
     *
	 * @param orderedList the ordered list of AnalyticVolume
	 * @param loav list of AnalyticVolume (pre-ordered)
	 * @return the array list of ordered list of AnalyticVolume (orderedList)
	 */
	private ArrayList<AnalyticVolume> orderVolume(ArrayList<AnalyticVolume> orderedList, ListOf<AnalyticVolume> loav){
		int numDom = (int) loav.size();
		
		for(int i = numDom - 1; i > 0 ; i--){
			AnalyticVolume av;
			for(int j = 0; j < numDom ; j++){
				av = loav.get(j);
				if(av.getOrdinal() == i){
					rearrangeAST(av.getMath());
					orderedList.add(av);
				}
			}
		}
		return orderedList;
	}
	
	/**
	 * Rearrange AST (Abstract Syntax Tree).
	 * This method will recursively rearrange AST which represents the analytic geometry
	 * so that the evaluation of AST can easily be handled while creating a simulation space.
	 * For example, the piecewise function will be replaces with boolean logic
	 * and expression, "-a" will be transformed to "-1 * a", etc.
	 *
	 * @param ast the AST to be rearranged
	 */
	private void rearrangeAST(ASTNode ast){
		Type type = ast.getType();
		if(type == Type.FUNCTION_PIECEWISE){
			Vector<ASTNode> astChildrenList = new Vector<ASTNode>();
			Vector<ASTNode> astBooleanList = new Vector<ASTNode>();
			long nc = ast.getNumChildren();
			ASTNode astOtherwise = new ASTNode();
			// piece to boolean * expression
			for (int i = 0 ; i < nc /2 ; i++){
				ASTNode ast_times = new ASTNode(Type.TIMES);
				astBooleanList.add((ASTNode) ast.getChild(1).clone());
				ast_times.addChild(ast.getChild(0));
				ast_times.addChild(ast.getChild(1));
				astChildrenList.add(ast_times);
				ast.removeChild(0);
				ast.removeChild(0);
			}
			// otherwise to nand
			if(nc % 2 != 0){
				astOtherwise.setType(Type.TIMES);
				ASTNode otherwiseExpression = ast.getChild(0);
				ast.removeChild(0);
				ASTNode ast_and = new ASTNode(Type.LOGICAL_AND);
				Iterator<ASTNode> it = astBooleanList.iterator();
				while(it.hasNext()){
					ASTNode ast_not = new ASTNode(Type.LOGICAL_NOT);
					ast_not.addChild(it.next());
					ast_and.addChild(ast_not);
				}
				astOtherwise.addChild(ast_and);
				astOtherwise.addChild(otherwiseExpression);
			} else {
				astOtherwise.setType(Type.INTEGER);
				astOtherwise.setValue(0);
			}
			ast.setType(Type.PLUS);
			ASTNode ast_next = ast;
			ast_next.addChild(astOtherwise);
			for(int i = 0 ; i < astChildrenList.size() - 1 ; i++){
				ast_next.addChild(new ASTNode(Type.PLUS));
				ast_next = ast_next.getChild(1);
				ast_next.addChild(astChildrenList.get(i));
			}
			ast_next.addChild(astChildrenList.lastElement());
		} else if(type == Type.MINUS && ast.getNumChildren() == 1){ 
			// -a to -1.0 * a
			ast.setType(Type.TIMES);
			ASTNode ast_minus_one = new ASTNode(Type.REAL);
			ast_minus_one.setValue(-1.0);
			ast.addChild(ast_minus_one);
		} else if (type == Type.PLUS && ast.getNumChildren() == 1){
			// +a to 1.0 * a
			ast.setType(Type.TIMES);
			ASTNode ast_plus_one = new ASTNode(Type.REAL);
			ast_plus_one.setValue(1.0);
			ast.addChild(ast_plus_one);
		} else if (ast.isLogical()) {
			if(type != Type.LOGICAL_NOT){
				if(ast.getNumChildren() == 1){
					ASTNode ast_one = new ASTNode(Type.INTEGER);
					ast_one.setValue(1);
					ast.addChild(ast_one);
				} else {
					//ast.reduceToBinary();
				}
			} else { 
				//logical not
			}
		} else if (type == Type.TIMES){
			if( (ast.getLeftChild().isReal() && ast.getLeftChild().getReal() == 0) ||
				(ast.getLeftChild().isInteger() && ast.getLeftChild().getReal() == 0) ||
				(ast.getRightChild().isReal() && ast.getRightChild().getReal() == 0) ||
				(ast.getRightChild().isInteger() && ast.getRightChild().getReal() == 0) 
				) {
				ast.setType(Type.REAL);
				ast.setValue(0);
				ast.removeChild(0);
				ast.removeChild(0);
			}
		}

		for(int i = 0 ; i < ast.getNumChildren(); i++){
			rearrangeAST(ast.getChild(i));
		}
	}
	
	/**
	 * Gets the size of geometry, and sets the height, depth and
	 * the delta (x, y, z) of the 3D space. These values will be calculated
	 * by the size of 3D space and the width of an image.
	 */
	private void getSize(){
		height = (int) (width * maxCoord.y / maxCoord.x);
		depth = (int) (width * maxCoord.z / maxCoord.x);
		if(depth == 0) depth = 1;
	
		delta.x = (maxCoord.x / width);
		delta.y = (maxCoord.y / height);
		delta.z = (maxCoord.z / depth);	
	}
	
	/**
     * Create a byte array (raw), which will be used to store the value of an image.
	 */
	private void getArray(){
		int length = height * width * depth;
		raw = new byte[length];
	}
	
	/**
	 * Create and return a new spatial image.
	 * SpatialImage object is generated with the ImagePlus object (img) and the hashmap of sampled value
	 * (pixel value of a SampledVolume).
	 * @see jp.ac.keio.bio.fun.xitosbml.geometry.ImageGeometryData#getSpatialImage()
	 *
	 * @return spatial image object, which is an object for handling spatial image in XitoSBML.
	 */
	@Override
	public SpatialImage getSpatialImage() {
		return  new SpatialImage(hashSampledValue, img);
	}

}
