package eu.essi_lab.profiler.rest.handler.info;

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

import java.util.Arrays;
public enum MessageFormat {

    /**
     * 
     */
    XML("xml"),
    /**
     * 
     */
    JSON("json");

    private String name;

    private MessageFormat(String name) {

	this.name = name;
    }

    public String getFormat() {

	return name;
    }

    /**
     * 
     * @param format
     * @return
     */
    public static MessageFormat fromFormat(String format) throws IllegalArgumentException{

	return Arrays.asList(values()).//
		stream().//
		filter(f -> f.getFormat().equals(format)).//
		findFirst().//
		orElseThrow(() -> new IllegalArgumentException("Unknown format: " + format));

    }
}
