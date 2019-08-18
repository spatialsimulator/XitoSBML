package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.ext.spatial.SpatialReactionPlugin;
import org.sbml.jsbml.text.parser.ParseException;

/**
 * The class ReactionTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing reaction.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ReactionTable extends SBaseTable {

	/** The header of table. */
	private final String[] header = { "id","reversible","islocal","kinetic law","reactant","product","modifier"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The SBML model. */
	private Model model;
	
	/** The ReactionDialog, which generates a GUI for creating / editing Reaction. */
	private ReactionDialog rd;
	
	/**
	 * Instantiates a new reaction table.
	 *
	 * @param lor the list of reactions
	 */
	ReactionTable(ListOf<Reaction> lor){
		this.model = lor.getModel();
		list = lor;
		setReactionToList(lor);
		MyTableModel tm = getTableModelWithReaction();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("reaction",table);
	}
	
	/**
	 * Sets the new reaction to the list.
	 *
	 * @param lor the new reaction to the list
	 */
	private void setReactionToList(ListOf<Reaction> lor){
		long max = lor.size();
		for(int i = 0; i < max; i++){
			Reaction r = lor.get(i);
			memberList.add(r.clone());
		}
	}
	
	/**
	 * Gets the table model.
	 *
	 * @return the table model with reaction
	 */
	private MyTableModel getTableModelWithReaction(){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Reaction r = (Reaction) memberList.get(i);
			SpatialReactionPlugin srp = (SpatialReactionPlugin) r.getPlugin("spatial");
			data[i][0] = r.getId();
			data[i][1] = r.getReversible();
			data[i][2] = srp.getIsLocal();
			data[i][3] = r.isSetKineticLaw() ? r.getKineticLaw().getMath().toFormula() : "";
			data[i][4] = listMemberToString(r.getListOfReactants());
			data[i][5] = listMemberToString(r.getListOfProducts());
			data[i][6] = listMemberToString(r.getListOfModifiers());
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
				case 1: // reversible
				case 2:	// islocal
				case 3: // kinetic law
				case 4: // reactant
				case 5: // product
				case 6: // modifier
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}
	
	/**
	 * Converts Reaction to a String. The converted string will contain the id
	 * of all species references (SimpleSpeciesReferences) included in the list
	 * separated with space character.
	 *
	 * @param lo the list of SimpleSpeciesReference
	 * @return the converted string
	 */
	private String listMemberToString(ListOf<?> lo){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0 ; i < lo.size(); i++)
			sb.append(((SimpleSpeciesReference)lo.get(i)).getSpecies() + " ");

		return sb.toString();
	}

	/**
	 * Converts Reaction to a Vector. The converted vector will contain reaction information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>boolean:reversible</li>
	 *     <li>boolean:isLocal to SpatialReactionPlugin</li>
	 *     <li>KineticLaw:kinetic law</li>
	 *     <li>String:list of reactants converted to string by listMemberToString(ListOf&lt;?&gt;)</li>
	 *     <li>String:list of products converted to string by listMemberToString(ListOf&lt;?&gt;)</li>
	 *     <li>String:list of modifiers converted to string by listMemberToString(ListOf&lt;?&gt;)</li>
	 * </ul>
	 *
	 * @param r the JSBML Reaction object
	 * @return the converted vector
	 */
	private Vector<Object> reactionToVector(Reaction r){
		SpatialReactionPlugin srp = (SpatialReactionPlugin) r.getPlugin("spatial");
		Vector<Object> v = new Vector<Object>();
		v.add(r.getId());
		v.add(r.getReversible());
		v.add(srp.getIsLocal());
		if (r.isSetKineticLaw()) {
		  v.add(r.getKineticLaw().getMath().toFormula());
		} else {
		  v.addElement("");
		}
		v.add(listMemberToString(r.getListOfReactants()));
		v.add(listMemberToString(r.getListOfProducts()));
		v.add(listMemberToString(r.getListOfModifiers()));
		
		return v;
	}
	
	/**
	 * Adds the Reaction object to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. ReactionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	@Override
	void add() throws IllegalArgumentException, ParseException{
		if(rd == null)
			rd = new ReactionDialog(model);
		
		Reaction r = rd.showDialog();
		
		if(r == null) return;
			
		memberList.add(r.clone());
		((MyTableModel)table.getModel()).addRow(reactionToVector(r.clone()));
	}

	/**
	 * Edits the Reaction object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. ReactionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the reaction
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 */
	@Override
	void edit(int index) throws IllegalArgumentException, ParseException{
		if(index == -1 ) return;
		if(rd == null)
			rd = new ReactionDialog(model);
		
		Reaction r = rd.showDialog((Reaction) memberList.get(index));
		
		if(r == null) return;
			
		memberList.set(index, r);

		// copy contents of AdvectionCoefficient(JTable) to AdvectionCoefficient(Model)
		Reaction dstr = (Reaction)list.getElementBySId(r.getId());
		SBMLProcessUtil.copyReactionContents(r, dstr);

		((MyTableModel)table.getModel()).updateRow(index, reactionToVector(r));
	
	}
}
