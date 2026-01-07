/**
 * 
 */
package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Fabrizio
 */
public class ParallelTaskHandler<I, O> {

    private ExecutorService service;
    private Function<I, O> executor;
    private List<I> inputList;

    /**
     * @param input
     * @param executor
     */
    public void set(List<I> input, Function<I, O> executor) {

	this.service = Executors.newFixedThreadPool(input.size());
	this.inputList = input;
	this.executor = executor;
    }

    /**
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<O> run() throws InterruptedException, ExecutionException {

	ArrayList<ParallelTaskHandler<I, O>.ExecutorTask> execList = new ArrayList<ExecutorTask>();

	for (int i = 0; i < inputList.size(); i++) {

	    execList.add(new ExecutorTask(i));
	}

	ArrayList<O> outputList = new ArrayList<>();

	List<Future<O>> futures = service.invokeAll(execList);

	for (Future<O> future : futures) {

	    O output = future.get();
	    outputList.add(output);
	}

	service.shutdown();

	return outputList;
    }

    /**
     * @author Fabrizio
     */
    private class ExecutorTask implements Callable<O> {

	private final int taskId;

	/**
	 * @param taskId
	 */
	private ExecutorTask(int taskId) {

	    this.taskId = taskId;
	}

	@Override
	public O call() throws Exception {

	    I input = inputList.get(taskId);
	    return executor.apply(input);
	}
    }

    /**
     * @param args
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {

	ParallelTaskHandler<String, String> exec = new ParallelTaskHandler<String, String>();

	List<String> inputList = Arrays.asList("A-3", "B-5", "C-2", "D-9", "E-4");

	exec.set(inputList, input -> {

	    int sleep = Integer.valueOf(input.substring(2, input.length()));

	    try {
		GSLoggerFactory.getLogger(ParallelTaskHandler.class).info("Sleeping {} seconds...", sleep);
		Thread.sleep(TimeUnit.SECONDS.toMillis(sleep));
	    } catch (InterruptedException e) {
	    }

	    GSLoggerFactory.getLogger(ParallelTaskHandler.class).info("Ready: " + input);

	    return input + "/" + sleep;
	});

	List<String> output = exec.run();

	GSLoggerFactory.getLogger(ParallelTaskHandler.class).info(output.toString());
    }
}
