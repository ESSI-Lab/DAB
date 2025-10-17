package eu.essi_lab.accessor.emodnet;

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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.json.JSONObject;

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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class ERDDAPRiverMapper extends FileIdentifierMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EMODNET_PHYSICS_RIVER_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	try {
	    mapMetadata(originalMD, dataset);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) throws Exception {

	String metadata = originalMD.getMetadata();

	JSONObject obj;
	try {
	    obj = new JSONObject(metadata);
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "ERROR", //
		    e//
	    );
	}

	ERDDAPRow row = new ERDDAPRow(obj);
	String stationCode = row.getValue("PLATFORMCODE").toString();
	String stationName = row.getValue("call_name").toString();
	BigDecimal latitude = new BigDecimal(row.getValue("latitude").toString());
	BigDecimal longitude = new BigDecimal(row.getValue("longitude").toString());
	String dataFeatureType = row.getValue("DataFeatureType").toString();
	String firstDateObservation = row.getValue("firstDateObservation").toString();
	String lastDateObservation = row.getValue("lastDateObservation").toString();
	String parametersGroupLongname = row.getValue("parameters_group_longname").toString();
	String parametersGroupP02 = row.getValue("parameters_group_P02").toString();
	String parameters = row.getValue("parameters").toString();
	String parametersP01 = row.getValue("parameters_P01").toString();
	String wmo = row.getValue("WMO") != null ? row.getValue("WMO").toString() : null;
	String bestPracticesDoi = row.getValue("BEST_PRACTICES_DOI").toString();
	String dataDoi = row.getValue("DATA_DOI").toString();
	String dataOwnerLongname = row.getValue("data_owner_longname").toString();
	String dataOwnerCountryCode = row.getValue("data_owner_country_code").toString();
	String dataOwnerCountryLongname = row.getValue("data_owner_country_longname").toString();
	Integer dataOwnerEdmo = Integer.valueOf(row.getValue("data_owner_EDMO").toString());
	String dataAssemblyCenterLongname = row.getValue("data_assembly_center_longname").toString();
	String platformTypeLongname = row.getValue("platform_type_longname").toString();
	String platformTypeSdnl06 = row.getValue("platform_type_SDNL06").toString();
	String platformPageLink = row.getValue("platformpage_link").toString();
	String integratorId = row.getValue("integrator_id").toString();

	ERDDAPRiverIdentifierMangler mangler = new ERDDAPRiverIdentifierMangler();
	// here the URN is not needed
	mangler.setPlatformIdentifier(stationCode);
	mangler.setParameterIdentifier(parameters);

	String id = mangler.getMangling();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(firstDateObservation);
	Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(lastDateObservation);

	if (optionalBegin.isPresent() && optionalEnd.isPresent()) {
	    TemporalExtent extent = new TemporalExtent();
	    extent.setBeginPosition(firstDateObservation);
	    extent.setEndPosition(lastDateObservation);
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	    setIndeterminatePosition(dataset);

	    String title = "Riverine discharge of water at EMODNet station " + stationName;
	    coreMetadata.setTitle(title);
	    coreMetadata.setAbstract(title);

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(wmo);

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    coreMetadata.addBoundingBox(latitude, longitude, latitude, longitude);

	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName("EMODNet Physics");
	    publisherContact.setRoleCode("publisher");
	    Contact contact = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress("helpdesk@emodnet.ec.europa.eu");
	    contact.setAddress(address);
	    publisherContact.setContactInfo(contact);
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	    ResponsibleParty originatorContact = new ResponsibleParty();
	    originatorContact.setOrganisationName(dataOwnerLongname);
	    originatorContact.setRoleCode("originator");

	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(originatorContact);

	    if (dataOwnerCountryCode != null && !dataOwnerCountryCode.isEmpty()) {
		dataset.getExtensionHandler().setCountry(dataOwnerCountryCode);
	    } else if (dataOwnerCountryLongname != null && !dataOwnerCountryLongname.isEmpty()) {
		dataset.getExtensionHandler().setCountry(dataOwnerCountryLongname);
	    }
	    
	    dataset.getExtensionHandler().setAttributeUnits("m3/s");

	    /**
	     * MIPLATFORM
	     **/

	    MIPlatform platform = new MIPlatform();

	    platform.setMDIdentifierCode("EMODNET:" + stationCode);

	    platform.setDescription(stationName);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(stationName);
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    /**
	     * COVERAGEDescription
	     **/

	    CoverageDescription coverageDescription = new CoverageDescription();

	    coverageDescription.setAttributeIdentifier("EMODNET:variable:" + parameters);
	    coverageDescription.setAttributeTitle("Riverine discharge of water");

	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    /**
	     * ONLINE
	     */

	    String url = originalMD.getAdditionalInfo().get("url", String.class);

	    Online online = new Online();
	    online.setIdentifier();
	    online.setLinkage(url);
	    online.setName(id);
	    online.setProtocol(CommonNameSpaceContext.EMODNET_PHYSICS_RIVER_NS_URI);
	    online.setFunctionCode("download");

	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	}

    }

}
