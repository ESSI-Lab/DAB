package eu.essi_lab.accessor.socat;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gmd.v_20060504.AbstractMDIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDDataIdentificationType;
import net.opengis.iso19139.gmd.v_20060504.MDIdentificationPropertyType;

public class SOCATConnector extends HarvestedQueryConnector<SOCATConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SOCATConnector";

    private static final String SOCAT_CONNECTOR_ERROR = "SOCAT_CONNECTOR_ERROR";

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().equals("https://data.pmel.noaa.gov/socat/erddap/tabledap/socat_v2023_fulldata");
	// old endpoint
	//return source.getEndpoint().equals("https://ferret.pmel.noaa.gov/socat/erddap/tabledap/socat_v2020_fulldata");
    }

    @XmlTransient

    private String metadataTemplate = null;

    @XmlTransient

    private XMLDocumentReader reader = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	String metadataURL = getSourceURL() + ".iso19115";

	if (metadataTemplate == null) {
	    metadataTemplate = downloadURL(metadataURL);
	}

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	if (reader == null) {
	    try {
		String dataURL = getSourceURL()
			+ ".xhtml?expocode%2Cdataset_name%2Cplatform_name%2Cplatform_type%2Corganization%2Cgeospatial_lon_min%2Cgeospatial_lon_max%2Cgeospatial_lat_min%2Cgeospatial_lat_max%2Ctime_coverage_start%2Ctime_coverage_end%2Cinvestigators%2Csocat_doi&WOCE_CO2_water!=%223%22&WOCE_CO2_atm!=%223%22&distinct()";
			//old request 
			//".xhtml?expocode%2Cdataset_name%2Cplatform_name%2Cplatform_type%2Corganization%2Cgeospatial_lon_min%2Cgeospatial_lon_max%2Cgeospatial_lat_min%2Cgeospatial_lat_max%2Ctime_coverage_start%2Ctime_coverage_end%2Cinvestigators%2Csocat_doi&distinct()";
		          
		String data = downloadURL(dataURL);

		GSLoggerFactory.getLogger(getClass()).info("Parsing XHTML...");
		reader = new XMLDocumentReader(data);
		GSLoggerFactory.getLogger(getClass()).info("Parsed...");
	    } catch (Exception e1) {
		GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
		throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, SOCAT_CONNECTOR_ERROR,
			e1);
	    }
	}

	Node[] headerNodes;
	try {
	    headerNodes = reader.evaluateNodes("//*:table/*:tr/*:th");
	} catch (XPathExpressionException e1) {
	    GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
	    throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, SOCAT_CONNECTOR_ERROR,
		    e1);
	}

	GSLoggerFactory.getLogger(getClass()).info("{} headers retrieved", headerNodes.length);

	String[] headers = new String[headerNodes.length];
	for (int j = 0; j < headers.length; j++) {

	    Node headerNode = headerNodes[j];
	    try {
		headers[j] = reader.evaluateString(headerNode, ".");
	    } catch (XPathExpressionException e) {
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, SOCAT_CONNECTOR_ERROR,
			e);
	    }
	}
	GSLoggerFactory.getLogger(getClass()).info("Retrieving records");
	Node[] recordNodes;
	try {
	    recordNodes = reader.evaluateNodes("//*:table/*:tr[*:td]");
	} catch (XPathExpressionException e1) {
	    e1.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
	    throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, SOCAT_CONNECTOR_ERROR,
		    e1);
	}
	GSLoggerFactory.getLogger(getClass()).info("{} records retrieved", recordNodes.length);

	String rt = request.getResumptionToken();
	if (rt == null) {
	    rt = "0";
	}
	int index = Integer.parseInt(rt);

	int tranche = 100;

	rt = (index + tranche) < recordNodes.length ? "" + (index + tranche) : null;

	GSLoggerFactory.getLogger(getClass()).info("Index {} Tranche {} Next resumption token {}", index, tranche, rt);
	for (int i = index; i < (index + tranche); i++) {

	    if (i >= recordNodes.length) {
		break;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Producing dataset {}", i);

	    MDMetadata mdMetadata;
	    try {
		// remove MD_identifier from XML (because it seems that the clear() function for identifier element
		// doesn't exist
		XMLDocumentReader reader = new XMLDocumentReader(metadataTemplate);
		XMLDocumentWriter writer = new XMLDocumentWriter(reader);
		reader.setNamespaceContext(new ISO2014NameSpaceContext());
		writer.remove("/*:MI_Metadata/*:identificationInfo/*:MD_DataIdentification/*:citation/*:CI_Citation/*:identifier");
		mdMetadata = new MDMetadata(reader.asString());

		List<MDIdentificationPropertyType> infos = mdMetadata.getElementType().getIdentificationInfo();
		List<MDIdentificationPropertyType> infosToRemove = new ArrayList<>();
		for (MDIdentificationPropertyType info : infos) {
		    AbstractMDIdentificationType ainfo = info.getAbstractMDIdentification().getValue();
		    if (ainfo instanceof MDDataIdentificationType) {
			MDDataIdentificationType dataInfo = (MDDataIdentificationType) ainfo;
			dataInfo.getExtent().clear();
		    } else {
			infosToRemove.add(info);
		    }
		}
		mdMetadata.getElementType().getIdentificationInfo().removeAll(infosToRemove);

	    } catch (Exception e1) {
		e1.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);
		throw GSException.createException(getClass(), ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, SOCAT_CONNECTOR_ERROR,
			e1);
	    }

	    MIMetadata miMetadata = new MIMetadata(mdMetadata.getElementType());

	    DatasetCollection dataset = new DatasetCollection();
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    try {

		HashMap<String, String> values = new HashMap<>();
		Node recordNode = recordNodes[i];
		Node[] valueNodes = reader.evaluateNodes(recordNode, "*:td");
		for (int j = 0; j < valueNodes.length; j++) {
		    Node valueNode = valueNodes[j];
		    String value = reader.evaluateString(valueNode, ".");
		    if (value != null && !value.equals("")) {
			values.put(headers[j], value);
		    }
		}
		String expocode = values.get("expocode");
		String datasetName = values.get("dataset_name");
		String platformName = values.get("platform_name");
		String platformType = values.get("platform_type");
		String organization = values.get("organization");
		String geospatialLonMin = values.get("geospatial_lon_min");
		String geospatialLonMax = values.get("geospatial_lon_max");
		String geospatialLatMin = values.get("geospatial_lat_min");
		String geospatialLatMax = values.get("geospatial_lat_max");
		String timeCoverageStart = values.get("time_coverage_start");
		String timeCoverageEnd = values.get("time_coverage_end");
		String investigators = values.get("investigators");
		String socatDOI = values.get("socat_doi");

		DataIdentification identification = miMetadata.getDataIdentification();
		// identification.setResourceIdentifier(socatDOI);
		identification.setCitationTitle(expocode);

		if (platformName != null || platformType != null) {
		    MIPlatform platform = new MIPlatform();
		    platform.setMDIdentifierCode(platformName);
		    Citation citation = new Citation();
		    citation.setTitle(platformName);
		    platform.setCitation(citation);
		    platform.setDescription(platformType);
		    miMetadata.addMIPlatform(platform);
		    Keywords k = new Keywords();
		    k.setTypeCode("platform");
		    k.addKeyword(platformName);
		    identification.addKeywords(k);
		}

		if (geospatialLatMax != null && geospatialLatMin != null && geospatialLonMin != null && geospatialLonMax != null) {
		    double n = Double.parseDouble(geospatialLatMax);
		    double s = Double.parseDouble(geospatialLatMin);
		    double w = Double.parseDouble(geospatialLonMin);
		    double e = Double.parseDouble(geospatialLonMax);
		    identification.clearGeographicBoundingBoxes();
		    identification.addGeographicBoundingBox(n, w, s, e);
		}
		identification.clearVerticalExtents();

		List<String> organizations = new ArrayList<>();
		List<String> individuals = new ArrayList<>();
		if (organization != null) {
		    String[] split;
		    if (organization.contains(":")) {
			split = organization.split(":");
		    } else {
			split = new String[] { organization };
		    }
		    for (String s : split) {
			organizations.add(s);
		    }
		}
		if (investigators != null) {
		    String[] split;
		    if (investigators.contains(":")) {
			split = investigators.split(":");
		    } else {
			split = new String[] { investigators };
		    }
		    for (String s : split) {
			individuals.add(s);
		    }
		}
		identification.clearPointOfContacts();
		for (String individual : individuals) {
		    ResponsibleParty pointOfContact = new ResponsibleParty();
		    pointOfContact.setIndividualName(individual);
		    pointOfContact.setRoleCode("principalInvestigator");
		    identification.addPointOfContact(pointOfContact);
		}
		for (String o : organizations) {
		    ResponsibleParty pointOfContact = new ResponsibleParty();
		    pointOfContact.setOrganisationName(o);
		    pointOfContact.setRoleCode("author");
		    identification.addPointOfContact(pointOfContact);
		}
		if (timeCoverageStart != null && timeCoverageEnd != null) {
		    if (timeCoverageEnd.contains("T")) {
			miMetadata.setDateStampAsDate(timeCoverageEnd.substring(0, timeCoverageEnd.indexOf("T")));
		    }
		    identification.setCitationRevisionDate(timeCoverageEnd);
		    identification.addTemporalExtent(timeCoverageStart, timeCoverageEnd);
		}

		miMetadata.clearContentInfos();
		String[] parameters = new String[] { "salinity", "sea surface temperature", "sea-level air pressure",
			"WOCE flag for aqueous CO2", "fCO2" };
		for (String parameter : parameters) {
		    CoverageDescription description = new CoverageDescription();
		    //description.setAttributeIdentifier(parameter);
		    description.setAttributeDescription(parameter);
		    description.setAttributeTitle(parameter);
		    miMetadata.addCoverageDescription(description);

		}
		try {
		    String identifier = StringUtils.hashSHA1messageDigest("SOCAT:" + expocode + ":" + socatDOI);
		    dataset.getHarmonizedMetadata().getCoreMetadata().setIdentifier(identifier);
		    miMetadata.setFileIdentifier(identifier);
		    miMetadata.getDataIdentification().setResourceIdentifier(expocode);
		} catch (Exception e) {
		    e.printStackTrace();
		}

		OriginalMetadata record = new OriginalMetadata();

		record.setSchemeURI(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);

		record.setMetadata(dataset.asString(true));

		ret.addRecord(record);
	    } catch (Exception ee) {
		ee.printStackTrace();
	    }
	}
	Optional<Integer> mr = getSetting().getMaxRecords();

	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && (index + tranche) > mr.get()) {
	    ret.setResumptionToken(null);
	} else {
	    ret.setResumptionToken(rt);
	}
	return ret;
    }

    private String downloadURL(String url) {
	Downloader d = new Downloader();
	return d.downloadOptionalString(url).get();
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI_GS_RESOURCE);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SOCATConnectorSetting initSetting() {

	return new SOCATConnectorSetting();
    }
}
