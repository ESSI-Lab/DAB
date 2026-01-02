package eu.essi_lab.lib.xml;

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

/**
 * @author Fabrizio
 */
public enum NameSpace {

    /**
     * The GI-suite name space
     */
    GI_SUITE_DATA_MODEL(NameSpace.GS_DATA_MODEL_SCHEMA_URI, NameSpace.GI_SUITE_DATA_MODEL_SCHEMA_PREFIX);
  
    /**
     * 
     */
    public static final String NAMESPACE_PREFIX_MAPPER_IMPL = "com.sun.xml.bind.namespacePrefixMapper";

    /**
     * The GI-suite Data model schema version
     */
    public static final String GS_DATA_MODEL_SCHEMA_VERSION = "2007";
    /**
     * The GI-suite Data model schema prefix
     */
    public static final String GI_SUITE_DATA_MODEL_SCHEMA_PREFIX = "gs";
    /**
     * The GI-suite Data model schema URI
     */
    public static final String GS_DATA_MODEL_SCHEMA_URI = "http://flora.eu/gi-suite/1.0/dataModel/schema";

    /**
     * The media type of the GI-suite data model xml encoding
     */
    public static final String GS_DATA_MODEL_XML_MEDIA_TYPE = "application/gs-schema+xml";

    /**
     * The XML encoding name of the GI-suite data model
     */
    public static final String GS_DATA_MODEL_XML_ENCODING_NAME = "gs-schema-xml-enc";

    /**
     * The XML encoding version of the GI-suite data model
     */
    public static final String GS_DATA_MODEL_XML_ENCODING_VERSION = "1.0";

    /**
     * The GI-suite Data model schema name
     */
    public static final String GS_DATA_MODEL_SCHEMA_NAME = "GI-suite Data Model Schema";

    private String uri;
    private String prefix;

    private NameSpace(String uri, String prefix) {

	this.uri = uri;
	this.prefix = prefix;
    }

    public String getURI() {

	return uri;
    }

    public String getPrefix() {

	return prefix;
    }

    @Override
    public String toString() {

	return uri;
    }
}
