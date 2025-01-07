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

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Roberto
 */
public class DIFMapper extends OAIPMHResourceMapper {

    private static final String DIF_MAPPER_MAP_ERROR = "DIF_MAPPER_MAP_ERROR";

    public DIFMapper() {
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.DIF_URI;
    }

    /**
     * @param resource
     * @param originalMD
     * @throws Exception
     */
    protected Optional<GSResource> mapRecord(OriginalMetadata originalMD, RecordType record, GSSource source) throws Exception {

	DatasetCollection dataset = new DatasetCollection();
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

	// ---------------------------
	//
	// distribution (multiple)
	//
	Node[] distributionNodes = reader.evaluateNodes("//*:Distribution");
	for (Node n : distributionNodes) {
	    String media = reader.evaluateString(n, "*:Distribution_Media");
	    String size = reader.evaluateString(n, "*:Distribution_Size");
	    String form = reader.evaluateString(n, "*:Distribution_Format");

	    if (form != null && !form.isEmpty()) {
		Format format = new Format();
		format.setName(form);
		distribution.addFormat(format);
	    }
	    // identification.addTemporalExtent(startDate, endDate);

	}

	// ---------------------------
	//
	// identifier (single)
	//

	String id = reader.evaluateString("//*:Entry_ID");
	if (id != null && !id.isEmpty()) {
	    miMetadata.setFileIdentifier(id);
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

	String dateStamp = reader.evaluateString("//*:Last_DIF_Revision_Date");
	miMetadata.setDateStampAsDate(dateStamp);

	String creationDate = reader.evaluateString("//*:DIF_Creation_Date");
	identification.setCitationCreationDate(creationDate);

	// ---------------------------
	//
	// title (single)
	//
	List<String> titles = getValues(reader, "Entry_Title");
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
	Node descriptions = reader.evaluateNode("//*:Summary");
	if (descriptions != null) {
	    String descp = reader.evaluateString(descriptions, "*:Abstract");
	    identification.setAbstract(descp);
	}

	// ---------------------------
	//
	// dates
	//
	Node[] temporalNode = reader.evaluateNodes("//*:Temporal_Coverage");
	for (Node timeNode : temporalNode) {
	    String startDate = reader.evaluateString(timeNode, "*:Start_Date");
	    String endDate = reader.evaluateString(timeNode, "*:Stop_Date");
	    identification.addTemporalExtent(startDate, endDate);

	}

	// ---------------------------
	//
	// language (single)
	//
	List<String> languages = getValues(reader, "Data_Set_Language");
	setValue(languages, new Callback() {
	    @Override
	    public void handleValue(String value) {
		miMetadata.setLanguage(value);
	    }
	});

	// ---------------------------
	//
	// Personnel
	//
	Node[] people = reader.evaluateNodes("//*:Personnel");
	List<ResponsibleParty> responsibleParties = new ArrayList<>();
	for (Node p : people) {
	    String role = reader.evaluateString(p, "*:Role");
	    String mail = reader.evaluateString(p, "*:Email");
	    String firstName = reader.evaluateString(p, "*:First_Name");
	    String lastName = reader.evaluateString(p, "*:Last_Name");
	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    if (role != null && !role.isEmpty())
		responsibleParty.setRoleCode(role.toLowerCase());
	    if (mail != null && !mail.isEmpty()) {
		Contact contact = new Contact();
		Address addr = new Address();
		addr.addElectronicMailAddress(mail);
		contact.setAddress(addr);
		responsibleParty.setContactInfo(contact);
	    }
	    String name = "";
	    if (firstName != null && !firstName.isEmpty())
		name += firstName + " ";
	    if (lastName != null && !lastName.isEmpty())
		name += lastName;

	    responsibleParty.setIndividualName(name);

	    responsibleParties.add(responsibleParty);

	}

	// ---------------------------
	//
	// Originating_Center
	//

	List<String> originatingCenterList = getValues(reader, "Originating_Center");
	for (String s : originatingCenterList) {
	    for (ResponsibleParty r : responsibleParties) {
		r.setOrganisationName(s);
		identification.addPointOfContact(r);
	    }
	}
	// ---------------------------
	//
	// Data_Center
	//

	Node[] dataCenterNodes = reader.evaluateNodes("//*:Data_Center");
	for (Node p : dataCenterNodes) {
	    Node dataCenterNameNode = reader.evaluateNode(p, "*:Data_Center_Name");
	    Node dataCenterPersonnelNode = reader.evaluateNode(p, "*:Personnel");
	    String dataCenterUrl = reader.evaluateString(p, "*:Data_Center_URL");
	    String dcShortName = reader.evaluateString(dataCenterNameNode, "*:Short_Name");
	    String dcLongName = reader.evaluateString(dataCenterNameNode, "*:Long_Name");
	    String dcRole = reader.evaluateString(dataCenterPersonnelNode, "*:Role");
	    String dcLastName = reader.evaluateString(dataCenterPersonnelNode, "*:Last_Name");
	    String dcPhone = reader.evaluateString(dataCenterPersonnelNode, "*:Last_Name");
	    String dcFax = reader.evaluateString(dataCenterPersonnelNode, "*:Last_Name");
	    Node contactAddressNode = reader.evaluateNode(dataCenterPersonnelNode, "*:Contact_Address");
	    String address = reader.evaluateString(contactAddressNode, "*:Address");
	    String city = reader.evaluateString(contactAddressNode, "*:City");
	    String province = reader.evaluateString(contactAddressNode, "*:Province_or_State");
	    String postalCode = reader.evaluateString(contactAddressNode, "*:Postal_Code");
	    String country = reader.evaluateString(contactAddressNode, "*:Country");
	}

	// ---------------------------
	//
	// parameter (multiple)
	//
	List<String> parameters = new ArrayList<>();
	Keywords gcmdKeys = new Keywords();
	gcmdKeys.setThesaurusNameCitationTitle("Global Change Master Directory (GCMD)");
	Node[] parametersNode = reader.evaluateNodes("//*:Parameters");
	for (Node p : parametersNode) {
	    String category = reader.evaluateString(p, "*:Category");
	    String topic = reader.evaluateString(p, "*:Topic");
	    String term = reader.evaluateString(p, "*:Term");
	    String var1 = reader.evaluateString(p, "*:Variable_Level_1");
	    String var2 = reader.evaluateString(p, "*:Variable_Level_2");
	    String var3 = reader.evaluateString(p, "*:Variable_Level_3");
	    StringBuilder sb = new StringBuilder();
	    if (category != null && !category.isEmpty()) {
		sb.append(category);
		sb.append(">");
	    }
	    if (topic != null && !topic.isEmpty()) {
		sb.append(topic);
		sb.append(">");
	    }
	    if (term != null && !term.isEmpty()) {
		sb.append(term);
		sb.append(">");
	    }
	    if (var1 != null && !var1.isEmpty()) {
		sb.append(var1);
		sb.append(">");
		// parameters.add(var1);
	    }
	    if (var2 != null && !var2.isEmpty()) {
		sb.append(var2);
		sb.append(">");
		// parameters.add(var2);
	    }
	    if (var3 != null && !var3.isEmpty()) {
		sb.append(var3);
		// parameters.add(var3);
		// sb.append(">");
	    }
	    String keyword = sb.toString();
	    while (keyword.endsWith(">")) {
		keyword = keyword.substring(0, keyword.length() - 1);
	    }
	    // gcmdKeys.addKeyword(keyword);
	    parameters.add(keyword);

	}
	// identification.addKeywords(gcmdKeys);

	for (String s : parameters) {
	    // s = s.trim();
	    CoverageDescription description = new CoverageDescription();
	    // description.setAttributeIdentifier(s);
	    description.setAttributeDescription(s);
	    description.setAttributeTitle(s);
	    miMetadata.addCoverageDescription(description);
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	}

	// ---------------------------
	//
	// keyword (multiple)
	//
	List<String> keywords = getValues(reader, "Keyword");
	Keywords k = new Keywords();
	// k.setThesaurusNameCitationTitle("Climate and Forecast (CF) Standard Name Table");
	addValue(keywords, new Callback() {
	    @Override
	    public void handleValue(String value) {
		k.addKeyword(value);
		// identification.addKeyword(value);
		try {
		    MDTopicCategoryCodeType fromValue = MDTopicCategoryCodeType.fromValue(value);
		    identification.addTopicCategory(fromValue);
		} catch (IllegalArgumentException ex) {
		}
	    }
	});
	identification.addKeywords(k);
	// ---------------------------
	//
	// topic category (multiple)
	// ISO_Topic_Category
	//
	List<String> topic_category = getValues(reader, "ISO_Topic_Category");
	addValue(topic_category, new Callback() {
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
	// sensor/instrument (multiple)
	//

	Node[] sensorNodes = reader.evaluateNodes("//*:Sensor_Name");
	for (Node p : sensorNodes) {
	    String shortSensorName = reader.evaluateString(p, "*:Short_Name");
	    String longSensorName = reader.evaluateString(p, "*:Long_Name");
	    if (longSensorName != null && !longSensorName.isEmpty()) {

		MIInstrument myInstrument = new MIInstrument();
		myInstrument.setMDIdentifierTypeIdentifier(shortSensorName);
		myInstrument.setMDIdentifierTypeCode(shortSensorName);
		myInstrument.setDescription(longSensorName);
		myInstrument.setTitle(longSensorName);
		// myInstrument.getElementType().getCitation().add(e)
		coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		Keywords keyword = new Keywords();
		keyword.setTypeCode("instrument");
		keyword.addKeyword(longSensorName);// or sensorModel
		identification.addKeywords(keyword);
	    }

	}

	// ---------------------------
	//
	// project (multiple)
	//
	Node[] projectNodes = reader.evaluateNodes("//*:Project");
	for (Node p : projectNodes) {
	    String shortProjectName = reader.evaluateString(p, "*:Short_Name");
	    String longProjectName = reader.evaluateString(p, "*:Long_Name");
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("project");
	    keyword.addKeyword(longProjectName + "(" + shortProjectName + ")");// or sensorModel
	    identification.addKeywords(keyword);
	}

	// ---------------------------
	//
	// link (related_url) (multiple)
	//
	Node[] linkNodes = reader.evaluateNodes("//*:Related_URL");
	for (Node p : linkNodes) {
	    String url = reader.evaluateString(p, "*:URL");
	    String descriptionUrl = reader.evaluateString(p, "*:Description");
	    Node contentTypeNode = reader.evaluateNode(p, "*:URL_Content_Type");
	    String url_type = reader.evaluateString(contentTypeNode, "*:Type");
	    String url_subtype = reader.evaluateString(contentTypeNode, "*:Subtype");
	    Online downloadOnline = new Online();
	    downloadOnline.setDescription(descriptionUrl);
	    if (url_subtype!=null && !url_subtype.isEmpty()) {
		downloadOnline.setProtocol(url_type + " - " + url_subtype);
	    }else {
		downloadOnline.setProtocol(url_type);
	    }
	    
	    downloadOnline.setFunctionCode("download");
	    downloadOnline.setLinkage(url);
	    distribution.addDistributionOnline(downloadOnline);

	}

	// // ---------------------------
	// //
	// // creator (multiple)
	// //
	// List<ResponsibleParty> creators = getCreatorResponsibleParties(reader, "creator");
	// for (ResponsibleParty rp : creators) {
	// identification.addPointOfContact(rp);
	// }

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
	// List<String> publishers = getValues(reader, "publisher");
	// addValue(publishers, new Callback() {
	// @Override
	// public void handleValue(String value) {
	// ResponsibleParty responsibleParty = new ResponsibleParty();
	// responsibleParty.setRoleCode("publisher");
	// responsibleParty.setOrganisationName(value);
	// identification.addPointOfContact(responsibleParty);
	// }
	// });

	// ---------------------------
	//
	// contributor (multiple)
	//
	// List<ResponsibleParty> contributors = getContributorResponsibleParties(reader, "contributor",
	// "contributorType");
	// for (ResponsibleParty rp : contributors) {
	// identification.addPointOfContact(rp);
	// }

	// ---------------------------
	//
	// subject (multiple)
	//
	// List<String> subjects = getValues(reader, "subject");
	// addValue(subjects, new Callback() {
	// @Override
	// public void handleValue(String value) {
	// identification.addKeyword(value);
	// try {
	// MDTopicCategoryCodeType fromValue = MDTopicCategoryCodeType.fromValue(value);
	// identification.addTopicCategory(fromValue);
	// } catch (IllegalArgumentException ex) {
	// }
	// }
	// });

	// ---------------------------
	//
	// BoundingBox (multiple)
	//
	Node[] bboxes = reader.evaluateNodes("//*:Spatial_Coverage");
	for (int i = 0; i < bboxes.length; i++) {
	    for (Node nodeResult : bboxes) {
		String w = reader.evaluateString(nodeResult, "*:Westernmost_Longitude");
		String e = reader.evaluateString(nodeResult, "*:Easternmost_Longitude");
		String n = reader.evaluateString(nodeResult, "*:Northernmost_Latitude");
		String s = reader.evaluateString(nodeResult, "*:Southernmost_Latitude");
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

	LegalConstraints lc = null;
	String useConstraints = reader.evaluateString("//*:Use_Constraints");
	String accessConstraints = reader.evaluateString("//*:Access_Constraints");

	if (useConstraints != null && !useConstraints.isEmpty()) {
	    lc = new LegalConstraints();
	    lc.addUseConstraintsCode(useConstraints);
	}
	if (accessConstraints != null && !accessConstraints.isEmpty()) {
	    if (lc == null)
		lc = new LegalConstraints();
	    lc.addAccessConstraintsCode(accessConstraints);
	}

	if (lc != null)
	    identification.addLegalConstraints(lc);

	Node[] datasetCitaion = reader.evaluateNodes("//*:Data_Set_Citation");
	// <dif:Dataset_Creator>Simon Filhol, Pierre Marie Lefeuvre, Jean-Charles Gallet</dif:Dataset_Creator>
	// <dif:Dataset_Title>Wireless Sensor Network for meteorological observations on Kongsvegen
	// glacier</dif:Dataset_Title>
	// <dif:Dataset_Series_Name/>
	// <dif:Dataset_Release_Date>2022-09-30</dif:Dataset_Release_Date>
	// <dif:Dataset_Release_Place/>
	// <dif:Dataset_Publisher>Norwegian Meteorological Institute</dif:Dataset_Publisher>
	// <dif:Version/>
	// <dif:Other_Citation_Details/>
	// <dif:Dataset_DOI>https://doi.org/10.21343/1r1z-ae07</dif:Dataset_DOI>
	// <dif:Online_Resource/>
	coreMetadata.setMIMetadata(miMetadata);

	return Optional.of(dataset);
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

    // protected List<ResponsibleParty> getCreatorResponsibleParties(XMLDocumentReader reader, String dataciteElement)
    // throws XPathExpressionException {
    //
    // List<ResponsibleParty> responsibleParties = new ArrayList<>();
    // Node[] nodes = reader.evaluateNodes("//*:" + dataciteElement);
    // ArrayList<String> list = new ArrayList<>();
    // for (int i = 0; i < nodes.length; i++) {
    // XMLNodeReader nodeRead = new XMLNodeReader(nodes[i]);
    // ResponsibleParty responsibleParty = new ResponsibleParty();
    // responsibleParty.setRoleCode("originator");
    //
    // String nameType = nodeRead.evaluateString("*:nameType");
    // String familyName = nodeRead.evaluateString("*:familyName");
    // String givenName = nodeRead.evaluateString("*:givenName");
    // String creatorName = nodeRead.evaluateString("*:creatorName");
    // String affiliation = nodeRead.evaluateString("*:affiliation");
    // String nameIdentifier = nodeRead.evaluateString("*:nameIdentifier");
    //
    // boolean personal = true;
    // if (nameType != null && !nameType.isEmpty()) {
    // if (nameType.toLowerCase().contains("organization") || nameType.toLowerCase().contains("organisation"))
    // personal = false;
    // }
    // // personal name
    // if (personal) {
    // // organization name
    // responsibleParty.setIndividualName(creatorName);
    // if (affiliation != null && !affiliation.isEmpty()) {
    // responsibleParty.setOrganisationName(affiliation);
    // }
    //
    // } else {
    // if (affiliation != null && !affiliation.isEmpty()) {
    // responsibleParty.setIndividualName(creatorName);
    // responsibleParty.setOrganisationName(affiliation);
    // } else {
    // responsibleParty.setOrganisationName(creatorName);
    // }
    // }
    //
    // if (nameIdentifier != null && !nameIdentifier.isEmpty()) {
    // boolean isORCID = false;
    // String idScheme = nodeRead.evaluateString("*:nameIdentifier/@nameIdentifierScheme");
    // if (idScheme != null) {
    // if (idScheme.toLowerCase().contains("orcid")) {
    // isORCID = true;
    // }
    // }
    // if (isORCID) {
    // Contact contactcreatorContactInfo = new Contact();
    // Online onlineOrcid = new Online();
    // onlineOrcid.setFunctionCode("information");
    // onlineOrcid.setLinkage("https://orcid.org/" + nameIdentifier);
    // contactcreatorContactInfo.setOnline(onlineOrcid);
    // responsibleParty.setContactInfo(contactcreatorContactInfo);
    // }
    // }
    //
    // responsibleParties.add(responsibleParty);
    // }
    //
    // return responsibleParties;
    // }

    // protected List<ResponsibleParty> getContributorResponsibleParties(XMLDocumentReader reader, String
    // dataciteElement, String attribute)
    // throws XPathExpressionException {
    //
    // List<ResponsibleParty> responsibleParties = new ArrayList<>();
    // Node[] nodes = reader.evaluateNodes("//*:" + dataciteElement);
    // List<String> attributes = reader.evaluateTextContent("//*:" + dataciteElement + "/@" + attribute);
    // ArrayList<String> list = new ArrayList<>();
    // for (int i = 0; i < nodes.length; i++) {
    // XMLNodeReader nodeRead = new XMLNodeReader(nodes[i]);
    // String role = attributes.get(i);
    // ResponsibleParty responsibleParty = new ResponsibleParty();
    // responsibleParty.setRoleCode(role);
    //
    // String nameType = nodeRead.evaluateString("*:nameType");
    // String familyName = nodeRead.evaluateString("*:familyName");
    // String givenName = nodeRead.evaluateString("*:givenName");
    // String contributorName = nodeRead.evaluateString("*:contributorName");
    // String affiliation = nodeRead.evaluateString("*:affiliation");
    //
    // boolean personal = true;
    // if (nameType != null && !nameType.isEmpty()) {
    // if (nameType.toLowerCase().contains("organization") || nameType.toLowerCase().contains("organisation"))
    // personal = false;
    // }
    // // personal name
    // if (personal) {
    // // organization name
    // responsibleParty.setIndividualName(contributorName);
    // if (affiliation != null && !affiliation.isEmpty()) {
    // responsibleParty.setOrganisationName(affiliation);
    // }
    //
    // } else {
    // if (affiliation != null && !affiliation.isEmpty()) {
    // responsibleParty.setIndividualName(contributorName);
    // responsibleParty.setOrganisationName(affiliation);
    // } else {
    // responsibleParty.setOrganisationName(contributorName);
    // }
    // }
    // responsibleParties.add(responsibleParty);
    // }
    //
    // return responsibleParties;
    // }

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
		    DIF_MAPPER_MAP_ERROR, //
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
