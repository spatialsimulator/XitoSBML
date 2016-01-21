package sbmlplugin.pane;

import javax.swing.JTable;

import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.Reaction;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class ReactionTable extends SBaseTable {

	private final String[] header = { "id","initialAmount", "initialConcentration", "Compartment", "substanceUnits","hasOnlySubstanceUnits","boundaryCondition"};
	private JTable table;
	
	ReactionTable(ListOfReactions lor){
		setReactionToList(lor);
		MyTableModel tm = getTableModelWithReaction(lor);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("reaction",table);
	}
	
	private void setReactionToList(ListOfReactions lor){
		long max = lor.size();
		for(int i = 0; i < max; i++){
			Reaction p = lor.get(i);
			memberList.add(p);
		}
	}
	
	private MyTableModel getTableModelWithReaction(ListOfReactions lor){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Reaction r = (Reaction) memberList.get(i);
			data[i][0] = r.getId();
//			data[i][1] = r.isSetInitialAmount() ? s.getInitialAmount(): null;			
//			data[i][2] = r.isSetInitialConcentration() ? s.getInitialConcentration(): null;
//			data[i][3] = r.getCompartment();
//			data[i][4] = r.getSubstanceUnits();
//			data[i][5] = r.getHasOnlySubstanceUnits();
//			data[i][6] = r.getBoundaryCondition();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
					return String.class;
				case 1: // amount
				case 2: // concentration
					return Double.class;
				case 3: // Compartment
				case 4: // substance unit
				case 5: // hasOnlySubstanceUnits
				case 6: // boundaryCondition
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
	 */
	@Override
	void add() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) {
		// TODO Auto-generated method stub
		
	}
}
