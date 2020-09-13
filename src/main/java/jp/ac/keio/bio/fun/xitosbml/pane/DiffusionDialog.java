package jp.ac.keio.bio.fun.xitosbml.pane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.spatial.CoordinateKind;
import org.sbml.jsbml.ext.spatial.DiffusionCoefficient;
import org.sbml.jsbml.ext.spatial.DiffusionKind;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;

import ij.gui.GenericDialog;

/**
 * The class DiffusionDialog, which generates a GUI for creating / editing Diffusion Coefficient.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.DiffusionTable}.
 * Date Created: Jan 20, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class DiffusionDialog {
	
	/** The JSBML Parameter object. */
	private Parameter parameter;
	
	/** The generic dialog. */
	private GenericDialog gd;
	
	/** The boolean value for isConstant. */
	private final String[] isConstant = {"true","false"};
	
	/** The SBML model. */
	private Model model;
	
	/**
	 * Instantiates a new diffusion dialog.
	 *
	 * @param model the SBML model
	 */
	public DiffusionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Create and show a dialog for adding Diffusion Coefficient.
	 * If a user creates a diffusion coefficient through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @return the JSBML Parameter object if a parameter is created
	 */
	public Parameter showDialog(){
		gd = new GenericDialog("Add Diffusion Coeffcient");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1,2,"true");
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), null);
		gd.addChoice("type:", SBMLProcessUtil.diffType, null);
		gd.addChoice("coordinate1:", SBMLProcessUtil.lcoord, null);
		gd.addChoice("coordinate2:", SBMLProcessUtil.lcoord, null);
		
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
	
		parameter = model.createParameter();
		setParameterData();
		
		return parameter;
	}

	/**
	 * Create and show a dialog for adding Diffusion Coefficient with given JSBML Parameter object.
	 * If a user edits the diffusion coefficient through this dialog,
	 * then a Parameter object will be returned. If not, null is returned.
	 *
	 * @param parameter the JSBML Parameter object
	 * @return the JSBML Parameter object if the parameter is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public Parameter showDialog(Parameter parameter){
		this.parameter = parameter;
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		DiffusionCoefficient dc = (DiffusionCoefficient) sp.getParamType();
		gd = new GenericDialog("Edit DiffusionCoefficient");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addRadioButtonGroup("constant:", isConstant, 1, 2, String.valueOf(parameter.getConstant()));
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), dc.getVariable());
		gd.addChoice("type:", SBMLProcessUtil.diffType, dc.getType().name());
		if(dc.isSetCoordinateReference1())
			gd.addChoice("coordinate1:", SBMLProcessUtil.lcoord, dc.getCoordinateReference1().name());
		else
			gd.addChoice("coordinate1:", SBMLProcessUtil.lcoord, SBMLProcessUtil.lcoord[0]);

		if(dc.isSetCoordinateReference2())
			gd.addChoice("coordinate2:", SBMLProcessUtil.lcoord, dc.getCoordinateReference2().name());
		else
			gd.addChoice("coordinate2:", SBMLProcessUtil.lcoord, SBMLProcessUtil.lcoord[0]);
	
		gd.showDialog();
		if(gd.wasCanceled())
			return null;
				
		setParameterData();
		
		return parameter;
	}
		
	/**
	 * Sets/updates the following information of the parameter (diffusion coefficient) from the GUI.
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:value</li>
	 *     <li>boolean:constant</li>
	 *     <li>String:variable</li>
	 *     <li>DiffusionKind:diffusion kind</li>
	 *     <li>CoordinateKind:coordinate kind</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void setParameterData(){
		String str = gd.getNextString();
		if (str.indexOf(' ')!=-1)
				str = str.replace(' ', '_');
		parameter.setId(str);
		parameter.setValue(gd.getNextNumber());
		parameter.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		
		DiffusionCoefficient dc = (DiffusionCoefficient) (sp.isSetParamType() ? sp.getParamType() : new DiffusionCoefficient());
		dc.setVariable(gd.getNextChoice());
		dc.setType(DiffusionKind.valueOf(gd.getNextChoice()));
		String coord1 = gd.getNextChoice();
		String coord2 = gd.getNextChoice();
		
		switch (dc.getType()) {
		case tensor:
			dc.setCoordinateReference2(CoordinateKind.valueOf(coord2));

		case anisotropic:
			dc.setCoordinateReference1(CoordinateKind.valueOf(coord1));

		case isotropic:
			break;
		}
		
		if(dc.getType() == DiffusionKind.tensor && dc.getCoordinateReference1() == dc.getCoordinateReference2())
			dc.unsetCoordinateReference2();
		
		if(!dc.isSetParentSBMLObject())
			sp.setParamType(dc);
	}
}
