/**
 * 
 */
package eu.essi_lab.model.exceptions;

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

/**
 * These kind of exceptions are intended to be thrown during a multi-step process which must continue by skipping the
 * current step
 * 
 * @author Fabrizio
 */
public abstract class SkipProcessStepException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5702513107232163500L;

    /**
     * 
     */
    public SkipProcessStepException() {
    }

    /**
     * @param message
     */
    public SkipProcessStepException(String message) {
	super(message);
    }

    /**
     * @param cause
     */
    public SkipProcessStepException(Throwable cause) {
	super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SkipProcessStepException(String message, Throwable cause) {
	super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SkipProcessStepException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

}
