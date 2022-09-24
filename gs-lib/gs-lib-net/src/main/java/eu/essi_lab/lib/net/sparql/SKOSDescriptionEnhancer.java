/**
 * 
 */
package eu.essi_lab.lib.net.sparql;

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

import java.util.Optional;

import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.skos.SKOSBroaderPredicate;
import eu.essi_lab.model.ontology.skos.SKOSDefinitionPredicate;
import eu.essi_lab.model.ontology.skos.SKOSLabelPredicate;
import eu.essi_lab.model.ontology.skos.SKOSNarrowerPredicate;

/**
 * @author Fabrizio
 */
public class SKOSDescriptionEnhancer extends DescriptionEnhancer {

    public SKOSDescriptionEnhancer() {
    }

    /**
     * @param description
     * @param objectValue
     * @param predValue
     * @param objLan
     */
    public void enhanceDescription(//
	    GSKnowledgeResourceDescription description, //
	    String objectValue, //
	    String predValue, //
	    Optional<String> objLan) {

	boolean broader = predValue.equals("http://www.w3.org/2004/02/skos/core#broader");
	boolean narrower = predValue.equals("http://www.w3.org/2004/02/skos/core#narrower");
	boolean label = predValue.equals("http://www.w3.org/2004/02/skos/core#prefLabel");
	boolean abstract_ = predValue.equals("http://www.w3.org/2004/02/skos/core#definition");

	if (broader) {

	    description.add(new SKOSBroaderPredicate(), factory.createIRI(objectValue));

	    // description.add(D2KGSPredicate.GENERATES_OUTPUT, factory.createLiteral(objectValue));
	    // description.add(D2KGSPredicate.IS_CHILD_OF, factory.createLiteral(objectValue));
	    // description.add(D2KGSPredicate.ADDRESSES, factory.createLiteral(objectValue));

	} else if (narrower) {

	    description.add(new SKOSNarrowerPredicate(), factory.createIRI(objectValue));

	    // description.add(D2KGSPredicate.OUTPUT_OF, factory.createLiteral(objectValue));
	    // description.add(D2KGSPredicate.ADDRESSED_BY, factory.createLiteral(objectValue));

	} else if (label) {

	    if (objLan.isPresent()) {

		String lan = objLan.get();

		description.add(new SKOSLabelPredicate(), factory.createLiteral(objectValue, lan.trim()));
		description.add(D2KGSPredicate.LABEL, factory.createLiteral(objectValue, lan.trim()));

	    } else {

		description.add(new SKOSLabelPredicate(), factory.createLiteral(objectValue));
		description.add(D2KGSPredicate.LABEL, factory.createLiteral(objectValue));
	    }

	    if (collectLabels) {
		labels.add(objectValue.trim());
	    }

	} else if (abstract_) {

	    if (objLan.isPresent()) {

		String lan = objLan.get();

		description.add(new SKOSDefinitionPredicate(), factory.createLiteral(objectValue, lan.trim()));
		description.add(D2KGSPredicate.DEFINITION, factory.createLiteral(objectValue, lan.trim()));

	    } else {

		description.add(new SKOSDefinitionPredicate(), factory.createLiteral(objectValue));
		description.add(D2KGSPredicate.DEFINITION, factory.createLiteral(objectValue));
	    }
	}
    }
}
