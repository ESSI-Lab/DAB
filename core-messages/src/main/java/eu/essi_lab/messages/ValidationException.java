package eu.essi_lab.messages;

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
 * @author Fabrizio
 */
public class ValidationException {

    private String code;
    private String locator;
    private String message;

    /**
     * @return
     */
    public String getCode() {
	return code;
    }

    /**
     * @param code
     */
    public void setCode(String code) {
	this.code = code;
    }

    /**
     * @return
     */
    public String getLocator() {
	return locator;
    }

    /**
     * @param locator
     */
    public void setLocator(String locator) {
	this.locator = locator;
    }

    /**
     * @return
     */
    public String getMessage() {
	return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
	this.message = message;
    }

    @Override
    public String toString() {

	return code + ":" + locator + ":" + message;
    }
}
