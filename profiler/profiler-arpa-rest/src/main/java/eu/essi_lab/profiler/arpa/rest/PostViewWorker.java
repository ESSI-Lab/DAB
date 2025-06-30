package eu.essi_lab.profiler.arpa.rest;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

public class PostViewWorker extends ViewWorker {
    
    public PostViewWorker(WebRequest request) throws GSException {
	super(request);
    }

    public String post() {
	try {

	    InputStream stream = request.getBodyStream().clone();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(stream, baos);
	    stream.close();
	    baos.close();
	    String str = new String(baos.toString("UTF-8"));
	    JSONObject obj = new JSONObject(str);
	    DatabaseWriter writer = createWriter();
	    View view = new View();
	    view.setCreationTime(new Date());
	    view.setCreator(CREATOR);

	    String viewId = obj.getString("id");
	    view.setId(viewId);

	    String label = obj.getString("label");
	    if (label == null || label.equals("")) {
		label = viewId;
	    }
	    view.setLabel(label);

	    Bond constraints = null;

	    /*
	     * Sources
	     */
	    if (obj.has("sources")) {
		JSONArray sourcesArray = obj.getJSONArray("sources");
		if (sourcesArray != null) {
		    Bond sourcesBond = null;
		    for (int i = 0; i < sourcesArray.length(); i++) {
			String sourceId = sourcesArray.getString(i);
			Bond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);
			sourcesBond = addInOr(sourcesBond, sourceBond);
		    }
		    constraints = addInAnd(constraints, sourcesBond);
		}
	    }

	    /*
	     * Parameters
	     */
	    if (obj.has("parameters")) {
		JSONArray parametersArray = obj.getJSONArray("parameters");
		if (parametersArray != null) {
		    Bond parametersBond = null;
		    for (int i = 0; i < parametersArray.length(); i++) {
			String parameter = parametersArray.getString(i);
			SimpleValueBond parameterBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL,
				MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, parameter);
			parametersBond = addInOr(parametersBond, parameterBond);
		    }
		    constraints = addInAnd(constraints, parametersBond);
		}
	    }

	    /*
	     * Stations
	     */
	    if (obj.has("stations")) {
		JSONArray stationsArray = obj.getJSONArray("stations");
		if (stationsArray != null) {
		    Bond stationsBond = null;
		    for (int i = 0; i < stationsArray.length(); i++) {
			String station = stationsArray.getString(i);
			SimpleValueBond stationBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL,
				MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, station);
			stationsBond = addInOr(stationBond, stationBond);
		    }
		    constraints = addInAnd(constraints, stationsBond);
		}
	    }

	    /*
	     * Where
	     */
	    if (obj.has("where")) {
		JSONObject where = obj.getJSONObject("where");
		if (where != null) {
		    Double south = Double.parseDouble(where.getString("south"));
		    Double west = Double.parseDouble(where.getString("west"));
		    Double north = Double.parseDouble(where.getString("north"));
		    Double east = Double.parseDouble(where.getString("east"));
		    SpatialExtent extent = new SpatialExtent(south, west, north, east);
		    SpatialBond whereBond = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, extent);
		    constraints = addInAnd(constraints, whereBond);
		}
	    }

	    /*
	     * When
	     */
	    if (obj.has("when")) {
		JSONObject when = obj.getJSONObject("when");
		if (when != null) {
		    String begin = when.getString("from");
		    SimpleValueBond beginBond = getBeginBond(begin);
		    constraints = addInAnd(constraints, beginBond);

		    String end = when.getString("to");
		    SimpleValueBond endBond = getBeginBond(end);
		    constraints = addInAnd(constraints, endBond);
		}
	    }

	    view.setBond(constraints);

	    if (viewId == null) {
		return errorMessage("The view id is null");
	    } else if (constraints == null) {
		return errorMessage("No constraints selected for this view");
	    } else {
		Optional<View> dbView = reader.getView(viewId);
		if (dbView.isPresent()) {
		    return errorMessage("View already present: " + viewId);
		} else {
		    writer.store(view);
		    return message("View stored: " + viewId);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    return errorMessage("Unexpected exception adding a new view: " + e.getMessage().replace("\"", "'"));
	}
    }

    public SimpleValueBond getBeginBond(String beginString) {
	try {
	    if (beginString != null && !beginString.equals("")) {
		Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginString);
		if (optionalDate.isPresent()) {
		    // here intersection is chosen
		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.GREATER_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_END, //
			    beginString);//
		    return ret;
		}

	    }

	} catch (Exception e) {
	}

	return null;
    }

    public SimpleValueBond getEndBond(String endString) {
	try {
	    if (endString != null && !endString.equals("")) {
		Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(endString);
		if (optionalDate.isPresent()) {
		    // here intersection is chosen
		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.LESS_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_BEGIN, //
			    endString);//
		    return ret;
		}

	    }

	} catch (Exception e) {
	}

	return null;
    }

    private Bond addInOr(Bond bonds, Bond bond) {
	if (bond == null) {
	    return bonds;
	} else if (bonds == null) {
	    return bond;
	} else if (bonds instanceof LogicalBond) {
	    LogicalBond orBond = (LogicalBond) bonds;
	    if (orBond.getLogicalOperator().equals(LogicalOperator.OR)) {
		orBond.getOperands().add(bond);
		return orBond;
	    }
	}
	LogicalBond ret = BondFactory.createOrBond(bonds, bond);
	return ret;
    }

    private Bond addInAnd(Bond bonds, Bond bond) {
	if (bond == null) {
	    return bonds;
	} else if (bonds == null) {
	    return bond;
	} else if (bonds instanceof LogicalBond) {
	    LogicalBond orBond = (LogicalBond) bonds;
	    if (orBond.getLogicalOperator().equals(LogicalOperator.AND)) {
		orBond.getOperands().add(bond);
		return orBond;
	    }
	}
	LogicalBond ret = BondFactory.createAndBond(bonds, bond);
	return ret;
    }

}
