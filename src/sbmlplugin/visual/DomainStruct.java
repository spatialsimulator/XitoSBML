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


public class DomainStruct {

	Geometry geometry;
	ListOfDomains lod;
	ListOfAdjacentDomains load;
	ListOfSampledVolumes losv;
	List<String> orderedList = new ArrayList<String>();
	
	public void show(Geometry geometry){
		
		this.geometry = geometry;
		this.lod = geometry.getListOfDomains();
		this.load = geometry.getListOfAdjacentDomains();
		GraphStruct GraphStruct = new GraphStruct();
		vertex(GraphStruct);
		GeometryDefinition gd = geometry.getGeometryDefinition(0);				// for multiple geometry
		
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
		if(orderedList.indexOf(dom1) > orderedList.indexOf(dom2)) return true; 
		else	return false;
	}
	
}
