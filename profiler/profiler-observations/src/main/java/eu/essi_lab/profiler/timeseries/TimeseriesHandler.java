package eu.essi_lab.profiler.timeseries;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.timeseries.TimeseriesMapper.Property;
import eu.essi_lab.profiler.timeseries.TimeseriesRequest.APIParameters;

public class TimeseriesHandler extends StreamingRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public TimeseriesHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		TimeseriesRequest request = new TimeseriesRequest(webRequest);

		String properties = request.getParameterValue(APIParameters.OUTPUT_PROPERTIES);
		List<Property> propertySet = new ArrayList<TimeseriesMapper.Property>();
		if (properties != null && !properties.isEmpty()) {
		    String[] split;
		    if (properties.contains(",")) {
			split = properties.split(",");
		    } else {
			split = new String[] { properties };
		    }
		    for (String s : split) {
			Property p = Property.decode(s);
			propertySet.add(p);
		    }
		}

		String useCacheParameter = request.getParameterValue(APIParameters.USE_CACHE);
		DataCacheConnector dataCacheConnector = null;
		Date begin = null;
		Date end = null;
		boolean useCache = false;
		if (useCacheParameter != null && (useCacheParameter.toLowerCase().trim().equals("true")
			|| useCacheParameter.toLowerCase().trim().equals("yes"))) {
		    useCache = true;
		}

		if (useCache) {
		    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

		    if (dataCacheConnector == null) {
			DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
			try {
			    dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
			} catch (Exception e) {
			    e.printStackTrace();
			    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] loading data cache");
			}
			String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
			String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
			String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
			dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
			dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
			dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
			DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
		    }
		}
		Optional<SimpleValueBond> beginBond = request.getBeginBond();
		Optional<SimpleValueBond> endBond = request.getEndBond();
		if (beginBond.isPresent() && endBond.isPresent()) {

		    if (beginBond.isPresent()) {
			begin = ISO8601DateTimeUtils.parseISO8601ToDate(beginBond.get().getPropertyValue()).get();
		    }
		    if (endBond.isPresent()) {
			end = ISO8601DateTimeUtils.parseISO8601ToDate(endBond.get().getPropertyValue()).get();
		    }
		}

		String format = request.getParameterValue(APIParameters.FORMAT);
		DataFormat dataFormat;

		if (format == null) {
		    format = "JSON";
		}

		format = format.toUpperCase();
		CSVField[] fields = null;
		switch (format) {
		case "CSV":
		    dataFormat = DataFormat.WATERML_1_1();
		    fields = new CSVField[] { CSVField.TIMESERIES_ID, CSVField.DATE_TIME, CSVField.VALUE };
		    break;
		case "JSON":
		    dataFormat = DataFormat.WATERML_1_1();
		    break;
		default:
		    throw new IllegalArgumentException("Unrecognized format. Choose between: CSV, JSON");
		}

		DiscoveryRequestTransformer transformer = getTransformer();

		DiscoveryMessage discoveryMessage;
		try {
		    discoveryMessage = transformer.transform(webRequest);
		} catch (GSException gse) {
		    List<ErrorInfo> list = gse.getErrorInfoList();
		    if (list.isEmpty()) {
			printErrorMessage(output, "Unknown error");
		    } else {
			ErrorInfo error = list.get(0);
			printErrorMessage(output, error.getErrorDescription());

		    }
		    return;
		}
		if (useCache) {
		    ResourceSelector selector = new ResourceSelector();
		    selector.setSubset(ResourceSubset.NONE);
		    selector.setIndexesPolicy(IndexesPolicy.NONE);
		    selector.addIndex(MetadataElement.ONLINE_ID);
		    selector.setIncludeOriginal(false);

		    // selector.addIndex(MetadataElement.PLATFORM_TITLE);
		    // selector.addIndex(ResourceProperty.SOURCE_ID);
		    // selector.addIndex(MetadataElement.BOUNDING_BOX);
		    // selector.addIndex(MetadataElement.COUNTRY);
		    discoveryMessage.setResourceSelector(selector);
		}

		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		Page userPage = discoveryMessage.getPage();
		int userSize = userPage.getSize();
		int pageSize = Math.min(userSize, 1000);
		userPage.setSize(pageSize);

		ResultSet<String> resultSet = null;
		int tempSize = 0;

		boolean first = true;

		do {

		    try {
			resultSet = exec(discoveryMessage);

			List<String> results = resultSet.getResultsList();
			tempSize += pageSize;

			if (results.isEmpty()) {
			    printErrorMessage(output, "No " + getObject() + " matched");
			    return;
			}

			List<JSONTimeseries> observations = new ArrayList<>();
			GSLoggerFactory.getLogger(getClass()).info("sorting");
			GSLoggerFactory.getLogger(getClass()).info("mapping");
			List<String> identifiers = new ArrayList<>();
			int j = 0;
			for (String result : results) {
				TimeseriesMapper observationMapper = new TimeseriesMapper();

			    JSONTimeseries observation = observationMapper.map(result, propertySet);
			    observations.add(observation);
			}
			Collections.sort(observations, new Comparator<JSONTimeseries>() {

			    @Override
			    public int compare(JSONTimeseries o1, JSONTimeseries o2) {
				return o1.getId().compareTo(o2.getId());
			    }
			});
			for (JSONTimeseries feature : observations) {
			    identifiers.add(feature.getId());
			}
			GSLoggerFactory.getLogger(getClass()).info("getting data");
			List<DataRecord> datas = new ArrayList<>();
			int h = 0;
			if (useCache) {
			    datas = dataCacheConnector.getRecords(begin, end, identifiers.toArray(new String[] {}));
			}
			GSLoggerFactory.getLogger(getClass()).info("formatting");
			for (JSONTimeseries observation : observations) {

			    String dataId = observation.getId();

			    if (format.equals("JSON")) {
				if (first) {
				    writer.write("{");
				    addIdentifier(writer);
				    writer.write("\"" + getSetName() + "\":[");
				}
			    }
			    if (format.equals("CSV")) {
				if (first) {
				    int i = 0;
				    for (CSVField field : fields) {
					writer.write(field.label);
					if (i++ == fields.length - 1) {
					    writer.write("\n");
					} else {
					    writer.write("\t");
					}
				    }

				}
			    }

			    // DATA part

			    String includeValues = request.getParameterValue(APIParameters.INCLUDE_VALUES);
			    if ((includeValues != null
				    && (includeValues.toLowerCase().equals("yes") || includeValues.toLowerCase().equals("true")))
				    || (beginBond.isPresent() && endBond.isPresent())) {

				if (useCache) {

				    DataRecord point;
				    String dataIdentifier;
				    while (h < datas.size() - 1) {
					point = datas.get(h);
					dataIdentifier = point.getDataIdentifier();
					if (dataIdentifier.equals(dataId)) {
					    observation.addPoint(point.getDate(), point.getValue());
					    h++;
					} else {
					    break;
					}
				    }

				} else {

				    AccessMessage accessMessage = new AccessMessage();
				    accessMessage.setOnlineId(observation.getId());
				    accessMessage.setSources(discoveryMessage.getSources());
				    accessMessage.setCurrentUser(discoveryMessage.getCurrentUser().orElse(null));
				    accessMessage.setDataBaseURI(discoveryMessage.getDataBaseURI());

				    DataDescriptor descriptor = new DataDescriptor();

				    descriptor.setDataFormat(dataFormat);
				    descriptor.setDataType(DataType.TIME_SERIES);
				    descriptor.setCRS(CRS.EPSG_4326());
				    if (begin != null && end != null) {
					descriptor.setTemporalDimension(begin, end);
					descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
					descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);
				    }
				    accessMessage.setTargetDataDescriptor(descriptor);

				    ResultSet<DataObject> accessResult = exec(accessMessage);

				    DataObject dataObject = accessResult.getResultsList().get(0);

				    try {
					FileInputStream stream = new FileInputStream(dataObject.getFile());

					StreamSource source = new StreamSource(stream);
					XMLEventReader reader = factory.createXMLEventReader(source);

					String nodataValue = null;

					while (reader.hasNext()) {

					    XMLEvent event = reader.nextEvent();

					    if (event.isStartElement()) {

						StartElement startElement = event.asStartElement();

						String startName = startElement.getName().getLocalPart();

						switch (startName) {
						case "noDataValue":
						    nodataValue = readValue(reader);
						    break;
						case "value":

						    Attribute dateTimeAttribute = startElement.getAttributeByName(new QName("dateTimeUTC"));
						    if (dateTimeAttribute == null) {
							dateTimeAttribute = startElement.getAttributeByName(new QName("dateTime"));
						    }
						    if (dateTimeAttribute != null) {
							String date = dateTimeAttribute.getValue();
							String value = readValue(reader);
							if (nodataValue == null || !nodataValue.equals(value)) {
							    Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
							    try {
								BigDecimal v = new BigDecimal(value);
								if (d.isPresent() && v != null) {
								    observation.addPoint(d.get(), v);
								}
							    } catch (Exception e) {
							    }
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

					dataObject.getFile().delete();

				    } catch (Exception e) {
					throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
				    }
				}

			    }

			    if (format.equals("JSON")) {
				if (!first) {
				    writer.write(",");
				}
				writeFeature(writer, observation.getJSONObject());
			    }
			    if (format.equals("CSV")) {
				writeCSVobservation(writer, observation, fields);
			    }
			    first = false;

			}
		    } catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception writing response");

		    }
		    int rest = userSize - tempSize;
		    if (rest > 0 && rest < pageSize) {
			userPage.setSize(rest);
		    }
		    userPage.setStart(userPage.getStart() + pageSize);

		    writer.flush();

		} while (tempSize < userSize && tempSize < resultSet.getCountResponse().getCount()
			&& !resultSet.getResultsList().isEmpty());

		if (format.equals("JSON")) {
		    writer.write("]}"); // result array closed, main JSON closed
		}
		writer.flush();
		writer.close();
		output.close();

	    }

	};

    }

    protected String getSetName() {
	return "member";
    }

    protected void addIdentifier(OutputStreamWriter writer) throws IOException {
	addProperty(writer, "id", "observation collection");

    }

    private void addProperty(OutputStreamWriter writer, String key, String value) throws IOException {
	writer.write("\"" + key + "\":\"" + value + "\",");

    }

    private static String readValue(XMLEventReader reader) {

	String ret = "";
	XMLEvent event = null;
	do {
	    try {
		event = reader.nextEvent();
		if (event instanceof Characters) {
		    Characters cei = (Characters) event;
		    ret += cei.getData();
		}
	    } catch (XMLStreamException e) {
		e.printStackTrace();
	    }

	} while (event != null && !event.isEndElement());

	return ret.trim();
    }

    static XMLInputFactory factory = XMLInputFactory.newInstance();

    public String getObject() {
	return "timeseries";
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new TimeseriesTransformer();
    }

    public void writeFeature(OutputStreamWriter writer, JSONObject feature) throws IOException {

	JSONObject jsonFoi = new JSONObject();
	JSONObject foi = feature.getJSONObject("featureOfInterest");
	if (!foi.has("id")){
	    System.err.println(feature);
	    System.err.println("feature without id: this should not happen");	    
	}else {
	    String href = foi.getString("id");
	    jsonFoi.put("href", href);
	}
	
	feature.put("featureOfInterest", jsonFoi);

	writer.write(feature.toString());

	writer.flush();

    }

    private void writeCSVobservation(OutputStreamWriter writer, JSONTimeseries observation, CSVField... fields) throws IOException {
	JSONArray points = observation.points;

	for (int i = 0; i < points.length(); i++) {
	    JSONObject point = points.getJSONObject(i);
	    JSONObject timeObject = point.getJSONObject("time");
	    String time = timeObject.getString("instant");
	    BigDecimal value = point.getBigDecimal("value");
	    String observedPropertyTitle = observation.getObservedPropertyTitle();
	    if (observedPropertyTitle == null) {
		observedPropertyTitle = "";
	    }
	    String timeseriesId = observation.getId();
	    if (timeseriesId == null) {
		timeseriesId = "";
	    }
	    String platformTitle = observation.getFeatureOfInterest().getSampledFeatureTitle();
	    if (platformTitle == null) {
		platformTitle = "";
	    }
	    int j = 0;
	    for (CSVField field : fields) {
		switch (field) {
		case TIMESERIES_ID:
		    writer.write(timeseriesId);
		    break;
		case OBSERVED_PROPERTY:
		    writer.write(observedPropertyTitle);
		    break;
		case MONITORING_POINT:
		    writer.write(platformTitle);
		    break;
		case DATE_TIME:
		    writer.write(time);
		    break;
		case VALUE:
		    if (value != null) {
			writer.write(value.toString());
		    }
		    break;
		default:
		    break;
		}
		if (j++ == fields.length - 1) {
		    writer.write("\n");
		} else {
		    writer.write("\t");
		}
	    }
	}

    }

    public enum CSVField {

	TIMESERIES_ID("Timeseries identifier"), MONITORING_POINT("Monitoring point"), OBSERVED_PROPERTY("Observed property"), DATE_TIME(
		"Date time"), VALUE("Value");

	private String label;

	CSVField(String label) {
	    this.label = label;
	}
    }

}
