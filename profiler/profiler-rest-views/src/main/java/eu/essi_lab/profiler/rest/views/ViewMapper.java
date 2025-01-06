package eu.essi_lab.profiler.rest.views;

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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class ViewMapper {

    /**
     * @param view
     * @param message
     * @return
     */
    public static JSONObject mapView(View view, RequestMessage message) {

	//
	// AND logical bond is expected
	//
	LogicalBond bond = (LogicalBond) view.getBond();

	List<Bond> operands = bond.getOperands();

	JSONObject viewObject = new JSONObject();

	//
	// spatial extent
	//
	Optional<Bond> optSpatialOp = operands.stream().filter(op -> op instanceof SpatialBond).findFirst();

	if (optSpatialOp.isPresent()) {

	    SpatialBond spatialBond = (SpatialBond) optSpatialOp.get();

	    SpatialExtent extent = (SpatialExtent) spatialBond.getPropertyValue();

	    JSONObject bboxObj = new JSONObject();

	    bboxObj.put("south", extent.getSouth());
	    bboxObj.put("west", extent.getWest());
	    bboxObj.put("north", extent.getNorth());
	    bboxObj.put("east", extent.getEast());

	    viewObject.put("where", bboxObj);
	}

	JSONObject tempObj = new JSONObject();

	//
	// temp extent begin
	//
	Optional<SimpleValueBond> tempExtentBegin = operands.//
		stream().//
		filter(op -> op instanceof SimpleValueBond).//
		map(b -> (SimpleValueBond) b).//
		filter(b -> b.getProperty() == MetadataElement.TEMP_EXTENT_BEGIN).//
		findFirst();

	if (tempExtentBegin.isPresent()) {
	    tempObj.put("from", tempExtentBegin.get().getPropertyValue());
	}

	//
	// temp extent end
	//
	Optional<SimpleValueBond> tempExtentEnd = operands.//
		stream().//
		filter(op -> op instanceof SimpleValueBond).//
		map(b -> (SimpleValueBond) b).//
		filter(b -> b.getProperty() == MetadataElement.TEMP_EXTENT_END).//
		findFirst();

	if (tempExtentEnd.isPresent()) {
	    tempObj.put("to", tempExtentEnd.get().getPropertyValue());
	}

	if (tempExtentBegin.isPresent() || tempExtentEnd.isPresent()) {
	    viewObject.put("when", tempObj);
	}

	//
	// sources
	//
	List<String> sourceIds = operands.//
		stream().//
		filter(op -> op instanceof ResourcePropertyBond).//
		map(b -> (ResourcePropertyBond) b).//
		filter(b -> b.getProperty() == ResourceProperty.SOURCE_ID).//
		map(s -> s.getPropertyValue().toString()).//
		collect(Collectors.toList());

	if (sourceIds != null && !sourceIds.isEmpty()) {

	    JSONArray jsonArray = new JSONArray();
	    sourceIds.forEach(k -> jsonArray.put(k));

	    viewObject.put("sources", jsonArray);
	}

	//
	// keywords
	//
	List<String> groupedKeywords = operands.//
		stream().//
		filter(op -> op instanceof LogicalBond).//
		map(op -> (LogicalBond) op).//
		filter(lb -> lb.getLogicalOperator() == LogicalOperator.OR).//
		map(lb -> ((SimpleValueBond) lb.getFirstOperand()).getPropertyValue()).//
		collect(Collectors.toList());

	List<String> nonGroupedKeywords = operands.//
		stream().//
		filter(op -> op instanceof LogicalBond).//
		map(op -> (LogicalBond) op).//
		flatMap(lb -> lb.getOperands().stream()).//
		map(b -> ((SimpleValueBond) b).getPropertyValue()).//
		distinct().//
		collect(Collectors.toList());

	List<String> keywords = (groupedKeywords.size() > nonGroupedKeywords.size()) ? groupedKeywords : nonGroupedKeywords;

	if (!keywords.isEmpty()) {

	    JSONArray jsonArray = new JSONArray();
	    keywords.forEach(k -> jsonArray.put(k));

	    viewObject.put("keywords", jsonArray);
	}

	//
	// parent view id (hidden in case it matches with the user base view id)
	//
	Optional<String> parentViewId = operands.//
		stream().//
		filter(op -> op instanceof ViewBond).//
		map(b -> ((ViewBond) b).getViewIdentifier()).//
		findFirst();//

	String userBaseViewId = ViewWorker.getUserRootViewIdentifier(message.getCurrentUser().get());

	if (parentViewId.isPresent() && !userBaseViewId.equals(parentViewId.get())) {

	    viewObject.put("parentView", parentViewId.get());
	}

	//
	//
	//

	viewObject.put("id", view.getId());
	viewObject.put("label", view.getLabel());
	viewObject.put("visible", view.getVisibility() == ViewVisibility.PRIVATE ? false : true);

	return viewObject;
    }

    /**
     * @param message
     * @param jsonView
     * @return
     * @throws RuntimeException, GSException
     */
    public static View mapView(JSONObject jsonView, RequestMessage message, DatabaseReader reader) throws RuntimeException, GSException {

	//
	//
	//

	View view = new View();

	view.setCreationTime(new Date());
	view.setCreator(ViewWorker.getUserRootViewIdentifier(message.getCurrentUser().get()));

	//
	// view id
	//

	String viewId = jsonView.getString("id");
	view.setId(viewId);

	//
	// view label
	//

	String label = jsonView.getString("label");
	view.setLabel(label);

	Bond constraints = null;

	//
	// view visibility
	//

	view.setVisibility(jsonView.getBoolean("visible") == true ? ViewVisibility.PUBLIC : ViewVisibility.PRIVATE);

	//
	// sources
	//
	if (jsonView.has("sources")) {

	    JSONArray sourcesArray = jsonView.getJSONArray("sources");

	    if (sourcesArray != null) {

		constraints = BondFactory.createAndBond();

		for (int i = 0; i < sourcesArray.length(); i++) {

		    String sourceId = sourcesArray.getString(i);
		    Bond sourceBond = BondFactory.createSourceIdentifierBond(sourceId);

		    ((LogicalBond) constraints).getOperands().add(sourceBond);
		}
	    }
	}

	//
	// keywords
	//
	if (jsonView.has("keywords")) {

	    JSONArray keywordsArray = jsonView.getJSONArray("keywords");

	    if (keywordsArray != null) {

		for (int i = 0; i < keywordsArray.length(); i++) {

		    LogicalBond keywordsBond = BondFactory.createOrBond();

		    String keyword = keywordsArray.getString(i);

		    Bond keywordBond = BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.KEYWORD, keyword);
		    Bond titleBond = BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, keyword);
		    Bond abstractBond = BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.ABSTRACT, keyword);

		    keywordsBond.getOperands().add(keywordBond);
		    keywordsBond.getOperands().add(titleBond);
		    keywordsBond.getOperands().add(abstractBond);

		    constraints = addInAnd(constraints, keywordsBond);
		}
	    }
	}

	//
	// spatial extent
	//
	if (jsonView.has("where")) {

	    JSONObject where = jsonView.getJSONObject("where");

	    if (where != null) {

		Double south = where.getDouble("south");
		Double west = where.getDouble("west");
		Double north = where.getDouble("north");
		Double east = where.getDouble("east");

		SpatialExtent extent = new SpatialExtent(south, west, north, east);
		SpatialBond whereBond = BondFactory.createSpatialExtentBond(BondOperator.CONTAINS, extent);

		constraints = addInAnd(constraints, whereBond);
	    }
	}

	//
	// temporal extent
	//
	if (jsonView.has("when")) {

	    JSONObject when = jsonView.getJSONObject("when");

	    if (when != null) {

		String begin = when.getString("from");
		SimpleValueBond beginBond = getBeginBond(begin);

		constraints = addInAnd(constraints, beginBond);

		String end = when.getString("to");
		SimpleValueBond endBond = getEndBond(end);

		constraints = addInAnd(constraints, endBond);
	    }
	}

	//
	// parent view
	//
	if (constraints != null) {

	    // if the parent view is not explicitly set, it corresponds to the user root view
	    String parentView = ViewWorker.getUserRootViewIdentifier(message.getCurrentUser().get());

	    if (jsonView.has("parentView")) {

		parentView = jsonView.getString("parentView");
	    }

	    ViewBond viewBond = new ViewBond();
	    viewBond.setViewIdentifier(parentView);

	    constraints = addInAnd(constraints, viewBond);
	}

	view.setBond(constraints);

	//
	// owner
	//
	view.setOwner(message.getCurrentUser().get().getIdentifier());

	if (constraints == null) {

	    throw new RuntimeException("No constraints selected for this view");

	} else {

	    Optional<View> dbView = reader.getView(viewId);

	    if (dbView.isPresent()) {

		throw new RuntimeException("A view with identifier [" + viewId + "] is already present");
	    }
	}

	return view;
    }

    /**
     * @param beginString
     * @return
     */
    private static SimpleValueBond getBeginBond(String beginString) {
	try {
	    if (beginString != null && !beginString.equals("")) {
		Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginString);
		if (optionalDate.isPresent()) {

		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.GREATER_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_BEGIN, //
			    beginString);//
		    return ret;
		}
	    }

	} catch (Exception e) {
	}

	return null;
    }

    /**
     * @param endString
     * @return
     */
    private static SimpleValueBond getEndBond(String endString) {
	try {
	    if (endString != null && !endString.equals("")) {
		Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(endString);
		if (optionalDate.isPresent()) {

		    SimpleValueBond ret = BondFactory.createSimpleValueBond(//
			    BondOperator.LESS_OR_EQUAL, //
			    MetadataElement.TEMP_EXTENT_END, //
			    endString);//
		    return ret;
		}
	    }

	} catch (Exception e) {
	}

	return null;
    }

    /**
     * @param bonds
     * @param bond
     * @return
     */
    private static Bond addInOr(Bond bonds, Bond bond) {
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

    /**
     * @param bonds
     * @param bond
     * @return
     */
    private static Bond addInAnd(Bond bonds, Bond bond) {
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
