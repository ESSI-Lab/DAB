package eu.essi_lab.accessor.wcs.mapper;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.accessor.wcs.connector.WCSConnector;
import eu.essi_lab.accessor.wcs.connector.WCSConnector_100;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;

/**
 * @author Fabrizio
 */
public class WCSMapper_100 extends WCSMapper {

    @Override
    protected List<String> getKeywords(XMLDocumentReader capabilities) {

	List<String> out = null;
	try {
	    out = capabilities.evaluateTextContent("/*:WCS_Capabilities/*:Service/*:keywords/*:keyword/text()");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<Double> getBBoxFromCapabilities(XMLNodeReader coverageOffering) {

	try {
	    String lower = coverageOffering.evaluateTextContent("*:lonLatEnvelope/*:pos/text()").get(0);
	    String upper = coverageOffering.evaluateTextContent("*:lonLatEnvelope/*:pos/text()").get(1);

	    Double west = Double.valueOf(lower.split(" ")[0]);
	    Double south = Double.valueOf(lower.split(" ")[1]);
	    Double east = Double.valueOf(upper.split(" ")[0]);
	    Double north = Double.valueOf(upper.split(" ")[1]);

	    return Arrays.asList(south, west, north, east);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected String getIdentifier(XMLDocumentReader coverageDescription) {

	String out = null;
	try {
	    out = coverageDescription.evaluateString("/*:CoverageDescription/*:CoverageOffering/*:name");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<String> getFormats(XMLDocumentReader description) {

	List<String> out = null;
	try {
	    out = description.evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:supportedFormats/*:formats/text()");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected String getTitle(XMLDocumentReader description) {

	String out = null;
	try {
	    out = description.evaluateString("/*:CoverageDescription/*:CoverageOffering/*:label");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected String getAbstract(XMLDocumentReader description) {

	String out = null;
	try {
	    out = description.evaluateString("/*:CoverageDescription/*:CoverageOffering/*:description");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<Double> getBboxFromDescription(XMLDocumentReader coverageDescription) {

	try {

	    String lower = coverageDescription
		    .evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:lonLatEnvelope/*:pos/text()").get(0);
	    String upper = coverageDescription
		    .evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:lonLatEnvelope/*:pos/text()").get(1);

	    if (checkString(lower) && checkString(upper)) {

		Double south = Double.valueOf(lower.split(" ")[1]);
		Double west = Double.valueOf(lower.split(" ")[0]);
		Double north = Double.valueOf(upper.split(" ")[1]);
		Double east = Double.valueOf(upper.split(" ")[0]);

		List<Double> bbox = Arrays.asList(south, west, north, east);
		if (checkBbox(bbox)) {
		    return bbox;
		}
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	try {
	    Node[] bboxes = coverageDescription
		    .evaluateNodes("/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:Envelope");

	    for (int i = 0; i < bboxes.length; i++) {

		Node bbox = bboxes[i];
		XMLNodeReader bboxReader = new XMLNodeReader(bbox);
		String crs = bboxReader.evaluateString("@srsName");
		if (crs != null) {
		    crs = crs.toLowerCase();
		    // lat/lon
		    String lower = coverageDescription
			    .evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:lonLatEnvelope/*:pos/text()").get(0);
		    String upper = coverageDescription
			    .evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:lonLatEnvelope/*:pos/text()").get(1);

		    Double west = Double.valueOf(lower.split(" ")[0]);
		    Double south = Double.valueOf(lower.split(" ")[1]);
		    Double east = Double.valueOf(upper.split(" ")[0]);
		    Double north = Double.valueOf(upper.split(" ")[1]);

		    return Arrays.asList(south, west, north, east);
		}
	    }

	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected String getGridBoudingBoxCRS(XMLDocumentReader coverageDescription) {

	try {

	    String nativeCRS = coverageDescription.evaluateString("/*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:nativeCRSs");
	    if (checkString(nativeCRS)) {
		return nativeCRS;
	    }

	    String crs = coverageDescription.evaluateString(
		    "/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:Envelope/@srsName | /*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:EnvelopeWithTimePeriod/@srsName");
	    if (checkString(crs)) {

		return crs;
	    }
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    protected List<Double> getGridBoudingBox(XMLDocumentReader coverageDescription) {

	try {

	    String nativeCRS = getGridBoudingBoxCRS(coverageDescription);
	    Node[] bboxes = coverageDescription.evaluateNodes(
		    "/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:Envelope | /*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:EnvelopeWithTimePeriod");

	    boolean nativeCRSFound = checkString(nativeCRS);
	    boolean multipleEnvelops = bboxes.length > 1;

	    if (!nativeCRSFound && multipleEnvelops) {
		// in this case there is no way to understand with no doubts in which
		// CRS are expressed the values of the grid envelope
		return new ArrayList<>();
	    }

	    Node bbox = null;
	    if (checkString(nativeCRS)) {
		for (int i = 0; i < bboxes.length; i++) {

		    Node curBox = bboxes[i];
		    String currenctCrs = coverageDescription.evaluateString(curBox, "@srsName");

		    if (checkString(currenctCrs) && nativeCRS.equals(currenctCrs)) {

			bbox = curBox;
			break;
		    }
		}
	    }

	    if (bbox == null) {
		bbox = bboxes[0];
	    }

	    Node[] positions = coverageDescription.evaluateNodes(bbox, "*:pos");
	    String lower = positions[0].getTextContent();
	    String upper = positions[1].getTextContent();

	    Double miny = Double.valueOf(lower.split(" ")[0]);
	    Double minx = Double.valueOf(lower.split(" ")[1]);
	    Double maxy = Double.valueOf(upper.split(" ")[0]);
	    Double maxx = Double.valueOf(upper.split(" ")[1]);

	    return Arrays.asList(miny, minx, maxy, maxx);

	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected TemporalExtent getTimeExtent(XMLDocumentReader coverageDescription) {

	try {

	    String beginPosition = coverageDescription
		    .evaluateString("/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:temporalDomain/*:timePeriod/*:beginPosition");
	    String endPosition = coverageDescription
		    .evaluateString("/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:temporalDomain/*:timePeriod/*:endPosition");
	    String timeRes = coverageDescription
		    .evaluateString("/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:temporalDomain/*:timePeriod/*:timeResolution");

	    if (!checkString(beginPosition) || !checkString(endPosition)) {

		List<String> timeStrings = coverageDescription.evaluateTextContent(
			"/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:temporalDomain/*:timePosition/text() | /*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:EnvelopeWithTimePeriod/*:timePosition");

		if (!timeStrings.isEmpty()) {

		    List<String> sorted = timeStrings.stream().//
			    distinct(). //
			    sorted().//
			    collect(Collectors.toList());

		    beginPosition = sorted.get(0);
		    endPosition = sorted.get(sorted.size() - 1);
		}
	    }

	    if (checkString(beginPosition) && checkString(endPosition)) {

		TemporalExtent extent = new TemporalExtent();
		extent.setBeginPosition(beginPosition);
		extent.setEndPosition(endPosition);

		if (checkString(timeRes)) {

		    setTimeInterval(extent, timeRes);
		}

		return extent;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    protected List<String> getSupportedCRS(XMLDocumentReader coverageDescription) {

	try {
	    return coverageDescription
		    .evaluateTextContent("/*:CoverageDescription/*:CoverageOffering/*:supportedCRSs/*:requestResponseCRSs/text()");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected ResponsibleParty getContact(XMLDocumentReader capabilities) {

	try {

	    String rpPath = "/*:WCS_Capabilities/*:Service/*:responsibleParty/";
	    String indName = capabilities.evaluateString(rpPath + "*:individualName");
	    String posName = capabilities.evaluateString(rpPath + "*:positionName");
	    String orgName = capabilities.evaluateString(rpPath + "*:organisationName");

	    String addrCity = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:city");
	    String addrCountry = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:country");

	    String addrMail = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:electronicMailAddress");

	    String addrPostalCode = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:postalCode");

	    String adminArea = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:administrativeArea");

	    String delPoint = capabilities.evaluateString(rpPath + "*:contactInfo/*:address/*:deliveryPoint");

	    ResponsibleParty out = new ResponsibleParty();

	    if (checkString(posName)) {
		out.setPositionName(posName);
	    }

	    if (checkString(orgName)) {
		out.setOrganisationName(orgName);
	    }

	    if (checkString(indName)) {
		out.setIndividualName(indName);
	    }

	    Contact contact = new Contact();

	    Address address = new Address();
	    boolean notEmpty = false;

	    if (checkString(adminArea)) {
		notEmpty = true;
		address.setAdministrativeArea(adminArea);
	    }

	    if (checkString(addrCity)) {
		notEmpty = true;
		address.setCity(addrCity);
	    }

	    if (checkString(addrPostalCode)) {
		notEmpty = true;
		address.setPostalCode(addrPostalCode);
	    }

	    if (checkString(delPoint)) {
		notEmpty = true;
		address.addDeliveryPoint(delPoint);
	    }

	    if (checkString(addrMail)) {
		notEmpty = true;
		address.addElectronicMailAddress(addrMail);
	    }

	    if (checkString(addrCountry)) {
		notEmpty = true;
		address.setCountry(addrCountry);
	    }

	    if (notEmpty) {
		contact.setAddress(address);
	    }

	    out.setContactInfo(contact);
	    return out;

	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    protected int getDimensionsCount(XMLDocumentReader coverageDescription) throws XPathExpressionException {

	String dimensions = coverageDescription
		.evaluateString("/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/@dimension");
	return Integer.valueOf(dimensions);
    }

    protected List<Double> getOffsets(XMLDocumentReader coverageDescription) throws XPathExpressionException {

	return coverageDescription.evaluateTextContent(
		"/*:CoverageDescription/*:CoverageOffering/*:domainSet/*:spatialDomain/*:RectifiedGrid/*:offsetVector/text()").//
		stream().//
		flatMap(s -> Arrays.asList(s.split(" ")).stream()).//
		map(o -> Double.valueOf(o)).//
		collect(Collectors.toList());
    }

    @Override
    protected CoverageDescription getCoverageDescription(XMLDocumentReader coverageDescription) {

	return null;
    }

    @Override
    protected String getWCSProtocol() {

	return NetProtocols.WCS_1_0_0.getCommonURN();
    }

    @Override
    protected String getVersion() {

	return "1.0.0";
    }

    public XMLNodeReader getReducedCapabilities(XMLDocumentReader capabilities, String coverageId) {

	try {
	    Node ret = capabilities
		    .evaluateNode("/*:WCS_Capabilities/*:ContentMetadata/*:CoverageOfferingBrief[*:name='" + coverageId + "']");
	    return new XMLNodeReader(ret);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return WCSConnector.WCS_SCHEME + WCSConnector_100.class.getSimpleName();
    }

}
