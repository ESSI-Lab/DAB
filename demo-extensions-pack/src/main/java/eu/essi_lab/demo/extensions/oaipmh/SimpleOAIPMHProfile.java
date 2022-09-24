package eu.essi_lab.demo.extensions.oaipmh;

import javax.ws.rs.core.MediaType;

import org.w3c.dom.Element;

import eu.essi_lab.demo.extensions.DemoProvider;
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

/**
 * Basic implementation of {@link OAIPMHProfile}. The provided {@link DiscoveryResultSetMapper} is a very simple implementation which
 * returns XML element with the {@link GSResource#getOriginalId()} as text content (e.g: '&lt;simpleRecord&gt;originalId&lt;/simpleRecord&gt;')
 *
 * @author Fabrizio
 */
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
