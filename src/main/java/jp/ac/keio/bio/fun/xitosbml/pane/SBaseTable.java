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

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 13, 2016
 */
@SuppressWarnings("serial")
public abstract class SBaseTable {
	
	/** The pane. */
	protected JScrollPane pane;
	
	/** The panel. */
	protected JPanel panel;
	
	/** The gray. */
	protected Color gray = new Color(169, 169, 169);
	
	/** The member list. */
	protected ListOf<SBase> memberList = new ListOf<SBase>(3,1);
	
	/** The list. */
	protected ListOf<?> list;
	
	/**
	 * Adds the.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 * @throws IdentifierException the identifier exception
	 */
	abstract void add() throws IllegalArgumentException, ParseException,IdentifierException;

	/**
	 * Edits the.
	 *
	 * @param index the index
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws ParseException the parse exception
	 * @throws IdentifierException the identifier exception
	 */
	abstract void edit(int index) throws IllegalArgumentException, ParseException, IdentifierException;
	
	/**
	 * Removes the from list.
	 *
	 * @param index the index
	 */
	void removeFromList(int index){
		if(index == -1) return;
		SBase id = memberList.get(index);
		list.remove(id);
		memberList.remove(index);
	}
	
	/**
	 * Removes the selected from table.
	 *
	 * @param table the table
	 */
	void removeSelectedFromTable(JTable table) {
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
	 * Sets the table properties.
	 *
	 * @param table the new table properties
	 */
	void setTableProperties(JTable table){
		setColumnSize(table);
		table.setBackground(gray);
		table.getTableHeader().setReorderingAllowed(false);
	}
	
	/**
	 * Sets the column size.
	 *
	 * @param table the new column size
	 */
	void setColumnSize(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		Enumeration<TableColumn> e = table.getColumnModel().getColumns();
		while (e.hasMoreElements())
			e.nextElement().setMinWidth(150);
	}
	
	/**
	 * Sets the table to scroll.
	 *
	 * @param name the name
	 * @param table the table
	 * @return the j scroll pane
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
		 * Instantiates a new my table model.
		 *
		 * @param data the data
		 * @param header the header
		 */
		public MyTableModel(Object[][] data, String[] header) {
			super(data, header);
		}

		/**
		 * Instantiates a new my table model.
		 *
		 * @param i the i
		 * @param j the j
		 */
		public MyTableModel(int i, int j) {
			super(i,j);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
        /**
         * Update row.
         *
         * @param index the index
         * @param vector the vector
         */
        public void updateRow(int index,Vector<Object> vector)
        {
            for (int i = 0 ; i < vector.size() ; i++)
            {
                setValueAt(vector.get(i), index, i);
            }
        }
	}
}
