package sbmlplugin.pane;

import ij.gui.GenericDialog;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SpatialSpeciesPlugin;
import org.sbml.libsbml.Species;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class SpeciesDialog {
	private Species species;
	private GenericDialog gd;
	private final String[] initial = {"amount","concentration"};
	private final String[] units = {"substance","mole","item","gram","kilogram","dimensionless"};
	private final String[] bool = {"true","false"};
	private Model model;
	
	public SpeciesDialog(Model model){
		this.model = model;
		
	}
	
	public Species showDialog(){
		gd = new GenericDialog("Add Species");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");
		gd.addNumericField("quantity:", 0, 1);
		gd.addChoice("compartment:", SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments()), null);
		gd.addChoice("substanceUnit:", units, null);
		gd.addRadioButtonGroup("hasOnlySubstnaceUnit:", bool, 1,2,"true");
		gd.addRadioButtonGroup("boundaryCondition:",bool,1,2,"true");
		gd.addRadioButtonGroup("constant:",bool,1,2,"true");
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		species  = model.createSpecies();
		setSpeciesData();
		
		return species;
	}
	
	public Species showDialog(Species species){
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
	
	private void setSpeciesData(){
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
		species.setSubstanceUnits(gd.getNextChoice());
		species.setHasOnlySubstanceUnits(Boolean.parseBoolean(gd.getNextRadioButton()));
		species.setBoundaryCondition(Boolean.parseBoolean(gd.getNextRadioButton()));
		species.setConstant(Boolean.parseBoolean(gd.getNextRadioButton()));
		SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) species.getPlugin("spatial");
		ssp.setIsSpatial(true);
	}
}
