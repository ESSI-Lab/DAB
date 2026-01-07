package eu.essi_lab.accessor.smhi;

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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Date;

import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author boldrini
 */
public class SMHIMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(getClass());
    private String endpoint;

    public SMHIMapper() {
    }

    public static String SMHI_URN = "se:smhi:opendata-download-hydroobs";

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	this.endpoint = source.getEndpoint();

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.SMHI_URI;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String metadataString = originalMD.getMetadata();
	ByteArrayInputStream bais = new ByteArrayInputStream(metadataString.getBytes());
	SMHIMetadata metadata = null;
	try {
	    metadata = SMHIMetadata.unmarshal(bais);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Error unmarshalling");
	}
	SMHIStation station = metadata.getStation();
	SMHIParameter parameter = metadata.getParameter();

	Date begin = null;
	Date end = null;

	Long from = station.getFrom();
	if (from != null) {
	    begin = new Date(from);
	}
	Long to = station.getTo();
	if (to != null) {
	    end = new Date(to);
	}

	SMHIIdentifierMangler mangler = new SMHIIdentifierMangler();
	mangler.setPlatformIdentifier(station.getKey());
	mangler.setParameterIdentifier(parameter.getKey());

	String id = mangler.getMangling();

	String metadataId = SMHI_URN+":"+id;
	dataset.setOriginalId(metadataId);
	dataset.setPrivateId(metadataId);
	dataset.setPublicId(metadataId);
	
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.setIdentifier(metadataId);
	
	TemporalExtent extent = new TemporalExtent();
	if (begin != null) {
	    extent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(begin));
	}
	if (end != null) {
	    extent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(end));
	}
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	setIndeterminatePosition(dataset);

	coreMetadata.setTitle(station.getName() + " - " + parameter.getTitle());
	coreMetadata.setAbstract("This dataset contains a time series of a specific variable (" + parameter.getTitle() + ") acquired by "
		+ station.getOwner() + " station " + station.getName() + ". The catchment is " + station.getCatchmentName());

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(parameter.getTitle());

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	BigDecimal lat = station.getLatitude();
	BigDecimal lon = station.getLongitude();
	coreMetadata.addBoundingBox(lat, lon, lat, lon);

	ResponsibleParty ownerContact = new ResponsibleParty();

	ownerContact.setOrganisationName(station.getOwner());
	ownerContact.setRoleCode("owner");
	Contact contact = new Contact();
	Address address = new Address();
	// address.addElectronicMailAddress("");
	contact.setAddress(address);
	ownerContact.setContactInfo(contact);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(ownerContact);

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(SMHI_URN + ":station:" + station.getKey());

	platform.setDescription(station.getSummary());

	Citation platformCitation = new Citation();
	platformCitation.setTitle(station.getName());
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(SMHI_URN + ":parameter:" + parameter.getKey());
	coverageDescription.setAttributeTitle(parameter.getTitle());

	String attributeDescription = parameter.getTitle() + " Units: " + parameter.getUnits();

	dataset.getExtensionHandler().setAttributeUnits(parameter.getUnits());
	dataset.getExtensionHandler().setAttributeUnitsAbbreviation(parameter.getUnits());

	dataset.getExtensionHandler().setCountry(Country.SWEDEN.getShortName());

	coverageDescription.setAttributeDescription(attributeDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);

	/**
	 * ONLINE
	 */

	Online onlineValues = new Online();
	onlineValues.setLinkage(endpoint);
	onlineValues.setName(id);
	onlineValues.setProtocol(CommonNameSpaceContext.SMHI_URI);
	onlineValues.setFunctionCode("download");

	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(onlineValues);

    }

}
