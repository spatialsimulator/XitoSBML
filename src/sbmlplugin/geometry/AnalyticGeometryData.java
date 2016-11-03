package sbmlplugin.geometry;

import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Point3f;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.AnalyticVolume;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 26, 2015
 */
public class AnalyticGeometryData extends ImageGeometryData {
	
	/** The ag. */
	private AnalyticGeometry ag;
	
	/** The min coord. */
	protected Point3f minCoord = new Point3f();
	
	/** The max coord. */
	protected Point3f maxCoord = new Point3f();
	
	/** The disp coord. */
	protected Point3f dispCoord = new Point3f();
	
	/** The width. */
	private int width = 32; //TODO find better way to determine image size
	
	/** The height. */
	private int height;
	
	/** The depth. */
	private int depth;
	
	/** The delta. */
	private Point3f delta = new Point3f();
	
	/**
	 * Instantiates a new analytic geometry data.
	 *
	 * @param gd the gd
	 * @param g the g
	 * @param minCoord the min coord
	 * @param maxCoord the max coord
	 * @param dispCoord the disp coord
	 */
	AnalyticGeometryData(GeometryDefinition gd, Geometry g, Point3f minCoord, Point3f maxCoord, Point3f dispCoord) {
		super(gd, g);
		this.minCoord = minCoord;
		this.maxCoord = maxCoord;
		this.dispCoord = dispCoord;
		ag = (AnalyticGeometry) gd;
		getSampledValues();
		createImage();
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSampledValue()
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

	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#createImage()
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
	 * Creates the stack.
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
	 * Sets the volume to array.
	 *
	 * @param orderedList the new volume to array
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
	 * Resolve domain.
	 *
	 * @param ast the ast
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the double
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
				return x * delta.getX() - dispCoord.getX();
			} else if (var.equals("y")) {
				return y * delta.getY() - dispCoord.getY();
			} else if (var.equals("z")) {
				return z * delta.getZ() - dispCoord.getZ();
			} else {
				System.err.println("can't find name");
				return 0;
			}
		}
		
		return 0;
	}

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
	 * Rearrange AST.
	 *
	 * @param ast the ast
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
					//TODO find a way in JSBML
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
	 * Gets the size.
	 *
	 * @return the size
	 */
	private void getSize(){
		height = (int) (width * maxCoord.getY() / maxCoord.getX());
		depth = (int) (width * maxCoord.getZ() / maxCoord.getX());
		if(depth == 0) depth = 1;
	
		delta.setX(maxCoord.getX() / width);
		delta.setY(maxCoord.getY() / height);
		delta.setZ(maxCoord.getZ() / depth);	
	}
	
	/**
	 * Gets the array.
	 *
	 * @return the array
	 */
	private void getArray(){
		int length = height * width * depth;
		raw = new byte[length];
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.visual.ImageGeometryData#getSpatialImage()
	 */
	@Override
	public SpatialImage getSpatialImage() {
		return  new SpatialImage(hashSampledValue, img);
	}

}
