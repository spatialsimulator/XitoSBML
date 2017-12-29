package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.ext.spatial.AdvectionCoefficient;
import org.sbml.jsbml.ext.spatial.BoundaryCondition;
import org.sbml.jsbml.ext.spatial.DiffusionCoefficient;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;
import org.sbml.jsbml.ext.spatial.SpatialReactionPlugin;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class SBMLProcessUtil {
	
	/** The Constant lcoord. */
	public static final String[] lcoord = {"cartesianX","cartesianY","cartesianZ"};
	
	/** The Constant bounds. */
	public static final String[] bounds = {"Xmax","Xmin","Ymax","Ymin","Zmax","Zmin"};
	
	/** The Constant boundType. */
	public static final String[] boundType = {"Robin_valueCoefficient","Robin_inwardNormalGradientCoefficient","Robin_sum","Neumann","Dirichlet"};
	
	/** The Constant diffType. */
	public static final String[] diffType = {"isotropic","anisotropic","tensor"};

	
	/**
	 * List id to string array.
	 *
	 * @param lo the lo
	 * @return the string[]
	 */
	public static String[] listIdToStringArray(ListOf<?> lo){
		String[] str = new String[(int)lo.size()];
				
		for(int i = 0; i < lo.size(); i++)
			str[i] = ((Symbol)lo.get(i)).getId();
		
		return str;
	}

	/**
	 * Copy contents of Species from src to dst.
	 * @param src
	 * @param dst
	 * @return
	 */
	public static Species copySpeciesContents(Species src, Species dst) {
		if (src.isSetInitialAmount()) dst.setInitialAmount(src.getInitialAmount());
		if (src.isSetInitialConcentration()) dst.setInitialConcentration(src.getInitialConcentration());
		dst.setCompartment(src.getCompartment());
		dst.setUnits(src.getUnits());
		dst.setBoundaryCondition(src.getBoundaryCondition());
		dst.setConstant(src.getConstant());
		dst.setHasOnlySubstanceUnits(src.getHasOnlySubstanceUnits());
	  return dst;
	}

	/**
	 * Copy contents of Parameter from src to dst.
	 * @param src
	 * @param dst
	 * @return
	 */
	public static Parameter copyParameterContents(Parameter src, Parameter dst) {
		dst.setValue(src.getValue());
		dst.setUnits(src.getUnits());
		dst.setConstant(src.getConstant());
	  return dst;
	}

	/**
	 * Copy contents of Reaction from src to dst.
	 * KineticLaw is replaced by deep-copied src.kineticLaw.
	 * @param src
	 * @param dst
	 * @return
	 */
	@SuppressWarnings("deprecation")
  public static Reaction copyReactionContents(Reaction src, Reaction dst) {
		SpatialReactionPlugin srcSrp = (SpatialReactionPlugin) src.getPlugin("spatial");
		SpatialReactionPlugin dstSrp = (SpatialReactionPlugin) dst.getPlugin("spatial");
	  dst.setReversible(src.getReversible());
	  dst.setFast(src.getFast());
	  dstSrp.setIsLocal(srcSrp.getIsLocal());
	  dst.unsetKineticLaw();
	  if (src.isSetKineticLaw()) {
	    dst.setKineticLaw(src.getKineticLaw().clone());
	  }
	  dst.unsetListOfReactants();
	  if (src.isSetListOfReactants()) {
	    dst.setListOfReactants(src.getListOfReactants().clone());
	  }
	  dst.unsetListOfProducts();
	  if (src.isSetListOfProducts()) {
	    dst.setListOfProducts(src.getListOfProducts().clone());
	  }
	  dst.unsetListOfModifiers();
	  if (src.isSetListOfModifiers()) {
	    dst.setListOfModifiers(src.getListOfModifiers().clone());
	  }
	  return dst;
	}

	/**
	 * Copy contents of AdvenctionCoefficient from src to dst.
	 * @param src
	 * @param dst
	 * @return
	 */
	public static Parameter copyAdvectionCoefficientContents(Parameter src, Parameter dst) {
	  SpatialParameterPlugin srcSp = (SpatialParameterPlugin) src.getPlugin("spatial");
		AdvectionCoefficient srcAc = (AdvectionCoefficient) (srcSp.isSetParamType() ? srcSp.getParamType() : new AdvectionCoefficient());
	  SpatialParameterPlugin dstSp = (SpatialParameterPlugin) dst.getPlugin("spatial");
		AdvectionCoefficient dstAc = (AdvectionCoefficient) (dstSp.isSetParamType() ? dstSp.getParamType() : new AdvectionCoefficient());
	  dst.setValue(src.getValue());
	  dst.setConstant(src.getConstant());
		dstAc.setVariable(srcAc.getVariable());
		dstAc.setCoordinate(srcAc.getCoordinate());
	  return dst;
	}

	/**
	 * Copy contents of BoundaryCondition from src to dst.
	 * @param src
	 * @param dst
	 * @return
	 */
	public static Parameter copyBoundaryConditionContents(Parameter src, Parameter dst) {
	  SpatialParameterPlugin srcSp = (SpatialParameterPlugin) src.getPlugin("spatial");
		BoundaryCondition srcBc = (BoundaryCondition) (srcSp.isSetParamType() ? srcSp.getParamType() : new BoundaryCondition());
	  SpatialParameterPlugin dstSp = (SpatialParameterPlugin) dst.getPlugin("spatial");
		BoundaryCondition dstBc = (BoundaryCondition) (dstSp.isSetParamType() ? dstSp.getParamType() : new BoundaryCondition());
	  dst.setValue(src.getValue());
	  dst.setConstant(src.getConstant());
		dstBc.setVariable(srcBc.getVariable());
		dstBc.setType(srcBc.getType());
		if (srcBc.isSetCoordinateBoundary()) {
		  dstBc.setCoordinateBoundary(srcBc.getCoordinateBoundary());
		} else if (srcBc.isSetBoundaryDomainType()) {
		  dstBc.setBoundaryDomainType(srcBc.getBoundaryDomainType());
		}
	  return dst;
	}

	/**
	 * Copy contents of DiffusionCoefficient from src to dst.
	 * @param src
	 * @param dst
	 * @return
	 */
	public static Parameter copyDiffusionCoefficientContents(Parameter src, Parameter dst) {
	  SpatialParameterPlugin srcSp = (SpatialParameterPlugin) src.getPlugin("spatial");
		DiffusionCoefficient srcDiff = (DiffusionCoefficient) (srcSp.isSetParamType() ? srcSp.getParamType() : new DiffusionCoefficient());
	  SpatialParameterPlugin dstSp = (SpatialParameterPlugin) dst.getPlugin("spatial");
		DiffusionCoefficient dstDiff = (DiffusionCoefficient) (dstSp.isSetParamType() ? dstSp.getParamType() : new DiffusionCoefficient());
	  dst.setValue(src.getValue());
	  dst.setConstant(src.getConstant());
		dstDiff.setVariable(srcDiff.getVariable());
		dstDiff.setDiffusionKind(srcDiff.getDiffusionKind());
		switch (srcDiff.getDiffusionKind()) {
		case tensor:
		  dstDiff.setCoordinateReference2(srcDiff.getCoordinateReference2());
		case anisotropic:
		  dstDiff.setCoordinateReference1(srcDiff.getCoordinateReference1());
		case isotropic:
		  break;
		}
	  return dst;
	}

}
