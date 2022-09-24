/**
 * 
 */
package eu.essi_lab.model.ontology.d2k;

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

import java.util.Arrays;

import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.ontology.OntologyURIs;
import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;

/**
 * @author Fabrizio
 */
public class D2KGSKnowledgeScheme extends GSKnowledgeScheme {

    private static final D2KGSKnowledgeScheme INSTANCE = new D2KGSKnowledgeScheme();

    /**
     * @return
     */
    public static D2KGSKnowledgeScheme getInstance() {

	return INSTANCE;
    }

    /**
     * 
     */
    public D2KGSKnowledgeScheme() {

	setNameSpace(OntologyURIs.ESSI_D2K_NAMESPACE);

	setCollapsePredicates(Arrays.asList(//
		D2KGSPredicate.OUTPUT_OF.getGSPredicate(), //
		D2KGSPredicate.ADDRESSED_BY.getGSPredicate()));

	setExpandPredicates(Arrays.asList(//
		D2KGSPredicate.GENERATES_OUTPUT.getGSPredicate(), //
		D2KGSPredicate.IS_CHILD_OF.getGSPredicate(), //
		D2KGSPredicate.ADDRESSES.getGSPredicate()));

	setLabelPredicate(D2KGSPredicate.LABEL.getGSPredicate());
	setAbstractPredicate(D2KGSPredicate.DEFINITION.getGSPredicate());
    }
}
