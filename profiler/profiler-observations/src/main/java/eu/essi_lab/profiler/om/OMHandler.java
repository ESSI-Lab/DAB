package eu.essi_lab.profiler.om;

import java.io.ByteArrayOutputStream;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialEntity;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.om.JSONObservation.ObservationType;
import eu.essi_lab.profiler.om.OMRequest.APIParameters;
import eu.essi_lab.profiler.om.ObservationMapper.Property;
import eu.essi_lab.profiler.om.scheduling.OMSchedulerSetting;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.StructureData;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.ft.DsgFeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureCollectionIterator;
import ucar.nc2.ft.point.PointDatasetImpl;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.standard.StandardTrajectoryCollectionImpl;

public class OMHandler extends StreamingRequestHandler {

    protected String viewId;

    public String getGeometryName() {
	if (viewId == null || !(viewId.equals("i-change") || viewId.equals("trigger"))) {
	    return "shape";
	} else {
	    return "geometry";
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

    public OMHandler() {

    }

    /**
     * @param output
     * @param webRequest
     * @throws Exception
     */
    public void handle(OutputStream output, WebRequest webRequest) throws Exception {

	OMRequest request = new OMRequest(webRequest);

	String properties = request.getParameterValue(APIParameters.OUTPUT_PROPERTIES);
	List<Property> propertySet = new ArrayList<ObservationMapper.Property>();
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
	if (useCacheParameter != null
		&& (useCacheParameter.toLowerCase().trim().equals("true") || useCacheParameter.toLowerCase().trim().equals("yes"))) {
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
	Optional<SpatialBond> spatialBond = request.getSpatialBond();
	Double w = null;
	Double s = null;
	Double e = null;
	Double n = null;
	if (spatialBond.isPresent()) {
	    SpatialEntity spatialValue = spatialBond.get().getPropertyValue();
	    if (spatialValue instanceof SpatialExtent) {
		SpatialExtent se = (SpatialExtent) spatialValue;
		w = se.getWest();
		s = se.getSouth();
		e = se.getEast();
		n = se.getNorth();
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
	if (format == null) {
	    format = "JSON";
	}

	format = format.toUpperCase();
	CSVField[] fields = null;
	switch (format) {
	case "CSV":
	    fields = new CSVField[] { CSVField.MONITORING_POINT, CSVField.OBSERVED_PROPERTY, CSVField.TIMESERIES_ID, CSVField.DATE_TIME,
		    CSVField.VALUE, CSVField.UOM, CSVField.LATITUDE, CSVField.LONGITUDE, CSVField.QUALITY };
	    break;
	case "JSON":
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
	    discoveryMessage.setExcludeResourceBinary(true);
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
	SearchAfter searchAfter = null;
	boolean distinctStations = getDistinctStations();
	String lastStation = null;

	Optional<Bond> initial = discoveryMessage.getUserBond();
	String resumption = request.getParameterValue(eu.essi_lab.profiler.om.OMRequest.APIParameters.RESUMPTION_TOKEN);
	if (resumption != null) {
	    List<Object> values = new ArrayList<Object>();
	    String[] split = resumption.split(",");
	    for (String rs : split) {
		values.add(rs);
	    }
	    searchAfter = new SearchAfter(values);
	    discoveryMessage.setSearchAfter(searchAfter);
	}
	do {

	    try {
		if (searchAfter != null) {
		    discoveryMessage.setSearchAfter(searchAfter);
		}
		if (initial.isPresent()) {
		    discoveryMessage.setUserBond(initial.get());
		}
		resultSet = exec(discoveryMessage);

		searchAfter = resultSet.getSearchAfter().isPresent() ? resultSet.getSearchAfter().get() : null;

		List<String> results = resultSet.getResultsList();

		String includeValues = request.getParameterValue(APIParameters.INCLUDE_VALUES);

		if ((includeValues != null && (includeValues.toLowerCase().equals("yes") || includeValues.toLowerCase().equals("true")))) {

		    String asynch = request.getParameterValue(APIParameters.ASYNCH_DOWNLOAD);

		    if (results.size() > 1) {

			if (asynch != null && asynch.toLowerCase().equals("true")) {

			    GSUser user = UserFinder.create().findCurrentUser(webRequest.getServletRequest());

			    GSProperty emailProperty = user.getProperty("email");

			    String email = null;
			    if (emailProperty != null) {
				email = emailProperty.getValue().toString();
			    }
			    String operationId = email + ":" + UUID.randomUUID().toString();

			    S3TransferWrapper s3wrapper = null;

			    if (getDownloadSetting().getDownloadStorage() == DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

			    } else {

				s3wrapper = getS3TransferWrapper();
			    }

			    HttpServletRequest sr = webRequest.getServletRequest();

			    StringBuilder requestURL = new StringBuilder(sr.getRequestURL().toString());
			    String queryString = sr.getQueryString();

			    if (queryString != null) {
				requestURL.append('?').append(queryString);
			    }

			    SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();
			    Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

			    OMSchedulerSetting setting = new OMSchedulerSetting();
			    setting.setRequestURL(requestURL.toString());
			    setting.setOperationId(operationId);
			    setting.setEmail(email);
			    String notifications = request.getParameterValue(APIParameters.E_MAIL_NOTIFICATIONS);
			    if (notifications != null && !notifications.isEmpty()) {
				setting.setEmailNotifications(notifications);
			    }

			    scheduler.schedule(setting);

			    JSONObject msg = new JSONObject();
			    msg.put("operationId", operationId);
			    msg.put("status", "Submitted asynchronous download operation");
			    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

			    status(s3wrapper, operationId, msg);

			    JSONObject json = new JSONObject();
			    json.put("operationId", operationId);
			    json.put("status", "Submitted asynchronous download operation");
			    json.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());

			    printJSON(output, json);

			    return;

			} else {

			    String info = resultSet.getCountResponse().getExpectedLabel();
			    if (info == null) {
				info = "";
			    }
			    printErrorMessage(output,
				    "Requests to download more than one dataset should be handled asynchronously. " + info);
			    return;
			}
		    }
		}

		List<JSONObservation> observations = new ArrayList<>();
		GSLoggerFactory.getLogger(getClass()).info("mapping");
		List<String> identifiers = new ArrayList<>();
		ObservationMapper observationMapper = new ObservationMapper();
		Optional<View> view = discoveryMessage.getView();

		for (String result : results) {
		    JSONObservation observation = observationMapper.map(view, result, propertySet);
		    if (distinctStations) {
			String stationId = observation.getFeatureOfInterest().getId();
			if (lastStation == null || !lastStation.equals(stationId)) {
			    observations.add(observation);
			    lastStation = stationId;
			} else {
			    // skip
			}
		    } else {
			observations.add(observation);
		    }
		}
		for (JSONObservation observation : observations) {
		    identifiers.add(observation.getId());
		}
		GSLoggerFactory.getLogger(getClass()).info("getting data");
		List<DataRecord> datas = new ArrayList<>();
		int h = 0;
		if (useCache) {
		    datas = dataCacheConnector.getRecords(begin, end, identifiers.toArray(new String[] {}));
		}
		tempSize += observations.size();
		GSLoggerFactory.getLogger(getClass()).info("formatting");

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

		for (JSONObservation observation : observations) {

		    ObservationType type = observation.getType();
		    String dataId = observation.getId();

		    // DATA part

		    if ((includeValues != null
			    && (includeValues.toLowerCase().equals("yes") || includeValues.toLowerCase().equals("true")))) {

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
			    accessMessage.setWebRequest(webRequest);
			    accessMessage.setOnlineId(observation.getId());
			    accessMessage.setSources(discoveryMessage.getSources());
			    accessMessage.setCurrentUser(discoveryMessage.getCurrentUser().orElse(null));
			    accessMessage.setDataBaseURI(discoveryMessage.getDataBaseURI());

			    DataDescriptor descriptor = new DataDescriptor();

			    switch (type) {
			    case TimeSeriesObservation:
				descriptor.setDataFormat(DataFormat.WATERML_1_1());
				descriptor.setDataType(DataType.TIME_SERIES);
				break;
			    case TrajectoryObservation:
				descriptor.setDataFormat(DataFormat.NETCDF());
				descriptor.setDataType(DataType.TRAJECTORY);
				break;
			    case SamplingSurfaceObservation:
				descriptor.setDataFormat(DataFormat.NETCDF());
				descriptor.setDataType(DataType.GRID);
				if (w != null && s != null && n != null && e != null) {
				    descriptor.setEPSG4326SpatialDimensions(n, e, s, w);
				}
				break;
			    default:
				break;
			    }
			    descriptor.setCRS(CRS.EPSG_4326());
			    if (begin != null && end != null) {
				descriptor.setTemporalDimension(begin, end);
				descriptor.getTemporalDimension().getContinueDimension().setLowerType(LimitType.CONTAINS);
				descriptor.getTemporalDimension().getContinueDimension().setUpperType(LimitType.CONTAINS);
			    }

			    accessMessage.setTargetDataDescriptor(descriptor);

			    ResultSet<DataObject> accessResult = exec(accessMessage);

			    DataObject dataObject = accessResult.getResultsList().get(0);

			    switch (type) {
			    case TimeSeriesObservation:
				addPointsFromWML(dataObject.getFile(), observation);
				break;
			    case TrajectoryObservation:
				addPointsFromTrajectoryNetCDF(dataObject.getFile(), observation, viewId);
				break;
			    case SamplingSurfaceObservation:
				addPointsFromGridNetCDF(dataObject.getFile(), observation, descriptor);
				break;
			    default:
				break;
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
	    } catch (Exception ee) {
		ee.printStackTrace();
		throw new RuntimeException("Exception writing response");

	    }
	    // int rest = userSize - tempSize;
	    // if (rest > 0 && rest < pageSize) {
	    // userPage.setSize(rest);
	    // }
	    // userPage.setStart(userPage.getStart() + pageSize);

	    writer.flush();

	} while (tempSize < userSize && searchAfter != null);

	if (format.equals("JSON")) {

	    String resumptionToken = "";
	    boolean completed = true;
	    if (searchAfter != null && searchAfter.getValues().isPresent() && !searchAfter.getValues().get().isEmpty()) {
		String rt = "";
		for (Object v : searchAfter.getValues().get()) {
		    rt += v.toString() + ",";
		}
		if (rt.endsWith(",")) {
		    rt = rt.substring(0, rt.length() - 1);
		}
		resumptionToken = ",\"resumptionToken\":\"" + rt + "\"";
		completed = false;
	    }

	    writer.write("],\"completed\":" + completed + "" + resumptionToken + " }"); // result array closed,
											// main JSON closed
	}
	writer.flush();
	writer.close();
	output.close();
    }

    /**
     * @param webRequest
     * @return
     * @throws Exception
     */
    public Optional<JSONObject> getJSONResponse(WebRequest webRequest) {

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	try {

	    handle(outputStream, webRequest);

	    return Optional.of(new JSONObject(outputStream.toString(StandardCharsets.UTF_8)));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return Optional.empty();
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {

	Optional<String> optionalView = webRequest.extractViewId();
	this.viewId = optionalView.isPresent() ? optionalView.get() : null;

	return new StreamingOutput() {

	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {

		try {
		    handle(output, webRequest);
		} catch (Exception e) {

		    e.printStackTrace();
		}
	    }
	};
    }

    protected boolean getDistinctStations() {
	return false;
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
	return new OMTransformer();
    }

    public void writeFeature(OutputStreamWriter writer, JSONObject feature) throws IOException {

	JSONObject jsonFoi = new JSONObject();
	JSONObject foi = feature.getJSONObject("featureOfInterest");
	if (!foi.has("id")) {
	    System.err.println(feature);
	    System.err.println("feature without id: this should not happen");
	} else {
	    String href = foi.getString("id");
	    jsonFoi.put("href", href);
	}

	feature.put("featureOfInterest", jsonFoi);

	writer.write(feature.toString());

	writer.flush();

    }

    private void writeCSVobservation(OutputStreamWriter writer, JSONObservation observation, CSVField... fields) throws IOException {
	JSONArray points = observation.points;
	SimpleEntry<BigDecimal, BigDecimal> latLon = observation.getFeatureOfInterest().getLatLonPoint();
	String lat = "";
	String lon = "";
	if (latLon != null) {
	    lat = latLon.getKey() == null ? "" : latLon.getKey().toString();
	    lon = latLon.getValue() == null ? "" : latLon.getValue().toString();
	}
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
	String uom = observation.getUOM();
	if (uom == null) {
	    uom = "";
	}
	for (int i = 0; i < points.length(); i++) {
	    JSONObject point = points.getJSONObject(i);
	    JSONObject timeObject = point.getJSONObject("time");
	    String time = timeObject.getString("instant");
	    String quality = null;
	    if (point.has("metadata")) {
		JSONObject metadataObject = point.getJSONObject("metadata");
		if (metadataObject.has("quality")) {
		    JSONObject qualityObject = metadataObject.getJSONObject("quality");
		    String term = qualityObject.getString("term");
		    String vocab = qualityObject.getString("vocabulary");
		    String separator = "";
		    if (!vocab.endsWith("/") && !term.startsWith("/")) {
			separator = "/";
		    }
		    quality = vocab + separator + term;
		}
	    }
	    BigDecimal value = null;
	    if (point.has("value")) {
		value = point.getBigDecimal("value");
	    }
	    int j = 0;
	    for (CSVField field : fields) {
		switch (field) {
		case TIMESERIES_ID:
		    writer.write(timeseriesId);
		    break;
		case UOM:
		    writer.write(uom);
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
		case LATITUDE:
		    writer.write(lat);
		    break;
		case LONGITUDE:
		    writer.write(lon);
		    break;
		case QUALITY:
		    if (quality != null) {
			writer.write(quality);
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

	TIMESERIES_ID("Timeseries identifier"), MONITORING_POINT("Monitoring point"), UOM("Units"), OBSERVED_PROPERTY(
		"Observed property"), DATE_TIME(
			"Date time"), VALUE("Value"), LATITUDE("Latitude"), LONGITUDE("Longitude"), QUALITY("Quality");

	private String label;

	CSVField(String label) {
	    this.label = label;
	}
    }

    /**
     * @return
     */
    public static S3TransferWrapper getS3TransferWrapper() {

	String accessKey = ConfigurationWrapper.getDownloadSetting().getS3StorageSetting().getAccessKey().get();
	String secretKey = ConfigurationWrapper.getDownloadSetting().getS3StorageSetting().getSecretKey().get();

	S3TransferWrapper s3wrapper = new S3TransferWrapper();
	s3wrapper.setAccessKey(accessKey);
	s3wrapper.setSecretKey(secretKey);
	s3wrapper.initialize();
	s3wrapper.setACLPublicRead(true);

	return s3wrapper;
    }

    /**
     * @param s3wrapper
     * @param operationId
     * @param json
     * @throws Exception
     */
    public static void status(//
	    S3TransferWrapper s3wrapper, //
	    String operationId, //
	    JSONObject json) throws Exception {

	status(s3wrapper, operationId, "his-central", "data-downloads/" + operationId + "-status.json", json);
    }

    /**
     * @param s3wrapper
     * @param operationId
     * @param bucket
     * @param key
     * @param json
     * @throws Exception
     */
    public static void status(//
	    S3TransferWrapper s3wrapper, //
	    String operationId, //
	    String bucket, //
	    String key, //
	    JSONObject json) throws Exception {

	Path tmpFile = Files.createTempFile(OMHandler.class.getSimpleName(), ".txt");

	FileOutputStream fos = new FileOutputStream(tmpFile.toFile());

	fos.write(json.toString().getBytes(StandardCharsets.UTF_8));
	fos.close();
	s3wrapper.uploadFile(tmpFile.toFile().getAbsolutePath(), bucket, key, "application/json");

	tmpFile.toFile().delete();
    }

    private DownloadSetting getDownloadSetting() {
	return ConfigurationWrapper.getDownloadSetting();
    }

    private void addPointsFromGridNetCDF(File file, JSONObservation observation, DataDescriptor descriptor) {
	NetcdfDataset dataset = null;
	try {

	    dataset = NetcdfDataset.openDataset(file.getAbsolutePath());
	    Variable variable = NetCDFUtils.getGeographicVariables(dataset).get(0);

	    CoordinateAxis yAxis = null;
	    CoordinateAxis xAxis = null;
	    CoordinateAxis1DTime timeAxis = null;

	    Double scaleFactor = null;

	    ucar.nc2.Attribute scaleAttribute = variable.findAttribute("scale_factor");
	    if (scaleAttribute != null) {
		scaleFactor = scaleAttribute.getNumericValue().doubleValue();
	    }
	    Double offset = null;

	    ucar.nc2.Attribute offsetAttribute = variable.findAttribute("add_offset");
	    if (offsetAttribute != null) {
		offset = offsetAttribute.getNumericValue().doubleValue();
	    }
	    Short missing = null;

	    ucar.nc2.Attribute missingAttribute = variable.findAttribute("missing_value");
	    if (missingAttribute != null) {
		missing = missingAttribute.getNumericValue().shortValue();
	    }
	    List<CoordinateAxis> axes = dataset.getCoordinateAxes();
	    int timePosition = -1;
	    int xPosition = -1;
	    int yPosition = -1;
	    for (int i = 0; i < axes.size(); i++) {
		CoordinateAxis axe = axes.get(i);
		AxisType axisType = axe.getAxisType();
		if (axisType != null) {
		    switch (axisType) {
		    case GeoX:
		    case Lon:
			xAxis = axe;
			xPosition = axes.size() - i - 1;
			break;
		    case GeoY:
		    case Lat:
			yAxis = axe;
			yPosition = axes.size() - i - 1;
			break;
		    case Time:
			timeAxis = CoordinateAxis1DTime.factory(dataset, axe, null);
			timePosition = axes.size() - i - 1;
			break;
		    default:
			break;
		    }
		}
	    }

	    int[] shape = variable.getShape();
	    Array array = variable.read();
	    int[] stride = variable.getShape();
	    int[] tmp = variable.getShape();
	    for (int i = 0; i < stride.length; i++) {
		stride[i] = 1;
		tmp[i] = 0;
	    }

	    Array timeArray = timeAxis.read();
	    Array xArray = xAxis.read();
	    Array yArray = yAxis.read();
	    Double xResolution = NetCDFUtils.readResolution(xArray);
	    Double yResolution = NetCDFUtils.readResolution(yArray);

	    DataDimension lat = descriptor.getFirstSpatialDimension();
	    double south = lat.getContinueDimension().getLower().doubleValue();
	    double north = lat.getContinueDimension().getUpper().doubleValue();
	    DataDimension lon = descriptor.getSecondSpatialDimension();
	    double west = lon.getContinueDimension().getLower().doubleValue();
	    double east = lon.getContinueDimension().getUpper().doubleValue();
	    DataDimension userTime = descriptor.getTemporalDimension();
	    long begin = userTime.getContinueDimension().getLower().longValue();
	    long end = userTime.getContinueDimension().getUpper().longValue();
	    double TOLX = xResolution / 2.0 + 0.00000001;
	    double TOLY = yResolution / 2.0 + 0.00000001;
	    for (int i = 0; i < timeAxis.getSize(); i++) {
		long time = timeAxis.getCalendarDate(i).getMillis();
		for (int j = 0; j < xAxis.getSize(); j++) {
		    double x = xArray.getDouble(j);
		    for (int k = 0; k < yAxis.getSize(); k++) {
			double y = yArray.getDouble(k);
			Index index = new Index(shape, stride);
			index.setDim(timePosition, i);
			index.setDim(xPosition, j);
			index.setDim(yPosition, k);
			short value = array.getShort(index);
			if (missing != null && missing.equals(value)) {
			    continue;
			}
			double v = value;
			if (scaleFactor != null) {
			    v = v * scaleFactor;
			}
			if (offset != null) {
			    v = v + offset;
			}
			List<Double> coords = new ArrayList<>();
			coords.add(x);
			coords.add(y);
			if (time >= begin && time <= end) {
			    if (south <= y + TOLY && north >= y - TOLY && east >= x - TOLX && west <= x + TOLX) {
				observation.addPointAndLocation(new Date(time), new BigDecimal(v), coords);
			    }
			}
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

    }

    private void addPointsFromTrajectoryNetCDF(File file, JSONObservation observation, String viewId) {
	FeatureDataset dataset = null;
	NetcdfDataset.setDefaultEnhanceMode(
		Collections.unmodifiableSet(EnumSet.of(Enhance.ApplyScaleOffset, Enhance.CoordSystems, Enhance.ConvertEnums)));
	try {

	    dataset = FeatureDatasetFactoryManager.open(FeatureType.TRAJECTORY, file.getAbsolutePath(), null, null);
	    List<ucar.nc2.Attribute> attributes = dataset.getDataVariables().get(0).getAttributes();
	    for (ucar.nc2.Attribute a : attributes) {
		GSLoggerFactory.getLogger(getClass()).info("Attribute: " + a.getShortName() + " " + a.toString());
	    }

	    String varName = dataset.getDataVariables().get(0).getShortName();

	    PointDatasetImpl fdp = (PointDatasetImpl) dataset;

	    List<DsgFeatureCollection> collections = fdp.getPointFeatureCollectionList();

	    if (collections.get(0) instanceof StandardTrajectoryCollectionImpl) {
		StandardTrajectoryCollectionImpl collection = (StandardTrajectoryCollectionImpl) collections.get(0);

		PointFeatureCollectionIterator iterator = collection.getPointFeatureCollectionIterator();

		while (iterator.hasNext()) {
		    PointFeatureCollection pfc = iterator.next();
		    while (pfc.hasNext()) {
			PointFeature pf = pfc.next();
			StationPointFeature spf = (StationPointFeature) pf;
			double alt = spf.getLocation().getAltitude();
			double lon = spf.getLocation().getLongitude();
			double lat = spf.getLocation().getLatitude();
			long time = spf.getNominalTimeAsCalendarDate().getMillis();
			StructureData data = spf.getFeatureData();
			// List<Member> members = data.getMembers();
			// StructureMembers structureMembers = data.getStructureMembers();
			double value = data.getScalarDouble(varName);
			BigDecimal bigDecValue = new BigDecimal(Math.round(value * 100));
			bigDecValue = bigDecValue.divide(new BigDecimal(100));
			List<Double> coords = new ArrayList<>();
			coords.add(lon);
			coords.add(lat);
			if (viewId == null || !(viewId.equals("i-change") || viewId.equals("trigger"))) {
			    coords.add(alt);
			}
			observation.addPointAndLocation(new Date(time), bigDecValue, coords);
		    }
		}
	    }

	    dataset.close();

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

    }

    private void addPointsFromWML(File file, JSONObservation observation) {
	try {
	    FileInputStream stream = new FileInputStream(file);

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
			    BigDecimal v = null;
			    if (nodataValue == null || !nodataValue.equals(value)) {
				v = new BigDecimal(value);
			    }
			    Attribute qualityAttribute = startElement.getAttributeByName(new QName("qualityControlLevelCode"));
			    String quality = null;
			    if (qualityAttribute != null) {
				quality = qualityAttribute.getValue();
			    }

			    Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
			    try {
				if (d.isPresent()) {

				    List<Double> coord = observation.getFeatureOfInterest().getCoordinates();
				    if (coord != null) {
					observation.addPointAndLocationAndQuality(d.get(), v, coord, quality);
				    } else {
					observation.addPointAndQuality(d.get(), v, quality);
				    }
				}
			    } catch (Exception e) {
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

	    file.delete();

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

    }

    protected void printJSON(OutputStream output, JSONObject json) throws IOException {

	OutputStreamWriter writer = new OutputStreamWriter(output);

	writer.write(json.toString());
	writer.close();
    }

}
