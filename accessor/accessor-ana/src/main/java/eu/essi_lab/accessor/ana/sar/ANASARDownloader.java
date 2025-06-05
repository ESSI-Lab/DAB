package eu.essi_lab.accessor.ana.sar;

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
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
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
import org.json.JSONObject;

import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class ANASARDownloader extends WMLDataDownloader {

    private static final String ANA_SAR_DOWNLOAD_ERROR = "ANA_SAR_DOWNLOAD_ERROR";

    @Override
    public boolean canDownload() {
	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.ANA_SAR_URI));

    }

    @Override
    public boolean canConnect() {
	String wsdl = online.getLinkage() + "?WSDL";

	try {
	    return HttpConnectionUtils.checkConnectivity(wsdl);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
	return false;
    }

    @Override
    protected void reduceDimension(DataDimension dataDimension) {
	if (dataDimension.getType().equals(DimensionType.TIME)) {
	    Number originalLower = dataDimension.getContinueDimension().getLower();
	    dataDimension.getContinueDimension().setUpper(originalLower);
	    dataDimension.getContinueDimension().setResolution(null);
	    dataDimension.getContinueDimension().setResolutionTolerance(null);
	    dataDimension.getContinueDimension().setSize(1l);

	}
    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();
	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());
	    String name = online.getName();

	    ANASARIdentifierMangler mangler = new ANASARIdentifierMangler();
	    mangler.setMangling(name);

	    String variable = mangler.getParameterIdentifier();
	    String reservoirId = mangler.getPlatformIdentifier();

	    ANASARClient client = new ANASARClient();

	    if (variable != null && reservoirId != null) {

		ExtensionHandler extensionHandler = resource.getExtensionHandler();
		Optional<String> optionalResolution = extensionHandler.getTimeResolution();

		boolean full = true;
		if (optionalResolution.isPresent()) {
		    full = false;
		}
		Optional<JSONObject> json = client.getSeriesInformation(reservoirId, variable, full);

		if (json.isPresent()) {

		    ANASARStation station = new ANASARStation(json.get());
		    String reservoirName = station.getReservoirName();
		    String reservoirCapacity = station.getReservoirCapacity();
		    String latitude = station.getLatitude();
		    String longitude = station.getLongitude();
		    String municipality = station.getMunicipality();
		    String state = station.getState();
		    String stateAbbreviation = station.getStateAbbreviation();
		    String basinName = station.getBasinName();
		    String network = station.getNetwork();
		    String variableUnits = station.getVariableUnits();
		    // String startDate = station.getStartDate();
		    // String endDate = station.getEndDate();
		    String resolutionMs;

		    if (full) {
			resolutionMs = JSONUtils.getString(json.get(), "resolution_ms");
		    } else {
			resolutionMs = optionalResolution.get();
		    }

		    TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		    if (temporalExtent != null) {
			Optional<Date> optionalBegin = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.getBeginPosition());
			Optional<Date> optionalEnd = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.getEndPosition());
			if (temporalExtent.getIndeterminateEndPosition() != null
				&& temporalExtent.getIndeterminateEndPosition().equals(TimeIndeterminateValueType.NOW)) {
			    optionalEnd = Optional.of(new Date());
			}
			if (!optionalBegin.isPresent() || !optionalEnd.isPresent()) {
			    GSLoggerFactory.getLogger(getClass())
				    .error("not possible to parse begin: " + temporalExtent.getBeginPosition());
			    GSLoggerFactory.getLogger(getClass()).error("or not possible to parse end: " + temporalExtent.getEndPosition());
			} else {
			    Date begin = optionalBegin.get();
			    Date end = optionalEnd.get();
			    GregorianCalendar startGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    startGregorian.setTime(begin);
			    GregorianCalendar endGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    endGregorian.setTime(end);

			    descriptor.setTemporalDimension(startGregorian.getTime(), endGregorian.getTime());

			    descriptor.setEPSG4326SpatialDimensions(Double.valueOf(latitude), Double.valueOf(longitude));

			    DataDimension temporalDimension = descriptor.getTemporalDimension();
			    Long oneDayInMilliseconds = 1000 * 60 * 60 * 24l;
			    Long oneWeekInMilliseconds = oneDayInMilliseconds * 7l;
			    temporalDimension.getContinueDimension().setLowerTolerance(4 * oneWeekInMilliseconds);
			    temporalDimension.getContinueDimension().setUpperTolerance(4 * oneWeekInMilliseconds);

			    Long res = resolutionMs == null ? null : Long.parseLong(resolutionMs);
			    System.out.println("Resolution: " + res);
			    if (res == null) {
				res = 4 * oneWeekInMilliseconds; // 1 month by default
			    }
			    temporalDimension.getContinueDimension().setResolution(res);
			    temporalDimension.getContinueDimension().setResolutionTolerance(res * 7);

			    ret.add(descriptor);
			}
		    }
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ANA_SAR_DOWNLOAD_ERROR, //
		    e);

	}

	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	String name = online.getName();

	ANASARIdentifierMangler mangler = new ANASARIdentifierMangler();
	mangler.setMangling(name);

	String variable = mangler.getParameterIdentifier();
	String reservoirId = mangler.getPlatformIdentifier();

	ANASARClient client = new ANASARClient();

	if (variable != null && reservoirId != null) {

	    Optional<JSONObject> json = client.getSeriesInformation(reservoirId, variable, false);

	    if (json.isPresent()) {

		ANASARStation station = new ANASARStation(json.get());

		String reservoirName = station.getReservoirName();
		String reservoirCapacity = station.getReservoirCapacity();
		String latitude = station.getLatitude();
		String longitude = station.getLongitude();
		String municipality = station.getMunicipality();
		String state = station.getState();
		String stateAbbreviation = station.getStateAbbreviation();
		String basinName = station.getBasinName();
		String network = station.getNetwork();
		String variableUnits = station.getVariableUnits();
		// String startDate = station.getStartDate();
		// String endDate = station.getEndDate();
		// String resolutionMs = station.getResolutionMs();

		BigDecimal missingValue = new BigDecimal("-9999.0");

		DataDimension dimension = descriptor.getTemporalDimension();
		Date begin = null;
		Date end = null;
		String startString = null;
		String endString = null;
		if (dimension != null && dimension.getContinueDimension().getUom().equals(Unit.MILLI_SECOND)) {
		    ContinueDimension sizedDimension = dimension.getContinueDimension();
		    begin = new Date(sizedDimension.getLower().longValue());
		    end = new Date(sizedDimension.getUpper().longValue());
		}
		TimeSeriesResponseType tsrt = getTimeSeriesTemplate();
		try {
		    List<SimpleEntry<Date, BigDecimal>> data = client.getData(reservoirId, variable, begin, end);

		    for (SimpleEntry<Date, BigDecimal> entry : data) {
			ValueSingleVariable v = new ValueSingleVariable();
			DateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date parsed = entry.getKey();
			BigDecimal dataValue = entry.getValue();
			if (dataValue == null) {
			    dataValue = missingValue;
			}
			v.setValue(dataValue);
			GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			c.setTime(parsed);
			XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			v.setDateTimeUTC(date2);
			addValue(tsrt, v);
		    }

		    ObjectFactory factory = new ObjectFactory();

		    JAXBElement<TimeSeriesResponseType> response = factory.createTimeSeriesResponse(tsrt);
		    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".wml");
		    tmpFile.deleteOnExit();
		    JAXBWML.getInstance().marshal(response, tmpFile);

		    return tmpFile;

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
	    }
	}

	throw GSException.createException(//
		getClass(), //
		"Unable to download", //
		null, ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		ANA_SAR_DOWNLOAD_ERROR //
	);
    }

    @Override
    public boolean canSubset(String dimensionName) {
	return true;
    }

}
