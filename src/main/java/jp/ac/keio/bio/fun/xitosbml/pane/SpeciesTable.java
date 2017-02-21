package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 13, 2016
 */
public class SpeciesTable extends SBaseTable{
	
	/** The header. */
	private final String[] header = { "id","initial","quantity", "compartment", "substanceUnits","hasOnlySubstanceUnits","boundaryCondition","constant"};
	
	/** The table. */
	private JTable table;
	
	/** The sd. */
	private SpeciesDialog sd;
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new species table.
	 *
	 * @param los the los
	 */
	SpeciesTable(ListOf<Species> los){
		this.model = los.getModel();
		list = los;
		setSpeciesToList(los);
		MyTableModel tm = getTableModelWithSpecies(los);
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("species",table);
	}
	
	/**
	 * Sets the species to list.
	 *
	 * @param los the new species to list
	 */
	private void setSpeciesToList(ListOf<Species> los){
		long max = los.size();
		for(int i = 0; i < max; i++){
			Species s = los.get(i).clone();
			memberList.add(s.clone());
		}
	}
	
	/**
	 * Gets the table model with species.
	 *
	 * @param los the los
	 * @return the table model with species
	 */
	private MyTableModel getTableModelWithSpecies(ListOf<Species> los){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Species s = (Species) memberList.get(i);
			data[i][0] = s.getId();
			if(s.isSetInitialAmount()){
				data[i][1] = "amount";
				data[i][2] = s.getInitialAmount();
			} else if(s.isSetInitialConcentration()){
				data[i][1] = "concentration";
				data[i][2] = s.getInitialConcentration();
			}
			data[i][3] = s.getCompartment();
			data[i][4] = s.getSubstanceUnits();
			data[i][5] = s.getHasOnlySubstanceUnits();
			data[i][6] = s.getBoundaryCondition();
			data[i][7] = s.getConstant();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
				case 1: // initial
					return String.class;
				case 2: // quantity
					return Double.class;
				case 3: // Compartment
				case 4: // substance unit
				case 5: // hasOnlySubstanceUnits
				case 6: // boundaryCondition
				case 7:	// constant
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	/**
	 * Species to vector.
	 *
	 * @param s the s
	 * @return the vector
	 */
	private Vector<Object> speciesToVector(Species s){
		Vector<Object> v = new Vector<Object>();
		v.add(s.getId());
		if(s.isSetInitialAmount()){
			v.add("amount");
			v.add(s.getInitialAmount());
		} else if(s.isSetInitialConcentration()){
			v.add("concentration");
			v.add(s.getInitialConcentration());
		}
		v.add(s.getCompartment());
		v.add(s.getSubstanceUnits());
		v.add(s.getHasOnlySubstanceUnits());
		v.add(s.getBoundaryCondition());
		v.add(s.getConstant());
		
		return v;
	}
	
	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#add()
	 */
	@Override
	void add() throws IllegalArgumentException{
		if(sd == null)
			sd = new SpeciesDialog(model);
		Species s = sd.showDialog();
		if(s == null) return;
			
		memberList.add(s.clone());
		((MyTableModel)table.getModel()).addRow(speciesToVector(s.clone()));
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(sd == null)
			sd = new SpeciesDialog(model);
		Species s = sd.showDialog((Species) memberList.get(index));
		
		if(s == null) return;
		
		memberList.set(index, s);
		((MyTableModel)table.getModel()).updateRow(index, speciesToVector(s));
		
	}
}
