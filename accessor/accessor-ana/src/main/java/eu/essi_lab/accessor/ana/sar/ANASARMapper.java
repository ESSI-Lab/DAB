package eu.essi_lab.accessor.ana.sar;

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

import java.math.BigInteger;
import java.util.Date;

import org.json.JSONObject;

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
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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

public class ANASARMapper extends OriginalIdentifierMapper {

    static final String ANA_SAR = "br.gov.ana.sar";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.ANA_SAR_URI;
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
	JSONObject json = new JSONObject(originalMetadata);

	ANASARStation station = new ANASARStation(json);

	String reservoirId = station.getReservoirId();
	String reservoirName = station.getReservoirName();
	String reservoirCapacity = station.getReservoirCapacity();
	String latitude = station.getLatitude();
	String longitude = station.getLongitude();
	String municipality = station.getMunicipality();
	String state = station.getState();
	String stateAbbreviation = station.getStateAbbreviation();
	String basinName = station.getBasinName();
	String network = station.getNetwork();
	String variable = station.getVariable();
	String variableUnits = station.getVariableUnits();
	String startDate = station.getStartDate();
	String endDate = station.getEndDate();
	String resolutionMs = station.getResolutionMs();

	String networkName = network;
	if (networkName == null) {
	    networkName = "unknown";
	}
	switch (networkName) {
	case "1":
	    networkName = "Nordeste e Semiárido";
	    break;
	case "2":
	    networkName = "SIN - Sistema Interligado Nacional";
	    break;
	case "3":
	    networkName = "Sistema Cantareira";
	    break;
	default:
	    networkName = "Network " + networkName;
	    break;
	}
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	coreMetadata.setTitle("Acquisitions at reservoir " + reservoirName + " - " + variable);
	String abs = "Acquisitions at reservoir " + reservoirName + " - " + variable + ". The telemetry station is from network: "
		+ networkName + " in the basin " + basinName + ". The municipality is " + municipality + " in the state: " + state + " ("
		+ stateAbbreviation + ").";
	if (reservoirCapacity != null && !reservoirCapacity.equals("null")) {
	    abs += " The reservoir capacity is " + reservoirCapacity + " " + ANASARVariable.CAPACIDADE.getUnits();
	}
	coreMetadata.setAbstract(abs);

	TemporalExtent extent = new TemporalExtent();

	extent.setBeginPosition(startDate);

	extent.setEndPosition(endDate);

	Long expectedSize = null;

	Long resolution = null;
	try {
	    resolution = Long.parseLong(resolutionMs);

	    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(startDate).get();
	    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(endDate).get();
	    double expectedValuesPerDay = 1000 * 60 * 60 * 24. / resolution;
	    expectedSize = TimeSeriesUtils.estimateSize(begin, end, expectedValuesPerDay);

	} catch (Exception e) {
	    // TODO: handle exception
	}

	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	String resolutionString = "";
	if (resolution != null) {
	    ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	    extensionHandler.setTimeResolution("" + resolution);
	    extensionHandler.setTimeUnits("ms");
	    setIndeterminatePosition(dataset, resolution * 10);
	    resolutionString = ":resolution:" + resolution;
	}

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

	// bbox

	double serieslat = Double.parseDouble(latitude);
	double serieslon = Double.parseDouble(longitude);

	coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);

	ResponsibleParty creatorContact = new ResponsibleParty();
	Contact info = new Contact();
	Online online = new Online();
	online.setLinkage("https://www.ana.gov.br/sar0/");
	info.setOnline(online);
	creatorContact.setContactInfo(info);
	creatorContact.setOrganisationName("SAR - Agência Nacional de Águas (ANA)");
	creatorContact.setRoleCode("pointOfContact");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = "urn:" + ANA_SAR + ":reservoir:" + reservoirId;

	platform.setMDIdentifierCode(platformIdentifier);

	platform.setDescription(reservoirName);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(reservoirName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();

	// dataset.getExtensionHandler().setTimeUnits("hour");
	// dataset.getExtensionHandler().setTimeUnits("h");

	InterpolationType interpolation;
	

	String variableId = "urn:" + ANA_SAR + ":parameter:" + variable + resolutionString;

	coverageDescription.setAttributeIdentifier(variableId);
	String variableTitle;
	switch (variable) {
	case "cota":
	    variableTitle = "Level";
	    interpolation = InterpolationType.CONTINUOUS;
	    break;
	case "capacidade":
	    variableTitle = "Total reservoir capacity";
	    interpolation = InterpolationType.CONST;
	    break;
	case "defluencia":
	    variableTitle = "Outflow discharge (from the reservoir)";
	    interpolation = InterpolationType.AVERAGE;
	    dataset.getExtensionHandler().setTimeSupport("1");
	    dataset.getExtensionHandler().setTimeUnits("day");
	    break;
	case "afluencia":
	    variableTitle = "Inflow discharge (to the reservoir)";
	    interpolation = InterpolationType.AVERAGE;
	    dataset.getExtensionHandler().setTimeSupport("1");
	    dataset.getExtensionHandler().setTimeUnits("day");
	    
	    break;
	case "volume":
	    variableTitle = "Reservoir storage";
	    interpolation = InterpolationType.CONTINUOUS;
	    break;
	case "volumeUtil":
	    variableTitle = "Volume";
	    interpolation = InterpolationType.CONTINUOUS;
	    break;
	default:
	    variableTitle = variable;
	    interpolation = InterpolationType.CONTINUOUS;
	    break;
	}
	coverageDescription.setAttributeTitle(variableTitle);

	dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	dataset.getExtensionHandler().setAttributeUnits(variableUnits);
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(variableUnits);
	
	String attributeDescription = variable;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */

	ANASARIdentifierMangler mangler = new ANASARIdentifierMangler();
	mangler.setParameterIdentifier(variable);
	mangler.setPlatformIdentifier(reservoirId);

	Online online2 = new Online();
	online2.setLinkage(dataset.getSource().getEndpoint());
	online2.setName(mangler.getMangling());
	online2.setProtocol(CommonNameSpaceContext.ANA_SAR_URI);
	online2.setFunctionCode("download");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online2);
	
	String resourceIdentifier = generateCode(dataset, mangler.getMangling());
	
	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);


	// restore OriginalMetadata
	
	dataset.getExtensionHandler().setCountry(Country.BRAZIL.getShortName());
    }

}
