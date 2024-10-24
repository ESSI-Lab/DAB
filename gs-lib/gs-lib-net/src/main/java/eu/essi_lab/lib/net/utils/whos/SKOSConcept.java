package eu.essi_lab.lib.net.utils.whos;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;


public class SKOSConcept {

    private SimpleEntry<String, String> preferredLabel;
    private SimpleEntry<String, String> definition;
    private HashSet<SimpleEntry<String, String>> alternateLabels = new java.util.HashSet();
    private HashSet<String> closeMatches = new HashSet<>();

    private String uri;

    public SKOSConcept(String uri) {
	this.uri = uri;
    }

    public SimpleEntry<String, String> getDefinition() {
	return definition;
    }

    public void setDefinition(SimpleEntry<String, String> definition) {
	this.definition = definition;
    }

    public SimpleEntry<String, String> getPreferredLabel() {
	return preferredLabel;
    }

    public void setPreferredLabel(SimpleEntry<String, String> preferredLabel) {
	this.preferredLabel = preferredLabel;
    }

    public HashSet<SimpleEntry<String, String>> getAlternateLabels() {
	return alternateLabels;
    }

    public void setAlternateLabels(HashSet<SimpleEntry<String, String>> alternateLabels) {
	this.alternateLabels = alternateLabels;
    }

    public HashSet<String> getCloseMatches() {
	return closeMatches;
    }

    public void setCloseMatches(HashSet<String> closeMatches) {
	this.closeMatches = closeMatches;
    }

    public String getURI() {
	return uri;
    }

    public void setURI(String uri) {
	this.uri = uri;
    }
}
