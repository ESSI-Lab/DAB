package eu.essi_lab.accessor.sensorthings._1_1.downloader;

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

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.sensorthings._1_1.SensorThingsConnector;
import eu.essi_lab.accessor.sensorthings._1_1.SensorThingsMangler;
import eu.essi_lab.accessor.sensorthings._1_1.mapper.SensorThingsMapper;
import eu.essi_lab.lib.sensorthings._1_1.client.SensorThingsClient;
import eu.essi_lab.lib.sensorthings._1_1.client.request.EntityRef;
import eu.essi_lab.lib.sensorthings._1_1.client.request.FluentSensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.SensorThingsRequest;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SelectOption;
import eu.essi_lab.lib.sensorthings._1_1.client.request.options.SystemQueryOptions;
import eu.essi_lab.lib.sensorthings._1_1.client.response.DataArrayFormatResult;
import eu.essi_lab.lib.sensorthings._1_1.client.response.DataArrayResultItem;
import eu.essi_lab.lib.sensorthings._1_1.client.response.PaginatedResult;
import eu.essi_lab.lib.sensorthings._1_1.model.entities.Datastream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author Fabrizio
 */
public abstract class SensorThingsDownloader extends WMLDataDownloader {

    /**
     *
     */
    protected String linkage;

    /**
     * @return
     */
    public abstract String getSupportedProtocol();

    /**
     * @return
     */
    protected Optional<SystemQueryOptions> getRemoteDescrptorsQueryOptions() {

	SystemQueryOptions options = SystemQueryOptions.//
		get().//
		// selects only stream phenomenonTime
			select(new SelectOption("phenomenonTime"));

	return Optional.of(options);
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	String name = online.getName();

	SensorThingsMangler mangler = new SensorThingsMangler();
	mangler.setMangling(name);

	String streamIdentifer = mangler.getStreamIdentifer();
	Boolean quoteIds = Boolean.valueOf(mangler.getQuoteIdentifiers());

	SensorThingsClient client = null;

	try {
	    client = new SensorThingsClient(new URL(linkage));
	} catch (MalformedURLException e) {
	}

	Optional<SystemQueryOptions> options = getRemoteDescrptorsQueryOptions();

	SensorThingsRequest request = FluentSensorThingsRequest.//
		get().//
		quoteIdentifiers(quoteIds).//
		add(EntityRef.DATASTREAMS, streamIdentifer);

	if (options.isPresent()) {

	    request = FluentSensorThingsRequest.//
		    get().//
		    quoteIdentifiers(quoteIds).//
		    add(EntityRef.DATASTREAMS, streamIdentifer).//
		    with(options.get());
	}

	Datastream stream = client.execute(request).//
		getAddressableEntityResult(Datastream.class).//
		get().//
		getEntities().//
		get(0);

	Optional<String[]> phenomenomTime = SensorThingsMapper.mapStreamPhenomenomTime(stream);
	if (phenomenomTime.isPresent()) {

	    descriptor.setTemporalDimension(//
		    ISO8601DateTimeUtils.parseISO8601ToDate(phenomenomTime.get()[0]).get(), //
		    ISO8601DateTimeUtils.parseISO8601ToDate(phenomenomTime.get()[1]).get());

	}

	ret.add(descriptor);

	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String name = online.getName();

	SensorThingsMangler mangler = new SensorThingsMangler();
	mangler.setMangling(name);

	String streamIdentifer = mangler.getStreamIdentifer();
	Boolean quoteIds = Boolean.valueOf(mangler.getQuoteIdentifiers());

	SensorThingsClient client = null;

	try {
	    client = new SensorThingsClient(new URL(linkage));
	} catch (MalformedURLException e) {
	}

	String begin = null;
	String end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();

	try {
	    ContinueDimension dimension = temporalDimension.getContinueDimension();

	    Number lower = dimension.getLower();
	    Number upper = dimension.getUpper();

	    Date beginDate = new Date(lower.longValue());
	    Date endDate = new Date(upper.longValue());

	    begin = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
	    end = ISO8601DateTimeUtils.getISO8601DateTime(endDate);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	SystemQueryOptions options = SystemQueryOptions.//
		get().//
		filter("phenomenonTime ge " + begin + " and phenomenonTime le " + end);

	List<DataArrayResultItem> resultItems = new ArrayList<>();

	Optional<String> nextLink = Optional.empty();

	do {

	    SensorThingsRequest request = FluentSensorThingsRequest.//
		    get().//
		    quoteIdentifiers(quoteIds).//
		    add(EntityRef.DATASTREAMS, streamIdentifer).//
		    add(EntityRef.OBSERVATIONS).//
		    setDataArrayResultFormat().//
		    with(options);

	    DataArrayFormatResult result = client.//
		    execute(request).//
		    getDataArrayFormatResult().//
		    get();

	    // usually there is only one item in each DataArrayFormatResult
	    resultItems.addAll(result.getResultItems());

	    nextLink = result.getNextLink();

	    if (!nextLink.isEmpty()) {

		Integer skip = PaginatedResult.getSkip(nextLink.get()).get();
		Integer top = PaginatedResult.getTop(nextLink.get()).orElse(100);

		options = options.skip(skip).top(top);
	    }

	} while (nextLink.isPresent());

	try {
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    if (!resultItems.isEmpty()) {

		resultItems.stream().flatMap(item -> getValue(item).stream()).forEach(var -> {
		    try {
			addValue(tsrt, var);
		    } catch (JAXBException ex) {

			GSLoggerFactory.getLogger(getClass()).error(ex);
		    }
		});

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("No results found between [{}/{}]", begin, end);
	    }

	    return tsrt.getDataFile();

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), getClass().getSimpleName() + "_FileCreationError", ex);
	}
    }

    /**
     * @param resultItem
     * @return
     */
    private List<ValueSingleVariable> getValue(DataArrayResultItem resultItem) {

	JSONArray dataArray = resultItem.getDataArray();
	JSONArray components = resultItem.getComponents();

	int phenomenonTimeIndex = getPhenomenonTimeIndex(components);
	int resultIndex = getResultIndex(components);
	int parametersIndex = getComponentIndex(components, "parameters");
	int resultQualityIndex = getComponentIndex(components, "resultQuality");

	List<ValueSingleVariable> out = new ArrayList<>();

	dataArray.forEach(dataElement -> {

	    JSONArray arrayElement = (JSONArray) dataElement;

	    String phenomenonTime = getPhenomenonTime(arrayElement, phenomenonTimeIndex);
	    BigDecimal result = arrayElement.getBigDecimal(resultIndex);

	    ValueSingleVariable v = new ValueSingleVariable();

	    try {
		v.setValue(result);

		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(phenomenonTime).get());

		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		v.setDateTimeUTC(date2);

		if (parametersIndex >= 0) {
		    JSONObject parameters = arrayElement.optJSONObject(parametersIndex);
		    if (parameters != null) {
			Set<String> keys = parameters.keySet();
			for (String key : keys) {
			    key = URLEncoder.encode(key, StandardCharsets.UTF_8);
			    String q = key + ":" + URLEncoder.encode(parameters.getString(key).trim(), StandardCharsets.UTF_8);
			    v.getQualifiers().add(q);
			}
		    }
		}
		if (resultQualityIndex >= 0) {
		    JSONObject resultQuality = arrayElement.optJSONObject(resultQualityIndex);
		    if (resultQuality != null) {
			JSONObject dqStatus = resultQuality.optJSONObject("DQ_Status");
			if (dqStatus != null) {
			    String code = dqStatus.optString("code");
			    if (code != null) {
				v.getQualifiers().add("qualityFlag:" + URLEncoder.encode(code, StandardCharsets.UTF_8));
			    }
			}
		    }
		}

		out.add(v);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	});

	return out;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canConnect() throws GSException {
	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	return new SensorThingsConnector().supports(source);
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(getSupportedProtocol()));
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    /**
     * @param arrayElement
     * @param phenomenonTimeIndex
     * @return
     */
    protected String getPhenomenonTime(JSONArray arrayElement, int phenomenonTimeIndex) {

	return arrayElement.getString(phenomenonTimeIndex);
    }

    /**
     * @param components
     * @return
     */
    private int getPhenomenonTimeIndex(JSONArray components) {

	return getComponentIndex(components, "phenomenonTime");
    }

    /**
     * @param components
     * @return
     */
    private int getResultIndex(JSONArray components) {

	return getComponentIndex(components, "result");
    }

    /**
     * @param components
     * @param component
     * @return
     */
    private int getComponentIndex(JSONArray components, String component) {

	return components.//
		toList().//
		stream().//
		map(e -> e.toString()).//
		collect(Collectors.toList()).//
		indexOf(component);
    }
}
