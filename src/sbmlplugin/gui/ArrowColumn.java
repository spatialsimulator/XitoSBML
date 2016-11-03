package sbmlplugin.gui;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;


// TODO: Auto-generated Javadoc
/**
 * The Class ArrowColumn.
 */
@SuppressWarnings({ "serial" })
public class ArrowColumn extends ButtonColumn{

	/** The table. */
	JTable table;
	
	/** The edit button. */
	BasicArrowButton editButton;
	
	/** The render button. */
	BasicArrowButton renderButton;

	/**
	 * Instantiates a new arrow column.
	 *
	 * @param table the table
	 * @param column the column
	 * @param direction the direction
	 */
	public ArrowColumn(JTable table, int column, int direction){
		super(table, column);
		renderButton = new BasicArrowButton(direction);
		editButton = new BasicArrowButton(direction);
		editButton.setText("arrow");
		setButtons(renderButton, editButton);
	}
}
