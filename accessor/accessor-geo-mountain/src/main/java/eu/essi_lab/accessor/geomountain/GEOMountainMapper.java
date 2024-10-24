/**
 *
 */
package eu.essi_lab.accessor.geomountain;

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
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class GEOMountainMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

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
    /**
     * 0)GEO Mountains ID,
     * 1)Name,
     * 2)Category,
     * 3)Latitude,
     * 4)Longitude,
     * 5)Elevation and/or range (m a.s.l),
     * 6)Country,
     * 7)Purpose,
     * 8)Operating Organization(s),
     * 9)URL(s) of site/station webpage,
     * 10)Email address or other contact,
     * 11)Parameters measured,
     * 12)Temporal coverage,
     * 13)Temporal Frequency,
     * 14)Measurement method(s) / protocol(s) followed,
     * 15)Instrumentation Deployed,
     * 16)Data freely available for download (including via fast online inscription process)?,
     * 17)URL(s) to data repository and download (if applicable) ,
     * 18)DOI(s) of associated publication(s),
     * 19)Parent network and / or other comment(s)
     */

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String originalMetadata = originalMD.getMetadata();

	String[] dataSplit = originalMetadata.split("\\|");

	// 0 GEOMOUNTAIN ID
	String fileId = checkString(dataSplit[0]);

	try {

	    // 1 Name
	    String title = checkString(dataSplit[1]);
	    // 2 Category
	    String category = checkString(dataSplit[2]);
	    // 3 Latitude
	    String lat = checkString(dataSplit[3]);
	    // 4 Longitude
	    String lon = checkString(dataSplit[4]);
	    // 5 Elevation and/or range (m a.s.l)
	    String elevation = checkString(dataSplit[5]);
	    // 6 Country
	    String country = checkString(dataSplit[6]);
	    // 7 Purpose
	    String purpose = checkString(dataSplit[7]);
	    // 8 Operating Organization(s)
	    String organizations = checkString(dataSplit[8]);
	    // 9 URL(s) of site/station webpage
	    String webpage = checkString(dataSplit[9]);
	    // 10 Email address or other contact
	    String email = checkString(dataSplit[10]);
	    // 11 Parameters measured
	    String parameters = checkString(dataSplit[11]);
	    // 12 Temporal coverage
	    String temporalCoverage = checkString(dataSplit[12]);
	    // 13 Temporal frequency
	    String temporalFrequency = checkString(dataSplit[13]);
	    // 14 Measurement method(s) / protocol(s) followed
	    String measurementMethod = checkString(dataSplit[14]);
	    // 15 Instrumentation Deployed
	    String instrument = checkString(dataSplit[15]);
	    // 16 Data freely available for download (including via fast online inscription process)?
	    String isDownloadAvailable = checkString(dataSplit[16]);
	    // 17 URL(s) to data repository and download (if applicable)
	    String urls = checkString(dataSplit[17]);
	    // 18 DOI(s) of associated publication(s)
	    String doiUrls = checkString(dataSplit[18]);
	    // 19 Parent network and / or other comment(s)
	    String orgNetwork = checkString(dataSplit[19]);

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	    
	    ExtensionHandler extensionHandler = dataset.getExtensionHandler();

	    // ---------------
	    //
	    // parent
	    //
	    // String parent = props.getProperty("PARENTID").replace("\"", "");

	    // MIMetadata miMetadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();

	    // -----------------
	    //
	    // id
	    if(fileId != null) {
		coreMetadata.setIdentifier(fileId);
		coreMetadata.getDataIdentification().setResourceIdentifier(fileId);
	    }

	    // -----------------
	    //
	    // title
	    //
	    //
	    if (title != null) {
		// title
		coreMetadata.setTitle(title);
	    }

	    String abstrakt = null;
	    if (category != null) {
		extensionHandler.addThemeCategory(category);
		abstrakt = category;
		abstrakt = (orgNetwork != null) ? abstrakt + " - " + orgNetwork : abstrakt;
	    } else if (orgNetwork != null) {
		abstrakt = orgNetwork;
	    }
	    if(orgNetwork != null) {
		extensionHandler.addOriginatorOrganisationDescription(orgNetwork);
	    }

	    // abstrakt
	    if (abstrakt != null) {
		coreMetadata.setAbstract(abstrakt);
	    }

	    // ---------------
	    //
	    // temporal extent
	    //

	    if (temporalCoverage != null && !temporalCoverage.toLowerCase().contains("to complete")) {
		TemporalExtent temporalExtent = new TemporalExtent();
		temporalCoverage = temporalCoverage.trim();
		// begin-end use case: e.g. 2009-2014, 2009-present
		if (temporalCoverage.contains("-")) {
		    String[] splittedTemporal = temporalCoverage.split("-");
		    temporalExtent.setBeginPosition(splittedTemporal[0] + "-01-01T00:00:00");
		    if (splittedTemporal[1].toLowerCase().contains("present")) {
			// end position indeterminate
			temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
			setIndeterminatePosition(dataset);
		    } else {
			temporalExtent.setEndPosition(splittedTemporal[1] + "-12-31T00:00:00");
		    }

		} else {
		    // single case: e.g. 2014
		    temporalExtent.setBeginPosition(temporalCoverage + "-01-01T00:00:00");
		}

		coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	    }

	    // ---------------
	    //
	    // bbox
	    //

	    if (lat != null && lon != null) {
		lat = lat.trim();
		lon = lon.trim();
		try {
		    Double dLat = Double.valueOf(lat);
		    Double dLon = Double.valueOf(lon);
		    if (dLat != null && dLon != null && !dLat.isNaN() && !dLon.isNaN()) {
			coreMetadata.addBoundingBox(dLat, dLon, dLat, dLon);
		    }
		} catch (Exception e) {
		    logger.error("BBOX exception! Lat({}) or Lon({}) are not Double for record with ID={}", lat, lon, fileId);
		}
	    }

	    // --------------
	    //
	    // vertical extent
	    //
	    Double minAlt = null;
	    Double maxAlt = null;

	    if (elevation != null) {
		elevation = elevation.trim();
		try {
		    if (elevation.contains(";")) {
			// multiple values: e.g. 2800; 2500-3100
			String[] splittedEl = elevation.split(";");
			if (splittedEl.length == 2) {
			    String range = splittedEl[1];
			    if (range.contains("-")) {
				String[] splittedRange = range.split("-");
				minAlt = Double.valueOf(splittedRange[0]);
				maxAlt = Double.valueOf(splittedRange[1]);
			    }
			} else {
			    logger.error("!!!SHOULD NOT HAPPEN!!!");
			}
		    } else if (elevation.contains("-")) {
			// min/max value: e.g. 1500-3600
			String[] splittedRange = elevation.split("-");
			minAlt = Double.valueOf(splittedRange[0]);
			maxAlt = Double.valueOf(splittedRange[1]);
		    } else {
			// single value: e.g. 1650
			minAlt = Double.valueOf(elevation);
			maxAlt = Double.valueOf(elevation);
		    }
		    if (minAlt != null && maxAlt != null)
			coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(minAlt, maxAlt);

		} catch (Exception e) {
		    logger.error("Reading elevation values ERROR exception for record with ID={}", fileId);
		}
	    }

	    // ---------------
	    //
	    // country
	    //
	    if (country != null && !country.toLowerCase().contains("to complete")) {
		extensionHandler.setCountry(country);
		Keywords keyword = new Keywords();
		keyword.addKeyword(country);// or sensorModel
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(country);
	    }

	    // ---------------
	    //
	    // purpose
	    //
	    if (purpose != null && !purpose.toLowerCase().contains("to complete")) {
		Keywords keyword = new Keywords();
		keyword.addKeyword(purpose);// or sensorModel
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(purpose);
	    }

	    // ---------------
	    //
	    // keywords
	    //
	    // String[] splittedKeywords = keywords.split(",");
	    // for (String k : splittedKeywords) {
	    // k = k.startsWith(" ") ? k.substring(1) : k;
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(k);
	    // }

	    List<String> paramList = new ArrayList<String>();
	    if (parameters != null) {
		String[] splittedParameters = null;
		if (parameters.contains(";")) {
		    splittedParameters = parameters.split(";");
		    for (String s : splittedParameters) {
			if (!s.isEmpty()) {
			    paramList.add(s.trim());
			}
		    }
		} else if (parameters.contains(",")) {
		    splittedParameters = parameters.split(",");
		    for (String s : splittedParameters) {
			if (!s.isEmpty()) {
			    paramList.add(s.trim());
			}
		    }
		}
	    }

	    for (String s : paramList) {
		CoverageDescription description = new CoverageDescription();
		description.setAttributeIdentifier(s);
		description.setAttributeDescription(s);
		description.setAttributeTitle(s);
		coreMetadata.getMIMetadata().addCoverageDescription(description);
	    }

	    /**
	     * Instrument Name
	     */
	    if (instrument != null && !instrument.toLowerCase().contains("to complete")) {
		MIInstrument miInstrument = new MIInstrument();
		miInstrument.setMDIdentifierTypeIdentifier(instrument);
		miInstrument.setMDIdentifierTypeCode(instrument);
		miInstrument.setDescription(instrument);
		miInstrument.setTitle(instrument);
		miInstrument.setSensorType(instrument);
		coreMetadata.getMIMetadata().addMIInstrument(miInstrument);
		Keywords keyword = new Keywords();
		keyword.setTypeCode("instrument");
		keyword.addKeyword(instrument);// or sensorModel
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    }

	    // platform
	    // if (platformId != null && !platformId.isEmpty()) {
	    // }

	    // 29 Platform Name
	    // 30 Platform Identifier
	    // MIPlatform platform = new MIPlatform();
	    // platform.setMDIdentifierCode(platformIdentifier);
	    // platform.setDescription(platformName);
	    // Citation platformCitation = new Citation();
	    // platformCitation.setTitle(platformName);
	    // platform.setCitation(platformCitation);
	    // coreMetadata.getMIMetadata().addMIPlatform(platform);

	    // Keywords keyword = new Keywords();
	    // keyword.setTypeCode("platform");
	    // keyword.addKeyword(platformIdentifier);// or platformDescription
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    // ----------------------
	    //
	    // limitations and conditions
	    //
	    //
	    // LegalConstraints legalConstraints = new LegalConstraints();
	    // legalConstraints.addUseLimitation(limitations);
	    // legalConstraints.addAccessConstraintsCode("other");
	    // // legalConstraints.addUseConstraintsCode("Lisenssi");
	    // coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);

	    // LegalConstraints access = new LegalConstraints();
	    // access.addAccessConstraintsCode("other");
	    // // access.addUseLimitation(limitations);
	    // // access.addOtherConstraints("le");
	    // access.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(
	    // "http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/INSPIRE_Directive_Article13_1b"));
	    // coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(access);
	    //
	    // LegalConstraints rc = new LegalConstraints();
	    // rc.addUseConstraintsCode("other");
	    // // rc.addOtherConstraints(conditions);
	    // // rc.addOtherConstraints("le");
	    // rc.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(conditions));
	    // coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(rc);

	    // ----------------------
	    //
	    // temporal interpolation
	    //
	    //

	    if (temporalFrequency != null && !temporalFrequency.toLowerCase().contains("to complete")) {
		if (temporalFrequency.toLowerCase().contains("daily")) {
		    extensionHandler.setTimeUnits("day");
		    extensionHandler.setTimeUnitsAbbreviation("d");
		    extensionHandler.setTimeResolution("1");
		    extensionHandler.setTimeSupport("1");
		} else if (temporalFrequency.toLowerCase().contains("hourly")) {
		    extensionHandler.setTimeUnits("hour");
		    extensionHandler.setTimeUnitsAbbreviation("h");
		    extensionHandler.setTimeResolution("1");
		    extensionHandler.setTimeSupport("1");
		} else if (temporalFrequency.toLowerCase().contains("monthly")) {
		    extensionHandler.setTimeUnits("month");
		    extensionHandler.setTimeUnitsAbbreviation("M");
		    extensionHandler.setTimeResolution("1");
		    extensionHandler.setTimeSupport("1");
		} else if (temporalFrequency.contains(" ")) {
		    String[] splittedTempFreq = temporalFrequency.split(" ");
		    if (splittedTempFreq[1].toLowerCase().contains("year")) {
			extensionHandler.setTimeUnits("year");
			extensionHandler.setTimeUnitsAbbreviation("Y");
			extensionHandler.setTimeResolution(splittedTempFreq[0]);
			extensionHandler.setTimeSupport(splittedTempFreq[0]);
		    } else if (splittedTempFreq[1].toLowerCase().contains("min")) {
			extensionHandler.setTimeUnits("minutes");
			extensionHandler.setTimeUnitsAbbreviation("m");
			extensionHandler.setTimeResolution(splittedTempFreq[0]);
			extensionHandler.setTimeSupport(splittedTempFreq[0]);
		    } else {
			extensionHandler.setTimeUnits(splittedTempFreq[1]);
			// dataset.getExtensionHandler().setTimeUnitsAbbreviation("m");
			extensionHandler.setTimeResolution(splittedTempFreq[0]);
			extensionHandler.setTimeSupport(splittedTempFreq[0]);
		    }
		}

	    }

	    // -------------------
	    //
	    // organizations
	    //
	    // creator organization and website and email
	    ResponsibleParty responsibleParty = new ResponsibleParty();
	    Contact contactInfo = new Contact();
	    Address address = new Address();
	    Online online = new Online();
	    if (email != null && !email.toLowerCase().contains("to complete")) {
		String[] splittedMail = email.split(" ");
		if (splittedMail.length > 1) {
		    address.addElectronicMailAddress(splittedMail[0]);
		    contactInfo.setAddress(address);
		} else if (email.contains("@")) {
		    address.addElectronicMailAddress(email);
		    contactInfo.setAddress(address);
		} else if (email.startsWith("http")) {
		    online.setLinkage(email);
		    contactInfo.setOnline(online);
		}
	    }
	    if (organizations != null && !organizations.toLowerCase().contains("to complete")) {
		extensionHandler.addOriginatorOrganisationIdentifier(organizations);
		responsibleParty.setOrganisationName(organizations);
	    }
	    responsibleParty.setContactInfo(contactInfo);
	    responsibleParty.setRoleCode("pointOfContact");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(responsibleParty);

	    // ------------------
	    //
	    // online
	    //
	    //
	    /**
	     * URL(s) to data repository and download (if applicable)
	     */
	    if (urls != null && !urls.toLowerCase().contains("to complete")) {
		List<Online> onlines = createOnlineList(urls, "Data repository or download URL");
		for (Online o : onlines) {
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);
		}
	    }

	    /**
	     * Distribution DOI
	     */
	    if (doiUrls != null && !doiUrls.toLowerCase().contains("to complete")) {
		List<Online> onlines = createOnlineList(doiUrls, "DOI");
		for (Online o : onlines) {
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);
		}
	    }

	    /**
	     * Webpages
	     */
	    if (webpage != null && !webpage.toLowerCase().contains("to complete")) {
		List<Online> onlines = createOnlineList(webpage, "Site/Station Webpage");
		for (Online o : onlines) {
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);
		}
	    }

	} catch (Exception e) {
	    logger.error("GEOMountain Mapper error: " + e.getMessage());
	    logger.error("Error with record ID= " + fileId);
	    e.printStackTrace();
	}

	// List<String> doiList = new ArrayList<String>();
	// if (doiUrls != null) {
	//
	// String[] splittedDOI;
	// if (doiUrls.contains(";")) {
	// // multiple url case: e.g. link1; link2
	// splittedDOI = doiUrls.split(";");
	// for (String s : splittedDOI) {
	// s = s.trim();
	// if (s.startsWith("http")) {
	// doiList.add(s);
	// }
	// }
	// } else if (doiUrls.contains(" ")) {
	// // simple link and comment: e.g. http://globalcryospherewatch.org/cryonet/sitepage.php?surveyid=21 (no
	// // specific link)
	// splittedDOI = doiUrls.split(" ");
	// if (splittedDOI[0].startsWith("http")) {
	// doiList.add(splittedDOI[0]);
	// }
	// } else {
	// doiList.add(doiUrls);
	// }
	// }
	//
	// for (String s : doiList) {
	// Online doiOnline = new Online();
	//
	// doiOnline.setLinkage(s);
	// doiOnline.setProtocol(NetProtocols.HTTP.getCommonURN());
	// doiOnline.setFunctionCode("information");
	// doiOnline.setDescription("DOI");
	//
	// coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(doiOnline);
	// }
	//
	// /**
	// * Webpages
	// */
	// List<String> webPageLits = new ArrayList<String>();
	// if (webpage != null) {
	//
	// String[] splittedPage;
	// if (webpage.contains(";")) {
	// // multiple url case: e.g. link1; link2
	// splittedPage = webpage.split(";");
	// for (String s : splittedPage) {
	// s = s.trim();
	// if (s.startsWith("http")) {
	// webPageLits.add(s);
	// }
	// }
	// } else if (webpage.contains(" ")) {
	// // simple link and comment: e.g. http://globalcryospherewatch.org/cryonet/sitepage.php?surveyid=21 (no
	// // specific link)
	// splittedPage = webpage.split(" ");
	// if (splittedPage[0].startsWith("http")) {
	// webPageLits.add(splittedPage[0]);
	// }
	// } else {
	// webPageLits.add(webpage);
	// }
	// }
	//
	// for (String s : webPageLits) {
	// Online webPageOnline = new Online();
	//
	// webPageOnline.setLinkage(s);
	// webPageOnline.setProtocol(NetProtocols.HTTP.getCommonURN());
	// webPageOnline.setFunctionCode("information");
	// webPageOnline.setDescription("DOI");
	//
	// coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(webPageOnline);
	// }

	// coreMetadata.addDistributionFormat(distributionFormat);

    }

    private List<Online> createOnlineList(String url, String type) {
	List<Online> ret = new ArrayList<Online>();
	List<String> linkageList = new ArrayList<String>();
	String[] splittedUrl;
	if (url.contains(";")) {
	    // multiple url case: e.g. link1; link2
	    splittedUrl = url.split(";");
	    for (String s : splittedUrl) {
		s = s.trim();
		if (s.startsWith("http")) {
		    linkageList.add(s);
		}
	    }
	} else if (url.contains(" ")) {
	    // simple link and comment: e.g. http://globalcryospherewatch.org/cryonet/sitepage.php?surveyid=21 (no
	    // specific link)
	    splittedUrl = url.split(" ");
	    if (splittedUrl[0].startsWith("http")) {
		linkageList.add(splittedUrl[0]);
	    }
	} else {
	    linkageList.add(url);
	}
	for (String s : linkageList) {
	    Online online = new Online();
	    online.setLinkage(s);
	    online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    online.setFunctionCode("information");
	    online.setDescription(type);
	    ret.add(online);

	}
	return ret;

    }

    private String checkString(String s) {

	if (s == null || s.isEmpty() || s.equals("null")) {
	    return null;
	}
	return s;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.GEOMOUNTAIN;
    }
}
