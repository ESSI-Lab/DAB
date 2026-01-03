package eu.essi_lab.accessor.canada;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.adk.timeseries.TimeSeriesUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class CANADAMSCMapper extends OriginalIdentifierMapper {

    private static final String MSC_URN = "urn:dd.weather.gc.ca.hydrometric:station:";

    // public static final String SCHEMA_URI = "http://essi-lab.eu/EC/";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public CANADAMSCMapper() {
    }

    public enum Resolution {
	HOURLY, DAILY
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
	return CommonNameSpaceContext.ENVIRONMENT_CANADA_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	// MIMetadata mi_metadata =
	// dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	// MDMetadata md_metadata =dataset.getHarmonizedMetadata().getCoreMetadata();

	ECStation station = new ECStation();

	String[] splittedStrings = originalMetadata.split(",");

	station.setStationCode(splittedStrings[0]);
	station.setName(splittedStrings[1]);
	station.setLat(splittedStrings[2]);
	station.setLon(splittedStrings[3]);
	station.setProv(splittedStrings[4]);
	station.setTimezone(splittedStrings[5]);
	station.getValues().add(splittedStrings[6]);
	station.setStartDate(splittedStrings[9]);
	String resolutionString = splittedStrings[10];
	try {
	    long resolutionL = Long.parseLong(resolutionString);
	    station.setResolutionMs(resolutionL);
	} catch (Exception e) {
	}
	// station.getValues().add(splittedStrings[7]);
	// station.getValues().add(splittedStrings[8]);

	Resolution resolution = Resolution.HOURLY;

	if (splittedStrings[6].contains("daily")) {
	    resolution = Resolution.DAILY;
	}

	ECVariable variable = ECVariable.decode(splittedStrings[8]);

	Calendar efd = null;
	Calendar eld = null;

	efd = null;
	eld = null;

	switch (variable) {
	case DISCHARGE:

	    efd = station.getDischargeEstimatedFirstDate();
	    eld = station.getDischargeEstimatedLastDate();

	    break;

	case WATER_LEVEL:
	    efd = station.getWaterLevelEstimatedFirstDate();
	    eld = station.getWaterLevelEstimatedLastDate();

	    break;

	default:
	    break;
	}

	String abbreviation = station.getProvince();
	String province = "";
	switch (abbreviation) {
	case "AB":
	    province = "Alberta";
	    break;
	case "BC":
	    province = "British Columbia";
	    break;
	case "MB":
	    province = "Manitoba";
	    break;
	case "NB":
	    province = "New Brunswick";
	    break;
	case "NL":
	    province = "Newfoundland and Labrador";
	    break;
	case "NS":
	    province = "Nova Scotia";
	    break;
	case "NT":
	    province = "Northwest Territories";
	    break;
	case "NU":
	    province = "Nunavut";
	    break;
	case "ON":
	    province = "Ontario";
	    break;
	case "PE":
	    province = "Prince Edward Island";
	    break;
	case "QC":
	    province = "Quebec";
	    break;
	case "SK":
	    province = "Saskatchewan";
	    break;
	case "YT":
	    province = "Yukon";
	    break;
	default:
	    break;
	}

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	if (efd != null && eld != null) {
	    TemporalExtent extent = new TemporalExtent();
	    TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	    extent.setIndeterminateEndPosition(endTimeInderminate);
	    //
	    // TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	    // String beginTime = getStartTime(station.getStartDate());

	    Optional<Date> dateTime = ISO8601DateTimeUtils.parseISO8601ToDate(station.getStartDate());
	    if (dateTime.isPresent()) {
		//
		// String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(dateTime.get());
		// extent.setPosition(beginTime, startIndeterminate, false, true);

		if (resolution.equals(Resolution.DAILY)) {
		    extent.setBeforeNowBeginPosition(FrameValue.P1M);
		}

		if (resolution.equals(Resolution.HOURLY)) {
		    extent.setBeforeNowBeginPosition(FrameValue.P2D);
		}

		// Estimate of the data size
		// only an estimate seems to be possible, as this odata service doesn't seem to support the /$count
		// operator

		double expectedValuesPerHours = 12.0; // 1 value every 5 minutes
		double expectedValuesPerDay = expectedValuesPerHours * 24.0;

		long expectedSize = TimeSeriesUtils.estimateSize(dateTime.get(), new Date(), expectedValuesPerDay);

		GridSpatialRepresentation grid = new GridSpatialRepresentation();
		grid.setNumberOfDimensions(1);
		grid.setCellGeometryCode("point");
		Dimension time = new Dimension();
		time.setDimensionNameTypeCode("time");
		try {
		    time.setDimensionSize(new BigInteger("" + expectedSize));
		    ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		    extensionHandler.setDataSize(expectedSize);
		} catch (Exception e) {
		}
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	}
	// coreMetadata.setTitle("Acquisitions at " + station.getName());
	// coreMetadata
	// .setAbstract("This collection contains hydrology time series acquired by a specific observing station ("
	// + station.getName()
	// + "). The time series has been published through the Environment Canada system.");

	coreMetadata.setTitle((variable + "@" + station.getName()));
	coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + variable
		+ ") acquired by a specific observing station (" + station.getName()
		+ "). The time series has been published through the Environment Canada system.");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Environment Canada");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Hydrology station");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Canadian region: " + province + "(" + abbreviation + ")");

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getStationCode());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());

	if (resolution.equals(Resolution.DAILY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("daily");

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	// String id = CANADAMSCMapper.createDatasetId("ODM", station.getName(), ECNETWORK, station.getStationCode(),
	// splittedStrings[8]);

	//
	// URL + variable
	//
	// String id = UUID.nameUUIDFromBytes((splittedStrings[6] + splittedStrings[8]).getBytes()).toString();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String lat = station.getLat();
	String lon = station.getLon();

	if (isDouble(lat) && isDouble(lon)) {

	    double serieslat = Double.parseDouble(station.getLat());
	    double serieslon = Double.parseDouble(station.getLon());

	    coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);
	}

	ResponsibleParty creatorContact = new ResponsibleParty();

	creatorContact.setOrganisationName("Environment Canada");
	creatorContact.setRoleCode("author");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = MSC_URN + ":" + station.getStationCode();

	platform.setMDIdentifierCode(platformIdentifier);

	String geographicLocation = "";
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
	// the name is from the CUAHSI concepts controlled vocabulary
	String variableId = variable.name() + "_" + resolution.name();

	coverageDescription.setAttributeIdentifier("urn:dd.weather.gc.ca.hydrometric:variable:" + variableId);
	coverageDescription.setAttributeTitle(variable.getLabel());

	String attributeDescription = variable.toString() + " Units: " + variable.getUnitAbbreviation() + " Resolution: "
		+ resolution.toString();

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);

	Long resolutionMs = station.getResolutionMs();
	if (resolutionMs != null) {

	    if (resolutionMs % 1000 == 0) {
		long resolutionSecs = resolutionMs / 1000;

		if (resolutionSecs % 60 == 0) {
		    long resolutionMins = resolutionSecs / 60;
		    dataset.getExtensionHandler().setTimeUnits("min");
		    dataset.getExtensionHandler().setTimeResolution("" + resolutionMins);
		} else {
		    dataset.getExtensionHandler().setTimeUnits("s");
		    dataset.getExtensionHandler().setTimeResolution("" + resolutionSecs);
		}

	    } else {
		dataset.getExtensionHandler().setTimeUnits("ms");
		dataset.getExtensionHandler().setTimeResolution("" + resolutionMs);

	    }

	}

	// dataset.getExtensionHandler().setTimeSupport("0");

	// no need of time units, being time support 0

	// String timeUnits = series.getTimeScaleUnitName();
	// dataset.getExtensionHandler().setTimeUnits(timeUnits);

	// String timeUnitsAbbreviation = series.getTimeScaleUnitAbbreviation();
	// dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);

	// dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	dataset.getExtensionHandler().setAttributeUnits(variable.getUnitAbbreviation());

	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(variable.getUnitAbbreviation());

	dataset.getExtensionHandler().setCountry(Country.CANADA.getShortName());

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();
	onlineValues.setLinkage(splittedStrings[6]);
	onlineValues.setName(variableId + "@" + station.getName());
	onlineValues.setProtocol(NetProtocolWrapper.ECANADA.getCommonURN());
	onlineValues.setFunctionCode("download");
	onlineValues.setDescription("Real-time Hydrometric Data");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

    }

    private boolean isDouble(String str) {
	try {
	    // check if it can be parsed as any double
	    Double.parseDouble(str);
	    return true;
	    // short version: return x != (int) x;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    // something like: OD:Firenze@34982374
    // the hash is needed because different sources may have same site id, such as
    // ARPA Italy
    public static String createCollectionId(String siteNetwork, String siteCode, String url) {
	try {
	    if (siteNetwork != null && !siteNetwork.equals("") && !siteCode.contains(":")) {
		siteCode = siteNetwork + ":" + siteCode;
	    }
	    if (url != null) {
		String hash = "" + URLEncoder.encode(url, "UTF-8").hashCode();
		return siteCode + "@" + hash;
	    } else {
		return siteCode;
	    }

	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	return null;
    }

    // something like: ODM:TMin@OD:Firenze@34982374
    // the hash is needed because different sources may have same site id, such as
    // ARPA Italy
    public static String createDatasetId(String variableVocabulary, String variableCode, String siteNetwork, String siteCode, String url) {

	// out = UUID.nameUUIDFromBytes(indentifier.getBytes()).toString();

	try {
	    // if (variableVocabulary != null && !variableVocabulary.equals("") && !variableCode.contains(":")) {
	    // variableCode = variableVocabulary + ":" + variableCode;
	    // }
	    // if (siteNetwork != null && !siteNetwork.equals("") && !siteCode.contains(":")) {
	    // siteCode = siteNetwork + ":" + siteCode;
	    // }
	    if (url != null) {
		String hash = "" + url.hashCode();// URLEncoder.encode(url, "UTF-8").hashCode();
		return URLEncoder.encode(variableCode + "@" + siteCode + "@" + hash, "UTF-8");
	    } else {
		return URLEncoder.encode(variableCode + "@" + siteCode, "UTF-8");
	    }
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public static void main(String[] args) throws Exception, IOException {
	TemporalExtent extent = new TemporalExtent();
	TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	extent.setIndeterminateEndPosition(endTimeInderminate);
	TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	extent.setIndeterminateBeginPosition(startIndeterminate);// setBeginPosition(efd.getTime().toString());//setBeginPosition(efd.getTime());
	Calendar efd = Calendar.getInstance();
	efd.setTime(new Date());
	String value = ISO8601DateTimeUtils.getISO8601Date(efd.getTime());
	extent.setBeginPosition(value);
	// extent.setIndeterminateBeginPosition(startIndeterminate);
	Dataset dataset = new Dataset();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	TemporalExtent timeExt = dataset.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	String begin = timeExt.getBeginPosition();
	String end = timeExt.getEndPosition();
	TimeIndeterminateValueType indBegin = timeExt.getIndeterminateBeginPosition();
	TimeIndeterminateValueType indEnd = timeExt.getIndeterminateEndPosition();

    }

}
