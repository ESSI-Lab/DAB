package eu.essi_lab.worflow.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import eu.essi_lab.workflow.builder.Workblock;

public class ParallelCartesianProductTest {

    private static class BlocksCouple {
	public BlocksCouple(Workblock v1, Workblock v2) {

	    v2.addPredecessor(v1);
	    v1.addSuccessor(v2);
	}
    }

    public static void main(String[] args) {

	{
	    ArrayList<Workblock> blockList = new ArrayList<>();

	    blockList.stream().//

		    flatMap(v1 -> blockList.parallelStream().//

			    filter(v2 -> !v1.equals(v2) && //
				    v2.getInput().accept(v1.getOutput()) && //
				    !v1.getInput().accept(v2.getOutput()) && //
				    !v1.isTerminal() && //
				    !v2.isInitial()) //

			    .map(v2 -> new BlocksCouple(v1, v2)))//

		    .count();
	}

	{

	    final ArrayList<String> startList = Lists.newArrayList();
	    for (int i = 0; i < 100; i++) {
		startList.add(String.valueOf(i));
	    }

	    int listSize = startList.size() > 10 ? startList.size() / 10 : 1;

	    List<List<String>> partition = Lists.partition(startList, listSize);

	    ArrayList<String> combinations = Lists.newArrayList();

	    ExecutorService pool = Executors.newCachedThreadPool();

	    for (final List<String> list : partition) {

		pool.execute(new Runnable() {

		    @Override
		    public void run() {

			for (String s1 : list) {
			    for (String s2 : startList) {

				synchronized (combinations) {
				    // System.out.println("v1: " + s1 + " v2: " + s2);
				    combinations.add("v1: " + s1 + " v2: " + s2);
				}
			    }
			}
		    }
		});
	    }

	    pool.shutdown();

	    try {
		pool.awaitTermination(1, TimeUnit.HOURS);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    System.out.println(combinations.size());
	    combinations.forEach(s -> System.out.println(s));
	    // System.out.println(combinations);
	}
    }
}
