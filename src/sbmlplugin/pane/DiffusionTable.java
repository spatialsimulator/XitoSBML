package sbmlplugin.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.DiffusionCoefficient;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class DiffusionTable extends SBaseTable {

	/** The header. */
	private final String[] header = { "id", "value", "constant", "species", "type" , "coordinateReference1","coordinateReference2"};
	
	/** The table. */
	private JTable table;
	
	/** The model. */
	private Model model;
	
	/** The dd. */
	private DiffusionDialog dd;
	
	/**
	 * Instantiates a new diffusion table.
	 *
	 * @param lop the lop
	 */
	DiffusionTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter(lop);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("species", table);
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
			if(!(sp.getParamType() instanceof DiffusionCoefficient)) continue;
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
		Object[][] data  = new Object[(int) max][header.length];
		for(int i = 0; i < max; i++){
			Parameter p = (Parameter) memberList.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			if(!(sp.getParamType() instanceof DiffusionCoefficient)) continue;
			DiffusionCoefficient dc = (DiffusionCoefficient) sp.getParamType();
			data[i][0] = p.getId();
			data[i][1] = p.isSetValue() ? p.getValue(): null;			
			data[i][2] = p.getConstant();
			data[i][3] = dc.getVariable();
			data[i][4] = SBMLProcessUtil.diffTypeIndexToString(dc.getDiffusionKind());
			data[i][5] = SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference1());
			data[i][6] = SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference2());
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
				case 5: // coordinate1
				case 6: // coordinate2
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
		DiffusionCoefficient dc = (DiffusionCoefficient) sp.getParamType();
		v.add(dc.getVariable());
		v.add(SBMLProcessUtil.diffTypeIndexToString(dc.getDiffusionKind()));
		v.add(SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference1()));
		v.add(SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference2()));
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
	 */
	@Override
	void add() throws IllegalArgumentException{
		if(dd == null)
			dd = new DiffusionDialog(model);
		
		Parameter p = dd.showDialog();
		
		if(p == null) return;
			
		memberList.add(p);
		((MyTableModel)table.getModel()).addRow(parameterToVector(p));
	
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(dd == null)
			dd = new DiffusionDialog(model);
		
		Parameter p = dd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;		
			
		memberList.set(index, p);
		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
		
	}
}
