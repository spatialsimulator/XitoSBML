
import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;




public class ImageExplorer extends JFrame implements ActionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValues;
	private DefaultTableModel tableModel;
	private JTable table;
	private final String[] domtype = {"Nucleus","Mitochondria","Golgi","Cytosol"};
	private final String[] columnNames = {"Domain Type","Image","Add","Up","Down"};
	private HashMap<String,ImagePlus> hashDomFile;
	private FileInfo compoInfo;
	private Integer selectedRow = null;
	private Integer selectedColumn = null;
	private boolean wasCanceled;
	
	public ImageExplorer(){
		super("DomainType Namer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setResizable(false);
		setBounds(100,100,500,240);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}
	
	public ImageExplorer(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues){
		this();
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		hashDomFile = new HashMap<String, ImagePlus>();
		
		//data sets for the table
		Object[][] data = new Object[domtype.length][5];
		for(int i = 0 ; i < domtype.length ; i++){
			data[i][0] = domtype[i];
		}
		
		//table
		tableModel = new DefaultTableModel(data,columnNames){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column){	
				if(column == 1)
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
				switch (Column) {
				case 0:
				case 1:
					return String.class;
				case 2:
					return JButton.class;
				case 3:
				case 4:
					return BasicArrowButton.class;
				default:
					return Boolean.class;
				}
			}
		};
		table.setBackground(new Color(169,169,169));
		table.getTableHeader().setReorderingAllowed(false);

		//mouse
		table.addMouseListener(this);
		table.setCellSelectionEnabled(true);
		
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
		p2.add(Box.createRigidArea(new Dimension(250, 0))); p2.add(ok);
		
		//set components 
		getContentPane().add(p2, BorderLayout.PAGE_END);
		getContentPane().add(scroll, BorderLayout.CENTER);	

		//arrow column
		new ArrowColumn(table, 3, BasicArrowButton.NORTH);
		new ArrowColumn(table, 4, BasicArrowButton.SOUTH);
		new ButtonColumn(table, 2);
		
		TableColumn column = (TableColumn)table.getColumnModel().getColumn(2);
		column.setMaxWidth(50);
		column = (TableColumn)table.getColumnModel().getColumn(3);
		column.setMaxWidth(50);
		column = (TableColumn)table.getColumnModel().getColumn(4);
		column.setMaxWidth(50);
			
		setVisible(true);
	}

	//sets the datatable to the domaintype and return it
	public HashMap<String, Integer> getDomainTypes(){	
		for(Entry<String, ImagePlus> e : hashDomFile.entrySet()){
			hashDomainTypes.put( e.getKey().toString(), 3);	
		}
		hashDomainTypes.put("Extracellular", 3);
		return hashDomainTypes;
	}
	
	//sets the datatable to the sampledvalue and return it
	public HashMap<String, Integer> getSampledValues(){
		int pixel = 255;
		int interval = 255 / hashDomFile.size();
		for(int i = 0 ; i < table.getRowCount() ; i++){
			String s = (String) table.getValueAt(i, 0);
			if(hashDomFile.containsKey(s)){
				hashSampledValues.put(s, pixel);
				pixel -= interval;
			}
		}
		hashSampledValues.put("Extracellular", 0);
		System.out.println(hashSampledValues.toString());
		return hashSampledValues;
	}
	
	private boolean checkAllImages() {
		Iterator<String> domNames = hashDomFile.keySet().iterator();
		if(!domNames.hasNext()) return false;
		ImagePlus compoImg = hashDomFile.get(domNames.next());
		compoInfo = compoImg.getFileInfo();
		ImagePlus temp;
		while(domNames.hasNext()) {
			temp = hashDomFile.get(domNames.next());
			if (!compImage(compoImg, temp)) {
				new MessageDialog(new Frame(), "Error", "Image size must be same for all input image");
				return false;
			}
		}
		return true;
	}
	
	private boolean compImage(ImagePlus compoImg, ImagePlus img) {
		boolean width = compoImg.getWidth() == img.getWidth();
		boolean height = compoImg.getHeight() == img.getHeight();
		boolean depth = compoImg.getStackSize() == img.getStackSize();
		FileInfo imgInfo = img.getFileInfo();
		boolean voxx, voxy, voxz;
		
		try {
			voxx = cmpsize(compoInfo.pixelWidth, imgInfo.pixelWidth);
			voxy = cmpsize(compoInfo.pixelHeight, imgInfo.pixelHeight);
			voxz = cmpsize(compoInfo.pixelDepth, imgInfo.pixelDepth);
		} catch (NullPointerException e) {
			voxx = true;voxy = true;voxz = true;
		}
		System.out.println(voxx + " " + voxy +" " + voxz);
		
		return width && height && depth && voxx && voxy && voxz;
	}
	
	private boolean cmpsize(double org, double in){
		double num = org - in;
		
		if(Math.abs(num) < 0.01)
			return true;
			
		return false;
	}
	
	public static void main(String[] args){	
		HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
		HashMap<String, Integer> hashSampledValues = new HashMap<String, Integer>();
		new ImageExplorer(hashDomainTypes, hashSampledValues);
		
	}

	public void importFile(int column, int row, ImagePlus img){
		table.setValueAt(img.getTitle(), row, 1);
		hashDomFile.put(table.getValueAt(row, 0).toString(), img);	
	}
	
	private void addRow(){
		tableModel.addRow(new Object[]{"Insert Name","","",""});
	}
	
	private void delRow(){
		if(selectedRow != null){
			tableModel.removeRow(selectedRow);
			selectedRow = null;
		}	
	}
	
	private void moveRow(boolean isUp, int selectedRow){
		if(isUp && selectedRow > 0){
			tableModel.moveRow(selectedRow - 1 , selectedRow - 1, selectedRow);
		}
	
		if(!isUp && selectedRow < table.getRowCount() - 1){
			tableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
		}
	}
	
	public HashMap<String, ImagePlus> getDomFile(){
		return hashDomFile;
	}
	
	public FileInfo getFileInfo(){
		return compoInfo;
	}
	
    public boolean wasCanceled() {
        return wasCanceled;
    }
    
	@Override
	public  void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();

		if(input == "+")
			addRow();
		
		if(input == "-")
			delRow();
	

		if(input == "OK" && !hashDomFile.isEmpty() &&checkAllImages()){
			hashDomainTypes = getDomainTypes();			
			hashSampledValues = getSampledValues();
			setVisible(false);
			wasCanceled = true;
			dispose();
		}else{
			new MessageDialog(new Frame(), "Error", "No Image");
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
		JTable table = (JTable) e.getSource();
		selectedRow = table.getSelectedRow();
		selectedColumn = table.getSelectedColumn();
		if(selectedColumn == 2){
			ImageDialog id = new ImageDialog();
			ImagePlus img = id.showDialog();
			if(img != null)
				importFile(1, selectedRow, img);
		}
		
		if(selectedColumn == 3 || selectedColumn == 4){
			moveRow(selectedColumn == 3, selectedRow);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
