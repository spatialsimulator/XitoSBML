/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package sbmlplugin.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.AdjacentDomains;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.AnalyticVolume;
import org.sbml.jsbml.ext.spatial.Domain;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;


// TODO: Auto-generated Javadoc
/**
 * The Class DomainStruct.
 */
public class DomainStruct {

	/** The geometry. */
	Geometry geometry;
	
	/** The lod. */
	ListOf<Domain> lod;
	
	/** The load. */
	ListOf<AdjacentDomains> load;
	
	/** The losv. */
	ListOf<SampledVolume> losv;
	
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
		GeometryDefinition gd = geometry.getListOfGeometryDefinitions().get(0); 	//TODO multiple definitions
		
		if(gd instanceof SampledFieldGeometry){
			SampledFieldGeometry sfg = (SampledFieldGeometry) gd;
			createSampledDomainOrder(sfg.getListOfSampledVolumes());

		} else if(gd instanceof AnalyticGeometry){
			AnalyticGeometry ag = (AnalyticGeometry) gd;	
			createAnalyticDomainOrder(ag.getListOfAnalyticVolumes());
		}
		edge(GraphStruct);
		GraphStruct.visualize();
	}

	/**
	 * Creates the analytic domain order.
	 *
	 * @param listOf the list of
	 */
	private void createAnalyticDomainOrder(ListOf<AnalyticVolume> listOf){
		int numDom = (int) listOf.size();
		
		for(int i = 0 ; i < numDom ; i++){
			AnalyticVolume av;
			for(int j = 0; j < numDom ; j++){
				av = listOf.get(j);
				if(av.getOrdinal() == i){
					orderedList.add(av.getDomainType());
				}
			}
		}
	}
	
	/**
	 * Creates the sampled domain order.
	 *
	 * @param losv the losv
	 */
	private void createSampledDomainOrder(ListOf<SampledVolume> losv){
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
			if(!dom.getSpatialId().contains("membrane"))
				GraphStruct.addVertex(dom.getSpatialId());
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
