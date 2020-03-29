package jp.ac.keio.bio.fun.xitosbml.pane;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.text.parser.ParseException;

/**
 * The class TabTables, which inherits JFrame and implements main component of XitoSBML.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.xitosbml.MainSpatial},
 * which creates a GUI for XitoSBML.
 * Date Created: Jan 12, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
@SuppressWarnings("serial")
public class TabTables extends JFrame implements ActionListener {

	/** The JTabbedPane object. */
	private JTabbedPane tabbedpane = new JTabbedPane();
	
	/** The list of SBaseTable (SpeciesTable, ReactionTable, ParameterTable, etc.). */
	private ArrayList<SBaseTable> sbaseList = new ArrayList<SBaseTable>();
	
	/** is running. Set to false when exporting to SBML file is done. */
	private boolean isRunning = true;	

	/** The SBML model. */
	private Model model;
	
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
	 * Instantiates a new tab tables with given SBML model.
	 *
	 * @param model the SBML model
	 */
	public TabTables(Model model) {
		this();
		this.model = model;
		ListOf<Parameter> lop = model.getListOfParameters();
		ListOf<Species> los = model.getListOfSpecies();
		ListOf<Reaction> lor = model.getListOfReactions();
//		ListOf<UnitDefinition> loud = model.getListOfUnitDefinitions();
		
		SpeciesTable stable = new SpeciesTable(los);		
		ParameterTable ptable = new ParameterTable(lop);		
		AdvectionTable atable = new AdvectionTable(lop);		
		BoundaryConditionTable bctable = new BoundaryConditionTable(lop);
		DiffusionTable dtable = new DiffusionTable(lop);
		ReactionTable rtable = new ReactionTable(lor);
	//	UnitDefinitionTable udtable = new UnitDefinitionTable(loud);
		
		sbaseList.add(stable);
		sbaseList.add(ptable);
		sbaseList.add(atable);
		sbaseList.add(bctable);
		sbaseList.add(dtable);
		sbaseList.add(rtable);
	//	sbaseList.add(udtable);
		
		tabbedpane.addTab("Species", stable.getPane());
		tabbedpane.addTab("Parameter", ptable.getPane());
		tabbedpane.addTab("Advection", atable.getPane());
		tabbedpane.addTab("Boundary Condition", bctable.getPane());
		tabbedpane.addTab("Diffusion", dtable.getPane());
		tabbedpane.addTab("Reaction", rtable.getPane());
		//tabbedpane.addTab("Unit Definition", udtable.getPane());
		
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
	 * Example main() method which will read an SBML file and create a GUI for editing SBML model.
	 * This example requires a specific SBML model "sam2d.xml" located under the specific directory.
	 * @param args an array of command-line arguments for the application
	 */
	public static void main(String[] args) {
		SBMLDocument d;
		try {
			d = SBMLReader.read(new File("/Users/ii/Desktop/sam2d.xml"));
			new TabTables(d.getModel());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if is running.
	 * The isRunning value will be set to false when exporting to SBML file is done.
	 *
	 * @return true, if running
	 */
	public boolean isRunning(){
		return isRunning;
	}
	
	/**
     * Invoked when an action occurs.
     * This action listener will handle events when a user pressed
	 * [add], [edit], [delete] or [OK] button. When an [OK] button is pressed,
	 * the model will be exported to an SBML document, and sent to stdout.
	 * @param e the ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = e.getActionCommand();
		int paneIndex = tabbedpane.getSelectedIndex();
		JScrollPane scroll = (JScrollPane) tabbedpane.getComponent(paneIndex);
		JViewport viewport = scroll.getViewport();
		JTable table = (JTable) viewport.getView();

		try{
			if (input.contentEquals("add")) {
				sbaseList.get(paneIndex).add();
			}else if (input.contentEquals("edit")) {
				sbaseList.get(paneIndex).edit(table.getSelectedRow());
			}else if (input.contentEquals("delete")) {
				SBase sbase = sbaseList.get(paneIndex).removeFromList(table.getSelectedRow());
				sbaseList.get(paneIndex).removeSelectedRowFromTable(table);
				model.unregister(sbase);
                                String Id = sbase.getId();//added by Morita
                                Id += "_initialConcentration";
                                SBase IC = model.getElementBySId(Id);
                                if( IC != null )
                                  model.unregister(IC);//added by Morita
			}
		} catch(IllegalArgumentException ex){
			if(ex.getCause() == null){
				JOptionPane.showMessageDialog(this, "No species in model");
			} else {
				JOptionPane.showMessageDialog(this, "Duplicate Id");
			}
			ex.printStackTrace();
		} catch (IdentifierException e1) {
			e1.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		if (input.equals("OK")) {
			try {
				SBMLWriter.write(model.getSBMLDocument(), System.out, ' ', (short)2);
			} catch (SBMLException e1) {
				e1.printStackTrace();
			} catch (XMLStreamException e1) {
				e1.printStackTrace();
			}
			
			isRunning = false;
			dispose();
			return;
		}
	}
}
