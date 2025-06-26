package eu.essi_lab.accessor.bndmet.download;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.essi.JAXBWML;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.bndmet.BNDMETClient;
import eu.essi_lab.accessor.bndmet.BNDMETIdentifierMangler;
import eu.essi_lab.accessor.bndmet.BNDMETMapper;
import eu.essi_lab.accessor.bndmet.model.BNDMETData;
import eu.essi_lab.accessor.bndmet.model.BNDMETData.BNDMET_Data_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter;
import eu.essi_lab.accessor.bndmet.model.BNDMETParameter.BNDMET_Parameter_Code;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation;
import eu.essi_lab.accessor.bndmet.model.BNDMETStation.BNDMET_Station_Code;
import eu.essi_lab.lib.net.protocols.NetProtocols;
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

public class BNDMETDownloader extends WMLDataDownloader {

    private static final String BNDMET_UNABLE_DOWNLOAD_STATION_ERROR = "BNDMET_UNABLE_DOWNLOAD_STATION_ERROR";
    private static final String BNDMET_GET_REMOTE_DESCRIPTORS_ERROR = "BNDMET_GET_REMOTE_DESCRIPTORS_ERROR";
    private static final String BNDMET_DOWNLOAD_ERROR = "BNDMET_DOWNLOAD_ERROR";

    @Override
    public boolean canConnect() {
	BNDMETClient client = new BNDMETClient(online.getLinkage());
	return !client.getAutomaticStations().isEmpty();
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

    // @Override
    // protected void reduceDimension(DataDimension dataDimension) {
    // // super.reduceDimension(dataDimension);
    // Number lower = dataDimension.getContinueDimension().getLower();
    // Number upper = dataDimension.getContinueDimension().getUpper();
    // if (lower != null && upper != null) {
    // long l = lower.longValue();
    // long u = upper.longValue();
    // long extent = u - l;
    //
    // dataDimension.getContinueDimension().setLower(u - thirtyDays);
    //
    // }
    // }

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocols.BNDMET.getCommonURN()));
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	String name = online.getName();

	BNDMETIdentifierMangler mangler = new BNDMETIdentifierMangler();
	mangler.setMangling(name);

	String stationCode = mangler.getPlatformIdentifier();
	String parameterCode = mangler.getParameterIdentifier();

	BNDMETStation station = null;

	BNDMETClient client = new BNDMETClient(online.getLinkage());

	try {

	    station = client.getStation(stationCode);

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BNDMET_UNABLE_DOWNLOAD_STATION_ERROR);

	}

	if (station == null) {

	    throw GSException.createException(//
		    getClass(), //
		    "BNDMET Downloader: unable to download station", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BNDMET_UNABLE_DOWNLOAD_STATION_ERROR);
	}

	try {

	    String stationOscarCode = station.getValue(BNDMET_Station_Code.CD_OSCAR);
	    String stationName = station.getValue(BNDMET_Station_Code.DC_NOME);
	    String stationCapital = station.getValue(BNDMET_Station_Code.FL_CAPITAL);
	    String stationTimeEnd = station.getValue(BNDMET_Station_Code.DT_FIM_OPERACAO);
	    String stationSituation = station.getValue(BNDMET_Station_Code.CD_SITUACAO);
	    String stationType = station.getValue(BNDMET_Station_Code.TP_ESTACAO);
	    String stationLatitude = station.getValue(BNDMET_Station_Code.VL_LATITUDE);
	    String stationWigosCode = station.getValue(BNDMET_Station_Code.CD_WSI);
	    String stationDistrict = station.getValue(BNDMET_Station_Code.CD_DISTRITO);
	    String stationAltitude = station.getValue(BNDMET_Station_Code.VL_ALTITUDE);
	    String stationState = station.getValue(BNDMET_Station_Code.SG_ESTADO);
	    String stationOrganization = station.getValue(BNDMET_Station_Code.SG_ENTIDADE);
	    String stationLongitude = station.getValue(BNDMET_Station_Code.VL_LONGITUDE);
	    String stationTimeBegin = station.getValue(BNDMET_Station_Code.DT_INICIO_OPERACAO);

	    BNDMETParameter parameter = client.getStationParameter(stationCode, parameterCode);

	    String parameterMeasurementType = parameter.getValue(BNDMET_Parameter_Code.TIPO_GERACAO);
	    String parameterDescription = parameter.getValue(BNDMET_Parameter_Code.DESCRICAO);
	    String parameterPeriod = parameter.getValue(BNDMET_Parameter_Code.PERIODICIDADE);
	    String parameterUnits = parameter.getValue(BNDMET_Parameter_Code.UNIDADE);
	    String parameterClass = parameter.getValue(BNDMET_Parameter_Code.CLASSE);

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

	    Date begin = getISO8601Date(stationTimeBegin);
	    if (begin == null) {
		Long threeMonths = 1000 * 60 * 60 * 24 * 100l;
		begin = new Date(System.currentTimeMillis() - threeMonths);

	    }
	    Date end = getISO8601Date(stationTimeEnd);
	    if (end == null) {
		end = new Date();
	    }

	    descriptor.setTemporalDimension(begin, end);
	    DataDimension temporalDimension = descriptor.getTemporalDimension();
	    long oneHourInMilliseconds = 1000 * 60 * 60l;
	    Long oneDayInMilliseconds = oneHourInMilliseconds * 24l;
	    Long oneMonthInMilliseconds = oneDayInMilliseconds * 31l;
	    switch (parameterPeriod.toLowerCase()) {
	    case "mensal":
		temporalDimension.getContinueDimension().setLowerTolerance(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setResolution(oneMonthInMilliseconds);
		temporalDimension.getContinueDimension().setResolutionTolerance(oneMonthInMilliseconds);
		break;
	    case "diario":
		temporalDimension.getContinueDimension().setLowerTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setUpperTolerance(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setResolution(oneDayInMilliseconds);
		temporalDimension.getContinueDimension().setResolutionTolerance(oneDayInMilliseconds);
		break;
	    case "horário":
	    case "horario":
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

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BNDMET_GET_REMOTE_DESCRIPTORS_ERROR);

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
	    BNDMETIdentifierMangler mangler = new BNDMETIdentifierMangler();
	    mangler.setMangling(name);
	    String stationCode = mangler.getPlatformIdentifier();
	    String parameterCode = mangler.getParameterIdentifier();
	    BNDMETClient client = new BNDMETClient(online.getLinkage());
//	    BNDMETStation station = client.getStation(stationCode);
//
//	    String stationOscarCode = station.getValue(BNDMET_Station_Code.CD_OSCAR);
//	    String stationName = station.getValue(BNDMET_Station_Code.DC_NOME);
//	    String stationCapital = station.getValue(BNDMET_Station_Code.FL_CAPITAL);
//	    String stationTimeEnd = station.getValue(BNDMET_Station_Code.DT_FIM_OPERACAO);
//	    String stationSituation = station.getValue(BNDMET_Station_Code.CD_SITUACAO);
//	    String stationType = station.getValue(BNDMET_Station_Code.TP_ESTACAO);
//	    String stationLatitude = station.getValue(BNDMET_Station_Code.VL_LATITUDE);
//	    String stationWigosCode = station.getValue(BNDMET_Station_Code.CD_WSI);
//	    String stationDistrict = station.getValue(BNDMET_Station_Code.CD_DISTRITO);
//	    String stationAltitude = station.getValue(BNDMET_Station_Code.VL_ALTITUDE);
//	    String stationState = station.getValue(BNDMET_Station_Code.SG_ESTADO);
//	    String stationOrganization = station.getValue(BNDMET_Station_Code.SG_ENTIDADE);
//	    String stationLongitude = station.getValue(BNDMET_Station_Code.VL_LONGITUDE);
//	    String stationTimeBegin = station.getValue(BNDMET_Station_Code.DT_INICIO_OPERACAO);

//	    BNDMETParameter parameter = client.getStationParameter(stationCode, parameterCode);
//
//	    String parameterMeasurementType = parameter.getValue(BNDMET_Parameter_Code.TIPO_GERACAO);
//	    String parameterDescription = parameter.getValue(BNDMET_Parameter_Code.DESCRICAO);
//	    String parameterPeriod = parameter.getValue(BNDMET_Parameter_Code.PERIODICIDADE);
//	    String parameterUnits = parameter.getValue(BNDMET_Parameter_Code.UNIDADE);
//	    String parameterClass = parameter.getValue(BNDMET_Parameter_Code.CLASSE);

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

	    List<BNDMETData> datas = client.getData(stationCode, parameterCode, startDate, endDate);

	    for (BNDMETData data : datas) {

		String dataValue = data.getValue(BNDMET_Data_Code.VL_MEDICAO);
		String dataDate = data.getValue(BNDMET_Data_Code.DT_MEDICAO);// e.g. "2002-01-01"
		String dataPeriod = data.getValue(BNDMET_Data_Code.PERIODICIDADE);
		String dataHour = data.getValue(BNDMET_Data_Code.HR_MEDICAO);// e.g. "1200"
		String dataUnits = data.getValue(BNDMET_Data_Code.UNIDADE);

		ValueSingleVariable v = new ValueSingleVariable();
		// ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// Source xmlSource = new DOMSource(n);
		// Result outputTarget = new StreamResult(outputStream);
		// TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
		// InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

		// if (dataValue != null && !dataValue.isEmpty()) {

		DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HHmm");
		iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date parsed = iso8601OutputFormat.parse(dataDate + " " + dataHour);
		BigDecimal dValue;
		if (dataValue != null && !dataValue.isEmpty()) {
		    dataValue = dataValue.replace(",", ".");
		    dValue = new BigDecimal(dataValue);
		} else {
		    dValue = new BigDecimal(BNDMETMapper.MISSING_VALUE);
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
		    BNDMET_DOWNLOAD_ERROR, //
		    e);
	}

    }

}
