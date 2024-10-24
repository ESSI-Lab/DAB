package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TaskListExecutorTest {

    private TaskListExecutor<Integer> pt;
    private List<Future<Integer>> futures;

    @Before
    public void init() {

    }

    @Test
    public void testSingleThread() {

	long milliseconds = getExecutionTime(10, 10, 1);
	// can't be faster than 100 - can't fail
	Assert.assertFalse(milliseconds < 100);
	// the following is an estimated max time - if it fails, it's strange
	Assert.assertFalse(milliseconds > 200);
	assertCorrectness();

    }

    @Test
    public void testAThreadPerTask() {

	long milliseconds = getExecutionTime(10, 10, 10);
	// can't be faster than 10 - can't fail
	Assert.assertFalse(milliseconds < 10);
	// the following is an estimated max time - if it fails, it's strange
	// Assert.assertFalse(milliseconds > 50);
	/**
	 * The previos assert gives a false negative test when it run on
	 * Jenkins. I increased expected duration to 500 ms.
	 * 
	 * @author pezzati
	 */
	Assert.assertFalse(milliseconds > 500);

	assertCorrectness();

    }

    /**
     * Checks that the results of the tasks are as expected
     */
    private void assertCorrectness() {
	int sum1 = 0;
	int sum2 = 0;
	for (int i = 0; i < futures.size(); i++) {
	    sum1 += i;
	    Future<Integer> future = futures.get(i);
	    try {
		Integer integer = future.get();
		sum2 += integer;
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
		Assert.fail();
	    }
	}
	Assert.assertEquals(sum1, sum2);

    }

    /**
     * Starts the execution of a tasklist composed of the specified tasks, with
     * the specific duration, using a specified number of threads.
     * 
     * @param numberOfTasks
     * @param taskDuration
     * @param numberOfThreads
     * @return the execution time of this method in milliseconds
     */
    private long getExecutionTime(int numberOfTasks, int taskDuration, int numberOfThreads) {

	long start = System.currentTimeMillis();

	this.pt = new TaskListExecutor<Integer>(numberOfThreads);
	for (int i = 0; i < numberOfTasks; i++) {
	    final Integer ret = i;
	    Callable<Integer> callable = new Callable<Integer>() {

		@Override
		public Integer call() throws Exception {
		    Thread.sleep(taskDuration);
		    return ret;
		}
	    };
	    pt.addTask(callable);
	}
	this.futures = pt.executeAndWait(60);

	long end = System.currentTimeMillis();

	long ret = end - start;
	System.out.println("Execution time: " + ret);
	return ret;

    }

    @Test
    public void testTimeout() throws Exception {
	this.pt = new TaskListExecutor<Integer>(2);
	int numberOfTasks = 4;
	for (int i = 0; i < numberOfTasks; i++) {
	    Callable<Integer> callable = new Callable<Integer>() {

		@Override
		public Integer call() throws Exception {
		    Thread.sleep(10000);
		    return 1;
		}
	    };
	    pt.addTask(callable);
	}
	this.futures = pt.executeAndWait(1);
	for (Future<Integer> future : futures) {
	    try {
		future.get();
		fail();
	    } catch (CancellationException ce) {
		// all will be cancellation exceptions in case of timeouts
	    } catch (Exception e) {
		fail();
	    }
	}
    }

    @Test
    public void testEmptyList() {
	long start = System.currentTimeMillis();
	this.pt = new TaskListExecutor<Integer>(0);
	List<Future<Integer>> futures = pt.executeAndWait(10);
	GSLoggerFactory.getLogger(getClass()).info("Future size: " + futures.size());
	long gap = System.currentTimeMillis() - start;
	System.out.println(gap);
	assertEquals(0, futures.size());
	assertTrue(gap < 5000);
    }

}
