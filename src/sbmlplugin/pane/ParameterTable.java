package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SpatialParameterPlugin;


/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class ParameterTable extends SBaseTable {

	private final String[] header = { "id", "value", "units", "constant" };
	private JTable table;
	private ParameterDialog pd;
	private Model model;
	
	ParameterTable(ListOfParameters lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameters(lop);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("parameter",table);
	}
	
	private void setParameterToList(ListOfParameters lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if( sp.isSetAdvectionCoefficient() || sp.isSetBoundaryCondition() || sp.isSetDiffusionCoefficient() ) continue;
			memberList.add(p);
		}
	}
	
	private MyTableModel getTableModelWithParameters(ListOfParameters lop){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			data[i][0] = p.getId();
			data[i][1] = p.isSetValue() ? p.getValue(): null;			
			data[i][2] = p.getUnits();
			data[i][3] = p.getConstant();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
					return String.class;
				case 1: // value
					return Double.class;
				case 2: // units
				case 3: // constant
					return String.class;
				default:
					return String.class;
				}
			}
		};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	private Vector<Object> parameterToVector(Parameter p){
		Vector<Object> v = new Vector<Object>();
		v.add(p.getId());
		v.add(p.getValue());
		v.add(p.getUnits());
		v.add(p.getConstant());
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
	 */
	@Override
	void add() {
		if(pd == null)
			pd = new ParameterDialog(model);
		
		Parameter p = pd.showDialog();
		
		if(p == null) return;
		
		if(containsDuplicateId(p)){
			errDupID(table);
			return;
		}
			
		memberList.add(p);
		((MyTableModel)table.getModel()).addRow(parameterToVector(p));
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) {
		if(index == -1 ) return ;
		if(pd == null)
			pd = new ParameterDialog(model);
		
		Parameter p = pd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;
		
		if(containsDuplicateId(p)){
			errDupID(table);
			return;
		}
			
		memberList.set(index, p);
		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
	
	}
}
