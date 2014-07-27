
import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;

public class graph extends JFrame{
   
	ListenableGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
    JGraphXAdapter<String, DefaultEdge>  jgxAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
	JFrame frame = new JFrame();
    
	graph(){
		super();
		frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}
	
	public ListenableGraph<String, DefaultEdge> getDirectedGraph(){
		return g;
	}
	
	public JGraphXAdapter<String, DefaultEdge> getjgxAdapter(){
		return jgxAdapter; 		
	}
	
	public void addVertex(String name){
		g.addVertex(name);
	}
	
	public void addEdge(String v1, String v2){
		g.addEdge(v1, v2);
	}
	
	public void visualize(){
		frame.getContentPane().add(new mxGraphComponent(jgxAdapter));
        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);
         layout.execute(jgxAdapter.getDefaultParent());
        frame.setVisible(true);
	}
	
	public static void main(String[] args){
		
	        graph graph = new graph();
	        ListenableGraph<String, DefaultEdge> g = graph.getDirectedGraph();
	        		
	         for(Integer i = 1 ; i < 7 ; i++){
		    	g.addVertex("v" + i.toString());
	         }
	        g.addEdge("v1", "v2");
	        g.addEdge("v1", "v3");
	        g.addEdge("v3", "v4");
	        g.addEdge("v4", "v5");
	        g.addEdge("v5", "v6");
	        g.addEdge("v4", "v6");
	    	graph.visualize();
	}
	
		
}
