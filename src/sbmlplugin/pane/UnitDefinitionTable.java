package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfUnitDefinitions;
import org.sbml.libsbml.ListOfUnits;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;

import sbmlplugin.pane.SBaseTable.MyTableModel;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 26, 2016
 */
public class UnitDefinitionTable extends SBaseTable {
	private final String[] header = { "id", "unit"};
	private JTable table;
	private UnitDefinitionDialog udd;
	private Model model;
	
	UnitDefinitionTable(ListOfUnitDefinitions loud){
		this.model = loud.getModel();
		list = loud;
		setUnitDefinitionToList(loud);
		MyTableModel tm = getTableModelWithUnitDefinitions(loud);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("unit",table);
	}
	
	private void setUnitDefinitionToList(ListOfUnitDefinitions loud){
		long max = loud.size();
		for(int i = 0; i < max; i++){
			UnitDefinition ud = loud.get(i);
			memberList.add(ud);
		}
	}
	
	private MyTableModel getTableModelWithUnitDefinitions(ListOfUnitDefinitions loud){
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

	private String unitsToString(ListOfUnits lou){
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
