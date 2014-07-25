package spatialplugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;



public class NamePanel extends JPanel{
	
	private static final long serialVersionUID = 6963081265241191096L;

	ArrayList<Integer> labelList = new ArrayList<Integer>();
	HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
	HashMap<String, Integer> hashSampledValue = new HashMap<String, Integer>();
	DefaultTableModel tableModel;
	JTable table;
	JFrame frame;
	
	public NamePanel(){
		frame = new JFrame("DomainType Namer");
		setBounds(100, 100, 300, 250);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		setVisible(true);
	}

	public NamePanel(ArrayList<Integer> labelList){
		this();
		this.labelList = labelList;
		
		//button
		JPanel keyPanel = new JPanel(new GridLayout(1,2));
		String[] buttons = {"cancel","OK"};
		
		//table
		String[] columnNames = {"Pixel Value","DomainType","Dimension"};
		tableModel = new DefaultTableModel(columnNames,0);  //set size = labelList * 3
		table = new JTable(tableModel);
		table.setBackground(new Color(169,169,169));
		table.setSelectionBackground(new Color(250,250,250,50));
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);

		frame.getContentPane().add(table.getTableHeader(),BorderLayout.NORTH);
		frame.getContentPane().add(keyPanel,BorderLayout.SOUTH);
		frame.getContentPane().add(table,BorderLayout.CENTER);
		
//		table.setEnabled(false);
		
		//add each pixel into the table
		for(int i = 0; i < labelList.size(); i++){
			String[] tabledata = {labelList.get(i).toString(),Integer.toString(i),Integer.toString(i+3)};
			tableModel.addRow(tabledata);
		}
		
		//button added
		Action[] action = {new buttonKey(buttons[0],table), new buttonKey(buttons[1],table)};
		keyPanel.add(new JButton(action[0]));
		keyPanel.add(new JButton(action[1]));
	
		
	}

	//sets the datatable to the domaintype and return it
	public HashMap<String, Integer> getDomainType(){	
		for(int i = 0; i < labelList.size(); i++){
			hashDomainTypes.put( table.getValueAt(i, 1).toString(),  Integer.parseInt(table.getValueAt(i, 0).toString()));
		}
		return hashDomainTypes;
	}

	//sets the datatable to the sampledvalue and return it
	public HashMap<String, Integer> getSampledValue(){
		for(int i = 0; i < labelList.size(); i++){
			hashSampledValue.put( table.getValueAt(i, 1).toString(), Integer.parseInt(table.getValueAt(i, 2).toString()));
		}
		return hashSampledValue;
	}
	
	public void exit(){
		System.exit(0);
	}
	
	
	public static void main(String args[]){
		ArrayList<Integer> labelList = new ArrayList<Integer>();
		labelList.add(new Integer(100));
		labelList.add(new Integer(200));
		labelList.add(new Integer(300));
		
		NamePanel name = new NamePanel(labelList);
		name.setVisible(true);
		HashMap<String, Integer> type = name.getSampledValue();
		Integer g = type.get("0");
		System.out.print("aaa" + g.toString());
	}


}
