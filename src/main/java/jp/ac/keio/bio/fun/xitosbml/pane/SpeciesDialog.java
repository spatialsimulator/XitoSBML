package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Arrays;
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.AbstractButton;
import java.awt.TextField;
import java.awt.Choice;
import java.awt.CheckboxGroup;
import java.awt.Checkbox;

import org.sbml.jsbml.IdentifierException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SampledField;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;
import org.sbml.jsbml.ext.spatial.CompressionKind;
import org.sbml.jsbml.ext.spatial.DataKind;
import org.sbml.jsbml.ext.spatial.InterpolationKind;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.SpatialSpeciesPlugin;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;
import org.sbml.jsbml.ext.spatial.SpatialSymbolReference;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.gui.MessageDialog;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.WindowManager;

//import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import java.awt.*;
import java.awt.event.*;

/**
 * The class SpeciesDialog, which generates a GUI for creating / editing
 * Species. This class is used in
 * {@link jp.ac.keio.bio.fun.xitosbml.pane.SpeciesTable}. Date Created: Jan 20,
 * 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SpeciesDialog implements ItemListener {

	/** The JSBML Species object. */
	private Species species;

	/** The generic dialog. */
	private GenericDialog gd;

	/** The string value for initial amount/concentration. */
	private final String[] initial = { "amount", "concentration" };

	/** The string value for units. */
	private final String[] units = { "mole", "item", "gram", "kilogram", "dimensionless" };

	/**
	 * The boolean value for boundaryCondition, constant and hasOnlySubstanceUnit.
	 */
	private final String[] bool = { "true", "false" };

	/** The SBML model. */
	private Model model;

	/** The distribution. */
	private final String[] distribution = { "uniform", "local" };

	/** The Localization from Image. */
	HashMap<String, String> speciesImage = new HashMap<String, String>();

	/**
	 * Instantiates a new species dialog.
	 *
	 * @param model the SBML model
	 */
	public SpeciesDialog(Model model) {
		this.model = model;
	}

	/**
	 *
	 * set HashMap from species dialog
	 *
	 * @param speciesImage the Localization from Image
	 */
	public void setHashMap(HashMap<String, String> speciesImage) {
		this.speciesImage = speciesImage;
	}

	/**
	 *
	 * get species image into species Dialog
	 *
	 * @param SFid the id of species
	 * @return the image name of the given species
	 */
	public StringBuffer getSpeciesImage(String SFid) {
		if (speciesImage.get(SFid) != null) {
			return new StringBuffer(speciesImage.get(SFid)/* imagename */);
		}
		return new StringBuffer("No Image"/* no image */);
	}

	/**
	 *
	 * Switch whether text field is enable
	 * 
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {

		Checkbox chb = (Checkbox) e.getItemSelectable();
		;

		Vector<Checkbox> cb = gd.getCheckboxes();
		Checkbox uniform = cb.get(0);
		Checkbox local = cb.get(1);

		Vector<TextField> nf = new Vector<TextField>();
		nf = gd.getNumericFields();
		TextField text = nf.get(0);

		Vector<Choice> choice = new Vector<Choice>();
		choice = gd.getChoices();
		Choice chc = choice.get(1);

		if (chb.getLabel().equals("uniform")) { // uniform distribution
			if (local.getState()) {
				// local checkbox is off
				uniform.setState(true);
				local.setState(false);
				uniform.show();
				local.show();
				// TextField is enable
				text.setEnabled(true);
				text.show();
				// Choice is enable
				chc.setEnabled(false);
				chc.show();
			} else if (!uniform.getState()) {
				// local checkbox is off
				uniform.setState(true);
				local.setState(false);
				uniform.show();
				local.show();
				// TextField is enable
				text.setEnabled(true);
				text.show();
				// Choice is enable
				chc.setEnabled(false);
				chc.show();
			}
		} else if (chb.getLabel().equals("local")) { // local distribution
			if (uniform.getState()) {
				// uniform checkbox is off
				uniform.setState(false);
				local.setState(true);
				uniform.show();
				local.show();
				// TextField is unenable
				text.setText("0.0");
				text.setEnabled(false);
				text.show();
				// Choice is enable
				chc.setEnabled(true);
				chc.show();
			} else if (!local.getState()) {
				// uniform checkbox is off
				uniform.setState(false);
				local.setState(true);
				uniform.show();
				local.show();
				// TextField is unenable
				text.setText("0.0");
				text.setEnabled(false);
				text.show();
				// Choice is enable
				chc.setEnabled(true);
				chc.show();
			}
		}
	}

	/**
	 * Create and show a dialog for adding Species. If a user creates a species
	 * through this dialog, then a Species object will be returned. If not, null is
	 * returned.
	 *
	 * @return the JSBML Species object if a species is created
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	public Species showDialog() throws IllegalArgumentException, IdentifierException {
		gd = new GenericDialog("Add Species");
		gd.setResizable(true);
		gd.pack();
		// species id
		gd.addStringField("id:", "");
		// compartment
		gd.addChoice("compartment:", SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments()), null);
		// distribution
		gd.addMessage("distribution: ");
		boolean[] distBool = { true, false };
		gd.addCheckboxGroup(1, 2, distribution, distBool);
		Vector<Checkbox> cb = gd.getCheckboxes();
		cb.get(0).addItemListener(this);
		cb.get(1).addItemListener(this);

		// initial amount / concentration
		gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");

		// uniform
		gd.addNumericField("quantity:", 0, 1);
		// local
		addImageChoice("No Image");
		Vector<Choice> choice = gd.getChoices(); // default image choice is unenabled
		choice.get(1).setEnabled(false);

		gd.addChoice("substanceUnit:", units, null);
		// boundary condition
		gd.addRadioButtonGroup("boundaryCondition:", bool, 1, 2, "false");
		// constant
		gd.addRadioButtonGroup("constant:", bool, 1, 2, "false");

		gd.showDialog();
		if (gd.wasCanceled())
			return null;

		species = model.createSpecies();
		setSpeciesData();

		return species;
	}

	/**
	 * Create and show a dialog for adding Species with given JSBML Species object.
	 * If a user edits the species through this dialog, then a Species object will
	 * be returned. If not, null is returned.
	 *
	 * @param species the JSBML Species object
	 * @return the JSBML Species object if the parameter is edited
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	public Species showDialog(Species species) throws IllegalArgumentException, IdentifierException {

		this.species = species;
		gd = new GenericDialog("Edit Species");
		gd.setResizable(true);
		gd.pack();

		gd.addStringField("id:", species.getId()); // id
		gd.addChoice("compartment:", SBMLProcessUtil.listIdToStringArray(model.getListOfCompartments()),
				species.getCompartment()); // compartment

		String dist = null;
		SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry geometry = spatialplugin.getGeometry();
		ListOf<SampledField> losf = geometry.getListOfSampledFields();

		int numOfLosf = 0;
		for (numOfLosf = 0; numOfLosf < (int) losf.size(); numOfLosf++) { // local distribution
			if (losf.get(numOfLosf).getId().equals(species.getId() + "_initialConcentration")) {

				// distribution
				dist = "local";
				gd.addMessage("distribution: ");
				boolean[] distBool = { false, true };
				gd.addCheckboxGroup(1, 2, distribution, distBool);
				Vector<Checkbox> cb = gd.getCheckboxes();
				cb.get(0).addItemListener(this);
				cb.get(1).addItemListener(this);

				if (species.isSetInitialAmount()) {
					gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");
					gd.addNumericField("quantity:", 0/* species.getInitialAmount() */, 1);
				} else if (species.isSetInitialConcentration()) {
					gd.addRadioButtonGroup("initial:", initial, 1, 2, "concentration");
					gd.addNumericField("quantity:", 0/* species.getInitialConcentration() */, 1);
				}

				Vector<TextField> nf = new Vector<TextField>();
				nf = gd.getNumericFields();
				TextField text = nf.get(0);
				text.setEnabled(false);

				break;
			}
		}
		if (numOfLosf == (int) losf.size()) { // uniform distribution

			// distribution
			dist = "uniform";
			gd.addMessage("distribution: ");
			boolean[] distBool = { true, false };
			gd.addCheckboxGroup(1, 2, distribution, distBool);
			Vector<Checkbox> cb = gd.getCheckboxes();
			cb.get(0).addItemListener(this);
			cb.get(1).addItemListener(this);

			if (species.isSetInitialAmount()) {
				gd.addRadioButtonGroup("initial:", initial, 1, 2, "amount");
				gd.addNumericField("quantity:", species.getInitialAmount(), 1);
			} else if (species.isSetInitialConcentration()) {
				gd.addRadioButtonGroup("initial:", initial, 1, 2, "concentration");
				gd.addNumericField("quantity:", species.getInitialConcentration(), 1);
			}
		}

		String SFid = species.getId() + "_initialConcentration";
		addImageChoice(speciesImage.get(SFid)/* , dist */ );
		if (dist.equals("uniform")) {
			Vector<Choice> chc = gd.getChoices();
			chc.get(1).setEnabled(false);
		}

		gd.addChoice("substanceUnit:", units, species.getUnits());
		gd.addRadioButtonGroup("boundaryCondition:", bool, 1, 2, String.valueOf(species.getBoundaryCondition()));
		gd.addRadioButtonGroup("constant:", bool, 1, 2, String.valueOf(species.getConstant()));
		gd.addRadioButtonGroup("hasOnlySubstnaceUnit:", bool, 1, 2, String.valueOf(species.getHasOnlySubstanceUnits()));
		// show dialog
		gd.showDialog();
		if (gd.wasCanceled())
			return null;

		// delete previous species data
		for (int i = 0; i < model.getNumSpecies(); i++) { // added by morita
			if (model.getSpecies(i).getId().equals(species.getId())) {
				model.getListOfSpecies().remove(i);
				break;
			}
		}
		if (dist != null && dist.equals("local")) {
			model.removeParameter(species.getId() + "_initialConcentration");
			for (int i = 0; i < model.getNumInitialAssignments(); i++) { // added by morita
				if (model.getInitialAssignment(i).getSymbol().equals(species.getId())) {
					model.getListOfInitialAssignments().remove(i);
					break;
				}
			}
			// model.getListOfInitialAssignments().remove(species.getId());
			geometry.removeSampledField(numOfLosf);
		}

		// set species data
		setSpeciesData();

		return this.species;
	}

	/**
	 * Sets/updates the following information of the species from the GUI.
	 * <ul>
	 * <li>String:Id</li>
	 * <li>double:initialAmount or initialConcentration</li>
	 * <li>String:compartment</li>
	 * <li>Kind:substance units</li>
	 * <li>boolean:has only substance units</li>
	 * <li>boolean:boundary condition</li>
	 * <li>boolean:constant</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	private void setSpeciesData() throws IllegalArgumentException, IdentifierException {

		String str = gd.getNextString();
		if (str.indexOf(' ') != -1)
			str = str.replace(' ', '_');

		String sCompartment = gd.getNextChoice();
		if (str.contains(sCompartment)) {
			str = str.replace("_" + sCompartment, "");
		}
		species.setName(str); // set name

		species.setCompartment(sCompartment); // set Compartment
		String SId = str + "_" + species.getCompartment();
		species.setId(SId); // set Id
		// distribute
		Vector<Checkbox> cb = gd.getCheckboxes();
		Checkbox uniform = cb.get(0);
		Checkbox local = cb.get(1);
		// quantity
		String quantity = gd.getNextRadioButton();
		// initial amount / concentration
		if (quantity.contains(initial[0]))
			species.setInitialAmount(gd.getNextNumber());
		else
			species.setInitialConcentration(gd.getNextNumber());

		String SFid = SId + "_initialConcentration";
		if (uniform.getState() /* distribute.equals(distribution[0]) */) { // uniform distribution

			gd.getNextChoice();
			speciesImage.put(SFid, "No Image");

		} else if (local.getState() /* distribute.equals(distribution[1]) */) { // local distribution

			SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
			Geometry geometry = spatialplugin.getGeometry();
			ListOf<SampledField> losf = geometry.getListOfSampledFields();
			ListOf<GeometryDefinition> logd = geometry.getListOfGeometryDefinitions();
			SampledFieldGeometry sfg = (SampledFieldGeometry) logd.get(0);
			ListOf<SampledVolume> losv = sfg.getListOfSampledVolumes();

			double volume = 1;

			for (int i = 0; i < losv.size(); i++) {
				if (losv.get(i).getDomainType().equals(species.getCompartment())) {
					SampledVolume sv = losv.get(i);
					volume = (double) sv.getSampledValue();
				}
			}

			String imagename = gd.getNextChoice();
			if (imagename.equals("No Image")) { // initial assignment ???
				speciesImage.put(SFid, "No Image");
				for (int i = 0; i < losf.size(); i++) {
					if (losf.get(i).getId().equals(SFid)) {
						losf.remove(i);
					}
				}
			} else {
				speciesImage.put(SFid, imagename);
				for (int i = 0; i < losf.size(); i++) {
					if (losf.get(i).getId().equals(SFid)) {
						losf.remove(i);
					}
				}
				InitialAssignment initialAssignment = model.createInitialAssignment();
				addInitalAssignment(initialAssignment, SId);// add initial assignment
				Parameter parameter = model.createParameter();
				addIAParameter(parameter, SFid);// add parameter in initial assignment's math
				double ia = 0;
				ia = addSampledField(imagename, volume);// add sampledField

				if (species.isSetInitialAmount()) {
					species.unsetInitialAmount();
					species.setInitialAmount(ia);
				}
				if (species.isSetInitialConcentration()) {
					species.unsetInitialConcentration();
					species.setInitialConcentration(ia);
				}
			}
		}
		// set substance units
		species.setSubstanceUnits(Unit.Kind.valueOf(gd.getNextChoice().toUpperCase()));

		if (species.isSetInitialAmount())
			species.setHasOnlySubstanceUnits(true);
		else
			species.setHasOnlySubstanceUnits(false);
		// set boundary condition
		Boolean hasBC = Boolean.valueOf(gd.getNextRadioButton());
		if (hasBC) {
			// MessageDialog md = new MessageDialog( null/*gd*/, "USAGE ;; Boundary
			// Condition",
			// "Please select which membrane has which boundary condition at Parameter
			// Panel.\n Also, set the other parameter such as diffusion coefficients for
			// leaked species." );
		}
		species.setBoundaryCondition(hasBC);
		// set constant
		species.setConstant(Boolean.valueOf(gd.getNextRadioButton()));
		SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) species.getPlugin(SpatialConstants.shortLabel);
		ssp.setSpatial(true);
	}

	/**
	 * Adds the image choice.
	 */
	private void addImageChoice(String topImg) {

		int numimage = WindowManager.getImageCount();
		Vector<String> windows = new Vector<String>();

		if (numimage == 0) {
			String[] s = { "No Image" };
			gd.addChoice("Image:", s, "NaN");
			return;
		} else {
			for (int i = 1; i <= numimage; i++) {
				int id = WindowManager.getNthImageID(i);
				ImagePlus ip = WindowManager.getImage(id);
				windows.add(ip.getTitle());
			}
		}

		final String[] images = new String[windows.size() + 1];
		windows.toArray(images);
		for (int i = windows.size(); i > 0; i--) {
			images[i] = images[i - 1];
		}
		images[0] = "No Image";

		gd.addChoice("Localization from Image", images, topImg);

	}

	/**
	 * Sets the species initial assignment.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	@SuppressWarnings("deprecation")
	private void addInitalAssignment(InitialAssignment ia, String SId)
			throws IllegalArgumentException, IdentifierException {// added by Morita

		ia.setSymbol(SId);

		String para = SId + "_initialConcentration";
		ASTNode astnode = new ASTNode(para);
		// astnode.setVariable(para);
		ia.setMath(astnode);
	}

	/**
	 * Sets the species initial assignment.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	private void addIAParameter(Parameter para, String SFid) throws IllegalArgumentException, IdentifierException {// added
																													// by
																													// Morita

		para.setId(SFid);
		para.setConstant(true);
                para.setUnits("mole");
                para.setValues(0); // need to modify average values in a image
		// SpatialSymbolReference
		SpatialParameterPlugin spp = (SpatialParameterPlugin) para.getPlugin(SpatialConstants.namespaceURI);
		SpatialSymbolReference ssr = new SpatialSymbolReference();
		ssr.setSpatialRef(SFid);
	}

	/**
	 * Sets the species sampledField data from Image.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	private double addSampledField(String imagename, double volume)
			throws IllegalArgumentException, IdentifierException {// added by Morita

		IJ.selectWindow(imagename);
		ImagePlus img = IJ.getImage();
		int width = img.getWidth();
		int height = img.getHeight();
		int depth = img.getStackSize();

		// MessageDialog md0 = new MessageDialog(null, "depth", String.valueOf(width));

		SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry geometry = spatialplugin.getGeometry();
		SampledField sf = geometry.createSampledField();

		sf.setSpatialId(species.getId() + "_initialConcentration");
		sf.setDataType(DataKind.DOUBLE);
		sf.setNumSamples1(width);
		sf.setNumSamples2(height);
		if (depth > 1) {
			depth += 2;
		}
		sf.setNumSamples3(depth);
		sf.setInterpolation(InterpolationKind.linear);
		sf.setCompression(CompressionKind.uncompressed);
		sf.setSamplesLength(width * height * depth);

		int brightness[] = new int[width * height * depth];
		for (int i = 0; i < (width * height * depth); i++) {
			brightness[i] = 0;
		}
		int amount = 0;
		String sample;

		ImageStack is = img.getStack();
		// 2 & 3 dimension
		for (int l = 1; l <= depth; l++) {
			if (depth > 1 && l > (depth - 2.0))
				break;
			ImageProcessor ip = is.getProcessor(l);
			for (int m = 0; m < height; m++) {
				for (int n = 0; n < width; n++) {
					if (depth == 1)
						brightness[(l - 1) * width * height + m * width + n] = ip.getPixel(n, m);
					else if (depth > 1)
						brightness[l * width * height + m * width + n] = ip.getPixel(n, m);
				}
			}
		}
		// check assigned local distribution in assigned compartment
		brightness = checkImageBoundary(geometry, brightness, width, height, depth);
		sample = Arrays.toString(brightness).replace("[", "").replace("]", "").replace(",", "");
		for (int i = 0; i < brightness.length; i++)
			amount = Math.max(amount, brightness[i]);
		sf.setSamples(sample);

		return (double) amount;
	}

	/**
	 * check boundary of species distribution from iamge.
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IdentifierException      the identifier exception
	 */
	private int[] checkImageBoundary(Geometry geo, int[] brightness, int width, int height, int depth)
			throws IllegalArgumentException, IdentifierException {// added by Morita
		// get compartment geometry
		SampledField sf = geo.getListOfSampledFields().get(0);
		String uncompr = sf.getSamples();
		int digit = 0;
		int num = 0;
		int[] sudoCompartment = sudoCompartment = new int[width * height * depth];
		int[] sCompartment = new int[width * height * depth];
		if (depth > 1) {
			uncompr = uncompr.substring(2 * width * height);
		}
		for (int i = 1; i < uncompr.length(); i++) {
			if (uncompr.substring(i - 1, i).equals(" ")) {
				if (depth > 1)
					if (num == width * height * (depth - 2))
						break;
				sudoCompartment[num] = Integer.parseInt(uncompr.substring(digit, i - 1));
				num++;
				digit = i;
			}
		}
		if (depth > 1) {
			for (int i = 0; i < (width * height * depth); i++)
				sCompartment[i] = 0;
			for (int i = 0; i < (width * height * (depth - 2.0)); i++)
				sCompartment[i + (width * height)] = sudoCompartment[i];
		} else if (depth == 1) {
			sCompartment = sudoCompartment;
		}

		ListOf<GeometryDefinition> logd = geo.getListOfGeometryDefinitions();
		SampledFieldGeometry sfg = (SampledFieldGeometry) logd.get(0);
		ListOf<SampledVolume> losv = sfg.getListOfSampledVolumes();

		for (int i = 0; i < losv.size(); i++) {
			if (losv.get(i).getDomainType().equals(species.getCompartment())) {

				double sv = losv.get(i).getSampledValue();
				int numBeyond = 0;
				int[] isInCompartment = new int[width * height * depth];
				for (int in = 0; in < (width * height * depth); in++) {
					if (sCompartment[in] == sv) {
						isInCompartment[i] = 1;
					} else { // check species beyond assigned compartment
						isInCompartment[i] = 0;
						if (brightness[in] > 0) {
							numBeyond++;
						}
					}
				}
				// show message in case of species distribution beyond boundary
				if (numBeyond > 0) {
					MessageDialog md = new MessageDialog(null/* gd */, "Species Boundary ERROR",
							"species are distributed beyond your selected compartment!\nSpecies beyond the compartment was deleted.");
					for (int j = 0; j < width * height * depth; j++)
						brightness[i] *= isInCompartment[i];
				}
				break;
			}
		}
		return brightness;
	}

}
