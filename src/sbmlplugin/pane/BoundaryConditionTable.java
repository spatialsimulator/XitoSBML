package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SpatialParameterPlugin;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class BoundaryConditionTable extends SBaseTable {

	/** The header. */
	private final String[] header =  { "id", "value", "constant","species", "type", "boundary"};
	
	/** The table. */
	private JTable table;
	
	/** The model. */
	private Model model;
	
	/** The bcd. */
	private BoundaryConditionDialog bcd;
	
	/**
	 * Instantiates a new boundary condition table.
	 *
	 * @param lop the lop
	 */
	BoundaryConditionTable(ListOfParameters lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("Boundary Condition",table);
	}
	
	/**
	 * Sets the parameter to list.
	 *
	 * @param lop the new parameter to list
	 */
	private void setParameterToList(ListOfParameters lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!sp.isSetBoundaryCondition()) continue;
			memberList.add(p);
		}
	}
	
	/**
	 * Gets the table model with parameter.
	 *
	 * @return the table model with parameter
	 */
	private MyTableModel getTableModelWithParameter(){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!sp.isSetBoundaryCondition()) continue;
			BoundaryCondition bc = sp.getBoundaryCondition();
			data[i][0] = p.getId();
			data[i][1] = p.isSetValue() ? p.getValue(): null;			
			data[i][2] = p.getConstant();
			data[i][3] = bc.getVariable();
			data[i][4] = SBMLProcessUtil.boundaryIndexToString(bc.getType());
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
	 * Parameter to vector.
	 *
	 * @param p the p
	 * @return the vector
	 */
	private Vector<Object> parameterToVector(Parameter p){
		Vector<Object> v = new Vector<Object>();
		v.add(p.getId());
		v.add(p.getValue());
		v.add(p.getConstant());
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
		BoundaryCondition bc = sp.getBoundaryCondition();
		v.add(bc.getVariable());
		v.add(SBMLProcessUtil.boundaryIndexToString(bc.getType()));
		v.add(bc.isSetCoordinateBoundary() ? bc.getCoordinateBoundary() : bc.getBoundaryDomainType());
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#abcd()
	 */
	@Override
	void add() {
		if(bcd == null)
			bcd = new BoundaryConditionDialog(model);
		
		Parameter p = bcd.showDialog();
		
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
		if(bcd == null)
			bcd = new BoundaryConditionDialog(model);
		
		Parameter p = bcd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;
		
		if(containsDuplicateId(p)){
			errDupID(table);
			return;
		}
			
		memberList.set(index, p);
		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
		
	}
}
