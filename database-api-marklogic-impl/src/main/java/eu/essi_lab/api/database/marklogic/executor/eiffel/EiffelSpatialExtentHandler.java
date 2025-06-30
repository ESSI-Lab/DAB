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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;

/**
 * @author Fabrizio
 */
public class EiffelSpatialExtentHandler implements DiscoveryBondHandler {

    private SpatialExtent extent;
    private BondOperator operator;

    /**
     * @return the extent
     */
    public SpatialExtent getExtent() {

	return extent;
    }

    /**
     * @return the operator
     */
    public BondOperator getOperator() {

	return operator;
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
    }

    @Override
    public void separator() {
    }

    @Override
    public void nonLogicalBond(Bond bond) {
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {
    }

    @Override
    public void spatialBond(SpatialBond bond) {

	extent = (SpatialExtent) bond.getPropertyValue();
	operator = bond.getOperator();
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
    }
}
