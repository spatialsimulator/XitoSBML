package jp.ac.keio.bio.fun.xitosbml.cli;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;

import ij.ImagePlus;
import jp.ac.keio.bio.fun.xitosbml.image.CreateImage;
import jp.ac.keio.bio.fun.xitosbml.image.Filler;
import jp.ac.keio.bio.fun.xitosbml.image.ImageBorder;
import jp.ac.keio.bio.fun.xitosbml.image.ImageEdit;
import jp.ac.keio.bio.fun.xitosbml.image.Interpolator;
import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;
import jp.ac.keio.bio.fun.xitosbml.visual.DomainStruct;
import jp.ac.keio.bio.fun.xitosbml.visual.Viewer;
import jp.ac.keio.bio.fun.xitosbml.xitosbml.SpatialSBMLExporter;

/**
 * 
 * The class CliMainImgSpatial.
 * 
 * This class implements the "run XitoSBML_CLI version" function. Date Created:
 * August 9, 2020
 * 
 * @author Medha Bhattacharya
 * @author Akira Funahashi
 * @author Kaito Ii
 * @author Yuta Tokuoka
 *
 */

public class CliMainImgSpatial {
	/** The ImagePlus image */
	protected ImagePlus imager;

	/** The SBML document. */
	protected SBMLDocument document;

	/** The SBML model. */
	protected Model model;

	/** The SBML spatialplugin. */
	protected SpatialModelPlugin spatialplugin;

	/** The hashmap of domain types. */
	private HashMap<String, Integer> hashDomainTypes;

	/** The hashmap of sampled value of spatial image. */
	protected HashMap<String, Integer> hashSampledValue;

	/** The viewer. */
	protected Viewer viewer;

	/**
	 * The SpatialImage, which is a class for handling spatial image in XitoSBML.
	 */
	protected SpatialImage spImg;

	/**
	 * The GetImgDom, which is a class responsible for associating the input image
	 * file with its domain
	 */
	protected GetImgDom imgDom;

	/**
	 * The path to an image file is taken as a parameter, which is used to
	 * instantiate an ImagePlus object. The domain corresponding to this image is
	 * set to 'Cytosol'. The following process is performed.
	 * <ol>
	 * <li>Interpolate an image if it is a Z-stack image (3D image)</li>
	 * <li>Fill holes (blank pixels) in the image by morphology operation if
	 * exists</li>
	 * <li>Export generated image to spatial SBML</li>
	 * </ol>
	 * 
	 * @param imagePath  the path for obtaining input image
	 * @param outputPath the path for saving the output SBML model
	 */
	public void runCli(String imagePath, String outputPath) {
		// Creating an ImagePlus object from the specified image path
		ImagePlus imager = new ImagePlus(imagePath);
		this.imager = imager;
		cli(imager);
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		model = sbmlexp.getModel();
		sbmlexp.createGeometryElements(); // visualize(spImg);

		sbmlexp.addCoordParameter();
		document = sbmlexp.getDocument();

		// To save the SBML document
		try {
			SBMLWriter.write(document, new File(outputPath), ' ', (short) 2);
			System.out.println("Saved model");
		} catch (NullPointerException e) {
			System.out.println("SBML document was not saved");
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// So that the original image does not get overwritten
		String spImgPath = null;
		if (outputPath.contains(".xml"))
			spImgPath = outputPath.substring(0, outputPath.indexOf('.'));
		spImg.saveAsImage(spImgPath);

		showDomainStructure();

		print();
		ModelValidator validator = new ModelValidator(document);
		validator.validate();

	}

	/**
	 * Provides access to the Hashmap of the ImagePlus object and its associated
	 * domain, i.e. Cytosol.
	 * 
	 * @param imager the ImagePlus object
	 */
	protected void cli(ImagePlus imager) {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer>();
		// imager.show();
		imgDom = new GetImgDom(hashDomainTypes, hashSampledValue, imager);
		// HashMap<String, ImagePlus> hashDomFile = trial.getDomFile();
		// System.out.println(hashDomFile.values());
	}

	/**
	 * Following process is performed to an image:
	 * <ol>
	 * <li>Interpolate an image if it is a Z-stack image (3D image) and the voxel
	 * size of each axis (x, y and z) is not equal</li>
	 * <li>Fill holes (blank pixels) in the image by morphology operation if
	 * exists</li>
	 * <li>Add a membrane between two different domains if exists</li>
	 * </ol>
	 *
	 * The converted image will be generated as
	 * {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}, which is a base class
	 * for representing spatial image in XitoSBML.
	 */
	protected void computeImg() {
		Interpolator interpolator = new Interpolator();
		HashMap<String, ImagePlus> hashDomFile = imgDom.getDomFile();
		interpolator.interpolate(hashDomFile);
		Filler fill = new Filler();

		for (Entry<String, ImagePlus> e : hashDomFile.entrySet())
			hashDomFile.put(e.getKey(), fill.fill(e.getValue()));

		CreateImage creIm = new CreateImage(imgDom.getDomFile(), hashSampledValue);
		spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		ImagePlus img = fill.fill(spImg);
		spImg.setImage(img);
		ImageBorder imgBorder = new ImageBorder(spImg);
		spImg.updateImage(imgBorder.getStackImage());

		new ImageEdit(spImg);

	}

	/**
	 * Show inclusion relationship of domains as a graph.
	 */
	protected void showDomainStructure() {
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		new DomainStruct().show(g);
	}

	/**
	 * Prints the SBML document to stdout.
	 */
	protected void print() {
		String docStr;
		try {
			docStr = new TidySBMLWriter().writeSBMLToString(document);
			System.out.println(docStr);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
