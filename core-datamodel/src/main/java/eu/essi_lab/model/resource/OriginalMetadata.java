package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringEscapeUtils;

import eu.essi_lab.iso.datamodel.CDATA_Adapter;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;

/**
 * An original metadata set obtained by a {@link GSSource}
 * 
 * @author Fabrizio
 */
public class OriginalMetadata {

    @NotNull(message = "schemeURI field of OriginalMetadata cannot be null")
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String schemeURI;

    @NotNull(message = "metadata field of OriginalMetadata cannot be null")
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    @XmlJavaTypeAdapter(value = CDATA_Adapter.class)
    private String metadata;

    @XmlTransient
    private GSPropertyHandler additionalInfo;

    public OriginalMetadata() {
    }

    /**
     * @return
     */
    public GSPropertyHandler getAdditionalInfo() {

	return additionalInfo;
    }

    /**
     * @param additionalInfo
     */
    public void setAdditionalInfo(GSPropertyHandler additionalInfo) {

	this.additionalInfo = additionalInfo;
    }

    @XmlTransient
    public String getSchemeURI() {

	return schemeURI;
    }

    public void setSchemeURI(String schemeURI) {

	this.schemeURI = schemeURI;
    }

    /**
     * Returns a copy of the private field <code>metadata</code> with the XML entities unescaped using the
     * {@link StringEscapeUtils#unescapeXml(String)} method
     */
    @XmlTransient
    public String getMetadata() {

	return StringEscapeUtils.unescapeXml(metadata);
    }

    /**
     * The {@link StringEscapeUtils#escapeXml10(String)} method is applied to the given
     * <code>metadata</code> in order to avoid unmarshalling issues (the {@link OriginalMetadata} string is enclosed in
     * a CDATA section by the {@link CDATA_Adapter})
     * 
     * @see OriginalMetadata#getMetadata(boolean)
     * @param metadata
     */
    public void setMetadata(String metadata) {

	this.metadata = StringEscapeUtils.escapeXml10(metadata);
    }

    @Override
    public String toString() {

	return metadata;
    }
}
