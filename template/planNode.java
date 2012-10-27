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
	private int totalCapacity;
	private Vehicle vehicle;
	private City nodeCity;
	
	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;
	
	public planNode(Vehicle _vehicle, City _nodeCity, ArrayList<ArrayList<Object>> _nodeState, int _capacity, double _costs, planNode _parent)
	{
		nodeState = checkDelivery(_nodeState);
		vehicle = _vehicle;
		totalCapacity = vehicle.capacity();
		capacity = _capacity;
		costs = _costs;
		parent = _parent;
		nodeCity = _nodeCity;
	}
	
	public double getCosts() {
		return costs;
	}
	
	public String getCity() {
		return nodeCity.toString();
	}
	
	public void printState() {
		System.out.println(nodeState);
	}
	
	public ArrayList<ArrayList<Object>> getState() {
		return nodeState;
	}
	
	// Need to shorten this!
	
	public ArrayList<planNode> expandNodes()
	{
//		System.out.println("------ new expansion ------");
//		System.out.println("actual node: "+nodeCity);
//		System.out.println("Costs; "+costs);
//		System.out.println("-----------Check nodestate!");
//		System.out.println(nodeState.get(0).get(1));
		
		ArrayList<planNode> childNodes = new ArrayList<planNode>();
		ArrayList<Integer> subState = createSubState(nodeState);
		
		int nbrTasks = subState.size();
		int nbrSubStates = (int) Math.pow(2,nbrTasks);
//		System.out.println("Number of tasks: "+nbrTasks);
		
		// Loop through all substates
		for(int i = 0; i < nbrSubStates; i++) {
			int newCapacity = capacity;
			// Loop through the task list and select the right ones
			ArrayList<ArrayList<Object>> childNodeState = new ArrayList<ArrayList<Object>>(nodeState);
			for(int j = 0; j < nbrTasks; j++) {
				if(i % (int) Math.pow(2, j+1) < ((int) Math.pow(2, j+1) / 2)) {
					int pos = subState.get(j);
					Task currentTask = (Task) childNodeState.get(pos).get(0);
					Object taskState = childNodeState.get(pos).get(1);
					
					// Replace the element because of references in arraylist
					ArrayList<Object> taskObject = new ArrayList<Object>();
					if(checkCapacity(newCapacity, currentTask) && taskState.equals(INITSTATE)) {
						newCapacity += currentTask.weight;
						taskObject.add(currentTask);
						taskObject.add(PICKEDUP);
						childNodeState.set(pos, taskObject);
//						System.out.println("pickup");
					} else {
						childNodeState = null;
//						System.out.println("move");
						break;
					}
				}
			}
			
			if(null != childNodeState) {
				ArrayList<planNode> newChildren = expandOverNeighbours(childNodeState, newCapacity);
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
	
	private ArrayList<Integer> createSubState(ArrayList<ArrayList<Object>> currentState) {
		ArrayList<Integer> subState = new ArrayList<Integer>();
		
		int size = currentState.size();
		for(int i = 0; i < size; i++) {
			ArrayList<Object> current = currentState.get(i);
			Task task = (Task) current.get(0);
			Integer pos = new Integer(i);
			if(task.pickupCity == nodeCity && current.get(1).equals(INITSTATE)) {
				subState.add(pos);
			}
		}
		
		return subState;
	}
	
	private ArrayList<planNode> expandOverNeighbours(ArrayList<ArrayList<Object>> newState, int newCapacity) {
		ArrayList<planNode> children = new ArrayList<planNode>();
		
		List<City> neighbours = nodeCity.neighbors();
		Iterator<City> iterator = neighbours.iterator();
		
		while(iterator.hasNext()) {
			City neighbour = iterator.next();
			// Calculate cost
			double newCost = costs + (neighbour.distanceTo(nodeCity) * vehicle.costPerKm());
			// create new child node
			planNode child = new planNode(vehicle, neighbour, newState, newCapacity, newCost, this);
			children.add(child);
		}
		
		return children;
	}
	
	private boolean checkCapacity(int capacity, Task currentTask) {
		return (capacity + currentTask.weight) < totalCapacity;
	}
	
	private ArrayList<ArrayList<Object>> checkDelivery(ArrayList<ArrayList<Object>> currentState) {
//		System.out.println("---- Show status ------");
		int size = currentState.size();
		for(int i = 0; i < size; i++) {
			Task task = (Task) currentState.get(i).get(0);
			Object taskStatus = currentState.get(i).get(1);

			if(task.deliveryCity == nodeCity && taskStatus.equals(PICKEDUP)) {
				currentState.get(i).set(1, DELIVERED);
				System.out.println("Task delivered");
			}
		}
		return currentState;
	}
	
}
