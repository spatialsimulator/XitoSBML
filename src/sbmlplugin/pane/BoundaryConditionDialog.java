package sbmlplugin.pane;

import ij.gui.GenericDialog;

import java.util.Arrays;

import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SpatialParameterPlugin;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 21, 2016
 */
public class BoundaryConditionDialog {
	private Parameter parameter;
	private GenericDialog gd;
	private final String[] bool = {"true","false"};
	private Model model;
	
	public BoundaryConditionDialog(Model model){
		this.model = model;
	}
	
	public Parameter showDialog(){
		gd = new GenericDialog("Add Boundary Condition");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addRadioButtonGroup("constant:", bool, 1, 2, "true");
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

	public Parameter showDialog(Parameter parameter){
		this.parameter = parameter;
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		BoundaryCondition bc = sp.getBoundaryCondition();
		gd = new GenericDialog("Edit Parameter");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addRadioButtonGroup("constant:", bool, 1, 2, String.valueOf(parameter.getConstant()));
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), bc.getVariable());
		gd.addChoice("type:", SBMLProcessUtil.boundType, SBMLProcessUtil.boundaryIndexToString(bc.getType()));
		gd.addChoice("boundary:", getAllBoundAsString(), bc.isSetCoordinateBoundary() ? bc.getCoordinateBoundary(): bc.getBoundaryDomainType());
	
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	private void setParameterData(){
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		parameter.setId(str);
		parameter.setValue(gd.getNextNumber());
		parameter.setConstant(Boolean.getBoolean(gd.getNextRadioButton()));
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		BoundaryCondition bc = sp.isSetBoundaryCondition() ? sp.getBoundaryCondition() : sp.createBoundaryCondition();
		bc.setVariable(gd.getNextChoice());
		bc.setType(gd.getNextChoice());
		String bound = gd.getNextChoice();
		if(Arrays.asList(SBMLProcessUtil.bounds).contains(bound))
			bc.setCoordinateBoundary(bound);
		else
			bc.setBoundaryDomainType(bound);
	} 
	
	private String[] getAllBoundAsString(){
		String[] bound = SBMLProcessUtil.bounds;
		String[] compartment = SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments());
		String[] str = new String[bound.length + compartment.length];
		System.arraycopy(bound, 0, str, 0, bound.length);
		System.arraycopy(compartment, 0, str, bound.length, compartment.length);
			
		return str;
	}
}
