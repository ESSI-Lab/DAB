package eu.essi_lab.accessor.mch;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.mch.MCHConnector.Resolution;
import eu.essi_lab.accessor.mch.datamodel.MCHAvailability;
import eu.essi_lab.accessor.mch.datamodel.MCHStation;
import eu.essi_lab.accessor.mch.datamodel.MCHValue;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
public class MCHDownloader extends WMLDataDownloader {

    private static final String MCH_DOWNLOADER_ERROR = "MCH_DOWNLOADER_ERROR";

    @Override
    public boolean canConnect() {
	return online.getProtocol() != null && online.getProtocol().equals(NetProtocols.MCH.getCommonURN());
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.MCH.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    MCHIdentifierMangler mangler = new MCHIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getStationId();
	    String parameterCode = mangler.getVariableName();
	    String resolution = mangler.getResolution();

	    MCHClient client = new MCHClient(online.getLinkage());

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    MCHStation station = client.getStationById(stationCode);
	    List<MCHAvailability> avails = client.getAvailability(station.getStationName());
	    MCHAvailability availability = null;
	    for (MCHAvailability avail : avails) {
		if (avail.getVariable().equals(parameterCode)) {
		    availability = avail;
		    break;
		}
	    }
	    if (availability == null) {
		return null;
	    }

	    Date begin = availability.getStartDate();
	    Date end = availability.getEndDate();

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

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MCH_DOWNLOADER_ERROR, //
		    e);
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    MCHIdentifierMangler mangler = new MCHIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getStationId();
	    String parameterCode = mangler.getVariableName();
	    Resolution resolution = Resolution.decode(mangler.getResolution());

	    MCHClient client = new MCHClient(online.getLinkage());

	    MCHStation station = client.getStationById(stationCode);
	    List<MCHAvailability> avails = client.getAvailability(station.getStationName());
	    MCHAvailability availability = null;
	    for (MCHAvailability avail : avails) {
		if (avail.getVariable().equals(parameterCode)) {
		    availability = avail;
		    break;
		}
	    }
	    if (availability == null) {
		return null;
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
		begin = availability.getStartDate();
	    }
	    if (end == null) {
		end = availability.getEndDate();
	    }

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesResponseType tsrt = getTimeSeriesTemplate();

	    List<MCHValue> datas;
	    if (resolution.equals(Resolution.DAILY)) {
		datas = client.getDailyData(stationCode, parameterCode, begin, end);
	    } else {
		datas = client.getDetailData(stationCode, parameterCode, begin, end);
	    }

	    for (MCHValue data : datas) {
		ValueSingleVariable v = new ValueSingleVariable();

		Date parsed = data.getDate();
		BigDecimal dValue = data.getValue();

		if (parsed.after(begin) && parsed.before(end)) {
		    v.setValue(dValue);
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(parsed);
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);
		}
	    }

	    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
	    tmpFile.deleteOnExit();
	    JAXBWML.getInstance().marshal(response, tmpFile);

	    return tmpFile;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    MCH_DOWNLOADER_ERROR, //
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
