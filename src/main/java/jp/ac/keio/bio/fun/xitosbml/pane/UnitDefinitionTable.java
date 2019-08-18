package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;


/**
 * The class UnitDefinitionTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing unit definition.
 * This class is not used in the current implementation of XitoSBML.
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 * Date Created: Jan 26, 2016
 */
public class UnitDefinitionTable extends SBaseTable {
	
	/** The header of table. */
	private final String[] header = { "id", "unit"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The UnitDefinitionDialog, which generates a GUI for creating / editing UnitDefinition. */
	private UnitDefinitionDialog udd;
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new unit definition table.
	 *
	 * @param loud the list of unit definition
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
	 * @param loud the list of unit definition
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
	 * Converts Unit to a String. The converted string will contain unit information as follows:
	 * <ul>
	 *     <li>String:(</li>
	 *     <li>String:multiplier</li>
	 *     <li>String:scale</li>
	 *     <li>String: </li>
	 *     <li>String:kind</li>
	 *     <li>String: )</li>
	 *     <li>String:exponent</li>
	 *     <li>String: * </li>
	 * </ul>
	 *
	 * @param lou the list of unit
	 * @return the converted string
	 */
	private String unitsToString(ListOf<Unit> lou){
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i< lou.size(); i++){
			Unit unit = lou.get(i);
			String mul = unit.getMultiplier() == 1 ? "" : String.valueOf(unit.getMultiplier()) + " *";
			String scale =  unit.getScale() == 0 ? "" : "10^" + unit.getScale() + " *";
			String exp = unit.getExponent() == 1 ? "" : "^" + String.valueOf(unit.getExponent());
			String kind = unit.getKind().getName(); 
			sb.append("(" + mul  + scale +  " " + kind + " )" + exp);
			if(i < lou.size() - 1) 
				sb.append(" * ");
		}
		
		return sb.toString();
	}
	
	/**
	 * Converts UnitDefinition to a Vector. The converted vector will contain unit definition information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 * </ul>
	 *
	 * @param ud the UnitDefinition
	 * @return the converted vector
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

	/**
	 * Adds the UnitDefinition object to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI.
	 * This method is not yet implemented.
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 */
	@Override
	void add() {

	}

	/**
	 * Edits the UnitDefinition object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. UnitDefinisionDialog).
	 * This method is not yet implemented.
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the unit definition
	 */
	@Override
	void edit(int index) {

	}

}
