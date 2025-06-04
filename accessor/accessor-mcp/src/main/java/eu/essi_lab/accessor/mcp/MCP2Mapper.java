package eu.essi_lab.accessor.mcp;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.odip.utils.ODIPUtils;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mapper from AODN MCP data model (a profile of ISO 19115 including MCP extended elements)
 * 
 * @author boldrini
 */
public class MCP2Mapper extends FileIdentifierMapper {

    private static final String MCP2_MAPPER_ERROR = "MCP2_MAPPER_ERROR";

    public MCP2Mapper() {
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);
	dataset.setOriginalMetadata(originalMD);

	mapMetadata(dataset);

	return dataset;
    }

    @Override

    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.MCP_2_NS_URI;
    }

    private void mapMetadata(Dataset dataset) throws GSException {
	OriginalMetadata originalMD = dataset.getOriginalMetadata();
	String originalMetadata = originalMD.getMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    // extracting information from the keywords
	    // PARAMETER IDENTIFIERS
	    Set<ComparableEntry<String, String>> parameters = new HashSet<ComparableEntry<String, String>>();
	    Node[] parameterNodes = reader.evaluateNodes("//*:parameterName/*:DP_Term");
	    for (Node parameterNode : parameterNodes) {
		String parameterIdentifier = reader.evaluateString(parameterNode, "*:vocabularyTermURL/*:URL");
		if (parameterIdentifier != null && !parameterIdentifier.equals("")) {
		    String parameterLabel = reader.evaluateString(parameterNode, "*:term/*:CharacterString");
		    parameters.add(new ComparableEntry<String, String>(normalize(parameterIdentifier), parameterLabel));
		}
	    }
	    // INSTRUMENT IDENTIFIERS
	    Set<ComparableEntry<String, String>> instruments = new HashSet<ComparableEntry<String, String>>();
	    Node[] instrumentNodes = reader.evaluateNodes("//*:parameterDeterminationInstrument/*:DP_Term");
	    for (Node instrumentNode : instrumentNodes) {
		String instrumentIdentifier = reader.evaluateString(instrumentNode, "*:vocabularyTermURL/*:URL");
		if (instrumentIdentifier != null && !instrumentIdentifier.equals("")) {
		    String instrumentLabel = reader.evaluateString(instrumentNode, "*:term/*:CharacterString");
		    instruments.add(new ComparableEntry<>(normalize(instrumentIdentifier), instrumentLabel));
		}
	    }
	    // PLATFORM IDENTIFIERS
	    Set<ComparableEntry<String, String>> platforms = new HashSet<ComparableEntry<String, String>>();
	    Node[] platformNodes = reader.evaluateNodes("//*:platform/*:DP_Term");
	    for (Node platformNode : platformNodes) {
		String platformIdentifier = reader.evaluateString(platformNode, "*:vocabularyTermURL/*:URL");
		if (platformIdentifier != null && !platformIdentifier.equals("")) {
		    String platformLabel = reader.evaluateString(platformNode, "*:term/*:CharacterString");
		    platforms.add(new ComparableEntry<String, String>(normalize(platformIdentifier), platformLabel));
		}
	    }
	    // ORIGINATOR ORGANIZATION IDENTIFIER --- not available for AODN MCP
	    TreeSet<String> originatorOrganisations = new TreeSet();
	    Node[] originatorNodes = reader.evaluateNodes(
		    "//*:organisationName[../*:role/*:CI_RoleCode/@codeListValue='originator' or ../*:role/*:CI_RoleCode/@codeListValue='owner']/*[1]");
	    for (Node originatorNode : originatorNodes) {
		String originatorLabel = reader.evaluateString(originatorNode, ".");
		originatorOrganisations.add(originatorLabel);
	    }

	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    // Here the SeaDataNet identification section is converted to GMD core profile data identification section
	    writer.rename("//mcp2:MD_Metadata", "gmd:MD_Metadata");
	    writer.rename("//mcp2:MD_DataIdentification", "gmd:MD_DataIdentification");
	    writer.rename("//mcp2:EX_TemporalExtent", "gmd:EX_TemporalExtent");
	    // Here the profile codelists are converted to GMD core profile free text
	    writer.rename("//mcp2:*[ends-with(name(), 'Code')]", "gco:CharacterString");
	    MDMetadata metadata = new MDMetadata(reader.asStream());
	    MIMetadata miMetadata = new MIMetadata(metadata.getElementType());

	    // adding extracted information in ISO 19115-2 (where possible) and extended parts
	    // PARAMETER IDENTIFIERS
	    for (SimpleEntry<String, String> parameter : parameters) {
		CoverageDescription description = new CoverageDescription();
		description.setAttributeIdentifier(parameter.getKey());
		description.setAttributeDescription(parameter.getValue());
		description.setAttributeTitle(parameter.getValue());
		miMetadata.addCoverageDescription(description);
	    }
	    // INSTRUMENT IDENTIFIERS
	    for (SimpleEntry<String, String> instrument : instruments) {
		MIInstrument myInstrument = new MIInstrument();
		myInstrument.setMDIdentifierTypeCode(instrument.getKey());
		myInstrument.setDescription(instrument.getValue());
		myInstrument.setTitle(instrument.getValue());
		miMetadata.addMIInstrument(myInstrument);
	    }
	    // PLATFORM IDENTIFIERS
	    for (SimpleEntry<String, String> plat : platforms) {
		MIPlatform platform = new MIPlatform();
		platform.setMDIdentifierCode(plat.getKey());
		platform.setDescription(plat.getValue());
		Citation platformCitation = new Citation();
		platformCitation.setTitle(plat.getValue());
		platform.setCitation(platformCitation);
		miMetadata.addMIPlatform(platform);
	    }
	    // ORIGINATOR ORGANIZATION
	    for (String originatorOrganisation : originatorOrganisations) {
		// ORIGINATOR ORGANIZATION IDENTIFIER --- not available in AODN MC
		// extendedMetadataHandler.addOriginatorOrganisationIdentifier(originatorOrganisation.getKey());
		extendedMetadataHandler.addOriginatorOrganisationDescription(originatorOrganisation);
	    }

	    // to generate the lists needed by BODC Rosetta Stone put parameter to true...
	    if (ODIPUtils.getInstance().isEnabled()) {
		ODIPUtils.getInstance().getParameters().addAll(parameters);
		ODIPUtils.getInstance().getInstruments().addAll(instruments);
		ODIPUtils.getInstance().getPlatforms().addAll(platforms);
		// ODIPUtils.getInstance().getOriginatorOrganizations().addAll(originatorOrganisationIdentifiers);
	    }

	    // System.out.println(miMetadata.asString(true));
	    coreMetadata.setMIMetadata(miMetadata);
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MCP2_MAPPER_ERROR, //
		    e);
	}
    }

    private String normalize(String identifier) {
	if (identifier == null) {
	    return null;
	}
	if (identifier.startsWith("http://vocab.nerc.ac.uk/collection/")) {
	    if (!identifier.endsWith("/")) {
		identifier = identifier + "/";
	    }
	}
	return identifier;
    }

    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());

	    String namespace = reader.evaluateString("namespace-uri(/*[1])").toLowerCase();

	    switch (namespace) {
	    case CommonNameSpaceContext.MCP_2_NS_URI:
		return true;

	    default:
		break;
	    }

	} catch (Exception e) {

	}
	return false;
    }

}
