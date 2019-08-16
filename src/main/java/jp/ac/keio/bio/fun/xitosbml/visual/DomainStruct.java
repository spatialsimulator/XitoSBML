package jp.ac.keio.bio.fun.xitosbml.visual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.spatial.AdjacentDomains;
import org.sbml.jsbml.ext.spatial.AnalyticGeometry;
import org.sbml.jsbml.ext.spatial.AnalyticVolume;
import org.sbml.jsbml.ext.spatial.Domain;
import org.sbml.jsbml.ext.spatial.DomainType;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryDefinition;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;


/**
 * The class DomainStruct, which defines the structure of domains, and visualize the graph with JGraphX.
 * The graph used in XitoSBML is an inclusion relationship of domains.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class DomainStruct {

	/** The dimension of CoordinateComponent. */
	private int dimension;
	
	/** The list of DomainTypes. */
	private ListOf<DomainType> lodt;
	
	/** The list of Domains. */
	private ListOf<Domain> lod;
	
	/** The list of AdjacentDomains. */
	private ListOf<AdjacentDomains> load;
		
	/** The ordered list of DomainTypes. */
	private List<String> orderedList = new ArrayList<String>();
	
	/** The graph structure of an inclusion relationship of domains. */
	private GraphStruct graphStruct;
	
	/**
	 * Visualize the graph structure of an inclusion relationship of domains.
	 * Currently it supports the domain order of SampledFieldGeometry and AnalyticGeometry.
	 *
	 * @param geometry the geometry obtained from SBML model
	 */
	public void show(Geometry geometry){
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
	 * @param listOf the list of AnalyticVolume
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
	 * @param losv the list of SampledVolume
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
	 * Add edges to the graph structure of an inclusion relationship of domains.
	 * TODO change to a better algorithm since this assumes the order of list of AdjacentDomains to be specified
	 *
	 * @param load the list of AdjacentDomains
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
	 * Add vertices to the graph structure of an inclusion relationship of domains.
	 *
	 * @param lod the list of Domains
	 */
	public void addVertex(ListOf<Domain> lod) {
		Domain dom;
		for (int i = 0; i < lod.size(); i++) {
			dom = lod.get(i);
			if(getDomainType(dom.getDomainType()).getSpatialDimensions() == dimension)
				graphStruct.addVertex(dom.getSpatialId());
		}
	}

	/**
	 * Get domain type by given SpatialId.
	 *
	 * @param spId the SpatialId of domain type as String
	 * @return
	 */
	public DomainType getDomainType(String spId){
	   	 for(DomainType d: lodt){
   		 if(d.getSpatialId().equals(spId)){
   			 return d;
   		 }
   	 }
		return null;
	}

	/**
	 * Compare the order of given DomainType1 and 2, and then returns true if
	 * the order of DomainType1 is larger than that of DomainType2.
	 *
	 * @param domType1 the DomainType 1
	 * @param domType2 the DomainType 2
	 * @return the order
	 */
	boolean getOrder(String domType1, String domType2){
		 domType1 = domType1.replaceAll("[0-9]","");
		 domType2 = domType2.replaceAll("[0-9]","");
		if(orderedList.indexOf(domType1) > orderedList.indexOf(domType2))
			return true; 
		else	
			return false;
	}

	/**
	 * Gets the graph structure.
	 *
	 * @return the graph structure
	 */
	public GraphStruct getGraphStruct() {
		return graphStruct;
	}

	/**
	 * Sets the graph structure.
	 *
	 * @param graphStruct the new graph structure
	 */
	public void setGraphStruct(GraphStruct graphStruct) {
		this.graphStruct = graphStruct;
	}

	/**
	 * Example main() method which will launch a GUI and draw an inclusion relationship of domains as a graph.
     * This example requires a specific SBML model "analytic_3d.xml" located under the "sample/" directory.
	 * @param args an array of command-line arguments for the application
	 */
	public static void main(String[] args){
		try {
			SBMLDocument d = SBMLReader.read(new File("sample/analytic_3d.xml"));
			Model m = d.getModel();
			SpatialModelPlugin smp = (SpatialModelPlugin) m.getPlugin("spatial");
			Geometry g = smp.getGeometry();
			DomainStruct ds = new DomainStruct();
			ds.show(g);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
