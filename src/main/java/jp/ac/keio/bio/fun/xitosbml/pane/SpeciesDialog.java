package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.SpatialSpeciesPlugin;

import ij.gui.GenericDialog;

/**
 * The class SpeciesDialog, which generates a GUI for creating / editing Species.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.SpeciesTable}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SpeciesDialog {
	
	/** The JSBML Species object. */
	private Species species;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The string value for initial amount/concentration. */
	private final String[] initial = {"amount","concentration"};
	
	/** The string value for units. */
	private final String[] units = {"mole","item","gram","kilogram","dimensionless"};
	
	/** The boolean value for boundaryCondition, constant and hasOnlySubstanceUnit. */
	private final String[] bool = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new species dialog.
	 *
	 * @param model the SBML model
	 */
	public SpeciesDialog(Model model){
		this.model = model;
		
	}
	
	/**
	 * Create and show a dialog for adding Species.
	 * If a user creates a species through this dialog,
	 * then a Species object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Species object if a species is created
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException the identifier exception
	 */
	public Species showDialog()throws IllegalArgumentException, IdentifierException{
		gd = new GenericDialog("Add Species");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");
		gd.addNumericField("quantity:", 0, 1);
		gd.addChoice("compartment:", SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments()), null);
		gd.addChoice("substanceUnit:", units, null);
		gd.addRadioButtonGroup("boundaryCondition:",bool,1,2,"false");
		gd.addRadioButtonGroup("constant:",bool,1,2,"false");
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		species  = model.createSpecies();
		setSpeciesData();
		
		return species;
	}
	
	/**
	 * Create and show a dialog for adding Species with given JSBML Species object.
	 * If a user edits the species through this dialog,
	 * then a Species object will be returned. If not, null is returned.
	 *
	 * @param species the JSBML Species object
	 * @return the JSBML Species object if the parameter is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException the identifier exception
	 */
	public Species showDialog(Species species)throws IllegalArgumentException, IdentifierException{
		this.species = species;
		gd = new GenericDialog("Edit Species");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", species.getId());
		if(species.isSetInitialAmount()){
			gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");
			gd.addNumericField("quantity:", species.getInitialAmount(), 1);
		} else if(species.isSetInitialConcentration()){
			gd.addRadioButtonGroup("initial:", initial, 1, 2, "concentration");
			gd.addNumericField("quantity:", species.getInitialConcentration(), 1);
		}
		gd.addChoice("compartment:", SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments()), species.getCompartment());
		gd.addChoice("substanceUnit:", units, species.getUnits());			
		gd.addRadioButtonGroup("boundaryCondition:",bool,1, 2, String.valueOf(species.getBoundaryCondition()));
		gd.addRadioButtonGroup("constant:",bool,1,2, String.valueOf(species.getConstant()));
		gd.addRadioButtonGroup("hasOnlySubstnaceUnit:", bool, 1, 2, String.valueOf(species.getHasOnlySubstanceUnits()));
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setSpeciesData();
		
		return this.species;
	}
	
	/**
	 * Sets/updates the following information of the species from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:initialAmount or initialConcentration</li>
	 *     <li>String:compartment</li>
	 *     <li>Kind:substance units</li>
	 *     <li>boolean:has only substance units</li>
	 *     <li>boolean:boundary condition</li>
	 *     <li>boolean:constant</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException the identifier exception
	 */
	private void setSpeciesData() throws IllegalArgumentException, IdentifierException{
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		species.setId(str);
		String quantity = gd.getNextRadioButton();
		if(quantity.contains(initial[0])) 
			species.setInitialAmount(gd.getNextNumber());
		else 
			species.setInitialConcentration(gd.getNextNumber());
	
		species.setCompartment(gd.getNextChoice());
		species.setSubstanceUnits(Unit.Kind.valueOf(gd.getNextChoice().toUpperCase()));

		if(species.isSetInitialAmount())
			species.setHasOnlySubstanceUnits(true);
		else
			species.setHasOnlySubstanceUnits(false);
		species.setBoundaryCondition(Boolean.valueOf(gd.getNextRadioButton()));
		species.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		
		SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) species.getPlugin(SpatialConstants.shortLabel);
		ssp.setSpatial(true);
	}
}
