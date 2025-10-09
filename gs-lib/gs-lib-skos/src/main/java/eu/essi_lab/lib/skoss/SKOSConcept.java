/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.ArrayList;
import java.util.List;

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

import java.util.Optional;

/**
 * @author Fabrizio
 */
public class SKOSConcept {

    private String concept;
    private String expanded;
    private String pref;
    private List<String> alt;

    /**
     * 
     */
    private SKOSConcept() {

	alt = new ArrayList<String>();
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param alt
     * @return
     */
    public static SKOSConcept of(String concept, String pref, String expanded, List<String> alt) {

	SKOSConcept item = new SKOSConcept();
	item.concept = concept;
	item.pref = pref;
	item.expanded = expanded;
	item.alt = alt;

	return item;
    }

    /**
     * @param concept
     * @param pref
     * @param expanded
     * @param alt
     * @return
     */
    public static SKOSConcept of(String concept, String pref, String expanded, String alt) {

	SKOSConcept item = new SKOSConcept();
	item.concept = concept;
	item.pref = pref;
	item.expanded = expanded;

	item.alt = new ArrayList<String>();
	item.alt.add(alt);

	return item;
    }

    /**
     * @return
     */
    public String getConcept() {

	return concept;
    }

    /**
     * @return
     */
    public Optional<String> getExpanded() {

	return Optional.ofNullable(expanded);
    }

    /**
     * @return
     */
    public Optional<String> getPref() {

	return Optional.ofNullable(pref);
    }

    /**
     * @return
     */
    public List<String> getAlt() {

	return alt;
    }

    @Override
    public String toString() {

	return "concept: " + getConcept() + //
		getExpanded().map(v -> "\nexpanded: " + v).orElse("") + //
		getPref().map(v -> "\npref: " + v).orElse("") + //
		"\nalt: " + getAlt();//

    }

    @Override
    public boolean equals(Object other) {

	return other instanceof SKOSConcept && other.toString().equals(this.toString());
    }
}
