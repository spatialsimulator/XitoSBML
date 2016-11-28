package ij;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.ProgressBar;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.macro.Interpreter;
import ij.macro.MacroRunner;
import ij.plugin.JavaProperties;
import ij.plugin.MacroInstaller;
import ij.plugin.Orthogonal_Views;
import ij.plugin.Startup;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.ContrastAdjuster;
import ij.plugin.frame.Editor;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.ThresholdAdjuster;
import ij.text.TextWindow;
import ij.util.Tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuComponent;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
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
import java.awt.image.ImageProducer;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;

// TODO: Auto-generated Javadoc
/**
This frame is the main ImageJ class.
<p>
ImageJ is a work of the United States Government. It is in the public domain 
and open source. There is no copyright. You are free to do anything you want
with this source but I like to get credit for my work and I would like you to 
offer your changes to me so I can possibly add them to the "official" version.

<pre>
The following command line options are recognized by ImageJ:

  "file-name"
     Opens a file
     Example 1: blobs.tif
     Example 2: /Users/wayne/images/blobs.tif
     Example 3: e81*.tif

  -macro path [arg]
     Runs a macro or script (JavaScript, BeanShell or Python), passing an
     optional string argument, which the macro or script can be retrieve
     using the getArgument() function. The macro or script is assumed to 
     be in the ImageJ/macros folder if 'path' is not a full directory path.
     Example 1: -macro analyze.ijm
     Example 2: -macro script.js /Users/wayne/images/stack1
     Example 2: -macro script.py '1.2 2.4 3.8'

  -batch path [arg]
    Runs a macro or script (JavaScript, BeanShell or Python) in
    batch (no GUI) mode, passing it an optional argument.
    ImageJ exits when the macro finishes.

  -eval "macro code"
     Evaluates macro code
     Example 1: -eval "print('Hello, world');"
     Example 2: -eval "return getVersion();"

  -run command
     Runs an ImageJ menu command
     Example: -run "About ImageJ..."
     
  -ijpath path
     Specifies the path to the directory containing the plugins directory
     Example: -ijpath /Applications/ImageJ

  -port<n>
     Specifies the port ImageJ uses to determine if another instance is running
     Example 1: -port1 (use default port address + 1)
     Example 2: -port2 (use default port address + 2)
     Example 3: -port0 (don't check for another instance)

  -debug
     Runs ImageJ in debug mode
</pre>
@author Wayne Rasband (wsr@nih.gov)
*/
public class ImageJ extends Frame implements ActionListener, 
	MouseListener, KeyListener, WindowListener, ItemListener, Runnable {

	/** Plugins should call IJ.getVersion() or IJ.getFullVersion() to get the version string. */
	public static final String VERSION = "1.49o";
	
	/** The Constant BUILD. */
	public static final String BUILD = ""; 
	
	/** The background color. */
	public static Color backgroundColor = new Color(237,237,237);
	/** SansSerif, 12-point, plain font. */
	public static final Font SansSerif12 = new Font("SansSerif", Font.PLAIN, 12);
	
	/**  Address of socket where Image accepts commands. */
	public static final int DEFAULT_PORT = 57294;
	
	/** Run as normal application. */
	public static final int STANDALONE = 0;
	
	/** Run embedded in another application. */
	public static final int EMBEDDED = 1;
	
	/** Run embedded and invisible in another application. */
	public static final int NO_SHOW = 2;
	
	/** Run ImageJ in debug mode. */
	public static final int DEBUG = 256;

	/** The Constant IJ_Y. */
	private static final String IJ_X="ij.x",IJ_Y="ij.y";
	
	/** The port. */
	private static int port = DEFAULT_PORT;
	
	/** The arguments. */
	private static String[] arguments;
	
	/** The toolbar. */
	private Toolbar toolbar;
	
	/** The status bar. */
	private Panel statusBar;
	
	/** The progress bar. */
	private ProgressBar progressBar;
	
	/** The status line. */
	private Label statusLine;
	
	/** The first time. */
	private boolean firstTime = true;
	
	/** The applet. */
	private java.applet.Applet applet; // null if not running as an applet
	
	/** The classes. */
	private Vector classes = new Vector();
	
	/** The exit when quitting. */
	private boolean exitWhenQuitting;
	
	/** The quitting. */
	private boolean quitting;
	
	/** The action performed time. */
	private long keyPressedTime, actionPerformedTime;
	
	/** The last key command. */
	private String lastKeyCommand;
	
	/** The embedded. */
	private boolean embedded;
	
	/** The window closed. */
	private boolean windowClosed;
	
	/** The command name. */
	private static String commandName;
		
	/** The hotkey. */
	boolean hotkey;
	
	/** Creates a new ImageJ frame that runs as an application. */
	public ImageJ() {
		this(null, STANDALONE);
	}
	
	/**
	 *  Creates a new ImageJ frame that runs as an application in the specified mode.
	 *
	 * @param mode the mode
	 */
	public ImageJ(int mode) {
		this(null, mode);
	}

	/**
	 *  Creates a new ImageJ frame that runs as an applet.
	 *
	 * @param applet the applet
	 */
	public ImageJ(java.applet.Applet applet) {
		this(applet, STANDALONE);
	}

	/**
	 *  If 'applet' is not null, creates a new ImageJ frame that runs as an applet.
	 * 		If  'mode' is ImageJ.EMBEDDED and 'applet is null, creates an embedded 
	 * 		(non-standalone) version of ImageJ.
	 *
	 * @param applet the applet
	 * @param mode the mode
	 */
	public ImageJ(java.applet.Applet applet, int mode) {
		super("ImageJ");
		if ((mode&DEBUG)!=0)
			IJ.setDebugMode(true);
		mode = mode & 255;
		if (IJ.debugMode) IJ.log("ImageJ starting in debug mode: "+mode);
		embedded = applet==null && (mode==EMBEDDED||mode==NO_SHOW);
		this.applet = applet;
		String err1 = Prefs.load(this, applet);
		setBackground(backgroundColor);
		Menus m = new Menus(this, applet);
		String err2 = m.addMenuBar();
		m.installPopupMenu(this);
		setLayout(new BorderLayout());
		
		// Tool bar
		toolbar = new Toolbar();
		toolbar.addKeyListener(this);
		add("Center", toolbar);

		// Status bar
		statusBar = new Panel();
		statusBar.setLayout(new BorderLayout());
		statusBar.setForeground(Color.black);
		statusBar.setBackground(backgroundColor);
		statusLine = new Label();
		statusLine.setFont(new Font("SansSerif", Font.PLAIN, 13));
		statusLine.addKeyListener(this);
		statusLine.addMouseListener(this);
		statusBar.add("Center", statusLine);
		progressBar = new ProgressBar(120, 20);
		progressBar.addKeyListener(this);
		progressBar.addMouseListener(this);
		statusBar.add("East", progressBar);
		add("South", statusBar);

		IJ.init(this, applet);
		addKeyListener(this);
		addWindowListener(this);
		setFocusTraversalKeysEnabled(false);
		m.installStartupMacroSet(); //add custom tools
		runStartupMacro();
 		
		Point loc = getPreferredLocation();
		Dimension tbSize = toolbar.getPreferredSize();
		setCursor(Cursor.getDefaultCursor()); // work-around for JDK 1.1.8 bug
		if (mode!=NO_SHOW) {
			if (IJ.isWindows()) try {setIcon();} catch(Exception e) {}
			setLocation(loc.x, loc.y);
			setResizable(!IJ.isMacOSX());
			pack();
			setVisible(true);
		}
		if (err1!=null)
			IJ.error(err1);
		if (err2!=null) {
			IJ.error(err2);
			IJ.runPlugIn("ij.plugin.ClassChecker", "");
		}
		if (IJ.isMacintosh()&&applet==null) { 
			Object qh = null; 
			qh = IJ.runPlugIn("MacAdapter", ""); 
			if (qh==null) 
				IJ.runPlugIn("QuitHandler", ""); 
		} 
		if (applet==null)
			IJ.runPlugIn("ij.plugin.DragAndDrop", "");
		String str = m.getMacroCount()==1?" macro":" macros";
		IJ.showStatus(version()+ m.getPluginCount() + " commands; " + m.getMacroCount() + str);
		configureProxy();
		if (applet==null)
			loadCursors();
 	}
 	
 	/**
	  * Run startup macro.
	  */
	 private void runStartupMacro() {
 		String macro = (new Startup()).getStartupMacro();
 		if (macro!=null && macro.length()>4)
 			new MacroRunner(macro);
 	}
 	
 	/**
	  * Load cursors.
	  */
	 private void loadCursors() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		String path = Prefs.getImageJDir()+"images/crosshair-cursor.gif";
		File f = new File(path);
		if (!f.exists())
			return;
		//Image image = toolkit.getImage(path);
		ImageIcon icon = new ImageIcon(path);
		Image image = icon.getImage();
		if (image==null)
			return;
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		Point hotSpot = new Point(width/2, height/2);
		Cursor crosshairCursor = toolkit.createCustomCursor(image, hotSpot, "crosshair-cursor.gif");
		ImageCanvas.setCursor(crosshairCursor, 0);
	}
    	
	/**
	 * Configure proxy.
	 */
	void configureProxy() {
		if (Prefs.useSystemProxies) {
			try {
				System.setProperty("java.net.useSystemProxies", "true");
			} catch(Exception e) {}
		} else {
			String server = Prefs.get("proxy.server", null);
			if (server==null||server.equals(""))
				return;
			int port = (int)Prefs.get("proxy.port", 0);
			if (port==0) return;
			Properties props = System.getProperties();
			props.put("proxySet", "true");
			props.put("http.proxyHost", server);
			props.put("http.proxyPort", ""+port);
		}
		//new ProxySettings().logProperties();
	}
	
    /**
     * Sets the icon.
     *
     * @throws Exception the exception
     */
    void setIcon() throws Exception {
		URL url = this.getClass().getResource("/microscope.gif");
		if (url==null) return;
		Image img = createImage((ImageProducer)url.getContent());
		if (img!=null) setIconImage(img);
	}
	
	/**
	 * Gets the preferred location.
	 *
	 * @return the preferred location
	 */
	public Point getPreferredLocation() {
		Rectangle maxBounds = GUI.getMaxWindowBounds();
		int ijX = Prefs.getInt(IJ_X,-99);
		int ijY = Prefs.getInt(IJ_Y,-99);
		if (ijX>=maxBounds.x && ijY>=maxBounds.y && ijX<(maxBounds.x+maxBounds.width-75))
			return new Point(ijX, ijY);
		Dimension tbsize = toolbar.getPreferredSize();
		int ijWidth = tbsize.width+10;
		double percent = maxBounds.width>832?0.8:0.9;
		ijX = (int)(percent*(maxBounds.width-ijWidth));
		if (ijX<10) ijX = 10;
		return new Point(ijX, maxBounds.y);
	}
	
	/**
	 * Show status.
	 *
	 * @param s the s
	 */
	void showStatus(String s) {
        statusLine.setText(s);
	}

	/**
	 * Gets the progress bar.
	 *
	 * @return the progress bar
	 */
	public ProgressBar getProgressBar() {
        return progressBar;
	}

	/**
	 * Gets the status bar.
	 *
	 * @return the status bar
	 */
	public Panel getStatusBar() {
        return statusBar;
	}

    /**
     *  Starts executing a menu command in a separate thread.
     *
     * @param name the name
     */
    void doCommand(String name) {
		new Executer(name, null);
    }
        
	/**
	 * Run filter plug in.
	 *
	 * @param theFilter the the filter
	 * @param cmd the cmd
	 * @param arg the arg
	 */
	public void runFilterPlugIn(Object theFilter, String cmd, String arg) {
		new PlugInFilterRunner(theFilter, cmd, arg);
	}
        
	/**
	 * Run user plug in.
	 *
	 * @param commandName the command name
	 * @param className the class name
	 * @param arg the arg
	 * @param createNewLoader the create new loader
	 * @return the object
	 */
	public Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
		return IJ.runUserPlugIn(commandName, className, arg, createNewLoader);	
	} 
	
	/**
	 *  Return the current list of modifier keys.
	 *
	 * @param flags the flags
	 * @return the string
	 */
	public static String modifiers(int flags) { //?? needs to be moved
		String s = " [ ";
		if (flags == 0) return "";
		if ((flags & Event.SHIFT_MASK) != 0) s += "Shift ";
		if ((flags & Event.CTRL_MASK) != 0) s += "Control ";
		if ((flags & Event.META_MASK) != 0) s += "Meta ";
		if ((flags & Event.ALT_MASK) != 0) s += "Alt ";
		s += "] ";
		return s;
	}

	/**
	 *  Handle menu events.
	 *
	 * @param e the e
	 */
	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() instanceof MenuItem)) {
			MenuItem item = (MenuItem)e.getSource();
			String cmd = e.getActionCommand();
			commandName = cmd;
			ImagePlus imp = null;
			if (item.getParent()==Menus.getOpenRecentMenu()) {
				new RecentOpener(cmd); // open image in separate thread
				return;
			} else if (item.getParent()==Menus.getPopupMenu()) {
				Object parent = Menus.getPopupMenu().getParent();
				if (parent instanceof ImageCanvas)
					imp = ((ImageCanvas)parent).getImage();
			}
			int flags = e.getModifiers();
			hotkey = false;
			actionPerformedTime = System.currentTimeMillis();
			long ellapsedTime = actionPerformedTime-keyPressedTime;
			if (cmd!=null && (ellapsedTime>=200L||!cmd.equals(lastKeyCommand))) {
				if ((flags & Event.ALT_MASK)!=0)
					IJ.setKeyDown(KeyEvent.VK_ALT);
				if ((flags & Event.SHIFT_MASK)!=0)
					IJ.setKeyDown(KeyEvent.VK_SHIFT);
				new Executer(cmd, imp);
			}
			lastKeyCommand = null;
			if (IJ.debugMode) IJ.log("actionPerformed: time="+ellapsedTime+", "+e);
		}
	}

	/**
	 *  Handles CheckboxMenuItem state changes.
	 *
	 * @param e the e
	 */
	public void itemStateChanged(ItemEvent e) {
		MenuItem item = (MenuItem)e.getSource();
		MenuComponent parent = (MenuComponent)item.getParent();
		String cmd = e.getItem().toString();
		if ((Menu)parent==Menus.window)
			WindowManager.activateWindow(cmd, item);
		else
			doCommand(cmd);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		Undo.reset();
		if (!Prefs.noClickToGC)
			System.gc();
		IJ.showStatus(version()+IJ.freeMemory());
		if (IJ.debugMode)
			IJ.log("Windows: "+WindowManager.getWindowCount());
	}
	
	/**
	 * Gets the info.
	 *
	 * @return the info
	 */
	public String getInfo() {
		return version()+System.getProperty("os.name")+" "+System.getProperty("os.version")+"; "+IJ.freeMemory();
	}

	/**
	 * Version.
	 *
	 * @return the string
	 */
	private String version() {
		return "ImageJ "+VERSION+BUILD + "; "+"Java "+System.getProperty("java.version")+(IJ.is64Bit()?" [64-bit]; ":" [32-bit]; ");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {}

 	/* (non-Javadoc)
	  * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	  */
	 public void keyPressed(KeyEvent e) {
 		//if (e.isConsumed()) return;
		int keyCode = e.getKeyCode();
		IJ.setKeyDown(keyCode);
		hotkey = false;
		if (keyCode==KeyEvent.VK_CONTROL || keyCode==KeyEvent.VK_SHIFT)
			return;
		char keyChar = e.getKeyChar();
		int flags = e.getModifiers();
		if (IJ.debugMode) IJ.log("keyPressed: code=" + keyCode + " (" + KeyEvent.getKeyText(keyCode)
			+ "), char=\"" + keyChar + "\" (" + (int)keyChar + "), flags="
			+ KeyEvent.getKeyModifiersText(flags));
		boolean shift = (flags & KeyEvent.SHIFT_MASK) != 0;
		boolean control = (flags & KeyEvent.CTRL_MASK) != 0;
		boolean alt = (flags & KeyEvent.ALT_MASK) != 0;
		boolean meta = (flags & KeyEvent.META_MASK) != 0;
		String cmd = null;
		ImagePlus imp = WindowManager.getCurrentImage();
		boolean isStack = (imp!=null) && (imp.getStackSize()>1);
		
		if (imp!=null && !control && ((keyChar>=32 && keyChar<=255) || keyChar=='\b' || keyChar=='\n')) {
			Roi roi = imp.getRoi();
			if (roi instanceof TextRoi) {
				if ((flags & KeyEvent.META_MASK)!=0 && IJ.isMacOSX()) return;
				if (alt) {
					switch (keyChar) {
						case 'u': case 'm': keyChar = IJ.micronSymbol; break;
						case 'A': keyChar = IJ.angstromSymbol; break;
						default:
					}
				}
				((TextRoi)roi).addChar(keyChar);
				return;
			}
		}
        		
		// Handle one character macro shortcuts
		if (!control && !meta) {
			Hashtable macroShortcuts = Menus.getMacroShortcuts();
			if (macroShortcuts.size()>0) {
				if (shift)
					cmd = (String)macroShortcuts.get(new Integer(keyCode+200));
				else
					cmd = (String)macroShortcuts.get(new Integer(keyCode));
				if (cmd!=null) {
					//MacroInstaller.runMacroCommand(cmd);
					commandName = cmd;
					MacroInstaller.runMacroShortcut(cmd);
					return;
				}
			}
		}

		if ((!Prefs.requireControlKey || control || meta) && keyChar!='+') {
			Hashtable shortcuts = Menus.getShortcuts();
			if (shift)
				cmd = (String)shortcuts.get(new Integer(keyCode+200));
			else
				cmd = (String)shortcuts.get(new Integer(keyCode));
		}
		
		if (cmd==null) {
			switch (keyChar) {
				case '<': case ',': if (isStack) cmd="Previous Slice [<]"; break;
				case '>': case '.': case ';': if (isStack) cmd="Next Slice [>]"; break;
				case '+': case '=': cmd="In [+]"; break;
				case '-': cmd="Out [-]"; break;
				case '/': cmd="Reslice [/]..."; break;
				default:
			}
		}

		if (cmd==null) {
			switch (keyCode) {
				case KeyEvent.VK_TAB: WindowManager.putBehind(); return;
				case KeyEvent.VK_BACK_SPACE: // delete
					if (deleteOverlayRoi(imp))
						return;
					cmd="Clear";
					hotkey=true;
					break; 
				//case KeyEvent.VK_BACK_SLASH: cmd=IJ.altKeyDown()?"Animation Options...":"Start Animation"; break;
				case KeyEvent.VK_EQUALS: cmd="In [+]"; break;
				case KeyEvent.VK_MINUS: cmd="Out [-]"; break;
				case KeyEvent.VK_SLASH: case 0xbf: cmd="Reslice [/]..."; break;
				case KeyEvent.VK_COMMA: case 0xbc: if (isStack) cmd="Previous Slice [<]"; break;
				case KeyEvent.VK_PERIOD: case 0xbe: if (isStack) cmd="Next Slice [>]"; break;
				case KeyEvent.VK_LEFT: case KeyEvent.VK_RIGHT: case KeyEvent.VK_UP: case KeyEvent.VK_DOWN: // arrow keys
					if (imp==null) return;
					Roi roi = imp.getRoi();
					if (IJ.shiftKeyDown()&&imp==Orthogonal_Views.getImage())
						return;
					boolean stackKey = imp.getStackSize()>1 && (roi==null||IJ.shiftKeyDown());
					boolean zoomKey = roi==null || IJ.shiftKeyDown() || IJ.controlKeyDown();
					if (stackKey && keyCode==KeyEvent.VK_RIGHT)
							cmd="Next Slice [>]";
					else if (stackKey && keyCode==KeyEvent.VK_LEFT)
							cmd="Previous Slice [<]";
					else if (zoomKey && keyCode==KeyEvent.VK_DOWN && !ignoreArrowKeys(imp) && Toolbar.getToolId()<Toolbar.SPARE6)
							cmd="Out [-]";
					else if (zoomKey && keyCode==KeyEvent.VK_UP && !ignoreArrowKeys(imp) && Toolbar.getToolId()<Toolbar.SPARE6)
							cmd="In [+]";
					else if (roi!=null) {
						if ((flags & KeyEvent.ALT_MASK) != 0)
							roi.nudgeCorner(keyCode);
						else
							roi.nudge(keyCode);
						return;
					}
					break;
				case KeyEvent.VK_ESCAPE:
					abortPluginOrMacro(imp);
					return;
				case KeyEvent.VK_ENTER: WindowManager.toFront(this); return;
				default: break;
			}
		}
		
		if (cmd!=null && !cmd.equals("")) {
			commandName = cmd;
			if (cmd.equals("Fill")||cmd.equals("Draw"))
				hotkey = true;
			if (cmd.charAt(0)==MacroInstaller.commandPrefix)
				MacroInstaller.runMacroShortcut(cmd);
			else {
				doCommand(cmd);
				keyPressedTime = System.currentTimeMillis();
				lastKeyCommand = cmd;
			}
		}
	}
	
	/**
	 * Delete overlay roi.
	 *
	 * @param imp the imp
	 * @return true, if successful
	 */
	private boolean deleteOverlayRoi(ImagePlus imp) {
		if (imp==null)
			return false;
		Overlay overlay = null;
		ImageCanvas ic = imp.getCanvas();
		if (ic!=null)
			overlay = ic.getShowAllList();
		if (overlay==null)
			overlay = imp.getOverlay();
		if (overlay==null)
			return false;
		Roi roi = imp.getRoi();
		for (int i=0; i<overlay.size(); i++) {
			Roi roi2 = overlay.get(i);
			if (roi2==roi) {
				overlay.remove(i);
				imp.deleteRoi();
				ic = imp.getCanvas();
				if (ic!=null)
					ic.roiManagerSelect(roi, true);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Ignore arrow keys.
	 *
	 * @param imp the imp
	 * @return true, if successful
	 */
	private boolean ignoreArrowKeys(ImagePlus imp) {
		Frame frame = WindowManager.getFrontWindow();
		String title = frame.getTitle();
		if (title!=null && title.equals("ROI Manager"))
			return true;
		// Control Panel?
		if (frame!=null && frame instanceof javax.swing.JFrame)
			return true;
		ImageWindow win = imp.getWindow();
		// LOCI Data Browser window?
		if (imp.getStackSize()>1 && win!=null && win.getClass().getName().startsWith("loci"))
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		char keyChar = e.getKeyChar();
		int flags = e.getModifiers();
		if (IJ.debugMode) IJ.log("keyTyped: char=\"" + keyChar + "\" (" + (int)keyChar 
			+ "), flags= "+Integer.toHexString(flags)+ " ("+KeyEvent.getKeyModifiersText(flags)+")");
		if (keyChar=='\\' || keyChar==171 || keyChar==223) {
			if (((flags&Event.ALT_MASK)!=0))
				doCommand("Animation Options...");
			else
				doCommand("Start Animation [\\]");
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		IJ.setKeyUp(e.getKeyCode());
	}
		
	/**
	 * Abort plugin or macro.
	 *
	 * @param imp the imp
	 */
	void abortPluginOrMacro(ImagePlus imp) {
		if (imp!=null) {
			ImageWindow win = imp.getWindow();
			if (win!=null) {
				win.running = false;
				win.running2 = false;
			}
		}
		Macro.abort();
		Interpreter.abort();
		if (Interpreter.getInstance()!=null) IJ.beep();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if (Executer.getListenerCount()>0)
			doCommand("Quit");
		else {
			quit();
			windowClosed = true;
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		if (IJ.isMacintosh() && !quitting) {
			IJ.wait(10); // may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {}
	
	/**
	 *  Adds the specified class to a Vector to keep it from being
	 * 		garbage collected, causing static fields to be reset.
	 *
	 * @param c the c
	 */
	public void register(Class c) {
		if (!classes.contains(c))
			classes.addElement(c);
	}

	/** Called by ImageJ when the user selects Quit. */
	public void quit() {
		Thread thread = new Thread(this, "Quit");
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
		IJ.wait(10);
	}
	
	/**
	 *  Returns true if ImageJ is exiting.
	 *
	 * @return true, if successful
	 */
	public boolean quitting() {
		return quitting;
	}
	
	/**
	 *  Called once when ImageJ quits.
	 *
	 * @param prefs the prefs
	 */
	public void savePreferences(Properties prefs) {
		Point loc = getLocation();
		prefs.put(IJ_X, Integer.toString(loc.x));
		prefs.put(IJ_Y, Integer.toString(loc.y));
		//prefs.put(IJ_WIDTH, Integer.toString(size.width));
		//prefs.put(IJ_HEIGHT, Integer.toString(size.height));
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]) {
		if (System.getProperty("java.version").substring(0,3).compareTo("1.5")<0) {
			javax.swing.JOptionPane.showMessageDialog(null,"ImageJ "+VERSION+" requires Java 1.5 or later.");
			System.exit(0);
		}
		boolean noGUI = false;
		int mode = STANDALONE;
		arguments = args;
		//System.setProperty("file.encoding", "UTF-8");
		int nArgs = args!=null?args.length:0;
		boolean commandLine = false;
		for (int i=0; i<nArgs; i++) {
			String arg = args[i];
			if (arg==null) continue;
			if (args[i].startsWith("-")) {
				if (args[i].startsWith("-batch"))
					noGUI = true;
				else if (args[i].startsWith("-debug"))
					IJ.setDebugMode(true);
				else if (args[i].startsWith("-ijpath") && i+1<nArgs) {
					if (IJ.debugMode) IJ.log("-ijpath: "+args[i+1]);
					Prefs.setHomeDir(args[i+1]);
					commandLine = true;
					args[i+1] = null;
				} else if (args[i].startsWith("-port")) {
					int delta = (int)Tools.parseDouble(args[i].substring(5, args[i].length()), 0.0);
					commandLine = true;
					if (delta==0)
						mode = EMBEDDED;
					else if (delta>0 && DEFAULT_PORT+delta<65536)
						port = DEFAULT_PORT+delta;
				}
			} 
		}
  		// If existing ImageJ instance, pass arguments to it and quit.
  		boolean passArgs = mode==STANDALONE && !noGUI;
		if (IJ.isMacOSX() && !commandLine) passArgs = false;
		if (passArgs && isRunning(args)) 
  			return;
 		ImageJ ij = IJ.getInstance();    	
		if (!noGUI && (ij==null || (ij!=null && !ij.isShowing()))) {
			ij = new ImageJ(null, mode);
			ij.exitWhenQuitting = true;
		}
		int macros = 0;
		for (int i=0; i<nArgs; i++) {
			String arg = args[i];
			if (arg==null) continue;
			if (arg.startsWith("-")) {
				if ((arg.startsWith("-macro") || arg.startsWith("-batch")) && i+1<nArgs) {
					String arg2 = i+2<nArgs?args[i+2]:null;
					Prefs.commandLineMacro = true;
					if (noGUI && args[i+1].endsWith(".js"))
						Interpreter.batchMode = true;
					IJ.runMacroFile(args[i+1], arg2);
					break;
				} else if (arg.startsWith("-eval") && i+1<nArgs) {
					String rtn = IJ.runMacro(args[i+1]);
					if (rtn!=null)
						System.out.print(rtn);
					args[i+1] = null;
				} else if (arg.startsWith("-run") && i+1<nArgs) {
					IJ.run(args[i+1]);
					args[i+1] = null;
				}
			} else if (macros==0 && (arg.endsWith(".ijm") || arg.endsWith(".txt"))) {
				IJ.runMacroFile(arg);
				macros++;
			} else if (arg.length()>0 && arg.indexOf("ij.ImageJ")==-1) {
				File file = new File(arg);
				IJ.open(file.getAbsolutePath());
			}
		}
		if (IJ.debugMode && IJ.getInstance()==null)
			new JavaProperties().run("");
		if (noGUI) System.exit(0);
	}
		
	/**
	 * Checks if is running.
	 *
	 * @param args the args
	 * @return true, if is running
	 */
	// Is there another instance of ImageJ? If so, send it the arguments and quit.
	static boolean isRunning(String args[]) {
		return OtherInstance.sendArguments(args);
	}

	/**
	 *  Returns the port that ImageJ checks on startup to see if another instance is running.
	 *
	 * @return the port
	 * @see ij.OtherInstance
	 */
	public static int getPort() {
		return port;
	}
	
	/**
	 *  Returns the command line arguments passed to ImageJ.
	 *
	 * @return the args
	 */
	public static String[] getArgs() {
		return arguments;
	}

	/**
	 *  ImageJ calls System.exit() when qutting when 'exitWhenQuitting' is true.
	 *
	 * @param ewq the ewq
	 */
	public void exitWhenQuitting(boolean ewq) {
		exitWhenQuitting = ewq;
	}
	
	/** Quit using a separate thread, hopefully avoiding thread deadlocks. */
	public void run() {
		quitting = true;
		boolean changes = false;
		int[] wList = WindowManager.getIDList();
		if (wList!=null) {
			for (int i=0; i<wList.length; i++) {
				ImagePlus imp = WindowManager.getImage(wList[i]);
				if (imp!=null && imp.changes==true) {
					changes = true;
					break;
				}
			}
		}
		Frame[] frames = WindowManager.getNonImageWindows();
		if (frames!=null) {
			for (int i=0; i<frames.length; i++) {
				if (frames[i]!=null && (frames[i] instanceof Editor)) {
					if (((Editor)frames[i]).fileChanged()) {
						changes = true;
						break;
					}
				}
			}
		}
		if (windowClosed && !changes && Menus.window.getItemCount()>Menus.WINDOW_MENU_ITEMS && !(IJ.macroRunning()&&WindowManager.getImageCount()==0)) {
			GenericDialog gd = new GenericDialog("ImageJ", this);
			gd.addMessage("Are you sure you want to quit ImageJ?");
			gd.showDialog();
			quitting = !gd.wasCanceled();
			windowClosed = false;
		}
		if (!quitting)
			return;
		if (!WindowManager.closeAllWindows()) {
			quitting = false;
			return;
		}
		if (applet==null) {
			saveWindowLocations();
			Prefs.savePreferences();
		}
		IJ.cleanup();
		dispose();
		if (exitWhenQuitting)
			System.exit(0);
	}
	
	/**
	 * Save window locations.
	 */
	void saveWindowLocations() {
		Window win = WindowManager.getWindow("B&C");
		if (win!=null)
			Prefs.saveLocation(ContrastAdjuster.LOC_KEY, win.getLocation());
		win = WindowManager.getWindow("Threshold");
		if (win!=null)
			Prefs.saveLocation(ThresholdAdjuster.LOC_KEY, win.getLocation());
		win = WindowManager.getWindow("Results");
		if (win!=null) {
			Prefs.saveLocation(TextWindow.LOC_KEY, win.getLocation());
			Dimension d = win.getSize();
			Prefs.set(TextWindow.WIDTH_KEY, d.width);
			Prefs.set(TextWindow.HEIGHT_KEY, d.height);
		}
		win = WindowManager.getWindow("Log");
		if (win!=null) {
			Prefs.saveLocation(TextWindow.LOG_LOC_KEY, win.getLocation());
			Dimension d = win.getSize();
			Prefs.set(TextWindow.LOG_WIDTH_KEY, d.width);
			Prefs.set(TextWindow.LOG_HEIGHT_KEY, d.height);
		}
		win = WindowManager.getWindow("ROI Manager");
		if (win!=null)
			Prefs.saveLocation(RoiManager.LOC_KEY, win.getLocation());
	}
	
	/**
	 * Gets the command name.
	 *
	 * @return the command name
	 */
	public static String getCommandName() {
		return commandName!=null?commandName:"null";
	}
	
	/**
	 * Sets the command name.
	 *
	 * @param name the new command name
	 */
	public static void setCommandName(String name) {
		commandName = name;
	}

}
