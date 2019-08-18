package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;


/**
 * The class ParameterTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing parameters.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ParameterTable extends SBaseTable {

	/** The header of table. */
	private final String[] header = { "id", "value", "units", "constant" };
	
	/** The JTable object. */
	private JTable table;
	
	/** The ParameterDialog, which generates a GUI for creating / editing Parameters. */
	private ParameterDialog pd;
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new parameter table.
	 *
	 * @param lop the list of parameters
	 */
	ParameterTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameters();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("parameter",table);
	}
	
	/**
	 * Sets the new parameter to the list.
	 *
	 * @param lop the new parameter to the list
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
	 * Gets the table model.
	 *
	 * @return the table model with parameters
	 *
	 */
	private MyTableModel getTableModelWithParameters(){
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
	 * Converts Parameter to a Vector. The converted vector will contain parameter information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>String:unit</li>
	 *     <li>boolean:constant</li>
	 * </ul>
	 *
	 * @param p the JSBML Parameter object
	 * @return the converted vector
	 */
	private Vector<Object> parameterToVector(Parameter p){
		Vector<Object> v = new Vector<Object>();
		v.add(p.getId());
		v.add(p.getValue());
		v.add(p.getUnits());
		v.add(p.getConstant());
		
		return v;
	}
	
	/**
	 * Adds the Parameter object to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. ParameterDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException the identifier exception
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

	/**
	 * Edits the Parameter object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. ParameterDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the advection coefficient
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException the identifier exception
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
		Parameter param = (Parameter) list.getElementBySId(p.getId());
		SBMLProcessUtil.copyParameterContents(p, param);

		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
	
	}
}
