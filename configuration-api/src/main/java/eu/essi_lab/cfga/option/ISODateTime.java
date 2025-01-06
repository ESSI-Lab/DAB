package eu.essi_lab.cfga.option;

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

import java.time.LocalDateTime;
import java.util.Date;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class ISODateTime {

    private String value;

    /**
     * 
     */
    public ISODateTime() {

    }

    /**
     * @param value
     */
    public ISODateTime(String value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public ISODateTime(Date date) {

	this.value = ISO8601DateTimeUtils.getISO8601DateTime(date);
	// removes final Z which is not parsable by ava.time.LocalDateTime.parse
	this.value = value.substring(0, value.length() - 1); 
    }

    /**
     * 
     * @param value
     * @return
     */
    public static ISODateTime fromValue(String value){
	
	return new ISODateTime(value);
    }
    
    /**
     * @return
     */
    public String getValue() {

	return value;
    }
    
    /**
     * 
     * @return
     */
    public static LocalDateTime asLocalDateTime(String isoDateTime){
	
	return LocalDateTime.parse(isoDateTime);
    }

    /**
     * @param value
     */
    public void setValue(String value) {

	this.value = value;
    }

    /**
     * @return
     */
    public Date asDate() {

	return ISO8601DateTimeUtils.parseISO8601ToDate(getValue()).get();
    }

    /**
     * 
     */
    public String toString() {

	return getValue();
    }
}
