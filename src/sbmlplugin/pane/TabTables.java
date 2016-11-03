package sbmlplugin.pane;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;

import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.ListOfUnitDefinitions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
// TODO: Auto-generated Javadoc

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp> Date Created: Jan 12, 2016
 */
@SuppressWarnings("serial")
public class TabTables extends JFrame implements ActionListener {

	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** The tabbedpane. */
	private JTabbedPane tabbedpane = new JTabbedPane();
	
	/** The sbase list. */
	private ArrayList<SBaseTable> sbaseList = new ArrayList<SBaseTable>();
	
	/** The is running. */
	private boolean isRunning = true;	
	
	/**
	 * Instantiates a new tab tables.
	 */
	public TabTables() {
		super("Parameter and Species Table");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(500, 250);
		setResizable(false);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}

	/**
	 * Instantiates a new tab tables.
	 *
	 * @param model the model
	 */
	public TabTables(Model model) {
		this();
		ListOfParameters lop = model.getListOfParameters();
		ListOfSpecies los = model.getListOfSpecies();
		ListOfReactions lor = model.getListOfReactions();
		ListOfUnitDefinitions loud = model.getListOfUnitDefinitions();
		
		SpeciesTable stable = new SpeciesTable(los);		
		ParameterTable ptable = new ParameterTable(lop);		
		AdvectionTable atable = new AdvectionTable(lop);		
		BoundaryConditionTable bctable = new BoundaryConditionTable(lop);
		DiffusionTable dtable = new DiffusionTable(lop);
		ReactionTable rtable = new ReactionTable(lor);
<<<<<<< HEAD
	//	UnitDefinitionTable udtable = new UnitDefinitionTable(loud);
=======
		UnitDefinitionTable udtable = new UnitDefinitionTable(loud);
>>>>>>> develop
		
		sbaseList.add(stable);
		sbaseList.add(ptable);
		sbaseList.add(atable);
		sbaseList.add(bctable);
		sbaseList.add(dtable);
		sbaseList.add(rtable);
<<<<<<< HEAD
	//	sbaseList.add(udtable);
=======
		sbaseList.add(udtable);
>>>>>>> develop
		
		tabbedpane.addTab("Species", stable.getPane());
		tabbedpane.addTab("Parameter", ptable.getPane());
		tabbedpane.addTab("Advection", atable.getPane());
		tabbedpane.addTab("Boundary Condition", bctable.getPane());
		tabbedpane.addTab("Diffusion", dtable.getPane());
		tabbedpane.addTab("Reaction", rtable.getPane());
<<<<<<< HEAD
		//tabbedpane.addTab("Unit Definition", udtable.getPane());
=======
		tabbedpane.addTab("Unit Definition", udtable.getPane());
>>>>>>> develop
		
		// button
		JButton ok = new JButton("OK"), add = new JButton("add"), del = new JButton("delete"), edit = new JButton("edit");
		ok.addActionListener(this);
		add.addActionListener(this);
		edit.addActionListener(this);
		del.addActionListener(this);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(add);
		p2.add(del);
		p2.add(edit);
		p2.add(Box.createRigidArea(new Dimension(180, 0)));
		p2.add(ok);
		
		// set components
		getContentPane().add(p2, BorderLayout.PAGE_END);
		getContentPane().add(tabbedpane, BorderLayout.CENTER);

		setVisible(true);
	}
	
	/**
	 * Adds the all S base.
	 */
	private void addAllSBase() {
//		for (Component c : tabbedpane.getComponents()) {
//			JViewport viewport = ((JScrollPane) c).getViewport();
//			JTable table = (JTable) viewport.getView();
//		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SBMLReader reader = new SBMLReader();
		//SBMLDocument d = reader.readSBML("spatial_example1.xml");
		//SBMLDocument d = reader.readSBML("sampledField_3d.xml");
		//SBMLDocument d = reader.readSBML("mem_diff.xml");
		SBMLDocument d = reader.readSBML("analytic_3d.xml");
		new TabTables(d.getModel());
	}

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning(){
		return isRunning;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		int paneIndex = tabbedpane.getSelectedIndex();
		JScrollPane scroll = (JScrollPane) tabbedpane.getComponent(paneIndex);
		JViewport viewport = scroll.getViewport();
		JTable table = (JTable) viewport.getView();
		
		if (input.contentEquals("add")) {
			sbaseList.get(paneIndex).add();
		}

		if (input.contentEquals("edit")) {
			sbaseList.get(paneIndex).edit(table.getSelectedRow());
		}
		
		if (input.contentEquals("delete")) {
			sbaseList.get(paneIndex).removeFromList(table.getSelectedRow());
			sbaseList.get(paneIndex).removeSelectedFromTable(table);
		}

		if (input.equals("OK")) {
<<<<<<< HEAD
			//addAllSBase();
=======
			addAllSBase();
>>>>>>> develop
			isRunning = false;
			dispose();
			return;
		}
	}
}
