package sbmlplugin.pane;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.libsbmlConstants;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class SBMLProcessUtil {
	public static final String[] lcoord = {/*"UNKNOWN",*/"cartesianX","cartesianY","cartesianZ"};
	public static final String[] bounds = {"Xmax","Xmin","Ymax","Ymin","Zmax","Zmin"};
	public static final String[] boundType = {/*"UNKNOWN",*/"Robin_valueCoefficient","Robin_inwardNormalGradientCoefficient","Robin_sum","Neumann","Dirichlet"};
	public static final String[] diffType = {/*"UNKNOWN", */"isotropic","anisotropic","tensor"};

	
	public static String[] listIdToStringArray(ListOf lo){
		String[] str = new String[(int)lo.size()];
				
		for(int i = 0; i < lo.size(); i++)
			str[i] = lo.get(i).getId();
		
		return str;
	}

	public static String boundaryIndexToString(int index){
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_VALUE_COEFFICIENT) return boundType[0];
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT) return boundType[1];
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_SUM) return boundType[2];
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_NEUMANN) return boundType[3];
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_DIRICHLET) return boundType[4];
	
		return null;
	}
	
	
	public static String coordinateIndexToString(int index){
		if(index == libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_X) return SBMLProcessUtil.lcoord[0];
		if(index == libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Y) return SBMLProcessUtil.lcoord[1];
		if(index == libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Z) return SBMLProcessUtil.lcoord[2];
		return null;
	}
	
	
	public static String diffTypeIndexToString(int index){
		if(index == libsbmlConstants.SPATIAL_DIFFUSIONKIND_ISOTROPIC) return diffType[0];
		if(index == libsbmlConstants.SPATIAL_DIFFUSIONKIND_ANISOTROPIC) return diffType[1];
		if(index == libsbmlConstants.SPATIAL_DIFFUSIONKIND_TENSOR) return diffType[2];
		
		return null;
	}
	
	public static String unitIndexToString(int index){
		if(index == libsbmlConstants.UNIT_KIND_AMPERE) return "ampere";
		if(index == libsbmlConstants.UNIT_KIND_AVOGADRO) return "avogadro";
		if(index == libsbmlConstants.UNIT_KIND_BECQUEREL) return "becquerel";
		if(index == libsbmlConstants.UNIT_KIND_CANDELA) return "candela";
		if(index == libsbmlConstants.UNIT_KIND_CELSIUS) return "celsius";
		if(index == libsbmlConstants.UNIT_KIND_COULOMB) return "coulomb";
		if(index == libsbmlConstants.UNIT_KIND_DIMENSIONLESS) return "dimensionless";
		if(index == libsbmlConstants.UNIT_KIND_FARAD) return "farad";
		if(index == libsbmlConstants.UNIT_KIND_GRAM) return "gram";
		if(index == libsbmlConstants.UNIT_KIND_GRAY) return "gray";
		if(index == libsbmlConstants.UNIT_KIND_HENRY) return "henry";
		if(index == libsbmlConstants.UNIT_KIND_HERTZ) return "hertz";
		if(index == libsbmlConstants.UNIT_KIND_ITEM) return "item";
		if(index == libsbmlConstants.UNIT_KIND_JOULE) return "joule";
		if(index == libsbmlConstants.UNIT_KIND_KATAL) return "katal";
		if(index == libsbmlConstants.UNIT_KIND_KELVIN) return "kelvin";
		if(index == libsbmlConstants.UNIT_KIND_KILOGRAM) return "kilogram";
		if(index == libsbmlConstants.UNIT_KIND_LITER) return "liter";
		if(index == libsbmlConstants.UNIT_KIND_LITRE) return "litre";
		if(index == libsbmlConstants.UNIT_KIND_LUMEN) return "lumen";
		if(index == libsbmlConstants.UNIT_KIND_LUX) return "lux";
		if(index == libsbmlConstants.UNIT_KIND_METER) return "meter";
		if(index == libsbmlConstants.UNIT_KIND_METRE) return "metre";
		if(index == libsbmlConstants.UNIT_KIND_MOLE) return "mole";
		if(index == libsbmlConstants.UNIT_KIND_NEWTON) return "newton";
		if(index == libsbmlConstants.UNIT_KIND_OHM) return "ohm";
		if(index == libsbmlConstants.UNIT_KIND_PASCAL) return "pascal";
		if(index == libsbmlConstants.UNIT_KIND_RADIAN) return "radian";
		if(index == libsbmlConstants.UNIT_KIND_SECOND) return "second";
		if(index == libsbmlConstants.UNIT_KIND_SIEMENS) return "siemens";
		if(index == libsbmlConstants.UNIT_KIND_SIEVERT) return "sievert";
		if(index == libsbmlConstants.UNIT_KIND_STERADIAN) return "steradian";
		if(index == libsbmlConstants.UNIT_KIND_TESLA) return "tesla";
		if(index == libsbmlConstants.UNIT_KIND_VOLT) return "volt";
		if(index == libsbmlConstants.UNIT_KIND_WATT) return "watt";
		if(index == libsbmlConstants.UNIT_KIND_WEBER) return "weber";
		
		return null;
	}
	
	
	enum Unit {
		
	}
}
