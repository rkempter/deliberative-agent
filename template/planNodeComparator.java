package template;

import java.util.Comparator;

public class planNodeComparator implements Comparator<planNode>{

	public int compare(planNode x, planNode y)
	{
		if(x.getCosts() < y.getCosts()) {
			return -1;
		} else if(x.getCosts() > y.getCosts()) {
			return 1;
		}
		return 0;
	}
}
