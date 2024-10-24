package eu.essi_lab.accessor.rihmi;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 */
public class RIHMIMapper extends OriginalIdentifierMapper {

    private static final String RIHMI_URN = "urn:rihmi:station:";
    private String endpoint;

    public RIHMIMapper() {
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	this.endpoint = source.getEndpoint();
	if (this.endpoint.contains("?")) {
	    this.endpoint = this.endpoint.substring(0, this.endpoint.indexOf("?") + 1);
	}

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.RIHMI_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String metadata = originalMD.getMetadata();

	ByteArrayInputStream bais = new ByteArrayInputStream(metadata.getBytes());
	RIHMIMetadata md = RIHMIMetadata.unmarshal(bais);
	try {
	    bais.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	Date begin = md.getBegin();
	Date end = md.getEnd();
	Double lat = md.getLatitude();
	Double lon = md.getLongitude();
	String parameterId = md.getParameterId();
	String parameterName = md.getParameterName();
	String stationId = md.getStationId();
	String stationName = md.getStationName();
	String units = md.getUnits();

	InterpolationType interpolation = md.getInterpolation();
	String aggregationDuration = md.getAggregationDuration();

	RIHMIIdentifierMangler mangler = new RIHMIIdentifierMangler();
	mangler.setPlatformIdentifier(stationId);
	mangler.setParameterIdentifier(parameterId);

	String id = mangler.getMangling();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	if (begin != null && end != null) {
	    TemporalExtent extent = new TemporalExtent();
	    if (interpolation == null) {
		TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
		extent.setIndeterminateEndPosition(endTimeInderminate);
		extent.setBeforeNowBeginPosition(FrameValue.P1Y);
	    } else {
		extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(begin));
		extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(end));
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    // try {
	    //
	    // time.setDimensionSize(new BigInteger("" + size));
	    // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	    // extensionHandler.setDataSize(size);
	    // grid.addAxisDimension(time);
	    // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	    // } catch (Exception e) {
	    // }

	}

	String interpolationString = "";
	if (interpolation != null) {
	    if (interpolation.equals(InterpolationType.AVERAGE_SUCC)) {
		interpolationString = "(" + aggregationDuration + " average)";
	    } else {
		interpolationString = "(" + aggregationDuration + " " + interpolation + ")";
	    }
	}

	coreMetadata.setTitle((parameterName + " acquisitions " + interpolationString + " at station " + stationName));
	coreMetadata.setAbstract("This dataset contains a hydrology time series of a specific variable (" + parameterName
		+ ") acquired by RIHMI-WDC station " + stationName);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(stationName);

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	coreMetadata.addBoundingBox(lat, lon, lat, lon);

	ResponsibleParty creatorContact = new ResponsibleParty();

	creatorContact.setOrganisationName("RIHMI-WDC");
	creatorContact.setRoleCode("pointOfContact");
	Contact contact = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("shevchen2007@yandex.ru");
	contact.setAddress(address);
	creatorContact.setContactInfo(contact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(RIHMI_URN + stationId);

	platform.setDescription(stationName);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier("urn:ru:meteo:ws:variable:" + parameterName + interpolationString);
	coverageDescription.setAttributeTitle(parameterName);

	String attributeDescription = parameterName + " Units: " + units;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();

	onlineValues.setName(id);
	if (interpolation != null) {
	    onlineValues.setProtocol(CommonNameSpaceContext.RIHMI_HISTORICAL_URI);
	    onlineValues.setLinkage(RIHMIClient.historicalEndpoint + stationId);
	} else {
	    onlineValues.setProtocol(CommonNameSpaceContext.RIHMI_URI);
	    onlineValues.setLinkage(endpoint);
	}

	onlineValues.setFunctionCode("download");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

	if (interpolation != null) {
	    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	}
	if (aggregationDuration != null && !aggregationDuration.isEmpty()) {
	    if (aggregationDuration.equals("P1M")) {
		dataset.getExtensionHandler().setTimeUnits("month");
		dataset.getExtensionHandler().setTimeSupport("1");
	    } else {
		GSLoggerFactory.getLogger(getClass()).error("Unrecognized aggregation duration: {}", aggregationDuration);
	    }
	}
	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);
	// dataset.getExtensionHandler().setTimeUnits(timeUnits);
	// dataset.getExtensionHandler().setTimeUnitsAbbreviation(timeUnitsAbbreviation);
	// dataset.getExtensionHandler().setAttributeMissingValue(missingValue);
	dataset.getExtensionHandler().setAttributeUnits(units);
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(units);

	dataset.getExtensionHandler().setCountry(Country.RUSSIAN_FEDERATION.getShortName());

	// LegalConstraints lc = new LegalConstraints();
	// lc.addOtherConstraints("Norwegian Licence for Open Government Data (NLOD)");
	// coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);

    }

}
