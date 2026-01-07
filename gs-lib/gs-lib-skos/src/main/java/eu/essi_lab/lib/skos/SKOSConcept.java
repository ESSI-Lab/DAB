/**
 * 
 */
package eu.essi_lab.lib.skos;

import java.util.HashSet;

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

import java.util.Optional;
import java.util.Set;

import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;

/**
 * @author Fabrizio
 */
public class SKOSConcept {

    private String concept;
    private Set<String> expandedFrom;
    private Set<String> expanded;
    private String pref;
    private Set<String> alt;
    private ExpansionLevel level;

    /**
     * 
     */
    private SKOSConcept() {

	alt = new HashSet<>();
	expandedFrom = new HashSet<>();
	expanded = new HashSet<>();
    }

    /**
     * @param concept
     * @return
     */
    public static SKOSConcept of(String concept) {

	return SKOSConcept.of(concept, null, new HashSet<>(), new HashSet<>(), new HashSet<>(), null);
    }

    /**
     * @param concept
     * @param pref
     * @return
     */
    public static SKOSConcept of(String concept, String pref) {

	return SKOSConcept.of(concept, pref, new HashSet<>(), new HashSet<>(), new HashSet<>(), null);
    }

    /**
     * @param expanded
     * @return
     */
    public static SKOSConcept of(Set<String> expanded) {

	return SKOSConcept.of(null, null, expanded, new HashSet<>(), new HashSet<>(), null);
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param expandedFrom
     * @param alt
     * @return
     */
    public static SKOSConcept of(String concept, String pref, String expanded, String expandedFrom, String alt) {

	return SKOSConcept.of(//
		concept, //
		pref, //
		new HashSet<String>(expanded != null ? Set.of(expanded) : Set.of()), //
		new HashSet<String>(expandedFrom != null ? Set.of(expandedFrom) : Set.of()), //
		new HashSet<String>(alt != null ? Set.of(alt) : Set.of()), //
		null);
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param expandedFrom
     * @param alt
     * @param level
     * @return
     */
    public static SKOSConcept of(String concept, String pref, String expanded, String expandedFrom, String alt, ExpansionLevel level) {

	return SKOSConcept.of(//
		concept, //
		pref, //
		new HashSet<String>(expanded != null ? Set.of(expanded) : Set.of()), //
		new HashSet<String>(expandedFrom != null ? Set.of(expandedFrom) : Set.of()), //
		new HashSet<String>(alt != null ? Set.of(alt) : Set.of()), //
		level);
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param expandedFrom
     * @param alt
     * @return
     */
    public static SKOSConcept of(//
	    String concept, //
	    String pref, //
	    Set<String> expanded, //
	    Set<String> expandedFrom, //
	    Set<String> alt) {

	return SKOSConcept.of(//
		concept, //
		pref, //
		expanded, //
		expandedFrom, //
		alt, //
		null);
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param expandedFrom
     * @param alt
     * @param level
     * @return
     */
    public static SKOSConcept of(//
	    String concept, //
	    String pref, Set<String> expanded, //
	    Set<String> expandedFrom, //
	    Set<String> alt, //
	    ExpansionLevel level) {

	SKOSConcept out = new SKOSConcept();
	out.concept = concept;
	out.pref = pref;
	out.expanded = expanded;
	out.expandedFrom = expandedFrom;
	out.alt = alt;
	out.level = level;

	return out;
    }

    /**
     * @param pref
     */
    public void setPref(String pref) {

	this.pref = pref;
    }

    /**
     * @param alt
     */
    public void setAlt(Set<String> alt) {

	this.alt = alt;
    }

    /**
     * @return
     */
    public String getConceptURI() {

	return concept;
    }

    /**
     * @return
     */
    public Set<String> getExpanded() {

	return expanded;
    }

    /**
     * @return
     */
    public Set<String> getExpandedFrom() {

	return expandedFrom;
    }

    /**
     * @return
     */
    public Optional<String> getPref() {

	if (pref != null) {
	    return Optional.of(pref);
	} else {
	    if (getAlt().isEmpty()) {
		return Optional.empty();
	    } else {
		return Optional.of(getAlt().iterator().next());
	    }
	}

    }

    /**
     * @return
     */
    public Set<String> getAlt() {

	return alt;
    }

    /**
     * @param level
     * @return
     */
    public void setLevel(ExpansionLevel expansionLevel) {

	this.level = expansionLevel;
    }

    /**
     * @return the level
     */
    public Optional<ExpansionLevel> getLevel() {

	return Optional.ofNullable(level);
    }

    @Override
    public String toString() {

	return "concept: " + getConceptURI() + //
		"\nexpanded from: " + getExpandedFrom() + //
		"\nexpanded: " + getExpanded() + //
		getPref().map(v -> "\npref: " + v).orElse("") + //
		"\nalt: " + getAlt() + getLevel().map(v -> "\nlevel: " + v).orElse(""); //

    }

    @Override
    public boolean equals(Object other) {

	return other instanceof SKOSConcept && other.toString().equals(this.toString());
    }
}
