package eu.essi_lab.api.database.marklogic.search;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
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

/**
 * @author Fabrizio
 */
public class MarkLogicDiscoveryBondHandler implements DiscoveryBondHandler {

    private String query;
    private MarkLogicSearchBuilder queryBuilder;

    public MarkLogicDiscoveryBondHandler(DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	query = "";
	queryBuilder = MarkLogicSearchBuilderFactory.createBuilder(message, markLogicDB);
    }

    /**
     * @return
     */
    public String getCTSSearch(boolean estimate) {

	if (query.isEmpty()) {

	    return queryBuilder.buildNoConstraintsCTSSearch(estimate);
	}

	return queryBuilder.buildCTSSearch(query, estimate);
    }

    /**
     * @param estimate
     * @return
     */
    public String getCTSSearchQuery(boolean estimate) {

	if (query.isEmpty()) {

	    return queryBuilder.buildNoConstraintsCTSSearchQuery(estimate);
	}

	return queryBuilder.buildCTSSearchQuery(query, estimate);
    }

    @Override
    public void customBond(QueryableBond<String> bond) {

	query += queryBuilder.buildQuery(bond);
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	query += queryBuilder.buildQuery(bond);
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {

	query += queryBuilder.buildQuery(bond);
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	query += queryBuilder.buildQuery(bond);
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	query += queryBuilder.buildQuery(bond);
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
	switch (bond.getLogicalOperator()) {
	case AND:
	    query += "cts:and-query((\n";
	    break;
	case OR:
	    query += "cts:or-query((\n";
	    break;
	case NOT:
	    query += "cts:not-query(\n";
	    break;
	}
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	switch (bond.getLogicalOperator()) {
	case AND:
	    query += "))";
	    break;
	case OR:
	    query += "))";
	    break;
	case NOT:
	    query += ")";
	    break;
	}
    }

    @Override
    public void separator() {
	query += ", \n";
    }

    @Override
    public void viewBond(ViewBond bond) {
	throw new RuntimeException(
		"View bonds should not be handled by MarkLogicBondHandler: they should be resolved by upper components.");

    }

    @Override
    public void nonLogicalBond(Bond bond) {
	// TODO Auto-generated method stub

    }

}
