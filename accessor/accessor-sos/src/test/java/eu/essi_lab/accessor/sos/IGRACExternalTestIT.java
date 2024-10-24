package eu.essi_lab.accessor.sos;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.Chronometer;

public class IGRACExternalTestIT {
    public static void main(String[] args) {
	Chronometer c = new Chronometer();
	c.start();
	// Create a fixed thread pool with 10 threads
	ExecutorService executorService = Executors.newFixedThreadPool(10);

	// List to hold Future objects
	List<Future<String>> futures = new ArrayList<>();

	int tn = 20;
	// Submit 10 tasks
	for (int i = 0; i < tn; i++) {
	    Callable<String> task = new Task(i);
	    Future<String> future = executorService.submit(task);
	    futures.add(future);
	}

	// Wait for all tasks to complete and collect results
	for (Future<String> future : futures) {
	    try {
		// Get the result of the task
		String result = future.get();
		System.out.println("Length: " + result.length());
		if (result.length()!=6505) {
		    System.err.println("ERROR, WRONG RESULT OBTAINED");
		    System.out.println(result);
		}
	    } catch (InterruptedException | ExecutionException e) {
		e.printStackTrace();
	    }
	}

	// Shutdown the executor service
	executorService.shutdown();
	System.out.println("Number of tasks: "+tn);
	System.out.println("Total time: " + c.getElapsedTimeMillis() + " ms");
	System.out.println("Average time: " + c.getElapsedTimeMillis() / (double) tn + " ms");
    }

    // Sample task class
    static class Task implements Callable<String> {
	private final int taskId;

	Task(int taskId) {
	    this.taskId = taskId;
	}

	@Override
	public String call() throws Exception {
	    String url = "https://ggis.un-igrac.org/istsos?service=SOS&version=1.0.0&request=DescribeSensor&procedure=urn:ogc:def:procedure:x-istsos:1.0:whostest123&outputFormat=text/xml;subtype=%22sensorML/1.0.1%22&api-key=724df1163f0a99f81af89f3b446d76281d07d3ac";
	    Downloader d = new Downloader();
	    Chronometer c = new Chronometer();
	    c.start();
	    String s = d.downloadOptionalString(url).get();
	    System.out.println("Task " + taskId + " completed. Elapsed time: " + c.getElapsedTimeMillis() + " ms");
	    return s;
	}
    }
}
