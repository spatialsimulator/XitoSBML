package sbmlplugin.gui;

import ij.gui.MessageDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamAndSpecies.
 */
@SuppressWarnings("serial")
public class ParamAndSpecies extends JFrame implements ActionListener, FocusListener{

	static {
		try{
			System.loadLibrary("sbmlj");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** The model. */
	private Model model;
	
	/** The lop. */
	private ListOfParameters lop; 
	
	/** The los. */
	private ListOfSpecies los;
	
	/** The init row. */
	private final int initRow = 1;
	
	/** The table model. */
	private DefaultTableModel tableModel;
	
	/** The table. */
	private JTable table;
	
	/**
	 * Instantiates a new param and species.
	 */
	public ParamAndSpecies(){
		super("Parameter and Species Table");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(480, 200);
		setResizable(false);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}
	
	/** The header. */
	private final String[] header = {"Parameter","Species"};
	
	/**
	 * Instantiates a new param and species.
	 *
	 * @param model the model
	 */
	public ParamAndSpecies(Model model){
		this();
		this.model = model;
		this.lop = model.getListOfParameters();
		this.los = model.getListOfSpecies();
		
		Object[][] data = new Object[initRow][header.length];
		tableModel = new DefaultTableModel(data, header){
			private static final long serialVersionUID = 1L;
		
			public boolean isCellEditable(int row, int column){	
					return false;
			}
		};
				
		//table setting 
		table = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public Class<?> getColumnClass(int Column){
				switch (Column) {
				case 0:
				case 1:
					return String.class;
				default:
					return Boolean.class;
				}
			}
		};
		table.setBackground(new Color(169,169,169));
		table.getTableHeader().setReorderingAllowed(false);
		addFocusListener(this);
		updateTable();
		
		//scrollbar
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
		//button
		JButton ok = new JButton("OK"), plus = new JButton("+"), minus = new JButton("-");
		ok.addActionListener(this); plus.addActionListener(this); minus.addActionListener(this);
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(plus); p2.add(minus);
		p2.add(Box.createRigidArea(new Dimension(200, 0))); p2.add(ok);
		
		//set components 
		getContentPane().add(p2, BorderLayout.PAGE_END);
		getContentPane().add(scroll, BorderLayout.CENTER);	

		setVisible(true);
	}
	
	/** The parameter. */
	private final int PARAMETER = 0;
	
	/** The species. */
	private final int SPECIES = 1;
	
	/**
	 * Removes the cell.
	 *
	 * @param row the row
	 * @param column the column
	 */
	private void removeCell(int row, int column){
		String name = (String) table.getValueAt(row, column);
		if(column == PARAMETER){
			lop.remove(name);	
		}
			
		if(column == SPECIES){
			los.remove(name);
		}
		
		tableModel.setValueAt("", row, column);
		updateTable();
	}
	
	/**
	 * Update table.
	 */
	private void updateTable(){
		long pSize = lop.size();
		long sSize = los.size();
		long listSize = Math.max(pSize, sSize);
		
		while(listSize > table.getRowCount())
			tableModel.addRow(new Object[]{"",""});
		
		for(int i = 0 ; i < table.getRowCount() ; i++){
			if (i < pSize) table.setValueAt(lop.get(i).getId(), i, PARAMETER);
			else table.setValueAt("", i, PARAMETER);
			
			if (i < sSize) table.setValueAt(los.get(i).getId(), i, SPECIES);
			else table.setValueAt("", i, SPECIES);
		}
		validate();
	}
	
	/** The is running. */
	private boolean isRunning = true;
	
	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning(){
		return isRunning;
	}

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("mem_diff.xml");
		new ParamAndSpecies( d.getModel());
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		
		if(input.contentEquals("+")){
			new Adder(model);
		}
		
		if(input.contentEquals("-")){
			int row = table.getSelectedRow();
			int column = table.getSelectedColumn();
			if( (row >= 0 || column >= 0) && table.getValueAt(row, column) != null)
				removeCell(row, column);
		}
		
		if(input.equals("OK")){ 
			if(los.size() > 0 && lop.size() > 0){
				isRunning = false;
				dispose();
				return;
			}else {
				new MessageDialog(new Frame(), "Error", "Must add at least one parameter and species");
			}
		}
			
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent f) {
		updateTable();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent arg0) {
		
	}

}
