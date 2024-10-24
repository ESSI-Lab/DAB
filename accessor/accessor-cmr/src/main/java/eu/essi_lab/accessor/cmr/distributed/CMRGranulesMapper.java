package eu.essi_lab.accessor.cmr.distributed;

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

import static eu.essi_lab.iso.datamodel.classes.Identification.REVISION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.mapper.OpensearchCollectionMapper;
import eu.essi_lab.iso.datamodel.classes.BrowseGraphic;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author ilsanto
 */
public class CMRGranulesMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(CMRGranulesMapper.class);
    private static final String CANT_READ_GRANULE = "Exception reading original atom granule";
    private static final String CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR = "CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR";
    private static final String IDENTIFIER_XPATH = "//*:id";
    private static final String TITLE_XPATH = "//*:title";
    private static final String REVISION_XPATH = "//*:updated";
    private static final String ABSTRACT_XPATH = "//*:summary";
    private static final String THUMBNAIL_XPATH = "//*:link[@rel='icon']/@href";
    private static final String BBOX_XPATH = "//georss:box";
    private static final String BBOX_POINT_XPATH = "//georss:point";
    private static final String BBOX_POLYGON_XPATH = "//georss:polygon";
    private static final String DIRECT_DOWNLOAD_XPATH = "//*:link[@rel='enclosure']/@href";
    private static final String DIRECT_DOWNLOAD_XPATH_TITLE = "//*:link[@rel='enclosure']/@title";
    private static final String TEMPRAL_EXTENT_XPATH = "//*:date";
    private static final String LINKS_XPATH = "//*:link";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(
		    new ByteArrayInputStream(resource.getOriginalMetadata().getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    Optional<String> optional = read(reader, IDENTIFIER_XPATH);

	    return optional.orElse(null);

	} catch (SAXException | IOException e) {

	    return null;
	}
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	XMLDocumentReader reader;
	try {

	    reader = new XMLDocumentReader(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	} catch (SAXException | IOException e) {

	    logger.error(CANT_READ_GRANULE, e);

	    throw GSException.createException(getClass(), CANT_READ_GRANULE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, CMRGRANULES_MAPPER_ORIGINAL_MD_READ_ERROR, e);

	}

	GSResource dataset = new Dataset();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	Optional<String> bbox = read(reader, BBOX_XPATH);

	Optional<String> bboxPoint = read(reader, BBOX_POINT_XPATH);

	if (bbox.isPresent() && (!bbox.get().isEmpty())) {

	    bbox.ifPresent(b -> {

		String[] bsplit = b.split(" ");

		if (bsplit.length != 4) {

		    logger.warn("Found unrecognized bbox {}", b);
		    return;
		}

		/**
		 * <georss:box>-90 -180 90 180</georss:box>
		 */

		dataIdentification.addGeographicBoundingBox("Granule extent", Double.valueOf(bsplit[2]), Double.valueOf(bsplit[1]),
			Double.valueOf(bsplit[0]), Double.valueOf(bsplit[3]));
	    });
	} else if (bboxPoint.isPresent() && (!bboxPoint.get().isEmpty())) {

	    bboxPoint.ifPresent(b -> {

		String[] bsplit = b.split(" ");

		if (bsplit.length != 2) {

		    logger.warn("Found unrecognized bbox {}", b);
		    return;
		}

		/**
		 * <georss:box>-90 -180 90 180</georss:box>
		 */

		dataIdentification.addGeographicBoundingBox("Collection extent", Double.valueOf(bsplit[0]), Double.valueOf(bsplit[1]),
			Double.valueOf(bsplit[0]), Double.valueOf(bsplit[1]));
	    });
	} else {
	    Optional<String> bboxPolygon = read(reader, BBOX_POLYGON_XPATH);
	    bboxPolygon.ifPresent(b -> {
		// String[] bsplit = b.split(" ");
		try {
		    String res = OpensearchCollectionMapper.toBBOX(b, true);
		    double west = Double.valueOf(res.split(" ")[1]);
		    double east = Double.valueOf(res.split(" ")[3]);
		    double north = Double.valueOf(res.split(" ")[2]);
		    double south = Double.valueOf(res.split(" ")[0]);
		    dataIdentification.addGeographicBoundingBox("Collection extent", north, west, south, east);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(CMRGranulesMapper.class).error("Get Number of Records Error: {}", e.getMessage());
		}
	    });
	}

	Optional<String> updated = read(reader, REVISION_XPATH);

	updated.ifPresent(up -> {
	    String time = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(up));
	    dataIdentification.addCitationDate(time, REVISION);
	});

	String title = "Ceos Granules";

	Optional<String> granulesTitle = read(reader, TITLE_XPATH);

	if (granulesTitle.isPresent() && !granulesTitle.get().isEmpty()) {
	    dataIdentification.setCitationTitle(granulesTitle.get());
	} else {
	    dataIdentification.setCitationTitle(read(reader, IDENTIFIER_XPATH).orElse(title));
	}

	Optional<String> abs = read(reader, ABSTRACT_XPATH);

	if (abs.isPresent() && !abs.get().isEmpty()) {
	    abs.ifPresent(dataIdentification::setAbstract);
	} else {
	    dataIdentification.setAbstract("Granule metadata for " + dataIdentification.getCitationTitle());
	}
	/**
	 * Optional<String> download = read(reader, DIRECT_DOWNLOAD_XPATH);
	 * download.ifPresent(d -> {
	 * Online online = new Online();
	 * online.setLinkage(d);
	 * // online.setProtocol();
	 * online.setName("");
	 * miMetadata.getDistribution().addDistributionOnline(online);
	 * });
	 */

	Optional<Node[]> downloads = readDoc(reader, LINKS_XPATH);

	downloads.ifPresent(d -> {
	    Set<String> urls = new HashSet<String>();
	    for (Node n : d) {
		NamedNodeMap attributes = n.getAttributes();
		String url = attributes.getNamedItem("href").getTextContent();
		String t = "";
		if (attributes.getNamedItem("title") != null) {
		    t = attributes.getNamedItem("title").getTextContent();
		} else {
		    t = "Online resource for Granules " + dataIdentification.getCitationTitle();
		}

		if (url != null && !url.isEmpty()) {
		    if (urls.contains(url))
			continue;
		    urls.add(url);
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setDescription(t);
		    miMetadata.getDistribution().addDistributionOnline(online);

		}

	    }
	    // Online online = new Online();
	    // online.setLinkage(d);
	    // miMetadata.getDistribution().addDistributionOnline(online);
	});

	Optional<String> preview = read(reader, THUMBNAIL_XPATH);

	preview.ifPresent(p -> dataIdentification.addGraphicOverview(createGraphicOverview(p)));

	Optional<String> temporalExtent = read(reader, TEMPRAL_EXTENT_XPATH);

	temporalExtent.ifPresent(t -> {

	    String[] tsplit = t.split("/");

	    if (tsplit.length != 2) {

		logger.warn("Found unrecognized time extent {}", t);
		return;
	    }

	    String start = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[0]));

	    String end = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[1]));

	    TemporalExtent tempExtent = new TemporalExtent();

	    tempExtent.setBeginPosition(start);

	    tempExtent.setEndPosition(end);

	    dataIdentification.addTemporalExtent(tempExtent);
	});

	//

	return dataset;
    }

    private Optional<String> read(XMLDocumentReader reader, String xpath) {

	try {

	    return Optional.ofNullable(reader.evaluateString(xpath));

	} catch (XPathExpressionException e) {
	    logger.warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();

    }

    private Optional<Node[]> readDoc(XMLDocumentReader reader, String xpath) {

	try {

	    return Optional.ofNullable(reader.evaluateNodes(xpath));

	} catch (XPathExpressionException e) {
	    logger.warn("Can't evalueate xpath {}", xpath, e);
	}

	return Optional.empty();

    }

    private BrowseGraphic createGraphicOverview(String url) {

	BrowseGraphic graphic = new BrowseGraphic();

	graphic.setFileName(url);
	graphic.setFileType("image/png");

	return graphic;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CMRGranulesMetadataSchemas.ATOM_ENTRY.toString();
    }

    public static void main(String[] args) throws Exception {
	// String coordinates = "10.439792 -84.062294 10.674375 -84.344933 10.660347 -84.538956 10.342014 -84.15493
	// 10.439792 -84.062294";
	String coordinates = "33.32 -116.669998 37.18 -116.669998 37.18 -118.879997 33.32 -118.879997 33.32 -116.669998";
	String res = OpensearchCollectionMapper.toBBOX(coordinates, true);
	System.out.println(res);

    }

}
