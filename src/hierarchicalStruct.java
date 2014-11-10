import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class hierarchicalStruct {
	HashMap<String, Integer> hashDomainTypes;
	HashMap<String, Integer> hashSampledValue;
	HashMap<String, Integer> hashDomainNum;
	ArrayList<ArrayList<Integer>> adjacentsPixel;
	ArrayList<Integer> labelList;
	
	hierarchicalStruct(){
		
	}
	
	hierarchicalStruct(imageEdit edit){
		this.hashDomainTypes = edit.hashDomainTypes;
		this.hashSampledValue = edit.hashSampledValue;
		this.hashDomainNum = edit.hashDomainNum;
		this.adjacentsPixel = edit.getAdjacentsPixel();
		this.labelList = edit.labelList;
		
		System.out.println("hashDomainNum");
		System.out.println(hashDomainNum.toString());
		System.out.println("hashSampledValue");
		System.out.println(hashSampledValue.toString());
		System.out.println("adjacentsPixel");
		System.out.println(adjacentsPixel.toString());
		
		
		
		graph graph = new graph();
		vertex(graph);
		edge(graph);
		graph.visualize();
	}
	
	public void vertex(graph graph){
		for (Entry<String, Integer> e : hashDomainNum.entrySet()) {
			
			for (int i = 0; i < e.getValue(); i++) {
				if(!e.getKey().contains("membrane"))
					graph.addVertex(e.getKey() + i);
			}
		}
	}
	
	public void edge(graph graph){
		for(ArrayList<Integer> a : adjacentsPixel){
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
	}
}
