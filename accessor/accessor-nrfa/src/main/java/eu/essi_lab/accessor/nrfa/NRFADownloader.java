package eu.essi_lab.accessor.nrfa;

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
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension.LimitType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

/**
 * @author boldrini
 */
public class NRFADownloader extends WMLDataDownloader {

    private static final String NRFA_DOWNLOADER_ERROR = "NRFA_DOWNLOADER_ERROR";

    @Override
    public boolean canConnect() {
	return online.getLinkage().contains(CommonNameSpaceContext.NRFA_URI);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.NRFA.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    NRFAIdentifierMangler mangler = new NRFAIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    NRFAClient client = new NRFAClient(online.getLinkage());

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    StationInfo station = client.getStationInfo(stationCode);

	    List<ParameterInfo> parameters = station.getParameterInfos();
	    ParameterInfo info = null;
	    for (ParameterInfo parameter : parameters) {
		if (parameter.getIdentifier().equals(parameterCode)) {
		    info = parameter;
		}
	    }

	    Date begin = info.getBegin();
	    Date end = info.getEnd();

	    descriptor.setTemporalDimension(begin, end);
	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);

	    ret.add(descriptor);
	    return ret;
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NRFA_DOWNLOADER_ERROR, //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    NRFAIdentifierMangler mangler = new NRFAIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    NRFAClient client = new NRFAClient(online.getLinkage());

	    StationInfo station = client.getStationInfo(stationCode);

	    List<ParameterInfo> parameters = station.getParameterInfos();
	    ParameterInfo info = null;
	    for (ParameterInfo parameter : parameters) {
		if (parameter.getIdentifier().equals(parameterCode)) {
		    info = parameter;
		}
	    }

	    DataDimension dimension = descriptor.getTemporalDimension();
	    Date begin = null;
	    Date end = null;
	    if (dimension != null && dimension.getContinueDimension().getUom() != null
		    && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension continueDimension = dimension.getContinueDimension();

		DataDescriptor remoteDescriptor = null;

		Number lower = continueDimension.getLower();
		LimitType lowerType = continueDimension.getLowerType();

		Number upper = continueDimension.getUpper();
		LimitType upperType = continueDimension.getUpperType();

		if (lowerType == null || !lowerType.equals(LimitType.ABSOLUTE) || //
			upperType == null || !upperType.equals(LimitType.ABSOLUTE)) {
		    // the remote descriptor is retrieved only in case an absolute value
		    // has not been provided
		    remoteDescriptor = getRemoteDescriptors().get(0);
		}

		lower = getActualLimit(lower, lowerType, remoteDescriptor);
		begin = new Date(lower.longValue());

		upper = getActualLimit(upper, upperType, remoteDescriptor);
		end = new Date(upper.longValue());
	    }

	    if (begin == null) {
		begin = info.getBegin();
	    }
	    if (end == null) {
		end = info.getEnd();
	    }

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    List<SimpleEntry<Date, BigDecimal>> datas = client.getValues(stationCode, parameterCode, begin, end);

	    for (SimpleEntry<Date, BigDecimal> data : datas) {
		ValueSingleVariable v = new ValueSingleVariable();

		Date parsed = data.getKey();
		BigDecimal dValue = data.getValue();

		v.setValue(dValue);
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(parsed);
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		v.setDateTimeUTC(date2);
		addValue(tsrt, v);
	    }

	   return tsrt.getDataFile();
	   
	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NRFA_DOWNLOADER_ERROR, //
		    e);
	}

    }

    private Number getActualLimit(Number limit, LimitType limitType, DataDescriptor remoteDescriptor) {
	switch (limitType) {
	case MINIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getLower();
	case MAXIMUM:
	    return remoteDescriptor.getTemporalDimension().getContinueDimension().getUpper();
	case ABSOLUTE:
	default:
	    return limit;
	}
    }

}
