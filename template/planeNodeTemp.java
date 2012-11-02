package template;

/* import table */
import java.util.ArrayList;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class planeNodeTemp {

	private planeNodeTemp parent;
	private ArrayList<planeNodeTemp> children;
	private ArrayList<ArrayList<Object>> nodeState;
	private int capacity;
	private double costs;
	private Vehicle vehicle;
	private City nodeCity;
	private ArrayList<ArrayList<Object>> hashTable;
	private enum Algorithm { BFS, ASTAR };
	private Algorithm algorithm;

	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;

	public planeNodeTemp(Vehicle _vehicle, City _nodeCity, ArrayList<ArrayList<Object>> _nodeState, int _capacity, double _costs, planeNodeTemp _parent, ArrayList<ArrayList<Object>> _hashTable, String alg){
		algorithm= Algorithm.valueOf(alg.toUpperCase());
		hashTable= _hashTable;
		nodeState = _nodeState;
		vehicle = _vehicle;
		capacity = _capacity;
		costs = _costs;
		parent = _parent;
		nodeCity = _nodeCity;
		children= new ArrayList<planeNodeTemp>();
	}

	public double getCosts() {
		return costs;
	}
	
	public int getCostsPerKm() {
		return vehicle.costPerKm();
	}

	public City getCity() {
		return nodeCity;
	}

	public void printState() {
		System.out.println(nodeState);
	}

	public ArrayList<ArrayList<Object>> getState() {
		return nodeState;
	}
	
	/**
	 * Used for Breath first search: We don't care about the best solution,
	 * Therefore, if we arrive at a state, where the same tasks are delivered and picked up,
	 * we don't expand the node anymore.
	 * 
	 * @param state
	 * @param newCost
	 * @return true / false
	 */
	public boolean computeHash(ArrayList<ArrayList<Object>> state, double newCost){
		int hashCode = state.hashCode();
		boolean present = false;
		boolean shouldAddNode = true;

		for(int i=0; i< hashTable.size(); i++){
			if(hashTable.get(i).get(0).equals(hashCode)){
				present = true;
				if(algorithm.equals(Algorithm.BFS)){
					shouldAddNode = false;
				}
			}
		}
		
		if(present == false) {
			hashTable.add(new ArrayList<Object>());
			hashTable.get(hashTable.size()-1).add(state.hashCode());
			hashTable.get(hashTable.size()-1).add(this);
		}
		
		return shouldAddNode;
	}

	public ArrayList<planeNodeTemp> expandNodes(){
		ArrayList<planeNodeTemp> childNodes = new ArrayList<planeNodeTemp>();
		ArrayList<Integer> subState = createSubState(nodeState);
//		System.out.println("------ new expansion ------");
//		System.out.println("actual node: "+ nodeCity);
//		System.out.println("Cost so far "+ costs);

		for (int i=0; i< subState.size(); i++){
			planeNodeTemp newState = createNewState(subState.get(i));
			if(newState != null){
				childNodes.add(newState);
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

		for(int i = 0; i < currentState.size(); i++) {
			Integer pos = new Integer(i);
			if(currentState.get(i).get(1).equals(INITSTATE) || currentState.get(i).get(1).equals(PICKEDUP)) {
				subState.add(pos);
			}
		}
		
		return subState;
	}

	private planeNodeTemp createNewState(Integer selectedTaskIndex) {
		ArrayList<ArrayList<Object>> newState= new ArrayList<ArrayList<Object>>();
		planeNodeTemp child = null;
		ArrayList<Object> currentTaskNode = nodeState.get(selectedTaskIndex);
		
		for(int i=0; i<nodeState.size(); i++){
			newState.add(new ArrayList<Object>());
			newState.get(i).add(nodeState.get(i).get(0));
			newState.get(i).add(nodeState.get(i).get(1));			
		}

		if(currentTaskNode.get(1).equals(PICKEDUP)) { //selected task is PICKEDUP
			child = calculateNewStateParameters(newState.get(selectedTaskIndex), newState);
		} else if(capacity >= ((Task) currentTaskNode.get(0)).weight) {
			child = calculateNewStateParameters(newState.get(selectedTaskIndex), newState);
		}
		
		return child;
	}
	
	private planeNodeTemp calculateNewStateParameters(ArrayList<Object> currentTaskNode, ArrayList<ArrayList<Object>> newState) {
		planeNodeTemp child = null;
		Task currentTaskNodeTask = (Task) currentTaskNode.get(0);
		double newCost = calculateCost(currentTaskNode, currentTaskNodeTask, currentTaskNode.get(1));
		int newCapacity = calculateCapacity(capacity, currentTaskNodeTask, currentTaskNode.get(1));
		if(computeHash(newState, newCost)){
			child = new planeNodeTemp(vehicle, currentTaskNodeTask.deliveryCity, newState, newCapacity, newCost, this, hashTable, algorithm.name());
			children.add(child);
		}
		
		return child;
	}
	
	private double calculateCost(ArrayList<Object> currentTaskNode, Task currentTaskNodeTask, Object taskState) {
		double newCost = 0;
		if(taskState.equals(PICKEDUP)) {
			deliverTask(currentTaskNode, currentTaskNodeTask.deliveryCity);
			double reward = currentTaskNodeTask.reward;
			newCost = costs + reward - (nodeCity.distanceTo(currentTaskNodeTask.deliveryCity) * vehicle.costPerKm());
		} else if (taskState.equals(INITSTATE)){
			currentTaskNode.set(1, PICKEDUP);
			newCost = costs - (nodeCity.distanceTo(currentTaskNodeTask.pickupCity) * vehicle.costPerKm());
		}
		
		return newCost;
	}
	
	private int calculateCapacity(int capacity, Task currentTaskNodeTask, Object taskState) {
		int newCapacity = 0;
		if(taskState.equals(PICKEDUP)) {
			newCapacity = capacity + currentTaskNodeTask.weight;
		} else {
			newCapacity = capacity - currentTaskNodeTask.weight;
		}
		
		return newCapacity;
	}
	
	private void deliverTask(ArrayList<Object> currentTaskNode, City city) {
		if( ((Task) currentTaskNode.get(0)).deliveryCity == city && currentTaskNode.get(1).equals(PICKEDUP)) {
			currentTaskNode.set(1, DELIVERED);
		}
	}
	
//	private ArrayList<Task> deliverTasks(ArrayList<ArrayList<Object>> nState, City city)
//	{
//		ArrayList<Task> deliveredTasks = new ArrayList<Task>();
//		for(int i = 0; i < nState.size(); i++) {
//			Task task = (Task) nState.get(i).get(0);
//			Object taskStatus = nState.get(i).get(1);
//			if(task.deliveryCity == city && taskStatus.equals(PICKEDUP)) {
//				nState.get(i).set(1, DELIVERED);
//				deliveredTasks.add(task);
//			}
//		}
//		
//		return deliveredTasks;
//	}
	
	private double getReward(ArrayList<Task> deliveredTasks)
	{
		double reward = 0;
		for(int i = 0; i < deliveredTasks.size(); i++) {
			reward += deliveredTasks.get(i).reward;
		}
		
		return reward;
	}

	public int numberDeliveredTasks() {
		int size = nodeState.size();
		int j = 0;
		for(int i = 0; i < size; i++) {
			Object taskStatus = nodeState.get(i).get(1);

			if(taskStatus.equals(DELIVERED)) {
				j++;
			}
		}
		if(nodeState.size() == j) {
			System.out.println("Goal state!");
		}
		return j;
	}
	
	public planeNodeTemp getParent() {
		return parent;
	}
	
	public void deleteNodeAndSubtree(){		//AAA the node must also delete itself (no idea how)
		if(!children.isEmpty()){
			for(int i=0; i<children.size(); i++){
				children.get(i).deleteNodeAndSubtree();
			}
		}
	}
}
