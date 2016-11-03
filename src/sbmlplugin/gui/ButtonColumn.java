package sbmlplugin.gui;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonColumn.
 */
@SuppressWarnings("serial")
public abstract class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor{

	/** The table. */
	JTable table;
	
	/** The column. */
	int column;
	
	/** The edit button. */
	JButton editButton;
	
	/** The render button. */
	JButton renderButton;

	/**
	 * Instantiates a new button column.
	 *
	 * @param table the table
	 * @param column the column
	 */
	public ButtonColumn(JTable table, int column) {
		super();
		this.table = table;
		this.column = column;
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
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int hasFocus, int row) {
		return editButton;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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
