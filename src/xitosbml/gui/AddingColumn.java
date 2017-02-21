package xitosbml.gui;
import javax.swing.JButton;
import javax.swing.JTable;

// TODO: Auto-generated Javadoc
/**
 * The Class AddingColumn.
 */
@SuppressWarnings("serial")
public class AddingColumn extends ButtonColumn {

	/** The table. */
	JTable table;
	
	/** The edit button. */
	JButton editButton;
	
	/** The render button. */
	JButton renderButton;

	
	/**
	 * Instantiates a new adding column.
	 *
	 * @param table the table
	 * @param column the column
	 */
	public AddingColumn(JTable table, int column){
		super(table,column);
		this.table = table;
		renderButton = new JButton("+");
		editButton = new JButton("+");
		editButton.setName("getimage");
		setButtons(renderButton, editButton);
	}
}
