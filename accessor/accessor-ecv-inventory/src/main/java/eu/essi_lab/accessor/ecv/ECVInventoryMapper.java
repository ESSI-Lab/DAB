package eu.essi_lab.accessor.ecv;

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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.impl.HTTPProtocol;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class ECVInventoryMapper extends OriginalIdentifierMapper {

    private static final String ECV = "ECV";

    private static final String ECV_ESCAPE_SEPARATOR = "ECV_ESCAPE_SEPARATOR";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public ECVInventoryMapper() {
	// do nothing
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ECV_INVENTORY;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	ECVInventorySatellite station = new ECVInventorySatellite();

	String[] splittedStrings = originalMetadata.split(ECV_ESCAPE_SEPARATOR);

	station.setAvailabilityLink(splittedStrings[0]);
	station.setDataFormat(splittedStrings[1]);
	station.setDataLink(splittedStrings[2]);
	station.setDataRecordID(splittedStrings[3]);
	station.setDataRecordName(splittedStrings[4]);
	station.setDomain(splittedStrings[5]);
	station.setEcv(splittedStrings[6]);
	station.setEcvProduct(splittedStrings[7]);
	station.setEditorMail(splittedStrings[8]);
	station.setEndDate(splittedStrings[9]);
	station.setExtent(splittedStrings[10]);
	station.setHresolution(splittedStrings[11]);
	station.setLinkToSource(splittedStrings[12]);
	station.setMaintenance(splittedStrings[13]);
	station.setPeerReview(splittedStrings[14]);
	station.setPhysicalQuantity(splittedStrings[15]);
	station.setQaProcess(splittedStrings[16]);
	station.setRecordID(splittedStrings[17]);
	station.setReleaseDate(splittedStrings[18]);
	station.setResponderMail(splittedStrings[19]);
	station.setResponderName(splittedStrings[20]);
	station.setResponsibleOrg(splittedStrings[21]);
	station.setSatInstrument(splittedStrings[22]);
	station.setSiUnit(splittedStrings[23]);
	station.setStartDate(splittedStrings[24]);
	station.setTresolution(splittedStrings[25]);
	station.setVresolution(splittedStrings[26]);

	// ECVInventoryVariable variable = ECVInventoryVariable.TD_COUNT;

	logger.debug("STARTED mapping for record number {} .", station.getRecordID());

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	/**
	 * TITLE-ABSTRACT
	 */
	if (station.getDataRecordName() != null && !station.getDataRecordName().equals("")) {
	    coreMetadata.setTitle(station.getDataRecordName());
	} else {
	    coreMetadata.setTitle("No TITLE for this dataset with Record Identifier " + station.getRecordID());
	}

	if (station.getPeerReview() != null && !station.getPeerReview().equals("")) {

	    String peerReview = station.getPeerReview();
	    if (station.getMaintenance() != null && !station.getMaintenance().equals("")) {
		peerReview = peerReview + "\n" + station.getMaintenance();
	    }

	    if (station.getQaProcess() != null && !station.getQaProcess().equals("")) {
		peerReview = peerReview + "\n" + station.getQaProcess();
	    }
	    coreMetadata.setAbstract(peerReview);
	} else {
	    coreMetadata.setAbstract("No ABSTRACT for this dataset with Record Identifier " + station.getRecordID());
	}

	/**
	 * KEYWORDS
	 */
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(ECV);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getEcv());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getEcvProduct());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getDomain());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getPhysicalQuantity());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getSiUnit());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getDataFormat());

	// publication date
	if (station.getReleaseDate() != null && !station.getReleaseDate().equals("")) {
	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.YEAR, Integer.valueOf(station.getReleaseDate()));
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(cal.toString());
	}

	// if (resolution.equals(Resolution.YEARLY))
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("yearly");

	//
	// URL + variable
	//
	// String id = UUID.nameUUIDFromBytes((splittedStrings[0] + splittedStrings[11]).getBytes()).toString();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	/**
	 * TEMPORAL EXTENT
	 */
	TemporalExtent temporalExtent = new TemporalExtent();
	if (station.getStartDate() != null) {

	    TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	    Optional<Date> startDateTime = null;

	    startDateTime = ISO8601DateTimeUtils.parseISO8601ToDate(station.getStartDate());
	    if (startDateTime.isPresent()) {
		Date begin = startDateTime.get();
		String stringStart = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		temporalExtent.setPosition(stringStart, startIndeterminate, false, true);
	    }

	    if (station.getEndDate() != null) {
		TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
		Optional<Date> endDateTime = null;
		endDateTime = ISO8601DateTimeUtils.parseISO8601ToDate(station.getEndDate());
		if (endDateTime.isPresent()) {
		    Date end = endDateTime.get();
		    String stringEnd = ISO8601DateTimeUtils.getISO8601DateTime(end);
		    temporalExtent.setPosition(stringEnd, endTimeInderminate, false, false);
		}
	    }

	    /**
	     * CODE COMMENTED BELOW COULD BE USEFUL
	     * // if (dateTime.isPresent()) {
	     * // String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(dateTime.get());
	     * // extent.setPosition(beginTime, startIndeterminate, false, true);
	     * // // Estimate of the data size
	     * // // only an estimate seems to be possible, as this odata service doesn't seem to support the /$count
	     * // // operator
	     * // double expectedValuesPerYears = 12.0; // 1 value every 5 minutes
	     * // double expectedValuesPerDay = expectedValuesPerHours * 24.0;
	     * // long expectedSize = TimeSeriesUtils.estimateSize(dateTime.get(), new Date(), expectedValuesPerDay);
	     * // GridSpatialRepresentation grid = new GridSpatialRepresentation();
	     * // grid.setNumberOfDimensions(1);
	     * // grid.setCellGeometryCode("point");
	     * // Dimension time = new Dimension();
	     * // time.setDimensionNameTypeCode("time");
	     * // try {
	     * // time.setDimensionSize(new BigInteger("" + expectedSize));
	     * // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	     * // extensionHandler.setDataSize(expectedSize);
	     * // } catch (Exception e) {
	     * // }
	     * // grid.addAxisDimension(time);
	     * // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	     * // }
	     */

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(temporalExtent);
	}

	/**
	 * BOUNDING BOX - Vertical Extent
	 */
	String extent = station.getExtent();
	// global bounding box
	if (extent.toLowerCase().contains("global") || extent.toLowerCase().contains("180") || extent.toLowerCase().contains("360")
		|| extent.toLowerCase().contains("90")) {

	    coreMetadata.addBoundingBox(90, 180, -90, 180);
	} else {
	    // TODO: extract corrdinates from string....
	}

	// elevation
	// String elevation = station.getVresolution();
	// if (elevation != null && !elevation.equals("")) {
	// VerticalExtent verticalExtent = new VerticalExtent();
	// if (isDouble(elevation)) {
	// verticalExtent.setMinimumValue(Double.parseDouble(elevation));
	// verticalExtent.setMaximumValue(Double.parseDouble(elevation));
	// coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	// }
	//
	// }

	/**
	 * CONTACT POINT
	 */

	// ResponsibleParty creatorContact = new ResponsibleParty();
	//
	// creatorContact.setOrganisationName("Tripura University");
	// creatorContact.setRoleCode("originator");
	// creatorContact.setIndividualName("Anirban Guha");
	//
	// Contact contactcreatorContactInfo = new Contact();
	// Address address = new Address();
	// address.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	// contactcreatorContactInfo.setAddress(address);
	// creatorContact.setContactInfo(contactcreatorContactInfo);

	ResponsibleParty creatorContact = null;
	if (station.getResponderName() != null && !station.getResponderName().equals("")) {
	    creatorContact = new ResponsibleParty();
	    creatorContact.setRoleCode("pointOfContact");
	    creatorContact.setIndividualName(station.getResponderName());

	    if (station.getResponderMail() != null && !station.getResponderMail().equals("")) {
		Contact contactcreatorContactInfo = new Contact();
		Address address = new Address();
		address.addElectronicMailAddress(station.getResponderMail());
		contactcreatorContactInfo.setAddress(address);
		creatorContact.setContactInfo(contactcreatorContactInfo);
	    }

	    if (station.getResponsibleOrg() != null && !station.getResponsibleOrg().equals("")) {
		creatorContact.setOrganisationName(station.getResponsibleOrg());
	    } else {
		creatorContact.setOrganisationName("Global Climate Observing System (GCOS)");
	    }

	}

	ResponsibleParty otherContact = null;

	if (station.getEditorMail() != null && !station.getEditorMail().equals("")) {
	    otherContact = new ResponsibleParty();
	    otherContact.setRoleCode("author");
	    Contact othercreatorContactInfo = new Contact();
	    Address otherAddress = new Address();
	    otherAddress.addElectronicMailAddress(station.getEditorMail());
	    othercreatorContactInfo.setAddress(otherAddress);
	    otherContact.setContactInfo(othercreatorContactInfo);
	}

	if (creatorContact != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	}

	if (otherContact != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(otherContact);
	}

	/**
	 * MIPLATFORM
	 **/

	// MIPlatform platform = new MIPlatform();
	//
	// String platformIdentifier = ECV + ":" + station.getStationCode();
	//
	// platform.setMDIdentifierCode(platformIdentifier);
	//
	// String siteDescription = station.getName();
	//
	// platform.setDescription(siteDescription);
	//
	// Citation platformCitation = new Citation();
	// platformCitation.setTitle(station.getName());
	// platform.setCitation(platformCitation);
	//
	// coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	// CoverageDescription coverageDescription = new CoverageDescription();
	// String variableId = variable.toString() + "_" + resolution.toString();
	//
	// coverageDescription.setAttributeIdentifier(variableId);
	// coverageDescription.setAttributeTitle(variable.toString());
	//
	// String attributeDescription = variable.toString() + " Units: " + variable.getUnit() + " Resolution: " +
	// resolution.toString();
	//
	// coverageDescription.setAttributeDescription(attributeDescription);
	// coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */
	Set<String> onlineList = new HashSet<String>();

	if (station.getAvailabilityLink() != null && !station.getAvailabilityLink().equals("")) {
	    String availabilityLink = extractLink(station.getAvailabilityLink());
	    if (availabilityLink != null) {
		Online online = createOnline(availabilityLink, station.getRecordID(), "Availability Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		onlineList.add(availabilityLink);
	    }
	}

	if (station.getDataLink() != null && !station.getDataLink().equals("")) {
	    String dataLink = extractLink(station.getDataLink());
	    if (dataLink != null && !onlineList.contains(dataLink)) {
		Online online = createOnline(dataLink, station.getRecordID(), "Data Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		onlineList.add(dataLink);
	    }
	}

	if (station.getLinkToSource() != null && !station.getLinkToSource().equals("")) {
	    String sourceLink = extractLink(station.getLinkToSource());
	    if (sourceLink != null && !onlineList.contains(sourceLink)) {
		Online online = createOnline(sourceLink, station.getRecordID(), "Source Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		onlineList.add(sourceLink);
	    }
	}

	if (station.getDataRecordID() != null && !station.getDataRecordID().equals("")) {
	    String doiLink = extractLink(station.getDataRecordID());
	    if (doiLink != null && !onlineList.contains(doiLink)) {
		Online online = createOnline(doiLink, station.getRecordID(), "DOI Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		onlineList.add(doiLink);
	    }
	}

	logger.debug("ENDED mapping for record number {} .", station.getRecordID());

	// Online online = new Online();
	// online.setProtocol(new FTPProtocol().getCommonURN());
	// online.setLinkage(splittedStrings[11]);
	// online.setName(variable + "@" + station.getName());
	// online.setFunctionCode("download");
	// online.setDescription(
	// "Global Thunder Day Count - Station name: " + station.getName() + " . Station code: " +
	// station.getStationCode());

	// coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

    }

    private Online createOnline(String link, String recordID, String description) {
	Online online = new Online();
	online.setProtocol(new HTTPProtocol().getCommonURN());
	online.setLinkage(link);
	online.setName(description + " for record number: " + recordID);
	online.setFunctionCode("download");
	online.setDescription(description + " for record number: " + recordID);
	return online;
    }

    public String extractLink(String dataLink) {
	// TODO several use case to be considered

	String data = dataLink.toLowerCase();

	String searchString = "";

	if (data.contains("https")) {
	    searchString = "https";
	} else if (data.contains("http")) {
	    searchString = "http";
	} else if (data.contains("www")) {
	    searchString = "www";
	} else if (data.contains("doi:")) {
	    searchString = "doi:";
	    String[] splittedString = data.split("doi:");

	    String doi = "";
	    if (splittedString[1].contains(" ")) {
		String[] splittedSpace = splittedString[1].split(" ");
		for (int i = 0; i < splittedSpace.length; i++) {
		    String tmp = splittedSpace[i].trim();
		    if (!tmp.isEmpty()) {
			doi = tmp;
			break;
		    }
		}
	    } else {
		doi = splittedString[1];
	    }
	    // if(splittedString[1].startsWith(" "))

	    // return "http://dx.doi.org/" + splittedString[1].split(" ")[0];
	    return "http://dx.doi.org/" + doi;
	}

	if (searchString.isEmpty())
	    return null;

	int idx = data.indexOf(searchString);
	String[] splittedString = data.substring(idx).split(" ");

	return splittedString[0];

    }

    public static void main(String[] args) {

	String data = "isccp_tovs_nat doi:  10.5067/isccp/tovs_nat";
	String[] splString = data.split("doi:");
	String[] ss = splString[1].split(" ");

	String data2 = "isccp_tovs_nat doi:  10.5067/isccp/tovs_nat aaq";
	String[] splString2 = data2.split("doi:");
	String[] ss2 = splString2[1].split(" ");

	TemporalExtent extent = new TemporalExtent();
	TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	extent.setIndeterminateEndPosition(endTimeInderminate);
	TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	extent.setIndeterminateBeginPosition(startIndeterminate);
	Calendar efd = Calendar.getInstance();
	efd.setTime(new Date());
	String value = ISO8601DateTimeUtils.getISO8601Date(efd.getTime());
	extent.setBeginPosition(value);

	Dataset dataset = new Dataset();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	// TemporalExtent timeExt = dataset.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	// String begin = timeExt.getBeginPosition();
	// String end = timeExt.getEndPosition();
	// TimeIndeterminateValueType indBegin = timeExt.getIndeterminateBeginPosition();
	// TimeIndeterminateValueType indEnd = timeExt.getIndeterminateEndPosition();

    }

}
