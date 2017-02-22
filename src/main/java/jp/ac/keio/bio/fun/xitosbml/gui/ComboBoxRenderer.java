package jp.ac.keio.bio.fun.xitosbml.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 30, 2015
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

    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean hasFocus){
        if (index == -1 && value == null) setText(title);
        else setText(value.toString());
        return this;
    }
}
