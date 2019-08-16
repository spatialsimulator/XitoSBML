package jp.ac.keio.bio.fun.xitosbml.gui;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * The class ArrowColumn, which creates an arrow column for JTable used in
 * {@link jp.ac.keio.bio.fun.xitosbml.image.ImageTable}.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
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
	 * @param table the JTable object
	 * @param columnIndex the column index
	 * @param direction the direction of the arrow
	 */
	public ArrowColumn(JTable table, int columnIndex, int direction){
		super(table, columnIndex);
		renderButton = new BasicArrowButton(direction);
		editButton = new BasicArrowButton(direction);
		editButton.setText("arrow");
		setButtons(renderButton, editButton);
	}
}
