/**
 * 
 */
package eu.essi_lab.api.database.opensearch.query;

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

import org.opensearch.client.opensearch._types.query_dsl.Query;

import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
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

    /**
     * @param wrapper
     * @param message
     * @param map
     */
    public OpenSearchBondHandler(OpenSearchWrapper wrapper, DiscoveryMessage message, HashMap<String, String> map) {

	this.queryBuilder = new OpenSearchQueryBuilder(//
		wrapper, message.getRankingStrategy(), //
		map, //
		message.isDeletedIncluded(), //
		message.isWeightedQueriesIncluded());
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND:
	    queryBuilder.appendBoolMustOpenTag();
	    break;
	case OR:
	    queryBuilder.appendBoolShouldOpenTag();
	    break;
	case NOT:
	    queryBuilder.appendBoolMustNotOpenTag();
	    break;
	}
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {

	switch (bond.getLogicalOperator()) {
	case AND:
	    queryBuilder.appendClosingTag(false);
	    break;
	case OR:
	    queryBuilder.appendClosingTag(true);
	    break;

	case NOT:
	    queryBuilder.appendClosingTag(false);
	    break;
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

	queryBuilder.append(queryBuilder.buildGeoShapeQuery(bond));
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
