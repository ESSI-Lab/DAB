/**
 * 
 */
package eu.essi_lab.api.database.opensearch.query;

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

import org.locationtech.jts.io.ParseException;
import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchBondHandler implements DiscoveryBondHandler {

    private OpenSearchQueryBuilder queryBuilder;
    private boolean count;

    /**
     * @param wrapper
     * @param message
     * @param map
     * @param count
     */
    public OpenSearchBondHandler(//
	    OpenSearchWrapper wrapper, //
	    DiscoveryMessage message, //
	    HashMap<String, String> map, //
	    boolean count) {

	this.count = count;
	this.queryBuilder = new OpenSearchQueryBuilder(//
		wrapper, message.getRankingStrategy(), //
		map, //
		message.isDeletedIncluded(), //
		message.isWeightedQueriesIncluded());
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND -> queryBuilder.appendBoolMustOpenTag();
	case OR -> queryBuilder.appendBoolShouldOpenTag();
	case NOT -> queryBuilder.appendBoolMustNotOpenTag();
	}
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND -> queryBuilder.appendClosingTag(false);
	case OR -> queryBuilder.appendClosingTag(true);
	case NOT -> queryBuilder.appendClosingTag(false);
	}
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	Query query = queryBuilder.buildResourcePropertyQuery(bond);

	queryBuilder.append(query);
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	Query query = null;

	if (bond.getProperty() == MetadataElement.SUBJECT) {

	    query = queryBuilder.buildSubjectQuery(bond.getPropertyValue(), bond.getOperator());

	} else {

	    query = queryBuilder.buildMetadataElementQuery(//
		    bond.getProperty(), //
		    bond.getOperator(), //
		    bond.getPropertyValue());
	}

	queryBuilder.append(query);
    }

    @Override
    public void separator() {

	queryBuilder.appendSeparator();
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	try {
	    queryBuilder.append(queryBuilder.buildGeoShapeQuery(bond, count));

	} catch (ParseException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unparsable spatial bond: {}", bond.getPropertyValue());
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    /**
     * @param count
     * @return
     */
    public Query getQuery(boolean count) {

	return queryBuilder.build(count);
    }
}
