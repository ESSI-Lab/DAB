package eu.essi_lab.pdk.rsm;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.MediaType;

import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;

/**
 * A POJO which provides information about the schema used by a {@link DiscoveryResultSetMapper} to map
 * {@link GSResource}s. The schema is
 * also used by the {@link DiscoveryResultSetMapperFactory} to load one or more mappers which satisfy the given mapping
 * properties <br>
 * <li>Metadata elements and their encoding</li><br>
 * The schema <i>name and version</i> refer to the conceptual model which define the set of metadata elements used to
 * map a {@link
 * GSResource}. For example, a schema with name "ISO19115" and version "2003", indicates that the metadata elements
 * derive from the <i>ISO
 * Geographic information Standard 19115 version 2003.</i> In order to simplify the choice of a
 * {@link DiscoveryResultSetMapper} using the
 * {@link DiscoveryResultSetMapperFactory}, it is recommended to use when possible, well known names like in the
 * previous example.<br>
 * <br>
 * While a schema define an abstract dictionary, the <i>encoding name and version</i> define a way to implement that
 * dictionary on a
 * particular <i>media type</i>.<br>
 * In the example above, a mapper {@link Provider} can choose to implement the ISO 19115 dictionary using
 * the "ISO 19139:2007" which defines a standard XML encoding for ISO 19115; in this case the encoding name will be
 * "ISO19139", the version
 * "2007" and the media type "application/xml".<br>
 * The same provider (or other providers) can also define an own non standard XML encoding
 * (with the proper encoding name and version ) which best fits his needs.<br>
 * In another case, the ISO 19115 metadata elements can be
 * implemented on a JSON format content with the media type "application/json". The mapping of the ISO 19115 terms to
 * the JSON objects, can
 * be provided using different encoding names and/or versions. For example an encoding name "ISO115-JSON" version
 * "2017-1" could implement
 * the "fileIdentifier" element with a JSON object having the key "fileid", and another version "2017-2" with a JSON
 * object having key
 * "FileID".<br>
 * <br>
 * The schema mapping can also provide a human readable {@link #getDescription()} which can helps developers to choose a
 * proper mapper<br>
 * <br>
 * <li>Usage note</li><br>
 * None of the mapping schema properties are mandatory but more properties are defined, the easier it will identify the
 * right mapper using
 * the {@link DiscoveryResultSetMapperFactory}<br>
 * <br>
 * See {@link #getName()}, {@link #getVersion()}, {@link #getEncoding()}, {@link #getEncodingVersion()}, {@link
 * #getEncodingMediaType()}<br>
 * <br>
 *
 * @author Fabrizio
 * @see DiscoveryMessage#setResourceEncoding(ResourceFormat)
 */
public class MappingSchema {

    /**
     * The GI-suite Data Model mapping schema
     * <ul>
     * <li>schema name: {@link NameSpace#GS_DATA_MODEL_SCHEMA_NAME}</li>
     * <li>schema version: {@link NameSpace#GS_DATA_MODEL_SCHEMA_VERSION}</li>
     * <li>schema uri: {@link NameSpace#GS_DATA_MODEL_SCHEMA_URI}</li>
     * <li>encoding name: {@link #GS_DATA_MODEL_XML_ENCODING_NAME}</li>
     * <li>encoding version: {@link #GS_DATA_MODEL_XML_ENCODING_VERSION}</li>
     * <li>encoding media type: {@link NameSpace#GS_DATA_MODEL_XML_MEDIA_TYPE}</li>
     * </ul>
     */
    public static final MappingSchema GS_DATA_MODEL_MAPPING_SCHEMA = new MappingSchema();
    private String name;
    private String version;
    private MediaType mediaType;
    private String description;
    private String encoding;
    private String encodingVersion;
    private String uri;

    static {

	GS_DATA_MODEL_MAPPING_SCHEMA.setUri(NameSpace.GS_DATA_MODEL_SCHEMA_URI);

	GS_DATA_MODEL_MAPPING_SCHEMA.setName(NameSpace.GS_DATA_MODEL_SCHEMA_NAME);
	GS_DATA_MODEL_MAPPING_SCHEMA.setVersion(NameSpace.GS_DATA_MODEL_SCHEMA_VERSION);

	GS_DATA_MODEL_MAPPING_SCHEMA.setEncoding(NameSpace.GS_DATA_MODEL_XML_ENCODING_NAME);
	GS_DATA_MODEL_MAPPING_SCHEMA.setEncodingVersion(NameSpace.GS_DATA_MODEL_XML_ENCODING_VERSION);
	String gsType = NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;
	String[] split = gsType.split("/");
	MediaType mt = new MediaType(split[0], split[1]);
	GS_DATA_MODEL_MAPPING_SCHEMA.setEncodingMediaType(mt);
	System.out.println();
    }

    public String getUri() {
	return uri;
    }

    public void setUri(String uri) {
	this.uri = uri;
    }

    /**
     * @return
     */
    public String getName() {
	return name;
    }

    /**
     * @param schemaName
     */
    public void setName(String schemaName) {
	this.name = schemaName;
    }

    /**
     * @return
     */
    public String getVersion() {
	return version;
    }

    /**
     * @param schemaVersion
     */
    public void setVersion(String schemaVersion) {
	this.version = schemaVersion;
    }

    /**
     * @return
     */
    public String getEncoding() {
	return encoding;
    }

    /**
     * @param encoding
     */
    public void setEncoding(String encoding) {
	this.encoding = encoding;
    }

    /**
     * @return
     */
    public String getEncodingVersion() {
	return encodingVersion;
    }

    /**
     * @param encodingVersion
     */
    public void setEncodingVersion(String encodingVersion) {
	this.encodingVersion = encodingVersion;
    }

    /**
     * @return
     */
    public MediaType getEncodingMediaType() {
	return mediaType;
    }

    /**
     * @param mediaType
     */
    public void setEncodingMediaType(MediaType mediaType) {
	this.mediaType = mediaType;
    }

    /**
     * Get a human readable description of this mapping schema
     *
     * @return
     */
    public String getDescription() {
	return description;
    }

    /**
     * Set a human readable description of this mapping schema
     *
     * @param description
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     *
     */
    @Override
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof MappingSchema))
	    return false;

	MappingSchema enc = (MappingSchema) object;

	return ((this.getVersion() == null && enc.getVersion() == null)
		|| (this.getVersion() != null && enc.getVersion() != null && this.getVersion().equals(enc.getVersion())))

		&& ((this.getName() == null && enc.getName() == null)
			|| (this.getName() != null && enc.getName() != null && this.getName().equals(enc.getName())))

		&& ((this.getEncoding() == null && enc.getEncoding() == null)
			|| (this.getEncoding() != null && enc.getEncoding() != null && this.getEncoding().equals(enc.getEncoding())))

		&& ((this.getEncodingVersion() == null && enc.getEncodingVersion() == null) || (this.getEncodingVersion() != null
			&& enc.getEncodingVersion() != null && this.getEncodingVersion().equals(enc.getEncodingVersion())))

		&& ((this.getEncodingMediaType() == null && enc.getEncodingMediaType() == null) || (this.getEncodingMediaType() != null
			&& enc.getEncodingMediaType() != null && this.getEncodingMediaType().equals(enc.getEncodingMediaType())))

		&& ((this.getUri() == null && enc.getUri() == null)
			|| (this.getUri() != null && enc.getUri() != null && this.getUri().equals(enc.getUri())));

    }

}
