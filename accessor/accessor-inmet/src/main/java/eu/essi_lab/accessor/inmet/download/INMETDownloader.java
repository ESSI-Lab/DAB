package eu.essi_lab.accessor.inmet.download;

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
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.cuahsi.waterml._1.ObjectFactory;
import org.cuahsi.waterml._1.ValueSingleVariable;

import eu.essi_lab.access.wml.TimeSeriesTemplate;
import eu.essi_lab.access.wml.WMLDataDownloader;
import eu.essi_lab.accessor.inmet.INMETConnector;
import eu.essi_lab.accessor.inmet.INMETVariable;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class INMETDownloader extends WMLDataDownloader {

    private static final String INMET_DOWNLOAD_ERROR = "INMET_DOWNLOAD_ERROR";
    private static final String INMET_STREAM_ERROR = "INMET_STREAM_ERROR";
    private String linkage;

    private static final String user = "broker";

    private static final String psw = "Pla645!z";

    private INMETConnector connector = new INMETConnector();

    @Override
    public boolean canConnect() throws GSException {
	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	return connector.supports(source);
    }

    @Override
    public void setOnlineResource(GSResource resource, String onlineResourceId) throws GSException {
	super.setOnlineResource(resource, onlineResourceId);
	String baseEndpoint = online.getLinkage();

	if (!baseEndpoint.contains("ftp")) {
	    return;
	}

	String[] splitted = baseEndpoint.split("ftp://");

	String url = "ftp://" + user + ":" + psw + "@" + splitted[1];

	this.linkage = url;
	this.connector.setSourceURL(resource.getSource().getEndpoint());
    }

    @Override
    public boolean canDownload() {

	return (online.getProtocol() != null && online.getProtocol().equals(CommonNameSpaceContext.INMET_CSV_URI));

    }

    @Override
    public boolean canSubset(String dimensionName) {

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
	    String name = online.getName();
	    // we expect a CUAHSI Hydro Server online resource name, as encoded by the
	    // WOFIdentifierMangler
	    if (name != null && name.contains("@")) {
		String variableId = name.split("@")[0];
		String stationName = name.split("@")[1];
		String fileName = name.split("@")[2];
		Date beginDate = null;
		Date endDate = null;

		int i = 0;
		int count = 0;

		File file = connector.getDownloader().downloadStream(linkage, fileName);

		if (file != null) {
		    try (FileReader reader = new FileReader(file); BufferedReader bfReader = new BufferedReader(reader)) {

			String temp = null;
			String[] values = connector.getValues(bfReader);
			if (values == null) {
			    GSLoggerFactory.getLogger(getClass()).info("ERROR READING FILE: {}", fileName);
			    return null;
			    // throw GSException.createException(this.getClass(), "Error reading inpustream: ",
			    // "IOException connecting to: ", ErrorInfo.ERRORTYPE_CLIENT, ErrorInfo.SEVERITY_ERROR,
			    // INMET_STREAM_ERROR);
			}

			HashMap<String, String> metadata = new HashMap<>();
			while (values != null && !connector.containsCaseInsensitive("precipita", values)) {
			    String key = values[0];
			    key = key.replace(":", "");
			    key = key.trim();
			    key = key.toUpperCase();
			    if (values.length > 1) {
				metadata.put(key, values[1]);
			    } else {
				GSLoggerFactory.getLogger(getClass()).info("ERROR XLS FILE: {}", fileName);
			    }
			    values = connector.getValues(bfReader);
			}

			String beginStringDate = null;
			String endStringDate = null;

			SortedMap<Date, Double> valuesPoint = new TreeMap<Date, Double>();
			while (bfReader.ready()) {
			    values = connector.getValues(bfReader);
			    String date = values[0];
			    String hour = values[1];
			    String value = values[2];
			    if (value == null || value.startsWith("-999")) {
				continue;
			    }
			    value = value.replace(",", ".");
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			    String tmpString = date + "T" + hour;
			    Date tmp = format.parse(tmpString);
			    // Date tmp = TimeAndDateHelper.parse(date + hour, "yyyy-MM-ddHHmm");
			    try {
				double v = Double.parseDouble(value);
				valuesPoint.put(tmp, v);
			    } catch (Exception e) {
				continue;
			    }
			    if (beginDate == null || beginDate.after(tmp)) {
				beginDate = tmp;
				beginStringDate = tmpString;
			    }
			    if (endDate == null || endDate.before(tmp)) {
				endDate = tmp;
				endStringDate = tmpString;
			    }
			    count++;
			}

			if (bfReader != null)
			    bfReader.close();
		    }
		}

		if (file.exists())
		    file.delete();

		// get: first time, last time, resolution

		if (beginDate != null && endDate != null) {
		    GregorianCalendar startGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    startGregorian.setTime(beginDate);
		    GregorianCalendar endGregorian = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		    endGregorian.setTime(endDate);

		    descriptor.setTemporalDimension(startGregorian.getTime(), endGregorian.getTime());
		}

		DataDimension temporalDimension = descriptor.getTemporalDimension();

		long valueCount = count; // number of rows

		// Long diffInMillies = null;
		// if (startTime.isPresent() && secondTime.isPresent()) {
		// diffInMillies = Math.abs(secondTime.get().getTime() -
		// startTime.get().getTime());
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
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", online.getName(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INMET_DOWNLOAD_ERROR, //
		    e);

	}
	return ret;

    }

    @Override
    public File download(DataDescriptor descriptor) throws GSException {
	String name = online.getName();
	// we expect a CUAHSI Hydro Server online resource name in the form as encoded
	// by WOFIdentifierMangler
	if (name != null && name.contains("@")) {
	    String variableId = name.split("@")[0];
	    String station = name.split("@")[1];
	    String fileName = name.split("@")[2];

	    try {
		// make waterml
		String variableName = variableId.split("_")[0];
		INMETVariable inmetVariable = INMETVariable.decode(variableName);

		TimeSeriesTemplate tsrt = getTimeSeriesTemplate(getClass().getSimpleName(), ".wml");

		File file = connector.getDownloader().downloadStream(linkage, fileName);

		if (file != null) {
		    try (FileReader reader = new FileReader(file); BufferedReader bfReader = new BufferedReader(reader)) {

			String[] values = connector.getValues(bfReader);
			if (values == null) {
			    GSLoggerFactory.getLogger(getClass()).info("ERROR DOWNLOADING FILE: {}", fileName);
			    File tmpFile = File.createTempFile("INMETDownloader", ".wml");
			    tmpFile.deleteOnExit();
			    return null;
			    // throw GSException.createException(this.getClass(), "Error Downloading file : " +
			    // fileName,
			    // "IOException connecting to ftp remote service: ", ErrorInfo.ERRORTYPE_CLIENT,
			    // ErrorInfo.SEVERITY_ERROR, INMET_DOWNLOAD_ERROR);
			}

			HashMap<String, String> metadata = new HashMap<>();
			while (values != null && !connector.containsCaseInsensitive("precipita", values)) {
			    String key = values[0];
			    key = key.replace(":", "");
			    key = key.trim();
			    key = key.toUpperCase();
			    if (values.length > 1) {
				metadata.put(key, values[1]);
			    } else {
				GSLoggerFactory.getLogger(getClass()).info("ERROR XLS FILE: {}", fileName);
			    }
			    values = connector.getValues(bfReader);
			}

			while (bfReader.ready()) {
			    values = connector.getValues(bfReader);
			    ValueSingleVariable v = new ValueSingleVariable();
			    String date = values[0];
			    String hour = values[1];
			    String valueString = values[2];
			    if (valueString == null || valueString.startsWith("-999")) {
				continue;
			    }
			    valueString = valueString.replace(",", ".");
			    BigDecimal dataValue = new BigDecimal(valueString);
			    v.setValue(dataValue);
			    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			    String tmpString = date + "T" + hour;
			    Date tmp = format.parse(tmpString);
			    GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			    c.setTime(tmp);
			    XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			    v.setDateTimeUTC(date2);
			    addValue(tsrt, v);
			}

			if (bfReader != null)
			    bfReader.close();
		    }

		    if (file.exists())
			file.delete();

		    return tsrt.getDataFile();

		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Error downloading {}", name, e);

		throw GSException.createException(//
			getClass(), //
			e.getMessage(), //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_ERROR, //
			INMET_DOWNLOAD_ERROR, //
			e);
	    }

	}

	throw GSException.createException(//

		getClass(), //
		"Error occurred, unable to download data", //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		INMET_DOWNLOAD_ERROR);

    }

}
