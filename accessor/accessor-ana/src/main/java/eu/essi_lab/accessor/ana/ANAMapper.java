package eu.essi_lab.accessor.ana;

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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.adk.timeseries.TimeSeriesUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
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
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author roncella
 */
public class ANAMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String BASIN_KEY = "basin";
    private static final String SUB_BASIN_KEY = "sub-basin";
    private static final String PARAMETER_KEY = "parameter";
    private static final String STATION_KEY = "station";

    private static final String ANA_SEPARATOR_STRING = "ANA_SEPARATOR_STRING";

    private static final String ANA = "ANA";

    private ANAConnector connector = new ANAConnector();

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ANA_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	String[] splittedString = originalMetadata.split(ANA_SEPARATOR_STRING);

	StationDocument station;
	try {

	    ANAVariable decodedVariable = ANAVariable.decode(splittedString[1]);
	    long startDate = Long.parseLong(splittedString[2]);
	    long endDate = Long.parseLong(splittedString[3]);
	    station = new StationDocument(new ByteArrayInputStream(splittedString[0].getBytes(StandardCharsets.UTF_8)), decodedVariable);

	    String variable = splittedString[1];

	    String altitude = station.getAltitude();

	    String lat = station.getLatitude();
	    String lon = station.getLongitude();
	    String city = station.getCity();
	    String origin = station.getOrigin();
	    String riverCode = station.getRiverCode();
	    String riverName = station.getRiverName();

	    String stationCode = station.getStationCode();
	    String stationName = station.getStationName();
	    String basin = station.getBasin();
	    String subBasin = station.getSubBasin();

	    // ParameterInfo parameterInfo = connector.getParameterInfo(stationCode, dataset.getSource().getEndpoint());

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // Iterator<String> iterator = parameterInfo.getParameters().iterator();

	    // String variable = null;
	    //
	    // if (iterator.hasNext()) {
	    // variable = iterator.next();
	    // }
	    // int size = parameterInfo.getParameters().size();

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    coreMetadata.setTitle("Acquisitions at " + stationName + "-" + variable);
	    coreMetadata.setAbstract("Acquisitions at " + stationName + " - " + variable + ". The telemetry station is from sub basin "
		    + subBasin + " (" + basin + ")");

	    // TEMPORAL EXTENT

	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(new Date(startDate)));
	    extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(new Date(endDate)));
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	    setIndeterminatePosition(dataset);
	    
	    
	   
	    // Estimate of the data size
	    // only an estimate seems to be possible, as this odata service doesn't seem to
	    // support the /$count
	    // operator

	    double expectedValuesPerHours = 1.0; // 1 value every hour
	    double expectedValuesPerDay = expectedValuesPerHours * 24.0;

	    long expectedSize = TimeSeriesUtils.estimateSize(new Date(startDate), new Date(endDate), expectedValuesPerDay);

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

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	    // bbox
	    if (isDouble(lat) && isDouble(lon)) {

		double serieslat = Double.parseDouble(lat);
		double serieslon = Double.parseDouble(lon);

		coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);
	    }

	    // vertical
	    try {
		VerticalExtent verticalExtent = new VerticalExtent();
		Double vertical = Double.parseDouble(altitude);
		verticalExtent.setMinimumValue(vertical);
		verticalExtent.setMaximumValue(vertical);
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    } catch (Exception e) {
		logger.warn("Unable to parse site elevation: " + altitude + " " + e.getMessage());
	    }

	    ResponsibleParty creatorContact = new ResponsibleParty();
	    Contact info = new Contact();
	    Online online = new Online();
	    online.setLinkage("https://www.ana.gov.br/");
	    info.setOnline(online);
	    creatorContact.setContactInfo(info);
	    creatorContact.setOrganisationName("National Water Agency of Brazil");
	    creatorContact.setRoleCode("author");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	    /**
	     * MIPLATFORM
	     **/

	    MIPlatform platform = new MIPlatform();

	    String platformIdentifier = "urn:" + ANA + ":" + station.getStationCode();

	    platform.setMDIdentifierCode(platformIdentifier);

	    platform.setDescription(stationName);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    /**
	     * COVERAGEDescription
	     **/

	    CoverageDescription coverageDescription = new CoverageDescription();

	    dataset.getExtensionHandler().setTimeUnits("hour");
	    dataset.getExtensionHandler().setTimeUnits("h");

	    dataset.getExtensionHandler().setTimeInterpolation(decodedVariable.getInterpolation());
	    dataset.getExtensionHandler().setAttributeUnits(decodedVariable.getUnit());
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(decodedVariable.getAbbreviation());

	    if (decodedVariable.equals(ANAVariable.CHUVA)) {
		dataset.getExtensionHandler().setTimeSupport("1");

	    }else {
		dataset.getExtensionHandler().setTimeResolution("1");
	    }

	    String variableId = variable + "_HOURLY";

	    coverageDescription.setAttributeIdentifier("urn:brazil-ana:" + variableId);
	    coverageDescription.setAttributeTitle(variable);

	    String attributeDescription = variable;

	    coverageDescription.setAttributeDescription(attributeDescription);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    /**
	     * ONLINE
	     */

	    Online online2 = new Online();
	    // online.setProtocol(protocol);
	    online2.setLinkage(dataset.getSource().getEndpoint());
	    String identifier = variable + "@" + stationCode + "@" + stationName;
	    online2.setName(identifier);
	    online2.setProtocol(CommonNameSpaceContext.ANA_URI);
	    online2.setFunctionCode("download");
	    online2.setDescription("Service Description - Home Page");

	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online2);

	    String resourceIdentifier = generateCode(dataset, identifier);
	    
	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	    
	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	    // restore OriginalMetadata
	    // OriginalMetadata omMetadata = new OriginalMetadata();
	    // dataset.setOriginalMetadata(new OriginalMetadata();

	} catch (SAXException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	
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

}
