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
import ij.process.FloodFiller;
import ij.process.ImageProcessor;


public class ImageDialog implements ItemListener{
	private ImagePlus img;
	private GenericDialog gd;
	private boolean fromFile;
	
	
	public ImagePlus showDialog(){
		gd = new GenericDialog("Add Image");
		
		String[] source = {"From Image","From File"}; 
		gd.addChoice("Image Source:", source, null);
		addImageChoice();
		
		// automatically update name if a different image is selected
		final Choice im = (Choice) gd.getChoices().get(0);
		im.addItemListener(this);
		
		System.out.println("image explorer");
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
		

		if(!fromFile) {
			img = fromImage();
		}
		System.out.println(img.getTitle());
		
		return img;
	}
	
	private ImagePlus fromImage(){
		fromFile = false;
		Vector<Choice> v = gd.getChoices();
		img = WindowManager.getImage(v.get(1).getSelectedItem());
		return img;
	}
	
	private void addImageChoice(){
		int numimage = WindowManager.getImageCount();
		Vector<String> windows = new Vector<String>();

		if(numimage == 0){
			String[] s = {"NoImage"};
			gd.addChoice("Image:", s, "NaN");
			return;
		}else{
			for(int i = 1 ; i <= numimage ; i++){
				int id = WindowManager.getNthImageID(i);
				ImagePlus ip = WindowManager.getImage(id);	
				if(checkImage(ip))
					windows.add(ip.getTitle());
			}
		}
		
		final String[] images = new String[windows.size()];
		windows.toArray(images);
		String name = images[0];
		gd.addChoice("Image", images, name);
	}
	

	void fill(ImageProcessor ip, int foreground, int background) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		FloodFiller ff = new FloodFiller(ip);
		ip.setColor(127);
		for (int y = 0; y < height; y++) {
			if (ip.getPixel(0, y) == background)
				ff.fill(0, y);
			if (ip.getPixel(width - 1, y) == background)
				ff.fill(width - 1, y);
		}
		for (int x = 0; x < width; x++) {
			if (ip.getPixel(x, 0) == background)
				ff.fill(x, 0);
			if (ip.getPixel(x, height - 1) == background)
				ff.fill(x, height - 1);
		}
		byte[] pixels = (byte[]) ip.getPixels();
		int n = width * height;
		for (int i = 0; i < n; i++) {
			if (pixels[i] == 127)
				pixels[i] = (byte) background;
			else
				pixels[i] = (byte) foreground;
		}
	}
	
	
	private FolderOpener openImg = new FolderOpener();
	private Opener open = new Opener();
	
	public ImagePlus fromFile(){
		fromFile = true;
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		File f = chooser.getSelectedFile();
		OpenDialog.setLastDirectory(f.getParentFile().getAbsolutePath());
		ImagePlus temp = null;
		
		try {
			temp = openImg.openFolder(f.getAbsolutePath());
			if (temp == null)
				temp = open.openImage(f.getAbsolutePath());
			
		} catch (Exception e) {
			errMessage();
		}
		if(checkImage(temp)){
			addImageName(temp.getTitle());
			temp.getTitle();
			return temp;	
		}else 
			return null;
	}

	private void addImageName(String title){
		Vector<Choice> vc = gd.getChoices();
		Choice c = vc.get(1);
		c.removeAll();
		c.add(title);
		gd.validate();
	}
	
	private void errMessage(){
		new MessageDialog(new Frame(), "Error", "Input Image must be 8-bit grayscale");
	}

	private boolean checkImage(ImagePlus img){
		if (img == null || img.getType() != ImagePlus.GRAY8) {
			errMessage();
			return false;
		} else {
			return true;		
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		Choice c = (Choice) e.getSource();
		int idx = c.getSelectedIndex();
		/*
		String name;
		if (idx > 0)	
			name = c.getSelectedItem();
		*/
		if(c.getSelectedItem().equals("From File"))
			img = fromFile();
		if(c.getSelectedItem().equals("From Image"))
			img = fromImage();
	}
}
