package xitosbml.pane;

import ij.gui.GenericDialog;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class SpeciesDialog {
	
	/** The species. */
	private Species species;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The initial. */
	private final String[] initial = {"amount","concentration"};
	
	/** The units. */
	private final String[] units = {"substance","mole","item","gram","kilogram","dimensionless"};
	
	/** The bool. */
	private final String[] bool = {"true","false"};
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new species dialog.
	 *
	 * @param model the model
	 */
	public SpeciesDialog(Model model){
		this.model = model;
		
	}
	
	/**
	 * Show dialog.
	 *
	 * @return the species
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
		gd.addRadioButtonGroup("boundaryCondition:",bool,1,2,"true");
		gd.addRadioButtonGroup("constant:",bool,1,2,"true");
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		species  = model.createSpecies();
		setSpeciesData();
		
		return species;
	}
	
	/**
	 * Show dialog.
	 *
	 * @param species the species
	 * @return the species
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
		gd.addRadioButtonGroup("hasOnlySubstnaceUnit:", bool, 1, 2, String.valueOf(species.getHasOnlySubstanceUnits()));
		gd.addRadioButtonGroup("boundaryCondition:",bool,1, 2, String.valueOf(species.getBoundaryCondition()));
		gd.addRadioButtonGroup("constant:",bool,1,2, String.valueOf(species.getConstant()));
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setSpeciesData();
		
		return species;
	}
	
	/**
	 * Sets the species data.
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
		species.setSubstanceUnits(SBMLProcessUtil.StringToUnit(gd.getNextChoice()));

		if(species.isSetInitialAmount())
			species.setHasOnlySubstanceUnits(true);
		else
			species.setHasOnlySubstanceUnits(false);
		species.setBoundaryCondition(Boolean.parseBoolean(gd.getNextRadioButton()));
		species.setConstant(Boolean.parseBoolean(gd.getNextRadioButton()));
	}
}
