package eu.essi_lab.pdk.rsm.impl.xml.iso19139;

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

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class GMD_2007_ResultSetMapper extends GMD_ResultSetMapper {

    /**
     * The schema uri of {@link #GMD_2007_MAPPING_SCHEMA}
     */
    public static final String GMD_2007_SCHEMA_URI = CommonNameSpaceContext.GMD_NS_URI;

    /**
     * The schema name of {@link #GMD_2007_MAPPING_SCHEMA}
     */
    public static final String GMD_2007_SCHEMA_NAME = "GMD";

    /**
     * The schema version of {@link #GMD_2007_MAPPING_SCHEMA}
     */
    public static final String GMD_2007_SCHEMA_VERSION = "2007";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema GMD_2007_MAPPING_SCHEMA = new MappingSchema();

    static {
	GMD_2007_MAPPING_SCHEMA.setUri(GMD_2007_SCHEMA_URI);
	GMD_2007_MAPPING_SCHEMA.setName(GMD_2007_SCHEMA_NAME);
	GMD_2007_MAPPING_SCHEMA.setVersion(GMD_2007_SCHEMA_VERSION);
    }

    public GMD_2007_ResultSetMapper(ElementSetType setType) {
	super(setType);
    }

    public GMD_2007_ResultSetMapper(List<QName> elementNames) {
	super(elementNames);
    }

    public GMD_2007_ResultSetMapper() {
	super();
    }

    protected String getTargetNamespace() {

	return CommonNameSpaceContext.GMD_NS_URI;
    }

    @Override
    public MappingSchema getMappingSchema() {

	return GMD_2007_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    public Element decorateElement(Element element) {
	if (element == null) {
	    return null;
	}
	XMLNodeReader reader = new XMLNodeReader(element);
	try {
	    String metadata = reader.asString();
	    metadata = metadata.replace("http://www.opengis.net/gml/3.2", "http://www.opengis.net/existing/gml/3.2");
	    metadata = metadata.replace("http://www.opengis.net/gml", "http://www.opengis.net/gml/3.2");
	    metadata = metadata.replace("http://www.opengis.net/existing/gml/3.2", "http://www.opengis.net/gml/3.2");
	    metadata = metadata.replace("20060504", "20070417");
	    metadata = metadata.replace("xsi:type=\"gmi:MI_Metadata_Type\"", "");
	    metadata = metadata.replace("xsi:schemaLocation=\"http://www.isotc211.org/2005/gmi http://www.isotc211.org/2005/gmi/gmi.xsd\"",
		    "xsi:schemaLocation=\"http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20070417/gmd/gmd.xsd\"");
	    XMLDocumentReader document = new XMLDocumentReader(metadata);
	    return document.getDocument().getDocumentElement();
	} catch (Exception e) {
	}
	return element;
    }
}
