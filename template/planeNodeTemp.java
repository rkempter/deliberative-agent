package template;

/* import table */
import java.util.ArrayList;

import template.DeliberativeTemplate.Algorithm;
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
	public boolean computeHash(ArrayList<ArrayList<Object>> state, double newCost){
		int hashCode = state.hashCode();
		boolean present = false;
		boolean shouldAddNode = true;

		for(int i=0; i< hashTable.size(); i++){
			if(hashTable.get(i).get(0).equals(hashCode)){
				present = true;
				if (algorithm.equals(Algorithm.ASTAR)){
					shouldAddNode= checkBestWeight(hashTable.get(i).get(1), newCost);
				}
				else if(algorithm.equals(Algorithm.BFS)){
					//System.out.println("Node already present");
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
	public boolean checkBestWeight(Object node, double newCost){
		if(newCost> ((planeNodeTemp)node).costs)
			return false;
		else{
			((planeNodeTemp)node).deleteNodeAndSubtree();
			return true;
		}
	}


	public ArrayList<planeNodeTemp> expandNodes(){

		ArrayList<planeNodeTemp> childNodes = new ArrayList<planeNodeTemp>();
		ArrayList<Integer> subState = createSubState(nodeState);
		System.out.println("------ new expansion ------");
		System.out.println("actual node: "+ nodeCity);
		System.out.println("Cost so far "+ costs);

		for (int i=0; i< subState.size(); i++){
			planeNodeTemp newState= createNewState( subState.get(i));
			if(newState!= null){
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
			if(currentState.get(i).get(1).equals(INITSTATE)|| currentState.get(i).get(1).equals(PICKEDUP)) {
				subState.add(pos);
			}
		}
		return subState;
	}

	private planeNodeTemp createNewState(Integer selectedTaskIndex) {
		ArrayList<ArrayList<Object>> newState= new ArrayList<ArrayList<Object>>();
		for(int i=0; i<nodeState.size(); i++){
			newState.add(new ArrayList<Object>());
			newState.get(i).add(nodeState.get(i).get(0));
			newState.get(i).add(nodeState.get(i).get(1));			
		}

		planeNodeTemp child= null;
		if(nodeState.get(selectedTaskIndex).get(1).equals(PICKEDUP)){			//selected task is PICKEDUP
			double newCost = costs + (nodeCity.distanceTo(((Task)nodeState.get(selectedTaskIndex).get(0)).deliveryCity) * vehicle.costPerKm());
			int newCapacity= deliverTasks(newState, ((Task)nodeState.get(selectedTaskIndex).get(0)).deliveryCity);
			if(computeHash(newState, newCost)){
				child = new planeNodeTemp(vehicle, ((Task)nodeState.get(selectedTaskIndex).get(0)).deliveryCity, newState, newCapacity, newCost, this, hashTable, algorithm.name());
				children.add(child);
			}
		}
		else{			//if task is still in INITSTATE mode
			if(capacity >= ((Task)nodeState.get(selectedTaskIndex).get(0)).weight){
				newState.get(selectedTaskIndex).set(1, PICKEDUP);
				double newCost = costs + (nodeCity.distanceTo(((Task)nodeState.get(selectedTaskIndex).get(0)).pickupCity) * vehicle.costPerKm());
				int newCapacity= capacity- ((Task)nodeState.get(selectedTaskIndex).get(0)).weight;
				if(computeHash(newState, newCost)){
					child = new planeNodeTemp(vehicle, ((Task)nodeState.get(selectedTaskIndex).get(0)).pickupCity, newState, newCapacity, newCost, this, hashTable, algorithm.name());
					children.add(child);
				}

			}
			else{
				//System.out.println("Truck is full can not pick up current task!!");
			}
		}
		return child;
	}

	private int deliverTasks(ArrayList<ArrayList<Object>> nState, City nodeCity) {
		int cap= capacity;
		for(int i = 0; i < nState.size(); i++) {
			Task task = (Task) nState.get(i).get(0);
			Object taskStatus = nState.get(i).get(1);

			if(task.deliveryCity == nodeCity && taskStatus.equals(PICKEDUP)) {
				nState.get(i).set(1, DELIVERED);
				cap-= ((Task)nState.get(i).get(0)).weight;
			}
		}
		return cap;
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
