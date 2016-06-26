package sbmlplugin.pane;

import ij.gui.GenericDialog;

import org.sbml.libsbml.DiffusionCoefficient;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.libsbmlConstants;
// TODO: Auto-generated Javadoc

/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jan 20, 2016
 */
public class DiffusionDialog {
	
	/** The parameter. */
	private Parameter parameter;
	
	/** The gd. */
	private GenericDialog gd;
	
	/** The bool. */
	private final String[] bool = {"true","false"};
	
	/** The model. */
	private Model model;
	
	/**
	 * Instantiates a new diffusion dialog.
	 *
	 * @param model the model
	 */
	public DiffusionDialog(Model model){
		this.model = model;
	}
	
	/**
	 * Show dialog.
	 *
	 * @return the parameter
	 */
	public Parameter showDialog(){
		gd = new GenericDialog("Add Diffusion Coeffcient");
		gd.setResizable(true);
		gd.pack();
	
		gd.addStringField("id:", "");
		gd.addNumericField("value:", 0, 1);
		gd.addRadioButtonGroup("constant:", bool, 1,2,"true");
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
	 * Show dialog.
	 *
	 * @param parameter the parameter
	 * @return the parameter
	 */
	public Parameter showDialog(Parameter parameter){
		this.parameter = parameter;
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		DiffusionCoefficient dc = sp.getDiffusionCoefficient();
		gd = new GenericDialog("Edit Parameter");
		gd.setResizable(true);
		gd.pack();
		
		gd.addStringField("id:", parameter.getId());
		gd.addNumericField("value:", parameter.getValue(), 1);
		gd.addRadioButtonGroup("constant:", bool, 1, 2, String.valueOf(parameter.getConstant()));
		gd.addChoice("species:", SBMLProcessUtil.listIdToStringArray(model.getListOfSpecies()), dc.getVariable());
		gd.addChoice("type:", SBMLProcessUtil.diffType, SBMLProcessUtil.diffTypeIndexToString(dc.getType()));
		gd.addChoice("coordinate1:", SBMLProcessUtil.lcoord, SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference1()));
		gd.addChoice("coordinate2:", SBMLProcessUtil.lcoord, SBMLProcessUtil.coordinateIndexToString(dc.getCoordinateReference2()));
	
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
		parameter.setConstant(Boolean.getBoolean(gd.getNextRadioButton()));
		SpatialParameterPlugin sp = (SpatialParameterPlugin) parameter.getPlugin("spatial");
		DiffusionCoefficient dc = sp.isSetDiffusionCoefficient() ? sp.getDiffusionCoefficient() : sp.createDiffusionCoefficient();
		dc.setVariable(gd.getNextChoice());
		dc.setType(gd.getNextChoice());

		String coord1 = gd.getNextChoice();
		String coord2 = gd.getNextChoice();
		
		switch (dc.getType()) {
		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_TENSOR:
			dc.setCoordinateReference2(coord2);

		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_ANISOTROPIC:
			dc.setCoordinateReference1(coord1);

		case libsbmlConstants.SPATIAL_DIFFUSIONKIND_ISOTROPIC:
			break;
		}
		
		if(dc.getType() == libsbmlConstants.SPATIAL_DIFFUSIONKIND_TENSOR && dc.getCoordinateReference1() == dc.getCoordinateReference2())
			dc.unsetCoordinateReference2();
		
	} 
}
