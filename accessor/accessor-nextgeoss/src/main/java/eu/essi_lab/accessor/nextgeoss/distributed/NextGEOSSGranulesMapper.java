package eu.essi_lab.accessor.nextgeoss.distributed;

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

import eu.essi_lab.accessor.nextgeoss.harvested.NextGEOSSCollectionMapper;
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
 * @author roncella
 */
public class NextGEOSSGranulesMapper extends OriginalIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(NextGEOSSGranulesMapper.class);
    private static final String CANT_READ_GRANULE = "Exception reading original atom granule";
    private static final String NEXTGEOSS_MAPPER_ORIGINAL_MD_READ_ERROR = "NEXTGEOSS_MAPPER_ORIGINAL_MD_READ_ERROR";
    private static final String IDENTIFIER_XPATH = "//*:id";
    private static final String TITLE_XPATH = "//*:title";
    private static final String REVISION_XPATH = "//*:updated";
    private static final String ABSTRACT_XPATH = "//*:summary";
    private static final String THUMBNAIL_XPATH = "//*:link[@rel='icon']/@href";
    private static final String BBOX_XPATH = "//georss:box";
    private static final String BBOX_POLYGON_XPATH = "//georss:polygon";
    private static final String LINKS_XPATH = "//*:link";
    // private static final String DIRECT_DOWNLOAD_XPATH = "//*:link[@rel='enclosure']/@href";
    private static final String TEMPRAL_EXTENT_XPATH = "//*:date";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	try {

	    XMLDocumentReader reader = new XMLDocumentReader(
		    new ByteArrayInputStream(resource.getOriginalMetadata().getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	    return read(reader, IDENTIFIER_XPATH).orElse(null);

	} catch (SAXException | IOException e) {

	    logger.error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	XMLDocumentReader reader;
	try {

	    reader = new XMLDocumentReader(new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8)));

	    reader.setNamespaceContext(new CommonNameSpaceContext());

	} catch (SAXException | IOException e) {

	    logger.error(CANT_READ_GRANULE, e);

	    throw GSException.createException(getClass(), CANT_READ_GRANULE, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, NEXTGEOSS_MAPPER_ORIGINAL_MD_READ_ERROR, e);

	}

	GSResource dataset = new Dataset();

	// id
	// Optional<String> optional = read(reader, IDENTIFIER_XPATH);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = coreMetadata.getMIMetadata();

	miMetadata.setHierarchyLevelName("dataset");

	miMetadata.setLanguage("eng");

	dataset.setSource(source);

	DataIdentification dataIdentification = miMetadata.getDataIdentification();

	Optional<String> bbox = read(reader, BBOX_XPATH);

	Optional<String> bboxPolygon = read(reader, BBOX_POLYGON_XPATH);

	if (bboxPolygon.isPresent() && (!bboxPolygon.get().isEmpty())) {
	    bboxPolygon.ifPresent(b -> {
		// String[] bsplit = b.split(" ");
		try {
		    String res = NextGEOSSCollectionMapper.toBBOX(b, false);
		    double west = Double.valueOf(res.split(" ")[1]);
		    double east = Double.valueOf(res.split(" ")[3]);
		    double north = Double.valueOf(res.split(" ")[2]);
		    double south = Double.valueOf(res.split(" ")[0]);
		    dataIdentification.addGeographicBoundingBox("Granule extent", north, west, south, east);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(NextGEOSSGranulesMapper.class).error("Get Polygon Bbox Error");
		    GSLoggerFactory.getLogger(NextGEOSSGranulesMapper.class).error(e.getMessage(), e);
		}
	    });
	} else {

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
	}

	Optional<String> updated = read(reader, REVISION_XPATH);

	updated.ifPresent(up -> {

	    dataIdentification.addCitationDate(up, REVISION);
	});

	String title = "NextGeoss Granule";

	dataIdentification.setCitationTitle(read(reader, TITLE_XPATH).orElse(title));

	// abstract not readable (it is an html table....)
	Optional<String> abs = read(reader, ABSTRACT_XPATH);

	abs.ifPresent(dataIdentification::setAbstract);

	// Optional<String> download = read(reader, DIRECT_DOWNLOAD_XPATH);
	//
	// download.ifPresent(d -> {
	// Online online = new Online();
	//
	// online.setLinkage(d);
	//
	// // online.setProtocol();
	//
	// online.setName("");
	//
	// miMetadata.getDistribution().addDistributionOnline(online);
	// });

	Optional<Node[]> downloads = readDoc(reader, LINKS_XPATH);

	downloads.ifPresent(d -> {
	    Set<String> urls = new HashSet<String>();
	    for (Node n : d) {
		NamedNodeMap attributes = n.getAttributes();
		String url = attributes.getNamedItem("href").getTextContent();
		String t = "Online resource for Granules " + read(reader, TITLE_XPATH).orElse(dataIdentification.getCitationTitle());
		if (attributes.getNamedItem("title") != null) {
		    t = attributes.getNamedItem("title").getTextContent();
		}
		if (url != null && !url.isEmpty() && !url.contains("example.com")) {
		    if (urls.contains(url))
			continue;
		    urls.add(url);
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setDescription(t);
		    miMetadata.getDistribution().addDistributionOnline(online);

		}

		// possibly other keywords

		// if (t.contains("Product metadata")) {
		// try {
		// String conceptXml = getDownloader().downloadString(url).orElse(null);
		//
		// XMLDocumentReader readerConcept = new XMLDocumentReader(conceptXml);
		//
		// //List<String> keywordsNodes = getKeywordsFromConceptXMLDocument(readerConcept);
		//
		// //keywordsNodes.forEach(key -> keywords.add(key));
		//
		// } catch (Exception e) {
		// GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error("Get Number of Records Error");
		// GSLoggerFactory.getLogger(NextGEOSSCollectionMapper.class).error(e.getMessage(), e);
		// }
		//
		// }

	    }

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

	    try {
		String start = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[0]));

		String end = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(tsplit[1]));

		TemporalExtent tempExtent = new TemporalExtent();

		tempExtent.setBeginPosition(start);

		tempExtent.setEndPosition(end);

		dataIdentification.addTemporalExtent(tempExtent);
	    } catch (Exception e) {
		logger.warn("Exception reading temporal extent ", e);
	    }
	});

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
	return NextGEOSSGranulesMetadataSchemas.ATOM_ENTRY_NEXTGEOSS.toString();
    }

    public static void main(String[] args) {
	String date = "2021-01-09T08:01:24.073000.000Z";
	try {
	    String res = ISO8601DateTimeUtils.getISO8601DateTime(ISO8601DateTimeUtils.parseISO8601(date));
	} catch (Exception e) {
	    // TODO: handle exception
	}
	System.out.println(date);
    }
}
