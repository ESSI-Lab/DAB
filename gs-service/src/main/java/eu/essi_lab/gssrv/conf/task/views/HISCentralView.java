package eu.essi_lab.gssrv.conf.task.views;

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

import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.resource.ResourceProperty;

public class HISCentralView extends View {

    public HISCentralView() {	
	super();
	setLabel("HIS CENTRAL");
	setBond(BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.SOURCE_DEPLOYMENT, "his-central"));
	setCreator("his_central");
	setId("his-central");
    }
    
}
