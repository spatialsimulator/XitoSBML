package jp.ac.keio.bio.fun.xitosbml.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * The class ComboBoxRenderer, which renders a combobox.
 * This class is not used in current implementation of XitoSBML.
 * Date Created: Aug 30, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The title. */
	private String title;

    /**
     * Instantiates a new combo box renderer.
     *
     * @param title the title
     */
    public ComboBoxRenderer(String title){
        this.title = title;
    }

    /**
     * Overrides {@link javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)}
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     *
     * @param list the JList object
     * @param value the value returned by list.getModel().getElementAt(index).
     * @param index cells index
     * @param isSelected is cell selected
     * @param hasFocus has focus
     * @return A component whose paint() method will render the specified value.
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean hasFocus){
        if (index == -1 && value == null) setText(title);
        else setText(value.toString());
        return this;
    }
}
