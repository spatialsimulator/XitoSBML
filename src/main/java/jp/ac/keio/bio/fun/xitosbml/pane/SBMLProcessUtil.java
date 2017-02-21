package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.ext.spatial.BoundaryConditionKind;
import org.sbml.jsbml.ext.spatial.CoordinateKind;
import org.sbml.jsbml.ext.spatial.DiffusionKind;

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
	public static final String[] lcoord = {/*"UNKNOWN",*/"cartesianX","cartesianY","cartesianZ"};
	
	/** The Constant bounds. */
	public static final String[] bounds = {"Xmax","Xmin","Ymax","Ymin","Zmax","Zmin"};
	
	/** The Constant boundType. */
	public static final String[] boundType = {/*"UNKNOWN",*/"Robin_valueCoefficient","Robin_inwardNormalGradientCoefficient","Robin_sum","Neumann","Dirichlet"};
	
	/** The Constant diffType. */
	public static final String[] diffType = {/*"UNKNOWN", */"isotropic","anisotropic","tensor"};

	
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
	 * Boundary index to string.
	 *
	 * @param index the index
	 * @return the string
	 */
	public static String boundaryIndexToString(BoundaryConditionKind index){
		if(index == BoundaryConditionKind.Robin_valueCoefficient)
			return boundType[0]; 
		else if(index == BoundaryConditionKind.Robin_inwardNormalGradientCoefficient)
			return boundType[1];
		else if(index == BoundaryConditionKind.Robin_sum)
			return boundType[2];
		else if(index == BoundaryConditionKind.Neumann)
			return boundType[3];
		else if(index == BoundaryConditionKind.Dirichlet)
			return boundType[4];
		else 
			return null;
	}
	
	/**
	 * String to boundary kind.
	 *
	 * @param kind the kind
	 * @return the boundary condition kind
	 */
	public static BoundaryConditionKind StringToBoundaryKind(String kind){
		if(SBMLProcessUtil.boundType[0].equals(kind))
			return BoundaryConditionKind.Robin_valueCoefficient;
		else if(SBMLProcessUtil.boundType[1].equals(kind))
			return BoundaryConditionKind.Robin_inwardNormalGradientCoefficient;
		else if(SBMLProcessUtil.boundType[2].equals(kind))
			return BoundaryConditionKind.Robin_sum;
		else if(SBMLProcessUtil.boundType[3].equals(kind))
			return BoundaryConditionKind.Neumann;
		else
			return BoundaryConditionKind.Dirichlet;		
	}
	
	/**
	 * Coordinate index to string.
	 *
	 * @param index the index
	 * @return the string
	 */
	public static String coordinateIndexToString(CoordinateKind index){
		if(index == CoordinateKind.cartesianX) return SBMLProcessUtil.lcoord[0];
		if(index == CoordinateKind.cartesianY) return SBMLProcessUtil.lcoord[1];
		if(index == CoordinateKind.cartesianZ) return SBMLProcessUtil.lcoord[2];
		return null;
	}
	
	/**
	 * String to coordinate kind.
	 *
	 * @param kind the kind
	 * @return the coordinate kind
	 */
	public static CoordinateKind StringToCoordinateKind(String kind){
		if(SBMLProcessUtil.lcoord[0].equals(kind))
			return CoordinateKind.cartesianX;
		else if(SBMLProcessUtil.lcoord[1].equals(kind))
			return CoordinateKind.cartesianY;
		else
			return CoordinateKind.cartesianZ; 
	}
	
	/**
	 * Diff type index to string.
	 *
	 * @param index the index
	 * @return the string
	 */
	public static String diffTypeIndexToString(DiffusionKind index){
		if(index == DiffusionKind.isotropic) return diffType[0];
		if(index == DiffusionKind.anisotropic) return diffType[1];
		if(index == DiffusionKind.tensor) return diffType[2];
		
		return null;
	}
	
	/**
	 * String to diffusion kind.
	 *
	 * @param kind the kind
	 * @return the diffusion kind
	 */
	public static DiffusionKind StringToDiffusionKind(String kind){
		if(SBMLProcessUtil.diffType[0].equals(kind))
			return DiffusionKind.isotropic;
		else if(SBMLProcessUtil.diffType[1].equals(kind))
			return DiffusionKind.anisotropic;
		else
			return DiffusionKind.tensor;
	}
	
	/**
>>>>>>> garuda
	 * Unit index to string.
	 *
	 * @param index the index
	 * @return the string
	 */
	@SuppressWarnings("deprecation")
	public static String unitIndexToString(Kind index){
		if(index == Kind.AMPERE) return "ampere";
		if(index == Kind.AVOGADRO) return "avogadro";
		if(index == Kind.BECQUEREL) return "becquerel";
		if(index == Kind.CANDELA) return "candela";
		if(index == Kind.CELSIUS) return "celsius";
		if(index == Kind.COULOMB) return "coulomb";
		if(index == Kind.DIMENSIONLESS) return "dimensionless";
		if(index == Kind.FARAD) return "farad";
		if(index == Kind.GRAM) return "gram";
		if(index == Kind.GRAY) return "gray";
		if(index == Kind.HENRY) return "henry";
		if(index == Kind.HERTZ) return "hertz";
		if(index == Kind.ITEM) return "item";
		if(index == Kind.JOULE) return "joule";
		if(index == Kind.KATAL) return "katal";
		if(index == Kind.KELVIN) return "kelvin";
		if(index == Kind.KILOGRAM) return "kilogram";
		if(index == Kind.LITER) return "liter";
		if(index == Kind.LITRE) return "litre";
		if(index == Kind.LUMEN) return "lumen";
		if(index == Kind.LUX) return "lux";
		if(index == Kind.METER) return "meter";
		if(index == Kind.METRE) return "metre";
		if(index == Kind.MOLE) return "mole";
		if(index == Kind.NEWTON) return "newton";
		if(index == Kind.OHM) return "ohm";
		if(index == Kind.PASCAL) return "pascal";
		if(index == Kind.RADIAN) return "radian";
		if(index == Kind.SECOND) return "second";
		if(index == Kind.SIEMENS) return "siemens";
		if(index == Kind.SIEVERT) return "sievert";
		if(index == Kind.STERADIAN) return "steradian";
		if(index == Kind.TESLA) return "tesla";
		if(index == Kind.VOLT) return "volt";
		if(index == Kind.WATT) return "watt";
		if(index == Kind.WEBER) return "weber";
		
		return null;
	}

	/**
	 * String to unit.
	 *
	 * @param s the s
	 * @return the kind
	 */
	//TODO use jsbml method
	@SuppressWarnings("deprecation")
	public static Kind StringToUnit(String s){
			if(s.equals("ampere")) return Kind.AMPERE;
			else if(s.equals("avogadro")) return Kind.AVOGADRO;
			else if(s.equals("becquerel")) return  Kind.BECQUEREL;
			else if(s.equals("candela")) return Kind.CANDELA;
			else if(s.equals("celsius")) return Kind.CELSIUS;
			else if(s.equals("coulomb")) return Kind.COULOMB;
			else if(s.equals("farad")) return Kind.FARAD;
			else if(s.equals("gram")) return Kind.GRAM;
			else if(s.equals("gray")) return Kind.GRAY;
			else if(s.equals("henry")) return Kind.HENRY;
			else if(s.equals("hertz")) return Kind.HERTZ;
			else if(s.equals( "item")) return Kind.ITEM;
			else if(s.equals("joule")) return Kind.JOULE;
			else if(s.equals( "katal")) return Kind.KATAL;
			else if(s.equals("kelvin")) return Kind.KELVIN;
			else if(s.equals("kilogram")) return Kind.KILOGRAM;
			else if(s.equals("liter")) return Kind.LITER;
			else if(s.equals("litre")) return Kind.LITRE;
			else if(s.equals("lumen")) return Kind.LUMEN;
			else if(s.equals("lux")) return Kind.LUX;
			else if(s.equals("meter")) return Kind.METER;
			else if(s.equals("metre")) return Kind.METRE;
			else if(s.equals("mole")) return Kind.MOLE;
			else if(s.equals("newton")) return Kind.NEWTON;
			else if(s.equals("ohm")) return Kind.OHM;
			else if(s.equals("pascal")) return Kind.PASCAL;
			else if(s.equals("radian")) return Kind.RADIAN;
			else if(s.equals("second")) return Kind.SECOND;
			else if(s.equals("siemens")) return Kind.SIEMENS;
			else if(s.equals("sievert")) return Kind.SIEVERT;
			else if(s.equals("steradian")) return Kind.STERADIAN;
			else if(s.equals("tesla")) return Kind.TESLA;
			else if(s.equals("volt")) return Kind.VOLT;
			else if(s.equals("watt")) return Kind.WATT;
			else if(s.equals( "weber")) return Kind.WEBER;
			else  return Kind.DIMENSIONLESS ;
		}
}
