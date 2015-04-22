import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public abstract class ButtonColumn extends AbstractCellEditor implements TableCellRenderer,
		TableCellEditor{

	/**
 * 
 */
	private static final long serialVersionUID = 1L;

	JTable table;
	int column;
	JButton editButton;
	JButton renderButton;

	public ButtonColumn(JTable table, int column) {
		super();
		this.table = table;
		this.column = column;
	}
	
	public void setButtons(JButton renderButton, JButton editButton){
		this.renderButton = renderButton;
		this.editButton = editButton;
		editButton.setFocusable(true);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
	}
	
	@Override
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int hasFocus, int row) {
		// TODO Auto-generated method stub
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
		// renderButton.setText("up");
		return renderButton;
	}

}
