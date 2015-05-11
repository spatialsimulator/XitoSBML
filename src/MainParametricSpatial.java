import ij.plugin.PlugIn;

import javax.swing.JOptionPane;


public class MainParametricSpatial extends MainSpatial implements PlugIn{
	
	@Override
	public void run(String arg) {
		/*
		Frame[] f =ImageJ.getFrames();
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
		createSBMLDoc();
		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
		visualize(spImg);

		sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
		
		
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addParaAndSpecies();

		sbmlexp.addCoordParameter();
		save(sbmlexp);
		//SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg, document);
	}
}
