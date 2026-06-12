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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import eu.essi_lab.api.database.opensearch.index.IndexData;
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
	JSONArray entries = new JSONArray();

	for (JSONObject source : client.loadPredefinedLayerSources()) {

	    String identifier = source.optString(IndexData.ENTRY_NAME, "");

	    if (identifier.isBlank()) {
		continue;
	    }

	    String owner = source.optString(ShapeFileMapping.OWNER, "");

	    if (!PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, owner)) {
		continue;
	    }

	    String title = source.optString(ShapeFileMapping.ENTRY_TITLE, "");
	    String name = title.isBlank() ? identifier : title;

	    JSONObject entry = new JSONObject();
	    entry.put("identifier", identifier);
	    entry.put("name", name);
	    entry.put("group", source.optString(ShapeFileMapping.SHAPE_GROUP, ""));
	    entry.put("owner", owner);
	    entry.put("legacy", owner.isBlank());

	    entries.put(entry);
	}

	JSONObject out = new JSONObject();
	out.put("areas", new JSONArray());
	out.put("entries", entries);
	out.put("shapeSourceId", ConfigurationWrapper.getShapeSourceId().orElse(""));

	return out;
    }

    /**
     * @param originalFileName
     * @param explicitShapeId
     * @param zipStream
     * @param group optional user-defined group label; defaults to zip base name when blank
     * @param owner uploader id ({@link PredefinedShapeAccess#ADMIN_OWNER} for administrators)
     * @param actorIsAdmin whether the current user is an administrator
     * @return upload outcome
     */
    public PredefinedShapeUploadService.UploadOutcome upload(String originalFileName, String explicitShapeId, InputStream zipStream,
	    String group, String owner, boolean actorIsAdmin) {

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

	    PredefinedShapeDeleteResult replaceCheck = deleteByPrefix(prefix, owner, actorIsAdmin, false, null);

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

	    String resolvedGroup = resolveGroupName(group, originalFileName);

	    boolean stored = folder.storeShapeFile(prefix, FolderEntry.of(zipStream), owner, resolvedGroup);

	    if (!stored) {

		return PredefinedShapeUploadService.UploadOutcome.failure("Unable to store shapefile in OpenSearch");
	    }

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

	return deleteByPrefix(prefix, actorOwner, actorIsAdmin, null);
    }

    /**
     * @param shapeView WMS view id for tile cache invalidation (from portal {@code config.shapeView})
     */
    public PredefinedShapeDeleteResult deleteByPrefix(String prefix, String actorOwner, boolean actorIsAdmin, String shapeView) {

	return deleteByPrefix(prefix, actorOwner, actorIsAdmin, true, shapeView);
    }

    /**
     * @param identifiers shape entry names to remove
     * @param actorOwner owner id of the current user
     * @param actorIsAdmin whether the current user is an administrator
     * @return deletion result
     */
    public PredefinedShapeDeleteResult deleteByIdentifiers(List<String> identifiers, String actorOwner, boolean actorIsAdmin) {

	return deleteByIdentifiers(identifiers, actorOwner, actorIsAdmin, null);
    }

    /**
     * @param shapeView WMS view id for tile cache invalidation (from portal {@code config.shapeView})
     */
    public PredefinedShapeDeleteResult deleteByIdentifiers(List<String> identifiers, String actorOwner, boolean actorIsAdmin,
	    String shapeView) {

	if (identifiers == null || identifiers.isEmpty()) {

	    return PredefinedShapeDeleteResult.failure("No shape entries selected");
	}

	List<String> uniqueIds = identifiers.stream()//
		.filter(id -> id != null && !id.isBlank())//
		.distinct()//
		.collect(Collectors.toList());

	if (uniqueIds.isEmpty()) {

	    return PredefinedShapeDeleteResult.failure("No shape entries selected");
	}

	try {

	    OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	    Set<String> existingNames = new HashSet<>(client.listEntryNames());
	    Map<String, String> owners = client.loadEntryOwners();

	    for (String identifier : uniqueIds) {

		if (!existingNames.contains(identifier)) {

		    return PredefinedShapeDeleteResult.failure("Shape entry not found: " + identifier);
		}

		if (PredefinedShapeRegistry.REGISTRY_ENTRY_NAME.equals(identifier)) {

		    return PredefinedShapeDeleteResult.failure("Invalid shape entry");
		}

		String resourceOwner = owners.getOrDefault(identifier, "");

		if (!PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, resourceOwner)) {

		    return PredefinedShapeDeleteResult.forbidden(PredefinedShapeAccess.FORBIDDEN_MESSAGE);
		}
	    }

	    client.getFolder().remove(uniqueIds);
	    WMSCache.invalidatePredefinedShapeEntries(uniqueIds, shapeView);
	    cleanupEmptyUploads(client);

	    GSLoggerFactory.getLogger(getClass()).info("Deleted {} predefined shape entries", uniqueIds.size());

	    return PredefinedShapeDeleteResult.ok();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    return PredefinedShapeDeleteResult.failure("Error deleting shapes: " + ex.getMessage());
	}
    }

    /**
     * @param currentIdentifier existing entry name
     * @param newIdentifier updated entry name, or blank to keep current
     * @param newName updated display name, or blank to keep current
     * @param newGroup updated group label; {@code null} keeps current
     * @param newOwner updated owner id; only applied when {@code actorIsAdmin} is {@code true}
     * @param actorOwner owner id of the current user
     * @param actorIsAdmin whether the current user is an administrator
     * @return update result
     */
    public PredefinedShapeDeleteResult updateEntry(String currentIdentifier, String newIdentifier, String newName, String newGroup,
	    String newOwner, String actorOwner, boolean actorIsAdmin) {

	return updateEntry(currentIdentifier, newIdentifier, newName, newGroup, newOwner, actorOwner, actorIsAdmin, null);
    }

    /**
     * @param shapeView WMS view id for tile cache invalidation (from portal {@code config.shapeView})
     */
    public PredefinedShapeDeleteResult updateEntry(String currentIdentifier, String newIdentifier, String newName, String newGroup,
	    String newOwner, String actorOwner, boolean actorIsAdmin, String shapeView) {

	if (currentIdentifier == null || currentIdentifier.isBlank()) {

	    return PredefinedShapeDeleteResult.failure("Missing shape entry identifier");
	}

	if (PredefinedShapeRegistry.REGISTRY_ENTRY_NAME.equals(currentIdentifier)) {

	    return PredefinedShapeDeleteResult.failure("Invalid shape entry");
	}

	try {

	    OpenSearchShapefileClient client = new OpenSearchShapefileClient();
	    OpenSearchFolder folder = client.getFolder();

	    if (!client.listEntryNames().contains(currentIdentifier)) {

		return PredefinedShapeDeleteResult.failure("Shape entry not found: " + currentIdentifier);
	    }

	    Optional<JSONObject> metadataOpt = client.getShapeMetadata(currentIdentifier);

	    if (metadataOpt.isEmpty()) {

		return PredefinedShapeDeleteResult.failure("Shape entry not found: " + currentIdentifier);
	    }

	    JSONObject metadata = metadataOpt.get();
	    String resourceOwner = metadata.optString(ShapeFileMapping.OWNER, "");

	    if (!PredefinedShapeAccess.canManage(actorOwner, actorIsAdmin, resourceOwner)) {

		return PredefinedShapeDeleteResult.forbidden(PredefinedShapeAccess.FORBIDDEN_MESSAGE);
	    }

	    String updatedIdentifier = currentIdentifier;
	    if (newIdentifier != null && !newIdentifier.isBlank()) {

		try {
		    String sanitized = ShapeEntryPrefix.sanitize(newIdentifier);
		    if (!sanitized.equals(currentIdentifier)) {

			if (client.listEntryNames().contains(sanitized)) {

			    return PredefinedShapeDeleteResult.failure("Identifier already in use: " + sanitized);
			}

			updatedIdentifier = sanitized;
		    }
		} catch (IllegalArgumentException ex) {
		    return PredefinedShapeDeleteResult.failure(ex.getMessage());
		}
	    }

	    String currentTitle = metadata.optString(ShapeFileMapping.ENTRY_TITLE, currentIdentifier);
	    String updatedName = currentTitle;

	    if (newName != null && !newName.isBlank()) {

		updatedName = newName.trim();
	    }

	    String currentGroup = metadata.optString(ShapeFileMapping.SHAPE_GROUP, "");
	    String updatedGroup = currentGroup;

	    if (newGroup != null) {

		updatedGroup = newGroup.trim();
	    }

	    String currentOwner = metadata.optString(ShapeFileMapping.OWNER, "");
	    String updatedOwner = currentOwner;

	    if (actorIsAdmin && newOwner != null) {

		updatedOwner = newOwner.trim();
	    }

	    if (updatedIdentifier.equals(currentIdentifier) && updatedName.equals(currentTitle)
		    && updatedGroup.equals(currentGroup) && updatedOwner.equals(currentOwner)) {

		return PredefinedShapeDeleteResult.failure("No changes to save");
	    }

	    List<String> cacheInvalidations = new ArrayList<>();
	    cacheInvalidations.add(currentIdentifier);

	    if (updatedIdentifier.equals(currentIdentifier)) {

		Map<String, Object> patch = new LinkedHashMap<>();

		if (!updatedName.equals(currentTitle)) {
		    patch.put(ShapeFileMapping.ENTRY_TITLE, updatedName);
		}

		if (!updatedGroup.equals(currentGroup)) {
		    patch.put(ShapeFileMapping.SHAPE_GROUP, updatedGroup);
		}

		if (!updatedOwner.equals(currentOwner)) {
		    patch.put(ShapeFileMapping.OWNER, updatedOwner);
		}

		if (!client.patchShapeFields(currentIdentifier, patch)) {

		    return PredefinedShapeDeleteResult.failure("Error updating shape entry metadata");
		}

	    } else {

		Optional<JSONObject> sourceOpt = client.getShapeSourceForRename(currentIdentifier);

		if (sourceOpt.isEmpty()) {

		    return PredefinedShapeDeleteResult.failure("Shape entry not found: " + currentIdentifier);
		}

		JSONObject source = new JSONObject(sourceOpt.get().toString());
		source.put(IndexData.ENTRY_NAME, updatedIdentifier);
		source.put(ShapeFileMapping.ENTRY_TITLE, updatedName);
		source.put(ShapeFileMapping.SHAPE_GROUP, updatedGroup);
		source.put(ShapeFileMapping.OWNER, updatedOwner);

		IndexData indexData = IndexData.fromShapeEntrySource(folder, source, updatedIdentifier);
		folder.getWrapper().storeWithOpenSearchClient(indexData);
		folder.getWrapper().synch();
		folder.remove(currentIdentifier);
		cacheInvalidations.add(updatedIdentifier);
	    }

	    WMSCache.invalidatePredefinedShapeEntries(cacheInvalidations, shapeView);

	    GSLoggerFactory.getLogger(getClass()).info("Updated predefined shape entry {} -> {} (title: {})", currentIdentifier,
		    updatedIdentifier, updatedName);

	    return PredefinedShapeDeleteResult.ok();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    return PredefinedShapeDeleteResult.failure("Error updating shape entry: " + ex.getMessage());
	}
    }

    private String resolveGroupName(String group, String originalFileName) {

	if (group != null && !group.isBlank()) {
	    return group.trim();
	}

	String base = originalFileName;
	int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));

	if (slash >= 0) {
	    base = base.substring(slash + 1);
	}

	if (base.toLowerCase(Locale.ROOT).endsWith(".zip")) {
	    base = base.substring(0, base.length() - 4);
	}

	return base.trim();
    }

    private PredefinedShapeDeleteResult deleteByPrefix(String prefix, String actorOwner, boolean actorIsAdmin,
	    boolean enforceWhenMissing, String shapeView) {

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
		WMSCache.invalidatePredefinedShapeEntries(toRemove, shapeView);
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

    private void cleanupEmptyUploads(OpenSearchShapefileClient client) throws Exception {

	PredefinedShapeRegistry registry = new PredefinedShapeRegistry();
	Set<String> remaining = new HashSet<>(client.listEntryNames());
	remaining.remove(PredefinedShapeRegistry.REGISTRY_ENTRY_NAME);

	for (OpenSearchShapefileClient.UploadRecord upload : registry.readUploads()) {

	    String prefix = upload.prefix();
	    String prefixMarker = prefix + "_";

	    boolean hasRemaining = remaining.stream()//
		    .anyMatch(name -> name.equals(prefix) || name.startsWith(prefixMarker));

	    if (!hasRemaining) {

		registry.unregisterUpload(prefix);
	    }
	}
    }
}
