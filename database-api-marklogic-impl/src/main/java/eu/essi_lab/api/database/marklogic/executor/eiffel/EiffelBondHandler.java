/**
 * 
 */
package eu.essi_lab.api.database.marklogic.executor.eiffel;

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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.api.database.marklogic.MarkLogicExecutor;
import eu.essi_lab.api.database.marklogic.search.MarkLogicDiscoveryBondHandler;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * This bond handler includes only search terms and the bonds of the Eiffel view by excluding temporal and spatial
 * constraints (except the one of the Eiffel view) to the query, simulating queries for the Eiffel API which
 * supports only search terms.<br>
 * To use as parameter of the {@link MarkLogicExecutor#retrieveSortedIds} method
 * 
 * @author Fabrizio
 */
public class EiffelBondHandler extends MarkLogicDiscoveryBondHandler {

    private SpatialExtent eiffelViewExtent;
    private BondOperator eiffelViewOperator;

    /**
     * @param message
     * @param db
     * @param eiffelViewExtent
     * @param eiffelViewOperator
     */
    public EiffelBondHandler(DiscoveryMessage message, MarkLogicDatabase db, SpatialExtent eiffelViewExtent,
	    BondOperator eiffelViewOperator) {

	super(message, db);

	this.eiffelViewExtent = eiffelViewExtent;
	this.eiffelViewOperator = eiffelViewOperator;
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	//
	// ignores temporal extent
	//
	if (bond.getProperty() == MetadataElement.TEMP_EXTENT_BEGIN || bond.getProperty() == MetadataElement.TEMP_EXTENT_END) {

	    stringBuilder.append(queryBuilder.buildTrueQuery());

	} else {
	    //
	    // other bonds are included
	    //
	    stringBuilder.append(queryBuilder.buildQuery(bond));
	}
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	BondOperator op = bond.getOperator();
	SpatialExtent ext = (SpatialExtent) bond.getPropertyValue();

	if (op == eiffelViewOperator && ext.equals(eiffelViewExtent)) {

	    //
	    // the eiffel view bond must be included
	    //
	    super.spatialBond(bond);

	} else {

	    //
	    // other spatial extents are ignored
	    //
	    stringBuilder.append(queryBuilder.buildTrueQuery());
	}
    }
}
