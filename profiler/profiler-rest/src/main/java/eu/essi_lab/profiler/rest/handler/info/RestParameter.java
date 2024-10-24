package eu.essi_lab.profiler.rest.handler.info;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public enum RestParameter {
    /**
     * 
     */
    START_INDEX("startIndex"),
    /**
     * 
     */
    MAX_REPORTS("maxReports"),
    /**
     * 
     */
    MAX_RECORDS("maxRecords"),
    /**
     * 
     */
    REPORT_SUBSET("reportSubset"),
    /**
     * 
     */
    IDENTIFIER("id"),
    /**
     * 
     */
    REQUEST_FORMAT("requestFormat"),
    /**
     * 
     */
    RESPONSE_FORMAT("responseFormat"),
    /**
     * 
     */
    INLCUDE_ORIGINAL("includeOriginal"),
    /**
     * 
     */
    RESOURCE_SUBSET("resourceSubset"),
    /**
     * 
     */
    INDEX_NAME("indexName"),
    /**
     * 
     */
    INDEXES_POLICY("indexesPolicy");

    private String name;

    private RestParameter(String name) {

	this.name = name;
    }

    public String getName() {

	return name;
    }

    /**
     * @param value
     * @return
     */
    public static RestParameter fromValue(String value) {

	return Arrays.asList(values()).//
		stream().//
		filter(f -> f.getName().equals(value)).//
		findFirst().//
		orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
    }

}
