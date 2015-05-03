
import java.util.ArrayList;
import java.util.List;

import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ListOfAdjacentDomains;
import org.sbml.libsbml.ListOfDomains;
import org.sbml.libsbml.ListOfSampledVolumes;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SpatialModelPlugin;


public class DomainStruct {
	static {
		try{
			System.loadLibrary("sbmlj");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	Model model;
	ListOfDomains lod;
	ListOfAdjacentDomains load;
	ListOfSampledVolumes losv;
	List<String> domainOrder = new ArrayList<String>();
	
	public void show(Model model){
		this.model = model;
		SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		this.lod = g.getListOfDomains();
		this.load = g.getListOfAdjacentDomains();
		SampledFieldGeometry sfg = (SampledFieldGeometry)g.getGeometryDefinition("mySampledField");
		losv = sfg.getListOfSampledVolumes();
	
		GraphStruct GraphStruct = new GraphStruct();
		vertex(GraphStruct);
		edge(GraphStruct);
		GraphStruct.visualize();
	}

	private void vertex(GraphStruct GraphStruct) {
		Domain dom;
		for (int i = 0; i < lod.size(); i++) {
			dom = lod.get(i);
			if(!dom.getId().contains("membrane"))
				GraphStruct.addVertex(dom.getId());
		}
	}
	
	private void edge(GraphStruct GraphStruct){
		for(int i = 0; i < load.size(); i+=2){
			addedge( ((AdjacentDomains) load.get(i)).getDomain2(), ((AdjacentDomains) load.get(i+1)).getDomain2(), GraphStruct);			
		}
	}
	
	private void addedge(String dom1, String dom2, GraphStruct GraphStruct){
		if(getOrder(dom1, dom2)) GraphStruct.addEdge(dom1, dom2);
		else GraphStruct.addEdge(dom2, dom1);
	}
	
	private boolean getOrder(String dom1, String dom2){
		 dom1 = dom1.replaceAll("[0-9]","");
		 dom2 = dom2.replaceAll("[0-9]","");
		if(losv.get(dom1).getSampledValue() > losv.get(dom2).getSampledValue()) return true; 
		else	return false;
	}
	
	public static void main(String[] args){
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("mem_diff.xml");
		DomainStruct ds = new DomainStruct();
		ds.show(d.getModel());
	}
}
