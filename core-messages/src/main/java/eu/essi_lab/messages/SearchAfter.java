/**
 * 
 */
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

import java.util.Optional;

/**
 * @author Fabrizio
 */
public class SearchAfter {

    private String stringValue;
    private Long longValue;
    private Double doubleValue;

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(String value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.setStringValue(value);

	return searchAfter;
    }

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(double value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.setDoubleValue(value);

	return searchAfter;
    }

    /**
     * @param value
     * @return
     */
    public static SearchAfter of(long value) {

	SearchAfter searchAfter = new SearchAfter();
	searchAfter.setLongValue(value);

	return searchAfter;
    }

    /**
     * @return the stringValue
     */
    public Optional<String> getStringValue() {
	
	return Optional.ofNullable(stringValue);
    }

    /**
     * @param stringValue
     */
    public void setStringValue(String stringValue) {
	this.stringValue = stringValue;
    }

    /**
     * @return the longValue
     */
    public Optional<Long> getLongValue() {
	return Optional.ofNullable(longValue);
    }

    /**
     * @param longValue
     */
    public void setLongValue(long longValue) {
	this.longValue = longValue;
    }

    /**
     * @return the doubleValue
     */
    public Optional<Double> getDoubleValue() {
	return Optional.ofNullable(doubleValue);
    }

    /**
     * @param doubleValue
     */
    public void setDoubleValue(double doubleValue) {
	this.doubleValue = doubleValue;
    }
}
