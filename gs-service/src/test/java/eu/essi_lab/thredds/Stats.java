package eu.essi_lab.thredds;

import java.util.ArrayList;
import java.util.List;

public class Stats {
    private Long min = null;
    private Long max = null;
    private List<Long> values = new ArrayList<>();

    public long getMin() {
	return min;
    }

    public void setMin(long min) {
	this.min = min;
    }

    public long getMax() {
	return max;
    }

    public void setMax(long max) {
	this.max = max;
    }

    public long getAverage() {
	long sum = 0;
	for (Long v : values) {
	    sum += v;
	}

	return sum / values.size();
    }

    public void print() {
	System.out.println("Min " + min);
	System.out.println("Max " + max);
	System.out.println("Avg " + getAverage());
	System.out.println();

    }

    public void addValue(long ms) {
	if (min == null || ms < min) {
	    min = ms;
	}
	if (max == null || ms > max) {
	    max = ms;
	}
	values.add(ms);
    }
}
