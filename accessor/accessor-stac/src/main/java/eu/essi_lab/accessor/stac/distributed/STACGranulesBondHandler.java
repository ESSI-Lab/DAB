package eu.essi_lab.accessor.stac.distributed;

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

import java.util.HashMap;
import java.util.Map;

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
 * https://explorer.digitalearth.africa/stac/search?collections=ndvi_climatology_ls&_o=1&limit=10
 * 
 * @author roberto
 */
public class STACGranulesBondHandler implements DiscoveryBondHandler {

    private Logger logger = GSLoggerFactory.getLogger(STACGranulesBondHandler.class);

    private static final String COLLECTION_ID_KEY = "collections";
    private static final String EQUAL = "=";
    private static final String START_KEY = "_o";
    private static final String COUNT_KEY = "limit";
    private static final String AND = "&";
    private static final String ENCODED_SPACE = "%20";
    private static final String COMMA = ",";
    private static final String BBOX = "bbox";
    // private static final String END_POLYGON = "))";
    // private static final String GEOM_KEY = "geometry";
    private static final String DATETIME = "datetime";
    private static final String TIME_SEPARATOR = "/";
    // private static final String ENDDATE_KEY = "enddate";
    private static final String INTERSECT = "intersect";
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
    public STACGranulesBondHandler(String datasetId) {

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

	StringBuilder builder = new StringBuilder();

	builder.append(west).append(COMMA);
	builder.append(south).append(COMMA);
	builder.append(east).append(COMMA);
	builder.append(north);

	/**
	 * bbox=-10.415,36.066,3.779,44.213
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

	StringBuilder builder = new StringBuilder(COLLECTION_ID_KEY);

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
	    builder.append(BBOX).append(EQUAL).append(polygon).append(AND);
	}

	if (startDate != null && endDate != null) {
	    builder.append(DATETIME).append(EQUAL).append(startDate).append(TIME_SEPARATOR).append(endDate).append(AND);
	}

	if (startDate != null && endDate == null) {
	    builder.append(DATETIME).append(EQUAL).append(startDate).append(TIME_SEPARATOR).append("..").append(AND);
	}

	if (startDate == null && endDate != null) {
	    builder.append(DATETIME).append(EQUAL).append("..").append(TIME_SEPARATOR).append(endDate).append(AND);
	}

	return builder.toString();
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
