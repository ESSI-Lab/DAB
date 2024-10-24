package eu.essi_lab.accessor.gbif;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
 * @author ilsanto
 */
public class GBIFQueryHandler implements DiscoveryBondHandler {

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    private StringBuilder queryStringBuilder;
    private String startTempExtent;
    private String endTempExtent;
    private boolean unsupported;
    private boolean bboxFound;
    private String textSearch;

    private static final String Q_PARAM = "q";
    private static final String DECIMAL_LAT_PARAM = "decimalLatitude";
    private static final String DECIMAL_LON_PARAM = "decimalLongitude";
    private static final String EQUAL = "=";
    private static final String COMMA = ",";
    private static final String PARAM_AND = "&";
    private static final String YEAR_KEY = "year";
    private static final String MONTH_KEY = "month";

    public GBIFQueryHandler() {

	queryStringBuilder = new StringBuilder("");
	textSearch = "";
    }

    /**
     * @return
     */
    public boolean isUnsupported() {

	return unsupported;
    }

    @Override
    public void viewBond(ViewBond bond) {
	// not supported
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	unsupported = true;
	logger.warn("Unsupported bond {}", bond);
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
	unsupported = true;
	logger.warn("Unsupported bond {}", bond);
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MetadataElement element = bond.getProperty();

	switch (element) {
	case PARENT_IDENTIFIER:
	    unsupported = false;
	    break;
	
	case TITLE:
	case ABSTRACT:
	case KEYWORD:
	case SUBJECT:
	case ANY_TEXT:

	    String keyword = bond.getPropertyValue();

	    if (!textSearch.contains(keyword)) {
		textSearch += keyword + " ";
	    }

	    break;

	case TEMP_EXTENT_BEGIN:

	    startTempExtent = bond.getPropertyValue();

	    break;

	case TEMP_EXTENT_END:

	    endTempExtent = bond.getPropertyValue();

	    break;

	default:

	    unsupported = true;
	    logger.warn("Unsupported bond {}", element);
	}
    }

    @Override
    public void spatialBond(SpatialBond b) {

	if (!bboxFound) {

	    SpatialExtent bbox = (SpatialExtent) b.getPropertyValue();

	    double east = bbox.getEast();

	    double west = bbox.getWest();

	    double north = bbox.getNorth();

	    double south = bbox.getSouth();

	    appendToQuery(createBboxKVP(west, east, south, north));
	}

	bboxFound = true;
    }

    private void appendToQuery(String kvp) {

	queryStringBuilder.append(kvp).append(PARAM_AND);
    }

    private String createBboxKVP(double west, double east, double south, double north) {

	StringBuilder builder = new StringBuilder(DECIMAL_LON_PARAM);

	builder.append(EQUAL).//
		append(west).//
		append(COMMA).//
		append(east).//
		append(PARAM_AND).//
		append(DECIMAL_LAT_PARAM).//
		append(EQUAL).//
		append(south).//
		append(COMMA).//
		append(north);

	return builder.toString();

    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
	// ignoring logical, all in AND
    }

    @Override
    public void separator() {
	// ignoring logical, all in AND
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	// ignoring logical, all in AND
    }

    public String getSearchTerms() {

	if (!textSearch.isEmpty()) {
	    textSearch = textSearch.trim();
	    try {
		textSearch = URLEncoder.encode(textSearch, StandardCharsets.UTF_8.name());
	    } catch (UnsupportedEncodingException e) {
	    }

	    queryStringBuilder.append(Q_PARAM).append(EQUAL).append(textSearch).append(PARAM_AND);
	}

	appendTime();

	return queryStringBuilder.toString();
    }

    private Optional<Date> retrieveDate(String dateString) {

	if (dateString == null)
	    return Optional.empty();

	return ISO8601DateTimeUtils.parseISO8601ToDate(dateString);

    }

    private void appendTime() {

	Optional<Date> startDate = retrieveDate(startTempExtent);
	Optional<Date> endDate = retrieveDate(endTempExtent);

	if (startDate.isPresent() && endDate.isPresent()) {

	    String startMonth = ISO8601DateTimeUtils.getISO8601DateTime(startDate.get()).substring(5, 7).replaceFirst("^0+(?!$)", "");
	    String endMonth = ISO8601DateTimeUtils.getISO8601DateTime(endDate.get()).substring(5, 7).replaceFirst("^0+(?!$)", "");

	    queryStringBuilder.append(MONTH_KEY).append(EQUAL).append(startMonth);

	    if (!startMonth.equals(endMonth)) {
		queryStringBuilder.append(COMMA).append(endMonth);
	    }

	    queryStringBuilder.append(PARAM_AND);

	    String startYear = ISO8601DateTimeUtils.getISO8601DateTime(startDate.get()).substring(0, 4);
	    String endYear = ISO8601DateTimeUtils.getISO8601DateTime(endDate.get()).substring(0, 4);

	    queryStringBuilder.append(YEAR_KEY).append(EQUAL).append(startYear);

	    if (!endYear.equals(startYear)) {
		queryStringBuilder.append(COMMA).append(endYear);
	    }

	    queryStringBuilder.append(PARAM_AND);

	} else if (startDate.isPresent()) {

	    String startYear = ISO8601DateTimeUtils.getISO8601DateTime(startDate.get()).substring(0, 4);
	    String startMonth = ISO8601DateTimeUtils.getISO8601DateTime(startDate.get()).substring(5, 7).replaceFirst("^0+(?!$)", "");

	    queryStringBuilder.append(MONTH_KEY).append(EQUAL).append(startMonth);
	    queryStringBuilder.append(PARAM_AND);
	    queryStringBuilder.append(YEAR_KEY).append(EQUAL).append(startYear);

	    queryStringBuilder.append(PARAM_AND);

	    // queryStringBuilder.append(YEAR_KEY).append(EQUAL).append(ISO8601DateTimeUtils.getISO8601DateTime(startDate.get()).substring(0,
	    // 4))
	    // .append(PARAM_AND);

	} else if (endDate.isPresent()) {

	    String endYear = ISO8601DateTimeUtils.getISO8601DateTime(endDate.get()).substring(0, 4);
	    String endMonth = ISO8601DateTimeUtils.getISO8601DateTime(endDate.get()).substring(5, 7).replaceFirst("^0+(?!$)", "");

	    queryStringBuilder.append(MONTH_KEY).append(EQUAL).append(endMonth);
	    queryStringBuilder.append(PARAM_AND);
	    queryStringBuilder.append(YEAR_KEY).append(EQUAL).append(endYear);

	    queryStringBuilder.append(PARAM_AND);

	    // queryStringBuilder.append(YEAR_KEY).append(EQUAL).append(ISO8601DateTimeUtils.getISO8601DateTime(endDate.get()).substring(0,
	    // 4))
	    // .append(PARAM_AND);

	}
    }

    @Override
    public void nonLogicalBond(Bond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	// TODO Auto-generated method stub
    }

}
