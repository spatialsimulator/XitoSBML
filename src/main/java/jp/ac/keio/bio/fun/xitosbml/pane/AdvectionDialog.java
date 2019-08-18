package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.AdvectionCoefficient;
import org.sbml.jsbml.ext.spatial.CoordinateKind;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

import ij.gui.GenericDialog;

/**
 * The class AdvectionDialog, which generates a GUI for creating / editing Advection Coefficient.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.AdvectionTable}.
 * Date Created: Jan 21, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class AdvectionDialog {
	
	/** The JSBML Parameter object. */
	private Parameter parameter;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The boolean value for isConstant. */
	private final String[] isConstant = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new advection dialog.
	 *
	 * @param model the SBML model
	 */
	public AdvectionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Create and show a dialog for adding Advection Coefficient.
	 * If a user creates an advection coefficient through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Parameter object if a parameter is created
	 */
	public Parameter showDialog(){
		gd = new GenericDialog("Add Advection Coefficient");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, "true");
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), null);
		gd.addChoice("coordinate:", SBMLProcessUtil.lcoord, null);
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		parameter = model.createParameter();
		setParameterData();
		
		return parameter;
	}

	/**
	 * Create and show a dialog for adding Advection Coefficient with given JSBML Parameter object.
	 * If a user edits the advection coefficient through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @param parameter the JSBML Parameter object
	 * @return the JSBML Parameter object if the parameter is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public Parameter showDialog(Parameter parameter) throws IllegalArgumentException{
		this.parameter = parameter;
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		AdvectionCoefficient ac = (AdvectionCoefficient) sp.getParamType();
		gd = new GenericDialog("Edit Advection Coefficient");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, String.valueOf(parameter.getConstant()));
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), ac.getVariable());
		gd.addChoice("coordinate:", SBMLProcessUtil.lcoord, ac.getCoordinate().name());
	
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	/**
	 * Sets/updates the following information of the parameter (advection coefficient) from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>boolean:constant</li>
	 *     <li>String:variable</li>
	 *     <li>String:name</li>
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
		AdvectionCoefficient ac = (AdvectionCoefficient) (sp.isSetParamType() ? sp.getParamType() : new AdvectionCoefficient());
		String var = gd.getNextChoice();
		ac.setVariable(var);
		ac.setCoordinate(CoordinateKind.valueOf(gd.getNextChoice()));
		if(!ac.isSetParentSBMLObject())
			sp.setParamType(ac);
	}	
}
