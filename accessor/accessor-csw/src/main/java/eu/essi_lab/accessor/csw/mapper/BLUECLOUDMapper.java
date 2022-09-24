package eu.essi_lab.accessor.csw.mapper;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mapper from SeaDataNet CDI data model
 *
 * @author boldrini
 */
public class BLUECLOUDMapper extends FileIdentifierMapper {

    private static final String BLUE_CLOUD_MAPPER_ERROR = "BLUE_CLOUD_MAPPER_ERROR";
    String HREF_ATTRIBUTE = "@*:href";

    public BLUECLOUDMapper() {
	// empty constructor needed for service loader
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
	return CommonNameSpaceContext.BLUECLOUD_NS_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);

	    // extracting information from the keywords
	    // PARAMETER IDENTIFIERS (map of identifier;label couples)
	    Set<ComparableEntry<String, String>> parameters = new HashSet<>();
	    Node[] parameterNodes = reader.evaluateNodes(
		    "//*:descriptiveKeywords/*:MD_Keywords/*:type/*:MD_KeywordTypeCode[@codeListValue=\"parameter\"]/../../*:keyword");
	    for (Node parameterNode : parameterNodes) {
		Node anchor = reader.evaluateNode(parameterNode, "*:Anchor");
		if (anchor != null) {
		    String parameterIdentifier = reader.evaluateString(anchor, HREF_ATTRIBUTE);
		    if (parameterIdentifier != null && !"".equals(parameterIdentifier)) {
			String parameterLabel = reader.evaluateString(parameterNode, ".");
			// parameterIdentifier = "http://www.seadatanet.org/urnurl/SDN:P02::" + parameterIdentifier +
			// "/";
			ComparableEntry<String, String> newEntry = new ComparableEntry<>(parameterIdentifier, parameterLabel);
			parameters.add(newEntry);
		    }
		}

	    }
	    // INSTRUMENT IDENTIFIERS
	    // Set<ComparableEntry<String, String>> instruments = new HashSet<>();
	    // Node[] instrumentNodes = reader.evaluateNodes("//*:SDN_DeviceCategoryCode");
	    // for (Node node : instrumentNodes) {
	    // String identifier = reader.evaluateString(node, CODE_LIST_VALUE_ATTRIBUTE);
	    // if (identifier != null && !"".equals(identifier)) {
	    // String label = reader.evaluateString(node, ".");
	    // identifier = "http://www.seadatanet.org/urnurl/SDN:L05::" + identifier + "/";
	    // ComparableEntry<String, String> newEntry = new ComparableEntry<>(identifier, label);
	    // instruments.add(newEntry);
	    // }
	    // }
	    // // PLATFORM IDENTIFIERS
	    // Set<ComparableEntry<String, String>> platforms = new HashSet<>();
	    // Node[] platformNodes = reader.evaluateNodes("//*:SDN_PlatformCategoryCode");
	    // for (Node node : platformNodes) {
	    // String identifier = reader.evaluateString(node, CODE_LIST_VALUE_ATTRIBUTE);
	    // if (identifier != null && !"".equals(identifier)) {
	    // String label = reader.evaluateString(node, ".");
	    // identifier = "http://www.seadatanet.org/urnurl/SDN:L06::" + identifier + "/";
	    // ComparableEntry<String, String> newEntry = new ComparableEntry<>(identifier, label);
	    // platforms.add(newEntry);
	    // }
	    // }
	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    // Set<ComparableEntry<String, String>> originatorOrganisations = new HashSet<>();
	    // Node[] originatorNodes =
	    // reader.evaluateNodes("//*:SDN_EDMOCode[../../*:role/*:CI_RoleCode/@codeListValue='originator']");
	    // for (Node originatorNode : originatorNodes) {
	    // String originatorIdentifier = reader.evaluateString(originatorNode, HREF_ATTRIBUTE);
	    // if (originatorIdentifier != null && !"".equals(originatorIdentifier)) {
	    // String originatorLabel = reader.evaluateString(originatorNode, ".");
	    // originatorIdentifier = "http://www.seadatanet.org/urnurl/SDN:EDMO::" + originatorIdentifier + "/";
	    // ComparableEntry<String, String> newEntry = new ComparableEntry<>(originatorIdentifier, originatorLabel);
	    // originatorOrganisations.add(newEntry);
	    // }
	    // }
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    // XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    // // Here the SeaDataNet identification section is converted to GMD core profile data identification
	    // section
	    // writer.rename("//sdn:SDN_DataIdentification", "gmd:MD_DataIdentification");
	    // // Here the profile codelists are converted to GMD core profile free text
	    // writer.rename("//sdn:*[ends-with(name(), 'Code')]", "gco:CharacterString");
	    MDMetadata metadata = new MDMetadata(reader.asStream());
	    MIMetadata miMetadata = new MIMetadata(metadata.getElementType());

	    metadata.getDataIdentification().setResourceIdentifier(metadata.getFileIdentifier());
	    miMetadata.getDataIdentification().setResourceIdentifier(metadata.getFileIdentifier());

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
	    // for (SimpleEntry<String, String> originatorOrganisation : originatorOrganisations) {
	    // extendedMetadataHandler.addOriginatorOrganisationIdentifier(originatorOrganisation.getKey());
	    // extendedMetadataHandler.addOriginatorOrganisationDescription(originatorOrganisation.getValue());
	    // }

	    // to generate the lists needed by BODC Rosetta Stone put parameter to true...
	    // if (ODIPUtils.getInstance().isEnabled()) {
	    // ODIPUtils.getInstance().getParameters().addAll(parameters);
	    // ODIPUtils.getInstance().getInstruments().addAll(instruments);
	    // ODIPUtils.getInstance().getPlatforms().addAll(platforms);
	    // ODIPUtils.getInstance().getOriginatorOrganizations().addAll(originatorOrganisations);
	    // }

	    coreMetadata.setMIMetadata(miMetadata);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BLUE_CLOUD_MAPPER_ERROR, //
		    e);
	}
    }

}
