package eu.essi_lab.pdk.rsf;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.ResultSet;

/**
 * A POJO which provides information about the encoding used by a {@link ResultSetFormetter} to formats a {@link ResultSet} of
 * <code>String</code>s. The encoding is also used by the {@link ResultSetFormtterFactory} to load one or more formatters which satisfy the
 * given formatting properties
 *
 * @author Fabrizio
 */
public class FormattingEncoding {

    private MediaType mediaType;
    private String description;
    private String encoding;
    private String encodingVersion;

    /**
     * @return
     */
    public MediaType getMediaType() {
	return mediaType;
    }

    /**
     * @param mediaType
     */
    public void setMediaType(MediaType mediaType) {
	this.mediaType = mediaType;
    }

    /**
     * @return
     */
    public String getDescription() {
	return description;
    }

    /**
     *
     */
    public void setDescription(String description) {
	this.description = description;
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

    @Override
    public boolean equals(Object object) {

	if (object == null)
	    return false;

	if (!(object instanceof FormattingEncoding))
	    return false;

	FormattingEncoding enc = (FormattingEncoding) object;

	return ((this.getEncoding() == null && enc.getEncoding() == null) || (this.getEncoding() != null && enc.getEncoding() != null
		&& this.getEncoding().equals(enc.getEncoding())))

		&& ((this.getEncodingVersion() == null && enc.getEncodingVersion() == null) || (this.getEncodingVersion() != null
		&& enc.getEncodingVersion() != null && this.getEncodingVersion().equals(enc.getEncodingVersion())))

		&& ((this.getMediaType() == null && enc.getMediaType() == null) || (this.getMediaType() != null
		&& enc.getMediaType() != null && this.getMediaType().equals(enc.getMediaType())));
    }

}
