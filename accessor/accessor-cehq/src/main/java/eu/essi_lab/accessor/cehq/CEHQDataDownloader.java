package eu.essi_lab.accessor.cehq;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.cehq.CEHQClient.CEHQProperty;
import eu.essi_lab.accessor.cehq.CEHQClient.CEHQVariable;
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

public class CEHQDataDownloader extends WMLDataDownloader {

    private static final String CEHQ_DATA_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR = "CEHQ_DATA_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR";
    private static final String CEHQ_DATA_DOWNLOADER_DOWNLOAD_ERROR = "CEHQ_DATA_DOWNLOADER_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {

	return (online.getLinkage().contains("www.cehq.gouv.qc.ca/hydrometrie"));
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    // @Override
    // public List<DataDescriptor> getPreviewRemoteDescriptors(List<DataDescriptor> descriptors) throws GSException {
    // List<DataDescriptor> ret = super.getPreviewRemoteDescriptors(descriptors);
    // for (DataDescriptor dd : ret) {
    // dd.getTemporalDimension().getContinueDimension().setResolution(null);
    // }
    // return ret;
    // }

    @Override
    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension.getType().equals(DimensionType.TIME)) {
	    Number lower = dataDimension.getContinueDimension().getLower();
	    dataDimension.getContinueDimension().setUpper(lower.longValue());
	    dataDimension.getContinueDimension().setSize(1l);
	} else {
	    super.reduceDimension(dataDimension);
	}
    }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.CEHQ.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {
	try {
	    String name = online.getName();
	    CEHQIdentifierMangler mangler = new CEHQIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    CEHQVariable var = CEHQVariable.decode(parameterCode);

	    CEHQClient client = new CEHQClient();
	    Map<CEHQProperty, String> properties = client.getStationProperties(stationCode);

	    String parameterDescription = var.getLabel();
	    // parameters.add(parameterDescription);
	    String parameterUnits = var.getUnits();

	    String stationName = properties.get(CEHQProperty.STATION_NAME);
	    String stationLatitude = properties.get(CEHQProperty.LAT);
	    String stationLongitude = properties.get(CEHQProperty.LON);
	    String stationOrganization = "Centre d'expertise hydrique du Québec (CEHQ)";

	    List<DataDescriptor> ret = new ArrayList<>();
	    DataDescriptor descriptor = new DataDescriptor();

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    descriptor.setEPSG4326SpatialDimensions(Double.valueOf(stationLatitude), Double.valueOf(stationLongitude));

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
	    // switch (spacingPeriod.toLowerCase()) {
	    // case "month":
	    // temporalDimension.getContinueDimension().setLowerTolerance(oneMonthInMilliseconds);
	    // temporalDimension.getContinueDimension().setUpperTolerance(oneMonthInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneMonthInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneMonthInMilliseconds);
	    // break;
	    // case "day":
	    // temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);
	    // break;
	    // case "hour":
	    // default:
	    // temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolution(oneHourInMilliseconds);
	    // temporalDimension.getContinueDimension().setResolutionTolerance(oneHourInMilliseconds);
	    // break;
	    // }

	    ret.add(descriptor);
	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CEHQ_DATA_DOWNLOADER_GET_REMOTE_DESCRIPTORS_ERROR, e//
	    );
	}
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	try {
	    String name = online.getName();
	    CEHQIdentifierMangler mangler = new CEHQIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();

	    CEHQVariable var = CEHQVariable.decode(parameterCode);

	    CEHQClient client = new CEHQClient();
	    Map<CEHQProperty, String> properties = client.getStationProperties(stationCode);

	    // types.add(parameterMeasurementType);
	    String parameterDescription = var.getLabel();
	    // parameters.add(parameterDescription);
	    String parameterUnits = var.getUnits();
	    // aggregations.add(aggregationPeriodUnits);

	    String stationName = properties.get(CEHQProperty.STATION_NAME);
	    String stationLatitude = properties.get(CEHQProperty.LAT);
	    String stationLongitude = properties.get(CEHQProperty.LON);
	    String stationOrganization = "Centre d'expertise hydrique du Québec (CEHQ)";

	    ObjectFactory factory = new ObjectFactory();
	    TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

	    DataDimension dimension = descriptor.getTemporalDimension();
	    Date begin = null;
	    Date end = null;
	    Integer startYear = null;
	    Integer endYear = null;
	    if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		ContinueDimension sizedDimension = dimension.getContinueDimension();
		begin = new Date(sizedDimension.getLower().longValue());
		end = new Date(sizedDimension.getUpper().longValue());
		startYear = getYear(begin);
		endYear = getYear(end);
	    }

	    for (int year = startYear; year <= endYear; year++) {

		CEHQData data = client.getData(stationCode, var, year);

		List<SimpleEntry<Date, BigDecimal>> values = data.getValues();
		for (SimpleEntry<Date, BigDecimal> value : values) {
		    Date parsed = value.getKey();

		    if (parsed.before(begin) || parsed.after(end)) {
			continue;
		    }
		    ValueSingleVariable v = new ValueSingleVariable();

		    BigDecimal dValue = value.getValue();
		    v.setValue(dValue);
		    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    c.setTime(parsed);
		    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		    v.setDateTimeUTC(date2);
		    addValue(tsrt, v);
		}
	    }

	    return tsrt.getDataFile();
	    
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CEHQ_DATA_DOWNLOADER_DOWNLOAD_ERROR, //
		    e//
	    );
	}

    }

    private Integer getYear(Date date) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	return calendar.get(Calendar.YEAR);
    }

}
