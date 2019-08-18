package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.AdvectionCoefficient;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

/**
 * The class AdvectionTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing advection coefficient.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class AdvectionTable extends SBaseTable {

	/** The header of table. */
	private final String[] header = { "id", "value", "constant", "species", "coordinate"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The SBML model. */
	private Model model;
	
	/** The AdvectionDialog, which generates a GUI for creating / editing Advection Coefficient. */
	private AdvectionDialog ad;
	
	/**
	 * Instantiates a new advection table.
	 *
	 * @param lop the list of parameters
	 */
	AdvectionTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("Advection",table);
	}
	
	/**
	 * Sets the new parameter (advection coefficient) to the list.
	 *
	 * @param lop the new advection coefficient to the list
	 */
	private void setParameterToList(ListOf<Parameter> lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!(sp.getParamType() instanceof AdvectionCoefficient)) continue;
			memberList.add(p.clone());
		}
	}
	
	/**
	 * Gets the table model with given parameter (advection coefficient).
	 *
	 * @return the table model with parameter
	 */
	private MyTableModel getTableModelWithParameter(){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!(sp.getParamType() instanceof AdvectionCoefficient)) continue;
			AdvectionCoefficient ac = (AdvectionCoefficient) sp.getParamType();
			data[i][0] = p.getId();
			data[i][1] = p.isSetValue() ? p.getValue(): null;			
			data[i][2] = p.getConstant();
			data[i][3] = ac.getVariable();
			data[i][4] = ac.getCoordinate().name();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
					return String.class;
				case 1:	// value
					return Double.class;
				case 2: // constant
				case 3:	// species
				case 4:	// coordinate
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
	 *     <li>String:coordinate name</li>
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
		AdvectionCoefficient ac = (AdvectionCoefficient) sp.getParamType();
		v.add(ac.getVariable());
		v.add(ac.getCoordinate().name());
		
		return v;
	}
	
	/**
	 * Adds the Parameter object (advection coefficient) to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. AdvectionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException
	 */
	@Override
	void add() throws IllegalArgumentException{
		if(ad == null)
			ad = new AdvectionDialog(model);
		
		Parameter p = ad.showDialog();
		if(p == null) return;
		
		memberList.add(p.clone());
		((MyTableModel)table.getModel()).addRow(parameterToVector(p));
	
	}

	/**
	 * Edits the Parameter object (advection coefficient) which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. AdvectionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
     *
	 * @param index the index of the advection coefficient
	 * @throws IllegalArgumentException
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(ad == null)
			ad = new AdvectionDialog(model);
		
		Parameter p = ad.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;

		memberList.set(index, p);

		// copy contents of AdvectionCoefficient(JTable) to AdvectionCoefficient(Model)
		Parameter adv = (Parameter)list.getElementBySId(p.getId());
		SBMLProcessUtil.copyAdvectionCoefficientContents(p, adv);

		((MyTableModel)table.getModel()).updateRow(index, parameterToVector(p));	
	}
}
