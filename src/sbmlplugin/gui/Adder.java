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
package sbmlplugin.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.libsbml.AdvectionCoefficient;
import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.ChangedMath;
import org.sbml.libsbml.CoordinateReference;
import org.sbml.libsbml.DiffusionCoefficient;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.ReqSBasePlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;
import org.sbml.libsbml.SpatialSpeciesPlugin;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.libsbmlConstants;



// TODO: Auto-generated Javadoc
/**
 * The Class Adder.
 */
@SuppressWarnings("serial")
public class Adder extends JFrame implements ItemListener, ActionListener, WindowListener{
	/**
	 * 
	 */
	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** The adding type. */
	private final String[] addingType = {"Parameter", "Species"};
	
	/** The parameter type. */
	private final String[] parameterType = {"advectionCoefficient", "boudaryCondition", "diffusionCoefficient"}; 
	
	/** The type combo. */
	private JComboBox typeCombo;
	
	/** The dom combo. */
	private JComboBox domCombo;
	
	/** The main panel. */
	private JPanel mainPanel;
	
	/** The combo list. */
	private List<String> comboList = new ArrayList<String>(Arrays.asList("Parameter", "Species","advectionCoefficient", "boudaryCondition", "diffusionCoefficient"));
	
	/** The model. */
	private Model model;
	
	/** The los. */
	private ListOfSpecies los;
	
	/** The loc. */
	private ListOfCompartments loc;
	
	/** The state. */
	private int state;
	
	/**
	 * Instantiates a new adder.
	 */
	public Adder(){
		super("Adder");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(400, 120);
		setResizable(false);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}
	
	/**
	 * Instantiates a new adder.
	 *
	 * @param model the model
	 */
	public Adder(Model model){
		this();
		this.model = model;
		this.los = model.getListOfSpecies();
		this.loc = model.getListOfCompartments();

		domCombo = createJComboBox("Compartments", getSbaseArray(loc));
		typeCombo = createJComboBox("type", addingType);
		getContentPane().add(typeCombo, BorderLayout.NORTH);
		setVisible(true);
		mainPanel = new JPanel();
	}
	
	/**
	 * Instantiates a new adder.
	 *
	 * @param model the model
	 * @param lop the lop
	 * @param los the los
	 */
	public Adder(Model model, ListOfParameters lop, ListOfSpecies los){
		this();
		this.model = model;
		this.los = los;
		this.loc = model.getListOfCompartments();
		domCombo = createJComboBox("Compartments", getSbaseArray(loc));
		typeCombo = createJComboBox("type" , addingType);
		getContentPane().add(typeCombo, BorderLayout.NORTH);
		setVisible(true);
		mainPanel = new JPanel();
	}
	
	/**
	 * Creates the J combo box.
	 *
	 * @param title the title
	 * @param source the source
	 * @return the j combo box
	 */
	private JComboBox createJComboBox(String title, String[] source){
		JComboBox box = new JComboBox(source);
		box.setName(title);
		box.addItemListener(this);
		box.setRenderer(new ComboBoxRenderer(title));
		box.setSelectedIndex(-1);
		return box;
	}

	/**
	 * Gets the sbase array.
	 *
	 * @param lo the lo
	 * @return the sbase array
	 */
	private String[] getSbaseArray(ListOf lo){
		String[] s = new String[(int) lo.size()];
		for(int i = 0 ; i < lo.size() ; i++)
			s[i] = lo.get(i).getId();
		
		return s;
	}
	
	/** The id field. */
	private JTextField idField;
	
	/** The val. */
	private JTextField val;
	
	/**
	 * Adds the text field.
	 */
	private void addTextField(){
		JLabel idlab = new JLabel("id:");
		idField = new JTextField(15);
		JLabel vallab = new JLabel("value:");
		val = new JTextField(3);
		mainPanel.add(idlab); mainPanel.add(idField);
		mainPanel.add(vallab); mainPanel.add(val);
	}
	
	/** The param combo. */
	JComboBox paramCombo;
	
	/**
	 * Addparameter mode.
	 */
	private void addparameterMode(){
		mainPanel.removeAll();
		paramCombo = createJComboBox("ParameterType", parameterType);
		mainPanel.add(paramCombo);
		addTextField();
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		validate();
		pack();
	}
	
	/**
	 * Adds the parameter.
	 *
	 * @param id the id
	 * @param value the value
	 * @param index the index
	 */
	private void addParameter(String id, double value, int index){
		String species = (String) speciesCombo.getSelectedItem();
		Parameter p = model.createParameter();
		p.setId(id); 	
		p.setValue(value); 
		p.setConstant(true);
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");

		switch (index) {
		case ADVECTION:
			AdvectionCoefficient ac = sp.createAdvectionCoefficient();
			ac.setVariable(species);
			ac.setCoordinate(coordCombo.getSelectedIndex() + 1);
			break;
		case BOUNDARY:
			BoundaryCondition bc = sp.createBoundaryCondition();
			bc.setVariable(species);
			setBound(bc, (String) boundCombo.getSelectedItem(), los.get(species));
			bc.setType(conditionCombo.getSelectedIndex() + 1);
			break;
		case DIFFUSION:
			DiffusionCoefficient dc = sp.createDiffusionCoefficient();
			dc.setVariable(species);
			dc.setType(diffCombo.getSelectedIndex() + 1);
			addDiffCoord(dc);
			break;
		}
	}

	/**
	 * Sets the bound.
	 *
	 * @param bc the bc
	 * @param boundary the boundary
	 * @param s the s
	 */
	private void setBound(BoundaryCondition bc , String boundary, Species s){
		if(match(boundary, lbound)){
			int index = getIndex(boundary, lbound);
			bc.setCoordinateBoundary(lbound[index]);
		} else 
			bc.setBoundaryDomainType(s.getCompartment());
	}
	
	/**
	 * Adds the diff coord.
	 *
	 * @param dc the dc
	 */
	private void addDiffCoord(DiffusionCoefficient dc){
		String s;
		for(int i = 0 ; i < coeff.getComponentCount() ; i++){
			s = coeff.getComponent(i).getName();
			if(s == null) continue;
			if(match(s, lcoord)){
				JCheckBox jcb = (JCheckBox) coeff.getComponent(i);
				if(jcb.isSelected()){
					CoordinateReference cr = new CoordinateReference();
					int index = getIndex(jcb.getName(), lcoord);
					cr.setCoordinate(index);
					addCoordinateReferences(dc.getType(), dc, index);
				}	
			}
		}
	}
	
	/**
	 * Adds the coordinate references.
	 *
	 * @param difftype the difftype
	 * @param dc the dc
	 * @param axis the axis
	 */
	private void addCoordinateReferences(int difftype, DiffusionCoefficient dc, int axis){
		switch (difftype){
		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_ISOTROPIC: 
				//no coordinateReference needed
			break;	
		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_TENSOR:
			//2 coordinateReference needed
			dc.setCoordinateReference2(axis);
		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_ANISOTROPIC:
			//1 coordinateReference needed
			dc.setCoordinateReference1(axis);
			break;
			default: 	
		}
	}
	
	/**
	 * Match.
	 *
	 * @param s the s
	 * @param list the list
	 * @return true, if successful
	 */
	private boolean match(String s, String[] list){
		for(int i = 0 ; i < list.length ; i++){
			if(s.equals(list[i]))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the index.
	 *
	 * @param s the s
	 * @param list the list
	 * @return the index
	 */
	private int getIndex(String s, String[] list){
		int num = 0;
		for(int i = 0 ; i < list.length ; i++)
			if(s.equals(list[i]))
				num = i;
		
		return num;
	}
	
	/** The coeff. */
	JPanel coeff;
	
	/** The diff combo. */
	JComboBox coordCombo, speciesCombo, boundCombo, conditionCombo, diffCombo;
	
	/** The lcoord. */
	private final String[] lcoord = {/*"UNKNOWN",*/"CARTESIANX","CARTESIANY","CARTESIANZ"};
	
	/** The lbound. */
	private final String[] lbound = {"Xmax","Xmin","Ymax","Ymin","Zmax","Zmin"};
	
	/** The lboundcondition. */
	private final String[] lboundcondition = {/*"UNKNOWN",*/"ROBIN_VALUE_COEFFICIENT","ROBIN_INWARD_NORMAL_GRADIENT_COEFFICIENT","ROBIN_SUM","NEUMANN","DIRICHLET"};
	
	/** The ldiffusion. */
	private final String[] ldiffusion = {/*"UNKNOWN", */"ISOTROPIC","ANISOTROPIC","TENSOR"};
	
	/** The boundaries. */
	private String[] boundaries;
	
	/**
	 * Join string array.
	 *
	 * @param arr1 the arr 1
	 * @param arr2 the arr 2
	 * @return the string[]
	 */
	private String[] joinStringArray(String[] arr1, String[] arr2){
		String[] s = new String[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, s, 0, arr1.length);
		System.arraycopy(arr2, 0, s, arr1.length, arr2.length);
		return s;
	}
	
	/**
	 * Adds the coeff part.
	 *
	 * @param index the index
	 */
	private void addCoeffPart(int index){
		if (mainPanel.getComponentCount() >= 6) { //removes previous parameter comboboxes if needed 
			mainPanel.remove(coeff);
			mainPanel.remove(ok);
		}
		
		coeff = new JPanel();
		
		speciesCombo = createJComboBox("Species", getSbaseArray(los));
		coordCombo = createJComboBox("Coordinate", lcoord);
		switch (index) {
		case ADVECTION:
			coeff.add(coordCombo);
			coeff.add(speciesCombo);
			break;
		case BOUNDARY:
			coeff.add(speciesCombo);
			String[] domain = {"domain"};
			boundaries = joinStringArray(lbound, domain);
			boundCombo = createJComboBox("BoundaryCoordinate",  boundaries);
			conditionCombo = createJComboBox("BoundaryKind", lboundcondition);
			coeff.add(boundCombo);
			coeff.add(conditionCombo);
			break;
		case DIFFUSION:
			diffCombo = createJComboBox("DiffusionType", ldiffusion);
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
		setLocationRelativeTo(null);
	}
	
	/**
	 * Adds the coord checkbox.
	 *
	 * @param panel the panel
	 */
	private void addCoordCheckbox(JPanel panel){	
		for(int i = 0 ; i < 3 ; i++){
			JCheckBox coordCheckbox = new JCheckBox(lcoord[i]);
			coordCheckbox.setName(lcoord[i]);
			panel.add(coordCheckbox);
		}
	}
	
	/**
	 * Adds the species.
	 *
	 * @param id the id
	 * @param compartment the compartment
	 * @param value the value
	 */
	private void addSpecies(String id, String compartment, double value){
		Species s = model.createSpecies();
		s.setId(id); s.setCompartment(compartment); s.setInitialConcentration(value);
		s.setSubstanceUnits("molecules");  													//need modification
		s.setHasOnlySubstanceUnits(false);s.setBoundaryCondition(false);
		s.setConstant(false);
		SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) s.getPlugin("spatial");
		ssp.setIsSpatial(true);
		
		ReqSBasePlugin rsb = (ReqSBasePlugin) s.getPlugin("req");		
		ChangedMath cm = rsb.createChangedMath();
		cm.setId("spatial");
		cm.setChangedBy( new SpatialPkgNamespaces(3, 1, 1).getURI());
		cm.setViableWithoutChange(true);	
	}

	/**
	 * Adds the species mode.
	 */
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
		setLocationRelativeTo(null);
	}
	
	/** The ok. */
	JButton ok;	
	
	/**
	 * Adds the ok button.
	 *
	 * @param name the name
	 */
	public void addOkButton(String name){
		ok = new JButton("OK");
		ok.setName(name);
		ok.addActionListener(this);
		mainPanel.add(ok,BorderLayout.PAGE_END);
		getContentPane().add(mainPanel,BorderLayout.CENTER);
		validate();
		pack();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("mem_diff.xml");
		new Adder( d.getModel());
	}
	
	/** The parameter. */
	private final int PARAMETER = 0;
	
	/** The species. */
	private	final int SPECIES   = 1;
	
	/** The advection. */
	private final int ADVECTION = 2;
	
	/** The boundary. */
	private final int BOUNDARY  = 3;
	
	/** The diffusion. */
	private final int DIFFUSION = 4;
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED ){
			state = comboList.indexOf(e.getItem());
			
			switch (state) {
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
				addCoeffPart(state);
				break;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String idText = idField.getText().replaceAll(" ", "_"); 	
		String compartment = (String) domCombo.getSelectedItem();

		Double num = null;
		try{
			num = Double.parseDouble(val.getText());
		}catch(NumberFormatException nfe){
			errMessage();
			return;
		}
		
		if(checkComponent(idText, compartment, num)){
			if(typeCombo.getSelectedItem().equals("Species"))
				addSpecies(idText, compartment, num);	
			else
				addParameter(idText, num, comboList.indexOf(paramCombo.getSelectedItem()));
	
			dispose();
		}
	}	
	
	/**
	 * Check component.
	 *
	 * @param idText the id text
	 * @param compartment the compartment
	 * @param num the num
	 * @return true, if successful
	 */
	private boolean checkComponent(String idText, String compartment, Double num) {
		boolean hasError = false;
		switch (state) {
		case SPECIES:
		case DIFFUSION:
			if (compartment == null)
				hasError = true;
		case ADVECTION:
		case BOUNDARY:
			if (domCombo.getSelectedIndex() < 0)
				hasError = true;
			if (idText == null)
				hasError = true;
			if (speciesCombo.getSelectedIndex() < 0)
				hasError = true;
			break;
		}

		if (hasError) {
			errMessage();
			return false;
		}

		return true;
	}
	
	/**
	 * Err message.
	 */
	private void errMessage(){
		JOptionPane.showMessageDialog(this, "Missing Component", "Error", JOptionPane.PLAIN_MESSAGE);	
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		mainPanel.removeAll();
		return;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent arg0) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent arg0) {
		
	}
	
}

