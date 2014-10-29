

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;



public class NamePanel extends JFrame implements ActionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Integer> labelList;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValues;
	private DefaultTableModel tableModel;
	private JTable table;
	boolean running = false;
	private final String[] domtype = {"Extracellular","Cytosol","Nucleus","Mitochondria","Golgi"}; 
	private HashMap<Integer, Boolean> appearingDom; 
	
	public NamePanel(){
		super("DomainType Namer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		//setDefaultCloseOperation(EXIT_ON_CLOSE);	
		setSize(400, 240);
	}

	public NamePanel(ArrayList<Integer> labelList, HashMap<Integer,Integer> hashLabelNum, HashMap<String,Integer> hashDomainTypes, HashMap<String,Integer> hashSampledValues){
		this();
		this.labelList = labelList;
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		running = true;

		//data sets for the table
		final String[] columnNames = {"Pixel Value","Number of Domains","DomainType","View"};
//		final String[] columnNames = {"Pixel Value","Number of Domains","DomainType"};
		Object[][] data = new Object[labelList.size()][4];
	//	Object[][] data = new Object[labelList.size()][3];
		appearingDom = new HashMap<Integer, Boolean>();
		for(int i = 0 ; i < labelList.size() ; i++){
			data[i][0]= labelList.get(i).toString();
			data[i][1] = hashLabelNum.get(labelList.get(i)).toString();
			data[i][2] = "";
			data[i][3] = true;
		appearingDom.put(labelList.get(i), true);
		}
		
		//table
		tableModel = new DefaultTableModel(data,columnNames){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column){				//locks the first and second column 
				if(column == 0 || column == 1)
					return false;
				else  							
					return true;
			}
		};
				
		//table setting 
		table = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public Class<?> getColumnClass(int Column){
				switch(Column){
				case 0:
				case 1:
					return Integer.class;
				case 2:
					return JComboBox.class;
				case 3:
					return Boolean.class;
				default :
					return Boolean.class;
				}
			}
		};
		table.setBackground(new Color(169,169,169));
		table.getTableHeader().setReorderingAllowed(false);
		
		//add combobox to third column
		JComboBox cb = new JComboBox(domtype);
		cb.setBorder(BorderFactory.createEmptyBorder());
		TableColumnModel tm = table.getColumnModel();
		TableColumn tc = tm.getColumn(2);
		tc.setCellEditor(new DefaultCellEditor(cb));

		//button
		JButton b = new JButton("OK");
		b.addActionListener(this);
		
		//mouse
		table.addMouseListener(this);
		table.setCellSelectionEnabled(true);
		
		//scrollbar
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//set components 
		getContentPane().add(table.getTableHeader(), BorderLayout.NORTH);
		getContentPane().add(b, BorderLayout.SOUTH);
		getContentPane().add(scroll, BorderLayout.CENTER);	
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
	public  void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		if(input == "cancel"){
			setVisible(false);
//			dispose();
		}
		
		if(input == "OK"){
			hashDomainTypes = getDomainTypes();			
			hashSampledValues = getSampledValues();
			setVisible(false);
		//		dispose();
		}
		dispose();
	//	running = false;
	}
	
	public static void main(String args[]) throws InterruptedException{
		ArrayList<Integer> labelList = new ArrayList<Integer>();
		labelList.add(new Integer(100));
		labelList.add(new Integer(200));
		labelList.add(new Integer(300));
		labelList.add(new Integer(400));
		labelList.add(new Integer(200));
		labelList.add(new Integer(300));
		labelList.add(new Integer(400));
		HashMap<Integer,Integer> LabelNum = new HashMap<Integer,Integer>();

		
		for(int i = 0 ; i < labelList.size() ; i++){
			LabelNum.put(labelList.get(i),i+5);			
		}	
	    HashMap<String, Integer> DomainTypes = new HashMap<String, Integer>();
	    HashMap<String, Integer> SampledValues = new HashMap<String, Integer>();		
		new NamePanel(labelList, LabelNum, DomainTypes, SampledValues);
		
		System.out.println("main");
		for(Entry<String, Integer> en : DomainTypes.entrySet()){
			System.out.println("main " + en.getKey() + " " + en.getValue());
		}
		
		for(Entry<String, Integer> en : SampledValues.entrySet()){
			System.out.println("main " + en.getKey() + " " + en.getValue());
		}
		
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		JTable table = (JTable)e.getSource();
		if(table.getSelectedColumn() == 3){
			int temp = Integer.parseInt((String) table.getValueAt(table.getSelectedRow(),0));
			System.out.println(temp);
			boolean bool = Boolean.valueOf((Boolean) table.getValueAt(table.getSelectedRow(),3));
			System.out.println(table.getValueAt(table.getSelectedRow(),3));
			appearingDom.put(temp , !bool);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
