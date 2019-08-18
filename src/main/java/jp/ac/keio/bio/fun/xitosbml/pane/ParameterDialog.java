package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Unit;

import ij.gui.GenericDialog;

/**
 * The class ParameterDialog, which generates a GUI for creating / editing a Parameter.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.ParameterTable}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class ParameterDialog {
	
	/** The JSBML Parameter object. */
	private Parameter parameter;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The units. */
	private final String[] units = {"mole","item","gram","kilogram","dimensionless"};
	
	/** The boolean value for isConstant. */
	private final String[] isConstant = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new parameter dialog.
	 *
	 * @param model the SBML model
	 */
	public ParameterDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Create and show a dialog for adding Parameter.
	 * If a user creates a parameter through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Parameter object if a parameter is created
	 */
	public Parameter showDialog(){
		gd = new GenericDialog("Add Parameter");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addChoice("units:", units, null);
		gd.addRadioButtonGroup("constant:", isConstant, 1,2,"true");
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;

		parameter = model.createParameter();
		setParameterData();
		
		return parameter;
	}
	
	/**
	 * Create and show a dialog for adding Parameter with given JSBML Parameter object.
	 * If a user edits the parameter through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @param parameter the JSBML Parameter object
	 * @return the JSBML Parameter object if the parameter is edited
	 */
	public Parameter showDialog(Parameter parameter){
		this.parameter = parameter;
		gd = new GenericDialog("Edit Parameter");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addChoice("units:", units, parameter.getUnits());
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, String.valueOf(parameter.getConstant()));
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	/**
	 * Sets/updates the following information of the parameter from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>Kind:unitKind</li>
	 *     <li>boolean:constant</li>
	 * </ul>
	 */
	private void setParameterData(){
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		parameter.setId(str);
		parameter.setValue(gd.getNextNumber());
		parameter.setUnits(Unit.Kind.valueOf(gd.getNextChoice().toUpperCase()));
		parameter.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		
	}
}
