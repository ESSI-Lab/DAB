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
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.accessor.s3.FeatureMetadata;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import org.locationtech.jts.geom.Geometry;

/**
 * Harvests shape features indexed in OpenSearch ({@link eu.essi_lab.api.database.Database#SHAPE_FILES_FOLDER}).
 */
public class OpenSearchShapefileConnector extends HarvestedQueryConnector<OpenSearchShapefileConnectorSetting> {

    public static final String TYPE = "OpenSearchShapefileConnector";

    public static final String DEFAULT_ENDPOINT = "opensearch://shapeFiles";

    @Override
    public boolean supports(GSSource source) {

	String url = source.getEndpoint();
	if (url == null) {
	    return false;
	}
	String lower = url.toLowerCase();
	return lower.contains("opensearch") || lower.startsWith("shapefile-opensearch");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();
	int added = 0;

	String sourceUrl = getSourceURL();
	if (sourceUrl == null || sourceUrl.isEmpty()) {
	    sourceUrl = DEFAULT_ENDPOINT;
	}

	try {
	    OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	    for (String entryName : client.listEntryNames()) {


		if (!unlimited && mr.isPresent() && added >= mr.get()) {
		    ret.setResumptionToken(null);
		    return ret;
		}

		Optional<JSONObject> source = client.getShapeSource(entryName);
		if (source.isEmpty() || !source.get().has("shape")) {
		    continue;
		}

		try {
		    JSONObject osSource = source.get();
		    JSONObject shape = osSource.getJSONObject("shape");
		    String shapeCrs = osSource.optString(ShapeFileMapping.SHAPE_CRS, CRS.EPSG_4326().getIdentifier());
		    Geometry geometry = ShapeBoundingBoxNormalizer.toWgs84(OpenSearchShapeGeometryUtils.toGeometry(shape), shapeCrs);
		    FeatureMetadata feature = new FeatureMetadata();
		    feature.setUrl(sourceUrl);
		    feature.setId(entryName);
		    OpenSearchShapeGeometryUtils.setBoundingBox(feature, geometry);
		    feature.getAttributes().put("entryName", entryName);
		    String entryTitle = osSource.optString(ShapeFileMapping.ENTRY_TITLE, "");
		    if (!entryTitle.isBlank()) {
			feature.getAttributes().put("entryTitle", entryTitle);
		    }
		    String owner = osSource.optString(ShapeFileMapping.OWNER, "");
		    if (!owner.isBlank()) {
			feature.getAttributes().put("owner", owner);
		    }

		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.OPENSEARCH_SHAPEFILE);
		    metadataRecord.setMetadata(feature.marshal());
		    ret.addRecord(metadataRecord);
		    added++;
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error mapping shape entry {}: {}", entryName, e.getMessage());
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error listing OpenSearch shapes: {}", e.getMessage());
	    throw GSException.createException(getClass(), e.getMessage(), null, null, null, null, null, e);
	}

	ret.setResumptionToken(null);
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.OPENSEARCH_SHAPEFILE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected OpenSearchShapefileConnectorSetting initSetting() {
	return new OpenSearchShapefileConnectorSetting();
    }
}
