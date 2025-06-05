/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.CacheMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ConfigurationMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.MetaFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.model.auth.UserIdentifierType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class SourceWrapper {

    private JSONObject source;

    /**
     * @param source
     */
    public SourceWrapper(JSONObject source) {

	this.source = source;
    }

    /**
     * - corresponds to the '_index' property
     * - this property is not indexed with the source, to avoid field duplication, instead it is
     * copied from the response to the source
     */
    public String getIndex() {

	return source.getString(IndexData.INDEX);
    }

    /**
     * - corresponds to the '_id' property
     * - see {@link OpenSearchFolder#getEntryId(DatabaseFolder, String)}
     * - this property is not indexed with the source, to avoid field duplication, instead it is
     * copied from the response to the source
     */
    public String getEntryId() {

	return source.getString(IndexData.ENTRY_ID);
    }

    /**
     * - the name (key) of the stored entry
     */
    public String getEntryName() {

	return source.getString(IndexData.ENTRY_NAME);
    }

    /**
     * - see {@link DatabaseFolder#getName()}
     */
    public String getFolderName() {

	return source.getString(IndexData.FOLDER_NAME);
    }

    /**
     * - see {@link OpenSearchFolder#getFolderId(eu.essi_lab.api.database.DatabaseFolder)}
     */
    public String getFolderId() {

	return source.getString(IndexData.FOLDER_ID);
    }

    /**
     * - see {@link Database#getIdentifier()}
     */
    public String getDatabaseId() {

	return source.getString(IndexData.DATABASE_ID);
    }

    /**
     * - this property indicates where (under which property) the binary is stored
     */
    public String getBinaryProperty() {

	return source.getString(IndexData.BINARY_PROPERTY);
    }

    /**
     * - uses {@link #getBinaryProperty()} to retrieve the binary value
     */
    public String getBinaryValue() {

	return source.getString(source.getString(IndexData.BINARY_PROPERTY));
    }

    /**
     * - possible values: see {@link DataType}
     */
    public DataType getDataType() {

	return DataType.decode(source.getString(IndexData.DATA_TYPE));
    }

    /**
     * - meta-folder-index property
     */
    public Optional<String> getSourceId() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.SOURCE_ID, null));
    }

    /**
     * - 'meta-folder-index' property<br>
     * - possible values: 'data-1', 'data-2'
     */
    public Optional<String> getDataFolder() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.DATA_FOLDER, null));
    }

    /**
     * - 'meta-folder-index' property<br>
     * - base64 encoded<br>
     * - the index doc storage is not strictly required, since the value of the data folder is
     * indexed by the 'dataFolder' property (see #getDataFolder())
     */
    public Optional<String> getIndexDoc() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.INDEX_DOC, null));
    }

    /**
     * - 'meta-folder-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getHarvestingProperties() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.HARVESTING_PROPERTIES, null));
    }

    /**
     * - 'meta-folder-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getErrorsReport() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.ERRORS_REPORT, null));
    }

    /**
     * - 'meta-folder-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getWarnReport() {

	return Optional.ofNullable(source.optString(MetaFolderMapping.WARN_REPORT, null));
    }

    /**
     * - 'users-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getUser() {

	return Optional.ofNullable(source.optString(UsersMapping.USER, null));
    }

    /**
     * - 'users-index' property
     */
    public Optional<String> getUserIdentifier() {

	return Optional.ofNullable(source.optString(UsersMapping.USER_ID, null));
    }

    /**
     * - 'users-index' property
     */
    public Optional<UserIdentifierType> getUserIdentifierType() {

	String optType = source.optString(UsersMapping.USER_ID_TYPE, null);
	if (optType != null) {

	    return UserIdentifierType.fromType(optType);
	}

	return Optional.empty();
    }

    /**
     * - 'users-index' property
     */
    public Optional<String> getUserRole() {

	return Optional.ofNullable(source.optString(UsersMapping.USER_ROLE, null));
    }

    /**
     * - 'users-index' property<
     */
    public Optional<Boolean> getUserEnabled() {

	return Optional.ofNullable(source.optBooleanObject(UsersMapping.ENABLED, null));
    }

    /**
     * - 'views-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getView() {

	return Optional.ofNullable(source.optString(ViewsMapping.VIEW, null));
    }

    /**
     * - 'views-index' property
     */
    public Optional<String> getViewId() {

	return Optional.ofNullable(source.optString(ViewsMapping.VIEW_ID, null));
    }

    /**
     * - 'views-index' property
     */
    public Optional<String> getViewLabel() {

	return Optional.ofNullable(source.optString(ViewsMapping.VIEW_LABEL, null));
    }

    /**
     * - 'views-index' property
     */
    public Optional<String> getViewOwner() {

	return Optional.ofNullable(source.optString(ViewsMapping.VIEW_OWNER, null));
    }

    /**
     * - 'views-index' property
     */
    public Optional<String> getViewCreator() {

	return Optional.ofNullable(source.optString(ViewsMapping.VIEW_CREATOR, null));
    }

    /**
     * - 'views-index' property
     */
    public Optional<ViewVisibility> getViewVisibility() {

	String visibility = source.optString(ViewsMapping.VIEW_VISIBILITY, null);
	if (visibility != null) {

	    return Optional.of(ViewVisibility.fromName(visibility));
	}

	return Optional.empty();
    }

    /**
     * - 'augmenters-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getAugmenterProperties() {

	return Optional.ofNullable(source.optString(AugmentersMapping.AUGMENTER_PROPERTIES, null));
    }

    /**
     * - 'data-folder-index' property<br>
     */
    public boolean hasWritingFolderTag() {

	return source.has(DataFolderMapping.WRITING_FOLDER_TAG);
    }

    /**
     * - 'data-folder-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getGSResource() {

	return Optional.ofNullable(source.optString(DataFolderMapping.GS_RESOURCE, null));
    }

    /**
     * - 'data-folder-index' property
     */
    public List<String> getGSResourceProperties(MetadataElement el) {

	return getGSResourceProperties(el.getName());
    }

    /**
     * - 'data-folder-index' property
     */
    public List<String> getGSResourceProperties(ResourceProperty rp) {

	return getGSResourceProperties(rp.getName());
    }

    /**
     * - 'data-folder-index' property
     */
    public List<String> getGSResourceProperties(String property) {

	if (source.has(property)) {

	    Object object = source.get(property);

	    if (object instanceof JSONArray) {

		return ((JSONArray) object).toList().stream().map(v -> v.toString()).collect(Collectors.toList());
	    }

	    return Arrays.asList(object.toString());
	}

	return new ArrayList<String>();
    }

    /**
     * - 'configuration-index' property<br>
     * - base64 encoded
     */
    public Optional<String> getConfiguration() {

	return Optional.ofNullable(source.optString(ConfigurationMapping.CONFIGURATION, null));
    }

    /**
     * - 'configuration-index' property
     */
    public Optional<String> getConfigurationName() {

	return Optional.ofNullable(source.optString(ConfigurationMapping.CONFIGURATION_NAME, null));
    }

    /**
     * - 'configuration-index' property
     * - base64 encoded
     */
    public Optional<String> getConfigurationLock() {

	return Optional.ofNullable(source.optString(ConfigurationMapping.CONFIGURATION_LOCK, null));
    }

    /**
     * - 'cache-index' property
     * - base64 encoded
     */
    public Optional<String> getCachedEntry() {

	return Optional.ofNullable(source.optString(CacheMapping.CACHED_ENTRY, null));
    }

    /**
     * @param hideBinary
     * @return
     */
    public String toStringHideBinary() {

	JSONObject clone = new JSONObject(source.toString());
	clone.remove(getBinaryProperty());

	return clone.toString(3);
    }

    @Override
    public String toString() {

	return source.toString(3);
    }

    /**
     * @return
     */
    public JSONObject getSource() {

	return source;
    }

}
