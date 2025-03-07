package eu.essi_lab.profiler.oaipmh.handler.discover;

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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.oaipmh.HeaderType;
import eu.essi_lab.jaxb.oaipmh.MetadataFormatType;
import eu.essi_lab.jaxb.oaipmh.MetadataType;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.jaxb.oaipmh.StatusType;
import eu.essi_lab.jaxb.oaipmh.VerbType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.oaipmh.OAIPMHNameSpaceMapper;
import eu.essi_lab.profiler.oaipmh.OAIPMHRequestReader;
import eu.essi_lab.profiler.oaipmh.profile.OAIPMHProfile;
import eu.essi_lab.profiler.oaipmh.token.ResumptionToken;

/**
 * See <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord">GetRecord</a> for an example of
 * OAIPMH 2 record. The
 * {@link #OAIPMH_MAPPING_SCHEMA} has the following properties:
 * <ul>
 * <li>schema name: {@value #OAIPMH_SCHEMA_NAME}</li>
 * <li>schema version: {@value #OAIPMH_SCHEMA_VERSION}</li>
 * <li>schema uri: {@value #OAIPMH_SCHEMA_URI}</li>
 * <li>encoding media type: {@link MediaType#APPLICATION_XML}</li>
 * </ul>
 *
 * @author Fabrizio
 */
public class OAIPMHResultSetMapper extends DiscoveryResultSetMapper<String> {

    /**
     * The schema uri of {@link #OAIPMH_MAPPING_SCHEMA}
     */
    public static final String OAIPMH_SCHEMA_URI = "http://www.openarchives.org/OAI/2.0/";

    /**
     * The schema name of {@link #OAIPMH_MAPPING_SCHEMA}
     */
    public static final String OAIPMH_SCHEMA_NAME = "OAI-PMH";

    /**
     * The schema version of {@link #OAIPMH_MAPPING_SCHEMA}
     */
    public static final String OAIPMH_SCHEMA_VERSION = "2.0";

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema OAIPMH_MAPPING_SCHEMA = new MappingSchema();

    private DiscoveryResultSetMapper<Element> mapper;

    static {

	OAIPMH_MAPPING_SCHEMA.setName(OAIPMH_SCHEMA_NAME);
	OAIPMH_MAPPING_SCHEMA.setVersion(OAIPMH_SCHEMA_VERSION);
	OAIPMH_MAPPING_SCHEMA.setUri(OAIPMH_SCHEMA_URI);

	OAIPMH_MAPPING_SCHEMA.setEncodingMediaType(MediaType.APPLICATION_XML_TYPE);
	System.out.println();
    }

    public OAIPMHResultSetMapper() {

	setMappingStrategy(MappingStrategy.PRIORITY_TO_CORE_METADATA);
    }

    @Override
    public String map(DiscoveryMessage message, GSResource resource) throws GSException {

	boolean valid = isValid(resource);
	if (!valid) {
	    return createDummyRecord(resource);
	}

	// -------------------------------------
	//
	// creates the header
	//
	//
	HeaderType header = new HeaderType();

	Optional<String> oaiHeaderIdentifier = resource.getPropertyHandler().getOAIPMHHeaderIdentifier();

	// this extension is set by the OAI-PMH connector
	if (oaiHeaderIdentifier.isPresent()) {
	    header.setIdentifier(oaiHeaderIdentifier.get());
	} else {
	    // if the OAI header identifier is missing, because this resource
	    // do not comes from an OAI-PMH connector, then we use the public identifier which corresponds
	    // to core ISO fileIdentifier
	    header.setIdentifier(resource.getPublicId());
	}

	// looking for deleted record
	boolean deleted = false;
	if (resource.getPropertyHandler().isDeleted()) {
	    deleted = true;
	    header.setStatus(StatusType.DELETED);
	}

	// set the header datestamp
	header.setDatestamp(resource.getPropertyHandler().getResourceTimeStamp().orElse(null));

	// set the source id as setSpec
	header.getSetSpec().add(resource.getSource().getUniqueIdentifier());

	// -------------------------------------
	//
	// creates the record type
	//
	//
	RecordType record = new RecordType();
	record.setHeader(header);

	OAIPMHRequestReader reader = OAIPMHRequestTransformer.createReader(message.getWebRequest());
	VerbType verbType = VerbType.fromValue(reader.getVerb());

	String metadata = null;

	if (!deleted && verbType != VerbType.LIST_IDENTIFIERS) {

	    MetadataType metadataType = new MetadataType();
	    record.setMetadata(metadataType);

	    // -------------------------------------
	    //
	    // retrieves the metadata prefix
	    //
	    //
	    OAIPMHRequestReader reqReader = OAIPMHRequestTransformer.createReader(message.getWebRequest());
	    String prefix = reqReader.getMetadataPrefix();
	    String resumptionToken = reqReader.getResumptionToken();
	    if (resumptionToken != null) {
		ResumptionToken rt = ResumptionToken.of(resumptionToken);
		prefix = rt.getMetadataPrefix();
	    }

	    // ----------------------------------------------
	    //
	    // retrieves the mapper from the selected profile
	    //
	    //
	    if (mapper == null) {

		Iterable<OAIPMHProfile> profiles = OAIPMHProfile.getAvailableProfiles();
		for (OAIPMHProfile profile : profiles) {

		    MetadataFormatType format = profile.getSupportedMetadataFormat();
		    if (format.getMetadataPrefix().equals(prefix)) {

			mapper = profile.getResultSetMapper();
			mapper.setMappingStrategy(this.strategy);

			GSLoggerFactory.getLogger(getClass()).info("Selected profile: {}", profile.getClass().getName());
			GSLoggerFactory.getLogger(getClass()).info("Selected mapper: {}", mapper.getClass().getName());

			break;
		    }
		}
	    }

	    if (mapper != null) {
		try {

		    Element any = mapper.map(message, resource);
		    if (any != null) {
			XMLNodeReader nodeReader = new XMLNodeReader(any);

			metadata = nodeReader.asString(true);
		    }
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}

	String ret = "";
	try {

	    switch (verbType) {
	    case GET_RECORD:
	    case LIST_RECORDS:
		if (Objects.nonNull(metadata)) {
		    ret = asString(record);
		    ret = ret.replace("<metadata/>", "<metadata>" + metadata + "</metadata>");
		} else {
		    record.getHeader().setStatus(StatusType.DELETED);
		    record.setMetadata(null);
		    ret = asString(record);
		}

		break;

	    case LIST_IDENTIFIERS:
		JAXBElement<HeaderType> jaxb = new JAXBElement<HeaderType>(//
			new QName(CommonNameSpaceContext.OAI_NS_URI, "header"), //
			HeaderType.class, //
			header);

		ret = CommonContext.asString(jaxb, true, new OAIPMHNameSpaceMapper());

	    default:
		break;
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return ret;
    }

    /**
     * @return
     */
    private String createDummyRecord(GSResource resource) {
	HeaderType header = new HeaderType();
	header.setIdentifier(UUID.randomUUID().toString());
	header.setStatus(StatusType.DELETED);
	header.getSetSpec().add(resource.getSource().getUniqueIdentifier());
	header.setDatestamp(ISO8601DateTimeUtils.getISO8601DateTime());
	RecordType record = new RecordType();
	record.setHeader(header);
	return asString(record);
    }

    /**
     * @param type
     * @return
     */
    private String asString(RecordType record) {

	try {
	    return CommonContext.asString(record, true, new OAIPMHNameSpaceMapper());
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    /**
     * 
     */
    protected boolean isValid(GSResource resource) {
	return true;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public MappingSchema getMappingSchema() {

	return OAIPMH_MAPPING_SCHEMA;
    }
}
