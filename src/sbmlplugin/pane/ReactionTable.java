package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.ext.spatial.SpatialReactionPlugin;
import org.sbml.jsbml.text.parser.ParseException;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class ReactionTable extends SBaseTable {

	/** The header. */
	private final String[] header = { "id","fast","reversible","islocal","kinetic law","reactant","product","modifier"};
	
	/** The table. */
	private JTable table;
	
	/** The model. */
	private Model model;
	
	/** The rd. */
	private ReactionDialog rd;
	
	/**
	 * Instantiates a new reaction table.
	 *
	 * @param lor the lor
	 */
	ReactionTable(ListOf<Reaction> lor){
		this.model = lor.getModel();
		setReactionToList(lor);
		MyTableModel tm = getTableModelWithReaction(lor);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("reaction",table);
	}
	
	/**
	 * Sets the reaction to list.
	 *
	 * @param lor the new reaction to list
	 */
	private void setReactionToList(ListOf<Reaction> lor){
		long max = lor.size();
		for(int i = 0; i < max; i++){
			Reaction r = lor.get(i);
			memberList.add(r.clone());
		}
	}
	
	/**
	 * Gets the table model with reaction.
	 *
	 * @param lor the lor
	 * @return the table model with reaction
	 */
	private MyTableModel getTableModelWithReaction(ListOf<Reaction> lor){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Reaction r = (Reaction) memberList.get(i);
			SpatialReactionPlugin srp = (SpatialReactionPlugin) r.getPlugin("spatial");
			data[i][0] = r.getId();
			data[i][1] = r.getFast();
			data[i][2] = r.getReversible();
			data[i][3] = srp.getIsLocal();
			data[i][4] = r.isSetKineticLaw() ? r.getKineticLaw().getMathMLString() : "";
			data[i][5] = listMemberToString(r.getListOfReactants());
			data[i][6] = listMemberToString(r.getListOfProducts());
			data[i][7] = listMemberToString(r.getListOfModifiers());
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
				case 1: // fast
				case 2: // reversible
				case 3:	// islocal
				case 4: // kinetic law
				case 5: // reactant
				case 6: // product
				case 7: // modifier
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}
	
	/**
	 * List member to string.
	 *
	 * @param lo the lo
	 * @return the string
	 */
	private String listMemberToString(ListOf<?> lo){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0 ; i < lo.size(); i++)
			sb.append(((SimpleSpeciesReference)lo.get(i)).getSpecies() + " ");

		return sb.toString();
	}

	/**
	 * Reaction to vector.
	 *
	 * @param r the r
	 * @return the vector
	 */
	private Vector<Object> reactionToVector(Reaction r){
		SpatialReactionPlugin srp = (SpatialReactionPlugin) r.getPlugin("spatial");
		Vector<Object> v = new Vector<Object>();
		v.add(r.getId());
		v.add(r.getFast());
		v.add(r.getReversible());
		v.add(srp.getIsLocal());
		v.add(r.getKineticLaw().getMathMLString());
		v.add(listMemberToString(r.getListOfReactants()));
		v.add(listMemberToString(r.getListOfProducts()));
		v.add(listMemberToString(r.getListOfModifiers()));
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
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

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) throws IllegalArgumentException, ParseException{
		if(index == -1 ) return;
		if(rd == null)
			rd = new ReactionDialog(model);
		
		Reaction r = rd.showDialog((Reaction) memberList.get(index));
		
		if(r == null) return;
			
		memberList.set(index, r);
		((MyTableModel)table.getModel()).updateRow(index, reactionToVector(r));
	
	}
}
