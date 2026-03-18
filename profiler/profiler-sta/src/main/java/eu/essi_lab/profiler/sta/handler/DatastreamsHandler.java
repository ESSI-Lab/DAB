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

package eu.essi_lab.profiler.sta.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.profiler.sta.DatastreamsObservationsTransformer;
import eu.essi_lab.profiler.sta.DatastreamsTransformer;
import eu.essi_lab.profiler.sta.ExpandSubRequest;
import eu.essi_lab.profiler.sta.ObservedPropertiesTransformer;
import eu.essi_lab.profiler.sta.STAResourceMapper;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for OGC STA Datastreams entity set (timeseries records) and Datastreams(id)/Observations.
 * Also handles ObservedProperties(id)/Datastreams navigation (filters by UNIQUE_ATTRIBUTE_IDENTIFIER).
 */
public class DatastreamsHandler extends StreamingRequestHandler {

    private static final int PAGINATION_MONTHS = 2;
    private static final Pattern DATASTREAMS_OBSERVATIONS_PATTERN =
	    Pattern.compile(".*/Datastreams\\([^)]+\\)/Observations.*");
    private static final XMLInputFactory XML_FACTORY = XMLFactories.newXMLInputFactory();

    private DatabaseExecutor executor;

    public DatastreamsHandler() {
	try {
	    eu.essi_lab.model.StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	    executor = DatabaseProviderFactory.getExecutor(uri);
	} catch (GSException e) {
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage vm = new ValidationMessage();
	vm.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return vm;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return output -> {
	    try {
		String path = webRequest.getRequestPath();
		if (path != null && DATASTREAMS_OBSERVATIONS_PATTERN.matcher(path).matches()) {
		    writeDatastreamsObservationsResponse(output, webRequest);
		} else {
		    writeDatastreamsResponse(output, webRequest);
		}
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling Datastreams request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new DatastreamsTransformer();
    }

    private DiscoveryRequestTransformer getObservationsTransformer() {
	return new DatastreamsObservationsTransformer();
    }

    protected void writeDatastreamsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	STARequest staRequest = new STARequest(webRequest);
	if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.ObservedProperties
		&& "Datastreams".equals(staRequest.getNavigationProperty().orElse(null))) {
	    String entityId = staRequest.getEntityIdNormalized().orElse(null);
	    if (entityId != null && entityId.matches("\\d+")) {
		String attributeCode = resolveObservedPropertyIdToAttributeCode(webRequest, entityId);
		if (attributeCode == null) {
		    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
		    return;
		}
		webRequest.getServletRequest().setAttribute(DatastreamsTransformer.ATTR_OBSERVED_PROPERTY_CODE,
			attributeCode);
	    }
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = exec(message);
	List<JSONObject> datastreams = new ArrayList<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    JSONObject ds = STAResourceMapper.datastreamFromParser(parser, baseUrl);
		    if (ds == null) {
			continue;
		    }
		    String id = ds.getString("@iot.id");
		    String platformId = ds.getJSONObject("properties").optString("platformId", null);
		    String begin = parser.getTmpExtentBegin();
		    String end = parser.getTmpExtentEnd();
		    String endNow = parser.getTmpExtentEndNow();
		    addExpandedObservedProperty(ds, parser, baseUrl, staRequest);
		    addExpandedSensor(ds, parser, baseUrl, staRequest);
		    addExpandedObservations(ds, id, platformId, begin, end, endNow, webRequest, message, baseUrl, staRequest);
		    datastreams.add(ds);
		} catch (XMLStreamException | IOException e) {
		}
	    }
	}

	if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Datastreams
		&& staRequest.getEntityId().isPresent()) {
	    if (datastreams.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    if ("ObservedProperty".equals(staRequest.getNavigationProperty().orElse(null))) {
		try {
		    String metadataXml = resultSet.getResultsList().get(0);
		    GIResourceParser parser = new GIResourceParser(metadataXml);
		    JSONObject op = STAResourceMapper.observedPropertyFromParser(parser, baseUrl);
		    if (op != null) {
			output.write(op.toString().getBytes(StandardCharsets.UTF_8));
		    } else {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		    }
		} catch (XMLStreamException | IOException e) {
		    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
		}
		return;
	    }
	    if ("Sensor".equals(staRequest.getNavigationProperty().orElse(null))) {
		try {
		    String metadataXml = resultSet.getResultsList().get(0);
		    GIResourceParser parser = new GIResourceParser(metadataXml);
		    JSONObject sensor = STAResourceMapper.sensorFromParser(parser, baseUrl);
		    if (sensor != null) {
			output.write(sensor.toString().getBytes(StandardCharsets.UTF_8));
		    } else {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		    }
		} catch (XMLStreamException | IOException e) {
		    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
		}
		return;
	    }
	    if ("Thing".equals(staRequest.getNavigationProperty().orElse(null))) {
		try {
		    String metadataXml = resultSet.getResultsList().get(0);
		    GIResourceParser parser = new GIResourceParser(metadataXml);
		    JSONObject thing = STAResourceMapper.thingFromParser(parser, baseUrl);
		    if (thing != null) {
			output.write(thing.toString().getBytes(StandardCharsets.UTF_8));
		    } else {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
		    }
		} catch (XMLStreamException | IOException e) {
		    throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
		}
		return;
	    }
	    JSONObject singleDs = datastreams.get(0);
	    String dsId = singleDs.getString("@iot.id");
	    String platformId = singleDs.getJSONObject("properties").optString("platformId", null);
	    String begin = null;
	    String end = null;
	    String endNow = null;
	    String pt = singleDs.optString("phenomenonTime", "");
	    if (pt.contains("/")) {
		int slash = pt.indexOf('/');
		begin = pt.substring(0, slash).trim();
		end = pt.substring(slash + 1).trim();
	    } else if (!pt.isEmpty()) {
		begin = pt;
		end = pt;
	    }
	    addExpandedObservations(singleDs, dsId, platformId, begin, end, endNow, webRequest, message, baseUrl, staRequest);
	    output.write(singleDs.toString().getBytes(StandardCharsets.UTF_8));
	    return;
	}

	Integer count = null;
	if (resultSet != null && resultSet.getCountResponse() != null) {
	    count = resultSet.getCountResponse().getCount();
	}

	String nextLink = null;
	int pageSize = staRequest.getTop() != null ? staRequest.getTop() : 100;
	boolean hasFullPage = datastreams.size() >= pageSize;
	if (hasFullPage && resultSet != null && resultSet.getSearchAfter().isPresent()) {
	    String token = resultSet.getSearchAfter().get().getValues()
		    .map(v -> v.isEmpty() ? null : v.get(0).toString())
		    .orElse(null);
	    if (token != null) {
		String entityPath = "Datastreams";
		if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Things
			&& staRequest.getEntityIdNormalized().isPresent()) {
		    entityPath = "Things(" + staRequest.getEntityIdNormalized().get() + ")/Datastreams";
		} else if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.ObservedProperties
			&& staRequest.getEntityIdNormalized().isPresent()
			&& "Datastreams".equals(staRequest.getNavigationProperty().orElse(null))) {
		    entityPath = "ObservedProperties(" + staRequest.getEntityIdNormalized().get() + ")/Datastreams";
		}
		nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(datastreams, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveObservedPropertyIdToAttributeCode(WebRequest webRequest, String numericId) {
	if (executor == null) {
	    return null;
	}
	try {
	    long targetId = Long.parseLong(numericId);
	    DiscoveryRequestTransformer transformer = new ObservedPropertiesTransformer();
	    DiscoveryMessage message = transformer.transform(webRequest);
	    ResultSet<String> resultSet = executor.discoverDistinctStrings(message);
	    if (resultSet == null) {
		return null;
	    }
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String uniqueAttributeId = parser.getAttributeCode();
		    if (uniqueAttributeId != null && !uniqueAttributeId.isEmpty()) {
			long id = STAResourceMapper.observedPropertyId(uniqueAttributeId);
			if (id == targetId) {
			    return uniqueAttributeId;
			}
		    }
		} catch (Exception e) {
		    // skip
		}
	    }
	} catch (Exception e) {
	    // ignore
	}
	return null;
    }

    private void addExpandedObservedProperty(JSONObject datastream, GIResourceParser parser, String baseUrl,
	    STARequest staRequest) {
	boolean expandOp = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "ObservedProperty".equals(o.getProperty()));
	if (!expandOp) {
	    return;
	}
	JSONObject op = STAResourceMapper.observedPropertyFromParser(parser, baseUrl);
	if (op != null) {
	    datastream.put("ObservedProperty", op);
	}
    }

    private void addExpandedSensor(JSONObject datastream, GIResourceParser parser, String baseUrl,
	    STARequest staRequest) {
	boolean expandSensor = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "Sensor".equals(o.getProperty()));
	if (!expandSensor || parser == null) {
	    return;
	}
	JSONObject sensor = STAResourceMapper.sensorFromParser(parser, baseUrl);
	if (sensor != null) {
	    datastream.put("Sensor", sensor);
	}
    }

    private void addExpandedObservations(JSONObject datastream, String datastreamId, String platformId,
	    String beginStr, String endStr, String endNowStr, WebRequest webRequest, DiscoveryMessage message,
	    String baseUrl, STARequest staRequest) {
	boolean expandObs = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "Observations".equals(o.getProperty()));
	if (!expandObs) {
	    return;
	}
	STARequest.ExpandOption obsOpt = staRequest.getExpandOptions().stream()
		.filter(o -> "Observations".equals(o.getProperty()))
		.findFirst()
		.orElse(null);
	if (obsOpt == null) {
	    return;
	}
	int top = obsOpt.getTop() != null ? obsOpt.getTop() : 100;
	String filter = obsOpt.getFilter();
	String orderBy = obsOpt.getOrderBy();

	java.util.Date windowBeginDate;
	java.util.Date windowEndDate;
	String[] range = STARequest.ExpandOption.parsePhenomenonTimeRange(filter);
	if (range != null && range.length == 2) {
	    Optional<java.util.Date> geOpt = ISO8601DateTimeUtils.parseISO8601ToDate(range[0]);
	    Optional<java.util.Date> leOpt = ISO8601DateTimeUtils.parseISO8601ToDate(range[1]);
	    if (geOpt.isPresent() && leOpt.isPresent()) {
		windowBeginDate = geOpt.get();
		windowEndDate = leOpt.get();
	    } else {
		windowBeginDate = null;
		windowEndDate = null;
	    }
	} else {
	    if (endNowStr != null && "true".equals(endNowStr)) {
		endStr = ISO8601DateTimeUtils.getISO8601DateTime();
	    }
	    if (beginStr == null || endStr == null) {
		datastream.put("Observations", new JSONArray());
		return;
	    }
	    Optional<java.util.Date> beginOpt = ISO8601DateTimeUtils.parseISO8601ToDate(beginStr);
	    Optional<java.util.Date> endOpt = ISO8601DateTimeUtils.parseISO8601ToDate(endStr);
	    if (!beginOpt.isPresent() || !endOpt.isPresent()) {
		datastream.put("Observations", new JSONArray());
		return;
	    }
	    ZonedDateTime phenomenonStart = ZonedDateTime.ofInstant(beginOpt.get().toInstant(), ZoneOffset.UTC);
	    ZonedDateTime phenomenonEnd = ZonedDateTime.ofInstant(endOpt.get().toInstant(), ZoneOffset.UTC);
	    ZonedDateTime windowEnd = phenomenonEnd;
	    ZonedDateTime windowBegin = phenomenonEnd.minusMonths(2);
	    if (windowBegin.isBefore(phenomenonStart)) {
		windowBegin = phenomenonStart;
	    }
	    windowBeginDate = java.util.Date.from(windowBegin.toInstant());
	    windowEndDate = java.util.Date.from(windowEnd.toInstant());
	}
	if (windowBeginDate == null || windowEndDate == null) {
	    datastream.put("Observations", new JSONArray());
	    return;
	}

	try {
	    List<JSONObject> obs = fetchObservationsForDatastream(
		    webRequest, message, datastreamId, platformId, windowBeginDate, windowEndDate, baseUrl);

	    boolean orderDesc = orderBy != null && orderBy.toLowerCase().contains("phenomenontime")
		    && orderBy.toLowerCase().contains("desc");
	    if (orderDesc) {
		obs.sort(Comparator.comparing((JSONObject o) -> o.optString("phenomenonTime", ""))
			.reversed());
	    }
	    if (obs.size() > top) {
		obs = obs.subList(0, top);
	    }
	    JSONArray arr = new JSONArray();
	    for (JSONObject o : obs) {
		arr.put(o);
	    }
	    datastream.put("Observations", arr);
	} catch (Exception e) {
	    datastream.put("Observations", new JSONArray());
	}
    }

    private void writeDatastreamsObservationsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	STARequest staRequest = new STARequest(webRequest);
	String datastreamId = staRequest.getEntityIdNormalized().orElse(null);
	if (datastreamId == null || datastreamId.isEmpty()) {
	    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Missing Datastream id").build());
	}

	DiscoveryRequestTransformer transformer = getObservationsTransformer();
	DiscoveryMessage discoveryMessage = transformer.transform(webRequest);
	ResultSet<String> discoveryResult = exec(discoveryMessage);

	if (discoveryResult == null || discoveryResult.getResultsList().isEmpty()) {
	    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	}

	String metadataXml = discoveryResult.getResultsList().get(0);
	GIResourceParser parser = new GIResourceParser(metadataXml);
	String beginStr = parser.getTmpExtentBegin();
	String endStr = parser.getTmpExtentEnd();
	String endNow = parser.getTmpExtentEndNow();
	if (endNow != null && "true".equals(endNow)) {
	    endStr = ISO8601DateTimeUtils.getISO8601DateTime();
	}

	if (beginStr == null || endStr == null) {
	    output.write(STAJsonWriter.collectionResponse(new ArrayList<>(), null, null).getBytes(StandardCharsets.UTF_8));
	    return;
	}

	Optional<java.util.Date> beginOpt = ISO8601DateTimeUtils.parseISO8601ToDate(beginStr);
	Optional<java.util.Date> endOpt = ISO8601DateTimeUtils.parseISO8601ToDate(endStr);
	if (!beginOpt.isPresent() || !endOpt.isPresent()) {
	    output.write(STAJsonWriter.collectionResponse(new ArrayList<>(), null, null).getBytes(StandardCharsets.UTF_8));
	    return;
	}

	ZonedDateTime phenomenonStart = ZonedDateTime.ofInstant(beginOpt.get().toInstant(), ZoneOffset.UTC);
	ZonedDateTime phenomenonEnd = ZonedDateTime.ofInstant(endOpt.get().toInstant(), ZoneOffset.UTC);

	String resumptionToken = staRequest.getResumptionToken();
	ZonedDateTime windowEnd;
	ZonedDateTime windowBegin;

	if (resumptionToken != null && !resumptionToken.isEmpty()) {
	    Optional<java.util.Date> tokenDate = ISO8601DateTimeUtils.parseISO8601ToDate(resumptionToken);
	    if (!tokenDate.isPresent()) {
		throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Invalid resumptionToken").build());
	    }
	    windowEnd = ZonedDateTime.ofInstant(tokenDate.get().toInstant(), ZoneOffset.UTC);
	    windowBegin = windowEnd.minusMonths(PAGINATION_MONTHS);
	    if (windowBegin.isBefore(phenomenonStart)) {
		windowBegin = phenomenonStart;
	    }
	} else {
	    windowEnd = phenomenonEnd;
	    windowBegin = phenomenonEnd.minusMonths(PAGINATION_MONTHS);
	    if (windowBegin.isBefore(phenomenonStart)) {
		windowBegin = phenomenonStart;
	    }
	}

	java.util.Date windowBeginDate = java.util.Date.from(windowBegin.toInstant());
	java.util.Date windowEndDate = java.util.Date.from(windowEnd.toInstant());

	AccessMessage accessMessage = new AccessMessage();
	accessMessage.setWebRequest(webRequest);
	accessMessage.setOnlineId(datastreamId);
	accessMessage.setSources(discoveryMessage.getSources());
	accessMessage.setCurrentUser(discoveryMessage.getCurrentUser().orElse(null));
	accessMessage.setDataBaseURI(discoveryMessage.getDataBaseURI());

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(eu.essi_lab.model.resource.data.CRS.EPSG_4326());
	descriptor.setTemporalDimension(windowBeginDate, windowEndDate);
	descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
	descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);
	accessMessage.setTargetDataDescriptor(descriptor);

	ResultSet<DataObject> accessResult = exec(accessMessage);
	if (accessResult == null || accessResult.getResultsList().isEmpty()) {
	    output.write(STAJsonWriter.collectionResponse(new ArrayList<>(), null, null).getBytes(StandardCharsets.UTF_8));
	    return;
	}

	DataObject dataObject = accessResult.getResultsList().get(0);
	File file = dataObject.getFile();
	if (file == null || !file.exists()) {
	    output.write(STAJsonWriter.collectionResponse(new ArrayList<>(), null, null).getBytes(StandardCharsets.UTF_8));
	    return;
	}

	List<DataPoint> points = parseWaterMLValues(file);
	String platformId = parser.getUniquePlatformCode();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());

	List<JSONObject> observations = new ArrayList<>();
	for (DataPoint point : points) {
	    long obsId = observationId(datastreamId, point.phenomenonTime);
	    JSONObject obs = STAJsonWriter.observation(
		    obsId,
		    point.value != null ? point.value : JSONObject.NULL,
		    point.phenomenonTime,
		    null,
		    datastreamId,
		    platformId,
		    baseUrl);
	    observations.add(obs);
	}

	String nextLink = null;
	if (windowBegin.isAfter(phenomenonStart)) {
	    String nextToken = ISO8601DateTimeUtils.getISO8601DateTime(java.util.Date.from(windowBegin.toInstant()));
	    String entityPath = "Datastreams(" + datastreamId + ")/Observations";
	    nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, nextToken, webRequest.getQueryString());
	}

	String json = STAJsonWriter.collectionResponse(observations, nextLink, null);
	output.write(json.getBytes(StandardCharsets.UTF_8));

	if (file.exists()) {
	    file.delete();
	}
    }

    /**
     * Fetches observation values for a datastream in the given time window.
     * Used for $expand=Observations and by ObservationsHandler.
     */
    List<JSONObject> fetchObservationsForDatastream(WebRequest webRequest, DiscoveryMessage discoveryMessage,
	    String datastreamId, String platformId, java.util.Date windowBegin, java.util.Date windowEnd,
	    String baseUrl) throws Exception {
	AccessMessage accessMessage = new AccessMessage();
	accessMessage.setWebRequest(webRequest);
	accessMessage.setOnlineId(datastreamId);
	accessMessage.setSources(discoveryMessage.getSources());
	accessMessage.setCurrentUser(discoveryMessage.getCurrentUser().orElse(null));
	accessMessage.setDataBaseURI(discoveryMessage.getDataBaseURI());

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(eu.essi_lab.model.resource.data.CRS.EPSG_4326());
	descriptor.setTemporalDimension(windowBegin, windowEnd);
	descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
	descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);
	accessMessage.setTargetDataDescriptor(descriptor);

	ResultSet<DataObject> accessResult = exec(accessMessage);
	if (accessResult == null || accessResult.getResultsList().isEmpty()) {
	    return new ArrayList<>();
	}

	DataObject dataObject = accessResult.getResultsList().get(0);
	File file = dataObject.getFile();
	if (file == null || !file.exists()) {
	    return new ArrayList<>();
	}

	List<DataPoint> points = parseWaterMLValues(file);
	List<JSONObject> observations = new ArrayList<>();
	for (DataPoint point : points) {
	    long obsId = observationId(datastreamId, point.phenomenonTime);
	    JSONObject obs = STAJsonWriter.observation(obsId,
		    point.value != null ? point.value : JSONObject.NULL,
		    point.phenomenonTime, null, datastreamId, platformId, baseUrl);
	    observations.add(obs);
	}

	if (file.exists()) {
	    file.delete();
	}
	return observations;
    }

    /**
     * Generates a deterministic numeric id for an Observation from datastream id and phenomenon time.
     * Used for @iot.id and @iot.selfLink since observations have no persistent id in the source data.
     */
    private static long observationId(String datastreamId, String phenomenonTime) {
	long h = (datastreamId + "|" + phenomenonTime).hashCode();
	return h >= 0 ? h : (h & 0x7FFFFFFF);
    }

    private static List<DataPoint> parseWaterMLValues(File file) throws IOException, XMLStreamException {
	List<DataPoint> points = new ArrayList<>();
	FileInputStream stream = new FileInputStream(file);
	StreamSource source = new StreamSource(stream);
	XMLEventReader reader = XML_FACTORY.createXMLEventReader(source);

	String nodataValue = null;
	while (reader.hasNext()) {
	    XMLEvent event = reader.nextEvent();
	    if (event.isStartElement()) {
		String startName = event.asStartElement().getName().getLocalPart();
		switch (startName) {
		case "noDataValue":
		    nodataValue = readValue(reader);
		    break;
		case "value":
		    Attribute dateTimeAttr = event.asStartElement().getAttributeByName(new QName("dateTimeUTC"));
		    if (dateTimeAttr == null) {
			dateTimeAttr = event.asStartElement().getAttributeByName(new QName("dateTime"));
		    }
		    if (dateTimeAttr != null) {
			String date = dateTimeAttr.getValue();
			String valueStr = readValue(reader);
			BigDecimal value = null;
			if (nodataValue == null || !nodataValue.equals(valueStr)) {
			    try {
				value = new BigDecimal(valueStr);
			    } catch (NumberFormatException e) {
				// skip invalid values
			    }
			}
			Optional<java.util.Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
			if (d.isPresent()) {
			    String iso8601 = ISO8601DateTimeUtils.getISO8601DateTime(d.get());
			    points.add(new DataPoint(iso8601, value));
			}
		    }
		    break;
		default:
		    break;
		}
	    }
	}
	reader.close();
	stream.close();
	return points;
    }

    private static String readValue(XMLEventReader reader) throws XMLStreamException {
	StringBuilder ret = new StringBuilder();
	XMLEvent event;
	do {
	    event = reader.nextEvent();
	    if (event instanceof Characters) {
		ret.append(((Characters) event).getData());
	    }
	} while (event != null && !event.isEndElement());
	return ret.toString().trim();
    }

    private static final class DataPoint {
	final String phenomenonTime;
	final BigDecimal value;

	DataPoint(String phenomenonTime, BigDecimal value) {
	    this.phenomenonTime = phenomenonTime;
	    this.value = value;
	}
    }
}
