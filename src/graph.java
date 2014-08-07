

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class graph extends JFrame{
   
	ListenableGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    JGraphXAdapter<String, DefaultEdge>  jgxAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
	JFrame frame = new JFrame();
    
	graph(){
		super();
		frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
	}
	
	public ListenableGraph<String, DefaultEdge> getDirectedGraph(){
		return g;
	}
	
	
	public void addVertex(String name){
		g.addVertex(name);
	}
	
	public void addEdge(String v1, String v2){
		g.addEdge(v1, v2);
	}
	
	public void visualize(){
		
        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);
        layout.setIntraCellSpacing(80);
        layout.setDisableEdgeStyle(false);
        layout.getGraph().setCellsLocked(true);
        layout.run(jgxAdapter.getDefaultParent());
        frame.add(new mxGraphComponent(jgxAdapter),new GridBagConstraints());
        frame.setVisible(true);
	}
	
	public static void main(String[] args){
		
	        graph graph = new graph();
	        		
	         for(Integer i = 1 ; i < 7 ; i++){
		    	graph.addVertex("v" + i.toString());
	         }
	        graph.addEdge("v1", "v2");
	        graph.addEdge("v1", "v3");
	        graph.addEdge("v3", "v4");
	        graph.addEdge("v4", "v5");
	        graph.addEdge("v5", "v6");
	    	graph.visualize();
	}
	
		
}
