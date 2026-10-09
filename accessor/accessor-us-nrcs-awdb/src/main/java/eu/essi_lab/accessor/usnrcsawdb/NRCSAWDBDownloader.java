package eu.essi_lab.accessor.usnrcsawdb;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
 * Downloads AWDB timeseries as WaterML 1.1, converting the values to metric units
 *
 * @author boldrini
 */
public class NRCSAWDBDownloader extends WMLDataDownloader {

    private JSONObject station;

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	Optional<NRCSAWDBTimeseriesId> id = NRCSAWDBTimeseriesId.parse(online.getName());
	Optional<JSONObject> optionalStation = id.isPresent() ? getStation(id.get()) : Optional.empty();

	if (optionalStation.isPresent()) {

	    JSONObject stationElement = optionalStation.get().getJSONArray(NRCSAWDBClient.STATION_ELEMENTS).getJSONObject(0);
	    double timeZone = getTimeZone(optionalStation.get());

	    Optional<Date> begin = NRCSAWDBClient.parseApiDate(stationElement.optString(NRCSAWDBClient.ELEMENT_BEGIN_DATE), timeZone);
	    Optional<Date> end = NRCSAWDBClient.parseApiDate(stationElement.optString(NRCSAWDBClient.ELEMENT_END_DATE), timeZone);

	    if (begin.isPresent()) {
		Date now = new Date();
		Date endDate = end.isEmpty() || end.get().after(now) ? now : end.get();
		descriptor.setTemporalDimension(begin.get(), endDate);
	    }
	}

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	Optional<NRCSAWDBTimeseriesId> optionalId = NRCSAWDBTimeseriesId.parse(online.getName());
	if (optionalId.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("Invalid AWDB timeseries identifier: {}", online.getName());
	    return null;
	}

	NRCSAWDBTimeseriesId id = optionalId.get();

	Date begin = null;
	Date end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null && temporalDimension.getContinueDimension() != null) {
	    ContinueDimension dimension = temporalDimension.getContinueDimension();
	    if (dimension.getLower() != null) {
		begin = new Date(dimension.getLower().longValue());
	    }
	    if (dimension.getUpper() != null) {
		end = new Date(dimension.getUpper().longValue());
	    }
	}

	if (begin == null || end == null) {
	    List<DataDescriptor> remote = getRemoteDescriptors();
	    DataDimension remoteTime = remote.get(0).getTemporalDimension();
	    if (remoteTime == null) {
		GSLoggerFactory.getLogger(getClass()).warn("No temporal extent available for timeseries {}", id);
		return null;
	    }
	    if (begin == null) {
		begin = new Date(remoteTime.getContinueDimension().getLower().longValue());
	    }
	    if (end == null) {
		end = new Date(remoteTime.getContinueDimension().getUpper().longValue());
	    }
	}

	double timeZone = getStation(id).map(this::getTimeZone).orElse(0.0);

	NRCSAWDBClient client = new NRCSAWDBClient(online.getLinkage());
	Optional<JSONObject> data = client.retrieveData(id, begin, end, timeZone);

	Map<String, JSONObject> elements = client.retrieveElements();
	JSONObject element = elements.get(id.getElementCode());
	String metricUnit = element == null ? null : element.optString(NRCSAWDBClient.REFERENCE_METRIC_UNIT, null);

	try {
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    if (data.isPresent()) {

		String storedUnit = data.get().getJSONObject(NRCSAWDBClient.RECORD_STATION_ELEMENT).optString(NRCSAWDBClient.ELEMENT_STORED_UNIT,
			null);

		JSONArray values = data.get().optJSONArray("values");

		for (int i = 0; values != null && i < values.length(); i++) {

		    JSONObject value = values.getJSONObject(i);

		    Optional<BigDecimal> number = NRCSAWDBClient.parseValue(value);
		    Optional<Date> date = NRCSAWDBClient.parseValueDate(value, timeZone);
		    if (number.isEmpty() || date.isEmpty()) {
			continue;
		    }

		    ValueSingleVariable v = new ValueSingleVariable();
		    v.setValue(NRCSAWDBUnits.convert(number.get(), storedUnit, metricUnit));

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(date.get());
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("No results found for {} between [{}/{}]", id, begin, end);
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {

	super.setOnlineResource(resource, onlineResourceId);
	this.station = null;
    }

    /**
     * @param id
     * @return the station with its station element, retrieved once
     */
    private Optional<JSONObject> getStation(NRCSAWDBTimeseriesId id) {

	if (station == null) {
	    station = new NRCSAWDBClient(online.getLinkage()).retrieveStationElement(id).orElse(null);
	}

	return Optional.ofNullable(station);
    }

    /**
     * @param station
     * @return the station data time zone, in hours; 0 if not declared (e.g. snow courses)
     */
    private double getTimeZone(JSONObject station) {

	return station.optDouble(NRCSAWDBClient.STATION_DATA_TIME_ZONE, 0);
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.NRCS_AWDB.getCommonURN()));
    }

    @Override
    public boolean canSubset(String dimensionName) {

	if (dimensionName == null) {
	    return false;
	}

	return DataDescriptor.TIME_DIMENSION_NAME.equalsIgnoreCase(dimensionName);
    }

    @Override
    public boolean canConnect() throws GSException {

	GSSource source = new GSSource();
	source.setEndpoint(online.getLinkage());
	return new NRCSAWDBConnector().supports(source);
    }
}
