package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.BoundaryCondition;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

/**
 * The class BoundaryConditionTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing boundary condition.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class BoundaryConditionTable extends SBaseTable {

	/** The header of table. */
	private final String[] header =  { "id", "value", "constant","species", "type", "boundary"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The SBML model. */
	private Model model;
	
 	/** The BoundaryConditionDialog, which generates a GUI for creating / editing Boundary Condition. */
	private BoundaryConditionDialog bcd;
	
	/**
	 * Instantiates a new boundary condition table.
	 *
	 * @param lop the list of parameters
	 */
	BoundaryConditionTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("Boundary Condition",table);
	}
	
	/**
	 * Sets the new parameter (boundary condition) to the list.
	 *
	 * @param lop the new boundary condition to the list
	 */
	private void setParameterToList(ListOf<Parameter> lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!(sp.getParamType() instanceof BoundaryCondition)) continue;
			memberList.add(p.clone());
		}
	}
	
	/**
	 * Gets the table model
	 *
	 * @return the table model with parameter
	 */
	private MyTableModel getTableModelWithParameter(){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!(sp.getParamType() instanceof BoundaryCondition)) continue;
			BoundaryCondition bc = (BoundaryCondition) sp.getParamType();
			data[i][0] = p.getId();
			data[i][1] = p.isSetValue() ? p.getValue(): null;			
			data[i][2] = p.getConstant();
			data[i][3] = bc.getVariable();
			data[i][4] = bc.getType().name();
			data[i][5] = bc.isSetCoordinateBoundary() ? bc.getCoordinateBoundary() : bc.getBoundaryDomainType();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
					return String.class;
				case 1:	//value
					return Double.class;
				case 2: // constant
				case 3: // species
				case 4: // type
				case 5: // coordinate boundary or boundary domain type
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	/**
	 * Converts Parameter to a Vector. The converted vector will contain parameter information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>boolean:constant</li>
	 *     <li>String:variable</li>
	 *     <li>String:type</li>
	 *     <li>String:boundary or domain type</li>
	 * </ul>
	 *
	 * @param p the JSBML Parameter object
	 * @return the converted vector
	 */
	private Vector<Object> parameterToVector(Parameter p){
		Vector<Object> v = new Vector<Object>();
		v.add(p.getId());
		v.add(p.getValue());
		v.add(p.getConstant());
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
		BoundaryCondition bc = (BoundaryCondition) sp.getParamType();
		v.add(bc.getVariable());
		v.add(bc.getType().name());
		v.add(bc.isSetCoordinateBoundary() ? bc.getCoordinateBoundary() : bc.getBoundaryDomainType());
		return v;
	}
	
	/**
	 * Adds the Parameter object (boundary condition) to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. BoundaryConditionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException
	 * @throws IdentifierException
	 */
	@Override
	void add() throws IllegalArgumentException, IdentifierException{
		if(bcd == null)
			bcd = new BoundaryConditionDialog(model);
		
		Parameter p = bcd.showDialog();
		
		if(p == null) return;
		
		memberList.add(p.clone());
		((MyTableModel)table.getModel()).addRow(parameterToVector(p.clone()));
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */

	/**
	 * Edits the Parameter object (boundary condition) which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. BoundaryConditionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the boundary condition
	 * @throws IllegalArgumentException
	 * @throws IdentifierException
	 */
	@Override
	void edit(int index) throws IllegalArgumentException, IdentifierException{
		if(index == -1 ) return ;
		if(bcd == null)
			bcd = new BoundaryConditionDialog(model);
		
		Parameter p = bcd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;
		
		memberList.set(index, p);

		// copy contents of BoundaryCondition(JTable) to BoundaryCondition(Model)
		Parameter bc = (Parameter)list.getElementBySId(p.getId());
		SBMLProcessUtil.copyBoundaryConditionContents(p, bc);

		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
	}
}
