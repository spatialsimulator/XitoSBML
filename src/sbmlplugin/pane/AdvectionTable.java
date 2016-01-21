package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.libsbml.AdvectionCoefficient;
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
public class AdvectionTable extends SBaseTable {

	private final String[] header = { "id", "value", "constant", "species", "coordinate"};
	private JTable table;
	private Model model;
	private AdvectionDialog ad;
	
	AdvectionTable(ListOfParameters lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter(lop);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("Advection",table);
	}
	
	private void setParameterToList(ListOfParameters lop){
		long max = lop.size();
		for(int i = 0; i < max; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!sp.isSetAdvectionCoefficient()) continue;
			memberList.add(p);
		}
	}
	
	private MyTableModel getTableModelWithParameter(ListOfParameters lop){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			AdvectionCoefficient ac = sp.getAdvectionCoefficient();
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
	
	private Vector<Object> parameterToVector(Parameter p){
		Vector<Object> v = new Vector<Object>();
		v.add(p.getId());
		v.add(p.getValue());
		v.add(p.getConstant());
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
		AdvectionCoefficient ac = sp.getAdvectionCoefficient();
		v.add(ac.getVariable());
		v.add(SBMLProcessUtil.coordinateIndexToString(ac.getCoordinate()));
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
	 */
	@Override
	void add() {
		if(ad == null)
			ad = new AdvectionDialog(model);
		
		Parameter p = ad.showDialog();
		
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
		if(ad == null)
			ad = new AdvectionDialog(model);
		
		Parameter p = ad.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;
		
		if(containsDuplicateId(p)){
			errDupID(table);
			return;
		}
			
		memberList.set(index, p);
		((MyTableModel)table.getModel()).updateRow(index, parameterToVector(p));
		
	}
}
