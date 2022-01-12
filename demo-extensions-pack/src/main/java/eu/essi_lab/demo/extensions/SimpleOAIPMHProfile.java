package eu.essi_lab.demo.extensions;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;
public class SimpleOAIPMHProfile extends OAIPMHProfile {

    protected static final String SIMPLE_OAI_RES_SET_MAPPPER_AS_DOCUMENT_ERROR = "SIMPLE_OAI_RES_SET_MAPPPER_AS_DOCUMENT_ERROR";
    private static final MappingSchema SIMPLE_MAPPING_SCHEMA = new MappingSchema();
    private static final MetadataFormatType DEMO_FORMAT_TYPE = new MetadataFormatType();

    static {
	SIMPLE_MAPPING_SCHEMA.setDescription("Simple mapping schema for demonstration purpose");
	SIMPLE_MAPPING_SCHEMA.setEncoding("Simple-encoding");
	SIMPLE_MAPPING_SCHEMA.setEncodingMediaType(MediaType.APPLICATION_XML_TYPE);
	SIMPLE_MAPPING_SCHEMA.setEncodingVersion("1.0.0");

	SIMPLE_MAPPING_SCHEMA.setUri("http://simple.mapping.schema");
	SIMPLE_MAPPING_SCHEMA.setName("Simple mapping schema");
	SIMPLE_MAPPING_SCHEMA.setVersion("1.0.0");
    }

    static {
	DEMO_FORMAT_TYPE.setMetadataNamespace("http://demo.profile.com");
	DEMO_FORMAT_TYPE.setSchema("http://www.demo.org/demo/demo.xsd");
	DEMO_FORMAT_TYPE.setMetadataPrefix("demo");
    }

    @Override
    public Provider getProvider() {

	return DemoProvider.getInstance();
    }

    @Override
    public MetadataFormatType getSupportedMetadataFormat() {

	return DEMO_FORMAT_TYPE;
    }

    @Override
    public DiscoveryResultSetMapper<Element> getResultSetMapper() {

	return new DiscoveryResultSetMapper<Element>() {

	    @Override
	    public Provider getProvider() {

		return DemoProvider.getInstance();
	    }

	    @Override
	    public Element map(DiscoveryMessage message, GSResource resource) throws GSException {

		String element = "<simpleRecord>" + resource.getOriginalId() + "</simpleRecord>";

		try {

		    return CommonContext.asDocument(element, true).getDocumentElement();

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error("Can't map resource with private id {}", resource.getPrivateId());

		    throw GSException.createException( //
			    getClass(), //
			    e.getMessage(), //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //
			    SIMPLE_OAI_RES_SET_MAPPPER_AS_DOCUMENT_ERROR);
		}

	    }

	    @Override
	    public MappingSchema getMappingSchema() {

		return SIMPLE_MAPPING_SCHEMA;
	    }
	};
    }
}
