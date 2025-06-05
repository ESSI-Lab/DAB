package eu.essi_lab.profiler.cdi;

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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.edmo.EDMOClient;
import eu.essi_lab.lib.net.nvs.NVSClient;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class CDIResultSetMapper extends DiscoveryResultSetMapper<Element> {

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema SDN_MAPPING_SCHEMA = new MappingSchema();
    private static final String CDI_RES_SET_MAPPER_ERROR = "CDI_RES_SET_MAPPER_ERROR";
    private NVSClient nvsClient;
    private EDMOClient edmoClient;
    private static final String REVISION_DATE_TYPE = "revision";
    private static final String CODE_LIST = "codeList";
    private static final String CODE_LIST_VLAUE = "codeListValue";
    private static final String CODE_SPACE = "codeSpace";
    private static final String SEADATANET = "SeaDataNet";
    private static final String SDN_P02_UNKNOWN = "http://www.seadatanet.org/urnurl/SDN:P02::UNKNOWN/";
    private static final String SDN_L06_UNKNOWN = "http://www.seadatanet.org/urnurl/SDN:L06::UNKNOWN/";

    public CDIResultSetMapper() {
	setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
	nvsClient = new NVSClient();
	edmoClient = new EDMOClient();
    }

    protected String getTargetNamespace() {

	return CommonNameSpaceContext.SDN_NS_URI;
    }

    @Override
    public MappingSchema getMappingSchema() {

	return SDN_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private String createKeywordXPath(String text) {
	return "//*:keyword/*:CharacterString[text()='" + text + "']";
    }

    @Override
    public Element map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    String originalSchemeURI = res.getOriginalMetadata().getSchemeURI();

	    String targetNamespace = getTargetNamespace();

	    if (targetNamespace.equals(originalSchemeURI) && strategy.equals(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA)) {

		String originalMd = res.getOriginalMetadata().getMetadata();

		XMLDocumentReader reader = new XMLDocumentReader(originalMd);
		return reader.getDocument().getDocumentElement();
		
	    }

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    MDMetadata metadata = coreMetadata.getReadOnlyMDMetadata();
	    MIMetadata metadata2 = coreMetadata.getMIMetadata();

	    // PARAMETER IDENTIFIERS
	    List<String> parameterIdentifiers = new ArrayList<>();
	    List<CoverageDescription> coverageDescriptions = Lists.newArrayList(metadata2.getCoverageDescriptions());
	    for (CoverageDescription coverageDescription : coverageDescriptions) {
		parameterIdentifiers.add(coverageDescription.getAttributeDescription());
	    }
	    parameterIdentifiers = getCDIIdentifiers(parameterIdentifiers, originalSchemeURI);
	    if (!parameterIdentifiers.isEmpty()) {
		Keywords parameterKeywords = new Keywords();
		Citation citation = new Citation();
		citation.setTitle("SeaDataNet Parameter Discovery Vocabulary");
		citation.addAlternateTitle("P02");
		citation.addDate("2017-02-03", REVISION_DATE_TYPE);
		citation.setEdition("97");
		citation.addIdentifier("http://www.seadatanet.org/urnurl/SDN:P02");
		parameterKeywords.setThesaurusCitation(citation);
		for (String parameterIdentifier : parameterIdentifiers) {
		    parameterKeywords.addKeyword(parameterIdentifier);
		}
		metadata.getDataIdentification().addKeywords(parameterKeywords);
	    }
	    // INSTRUMENT IDENTIFIERS
	    List<String> instrumentIdentifiers = new ArrayList<>();
	    List<MIInstrument> instruments = Lists.newArrayList(metadata2.getMIInstruments());
	    for (MIInstrument instrument : instruments) {
		String instrumentIdentifier = instrument.getMDIdentifierCode();
		instrumentIdentifiers.add(instrumentIdentifier);
	    }
	    instrumentIdentifiers = getCDIIdentifiers(instrumentIdentifiers, originalSchemeURI);
	    if (!instrumentIdentifiers.isEmpty()) {
		Keywords instrumentKeywords = new Keywords();
		Citation citation = new Citation();
		citation.setTitle("SeaDataNet device categories");
		citation.addAlternateTitle("L05");
		citation.addDate("2016-11-30", REVISION_DATE_TYPE);
		citation.setEdition("57");
		citation.addIdentifier("http://www.seadatanet.org/urnurl/SDN:L05");
		instrumentKeywords.setThesaurusCitation(citation);
		for (String instrumentIdentifier : instrumentIdentifiers) {
		    instrumentKeywords.addKeyword(instrumentIdentifier);
		}
		metadata.getDataIdentification().addKeywords(instrumentKeywords);
	    }
	    // PLATFORM IDENTIFIERS
	    List<String> platformIdentifiers = new ArrayList<>();
	    List<MIPlatform> platforms = Lists.newArrayList(metadata2.getMIPlatforms());
	    for (MIPlatform platform : platforms) {
		String platformIdentifier = platform.getMDIdentifierCode();
		if (platformIdentifier != null) {
		    platformIdentifiers.add(platformIdentifier);
		}
	    }
	    platformIdentifiers = getCDIIdentifiers(platformIdentifiers, originalSchemeURI);
	    if (!platformIdentifiers.isEmpty()) {
		Keywords platformKeywords = new Keywords();
		Citation citation = new Citation();
		citation.setTitle("SeaVoX Platform Categories");
		citation.addAlternateTitle("L06");
		citation.addDate("2016-01-07", REVISION_DATE_TYPE);
		citation.setEdition("57");
		citation.addIdentifier("http://www.seadatanet.org/urnurl/SDN:L06");
		platformKeywords.setThesaurusCitation(citation);
		for (String instrumentIdentifier : platformIdentifiers) {
		    platformKeywords.addKeyword(instrumentIdentifier);
		}
		metadata.getDataIdentification().addKeywords(platformKeywords);
	    }
	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    ExtensionHandler handler = res.getExtensionHandler();
	    List<String> originatorOrganisationIdentifiers = handler.getOriginatorOrganisationIdentifiers();
	    originatorOrganisationIdentifiers = getCDIIdentifiers(originatorOrganisationIdentifiers, originalSchemeURI);
	    if (!originatorOrganisationIdentifiers.isEmpty()) {
		for (String originatorOrganisationIdentifier : originatorOrganisationIdentifiers) {
		    ResponsibleParty party = edmoClient.getResponsiblePartyFromURI(originatorOrganisationIdentifier);
		    metadata.getDataIdentification().addCitationResponsibleParty(party);
		}
	    }

	    XMLDocumentReader reader = new XMLDocumentReader(metadata.asDocument(false));
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	    // PARAMETER IDENTIFIERS
	    for (String parameterIdentifier : parameterIdentifiers) {
		writer.addAttributes(createKeywordXPath(parameterIdentifier), //
			CODE_LIST, "http://vocab.nerc.ac.uk/isoCodelists/sdnCodelists/cdicsrCodeList.xml#SDN_ParameterDiscoveryCode", //
			CODE_LIST_VLAUE, parameterIdentifier, //
			CODE_SPACE, SEADATANET);
		writer.setText(createKeywordXPath(parameterIdentifier), nvsClient.getLabel(parameterIdentifier));
		writer.rename(createKeywordXPath(parameterIdentifier), "sdn:SDN_ParameterDiscoveryCode");

	    }
	    // INSTRUMENT IDENTIFIERS
	    for (String instrumentIdentifier : instrumentIdentifiers) {
		writer.addAttributes(createKeywordXPath(instrumentIdentifier), //
			CODE_LIST, "http://vocab.nerc.ac.uk/isoCodelists/sdnCodelists/cdicsrCodeList.xml#SDN_DeviceCategoryCode", //
			CODE_LIST_VLAUE, instrumentIdentifier, //
			CODE_SPACE, SEADATANET);
		writer.setText(createKeywordXPath(instrumentIdentifier), nvsClient.getLabel(instrumentIdentifier));
		writer.rename(createKeywordXPath(instrumentIdentifier), "sdn:SDN_DeviceCategoryCode");
	    }
	    // PLATFORM IDENTIFIERS
	    for (String platformIdentifier : platformIdentifiers) {
		writer.addAttributes(createKeywordXPath(platformIdentifier), //
			CODE_LIST, "http://vocab.nerc.ac.uk/isoCodelists/sdnCodelists/cdicsrCodeList.xml#SDN_PlatformCategoryCode", //
			CODE_LIST_VLAUE, platformIdentifier, //
			CODE_SPACE, SEADATANET);
		writer.setText(createKeywordXPath(platformIdentifier), nvsClient.getLabel(platformIdentifier));
		writer.rename(createKeywordXPath(platformIdentifier), "sdn:SDN_PlatformCategoryCode");
	    }
	    // ORIGINATOR ORGANIZATION IDENTIFIERS
	    for (String originatorOrganisationIdentifier : originatorOrganisationIdentifiers) {
		writer.addAttributes(createOrganizationXPath(originatorOrganisationIdentifier), //
			CODE_LIST, "http://vocab.nerc.ac.uk/isoCodelists/sdnCodelists/cdicsrCodeList.xml#SDN_EDMOCode", //
			CODE_LIST_VLAUE, originatorOrganisationIdentifier, //
			CODE_SPACE, SEADATANET);
		writer.setText(createOrganizationXPath(originatorOrganisationIdentifier),
			nvsClient.getLabel(originatorOrganisationIdentifier));
		writer.rename(createOrganizationXPath(originatorOrganisationIdentifier), "sdn:SDN_EDMOCode");
	    }

	    // Here the SeaDataNet identification section is converted from GMD core profile data identification section
	    writer.rename("//gmd:MD_DataIdentification", "sdn:SDN_DataIdentification");

	    return reader.getDocument().getDocumentElement();

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CDI_RES_SET_MAPPER_ERROR);
	}

    }

    private String createOrganizationXPath(String originatorOrganisationIdentifier) {
	return "//*:organisationName/*:CharacterString[text()='" + originatorOrganisationIdentifier + "']";
    }

    private List<String> getCDIIdentifiers(List<String> originalSchemeIdentifiers, String originalSchemeURI) {
	List<String> ret = new ArrayList<>();
	for (String identifier : originalSchemeIdentifiers) {
	    String mappedIdentifier = getCDIIdentifier(identifier, originalSchemeURI);
	    if (mappedIdentifier != null) {
		ret.add(mappedIdentifier);
	    }
	}
	return ret;
    }

    private String getCDIIdentifier(String identifier, String originalSchemeURI) {

	// SOME MANUAL TRANSLATIONS

	if ("http://vocab.nerc.ac.uk/collection/P01/current/PSLTZZ01".equals(identifier)) {
	    // Practical salinity of the water body

	    // Salinity of the water column
	    return "http://www.seadatanet.org/urnurl/SDN:P02::PSAL/";
	}

	switch (originalSchemeURI) {
	case CommonNameSpaceContext.SDN_NS_URI:
	    // the identifier is added as is
	    return identifier;
	case CommonNameSpaceContext.MCP_2_NS_URI:
	    // TODO: Rosetta Stone mapping needed here!
	    // PARAMETER
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/P01")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    if (identifier.contains("http://vocab.aodn.org.au/def/discovery_parameter/")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    // INSTRUMENT
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/L05/current/")) {
		return identifier.replace("http://vocab.nerc.ac.uk/collection/L05/current/", "http://www.seadatanet.org/urnurl/SDN:L05::")
			+ "/";
	    }
	    if (identifier.contains("vocab.aodn.org.au/def/instrument/entity")) {
		// fake record
		return "http://www.seadatanet.org/urnurl/SDN:L05::UNKNOWN/";
	    }
	    // PLATFORMS
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/L06/current/")) {
		return identifier.replace("http://vocab.nerc.ac.uk/collection/L06/current/", "http://www.seadatanet.org/urnurl/SDN:L06::")
			+ "/";
	    }
	    if (identifier.contains("http://vocab.aodn.org.au/def/platform/entity")) {
		return SDN_L06_UNKNOWN;
	    }
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/C17/current")) {
		return SDN_L06_UNKNOWN;
	    }
	    break;
	case CommonNameSpaceContext.NODC_NS_URI:
	    // TODO: Rosetta Stone mapping needed here!
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details")) {
		// fake record
		return "http://www.seadatanet.org/urnurl/SDN:L05::UNKNOWN/";
	    }
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/platform")) {
		return SDN_L06_UNKNOWN;
	    }
	    if (identifier.contains("https://www.nodc.noaa.gov/cgi-bin/OAS/prd/institution/details/")) {
		return "http://www.seadatanet.org/urnurl/SDN:EDMO::UNKNOWN/";
	    }

	    break;

	default:
	    // no identifier is added

	}
	return null;
    }
}
