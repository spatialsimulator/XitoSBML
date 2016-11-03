package sbmlplugin.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.AnalyticGeometry;
import org.sbml.libsbml.AnalyticVolume;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOfAdjacentDomains;
import org.sbml.libsbml.ListOfAnalyticVolumes;
import org.sbml.libsbml.ListOfDomains;
import org.sbml.libsbml.ListOfSampledVolumes;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SampledVolume;


// TODO: Auto-generated Javadoc
/**
 * The Class DomainStruct.
 */
public class DomainStruct {

	/** The geometry. */
	Geometry geometry;
	
	/** The lod. */
	ListOfDomains lod;
	
	/** The load. */
	ListOfAdjacentDomains load;
	
	/** The losv. */
	ListOfSampledVolumes losv;
	
	/** The ordered list. */
	List<String> orderedList = new ArrayList<String>();
	
	/**
	 * Show.
	 *
	 * @param geometry the geometry
	 */
	public void show(Geometry geometry){
		this.geometry = geometry;
		this.lod = geometry.getListOfDomains();
		this.load = geometry.getListOfAdjacentDomains();
		GraphStruct GraphStruct = new GraphStruct();
		vertex(GraphStruct);
		GeometryDefinition gd = geometry.getGeometryDefinition(0);				// multiple geometry
		
		if(gd.isSampledFieldGeometry()){
			SampledFieldGeometry sfg = (SampledFieldGeometry) gd;
			createDomainOrder(sfg.getListOfSampledVolumes());

		} else if(gd.isAnalyticGeometry()){
			AnalyticGeometry ag = (AnalyticGeometry) gd;	
			createDomainOrder(ag.getListOfAnalyticVolumes());
		}
		edge(GraphStruct);
		GraphStruct.visualize();
	}

	/**
	 * Creates the domain order.
	 *
	 * @param loav the loav
	 */
	private void createDomainOrder(ListOfAnalyticVolumes loav){
		int numDom = (int) loav.size();
		
		for(int i = 0 ; i < numDom ; i++){
			AnalyticVolume av;
			for(int j = 0; j < numDom ; j++){
				av = loav.get(j);
				if(av.getOrdinal() == i){
					orderedList.add(av.getDomainType());
				}
			}
		}
	}
	
	/**
	 * Creates the domain order.
	 *
	 * @param losv the losv
	 */
	private void createDomainOrder(ListOfSampledVolumes losv){
		int numDom = (int) losv.size();
		List<Double> sampleList = new ArrayList<Double>();
		for(int i = 0; i < numDom ; i++){
			sampleList.add(losv.get(i).getSampledValue());
		}
		Collections.sort(sampleList);
		for(int i = 0; i < numDom ; i++){
			for(int j = 0 ; j < numDom ; j++){
				SampledVolume sv = losv.get(j);
				if(sampleList.get(i) == sv.getSampledValue()){
					orderedList.add(sv.getDomainType());		
				}
			}
		}
	}
	
	/**
	 * Vertex.
	 *
	 * @param GraphStruct the graph struct
	 */
	private void vertex(GraphStruct GraphStruct) {
		Domain dom;
		for (int i = 0; i < lod.size(); i++) {
			dom = lod.get(i);
			if(!dom.getId().contains("membrane"))
				GraphStruct.addVertex(dom.getId());
		}
	}
	
	/**
	 * Edge.
	 *
	 * @param GraphStruct the graph struct
	 */
	private void edge(GraphStruct GraphStruct){
		for(int i = 0; i < load.size(); i+=2){
			addedge( ((AdjacentDomains) load.get(i)).getDomain2(), ((AdjacentDomains) load.get(i+1)).getDomain2(), GraphStruct);			
		}
	}
	
	/**
	 * Addedge.
	 *
	 * @param dom1 the dom 1
	 * @param dom2 the dom 2
	 * @param GraphStruct the graph struct
	 */
	private void addedge(String dom1, String dom2, GraphStruct GraphStruct){
		if(getOrder(dom1, dom2)) GraphStruct.addEdge(dom1, dom2);
		else GraphStruct.addEdge(dom2, dom1);
	}
	
	/**
	 * Gets the order.
	 *
	 * @param dom1 the dom 1
	 * @param dom2 the dom 2
	 * @return the order
	 */
	private boolean getOrder(String dom1, String dom2){
		 dom1 = dom1.replaceAll("[0-9]","");
		 dom2 = dom2.replaceAll("[0-9]","");
		if(orderedList.indexOf(dom1) > orderedList.indexOf(dom2)) return true; 
		else	return false;
	}
	
}
