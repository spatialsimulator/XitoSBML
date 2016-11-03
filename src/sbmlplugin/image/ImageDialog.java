package sbmlplugin.image;

import java.awt.Choice;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.MessageDialog;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.FolderOpener;


// TODO: Auto-generated Javadoc
/**
 * The Class ImageDialog.
 */
public class ImageDialog implements ItemListener{
	
	/** The img. */
	private ImagePlus img;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The from file. */
	private boolean fromFile;
	
	
	/**
	 * Show dialog.
	 *
	 * @return the image plus
	 */
	public ImagePlus showDialog(){
		gd = new GenericDialog("Add Image");
		gd.setResizable(true);
		gd.pack();
		String[] source = {"From Image","From File"}; 
		gd.addChoice("Image Source:", source, null);
		addImageChoice();
		
		// automatically update name if a different image is selected
		final Choice im = (Choice) gd.getChoices().get(0);
		im.addItemListener(this);
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
		
		if(!fromFile) {
			img = fromImage();
		}
		return img;
	}
	
	/**
	 * From image.
	 *
	 * @return the image plus
	 */
	@SuppressWarnings("unchecked")
	private ImagePlus fromImage(){
		fromFile = false;
		Vector<Choice> v = gd.getChoices();
		img = WindowManager.getImage(v.get(1).getSelectedItem());
		if(!checkImage(img))	return null;
		return img;
	}
	
	/**
	 * Adds the image choice.
	 */
	private void addImageChoice(){
		int numimage = WindowManager.getImageCount();
		Vector<String> windows = new Vector<String>();

		if(numimage == 0){
			String[] s = {"No Image"};
			gd.addChoice("Image:", s, "NaN");	
			return;
		}else{
			for(int i = 1 ; i <= numimage ; i++){
				int id = WindowManager.getNthImageID(i);
				ImagePlus ip = WindowManager.getImage(id);	
				windows.add(ip.getTitle());
			}
		}
		
		final String[] images = new String[windows.size()];
		windows.toArray(images);
		String name = images[0];
		gd.addChoice("Image", images, name);
	}
		
	/** The open img. */
	private FolderOpener openImg = new FolderOpener();
	
	/** The open. */
	private Opener open = new Opener();
	
	/**
	 * From file.
	 *
	 * @return the image plus
	 */
	public ImagePlus fromFile(){
		File f = getFile();
		if(f == null) return null;
		
		OpenDialog.setLastDirectory(f.getParentFile().getAbsolutePath());
		ImagePlus inImg = null;
		

		inImg = openImg.openFolder(f.getAbsolutePath());
		if (inImg == null)
			inImg = open.openImage(f.getAbsolutePath());

		if(checkImage(inImg)){
			addImageName(inImg.getTitle());
			inImg.getTitle();
			return inImg;	
		}else 
			return null;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	private File getFile(){
		fromFile = true;
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(null);
		
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		
		return chooser.getSelectedFile();
	}
	
	/**
	 * Adds the image name.
	 *
	 * @param title the title
	 */
	@SuppressWarnings("unchecked")
	private void addImageName(String title){
		Vector<Choice> vc = gd.getChoices();
		Choice c = vc.get(1);
		c.removeAll();
		c.add(title);
		gd.validate();
		gd.pack();
	}

	/**
	 * Check image.
	 *
	 * @param img the img
	 * @return true, if successful
	 */
	private boolean checkImage(ImagePlus img){
		if (img == null)
			return false;
		else if(img.getType() != ImagePlus.GRAY8) {
			new MessageDialog(new Frame(), "Error", "Input Image must be 8-bit grayscale");
			return false;
		} else {
			return true;		
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		Choice c = (Choice) e.getSource();
		if(c.getSelectedItem().equals("From File"))
			img = fromFile();
		if(c.getSelectedItem().equals("From Image"))
			img = fromImage();
	}
}
