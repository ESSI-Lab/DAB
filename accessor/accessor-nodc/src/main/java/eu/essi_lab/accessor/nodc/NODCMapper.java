package eu.essi_lab.accessor.nodc;

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
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.xml.XMLDocumentReader;
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
 * Mapper from NODC data model (a profile of ISO 19115-2 without extended elements)
 * 
 * @author boldrini
 */
public class NODCMapper extends FileIdentifierMapper {

    private static final String NODC_MAPPER_ERROR = "NODC_MAPPER_ERROR";

    public NODCMapper() {
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.NODC_NS_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {
	String originalMetadata = originalMD.getMetadata();
	
	originalMetadata = originalMetadata.replace("http://www.opengis.net/gml/3.2", "http://www.opengis.net/gml");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();
	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    // extracting information from the keywords
	    // PARAMETER IDENTIFIERS
	    Set<ComparableEntry<String, String>> parameters = new TreeSet<ComparableEntry<String, String>>();

	    Node[] parameterNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/jquery/datatype')]]");
	    
	    if(parameterNodes.length == 0) {
		parameterNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/datatype')]]");
	    }

	    for (Node parameterNode : parameterNodes) {
		String parameterIdentifier = reader.evaluateString(parameterNode, "@*:href");
		if (parameterIdentifier != null && !parameterIdentifier.equals("")) {
		    String parameterLabel = reader.evaluateString(parameterNode, ".");
		    parameters.add(new ComparableEntry<String, String>(parameterIdentifier, parameterLabel));
		}
	    }

	    if (parameterNodes.length == 0) {

		parameterNodes = reader.evaluateNodes(
			"//*:MD_Keywords[*:thesaurusName/@*:title='GCMD Science Keywords' or *:thesaurusName/@*:title='Global Change Master Directory (GCMD) Science and Services Keywords'   ]/*:keyword/*:CharacterString/text()");

		for (Node parameterNode : parameterNodes) {

		    parameters.add(new ComparableEntry<String, String>(parameterNode.getTextContent(), parameterNode.getTextContent()));
		}
	    }

	    // INSTRUMENT IDENTIFIERS
	    Set<ComparableEntry<String, String>> instruments = new TreeSet<ComparableEntry<String, String>>();
	    Node[] instrumentNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/jquery/insttype')]]");
	    if(instrumentNodes.length == 0) {
		instrumentNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/insttype')]]");
	    }
	    for (Node instrumentNode : instrumentNodes) {
		String instrumentIdentifier = reader.evaluateString(instrumentNode, "@*:href");
		if (instrumentIdentifier != null && !instrumentIdentifier.equals("")) {
		    String instrumentLabel = reader.evaluateString(instrumentNode, ".");
		    instruments.add(new ComparableEntry<String, String>(instrumentIdentifier, instrumentLabel));
		}
	    }
	    // PLATFORM IDENTIFIERS
	    Set<ComparableEntry<String, String>> platforms = new TreeSet<ComparableEntry<String, String>>();
	    Node[] platformNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/jquery/platform')]]");
	    if(platformNodes.length == 0) {
		platformNodes = reader.evaluateNodes("//*:Anchor[@*:href[contains(.,'prd/platform')]]");
	    }
	    for (Node platformNode : platformNodes) {
		String platformIdentifier = reader.evaluateString(platformNode, "@*:href");
		if (platformIdentifier != null && !platformIdentifier.equals("")) {
		    String platformLabel = reader.evaluateString(platformNode, ".");
		    platforms.add(new ComparableEntry<String, String>(platformIdentifier, platformLabel));
		}
	    }
	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    Set<ComparableEntry<String, String>> originatorOrganisations = new TreeSet<ComparableEntry<String, String>>();
	    Node[] originatorNodes = reader.evaluateNodes(
		    "//*:CI_ResponsibleParty[*:role/*:CI_RoleCode/@codeListValue='resourceProvider']/*:organisationName/*:Anchor[@*:href[contains(.,'prd/jquery/institution')]]");
	    if(originatorNodes.length == 0) {
		originatorNodes = reader.evaluateNodes(
			    "//*:CI_ResponsibleParty[*:role/*:CI_RoleCode/@codeListValue='resourceProvider']/*:organisationName/*:Anchor[@*:href[contains(.,'prd/institution')]]");
	    }
	    for (Node originatorNode : originatorNodes) {
		String originatorIdentifier = reader.evaluateString(originatorNode, "@*:href");
		if (originatorIdentifier != null && !originatorIdentifier.equals("")) {
		    String originatorLabel = reader.evaluateString(originatorNode, ".");
		    originatorOrganisations.add(new ComparableEntry<String, String>(originatorIdentifier, originatorLabel));
		}
	    }
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    // XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    // Here the SeaDataNet identification section is converted to GMD core profile data identification section
	    // writer.rename("//mcp:MD_Metadata", "gmd:MD_Metadata");
	    // writer.rename("//mcp:MD_DataIdentification", "gmd:MD_DataIdentification");
	    // writer.rename("//mcp:EX_TemporalExtent", "gmd:EX_TemporalExtent");
	    // Here the profile codelists are converted to GMD core profile free text
	    // writer.rename("//mcp:*[ends-with(name(), 'Code')]", "gco:CharacterString")
	    MIMetadata miMetadata = new MIMetadata(reader.asStream());
	    // MDMetadata metadata = new MDMetadata(reader.asStream());

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
	    // for (SimpleEntry<String, String> instrument : instruments) {
	    // MIInstrument myInstrument = new MIInstrument();
	    // myInstrument.setMDIdentifierTypeCode(instrument.getKey());
	    // myInstrument.setDescription(instrument.getValue());
	    // myInstrument.setTitle(instrument.getValue());
	    // miMetadata.addMIInstrument(myInstrument);
	    // }
	    // // PLATFORM IDENTIFIERS
	    // for (SimpleEntry<String, String> plat : platforms) {
	    // MIPlatform platform = new MIPlatform();
	    // platform.setMDIdentifierCode(plat.getKey());
	    // platform.setDescription(plat.getValue());
	    // Citation platformCitation = new Citation();
	    // platformCitation.setTitle(plat.getValue());
	    // platform.setCitation(platformCitation);
	    // miMetadata.addMIPlatform(platform);
	    // }

	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    for (SimpleEntry<String, String> originatorOrganisation : originatorOrganisations) {
		extendedMetadataHandler.addOriginatorOrganisationIdentifier(originatorOrganisation.getKey());
		extendedMetadataHandler.addOriginatorOrganisationDescription(originatorOrganisation.getValue());
	    }

	    // to generate the lists needed by BODC Rosetta Stone put parameter to true...
	    if (ODIPUtils.getInstance().isEnabled()) {
		ODIPUtils.getInstance().getParameters().addAll(parameters);
		ODIPUtils.getInstance().getInstruments().addAll(instruments);
		ODIPUtils.getInstance().getPlatforms().addAll(platforms);
		ODIPUtils.getInstance().getOriginatorOrganizations().addAll(originatorOrganisations);
	    }

	    // System.out.println(miMetadata.asString(true));
	    coreMetadata.setMIMetadata(miMetadata);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NODC_MAPPER_ERROR, //
		    e);
	}
    }

}
