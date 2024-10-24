/**
 * 
 */
package eu.essi_lab.api.database.marklogic.executor;

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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.api.database.marklogic.search.MarkLogicSearchBuilder.CTSLogicOperator;
import eu.essi_lab.api.database.marklogic.search.def.DefaultMarkLogicSearchBuilder;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SpatialBond;

/**
 * @author Fabrizio
 */
public class CoveringModeBondHandler extends MarkLogicDiscoveryBondHandler {

    private boolean temporalConstraintEnabled;

    /**
     * @param message
     * @param markLogicDB
     * @param temporalConstraintEnabled
     */
    public CoveringModeBondHandler(DiscoveryMessage message, MarkLogicDatabase markLogicDB, boolean temporalConstraintEnabled) {

	super(message, markLogicDB);
	this.temporalConstraintEnabled = temporalConstraintEnabled;
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	if (bond.getOperator() == BondOperator.CONTAINS) {

	    stringBuilder.append(queryBuilder.buildTrueQuery());

	} else {

	    if (temporalConstraintEnabled) {

		String spatialQuery = queryBuilder.buildQuery(bond);
		String tmpQuery = ((DefaultMarkLogicSearchBuilder) queryBuilder).buildLastSixWeeksTemporalQuery();

		stringBuilder.append(queryBuilder.buildCTSLogicQuery(CTSLogicOperator.AND, spatialQuery, tmpQuery));
	    } else {

		super.spatialBond(bond);
	    }
	}
    }
}
