/**
 * 
 */

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentMapping;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialCompartmentPlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialSpeciesPlugin;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.libsbml;

/**
 * @author Akira Funahashi
 *
 */
public class Test {

	/**
	 * 
	 */
	public Test() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.loadLibrary("sbmlj");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.exit(1);
		}
		SBMLDocument d = new SBMLReader().readSBMLFromFile("fish_skin_vcell.xml");
		Model m = d.getModel();
		System.out.println(m.getName());
		System.out.println(m.getPackageName() + ":" + m.getPackageVersion());
		SpatialModelPlugin splugin = (SpatialModelPlugin) m.getPlugin("spatial");
		System.out.println(splugin.getPackageName() + ":" + splugin.getPackageVersion());
		// Compartments
		for (int i = 0; i < m.getNumCompartments(); i++) {
			Compartment c = m.getCompartment(i);
			System.out.println("Compartment" + i + ": " + c.getId());
			SpatialCompartmentPlugin cplugin = (SpatialCompartmentPlugin) c.getPlugin("spatial");
			CompartmentMapping cmap = cplugin.getCompartmentMapping();
			if (cplugin.getCompartmentMapping().isSetId()) {
				System.out.println("  Comp" + i + "  CMSpId: " + cmap.getId());
				System.out.println("  Comp" + i + "  CM_DType: " + cmap.getDomainType());
				System.out.println("  Comp" + i + "  CM_UnitSz: " + cmap.getUnitSize());
			}
		}
		// Species
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species s = m.getSpecies(i);
			System.out.println("Species" + i + ": " + s.getId());
			SpatialSpeciesPlugin ss = (SpatialSpeciesPlugin) s.getPlugin("spatial");
			if (ss.getIsSpatial()) {
				System.out.println("  species" + i + " isSpatial: " + ss.getIsSpatial());
			}
		}
		// Parameters
		for (int i = 0; i < m.getNumParameters(); i++) {
			Parameter param = m.getParameter(i);
			System.out.println("Parameter" + i + ": " + param.getId());
			SpatialParameterPlugin pplugin = (SpatialParameterPlugin) param.getPlugin("spatial");
			if (pplugin.isSetSpatialSymbolReference()) {
				System.out.println("Parameter" + i + "  SpRefId: " + pplugin.getSpatialSymbolReference().getSpatialRef());
			}
			if (pplugin.isSetDiffusionCoefficient()) {
				System.out.println("Diff_" + i + "  SpeciesVarId: " + pplugin.getDiffusionCoefficient().getVariable());
				System.out.println("Diff_" + i + "  Type: " + libsbml.DiffusionKind_toString(pplugin.getDiffusionCoefficient().getType()));
				for (int j = 0; j < pplugin.getDiffusionCoefficient().getNumCoordinateReferences(); ++j) {
					System.out.println("Diff_" + i + "  SpCoordIndex  " + j + " : " +
					libsbml.CoordinateKind_toString(pplugin.getDiffusionCoefficient().getCoordinateReference(j).getCoordinate()));
				}
			}
			if (pplugin.isSetAdvectionCoefficient()) {
				System.out.println("Adv_" + i + "  SpeciesVarId: " + pplugin.getAdvectionCoefficient().getVariable());
				System.out.println("Adv_" + i + "  SpCoordIndex: " +
				libsbml.CoordinateKind_toString(pplugin.getAdvectionCoefficient().getCoordinate()));
			}
			if (pplugin.isSetBoundaryCondition()) {
				System.out.println("BC_" + i + "  SpeciesVarId: " + pplugin.getBoundaryCondition().getVariable());
				System.out.println("BC_" + i + "  SpCoordBoundary: " + pplugin.getBoundaryCondition().getCoordinateBoundary());
				System.out.println("BC_" + i + "  SpBoundaryType: " + pplugin.getBoundaryCondition().getType());
			}
		}
	}

}
