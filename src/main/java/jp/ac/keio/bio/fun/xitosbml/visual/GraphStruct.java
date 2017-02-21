package jp.ac.keio.bio.fun.xitosbml.visual;


import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class GraphStruct.
 */
@SuppressWarnings("serial")
public class GraphStruct extends JFrame{
   
	/** The g. */
	ListenableGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    
    /** The jgx adapter. */
    JGraphXAdapter<String, DefaultEdge>  jgxAdapter = new JGraphXAdapter<String, DefaultEdge>(g);

	/**
	 * Instantiates a new graph struct.
	 */
	GraphStruct(){
		super("Domain Hiearchal Structure");
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(200,100));
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Adds the vertex.
	 *
	 * @param name the name
	 */
	public void addVertex(String name){
		g.addVertex(name);
	}
	
	/**
	 * Adds the edge.
	 *
	 * @param v1 the v 1
	 * @param v2 the v 2
	 */
	public void addEdge(String v1, String v2){
		g.addEdge(v1, v2);
	}
	
	/**
	 * Visualize.
	 */
	public void visualize(){
		mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);	
        layout.setDisableEdgeStyle(false);
        layout.getGraph().setCellsLocked(true);
        layout.getGraph().setAutoOrigin(true);
        layout.run(jgxAdapter.getDefaultParent());
        mxGraphComponent gc = new mxGraphComponent(jgxAdapter);
        gc.setDragEnabled(false);
        add(gc);
        pack();
        setVisible(true);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
	        GraphStruct graph = new GraphStruct();
	        HashMap<String, Integer> hashDomainNum = new HashMap<String,Integer>();
	        hashDomainNum.put("EC", 1);
	        hashDomainNum.put("Cyt", 1);
	        hashDomainNum.put("Nuc", 1);
	        HashMap<String, Integer> hashSampledValue = new HashMap<String,Integer>();
	        hashSampledValue.put("EC", 0);
	        hashSampledValue.put("Cyt", 85);
	        hashSampledValue.put("Nuc", 170);
	        ArrayList<Integer> labelList = new ArrayList<Integer>();
	        labelList.add(0);
	        labelList.add(85);
	        labelList.add(170);
	        ArrayList<ArrayList<Integer>> adjacentsList = new ArrayList<ArrayList<Integer>>();
	        ArrayList<Integer> temp = new ArrayList<Integer>();
	        temp.add(10); temp.add(0);
	        adjacentsList.add(temp);
	        temp = new ArrayList<Integer>();
	        temp.add(20); temp.add(10);
	        adjacentsList.add(temp);
	        
			for (Entry<String, Integer> e : hashDomainNum.entrySet()) {
				for (int i = 0; i < e.getValue(); i++) {
					graph.addVertex(e.getKey() + i);
				}
			}
			
			for(ArrayList<Integer> a : adjacentsList){
				System.out.println(a.get(0));
				String edge1 = new String();
				String edge2 = new String();
				for(Entry<String, Integer> e : hashSampledValue.entrySet()){
					if(e.getValue().equals(labelList.get(a.get(0)/10))){
						edge1 = e.getKey() + (a.get(0) % 10);
					}
					if(e.getValue().equals(labelList.get(a.get(1)/10))){
						edge2 = e.getKey() + (a.get(1) % 10);
					}
				}
				graph.addEdge(edge1,edge2);	
			}
	    	graph.visualize();
	   
	}
	
		
}
