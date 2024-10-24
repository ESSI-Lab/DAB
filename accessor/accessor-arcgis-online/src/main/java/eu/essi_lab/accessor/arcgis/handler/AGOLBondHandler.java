package eu.essi_lab.accessor.arcgis.handler;

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

import java.util.Date;
import java.util.Optional;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
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
 * Based on doc @ https://developers.arcgis.com/rest/users-groups-and-items/search.htm
 *
 * @author ilsanto
 */
/**
 * @author Fabrizio
 */
public class AGOLBondHandler implements DiscoveryBondHandler {

    private boolean bboxFound = false;
    private String queryString;
    private static final String BBOX_PARAM = "bbox=";
    private String bboxPart;

    private static final String ENCODED_SPACE = "%20";
    private static final String UNSUPPORTED_LOGICAL = "UNSUPPORTED_LOGICAL";
    private static final String Q_PARAM = "q=";
    private final QueryParts qparts;
    private final LogicalOperatorsTree opTree;
    private boolean jumpNextSeprator;
    private String startTempExtent;
    private String endTempExtent;
    private boolean isSupported;

    public AGOLBondHandler() {

	queryString = "f=json&";

	qparts = new QueryParts();

	opTree = new LogicalOperatorsTree();

	isSupported = true;
    }

    /**
     * @return
     */
    public boolean isSupported() {
	return isSupported;
    }

    @Override
    public void viewBond(ViewBond bond) {
	handleNotQueryBond();
	isSupported = false;

    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	handleNotQueryBond();
	isSupported = false;

    }

    @Override
    public void customBond(QueryableBond<String> bond) {
	handleNotQueryBond();
	isSupported = false;

    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MetadataElement element = bond.getProperty();

	switch (element) {
	case TITLE:

	    qparts.add(titleQueryClause(bond.getPropertyValue()));
	    break;
	case TEMP_EXTENT_BEGIN:

	    startTempExtent = bond.getPropertyValue();

	    handleNotQueryBond();

	    break;

	case TEMP_EXTENT_END:

	    endTempExtent = bond.getPropertyValue();

	    handleNotQueryBond();

	    break;

	default:

	    handleNotQueryBond();
	    isSupported = false;

	}
    }

    private String titleQueryClause(String value) {

	value = StringUtils.URLEncodeUTF8(value);

	return new StringBuilder("(") //

		.append("title:").append(value) //

		.append(ENCODED_SPACE).append("OR").append(ENCODED_SPACE) //

		.append("tags:").append(value)//

		.append(")").toString();

    }

    @Override
    public void spatialBond(SpatialBond b) {
	SpatialExtent bbox = (SpatialExtent) b.getPropertyValue();

	if (!bboxFound) {

	    double east = bbox.getEast();

	    double west = bbox.getWest();

	    double north = bbox.getNorth();

	    double south = bbox.getSouth();

	    bboxPart = BBOX_PARAM + west + "%2C" + south + "%2C" + east + "%2C" + north;
	}

	bboxFound = true;
	handleNotQueryBond();
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	LogicalBond.LogicalOperator operator = bond.getLogicalOperator();

	switch (operator) {
	case AND:
	case OR:
	    opTree.add(operator.toString());
	    break;

	default:
	    opTree.add(UNSUPPORTED_LOGICAL);
	    isSupported = false;

	}
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {

	opTree.removeLast();

    }

    public void handleNotQueryBond() {

	if (!opTree.isEmpty() && !opTree.getLast().equals(UNSUPPORTED_LOGICAL)) {
	    Optional<String> removed = removeLastQueryPart();

	    if (!removed.isPresent()) {

		jumpNextSeprator = true;
	    }
	}

    }

    public Optional<String> removeLastQueryPart() {

	return qparts.removeLast();

    }

    @Override
    public void separator() {

	String currentOp = opTree.getLast();

	if (!currentOp.equals(UNSUPPORTED_LOGICAL))

	    if (!jumpNextSeprator)

		qparts.add(new StringBuilder(ENCODED_SPACE).append(currentOp).append(ENCODED_SPACE).toString());
	    else {
		jumpNextSeprator = false;
	    }
    }

    public String getQueryString() {

	StringBuilder builder = new StringBuilder(queryString);

	if (bboxFound) {

	    builder.append(bboxPart).append("&");
	}

	String timeClause = null;

	if (startTempExtent != null || endTempExtent != null) {

	    timeClause = createTimeExtentClause(startTempExtent, endTempExtent);
	}

	if (timeClause != null || !qparts.isEmpty()) {

	    builder.append(Q_PARAM);
	}

	if (!qparts.isEmpty()) {

	    qparts.stream().forEach(builder::append);
	}

	if (timeClause != null && qparts.isEmpty()) {

	    builder.append(timeClause);
	}

	if (timeClause != null && !qparts.isEmpty()) {

	    builder.append(ENCODED_SPACE).append("AND").append(ENCODED_SPACE);

	    builder.append(timeClause);
	}

	//
	// at least q or bbox params must be set
	//
	if (builder.toString().endsWith("f=json&")) {

	    builder.append("bbox=-180,-90,180,90");
	}

	return builder.toString();
    }

    /**
     * @param start
     * @param end
     * @return
     */
    private String createTimeExtentClause(String start, String end) {

	String timeStart = null;
	String timeEnd = null;

	if (start != null && end == null) {

	    Date startDate = ISO8601DateTimeUtils.parseISO8601ToDate(start).get();

	    timeStart = String.valueOf(startDate.getTime());
	    timeEnd = String.valueOf(System.currentTimeMillis());
	}

	else if (start == null && end != null) {

	    Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();

	    timeStart = "0";
	    timeEnd = String.valueOf(endDate.getTime());
	}

	else {

	    Date startDate = ISO8601DateTimeUtils.parseISO8601ToDate(start).get();
	    Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();

	    timeStart = String.valueOf(startDate.getTime());
	    timeEnd = String.valueOf(endDate.getTime());
	}

	StringBuilder timeBuilder = new StringBuilder("created:[");

	timeBuilder.append(timeStart).append(ENCODED_SPACE).append("TO").append(ENCODED_SPACE).append(timeEnd).append("]");

	return timeBuilder.toString();
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

}
