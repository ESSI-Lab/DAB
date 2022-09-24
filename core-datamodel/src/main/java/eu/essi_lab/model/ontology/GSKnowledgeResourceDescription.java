package eu.essi_lab.model.ontology;

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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

import eu.essi_lab.model.ontology.d2k.predicates.D2KGSPredicate;
import eu.essi_lab.model.ontology.d2k.resources.GSKnowledgeResource;

/**
 * @author ilsanto
 */
public class GSKnowledgeResourceDescription {

    private GSKnowledgeResource resource;
    private HashMap<GSPredicate, List<Value>> attributes;
    private RelationToParent relationToParent;

    /**
     * @param r
     */
    public GSKnowledgeResourceDescription(GSKnowledgeResource r) {
	resource = r;
	attributes = new HashMap<GSPredicate, List<Value>>();
    }

    /**
     * @param d2kPredicate
     * @param value
     */
    public void add(D2KGSPredicate d2kPredicate, Value value) {

	add(d2kPredicate.getGSPredicate(), value);
    }

    /**
     * @param gsPredicate
     * @param value
     */
    public void add(GSPredicate gsPredicate, Value value) {

	attributes.computeIfAbsent(//
		gsPredicate, //
		k -> attributes.put(gsPredicate, new ArrayList<>()));

	attributes.get(gsPredicate).add(value);
    }

    /**
     * @param d2Predicate
     * @return
     */
    public List<Value> getValues(D2KGSPredicate d2Predicate) {

	return getValues(d2Predicate.getGSPredicate());
    }

    /**
     * @param gsPredicate
     * @return
     */
    public List<Value> getValues(GSPredicate gsPredicate) {

	return attributes.get(gsPredicate) != null ? attributes.get(gsPredicate) : new ArrayList<>();
    }

    /**
     * Returns all the labels according to the {@link D2KGSPredicate#LABEL} predicate
     */
    public List<Literal> getLabels() {

	return getLabels(D2KGSPredicate.LABEL.getGSPredicate());
    }

    /**
     * @param predicate
     * @return
     */
    public List<Literal> getLabels(GSPredicate predicate) {

	List<Value> list = attributes.get(predicate);

	if (list != null && !list.isEmpty()) {

	    return list.stream().map(l -> (Literal) l).collect(Collectors.toList());
	}

	return new ArrayList<>();
    }

    /**
     * Returns all the labels of the provided <code>language</code> according to the {@link D2KGSPredicate#LABEL}
     * predicate
     * 
     * @param language
     */
    public Optional<Literal> getLabel(String language) {

	return getLabel(D2KGSPredicate.LABEL.getGSPredicate(), language);
    }

    /**
     * Returns all the labels of the provided <code>language</code> according to the provided
     * <code>predicate</code>
     * 
     * @param predicate
     * @param language
     */
    public Optional<Literal> getLabel(GSPredicate predicate, String language) {

	return getLabels(predicate).//
		stream().//
		filter(l -> l.getLanguage().isPresent()).//
		filter(l -> l.getLanguage().get().equals(language)).//
		findFirst();
    }

    /**
     * Returns the label with no language set, if any, according to the {@link D2KGSPredicate#LABEL} predicate
     */
    public Optional<Literal> getNoLanguageLabel() {

	return getNoLanguageLabel(D2KGSPredicate.LABEL.getGSPredicate());
    }

    /**
     * Returns the label with no language set, if any, according to the provided
     * <code>predicate</code>
     * 
     * @param predicate
     */
    public Optional<Literal> getNoLanguageLabel(GSPredicate predicate) {

	return getLabels(predicate).//
		stream().//
		filter(l -> !l.getLanguage().isPresent()).//
		findFirst();
    }

    /**
     * @return
     */
    public Set<GSPredicate> getPredicates() {

	return attributes.keySet();
    }

    /**
     * @return
     */
    public GSKnowledgeResource getResource() {

	return resource;
    }

    /**
     * @return
     */
    public RelationToParent getRelationToParent() {

	return relationToParent;
    }

    /**
     * @param relationToParent
     */
    public void setRelationToParent(RelationToParent relationToParent) {

	this.relationToParent = relationToParent;
    }
}
