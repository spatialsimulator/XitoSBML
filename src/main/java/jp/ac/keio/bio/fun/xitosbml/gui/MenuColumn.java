package jp.ac.keio.bio.fun.xitosbml.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;


// TODO: Auto-generated Javadoc
/**
 * The Class MenuColumn.
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
	 * @param table the table
	 * @param column the column
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
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {

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
