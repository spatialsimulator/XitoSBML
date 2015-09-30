package sbmlplugin.geometry;

import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Point3f;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AnalyticGeometry;
import org.sbml.libsbml.AnalyticVolume;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfAnalyticVolumes;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;

import sbmlplugin.image.SpatialImage;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 26, 2015
 */
public class AnalyticGeometryData extends ImageGeometryData {
	private AnalyticGeometry ag;
	protected Point3f minCoord = new Point3f();
	protected Point3f maxCoord = new Point3f();
	protected Point3f dispCoord = new Point3f();
	private int width = 32; //TODO find better way to determine image size
	private int height;
	private int depth;
	private Point3f delta = new Point3f();
	
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
		ListOfAnalyticVolumes loav = ag.getListOfAnalyticVolumes();
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
		ListOfAnalyticVolumes loav = ag.getListOfAnalyticVolumes();
		ArrayList<AnalyticVolume> orderedList = new ArrayList<AnalyticVolume>();
		orderedList = orderVolume(orderedList, loav);
		setVolumeToArray(orderedList);	
		ImageStack is = createStack();
		img.setStack(is);
		img.setTitle(title);
	}
	
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
	
	private double resolveDomain(ASTNode ast, int x, int y, int z) {
		
		if (ast.isRelational()) { // relational
			int type = ast.getType();
			switch (type) {
			case libsbmlConstants.AST_RELATIONAL_GEQ:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) >= 0) {
					return 1;
				} else {
					return 0;
				}
			case libsbmlConstants.AST_RELATIONAL_GT:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) > 0) {
					return 1;
				} else {
					return 0;
				}
			case libsbmlConstants.AST_RELATIONAL_LEQ:
				if (resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z) <= 0) {
					return 1;
				} else {
					return 0;
				}
			case libsbmlConstants.AST_RELATIONAL_LT:
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
			int type = ast.getType();
			switch (type) {
			case libsbmlConstants.AST_PLUS:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						+ resolveDomain(ast.getRightChild(), x, y, z);

			case libsbmlConstants.AST_MINUS:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						- resolveDomain(ast.getRightChild(), x, y, z);

			case libsbmlConstants.AST_TIMES:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						* resolveDomain(ast.getRightChild(), x, y, z);

			case libsbmlConstants.AST_DIVIDE:
				return resolveDomain(ast.getLeftChild(), x, y, z)
						/ resolveDomain(ast.getRightChild(), x, y, z);

			case libsbmlConstants.AST_POWER:
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
			int type = ast.getType();
			switch (type) {
			case libsbmlConstants.AST_CONSTANT_E:
				return Math.E;
			case libsbmlConstants.AST_CONSTANT_PI:
				return Math.PI;
			}
		} else if(ast.isFunction()) {
			if(ast.getType() == libsbml.AST_FUNCTION_POWER){
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
	
	private ArrayList<AnalyticVolume> orderVolume(ArrayList<AnalyticVolume> orderedList, ListOfAnalyticVolumes loav){
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
	
	private void rearrangeAST(ASTNode ast){
		int type = ast.getType();
		if(type == libsbml.AST_FUNCTION_PIECEWISE){
			Vector<ASTNode> astChildrenList = new Vector<ASTNode>();
			Vector<ASTNode> astBooleanList = new Vector<ASTNode>();
			long nc = ast.getNumChildren();
			ASTNode astOtherwise = new ASTNode();
			// piece to boolean * expression
			for (int i = 0 ; i < nc /2 ; i++){
				ASTNode ast_times = new ASTNode(libsbml.AST_TIMES);
				astBooleanList.add((ASTNode) ast.getChild(1).deepCopy());
				ast_times.addChild(ast.getChild(0));
				ast_times.addChild(ast.getChild(1));
				astChildrenList.add(ast_times);
				ast.removeChild(0);
				ast.removeChild(0);
			}
			// otherwise to nand
			if(nc % 2 != 0){
				astOtherwise.setType(libsbml.AST_TIMES);
				ASTNode otherwiseExpression = ast.getChild(0);
				ast.removeChild(0);
				ASTNode ast_and = new ASTNode(libsbml.AST_LOGICAL_AND);
				Iterator<ASTNode> it = astBooleanList.iterator();
				while(it.hasNext()){
					ASTNode ast_not = new ASTNode(libsbml.AST_LOGICAL_NOT);
					ast_not.addChild(it.next());
					ast_and.addChild(ast_not);
				}
				astOtherwise.addChild(ast_and);
				astOtherwise.addChild(otherwiseExpression);
			} else {
				astOtherwise.setType(libsbml.AST_INTEGER);
				astOtherwise.setValue(0);
			}
			ast.setType(libsbml.AST_PLUS);
			ASTNode ast_next = ast;
			ast_next.addChild(astOtherwise);
			for(int i = 0 ; i < astChildrenList.size() - 1 ; i++){
				ast_next.addChild(new ASTNode(libsbml.AST_PLUS));
				ast_next = ast_next.getChild(1);
				ast_next.addChild(astChildrenList.get(i));
			}
			ast_next.addChild(astChildrenList.lastElement());
		} else if(type == libsbml.AST_MINUS && ast.getNumChildren() == 1){ 
			// -a to -1.0 * a
			ast.setType(libsbml.AST_TIMES);
			ASTNode ast_minus_one = new ASTNode(libsbml.AST_REAL);
			ast_minus_one.setValue(-1.0);
			ast.addChild(ast_minus_one);
		} else if (type == libsbml.AST_PLUS && ast.getNumChildren() == 1){
			// +a to 1.0 * a
			ast.setType(libsbml.AST_TIMES);
			ASTNode ast_plus_one = new ASTNode(libsbml.AST_REAL);
			ast_plus_one.setValue(1.0);
			ast.addChild(ast_plus_one);
		} else if (ast.isLogical()) {
			if(type != libsbml.AST_LOGICAL_NOT){
				if(ast.getNumChildren() == 1){
					ASTNode ast_one = new ASTNode(libsbml.AST_INTEGER);
					ast_one.setValue(1);
					ast.addChild(ast_one);
				} else {
					ast.reduceToBinary();
				}
			} else { 
				//logical not
			}
		} else if (type == libsbml.AST_TIMES){
			if( (ast.getLeftChild().isReal() && ast.getLeftChild().getReal() == 0) ||
				(ast.getLeftChild().isInteger() && ast.getLeftChild().getReal() == 0) ||
				(ast.getRightChild().isReal() && ast.getRightChild().getReal() == 0) ||
				(ast.getRightChild().isInteger() && ast.getRightChild().getReal() == 0) 
				) {
				ast.setType(libsbml.AST_REAL);
				ast.setValue(0);
				ast.removeChild(0);
				ast.removeChild(0);
			}
		}

		for(int i = 0 ; i < ast.getNumChildren(); i++){
			rearrangeAST(ast.getChild(i));
		}
	}
	
	private void getSize(){
		//width = 256;
		height = (int) (width * maxCoord.getY() / maxCoord.getX());
		depth = (int) (width * maxCoord.getZ() / maxCoord.getX());
		if(depth == 0) depth = 1;
	
		delta.setX(maxCoord.getX() / width);
		delta.setY(maxCoord.getY() / height);
		delta.setZ(maxCoord.getZ() / depth);	
	}
	
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
