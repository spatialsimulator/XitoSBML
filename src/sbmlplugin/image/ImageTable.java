package sbmlplugin.image;

import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import sbmlplugin.gui.AddingColumn;
import sbmlplugin.gui.ArrowColumn;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Dec 7, 2015
 */

@SuppressWarnings("serial")
public class ImageTable extends JTable implements MouseListener{
	
	/** The table model. */
	private DefaultTableModel tableModel;
	
	/** The default domtype. */
	private final String[] defaultDomtype = {"Nucleus","Mitochondria","Golgi","Cytosol"};
	
	/** The column names. */
	private final String[] columnNames = {"Domain Type","Image","Add","Up","Down"};
	
	/** The col dom type. */
	private final int colDomType = 0;
	
	/** The col img name. */
	private final int colImgName = 1;
	
	/** The col add image. */
	private final int colAddImage = 2;
	
	/** The col up button. */
	private final int colUpButton = 3;
	
	/** The col down button. */
	private final int colDownButton = 4;
	
	/** The hash dom file. */
	private HashMap<String,ImagePlus> hashDomFile = new HashMap<String, ImagePlus>();
	
	/** The compo info. */
	private FileInfo compoInfo;
	
	/**
	 * Instantiates a new image table.
	 */
	public ImageTable() {
		super();

		Object[][] data = new Object[defaultDomtype.length][columnNames.length];
		for (int i = 0; i < defaultDomtype.length; i++) {
			data[i][0] = defaultDomtype[i];
		}

		// table
		tableModel = new MyTableModel(data, columnNames);

		setModel(tableModel);
		setBackground(new Color(169,169,169));
		getTableHeader().setReorderingAllowed(false);

		// mouse
		addMouseListener(this);
		setCellSelectionEnabled(true);

		new ArrowColumn(this, colUpButton, BasicArrowButton.NORTH);
		new ArrowColumn(this, colDownButton, BasicArrowButton.SOUTH);
		new AddingColumn(this, colAddImage);

		TableColumn column = (TableColumn) this.getColumnModel().getColumn(colAddImage);
		column.setMaxWidth(50);
		column = (TableColumn) getColumnModel().getColumn(colDownButton);
		column.setMaxWidth(50);
		column = (TableColumn) getColumnModel().getColumn(colUpButton);
		column.setMaxWidth(50);
		setVisible(true);
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		ImageTable table = new ImageTable();
		JScrollPane scroll = new JScrollPane(table);
		JFrame frame = new JFrame();
		frame.getContentPane().add(scroll, BorderLayout.CENTER);
		frame.setResizable(false);
		frame.setBounds(100,100,500,240);
		frame.setLocationByPlatform(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	/**
	 * Cmpsize.
	 *
	 * @param org the org
	 * @param in the in
	 * @return true, if successful
	 */
	private boolean cmpsize(double org, double in){
		double num = org - in;
		
		if(Math.abs(num) < 0.01)
			return true;
			
		return false;
	}	
	
	/**
	 * Comp image.
	 *
	 * @param compoImg the compo img
	 * @param img the img
	 * @return true, if successful
	 */
	private boolean compImage(ImagePlus compoImg, ImagePlus img) {
		boolean width = compoImg.getWidth() == img.getWidth();
		boolean height = compoImg.getHeight() == img.getHeight();
		boolean depth = compoImg.getStackSize() == img.getStackSize();
		FileInfo imgInfo = img.getFileInfo();
		compoInfo = compoImg.getFileInfo();
		boolean voxx, voxy, voxz;
		try {
			voxx = cmpsize(compoInfo.pixelWidth, imgInfo.pixelWidth);
			voxy = cmpsize(compoInfo.pixelHeight, imgInfo.pixelHeight);
			voxz = cmpsize(compoInfo.pixelDepth, imgInfo.pixelDepth);
		} catch (NullPointerException e) {
			voxx = true; voxy = true; voxz = true;
		}
		System.out.println(voxx + " " + voxy +" " + voxz);
		
		return width && height && depth && voxx && voxy && voxz;
	}
	
	/**
	 * Adds the row.
	 */
	void addRow(){
		tableModel.addRow(new Object[]{"Insert Name","","",""});
	}
	
	/**
	 * Del row.
	 */
	void delRow(){
		int selectedRow = getSelectedRow();
		if(selectedRow == -1)
			return;

		if (tableModel.getValueAt(selectedRow, colImgName) != null)
			hashDomFile.remove(tableModel.getValueAt(selectedRow, colDomType));

		tableModel.removeRow(selectedRow);
	}
	
	/**
	 * Move row up.
	 *
	 * @param selectedRow the selected row
	 */
	private void moveRowUp(int selectedRow){
		if(selectedRow > 0)
			tableModel.moveRow(selectedRow - 1 , selectedRow - 1, selectedRow);
	}
	
	/**
	 * Move row down.
	 *
	 * @param selectedRow the selected row
	 */
	private void moveRowDown(int selectedRow){	
		if(selectedRow < getRowCount() - 1)
			tableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
	}
	
	
	/**
	 * Import file.
	 *
	 * @param row the row
	 * @param img the img
	 */
	public void importFile(int row, ImagePlus img){
		if(!hashDomFile.isEmpty() && !compImage(img, hashDomFile.values().iterator().next())) {
			new MessageDialog(new Frame(), "Error", "Image size must be same for all input image");
			return;
		}
			
		setValueAt(img.getTitle(), row, colImgName);
		hashDomFile.put(getValueAt(row, colDomType).toString(), img);	
	}

	/**
	 * Gets the hash dom file.
	 *
	 * @return the hash dom file
	 */
	public HashMap<String,ImagePlus> getHashDomFile(){
		return hashDomFile;
	}
	
	/**
	 * Gets the file info.
	 *
	 * @return the file info
	 */
	public FileInfo getFileInfo(){
		return compoInfo;
	}
	
	/**
	 * Gets the img num.
	 *
	 * @return the img num
	 */
	public int getImgNum(){
		return hashDomFile.size();
	}
			
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int selectedColumn = this.getSelectedColumn();
		int selectedRow = this.getSelectedRow();
		
		if(selectedColumn == colAddImage){
			ImageDialog id = new ImageDialog();
			ImagePlus img = id.showDialog();
			
			if(img != null)
				importFile(selectedRow, img);
		} else if(selectedColumn == colUpButton)
			moveRowUp(selectedRow);
		 else if(selectedColumn == colDownButton)
			moveRowDown( selectedRow);	
	
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * The Class MyTableModel.
	 */
	public class MyTableModel extends DefaultTableModel {

		/**
		 * Instantiates a new my table model.
		 *
		 * @param data the data
		 * @param colName the col name
		 */
		public MyTableModel(Object[][] data, String[] colName) {
			super(data, colName);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return false;
			else
				return true;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int Column) {
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
	}

}
