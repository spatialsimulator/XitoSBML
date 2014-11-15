import java.util.HashMap;

import org.sbml.libsbml.libsbml;

import ij.IJ;
import ij.ImagePlus;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij3d.Content;
import ij3d.Image3DUniverse;


public class mainSpatial implements PlugIn {
	private ImagePlus image;
	private ImageExplorer imgexp;
	private HashMap<String, Integer> hashDomainTypes;
	private HashMap<String, Integer> hashSampledValue;
	private Image3DUniverse univ;
	
	@Override
	public void run(String arg) {
		gui();
		CreateImage creIm = new CreateImage(imgexp.getDomFile(),hashSampledValue, imgexp.getFileInfo());
		
		Interpolate interpolate = new Interpolate(creIm.getCompoImg());
		image = interpolate.getInterpolatedImage();
		univ = new Image3DUniverse();
		univ.show();
		Content c = univ.addVoltex(creIm.getCompoImg());
		c.setTransparency(40);
		
		imageEdit edit = new imageEdit(image, hashDomainTypes, hashSampledValue);

		new hierarchicalStruct(edit);
		RawSpatialImage ri = new RawSpatialImage(edit.pixels, image.getWidth(),
				image.getHeight(), image.getStackSize(), hashDomainTypes,
				hashSampledValue, edit.hashDomainNum, edit.adjacentsList);
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(ri);
		sbmlexp.createGeometryElements();
		save(sbmlexp);
		// IJ.log(edit.pixels.toString());
		
	}

	public void gui() {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer> ();
		imgexp = new ImageExplorer(hashDomainTypes,hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
			if(name.contains(".")) libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name);  
			else 					libsbml.writeSBMLToFile(sbmlexp.document, sd.getDirectory() + "/" + name + ".xml"); 
		}catch(NullPointerException e){
			System.out.println("SBML document was not saved");
		}
        IJ.log(sbmlexp.document.toSBML());
	}
	
}
