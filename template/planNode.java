package template;

/* import table */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class planNode {
	private planNode parent;
	private ArrayList<City> nodeState;
	private int capacity;
	private double costs;
	private City vehiculePos;
	private int totalCapacity;
	private Vehicle vehicle;
	private City parentCity;
	
	public planNode(Vehicle _vehicle, City _parentCity, ArrayList<City> _nodeState, int _capacity, int _costs)
	{
		nodeState = _nodeState;
		totalCapacity = vehicle.capacity();
		capacity = _capacity;
		costs = _costs;
		vehicle = _vehicle;
		parentCity = _parentCity;
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
			int newCost = (int) (neighbour.distanceTo(parentCity) * vehicle.costPerKm());
			// create new child node
			planNode child = new planNode(vehicle, neighbour, nodeState, capacity, newCost);
		}
		
		
		// Go through all state combinations
	
		
		return childNodes;
	}

	
	
}
