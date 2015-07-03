package sbmlplugin.geometry;

import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.AnalyticGeometry;
import org.sbml.libsbml.AnalyticVolume;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfAnalyticVolumes;
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
	private int width = 256;
	private int height = 256;
	private int depth;
	private Point3f delta = new Point3f();
	/**
	 * @param gd
	 * @param g
	 */
	AnalyticGeometryData(GeometryDefinition gd, Geometry g, Point3f minCoord, Point3f maxCoord, Point3f dispCoord) {
		super(gd, g);
		this.minCoord = minCoord;
		this.maxCoord = maxCoord;
		this.dispCoord = dispCoord;
		ag = (AnalyticGeometry)gd;
		getSampledValues();
		
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
		int size = (int) orderedList.size();

		for(int d = 0 ; d < depth ; d++){
			for(int h = 0 ; d < height ; h++){
				for(int w = 0 ; d < width ; w++){
					for(int i = 0 ; i < size ; i++){
						AnalyticVolume av = orderedList.get(i);
						if(resolveDomain(av.getMath(), w,h,d) == 1){
							raw[d * width * height + h * width + w] = (byte) (hashSampledValue.get(av.getDomainType()) & 0xFF);
							continue;
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
				System.err.println("Errot at relational");
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
				System.err.println("Errot at operator");
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
		} else {// variable
			String var = ast.getName();
			if (var.equals("x")) {
				return x + delta.getX() + dispCoord.getX();
			} else if (var.equals("y")) {
				return y + delta.getY() + dispCoord.getY();
			} else if (var.equals("z")) {
				return z + delta.getZ() + dispCoord.getZ();
			} else {
				System.err.println("cant find name");
				return 0;
			}
		}
		System.err.println("end of method");
		return 0;
	}
	
	private ArrayList<AnalyticVolume> orderVolume(ArrayList<AnalyticVolume> orderedList, ListOfAnalyticVolumes loav){
		int numDom = (int) loav.size();
		
		for(int i = numDom ; i > 0 ; i--){
			AnalyticVolume av;
			for(int j = 0; j < numDom ; j++){
				av = loav.get(i);
				if(av.getOrdinal() == i){
					orderedList.add(av);
				}
			}
		}
		IJ.log(orderedList.toString());
		return orderedList;
	}
	
	private void getSize(){
		width = 256;
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
	SpatialImage getSpatialImage() {
		return  new SpatialImage(hashSampledValue, img);
	}

}
