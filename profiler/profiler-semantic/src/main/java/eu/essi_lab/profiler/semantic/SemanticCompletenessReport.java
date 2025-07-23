package eu.essi_lab.profiler.semantic;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SemanticCompletenessReport {

	private Set<String> conceptURIs = new TreeSet();
	
	private HashMap<String, String>urisToLabel = new HashMap<String, String>();

	public Set<String> getConceptURIs() {
		return conceptURIs;
	}

	private HashMap<String, String> map = new HashMap<String, String>();

	public void addTerm(String sourceId, String conceptURI, String term) {
		map.put(sourceId + conceptURI, term);
	}

	public String getTerm(String sourceId, String conceptURI) {
		return map.get(sourceId + conceptURI);
	}

	public void addConcept(String uri, String label) {
		conceptURIs.add(uri);
		urisToLabel.put(uri, label);
	}
	
	public String getLabel(String uri) {
		return urisToLabel.get(uri);
	}
	
	public boolean hasConcept(String uri) {
		if (uri==null) {
			return false;
		}
		return conceptURIs.contains(uri);
	}

}
