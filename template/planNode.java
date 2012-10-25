package template;

/* import table */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logist.topology.Topology.City;

public class planNode {
	private planNode parent;
	private ArrayList<City> nodeState;
	private int capacity;
	private double costs;
	private City vehiculePos;
	
	public planNode(City parent, ArrayList<City> _nodeState, int _capacity, int _costs)
	{
		nodeState = _nodeState;
		capacity = _capacity;
		costs = _costs;
	}
	
	public City getVehiculePosition() {
		return vehiculePos;
	}
	
	public ArrayList<planNode> expandNodes(planNode parent)
	{
		ArrayList<planNode> childNodes = new ArrayList<planNode>();
		
		// Move to neighbours
		List<City> neighbours = vehiculePos.neighbors();
		Iterator<City> iterator = neighbours.iterator();
		
		while(iterator.hasNext()) {
			City neighbour = iterator.next();
			// Calculate cost
			newCost = ;
			// create new child node
			planNode child = planNode(neighbour, nodeState, capacity, newCost);
		}
		
		
		// Go through all state combinations
	
		
		return childNodes;
	}

	
	
}
