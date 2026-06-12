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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;

/**
 * Reads polygon features from the OpenSearch {@link Database#SHAPE_FILES_FOLDER} index.
 */
public class OpenSearchShapefileClient {

    private static final List<String> METADATA_FIELDS = List.of(//
	    IndexData.ENTRY_NAME, //
	    ShapeFileMapping.ENTRY_TITLE, //
	    ShapeFileMapping.SHAPE_GROUP, //
	    ShapeFileMapping.OWNER);

    private static final List<String> RENAME_SOURCE_EXCLUDES = List.of(ShapeFileMapping.SHAPE_FILE);

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

    /**
     * @param entryName shape entry name
     * @return metadata fields only (no geometry or embedded binary payload)
     */
    public Optional<JSONObject> getShapeMetadata(String entryName) {

	try {
	    String entryId = OpenSearchFolder.getEntryId(folder, entryName);
	    return wrapper.getSourceFields(ShapeFileMapping.get().getIndex(), entryId, METADATA_FIELDS, null);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error reading shape metadata {}: {}", entryName, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param entryName shape entry name
     * @param fields document fields to merge
     * @return {@code true} when the update was applied
     */
    public boolean patchShapeFields(String entryName, Map<String, Object> fields) {

	if (fields == null || fields.isEmpty()) {
	    return true;
	}

	try {
	    String entryId = OpenSearchFolder.getEntryId(folder, entryName);
	    return wrapper.updateSourceFields(ShapeFileMapping.get().getIndex(), entryId, fields);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error updating shape {}: {}", entryName, e.getMessage());
	    return false;
	}
    }

    /**
     * @param entryName shape entry name
     * @return full document without the redundant {@link ShapeFileMapping#SHAPE_FILE} binary field
     */
    public Optional<JSONObject> getShapeSourceForRename(String entryName) {

	try {
	    String entryId = OpenSearchFolder.getEntryId(folder, entryName);
	    return wrapper.getSourceFields(ShapeFileMapping.get().getIndex(), entryId, null, RENAME_SOURCE_EXCLUDES);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error reading shape {} for rename: {}", entryName, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @return map of shape entry name → owner id (single paginated OpenSearch query series)
     */
    public Map<String, String> loadEntryOwners() throws Exception {

	Map<String, String> out = new HashMap<>();

	String index = ShapeFileMapping.get().getIndex();
	Query query = OpenSearchQueryBuilder.buildFolderEntriesQuery(folder);

	int pageSize = 1000;
	int from = 0;

	while (true) {

	    SearchResponse<Object> response = wrapper.search(//
		    index, //
		    query, //
		    List.of(IndexData.ENTRY_NAME, ShapeFileMapping.OWNER), //
		    from, //
		    pageSize, //
		    Optional.empty(), //
		    Optional.empty(), //
		    false, //
		    true);

	    List<JSONObject> batch = OpenSearchUtils.toJSONSourcesList(response);

	    if (batch.isEmpty()) {
		break;
	    }

	    for (JSONObject source : batch) {

		String entryName = source.optString(IndexData.ENTRY_NAME, "");

		if (entryName.isBlank() || ShapeFileMapping.UPLOAD_REGISTRY_ENTRY_NAME.equals(entryName)) {
		    continue;
		}

		out.put(entryName, source.optString(ShapeFileMapping.OWNER, ""));
	    }

	    from += batch.size();

	    if (batch.size() < pageSize) {
		break;
	    }
	}

	return out;
    }

    /**
     * @return shape index sources with entry name, title and owner (excluding upload registry)
     */
    public List<JSONObject> loadPredefinedLayerSources() throws Exception {

	List<JSONObject> out = new ArrayList<>();

	String index = ShapeFileMapping.get().getIndex();
	Query query = OpenSearchQueryBuilder.buildFolderEntriesQuery(folder);

	int pageSize = 1000;
	int from = 0;

	while (true) {

	    SearchResponse<Object> response = wrapper.search(//
		    index, //
		    query, //
		    List.of(IndexData.ENTRY_NAME, ShapeFileMapping.ENTRY_TITLE, ShapeFileMapping.SHAPE_GROUP,
			    ShapeFileMapping.OWNER), //
		    from, //
		    pageSize, //
		    Optional.empty(), //
		    Optional.empty(), //
		    false, //
		    true);

	    List<JSONObject> batch = OpenSearchUtils.toJSONSourcesList(response);

	    if (batch.isEmpty()) {
		break;
	    }

	    for (JSONObject source : batch) {

		String entryName = source.optString(IndexData.ENTRY_NAME, "");

		if (entryName.isBlank() || ShapeFileMapping.UPLOAD_REGISTRY_ENTRY_NAME.equals(entryName)) {
		    continue;
		}

		out.add(source);
	    }

	    from += batch.size();

	    if (batch.size() < pageSize) {
		break;
	    }
	}

	return out;
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
    public void registerUpload(String prefix, String fileName, String owner) throws Exception {

	List<UploadRecord> uploads = readUploadRegistry();

	uploads.removeIf(u -> u.prefix().equals(prefix));
	uploads.add(new UploadRecord(prefix, fileName, ISO8601DateTimeUtils.getISO8601DateTime(), owner));

	writeUploadRegistry(uploads);
    }

    /**
     * @param prefix upload identifier
     * @return registry record when present
     */
    public Optional<UploadRecord> findUpload(String prefix) throws Exception {

	return readUploadRegistry().stream().filter(u -> u.prefix().equals(prefix)).findFirst();
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
	    out.add(new UploadRecord(object.optString("prefix"), object.optString("fileName"), object.optString("uploadedAt"),
		    object.optString("owner", "")));
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
	    object.put("owner", upload.owner() == null ? "" : upload.owner());
	    array.put(object);
	}

	root.put("uploads", array);
	storeUploadRegistry(root.toString());
    }

    /**
     * Upload metadata.
     */
    public record UploadRecord(String prefix, String fileName, String uploadedAt, String owner) {
    }
}
