package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.AdvectionCoefficient;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class AdvectionTable extends SBaseTable {

	/** The header. */
	private final String[] header = { "id", "value", "constant", "species", "coordinate"};
	
	/** The table. */
	private JTable table;
	
	/** The model. */
	private Model model;
	
	/** The ad. */
	private AdvectionDialog ad;
	
	/**
	 * Instantiates a new advection table.
	 *
	 * @param lop the lop
	 */
	AdvectionTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter(lop);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("Advection",table);
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
			if(!(sp.getParamType() instanceof AdvectionCoefficient)) continue;
			memberList.add(p.clone());
		}
	}
	
	/**
	 * Gets the table model with parameter.
	 *
	 * @param lop the lop
	 * @return the table model with parameter
	 */
	private MyTableModel getTableModelWithParameter(ListOf<Parameter> lop){
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
			data[i][4] = SBMLProcessUtil.coordinateIndexToString(ac.getCoordinate());
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
		AdvectionCoefficient ac = (AdvectionCoefficient) sp.getParamType();
		v.add(ac.getVariable());
		v.add(SBMLProcessUtil.coordinateIndexToString(ac.getCoordinate()));
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
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

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
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
