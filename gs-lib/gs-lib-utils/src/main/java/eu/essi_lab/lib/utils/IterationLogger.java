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

/**
 * Logs the progress of an iteration by showing a percentage of the overall process
 * 
 * @author Fabrizio
 */
public class IterationLogger {

    private String message;
    private Object caller;
    private float targetIterationsCount;
    private float iterationsCount;

    /**
     * Creates a new instance of <code>IterationLogger</code>
     * 
     * @param caller a reference to the caller (typically <code>this</code>)
     * @param itemsCount total number of items to iterate
     */
    public IterationLogger(Object caller, int itemsCount) {

	this(caller, itemsCount, 1);
    }

    /**
     * Creates a new instance of <code>IterationLogger</code>
     * 
     * @param caller a reference to the caller (typically <code>this</code>)
     * @param itemsCount total number of items to iterate
     * @param step the iterationsCount step
     */
    public IterationLogger(Object caller, int itemsCount, int step) {

	this.caller = caller;
	this.targetIterationsCount = (float) Math.ceil(((double) itemsCount / step));
	this.iterationsCount = 1;

	message = "";
    }
    
    /**
     * Creates a new instance of <code>IterationLogger</code>
     * 
     * @param caller a reference to the caller (typically <code>this</code>)
     * @param iterationsCount
     * @param itemsCount total number of items to iterate
     * @param step the iterationsCount step
     */
    public IterationLogger(Object caller, int iterationsCount, int itemsCount, int step) {

	this.caller = caller;
	this.targetIterationsCount = (float) Math.ceil(((double) itemsCount / step));
	this.iterationsCount = iterationsCount;

	message = "";
    }

    /**
     * Set a message to show before the current percentage
     * 
     * @see #iterationEnded()
     */
    public void setMessage(String message) {

	this.message = message;
    }

    /**
     * 
     */
    public void iterationStarted() {

	GSLoggerFactory.getLogger(caller.getClass()).debug("Iteration [{}/{}] STARTED", (int) iterationsCount, getTargetIterations());
    }

    /**
     * Shows the current iteration progress expressed in
     * percentage. This method must be called <i>after each</i> iteration.<br>
     * If a message is set, the percentage is preceded by the message
     * 
     * @see #setMessage(String)
     */
    public void iterationEnded() {

	if (iterationsCount == targetIterationsCount) {

	    GSLoggerFactory.getLogger(caller.getClass()).debug(message + "100%");

	} else {

	    GSLoggerFactory.getLogger(caller.getClass()).debug(message + getProgress() + "%");
	}

	GSLoggerFactory.getLogger(caller.getClass()).debug("Iteration [{}/{}] ENDED", (int) iterationsCount, getTargetIterations());

	iterationsCount++;
    }

    /**
     * @return
     */
    public int getTargetIterations() {

	return (int) targetIterationsCount;
    }

    /**
     * @return
     */
    public int getIterationsCount() {

	return (int) iterationsCount;
    }

    /**
     * @return
     */
    public String getProgress() {

	double percent = 100f / (targetIterationsCount / iterationsCount);

	return StringUtils.format(percent);
    }

}
