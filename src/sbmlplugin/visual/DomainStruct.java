package sbmlplugin.visual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.spatial.AdjacentDomains;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.AnalyticVolume;
import org.sbml.jsbml.ext.spatial.Domain;
import org.sbml.jsbml.ext.spatial.DomainType;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;


// TODO: Auto-generated Javadoc
/**
 * The Class DomainStruct.
 */
public class DomainStruct {

	private int dimension;
	
	/** The geometry. */
	private Geometry geometry;
	
	private ListOf<DomainType> lodt;
	
	/** The lod. */
	private ListOf<Domain> lod;
	
	/** The load. */
	private ListOf<AdjacentDomains> load;
		
	/** The ordered list. */
	private List<String> orderedList = new ArrayList<String>();
	
	private GraphStruct graphStruct;
	
	/**
	 * Show.
	 *
	 * @param geometry the geometry
	 */
	public void show(Geometry geometry){
		this.geometry = geometry;
		this.lodt = geometry.getListOfDomainTypes();
		this.lod = geometry.getListOfDomains();
		this.load = geometry.getListOfAdjacentDomains();
		this.dimension = geometry.getListOfCoordinateComponents().size();
		
		graphStruct = new GraphStruct();
		addVertex(lod);
		GeometryDefinition gd = geometry.getListOfGeometryDefinitions().get(0); 	//TODO multiple definitions
		
		if(gd instanceof SampledFieldGeometry){
			SampledFieldGeometry sfg = (SampledFieldGeometry) gd;
			createSampledDomainOrder(sfg.getListOfSampledVolumes());

		} else if(gd instanceof AnalyticGeometry){
			AnalyticGeometry ag = (AnalyticGeometry) gd;
			createAnalyticDomainOrder(ag.getListOfAnalyticVolumes());
		}
		addEdge(load);
		graphStruct.visualize();
	}

	/**
	 * Creates the analytic domain order.
	 *
	 * @param listOf the list of
	 */
	private void createAnalyticDomainOrder(ListOf<AnalyticVolume> listOf){
		int numDom = (int) listOf.size();
		
		for(int i = 0 ; i < numDom ; i++){
			for(int j = 0; j < numDom ; j++){
				AnalyticVolume	av = listOf.get(j);
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
	 * Edge.
	 * TODO change to a better algorithm since this assumes the order of load to be specified
	 * @param domainStruct 
	 */
	public void addEdge(ListOf<AdjacentDomains> load){
		for(int i = 0; i < load.size(); i+=2){
			String dom1 = ((AdjacentDomains)load.get(i)).getDomain2();
			String dom2 = ((AdjacentDomains)load.get(i+1)).getDomain2();
			lod.get(dom1);
			if(getOrder(dom1, dom2))
				graphStruct.addEdge(dom1, dom2);
			else
				graphStruct.addEdge(dom2, dom1);
		}
	}
	
	
	/**
	 * Vertex.
	 *
	 * @param GraphStruct the graph struct
	 */
	public void addVertex(ListOf<Domain> lod) {
		Domain dom;
		for (int i = 0; i < lod.size(); i++) {
			dom = lod.get(i);
			if(lodt.get(dom.getDomainType()).getSpatialDimensions() == dimension)
				graphStruct.addVertex(dom.getSpatialId());
		}
	}
	
	/**
	 * Gets the order.
	 *
	 * @param dom1 the dom 1
	 * @param dom2 the dom 2
	 * @return the order
	 */
	boolean getOrder(String dom1, String dom2){
		 dom1 = dom1.replaceAll("[0-9]","");
		 dom2 = dom2.replaceAll("[0-9]","");
		if(orderedList.indexOf(dom1) > orderedList.indexOf(dom2)) 
			return true; 
		else	
			return false;
	}

	public GraphStruct getGraphStruct() {
		return graphStruct;
	}

	public void setGraphStruct(GraphStruct graphStruct) {
		this.graphStruct = graphStruct;
	}
	
}
