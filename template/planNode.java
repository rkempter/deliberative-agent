package template;

/* import table */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;





public class planNode {
	private planNode parent;
	private ArrayList<ArrayList<Object>> nodeState;
	private int capacity;
	private double costs;
	private City vehiculePos;
	private int totalCapacity;
	private Vehicle vehicle;
	private City parentCity;
	
	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;
	
	public planNode(Vehicle _vehicle, City _parentCity, ArrayList<ArrayList<Object>> _nodeState, int _capacity, int _costs)
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
	
	public double getCosts() {
		return costs;
	}
	
	public ArrayList<ArrayList<Object>> getState() {
		return nodeState;
	}
	
	// Need to shorten this!
	
	public ArrayList<planNode> expandNodesAStar()
	{
		System.out.println("------ new expansion ------");
		System.out.println("parent node: "+parentCity);
		ArrayList<planNode> childNodes = new ArrayList<planNode>();
		
		// Move to neighbours
		List<City> neighbours = vehiculePos.neighbors();
		Iterator<City> iterator = neighbours.iterator();
		
		while(iterator.hasNext()) {
			City neighbour = iterator.next();
			System.out.println("to: "+neighbour);
			// Calculate cost
			int newCost = (int) (neighbour.distanceTo(parentCity) * vehicle.costPerKm());
			// create new child node
			planNode child = new planNode(vehicle, neighbour, nodeState, capacity, newCost);
			childNodes.add(child);
		}
		
		int nbrTasks = 3;
		int nbrSubStates = (int) Math.pow(2,nbrTasks);
		
		// Loop through all substates
		for(int i = 0; i < nbrSubStates; i++) {
			// Loop through the task list and select the right ones
			/**
			 * Create a table in format
			 * task 1 | task 2 | ... | task n
			 * p | p | p | p
			 * p | p | p | -
			 * p | p | - | p
			 * ...
			 */
			ArrayList<ArrayList<Object>> childNodeState = nodeState;
			boolean validState = true;
			System.out.print("\n|");
			for(int j = 0; j < nbrTasks; j++) {
				if(i % j < (int) Math.pow(2, j)) {
					
					Task currentTask = (Task) childNodeState.get(j).get(0);
					if(capacity + currentTask.weight < totalCapacity) {
						capacity += currentTask.weight;
						childNodeState.get(j).set(1, PICKEDUP);
						System.out.print("x |");
					} else {
						validState = false;
						System.out.print("- |");
						break;
					}
				}
			}
			
			if(validState) {
				// Go through all the cities
				iterator = neighbours.iterator();
				
				while(iterator.hasNext()) {
					City neighbour = iterator.next();
					// Calculate cost
					int newCost = (int) (neighbour.distanceTo(parentCity) * vehicle.costPerKm());
					// create new child node
					planNode child = new planNode(vehicle, neighbour, childNodeState, capacity, newCost);
					childNodes.add(child);
				}
			}
		}
		
		return childNodes;
	}
	
}
