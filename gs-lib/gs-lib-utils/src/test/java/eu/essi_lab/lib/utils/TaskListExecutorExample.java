package eu.essi_lab.lib.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskListExecutorExample {

    /**
     * The following is an usage example of the {@link TaskListExecutor}, starting ten tasks with 3 threads. Some
     * tasks could throw exceptions. At the end, exceptions and results are gathered and displayed.
     * 
     * @param args
     */
    public static void main(String[] args) {
	TaskListExecutor<Integer> pt = new TaskListExecutor<Integer>(3);
	for (int i = 0; i < 10; i++) {
	    Callable<Integer> mr = new Callable<Integer>() {

		@Override
		public Integer call() throws Exception {
		    int max = 3;
		    int r = (int) (Math.random() * max);
		    Thread.sleep(r * 1000);
		    if (r < max / 2) {
			throw new RuntimeException("runtime exception!");
		    }
		    System.out.println("o.k.");
		    System.out.println("remaining tasks: " + pt.getRunningTasks());
		    return r;

		}
	    };
	    pt.addTask(mr);
	}
	List<Future<Integer>> futures = pt.executeAndWait(60);
	for (Future<Integer> future : futures) {
	    Integer result;
	    try {
		result = future.get();
		System.out.println("Result: " + result);
	    } catch (InterruptedException ie) {
		System.out.println("ie");
	    } catch (ExecutionException ee) {
		Throwable cause = ee.getCause();
		System.out.println("Exception: " + cause.getMessage());
	    }

	}
	// pb.startExecution();
	System.out.println("END");

    }
}
