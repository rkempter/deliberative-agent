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
		
		ArrayList<planNode> newChildren = expandOverNeighbours(nodeState, capacity);
		childNodes.addAll(newChildren);
		
		// How many tasks in this city?
		ArrayList<Integer> subState = createSubState(nodeState);
		int nbrTasks = subState.size();
		int nbrSubStates = (int) Math.pow(2,nbrTasks);
		
		// Loop through all substates
		for(int i = 0; i < nbrSubStates; i++) {
			int newCapacity = capacity;
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
					if(newCapacity + currentTask.weight < totalCapacity) {
						newCapacity += currentTask.weight;
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
				newChildren = expandOverNeighbours(childNodeState, newCapacity);
				childNodes.addAll(newChildren);
			}
		}
		
		return childNodes;
	}
	
	/**
	 * All tasks, that are on the current city (vehicle position)
	 * and haven't been moved yet, are extracted from the currentState
	 * into a subState. Based on the substate, we calculate the possible
	 * next global states
	 * 
	 * @param currentState
	 * @return subState
	 */
	
	public ArrayList<Integer> createSubState(ArrayList<ArrayList<Object>> currentState) {
		ArrayList<Integer> subState = new ArrayList<Integer>();
		
		int size = currentState.size();
		for(int i = 0; i < size; i++) {
			ArrayList<Object> current = currentState.get(i);
			Task task = (Task) current.get(0);
			Integer pos = new Integer(i);
			if(task.pickupCity == parentCity && current.get(1).equals(INITSTATE)) {
				subState.add(pos);
			}
		}
		
		return subState;
	}
	
	public ArrayList<planNode> expandOverNeighbours(ArrayList<ArrayList<Object>> newState, int newCapacity) {
		ArrayList<planNode> children = new ArrayList<planNode>();
		
		List<City> neighbours = vehiculePos.neighbors();
		Iterator<City> iterator = neighbours.iterator();
		
		while(iterator.hasNext()) {
			City neighbour = iterator.next();
			System.out.println("to: "+neighbour);
			// Calculate cost
			int newCost = (int) (neighbour.distanceTo(parentCity) * vehicle.costPerKm());
			// create new child node
			planNode child = new planNode(vehicle, neighbour, newState, newCapacity, newCost);
			children.add(child);
		}
		
		return children;
	}
	
}
