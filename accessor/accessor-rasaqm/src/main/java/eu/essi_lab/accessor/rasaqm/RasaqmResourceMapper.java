package eu.essi_lab.accessor.rasaqm;

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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

public class RasaqmResourceMapper extends OriginalIdentifierMapper {

    private static final String RASAQM_RESOURCE_MAPPER_ERROR = "RASAQM_RESOURCE_MAPPER_ERROR";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.RASAQM_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	Dataset dataset = new Dataset();
	dataset.setSource(source);

	String metadata = originalMD.getMetadata();

	ByteArrayInputStream bais = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));
	RasaqmSeries series;
	try {
	    series = RasaqmSeries.unmarshal(bais);
	    bais.close();
	} catch (Exception e) {
	     
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RASAQM_RESOURCE_MAPPER_ERROR, //
		    e);
	}

	Date end = new Date();
	Date begin = new Date(end.getTime() - 1000 * 60 * 60 * 24 * 30l); // last 30 days by default

	String parameterId = series.getParameterId();
	String parameterName = series.getParameterName();
	String stationId = series.getStationName();
	String stationName = series.getStationName();
	String unit = series.getUnits();

	RasaqmIdentifierMangler mangler = new RasaqmIdentifierMangler();
	mangler.setPlatformIdentifier(stationId);
	mangler.setParameterIdentifier(parameterId);
	mangler.setLatitude("" + series.getLatitude());
	mangler.setLongitude("" + series.getLongitude());

	String id = mangler.getMangling();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	if (begin != null && end != null) {
	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(begin));
	    extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(end));
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    Dimension time = new Dimension();
	    time.setDimensionNameTypeCode("time");
	    try {

		time.setDimensionSize(new BigInteger("1000"));
		ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		extensionHandler.setDataSize(1000l);
		grid.addAxisDimension(time);
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	    } catch (Exception e) {
	    }

	}

	coreMetadata.setTitle((parameterName + " at RASAQM station " + stationName));
	coreMetadata.setAbstract("This dataset contains an air quality time series of a specific variable (" + parameterName
		+ ") acquired by RASAQM station " + stationName);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("RASAQM station " + stationName);

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	Double lat = series.getLatitude().doubleValue();
	Double lon = series.getLongitude().doubleValue();
	coreMetadata.addBoundingBox(lat, lon, lat, lon);

	ResponsibleParty creatorContact = new ResponsibleParty();
	creatorContact.setIndividualName("Valery S. Kosykh");
	creatorContact.setOrganisationName("Research Production Association \"Typhoon\"");
	creatorContact.setRoleCode("author");
	Contact contact = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress("vsk@feerc.ru");
	contact.setAddress(address);
	creatorContact.setContactInfo(contact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationId);

	platform.setDescription(stationId);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(parameterId);
	coverageDescription.setAttributeTitle(parameterName);

	String attributeDescription = parameterName + " Units: " + unit;

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();
	onlineValues.setLinkage("http://www.feerc.ru/geoss/rasaqm");
	onlineValues.setName(id);
	onlineValues.setProtocol(CommonNameSpaceContext.RASAQM_URI);
	onlineValues.setFunctionCode("download");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

	// LegalConstraints lc = new LegalConstraints();
	// lc.addOtherConstraints("Norwegian Licence for Open Government Data (NLOD)");
	// coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(lc);

	return dataset;
    }

}
