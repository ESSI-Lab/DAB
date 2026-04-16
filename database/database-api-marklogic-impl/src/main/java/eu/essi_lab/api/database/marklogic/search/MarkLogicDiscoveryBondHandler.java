package eu.essi_lab.api.database.marklogic.search;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
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

    protected StringBuilder stringBuilder;
    protected MarkLogicSearchBuilder queryBuilder;

    /**
     * @param message
     * @param markLogicDB
     */
    public MarkLogicDiscoveryBondHandler(DiscoveryMessage message, MarkLogicDatabase markLogicDB) {

	queryBuilder = MarkLogicSearchBuilderFactory.createBuilder(message, markLogicDB);

	stringBuilder = new StringBuilder();
    }

    /**
     * @return
     */
    public String getCTSSearch(boolean estimate) {

	if (stringBuilder.toString().isEmpty()) {

	    return queryBuilder.buildNoConstraintsCTSSearch(estimate);
	}

	return queryBuilder.buildCTSSearch(stringBuilder.toString(), estimate);
    }

    /**
     * @param estimate
     * @return
     */
    public String getCTSSearchQuery(boolean estimate) {

	if (stringBuilder.toString().isEmpty()) {

	    return queryBuilder.buildNoConstraintsCTSSearchQuery(estimate);
	}

	return queryBuilder.buildCTSSearchQuery(stringBuilder.toString(), estimate);
    }

    @Override
    public void customBond(QueryableBond<String> bond) {

	stringBuilder.append(queryBuilder.buildQuery(bond));
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	stringBuilder.append(queryBuilder.buildQuery(bond));
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {

	stringBuilder.append(queryBuilder.buildQuery(bond));
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	stringBuilder.append(queryBuilder.buildQuery(bond));
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	stringBuilder.append(queryBuilder.buildQuery(bond));
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {

	String name = queryBuilder.getCTSLogicQueryName(bond.getLogicalOperator());

	stringBuilder.append(name);
	stringBuilder.append(bond.getLogicalOperator() == LogicalOperator.NOT ? "(\n" : "((\n");
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	switch (bond.getLogicalOperator()) {
	case AND:
	    stringBuilder.append("))");
	    break;
	case OR:
	    stringBuilder.append("))");
	    break;
	case NOT:
	    stringBuilder.append(")");
	    break;
	}
    }

    @Override
    public void separator() {
	stringBuilder.append(", \n");
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

    /**
     * @return
     */
    public String getParsedQuery() {
	return stringBuilder.toString();
    }
}
