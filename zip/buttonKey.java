

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;


public class buttonKey extends AbstractAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2388641835015902698L;

	JTable table;
	
	buttonKey(String key, JTable table){
		putValue(Action.NAME,key);
		this.table = table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String input = (String) getValue(Action.NAME);
		
		if(input == "cancel"){
			System.exit(0);
			
		}else{
			graph graph = new graph();
			 
			for(int i = 0 ; i < table.getRowCount() ; i++){
				graph.addVertex(table.getValueAt(i, 1).toString());
			}
		
			for(int i = 0 ; i < table.getRowCount() - 1; i++){
				graph.addEdge(table.getValueAt(i, 1).toString() ,table.getValueAt(i+1, 1).toString());	
			}
		
			graph.visualize();
		}
		
	}

}
