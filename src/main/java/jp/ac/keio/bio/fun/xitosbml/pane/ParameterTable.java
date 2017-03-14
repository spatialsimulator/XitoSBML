package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class ParameterTable extends SBaseTable {

	/** The header. */
	private final String[] header = { "id", "value", "units", "constant" };
	
	/** The table. */
	private JTable table;
	
	/** The pd. */
	private ParameterDialog pd;
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new parameter table.
	 *
	 * @param lop the lop
	 */
	ParameterTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameters(lop);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("parameter",table);
	}
	
	/**
	 * Sets the parameter to list.
	 *
	 * @param lop the new parameter to list
	 */
	private void setParameterToList(ListOf<Parameter> lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
	
			if( sp.isSetParamType() ) continue;
			memberList.add(p.clone());
		}
	}
	
	/**
	 * Gets the table model with parameters.
	 *
	 * @param lop the lop
	 * @return the table model with parameters
	 */
	private MyTableModel getTableModelWithParameters(ListOf<Parameter> lop){
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

	/**
	 * Parameter to vector.
	 *
	 * @param p the p
	 * @return the vector
	 */
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
	void add() throws IllegalArgumentException , IdentifierException{
		if(pd == null)
			pd = new ParameterDialog(model);
		
		Parameter p = pd.showDialog();
		
		if(p == null) return;
		
		memberList.add(p.clone());
		((MyTableModel)table.getModel()).addRow(parameterToVector(p.clone()));
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) throws IllegalArgumentException, IdentifierException{
		if(index == -1 ) return ;
		if(pd == null)
			pd = new ParameterDialog(model);
		
		Parameter p = pd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;
		memberList.set(index, p);

		// copy contents of Parameter(JTable) to Parameter(Model)
		Parameter memberParam = (Parameter)memberList.get(index);
		Parameter param = (Parameter) list.getElementBySId(memberParam.getId());
		param.setValue(memberParam.getValue());
		param.setUnits(memberParam.getUnits());
		param.setConstant(memberParam.getConstant());

		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
	
	}
}
