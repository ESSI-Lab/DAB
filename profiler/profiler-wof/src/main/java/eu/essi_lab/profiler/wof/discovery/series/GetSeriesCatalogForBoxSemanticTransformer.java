package eu.essi_lab.profiler.wof.discovery.series;

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

import java.util.ArrayList;
import java.util.List;

import eu.essi_lab.lib.net.utils.whos.HISCentralOntology;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.resource.MetadataElement;

public class GetSeriesCatalogForBoxSemanticTransformer extends GetSeriesCatalogForBoxTransformer {

    public List<SimpleValueBond> getKeywords(String viewCreator, SimpleValueBond simpleValueBond) {
	List<SimpleValueBond> ret = new ArrayList<>();
	HydroOntology ontology = null;
	if (viewCreator != null && viewCreator.toLowerCase().contains("central")) {
	    ontology = new HISCentralOntology();
	} else {
	    ontology = new WHOSOntology();
	}
	List<SKOSConcept> concepts = ontology.findConcepts(simpleValueBond.getPropertyValue(), true, true);
	for (SKOSConcept concept : concepts) {
	    SimpleValueBond svb = new SimpleValueBond();
	    svb.setOperator(BondOperator.EQUAL);
	    svb.setProperty(MetadataElement.OBSERVED_PROPERTY_URI);
	    svb.setPropertyValue(concept.getURI());	    
	    ret.add(svb);   
	}	
	return ret;
    }
}
