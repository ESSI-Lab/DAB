package eu.essi_lab.accessor.fdsn;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.Node;

import eu.essi_lab.accessor.fdsn.md.FDSNMetadataSchemas;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.AccessType;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

public class FDSNMapper extends OriginalIdentifierMapper {

    private static final String INVALID_ORIGINAL_METADATA = "INVALID_ORIGINAL_METADATA";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public FDSNMapper() {
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return FDSNMetadataSchemas.QUAKEML.toString();
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {
	try {

	    String metadata = resource.getOriginalMetadata().getMetadata();

	    InputStream is = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));

	    XMLDocumentReader xml = new XMLDocumentReader(is);

	    Node event = xml.evaluateNode("//*:event[1]");
	    String publicId = xml.evaluateString(event, "@publicID");

	    if (publicId.contains("eventid=")) {
		String ret = publicId.substring(publicId.lastIndexOf("eventid=")).replace("eventid=", "");
		if (ret.contains("&")) {
		    ret = ret.substring(0, ret.indexOf("&"));
		}
		return ret;
	    }

	    if (publicId.contains("/event/")) {
		return publicId.substring(publicId.indexOf("/event/") + ("/event/".length()), publicId.length());
	    }

	    return publicId;

	} catch (Throwable e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
	
	return null;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) throws GSException {

	String metadata = originalMD.getMetadata();

	if (metadata.equals("")) {

	    throw GSException.createException(//
		    getClass(), //
		    "Empty original metadata", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_ORIGINAL_METADATA);
	}

	InputStream is = new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8));

	XMLDocumentReader xml;
	try {
	    xml = new XMLDocumentReader(is);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_ORIGINAL_METADATA, //
		    e);
	}

	Node event = null;
	try {
	    event = xml.evaluateNode("//*:event[1]");
	} catch (XPathExpressionException e) {
	    // nothing to do, because it will never be thrown, as the formulated expression is valid
	    logger.error("Error evaluating //*:event[1]", e);
	}

	if (event == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "Event element not found: Invalid QuakeML metadata", //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INVALID_ORIGINAL_METADATA);
	}

	try {
	    String publicId = xml.evaluateString(event, "@publicID");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    MIMetadata miMetadata = coreMetadata.getMIMetadata();

	    miMetadata.setHierarchyLevelName("dataset");

	    miMetadata.setLanguage("eng");

	    String preferredMagnitudeId = xml.evaluateString(event, "*:preferredMagnitudeID");
	    Node magnitude = null;
	    if (preferredMagnitudeId != null && !preferredMagnitudeId.equals("")) {
		magnitude = xml.evaluateNode(event, "*:magnitude[@publicID='" + preferredMagnitudeId + "']");
	    }

	    Node[] magnitudes = xml.evaluateNodes(event, "*:magnitude");

	    for (Node m : magnitudes) {

		String type = xml.evaluateString(m, "*:type");

		if (type.toLowerCase().startsWith("mw")) {
		    magnitude = m;
		    break;
		}

		if (type.toLowerCase().startsWith("ml")) {

		    magnitude = m;
		}
	    }

	    String magType = "unknown";
	    String magValue = "unknown";

	    if (magnitude != null) {
		magType = xml.evaluateString(magnitude, "*:type");
		magValue = xml.evaluateString(magnitude, "*:mag/*:value");

		dataset.getExtensionHandler().setMagnitudeLevel(magValue);
	    }

	    String type = xml.evaluateString(event, "*:type");

	    if (type == null || type.equals("")) {
		type = "Event";
	    }
	    String name = xml.evaluateString(event, "*:description[*:type='region name']/*:text");
	    if (name == null || name.equals("")) {
		String flinnEngdahlRegionName = xml.evaluateString(event,
			"*:description[*:type='FEcode' or *:type='Flinn-Engdahl region']/*:text");
		name = flinnEngdahlRegionName;
	    }

	    String preferredOriginID = xml.evaluateString(event, "*:preferredOriginID");
	    Node origin = null;
	    if (preferredOriginID != null && !preferredOriginID.equals("")) {
		origin = xml.evaluateNode(event, "*:origin[@publicID='" + preferredOriginID + "']");
	    }
	    Double origDepth = null;
	    Double origLon = null;
	    Double origLat = null;

	    DataIdentification dataIdentification = miMetadata.getDataIdentification();

	    Distribution distribution = miMetadata.getDistribution();

	    if (origin != null) {

		origDepth = xml.evaluateNumber(origin, "*:depth/*:value").doubleValue();

		String originalTime = xml.evaluateString(origin, "*:time/*:value");

		try {
		    Date parsedTime = ISO8601DateTimeUtils.parseISO8601(originalTime);
		    String time = ISO8601DateTimeUtils.getISO8601DateTime(parsedTime);

		    dataIdentification.addCitationDate(time, "creation");
		    dataIdentification.addTemporalExtent(UUID.randomUUID().toString(), time, time);
		} catch (IllegalArgumentException e) {
		    System.out.println("Warning, unparsable ISO8601 date time in FDSNMapper: " + originalTime);
		}

		origLon = xml.evaluateNumber(origin, "*:longitude/*:value").doubleValue();
		origLat = xml.evaluateNumber(origin, "*:latitude/*:value").doubleValue();
		dataIdentification.addGeographicBoundingBox("Event origin", origLat, origLon, origLat, origLon);

		if (origDepth != null) {

		    dataIdentification.addVerticalExtent(origDepth, origDepth);
		}

	    }

	    String title = type + " of magnitude " + magValue + " (magnitude type " + magType + ") localized in " + name + " at lat: "
		    + origLat + "; lon: " + origLon + ";" + (origDepth != null ? (" depth: " + (origDepth / 1000) + " km depth") : "");
	    dataIdentification.setCitationTitle(title);

	    String abs = type + " of magnitude " + magValue + " (magnitude type " + magType + ") localized in " + name + " at lat: "
		    + origLat + "; lon: " + origLon + ";" + (origDepth != null ? (" depth: " + (origDepth / 1000) + " km depth") : "");
	    dataIdentification.setAbstract(abs);

	    dataIdentification.addKeyword("QuakeML");

	    // online to the full quake ML metadata
	    if (publicId != null && !publicId.equals("")) {
		distribution.clearDistributionOnlines();
		Online online = createOnline(publicId);
		distribution.addDistributionOnline(online);
	    }

	    // distribution format
	    Format format = new Format();
	    format.setName("QuakeML");
	    format.setVersion("1.2");
	    distribution.addFormat(format);

	} catch (XPathExpressionException e) {
	    // nothing to do, because it will never be thrown, as the formulated expressions are all valid

	    logger.error("Error evaluating @publicID", e);
	}
    }

    private Online createOnline(String publicId) {

	Online online = new Online();
	online.setLinkage(publicId.replace("smi:", "http://").replace("quakeml:", "http://"));
	online.setProtocol("HTTP-GET");
	online.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

	online.setFunctionCode("information");

	return online;
    }
}
