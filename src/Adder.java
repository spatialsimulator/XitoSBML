
import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.sbml.libsbml.AdvectionCoefficient;
import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.CoordinateReference;
import org.sbml.libsbml.DiffusionCoefficient;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialSpeciesPlugin;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.libsbml;



public class Adder extends JFrame implements ItemListener, ActionListener{
	  static {
		    System.loadLibrary("sbmlj");                //read system library sbmlj
		  }
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String[] addingType = {"Parameter", "Species"};
	private final String[] parameterType = {"advectionCoefficient", "boudaryCondition", "diffusionCoefficient"}; 
	private JComboBox typeCombo;
	private JComboBox domCombo;
	private JPanel mainPanel;
	private List<String> comboList = new ArrayList<String>(Arrays.asList("Parameter", "Species","advectionCoefficient", "boudaryCondition", "diffusionCoefficient"));
	private Model model;
	private ListOfParameters lop; 
	private ListOfSpecies los;
	
	public Adder(){
		super("Adder");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(400, 120);
		setResizable(false);
	}
	
	public Adder(Model model){
		this();
		this.model = model;
		this.lop = model.getListOfParameters();
		this.los = model.getListOfSpecies();

		addComp(model.getListOfCompartments());
		typeCombo = createJComboBox("type", addingType);
		getContentPane().add(typeCombo, BorderLayout.NORTH);
		setVisible(true);
		mainPanel = new JPanel();
	}
	
	public Adder(Model model, ListOfParameters lop, ListOfSpecies los){
		this();
		this.model = model;
		this.lop = lop;
		this.los = los;
		addComp(model.getListOfCompartments());
		typeCombo = createJComboBox("type" ,addingType);
		getContentPane().add(typeCombo, BorderLayout.NORTH);
		setVisible(true);
		mainPanel = new JPanel();
	}
	
	private JComboBox createJComboBox(String title, String[] source){
		JComboBox box = new JComboBox(source);
		box.setName(title);
		box.addItemListener(this);
		box.setRenderer(new ComboBoxRenderer(title));
		box.setSelectedIndex(-1);
		return box;
	}

	
/*
 	private void addDom(){
		for(int i = 0 ; i < table.getColumnCount() ; i++)
			
	}
 */
	
	private void addComp(ListOfCompartments loc){
		String[] s = new String[(int) loc.size()];
		for(int i = 0 ; i < loc.size() ; i++)
			s[i] = loc.get(i).getId();
		
		domCombo = createJComboBox("Compartments", s);
	}

	private String[] addSpecies(){
		String[] species = new String[(int) los.size()];
		for(int i = 0 ; i < los.size() ; i++)
			species[i] = los.get(i).getId();
		
		return species;	
	}
	
	private JTextField id;
	private JTextField val;
	private void addTextField(){
		JLabel idlab = new JLabel("id:");
		id = new JTextField(15);
		JLabel vallab = new JLabel("value:");
		val = new JTextField(3);
		mainPanel.add(idlab); mainPanel.add(id);
		mainPanel.add(vallab); mainPanel.add(val);
	}
	
	JComboBox paramCombo;
	private void addparameterMode(){
		mainPanel.removeAll();
		paramCombo = createJComboBox("ParameterType", parameterType);
		mainPanel.add(paramCombo);
		addTextField();
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		validate();
		pack();
	}
	
	private void addParameter(String id, double value, int index){
		//ListOfParameters lop;
		String species = (String) speciesCombo.getSelectedItem();
		Parameter p = model.createParameter();
		p.setId(id); 	
		p.setValue(value); 
		p.setConstant(true);
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");

		switch(index){
		case ADVECTION:
			AdvectionCoefficient ac = sp.createAdvectionCoefficient();
			ac.setVariable(species);
			ac.setCoordinate(coordCombo.getSelectedIndex());
			break;
		case BOUNDARY:
			BoundaryCondition bc = sp.createBoundaryCondition();
			bc.setVariable(species); bc.setBoundaryDomainType((String) domCombo.getSelectedItem());
			bc.setCoordinateBoundary((String) boundCombo.getSelectedItem());
			bc.setType(conditionCombo.getSelectedIndex());
			break;
		case DIFFUSION:
			DiffusionCoefficient dc = sp.createDiffusionCoefficient();
			dc.setVariable(species); 
			addCoordinateReference(dc);
			dc.setType(diffCombo.getSelectedIndex());
			break;
		}
		model.addParameter(p);
	}

	private void addCoordinateReference(DiffusionCoefficient dc){
		String s;
		for(int i = 0 ; i < coeff.getComponentCount() ; i++){
			s = coeff.getComponent(i).getName();
			if(s == null) continue;
			if(match(s)){
				JCheckBox jcb = (JCheckBox) coeff.getComponent(i);
				if(jcb.isSelected()){
					CoordinateReference cr = new CoordinateReference();
					int index = getIndex(jcb.getName());
					cr.setCoordinate(index);
					dc.addCoordinateReference(cr);
				}	
			}
		}
	}
	
	private boolean match(String s){
		for(int i = 0 ; i < lcoord.length ; i++){
			if(s.equals(lcoord[i]))
				return true;
		}
		return false;
	}
	
	private int getIndex(String s){
		int num = 0;
		
		for(int i = 0 ; i < lcoord.length ; i++)
			if(s.equals(lcoord[i]))
				num = i;
		
		return num;
	}
	
	JPanel coeff;
	JComboBox coordCombo, speciesCombo, boundCombo, conditionCombo, diffCombo;
	String[] lcoord = {"UNKNOWN","CARTESIANX","CARTESIANY","CARTESIANZ"};
	String[] lbound = {"Xmax","Xmin","Ymax","Ymin","Zmax","Zmin"};
	String[] lboundcondition = {"UNKNOWN", "ROBIN_VALUE_COEFFICIENT","ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT","ROBIN_SUM","NEUMANN","DIRICHLET"};
	String[] ldiffusion = {"UNKNOWN", "ISOTROPIC","ANISOTROPIC","TENSOR"};

	private void addCoeffPart(int index){
		if (mainPanel.getComponentCount() >= 6) { //removes previous parameter comboboxes if needed 
			mainPanel.remove(coeff);
			mainPanel.remove(ok);
		}
		
		coeff = new JPanel();
		String[] lspecies = addSpecies(); 
		speciesCombo = createJComboBox("Species", lspecies);
		coordCombo = createJComboBox("Coordinate", lcoord);
		switch (index) {
		case ADVECTION:
			coeff.add(coordCombo);
			coeff.add(speciesCombo);
			break;
		case BOUNDARY:
			boundCombo = createJComboBox("BoundaryCoordinate", lbound);
			conditionCombo = createJComboBox("BoundaryKind", lboundcondition);
			coeff.add(domCombo);
			coeff.add(boundCombo);
			coeff.add(conditionCombo);
			coeff.add(speciesCombo);
			break;
		case DIFFUSION:
			diffCombo = createJComboBox("DiffusionType", ldiffusion);
			//coeff.add(coordCombo);
			addCoordCheckbox(coeff);
			coeff.add(diffCombo);
			coeff.add(speciesCombo);
			break;
		}

		mainPanel.add(coeff);
		addOkButton("parameter");
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		validate();
		pack();
	}
	
	private void addCoordCheckbox(JPanel panel){	
		for(int i = 1 ; i <= 3 ; i++){
			JCheckBox coordCheckbox = new JCheckBox(lcoord[i]);
			coordCheckbox.setName(lcoord[i]);
			panel.add(coordCheckbox);
		}
	}
	
	private void addSpecies(String id, String compartment, double value){
		Species s = model.createSpecies();
		s.setId(id); s.setCompartment(compartment); s.setInitialConcentration(value);
		s.setSubstanceUnits("molecules");  													//need modification
		s.setHasOnlySubstanceUnits(false);s.setBoundaryCondition(false);
		s.setConstant(false);
		SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) s.getPlugin("spatial");
		ssp.setIsSpatial(true);
		model.addSpecies(s);
	}

	private void addSpeciesMode(){
		mainPanel.removeAll();
		domCombo.setRenderer(new ComboBoxRenderer("Compartment"));
		domCombo.setSelectedIndex(-1);
		domCombo.addItemListener(this);
		mainPanel.add(domCombo);
		addTextField();
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		validate();
		pack();
	}
	
	JButton ok;	
	public void addOkButton(String name){
		ok = new JButton("OK");
		ok.setName(name);
		ok.addActionListener(this);
		mainPanel.add(ok,BorderLayout.PAGE_END);
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		validate();
		pack();
	}
	
	public void printComponentAdded(){
		IJ.log("Parameters\n");
		for(int i = 0 ; i < lop.size() ; i++)
			IJ.log(lop.get(i).toString());
		
		IJ.log("Species\n");
		for(int i = 0 ; i < los.size() ; i++)
			IJ.log(los.get(i).toString());
	}
	
	public static void main(String[] args){
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("simple_mem_diffusion.xml");
		new Adder( d.getModel());
	}
	
	private final int PARAMETER = 0;
	private	final int SPECIES   = 1;
	private final int ADVECTION = 2;
	private final int BOUNDARY  = 3;
	private final int DIFFUSION = 4;
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if(e.getStateChange() == ItemEvent.SELECTED ){
			int index = comboList.indexOf(e.getItem()); 
			switch (index) {
			case PARAMETER:
				addparameterMode();
				break;
			case SPECIES:
				addSpeciesMode();
				addOkButton("Species");
				break;
			case ADVECTION:
			case BOUNDARY:
			case DIFFUSION:
				addCoeffPart(index);
				break;
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try{
			String idText = id.getText().replaceAll(" ", "_"); 				// string starting with an int will not be applied
			String compartment = (String) domCombo.getSelectedItem();
			Integer num = Integer.parseInt(val.getText());            // need to include integer with exponential
			if(typeCombo.getSelectedItem().equals("Species"))
				addSpecies(idText, compartment, num);	
			else{
				addParameter(idText, num, comboList.indexOf(paramCombo.getSelectedItem()));
			}
			dispose();
			return;
		} catch (NullPointerException ex){
			JOptionPane.showMessageDialog(this, "Missing Component", "Error", JOptionPane.PLAIN_MESSAGE);	
		}
	}
	
	
	
	class ComboBoxRenderer extends JLabel implements ListCellRenderer
    {
		private static final long serialVersionUID = 1L;
		private String title;

        public ComboBoxRenderer(String title)
        {
            this.title = title;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean hasFocus)
        {
            if (index == -1 && value == null) setText(title);
            else setText(value.toString());
            return this;
        }
    }

	enum DIFFUSIONKIND{
		 DIFFUSIONKIND_UNKNOWN,  /*!< Unknown DiffusionKind */
		 DIFFUSIONKIND_ISOTROPIC, /*!< isotropic */
		 DIFFUSIONKIND_ANISOTROPIC, /*!< anisotropic */
		 DIFFUSIONKIND_TENSOR /*!< tensor */
		 }
	
	enum COORDINATEKIND{
		COORDINATEKIND_UNKNOWN,
		COORDINATEKIND_CARTESIANX,
		COORDINATEKIND_CARTESIANY,
		COORDINATEKIND_CARTESIANZ
	}
	
	enum BOUNDARYKIND{   
		BOUNDARYKIND_UNKNOWN,  /*!< Unknown BoundaryConditionKind */
		BOUNDARYKIND_ROBIN_VALUE_COEFFICIENT, /*!< Robin_valueCoefficient */
		BOUNDARYKIND_ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT, /*!< Robin_inwardNormalGradientCoefficient */
		BOUNDARYKIND_ROBIN_SUM, /*!< Robin_sum */
		BOUNDARYKIND_NEUMANN, /*!< Neumann */
		BOUNDARYKIND_DIRICHLET, /*!< Dirichlet */
	}
	
}

