package eu.essi_lab.accessor.depegelonline;

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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
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
public class PegelonlineDownloader extends WMLDataDownloader {

    private String linkage;

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setCRS(CRS.EPSG_4326());

	String timeseriesId = online.getName();
	PegelonlineClient client = new PegelonlineClient(online.getLinkage());

	SimpleEntry<Date, Date> extent = client.retrieveMeasurementExtent(timeseriesId);
	if (extent != null) {
	    descriptor.setTemporalDimension(extent.getKey(), extent.getValue());
	}

	ret.add(descriptor);
	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	String timeseriesId = online.getName();
	Date begin = null;
	Date end = null;

	DataDimension temporalDimension = descriptor.getTemporalDimension();
	if (temporalDimension != null) {
	    try {
		ContinueDimension dimension = temporalDimension.getContinueDimension();
		Number lower = dimension.getLower();
		Number upper = dimension.getUpper();
		begin = new Date(lower.longValue());
		end = new Date(upper.longValue());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	if (begin == null || end == null) {
	    PegelonlineClient client = new PegelonlineClient(online.getLinkage());
	    SimpleEntry<Date, Date> extent = client.retrieveMeasurementExtent(timeseriesId);
	    if (extent != null) {
		if (begin == null) {
		    begin = extent.getKey();
		}
		if (end == null) {
		    end = extent.getValue();
		}
	    }
	}

	if (begin == null || end == null) {
	    GSLoggerFactory.getLogger(getClass()).warn("No temporal extent available for timeseries {}", timeseriesId);
	    return null;
	}

	PegelonlineClient client = new PegelonlineClient(online.getLinkage());
	List<PegelonlineEntity> values = client.retrieveMeasurements(timeseriesId, begin, end);

	try {
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    if (!values.isEmpty()) {
		for (PegelonlineEntity entity : values) {
		    String phenomenonTime = entity.getObject().optString(PegelonlineClient.MEASUREMENT_TIMESTAMP, null);
		    if (phenomenonTime == null || phenomenonTime.isEmpty()) {
			continue;
		    }
		    double result = entity.getObject().getDouble(PegelonlineClient.MEASUREMENT_VALUE);

		    ValueSingleVariable v = new ValueSingleVariable();
		    v.setValue(new BigDecimal(result));

		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(ISO8601DateTimeUtils.parseISO8601ToDate(phenomenonTime).get());
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
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

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.PEGELONLINE.getCommonURN()));
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
	return new PegelonlineConnector().supports(source);
    }
}
