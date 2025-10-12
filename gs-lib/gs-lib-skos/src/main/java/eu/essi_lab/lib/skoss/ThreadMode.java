/**
 * 
 */
package eu.essi_lab.lib.skoss;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
    public static class SingleThreadMode extends ThreadMode {
    }

    /**
     * @author Fabrizio
     */
    public static class MultiThreadMode extends ThreadMode {

	private ExecutorService executor;

	/**
	 * @param executor
	 */
	MultiThreadMode() {
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

	    return executor == null ? Executors.newVirtualThreadPerTaskExecutor() : executor;
	}
    }
}
