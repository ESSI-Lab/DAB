/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.json.jsonb.JsonbJsonpMapper;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.ResourceDecorator;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourceType;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

/**
 * @author Fabrizio
 */
public class ConversionUtils {

    /**
     * @param aggs
     * @return
     */
    public static TermFrequencyMapType fromAgg(Map<String, Aggregate> aggs) {

	TermFrequencyMapType mapType = new TermFrequencyMapType();

	aggs.keySet().forEach(target -> {

	    Aggregate aggregate = aggs.get(target);
	    StringTermsAggregate sterms = aggregate.sterms();

	    Buckets<StringTermsBucket> buckets = sterms.buckets();
	    List<StringTermsBucket> array = buckets.array();

	    for (StringTermsBucket bucket : array) {

		int count = (int) bucket.docCount();
		String term = bucket.key();

		TermFrequencyItem item = new TermFrequencyItem();
		item.setTerm(term);
		item.setDecodedTerm(term);
		item.setFreq(count);
		item.setLabel(target);

		switch (TermFrequencyTarget.fromValue(target)) {
		case ATTRIBUTE_IDENTIFIER:
		    mapType.getAttributeId().add(item);
		    break;
		case ATTRIBUTE_TITLE:
		    mapType.getAttributeTitle().add(item);
		    break;
		case FORMAT:
		    mapType.getFormat().add(item);
		    break;
		case INSTRUMENT_IDENTIFIER:
		    mapType.getInstrumentId().add(item);
		    break;
		case INSTRUMENT_TITLE:
		    mapType.getAttributeTitle().add(item);
		    break;
		case KEYWORD:
		    mapType.getKeyword().add(item);
		    break;
		case OBSERVED_PROPERTY_URI:
		    mapType.getObservedPropertyURI().add(item);
		    break;
		case ORGANISATION_NAME:
		    mapType.getOrganisationName().add(item);
		    break;
		case ORIGINATOR_ORGANISATION_DESCRIPTION:
		    mapType.getOrigOrgDescription().add(item);
		    break;
		case ORIGINATOR_ORGANISATION_IDENTIFIER:
		    mapType.getOrigOrgId().add(item);
		    break;
		case PLATFORM_IDENTIFIER:
		    mapType.getPlatformId().add(item);
		    break;
		case PLATFORM_TITLE:
		    mapType.getPlatformTitle().add(item);
		    break;
		case PROD_TYPE:
		    mapType.getProdType().add(item);
		    break;
		case PROTOCOL:
		    mapType.getProtocol().add(item);
		    break;
		case S3_INSTRUMENT_IDX:
		    mapType.getS3InstrumentIdx().add(item);
		    break;
		case S3_PRODUCT_LEVEL:
		    mapType.getS3ProductLevel().add(item);
		    break;
		case S3_TIMELINESS:
		    mapType.getS3Timeliness().add(item);
		    break;
		case SAR_POL_CH:
		    mapType.getSarPolCh().add(item);
		    break;
		case SENSOR_OP_MODE:
		    mapType.getSensorOpMode().add(item);
		    break;
		case SENSOR_SWATH:
		    mapType.getSensorSwath().add(item);
		    break;
		case SOURCE:
		    mapType.getSourceId().add(item);
		    break;
		case SSC_SCORE:
		    mapType.getSSCScore().add(item);
		    break;
		}
	    }
	});

	return mapType;
    }

    /**
     * @param searchResponse
     * @return
     */
    public static List<JSONObject> toJSONSourcesList(SearchResponse<Object> searchResponse) {

	List<JSONObject> list = toList(searchResponse, hit -> toJSONObject(hit.source()));

	return list;
    }

    /**
     * @param searchResponse
     * @return
     */
    public static List<InputStream> toBinaryList(SearchResponse<Object> searchResponse) {

	List<InputStream> list = toList(searchResponse, hit -> {

	    SourceWrapper wrapper = new SourceWrapper(toJSONObject(hit.source()));
	    String binaryValue = wrapper.getBinaryValue();
	    return decode(binaryValue);
	});

	return list;
    }

    /**
     * @param searchResponse
     * @return
     */
    public static List<String> toStringList(SearchResponse<Object> searchResponse) {

	PerformanceLogger pl = new PerformanceLogger(//
		PerformanceLogger.PerformancePhase.OPENSEARCH_WRAPPER_TO_STRING_LIST, //
		UUID.randomUUID().toString(), //
		Optional.empty());

	List<String> list = toList(searchResponse, hit -> {

	    SourceWrapper wrapper = new SourceWrapper(toJSONObject(hit.source()));
	    String binaryValue = wrapper.getBinaryValue();
	    return decodeToString(binaryValue);
	});

	pl.logPerformance(GSLoggerFactory.getLogger(OpenSearchWrapper.class));

	return list;
    }

    /**
     * @param searchResponse
     * @return
     */
    public static List<Node> toNodeList(SearchResponse<Object> searchResponse) {

	PerformanceLogger pl = new PerformanceLogger(//
		PerformanceLogger.PerformancePhase.OPENSEARCH_WRAPPER_TO_NODE_LIST, //
		UUID.randomUUID().toString(), //
		Optional.empty());

	List<Node> list = toList(searchResponse, hit -> {

	    SourceWrapper wrapper = new SourceWrapper(toJSONObject(hit.source()));
	    String binaryValue = wrapper.getBinaryValue();

	    InputStream stream = decode(binaryValue);

	    return toNodeOrNull(stream);
	});

	pl.logPerformance(GSLoggerFactory.getLogger(OpenSearchWrapper.class));

	return list;
    }

    /**
     * @param obj
     * @return
     */
    public static JSONObject toJSONObject(JsonpSerializable obj) {

	StringWriter stringWriter = new StringWriter();

	JsonbJsonpMapper mapper = new JsonbJsonpMapper();
	JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);

	mapper.serialize(obj, generator);
	generator.close();

	return new JSONObject(stringWriter.toString());
    }

    /**
     * @param query
     * @return
     */
    public static Query toQuery(JSONObject query) {

	JsonbJsonpMapper mapper = new JsonbJsonpMapper();

	JsonParser parser = mapper.jsonProvider().//
		createParser(new ByteArrayInputStream(query.toString(3).getBytes()));

	return mapper.deserialize(parser, Query.class);
    }

    /**
     * @param doc
     * @param stream
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    public static String encode(Document doc, ClonableInputStream stream) throws IOException, TransformerException {

	byte[] bytes = null;

	if (doc != null) {

	    bytes = toString(doc).getBytes();

	} else {

	    bytes = IOStreamUtils.getBytes(stream.clone());
	}

	return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @param resource
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws JAXBException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String encode(GSResource resource) {

	try {

	    return encode(resource.asDocument(false));

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ConversionUtils.class).error(ex);
	}

	return null;
    }

    /**
     * @param resource
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws JAXBException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String encode(Document resourceDoc) {

	try {
	    byte[] bytes = toString(resourceDoc).getBytes();
	    return Base64.getEncoder().encodeToString(bytes);
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ConversionUtils.class).error(ex);
	}

	return null;
    }

    /**
     * @param entry
     * @return
     * @throws IOException
     * @throws TransformerException
     */
    public static String encode(FolderEntry entry) throws IOException, TransformerException {

	byte[] bytes = null;

	if (entry.getDocument().isPresent()) {

	    Document doc = entry.getDocument().get();

	    bytes = toString(doc).getBytes();

	} else {

	    InputStream stream = entry.getStream().get();
	    bytes = IOStreamUtils.getBytes(stream);
	}

	return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * @param binaryData
     * @return
     */
    public static InputStream decode(String binaryData) {

	byte[] decoded = Base64.getDecoder().decode(binaryData);
	return new ByteArrayInputStream(decoded);
    }

    /**
     * @param binaryData
     * @return
     */
    public static String decodeToString(String binaryData) {

	byte[] decoded = Base64.getDecoder().decode(binaryData);
	return new String(decoded);
    }

    /**
     * @param source
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public static Optional<GSResource> toGSResource(JSONObject source) {

	try {

	    GSResource res = null;

	    if (source.has(DataFolderMapping.GS_RESOURCE)) {

		InputStream stream = toStream(source);
		res = GSResource.create(stream);

	    } else {

		ResourceType type = ResourceType.fromType(//
			source.getJSONArray(ResourceProperty.TYPE.getName()).getString(0));
		
		switch (type) {
		case DATASET:
		    res = new Dataset();
		    break;
		    
		case DATASET_COLLECTION:
		    res = new DatasetCollection();
		    break;
		}
	    }

	    return Optional.of(ResourceDecorator.get().decorate(source, res));

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ConversionUtils.class).error(ex);
	}

	return Optional.empty();
    }

    /**
     * @param source
     * @return
     */
    @SuppressWarnings("unchecked")
    public static JSONObject toJSONObject(Object source) {

	return new JSONObject((HashMap<String, String>) source);
    }

    /**
     * @param source
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Node toNode(InputStream source) throws SAXException, IOException, ParserConfigurationException {

	DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
	DocumentBuilder builder = factory.newDocumentBuilder();

	return builder.parse(source);
    }

    /**
     * @param source
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Node toNodeOrNull(InputStream source) {

	try {
	    return toNode(source);

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(OpenSearchWrapper.class).error(ex);
	}

	return null;
    }

    /**
     * @param source
     * @return
     */
    public static InputStream toStream(JSONObject source) {

	String binaryProperty = source.getString(IndexData.BINARY_PROPERTY);
	String binaryData = source.getString(binaryProperty);

	return decode(binaryData);
    }

    /**
     * @param document
     * @return
     * @throws TransformerException
     */
    public static String toString(Document document) throws TransformerException {

	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();

	StringWriter stringWriter = new StringWriter();
	transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

	return stringWriter.toString();
    }

    /**
     * @param dateTime
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Optional<Long> parseToLong(String dateTime) {

	dateTime = dateTime.replace("/", "-");

	Optional<Date> date = Optional.empty();

	if (dateTime.length() == "yyyy".length()) {

	    date = parseYYYToDate(dateTime);

	} else if (dateTime.length() == "yyyyMMddHHmm".length()) {

	    date = ISO8601DateTimeUtils.parseNotStandard2ToDate(dateTime);

	} else if (dateTime.length() == "yyyyMMdd".length()) {

	    date = ISO8601DateTimeUtils.parseNotStandardToDate(dateTime);

	} else {

	    date = ISO8601DateTimeUtils.parseISO8601ToDate(dateTime);
	}

	if (!date.isEmpty()) {

	    int year = date.get().getYear();
	    if (year > 9999) {

		return Optional.empty();
	    }

	    return date.map(d -> d.getTime());
	}

	return Optional.empty();
    }

    /**
     * @param dateTime
     * @return
     */
    public static String parseToLongString(String dateTime) {

	Optional<Long> longValue = ConversionUtils.parseToLong(dateTime);
	if (longValue.isPresent()) {

	    return longValue.get().toString();
	}

	throw new IllegalArgumentException("Unparsable date/date time value: " + dateTime);
    }

    /**
     * @param dateTimeString
     * @return
     */
    private static Optional<Date> parseYYYToDate(String dateTimeString) {

	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601DateTimeUtils.ISO_WITH_MILLIS);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

	    Date date = dateFormat.parse(dateTimeString + "-01-01T00:00:00Z");

	    return Optional.of(date);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(ISO8601DateTimeUtils.class).warn("Unparsable Date: {}", dateTimeString);
	}

	return Optional.empty();
    }

    /**
     * @param <T>
     * @param searchResponse
     * @param type
     * @return
     */
    private static <T> List<T> toList(SearchResponse<Object> searchResponse, Function<? super Hit<Object>, ? extends T> mapper) {

	HitsMetadata<Object> hits = searchResponse.hits();

	List<Hit<Object>> hitsList = hits.hits();

	List<T> list = hitsList.stream().//

		map(mapper).//

		collect(Collectors.toList());

	return list;
    }
}
