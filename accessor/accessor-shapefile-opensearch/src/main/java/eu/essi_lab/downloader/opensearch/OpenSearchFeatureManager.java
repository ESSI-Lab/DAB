package eu.essi_lab.downloader.opensearch;

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

import java.util.HashMap;
import java.util.Optional;

import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;

import eu.essi_lab.accessor.opensearch.shape.OpenSearchShapeGeometryUtils;
import eu.essi_lab.accessor.opensearch.shape.OpenSearchShapefileClient;
import eu.essi_lab.accessor.opensearch.shape.ShapeBoundingBoxNormalizer;
import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Loads and caches geometries from the OpenSearch shape-files folder.
 */
public class OpenSearchFeatureManager {

    private static OpenSearchFeatureManager instance;

    private final HashMap<String, OpenSearchFeature> features = new HashMap<>();

    private OpenSearchFeatureManager() {
    }

    public static OpenSearchFeatureManager getInstance() {
	if (instance == null) {
	    instance = new OpenSearchFeatureManager();
	}
	return instance;
    }

    /**
     * @param linkage online resource linkage (source endpoint)
     * @param name full OpenSearch shape-files entry name
     */
    public OpenSearchFeature getFeature(String linkage, String name) throws Exception {

	String cacheId = linkage + ":" + name;
	OpenSearchFeature cached = features.get(cacheId);
	if (cached != null) {
	    return cached;
	}

	OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	Optional<JSONObject> source = client.getShapeSource(name);
	if (source.isEmpty() || !source.get().has("shape")) {
	    GSLoggerFactory.getLogger(getClass()).warn("Shape not found for entry {}", name);
	    return null;
	}

	JSONObject osSource = source.get();
	JSONObject shape = osSource.getJSONObject("shape");
	String shapeCrs = osSource.optString(ShapeFileMapping.SHAPE_CRS, CRS.EPSG_4326().getIdentifier());
	Geometry geometry = ShapeBoundingBoxNormalizer.toWgs84(OpenSearchShapeGeometryUtils.toGeometry(shape), shapeCrs);

	FeatureMetadata metadata = new FeatureMetadata();
	metadata.setUrl(linkage);
	metadata.setId(name);
	OpenSearchShapeGeometryUtils.setBoundingBox(metadata, geometry);

	OpenSearchFeature feature = new OpenSearchFeature();
	feature.setGeometry(geometry);
	feature.setFeatureMetadata(metadata);
	features.put(cacheId, feature);
	return feature;
    }
}
