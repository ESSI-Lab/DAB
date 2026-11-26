/**
 *
 */
package eu.essi_lab.authorization.userfinder;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
 * Thrown when a request carries a bearer token issued by the configured Keycloak realm, but the token is not valid (expired, wrong
 * signature, wrong audience, ...). The request must be refused with HTTP 401.
 */
public class InvalidBearerTokenException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     * @param cause
     */
    public InvalidBearerTokenException(String message, Throwable cause) {

	super(message, cause);
    }

    /**
     * @param message
     */
    public InvalidBearerTokenException(String message) {

	super(message);
    }
}
