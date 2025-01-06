package eu.essi_lab.accessor.oaipmh;

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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Roberto
 */
public class DataCiteResourceMapper extends OAIPMHResourceMapper {

    private static final String DATACITE_MAPPER_MAP_ERROR = "DATACITE_MAPPER_MAP_ERROR";

    public DataCiteResourceMapper() {
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.OAI_DATACITE_NS_URI;
    }

    /**
     * @param resource
     * @param originalMD
     * @throws Exception
     */
    protected Optional<GSResource> mapRecord(OriginalMetadata originalMD, RecordType record, GSSource source) throws Exception {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = new MIMetadata();

	DataIdentification identification = new DataIdentification();
	miMetadata.addDataIdentification(identification);

	Distribution distribution = new Distribution();
	miMetadata.setDistribution(distribution);

	ByteArrayInputStream inputStream = new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8));
	XMLDocumentReader reader = new XMLDocumentReader(inputStream);

	List<String> setSpec = record.getHeader().getSetSpec();
	boolean isSeanoe = false;
	boolean isZenodo = false;
	for (String s : setSpec) {
	    if (s.toLowerCase().contains("euvi.seanoe")) {
		isSeanoe = true;
		break;
	    }
	}

	// zenodo use-case
	if (source.getEndpoint().contains("https://s3.amazonaws.com/zenodo-iatlantic")) {
	    isZenodo = true;
	}

	// ---------------------------
	//
	// format (multiple)
	//
	List<String> formats = getValues(reader, "format");
	addValue(formats, new Callback() {
	    @Override
	    public void handleValue(String value) {
		Format format = new Format();
		format.setName(value);
		distribution.addFormat(format);
	    }
	});

	// ---------------------------
	//
	// identifier (single)
	//

	String doiId = reader.evaluateString("//*:identifier[@identifierType]");
	if (doiId != null && !doiId.isEmpty()) {
	    miMetadata.setFileIdentifier(doiId);
	    if (isSeanoe || isZenodo) {
		// SEANOE/ZEONODO use-case
		// ---------------------------
		//
		// link (multiple)
		//
		List<String> links = getValues(reader, "link");
		addValue(links, new Callback() {
		    @Override
		    public void handleValue(String value) {
			List<Online> ret = new ArrayList<>();
			Online online = new Online();
			online.setLinkage(value);
			ret.add(online);
			// List<Online> onLines = createOnlineResource(value,null);
			for (Online o : ret) {
			    distribution.addDistributionOnline(o);
			}
		    }
		});

		Online online = new Online();
		String link = "https://doi.org/";
		link = link + doiId;
		online.setLinkage(link);
		online.setFunctionCode("information");
		distribution.addDistributionOnline(online);
	    } else {
		// PANGAEA use-case
		List<Online> onLines;
		onLines = createOnlineResource(doiId, formats);
		for (Online online : onLines) {
		    distribution.addDistributionOnline(online);
		}
	    }
	} else {
	    List<String> identifiers = getValues(reader, "identifier");
	    setValue(identifiers, new Callback() {
		@Override
		public void handleValue(String value) {
		    miMetadata.setFileIdentifier(value);
		}
	    });
	}

	// -----------------
	// datestamp
	//
	//
	//

	String dateStamp = reader.evaluateString("//*:datestamp");
	miMetadata.setDateStampAsDate(dateStamp);

	// ---------------------------
	//
	// title (single)
	//
	List<String> titles = getValues(reader, "title");
	setValue(titles, new Callback() {
	    @Override
	    public void handleValue(String value) {
		identification.setCitationTitle(value);
	    }
	});

	// ---------------------------
	//
	// description (single)
	//
	List<String> descriptions = getValues(reader, "description");
	// descriptions.addAll(getValues(reader, "description"));

	setValue(descriptions, new Callback() {
	    @Override
	    public void handleValue(String value) {
		identification.setAbstract(value);
	    }
	});

	// ---------------------------
	//
	// date (single)
	//
	List<String> dates = getValues(reader, "date");
	// dates.addAll(getValues(reader, "modified"));
	setValue(dates, new Callback() {
	    @Override
	    public void handleValue(String value) {
		if (value.contains("/")) {
		    String[] splittedTime = value.split("/");
		    String timeStart = splittedTime[0];
		    String timeEnd = splittedTime[1];
		    identification.addTemporalExtent(timeStart, timeEnd);

		} else {
		    TemporalExtent temporalExtent = new TemporalExtent();
		    temporalExtent.setBeginPosition(value);
		    identification.addTemporalExtent(temporalExtent);
		}

	    }
	});

	List<String> publicationYear = getValues(reader, "publicationYear");
	// dates.addAll(getValues(reader, "modified"));
	setValue(publicationYear, new Callback() {
	    @Override
	    public void handleValue(String value) {
		// publication year
		identification.setCitationPublicationDate(value);
	    }
	});

	// ---------------------------
	//
	// type (single)
	//// *:resourceType/@resourceTypeGeneral
	String type = getAttributeValue(reader, "resourceType", "resourceTypeGeneral");
	if (type != null && !type.isEmpty()) {
	    miMetadata.addHierarchyLevelScopeCodeListValue(type);
	}

	// ---------------------------
	//
	// language (single)
	//
	List<String> languages = getValues(reader, "language");
	setValue(languages, new Callback() {
	    @Override
	    public void handleValue(String value) {
		miMetadata.setLanguage(value);
	    }
	});

	// ---------------------------
	//
	// creator (multiple)
	//
	List<ResponsibleParty> creators = getCreatorResponsibleParties(reader, "creator");
	for (ResponsibleParty rp : creators) {
	    identification.addPointOfContact(rp);
	}

	// addValue(creators, new Callback() {
	// @Override
	// public void handleValue(String value) {
	// ResponsibleParty responsibleParty = new ResponsibleParty();
	// responsibleParty.setRoleCode("originator");
	// responsibleParty.setOrganisationName(value);
	// identification.addPointOfContact(responsibleParty);
	// }
	// });

	// ---------------------------
	//
	// publisher (multiple)
	//
	List<String> publishers = getValues(reader, "publisher");
	addValue(publishers, new Callback() {
	    @Override
	    public void handleValue(String value) {
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("publisher");
		responsibleParty.setOrganisationName(value);
		identification.addPointOfContact(responsibleParty);
	    }
	});

	// ---------------------------
	//
	// contributor (multiple)
	//
	List<ResponsibleParty> contributors = getContributorResponsibleParties(reader, "contributor", "contributorType");
	for (ResponsibleParty rp : contributors) {
	    identification.addPointOfContact(rp);
	}

	// ---------------------------
	//
	// subject (multiple)
	//
	List<String> subjects = getValues(reader, "subject");
	addValue(subjects, new Callback() {
	    @Override
	    public void handleValue(String value) {
		identification.addKeyword(value);
		try {
		    MDTopicCategoryCodeType fromValue = MDTopicCategoryCodeType.fromValue(value);
		    identification.addTopicCategory(fromValue);
		} catch (IllegalArgumentException ex) {
		}
	    }
	});

	// ---------------------------
	//
	// link (multiple)
	// MAYBE WE CAN CREATE IT???
	//
	// List<String> links = getValues(reader, "link");
	// addValue(links, new Callback() {
	// @Override
	// public void handleValue(String value) {

	// }
	// });

	// ---------------------------
	//
	// BoundingBox and/or time as coverage (very special case for the GBIF OAI-PMH)
	//

	// ---------------------------
	//
	// BoundingBox (multiple)
	//
	Node[] bboxes = reader.evaluateNodes("//*:geoLocationBox");
	for (int i = 0; i < bboxes.length; i++) {
	    for (Node nodeResult : bboxes) {
		String w = reader.evaluateString(nodeResult, "*:westBoundLongitude");
		String e = reader.evaluateString(nodeResult, "*:eastBoundLongitude");
		String n = reader.evaluateString(nodeResult, "*:northBoundLatitude");
		String s = reader.evaluateString(nodeResult, "*:southBoundLatitude");
		try {
		    Double west = Double.valueOf(w);
		    Double south = Double.valueOf(s);
		    Double east = Double.valueOf(e);
		    Double north = Double.valueOf(n);

		    if (west >= -180 && west <= 180 && east >= -180 && east <= 180 && south >= -90 && north <= 90 && south <= north) {

			identification.addGeographicBoundingBox(north, west, south, east);
		    }
		} catch (Exception ex) {
		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
		}

	    }
	}

	bboxes = reader.evaluateNodes("//*:geoLocationPoint");
	for (int i = 0; i < bboxes.length; i++) {
	    for (Node nodeResult : bboxes) {
		String lat = reader.evaluateString(nodeResult, "*:pointLatitude");
		String lon = reader.evaluateString(nodeResult, "*:pointLongitude");
		try {
		    Double west = Double.valueOf(lon);
		    Double south = Double.valueOf(lat);
		    Double east = Double.valueOf(lon);
		    Double north = Double.valueOf(lat);

		    if (west >= -180 && west <= 180 && east >= -180 && east <= 180 && south >= -90 && north <= 90 && south <= north) {

			identification.addGeographicBoundingBox(north, west, south, east);
		    }
		} catch (Exception ex) {
		    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
		}

	    }
	}

	LegalConstraints lc = getRights(reader, "rights");
	if (lc != null)
	    identification.addLegalConstraints(lc);

	// fundingReferences

	Node[] fundingReferences = reader.evaluateNodes("//*:fundingReference");
	for (Node fundingRef : fundingReferences) {
	    String awardTitle = reader.evaluateString(fundingRef, "*:awardTitle");
	    identification.addKeyword(awardTitle);

	}
	
	// zenodo case
	if(isZenodo) {
	    identification.addKeyword("iAtlantic");
	    identification.addKeyword("818123");
	}

	coreMetadata.setMIMetadata(miMetadata);

	return Optional.of(dataset);
    }

    protected List<Online> createOnlineResource(String identifier, List<String> formats) {
	List<Online> ret = new ArrayList<Online>();
	Online online = new Online();
	String link = "https://doi.pangaea.de/";
	link = link + identifier;
	online.setLinkage(link);
	online.setFunctionCode("information");
	ret.add(online);

	String downloadFormat = "?format=download";
	if (formats != null && !formats.isEmpty()) {
	    String format = formats.get(0);
	    if (format.contains("text")) {
		downloadFormat = "?format=textfile";
	    } else if (format.contains("zip")) {
		downloadFormat = "?format=zip";
	    }
	}
	Online downloadOnline = new Online();
	String downloadLink = link + downloadFormat;
	downloadOnline.setFunctionCode("download");
	downloadOnline.setLinkage(downloadLink);
	ret.add(downloadOnline);
	return ret;
    }

    public interface Callback {

	public void handleValue(String value);
    }

    private void setValue(List<String> list, Callback callback) {

	if (!list.isEmpty()) {
	    String string = list.get(0);
	    if (checkString(string)) {
		callback.handleValue(string);
	    }
	}
    }

    private void addValue(List<String> list, Callback callback) {

	for (String string : list) {
	    if (checkString(string)) {
		callback.handleValue(string);
	    }
	}
    }

    private boolean checkString(String string) {

	return string != null && !string.equals("");
    }

    protected List<ResponsibleParty> getCreatorResponsibleParties(XMLDocumentReader reader, String dataciteElement)
	    throws XPathExpressionException {

	List<ResponsibleParty> responsibleParties = new ArrayList<>();
	Node[] nodes = reader.evaluateNodes("//*:" + dataciteElement);
	ArrayList<String> list = new ArrayList<>();
	for (int i = 0; i < nodes.length; i++) {
	    XMLNodeReader nodeRead = new XMLNodeReader(nodes[i]);
	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    responsibleParty.setRoleCode("originator");

	    String nameType = nodeRead.evaluateString("*:nameType");
	    String familyName = nodeRead.evaluateString("*:familyName");
	    String givenName = nodeRead.evaluateString("*:givenName");
	    String creatorName = nodeRead.evaluateString("*:creatorName");
	    String affiliation = nodeRead.evaluateString("*:affiliation");
	    String nameIdentifier = nodeRead.evaluateString("*:nameIdentifier");

	    boolean personal = true;
	    if (nameType != null && !nameType.isEmpty()) {
		if (nameType.toLowerCase().contains("organization") || nameType.toLowerCase().contains("organisation"))
		    personal = false;
	    }
	    // personal name
	    if (personal) {
		// organization name
		responsibleParty.setIndividualName(creatorName);
		if (affiliation != null && !affiliation.isEmpty()) {
		    responsibleParty.setOrganisationName(affiliation);
		}

	    } else {
		if (affiliation != null && !affiliation.isEmpty()) {
		    responsibleParty.setIndividualName(creatorName);
		    responsibleParty.setOrganisationName(affiliation);
		} else {
		    responsibleParty.setOrganisationName(creatorName);
		}
	    }

	    if (nameIdentifier != null && !nameIdentifier.isEmpty()) {
		boolean isORCID = false;
		String idScheme = nodeRead.evaluateString("*:nameIdentifier/@nameIdentifierScheme");
		if (idScheme != null) {
		    if (idScheme.toLowerCase().contains("orcid")) {
			isORCID = true;
		    }
		}
		if (isORCID) {
		    Contact contactcreatorContactInfo = new Contact();
		    Online onlineOrcid = new Online();
		    onlineOrcid.setFunctionCode("information");
		    onlineOrcid.setLinkage("https://orcid.org/" + nameIdentifier);
		    contactcreatorContactInfo.setOnline(onlineOrcid);
		    responsibleParty.setContactInfo(contactcreatorContactInfo);
		}
	    }

	    responsibleParties.add(responsibleParty);
	}

	return responsibleParties;
    }

    protected List<ResponsibleParty> getContributorResponsibleParties(XMLDocumentReader reader, String dataciteElement, String attribute)
	    throws XPathExpressionException {

	List<ResponsibleParty> responsibleParties = new ArrayList<>();
	Node[] nodes = reader.evaluateNodes("//*:" + dataciteElement);
	List<String> attributes = reader.evaluateTextContent("//*:" + dataciteElement + "/@" + attribute);
	ArrayList<String> list = new ArrayList<>();
	for (int i = 0; i < nodes.length; i++) {
	    XMLNodeReader nodeRead = new XMLNodeReader(nodes[i]);
	    String role = attributes.get(i);
	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    responsibleParty.setRoleCode(role);

	    String nameType = nodeRead.evaluateString("*:nameType");
	    String familyName = nodeRead.evaluateString("*:familyName");
	    String givenName = nodeRead.evaluateString("*:givenName");
	    String contributorName = nodeRead.evaluateString("*:contributorName");
	    String affiliation = nodeRead.evaluateString("*:affiliation");

	    boolean personal = true;
	    if (nameType != null && !nameType.isEmpty()) {
		if (nameType.toLowerCase().contains("organization") || nameType.toLowerCase().contains("organisation"))
		    personal = false;
	    }
	    // personal name
	    if (personal) {
		// organization name
		responsibleParty.setIndividualName(contributorName);
		if (affiliation != null && !affiliation.isEmpty()) {
		    responsibleParty.setOrganisationName(affiliation);
		}

	    } else {
		if (affiliation != null && !affiliation.isEmpty()) {
		    responsibleParty.setIndividualName(contributorName);
		    responsibleParty.setOrganisationName(affiliation);
		} else {
		    responsibleParty.setOrganisationName(contributorName);
		}
	    }
	    responsibleParties.add(responsibleParty);
	}

	return responsibleParties;
    }

    protected List<String> getValues(XMLDocumentReader reader, String dataciteElement) throws XPathExpressionException {

	Node[] nodes = reader.evaluateNodes("//*:" + dataciteElement);
	ArrayList<String> list = new ArrayList<>();
	for (int i = 0; i < nodes.length; i++) {
	    list.add(nodes[i].getTextContent());
	}

	return list;
    }

    protected String getAttributeValue(XMLDocumentReader reader, String dataciteElement, String attribute) throws XPathExpressionException {

	String attributeValue = reader.evaluateString("//*:" + dataciteElement + "/@" + attribute);
	return attributeValue;
    }

    protected LegalConstraints getRights(XMLDocumentReader reader, String dataciteElement) throws XPathExpressionException {

	// rights
	LegalConstraints lc = null;
	String rights = reader.evaluateString("//*:" + dataciteElement);
	if (rights != null && !rights.isEmpty()) {
	    lc = new LegalConstraints();

	    String rightsId = reader.evaluateString("//*:" + dataciteElement + "/@rightsIdentifier");
	    String rightsURI = reader.evaluateString("//*:" + dataciteElement + "/@rightsURI");
	    String rightsScheme = reader.evaluateString("//*:" + dataciteElement + "/@schemeURI");
	    String rightsIdScheme = reader.evaluateString("//*:" + dataciteElement + "/@rightsIdentifierScheme");

	    if (rightsId != null && !rightsId.isEmpty()) {
		lc.addOtherConstraints(rightsId + ": " + rights);
	    }
	    if (rightsIdScheme != null && !rightsIdScheme.isEmpty()) {
		lc.addUseConstraintsCode(rightsIdScheme);
		// lc.addOtherConstraints(rightsId + ": " + rights);
	    }
	    if (rightsScheme != null && !rightsScheme.isEmpty()) {
		// TODO: check
	    }
	    if (rightsURI != null && !rightsURI.isEmpty()) {
		// TODO: check
	    }
	}

	return lc;
    }

    private RecordType getRecordType(String metadata) throws GSException {

	try {
	    return CommonContext.unmarshal(metadata, RecordType.class);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    DATACITE_MAPPER_MAP_ERROR, //
		    e);
	}
    }

    @Override
    public Boolean supportsOriginalMetadata(OriginalMetadata originalMD) {
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(originalMD.getMetadata());

	    String localName = reader.evaluateString("local-name(/*[1])").toLowerCase();

	    switch (localName) {
	    case "record":
		return true;

	    default:
		break;
	    }

	} catch (Exception e) {

	}
	return false;
    }
}
