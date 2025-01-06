package eu.essi_lab.profiler.wis;

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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import eu.essi_lab.messages.web.WebRequest;

/**
 * A WIS request parser
 * 
 * @author boldrini
 */
public class WISRequest {

    public enum TopRequest {

	COLLECTIONS("collections"), //
	CONFORMANCE("conformance"), //
	OPENAPI("openapi"), //
	PROCESSES("processes"); //

	private String name;

	public String getName() {
	    return name;
	}

	TopRequest(String name) {
	    this.name = name;
	}

    }

    public enum CollectionItems {

	MESSAGES("messages"), //
	DISCOVERY_METADATA("discovery-metadata"), //
	OBSERVATIONS("observations"), //
	DATASETS("datasets"), //
	STATIONS("stations"); //

	private String name;

	public String getName() {
	    return name;
	}

	CollectionItems(String name) {
	    this.name = name;
	}

    }

    public enum CollectionOperation {

	GET_DESCRIPTION(1, null), //
	GET_ITEMS(2, "items"), //
	GET_ITEM(3, "items"), //
	GET_QUERYABLES(2, "queryables"); //

	private String name;

	private int parameters;

	public int getParameters() {
	    return parameters;
	}

	public String getName() {
	    return name;
	}

	CollectionOperation(int parameters, String name) {
	    this.parameters = parameters;
	    this.name = name;
	}

    }

    public static final String DATASET_DISCOVERY = "dataset-discovery-";

    private String process = null;

    public String getProcess() {
	return process;
    }

    public boolean isProcessExecution() {
	return processExecution;
    }

    private boolean processExecution = false;

    private TopRequest topRequest = null;

    public TopRequest getTopRequest() {
	return topRequest;
    }

    private CollectionItems collectionItems = null;

    private String topic;

    public String getTopic() {
	return topic;
    }

    /**
     * 
     */
    public CollectionItems getCollectionItem() {
	return collectionItems;
    }

    private CollectionOperation collectionOperation = null;

    /**
     * 
     */
    public CollectionOperation getCollectionOperation() {
	return collectionOperation;
    }

    private String collectionParameter = null;

    public String getCollectionParameter() {
	return collectionParameter;
    }

    public enum Parameter {
	FORMAT("format", "f"), //
	BBOX("bbox"), //
	LIMIT("limit"), //
	OFFSET("offset"), //
	PROPERTIES("properties"), //
	SKIP_GEOMETRY("skipGeometry"), //
	SORT_BY("sortby"), //
	DATE_TIME("datetime"), //
	PHENOMENON_TIME("phenomenonTime"), //
	PUB_TIME("pubtime"), //
	RESULT_TIME("resultTime"), //
	VALUE("value"), //
	NAME("name"), //
	WIGOS_STATION_IDENTIFIER("wigos_station_identifier"),//
	;

	private String[] keys;

	public String[] getKeys() {
	    return keys;
	}

	private Parameter(String... keys) {
	    this.keys = keys;
	}

    }

    private HashMap<Parameter, String> map = new HashMap<>();

    private InputStream body = null;

    public InputStream getBody() {
	return body;
    }

    public String getParameterValue(Parameter parameter) {
	return map.get(parameter);
    }

    public WISRequest(WebRequest request) {

	String path = request.getRequestPath();
	String[] split = path.split("/");
	f: for (int i = 0; i < split.length; i++) {
	    for (TopRequest topRequest : TopRequest.values()) {
		if (topRequest.getName().equalsIgnoreCase(split[i])) {
		    this.topRequest = topRequest;
		    int parameters = split.length - i - 1;
		    if (topRequest.equals(TopRequest.PROCESSES)) {
			this.process = split[i + 1];
			if (parameters == 2) {
			    if (split[i + 2].equalsIgnoreCase("execution")) {
				this.processExecution = true;
			    }
			}
		    }
		    for (CollectionOperation operation : CollectionOperation.values()) {
			if (parameters == operation.getParameters()) {
			    // i collection
			    // i+ 1 discovery-metadata
			    // i+ 2 items
			    // i+ 3 {id}
			    switch (parameters) {
			    case 3:
				this.collectionParameter = split[i + 3];
			    case 2:
				if (split[i + 2].equalsIgnoreCase(operation.getName())) {
				    this.collectionOperation = operation;
				}
				for (CollectionItems item : CollectionItems.values()) {
				    if (item.getName().equals(split[i + 1])) {
					this.collectionItems = item;
					break;
				    }
				}
				topic = split[i + 1];
				if (collectionItems == null) {
				    if (topic.startsWith(DATASET_DISCOVERY)) {
					collectionItems = CollectionItems.DATASETS;
				    } else {
					collectionItems = CollectionItems.OBSERVATIONS;
				    }
				}

				break;
			    case 1:
				this.collectionOperation = operation;
				for (CollectionItems item : CollectionItems.values()) {
				    if (item.getName().equals(split[i + 1])) {
					this.collectionItems = item;
					break;
				    }
				}
				if (collectionItems == null) {
				    if (topic.startsWith(DATASET_DISCOVERY)) {
					collectionItems = CollectionItems.DATASETS;
				    } else {
					collectionItems = CollectionItems.OBSERVATIONS;
				    }
				}
				topic = split[i + 1];
				break;
			    default:
				break;
			    }
			}
		    }
		    break f;
		}
	    }
	}

	if (request.isGetRequest()) {

	    Map<String, String[]> servletMap = request.getServletRequest().getParameterMap();
	    for (String key : servletMap.keySet()) {
		String[] values = servletMap.get(key);
		if (values != null && values.length > 0) {
		    for (Parameter parameter : Parameter.values()) {
			for (String parameterKey : parameter.getKeys()) {
			    if (parameterKey.equalsIgnoreCase(key)) {
				String v = values[0];
				if (v != null) {
				    map.put(parameter, v);
				}
			    }
			}
		    }
		}
	    }

	} else if (request.isPostRequest()) {

	    this.body = request.getBodyStream().clone();

	} else {

	    throw new IllegalArgumentException("Supported HTTP requests: GET or POST");
	}

    }

}
