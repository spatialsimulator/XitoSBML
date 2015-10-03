package sbmlplugin.sbmlplugin;
import javax.swing.JOptionPane;

import sbmlplugin.util.ModelSaver;


public class MainParametricSpatial extends MainSpatial{
	
	@Override
	public void run(String arg) {
		gui();
		computeImg();
		SpatialSBMLExporter sbmlexp = new SpatialSBMLExporter(spImg);
		visualize(spImg);
		viewer.findPoints();
		sbmlexp.createParametric(viewer.gethashVertices(), viewer.gethashBound());
		
		
		int reply = JOptionPane.showConfirmDialog(null, "Do you want to add Parameters or Species to the model?", "Adding Parameters and species", JOptionPane.YES_NO_CANCEL_OPTION);
		if(reply == JOptionPane.YES_OPTION)
			addParaAndSpecies();

		sbmlexp.addCoordParameter();
		document = sbmlexp.getDocument();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
	}
}
