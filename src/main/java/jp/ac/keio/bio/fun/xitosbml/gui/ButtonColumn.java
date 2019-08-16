package jp.ac.keio.bio.fun.xitosbml.gui;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * The class ButtonColumn, which creates a button column for JTable used in
 * {@link jp.ac.keio.bio.fun.xitosbml.image.ImageTable}.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public abstract class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor{

	/** The table. */
	JTable table;
	
	/** The column index. */
	int columnIndex;

	/** The edit button. */
	JButton editButton;
	
	/** The render button. */
	JButton renderButton;

	/**
	 * Instantiates a new button column.
	 *
	 * @param table the table
	 * @param columnIndex the column index
	 */
	public ButtonColumn(JTable table, int columnIndex) {
		super();
		this.table = table;
		this.columnIndex = columnIndex;
	}
	
	/**
	 * Sets the buttons.
	 *
	 * @param renderButton the render button
	 * @param editButton the edit button
	 */
	public void setButtons(JButton renderButton, JButton editButton){
		this.renderButton = renderButton;
		this.editButton = editButton;
		editButton.setFocusable(true);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(columnIndex).setCellRenderer(this);
		columnModel.getColumn(columnIndex).setCellEditor(this);
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
