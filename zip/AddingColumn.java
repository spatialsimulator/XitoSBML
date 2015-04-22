import javax.swing.JButton;
import javax.swing.JTable;

public class AddingColumn extends ButtonColumn {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JTable table;
	JButton editButton;
	JButton renderButton;

	
	public AddingColumn(JTable table, int column){
		super(table,column);
		this.table = table;
		renderButton = new JButton("+");
		editButton = new JButton("+");
		editButton.setName("getimage");
		setButtons(renderButton, editButton);
	}
}
