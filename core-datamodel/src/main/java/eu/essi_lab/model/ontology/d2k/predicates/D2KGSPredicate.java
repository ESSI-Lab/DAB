package eu.essi_lab.model.ontology.d2k.predicates;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;

import eu.essi_lab.model.ontology.GSPredicate;
public enum D2KGSPredicate {

    IS_CHILD_OF(new GSPredicateIsChildOf()), //
    IS_DEFINED_BY(new GSPredicateIsDefinedBy()), //
    ADDRESSED_BY(new GSPredicateAddressedBy()), //
    ADDRESSES(new GSPredicateAddresses()), //
    DEFINITION(new GSPredicateDefinition()), //
    LABEL(new GSPredicateLabel()), //
    TYPE(new GSPredicateType()), //
    GENERATES_OUTPUT(new GSPredicateGeneratesOutput()), //
    OUTPUT_OF(new GSPredicateOutputOf()), //
    OUTPUT_OF_SCIENTIFIC_BP(new GSPredicateOutputOfScientificBP());

    private final GSPredicate gsPredicate;

    /**
     * @param predicate
     */
    private D2KGSPredicate(GSPredicate predicate) {

	gsPredicate = predicate;
    }

    /**
     * @param predicate
     * @return
     */
    public static Optional<D2KGSPredicate> fromGSPredicate(GSPredicate predicate) {

	return Arrays.asList(values()).//
		stream().//
		filter(p -> p.getGSPredicate().getLocalName().equals(predicate.getLocalName())).//
		findFirst();
    }

    public GSPredicate getGSPredicate() {

	return gsPredicate;
    }
}
