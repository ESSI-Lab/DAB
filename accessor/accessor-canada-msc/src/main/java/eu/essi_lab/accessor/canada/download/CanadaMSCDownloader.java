package eu.essi_lab.accessor.canada.download;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.canada.CANADAMSCConnector;
import eu.essi_lab.accessor.canada.CANADAMSCMapper.Resolution;
import eu.essi_lab.accessor.canada.ECVariable;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.protocols.NetProtocolWrapper;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class CanadaMSCDownloader extends WMLDataDownloader {

    private static final String CANADA_MSC_DOWNLOAD_ERROR = "CANADA_MSC_DOWNLOAD_ERROR";
    private String linkage;

    private CANADAMSCConnector connector = new CANADAMSCConnector();

    @Override
    public boolean canConnect() throws GSException {

	try {
	    return HttpConnectionUtils.checkConnectivity(online.getLinkage());
	} catch (URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	this.linkage = online.getLinkage();
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(NetProtocolWrapper.ECANADA.getCommonURN()));

    }

    @Override
    public boolean canSubset(String dimensionName) {

	return false;

    }

    @Override
    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Get remote descriptors STARTED");

	List<DataDescriptor> ret = new ArrayList<>();
	DataDescriptor descriptor = new DataDescriptor();

	try {

	    descriptor.setDataType(DataType.TIME_SERIES);
	    descriptor.setDataFormat(DataFormat.WATERML_1_1());
	    descriptor.setCRS(CRS.EPSG_4326());

	    String name = online.getName();

	    GSLoggerFactory.getLogger(getClass()).debug("Current descriptor: {}", descriptor);

	    if (name != null && name.contains("@")) {

		GSLoggerFactory.getLogger(getClass()).debug("Current name: {}", name);

		String variableId = name.split("@")[0];
		String stationName = name.split("@")[1];

		// download stations
		// connector.createStationsMap();
		// List<ECStation> stationsList = connector.getStations();

		// get lat, lon

		// Double lat = null;
		// Double lon = null;
		// for (ECStation station : stationsList) {
		// if (station.getName().equals(stationName)) {
		// lat = Double.valueOf(station.getLat());
		// lon = Double.valueOf(station.getLon());
		// break;
		// }
		// }
		//
		// if (lat != null && lon != null)
		// descriptor.setEPSG4326SpatialDimensions(lat, lon);

		String firsDate = null;
		String secondDate = null;
		String lastDate = null;

		int i = 0;

		InputStream body = getData();

		if (body != null) {

		    BufferedReader bfReader = new BufferedReader(new InputStreamReader(body));
		    String temp = null;
		    bfReader.readLine(); // skip header line

		    while ((temp = bfReader.readLine()) != null) {

			String[] split = temp.split(",", -1);
			String d = split[1];

			if (i == 0) {
			    firsDate = d;
			}

			if (i == 1) {
			    secondDate = d;
			}

			lastDate = d;
			i++;
		    }

		    //
		    // get: first time, last time, resolution
		    //
		    Date begin = null;
		    Date end = null;

		    Optional<Date> endTime = ISO8601DateTimeUtils.parseISO8601ToDate(lastDate);
		    Optional<Date> startTime = ISO8601DateTimeUtils.parseISO8601ToDate(firsDate);

		    if (startTime.isPresent()) {
			begin = startTime.get();
		    }

		    if (endTime.isPresent()) {
			end = endTime.get();
		    }

		    if (begin != null && end != null) {

			GregorianCalendar startGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			startGregorian.setTime(begin);

			GregorianCalendar endGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			endGregorian.setTime(end);

			descriptor.setTemporalDimension(startGregorian.getTime(), endGregorian.getTime());
		    }

		    DataDimension temporalDimension = descriptor.getTemporalDimension();

		    long valueCount = i; // number of rows
		    Optional<Date> secondTime = ISO8601DateTimeUtils.parseISO8601ToDate(secondDate);

		    // Long diffInMillies = null;
		    // if (startTime.isPresent() && secondTime.isPresent()) {
		    // diffInMillies = Math.abs(secondTime.get().getTime() - startTime.get().getTime());
		    // }
		    //
		    // Number resolution = diffInMillies; // in milliseconds

		    // if (resolution != null) {
		    //
		    // temporalDimension.getContinueDimension().setResolution(resolution);

		    /**
		     * N.B.the value count is not used, as it only reports the number of non missing
		     * values and not the total number of values considering the temporal extent and
		     * the resolution
		     */
		    // temporalDimension.getContinueDimension().setSize(valueCount);
		    //
		    // }

		    ret.add(descriptor);

		    //
		    // closing streams
		    //
		    bfReader.close();
		    body.close();
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading descriptors of online {}", online.getName(), e);
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CANADA_MSC_DOWNLOAD_ERROR, //
		    e);

	}

	GSLoggerFactory.getLogger(getClass()).debug("Found {} descriptors", ret.size());
	GSLoggerFactory.getLogger(getClass()).debug("Get remote descriptors ENDED");

	return ret;
    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Downloading of descriptor {} STARTED", descriptor);

	String name = online.getName();
	// we expect a CUAHSI Hydro Server online resource name in the form as encoded
	// by WOFIdentifierMangler

	GSLoggerFactory.getLogger(getClass()).debug("Current name: {}", name);

	if (name != null && name.contains("@")) {

	    String variableId = name.split("@")[0];
	    String station = name.split("@")[1];

	    try {
		// make waterml
		String variableName = variableId.split("_")[0];
		ECVariable ecVariable = ECVariable.decode(variableName);
		String variableResolution = variableId.split("_")[1];
		Resolution resolution = Resolution.DAILY;

		if (variableResolution.equals("HOURLY")) {
		    resolution = Resolution.HOURLY;
		}

		ObjectFactory factory = new ObjectFactory();

		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		InputStream is = getData();

		String firsDate = null;
		String secondDate = null;

		if (is != null) {

		    BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));

		    String temp = null;
		    bfReader.readLine(); // skip header line

		    int j = 0;
		    while ((temp = bfReader.readLine()) != null) {

			ValueSingleVariable v = new ValueSingleVariable();
			String[] split = temp.split(",", -1);
			String d = split[1];

			if (j == 0) {
			    firsDate = d;
			}

			if (j == 1) {
			    secondDate = d;
			}

			String variableValue = null;

			if (ECVariable.decode(variableName).equals(ECVariable.WATER_LEVEL)) {
			    variableValue = split[2];
			}

			if (ECVariable.decode(variableName).equals(ECVariable.DISCHARGE)) {
			    variableValue = split[6];
			}

			if (variableValue != null && !variableValue.isEmpty()) {

			    BigDecimal dataValue = new BigDecimal(variableValue);
			    v.setValue(dataValue);
			    Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(d);

			    if (date.isPresent()) {
				GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
				c.setTime(date.get());

				XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				v.setDateTimeUTC(date2);
				addValue(tsrt, v);
			    }
			}

			j++;
		    }


		    bfReader.close();
		    is.close();

		    GSLoggerFactory.getLogger(getClass()).debug("Downloading of descriptor {} ENDED", descriptor);

		    return tsrt.getDataFile();
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			CANADA_MSC_DOWNLOAD_ERROR, //
			e);
	    }
	}

	throw GSException.createException(//

		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		CANADA_MSC_DOWNLOAD_ERROR);

    }

    private InputStream getData() throws Exception {

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from {} STARTED", linkage);

	// download data
	Downloader executor = new Downloader();
	executor.setConnectionTimeout(TimeUnit.MINUTES, 1);

	HttpResponse<InputStream> response = executor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, linkage));

	int statusCode = response.statusCode();

	GSLoggerFactory.getLogger(getClass()).trace("Status code: {}", statusCode);

	if (statusCode != 200) {

	    return null;
	}

	InputStream content = response.body();

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from {} ENDED", linkage);

	return content;
    }
}
