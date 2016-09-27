package sbmlplugin.pane;

import ij.gui.GenericDialog;

import java.awt.Checkbox;
import java.util.Vector;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.spatial.SpatialReactionPlugin;
import org.sbml.jsbml.text.parser.ParseException;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 22, 2016
 */
public class ReactionDialog {
	
	/** The reaction. */
	private Reaction reaction;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The bool. */
	private final String[] bool = {"true","false"};
	
	/** The model. */
	private Model model;
	
	/** The los. */
	private ListOf<Species> los;
	
	/**
	 * Instantiates a new reaction dialog.
	 *
	 * @param model the model
	 */
	public ReactionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Show dialog.
	 *
	 * @return the reaction
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	public Reaction showDialog() throws IllegalArgumentException, ParseException{
		this.los = model.getListOfSpecies();
		gd = new GenericDialog("Add Reaction");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", null);
		gd.addRadioButtonGroup("fast:", bool, 1, 2, "true");
		gd.addRadioButtonGroup("reversible:", bool, 1, 2, "true");
		gd.addRadioButtonGroup("isLocal:", bool, 1, 2, "true");
		gd.addStringField("kinetic law", null);
		gd.addMessage("reactant:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), new boolean[(int)los.size()]);
		gd.addMessage("product:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), new boolean[(int)los.size()]);
		gd.addMessage("modifier:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), new boolean[(int)los.size()]);
		
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;

		reaction = model.createReaction();
		setReactionData();
		
		return reaction;
	}
	
	/**
	 * Show dialog.
	 *
	 * @param reaction the reaction
	 * @return the reaction
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	public Reaction showDialog(Reaction reaction) throws IllegalArgumentException, ParseException{
		this.los = model.getListOfSpecies();
		this.reaction = reaction;
		SpatialReactionPlugin srp = (SpatialReactionPlugin) reaction.getPlugin("spatial");
		gd = new GenericDialog("Edit Parameter");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", reaction.getId());
		gd.addRadioButtonGroup("fast", bool, 1, 2, String.valueOf(reaction.getFast()));
		gd.addRadioButtonGroup("reversible", bool, 1, 2, String.valueOf(reaction.getReversible()));
		gd.addRadioButtonGroup("isLocal", bool, 1, 2, String.valueOf(srp.getIsLocal()));
		gd.addStringField("kinetic law", reaction.getKineticLaw().getMathMLString());
		gd.addMessage("reactant:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), boolSpeciesInSReference(los, reaction.getListOfReactants()));
		gd.addMessage("product:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), boolSpeciesInSReference(los, reaction.getListOfProducts()));
		gd.addMessage("modifier:");
		gd.addCheckboxGroup((int) los.size() / 3 + 1, 3, SBMLProcessUtil.listIdToStringArray(los), boolSpeciesInModifierSReference(los, reaction.getListOfModifiers()));
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setReactionData();
		
		return reaction;
	}
	
	/**
	 * Bool species in S reference.
	 *
	 * @param los the los
	 * @param losr the losr
	 * @return the boolean[]
	 */
	private boolean[] boolSpeciesInSReference(ListOf<Species> los, ListOf<SpeciesReference> losr){
		boolean[] bool = new boolean[(int)los.size()];
		
		for(int i = 0; i < los.size(); i++)
			for(int j = 0; j < losr.size(); j++)
				if(los.get(i).getId().equals(losr.get(j).getSpecies()))
					bool[i] = true;
			
		return bool;
	}
	
	/**
	 * Bool species in modifier S reference.
	 *
	 * @param los the los
	 * @param losr the losr
	 * @return the boolean[]
	 */
	private boolean[] boolSpeciesInModifierSReference(ListOf<Species> los, ListOf<ModifierSpeciesReference> losr){
		boolean[] bool = new boolean[(int)los.size()];
		
		for(int i = 0; i < los.size(); i++)
			for(int j = 0; j < losr.size(); j++)
				if(los.get(i).getId().equals(losr.get(j).getSpecies()))
					bool[i] = true;
			
		return bool;
	}
	
	/**
	 * Sets the reaction data.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	private void setReactionData() throws IllegalArgumentException, ParseException{
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		reaction.setId(str);
		reaction.setFast(Boolean.getBoolean(gd.getNextRadioButton()));
		reaction.setReversible(Boolean.getBoolean(gd.getNextRadioButton()));
		SpatialReactionPlugin srp = (SpatialReactionPlugin) reaction.getPlugin("spatial");
		srp.setIsLocal(Boolean.getBoolean(gd.getNextRadioButton()));
		KineticLaw kl = reaction.isSetKineticLaw() ? reaction.getKineticLaw() : reaction.createKineticLaw();
		String formula = gd.getNextString();
		if(formula != null)
			kl.setMath(ASTNode.parseFormula(formula));
		
		@SuppressWarnings("unchecked")
		Vector<Checkbox> v = gd.getCheckboxes();
		ListOf<SpeciesReference> losr = reaction.getListOfReactants();
		int size = (int) los.size();
		
		for (int i = 0; i < size; i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel().trim();
			if (label.indexOf(' ') != -1)
					label = label.replace(' ', '_');
			if (cb.getState()) {
				SpeciesReference sr = (SpeciesReference) (losr.get(label) != null ? losr.get(label) : reaction.createReactant());
				sr.setSpecies(label);
				sr.setConstant(true);
				sr.setStoichiometry(1);
			} else {
				losr.remove(label);
			}
		}
		
		losr = reaction.getListOfProducts();
		for (int i = size; i < size * 2; i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel();
			if (label.indexOf(' ') != -1)
					label = label.replace(' ', '_');
			if (cb.getState()) {
				SpeciesReference sr = (SpeciesReference) (losr.get(label) != null ? losr.get(label) : reaction.createProduct());
				sr.setSpecies(label);
				sr.setConstant(true);
				sr.setStoichiometry(1);
			} else {
				losr.remove(label);
			}
		}
		
		ListOf<ModifierSpeciesReference> lom = reaction.getListOfModifiers();
		for (int i = size * 2; i < v.size(); i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel();
			if (label.indexOf(' ') != -1)
					label = label.replace(' ', '_');
			if (cb.getState()) {
				ModifierSpeciesReference sr = (ModifierSpeciesReference) (lom.get(label) != null ? losr.get(label) : reaction.createModifier());				
				sr.setSpecies(label);
			} else {
				losr.remove(label);
			}
		}
		
	}
}
