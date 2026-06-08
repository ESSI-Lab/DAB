package eu.essi_lab.gssrv.rest;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.opensearch.shape.OpenSearchShapefileClient;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.gssrv.servlet.wmscache.WMSCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.spatial.ShapeEntryPrefix;

/**
 * Lists, uploads, deletes and harvests predefined search area shapefiles.
 */
public class PredefinedShapeManagementService {

    /**
     * @param actorOwner owner id of the current user
     * @param actorIsAdmin whether the current user is an administrator
     * @return JSON payload for the management panel (selection status is computed client-side via WMS GetCapabilities)
     */
    public JSONObject listAreas(String actorOwner, boolean actorIsAdmin) throws Exception {

	OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	List<String> entryNames = client.listEntryNames().stream()//
		.filter(name -> !PredefinedShapeRegistry.REGISTRY_ENTRY_NAME.equals(name))//
		.collect(Collectors.toList());

	PredefinedShapeRegistry registry = new PredefinedShapeRegistry();
	List<OpenSearchShapefileClient.UploadRecord> uploads = registry.readUploads();

	Set<String> knownPrefixes = uploads.stream().map(OpenSearchShapefileClient.UploadRecord::prefix).collect(Collectors.toSet());

	Map<String, List<String>> legacyGroups = groupLegacyEntries(entryNames, knownPrefixes);

	JSONArray areas = new JSONArray();

	for (OpenSearchShapefileClient.UploadRecord upload : uploads) {

	    if (!PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, upload.owner())) {
		continue;
	    }

	    List<String> features = entryNames.stream()//
		    .filter(name -> name.equals(upload.prefix()) || name.startsWith(upload.prefix() + "_"))//
		    .collect(Collectors.toList());

	    areas.put(toAreaJson(upload.prefix(), upload.fileName(), upload.uploadedAt(), upload.owner(), features, false));
	}

	if (actorIsAdmin) {

	    for (Map.Entry<String, List<String>> legacy : legacyGroups.entrySet()) {

		areas.put(toAreaJson(legacy.getKey(), "", "", "", legacy.getValue(), true));
	    }
	}

	JSONObject out = new JSONObject();
	out.put("areas", areas);
	out.put("shapeSourceId", ConfigurationWrapper.getShapeSourceId().orElse(""));

	return out;
    }

    /**
     * @param originalFileName
     * @param explicitShapeId
     * @param zipStream
     * @param owner uploader id ({@link PredefinedShapeAccess#ADMIN_OWNER} for administrators)
     * @param actorIsAdmin whether the current user is an administrator
     * @return upload outcome
     */
    public PredefinedShapeUploadService.UploadOutcome upload(String originalFileName, String explicitShapeId, InputStream zipStream,
	    String owner, boolean actorIsAdmin) {

	if (originalFileName == null || originalFileName.isBlank()) {

	    return PredefinedShapeUploadService.UploadOutcome.failure("Missing file name");
	}

	if (!originalFileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {

	    return PredefinedShapeUploadService.UploadOutcome.failure("Only zipped shapefiles (.zip) are supported");
	}

	if (owner == null || owner.isBlank()) {

	    return PredefinedShapeUploadService.UploadOutcome.failure("Missing owner");
	}

	String prefix;
	try {
	    prefix = ShapeEntryPrefix.resolve(explicitShapeId, originalFileName);
	} catch (IllegalArgumentException ex) {
	    return PredefinedShapeUploadService.UploadOutcome.failure(ex.getMessage());
	}

	try {

	    PredefinedShapeDeleteResult replaceCheck = deleteByPrefix(prefix, owner, actorIsAdmin, false);

	    if (!replaceCheck.isSuccess()) {

		if (replaceCheck.isForbidden()) {
		    return PredefinedShapeUploadService.UploadOutcome.forbidden(replaceCheck.getErrorMessage().orElse(
			    PredefinedShapeAccess.FORBIDDEN_MESSAGE));
		}
		return PredefinedShapeUploadService.UploadOutcome.failure(
			replaceCheck.getErrorMessage().orElse("Unable to replace existing shape area"));
	    }

	    OpenSearchDatabase database = new OpenSearchDatabase();
	    database.initialize(ConfigurationWrapper.getStorageInfo());
	    OpenSearchFolder folder = new OpenSearchFolder(database, OpenSearchDatabase.SHAPE_FILES_FOLDER);

	    boolean stored = folder.storeShapeFile(prefix, FolderEntry.of(zipStream), owner);

	    if (!stored) {

		return PredefinedShapeUploadService.UploadOutcome.failure("Unable to store shapefile in OpenSearch");
	    }

	    PredefinedShapeRegistry registry = new PredefinedShapeRegistry();
	    registry.registerUpload(prefix, originalFileName, owner);

	    GSLoggerFactory.getLogger(getClass()).info("Stored predefined shape zip {} with prefix {} for owner {}", originalFileName,
		    prefix, owner);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    return PredefinedShapeUploadService.UploadOutcome.failure("Error storing shapefile: " + ex.getMessage());
	}

	return PredefinedShapeUploadService.UploadOutcome.success(prefix);
    }

    /**
     * @param prefix
     * @param actorOwner owner id of the current user
     * @param actorIsAdmin whether the current user is an administrator
     * @return deletion result
     */
    public PredefinedShapeDeleteResult deleteByPrefix(String prefix, String actorOwner, boolean actorIsAdmin) {

	return deleteByPrefix(prefix, actorOwner, actorIsAdmin, true);
    }

    private PredefinedShapeDeleteResult deleteByPrefix(String prefix, String actorOwner, boolean actorIsAdmin,
	    boolean enforceWhenMissing) {

	try {

	    if (prefix == null || prefix.isBlank()) {

		return PredefinedShapeDeleteResult.failure("Missing shape identifier");
	    }

	    PredefinedShapeRegistry registry = new PredefinedShapeRegistry();
	    Optional<OpenSearchShapefileClient.UploadRecord> existing = registry.findUpload(prefix);

	    if (existing.isPresent()) {

		if (!PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, existing.get().owner())) {
		    return PredefinedShapeDeleteResult.forbidden(PredefinedShapeAccess.FORBIDDEN_MESSAGE);
		}

	    } else {

		OpenSearchShapefileClient client = new OpenSearchShapefileClient();
		Optional<String> featureOwner = readOwnerFromFeatures(client, prefix);

		if (featureOwner.isPresent()
			&& !PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, featureOwner.get())) {
		    return PredefinedShapeDeleteResult.forbidden(PredefinedShapeAccess.FORBIDDEN_MESSAGE);
		}

		if (enforceWhenMissing && featureOwner.isEmpty()) {
		    String prefixMarker = prefix + "_";
		    boolean hasFeatures = client.listEntryNames().stream()//
			    .anyMatch(name -> name.equals(prefix) || name.startsWith(prefixMarker));

		    if (hasFeatures && !actorIsAdmin) {
			return PredefinedShapeDeleteResult.forbidden(PredefinedShapeAccess.FORBIDDEN_MESSAGE);
		    }
		}
	    }

	    OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	    String prefixMarker = prefix + "_";

	    List<String> toRemove = client.listEntryNames().stream()//
		    .filter(name -> name.equals(prefix) || name.startsWith(prefixMarker))//
		    .filter(name -> !PredefinedShapeRegistry.REGISTRY_ENTRY_NAME.equals(name))//
		    .collect(Collectors.toList());

	    if (!toRemove.isEmpty()) {

		client.getFolder().remove(toRemove);
		WMSCache.invalidatePredefinedShapeEntries(toRemove);
	    }

	    registry.unregisterUpload(prefix);

	    GSLoggerFactory.getLogger(getClass()).info("Deleted predefined shape prefix {} ({} features)", prefix, toRemove.size());

	    return PredefinedShapeDeleteResult.ok();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    return PredefinedShapeDeleteResult.failure("Error deleting shapes: " + ex.getMessage());
	}
    }

    /**
     * @return error message when harvest could not be scheduled
     */
    public Optional<String> triggerHarvest() {

	Optional<String> sourceId = ConfigurationWrapper.getShapeSourceId();

	if (sourceId.isEmpty()) {

	    return Optional.of("Shape source id is not configured (system key-value option shape-source-id)");
	}

	return ShapeHarvestScheduler.scheduleHarvestNow(sourceId.get());
    }

    private JSONObject toAreaJson(String prefix, String fileName, String uploadedAt, String owner, List<String> featureNames,
	    boolean legacy) {

	JSONObject area = new JSONObject();
	area.put("prefix", prefix);
	area.put("fileName", fileName == null ? "" : fileName);
	area.put("uploadedAt", uploadedAt == null ? "" : uploadedAt);
	area.put("owner", owner == null ? "" : owner);
	area.put("featureCount", featureNames.size());
	area.put("legacy", legacy);

	JSONArray entryNames = new JSONArray();
	for (String featureName : featureNames) {
	    entryNames.put(featureName);
	}
	area.put("entryNames", entryNames);

	return area;
    }

    private Optional<String> readOwnerFromFeatures(OpenSearchShapefileClient client, String prefix) throws Exception {

	String prefixMarker = prefix + "_";

	for (String name : client.listEntryNames()) {

	    if (!name.equals(prefix) && !name.startsWith(prefixMarker)) {
		continue;
	    }

	    Optional<org.json.JSONObject> source = client.getShapeSource(name);
	    if (source.isEmpty()) {
		continue;
	    }

	    String owner = source.get().optString(ShapeFileMapping.OWNER, "");
	    if (!owner.isBlank()) {
		return Optional.of(owner);
	    }
	}

	return Optional.empty();
    }

    private Map<String, List<String>> groupLegacyEntries(List<String> entryNames, Set<String> knownPrefixes) {

	Map<String, List<String>> legacy = new HashMap<>();

	for (String name : entryNames) {

	    boolean matched = knownPrefixes.stream().anyMatch(p -> name.equals(p) || name.startsWith(p + "_"));

	    if (matched) {
		continue;
	    }

	    int idx = name.indexOf('_');
	    String inferredPrefix = idx > 0 ? name.substring(0, idx) : name;

	    legacy.computeIfAbsent(inferredPrefix, k -> new ArrayList<>()).add(name);
	}

	return legacy;
    }
}
