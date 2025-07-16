package eu.essi_lab.pdk;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.resource.MetadataElement;

public class Semantics {

    public static Bond getSemanticBond(String value, String semantics, String ontology) {
	Set<Bond> operands = new HashSet<>();

	HydroOntology ho = null;

	if (semantics != null && !semantics.isEmpty() && ontology != null && !ontology.isEmpty()) {
	    switch (ontology.toLowerCase()) {
	    case "whos":
		ho = new WHOSOntology();
		break;
	    case "his-central":
		ho = new HISCentralOntology();
		break;
	    default:
		break;
	    }
	}
	SimpleValueBond bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.ATTRIBUTE_TITLE, value);
	operands.add(bond);
	if (ho != null) {
	    List<SKOSConcept> concepts = ho.findConcepts(value, true, false);
	    HashSet<String> uris = new HashSet<String>();
	    for (SKOSConcept concept : concepts) {
		uris.add(concept.getURI());
	    }
	    for (String uri : uris) {
		SimpleValueBond b = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.OBSERVED_PROPERTY_URI, uri);
		operands.add(b);
	    }
	}
	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.iterator().next();	    
	default:
	    return BondFactory.createOrBond(operands);
	}
    }

}
