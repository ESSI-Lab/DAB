/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Fabrizio
 */
public abstract class ThreadMode {

    /**
     * @return
     */
    public static ThreadMode SINGLE() {

	return new SingleThreadMode();
    }

    /**
     * @return
     */
    public static ThreadMode MULTI() {

	return new MultiThreadMode();
    }

    /**
     * @return
     */
    public static ThreadMode MULTI(ExecutorService executor) {

	return new MultiThreadMode(executor);
    }

    /**
     * @author Fabrizio
     */
    static class SingleThreadMode extends ThreadMode {
    }

    /**
     * @author Fabrizio
     */
    static class MultiThreadMode extends ThreadMode {

	private ExecutorService executor;

	/**
	 * @param executor
	 */
	MultiThreadMode() {

	    this(Executors.newVirtualThreadPerTaskExecutor());
	}

	/**
	 * @param executor
	 */
	MultiThreadMode(ExecutorService executor) {

	    this.executor = executor;
	}

	/**
	 * @return the executor
	 */
	public ExecutorService getExecutor() {

	    return executor;
	}
    }
}
