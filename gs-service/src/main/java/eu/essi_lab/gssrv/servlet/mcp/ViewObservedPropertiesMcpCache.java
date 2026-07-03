package eu.essi_lab.gssrv.servlet.mcp;

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
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of MCP observed-properties JSON payloads keyed by DAB view id.
 */
final class ViewObservedPropertiesMcpCache {

    private static final ConcurrentHashMap<String, String> PAYLOAD_BY_VIEW_ID = new ConcurrentHashMap<>();

    private ViewObservedPropertiesMcpCache() {
    }

    static Optional<String> get(String viewId) {

	if (viewId == null || viewId.isBlank()) {
	    return Optional.empty();
	}
	return Optional.ofNullable(PAYLOAD_BY_VIEW_ID.get(viewId));
    }

    static void put(String viewId, String jsonPayload) {

	if (viewId == null || viewId.isBlank() || jsonPayload == null) {
	    return;
	}
	PAYLOAD_BY_VIEW_ID.put(viewId, jsonPayload);
    }

    static int size() {

	return PAYLOAD_BY_VIEW_ID.size();
    }
}
