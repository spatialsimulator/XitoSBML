
import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.FolderOpener;
import ij3d.Image3DUniverse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
	private final String[] domtype = {"Nucleus","Mitochondria","Golgi","Cytosol"}; 
	private final String[] columnNames = {"Domain Type","Image"};
	private HashMap<String,ImagePlus> hashDomFile;
	private FolderOpener openImg = new FolderOpener();
	private Opener open = new Opener();
	private FileInfo compoInfo;
	
	public ImageExplorer(){
		super("DomainType Namer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(500, 240);
	}
	
	public ImageExplorer(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues){
		this();
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;
		//data sets for the table
		Object[][] data = new Object[domtype.length][2];
		for(int i = 0 ; i < domtype.length ; i++){
			data[i][0] = domtype[i];
			data[i][1] = "";
		}
		
		//table
		tableModel = new DefaultTableModel(data,columnNames){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column){				//locks the first and second column 
				if(column == 0)
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
					return String.class;
				default :
					return Boolean.class;
				}
			}
		};
		table.setBackground(new Color(169,169,169));
		table.getTableHeader().setReorderingAllowed(false);
		
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
		
		hashDomFile = new HashMap<String, ImagePlus>();
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
		for(Entry<String, ImagePlus> e : hashDomFile.entrySet()){
			hashSampledValues.put( e.getKey().toString(), pixel);
			pixel -= interval;
		}
		hashSampledValues.put("Extracellular", 0);
		return hashSampledValues;
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
	}
	
	private boolean checkAllImages() {
		Iterator<String> domNames = hashDomFile.keySet().iterator();
		ImagePlus compoImg = hashDomFile.get(domNames.next());
		compoInfo = compoImg.getFileInfo();
		System.out.println(compoInfo);
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
			voxx = true;
			voxy = true;
			voxz = true;
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
			if (temp.getType() == 0) {
				table.setValueAt(f.getName(), row, column);
				hashDomFile.put(table.getValueAt(row, 0).toString(), temp);
			} else if(temp.getType() == 3){
				Image3DUniverse univ = new Image3DUniverse();
				univ.show();
				univ.addVoltex(temp);
				
			} else{
				errMessage();
			}
		} catch (Exception e) {
			errMessage();
		}		
	}
	
	private void errMessage(){
		new MessageDialog(new Frame(), "Error", "Input Image must be 8-bit grayscale");
	}
	
	public HashMap<String, ImagePlus> getDomFile(){
		return hashDomFile;
	}
	
	public FileInfo getFileInfo(){
		return compoInfo;
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
		if(table.getSelectedColumn() == 1){
			importFile(1 , table.getSelectedRow());
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
