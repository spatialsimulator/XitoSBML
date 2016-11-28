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
/** This plugin implements the Plugins/Utilities/Find Commands 
    command. It provides an easy user interface to finding commands 
    you might know the name of without having to go through
    all the menus.  If you type a part of a command name, the box
    below will only show commands that match that substring (case
    insensitively).  If only a single command matches then that
    command can be run by hitting Enter.  If multiple commands match,
    they can be selected by selecting with the mouse and clicking
    "Run"; alternatively hitting the up or down arrows will move the
    keyboard focus to the list and the selected command can be run
    with Enter.  Double-clicking on a command in the list should
    also run the appropriate command.

    @author Mark Longair <mark-imagej@longair.net>
    @author Johannes Schindelin <johannes.schindelin@gmx.de>
    @author Curtis Rueden <ctrueden@wisc.edu>
 */

package ij.plugin;
import ij.IJ;
import ij.ImageJ;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.plugin.frame.Editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;


// TODO: Auto-generated Javadoc
/**
 * The Class CommandFinder.
 */
public class CommandFinder implements PlugIn, ActionListener, WindowListener, KeyListener, ItemListener, MouseListener {

	/** The Constant TABLE_WIDTH. */
	private static final int TABLE_WIDTH = 640;
	
	/** The Constant TABLE_ROWS. */
	private static final int TABLE_ROWS = 18;
	
	/** The multi click interval. */
	private int multiClickInterval;
	
	/** The last click time. */
	private long lastClickTime;
	
	/** The frame. */
	private static JFrame frame;
	
	/** The prompt. */
	private JTextField prompt;
	
	/** The scroll pane. */
	private JScrollPane scrollPane;
	
	/** The close button. */
	private JButton runButton, sourceButton, closeButton;
	
	/** The close check box. */
	private JCheckBox closeCheckBox;
	
	/** The commands hash. */
	private Hashtable commandsHash;
	
	/** The commands. */
	private String [] commands;
	
	/** The close when running. */
	private static boolean closeWhenRunning = Prefs.get("command-finder.close", false);
	
	/** The table. */
	private JTable table;
	
	/** The table model. */
	private TableModel tableModel;
	
	/** The last clicked row. */
	private int lastClickedRow;

	/**
	 * Instantiates a new command finder.
	 */
	public CommandFinder() {
		Toolkit toolkit=Toolkit.getDefaultToolkit();
		Integer interval=(Integer)toolkit.getDesktopProperty("awt.multiClickInterval");
		if (interval==null)
			// Hopefully 300ms is a sensible default when the property
			// is not available.
			multiClickInterval = 300;
		else
			multiClickInterval = interval.intValue();
	}

	/**
	 * The Class CommandAction.
	 */
	class CommandAction {
		
		/**
		 * Instantiates a new command action.
		 *
		 * @param classCommand the class command
		 * @param menuItem the menu item
		 * @param menuLocation the menu location
		 */
		CommandAction(String classCommand, MenuItem menuItem, String menuLocation) {
			this.classCommand = classCommand;
			this.menuItem = menuItem;
			this.menuLocation = menuLocation;
		}
		
		/** The class command. */
		String classCommand;
		
		/** The menu item. */
		MenuItem menuItem;
		
		/** The menu location. */
		String menuLocation;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "classCommand: " + classCommand + ", menuItem: "+menuItem+", menuLocation: "+menuLocation;
		}
	}

	/**
	 * Make row.
	 *
	 * @param command the command
	 * @param ca the ca
	 * @return the string[]
	 */
	protected String[] makeRow(String command, CommandAction ca) {
		String[] result = new String[tableModel.getColumnCount()];
		result[0] = command;
		if (ca.menuLocation != null)
			result[1] = ca.menuLocation;
		if (ca.classCommand != null)
			result[2] = ca.classCommand;
		String jarFile = Menus.getJarFileForMenuEntry(command);
		if (jarFile != null)
			result[3] = jarFile;
		return result;
	}

	/**
	 * Populate list.
	 *
	 * @param matchingSubstring the matching substring
	 */
	protected void populateList(String matchingSubstring) {
		String substring = matchingSubstring.toLowerCase();
		ArrayList list = new ArrayList();
		int count = 0;
		for (int i=0; i<commands.length; ++i) {
			String commandName = commands[i];
			String command = commandName.toLowerCase();
			CommandAction ca = (CommandAction)commandsHash.get(commandName);
			String menuPath = ca.menuLocation;
			if (menuPath==null)
				menuPath = "";
			menuPath = menuPath.toLowerCase();
			if (command.indexOf(substring)>=0 || menuPath.indexOf(substring)>=0) {
				String[] row = makeRow(commandName, ca);
				list.add(row);
			}
		}
		tableModel.setData(list);
		prompt.requestFocus();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source==runButton) {
			int row = table.getSelectedRow();
			if (row<0) {
				error("Please select a command to run");
				return;
			}
			runCommand(tableModel.getCommand(row));
		} else if (source==sourceButton) {
			int row = table.getSelectedRow();
			if (row<0) {
				error("Please select a command");
				return;
			}
			showSource(tableModel.getCommand(row));
		} else if (source == closeButton) {
			closeWindow();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		populateList(prompt.getText());
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		long now=System.currentTimeMillis();
		int row = table.getSelectedRow();
		// Is this fast enough to be a double-click?
		long thisClickInterval = now-lastClickTime;
		if (thisClickInterval<multiClickInterval) {
			if (row>=0 && lastClickedRow>=0 && row==lastClickedRow)
				runCommand(tableModel.getCommand(row));
		}
		lastClickTime = now;
		lastClickedRow = row;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}
	
	/**
	 * Show source.
	 *
	 * @param cmd the cmd
	 */
	void showSource(String cmd) {
		if (showMacro(cmd))
			return;
		Hashtable table = Menus.getCommands();
		String className = (String)table.get(cmd);
		if (IJ.debugMode)
			IJ.log("showSource: "+cmd+"   "+className);
		if (className==null) {
			error("No source associated with this command:\n  "+cmd);
			return;
		}
		int mstart = className.indexOf("ij.plugin.Macro_Runner(\"");
		if (mstart>=0) { // macro or script
			int mend = className.indexOf("\")");
			if (mend==-1)
				return;
			String macro = className.substring(mstart+24,mend);
			IJ.open(IJ.getDirectory("plugins")+macro);
			return;
		}
		if (className.endsWith("\")")) {
			int openParen = className.lastIndexOf("(\"");
			if (openParen>0)
				className = className.substring(0, openParen);
		}
		if (className.startsWith("ij.")) {
			className = className.replaceAll("\\.", "/");
			IJ.runPlugIn("ij.plugin.BrowserLauncher", IJ.URL+"/source/"+className+".java");
			return;
		}
		className = IJ.getDirectory("plugins")+className.replaceAll("\\.","/");
		String path = className+".java";
		File f = new File(path);
		if (f.exists()) {
			IJ.open(path);
			return;
		}
		error("Unable to display source for this plugin:\n  "+className);
	}
	
	/**
	 * Show macro.
	 *
	 * @param cmd the cmd
	 * @return true, if successful
	 */
	private boolean showMacro(String cmd) {
		String name = null;
		if (cmd.equals("Display LUTs"))
			name = "ShowAllLuts.txt";
		else if (cmd.equals("Search..."))
			name = "Search.txt";
		if (name==null)
			return false;
		String code = BatchProcessor.openMacroFromJar(name);
		if (code!=null) {
			Editor ed = new Editor();
			ed.setSize(700, 600);
			ed.create(name, code);
			return true;
		}
		return false;
	}

	/**
	 * Error.
	 *
	 * @param msg the msg
	 */
	private void error(String msg) {
		IJ.error("Command Finder", msg);
	}

	/**
	 * Run command.
	 *
	 * @param command the command
	 */
	protected void runCommand(String command) {
		IJ.showStatus("Running command "+command);
		IJ.doCommand(command);
		closeWhenRunning = closeCheckBox.isSelected();
		if (closeWhenRunning)
			closeWindow();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		int flags = ke.getModifiers();
		int items = tableModel.getRowCount();
		Object source = ke.getSource();
		boolean meta = ((flags&KeyEvent.META_MASK) != 0) || ((flags&KeyEvent.CTRL_MASK) != 0);
		if (key==KeyEvent.VK_ESCAPE || (key==KeyEvent.VK_W&&meta)) {
			closeWindow();
		} else if (source==prompt) {
			/* If you hit enter in the text field, and
			   there's only one command that matches, run
			   that: */
			if (key==KeyEvent.VK_ENTER) {
				if (1==items)
					runCommand(tableModel.getCommand(0));
			}
			/* If you hit the up or down arrows in the
			   text field, move the focus to the
			   table and select the row at the
			   bottom or top. */
			int index = -1;
			if (key==KeyEvent.VK_UP) {
				index = table.getSelectedRow() - 1;
				if (index<0)
					index = items - 1;
			} else if (key==KeyEvent.VK_DOWN) {
				index = table.getSelectedRow() + 1;
				if (index>=items)
					index = Math.min(items-1, 0);
			}
			if (index>=0) {
				table.requestFocus();
				//completions.ensureIndexIsVisible(index);
				table.setRowSelectionInterval(index, index);
			}
		} else if (key==KeyEvent.VK_BACK_SPACE) {
			/* If someone presses backspace they probably want to
			   remove the last letter from the search string, so
			   switch the focus back to the prompt: */
			prompt.requestFocus();
		} else if (source==table) {
			/* If you hit enter with the focus in the table, run the selected command */
			if (key==KeyEvent.VK_ENTER) {
				ke.consume();
				int row = table.getSelectedRow();
				if (row>=0)
					runCommand(tableModel.getCommand(row));
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent ke) { }

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent ke) { }

	/**
	 * The listener interface for receiving promptDocument events.
	 * The class that is interested in processing a promptDocument
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPromptDocumentListener<code> method. When
	 * the promptDocument event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see PromptDocumentEvent
	 */
	class PromptDocumentListener implements DocumentListener {
		
		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		public void insertUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		public void removeUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		public void changedUpdate(DocumentEvent e) {
			populateList(prompt.getText());
		}
	}

	/* This function recurses down through a menu, adding to
	   commandsHash the location and MenuItem of any items it
	   finds that aren't submenus. */

	/**
	 * Parses the menu.
	 *
	 * @param path the path
	 * @param menu the menu
	 */
	public void parseMenu(String path, Menu menu) {
		int n=menu.getItemCount();
		for (int i=0; i<n; ++i) {
			MenuItem m=menu.getItem(i);
			String label=m.getActionCommand();
			if (m instanceof Menu) {
				Menu subMenu=(Menu)m;
				parseMenu(path+">"+label,subMenu);
			} else {
				String trimmedLabel = label.trim();
				if (trimmedLabel.length()==0 || trimmedLabel.equals("-"))
					continue;
				CommandAction ca=(CommandAction)commandsHash.get(label);
				if( ca == null )
					commandsHash.put(label, new CommandAction(null,m,path));
				else {
					ca.menuItem=m;
					ca.menuLocation=path;
				}
				CommandAction caAfter=(CommandAction)commandsHash.get(label);
			}
		}
	}

	/* Finds all the top level menus from the menu bar and
	   recurses down through each. */

	/**
	 * Find all menu items.
	 */
	public void findAllMenuItems() {
		MenuBar menuBar = Menus.getMenuBar();
		int topLevelMenus = menuBar.getMenuCount();
		for (int i=0; i<topLevelMenus; ++i) {
			Menu topLevelMenu=menuBar.getMenu(i);
			parseMenu(topLevelMenu.getLabel(), topLevelMenu);
		}
	}

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String ignored) {
		if (frame!=null) {
			WindowManager.toFront(frame);
			return;
		}
		commandsHash = new Hashtable();

		/* Find the "normal" commands; those which are
		   registered plugins: */
		Hashtable realCommandsHash = (Hashtable)(ij.Menus.getCommands().clone());
		Set realCommandSet = realCommandsHash.keySet();
		for (Iterator i = realCommandSet.iterator();
		     i.hasNext();) {
			String command = (String)i.next();
			// Some of these are whitespace only or separators - ignore them:
			String trimmedCommand = command.trim();
			if (trimmedCommand.length()>0 && !trimmedCommand.equals("-")) {
				commandsHash.put(command,
						 new CommandAction((String)realCommandsHash.get(command), null, null));
			}
		}

		/* There are some menu items that don't correspond to
		   plugins, such as those added by RefreshScripts, so
		   look through all the menus as well: */
		findAllMenuItems();

		/* Sort the commands, generate list labels for each
		   and put them into a hash: */
		commands = (String[])commandsHash.keySet().toArray(new String[0]);
		Arrays.sort(commands);

		/* The code below just constructs the dialog: */
		ImageJ imageJ = IJ.getInstance();
		frame = new JFrame("Command Finder") {
			public void setVisible(boolean visible) {
				if (visible)
					WindowManager.addWindow(this);
				super.setVisible(visible);
			}
			public void dispose() {
				WindowManager.removeWindow(this);
				Prefs.set("command-finder.close", closeWhenRunning);
				frame = null;
				super.dispose();
			}
		};
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		frame.addWindowListener(this);
		if (imageJ!=null && !IJ.isMacOSX()) {
			Image img = imageJ.getIconImage();
			if (img!=null)
				try {frame.setIconImage(img);} catch (Exception e) {}
		}


		closeCheckBox = new JCheckBox("Close window after running command", closeWhenRunning);
		closeCheckBox.addItemListener(this);

		JPanel northPanel = new JPanel();
		northPanel.add(new JLabel("Search:"));
		prompt = new JTextField("", 20);
		prompt.getDocument().addDocumentListener(new PromptDocumentListener());
		prompt.addKeyListener(this);
		northPanel.add(prompt);
		contentPane.add(northPanel, BorderLayout.NORTH);

		tableModel = new TableModel();
		table = new JTable(tableModel);
		//table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		//table.setAutoCreateRowSorter(true);
		tableModel.setColumnWidths(table.getColumnModel());
		Dimension dim = new Dimension(TABLE_WIDTH, table.getRowHeight()*TABLE_ROWS);
		table.setPreferredScrollableViewportSize(dim);
		table.addKeyListener(this);
		table.addMouseListener(this);

		scrollPane = new JScrollPane(table);
		populateList("");
		contentPane.add(scrollPane, BorderLayout.CENTER);

		runButton = new JButton("Run");
		sourceButton = new JButton("Source");
		closeButton = new JButton("Close");
		runButton.addActionListener(this);
		sourceButton.addActionListener(this);
		closeButton.addActionListener(this);
		runButton.addKeyListener(this);
		sourceButton.addKeyListener(this);
		closeButton.addKeyListener(this);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());

		JPanel optionsPanel = new JPanel();
		optionsPanel.add(closeCheckBox);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(runButton);
		buttonsPanel.add(sourceButton);
		buttonsPanel.add(closeButton);

		southPanel.add(optionsPanel, BorderLayout.CENTER);
		southPanel.add(buttonsPanel, BorderLayout.SOUTH);

		contentPane.add(southPanel, BorderLayout.SOUTH);

		Dimension screenSize = IJ.getScreenSize();

		frame.pack();

		int dialogWidth = frame.getWidth();
		int dialogHeight = frame.getHeight();
		int screenWidth = (int)screenSize.getWidth();
		int screenHeight = (int)screenSize.getHeight();

		Point pos = imageJ.getLocationOnScreen();
		Dimension size = imageJ.getSize();

		/* Generally try to position the dialog slightly
		   offset from the main ImageJ window, but if that
		   would push the dialog off to the screen to any
		   side, adjust it so that it's on the screen.
		*/
		int initialX = (int)pos.getX() + 10;
		int initialY = (int)pos.getY() + size.height+10;

		if (initialX+dialogWidth>screenWidth)
			initialX = screenWidth-dialogWidth;
		if (initialX<0)
			initialX = 0;
		if (initialY+dialogHeight>screenHeight)
			initialY = screenHeight-dialogHeight;
		if (initialY<0)
			initialY = 0;

		frame.setLocation(initialX,initialY);
		frame.setVisible(true);
		frame.toFront();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	/* Make sure that clicks on the close icon close the window: */
	public void windowClosing(WindowEvent e) {
		closeWindow();
	}
	
	/**
	 * Close window.
	 */
	private void closeWindow() {
		frame.dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && frame!=null) {
			IJ.wait(10);
			frame.setMenuBar(Menus.getMenuBar());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) { }
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) { }
	
	
	/**
	 * The Class TableModel.
	 */
	private class TableModel extends AbstractTableModel {
		
		/** The list. */
		protected ArrayList list;
		
		/** The Constant COLUMNS. */
		public final static int COLUMNS = 4;

		/**
		 * Instantiates a new table model.
		 */
		public TableModel() {
			list = new ArrayList();
		}

		/**
		 * Sets the data.
		 *
		 * @param list the new data
		 */
		public void setData(ArrayList list) {
			this.list = list;
			fireTableDataChanged();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return COLUMNS;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) {
			switch (column) {
				case 0: return "Command";
				case 1: return "Menu Path";
				case 2: return "Class";
				case 3: return "File";
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return list.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column) {
			if (row>=list.size() || column>=COLUMNS)
				return null;
			String[] strings = (String[])list.get(row);
			return strings[column];
		}
		
		/**
		 * Gets the command.
		 *
		 * @param row the row
		 * @return the command
		 */
		public String getCommand(int row) {
			if (row<0 || row>=list.size())
				return "";
			else
				return (String)getValueAt(row, 0);
		}

		/**
		 * Sets the column widths.
		 *
		 * @param columnModel the new column widths
		 */
		public void setColumnWidths(TableColumnModel columnModel) {
			int[] widths = {170, 150, 170, 30};
			for (int i=0; i<widths.length; i++)
				columnModel.getColumn(i).setPreferredWidth(widths[i]);
		}

	}

}
