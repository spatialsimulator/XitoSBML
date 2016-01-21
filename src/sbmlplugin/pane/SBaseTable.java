package sbmlplugin.pane;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.SBase;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 13, 2016
 */
@SuppressWarnings("serial")
public abstract class SBaseTable {
	protected JScrollPane pane;
	protected JPanel panel;
	protected Color gray = new Color(169, 169, 169);
	protected ArrayList<SBase> memberList = new ArrayList<SBase>();
	protected ListOf list;
	
	abstract void add();

	abstract void edit(int index);
	
	void removeFromList(int index){
		if(index == -1) return;
		String id = memberList.get(index).getId();
		for(int i = 0; i < list.size(); i++)
			if(list.get(i).getId().equals(id)) 
				list.remove(i);
		memberList.remove(index);
	}
	
	void removeSelectedFromTable(JTable table) {
		int row = table.getSelectedRow();
		if(row == -1) return;
		((DefaultTableModel) table.getModel()).removeRow(row);
	}
	
	JScrollPane getPane(){
		return pane;
	}
	
	boolean containsDuplicateId(SBase sbase){
		Boolean bool = false;
		
		for(SBase s: memberList)
			if(s != sbase && s.getId().equals(sbase.getId())) bool = true;

		return bool;
	}

	void errDupID(JTable table){
		JOptionPane.showMessageDialog(table, "Duplicate id", "Error", JOptionPane.PLAIN_MESSAGE);	
	}
	
	void setTableProperties(JTable table){
		setColumnSize(table);
		table.setBackground(gray);
		table.getTableHeader().setReorderingAllowed(false);
	}
	
	void setColumnSize(JTable table) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		Enumeration<TableColumn> e = table.getColumnModel().getColumns();
		while (e.hasMoreElements())
			e.nextElement().setMinWidth(150);
	}
	
	JScrollPane setTableToScroll(String name, JTable table){
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setName(name);
		return scroll;
	}
	
	protected class MyTableModel extends DefaultTableModel {
		/**
		 * @param data
		 * @param header
		 */
		public MyTableModel(Object[][] data, String[] header) {
			super(data, header);
		}

		/**
		 * @param i
		 * @param j
		 */
		public MyTableModel(int i, int j) {
			super(i,j);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
        public void updateRow(int index,Vector<Object> vector)
        {
            for (int i = 0 ; i < vector.size() ; i++)
            {
                setValueAt(vector.get(i), index, i);
            }
        }
	}
}
