package eu.essi_lab.gssrv.conf.task.bluecloud;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class MetadataManager {

    private static MetadataManager instance = null;

    public static MetadataManager getInstance() {
	if (instance == null) {
	    instance = new MetadataManager();
	}
	return instance;
    }

    private MetadataManager() {
    }

    private HashMap<String, DocumentReport> metadatas = new HashMap<>();

    public void addMetadata(String id, DocumentReport metadata) {
	metadatas.put(id, metadata);
    }

    public void printStatistics() {
	int size = metadatas.size();
	System.out.println("Metadata documents: " + size);
	Set<Entry<String, DocumentReport>> set = metadatas.entrySet();
	HashMap<MetadataElement, Integer> map = new HashMap<>();
	for (Entry<String, DocumentReport> entry : set) {
	    String id = entry.getKey();
	    DocumentReport metadata = entry.getValue();
	    HashMap<MetadataElement, List<String>> innerMap = metadata.getMap();
	    Set<Entry<MetadataElement, List<String>>> innerEntries = innerMap.entrySet();
	    for (Entry<MetadataElement, List<String>> innerEntry : innerEntries) {
		MetadataElement element = innerEntry.getKey();
		List<String> values = innerEntry.getValue();
		if (values != null && !values.isEmpty()) {
		    Integer count = map.get(element);
		    if (count == null) {
			count = 0;
		    }
		    count++;
		    map.put(element, count);
		}
	    }
	}
	for (Entry<MetadataElement, Integer> entry : map.entrySet()) {
	    MetadataElement element = entry.getKey();
	    Integer value = entry.getValue();
	    int percent = (int) (((double) value / (double) size) * 100.0);
	    System.out.println(element + ": " + value + " (" + percent + "%)");
	}

    }

}
