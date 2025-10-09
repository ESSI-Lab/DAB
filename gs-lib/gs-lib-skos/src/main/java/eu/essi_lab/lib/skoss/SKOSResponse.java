/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.ArrayList;

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabrizio
 */
public class SKOSResponse {

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

    public List<SKOSConcept> getAssembledResults() {

	Map<String, List<SKOSConcept>> map = results.stream().collect(Collectors.groupingBy((c) -> c.getConcept()));

	List<SKOSConcept> ret = new ArrayList<SKOSConcept>();

	Set<Entry<String, List<SKOSConcept>>> entries = map.entrySet();
	for (Entry<String, List<SKOSConcept>> entry : entries) {
	    SKOSConcept tmp = null;
	    for (SKOSConcept concept : entry.getValue()) {
		if (tmp == null) {
		    tmp = concept;
		} else {
		    tmp.getAlt().addAll(concept.getAlt());
		}
	    }
	    if (tmp != null) {
		ret.add(tmp);
	    }
	}

	return ret;
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

}
