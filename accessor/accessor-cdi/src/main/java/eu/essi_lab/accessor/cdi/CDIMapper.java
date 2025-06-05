package eu.essi_lab.accessor.cdi;

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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.ComparableEntry;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.emod_pace.EMODPACEThemeCategory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.odip.utils.ODIPUtils;
import eu.essi_lab.ommdk.AbstractResourceMapper;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;

/**
 * Mapper from SeaDataNet CDI data model
 *
 * @author boldrini
 */
public class CDIMapper extends AbstractResourceMapper {

    private static final String CDI_MAPPER_ERROR = "CDI_MAPPER_ERROR";

    String CODE_LIST_VALUE_ATTRIBUTE = "@codeListValue";

    private String temperatureURL = "http://222.186.3.18:8888/erddap/files/data/Physics%20Data(new)/temperature%20and%20salinity/";
    private String oceanURL = "http://222.186.3.18:8888/erddap/files/data/Physics%20Data/ocean%20current/";
    private String marineURL = "http://222.186.3.18:8888/erddap/files/data/Marine%20meteorology/";
    private String salinityURL = "http://222.186.3.18:8888/erddap/files/data/Physics%20Data/temperature%20and%20salinity/";

    private String bathymetryURL = "http://222.186.3.18:8888/erddap/files/data/Bathymetry%20Data/";
    private String chemistryURL = "http://222.186.3.18:8888/erddap/files/data/Chemistry%20Data/";

    public static Double TOL = Math.pow(10, -8);

    public CDIMapper() {
	// empty constructor needed for service loader
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    protected String createOriginalIdentifier(GSResource resource) {

	String prefix = "";

	GSSource source = resource.getSource();
	if (source != null) {
	    String endpoint = source.getEndpoint();
	    if (endpoint != null) {
		if (endpoint.contains("aggregation/open")) {
		    prefix = "sdn-open:";
		}
	    }
	}

	return prefix + resource.getHarmonizedMetadata().getCoreMetadata().getIdentifier();

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.SDN_NS_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {
	    InputStream stream = new ByteArrayInputStream(originalMetadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);

	    // extracting information from the keywords
	    // PARAMETER IDENTIFIERS (map of identifier;label couples)
	    Set<ComparableEntry<String, String>> parameters = new HashSet<>();
	    Node[] parameterNodes = reader.evaluateNodes("//*:SDN_ParameterDiscoveryCode");
	    for (Node parameterNode : parameterNodes) {
		Node nodeAttribute = reader.evaluateNode(parameterNode, CODE_LIST_VALUE_ATTRIBUTE);
		String parameterIdentifier = reader.evaluateString(parameterNode, CODE_LIST_VALUE_ATTRIBUTE);
		if (parameterIdentifier != null && !"".equals(parameterIdentifier)) {
		    String parameterLabel = reader.evaluateString(parameterNode, ".");
		    parameterIdentifier = "http://vocab.nerc.ac.uk/collection/P02/current/" + parameterIdentifier + "/";
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(parameterIdentifier, parameterLabel);
		    writer.setText(nodeAttribute, ".", parameterIdentifier);
		    parameters.add(newEntry);
		}
	    }
	    // INSTRUMENT IDENTIFIERS
	    Set<ComparableEntry<String, String>> instruments = new HashSet<>();
	    Node[] instrumentNodes = reader.evaluateNodes("//*:SDN_DeviceCategoryCode");
	    for (Node node : instrumentNodes) {
		Node nodeAttribute = reader.evaluateNode(node, CODE_LIST_VALUE_ATTRIBUTE);
		String identifier = reader.evaluateString(node, CODE_LIST_VALUE_ATTRIBUTE);
		if (identifier != null && !"".equals(identifier)) {
		    String label = reader.evaluateString(node, ".");
		    identifier = "http://vocab.nerc.ac.uk/collection/L05/current/" + identifier + "/";
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(identifier, label);
		    writer.setText(nodeAttribute, ".", identifier);
		    instruments.add(newEntry);
		}
	    }
	    // PLATFORM IDENTIFIERS
	    Set<ComparableEntry<String, String>> platforms = new HashSet<>();
	    Node[] platformNodes = reader.evaluateNodes("//*:SDN_PlatformCategoryCode");
	    for (Node node : platformNodes) {
		Node nodeAttribute = reader.evaluateNode(node, CODE_LIST_VALUE_ATTRIBUTE);
		String identifier = reader.evaluateString(node, CODE_LIST_VALUE_ATTRIBUTE);
		if (identifier != null && !"".equals(identifier)) {
		    String label = reader.evaluateString(node, ".");
		    identifier = "http://vocab.nerc.ac.uk/collection/L06/current/" + identifier + "/";
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(identifier, label);
		    writer.setText(nodeAttribute, ".", identifier);
		    platforms.add(newEntry);
		}
	    }
	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    Set<ComparableEntry<String, String>> originatorOrganisations = new HashSet<>();
	    Node[] originatorNodes = reader.evaluateNodes("//*:SDN_EDMOCode");
	    for (Node originatorNode : originatorNodes) {
		Node nodeAttribute = reader.evaluateNode(originatorNode, CODE_LIST_VALUE_ATTRIBUTE);
		String originatorIdentifier = reader.evaluateString(originatorNode, CODE_LIST_VALUE_ATTRIBUTE);
		if (originatorIdentifier != null && !"".equals(originatorIdentifier)) {
		    String originatorLabel = reader.evaluateString(originatorNode, ".");
		    originatorIdentifier = "https://edmo.seadatanet.org/report/" + originatorIdentifier;
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(originatorIdentifier, originatorLabel);
		    writer.setText(nodeAttribute, ".", originatorIdentifier);
		    originatorOrganisations.add(newEntry);
		}
	    }
	    // CRUISE and CRUISE_URI
	    Set<ComparableEntry<String, String>> cruise = new HashSet<>();
	    Node[] cruiseNodes = reader.evaluateNodes("//*:SDN_CSRCode");
	    for (Node cruiseNode : cruiseNodes) {
		Node nodeAttribute = reader.evaluateNode(cruiseNode, CODE_LIST_VALUE_ATTRIBUTE);
		String parameterIdentifier = reader.evaluateString(cruiseNode, CODE_LIST_VALUE_ATTRIBUTE);
		if (parameterIdentifier != null && !"".equals(parameterIdentifier)) {
		    String parameterLabel = reader.evaluateString(cruiseNode, ".");
		    parameterIdentifier = "https://csr.seadatanet.org/report/" + parameterIdentifier + "/";
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(parameterIdentifier, parameterLabel);
		    writer.setText(nodeAttribute, ".", parameterIdentifier);
		    cruise.add(newEntry);
		}
	    }

	    //
	    // PROJECTS URI
	    Set<ComparableEntry<String, String>> projects = new HashSet<>();
	    Node[] projectNodes = reader.evaluateNodes("//*:SDN_EDMERPCode");
	    for (Node projectNode : projectNodes) {
		Node nodeAttribute = reader.evaluateNode(projectNode, CODE_LIST_VALUE_ATTRIBUTE);
		String projectIdentifier = reader.evaluateString(projectNode, CODE_LIST_VALUE_ATTRIBUTE);
		if (projectIdentifier != null && !"".equals(projectIdentifier)) {
		    String projectLabel = reader.evaluateString(projectNode, ".");
		    projectIdentifier = "https://edmerp.seadatanet.org/report/" + projectIdentifier;
		    ComparableEntry<String, String> newEntry = new ComparableEntry<>(projectIdentifier, projectLabel);
		    writer.setText(nodeAttribute, ".", projectIdentifier);
		    projects.add(newEntry);
		}
	    }

	    // Here the SeaDataNet identification section is converted to GMD core profile data identification section
	    writer.rename("//sdn:SDN_DataIdentification", "gmd:MD_DataIdentification");
	    // Here the profile codelists are converted to GMD core profile free text
	    // writer.rename("//sdn:*[ends-with(name(), 'Code')]", "gco:CharacterString");
	    // Here the profile codelists are converted to GMX anchors core profile free text
	    writer.rename("//sdn:*[ends-with(name(), 'Code')]", "gmx:Anchor");
	    writer.rename("//gmx:Anchor/@codeListValue", "xlink:href");

	    Node ogfNode = reader.evaluateNode("//gco:CharacterString[text()=\"Oceanographic geographical features\"]");
	    if (ogfNode != null) {
		XMLNodeReader nr = new XMLNodeReader(ogfNode);
	            XMLNodeWriter nw = new XMLNodeWriter(nr);
	            nw.addAttributesNS(".", CommonNameSpaceContext.XLINK_NS_URI, "xlink:href", "http://inspire.ec.europa.eu/theme/of");
	          
		writer.rename(ogfNode, ".", "gmx:Anchor");
		
		
	    }

	    if (dataset.getSource().getEndpoint().contains("cdi.seadatanet.org/report/aggregation/open")) {
		writer.remove("/*:MD_Metadata/*:identificationInfo/*:MD_DataIdentification/*:citation/*:CI_Citation/*:identifier");

	    }

	    MDMetadata metadata = new MDMetadata(reader.asStream());
	    MIMetadata miMetadata = new MIMetadata(metadata.getElementType());

	    if (dataset.getSource().getEndpoint().contains("cdi.seadatanet.org/report/aggregation/open")) {
		metadata.getDataIdentification().setResourceIdentifier(metadata.getFileIdentifier());
		miMetadata.getDataIdentification().setResourceIdentifier(metadata.getFileIdentifier());
	    }

	    // miMetadata.getDataIdentification().setResourceIdentifier(metadata.getFileIdentifier());
	    // adding extracted information in ISO 19115-2 (where possible) and extended parts
	    // PARAMETER IDENTIFIERS
	    for (SimpleEntry<String, String> parameter : parameters) {
		CoverageDescription description = new CoverageDescription();
		description.setAttributeIdentifier(parameter.getKey());
		description.setAttributeDescription(parameter.getValue());
		description.setAttributeTitle(parameter.getValue());
		extendedMetadataHandler.setObservedPropertyURI(parameter.getKey());
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
	    // ORIGINATOR ORGANIZATION IDENTIFIER
	    for (SimpleEntry<String, String> originatorOrganisation : originatorOrganisations) {
		extendedMetadataHandler.addOriginatorOrganisationIdentifier(originatorOrganisation.getKey());
		extendedMetadataHandler.addOriginatorOrganisationDescription(originatorOrganisation.getValue());
	    }

	    // cruise keywords
	    for (SimpleEntry<String, String> c : cruise) {
		Keywords cruiseKeyword = new Keywords();
		cruiseKeyword.setTypeCode("cruise");
		cruiseKeyword.addKeyword(c.getValue());
		if (c.getKey() != null) {
		    CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(c.getKey(),
			    StringEscapeUtils.escapeXml11(c.getValue()));
		    List<CharacterStringPropertyType> list = new ArrayList<CharacterStringPropertyType>();
		    list.add(value);
		    cruiseKeyword.getElementType().setKeyword(list);
		}
		miMetadata.getDataIdentification().addKeywords(cruiseKeyword);
	    }

	    // project keywords
	    // for (SimpleEntry<String, String> p : projects) {
	    // Keywords projectKeyword = new Keywords();
	    // projectKeyword.setTypeCode("project");
	    // projectKeyword.addKeyword(p.getValue());
	    // if (p.getKey() != null) {
	    // CharacterStringPropertyType value = ISOMetadata.createAnchorPropertyType(p.getKey(),
	    // StringEscapeUtils.escapeXml11(p.getValue()));
	    // List<CharacterStringPropertyType> list = new ArrayList<CharacterStringPropertyType>();
	    // list.add(value);
	    // projectKeyword.getElementType().setKeyword(list);
	    // }
	    // miMetadata.getDataIdentification().addKeywords(projectKeyword);
	    // }

	    // to generate the lists needed by BODC Rosetta Stone put parameter to true...
	    if (ODIPUtils.getInstance().isEnabled()) {
		ODIPUtils.getInstance().getParameters().addAll(parameters);
		ODIPUtils.getInstance().getInstruments().addAll(instruments);
		ODIPUtils.getInstance().getPlatforms().addAll(platforms);
		ODIPUtils.getInstance().getOriginatorOrganizations().addAll(originatorOrganisations);
	    }

	    if (dataset.getSource().getEndpoint().contains("222.186.3.18:8889/services/xml")) {
		// onlineResource EMOD-PACE
		// add keywords EMOD-PACE
		GeographicBoundingBox bbox = miMetadata.getDataIdentification().getGeographicBoundingBox();
		if (bbox != null) {
		    Double east = bbox.getEast();
		    Double north = bbox.getNorth();
		    Double west = bbox.getWest();
		    Double south = bbox.getSouth();
		    if (Math.abs(east) < TOL && Math.abs(north) < TOL) {
			// set east=west
			miMetadata.getDataIdentification().getGeographicBoundingBox().setEast(west);
			miMetadata.getDataIdentification().getGeographicBoundingBox().setNorth(south);
		    }
		}
		Iterator<TransferOptions> iterator2 = miMetadata.getDistribution().getDistributionTransferOptions();
		while (iterator2.hasNext()) {
		    TransferOptions transfer2 = iterator2.next();
		    transfer2.clearOnlines();
		}
		miMetadata.getDataIdentification().addKeyword("EMOD-PACE project");
		metadataUpdate(miMetadata, extendedMetadataHandler);
	    }

	    coreMetadata.setMIMetadata(miMetadata);
	} catch (Exception e) {

	    throwException(e);
	}
    }

    private void metadataUpdate(MIMetadata miMetadata, ExtensionHandler extendedMetadataHandler) throws GSException {
	String alternateTitle = miMetadata.getDataIdentification().getCitationAlternateTitle();
	if (miMetadata.getFileIdentifier().contains(" ")) {
	    String new_id = miMetadata.getFileIdentifier().trim().replace(" ", "");
	    miMetadata.setFileIdentifier(new_id);
	}
	Downloader downloader = new Downloader();
	try {
	    if (alternateTitle != null && !alternateTitle.isEmpty()) {
		// temperature case
		String tempTxtFile = temperatureURL + URLEncoder.encode(alternateTitle, "UTF-8") + ".txt";
		boolean exist = HttpConnectionUtils.checkConnectivity(tempTxtFile);
		if (exist) {
		    Online online = new Online();
		    online.setLinkage(tempTxtFile);
		    online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
		    online.setFunctionCode("download");
		    online.setDescription("Direct Download");
		    miMetadata.getDistribution().addDistributionOnline(online);
		    extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.PHYSICS.getThemeCategory());
		} else {
		    // ocean case
		    String oceanTitle = miMetadata.getDataIdentification().getCitationTitle();
		    String oceanTxtFile = oceanURL + URLEncoder.encode(oceanTitle, "UTF-8") + ".txt";
		    exist = HttpConnectionUtils.checkConnectivity(oceanTxtFile);
		    if (exist) {
			Online online = new Online();
			online.setLinkage(oceanTxtFile);
			online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
			online.setFunctionCode("download");
			online.setDescription("Direct Download");
			miMetadata.getDistribution().addDistributionOnline(online);
			extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.PHYSICS.getThemeCategory());
		    } else {
			// marine case
			String marineTxtFile = marineURL + URLEncoder.encode(alternateTitle, "UTF-8") + ".txt";
			exist = HttpConnectionUtils.checkConnectivity(marineTxtFile);
			if (exist) {
			    Online online = new Online();
			    online.setLinkage(marineTxtFile);
			    online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
			    online.setFunctionCode("download");
			    online.setDescription("Direct Download");
			    miMetadata.getDistribution().addDistributionOnline(online);
			    extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.METEOROLOGY.getThemeCategory());
			} else {
			    // salinity case
			    String salinityTxtFile = salinityURL + URLEncoder.encode(alternateTitle, "UTF-8") + ".txt";
			    exist = HttpConnectionUtils.checkConnectivity(salinityTxtFile);
			    if (exist) {
				Online online = new Online();
				online.setLinkage(salinityTxtFile);
				online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
				online.setFunctionCode("download");
				online.setDescription("Direct Download");
				miMetadata.getDistribution().addDistributionOnline(online);
				extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.PHYSICS.getThemeCategory());
			    } else {
				alternateTitle = alternateTitle.contains(" ") ? alternateTitle.replaceAll(" ", "") : alternateTitle;
				String bathymetryDir = bathymetryURL + URLEncoder.encode(alternateTitle, "UTF-8") + "/";
				exist = HttpConnectionUtils.checkConnectivity(bathymetryDir);
				if (exist) {
				    String bathymetryDtmFile = bathymetryDir + "file/" + alternateTitle + ".dtm";
				    boolean dtmExist = HttpConnectionUtils.checkConnectivity(bathymetryDtmFile);
				    if (dtmExist) {
					Online online = new Online();
					online.setLinkage(bathymetryDtmFile);
					online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
					online.setFunctionCode("download");
					online.setDescription("Direct Download");
					miMetadata.getDistribution().addDistributionOnline(online);
					extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.BATHYMETRY.getThemeCategory());
				    }
				} else {
				    // chemistry case
				    Iterator<String> keywords = miMetadata.getDataIdentification().getKeywordsValues();
				    boolean found = false;
				    while (keywords.hasNext() && !found) {
					String kwd = keywords.next();
					if (kwd.toLowerCase().contains("nitrite") || kwd.toLowerCase().contains("nitrate")
						|| kwd.toLowerCase().contains("phosphate") || kwd.toLowerCase().contains("silicate")
						|| kwd.toLowerCase().contains("alkalinity")) {
					    found = true;
					}
				    }
				    if (found) {
					String title = miMetadata.getDataIdentification().getCitationTitle();
					String chemistryDir = chemistryURL + "data-" + URLEncoder.encode(title, "UTF-8") + "/";
					exist = HttpConnectionUtils.checkConnectivity(chemistryDir);
					if (exist) {
					    String chemistryTxtFile = chemistryDir + "file/" + alternateTitle + ".txt";
					    boolean chemistryExist = HttpConnectionUtils.checkConnectivity(chemistryTxtFile);
					    if (chemistryExist) {
						Online online = new Online();
						online.setLinkage(chemistryTxtFile);
						online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
						online.setFunctionCode("download");
						online.setDescription("Direct Download");
						miMetadata.getDistribution().addDistributionOnline(online);
						extendedMetadataHandler
							.addThemeCategory(EMODPACEThemeCategory.CHEMISTRY.getThemeCategory());
					    }
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	} catch (Exception e) {

	    throwException(e);
	}
    }

    /**
     * @param e
     * @throws GSException
     */
    private void throwException(Exception e) throws GSException {

	String message = e.getMessage();

	if (message != null && message.contains("unexpected element")) {

	    message = message.substring(0, message.indexOf(")") + 1);

	    throw GSException.createException(//
		    getClass(), //
		    message, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CDI_MAPPER_ERROR);
	}

	throw GSException.createException(//
		getClass(), //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		CDI_MAPPER_ERROR, //
		e);
    }
}
