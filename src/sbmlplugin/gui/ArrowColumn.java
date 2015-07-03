package sbmlplugin.gui;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;


@SuppressWarnings({ "serial" })
public class ArrowColumn extends ButtonColumn{

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
