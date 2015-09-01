package sbmlplugin.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 30, 2015
 */
public class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	private String title;

    public ComboBoxRenderer(String title){
        this.title = title;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus){
        if (index == -1 && value == null) setText(title);
        else setText(value.toString());
        return this;
    }
}
