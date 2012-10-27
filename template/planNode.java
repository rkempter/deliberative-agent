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
	
	public ArrayList<ArrayList<Object>> getState() {
		return nodeState;
	}
	
	// Need to shorten this!
	
	public ArrayList<planNode> expandNodes()
	{
		System.out.println("------ new expansion ------");
		System.out.println("actual node: "+nodeCity);
		System.out.println("Costs; "+costs);
		System.out.println("-----------Check nodestate!");
		System.out.println(nodeState.get(0).get(1));
		
		ArrayList<planNode> childNodes = new ArrayList<planNode>();
		ArrayList<Integer> subState = createSubState(nodeState);
		
		int nbrTasks = subState.size();
		int nbrSubStates = (int) Math.pow(2,nbrTasks);
		System.out.println("Number of tasks: "+nbrTasks);
		
		// Loop through all substates
		for(int i = 0; i < nbrSubStates; i++) {
			System.out.println("substate iteration");
			int newCapacity = capacity;
			// Loop through the task list and select the right ones
			ArrayList<ArrayList<Object>> childNodeState = nodeState;
			for(int j = 0; j < nbrTasks; j++) {
				ArrayList<Object> taskStateObject = new ArrayList<Object>();
				if(i % (int) Math.pow(2, j+1) < ((int) Math.pow(2, j+1) / 2)) {
					Task currentTask = (Task) childNodeState.get(j).get(0);
					Object taskState = childNodeState.get(j).get(1);
					if(checkCapacity(newCapacity, currentTask) && taskState.equals(INITSTATE)) {
						newCapacity += currentTask.weight;
						taskStateObject.add(currentTask);
						taskStateObject.add(PICKEDUP);
						childNodeState.set(j, taskStateObject);
						System.out.println("pickup");
						System.out.print("x |");
					} else {
						childNodeState = null;
						System.out.println("move");
						break;
					}
				}
			}
			
			if(null != childNodeState) {
				ArrayList<planNode> newChildren = expandOverNeighbours(childNodeState, newCapacity);
				childNodes.addAll(newChildren);
			}
		}
		
		System.out.println("-----------Check nodestate!");
		System.out.println(nodeState.get(0).get(1));
		
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
			System.out.println("Create node");
			City neighbour = iterator.next();
			System.out.println("to: "+neighbour);
			// Calculate cost
			System.out.println("Distance to "+nodeCity+": "+neighbour.distanceTo(nodeCity)+" costs per km: "+vehicle.costPerKm()+" makes "+(neighbour.distanceTo(nodeCity) * vehicle.costPerKm()));
			double newCost = costs + (neighbour.distanceTo(nodeCity) * vehicle.costPerKm());
			System.out.println("New costs: "+newCost);
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
		System.out.println("---- Show status ------");
		int size = currentState.size();
		for(int i = 0; i < size; i++) {
			Task task = (Task) currentState.get(i).get(0);
			Object taskStatus = currentState.get(i).get(1);
			System.out.println(taskStatus);
			if(task.deliveryCity == nodeCity && taskStatus.equals(PICKEDUP)) {
				currentState.get(i).set(1, DELIVERED);
				System.out.println("Task delivered");
			}
		}
		return currentState;
	}
	
}
