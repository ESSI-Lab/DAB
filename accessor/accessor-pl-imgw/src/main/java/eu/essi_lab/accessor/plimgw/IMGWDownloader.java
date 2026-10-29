package eu.essi_lab.accessor.plimgw;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;
import org.json.JSONObject;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
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
 * Downloads the daily values of the archive and the latest observation of the real time API of an IMGW series (see
 * {@link IMGWClient})
 *
 * @author boldrini
 */
public class IMGWDownloader extends WMLDataDownloader {

    private String linkage;

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	SimpleEntry<Date, Date> extent = getExtent();
	if (extent != null) {
	    descriptor.setTemporalDimension(extent.getKey(), extent.getValue());
	}

	ret.add(descriptor);
	return ret;
    }

    /**
     * @return the harvested begin and the current latest observation of the real time API (falling back to the harvested end)
     */
    private SimpleEntry<Date, Date> getExtent() {

	Date begin = null;
	Date end = null;

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent != null) {
	    begin = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getBeginPosition()).orElse(null);
	    end = extent.isEndPositionIndeterminate() ? new Date()
		    : ISO8601DateTimeUtils.parseISO8601ToDate(extent.getEndPosition()).orElse(null);
	}

	IMGWEntity series = getSeries();
	if (series != null && series.getProperty().isPresent()) {
	    try {
		Optional<JSONObject> station = new IMGWClient(online.getLinkage()).retrieveStation(series.getStationCode());
		Optional<Date> latest = station.flatMap(s -> IMGWClient.getLatestTime(s, series.getProperty().get()));
		if (latest.isPresent()) {
		    end = latest.get();
		    if (begin == null || latest.get().before(begin)) {
			begin = latest.get();
		    }
		}
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve latest observation of {}", series.getSeriesId());
	    }
	}

	if (begin == null || end == null) {
	    return null;
	}

	return new SimpleEntry<>(begin, end);
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	IMGWEntity series = getSeries();
	if (series == null) {
	    GSLoggerFactory.getLogger(getClass()).warn("Invalid series identifier {}", online.getName());
	    return null;
	}

	// the period of the series in the archive is needed to read only the relevant files (null if not in the archive)
	IMGWEntity harvested = getHarvestedSeries();
	if (harvested != null) {
	    for (String key : new String[] { IMGWClient.ARCHIVE_BEGIN, IMGWClient.ARCHIVE_END }) {
		String value = harvested.getString(key);
		series.getObject().put(key, value.isEmpty() ? JSONObject.NULL : value);
	    }
	}

	Date begin = null;
	Date end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null) {
	    try {
		ContinueDimension dimension = temporalDimension.getContinueDimension();
		begin = new Date(dimension.getLower().longValue());
		end = new Date(dimension.getUpper().longValue());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	if (begin == null || end == null) {
	    SimpleEntry<Date, Date> extent = getExtent();
	    if (extent == null) {
		GSLoggerFactory.getLogger(getClass()).warn("No temporal extent available for series {}", series.getSeriesId());
		return null;
	    }
	    begin = begin == null ? extent.getKey() : begin;
	    end = end == null ? extent.getValue() : end;
	}

	TreeMap<Date, BigDecimal> values = new IMGWClient(online.getLinkage()).retrieveObservations(series, begin, end);

	try {

	    if (!values.isEmpty()) {

		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		for (Entry<Date, BigDecimal> entry : values.entrySet()) {

		    ValueSingleVariable v = new ValueSingleVariable();
		    v.setValue(entry.getValue());

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(entry.getKey());
		    XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date);
		    addValue(tsrt, v);
		}

		return tsrt.getDataFile();
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("No results found between [{}/{}]", begin, end);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @return
     */
    private IMGWEntity getSeries() {

	return Optional.ofNullable(online.getName()).map(IMGWClient::parseSeriesId).orElse(null);
    }

    /**
     * @return the harvested series, from the original metadata of the resource, if available
     */
    private IMGWEntity getHarvestedSeries() {

	try {
	    if (resource.getOriginalMetadata() != null && resource.getOriginalMetadata().getMetadata() != null) {
		return new IMGWEntity(resource.getOriginalMetadata().getMetadata());
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to read the original metadata: {}", e.getMessage());
	}

	return null;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {

	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.IMGW.getCommonURN())
		&& IMGWClient.parseSeriesId(online.getName()) != null);
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
	source.setEndpoint(linkage);
	return new IMGWConnector().supports(source);
    }
}
