package jp.ac.keio.bio.fun.xitosbml.pane;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.text.parser.ParseException;

/**
 * The class SBaseTable, which is an abstract class and be used to
 * create tables for SBase objects (Species, Reaction, Parameter, etc.).
 * The inherited class should implement add() and edit() methods.
 * Date Created: Jan 13, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public abstract class SBaseTable {
	
	/** The JScrollPane object. */
	protected JScrollPane pane;
	
	/** The JPanel object. */
	protected JPanel panel;
	
	/** The gray color. */
	protected Color gray = new Color(169, 169, 169);
	
	/** The member list of SBML Level 3 Version 1 SBase objects. */
	protected ListOf<SBase> memberList = new ListOf<SBase>(3,1);
	
	/** The list of Object. */
	protected ListOf<?> list;
	
	/**
	 * Adds the SBase object to a table. This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. JDialog).
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 * @throws IdentifierException the identifier exception
	 */
	abstract void add() throws IllegalArgumentException, ParseException,IdentifierException;

	/**
	 * Edits the SBase object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. SpeciesDialog, ReactionDialog, ParameterDialog, etc.).
	 *
	 * @param index the index of the SBase object
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 * @throws IdentifierException the identifier exception
	 */
	abstract void edit(int index) throws IllegalArgumentException, ParseException, IdentifierException;
	
	/**
	 * Removes the SBase object specified by an index from the memberlist.
	 *
	 * @param index the index of SBase object
	 */
	SBase removeFromList(int index){
		if(index == -1) return null;
		SBase id = memberList.get(index);
		list.remove(id);
		return memberList.remove(index);
	}
	
	/**
	 * Removes the selected row from table.
	 *
	 * @param table the table
	 */
	void removeSelectedRowFromTable(JTable table) {
		int row = table.getSelectedRow();
		if(row == -1) return;
		((DefaultTableModel) table.getModel()).removeRow(row);
	}
	
	/**
	 * Gets the pane.
	 *
	 * @return the pane
	 */
	JScrollPane getPane(){
		return pane;
	}
		
	/**
	 * Sets the table properties. This method will set the following properties.
	 * <ul>
	 *     <li>column size</li>
	 *     <li>background color</li>
	 *     <li>disable reordering</li>
	 * </ul>
	 *
	 * @param table the table object
	 */
	void setTableProperties(JTable table){
		setColumnSize(table);
		table.setBackground(gray);
		table.getTableHeader().setReorderingAllowed(false);
	}
	
	/**
	 * Sets all the column min width to 150.
	 *
	 * @param table the table object
	 */
	void setColumnSize(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		Enumeration<TableColumn> e = table.getColumnModel().getColumns();
		while (e.hasMoreElements())
			e.nextElement().setMinWidth(150);
	}
	
	/**
	 * Create scroll pane and sets to the table.
	 *
	 * @param name the name of scroll pane
	 * @param table the table object
	 * @return the JCcrollPane object
	 */
	JScrollPane setTableToScroll(String name, JTable table){
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setName(name);
		return scroll;
	}
	
	/**
	 * The Class MyTableModel.
	 */
	protected class MyTableModel extends DefaultTableModel {
		
		/**
		 * Instantiates a new my table model with data and header.
		 *
		 * @param data the 2D array of Object
		 * @param header the array of string for header
		 */
		public MyTableModel(Object[][] data, String[] header) {
			super(data, header);
		}

		/**
		 * Instantiates a new my table model with row count and column count.
		 *
		 * @param rowCount           the number of rows the table holds
		 * @param columnCount        the number of columns the table holds
		 */
		public MyTableModel(int rowCount, int columnCount) {
			super(rowCount,columnCount);
		}

		/**
         * Returns false regardless of parameter values.
		 * @param row the row whose value is to be queried
		 * @param column the column whose value is to be queried
		 * @return false
		 */
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
        /**
         * Update row. The element number of vector should be identical with column number.
         *
         * @param row the row whose value will be updated
         * @param vector the data for new row
         */
        public void updateRow(int row,Vector<Object> vector)
        {
            for (int i = 0 ; i < vector.size() ; i++)
            {
                setValueAt(vector.get(i), row, i);
            }
        }
	}
}
