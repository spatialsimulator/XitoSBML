import java.util.HashMap;

import org.sbml.libsbml.libsbml;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij3d.Image3DUniverse;


public class mainSpatial implements PlugIn {
	private ImagePlus image;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValue;
	
	@Override
	public void run(String arg0) {
		image = WindowManager.getCurrentImage();
		Interpolate interpolate = new Interpolate(image);
		image = interpolate.getInterpolatedImage();
		Image3DUniverse univ = new Image3DUniverse();
		univ.show();
		univ.addVoltex(image);
		imageEdit edit = new imageEdit(image); 
		gui(edit);
		edit.createMembrane(hashDomainTypes, hashSampledValue);
		new hierarchicalStruct(edit);
		
        RawSpatialImage ri = new RawSpatialImage(edit.pixels, image.getWidth(), image.getHeight(), image.getStackSize(), hashDomainTypes, hashSampledValue, edit.hashDomainNum, edit.adjacentsList);
        SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);                                 //calls sbmlexporter and create sbml document with string s
        sbmlexp.createGeometryElements();
  
        save(sbmlexp);
        IJ.log(edit.pixels.toString());
	}

	public void gui(imageEdit edit) {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer>();
		new NamePanel(edit.labelList, edit.hashLabelNum, hashDomainTypes, hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					System.out.println("gui inprogress");
				}
			}
		}
	}

	public void save(SpatialSBMLExporter sbmlexp){
		SaveDialog sd = new SaveDialog("Save SBML Document",image.getTitle(),".xml");
		String name = sd.getFileName();
		IJ.log(name);
		try{
			sbmlexp.document.getModel().setId(name);
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);                             //write SBML document to xml file
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
//		IJ.log(pixels.toString()); //print matrix
        IJ.log(sbmlexp.document.toSBML());
        
	}
	
}
