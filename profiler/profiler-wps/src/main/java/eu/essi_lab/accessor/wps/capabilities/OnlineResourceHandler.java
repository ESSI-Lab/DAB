package eu.essi_lab.accessor.wps.capabilities;

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

public class OnlineResourceHandler implements DiscoveryBondHandler {

    private String onlineId;

    public String getOnlineId() {
	return onlineId;
    }

    public void setOnlineId(String onlineId) {
	this.onlineId = onlineId;
    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void separator() {
	// TODO Auto-generated method stub

    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void viewBond(ViewBond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void customBond(QueryableBond<String> bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {
	if (bond.getProperty().equals(MetadataElement.ONLINE_ID)) {
	    onlineId = bond.getPropertyValue();
	}

    }

    @Override
    public void spatialBond(SpatialBond bond) {
	// TODO Auto-generated method stub

    }

    @Override
    public void nonLogicalBond(Bond bond) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	// TODO Auto-generated method stub
	
    }

}
