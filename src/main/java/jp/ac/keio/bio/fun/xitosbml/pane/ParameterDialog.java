package jp.ac.keio.bio.fun.xitosbml.pane;

import ij.gui.GenericDialog;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Unit;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class ParameterDialog {
	
	/** The parameter. */
	private Parameter parameter;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The units. */
	private final String[] units = {"substance","mole","item","gram","kilogram","dimensionless"};
	
	/** The bool. */
	private final String[] bool = {"true","false"};
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new parameter dialog.
	 *
	 * @param model the model
	 */
	public ParameterDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Show dialog.
	 *
	 * @return the parameter
	 */
	public Parameter showDialog(){
		gd = new GenericDialog("Add Parameter");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addChoice("units:", units, null);
		gd.addRadioButtonGroup("constant:", bool, 1,2,"true");
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;

		parameter = model.createParameter();
		setParameterData();
		
		return parameter;
	}
	
	/**
	 * Show dialog.
	 *
	 * @param parameter the parameter
	 * @return the parameter
	 */
	public Parameter showDialog(Parameter parameter){
		this.parameter = parameter;
		gd = new GenericDialog("Edit Parameter");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addChoice("units:", units, parameter.getUnits());
		gd.addRadioButtonGroup("constant:", bool, 1, 2, String.valueOf(parameter.getConstant()));
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	/**
	 * Sets the parameter data.
	 */
	private void setParameterData(){
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		parameter.setId(str);
		parameter.setValue(gd.getNextNumber());
		parameter.setUnits(Unit.Kind.valueOf(gd.getNextChoice()));
		parameter.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		
	}
}
