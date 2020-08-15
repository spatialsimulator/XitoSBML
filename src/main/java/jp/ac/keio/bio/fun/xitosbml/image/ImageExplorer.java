package jp.ac.keio.bio.fun.xitosbml.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import ij.ImagePlus;
import ij.gui.MessageDialog;
import ij.io.FileInfo;

/**
 * The class ImageExplorer, which inherits JFrame and implements table component
 * of XitoSBML. This class is used in
 * {@link jp.ac.keio.bio.fun.xitosbml.xitosbml.MainSpatial}, which creates a GUI
 * for XitoSBML. Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public class ImageExplorer extends JFrame implements ActionListener {

	/** The hashmap of domain types. HashMap&lt;String, Integer&gt; */
	private HashMap<String, Integer> hashDomainTypes;

	/**
	 * The hashmap of sampled value of spatial image. HashMap&lt;String, Integer&gt;
	 */
	private HashMap<String, Integer> hashSampledValues;

	/** The hashmap of domain file. HashMap&lt;String, ImagePlus&gt; */
	private HashMap<String, ImagePlus> hashDomFile;

	/** The file information of composite image. */
	private FileInfo compoInfo;

	/** The scrollpane. */
	private JScrollPane scroll;

	/**
	 * Instantiates a new image explorer.
	 */
	public ImageExplorer() {
		super("DomainType Namer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 500, 240);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}

	/**
	 * Instantiates a new image explorer with given hashmap of domain types and
	 * hashmap of sampled values.
	 *
	 * @param hashDomainTypes   the hashmap of domain types. HashMap&lt;String,
	 *                          Integer&gt;
	 * @param hashSampledValues the hashmap of sampled value of spatial image.
	 *                          HashMap&lt;String, Integer&gt;
	 */
	public ImageExplorer(HashMap<String, Integer> hashDomainTypes, HashMap<String, Integer> hashSampledValues) {
		this();
		this.hashDomainTypes = hashDomainTypes;
		this.hashSampledValues = hashSampledValues;

		ImageTable table = new ImageTable();

		// scrollbar
		scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setName("table");

		// button
		JButton ok = new JButton("OK"), plus = new JButton("+"), minus = new JButton("-");
		ok.addActionListener(this);
		plus.addActionListener(this);
		minus.addActionListener(this);
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(plus);
		p2.add(minus);
		p2.add(Box.createRigidArea(new Dimension(250, 0)));
		p2.add(ok);

		// set components
		getContentPane().add(p2, BorderLayout.PAGE_END);
		getContentPane().add(scroll, BorderLayout.CENTER);

		setVisible(true);
	}

	/**
	 * Sets the datatable(hashmap of domain file) to the hashmap of domain types and
	 * returns the hashmap of domain types.
	 *
	 * @return the hashmap of domain types. HashMap&lt;String, Integer&gt;
	 */
	public HashMap<String, Integer> getDomainTypes() {
		int dimension = 3;
		for (Entry<String, ImagePlus> e : hashDomFile.entrySet()) {
			if (e.getValue().getNSlices() == 1)
				dimension = 2;
			hashDomainTypes.put(e.getKey().toString(), dimension);
		}
		hashDomainTypes.put("Extracellular", dimension);
		return hashDomainTypes;
	}

	/**
	 * Sets the hashmap of sampled values and returns the hashmap of sampled values.
	 * The sampled value is calculated by the value of its domain type.
	 *
	 * @param table the ImageTable object
	 * @return the hashmap of sampled value of spatial image. HashMap&lt;String,
	 *         Integer&gt;
	 */
	public HashMap<String, Integer> getSampledValues(ImageTable table) {
		int pixel = 255;
		int interval = 255 / hashDomFile.size();
		for (int i = 0; i < table.getRowCount(); i++) {
			String s = (String) table.getValueAt(i, 0);
			if (hashDomFile.containsKey(s)) {
				hashSampledValues.put(s, pixel);
				pixel -= interval;
			}
		}
		hashSampledValues.put("Extracellular", 0);
		return hashSampledValues;
	}

	/**
	 * Example main() method which will launch a GUI and show this JFrame object.
	 *
	 * @param args an array of command-line arguments for the application
	 */
	public static void main(String[] args) {
		HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
		HashMap<String, Integer> hashSampledValues = new HashMap<String, Integer>();
		new ImageExplorer(hashDomainTypes, hashSampledValues);
	}

	/**
	 * Gets the hashmap of domain file. HashMap&lt;String, ImagePlus&gt;
	 *
	 * @return the hashmap of domain file.
	 */
	public HashMap<String, ImagePlus> getDomFile() {
		return hashDomFile;
	}

	/**
	 * Gets the file information of composite image.
	 *
	 * @return the file information of composite image
	 */
	public FileInfo getFileInfo() {
		if (compoInfo != null)
			return compoInfo;
		else
			return null;
	}

	/**
	 * The listener interface for receiving action events. Add a row if "+" is
	 * pressed, Remove the selected row if "-" is pressed. Import the selected image
	 * to the spatial model if "OK" is pressed.
	 *
	 * @param e the ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		JViewport viewport = scroll.getViewport();
		ImageTable table = (ImageTable) viewport.getView();

		if (input == "+")
			table.addRow();

		else if (input == "-")
			table.delRow();

		else if (input == "OK" && table.getImgNum() > 0) {
			hashDomFile = table.getHashDomFile();
			hashDomainTypes = getDomainTypes();
			hashSampledValues = getSampledValues(table);
			setVisible(false);
			dispose();
		} else
			new MessageDialog(new Frame(), "Error", "No Image");

	}

}