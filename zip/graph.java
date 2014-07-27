
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class graph {
	    public static void main(String[] args)
	    {
	        UndirectedGraph<String, DefaultEdge> G =
	        new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

	        G.addVertex("v1"); G.addVertex("v2"); G.addVertex("v3");
	        G.addVertex("v4"); G.addVertex("v5"); G.addVertex("v6");
	        G.addEdge("v1", "v2");
	        G.addEdge("v3", "v2");
	        G.addEdge("v3", "v4");
	        G.addEdge("v4", "v5");
	        G.addEdge("v5", "v6");
	        G.addEdge("v6", "v1");
	        G.addEdge("v1", "v4");
	        G.addEdge("v2", "v6");
	        G.addEdge("v3", "v6");
	        System.out.println("頂点");
	        System.out.println("総数 = " + G.vertexSet().size());
	        System.out.print("頂点 = ");
	        for(String v : G.vertexSet()) System.out.print(v + " ");
	        System.out.println(); System.out.println();

	        System.out.println("辺");
	        System.out.println("総数 = " + G.edgeSet().size());
	        System.out.println("辺 = ");
	        for(DefaultEdge e : G.edgeSet()) System.out.println(e);

	    }
}
