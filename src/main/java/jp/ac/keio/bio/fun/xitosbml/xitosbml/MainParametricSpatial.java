package jp.ac.keio.bio.fun.xitosbml.xitosbml;
import javax.swing.JOptionPane;

import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;

/**
 * The class MainParametricSpatial, which implements "run Spatial Parametric SBML plugin" function.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class MainParametricSpatial extends MainSpatial{
	
	/**
	 * Overrides ij.plugin.PlugIn#run(java.lang.String).
	 * A dialog for specifying the correspondence between the image and the region in the cell will be displayed.
	 * The specified image is read and the following process is performed.
	 *
	 * 1. Interpolate an image if it is a Z-stack image (3D image)
	 * 2. Fill holes (blank pixels) in the image by morphology operation if exists
	 * 3. Export generated image to spatial parametric SBML
	 *
	 * @param arg name of the method defined in plugins.config
	 */
	@Override
	public void run(String arg) {
		/*
		Frame[] f = ImageJ.getFrames();
		
		Frame frame = null;
		for(int i = 0 ; i < f.length ; i++){
			if(f[i].getTitle().equals("ImageJ 3D Viewer")) frame = f[i];
		}
		if(frame == null){
			IJ.error("3D Viewer not opened");
			return;
		}
		ImageWindow3D win = (ImageWindow3D) frame;
		univ = (Image3DUniverse) win.getUniverse();
		*/

		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		visualize(spImg);
		viewer.findPoints();
		sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
		
		
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addSBases();

		sbmlexp.addCoordParameter();
		document = sbmlexp.getDocument();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
	}
}
