package xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 26, 2016
 */
public class UnitDefinitionTable extends SBaseTable {
	
	/** The header. */
	private final String[] header = { "id", "unit"};
	
	/** The table. */
	private JTable table;
	
	/** The udd. */
	private UnitDefinitionDialog udd;
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new unit definition table.
	 *
	 * @param loud the loud
	 */
	UnitDefinitionTable(ListOf<UnitDefinition> loud){
		this.model = loud.getModel();
		list = loud;
		setUnitDefinitionToList(loud);
		MyTableModel tm = getTableModelWithUnitDefinitions(loud);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("unit",table);
	}
	
	/**
	 * Sets the unit definition to list.
	 *
	 * @param loud the new unit definition to list
	 */
	private void setUnitDefinitionToList(ListOf<UnitDefinition> loud){
		long max = loud.size();
		for(int i = 0; i < max; i++){
			UnitDefinition ud = loud.get(i);
			memberList.add(ud.clone());
		}
	}
	
	/**
	 * Gets the table model with unit definitions.
	 *
	 * @param loud the loud
	 * @return the table model with unit definitions
	 */
	private MyTableModel getTableModelWithUnitDefinitions(ListOf<UnitDefinition> loud){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			UnitDefinition ud = (UnitDefinition) memberList.get(i);
			data[i][0] = ud.getId();
			data[i][1] = unitsToString(ud.getListOfUnits());
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
				case 1: // units
					return String.class;
				default:
					return String.class;
				}
			}
		};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	/**
	 * Units to string.
	 *
	 * @param lou the lou
	 * @return the string
	 */
	private String unitsToString(ListOf<Unit> lou){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i< lou.size(); i++){
			Unit unit = lou.get(i);
			String mul = unit.getMultiplier() == 1 ? "" : String.valueOf(unit.getMultiplier()) + " *";
			String scale =  unit.getScale() == 0 ? "" : "10^" + unit.getScale() + " *";
			String exp = unit.getExponent() == 1 ? "" : "^" + String.valueOf(unit.getExponent());
			String kind = SBMLProcessUtil.unitIndexToString(unit.getKind()); 
			sb.append("(" + mul  + scale +  " " + kind + " )" + exp);
			if(i < lou.size() - 1) 
				sb.append(" * ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Unit definition to vector.
	 *
	 * @param ud the ud
	 * @return the vector
	 */
	private Vector<Object> unitDefinitionToVector(UnitDefinition ud){
		Vector<Object> v = new Vector<Object>();
		v.add(ud.getId());
//		v.add(p.getValue());
//		v.add(p.getUnits());
//		v.add(p.getConstant());
//		
		return v;
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
