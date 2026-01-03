package eu.essi_lab.accessor.hmfs;

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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author boldrini
 */
public class HMFSDownloader extends WMLDataDownloader {

    private static final String HMFS_DOWNLOADER_DOWNLOAD_ERROR = "HMFS_DOWNLOADER_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {
	HMFSClient client = new HMFSClient(online.getLinkage());
	try {
	    String date = client.getLastForecast("1005802104", "31407", "4");
	    if (!date.isEmpty()) {
		return true;
	    }
	} catch (Exception e) {
	}
	return false;
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.HMFS.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    HMFSIdentifierMangler mangler = new HMFSIdentifierMangler();
	    mangler.setMangling(name);
	    String date = mangler.getForecastDate();
	    String qualifier = mangler.getQualifier();
	    String series = mangler.getSeries();
	    String station = mangler.getStation();
	    String variable = mangler.getVariable();

	    HMFSClient client = new HMFSClient(online.getLinkage());

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    date = client.getForecastDate();
	    if (date == null) {
		date = client.getLastForecast(station, series, variable);
	    }
	    HMFSSeriesInformation info = client.getSeriesInformation(station, series, variable, date, qualifier);

	    Date begin = ISO8601DateTimeUtils.parseISO8601ToDate(info.getBeginDate()).get();
	    Date end = ISO8601DateTimeUtils.parseISO8601ToDate(info.getEndDate()).get();

	    descriptor.setTemporalDimension(begin, end);

	    //
	    //
	    //

	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);

	    ret.add(descriptor);
	    return ret;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HMFS_DOWNLOADER_DOWNLOAD_ERROR, //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    HMFSIdentifierMangler mangler = new HMFSIdentifierMangler();
	    mangler.setMangling(name);
	    String date = mangler.getForecastDate();
	    String qualifier = mangler.getQualifier();
	    String series = mangler.getSeries();
	    String station = mangler.getStation();
	    String variable = mangler.getVariable();
	    String type = mangler.getType();

	    HMFSClient client = new HMFSClient(online.getLinkage());

	    date = client.getLastForecast(station, series, variable);

	    Date begin = null;
	    Date end = null;
	    DataDimension dimension = descriptor.getTemporalDimension();
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
	    }

	    List<SimpleEntry<Date, BigDecimal>> observations = client.getValues(station, series, variable, date, qualifier, begin, end,
		    type);

	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    for (SimpleEntry<Date, BigDecimal> observation : observations) {

		ValueSingleVariable v = new ValueSingleVariable();

		Date parsed = observation.getKey();
		BigDecimal dValue = observation.getValue();

		v.setValue(dValue);
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(parsed);
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		v.setDateTimeUTC(date2);
		addValue(tsrt, v);
	    }

	    return tsrt.getDataFile();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HMFS_DOWNLOADER_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
