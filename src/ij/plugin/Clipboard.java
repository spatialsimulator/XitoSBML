/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.plugin; 
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.frame.Editor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
	
// TODO: Auto-generated Javadoc
/**	Copies/pastes images to/from the system clipboard. */
public class Clipboard implements PlugIn, Transferable {
	
	/** The clipboard. */
	static java.awt.datatransfer.Clipboard clipboard;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (IJ.altKeyDown()) {
			if (arg.equals("copy"))
				arg = "scopy";
			else if (arg.equals("paste"))
				arg = "spaste";
		}
  		if (arg.equals("copy"))
			copy(false);
  		else if (arg.equals("paste"))
			paste();
  		else if (arg.equals("cut"))
			copy(true);
  		else if (arg.equals("scopy"))
			copyToSystem();
		else if (arg.equals("showsys"))
			showSystemClipboard();
		else if (arg.equals("show"))
			showInternalClipboard();
	}
	
	/**
	 * Copy.
	 *
	 * @param cut the cut
	 */
	void copy(boolean cut) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
	 		imp.copy(cut);
	 	else
	 		IJ.noImage();
	}
	
	/**
	 * Paste.
	 */
	void paste() {
		if (ImagePlus.getClipboard()==null)
			showSystemClipboard();
		else {
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null)
				imp.paste();
			else
				showInternalClipboard	();
		}
	}

	/**
	 * Setup.
	 */
	void setup() {
		if (clipboard==null)
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}
	
	/**
	 * Copy to system.
	 */
	void copyToSystem() {
		setup();
		try {
			clipboard.setContents(this, null);
		} catch (Throwable t) {}
	}
	
	/**
	 * Show system clipboard.
	 */
	void showSystemClipboard() {
		setup();
		IJ.showStatus("Opening system clipboard...");
		try {
			Transferable transferable = clipboard.getContents(null);
			boolean imageSupported = transferable.isDataFlavorSupported(DataFlavor.imageFlavor);
			boolean textSupported = transferable.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (!imageSupported && IJ.isMacOSX() && !IJ.isJava16()) {
				// attempt to open PICT file using QuickTime for Java
				Object mc = IJ.runPlugIn("MacClipboard", ""); 
				if (mc!=null && (mc instanceof ImagePlus) && ((ImagePlus)mc).getImage()!=null)
					return;
			}
			if (imageSupported) {
				Image img = (Image)transferable.getTransferData(DataFlavor.imageFlavor);
				if (img==null) {
					IJ.error("Unable to convert image on system clipboard");
					IJ.showStatus("");
					return;
				}
				int width = img.getWidth(null);
				int height = img.getHeight(null);
				BufferedImage   bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics g = bi.createGraphics();
				g.drawImage(img, 0, 0, null);
				g.dispose();
				WindowManager.checkForDuplicateName = true;
				new ImagePlus("Clipboard", bi).show();
			} else if (textSupported) {
				String text = (String)transferable.getTransferData(DataFlavor.stringFlavor);
				if (IJ.isMacintosh())
					text = Tools.fixNewLines(text);
				Editor ed = new Editor();
				ed.setSize(600, 300);
				ed.create("Clipboard", text);
				IJ.showStatus("");
			} else
				IJ.error("Unable to find an image on the system clipboard");
		} catch (Throwable e) {
			IJ.handleException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			ImageProcessor ip;
			if (imp.isComposite()) {
				ip = new ColorProcessor(imp.getImage());
				ip.setRoi(imp.getRoi());
			} else	
				ip = imp.getProcessor();
			ip = ip.crop();
			int w = ip.getWidth();
			int h = ip.getHeight();
			IJ.showStatus(w+"x"+h+ " image copied to system clipboard");
			Image img = IJ.getInstance().createImage(w, h);
			Graphics g = img.getGraphics();
			g.drawImage(ip.createImage(), 0, 0, null);
			g.dispose();
			return img;
		} else {
			//IJ.noImage();
			return null;
		}
	}
	
	/**
	 * Show internal clipboard.
	 */
	void showInternalClipboard() {
		ImagePlus clipboard = ImagePlus.getClipboard();
		if (clipboard!=null) {
			ImageProcessor ip = clipboard.getProcessor();
			ImagePlus imp2 = new ImagePlus("Clipboard", ip.duplicate());
			Roi roi = clipboard.getRoi();
			imp2.deleteRoi();
			if (roi!=null && roi.isArea() && roi.getType()!=Roi.RECTANGLE) {
				roi = (Roi)roi.clone();
				roi.setLocation(0, 0);
				imp2.setRoi(roi);
				IJ.run(imp2, "Clear Outside", null);
				imp2.deleteRoi();
			}
			WindowManager.checkForDuplicateName = true;          
			imp2.show();
		} else
			IJ.error("The internal clipboard is empty.");
	}

}



