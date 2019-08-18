package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

/**
 * The class SpeciesTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing species.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 13, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SpeciesTable extends SBaseTable{
	
	/** The header of table. */
	private final String[] header = { "id","initial","quantity", "compartment", "substanceUnits","hasOnlySubstanceUnits","boundaryCondition","constant"};
	
	/** The JTable object. */
	private JTable table;
	
	/** The SpeciesDialog, which generates a GUI for creating / editing Species. */
	private SpeciesDialog sd;
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new species table.
	 *
	 * @param los the list of species
	 */
	SpeciesTable(ListOf<Species> los){
		this.model = los.getModel();
		list = los;
		setSpeciesToList(los);
		MyTableModel tm = getTableModelWithSpecies();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("species",table);
	}
	
	/**
	 * Sets the new species to the list.
	 *
	 * @param los the new species to the list
	 */
	private void setSpeciesToList(ListOf<Species> los){
		long max = los.size();
		for(int i = 0; i < max; i++){
			Species s = los.get(i).clone();
			memberList.add(s.clone());
		}
	}
	
	/**
	 * Gets the table model.
	 *
	 * @return the table model with species
	 */
	private MyTableModel getTableModelWithSpecies(){
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
	 * Converts Species to a Vector. The converted vector will contain species information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:initialAmount or initialConcentration</li>
	 *     <li>String:compartment</li>
	 *     <li>Kind:substance units</li>
	 *     <li>boolean:has only substance units</li>
	 *     <li>boolean:boundary condition</li>
	 *     <li>boolean:constant</li>
	 * </ul>
	 *
	 * @param s the JSBML Species object
	 * @return the converted vector
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
	
	/**
	 * Adds the Species object to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. SpeciesDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException the illegal argument exception
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

	/**
	 * Edits the Species object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. SpeciesDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the species
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(sd == null)
			sd = new SpeciesDialog(model);
		Species s = sd.showDialog((Species) memberList.get(index));
		
		if(s == null) return;
		
		memberList.set(index, s);

		// copy contents of Species(JTable) to Species(Model)
		Species sp = (Species) list.getElementBySId(s.getId());
		SBMLProcessUtil.copySpeciesContents(s, sp);

		((MyTableModel)table.getModel()).updateRow(index, speciesToVector(s));
		
	}
}
