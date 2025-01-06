package eu.essi_lab.accessor.inmet;

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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class INMETMapper extends OriginalIdentifierMapper {

    private static final String INMETNETWORK = "INMET";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public INMETMapper() {
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
	return CommonNameSpaceContext.INMET_CSV_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	INMETStation station = new INMETStation();

	String[] splittedStrings = originalMetadata.split(",");

	station.setRegion(splittedStrings[0]);
	station.setState(splittedStrings[1]);
	station.setStationName(splittedStrings[2]);
	station.setWmoCode(splittedStrings[3]);
	station.setWigosId(splittedStrings[4]);
	station.setLatitude(splittedStrings[5]);
	station.setLongitude(splittedStrings[6]);
	station.setHeight(splittedStrings[7]);
	station.setFoundationDate(splittedStrings[8]);

	Date begin = null;
	Date end = null;

	if (splittedStrings[9] != null && splittedStrings[9] != "") {
	    station.setStartDate(splittedStrings[9]);
	}
	if (splittedStrings[10] != null && splittedStrings[10] != "") {
	    station.setEndDate(splittedStrings[10]);
	}

	station.setNameFile(splittedStrings[11]);

	INMETVariable variable = INMETVariable.PRECIPITATION;

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	if (station.getStartDate() != null && station.getEndDate() != null) {
	    TemporalExtent extent = new TemporalExtent();
	    extent.setEndPosition(normalizeDate(station.getEndDate()));
	    // TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	    extent.setBeginPosition(normalizeDate(station.getStartDate()));
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	}

	if (station.getHeight() != null && isDouble(station.getHeight())) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    Double vertical = Double.parseDouble(station.getHeight());
	    verticalExtent.setMinimumValue(vertical);
	    verticalExtent.setMaximumValue(vertical);
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	}

	if (splittedStrings[13] != null && !splittedStrings[13].isEmpty()) {
	    String expectedSize = splittedStrings[13];
	    //
	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    try {
		time.setDimensionSize(new BigInteger(expectedSize));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(Long.valueOf(expectedSize));
	    } catch (Exception e) {
	    }
	    grid.addAxisDimension(time);
	    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	}

	coreMetadata.setTitle(("Acquisitions at " + station.getStationName() + " - " + variable));
	coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + variable
		+ ") acquired by a specific observing station (" + station.getStationName()
		+ "). The time series has been published through the National Metereology Institute of Brazil system.");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("National Metereology Institute of Brazil");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Hydrology station");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("INMET");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(station.getFoundationDate());

	coreMetadata.getMIMetadata().getDataIdentification()
		.addKeyword("Brazilian region: " + station.getRegion() + "(State:" + station.getState() + ")");

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getStationName());

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getWmoCode());
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getRegion());
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getState());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getWigosId());

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	// String id = "PRECIPITATION_DATA_" + station.getWigosId() + "_" + station.getWmoCode();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	String lat = station.getLatitude();
	String lon = station.getLongitude();
	if (isDouble(lat) && isDouble(lon)) {

	    double serieslat = Double.parseDouble(lat);
	    double serieslon = Double.parseDouble(lon);

	    coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);
	}

	ResponsibleParty creatorContact = new ResponsibleParty();

	creatorContact.setOrganisationName("National Metereology Institute of Brazil");
	creatorContact.setRoleCode("author");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = "urn:wmo:wigos:" + station.getWigosId();

	platform.setMDIdentifierCode(platformIdentifier);

	String geographicLocation = "";
	String siteDescription = station.getStationName();

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(station.getStationName());
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String variableId = "urn:brazil-inmet:" + variable.toString();

	coverageDescription.setAttributeIdentifier(variableId);
	coverageDescription.setAttributeTitle(variable.toString());

	String attributeDescription = variable.toString() + " Units: " + variable.getUnit() + " Resolution: hourly";

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();
	onlineValues.setLinkage(splittedStrings[12]);
	String identifier = variableId + "@" + station.getStationName() + "@" + station.getNameFile();
	onlineValues.setName(identifier);
	onlineValues.setProtocol(CommonNameSpaceContext.INMET_CSV_URI);
	onlineValues.setFunctionCode("download");
	// online.setFunctionCode(function);
	onlineValues.setDescription("Hourly Precipitation Data");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

	String resourceIdentifier = generateCode(dataset, identifier);
	
	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	dataset.getExtensionHandler().setTimeSupport("-1");
	dataset.getExtensionHandler().setTimeUnits("hours");
	dataset.getExtensionHandler().setTimeUnitsAbbreviation("h");
	dataset.getExtensionHandler().setTimeResolution("1");
	dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	dataset.getExtensionHandler().setAttributeUnits(variable.getUnit());
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(variable.getAbbreviation());
	dataset.getExtensionHandler().setAttributeMissingValue("-9999");

	dataset.getExtensionHandler().setCountry(Country.BRAZIL.getShortName());

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
