package eu.essi_lab.accessor.rasaqm;

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
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.SiteCode;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.TsValuesSingleVariableType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.VariableInfoType.VariableCode;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.lib.net.time.TimeZoneInfo;
import eu.essi_lab.lib.net.time.TimeZoneUtils;
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

public class RasaqmDownloader extends DataDownloader {

    private static final String RASAQM_DOWNLOAD_ERROR = "RASAQM_DOWNLOAD_ERROR";
    private static final String RASAQM_EMPTY_DATASET = "RASAQM_EMPTY_DATASET";

    @Override
    public boolean canDownload() {
	return getOnline().getLinkage().contains("rasaqm");
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public boolean canConnect() throws GSException {
	RasaqmClient client = new RasaqmClient();
	try {
	    return !client.getParameters().isEmpty();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return false;
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    Date end = new Date();
	    Date begin = new Date(end.getTime() - 1000 * 60 * 60 * 24 * 30l); // last 30 days by default

	    descriptor.setTemporalDimension(begin, end);
	    descriptor.getTemporalDimension().getContinueDimension().setLowerTolerance(1000 * 60 * 60 * 24l);
	    descriptor.getTemporalDimension().getContinueDimension().setUpperTolerance(1000 * 60 * 60 * 24l);
	    ret.add(descriptor);
	    return ret;

	} catch (

	Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RASAQM_DOWNLOAD_ERROR, //
		    e);

	}

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();

	    RasaqmIdentifierMangler mangler = new RasaqmIdentifierMangler();
	    if (name != null) {
		mangler.setMangling(name);
		String parameterId = mangler.getParameterIdentifier();
		String stationId = mangler.getPlatformIdentifier();
		BigDecimal latitude = new BigDecimal(mangler.getLatitude());
		BigDecimal longitude = new BigDecimal(mangler.getLongitude());
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

		RasaqmClient client = new RasaqmClient();
		TimeZoneUtils tzu = new TimeZoneUtils();
		TimeZoneInfo timeZoneInfo = tzu.getTimeZoneInfo(latitude, longitude);
		RasaqmDataset dataset = client.parseData(parameterId, begin, end, timeZoneInfo.getTimezone());

		if (dataset.isEmpty()) {

		    throw GSException.createException(//
			    getClass(), //
			    "Empty dataset", //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_ERROR, //

			    RASAQM_EMPTY_DATASET);
		}

		RasaqmSeries series = dataset.getSeries(stationId);

		List<RasaqmData> data = series.getData();

		ObjectFactory factory = new ObjectFactory();
		TimeSeriesResponseType tsrt = new TimeSeriesResponseType();
		TimeSeriesType timeSeries = new TimeSeriesType();
		VariableInfoType variableType = new VariableInfoType();
		variableType.setVariableName(parameterId);
		VariableCode variableCode = new VariableCode();
		variableCode.setValue(parameterId);
		variableType.getVariableCode().add(variableCode);
		timeSeries.setVariable(variableType);

		TsValuesSingleVariableType value = new TsValuesSingleVariableType();

		data.sort(new Comparator<RasaqmData>() {

		    @Override
		    public int compare(RasaqmData o1, RasaqmData o2) {
			return o1.getDate().compareTo(o2.getDate());
		    }
		});

		for (RasaqmData rd : data) {
		    try {
			ValueSingleVariable v = new ValueSingleVariable();
			BigDecimal dataValue = rd.getValue();
			v.setValue(dataValue);
			GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			c.setTime(rd.getDate());
			XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			v.setDateTimeUTC(date2);
			value.getValue().add(v);

		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}

		timeSeries.getValues().add(value);
		tsrt.getTimeSeries().add(timeSeries);

		SiteInfoType siteInfo = new SiteInfoType();
		SiteCode siteCode = new SiteCode();
		siteCode.setValue(stationId);
		siteInfo.getSiteCode().add(siteCode);
		siteInfo.setSiteName(series.getStationName());
		timeSeries.setSourceInfo(siteInfo);

		String unitsString = series.getUnits();
		UnitsType units = new UnitsType();
		units.setUnitName(unitsString);
		units.setUnitCode(unitsString);
		units.setUnitAbbreviation(unitsString);
		units.setUnitDescription(unitsString);

		variableType.setUnit(units);
		JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
		tmpFile.deleteOnExit();
		JAXBWML.getInstance().marshal(response, tmpFile);

		return tmpFile;

	    }

	    return null;
	    
	}catch(GSException gse) {

	    throw gse;
	    
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading ");

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RASAQM_DOWNLOAD_ERROR, //
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
