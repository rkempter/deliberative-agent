package template;

/* import table */
import java.util.ArrayList;
import java.util.Collections;
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
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

	}

	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		Plan plan = null;
		planeNodeTemp goalNode;
		planeNodeTemp currentNode;

		Iterator<Task> itr = tasks.iterator();

		System.out.println("Number of tasks: "+ tasks.size());

		while(itr.hasNext()){			
			startState.add(new ArrayList<Object>());
			goalState.add(new ArrayList<Object>());

			Task currentTask = itr.next();
			startState.get(startState.size()-1).add(currentTask);
			startState.get(startState.size()-1).add(INITSTATE);

			goalState.get(goalState.size()-1).add(currentTask);
			goalState.get(goalState.size()-1).add(DELIVERED);
		}

		planeNodeTemp startNode = createStartNode(vehicle, startState, tasks);
		ArrayList<ArrayList<Object>> currentState = startState;
		// Compute the plan with the selected algorithm.
		algorithm = Algorithm.ASTAR;
		switch (algorithm) {
		case ASTAR:
			System.out.println("Debug Astar");
			Comparator<planeNodeTemp> comparator = new planNodeComparator();
			PriorityQueue<planeNodeTemp> nodeQueue = new PriorityQueue<planeNodeTemp> (1000, comparator);
			ArrayList<planeNodeTemp> visitedNodes = new ArrayList<planeNodeTemp>();
			currentNode = startNode;
			int i = 0;
			while(!checkGoalState(currentState)) {
				//				System.out.println("--------");
				ArrayList<planeNodeTemp> childQueue = currentNode.expandNodes();
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
			System.out.println("Iteration: "+i);
			System.out.println("Arrived at Astar goal node");
			goalNode = currentNode;

			// Do backtracking from goalnode and create plan

			plan = backtrackingPlan(goalNode);
			break;
		case BFS:
			System.out.println("Debug BFS");
			currentNode = startNode;
			ArrayList<planeNodeTemp> nodeQueueList = new ArrayList<planeNodeTemp>();
			i = 0;
			while(!checkGoalState(currentState)) {
				//System.out.println("--------");
				ArrayList<planeNodeTemp> Queue = currentNode.expandNodes();
				nodeQueueList.addAll(Queue);
				currentNode = nodeQueueList.remove(0);
				//currentNode.printState();
				currentState = currentNode.getState();
				i++;
				System.out.println("Iteration: "+ i);
			}
			System.out.println("GOAL NODE REACHED!!!!!!!!!!!");
			goalNode = currentNode;

			plan = backtrackingPlan(goalNode);
			break;
		default:
			throw new AssertionError("Should not happen.");
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

	private planeNodeTemp createStartNode(Vehicle vehicle, ArrayList<ArrayList<Object>> startState, TaskSet tasks) {
		ArrayList<ArrayList<Object>> stateHash= new ArrayList<ArrayList<Object>>();
		System.out.println(algorithm.name());
		planeNodeTemp startNode = new planeNodeTemp(vehicle, vehicle.getCurrentCity(), startState, vehicle.capacity(), 0, null, stateHash, "BFS");
		return startNode;
	}

	private Plan backtrackingPlan(planeNodeTemp goalNode) {
		planeNodeTemp currentNode = goalNode;
		ArrayList<planeNodeTemp> path = new ArrayList<planeNodeTemp>();

		while(currentNode != null) {
			path.add(currentNode);
			currentNode = currentNode.getParent();
		}
		Collections.reverse(path);
		Plan optimalPlan = new Plan(path.get(0).getCity());
		for(int i = 1; i < path.size() ; i++) {
			for(City city : path.get(i-1).getCity().pathTo(path.get(i).getCity())){
				optimalPlan.appendMove(city);
			}
			int stateSize = path.get(i).getState().size();
			for(int j=0; j< stateSize; j++) {
				if(!path.get(i).getState().get(j).get(1).equals(path.get(i-1).getState().get(j).get(1))) {
					System.out.println(path.get(i).getState().get(j).get(0));
					Object action = path.get(i).getState().get(j).get(1);			// switch pickup or delivery
					Task currentTask = (Task) path.get(i).getState().get(j).get(0);

					switch((Integer)action) {
					case PICKEDUP:
						optimalPlan.appendPickup(currentTask);
						break;
					case DELIVERED:
						optimalPlan.appendDelivery(currentTask);
						break;
					default:
						System.out.println("_OO_");
					}
				}
			}
		}
		return optimalPlan;
	}
}
