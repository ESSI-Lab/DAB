package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A parallel executor of a set of tasks, using the given number of threads at the same time.
 * <p>
 * Methods:
 * <ul>
 * <li><b>addTask</b>: adds a task to the task list. A task can be a {@link Runnable} or a {@link Callable}</li>
 * <li><b>executeAndWait</b>: starts the task list execution and waits for completion</li>
 * <li><b>getRunningTasks</b>: returns the number of running tasks</li>
 * </ul>
 *
 * @author boldrini
 */
public class TaskListExecutor<T> {

    List<Callable<T>> taskList = new ArrayList<>();

    private int threads;

    private CountDownLatch cdl = null;

    public List<Callable<T>> getTaskList() {
	return taskList;
    }

    public int getThreads() {
	return threads;
    }

    public void setThreads(int threads) {
	this.threads = threads;
    }

    /**
     * Integer that indicates the number of simultaneous executing processes.
     *
     * @param threads
     */
    public TaskListExecutor(int threads) {
	if (threads == 0) {
	    threads = 1;
	}
	this.threads = threads;
    }

    /**
     * Returns the number of running tasks
     *
     * @return
     */
    public long getRunningTasks() {
	if (cdl == null) {
	    return -1;
	}
	return cdl.getCount();
    }

    /**
     * Adds a task with a return value to the list of processes to be executed
     *
     * @param task
     */
    public void addTask(Callable<T> task) {
	this.taskList.add(getCountDownTask(task));
    }

    /**
     * Adds a void task to the list of processes to be executed
     *
     * @param task
     */
    public void addTask(Runnable task) {
	this.taskList.add(getCountDownTask(Executors.callable(task, null)));
    }

    private Callable<T> getCountDownTask(Callable<T> task) {
	return new Callable<T>() {
	    @Override
	    public T call() throws Exception {
		T ret = task.call();
		cdl.countDown();
		return ret;
	    }
	};
    }

    /**
     * Starts executing the processes and waits for their completion.
     *
     * @return a list of futures of the given type, containing the result type and, in case, the exception that was
     *         thrown.
     */
    public List<Future<T>> executeAndWait() {

	return executeAndWait(0);
    }

    /**
     * Starts executing the processes and waits for their completion.
     *
     * @param timeout the timeout expressed in seconds
     * @return a list of futures of the given type, containing the result type and, in case, the exception that was
     *         thrown.
     */
    public List<Future<T>> executeAndWait(int timeout) {

	int size = taskList.size();

	ExecutorService executor = Executors.newFixedThreadPool(threads);
	this.cdl = new CountDownLatch(size);
	List<Future<T>> futures = null;

	try {
	    if (timeout == 0) {

		futures = executor.invokeAll(taskList);

	    } else {

		futures = executor.invokeAll(taskList, timeout, TimeUnit.SECONDS);
	    }

	} catch (InterruptedException e1) {

	    GSLoggerFactory.getLogger(getClass()).warn("Interrupted", e1);

	    Thread.currentThread().interrupt();

	} finally {

	    executor.shutdown();
	}

	return futures;
    }
}
