package eu.essi_lab.accessor.obis.distributed;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * https://api.obis.org/v3/occurrence?datasetid=852b35d0-f92d-485e-b248-00a95e6ee472&offset=0&limit=1&
 *
 * @author ilsanto
 */
public class OBISGranulesBondHandler implements DiscoveryBondHandler {

    private Logger logger = GSLoggerFactory.getLogger(OBISGranulesBondHandler.class);

    private static final String RESOURCE_ID_KEY = "datasetid";
    private static final String EQUAL = "=";
    private static final String START_KEY = "after";
    private static final String COUNT_KEY = "size";
    private static final String AND = "&";
    private static final String ENCODED_SPACE = "%20";
    private static final String COMMA = ",";
    private static final String START_POLYGON = "POLYGON((";
    private static final String END_POLYGON = "))";
    private static final String GEOM_KEY = "geometry";
    private static final String STARTDATE_KEY = "startdate";
    private static final String ENDDATE_KEY = "enddate";
    private static final String SCIENTIFIC_NAME_KEY = "scientificname";
    private static final String INVALID_DATASET_ID = "INVALID_DATASET_ID";

    private Integer start;
    private Integer count;
    private final String datasetId;
    private String polygon;
    private String startDate;
    private String endDate;
    private Map<String, String> textSearches;
    private boolean valid;

    /**
     * @param datasetId
     */
    public OBISGranulesBondHandler(String datasetId) {

	this.valid = true;
	this.datasetId = datasetId;
	this.textSearches = new HashMap<>();
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MetadataElement element = bond.getProperty();

	switch (element) {

	case TEMP_EXTENT_BEGIN:

	    ISO8601DateTimeUtils.parseISO8601ToDate(bond.getPropertyValue())
		    .ifPresent(d -> startDate = ISO8601DateTimeUtils.getISO8601Date(d));

	    break;

	case TEMP_EXTENT_END:

	    ISO8601DateTimeUtils.parseISO8601ToDate(bond.getPropertyValue())
		    .ifPresent(d -> endDate = ISO8601DateTimeUtils.getISO8601Date(d));

	    break;

	case TITLE:
	case ABSTRACT:
	case KEYWORD:
	case SUBJECT:
	case ANY_TEXT:

	    textSearches.put(element.getName(), bond.getPropertyValue());

	    break;

	case PARENT_IDENTIFIER:
	    break;

	default:

	    logger.warn("Invalid bond: " + element.getName());
	    valid = false;
	}
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	SpatialExtent bbox = (SpatialExtent) bond.getPropertyValue();

	double east = bbox.getEast();

	double west = bbox.getWest();

	double north = bbox.getNorth();

	double south = bbox.getSouth();

	StringBuilder builder = new StringBuilder(START_POLYGON);

	builder.append(west).append(ENCODED_SPACE).append(south).append(COMMA);
	builder.append(west).append(ENCODED_SPACE).append(north).append(COMMA);
	builder.append(east).append(ENCODED_SPACE).append(north).append(COMMA);
	builder.append(east).append(ENCODED_SPACE).append(south).append(COMMA);
	builder.append(west).append(ENCODED_SPACE).append(south);
	builder.append(END_POLYGON);

	/**
	 * west=-5.411 south=53.105 east=-0.489 north=58.203
	 * POLYGON((-5.411%2053.105,-5.411%2058.203,-0.489%2058.203,-0.489%2053.105,-5.411%2053.105))
	 */

	polygon = builder.toString();
    }

    /**
     * @param s
     */
    public void setStart(int s) {

	start = s - 1;
    }

    /**
     * @param c
     */
    public void setCount(int c) {

	count = c;
    }

    public String getQueryString() {

	StringBuilder builder = new StringBuilder(RESOURCE_ID_KEY);

	if (!valid) {

	    builder.append(EQUAL).append(INVALID_DATASET_ID);
	    return builder.toString();
	}

	builder.append(EQUAL).append(datasetId).append(AND);

	if (start != null) {
	    builder.append(START_KEY).append(EQUAL).append(start).append(AND);
	}

	if (count != null) {
	    builder.append(COUNT_KEY).append(EQUAL).append(count).append(AND);
	}

	if (polygon != null) {
	    builder.append(GEOM_KEY).append(EQUAL).append(polygon).append(AND);
	}

	if (startDate != null) {
	    builder.append(STARTDATE_KEY).append(EQUAL).append(startDate).append(AND);
	}

	if (endDate != null) {
	    builder.append(ENDDATE_KEY).append(EQUAL).append(endDate).append(AND);
	}

	String scientificName = getScientificName();

	if (scientificName != null) {
	    builder.append(SCIENTIFIC_NAME_KEY).append(EQUAL).append(scientificName).append(AND);
	}

	return builder.toString();
    }

    private String getScientificName() {

	return readText(MetadataElement.KEYWORD).orElseGet(//
		() -> readText(MetadataElement.TITLE).orElseGet(//
			() -> readText(MetadataElement.SUBJECT).orElseGet(//
				() -> readText(MetadataElement.ABSTRACT).orElseGet(//
					() -> readText(MetadataElement.ANY_TEXT).orElse(null)))));

    }

    private Optional<String> readText(MetadataElement element) {

	return Optional.ofNullable(textSearches.get(element.getName()));

    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
    }

    @Override
    public void separator() {
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
    }

}
