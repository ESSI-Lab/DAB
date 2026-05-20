package eu.essi_lab.messages.bond.spatial;

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

import java.util.Iterator;
import java.util.Optional;

import eu.essi_lab.model.resource.GSResource;

/**
 * Owner keyword on harvested shape WMS layers and visibility rules for the portal.
 */
public final class ShapeLayerOwner {

    /** Token segment used before login ({@code /token/public/view/...}). */
    public static final String PUBLIC_TOKEN = "public";

    /** Owner value for layers uploaded by administrators. */
    public static final String ADMIN_OWNER = "admin";

    public static final String KEYWORD_PREFIX = "shapeOwner:";

    public static final String OPENSEARCH_ONLINE_PREFIX = "opensearch://shapeFiles:";

    private ShapeLayerOwner() {
    }

    /**
     * @param owner stored owner id
     * @return keyword stored in metadata
     */
    public static String toKeyword(String owner) {

	return KEYWORD_PREFIX + owner;
    }

    /**
     * @param resource harvested dataset
     * @return owner id when this is a shape layer
     */
    public static Optional<String> readFromResource(GSResource resource) {

	if (resource == null || resource.getHarmonizedMetadata() == null) {
	    return Optional.empty();
	}

	Iterator<String> keywords = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDataIdentification()
		.getKeywordsValues();

	while (keywords.hasNext()) {

	    String keyword = keywords.next();

	    if (keyword != null && keyword.startsWith(KEYWORD_PREFIX)) {
		return Optional.of(keyword.substring(KEYWORD_PREFIX.length()));
	    }
	}

	return Optional.empty();
    }

    /**
     * @param resourceOwner owner stored on the layer; empty for legacy layers without owner
     * @param viewerOwner owner id of the requesting user; empty when anonymous
     * @return whether the layer is listed in GetCapabilities / predefined selection
     */
    public static boolean isVisible(String resourceOwner, String viewerOwner) {

	if (ADMIN_OWNER.equals(resourceOwner)) {
	    return true;
	}

	if (viewerOwner == null || viewerOwner.isBlank()) {
	    return false;
	}

	return viewerOwner.equals(resourceOwner);
    }

    /**
     * @param onlineId WMS layer name / online id
     * @return OpenSearch shape entry name when applicable
     */
    public static Optional<String> entryNameFromOnlineId(String onlineId) {

	if (onlineId == null || !onlineId.startsWith(OPENSEARCH_ONLINE_PREFIX)) {
	    return Optional.empty();
	}

	return Optional.of(onlineId.substring(OPENSEARCH_ONLINE_PREFIX.length()));
    }
}
