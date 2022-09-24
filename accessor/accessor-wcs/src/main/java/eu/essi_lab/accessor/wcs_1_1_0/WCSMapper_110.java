/**
 * 
 */
package eu.essi_lab.accessor.wcs_1_1_0;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs.WCSMapper;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;

/**
 * @author Fabrizio
 */
public class WCSMapper_110 extends WCSMapper {

    @Override
    protected String getIdentifier(XMLDocumentReader coverageDescription) {

	String out = null;
	try {
	    out = coverageDescription.evaluateString("/*:CoverageDescription/*:Identifier");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<String> getKeywords(XMLDocumentReader reader) {

	List<String> out = null;
	try {
	    out = reader.evaluateTextContent("/*:Capabilities/*:ServiceIdentification/*:Keywords/*:Keyword/text()");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<String> getFormats(XMLDocumentReader description) {

	List<String> out = null;
	try {
	    out = description.evaluateTextContent("/*:CoverageDescription/*:SupportedFormat/text()");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected String getTitle(XMLDocumentReader description) {

	String out = null;
	try {
	    out = description.evaluateString("/*:CoverageDescription/*:Title");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected String getAbstract(XMLDocumentReader reader) {

	String out = null;
	try {
	    out = reader.evaluateString("/*:CoverageDescription/*:Abstract");
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return out;
    }

    @Override
    protected List<Double> getBBoxFromCapabilities(XMLNodeReader coverageOffering) {

	try {
	    String lower = coverageOffering.evaluateString("*:WGS84BoundingBox/*:LowerCorner");
	    String upper = coverageOffering.evaluateString("*:WGS84BoundingBox/*:UpperCorner");

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
    protected List<Double> getBboxFromDescription(XMLDocumentReader coverageDescription) {

	try {
	    Node[] bboxes = coverageDescription.evaluateNodes("/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox");

	    for (int i = 0; i < bboxes.length; i++) {

		Node bbox = bboxes[i];
		String crs = coverageDescription.evaluateString(bbox, "@crs");
		if (crs != null) {
		    crs = crs.toLowerCase();

		    String lower = coverageDescription.evaluateTextContent("*:LowerCorner/text()").get(0);
		    String upper = coverageDescription.evaluateTextContent("*:UpperCorner/text()").get(1);

		    Double west, south, east, north;

		    // lat/lon
		    if (crs.contains("wgs84") || crs.contains("4326")) {

			west = Double.valueOf(lower.split(" ")[1]);
			south = Double.valueOf(lower.split(" ")[0]);
			east = Double.valueOf(upper.split(" ")[1]);
			north = Double.valueOf(upper.split(" ")[0]);

		    } else { // if (crs.contains("crs84")) {

			west = Double.valueOf(lower.split(" ")[0]);
			south = Double.valueOf(lower.split(" ")[1]);
			east = Double.valueOf(upper.split(" ")[0]);
			north = Double.valueOf(upper.split(" ")[1]);
		    }

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
	    String crs = coverageDescription.evaluateString("/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridBaseCRS");

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
	    String gridBaseCRS = getGridBoudingBoxCRS(coverageDescription);

	    if (!checkString(gridBaseCRS)) {
		return new ArrayList<>();
	    }

	    Node[] bboxes = coverageDescription.evaluateNodes("/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox");

	    for (int i = 0; i < bboxes.length; i++) {

		Node bbox = bboxes[i];
		String currenctCrs = coverageDescription.evaluateString(bbox, "@crs");

		if (checkString(currenctCrs)) {

		    if (gridBaseCRS.equals(currenctCrs)) {

			String lower = coverageDescription.evaluateString(bbox, "*:LowerCorner");
			String upper = coverageDescription.evaluateString(bbox, "*:UpperCorner");

			Double miny = Double.valueOf(lower.split(" ")[0]);
			Double minx = Double.valueOf(lower.split(" ")[1]);
			Double maxy = Double.valueOf(upper.split(" ")[0]);
			Double maxx = Double.valueOf(upper.split(" ")[1]);

			return Arrays.asList(miny, minx, maxy, maxx);
		    }
		}
	    }
	} catch (XPathExpressionException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected TemporalExtent getTimeExtent(XMLDocumentReader coverageDescription) {

	try {
	    String beginPosition = coverageDescription
		    .evaluateString("/*:CoverageDescription/*:Domain/*:TemporalDomain/*:TimePeriod/*:BeginPosition");
	    String endPosition = coverageDescription
		    .evaluateString("/*:CoverageDescription/*:Domain/*:TemporalDomain/*:TimePeriod/*:EndPosition");

	    if (checkString(beginPosition) && checkString(endPosition)) {

		TemporalExtent extent = new TemporalExtent();
		extent.setBeginPosition(beginPosition);
		extent.setEndPosition(endPosition);

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
	    return coverageDescription.evaluateTextContent("/*:CoverageDescription/*:SupportedCRS");
	} catch (XPathExpressionException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    @Override
    protected ResponsibleParty getContact(XMLDocumentReader capabilities) {

	try {
	    String provName = capabilities.evaluateString("/*:Capabilities/*:ServiceProvider/*:ProviderName");
	    String provSite = capabilities.evaluateString("/*:Capabilities/*:ServiceProvider/*:ProviderSite/@*:href");

	    String indName = capabilities.evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:IndividualName");
	    String posName = capabilities.evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:PositionName");

	    String phoneVoice = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Phone/*:Voice");

	    String addrCity = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:City");
	    String addrCountry = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:Country");
	    String addrMail = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:ElectronicMailAddress");
	    String addrPostalCode = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:PostalCode");
	    String adminArea = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:AdministrativeArea");
	    String delPoint = capabilities
		    .evaluateString("/*:Capabilities/*:ServiceProvider/*:ServiceContact/*:ContactInfo/*:Address/*:DeliveryPoint");

	    ResponsibleParty out = new ResponsibleParty();
	    if (checkString(provName)) {
		out.setOrganisationName(provName);
	    }

	    if (checkString(posName)) {
		out.setPositionName(posName);
	    }

	    if (checkString(indName)) {
		out.setIndividualName(indName);
	    }

	    Contact contact = new Contact();

	    if (checkString(provSite)) {
		Online online = new Online();

		online.setLinkage(provSite);
		online.setDescription("Provider Site");
		online.setName("Provider Site");
		contact.setOnline(online);
	    }

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
		.evaluateString("/*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:BoundingBox/@dimensions");
	return Integer.valueOf(dimensions);
    }

    protected List<Double> getOffsets(XMLDocumentReader coverageDescription) throws XPathExpressionException {

	return Arrays.asList(//
		coverageDescription
			.evaluateString(
				"/*:CoverageDescriptions/*:CoverageDescription/*:Domain/*:SpatialDomain/*:GridCRS/*:GridOffsets/text()")
			.split(" "))
		.//
		stream().//
		map(o -> Double.valueOf(o)).//
		collect(Collectors.toList());
    }

    @Override
    protected CoverageDescription getCoverageDescription(XMLDocumentReader coverageDescription) {

	return null;
    }

    @Override
    protected String getWCSProtocol() {

	return NetProtocols.WCS_1_1.getCommonURN();
    }

    @Override
    protected String getVersion() {

	return "1.1.0";
    }

    public XMLNodeReader getReducedCapabilities(XMLDocumentReader capabilities, String coverageId) {

	try {
	    Node ret = capabilities.evaluateNode("/*:Capabilities/*:Contents/*:CoverageSummary[*:Identifier='" + coverageId + "']");
	    return new XMLNodeReader(ret);
	} catch (XPathExpressionException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return WCSConnector.WCS_SCHEME + WCSConnector_110.class.getSimpleName();
    }

}
