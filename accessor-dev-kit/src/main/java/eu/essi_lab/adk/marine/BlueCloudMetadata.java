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

public class BlueCloudMetadata {

    public enum BlueCloudElement {
	IDENTIFIER, //
	TITLE, //
	KEYWORD, //
	BOUNDING_BOX, //
	GEOGRAPHIC_DESCRIPTION, //
	TEMPORAL_EXTENT, //
	PARAMETER, //
	INSTRUMENT, //
	PLATFORM, //
	ORGANIZATION,
	DATESTAMP,//
	REVISION_DATE,//
	RESOURCE_IDENTIFIER
    }
    
    private HashMap<BlueCloudElement, String> map = new HashMap<>();
    
    public HashMap<BlueCloudElement, String> getMap() {
        return map;
    }

    public void addMetadata(BlueCloudElement element, String value) {
	map.put(element, value);
    }

}
