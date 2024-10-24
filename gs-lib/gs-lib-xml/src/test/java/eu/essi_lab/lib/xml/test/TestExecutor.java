package eu.essi_lab.lib.xml.test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class TestExecutor {
    public static void main(String[] args) throws Exception {
	
	TaskListExecutor<Boolean> tle = new TaskListExecutor<Boolean>(1);
	tle.addTask(new Callable<Boolean>() {
	    
	    @Override
	    public Boolean call() throws Exception {
		String xml = "<test/>";
		try {
		    XMLDocumentReader reader = new XMLDocumentReader(xml);
		    return true;
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return false;
	    }
	});
	List<Future<Boolean>> ret = tle.executeAndWait(60);
	if (ret.isEmpty()) {
	    
	}else {
	    Boolean result = ret.get(0).get();
	    System.out.println(result);
	}
	
	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	final Runnable actualTask = new Runnable() {
	    int i = 0;
	    @Override
	    public void run() {
		String xml = "<test/>";
		try {
		    XMLDocumentReader reader = new XMLDocumentReader(xml);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		System.out.println("test: " + ++i);
	    }
	};
	executorService.scheduleAtFixedRate(actualTask, 1, 1, TimeUnit.SECONDS);
	Thread.sleep(5000);
	executorService.shutdown();
    }
}
