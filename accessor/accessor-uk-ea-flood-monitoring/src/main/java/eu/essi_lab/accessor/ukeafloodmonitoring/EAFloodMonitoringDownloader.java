package eu.essi_lab.accessor.ukeafloodmonitoring;

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
 * @author boldrini
 */
public class EAFloodMonitoringDownloader extends WMLDataDownloader {

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
     * @return the current rolling window of the readings (not before the harvested begin), ending at the current latest reading
     *         (falling back to the harvested end)
     */
    private SimpleEntry<Date, Date> getExtent() {

	Date now = new Date();
	Date begin = EAFloodMonitoringClient.getWindowBegin(now);
	Date end = null;

	TemporalExtent extent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	if (extent != null) {
	    Optional<Date> harvestedBegin = ISO8601DateTimeUtils.parseISO8601ToDate(extent.getBeginPosition());
	    if (harvestedBegin.isPresent() && harvestedBegin.get().after(begin)) {
		begin = harvestedBegin.get();
	    }
	    end = extent.isEndPositionIndeterminate() ? now : ISO8601DateTimeUtils.parseISO8601ToDate(extent.getEndPosition()).orElse(null);
	}

	String seriesId = online.getName();
	try {
	    Optional<Date> latest = new EAFloodMonitoringClient(online.getLinkage()).retrieveLatestObservation(seriesId);
	    if (latest.isPresent()) {
		end = latest.get();
	    }
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve latest observation of {}", seriesId);
	}

	if (end == null || end.before(begin)) {
	    return null;
	}

	return new SimpleEntry<>(begin, end);
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String seriesId = online.getName();
	if (seriesId == null || seriesId.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Missing series identifier");
	    return null;
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
		GSLoggerFactory.getLogger(getClass()).warn("No temporal extent available for series {}", seriesId);
		return null;
	    }
	    begin = begin == null ? extent.getKey() : begin;
	    end = end == null ? extent.getValue() : end;
	}

	TreeMap<Date, BigDecimal> values = new EAFloodMonitoringClient(online.getLinkage()).retrieveObservations(seriesId, begin, end);

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

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {

	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.EA_FLOOD_MONITORING.getCommonURN())
		&& online.getName() != null && !online.getName().isEmpty());
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
	return new EAFloodMonitoringConnector().supports(source);
    }
}
