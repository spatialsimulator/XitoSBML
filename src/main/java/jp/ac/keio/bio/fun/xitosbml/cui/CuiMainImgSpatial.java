package jp.ac.keio.bio.fun.xitosbml.cui;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.TidySBMLWriter;

import ij.IJ;
import ij.ImagePlus;
import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;
import jp.ac.keio.bio.fun.xitosbml.xitosbml.SpatialSBMLExporter;

public class CuiMainImgSpatial extends CuiMainSpatial {

	protected ImagePlus imager;

	public void run(String arg) {
		// empty
	}

	public void runCui(String imagePath, String outputPath) {
		ImagePlus imager = new ImagePlus(imagePath);
		this.imager = imager;
		cui(imager);
		computeImgTrial();
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

		try {
			String docStr = new TidySBMLWriter().writeSBMLToString(document);
			IJ.log(docStr);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
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

}
