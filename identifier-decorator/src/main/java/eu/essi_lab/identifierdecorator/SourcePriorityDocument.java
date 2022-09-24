package eu.essi_lab.identifierdecorator;

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

import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
@Deprecated
public class SourcePriorityDocument {

    public static final String PRIORITY_DOCUMENT = "PRIORITY_DOCUMENT";
    public static final String OGC_CITE_CSW_TEST_DATA_SOURCE_ID = "ogc-cite-csw-test-data";
    public static final String GERMAN_AEROSPACE_CENTER_ID = "dlrcswID";
    public static final String PANGAEA = "UUID-2dc3a01b-934e-4c3d-9311-527ac93ec058";
    public static final String ARGO = "argo";
    public static final String WEKEO = "wekeo";
    public static final String ELIXIR_ENA = "elixir-ena";
    public static final String ICOS_SOCAT = "icos-socat";
    public static final String ICOS_DATA_PORTAL = "icos-data-portal";
    public static final String EUROBIS = "eurobis";
    
    public static final String EMODNET_CHEMISTRY= "emodnet-chemistry";
    public static final String SEADATANET_OPEN= "seadatanet-open";
    public static final String SEADATANET_PRODUCTS= "seadatanet-products";
    public static final String CHINA_GEOSS= "chinageosatellite";
    
    
//    public static final String STARS4ALL = "start4allID";
//    public static final String C3S = "climatec3sID";

    /**
     * Check by source's identifier if the given source is in the priority collection.
     *
     * @param source the source to check if is in
     * @return true if the given source is in the collection, false otherwise.
     */
    public boolean isPrioritySource(GSSource source) {
	String sourceId = source.getUniqueIdentifier();
	if (sourceId != null && (//
	sourceId.equals(OGC_CITE_CSW_TEST_DATA_SOURCE_ID) || //
		sourceId.equals(GERMAN_AEROSPACE_CENTER_ID) || //
		sourceId.equals(ARGO) || //
		sourceId.equals(WEKEO) || //
		sourceId.equals(ICOS_DATA_PORTAL) || //
		sourceId.equals(ICOS_SOCAT) || //
		sourceId.equals(ELIXIR_ENA) || //
		sourceId.equals(EUROBIS) || //
		sourceId.equals(SEADATANET_OPEN) ||
		sourceId.equals(CHINA_GEOSS) ||//
		sourceId.equals(PANGAEA)) ) {
	    return true;
	}
	return false;
    }

    /**
     * Given two sources, it check if the first one has bigger priority than second one.
     *
     * @param biggerPrioritySource source with the bigger priority expected
     * @param lowerPrioritySource source with the lower priority expected
     * @return true if biggerPrioritySource has greater priority than lowerPrioritySource. Returns false otherwise.
     */
    public boolean hasGreaterPriorityThan(GSSource biggerPrioritySource, GSSource lowerPrioritySource) {

	return false;
    }

}
