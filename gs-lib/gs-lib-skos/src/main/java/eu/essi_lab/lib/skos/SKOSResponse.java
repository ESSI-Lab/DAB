/**
 * 
 */
package eu.essi_lab.lib.skos;

import java.util.ArrayList;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;

/**
 * @author Fabrizio
 */
public class SKOSResponse {

    /**
     * 
     */
    public static final String NONE_VALUE = "none";

    private List<SKOSConcept> results;

    /**
     * @param results
     */
    private SKOSResponse(List<SKOSConcept> results) {

	this.results = results;
    }

    /**
     * @param results
     * @return
     */
    public static SKOSResponse of(List<SKOSConcept> results) {

	return new SKOSResponse(results);
    }

    /**
     * @param concept
     * @return
     */
    public Optional<SKOSConcept> getAggregatedConcept(String concept) {

	return getAggregatedResults().//
		stream().//
		filter(c -> c.getConceptURI().equals(concept)).//
		findFirst();
    }

    /**
     * 
     */
    public List<SKOSConcept> getAggregatedResults() {

	Map<String, List<SKOSConcept>> map = getResults().//
		stream().//
		collect(Collectors.groupingBy((c) -> c.getConceptURI()));

	ArrayList<SKOSConcept> out = new ArrayList<SKOSConcept>();

	map.keySet().forEach(concept -> {

	    Optional<SKOSConcept> optional = out.stream().filter(c -> c.getConceptURI().equals(concept)).findFirst();

	    List<SKOSConcept> list = map.get(concept);

	    if (optional.isEmpty()) {

		SKOSConcept skosConcept = SKOSConcept.of(//
			concept, //
			list.get(0).getPref().orElse("none"));

		list.forEach(c -> skosConcept.getAlt().addAll(c.getAlt()));
		list.forEach(c -> skosConcept.getExpanded().addAll(c.getExpanded()));
		list.forEach(c -> skosConcept.getExpandedFrom().addAll(c.getExpandedFrom()));

		out.add(skosConcept);
	    }
	});

	return out;
    }

    /**
     * @author Fabrizio
     */
    private record AggregationKey(String concept, Integer level) {
    };

    /**
     * @param limit
     * @param tempResponse
     * @param results
     * @return
     */
    public static List<SKOSConcept> getAggregatedResults(ExpansionLimit limit, List<SKOSConcept> results) {

	int altCount = 0;
	int labCount = 0;

	Map<AggregationKey, List<SKOSConcept>> map = results.//
		stream().//
		collect(Collectors.groupingBy((c) -> new AggregationKey(c.getConceptURI(), c.getLevel().get().getValue())));

	ArrayList<SKOSConcept> out = new ArrayList<SKOSConcept>();

	List<AggregationKey> sortedKeys = map.keySet().//
		stream().//
		sorted((key1, key2) -> key1.level().compareTo(key2.level())).//
		toList();

	for (AggregationKey key : sortedKeys) {

	    Optional<SKOSConcept> optional = out.stream().filter(c -> c.getConceptURI().equals(key.concept())).findFirst();

	    List<SKOSConcept> list = map.get(key);

	    if (optional.isEmpty()) {

		SKOSConcept skosConcept = SKOSConcept.of(//
			key.concept(), //
			list.get(0).getPref().orElse(NONE_VALUE));

		skosConcept.setLevel(ExpansionLevel.of(key.level()).get());

		list.forEach(c -> skosConcept.getAlt().addAll(c.getAlt()));
		list.forEach(c -> skosConcept.getExpanded().addAll(c.getExpanded()));
		list.forEach(c -> skosConcept.getExpandedFrom().addAll(c.getExpandedFrom()));

		switch (limit.getTarget()) {
		case CONCEPTS:
		    if (out.size() + 1 > limit.getLimit()) {
			return out;
		    }
		    break;

		case ALT_LABELS:

		    if (altCount + skosConcept.getAlt().size() > limit.getLimit()) {
			int exc = (altCount + skosConcept.getAlt().size()) - limit.getLimit();
			int len = skosConcept.getAlt().size() - exc;
			skosConcept.setAlt(skosConcept.getAlt().stream().limit(len).collect(Collectors.toSet()));
			out.add(skosConcept);
			return out;
		    }
		    break;

		case LABELS:

		    if (labCount + skosConcept.getAlt().size() + 1 > limit.getLimit()) {

			int exc = (labCount + skosConcept.getAlt().size() + 1) - limit.getLimit();
			if (skosConcept.getAlt().size() >= exc) {
			    int len = skosConcept.getAlt().size() - exc;
			    skosConcept.setAlt(skosConcept.getAlt().stream().limit(len).collect(Collectors.toSet()));
			} else {
			    return out;
			}
		    }

		    break;
		}

		altCount += skosConcept.getAlt().size();
		labCount += skosConcept.getAlt().size() + 1;

		out.add(skosConcept);
	    }
	}

	return out;
    }

    /**
     * @param limit
     * @param tempResponse
     * @param results
     * @return
     */
    public static List<SKOSConcept> getAggregatedResults(ExpansionLimit limit, SKOSResponse tempResponse, List<SKOSConcept> results) {

	int altCount = (int) results.stream().flatMap(c -> c.getAlt().stream()).count();
	int labCount = results.size() + altCount;

	Map<String, List<SKOSConcept>> map = tempResponse.getResults().//
		stream().//
		collect(Collectors.groupingBy((c) -> c.getConceptURI()));

	ArrayList<SKOSConcept> out = new ArrayList<SKOSConcept>();

	for (String concept : map.keySet()) {

	    Optional<SKOSConcept> optional = out.stream().filter(c -> c.getConceptURI().equals(concept)).findFirst();

	    List<SKOSConcept> list = map.get(concept);

	    if (optional.isEmpty()) {

		SKOSConcept skosConcept = SKOSConcept.of(//
			concept, //
			list.get(0).getPref().orElse("none"));

		list.forEach(c -> skosConcept.getAlt().addAll(c.getAlt()));
		list.forEach(c -> skosConcept.getExpanded().addAll(c.getExpanded()));
		list.forEach(c -> skosConcept.getExpandedFrom().addAll(c.getExpandedFrom()));

		switch (limit.getTarget()) {
		case CONCEPTS:
		    if (out.size() + results.size() + 1 > limit.getLimit()) {
			return out;
		    }
		    break;

		case ALT_LABELS:

		    if (altCount + skosConcept.getAlt().size() > limit.getLimit()) {
			int exc = (altCount + skosConcept.getAlt().size()) - limit.getLimit();
			int len = skosConcept.getAlt().size() - exc;
			skosConcept.setAlt(skosConcept.getAlt().stream().limit(len).collect(Collectors.toSet()));
			out.add(skosConcept);
			return out;
		    }
		    break;

		case LABELS:

		    if (labCount + skosConcept.getAlt().size() + 1 > limit.getLimit()) {

			int exc = (labCount + skosConcept.getAlt().size() + 1) - limit.getLimit();
			if (skosConcept.getAlt().size() >= exc) {
			    int len = skosConcept.getAlt().size() - exc;
			    skosConcept.setAlt(skosConcept.getAlt().stream().limit(len).collect(Collectors.toSet()));
			} else {
			    return out;
			}
		    }

		    break;
		}

		altCount += skosConcept.getAlt().size();
		labCount += skosConcept.getAlt().size() + 1;

		out.add(skosConcept);
	    }
	}

	return out;
    }

    /**
     * @return the results
     */
    public List<SKOSConcept> getResults() {

	return results;
    }

    /**
     * @return
     */
    public List<String> getLabels() {

	return Stream.concat(//
		getPrefLabels().stream(), //
		getAltLabels().stream()).//
		sorted().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<String> getPrefLabels() {

	return getResults().//
		stream().//
		filter(r -> r.getPref().isPresent()).//
		map(r -> r.getPref().get()).//
		distinct().//
		sorted().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public List<String> getAltLabels() {

	return getResults().//
		stream().//
		flatMap(r -> r.getAlt().stream()).//
		distinct().//
		sorted().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public Set<String> getURIs() {

	return getAggregatedResults().stream().map(SKOSConcept::getConceptURI).collect(Collectors.toSet());
    }

}
