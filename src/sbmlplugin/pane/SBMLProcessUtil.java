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
	public static final String[] boundType = {/*"UNKNOWN",*/"ROBIN_VALUE_COEFFICIENT","ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT","ROBIN_SUM","NEUMANN","DIRICHLET"};
	public static final String[] diffType = {/*"UNKNOWN", */"ISOTROPIC","ANISOTROPIC","TENSOR"};

	
	public static String[] listIdToStringArray(ListOf lo){
		String[] str = new String[(int)lo.size()];
				
		for(int i = 0; i < lo.size(); i++)
			str[i] = lo.get(i).getId();
		
		return str;
	}

	public static String boundaryIndexToString(int index){
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_DIRICHLET) return "Dirichlet";
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_NEUMANN) return "Neumann";
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT) return "ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT";
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_SUM) return "ROBIN_SUM";
		if(index == libsbmlConstants.SPATIAL_BOUNDARYKIND_ROBIN_VALUE_COEFFICIENT) return "ROBIN_VALUE_COEFFICIENT";
	
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
}
