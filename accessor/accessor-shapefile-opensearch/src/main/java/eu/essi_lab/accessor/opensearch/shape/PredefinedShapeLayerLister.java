package eu.essi_lab.accessor.opensearch.shape;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.accessor.opensearch.shape.ShapeWmsLayerFilter.LayerFilterContext;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.messages.bond.spatial.ShapeLayerOwner;

/**
 * Lists predefined search areas for the O&amp;M properties API and portal (same visibility as shape WMS).
 */
public final class PredefinedShapeLayerLister {

    /**
     * One selectable predefined area.
     *
     * @param value {@code predefinedSearchArea} (or {@code predefinedLayer}) parameter value (harvested online id)
     * @param label human-readable title
     */
    public record LayerItem(String value, String label) {
    }

    /**
     * One page of predefined layers for the properties API.
     *
     * @param items layers in this page
     * @param completed {@code true} when there is no next page
     * @param resumptionToken offset for the next request, or {@code null} when {@code completed}
     */
    public record PagedLayerItems(List<LayerItem> items, boolean completed, String resumptionToken) {
    }

    private PredefinedShapeLayerLister() {
    }

    /**
     * @param tokenSegment token from {@code /token/{token}/view/...} ({@link ShapeLayerOwner#PUBLIC_TOKEN} when absent)
     * @param limit maximum number of items (ignored when {@code <= 0})
     * @return visible predefined layers sorted by label
     */
    public static List<LayerItem> list(String tokenSegment, int limit) {

	return listPaged(tokenSegment, limit, null).items();
    }

    /**
     * @param tokenSegment token from {@code /token/{token}/view/...}
     * @param limit maximum number of items per page (ignored when {@code <= 0}, returns remainder)
     * @param resumptionToken zero-based offset from a previous response, or {@code null} for the first page
     * @return one page of visible predefined layers sorted by label
     */
    public static PagedLayerItems listPaged(String tokenSegment, int limit, String resumptionToken) {

	List<LayerItem> all = loadAll(tokenSegment);
	int offset = parseOffset(resumptionToken);
	if (offset > all.size()) {
	    offset = all.size();
	}

	int end = limit > 0 ? Math.min(offset + limit, all.size()) : all.size();
	List<LayerItem> page = all.subList(offset, end);
	boolean completed = end >= all.size();
	String nextToken = completed ? null : String.valueOf(end);

	return new PagedLayerItems(page, completed, nextToken);
    }

    private static int parseOffset(String resumptionToken) {

	if (resumptionToken == null || resumptionToken.isBlank()) {
	    return 0;
	}
	try {
	    int offset = Integer.parseInt(resumptionToken.trim());
	    return Math.max(0, offset);
	} catch (NumberFormatException ex) {
	    return 0;
	}
    }

    private static List<LayerItem> loadAll(String tokenSegment) {

	LayerFilterContext context = ShapeWmsLayerFilter.createContext(tokenSegment);
	List<LayerItem> items = new ArrayList<>();

	try {

	    for (JSONObject source : new OpenSearchShapefileClient().loadPredefinedLayerSources()) {

		String entryName = source.optString(IndexData.ENTRY_NAME, "");
		if (entryName.isBlank()) {
		    continue;
		}

		String onlineId = ShapeLayerOwner.OPENSEARCH_ONLINE_PREFIX + entryName;
		if (!ShapeWmsLayerFilter.isLayerVisible(onlineId, context)) {
		    continue;
		}

		String label = source.optString(ShapeFileMapping.ENTRY_TITLE, "");
		if (label.isBlank()) {
		    label = entryName;
		}

		items.add(new LayerItem(onlineId, label));
	    }

	} catch (Exception ex) {
	    return List.of();
	}

	items.sort(Comparator.comparing(LayerItem::label, String.CASE_INSENSITIVE_ORDER));
	return items;
    }

    /**
     * @param tokenSegment token from the request path
     * @return token segment for {@link #list(String, int)}
     */
    public static String tokenFromRequest(Optional<String> tokenSegment) {

	return tokenSegment.filter(t -> !t.isBlank()).orElse(ShapeLayerOwner.PUBLIC_TOKEN);
    }
}
