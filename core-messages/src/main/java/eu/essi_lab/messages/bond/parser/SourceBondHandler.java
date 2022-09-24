package eu.essi_lab.messages.bond.parser;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class SourceBondHandler implements DiscoveryBondHandler {

    List<ResourcePropertyBond> sourceBonds = new ArrayList<>();

    public List<ResourcePropertyBond> getSourceBonds() {
	return sourceBonds;
    }

    List<String> sourceIdentifiers = new ArrayList<>();

    public List<String> getSourceIdentifiers() {
	return sourceIdentifiers;
    }

    public SourceBondHandler(Bond bond) {
	DiscoveryBondParser parser = new DiscoveryBondParser(bond);
	parser.parse(this);
    }

    @Override
    public void separator() {
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

    }

    @Override
    public void spatialBond(SpatialBond b) {

    }

    @Override
    public void startLogicalBond(LogicalBond b) {

    }

    @Override
    public void endLogicalBond(LogicalBond b) {

    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	if (bond.getProperty() == ResourceProperty.SOURCE_ID) {
	    this.sourceIdentifiers.add(bond.getPropertyValue());
	    this.sourceBonds.add(bond);
	}
    }

    @Override
    public void customBond(QueryableBond<String> bond) {

    }

    @Override
    public void viewBond(ViewBond bond) {

    }

    @Override
    public void nonLogicalBond(Bond bond) {
	
    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	// TODO Auto-generated method stub
	
    }

}
