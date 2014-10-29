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
	public void run(String arg) {
		Interpolate interpolate = new Interpolate(WindowManager.getCurrentImage());
		image = interpolate.getInterpolatedImage();
		Image3DUniverse univ = new Image3DUniverse();
		univ.show();
//		univ.addVoltex(image);	
	//	imageEdit edit = new imageEdit(image); 
		univ.addVoltex(WindowManager.getCurrentImage());
		imageEdit edit = new imageEdit(WindowManager.getCurrentImage());
		gui(edit);
		if (checkHash()) {
			edit.createMembrane(hashDomainTypes, hashSampledValue);
			new hierarchicalStruct(edit);
			RawSpatialImage ri = new RawSpatialImage(edit.pixels,
					image.getWidth(), image.getHeight(), image.getStackSize(),
					hashDomainTypes, hashSampledValue, edit.hashDomainNum,
					edit.adjacentsList);
			SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);
			sbmlexp.createGeometryElements();
			save(sbmlexp);
			//IJ.log(edit.pixels.toString());
		}
	}

	public void gui(imageEdit edit) {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer>();
		new NamePanel(edit.labelList, edit.hashLabelNum, hashDomainTypes, hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					
				}
			}
		}
	}

	private boolean checkHash(){
		if(hashDomainTypes.containsKey("") || hashSampledValue.containsKey("")){
			IJ.error("Missing DomainType name in the table");
			return false;
		}
			
		
		return true;
	}
	
	public void save(SpatialSBMLExporter sbmlexp){
		SaveDialog sd = new SaveDialog("Save SBML Document",image.getTitle(),".xml");
		String name = sd.getFileName();
		IJ.log(name);
		try{
			sbmlexp.document.getModel().setId(name);
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);  
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
        IJ.log(sbmlexp.document.toSBML());
	}
	
}
