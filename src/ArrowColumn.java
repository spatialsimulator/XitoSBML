
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;


public class ArrowColumn extends ButtonColumn{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	JTable table;
	BasicArrowButton editButton;
	BasicArrowButton renderButton;

	public ArrowColumn(JTable table, int column, int direction){
		super(table, column);
		renderButton = new BasicArrowButton(direction);
		editButton = new BasicArrowButton(direction);
		editButton.setText("arrow");
		setButtons(renderButton, editButton);
	}
}
