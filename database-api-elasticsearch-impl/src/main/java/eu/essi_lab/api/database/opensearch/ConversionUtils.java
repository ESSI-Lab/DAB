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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.PerformanceLogger;
import jakarta.json.stream.JsonGenerator;

/**
 * @author Fabrizio
 */
public class ConversionUtils {

    /**
     * @param searchResponse
     * @return
     */
    public static List<InputStream> toBinaryList(SearchResponse<Object> searchResponse) {

	PerformanceLogger pl = new PerformanceLogger(//
		PerformanceLogger.PerformancePhase.OPENSEARCH_WRAPPER_TO_BINARY_LIST, //
		UUID.randomUUID().toString(), //
		Optional.empty());

	List<InputStream> list = toList(searchResponse, hit -> {

	    SourceWrapper wrapper = new SourceWrapper(toJSONObject(hit.source()));
	    String binaryValue = wrapper.getBinaryValue();
	    return decode(binaryValue);
	});

	pl.logPerformance(GSLoggerFactory.getLogger(OpenSearchWrapper.class));

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
    public static JSONObject toJsonObject(JsonpSerializable obj) {

	StringWriter stringWriter = new StringWriter();

	JsonbJsonpMapper mapper = new JsonbJsonpMapper();
	JsonGenerator generator = mapper.jsonProvider().createGenerator(stringWriter);

	mapper.serialize(obj, generator);
	generator.close();

	return new JSONObject(stringWriter.toString());
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

	String binaryProperty = source.getString("binaryProperty");
	String binaryData = source.getString(binaryProperty);

	return decode(binaryData);
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

    /**
     * @param document
     * @return
     * @throws TransformerException
     */
    private static String toString(Document document) throws TransformerException {

	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Transformer transformer = transformerFactory.newTransformer();

	StringWriter stringWriter = new StringWriter();
	transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

	return stringWriter.toString();
    }
}
