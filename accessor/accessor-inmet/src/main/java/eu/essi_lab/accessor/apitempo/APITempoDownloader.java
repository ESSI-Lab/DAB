package eu.essi_lab.accessor.apitempo;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.apitempo.APITempoData.APITempoDataCode;
import eu.essi_lab.accessor.apitempo.APITempoParameter.APITempoParameterCode;
import eu.essi_lab.accessor.apitempo.APITempoStation.APITempoStationCode;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class APITempoDownloader extends WMLDataDownloader {

    private static final String APITEMPO_GET_REMOTE_DESCRIPTORS_ERROR = "APITEMPO_GET_REMOTE_DESCRIPTORS_ERROR";
    private static final String APITEMPO_DOWNLOAD_ERROR = "APITEMPO_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {
	APITempoClient client = new APITempoClient(online.getLinkage());
	return !client.getStations().isEmpty();
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    @Override
    public List<DataDescriptor> getPreviewRemoteDescriptors(List<DataDescriptor> descriptors) throws GSException {
	List<DataDescriptor> ret = super.getPreviewRemoteDescriptors(descriptors);
	for (DataDescriptor dd : ret) {
	    dd.getTemporalDimension().getContinueDimension().setResolution(null);
	}
	return ret;
    }

    @Override
    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension.getType().equals(DimensionType.TIME)) {
	    Number lower = dataDimension.getContinueDimension().getLower();
	    dataDimension.getContinueDimension().setUpper(lower.longValue());
	} else {
	    super.reduceDimension(dataDimension);
	}
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.APITEMPO.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    APITempoIdentifierMangler mangler = new APITempoIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();
	    APITempoClient client = new APITempoClient(online.getLinkage());
	    APITempoStation station = client.getStation(stationCode);

	    APITempoParameter parameter = client.getStationParameter(stationCode, parameterCode);

	    String parameterMeasurementType = parameter.getValue(APITempoParameterCode.INTERPOLATION);
	    // types.add(parameterMeasurementType);
	    String parameterDescription = parameter.getValue(APITempoParameterCode.NAME);
	    // parameters.add(parameterDescription);
	    String parameterUnits = parameter.getValue(APITempoParameterCode.UNITS);
	    String aggregationPeriod = parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD);
	    String aggregationPeriodUnits = parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD_UNITS);
	    // aggregations.add(aggregationPeriodUnits);
	    String spacingPeriod = parameter.getValue(APITempoParameterCode.TIME_SPACING);
	    String spacingPeriodUnits = parameter.getValue(APITempoParameterCode.TIME_SPACING_UNITS);

	    String stationName = station.getValue(APITempoStationCode.NAME);
	    String stationLatitude = station.getValue(APITempoStationCode.LATITUDE);
	    String stationLongitude = station.getValue(APITempoStationCode.LONGITUDE);
	    String stationAltitude = station.getValue(APITempoStationCode.ELEVATION);
	    String stationOrganization = station.getValue(APITempoStationCode.RESPONSIBLE);
	    String stationWigosCode = station.getValue(APITempoStationCode.WIGOS_ID);

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    descriptor.setEPSG4326SpatialDimensions(Double.valueOf(stationLatitude), Double.valueOf(stationLongitude));
	    if (stationAltitude != null && !stationAltitude.isEmpty()) {
		try {
		    double altDouble = Double.parseDouble(stationAltitude);
		    descriptor.setVerticalDimension(altDouble, altDouble);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Not parsable altitude: " + stationAltitude);
		}
	    }

	    Date begin = ISO8601DateTimeUtils
		    .parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getBeginPosition()).get();

	    Date end;
	    TimeIndeterminateValueType indeterminate = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent()
		    .getIndeterminateEndPosition();
	    if (indeterminate != null && indeterminate.equals(TimeIndeterminateValueType.NOW)) {
		end = new Date();
	    } else {
		end = ISO8601DateTimeUtils
			.parseISO8601ToDate(resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent().getEndPosition()).get();
	    }

	    descriptor.setTemporalDimension(begin, end);
	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    Long oneMonthInMilliseconds = oneDayInMilliseconds * 31l;
	    switch (spacingPeriod.toLowerCase()) {
	    case "month":
		temporalDimension.getContinueDimension().setLowerTolerance(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setResolution(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setResolutionTolerance(oneMonthInMilliseconds);
		break;
	    case "day":
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);
		break;
	    case "hour":
	    default:
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setResolution(oneHourInMilliseconds);
		temporalDimension.getContinueDimension().setResolutionTolerance(oneHourInMilliseconds);
		break;
	    }

	    ret.add(descriptor);
	    return ret;
	    
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    APITEMPO_GET_REMOTE_DESCRIPTORS_ERROR, //
		    e);
	}
    }

    /**
     * @param timeString e.g. "2009-04-21T21:00:00.000-03:00"
     * @return
     */
    public static Date getISO8601Date(String timeString) {
	if (timeString == null) {
	    return null;
	}
	Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(timeString);
	if (optionalDate.isPresent()) {
	    return optionalDate.get();
	}
	return null;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    APITempoIdentifierMangler mangler = new APITempoIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();
	    APITempoClient client = new APITempoClient(online.getLinkage());
	    APITempoStation station = client.getStation(stationCode);

	    APITempoParameter parameter = client.getStationParameter(stationCode, parameterCode);

	    String parameterMeasurementType = parameter.getValue(APITempoParameterCode.INTERPOLATION);
	    // types.add(parameterMeasurementType);
	    String parameterDescription = parameter.getValue(APITempoParameterCode.NAME);
	    // parameters.add(parameterDescription);
	    String parameterUnits = parameter.getValue(APITempoParameterCode.UNITS);
	    String aggregationPeriod = parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD);
	    String aggregationPeriodUnits = parameter.getValue(APITempoParameterCode.AGGREGATION_PERIOD_UNITS);
	    // aggregations.add(aggregationPeriodUnits);
	    String spacingPeriod = parameter.getValue(APITempoParameterCode.TIME_SPACING);
	    String spacingPeriodUnits = parameter.getValue(APITempoParameterCode.TIME_SPACING_UNITS);

	    String stationName = station.getValue(APITempoStationCode.NAME);
	    String stationLatitude = station.getValue(APITempoStationCode.LATITUDE);
	    String stationLongitude = station.getValue(APITempoStationCode.LONGITUDE);
	    String stationAltitude = station.getValue(APITempoStationCode.ELEVATION);
	    String stationOrganization = station.getValue(APITempoStationCode.RESPONSIBLE);
	    String stationWigosCode = station.getValue(APITempoStationCode.WIGOS_ID);

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    DataDimension dimension = descriptor.getTemporalDimension();
	    Date begin = null;
	    Date end = null;
	    String startDate = null;
	    String endDate = null;
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
		startDate = ISO8601DateTimeUtils.getISO8601Date(begin);
		endDate = ISO8601DateTimeUtils.getISO8601Date(end);
	    }

	    // if (startString == null || endString == null) {
	    // startString = ISO8601DateTimeUtils.getISO8601Date(new Date(System.currentTimeMillis() - 24 * 60 * 60 *
	    // 1000L));
	    // endString = ISO8601DateTimeUtils.getISO8601Date(new Date());
	    // }

	    List<APITempoData> datas = client.getData(stationCode, parameterCode, startDate, endDate);

	    for (APITempoData data : datas) {

		Date parsed = data.getDate();

		if (parsed.before(begin) || parsed.after(end)) {
		    continue;
		}

		String dataValue = data.getValue(APITempoDataCode.VALUE);

		ValueSingleVariable v = new ValueSingleVariable();

		BigDecimal dValue;
		if (dataValue != null && !dataValue.isEmpty()) {
		    dataValue = dataValue.replace(",", ".");
		    dValue = new BigDecimal(dataValue);
		} else {
		    dValue = new BigDecimal(APITempoMapper.MISSING_VALUE);
		}
		v.setValue(dValue);
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		c.setTime(parsed);
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		v.setDateTimeUTC(date2);
		addValue(tsrt, v);
		// }
	    }

	    return tsrt.getDataFile();
	} catch (Exception e) {
	    
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    APITEMPO_DOWNLOAD_ERROR, //
		    e);
	}
    }
}
