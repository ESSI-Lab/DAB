/**
 *
 */
package eu.essi_lab.accessor.prisma;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MDResolution;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.SatelliteScene;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.iso19139.gco.v_20060504.RealPropertyType;
import net.opengis.iso19139.gmd.v_20060504.MDBandType;
import net.opengis.iso19139.gmd.v_20060504.MDRangeDimensionPropertyType;
import net.opengis.iso19139.gmd.v_20060504.ObjectFactory;

/**
 * @author roncella
 */
public class PRISMAMapper extends OriginalIdentifierMapper {

    public static final String PRISMA_SCHEME_URI = "prisma-scheme-uri";

    // @Override
    // protected String createOriginalIdentifier(GSResource resource) {
    //
    // try {
    // Properties props = new Properties();
    // props.load(new
    // ByteArrayInputStream(resource.getOriginalMetadata().getMetadata().getBytes(StandardCharsets.UTF_8)));
    //
    // String sceneId = props.getProperty("LANDSAT_SCENE_ID");
    // if (Objects.nonNull(sceneId) && !sceneId.isEmpty()) {
    //
    // return sceneId.replace("\"", "");
    // }
    //
    // } catch (Exception e) {
    // GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
    // }
    //
    // return null;
    // }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    /**
     * @param originalMD
     * @param dataset
     * @throws GSException
     */
    // missing: parentid, fileidentifier, check dates, band, crs, spatialResolution, spatialUnits, limitations on Public
    // Access,
    // conditions, distribution format, lineage, keywords, parameter

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	String[] dataSplit = originalMetadata.split("\\|");

	// 0 FILENAME CODE
	String fileName = dataSplit[0];
	// 1 Validity Start
	String startDate = dataSplit[1];
	// 2 Validity Stop
	String endDate = dataSplit[2];
	// 3 Polygon
	String polygon = dataSplit[3];
	// 4 Cloud %
	String cloudCoverage = dataSplit[4];
	// 5 Metadata Point of Contact Organization Name
	String metadataOrganizationName = dataSplit[5];
	// 6 Metadata Point of Contact Organization Mail
	String metadataOrganizationMail = dataSplit[6];
	// 7 Metadata Date Stamp
	String dateStamp = dataSplit[7];
	// 8 Coordinate Reference System (CRS)
	String crs = dataSplit[8];
	// 9 Title
	String title = dataSplit[9];
	// 10 Dataset Identifier
	String identifier = dataSplit[10];
	// 11 Abstract
	String abstrakt = dataSplit[11];
	// 12 Dataset Creator Organization Name
	String creatorOrganizationName = dataSplit[12];
	// 13 Dataset Creator Organization Mail
	String creatorOrganizationMail = dataSplit[13];
	// 14 Dataset Spatial Resolution
	String spatialResolution = dataSplit[14];
	// 15 Dataset Spatial Units
	String spatialUnit = dataSplit[15];
	// 16 Limitations on Public Access
	String limitations = dataSplit[16];
	// 17 Conditions applying to Access and Use
	String conditions = dataSplit[17];
	// 18 Distribution Format
	String distributionFormat = dataSplit[18];
	// 19 Distribution link
	String distributionLink = dataSplit[19];
	// 20 Lineage
	String lineage = dataSplit[20];
	// 21 Graphic Overview
	String overview = dataSplit[21];
	// 22 Keywords
	String keywords = dataSplit[22];
	// 23 Measured Attribute (parameter) Name
	String parameter = dataSplit[23];
	// 24 Band Bound Min
	String bandMin = dataSplit[24];
	// 25 Band Bound Max
	String bandMax = dataSplit[25];
	// 26 Instrument Name
	String instrumentName = dataSplit[26];
	// 27 Instrument Identifier
	String instrumentIdentifier = dataSplit[27];
	// 28 Instrument Type
	String instrumentType = dataSplit[28];
	// 29 Platform Name
	String platformName = dataSplit[29];
	// 30Platform Identifier
	String platformIdentifier = dataSplit[30];
	// 31 Sensor Name
	String sensorName = dataSplit[31];
	// 32 Sensor Identifier
	String sensorIdentifier = dataSplit[32];
	// 33 Sensor Type
	String sensorType = dataSplit[33];

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	// ---------------
	//
	// parent
	//
	// String parent = props.getProperty("PARENTID").replace("\"", "");
	coreMetadata.getMIMetadata().setParentIdentifier("ASI_PRISMA");

	// MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	// -----------------
	//
	// title
	//
	//

	coreMetadata.setTitle(title);

	// abstrakt

	coreMetadata.setAbstract(abstrakt);

	// time stamp
	if (dateStamp.contains("/")) {

	    String[] splittedDate = dateStamp.split("\\/");
	    String dateTimeString = splittedDate[2] + "-" + splittedDate[1] + "-" + splittedDate[0];
	    coreMetadata.getMIMetadata().setDateStampAsDate(dateTimeString);
	    // try {
	    // Optional<Date> iso8601 = ISO8601DateTimeUtils.parseNotStandardToDate(dateTimeString);
	    // if (iso8601.isPresent()) {
	    //
	    //
	    // miMetadata.setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(iso8601.get()));
	    //
	    // }
	    // } catch (DatatypeConfigurationException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // } catch (ParseException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }
	}

	// temporal extent
	if(startDate.contains(" ")) {
	    startDate = startDate.replace(" ", "T") + "Z";
	}
	if(endDate.contains(" ")) {
	    endDate = endDate.replace(" ", "T") + "Z";
	}
	dataset.getHarmonizedMetadata().getCoreMetadata().addTemporalExtent(startDate, endDate);

	// ---------------
	//
	// bbox
	//

	String bbox = BBOXUtils.toBBOX(polygon, false);

	double west = Double.valueOf(bbox.split(" ")[1]);
	double east = Double.valueOf(bbox.split(" ")[3]);
	double north = Double.valueOf(bbox.split(" ")[2]);
	double south = Double.valueOf(bbox.split(" ")[0]);

	coreMetadata.addBoundingBox(north, west, south, east);

	// ---------------
	//
	// thumbnail
	//

	BrowseGraphic browseGraphic = new BrowseGraphic();
	browseGraphic.setFileDescription("Overview of PRISMA scene " + fileName);
	browseGraphic.setFileName(overview);
	coreMetadata.getMIMetadata().getDataIdentification().addGraphicOverview(browseGraphic);

	// ---------------
	//
	// keywords
	//
	String[] splittedKeywords = keywords.split(",");
	for (String k : splittedKeywords) {
	    k = k.startsWith(" ") ? k.substring(1) : k;
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(k);
	}

	// parameter
	if (parameter.contains(".")) {
	    String[] splittedParameters = parameter.split("\\.");
	    for (String s : splittedParameters) {
		if (!s.isEmpty() && s.contains(":")) {

		    String[] params = s.split(":");

		    CoverageDescription description = new CoverageDescription();
		    s = s.startsWith(" ") ? s.substring(1) : s;
		    description.setAttributeIdentifier(params[0]);
		    description.setAttributeDescription(s);
		    description.setAttributeTitle(params[1]);
		    coreMetadata.getMIMetadata().addCoverageDescription(description);
		}
	    }
	}

	// instrument
	// // INSTRUMENT IDENTIFIERS
	// if (instrumentName != null && !instrumentName.isEmpty()) {
	// }

	// 26 Instrument Name
	// 27 Instrument Identifier
	// 28 Instrument Type
	MIInstrument miInstrument = new MIInstrument();
	miInstrument.setMDIdentifierTypeIdentifier(instrumentIdentifier);
	miInstrument.setMDIdentifierTypeCode(instrumentIdentifier);
	miInstrument.setDescription(instrumentName);
	miInstrument.setTitle(instrumentName);
	miInstrument.setSensorType(instrumentType);
	// myInstrument.getElementType().getCitation().add(e)
	coreMetadata.getMIMetadata().addMIInstrument(miInstrument);

	// Keywords keyword = new Keywords();
	// keyword.setTypeCode("instrument");
	// keyword.addKeyword(instrumentName);// or sensorModel
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	// platform
	// if (platformId != null && !platformId.isEmpty()) {
	// }

	// 29 Platform Name
	// 30 Platform Identifier
	MIPlatform platform = new MIPlatform();
	platform.setMDIdentifierCode(platformIdentifier);
	platform.setDescription(platformName);
	Citation platformCitation = new Citation();
	platformCitation.setTitle(platformName);
	platform.setCitation(platformCitation);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	// Keywords keyword = new Keywords();
	// keyword.setTypeCode("platform");
	// keyword.addKeyword(platformIdentifier);// or platformDescription
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	// ----------------
	//
	// sensor identifier
	// sensor name
	// sensor type

	MIInstrument sensorInstrument = new MIInstrument();
	sensorInstrument.setMDIdentifierTypeIdentifier(sensorIdentifier);
	sensorInstrument.setMDIdentifierTypeCode(sensorType);
	sensorInstrument.setDescription(sensorName);
	sensorInstrument.setTitle(sensorName);
	sensorInstrument.setSensorType(sensorType);
	coreMetadata.getMIMetadata().addMIInstrument(sensorInstrument);

	// ----------------------
	//
	// limitations and conditions
	//
	//
//	LegalConstraints legalConstraints = new LegalConstraints();
//	legalConstraints.addUseLimitation(limitations);
//	legalConstraints.addAccessConstraintsCode("other");
//	// legalConstraints.addUseConstraintsCode("Lisenssi");
//	coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);

	LegalConstraints access = new LegalConstraints();
	access.addAccessConstraintsCode("other");
	//access.addUseLimitation(limitations);
	// access.addOtherConstraints("le");
	access.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(
		"http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b"));
	coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(access);

	LegalConstraints rc = new LegalConstraints();
	rc.addUseConstraintsCode("other");
	//rc.addOtherConstraints(conditions);
	// rc.addOtherConstraints("le");
	rc.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(conditions));
	coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(rc);

	// ----------------------
	//
	// lineage
	//
	//
	DataQuality dataQuality = new DataQuality();
	dataQuality.setLineageStatement(lineage);
	coreMetadata.getMIMetadata().addDataQuality(dataQuality);

	// ----------------------
	//
	// cloud cover percentage
	//
	//
	Double cloud = Double.valueOf(cloudCoverage);
	coreMetadata.getMIMetadata().addCloudCoverPercentage(cloud);

	// ----------------------
	//
	// resolution
	//
	//
	List<String> resolutions = new ArrayList<String>();
	if (spatialResolution.contains(",")) {

	    String[] splittedResolution = spatialResolution.split(",");
	    for (String s : splittedResolution) {
		if (s.contains(" ")) {

		    if (s.startsWith(" ")) {
			s = s.substring(1);
		    }
		    resolutions.add(s.split(" ")[0]);
		}
	    }
	}

	for (String res : resolutions) {
	    MDResolution resolution = new MDResolution();
	    resolution.setDistance(spatialUnit, Double.valueOf(res));
	    coreMetadata.getMIMetadata().getDataIdentification().addSpatialResolution(resolution);
	}

	// -------------------
	//
	// organizations
	//
	// creator name and email
	ResponsibleParty creatorResponsibleParty = new ResponsibleParty();

	Contact contactInfo = new Contact();
	Address address = new Address();
	address.addElectronicMailAddress(creatorOrganizationMail);
	contactInfo.setAddress(address);
	// responsibleParty.setIndividualName(contactIndividualName);

	// TODO: distribution link at the moment points to https://prisma.asi.it/
	Online online = new Online();
	online.setLinkage(distributionLink);
	contactInfo.setOnline(online);

	creatorResponsibleParty.setContactInfo(contactInfo);
	creatorResponsibleParty.setRoleCode("originator");
	creatorResponsibleParty.setOrganisationName(creatorOrganizationName);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorResponsibleParty);

	// metadata point of contact and mail
	ResponsibleParty metadataResponsibleParty = new ResponsibleParty();

	Contact metadatacontactInfo = new Contact();
	Address metadataAddress = new Address();
	metadataAddress.addElectronicMailAddress(metadataOrganizationMail);
	metadatacontactInfo.setAddress(metadataAddress);
	// responsibleParty.setIndividualName(contactIndividualName);

	// TODO: distribution link at the moment points to https://prisma.asi.it/
	Online metadataOnline = new Online();
	metadataOnline.setLinkage(distributionLink);
	metadatacontactInfo.setOnline(metadataOnline);

	metadataResponsibleParty.setContactInfo(metadatacontactInfo);
	metadataResponsibleParty.setRoleCode("pointOfContact");
	metadataResponsibleParty.setOrganisationName(metadataOrganizationName);
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(metadataResponsibleParty);

	// ---------------
	//
	// min and max band
	//
	//
//	List<MDRangeDimensionPropertyType> rangeDimensions = coreMetadata.getMIMetadata().getCoverageDescription().getElementType()
//		.getDimension();
//	
	List<MDRangeDimensionPropertyType> rangeDimensions = new ArrayList<MDRangeDimensionPropertyType>();
	
	ObjectFactory of = new ObjectFactory();
	MDBandType band = new MDBandType();
	RealPropertyType maxReal = new RealPropertyType();
	RealPropertyType minReal = new RealPropertyType();
	String min = "";
	String max = "";
	if (bandMin.contains(" ")) {
	    min = bandMin.split(" ")[0];
	}
	if (bandMax.contains(" ")) {
	    max = bandMax.split(" ")[0];
	}
	maxReal.setReal(Double.valueOf(max));
	minReal.setReal(Double.valueOf(min));
	band.setMaxValue(maxReal);
	band.setMinValue(minReal);

	JAXBElement<MDBandType> range = of.createMDBand(band);
	MDRangeDimensionPropertyType e = new MDRangeDimensionPropertyType();
	e.setMDRangeDimension(range);
	rangeDimensions.add(e);
	
	Iterator<CoverageDescription> coverages = coreMetadata.getMIMetadata().getCoverageDescriptions();

	while(coverages.hasNext()) {
	    CoverageDescription c = coverages.next();
	    c.getElementType().setDimension(rangeDimensions);
	}
	//.getElementType().setDimension(rangeDimensions);

	// ---------------
	//
	// scene id
	//
	//
	// String sceneId = props.getProperty("LANDSAT_PRODUCT_ID");
	// if (Objects.isNull(sceneId) || sceneId.isEmpty()) {
	// sceneId = props.getProperty("LANDSAT_SCENE_ID").replace("\"", "");
	// } else {
	// sceneId = sceneId.replace("\"", "");
	// }

	// ------------------
	//
	// online
	//
	//
	coreMetadata.addDistributionFormat(distributionFormat);

	// ------------------------
	//
	// SatelliteScene extension
	//
	//
	SatelliteScene satelliteScene = new SatelliteScene();
	satelliteScene.setOrigin("prisma");

	// String productType = props.getProperty("DATA_TYPE").replace("\"", "");
	satelliteScene.setProductType("L1,L2B,L2C,L2D");

	String queryables = "prodType,";
	queryables += "cloudcp,";
	queryables += "pubDatefrom,";
	queryables += "pubDateuntil,";

	satelliteScene.setCollectionQueryables(queryables);

	dataset.getExtensionHandler().setSatelliteScene(satelliteScene);

    }

    /**
     * @param sensorId
     * @return
     */
    private String createSensorTitle(String sensorId) {

	String ret = null;

	if (sensorId != null && sensorId.toLowerCase().contains("oli")) {
	    ret = "Operational Land Imager";
	}

	if (sensorId != null && sensorId.toLowerCase().contains("tirs")) {
	    if (ret == null) {
		ret = "";
	    } else {
		ret += " ";
	    }
	    ret += "Thermal Infrared Sensor";
	}

	return ret;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return PRISMA_SCHEME_URI;
    }
}
