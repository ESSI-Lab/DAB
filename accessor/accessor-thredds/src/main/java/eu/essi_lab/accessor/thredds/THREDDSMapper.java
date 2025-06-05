package eu.essi_lab.accessor.thredds;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.DataQuality;
import eu.essi_lab.iso.datamodel.classes.Dimension;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class THREDDSMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    private static final String WEBSERVICE_ENERGY_THREDDS_BASEURL = "http://tds.webservice-energy.org";

    public enum THREDDS_SERVICE_TYPE {
	WMS, WCS, SOS, OPENDAP, NCSS, ISO, NCML, UDDC, HTTPServer

    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.THREDDS_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	try {

	    XMLDocumentReader xdoc = new XMLDocumentReader(originalMetadata);

	    // timecoverage
	    String startDate = xdoc.evaluateString("//*:timeCoverage/*:start");
	    String endDate = xdoc.evaluateString("//*:timeCoverage/*:end");
	    String resolutionTime = xdoc.evaluateString("//*:timeCoverage/*:resolution");

	    // geospatialcoverage
	    String northSouth = xdoc.evaluateString("//*:geospatialCoverage/*:northsouth/*:start");

	    String northSouthSize = xdoc.evaluateString("//*:geospatialCoverage/*:northsouth/*:size");

	    String northSouthResolution = xdoc.evaluateString("//*:geospatialCoverage/*:northsouth/*:resolution");

	    String northSouthUOM = xdoc.evaluateString("//*:geospatialCoverage/*:northsouth/*:units");

	    String eastWest = xdoc.evaluateString("//*:geospatialCoverage/*:eastwest/*:start");

	    String eastWestSize = xdoc.evaluateString("//*:geospatialCoverage/*:eastwest/*:size");

	    String eastWestResolution = xdoc.evaluateString("//*:geospatialCoverage/*:eastwest/*:resolution");

	    String eastWestUOM = xdoc.evaluateString("//*:geospatialCoverage/*:eastwest/*:units");

	    String verticalStart = xdoc.evaluateString("//*:geospatialCoverage/*:updown/*:start");

	    String verticalSize = xdoc.evaluateString("//*:geospatialCoverage/*:updown/*:size");

	    String verticalUnits = xdoc.evaluateString("//*:geospatialCoverage/*:updown/*:units");

	    // dataset
	    String dataSize = xdoc.evaluateString("//*:dataset/*:dataset/*:dataSize");
	    String dateStamp = xdoc.evaluateString("//*:dataset/*:dataset/*:date");
	    String datasetName = xdoc.evaluateString("//*:dataset/*:dataset/@name");
	    String datasetURLPath = xdoc.evaluateString("//*:dataset/*:dataset/@urlPath");

	    // publisher
	    Node[] publishers = xdoc.evaluateNodes("//*:publisher");
	    // String publisherEmail = xdoc.evaluateString("//*:publisher/*:contact/@email");
	    // String publisherURL = xdoc.evaluateString("//*:publisher/*:contact/url");

	    // keyword
	    List<String> keywords = xdoc.evaluateTextContent("//*:keyword/text()");

	    // creators
	    Node[] creators = xdoc.evaluateNodes("//*:creator");

	    // contributors
	    Node[] contributors = xdoc.evaluateNodes("//*:contributor");

	    // documentations
	    List<String> abstrakts = xdoc.evaluateTextContent("//*:documentation[@type='summary']/text()");
	    String history = xdoc.evaluateString("//*:documentation[@type='history']");
	    String rights = xdoc.evaluateString("//*:documentation[@type='rights']");
	    String references = xdoc.evaluateString("//*:documentation[@type='references']");
	    Node[] hrefs = xdoc.evaluateNodes("//*:documentation[@*[name()='xlink:href']]");

	    // service
	    Node[] serviceNodes = xdoc.evaluateNodes("//*:service/*:service");

	    // dataFormat
	    String dataFormat = xdoc.evaluateString("//*:dataFormat");

	    // dataType
	    String dataType = xdoc.evaluateString("//*:dataType");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // title
	    if (datasetName != null && !datasetName.isEmpty()) {
		// String title = "THREDDS Dataset: ";
		coreMetadata.setTitle(datasetName);
	    }
	    // abstract
	    if (abstrakts != null && !abstrakts.isEmpty()) {
		String description = "";
		for (String s : abstrakts) {
		    description += s + "\n";
		}
		coreMetadata.setAbstract(description);
	    }

	    for (String s : keywords) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	    }
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("THREDDS");

	    // legal constraints
	    if (rights != null && !rights.isEmpty()) {
		LegalConstraints access = new LegalConstraints();
		access.addUseConstraintsCode("otherRestrictions");
		access.addOtherConstraints(rights);
		// access.addUseLimitation(rights);
		// access.addOtherConstraints("le");
		coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(access);
	    }

	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    List<Dimension> dimensions = new ArrayList<Dimension>();
	    grid.setNumberOfDimensions(4);
	    if (northSouth != null && !northSouth.isEmpty()) {
		Dimension dimension = new Dimension();
		dimension.setDimensionNameTypeCode("row");
		if (northSouthResolution != null && !northSouthResolution.isEmpty() && isDouble(northSouthResolution)) {
		    dimension.setResolution(northSouthUOM, Double.valueOf(northSouthResolution));
		}
		dimensions.add(dimension);
	    }
	    if (eastWest != null && !eastWest.isEmpty()) {
		Dimension dimension = new Dimension();
		dimension.setDimensionNameTypeCode("column");
		if (eastWestResolution != null && !eastWestResolution.isEmpty() && isDouble(eastWestResolution)) {
		    dimension.setResolution(eastWestUOM, Double.valueOf(eastWestResolution));
		}
		dimensions.add(dimension);
	    }
	    if (verticalStart != null && !verticalStart.isEmpty()) {
		Dimension dimension = new Dimension();
		dimension.setDimensionNameTypeCode("vertical");
		if (verticalStart != null && !verticalStart.isEmpty() && isDouble(verticalStart)) {
		    dimension.setResolution(verticalUnits, Double.valueOf(verticalStart));
		}
		dimensions.add(dimension);
	    }
	    if (startDate != null && !startDate.isEmpty()) {
		Dimension dimension = new Dimension();
		dimension.setDimensionNameTypeCode("time");
		if (resolutionTime != null && !resolutionTime.isEmpty()) {
		    String[] splittedResolutionTime = resolutionTime.split(" ");
		    if (splittedResolutionTime.length > 0) {
			if (splittedResolutionTime[0] != null && !splittedResolutionTime[0].isEmpty()
				&& isDouble(splittedResolutionTime[0])) {
			    dimension.setResolution(splittedResolutionTime[1], Double.valueOf(splittedResolutionTime[0]));
			}
		    }
		}
		dimensions.add(dimension);
	    }

	    if (dimensions.size() > 0) {
		grid.setNumberOfDimensions(dimensions.size());
		for (Dimension d : dimensions) {
		    grid.addAxisDimension(d);
		}
		coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
	    }
	    // Dimension dimension = new Dimension(); //lon lat time elevation
	    // dimension.
	    // double resolution // xpath;
	    // dimension.setResolution(resolution // xpath);
	    // String uom = //path;
	    // dimension.setResolution(uom , value);
	    // grid.addAxisDimension(dimension);
	    // dimension.setDimensionNameTypeCode("time"); // row(northsouth) column(eastwest) vertical time
	    // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid );

	    // online
	    if (serviceNodes.length > 0) {

		String endpoint = dataset.getSource().getEndpoint();
		String baseThreddsEndpoint = null;
		// classic use case (harvesting from THREDDS catalogue
		if (endpoint.contains("/thredds")) {
		    String[] splittedEndpoint = endpoint.split("/thredds");
		    baseThreddsEndpoint = splittedEndpoint[0];
		    // other possible cases: we harvest from other catalogues but we would like to add datasets from an
		    // External THREDDS catalogue
		    // (e.g. Webservice Energy Catalogue)
		    // currently we have only the case of WebService Energy catalog
		} else if (endpoint.contains("geocatalog.webservice-energy.org")) {

		    baseThreddsEndpoint = WEBSERVICE_ENERGY_THREDDS_BASEURL;
		}

		for (Node n : serviceNodes) {
		    String serviceName = n.getAttributes().getNamedItem("name").getNodeValue();
		    String serviceType = n.getAttributes().getNamedItem("serviceType").getNodeValue();
		    String relativeURL = n.getAttributes().getNamedItem("base").getNodeValue();
		    datasetURLPath = datasetURLPath.endsWith("/") ? datasetURLPath.substring(0, datasetURLPath.length() - 1)
			    : datasetURLPath;
		    List<Online> onlines = createOnline(baseThreddsEndpoint, serviceName, serviceType, relativeURL, datasetURLPath);

		    for (Online o : onlines) {
			coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);
		    }
		}

	    }

	    for (Node n : hrefs) {
		String href = n.getAttributes().getNamedItem("xlink:href").getNodeValue();
		Node nodeTit = n.getAttributes().getNamedItem("xlink:title");
		String hrefTitle = (nodeTit != null) ? nodeTit.getNodeValue() : href;
		
		Online online = new Online();
		online.setLinkage(href);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setFunctionCode("information");
		online.setDescription(hrefTitle);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }

	    // dataSize
	    if (dataSize != null && !dataSize.isEmpty()) {
		Iterator<TransferOptions> transferOptions = coreMetadata.getMIMetadata().getDistribution().getDistributionTransferOptions();
		while (transferOptions.hasNext()) {
		    transferOptions.next().setTransferSize(Double.valueOf(dataSize));
		}
	    }

	    // dataFormat
	    if (dataFormat != null && !dataFormat.isEmpty()) {
		coreMetadata.addDistributionFormat(dataFormat);
	    }

	    // bbox
	    // 0-360
	    // start+size > 180 -> mod(180) è quanto rimane fuori e a quello ci tolgo -180 solo su east
	    // e.g. strat+size = 200 mod(180)= 20 east= -180 + 20
	    // resolution se negativa va invertito
	    if ((northSouth != null && !northSouth.isEmpty()) && (eastWest != null && !eastWest.isEmpty())
		    && (northSouthSize != null && !northSouthSize.isEmpty()) && (eastWestSize != null && !eastWestSize.isEmpty())) {
		boolean inverseNorthSouth = false;
		boolean inverseEastWest = false;
		if (northSouthResolution != null && !northSouthResolution.isEmpty() && isDouble(northSouthResolution)) {
		    if (Double.valueOf(northSouthResolution) < 0) {
			inverseNorthSouth = true;
		    }
		}
		if (eastWestResolution != null && !eastWestResolution.isEmpty() && isDouble(eastWestResolution)) {
		    if (Double.valueOf(eastWestResolution) < 0) {
			inverseEastWest = true;
		    }
		}
		double w;
		double e;
		double s;
		double n;

		if (!inverseNorthSouth) {
		    s = Double.valueOf(northSouth);
		    n = Double.valueOf(northSouthSize) + s;
		} else {
		    n = Double.valueOf(northSouth);
		    s = Double.valueOf(northSouthSize) + n;
		}
		if (!inverseEastWest) {
		    w = Double.valueOf(eastWest);
		    e = Double.valueOf(eastWestSize) + w;
		    if (e > 180) {
			double mod = e % 180;
			e = -180 + mod;
		    }
		} else {
		    e = Double.valueOf(eastWest);
		    w = Double.valueOf(eastWestSize) + e;
		    if (w > 180) {
			double mod = w % 180;
			w = -180 + mod;
		    }
		}

		dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);

	    }

	    // vertical extent
	    if (verticalStart != null && !verticalStart.isEmpty()) {
		VerticalExtent verticalExtent = new VerticalExtent();
		double elevationMin = Double.valueOf(verticalStart);
		verticalExtent.setMinimumValue(elevationMin);
		if (verticalSize != null && !verticalSize.isEmpty()) {
		    double elevationMax = Double.valueOf(verticalSize) + elevationMin;
		    verticalExtent.setMaximumValue(elevationMax);
		} else {
		    verticalExtent.setMaximumValue(elevationMin);
		}
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    }

	    // datestamp
	    if (dateStamp != null && !dateStamp.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(dateStamp);
		coreMetadata.getMIMetadata().setDateStampAsDate(dateStamp);
	    }

	    // temporal extent
	    TemporalExtent tempExtent = new TemporalExtent();

	    if (startDate != null && !startDate.isEmpty() && !startDate.contains("unknown")) {
		tempExtent.setBeginPosition(startDate);
		// coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	    }
	    if (endDate != null && !endDate.isEmpty() && !endDate.contains("unknown")) {
		tempExtent.setEndPosition(endDate);
		if (coreMetadata.getMIMetadata().getDataIdentification().getCitationRevisionDate() == null
			|| coreMetadata.getMIMetadata().getDataIdentification().getCitationRevisionDate().isEmpty()) {
		    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(endDate);
		    coreMetadata.getMIMetadata().setDateStampAsDate(endDate);
		}
		// coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	    }

	    // if (tempExtent.getBeginPosition() != null && tempExtent.getEndPosition() == null) {
	    // TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	    // tempExtent.setIndeterminateEndPosition(endTimeInderminate);
	    // }
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	    // organization
	    for (Node n : creators) {
		ResponsibleParty creatorContact = new ResponsibleParty();
		// String orgName = xdoc.evaluateString(n, "*:organizationName");
		// String ct = xdoc.evaluateString(n, "*:address/*:country");
		String individualName = xdoc.evaluateString(n, "*:name");
		String mail = xdoc.evaluateString(n, "*:contact/@email");
		String url = xdoc.evaluateString(n, "*:contact/@url");

		Contact contactInfo = new Contact();
		Address address = new Address();
		if (mail != null && !mail.isEmpty()) {
		    address.addElectronicMailAddress(mail);
		}
		// if (ct != null && !ct.isEmpty()) {
		// address.setCountry(ct);
		// }
		contactInfo.setAddress(address);
		if (individualName != null && !individualName.isEmpty()) {
		    creatorContact.setIndividualName(individualName);
		}

		// if (orgName != null && !orgName.isEmpty()) {
		// creatorContact.setOrganisationName(orgName);
		// }

		if (url != null && !url.isEmpty()) {
		    Online online = new Online();
		    online.setLinkage(url);
		    contactInfo.setOnline(online);
		}

		creatorContact.setContactInfo(contactInfo);
		creatorContact.setRoleCode("author");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
		coreMetadata.getMIMetadata().addContact(creatorContact);
	    }

	    for (Node n : contributors) {
		ResponsibleParty contributorContact = new ResponsibleParty();
		String role = "contributor";
		// String orgName = xdoc.evaluateString(n, "*:organizationName");
		// String ct = xdoc.evaluateString(n, "*:address/*:country");
		String rolename = xdoc.evaluateString(n, "@role");
		if (rolename != null && !rolename.isEmpty() && rolename.toLowerCase().contains("data manager")) {
		    role = "resourceProvider";
		}
		if (rolename != null && !rolename.isEmpty() && rolename.toLowerCase().contains("principal investigator")) {
		    role = "principalInvestigator";
		}

		String contributorName = xdoc.evaluateString(n, "text()");

		Contact contactInfo = new Contact();

		// if (ct != null && !ct.isEmpty()) {
		// address.setCountry(ct);
		// }
		if (contributorName != null && !contributorName.isEmpty()) {
		    contributorContact.setIndividualName(contributorName);
		}

		contributorContact.setContactInfo(contactInfo);
		contributorContact.setRoleCode(role);
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(contributorContact);
		coreMetadata.getMIMetadata().addContact(contributorContact);
	    }

	    // lineage
	    if (history != null && !history.isEmpty()) {
		DataQuality dataQuality = new DataQuality();
		dataQuality.setLineageStatement(history);
		coreMetadata.getMIMetadata().addDataQuality(dataQuality);
	    }

	    // supplementalInformation
	    if (references != null && !references.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().setSupplementalInformation(references);
	    }

	    for (Node n : publishers) {
		ResponsibleParty pointOfContact = new ResponsibleParty();
		// String orgName = xdoc.evaluateString(n, "*:organizationName");
		// String ct = xdoc.evaluateString(n, "*:address/*:country");
		String orgName = xdoc.evaluateString(n, "*:name");
		String mail = xdoc.evaluateString(n, "*:contact/@email");
		String url = xdoc.evaluateString(n, "*:contact/@url");

		Contact contactInfo = new Contact();
		Address address = new Address();
		if (mail != null && !mail.isEmpty()) {
		    address.addElectronicMailAddress(mail);
		}
		// if (ct != null && !ct.isEmpty()) {
		// address.setCountry(ct);
		// }
		contactInfo.setAddress(address);
		// if (individualName != null && !individualName.isEmpty()) {
		// creatorContact.setIndividualName(individualName);
		// }

		if (orgName != null && !orgName.isEmpty()) {
		    pointOfContact.setOrganisationName(orgName);
		}

		if (url != null && !url.isEmpty()) {
		    Online online = new Online();
		    online.setLinkage(url);
		    contactInfo.setOnline(online);
		}

		pointOfContact.setContactInfo(contactInfo);
		pointOfContact.setRoleCode("pointOfContact");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(pointOfContact);
	    }

	    logger.info("THREDDS Mappper ENDED");

	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (XPathExpressionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private List<Online> createOnline(String baseURL, String serviceName, String serviceType, String relativeURL, String datasetURLPath) {

	List<Online> onlines = new ArrayList<Online>();
	String linkage = null;
	Downloader downloader = new Downloader();
	try {
	    // wms case
	    if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.WMS.toString())) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath + "?" : linkage + "/" + datasetURLPath + "?";
		String wmsString = linkage + "service=WMS&version=1.3.0&request=GetCapabilities";
		downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
		String wmsCapabilities = downloader.downloadOptionalString(wmsString).get();

		XMLDocumentReader capabilitiesDoc = new XMLDocumentReader(wmsCapabilities);
		Node[] layerNodes = capabilitiesDoc.evaluateNodes("//*:Layer/*:Name");
		for (Node n : layerNodes) {
		    String layerName = n.getTextContent();
		    Online wmsOnline = new Online();
		    wmsOnline.setLinkage(linkage);
		    wmsOnline.setProtocol(NetProtocols.WMS_1_3_0.getCommonURN());
		    wmsOnline.setName(layerName);
		    wmsOnline.setDescription(layerName);// "OGC:WMS"
		    wmsOnline.setFunctionCode("download");
		    onlines.add(wmsOnline);
		}
		if (onlines.isEmpty()) {
		    Online onlineCap = new Online();
		    onlineCap.setLinkage(wmsString);
		    onlineCap.setProtocol("WWW:LINK-1.0-http–link");// "OGC:WMS"
		    // online.setFunctionCode("download");
		    onlineCap.setDescription("WMS Capabilities");
		    onlines.add(onlineCap);
		}

		// wcs case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.WCS.toString())) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath + "?" : linkage + "/" + datasetURLPath + "?";
		String wcsString = linkage + "service=WCS&version=1.0.0&request=GetCapabilities";
		downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);
		String wcsCapabilities = downloader.downloadOptionalString(wcsString).get();

		XMLDocumentReader capabilitiesDoc = new XMLDocumentReader(wcsCapabilities);
		Node[] layerNodes = capabilitiesDoc.evaluateNodes("//*:CoverageOfferingBrief/*:name");
		for (Node n : layerNodes) {
		    String coverageName = n.getTextContent();
		    Online wcsOnline = new Online();
		    wcsOnline.setLinkage(linkage);
		    wcsOnline.setProtocol(NetProtocols.WCS_1_0_0.getCommonURN());
		    wcsOnline.setName(coverageName);
		    wcsOnline.setDescription(coverageName);// "OGC:WMS"
		    wcsOnline.setFunctionCode("download");
		    onlines.add(wcsOnline);
		}
		if (onlines.isEmpty()) {
		    Online onlineCap = new Online();
		    onlineCap.setLinkage(wcsString);
		    onlineCap.setProtocol("WWW:LINK-1.0-http–link");// "OGC:WMS"
		    // online.setFunctionCode("download");
		    onlineCap.setDescription("WCS Capabilities");
		    onlines.add(onlineCap);
		}
		// sos case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.SOS.toString())) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath + "?SERVICE=SOS&ACCEPTVERSIONS=1.0.0&REQUEST=GetCapabilities"
			: linkage + "/" + datasetURLPath + "?SERVICE=SOS&ACCEPTVERSIONS=1.0.0&REQUEST=GetCapabilities";
		Online online = new Online();
		online.setLinkage(linkage);
		online.setProtocol("WWW:LINK-1.0-http–link");// "OGC:SOS"
		// online.setFunctionCode("download");
		online.setDescription("SOS Capabilities");
		onlines.add(online);
		// opendap case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.OPENDAP.toString())) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath + ".html" : linkage + "/" + datasetURLPath + ".html";
		Online online = new Online();
		online.setLinkage(linkage);
		online.setProtocol("WWW:LINK-1.0-http–opendap");
		// online.setFunctionCode("download");
		online.setDescription(serviceName);
		onlines.add(online);
		// netcdfsubset case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.NCSS.toString()) || serviceType.equalsIgnoreCase("netcdfsubset")) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath + "/dataset.html"
			: linkage + "/" + datasetURLPath + "/dataset.html";
		Online online = new Online();
		online.setLinkage(linkage);
		online.setProtocol(serviceType);
		// online.setFunctionCode("download");
		online.setDescription(serviceName);
		onlines.add(online);
		// dowload case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.HTTPServer.toString())
		    || serviceType.toLowerCase().contains("download")) {
		linkage = relativeURL.startsWith("/") ? baseURL + relativeURL : baseURL + "/" + relativeURL;
		linkage = linkage.endsWith("/") ? linkage + datasetURLPath : linkage + "/" + datasetURLPath;
		Online online = new Online();
		online.setLinkage(linkage);
		online.setProtocol("WWW:DOWNLOAD-1.0-http–download");
		online.setFunctionCode("download");
		onlines.add(online);
		// online.setDescription(serviceName);
		// ncml case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.NCML.toString())) {
		// UDDC case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.UDDC.toString())) {
		// iso case
	    } else if (serviceType.equalsIgnoreCase(THREDDS_SERVICE_TYPE.ISO.toString())) {

	    }

	    return onlines;
	} catch (SAXException | IOException | XPathExpressionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return onlines;
    }

    private boolean isDouble(String str) {
	try {
	    // check if it can be parsed as any double
	    Double.parseDouble(str);
	    return true;
	    // short version: return x != (int) x;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

}
