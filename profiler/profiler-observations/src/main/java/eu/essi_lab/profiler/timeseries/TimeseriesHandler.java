package eu.essi_lab.profiler.timeseries;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.hsqldb.types.Charset;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.timeseries.FeatureMapper.Property;
import eu.essi_lab.profiler.timeseries.TimeseriesRequest.APIParameters;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;

import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class TimeseriesHandler extends StreamingRequestHandler {

    private static IDiscoveryStringExecutor executor;
    private static IAccessExecutor accessExecutor;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    static {
	ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
	executor = loader.iterator().next();

	ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	accessExecutor = accessLoader.iterator().next();
    }

    public TimeseriesHandler() {

    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		TimeseriesRequest tr = new TimeseriesRequest(webRequest);

		String properties = tr.getParameterValue(APIParameters.OUTPUT_PROPERTIES);
		List<Property> propertySet = new ArrayList<FeatureMapper.Property>();
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

		String format = tr.getParameterValue(APIParameters.FORMAT);
		DataFormat dataFormat;

		if (format == null) {
		    format = "JSON";
		}

		format = format.toUpperCase();
		CSVField[] fields = null;
		switch (format) {
		case "CSV":
		    dataFormat = DataFormat.WATERML_1_1();
		    fields = new CSVField[] { CSVField.MONITORING_POINT, CSVField.OBSERVED_PROPERTY, CSVField.DATE_TIME, CSVField.VALUE };
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
		OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.UTF_8);

		Page userPage = discoveryMessage.getPage();
		int userSize = userPage.getSize();
		int pageSize = Math.min(userSize, 1000);
		userPage.setSize(pageSize);

		ResultSet<String> resultSet = null;
		int tempSize = 0;
		do {

		    try {
			resultSet = executor.retrieveStrings(discoveryMessage);
			List<String> results = resultSet.getResultsList();
			tempSize += pageSize;

			if (results.isEmpty()) {
			    printErrorMessage(output, "No " + getObject() + " matched");
			    return;
			}

			boolean first = true;
			for (String result : results) {

			    FeatureMapper featureMapper = new FeatureMapper();
			    Feature feature = featureMapper.map(result, propertySet);

			    if (format.equals("JSON")) {
				if (first) {
				    writer.write("{");
				    addProperty(writer, "type", "FeatureCollection");
				    writer.write("\"features\":[");
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

			    Optional<SimpleValueBond> beginBond = tr.getBeginBond();
			    Optional<SimpleValueBond> endBond = tr.getEndBond();
			    String includeValues = tr.getParameterValue(APIParameters.INCLUDE_VALUES);
			    if ((includeValues != null
				    && (includeValues.toLowerCase().equals("yes") || includeValues.toLowerCase().equals("true")))
				    || (beginBond.isPresent() && endBond.isPresent())) {
				Date begin = null;
				Date end = null;
				if (beginBond.isPresent()) {
				    begin = ISO8601DateTimeUtils.parseISO8601ToDate(beginBond.get().getPropertyValue()).get();
				}
				if (endBond.isPresent()) {
				    end = ISO8601DateTimeUtils.parseISO8601ToDate(endBond.get().getPropertyValue()).get();
				}
				AccessMessage accessMessage = new AccessMessage();
				accessMessage.setOnlineId(feature.getOnlineResource());
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
				ResultSet<DataObject> accessResult = accessExecutor.retrieve(accessMessage);
				DataObject dataObject = accessResult.getResultsList().get(0);
				TimeSeriesResponseType trt = null;
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
								feature.getSeries().addPoint(d.get(), v);
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

			    if (format.equals("JSON")) {
				if (!first) {
				    writer.write(",");
				}
				writeFeature(writer, feature.getJSONObject());
			    }
			    if (format.equals("CSV")) {
				writeCSVFeature(writer, feature, fields);
			    }
			    first = false;

			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    int rest = userSize - tempSize;
		    if (rest > 0 && rest < pageSize) {
			userPage.setSize(rest);
		    }
		    userPage.setStart(userPage.getStart() + pageSize);

		} while (tempSize < userSize && tempSize < resultSet.getCountResponse().getCount()
			&& !resultSet.getResultsList().isEmpty());

		if (format.equals("JSON")) {
		    writer.write("]}"); // result array closed, main JSON closed
		}
		writer.flush();
		writer.close();
		output.close();

	    }

	    private void addProperty(OutputStreamWriter writer, String key, String value) throws IOException {
		writer.write("\"" + key + "\":\"" + value + "\",");

	    }

	};

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

    public void printErrorMessage(OutputStream output, String message) throws IOException {
	OutputStreamWriter writer = new OutputStreamWriter(output);
	JSONObject error = new JSONObject();
	error.put("message", message);
	writer.write(error.toString());
	writer.close();
    }

    public void writeFeature(OutputStreamWriter writer, JSONObject feature) throws IOException {
	writer.write(feature.toString());

	writer.flush();

    }

    private void writeCSVFeature(OutputStreamWriter writer, Feature feature, CSVField... fields) throws IOException {
	JSONArray points = feature.getSeries().points;

	for (int i = 0; i < points.length(); i++) {
	    JSONObject point = points.getJSONObject(i);
	    JSONObject timeObject = point.getJSONObject("time");
	    String time = timeObject.getString("instant");
	    BigDecimal value = point.getBigDecimal("value");
	    String observedPropertyTitle = feature.getSeries().getObservedPropertyTitle();
	    String platformTitle = feature.getSeries().getFeatureOfInterest().getSampledFeatureTitle();
	    int j = 0;
	    for (CSVField field : fields) {
		switch (field) {
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

	MONITORING_POINT("Monitoring point"), OBSERVED_PROPERTY("Observed property"), DATE_TIME("Date time"), VALUE("value");

	private String label;

	CSVField(String label) {
	    this.label = label;
	}
    }

}
