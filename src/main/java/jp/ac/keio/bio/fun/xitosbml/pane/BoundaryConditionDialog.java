package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Arrays;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.BoundaryCondition;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

import ij.gui.GenericDialog;

/**
 * The class BoundaryConditionDialog, which generates a GUI for creating / editing Boundary Condition.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.BoundaryConditionTable}.
 * Date Created: Jan 21, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class BoundaryConditionDialog {
	
	/** The JSBML Parameter object. */
	private Parameter parameter;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The boolean value for isConstant. */
	private final String[] isConstant = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new boundary condition dialog.
	 *
	 * @param model the SBML model
	 */
	public BoundaryConditionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Create and show a dialog for adding Boundary Condition.
	 * If a user creates a boundary condition through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Parameter object if a parameter is created
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public Parameter showDialog() throws IllegalArgumentException{
		gd = new GenericDialog("Add Boundary Condition");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, "true");
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), null);
		gd.addChoice("type:", SBMLProcessUtil.boundType, null);
		gd.addChoice("boundary:", getAllBoundAsString(), null);

		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		parameter = model.createParameter();
		setParameterData();
		
		return parameter;
	}

	/**
	 * Create and show a dialog for adding Boundary Condition with given JSBML Parameter object.
	 * If a user edits the boundary condition through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @param parameter the JSBML Parameter object
	 * @return the JSBML Parameter object if the parameter is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public Parameter showDialog(Parameter parameter) throws IllegalArgumentException{
		this.parameter = parameter;
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		BoundaryCondition bc = (BoundaryCondition) sp.getParamType();
		gd = new GenericDialog("Edit BoundaryCondition");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, String.valueOf(parameter.getConstant()));
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), bc.getVariable());
		gd.addChoice("type:", SBMLProcessUtil.boundType, bc.getType().name());
		gd.addChoice("boundary:", getAllBoundAsString(), bc.isSetCoordinateBoundary() ? bc.getCoordinateBoundary(): bc.getBoundaryDomainType());
	
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	/**
	 * Sets/updates the following information of the parameter (boundary condition) from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>boolean:constant</li>
	 *     <li>String:variable</li>
	 *     <li>String:type</li>
	 *     <li>String:boundary or domain type</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void setParameterData() throws IllegalArgumentException{
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		parameter.setId(str);
		parameter.setValue(gd.getNextNumber());
		parameter.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		BoundaryCondition bc = (BoundaryCondition) (sp.isSetParamType() ? sp.getParamType() : new BoundaryCondition());
		bc.setVariable(gd.getNextChoice());
		bc.setType(gd.getNextChoice());
		String bound = gd.getNextChoice();
		if(Arrays.asList(SBMLProcessUtil.bounds).contains(bound))
			bc.setCoordinateBoundary(bound);
		else
			bc.setBoundaryDomainType(bound);

		if(!bc.isSetParentSBMLObject())
			sp.setParamType(bc);
	} 
	
	/**
	 * Gets the all bound as string.
	 *
	 * @return an array of String which contains the all bound as string
	 */
	private String[] getAllBoundAsString(){
		String[] bound = SBMLProcessUtil.bounds;
		String[] compartment = SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments());
		String[] str = new String[bound.length + compartment.length];
		System.arraycopy(bound, 0, str, 0, bound.length);
		System.arraycopy(compartment, 0, str, bound.length, compartment.length);
			
		return str;
	}
}
