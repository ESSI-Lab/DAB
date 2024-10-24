package eu.essi_lab.accessor.rasaqm;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class RasaqmClient {

    private String endpoint = "http://www.feerc.ru/geoss/rasaqm";
    private List<SimpleEntry<String, String>> parameters = null;

    private static final int EXPECTED_SPLIT_SIZE = 7;
    private static final String MALFORMED_RASAQM_DATASET_ERROR = "MALFORMED_RASAQM_DATASET_ERROR";

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    public List<SimpleEntry<String, String>> getParameters() throws Exception {

	if (this.parameters != null) {
	    return parameters;
	}

	List<SimpleEntry<String, String>> ret = new ArrayList<>();

	Date date = new Date();

	File tmpFile = retrieveData("CO", date, date, false, null);
	String parameters = new String(Files.readAllBytes(tmpFile.toPath()), StandardCharsets.UTF_8);
	tmpFile.delete();

	parameters = parameters.substring(parameters.indexOf("select id=\'param\'"));
	parameters = parameters.substring(0, parameters.indexOf("</select>"));
	// select id='param' name='param'><option value='CO'>Concentration of
	// CO</option><option value='NO'>Concentration of NO</option><option value='NO2'>Concentration of
	// NO2</option><option value='SO2'>Concentration of SO2</option><option value='H2S'>Concentration of
	// H2S</option><option value='O3'>Concentration of O3</option><option value='NH3'>Concentration of
	// NH3</option><option value='HCH'>Concentration of ∑CH</option><option value='CH4'>Concentration of
	// CH4</option><option value='PM10'>Concentration of PM-10</option><option value='PM25'>Concentration of
	// PM-2,5</option>
	String[] split = parameters.split("<option");
	for (int i = 1; i < split.length; i++) {
	    String option = split[i];
	    String id = option.substring(option.indexOf("'") + 1, option.lastIndexOf("'"));
	    String label = option.substring(option.indexOf("'>") + 2, option.lastIndexOf("</option>"));
	    ret.add(new SimpleEntry<String, String>(id, label));
	}

	this.parameters = ret;
	return ret;
    }

    private File retrieveData(String parameter, Date begin, Date end, boolean csvOutput, java.util.TimeZone timezone) throws Exception {
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	if (timezone == null) {
	    timezone = java.util.TimeZone.getTimeZone("GMT");
	}
	sdf.setTimeZone(timezone);
	String[] splitSDF = sdf.format(begin).split("-");
	String beginMonth = splitSDF[0];
	String beginDay = splitSDF[1];
	String beginYear = splitSDF[2];
	splitSDF = sdf.format(end).split("-");
	String endMonth = splitSDF[0];
	String endDay = splitSDF[1];
	String endYear = splitSDF[2];
	String output = "0";
	if (csvOutput) {
	    output = "1";
	}
	String url = endpoint + "/datasets?param=" + parameter + "&from=" + beginMonth + "%2F" + beginDay + "%2F" + beginYear + //
		"&to=" + endMonth + "%2F" + endDay + "%2F" + endYear + "&output=" + output + "&request=Request";
	Downloader downloader = new Downloader();
	Optional<InputStream> optionalStream = downloader.downloadOptionalStream(url);
	InputStream stream = optionalStream.get();
	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".txt");
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	return tmpFile;
    }

    public RasaqmDataset parseData(String parameterId, Date begin, Date end, java.util.TimeZone timezone) throws Exception {

	File tmpFile = retrieveData(parameterId, begin, end, true, timezone);
	FileInputStream fis = new FileInputStream(tmpFile);
	InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
	BufferedReader reader = new BufferedReader(isr);

	RasaqmDataset dataset = new RasaqmDataset();
	dataset.setParameterId(parameterId);

	String header = reader.readLine();

	String[] split = header.split(";");

	if (split.length < EXPECTED_SPLIT_SIZE) {

	    release(reader, isr, fis, tmpFile);

	    throw GSException.createException(//
		    getClass(), //
		    "Malformed RASAQM Dataset", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, MALFORMED_RASAQM_DATASET_ERROR);
	}

	String parameterName = split[6];

	parameterName = parameterName.replace("\"", "");
	if (parameterName.contains(",")) {
	    String[] parameterSplit = parameterName.split(",");
	    dataset.setParameterName(parameterSplit[0]);
	    dataset.setUnits(parameterSplit[1]);
	} else {
	    dataset.setParameterName(parameterName);
	}
	SimpleDateFormat sdf = new SimpleDateFormat("dd'.'MM'.'yyyy'T'HH:mm");
	if (timezone == null) {
	    timezone = java.util.TimeZone.getTimeZone("GMT");
	}
	sdf.setTimeZone(timezone);
	String line = null;
	while ((line = reader.readLine()) != null) {

	    split = line.split(";");

	    String date = split[0];
	    String time = split[1];
	    String stationLatitude = split[3];
	    String stationLongitude = split[4];
	    String stationName = split[5];
	    String value = split[6];

	    RasaqmSeries station = dataset.getSeries(stationName);

	    if (station == null) {
		station = new RasaqmSeries();
		station.setParameterId(dataset.getParameterId());
		station.setParameterName(dataset.getParameterName());
		station.setUnits(dataset.getUnits());
		station.setStationName(stationName);
		station.setLatitude(new BigDecimal(stationLatitude.replace(",", ".")));
		station.setLongitude(new BigDecimal(stationLongitude.replace(",", ".")));
		dataset.addSeries(station);
	    }

	    BigDecimal decimalValue = new BigDecimal(value.replace(",", "."));
	    Date dateDate = sdf.parse(date + "T" + time);
	    station.getData().add(new RasaqmData(dateDate, decimalValue));

	}

	release(reader, isr, fis, tmpFile);

	return dataset;
    }

    /**
     * @param reader
     * @param isr
     * @param isr2
     * @param fis
     * @param tmpFile
     */
    private void release(BufferedReader reader, InputStreamReader isr, FileInputStream fis, File tmpFile) {

	try {
	    reader.close();
	    isr.close();
	    fis.close();
	    tmpFile.delete();

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
