package sbmlplugin.gui;
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


@SuppressWarnings("serial")
public class MenuColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

	JTable table;
	JComboBox editButton;
	JComboBox renderButton;

	
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
	
	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int hasFocus, int row) {
		return editButton;
	}

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
