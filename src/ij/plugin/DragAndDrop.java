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
import ij.ImageJ;
import ij.Macro;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij.io.Opener;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/** This class opens images, roi's, luts and text files dragged and dropped on  the "ImageJ" window.
     It is based on the Draw_And_Drop plugin by Eric Kischell (keesh@ieee.org).
     
     10 November 2006: Albert Cardona added Linux support and an  
     option to open all images in a dragged folder as a stack.
*/
     
public class DragAndDrop implements PlugIn, DropTargetListener, Runnable {
	
	/** The iterator. */
	private Iterator iterator;
	
	/** The convert to RGB. */
	private static boolean convertToRGB;
	
	/** The virtual stack. */
	private static boolean virtualStack;
	
	/** The open as virtual stack. */
	private boolean openAsVirtualStack;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImageJ ij = IJ.getInstance();
		ij.setDropTarget(null);
		new DropTarget(ij, this);
		new DropTarget(Toolbar.getInstance(), this);
		new DropTarget(ij.getStatusBar(), this);
	}  
	    
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dtde)  {
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		DataFlavor[] flavors = null;
		try  {
			Transferable t = dtde.getTransferable();
			iterator = null;
			flavors = t.getTransferDataFlavors();
			if (IJ.debugMode) IJ.log("DragAndDrop.drop: "+flavors.length+" flavors");
			for (int i=0; i<flavors.length; i++) {
			if (IJ.debugMode) IJ.log("  flavor["+i+"]: "+flavors[i].getMimeType());
			if (flavors[i].isFlavorJavaFileListType()) {
				Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
				iterator = ((List)data).iterator();
				break;
			} else if (flavors[i].isFlavorTextType()) {
				Object ob = t.getTransferData(flavors[i]);
				if (!(ob instanceof String)) continue;
				String s = ob.toString().trim();
				if (IJ.isLinux() && s.length()>1 && (int)s.charAt(1)==0)
				s = fixLinuxString(s);
				ArrayList list = new ArrayList();
				if (s.indexOf("href=\"")!=-1 || s.indexOf("src=\"")!=-1) {
					s = parseHTML(s);
					if (IJ.debugMode) IJ.log("  url: "+s);
					list.add(s);
					this.iterator = list.iterator();
					break;
				}
				BufferedReader br = new BufferedReader(new StringReader(s));
				String tmp;
				while (null != (tmp = br.readLine())) {
					tmp = java.net.URLDecoder.decode(tmp.replaceAll("\\+","%2b"), "UTF-8");
					if (tmp.startsWith("file://")) tmp = tmp.substring(7);
					if (IJ.debugMode) IJ.log("  content: "+tmp);
					if (tmp.startsWith("http://"))
						list.add(s);
					else
						list.add(new File(tmp));
					}
					this.iterator = list.iterator();
					break;
				}
			}
			if (iterator!=null) {
				Thread thread = new Thread(this, "DrawAndDrop");
				thread.setPriority(Math.max(thread.getPriority()-1, Thread.MIN_PRIORITY));
				thread.start();
			}
		}
		catch(Exception e)  {
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
		if (flavors==null || flavors.length==0) {
			if (IJ.isMacOSX())
				IJ.error("First drag and drop ignored. Please try again. You can avoid this\n"
				+"problem by dragging to the toolbar instead of the status bar.");
			else
				IJ.error("Drag and drop failed");
		}
	}
	    
	    /**
    	 * Fix linux string.
    	 *
    	 * @param s the s
    	 * @return the string
    	 */
    	private String fixLinuxString(String s) {
	    	StringBuffer sb = new StringBuffer(200);
	    	for (int i=0; i<s.length(); i+=2)
	    		sb.append(s.charAt(i));
	    	return new String(sb);
	    }
	    
	    /**
    	 * Parses the HTML.
    	 *
    	 * @param s the s
    	 * @return the string
    	 */
    	private String parseHTML(String s) {
	    	if (IJ.debugMode) IJ.log("parseHTML:\n"+s);
	    	int index1 = s.indexOf("src=\"");
	    	if (index1>=0) {
	    		int index2 = s.indexOf("\"", index1+5);
	    		if (index2>0)
	    			return s.substring(index1+5, index2);
	    	}
	    	index1 = s.indexOf("href=\"");
	    	if (index1>=0) {
	    		int index2 = s.indexOf("\"", index1+6);
	    		if (index2>0)
	    			return s.substring(index1+6, index2);
	    	}
	    	return s;
	    }

	    /* (non-Javadoc)
    	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
    	 */
    	public void dragEnter(DropTargetDragEvent e)  {
	    	IJ.showStatus("<<Drag and Drop>>");
			if (IJ.debugMode) IJ.log("DragEnter: "+e.getLocation());
			e.acceptDrag(DnDConstants.ACTION_COPY);
			openAsVirtualStack = false;
	    }

	    /* (non-Javadoc)
    	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
    	 */
    	public void dragOver(DropTargetDragEvent e) {
			if (IJ.debugMode) IJ.log("DragOver: "+e.getLocation());
			Point loc = e.getLocation();
			int buttonSize = Toolbar.getButtonSize();
			int width = IJ.getInstance().getSize().width;
			openAsVirtualStack = width-loc.x<=buttonSize;
			if (openAsVirtualStack)
	    		IJ.showStatus("<<Open as Virtual Stack>>");
	    	else
	    		IJ.showStatus("<<Drag and Drop>>");
	    }
	    
	    /* (non-Javadoc)
    	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
    	 */
    	public void dragExit(DropTargetEvent e) {
	    	IJ.showStatus("");
	    }
	    
    	/* (non-Javadoc)
    	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
    	 */
    	public void dropActionChanged(DropTargetDragEvent e) {}
	    
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			Iterator iterator = this.iterator;
			while(iterator.hasNext()) {
				Object obj = iterator.next();
				if (obj!=null && (obj instanceof String))
					openURL((String)obj);
				else
					openFile((File)obj);
			}
		}
		
		/**
		 *  Open a URL.
		 *
		 * @param url the url
		 */
		private void openURL(String url) {
			if (IJ.debugMode) IJ.log("DragAndDrop.openURL: "+url);
			if (url!=null)
				IJ.open(url);
		}

		/**
		 *  Open a file. If it's a directory, ask to open all images as a sequence in a stack or individually.
		 *
		 * @param f the f
		 */
		public void openFile(File f) {
			if (IJ.debugMode) IJ.log("DragAndDrop.openFile: "+f);
			try {
				if (null == f) return;
				String path = f.getCanonicalPath();
				if (f.exists()) {
					if (f.isDirectory())
						openDirectory(f, path);
					else {
						if (openAsVirtualStack && (path.endsWith(".tif")||path.endsWith(".TIF")))
							(new FileInfoVirtualStack()).run(path);
						else
							(new Opener()).openAndAddToRecent(path);
						OpenDialog.setLastDirectory(f.getParent()+File.separator);
						OpenDialog.setLastName(f.getName());
					}
				} else {
					IJ.log("File not found: " + path);
				}
			} catch (Throwable e) {
				if (!Macro.MACRO_CANCELED.equals(e.getMessage()))
					IJ.handleException(e);
			}
		}
		
		/**
		 * Open directory.
		 *
		 * @param f the f
		 * @param path the path
		 */
		private void openDirectory(File f, String path) {
			if (path==null) return;
			if (!(path.endsWith(File.separator)||path.endsWith("/")))
				path += File.separator;
			String[] names = f.list();
			names = (new FolderOpener()).trimFileList(names);
			if (names==null)
				return;
			String msg = "Open all "+names.length+" images in \"" + f.getName() + "\" as a stack?";
			GenericDialog gd = new GenericDialog("Open Folder");
			gd.setInsets(10,5,0);
			gd.addMessage(msg);
			gd.setInsets(15,35,0);
			gd.addCheckbox("Convert to RGB", convertToRGB);
			gd.setInsets(0,35,0);
			gd.addCheckbox("Use Virtual Stack", virtualStack);
			gd.enableYesNoCancel();
			gd.showDialog();
			if (gd.wasCanceled()) return;
			if (gd.wasOKed()) {
				convertToRGB = gd.getNextBoolean();
				virtualStack = gd.getNextBoolean();
				String options  = " sort";
				if (convertToRGB) options += " convert_to_rgb";
				if (virtualStack) options += " use";
				IJ.run("Image Sequence...", "open=[" + path + "]"+options);
				DirectoryChooser.setDefaultDirectory(path);
			} else {
				for (int k=0; k<names.length; k++) {
					if (!names[k].startsWith(".")) {
						IJ.redirectErrorMessages(true);
						(new Opener()).open(path + names[k]);
						IJ.redirectErrorMessages(false);
					}
				}
			}
			IJ.register(DragAndDrop.class);
		}
		
}
