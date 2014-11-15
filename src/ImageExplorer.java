
import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.FolderOpener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;




public class ImageExplorer extends JFrame implements ActionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValues;
	private DefaultTableModel tableModel;
	private JTable table;
	private final String[] domtype = {"Nucleus","Mitochondria","Golgi","Cytosol"}; 		//in order of priority when making the composed image
	private final String[] columnNames = {"Domain Type","Image","Up","Down"};
	private HashMap<String,ImagePlus> hashDomFile;
	private FolderOpener openImg = new FolderOpener();
	private Opener open = new Opener();
	private FileInfo compoInfo;
	private Integer selectedRow = null;
	private Integer selectedColumn = null;
	
	public ImageExplorer(){
		super("DomainType Namer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(500, 240);
	}
	
	public ImageExplorer(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues){
		this();
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		hashDomFile = new HashMap<String, ImagePlus>();
		
		//data sets for the table
		Object[][] data = new Object[domtype.length][4];
		for(int i = 0 ; i < domtype.length ; i++){
			data[i][0] = domtype[i];
		}
		
		//table
		tableModel = new DefaultTableModel(data,columnNames){
			private static final long serialVersionUID = 1L;
		/*
			public boolean isCellEditable(int row, int column){	
				if(column == 0)
					return false;
				else  							
					return true;
			}
			*/
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
				case 3:
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
		JButton ok = new JButton("OK");
		ok.addActionListener(this);
		JButton plus = new JButton("+");
		plus.addActionListener(this);
		JButton minus = new JButton("-");
		minus.addActionListener(this);
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(plus);
		p2.add(minus);
		p2.add(Box.createRigidArea(new Dimension(250, 0)));
		p2.add(ok);
		
		//set components 
		getContentPane().add(p2, BorderLayout.PAGE_END);
		getContentPane().add(scroll, BorderLayout.CENTER);	

		//arrow column
		new ArrowColumn(table, 2 , BasicArrowButton.NORTH);
		new ArrowColumn(table, 3 , BasicArrowButton.SOUTH);
		
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
		for(int i = 0 ; i < table.getColumnCount() ; i++){
			String s = (String) table.getValueAt(i, 0);
			if(hashDomFile.containsKey(s)){
				hashSampledValues.put(s, pixel);
				pixel -= interval;
			}
		}
		hashSampledValues.put("Extracellular", 0);
		return hashSampledValues;
	}
	
	private boolean checkAllImages() {
		Iterator<String> domNames = hashDomFile.keySet().iterator();
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
			voxx = compoInfo.pixelWidth == imgInfo.pixelWidth;
			voxy = compoInfo.pixelHeight == imgInfo.pixelHeight;
			voxz = compoInfo.pixelDepth == imgInfo.pixelDepth;
		} catch (Exception e) {
			voxx = true;voxy = true;voxz = true;
		}
		return width && height && depth && voxx && voxy && voxz;
	}
	
	public static void main(String args[]){	
		HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
		HashMap<String, Integer> hashSampledValues = new HashMap<String, Integer>();
		new ImageExplorer(hashDomainTypes, hashSampledValues);
	}

	public void importFile(int column, int row){
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		File f = chooser.getSelectedFile();
		OpenDialog.setLastDirectory(f.getParentFile().getAbsolutePath());
		
		try {
			ImagePlus temp = openImg.openFolder(f.getAbsolutePath());
			if (temp == null)
				temp = open.openImage(f.getAbsolutePath());
			if (temp.getType() == ImagePlus.GRAY8) {
				table.setValueAt(f.getName(), row, column);
				hashDomFile.put(table.getValueAt(row, 0).toString(), temp);
			} else {
				errMessage();
			}
		} catch (Exception e) {
			errMessage();
		}		
	}
	
	private void errMessage(){
		new MessageDialog(new Frame(), "Error", "Input Image must be 8-bit grayscale");
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
	
	@Override
	public  void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		if(input == "OK" && checkAllImages()){
			hashDomainTypes = getDomainTypes();			
			hashSampledValues = getSampledValues();
			setVisible(false);
			dispose();
		}

		if(input == "+")
			addRow();
		
		if(input == "-")
			delRow();
	
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
		if(selectedColumn == 1){
			importFile(1 , selectedRow);
		}
		
		if(selectedColumn == 2 || selectedColumn == 3){
			moveRow(selectedColumn == 2, selectedRow);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
