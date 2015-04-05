
import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ListOfAdjacentDomains;
import org.sbml.libsbml.ListOfDomains;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialModelPlugin;


public class DomainStruct {
	static {
	  //  System.loadLibrary("sbmlj");                //read system library sbmlj
	  }
	Model model;
	ListOfDomains lod;
	ListOfAdjacentDomains load;
	
	DomainStruct(){
		
	}
	
	public void show(Model model){
		this.model = model;
		SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		this.lod = g.getListOfDomains();
		this.load = g.getListOfAdjacentDomains();
		
		Graph graph = new Graph();
		vertex(graph);
		edge(graph);
		graph.visualize();
	}
	public void vertex(Graph graph){ 
		Domain dom;
			for (int i = 0; i < lod.size(); i++) {
				dom = lod.get(i);
				graph.addVertex(dom.getId());
			}
	}
	
	public void edge(Graph graph){
	AdjacentDomains ad;
		for(int i = 0; i < load.size(); i++){
			ad = (AdjacentDomains) load.get(i);
			graph.addEdge(ad.getDomain2(), ad.getDomain1());
		}
	}
	
	public static void main(String[] args){
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("outttt.xml");
		DomainStruct ds = new DomainStruct();
		ds.show(d.getModel());
	}
}
