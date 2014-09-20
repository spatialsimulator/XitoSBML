

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



public class NamePanel extends JFrame implements ActionListener{

	ArrayList<Integer> labelList = new ArrayList<Integer>();
	HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
	HashMap<String, Integer> hashSampledValues = new HashMap<String, Integer>();
	HashMap<Integer,Integer> hashLabelNum; 
	DefaultTableModel tableModel;
	JTable table;
	boolean running = false;
	private final String[] domtype = {"Extracellular","Cytosol","Nucleus","Mitochondria","Golgi"}; 
	
	public NamePanel(){
		super("DomainType Namer");
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		setSize(400, 160);
	}

	public NamePanel(ArrayList<Integer> labelList, HashMap<Integer,Integer> hashLabelNum, HashMap<String,Integer> hashDomainTypes, HashMap<String,Integer> hashSampledValues){
		this();
		this.labelList = labelList;
		this.hashLabelNum = hashLabelNum;
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		running = true;
		

		
		//table
		final String[] columnNames = {"Pixel Value","Number of Domains","DomainType"};
		  //set size of table = labelList * 3
		tableModel = new DefaultTableModel(columnNames,0){
			public boolean isCellEditable(int row, int column){				//locks the first and second column 
				if(column == 0 || column == 1){
					return false;
				}else{
					return true;
				}
			}
		};
				
		table = new JTable(tableModel);
		table.setBackground(new Color(169,169,169));
		table.getTableHeader().setReorderingAllowed(false);
		
		//add combobox to third column
		JComboBox cb = new JComboBox(domtype);
		cb.setBorder(BorderFactory.createEmptyBorder());
		TableColumnModel tm = table.getColumnModel();
		TableColumn tc = tm.getColumn(2);
		tc.setCellEditor(new DefaultCellEditor(cb));
		
		//add each pixel, number of domain  into the table
		for(int i = 0; i < labelList.size(); i++){
			String[] tabledata = {labelList.get(i).toString(),hashLabelNum.get(labelList.get(i)).toString()};
			tableModel.addRow(tabledata);
		}
		
		//button
		JPanel keyPanel = new JPanel(new GridLayout(1,2));
		JButton b1 = new JButton("cancel");
		JButton b2 = new JButton("OK");
		keyPanel.add(b1);keyPanel.add(b2);
		b1.addActionListener(this);b2.addActionListener(this);
		
		//set components 
		getContentPane().add(table.getTableHeader(),BorderLayout.NORTH);
		getContentPane().add(keyPanel,BorderLayout.SOUTH);
		getContentPane().add(table,BorderLayout.CENTER);
		setVisible(true);
		
	}

	//sets the datatable to the domaintype and return it
	public HashMap<String, Integer> getDomainTypes(){	
		for(int i = 0; i < labelList.size(); i++){
			hashDomainTypes.put( table.getValueAt(i, 2).toString(), 3);	
		}
		return hashDomainTypes;
	}
	
	//sets the datatable to the sampledvalue and return it
	public HashMap<String, Integer> getSampledValues(){
		for(int i = 0; i < labelList.size(); i++){
			hashSampledValues.put( table.getValueAt(i, 2).toString(), Integer.parseInt(table.getValueAt(i, 0).toString()));
		}
		return hashSampledValues;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		if(input == "cancel"){
			setVisible(false);
			running= false;
			dispose();
		}
		
		if(input == "OK"){
			hashDomainTypes = getDomainTypes();			
			hashSampledValues = getSampledValues();
			setVisible(false);
			running = false;
			dispose();
		}
	}
	
	public static void main(String args[]) throws InterruptedException{
		ArrayList<Integer> labelList = new ArrayList<Integer>();
		labelList.add(new Integer(100));
		labelList.add(new Integer(200));
		labelList.add(new Integer(300));
		HashMap<Integer,Integer> LabelNum = new HashMap<Integer,Integer>();

		
		for(int i = 0 ; i < labelList.size() ; i++){
			LabelNum.put(labelList.get(i),i+5);			
		}	
	    HashMap<String, Integer> DomainTypes = new HashMap<String, Integer>();
	    HashMap<String, Integer> SampledValues = new HashMap<String, Integer>();		
		NamePanel name = new NamePanel(labelList, LabelNum, DomainTypes, SampledValues);

		while (name.running) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {

			}
		}

		System.out.println("main");
		for(Entry<String, Integer> en : DomainTypes.entrySet()){
			System.out.println("main " + en.getKey() + " " + en.getValue());
		}
		
		for(Entry<String, Integer> en : SampledValues.entrySet()){
			System.out.println("main " + en.getKey() + " " + en.getValue());
		}
		
	}

}
