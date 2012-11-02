package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import logist.task.Task;

public class planNodeComparator implements Comparator<planeNodeTemp>{

	private static final int INITSTATE = 0;
	private static final int PICKEDUP = 1;
	private static final int DELIVERED = 2;
	
	public int compare(planeNodeTemp x, planeNodeTemp y)
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
