package jp.ac.keio.bio.fun.xitosbml.image;

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

import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;
import jp.ac.keio.bio.fun.xitosbml.gui.AddingColumn;
import jp.ac.keio.bio.fun.xitosbml.gui.ArrowColumn;

/**
 * The class ImageTable, which inherits JTable and implements mousePressed()
 * method for adding images to the spatial model.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.image.ImageExplorer}.
 * Date Created: Dec 7, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public class ImageTable extends JTable implements MouseListener{
	
	/** The table model. */
	private DefaultTableModel tableModel;
	
	/** The default domain type. */
	private final String[] defaultDomtype = {"Nucleus","Mitochondria","Golgi","Cytosol"};
	
	/** The column title. */
	private final String[] columnNames = {"Domain Type","Image","Add","Up","Down"};
	
	/** The column index of domain type. */
	private final int colDomType = 0;
	
	/** The column index of image name. */
	private final int colImgName = 1;
	
	/** The column index of add image. */
	private final int colAddImage = 2;
	
	/** The column index of up button. */
	private final int colUpButton = 3;
	
	/** The column index of down button. */
	private final int colDownButton = 4;
	
	/** The hashmap of domain file. HashMap&lt;String, ImagePlus&gt; */
	private HashMap<String,ImagePlus> hashDomFile = new HashMap<String, ImagePlus>();
	
	/** The file information of composite image. */
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
	 * Example main() method which will launch a GUI and show this JTable object.
	 *
	 * @param args an array of command-line arguments for the application
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
	 * Checks whether the size(width, height or depth) of given image (img) is
	 * same with the size of composite image (compoImg).
	 *
	 * @param compoImg the original image size (usually, the size of composite image is used)
	 * @param image the input image size
	 * @return true, if the size is same
	 */
	private boolean cmpsize(double compoImg, double image){
		double num = compoImg - image;
		
		if(Math.abs(num) < 0.01)
			return true;
			
		return false;
	}	
	
	/**
	 * Checks whether the size of given image (img) is same with the size of composite image (compoImg).
	 * new MessageDialog(new Frame(), "Error", "Image size must be same for all input image");
	 *
	 * @param compoImg the composite img
	 * @param img the given image
	 * @return true, if the size of given image and the composite image is same
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
	 * Adds the row to table model.
	 */
	void addRow(){
		tableModel.addRow(new Object[]{"Insert Name","","",""});
	}
	
	/**
	 * Delete the selected row from table model.
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
	 * Move the selected row up in the table model.
	 *
	 * @param selectedRow the selected row
	 */
	private void moveRowUp(int selectedRow){
		if(selectedRow > 0)
			tableModel.moveRow(selectedRow - 1 , selectedRow - 1, selectedRow);
	}
	
	/**
	 * Move the selected row down in the table model.
	 *
	 * @param selectedRow the selected row
	 */
	private void moveRowDown(int selectedRow){	
		if(selectedRow < getRowCount() - 1)
			tableModel.moveRow(selectedRow, selectedRow, selectedRow + 1);
	}
	
	
	/**
	 * Import a file to the selected row.
	 * The input image size will be checked to be same with the composite image.
	 *
	 * @param row the selected row
	 * @param img the image to be imported
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
	 * Gets the hashmap of domain file. HashMap&lt;String, ImagePlus&gt;
	 *
	 * @return the hashmap of domain file
	 */
	public HashMap<String,ImagePlus> getHashDomFile(){
		return hashDomFile;
	}
	
	/**
	 * Gets the file information of composite image.
	 *
	 * @return the file information of composite image
	 */
	public FileInfo getFileInfo(){
		return compoInfo;
	}
	
	/**
	 * Gets the number of images, that is, the size of the hashmap of domain file.
	 *
	 * @return the number of images
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

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 * Adds an image, move selected row up or move selected row down.
	 * @param e the MouseEvent
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
	 * The class MyTableModel, which inherits DefaultTableModel and implements
     * isCellEditable() and getColumnClass() methods.
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

		/**
		 * The cells in column 1 is not editable.
		 *
		 * @param row the selected row
		 * @param column the selected column
		 * @return true if the selected cell is editable
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return false;
			else
				return true;
		}

		/**
		 * Get the class of selected cell.
		 *
		 * @param Column
		 * @return the class of selected cell
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
