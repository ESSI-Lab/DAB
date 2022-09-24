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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.OntologyPropertyBond;
import eu.essi_lab.messages.bond.parser.SemanticBondHandler;
import eu.essi_lab.messages.sem.SemanticMessage;

/**
 * @author Fabrizio
 */
public class MarkLogicSemanticBondHandler implements SemanticBondHandler {

    public MarkLogicSemanticBondHandler(SemanticMessage message) {
	//this will be implemented if and when needed, it appears not used

    }

    @Override
    public void ontologyPropertyBond(OntologyPropertyBond bond) {
	//this will be implemented if and when needed, it appears not used
	//	String value = bond.getPropertyValue();

    }

    @Override
    public void startLogicalBond(LogicalBond bond) {
    }

    @Override
    public void separator() {
    }

    @Override
    public void endLogicalBond(LogicalBond bond) {
    }

    @Override
    public void nonLogicalBond(Bond bond) {
	
    }

}
