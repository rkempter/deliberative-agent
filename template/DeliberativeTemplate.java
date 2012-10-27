package template;

/* import table */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
public class DeliberativeTemplate implements DeliberativeBehavior {

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;
	
	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;

	ArrayList<ArrayList<Object>> startState= new ArrayList<ArrayList<Object>>();
	ArrayList<ArrayList<Object>> goalState= new ArrayList<ArrayList<Object>>();
	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;

		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan;
		
		// Externalize this stuff
		
		Iterator<Task> itr = tasks.iterator();
		
		System.out.println("Number of tasks: "+tasks.size());
		
		while(itr.hasNext()){			
			startState.add(new ArrayList<Object>());
			goalState.add(new ArrayList<Object>());

			Task currentTask = itr.next();
			startState.get(startState.size()-1).add(currentTask);
			startState.get(startState.size()-1).add(INITSTATE);

			goalState.get(goalState.size()-1).add(currentTask);
			goalState.get(goalState.size()-1).add(DELIVERED);
		}
		
		planNode startNode = createStartNode(vehicle, startState);
		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			System.out.println("Debug Astar");
			Comparator<planNode> comparator = new planNodeComparator();
			PriorityQueue<planNode> nodeQueue = new PriorityQueue<planNode> (1000, comparator);
			ArrayList<ArrayList<Object>> currentState = startState;
			ArrayList<planNode> visitedNodes = new ArrayList<planNode>();
			planNode currentNode = startNode;
			int i = 0;
			while(!checkGoalState(currentState)) {
//				System.out.println("--------");
				ArrayList<planNode> childQueue = currentNode.expandNodes();
				nodeQueue.addAll(childQueue);
				currentNode = nodeQueue.remove();
//				System.out.println("best node cost: "+currentNode.getCosts());
//				System.out.println("best node city: "+currentNode.getCity());
				//currentNode.printState();
				visitedNodes.add(currentNode);
//				System.out.println(currentNode.numberDeliveredTasks());
				currentState = currentNode.getState();
				i++;
//				System.out.println("Node created: "+i);
			}
			System.out.println("Arrived at goal node");
			planNode goalNode = currentNode;
			
			// Do backtracking from goalnode and create plan
			
			plan = naivePlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = naivePlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
	
	public boolean checkGoalState(ArrayList<ArrayList<Object>> state) {
		int stateSize = state.size();
		for(int i = 0; i < stateSize; i++) {
			if(!state.get(i).get(1).equals(DELIVERED)) {
				return false;
			}
		}
		
		return true;
	}
	
	private planNode createStartNode(Vehicle vehicle, ArrayList<ArrayList<Object>> startState) {
		planNode startNode = new planNode(vehicle, vehicle.getCurrentCity(), startState, 0, 0, null);
		return startNode;
	}
}
