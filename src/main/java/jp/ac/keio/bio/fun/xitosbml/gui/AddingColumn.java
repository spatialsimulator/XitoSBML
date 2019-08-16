package jp.ac.keio.bio.fun.xitosbml.gui;
import javax.swing.JButton;
import javax.swing.JTable;

/**
 * The class AddingColumn, which creates an adding column for JTable used in
 * {@link jp.ac.keio.bio.fun.xitosbml.image.ImageTable}.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
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
	 * @param table the JTable object
	 * @param columnIndex the column index
	 */
	public AddingColumn(JTable table, int columnIndex){
		super(table,columnIndex);
		this.table = table;
		renderButton = new JButton("+");
		editButton = new JButton("+");
		editButton.setName("getimage");
		setButtons(renderButton, editButton);
	}
}
