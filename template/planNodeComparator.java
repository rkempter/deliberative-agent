package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import logist.task.Task;

public class planNodeComparator implements Comparator<planeNode>{

	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;
	
	
	/**
	 * Compares two nodes and checks which one has the higher reward.
	 * 
	 * @param planeNode x
	 * @param planeNode y
	 * @return int
	 */
	public int compare(planeNode x, planeNode y)
	{
		double xCost = x.getCosts()+getHeuristicCost(x.getState(), x.getCostsPerKm());
		double yCost = y.getCosts()+getHeuristicCost(y.getState(), y.getCostsPerKm());
		if(xCost > yCost) {
			return -1;
		} else if(xCost < yCost) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Heuristic function that computes an cost estimation until
	 * reaching the goal state. The heuristic function is based on
	 * the distance from pick up to delivery of all remaining tasks.
	 * 
	 * @param taskList
	 * @param costPerKm
	 * @return double
	 */
	
	public double getHeuristicCost(ArrayList<ArrayList<Object>> taskList, int costPerKm) {
		double heuristicCost = 0;
		
		for(int i = 0; i < taskList.size(); i++) {
			Task currentTask = (Task) taskList.get(i).get(0);
			Object taskStatus = taskList.get(i).get(1);
			
			if(taskStatus.equals(INITSTATE)) {
				heuristicCost += currentTask.pathLength() * costPerKm;
			}
		}
		
		return heuristicCost;
	}
}
