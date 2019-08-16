package jp.ac.keio.bio.fun.xitosbml.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


/**
 * The class MenuColumn, which generates a GUI for XitoSBML.
 * This class is not used in current implementation of XitoSBML.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public class MenuColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

	/** The table. */
	JTable table;
	
	/** The edit button. */
	JComboBox<?> editButton;
	
	/** The render button. */
	JComboBox<?> renderButton;

	
	/**
	 * Instantiates a new menu column.
	 *
	 * @param table the JTable object
	 * @param column the number of columns
	 */
	public MenuColumn(JTable table, int column){
		super();
		this.table = table;
		String[] s = {"From image","From file"};
		renderButton = new JComboBox();
		editButton = new JComboBox(s);
		editButton.setName("getimage");
		editButton.setFocusable(true);
		editButton.addActionListener(this);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
	}
	
	/**
     * Not used in this class
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 * @param arg0 ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {

	}
	
	/**
	 * Overrides javax.swing.CellEditor#getCellEditorValue()
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 * @return the value stored in the editor
	 */
	@Override
	public Object getCellEditorValue() {
		return null;
	}

	/**
	 * Sets an initial value for the editor.
	 * Overrides {@link javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)}
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 *
	 * @param table the JTable object
	 * @param value the value of the cell to be edited
	 * @param isSelected is selected
	 * @param row the row of the cell to be edited
	 * @param column the column of the cell to be edited
	 * @return the component for editing
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return editButton;
	}

	/**
     * Returns the component used for drawing the cell.
	 * Overrides {@link javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)}
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     *
	 * @param table the JTable object
	 * @param value the value of the cell to be edited
	 * @param isSelected is selected
	 * @param hasFocus if true, render cell appropriately.
	 * @param row the row of the cell to be edited
	 * @param column the column of the cell to be edited
	 * @return the component used for drawing the cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		 if (hasFocus) {
             renderButton.setForeground(table.getForeground());
             renderButton.setBackground(UIManager.getColor("Button.background"));
         } else if (isSelected) {
             renderButton.setForeground(table.getSelectionForeground());
              renderButton.setBackground(table.getSelectionBackground());
         } else {
             renderButton.setForeground(table.getForeground());
             renderButton.setBackground(UIManager.getColor("Button.background"));
         }
		return renderButton;
	}

}
