package eu.essi_lab.adk.marine;

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

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import eu.essi_lab.adk.marine.BlueCloudMetadata.BlueCloudElement;

public class BlueCloudObjectManager {
    
    public BlueCloudObjectManager() {
	
    }
    
    private HashMap<String, BlueCloudMetadata> metadatas = new HashMap<>();
    
    private HashMap<BlueCloudElement, Integer> metadataPercentMap = new HashMap<>();

    public HashMap<BlueCloudElement, Integer> getmetadataPercentMap() {
        return metadataPercentMap;
    }
    
    
    public void reset() {
	    metadatas = new HashMap<>();
	    metadataPercentMap = new HashMap<>();
    }

    public void addMetadata(String id, BlueCloudMetadata metadata) {
	metadatas.put(id, metadata);
    }

    public void printStatistics() {
	int size = metadatas.size();
	System.out.println("Metadata documents: " + size);
	Set<Entry<String, BlueCloudMetadata>> set = metadatas.entrySet();
	HashMap<BlueCloudElement, Integer> map = new HashMap<>();
	for (Entry<String, BlueCloudMetadata> entry : set) {
	    String id = entry.getKey();
	    BlueCloudMetadata metadata = entry.getValue();
	    HashMap<BlueCloudElement, String> innerMap = metadata.getMap();
	    Set<Entry<BlueCloudElement, String>> innerEntries = innerMap.entrySet();
	    for (Entry<BlueCloudElement, String> innerEntry : innerEntries) {
		BlueCloudElement element = innerEntry.getKey();
		String value = innerEntry.getValue();
		if (value != null && !value.equals("")) {
		    Integer count = map.get(element);
		    if (count == null) {
			count = 0;
		    }
		    count++;
		    map.put(element, count);
		}
	    }
	}
	for (Entry<BlueCloudElement, Integer> entry : map.entrySet()) {
	    BlueCloudElement element = entry.getKey();
	    Integer value = entry.getValue();
	    int percent = (int) (((double) value / (double) size) * 100.0);
	    metadataPercentMap.put(element, percent);
	    System.out.println(element + ": " + value + " (" + percent + "%)");
	}

    }

}
