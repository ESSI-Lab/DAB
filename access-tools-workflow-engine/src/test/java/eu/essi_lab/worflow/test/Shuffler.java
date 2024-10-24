package eu.essi_lab.worflow.test;

import java.util.ArrayList;
import java.util.Collections;

import eu.essi_lab.workflow.builder.Workblock;
import eu.essi_lab.workflow.builder.WorkflowBuilder;

public class Shuffler {

    private ArrayList<Workblock> list;
    private ArrayList<Integer> random;

    public Shuffler() {

	list = new ArrayList<>();
    }

    public void add(Workblock v) {

	list.add(v);
    }

    public void shuffle(WorkflowBuilder builder) {

	random = new ArrayList<>();
	for (int i = 0; i < list.size(); i++) {
	    random.add(i);
	}
	Collections.shuffle(random);

	for (Integer integer : random) {
	    builder.add(list.get(integer));
	}
    }

    public void printOrder() {

	System.out.println(random);
    }
}
