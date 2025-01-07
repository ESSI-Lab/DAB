package eu.essi_lab.profiler.thredds.catalog;

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

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.thredds._1_0_6.ControlledVocabulary;
import eu.essi_lab.thredds._1_0_6.DatasetType;
import eu.essi_lab.thredds._1_0_6.DateTypeFormatted;
import eu.essi_lab.thredds._1_0_6.DocumentationType;
import eu.essi_lab.thredds._1_0_6.GeospatialCoverage;
import eu.essi_lab.thredds._1_0_6.Metadata;
import eu.essi_lab.thredds._1_0_6.Property;
import eu.essi_lab.thredds._1_0_6.SourceType;
import eu.essi_lab.thredds._1_0_6.SourceType.Contact;
import eu.essi_lab.thredds._1_0_6.SpatialRange;
import eu.essi_lab.thredds._1_0_6.TimeCoverageType;
import eu.essi_lab.thredds._1_0_6.factory.JAXBTHREDDS;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class THREDDSCatalogResultSetMapper extends DiscoveryResultSetMapper<JAXBElement<DatasetType>> {

    @Override
    public MappingSchema getMappingSchema() {
	MappingSchema map = new MappingSchema();
	map.setEncoding("THREDDS");
	map.setEncodingMediaType(MediaType.APPLICATION_XML_TYPE);
	map.setEncodingVersion("1.0.6");
	return map;
    }

    @Override
    public Provider getProvider() {
	return Provider.essiLabProvider();
    }

    @Override
    public JAXBElement<DatasetType> map(DiscoveryMessage message, GSResource resource) throws GSException {
	DatasetType dt = new DatasetType();
	String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	dt.setName(title);
	dt.setHarvest(true);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
	List<DataComplianceReport> reports = handler.getReports();
	if (reports != null && !reports.isEmpty()) {
	    String id = "";
	    for (DataComplianceReport report : reports) {
		DataComplianceTest target = report.getLastSucceededTest();
		id = report.getOnlineId();
		if (target.equals(DataComplianceTest.EXECUTION)) {
		    break;
		}
	    }
	    id = id.replace("urn:uuid:", "urn-uuid-");
	    dt.setID(id);
	    dt.setUrlPath(id);
	}

	DocumentationType documentation = new DocumentationType();
	documentation.setDocumentationType("summary");
	documentation.getContent().add(resource.getHarmonizedMetadata().getCoreMetadata().getAbstract());
	dt.getThreddsMetadataGroup().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeDocumentation(documentation));
	Metadata metadata = JAXBTHREDDS.getInstance().getFactory().createMetadata();
	metadata.setInherited(true);
	if (resource.getPropertyHandler().isGrid()) {
	    metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeDataType("GRID"));
	    metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeDataFormat("NetCDF"));
	    metadata.getAny().add(createProperty("featureType", "GRID"));
	}
	SourceType source = new SourceType();
	ControlledVocabulary vocabulary = new ControlledVocabulary();
	List<ResponsibleParty> parties = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification()
		.getPointOfContactParty();
	parties.addAll(resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getCitationResponsibleParties());

	for (ResponsibleParty party : parties) {

	    vocabulary.setValue(party.getOrganisationName());
	    Contact contact = new Contact();
	    eu.essi_lab.iso.datamodel.classes.Contact partyContact = party.getContact();
	    if (partyContact != null) {
		Address partyAddress = partyContact.getAddress();
		if (partyAddress != null) {
		    contact.setEmail(partyAddress.getElectronicMailAddress());
		}
		Online online = partyContact.getOnline();
		if (online != null) {
		    contact.setUrl(online.getLinkage());
		}
	    }
	    source.setContact(contact);

	    source.setName(vocabulary);

	    switch (party.getRoleCode()) {

	    case "creator":
	    case "originator":
	    case "principalnvestigator":
		metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeCreator(source));
		break;
	    case "publisher":
	    default:
		metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypePublisher(source));
		break;
	    }

	}

	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getGeographicBoundingBox();
	if (bbox != null) {
	    GeospatialCoverage geoCoverage = new GeospatialCoverage();
	    SpatialRange ewRange = new SpatialRange();
	    ewRange.setStart(bbox.getWest());
	    ewRange.setUnits("degrees_east");
	    ewRange.setSize(bbox.getEast() - bbox.getWest());
	    geoCoverage.setEastwest(ewRange);
	    SpatialRange nsRange = new SpatialRange();
	    nsRange.setStart(bbox.getSouth());
	    nsRange.setUnits("degrees_north");
	    nsRange.setSize(bbox.getNorth() - bbox.getSouth());
	    geoCoverage.setNorthsouth(nsRange);
	    metadata.getAny().add(geoCoverage);
	}
	TemporalExtent timeExtent = resource.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getTemporalExtent();
	if (timeExtent != null) {
	    TimeCoverageType timeCoverage = new TimeCoverageType();
	    String begin = timeExtent.getBeginPosition();
	    if (timeExtent.getBeforeNowBeginPosition().isPresent()) {
		FrameValue before = timeExtent.getBeforeNowBeginPosition().get();
		Date date = new Date(new Date().getTime() - before.asMillis());
		begin = ISO8601DateTimeUtils.getISO8601DateTime(date);
	    }
	    if (begin != null) {
		DateTypeFormatted dtf = new DateTypeFormatted();
		dtf.setValue(begin);
		timeCoverage.getStartOrEndOrDuration().add(JAXBTHREDDS.getInstance().getFactory().createTimeCoverageTypeStart(dtf));
	    }
	    String end = timeExtent.getEndPosition();
	    if (timeExtent.getIndeterminateEndPosition() != null) {
		if (timeExtent.getIndeterminateEndPosition() == TimeIndeterminateValueType.NOW) {
		    Date date = new Date();
		    end = ISO8601DateTimeUtils.getISO8601DateTime(date);
		}
	    }
	    if (end != null) {
		DateTypeFormatted dtf = new DateTypeFormatted();
		dtf.setValue(end);
		timeCoverage.getStartOrEndOrDuration().add(JAXBTHREDDS.getInstance().getFactory().createTimeCoverageTypeEnd(dtf));
	    }
	    metadata.getAny().add(JAXBTHREDDS.getInstance().getFactory().createDatasetTypeTimeCoverage(timeCoverage));
	}
	dt.getThreddsMetadataGroup().add(metadata);
	JAXBElement<DatasetType> ret = JAXBTHREDDS.getInstance().getFactory().createDataset(dt);
	return ret;
    }

    private Property createProperty(String name, String value) {
	Property ret = new Property();
	ret.setName(name);
	ret.setValue(value);
	return ret;
    }

}
