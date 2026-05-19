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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;

/**
 * Reads polygon features from the OpenSearch {@link Database#SHAPE_FILES_FOLDER} index.
 */
public class OpenSearchShapefileClient {

    private final OpenSearchFolder folder;
    private final OpenSearchWrapper wrapper;

    public OpenSearchShapefileClient() throws Exception {

	this(ConfigurationWrapper.getStorageInfo());
    }

    public OpenSearchShapefileClient(StorageInfo storageInfo) throws Exception {

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(storageInfo);
	this.folder = new OpenSearchFolder(database, OpenSearchDatabase.SHAPE_FILES_FOLDER);
	this.wrapper = new OpenSearchWrapper(database);
    }

    public List<String> listEntryNames() throws Exception {

	String[] keys = folder.listKeys();
	if (keys == null) {
	    return List.of();
	}
	return new ArrayList<>(Arrays.asList(keys));
    }

    public Optional<JSONObject> getShapeSource(String entryName) {

	try {
	    String entryId = OpenSearchFolder.getEntryId(folder, entryName);
	    return wrapper.getSource(ShapeFileMapping.get().getIndex(), entryId);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error reading shape {}: {}", entryName, e.getMessage());
	    return Optional.empty();
	}
    }

    public OpenSearchFolder getFolder() {
	return folder;
    }

    /**
     * @param registryJson JSON with upload metadata
     */
    public void storeUploadRegistry(String registryJson) throws Exception {

	IndexData indexData = IndexData.ofUploadRegistry(folder, registryJson);
	wrapper.storeWithOpenSearchClient(indexData);
	wrapper.synch();
    }

    /**
     * @param prefix upload identifier
     * @param fileName original zip file name
     */
    public void registerUpload(String prefix, String fileName) throws Exception {

	List<UploadRecord> uploads = readUploadRegistry();

	uploads.removeIf(u -> u.prefix().equals(prefix));
	uploads.add(new UploadRecord(prefix, fileName, ISO8601DateTimeUtils.getISO8601DateTime()));

	writeUploadRegistry(uploads);
    }

    /**
     * @param prefix upload identifier
     */
    public void unregisterUpload(String prefix) throws Exception {

	List<UploadRecord> uploads = readUploadRegistry();
	uploads.removeIf(u -> u.prefix().equals(prefix));
	writeUploadRegistry(uploads);
    }

    /**
     * @return upload records from registry
     */
    public List<UploadRecord> readUploadRegistry() throws Exception {

	Optional<JSONObject> source = getShapeSource(ShapeFileMapping.UPLOAD_REGISTRY_ENTRY_NAME);

	if (source.isEmpty()) {
	    return new ArrayList<>();
	}

	String registryJson = source.get().optString(ShapeFileMapping.UPLOAD_REGISTRY);

	if (registryJson == null || registryJson.isBlank()) {
	    return new ArrayList<>();
	}

	JSONObject root = new JSONObject(registryJson);
	JSONArray array = root.optJSONArray("uploads");

	if (array == null) {
	    return new ArrayList<>();
	}

	List<UploadRecord> out = new ArrayList<>();

	for (int i = 0; i < array.length(); i++) {

	    JSONObject object = array.getJSONObject(i);
	    out.add(new UploadRecord(object.optString("prefix"), object.optString("fileName"), object.optString("uploadedAt")));
	}

	return out;
    }

    private void writeUploadRegistry(List<UploadRecord> uploads) throws Exception {

	JSONObject root = new JSONObject();
	JSONArray array = new JSONArray();

	for (UploadRecord upload : uploads) {

	    JSONObject object = new JSONObject();
	    object.put("prefix", upload.prefix());
	    object.put("fileName", upload.fileName());
	    object.put("uploadedAt", upload.uploadedAt());
	    array.put(object);
	}

	root.put("uploads", array);
	storeUploadRegistry(root.toString());
    }

    /**
     * Upload metadata.
     */
    public record UploadRecord(String prefix, String fileName, String uploadedAt) {
    }
}
