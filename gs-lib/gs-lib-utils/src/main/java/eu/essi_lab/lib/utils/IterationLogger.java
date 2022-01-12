package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.text.DecimalFormat;
public class IterationLogger {

    private String message;
    private Object caller;
    private float iterationsToReach;
    private float iterations;
    private DecimalFormat format;

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
     * @param step the iterations step
     */
    public IterationLogger(Object caller, int itemsCount, int step) {

	this.caller = caller;
	this.iterationsToReach = itemsCount / step;
	this.format = new DecimalFormat();
	this.format.setMaximumFractionDigits(3);

	message = "";
    }

    /**
     * Set a message to show before the current percentage
     * 
     * @see #iterationDone()
     */
    public void setMessage(String message) {

	this.message = message;
    }

    /**
     * Shows the current iteration progress expressed in
     * percentage. This method must be called <i>after each</i> iteration.<br>
     * If a message is set, the percentage is preceded by the message
     * 
     * @see #setMessage(String)
     */
    public void iterationDone() {

	double percent = 100f / (iterationsToReach / iterations);

	String formattedPercent = this.format.format(percent);

	GSLoggerFactory.getLogger(caller.getClass()).debug(message + formattedPercent + "%");

	iterations++;

	if (iterations == iterationsToReach) {

	    GSLoggerFactory.getLogger(caller.getClass()).debug(message + "100%");
	}
    }

    public static void main(String[] args) {

	IterationLogger iterationLogger = new IterationLogger(IterationLogger.class, 10000000, 200);
	for (int i = 0; i < 10000000; i += 200) {
	    iterationLogger.iterationDone();
	}
    }
}
