package eu.essi_lab.accessor.wekeo;

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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class WEKEOMapper extends FileIdentifierMapper {

    /**
     * FIELDS USED FOR MAPPING
     * abstract
     * contact >
     * created
     * datasetID
     * details >
     * extent >
     * parameters
     * previewImage
     * title
     */

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.WEKEO_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;

	// Dataset dataset = new Dataset();
	// dataset.setSource(source);
	//
	// String md = originalMD.getMetadata();
	// String[] split = md.split(BNHSPropertyReader.SEPARATOR);
	// Map<BNHSProperty, String> table = new HashMap<>();
	// int max = split.length - 1;
	// for (int i = 0; i < max; i += 2) {
	// String key = split[i];
	// String value = split[i + 1];
	// BNHSProperty column = BNHSProperty.decode(key);
	// if (key != null) {
	// table.put(column, value);
	// }
	// }
	//
	// CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	//
	// MIPlatform platform = new MIPlatform();
	//
	// coreMetadata.getMIMetadata().addMIPlatform(platform);
	//
	// String id = table.get(BNHSProperty.HYCOSID);
	// if (id != null) {
	// id = "wmo:hycos:id:" + id;
	// platform.setMDIdentifierCode(id);
	// }
	//
	// String stationName = table.get(BNHSProperty.STATION_NAME);
	// if (stationName != null) {
	// Citation platformCitation = new Citation();
	// platformCitation.setTitle(stationName);
	// platform.setCitation(platformCitation);
	//
	// coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " station");
	// }
	//
	// String latitude = table.get(BNHSProperty.LATITUDE);
	// String longitude = table.get(BNHSProperty.LONGITUDE);
	// if (latitude != null && longitude != null) {
	// double lat = Double.parseDouble(latitude);
	// double lon = Double.parseDouble(longitude);
	// coreMetadata.addBoundingBox(lat, lon, lat, lon);
	// }
	//
	// dataset.getPropertyHandler().setIsTimeseries(true);
	//
	// return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	logger.info("WEKEO Mappper STARTED");

	String originalMetadata = originalMD.getMetadata();

	JSONObject object = new JSONObject(originalMetadata);

	String title = WEKEOClient.getString(object, "title");
	String abstrakt = WEKEOClient.getString(object, "abstract");
	String parameters = WEKEOClient.getString(object, "parameters");
	String previewImage = WEKEOClient.getString(object, "previewImage");
	String created = WEKEOClient.getString(object, "created");
	String datasetID = WEKEOClient.getString(object, "datasetId");

	JSONObject details = object.optJSONObject("details");
	JSONObject contact = object.optJSONObject("contact");

	JSONObject extent = object.optJSONObject("extent");
	String bbox = extent.optString("bbox");
	String startDate = extent.optString("startDate");
	String endDate = extent.optString("endDate");

	String contactMail = contact.optString("contactEmail");
	String contactIndividualName = contact.optString("contactIndividualName");
	String contactOrganizationName = contact.optString("contactOrganizationName");
	String contactPhone = contact.optString("contactPhone");
	String contactUrl = contact.optString("contactUrl");

	String param = details.optString("instrumentType");
	String instrumentId = details.optString("instrumentId");
	String instrumentName = details.optString("instrumentName");
	String orbitType = details.optString("orbitType");
	String platformDescription = details.optString("platformDescription");
	String platformId = details.optString("platformId");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	// identifier
	if (datasetID != null && !datasetID.isEmpty()) {

	    try {
		String identifier = StringUtils.hashSHA1messageDigest(datasetID);
		coreMetadata.setIdentifier(identifier);
		coreMetadata.getMIMetadata().setFileIdentifier(identifier);
	    } catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	// title
	if (title != null && !title.isEmpty()) {
	    coreMetadata.setTitle(title);

	}
	// abstract
	if (abstrakt != null && !abstrakt.isEmpty()) {
	    coreMetadata.setAbstract(abstrakt);
	}
	// keywords and PARAMETER IDENTIFIERS

	if (parameters != null && !parameters.isEmpty()) {

	    String[] splittedKeyword = parameters.split(",");
	    for (String s : splittedKeyword) {
		s = s.trim();
		CoverageDescription description = new CoverageDescription();
		//description.setAttributeIdentifier(s);
		description.setAttributeDescription(s);
		//description.setAttributeTitle(s);
		coreMetadata.getMIMetadata().addCoverageDescription(description);
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	    }
	}

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("WEKEO");

	if (orbitType != null && !orbitType.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(orbitType);
	}

	// preview
	if (previewImage != null && !previewImage.isEmpty()) {
	    BrowseGraphic graphic = new BrowseGraphic();
	    graphic.setFileName(previewImage);
	    String fileType = "image/png";
	    if (previewImage.toLowerCase().endsWith(".jpg") || previewImage.toLowerCase().endsWith(".jpeg")) {
		fileType = "image/jpg";
	    } else if (previewImage.toLowerCase().endsWith(".tiff") || previewImage.toLowerCase().endsWith(".tif")) {
		fileType = "image/tiff";
	    }
	    graphic.setFileType(fileType);

	    coreMetadata.getMIMetadata().getDataIdentification().addGraphicOverview(graphic);
	}

	// bbox

	// if (minLat != null && minLon != null) {
	//
	// coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);
	// }
	if (bbox != null && !bbox.isEmpty()) {
	    try {

		String bboxExtent = toBBOX(bbox, false);

		double west = Double.valueOf(bboxExtent.split(" ")[1]);
		double east = Double.valueOf(bboxExtent.split(" ")[3]);
		double north = Double.valueOf(bboxExtent.split(" ")[2]);
		double south = Double.valueOf(bboxExtent.split(" ")[0]);

		dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(north, west, south, east);

	    } catch (NumberFormatException ex) {
		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }
	}

	// temporal extent
	TemporalExtent tempExtent = new TemporalExtent();

	if (startDate != null && !startDate.isEmpty() && !startDate.contains("unknown")) {
	    tempExtent.setBeginPosition(startDate);
	    // coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	}
	if (endDate != null && !endDate.isEmpty() && !endDate.contains("unknown")) {
	    tempExtent.setEndPosition(endDate);
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(endDate);
	    coreMetadata.getMIMetadata().setDateStampAsDate(endDate);
	    // coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	}
	
	if(tempExtent.getBeginPosition() != null && tempExtent.getEndPosition() == null) {
	    TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	    tempExtent.setIndeterminateEndPosition(endTimeInderminate);
	}
	
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	if (created != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(created);
	    if (coreMetadata.getMIMetadata().getDateStamp() == null) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(created);
		coreMetadata.getMIMetadata().setDateStampAsDate(created);
	    }
	}

	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");// 2020-01-02T06:06:30.000+0000
	// df.setTimeZone(TimeZone.getTimeZone("UTC"));
	// Date d;
	// try {
	// d = df.parse(endTime);
	// coreMetadata.getMIMetadata().setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(d));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }




	// instrument
	// // INSTRUMENT IDENTIFIERS
	if (instrumentName != null && !instrumentName.isEmpty()) {

	    MIInstrument myInstrument = new MIInstrument();
	    String id = (instrumentId != null && !instrumentId.isEmpty()) ? instrumentId : instrumentName;
	    String type = (orbitType != null && !orbitType.isEmpty()) ? orbitType : id;
	    myInstrument.setMDIdentifierTypeIdentifier(id);
	    myInstrument.setMDIdentifierTypeCode(param);
	    myInstrument.setDescription(instrumentName);
	    myInstrument.setTitle(instrumentName);
	    // myInstrument.getElementType().getCitation().add(e)
	    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("instrument");
	    keyword.addKeyword(instrumentName);// or sensorModel
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// platform
	if (platformId != null && !platformId.isEmpty()) {
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(platformId);
	    platformDescription = (platformDescription != null && !platformDescription.isEmpty()) ? platformDescription : platformId;
	    platform.setDescription(platformDescription);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(platformId);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);
	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    keyword.addKeyword(platformId);// or platformDescription
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	}

	// organization

	if ((contactMail == null || contactMail.isEmpty()) && (contactIndividualName == null || contactIndividualName.isEmpty())
		&& (contactOrganizationName == null || contactOrganizationName.isEmpty())
		&& (contactPhone == null || contactPhone.isEmpty()) && (contactUrl == null || contactUrl.isEmpty())) {
	    logger.warn("NO CONTACT POINT AVAILABLE FOR WEKEO RESOURCE WITH ID : " + datasetID);
	} else {

	    ResponsibleParty istitutionContact = new ResponsibleParty();
	    // Contact contactcreatorContactInfo = new Contact();
	    // Address address = new Address();
	    // address.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	    // contactcreatorContactInfo.setAddress(address);
	    // creatorContact.setContactInfo(contactcreatorContactInfo);

	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    if (contactMail != null && !contactMail.isEmpty()) {
		address.addElectronicMailAddress(contactMail);
		contactInfo.setAddress(address);
	    }
	    if (contactIndividualName != null && !contactIndividualName.isEmpty()) {
		istitutionContact.setIndividualName(contactIndividualName);
	    }

	    if (contactOrganizationName != null && !contactOrganizationName.isEmpty()) {
		istitutionContact.setOrganisationName(contactOrganizationName);
	    }
	    if (contactPhone != null && !contactPhone.isEmpty()) {
		contactInfo.addPhoneVoice(contactPhone);
	    }
	    if (contactUrl != null && !contactUrl.isEmpty()) {
		Online online = new Online();
		online.setLinkage(contactUrl);
		contactInfo.setOnline(online);
	    }
	    istitutionContact.setContactInfo(contactInfo);
	    istitutionContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(istitutionContact);
	}

	// if(owner != null || authorName != null || dataCenterName != null || istitutionName != null ) {
	//
	// ResponsibleParty creatorContact = new ResponsibleParty();
	//// Contact info = new Contact();
	//// Online online = new Online();
	//// online.setLinkage("https://www.ana.gov.br/");
	//// info.setOnline(online);
	//// creatorContact.setContactInfo(info);
	// if(authorName != null)
	// creatorContact.setIndividualName(authorName);
	// if(istitutionName != null) {
	// creatorContact.setOrganisationName(istitutionName);
	// }else if(owner != null){
	// creatorContact.setOrganisationName(owner);
	// } else {
	// creatorContact.setOrganisationName(dataCenterName);
	// }
	// creatorContact.setRoleCode("author");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	// }

	logger.info("WEKEO Mappper ENDED");

    }

    public static String toBBOX(String polygon, boolean firstIsLat) {

	polygon = polygon.replace("MULTIPOLYGON", "").//
		replace("POLYGON", "").//
		replace("(((", "").//
		replace("((", "").//
		replace(")))", "").//
		replace("))", "").//
		replace(",", " ").//
		replace("  ", " ").//
		trim();

	String[] coords = polygon.split(" ");

	Double[] lats = new Double[coords.length / 2];
	Double[] lons = new Double[coords.length / 2];

	for (int i = 0; i < coords.length; i++) {

	    if (i % 2 == 0) {

		if (firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    } else {

		if (!firstIsLat) {
		    lats[i / 2] = Double.valueOf(coords[i]);
		} else {
		    lons[i / 2] = Double.valueOf(coords[i]);
		}
	    }
	}

	Double minLat = lats[0];
	Double maxLat = lats[0];
	Double minLon = lons[0];
	Double maxLon = lons[0];

	for (int i = 1; i < lats.length; i++) {

	    if (lats[i] < minLat) {
		minLat = lats[i];
	    }
	    if (lats[i] > maxLat) {
		maxLat = lats[i];
	    }

	    if (lons[i] < minLon) {
		minLon = lons[i];
	    }
	    if (lons[i] > maxLon) {
		maxLon = lons[i];
	    }
	}

	return minLat + " " + minLon + " " + maxLat + " " + maxLon;
    }

}
