package jp.ac.keio.bio.fun.xitosbml.pane;

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

import ij.gui.GenericDialog;

/**
 * The class ReactionDialog, which generates a GUI for creating / editing a Reaction.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.ReactionTable}.
 * Date Created: Jan 22, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ReactionDialog {
	
	/** The JSBML Reaction object. */
	private Reaction reaction;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The boolean value for reversible and isLocal. */
	private final String[] bool = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/** The list of Species. */
	private ListOf<Species> los;
	
	/**
	 * Instantiates a new reaction dialog.
	 *
	 * @param model the SBML model
	 */
	public ReactionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Create and show a dialog for adding Reaction.
	 * If a user creates a reaction through this dialog,
	 * then a Reaction object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Reaction object if a reaction is created
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	public Reaction showDialog() throws IllegalArgumentException, ParseException{
		this.los = model.getListOfSpecies();
		gd = new GenericDialog("Add Reaction");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", null);
		gd.addRadioButtonGroup("reversible:", bool, 1, 2, "false");
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
	 * Create and show a dialog for adding Reaction with given JSBML Reaction object.
	 * If a user edits the reaction through this dialog,
	 * then a Reaction object will be returned. If not, null is returned.
	 *
	 * @param reaction the JSBML Reaction object
	 * @return the JSBML Reaction object if the reaction is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	public Reaction showDialog(Reaction reaction) throws IllegalArgumentException, ParseException{
		this.los = model.getListOfSpecies();
		this.reaction = reaction;
		SpatialReactionPlugin srp = (SpatialReactionPlugin) reaction.getPlugin("spatial");
		gd = new GenericDialog("Edit Reaction");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", reaction.getId());
		gd.addRadioButtonGroup("reversible:", bool, 1, 2, String.valueOf(reaction.getReversible()));
		gd.addRadioButtonGroup("isLocal:", bool, 1, 2, String.valueOf(srp.getIsLocal()));
		if (reaction.isSetKineticLaw()) {
		  gd.addStringField("kinetic law", reaction.getKineticLaw().getMath().toFormula());
		} else {
		  gd.addStringField("kinetic law", "");
		}
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
     * Check whether given species (listOf&lt;Species&gt; los) are included in
	 * the given list of SpeciesReferences, and returns the array of boolean for all species.
	 * The list of SpeciesReferences might be listOfReactants or listOfProducts.
	 *
	 * @param los the list of Species
	 * @param losr the list of SpeciesReferences (listOfReactants or listOfProducts)
	 * @return the array of boolean value (boolean[]) which contains whether the species is included in the given list of SpeciesReferences.
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
	 * Check whether given species (listOf&lt;Species&gt; los) are included in
	 * the given list of ModifierSpeciesReferences, and returns the array of boolean for all species.
	 *
	 * @param los the list of Species
	 * @param losr the list of ModifierSpeciesReferences
	 * @return the array of boolean value (boolean[]) which contains whether the species is included in the given list of ModifierSpeciesReferences.
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
	 * Sets/updates the following information of the reaction and the spatial reaction plugin from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>boolean:reversible</li>
	 *     <li>boolean:fast</li>
	 *     <li>boolean:isLocal to SpatialReactionPlugin</li>
	 *     <li>KineticLaw:kinetic law</li>
	 *     <li>SpeciesReference:reactants</li>
	 *     <li>SpeciesReference:products</li>
	 *     <li>SpeciesReference:modifiers</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	@SuppressWarnings("deprecation")
  private void setReactionData() throws IllegalArgumentException, ParseException{
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		reaction.setId(str);
		reaction.setReversible(Boolean.valueOf(gd.getNextRadioButton()));
		reaction.setFast(false); // I know it is deprecated, but we are using L3v1, so "fast" is required.
		SpatialReactionPlugin srp = (SpatialReactionPlugin) reaction.getPlugin("spatial");
		srp.setIsLocal(Boolean.valueOf(gd.getNextRadioButton()));
		String formula = gd.getNextString();
		if(formula != null && !formula.equals("")) {
		  KineticLaw kl = reaction.isSetKineticLaw() ? reaction.getKineticLaw() : reaction.createKineticLaw();
			kl.setMath(ASTNode.parseFormula(formula));
		}
		
		@SuppressWarnings("unchecked")
		Vector<Checkbox> v = gd.getCheckboxes();
		ListOf<SpeciesReference> losr = reaction.getListOfReactants();
		int size = (int) los.size();
		
		for (int i = 0; i < size; i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel().trim();
			if (label.indexOf(' ') != -1) {
					label = label.replace(' ', '_');
			}
			String id = "sr_reac_" + label;
			if (cb.getState()) {
				SpeciesReference sr = (SpeciesReference) (losr.get(id) != null ? losr.get(id) : reaction.createReactant(id));
				sr.setSpecies(label);
				sr.setConstant(true);
				sr.setStoichiometry(1);
			} else {
				losr.remove(id);
			}
		}
		
		losr = reaction.getListOfProducts();
		for (int i = size; i < size * 2; i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel();
			if (label.indexOf(' ') != -1) {
					label = label.replace(' ', '_');
			}
			String id = "sr_prod_" + label;
			if (cb.getState()) {
				SpeciesReference sr = (SpeciesReference) (losr.get(id) != null ? losr.get(id) : reaction.createProduct(id));
				sr.setSpecies(label);
				sr.setConstant(true);
				sr.setStoichiometry(1);
			} else {
				losr.remove(id);
			}
		}
		
		ListOf<ModifierSpeciesReference> lom = reaction.getListOfModifiers();
		for (int i = size * 2; i < v.size(); i++) {
			Checkbox cb = v.get(i);
			String label = cb.getLabel();
			if (label.indexOf(' ') != -1) {
					label = label.replace(' ', '_');
			}
			String id = "sr_mod_" + label;
			if (cb.getState()) {
				ModifierSpeciesReference sr = (ModifierSpeciesReference) (lom.get(id) != null ? lom.get(id) : reaction.createModifier(id));
				sr.setSpecies(label);
			} else {
				lom.remove(id);
			}
		}
	}
}
