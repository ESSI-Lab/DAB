package eu.essi_lab.ommdk;

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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Fabrizio
 */
public class DublinCoreResourceMapper extends FileIdentifierMapper {

    private static final String DUBLIN_CORE_MAPPER_MAP_ERROR = "DUBLIN_CORE_MAPPER_MAP_ERROR";

    public DublinCoreResourceMapper() {
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	GSResource dataset = createResource();
	dataset.setSource(source);

	try {

	    mapMetadata(dataset, originalMD);

	} catch (Exception e) {

	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    DUBLIN_CORE_MAPPER_MAP_ERROR, //
		    e);
	}

	return dataset;
    }

    /**
     * @return
     */
    protected GSResource createResource() {

	return new Dataset();
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.CSW_NS_URI;
    }

    private void mapMetadata(GSResource resource, OriginalMetadata originalMD) throws Exception {

	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	MIMetadata miMetadata = new MIMetadata();

	DataIdentification identification = new DataIdentification();
	miMetadata.addDataIdentification(identification);

	Distribution distribution = new Distribution();
	miMetadata.setDistribution(distribution);

	ByteArrayInputStream inputStream = new ByteArrayInputStream(originalMD.getMetadata().getBytes(StandardCharsets.UTF_8));
	XMLDocumentReader reader = new XMLDocumentReader(inputStream);

	// ---------------------------
	//
	// identifier (single)
	//
	List<String> identifiers = getValues(reader, "identifier");
	String sourceEndpoint = resource.getSource().getEndpoint();
	if (sourceEndpoint.contains("eudat-b2share-test.csc.fi") || sourceEndpoint.contains("b2share.eudat.eu/api/oai2d")) {
	    if (!identifiers.isEmpty()) {
		int last = identifiers.size() - 1;
		miMetadata.setFileIdentifier(identifiers.get(last));
	    }
	} else {
	    setValue(identifiers, new Callback() {
		@Override
		public void handleValue(String value) {
		    miMetadata.setFileIdentifier(value);
		}
	    });
	}

	// ---------------------------
	//
	// title (single)
	//
	List<String> titles = getValues(reader, "title");
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
	List<String> descriptions = getValues(reader, "abstract");
	descriptions.addAll(getValues(reader, "description"));

	setValue(descriptions, new Callback() {
	    @Override
	    public void handleValue(String value) {
		identification.setAbstract(value);
	    }
	});

	// ---------------------------
	//
	// date (single)
	//
	List<String> dates = getValues(reader, "date");
	dates.addAll(getValues(reader, "modified"));
	setValue(dates, new Callback() {
	    @Override
	    public void handleValue(String value) {
		if (value.contains("T")) {
		    try {
			Date iso8601 = ISO8601DateTimeUtils.parseISO8601ToDate(value).get();
			miMetadata.setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(iso8601));
		    } catch (Exception ex) {
			GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
		    }
		} else {
		    miMetadata.setDateStampAsDate(value);
		}
	    }
	});

	// ---------------------------
	//
	// type (single)
	//
	List<String> types = getValues(reader, "type");
	setValue(types, new Callback() {
	    @Override
	    public void handleValue(String value) {

		miMetadata.addHierarchyLevelScopeCodeListValue(value);
	    }
	});

	// ---------------------------
	//
	// language (single)
	//
	List<String> languages = getValues(reader, "language");
	setValue(languages, new Callback() {
	    @Override
	    public void handleValue(String value) {
		miMetadata.setLanguage(value);
	    }
	});

	// ---------------------------
	//
	// source (single)
	//
	List<String> sources = getValues(reader, "source");
	setValue(sources, new Callback() {
	    @Override
	    public void handleValue(String value) {
		miMetadata.setParentIdentifier(value);
	    }
	});

	// ---------------------------
	//
	// creator (multiple)
	//
	List<String> creators = getValues(reader, "creator");
	addValue(creators, new Callback() {
	    @Override
	    public void handleValue(String value) {
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("originator");
		responsibleParty.setOrganisationName(value);
		identification.addPointOfContact(responsibleParty);
	    }
	});

	// ---------------------------
	//
	// publisher (multiple)
	//
	List<String> publishers = getValues(reader, "publisher");
	addValue(publishers, new Callback() {
	    @Override
	    public void handleValue(String value) {
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("publisher");
		responsibleParty.setOrganisationName(value);
		identification.addPointOfContact(responsibleParty);
	    }
	});

	// ---------------------------
	//
	// contributor (multiple)
	//
	List<String> contributors = getValues(reader, "contributor");
	addValue(contributors, new Callback() {
	    @Override
	    public void handleValue(String value) {
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("contributor");
		responsibleParty.setOrganisationName(value);
		identification.addPointOfContact(responsibleParty);
	    }
	});

	// ---------------------------
	//
	// custodian (multiple)
	//
	List<String> custodians = getValues(reader, "custodian");
	addValue(custodians, new Callback() {
	    @Override
	    public void handleValue(String value) {
		ResponsibleParty responsibleParty = new ResponsibleParty();
		responsibleParty.setRoleCode("custodian");
		responsibleParty.setOrganisationName(value);
		identification.addPointOfContact(responsibleParty);
	    }
	});

	// ---------------------------
	//
	// format (multiple)
	//
	List<String> formats = getValues(reader, "format");
	addValue(formats, new Callback() {
	    @Override
	    public void handleValue(String value) {
		Format format = new Format();
		format.setName(value);
		distribution.addFormat(format);
	    }
	});

	// ---------------------------
	//
	// subject (multiple)
	//
	List<String> subjects = getValues(reader, "subject");
	addValue(subjects, new Callback() {
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
	// link (multiple)
	//
	List<String> links = getValues(reader, "link");
	addValue(links, new Callback() {
	    @Override
	    public void handleValue(String value) {
		List<Online> onLines = createOnlineResource(value);
		for (Online online : onLines) {
		    distribution.addDistributionOnline(online);
		}
	    }
	});

	// ---------------------------
	//
	// relation (multiple)
	//
	List<String> relations = getValues(reader, "relation");
	addValue(relations, new Callback() {
	    @Override
	    public void handleValue(String value) {
		miMetadata.addAggregatedResourceIdentifier(value);
	    }
	});

	// ---------------------------
	//
	// BoundingBox and/or time as coverage (very special case for the GBIF OAI-PMH)
	//
	List<String> coverages = getValues(reader, "coverage");
	if (!coverages.isEmpty()) {

	    for (String coverage : coverages) {

		try {

		    if (coverage.split("\\/").length == 2) {

			String startTime = coverage.split("/")[0];
			String endTime = coverage.split("/")[1];

			try {

			    ISO8601DateTimeUtils.parseISO8601(startTime);
			    ISO8601DateTimeUtils.parseISO8601(endTime);

			    identification.addTemporalExtent(startTime, endTime);
			} catch (IllegalArgumentException ex) {
			}
		    }

		    else if (coverage.split(" \\/ ").length == 3) {

			String minMaxLat = coverage.split(" / ")[0];
			String minMaxLon = coverage.split(" / ")[1];
			minMaxLon = minMaxLon.substring(0, minMaxLon.indexOf("(") - 1);

			double south = Double.valueOf(minMaxLat.split(",")[0]);
			double north = Double.valueOf(minMaxLat.split(",")[1]);

			double west = Double.valueOf(minMaxLat.split(",")[0]);
			double east = Double.valueOf(minMaxLat.split(",")[1]);

			identification.addGeographicBoundingBox(north, west, south, east);
		    }
		} catch (Exception ex) {
		}
	    }
	}

	// ---------------------------
	//
	// BoundingBox (multiple)
	//
	Node[] bboxes = reader.evaluateNodes("//*:BoundingBox");
	for (int i = 0; i < bboxes.length; i++) {
	    for (Node nodeResult : bboxes) {
		String lower = reader.evaluateString(nodeResult, "*:LowerCorner");
		String upper = reader.evaluateString(nodeResult, "*:UpperCorner");
		if (checkString(lower) && checkString(upper)) {

		    String[] lowerValues = lower.split(" ");
		    String[] upperValues = upper.split(" ");

		    if (lowerValues.length == 2 && upperValues.length == 2) {

			try {
			    Double west = Double.valueOf(lowerValues[0]);
			    Double south = Double.valueOf(lowerValues[1]);
			    Double east = Double.valueOf(upperValues[0]);
			    Double north = Double.valueOf(upperValues[1]);

			    if (west >= -180 && west <= 180 && east >= -180 && east <= 180 && south >= -90 && north <= 90
				    && south <= north) {

				identification.addGeographicBoundingBox(north, west, south, east);
			    }
			} catch (Exception ex) {
			    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
			}
		    }
		}
	    }
	}

	bboxes = reader.evaluateNodes("//*:bbox");
	for (int i = 0; i < bboxes.length; i++) {
	    for (Node bbox : bboxes) {
		String bboxString = reader.evaluateString(bbox, ".");
		if (checkString(bboxString)) {

		    try {
			String[] split = bboxString.contains(",") ? bboxString.split(",") : bboxString.split(" ");
			Double south = Double.valueOf(split[0]);
			Double west = Double.valueOf(split[1]);
			Double north = Double.valueOf(split[2]);
			Double east = Double.valueOf(split[3]);

			if (west >= -180 && west <= 180 && east >= -180 && east <= 180 && south >= -90 && north <= 90 && south <= north) {

			    identification.addGeographicBoundingBox(north, west, south, east);
			}
		    } catch (Exception ex) {
			GSLoggerFactory.getLogger(getClass()).error(ex.getMessage());
		    }

		}
	    }
	}

	coreMetadata.setMIMetadata(miMetadata);
    }

    protected List<Online> createOnlineResource(String link) {
	List<Online> ret = new ArrayList<>();
	Online online = new Online();
	online.setLinkage(link);
	ret.add(online);
	return ret;
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

    protected List<String> getValues(XMLDocumentReader reader, String dcElement) throws XPathExpressionException {

	Node[] nodes = reader.evaluateNodes("//*:" + dcElement);
	ArrayList<String> list = new ArrayList<>();
	for (int i = 0; i < nodes.length; i++) {
	    list.add(nodes[i].getTextContent());
	}

	return list;
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
