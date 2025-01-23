package eu.essi_lab.accessor.thunder;

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
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.impl.FTPProtocol;
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
public class ThunderMapper extends OriginalIdentifierMapper {

    private static final String THUNDERNETWORK = "THUNDER";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public ThunderMapper() {
	// do nothing
    }

    public enum Resolution {
	YEARLY
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
	return CommonNameSpaceContext.THUNDER_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	ThunderStation station = new ThunderStation();

	String[] splittedStrings = originalMetadata.split(",");

	station.setStationCode(splittedStrings[0]);
	station.setName(splittedStrings[1]);
	station.setLat(splittedStrings[2]);
	station.setLon(splittedStrings[3]);
	station.setElevation(splittedStrings[4]);
	station.setStartDate(splittedStrings[5]);
	station.setEndDate(splittedStrings[6]);
	station.setIcao(splittedStrings[7]);
	station.setState(splittedStrings[8]);
	station.setCountry(splittedStrings[9]);

	Resolution resolution = Resolution.YEARLY;

	ThunderVariable variable = ThunderVariable.TD_COUNT;

	// TEMPORAL EXTENT
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	TemporalExtent extent = new TemporalExtent();
	if (station.getStartDate() != null) {

	    TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	    Optional<Date> startDateTime = null;

	    startDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(station.getStartDate());

	    if (startDateTime.isPresent()) {
		Date begin = startDateTime.get();
		String stringStart = ISO8601DateTimeUtils.getISO8601DateTime(begin);
		extent.setPosition(stringStart, startIndeterminate, false, true);
	    }

	    if (station.getEndDate() != null) {
		TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
		Optional<Date> endDateTime = null;

		endDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(station.getEndDate());

		if (endDateTime.isPresent()) {
		    Date end = endDateTime.get();
		    String stringEnd = ISO8601DateTimeUtils.getISO8601DateTime(end);
		    extent.setPosition(stringEnd, endTimeInderminate, false, false);
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

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	}
	if (!station.getName().equals("")) {
	    coreMetadata.setTitle("Acquisitions of " + variable + " (TD) at station: " + station.getName());
	    coreMetadata.setAbstract(
		    "This dataset contains Global Monthly Thunder Day (TD) time series acquired by a specific observing station ("
			    + station.getName() + "). The time series has been published processing data from NOAA GSOD database.");
	} else {
	    coreMetadata.setTitle("Acquisitions of " + variable + " (TD) at station: " + station.getStationCode());
	    coreMetadata.setAbstract(
		    "This dataset contains Global Monthly Thunder Day (TD) time series acquired by a specific observing station ("
			    + station.getStationCode() + "). The time series has been published processing data from NOAA GSOD database.");
	}
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(THUNDERNETWORK);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("TD");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	if (!station.getState().equals("")) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("State: " + station.getState());
	}
	if (!station.getCountry().equals("")) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Country: " + station.getCountry());
	}
	if (!station.getIcao().equals("")) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ICAO: " + station.getIcao());
	}

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getStationCode());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("GSOD");

	if (resolution.equals(Resolution.YEARLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("yearly");

	//
	// URL + variable
	//
	// String id = UUID.nameUUIDFromBytes((splittedStrings[0] + splittedStrings[11]).getBytes()).toString();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	// bounding box
	String lat = station.getLat();
	String lon = station.getLon();

	if (isDouble(lat) && isDouble(lon)) {

	    double serieslat = Double.parseDouble(station.getLat());
	    double serieslon = Double.parseDouble(station.getLon());

	    coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);
	}

	// elevation
	String elevation = station.getElevation();
	if (elevation != null && !elevation.equals("")) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    if (isDouble(elevation)) {
		verticalExtent.setMinimumValue(Double.parseDouble(elevation));
		verticalExtent.setMaximumValue(Double.parseDouble(elevation));
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    }

	}

	// contact point
	ResponsibleParty creatorContact = new ResponsibleParty();

	creatorContact.setOrganisationName("Tripura University");
	creatorContact.setRoleCode("originator");
	creatorContact.setIndividualName("Anirban Guha");

	Contact contactcreatorContactInfo = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	contactcreatorContactInfo.setAddress(address);
	creatorContact.setContactInfo(contactcreatorContactInfo);

	ResponsibleParty creatorTechContact = new ResponsibleParty();

	creatorTechContact.setOrganisationName("Tripura University");
	creatorTechContact.setRoleCode("pointOfContact");
	creatorTechContact.setIndividualName("Anirban Guha");

	Contact contactcreatorTechContactInfo = new Contact();
	Address addresscreatorTechContact = new Address();
	addresscreatorTechContact.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	contactcreatorTechContactInfo.setAddress(addresscreatorTechContact);
	creatorTechContact.setContactInfo(contactcreatorTechContactInfo);

	ResponsibleParty otherTechContact = new ResponsibleParty();

	otherTechContact.setOrganisationName("Tripura University");
	otherTechContact.setRoleCode("author");
	otherTechContact.setIndividualName("Prasanth S.");

	ResponsibleParty otherContact = new ResponsibleParty();

	otherContact.setOrganisationName("Massachusetts Institute of Technology (MIT)");
	otherContact.setRoleCode("author");
	otherContact.setIndividualName("Earle Williams");

	Contact contactotherContactInfo = new Contact();
	Address addressotherContact = new Address();
	addressotherContact.addElectronicMailAddress("ekagww@gmail.com");
	contactotherContactInfo.setAddress(addressotherContact);
	otherContact.setContactInfo(contactotherContactInfo);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(otherTechContact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorTechContact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(otherContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = THUNDERNETWORK + ":" + station.getStationCode();

	platform.setMDIdentifierCode(platformIdentifier);

	String siteDescription = station.getName();

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(station.getName());
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String variableId = variable.toString() + "_" + resolution.toString();

	coverageDescription.setAttributeIdentifier(variableId);
	coverageDescription.setAttributeTitle(variable.toString());

	String attributeDescription = variable.toString() + " Units: " + variable.getUnit() + " Resolution: " + resolution.toString();

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */

	Online online = new Online();
	online.setProtocol(new FTPProtocol().getCommonURN());
	online.setLinkage(splittedStrings[11]);
	online.setName(variable + "@" + station.getName());
	online.setFunctionCode("download");
	online.setDescription(
		"Global Thunder Day Count - Station name: " + station.getName() + " . Station code: " + station.getStationCode());

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

    }

    private boolean isDouble(String str) {
	try {
	    // check if it can be parsed as any double
	    Double.parseDouble(str);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static void main(String[] args) {
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
