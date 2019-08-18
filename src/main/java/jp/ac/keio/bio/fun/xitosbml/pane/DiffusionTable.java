package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.DiffusionCoefficient;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

/**
 * The class DiffusionTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing diffusion coefficient.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class DiffusionTable extends SBaseTable {

	/** The header of table. */
	private final String[] header = { "id", "value", "constant", "species", "type" , "coordinateReference1","coordinateReference2"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The SBML model. */
	private Model model;
	
	/** The DiffusionDialog, which generates a GUI for creating / editing Diffusion Coefficient. */
	private DiffusionDialog dd;
	
	/**
	 * Instantiates a new diffusion table.
	 *
	 * @param lop the list of parameters
	 */
	DiffusionTable(ListOf<Parameter> lop){
		this.model = lop.getModel();
		list = lop;
		setParameterToList(lop);
		MyTableModel tm = getTableModelWithParameter();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("species", table);
	}
	
	/**
	 * Sets the new parameter (diffusion coefficient) to the list.
	 *
	 * @param lop the new diffusion coefficient to the list
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
	 * Gets the table model.
	 *
	 * @return the table model with parameter
	 */
	private MyTableModel getTableModelWithParameter(){
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
			data[i][4] = dc.getDiffusionKind().name();
			data[i][5] = dc.isSetCoordinateReference1() ? dc.getCoordinateReference1().name() : "";
			data[i][6] = dc.isSetCoordinateReference2() ? dc.getCoordinateReference2().name() : "";
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
	 * Converts Parameter to a Vector. The converted vector will contain parameter information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>boolean:constant</li>
	 *     <li>String:variable</li>
	 *     <li>String:diffusion kind</li>
	 *     <li>String:coordinate kind</li>
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
		DiffusionCoefficient dc = (DiffusionCoefficient) sp.getParamType();
		v.add(dc.getVariable());
		v.add(dc.getDiffusionKind().name());
		v.add(dc.isSetCoordinateReference1() ? dc.getCoordinateReference1().name() : "");
		v.add(dc.isSetCoordinateReference2() ? dc.getCoordinateReference2().name() : "");
		
		return v;
	}

	/**
	 * Adds the Parameter object (diffusion coefficient) to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. DiffusionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	@Override
	void add() throws IllegalArgumentException{
		if(dd == null)
			dd = new DiffusionDialog(model);
		
		Parameter p = dd.showDialog();
		
		if(p == null) return;
			
		memberList.add(p.clone());
		((MyTableModel)table.getModel()).addRow(parameterToVector(p.clone()));	
	}

	/**
	 * Edits the Parameter object (diffusion coefficient) which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. DiffusionDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the diffusion coefficient
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(dd == null)
			dd = new DiffusionDialog(model);
		
		Parameter p = dd.showDialog((Parameter) memberList.get(index));
		
		if(p == null) return;		
			
		memberList.set(index, p);

		// copy contents of DiffusionCoefficient(JTable) to DiffusionCoefficient(Model)
		Parameter diffusion = (Parameter)list.getElementBySId(p.getId());
		SBMLProcessUtil.copyDiffusionCoefficientContents(p, diffusion);

		((MyTableModel)table.getModel()).updateRow(index,parameterToVector(p));
		
	}
}
