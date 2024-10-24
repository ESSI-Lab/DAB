package eu.essi_lab.accessor.ckan;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import eu.essi_lab.accessor.ckan.datamodel.CKANDataset;
import eu.essi_lab.accessor.ckan.datamodel.CKANPoint;
import eu.essi_lab.accessor.ckan.datamodel.CKANRelationship;
import eu.essi_lab.accessor.ckan.datamodel.CKANResource;
import eu.essi_lab.accessor.ckan.datamodel.CKANTag;
import eu.essi_lab.accessor.ckan.md.CKANConstants;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * Mapper from CKAN data model
 *
 * @author boldrini
 */
public class CKANMapper extends OriginalIdentifierMapper {

    
    private transient CKANParser parser = new CKANParser();

    public CKANMapper() {
	// empty constructor for service loader
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String originalMetadata = resource.getOriginalMetadata().getMetadata();
	CKANDataset dataset = parser.parseDataset(originalMetadata);

	return dataset.getId();
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset ds = new Dataset();

	ds.setSource(source);

	String originalMetadata = originalMD.getMetadata();
	CKANDataset dataset = parser.parseDataset(originalMetadata);

	getMappedMetadata(ds, dataset);

	return ds;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CKANConstants.CKAN;
    }

    protected HashSet<String> addCKANResource(CKANResource resource, MIMetadata metadata) {

	String urlString = resource.getUrl();

	String resFormat = resource.getFormat();

	String resName = resource.getName();

	String function = "download";

	/**
	 * Often CKAN systems use format to encode the protocol, setting it to e.g. "OGC WMS"
	 */
	String protocol = resFormat;

	String type = resource.getType();

	if (type != null) {
	    switch (type) {
	    case "file":
	    case "file.upload":
		function = "download";
		break;
	    case "api":
	    case "code":
		function = "order";
		break;
	    case "visualization":
	    case "documentation":
		function = "information";
		break;
	    default:
		break;
	    }
	}

	HashSet<String> formats = new HashSet<>();

	if (resFormat != null) {
	    formats.add(resFormat);
	}

	Online online = new Online();
	online.setProtocol(protocol);
	online.setLinkage(urlString);
	online.setName(resName);
	online.setFunctionCode(function);
	online.setDescription(resource.getDescription());
	Double size = null;
	if (resource.getSize() != null && !"".equals(resource.getSize())) {
	    try {
		size = Double.parseDouble(resource.getSize());
	    } catch (NumberFormatException e) {

	    }
	}
	if (size == null) {
	    metadata.getDistribution().addDistributionOnline(online);
	} else {
	    metadata.getDistribution().addDistributionOnline(online, size);
	}

	return formats;
    }

    protected boolean isGDCCompatible(String license) {

	return false;
    }

    private void addLicense(String license, MIMetadata md) {

	LegalConstraints lc = new LegalConstraints();
	lc.addUseLimitation(license);
	md.getDataIdentification().addLegalConstraints(lc);

    }

    private boolean include(CKANDataset pack) {
	return true;
    }

    private void getMappedMetadata(Dataset ds, CKANDataset pack) {

	if (!include(pack)) {
	    return;
	}

	// START MAPPING
	MIMetadata metadata = ds.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
 
	// relations: commented for the moment

	List<CKANRelationship> relations = pack.getRelations();
	for (CKANRelationship relation : relations) {

	    // TODO: add relations between resources
	    // Relation newRelation = new Relation();

	    // newRelation.setDescription(relation.getComment());

	    String objectId = relation.getObjectId();
	    String subjectId = relation.getSubjectId();

	    if (subjectId.equals(pack.getId())) {

		// SUBJECT
		// newRelation.setRelatedResourceIdentifier(objectId);
		// newRelation.setType(relation.getType());

	    } else {

		// OBJECT
		// newRelation.setRelatedResourceIdentifier(subjectId);
		String relationType = relation.getType();
		String newRelationType;

		switch (relationType) {
		case "depends_on":
		    newRelationType = "dependency_of";
		    break;

		case "dependency_of":
		    newRelationType = "dependency_on";
		    break;

		case "derives_from":
		    newRelationType = "has_derivation";
		    break;

		case "has_derivation":
		    newRelationType = "derives_from";
		    break;

		case "links_to":
		    newRelationType = "linked_from";
		    break;

		case "linked_from":
		    newRelationType = "links_to";
		    break;

		case "child_of":
		    newRelationType = "parent_of";
		    break;

		case "parent_of":
		    newRelationType = "child_of";
		    break;
		default:
		    newRelationType = relationType;
		    break;
		}
		// newRelation.setType(newRelationType);
	    }
	    // MS: I don't understand this check
	    // if (identifiers.contains(newRelation.getRelatedResourceIdentifier())) {
	    // dataset.getRelations().add(newRelation);
	    // }
	}

	DataIdentification identification = metadata.getDataIdentification();

	String date = null;

	for (int i = 0; i < pack.getResources().size(); i++) {
	    CKANResource resource = pack.getResources().get(i);
	    if (resource.getLastModified() != null) {
		date = resource.getLastModified();
	    }
	}

	identification.setCitationTitle(pack.getTitle());
	identification.setCitationAlternateTitle(pack.getName());
	identification.setCitationRevisionDate(date);

	if (pack.getUrl() != null) {

	    Online onLine = new Online();
	    onLine.setProtocol("HTTP");
	    onLine.setLinkage(pack.getUrl());
	    onLine.setName("Dataset homepage");
	    onLine.setDescription("Dataset homepage");
	    onLine.setFunctionCode("information");
	    metadata.getDistribution().addDistributionOnline(onLine);

	}

	// NO CORE element
	String organization = pack.getOrganization();

	if (pack.getAuthor() != null) {
	    ResponsibleParty creatorContact = new ResponsibleParty();
	    creatorContact.setIndividualName(pack.getAuthor());
	    creatorContact.setOrganisationName(organization);
	    creatorContact.setRoleCode("author");
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress(pack.getAuthorEmail());
	    contactInfo.setAddress(address);
	    creatorContact.setContactInfo(contactInfo);
	    identification.addPointOfContact(creatorContact);
	}

	if (pack.getMaintainer() != null) {
	    ResponsibleParty maintainerContact = new ResponsibleParty();
	    maintainerContact.setIndividualName(pack.getMaintainer());
	    maintainerContact.setOrganisationName(organization);
	    maintainerContact.setRoleCode("custodian");
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress(pack.getMaintainerEmail());
	    contactInfo.setAddress(address);
	    maintainerContact.setContactInfo(contactInfo);
	    identification.addPointOfContact(maintainerContact);
	}

	if (pack.getLicense() != null) {

	    addLicense(pack.getLicense(), metadata);

	} else if (pack.getLicenseTitle() != null) {

	    addLicense(pack.getLicenseTitle(), metadata);

	} else if (pack.getLicenseId() != null) {
	    LegalConstraints lc = new LegalConstraints();
	    lc.addUseLimitation(pack.getLicenseId());
	    identification.addLegalConstraints(lc);
	}

	// private String version;
	identification.setAbstract(pack.getNotes());

	for (int i = 0; i < pack.getTags().size(); i++) {
	    CKANTag tag = pack.getTags().get(i);
	    String name = tag.getDisplayName();
	    identification.addKeyword(name);
	}
	// private String state;

	HashSet<String> formats = new HashSet<String>();

	for (int i = 0; i < pack.getResources().size(); i++) {
	    HashSet<String> fs = addCKANResource(pack.getResources().get(i), metadata);
	    formats.addAll(fs);
	}

	for (String format : formats) {
	    Format mdFormat = new Format();
	    mdFormat.setName(format);
	    metadata.getDistribution().addFormat(mdFormat);
	}

	// private List<CKANGroup> groups = new ArrayList<CKANGroup>();
	// PRIVATE List<KeyValuePair> extras = new ArrayList<KeyValuePair>();

	// ADDITIONAL MAPPING - NO MANDATORY elements

	if (pack.getMetadataDate() != null) {
	    metadata.setDateStampAsDate(pack.getMetadataDate());
	} else if (pack.getMetadataCreated() != null) {
	    metadata.setDateStampAsDate(pack.getMetadataCreated());
	} else if (pack.getMetadataModified() != null) {
	    metadata.setDateStampAsDate(pack.getMetadataModified());
	}

	String startTime = null;
	String endTime = null;

	if (pack.getDatasetStartDate() != null) {
	    startTime = pack.getDatasetStartDate();
	}

	if (pack.getDatasetEndDate() != null) {
	    endTime = pack.getDatasetEndDate();
	}

	if (startTime != null || endTime != null) {
	    identification.addTemporalExtent(startTime, endTime);
	}

	if (pack.getAccessConstraints() != null) {
	    LegalConstraints lc = new LegalConstraints();
	    lc.addUseLimitation(pack.getAccessConstraints());
	    identification.addLegalConstraints(lc);
	}

	if (pack.getBboxEastLongitude() != null) {
	    try {
		double north = Double.parseDouble(pack.getBboxNorthLatitude());
		double west = Double.parseDouble(pack.getBboxWestLongitude());
		double south = Double.parseDouble(pack.getBboxSouthLatitude());
		double east = Double.parseDouble(pack.getBboxEastLongitude());
		identification.addGeographicBoundingBox(north, west, south, east);
	    } catch (Exception ex) {
		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		pack.setBboxEastLongitude(null);
		pack.setBboxWestLongitude(null);
		pack.setBboxNorthLatitude(null);
		pack.setBboxSouthLatitude(null);
	    }
	}

	if (pack.getMetadataLanguage() != null) {
	    metadata.setLanguage(pack.getMetadataLanguage());
	} else {
	    metadata.setLanguage("Eng");
	}

	if (pack.getContactEmail() != null) {
	    ResponsibleParty contact = new ResponsibleParty();
	    contact.setOrganisationName(pack.getResponsibleParty());

	    contact.setRoleCode("pointOfContact");
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    address.addElectronicMailAddress(pack.getContactEmail());
	    contactInfo.setAddress(address);
	    contact.setContactInfo(contactInfo);
	    metadata.addContact(contact);
	}

	identification.setCitationEditionDate(pack.getDatasetPublicationDate());

	ReferenceSystem ref = new ReferenceSystem();
	ref.setCode(pack.getSpatialReferenceSystem());
	metadata.addReferenceSystemInfo(ref);

	List<CKANPoint> polygon = pack.getPolygon();
	if (polygon != null && !polygon.isEmpty()) {
	    BoundingPolygon myPolygon = new BoundingPolygon();
	    List<Double> coordinates = new ArrayList<>();
	    for (CKANPoint ckanPoint : polygon) {
		coordinates.add(ckanPoint.getLat());
		coordinates.add(ckanPoint.getLon());
	    }
	    myPolygon.setCoordinates(coordinates);
	    identification.addBoundingPolygon(myPolygon);
	}
    }
}
