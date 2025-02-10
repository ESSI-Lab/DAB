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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.messages.web.WebRequest;

/**
 * @author ilsanto
 */
public class PerformanceLogger {

    private final PerformancePhase phase;
    private final String rid;
    private final Optional<String> qs;
    private final Chronometer chronometer;

    public enum PerformancePhase {

	MESSAGE_AUTHORIZATION, //
	RESULT_SET_FORMATTING, //
	RESULT_SET_MAPPING, //

	RESULT_SET_COUNTING,//
	RESULT_SET_RETRIEVING, //

	RESULT_SET_OVERALL_RETRIEVING, //

	REQUEST_HANDLING, //
	BOND_NORMALIZATION, //
	
	
	MARKLOGIC_QUERY_EXECUTION, //
	MARKLOGIC_QUERY_GENERATION, //
	MARKLOGIC_NODES_CREATION, //
	MARKLOGIC_NODES_TO_GS_RESOURCE_MAPPING, //
	MARKLOGIC_COUNTQUERY_GENERATION, //
	MARKLOGIC_COUNTQUERY_EXECUTION, //
	
	OPENSEARCH_WRAPPER_SEARCH, //
	OPENSEARCH_WRAPPER_TO_BINARY_LIST, //
	OPENSEARCH_WRAPPER_TO_NODE_LIST,//
	OPENSEARCH_WRAPPER_TO_STRING_LIST,//
	
	OPENSEARCH_FINDER_COUNT, //
	OPENSEARCH_FINDER_DISCOVERY, //
	OPENSEARCH_FINDER_GET_SOURCES_DATA_DIR_MAP,//
	OPENSEARCH_FINDER_RESOURCES_CREATION, //

	ITERATED_WORKFLOW;

    }

    public PerformanceLogger(PerformancePhase p, String requestIdentifier, Optional<WebRequest> webRequest) {
	chronometer = new Chronometer(Chronometer.TimeFormat.SEC_MLS);
	chronometer.start();
	this.phase = p;
	this.rid = requestIdentifier;
	if (webRequest.isPresent())
	    this.qs = Optional.ofNullable(webRequest.get().getQueryString());
	else
	    this.qs = Optional.empty();

    }

    public void logPerformance(Logger logger) {
	Double elapsedTime = chronometer.getElapsedTimeMillis() / 1000.0;

	String queryString = "N/A";

	if (qs.isPresent()) {
	    queryString = qs.get();
	}

	if (queryString.length() > 100) {

	    queryString = queryString.substring(0, 100) + "...";
	}

	logger.info("Performance [{}] [{}] [{}] [{}] [{}]", phase, elapsedTime, "secs", rid, queryString);
    }

    public static void main(String[] args) {

	PerformanceLogger pl = new PerformanceLogger(PerformancePhase.RESULT_SET_FORMATTING, "id", Optional.empty());

	try {
	    Thread.sleep(1000L);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	pl.logPerformance(LoggerFactory.getLogger(PerformanceLogger.class));
	pl = new PerformanceLogger(PerformancePhase.MESSAGE_AUTHORIZATION, "id", Optional.empty());

	try {
	    Thread.sleep(1500L);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	pl.logPerformance(LoggerFactory.getLogger(PerformanceLogger.class));
    }
}
