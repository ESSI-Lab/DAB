package eu.essi_lab.accessor.niwa;

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
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class NIWAClient {

    /**
     * 
     */
    private static final String DATA_LIST_REQUEST_FILE = "data-list-request.txt";

    /**
     * 
     */
    private static final String DOWNLOAD_REQUEST_FILE = "download-request.txt";
    /**
     * 
     */
    private static final String PAGE_SIZE = "PAGE_SIZE";
    /**
     * 
     */
    private static final String DATASET_IDENTIFIER = "DATASET_IDENTIFIER";
    /**
     * 
     */
    private static final String START_TIME = "START_TIME";
    /**
     * 
     */
    private static final String END_TIME = "END_TIME";

    /**
     * 
     */
    private static final int CSV_START_INDEX = 5;

    /**
     * 
     */
    public NIWAClient() {

    }

    /**
     * @return
     * @throws IOException
     */
    public List<JSONObject> getDataList() throws IOException {

	String size = String.valueOf(getDataListSize());

	GSLoggerFactory.getLogger(getClass()).debug("Data list size: {}", size);

	ArrayList<JSONObject> list = new ArrayList<>();

	String request = getDataListRequest(Arrays.asList(PAGE_SIZE), Arrays.asList(size));

	Downloader downloader = new Downloader();

	downloader.downloadOptionalString(request).ifPresent(s -> new JSONObject(s).getJSONArray("Data").forEach(e -> list.add((JSONObject) e)));

	return list;
    }

    /**
     * @param datasetIdentifier
     * @return
     * @throws IOException
     */
    public Optional<TemporalExtent> getTemporalExtent(String datasetIdentifier) {

	String request = null;
	try {
	    String size = String.valueOf(getDataListSize());

	    request = getDataListRequest(Arrays.asList(PAGE_SIZE), Arrays.asList(size));

	} catch (IOException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	    return Optional.empty();

	}
	ArrayList<JSONObject> list = new ArrayList<>();

	Downloader downloader = new Downloader();

	downloader.downloadOptionalString(request).ifPresent(s -> new JSONObject(s).getJSONArray("Data").forEach(e -> {

	    JSONObject dataObject = ((JSONObject) e);
	    if (NIWAMapper.readDatasetIDentifier(dataObject).equals(datasetIdentifier)) {

		list.add(dataObject);
	    }
	}));

	if (!list.isEmpty()) {

	    JSONObject dataObject = list.get(0);
	    String startOfRecord = NIWAMapper.readStartOfRecord(dataObject);
	    String endOfRecord = NIWAMapper.readEndOfRecord(dataObject);

	    TemporalExtent temporalExtent = new TemporalExtent();
	    temporalExtent.setBeginPosition(startOfRecord);
	    temporalExtent.setEndPosition(endOfRecord);

	    return Optional.of(temporalExtent);
	}

	return Optional.empty();
    }

    public static void main(String[] args) throws IOException {

	ArrayList<SimpleEntry<String, String>> download = NIWAClient.download("Stage.Master@15341", //
		ISO8601DateTimeUtils.parseISO8601ToDate("2022-12-10T22:20:00").get(), //
		ISO8601DateTimeUtils.parseISO8601ToDate("2022-12-14T22:20:00").get());

    }

    /**
     * @return
     * @throws IOException
     */
    public int getDataListSize() throws IOException {

	String request = getDataListRequest(Arrays.asList(PAGE_SIZE), Arrays.asList("1"));

	Downloader downloader = new Downloader();

	final int[] size = new int[1];

	downloader.downloadOptionalString(request).ifPresent(s -> size[0] = new JSONObject(s).getInt("Total"));

	return size[0];
    }

    /**
     * @param datasetIdentifier
     * @param beginDate
     * @param endDate
     * @return
     * @throws IOException
     */
    public static ArrayList<SimpleEntry<String, String>> download(String datasetIdentifier, Date beginDate, Date endDate)
	    throws IOException {

	String startTime = ISO8601DateTimeUtils.getISO8601DateTime(beginDate);
	String endTime = ISO8601DateTimeUtils.getISO8601DateTime(endDate);

	URL downloadURL = NIWAClient.getDownloadURL(datasetIdentifier, startTime, endTime);

	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(downloadURL.toExternalForm());

	ArrayList<SimpleEntry<String, String>> out = new ArrayList<>();

	if (response.isPresent()) {

	    String csv = response.get();

	    BufferedReader bufferedReader = new BufferedReader(new StringReader(csv));

	    Object[] array = bufferedReader.lines().toArray();

	    for (int i = CSV_START_INDEX; i < array.length; i++) {

		String line = array[i].toString();
		String date = line.split(" ")[0].split(",")[0];
		String time = line.split(" ")[1].split(",")[0];
		String dateTime = date + "T" + time;
		String value = line.split(" ")[1].split(",")[1];

		SimpleEntry<String, String> entry = new SimpleEntry<String, String>(dateTime, value);

		out.add(entry);
	    }
	}

	return out;
    }

    /**
     * @param datasetIdentifier
     * @param endDate
     * @return
     * @throws IOException
     */
    public static Optional<String> retrieveUOM(String datasetIdentifier, Date endDate) throws IOException {

	String endTime = ISO8601DateTimeUtils.getISO8601DateTime(endDate);
	String startTime = endTime;

	URL downloadURL = NIWAClient.getDownloadURL(datasetIdentifier, startTime, endTime);

	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(downloadURL.toExternalForm());

	if (response.isPresent()) {

	    String csv = response.get();

	    BufferedReader bufferedReader = new BufferedReader(new StringReader(csv));

	    Object[] array = bufferedReader.lines().toArray();

	    String row = array[4].toString();

	    String value = row.split(",")[1];

	    value = value.replace("Value (", "");
	    value = value.replace(")", "");

	    return Optional.of(value);
	}

	return Optional.empty();
    }

    /**
     * @param datasetIdentifier
     * @param startTime
     * @param endTime
     * @return
     * @throws IOException
     */
    public static URL getDownloadURL(String datasetIdentifier, String startTime, String endTime) throws IOException {

	datasetIdentifier = URLEncoder.encode(datasetIdentifier, "UTF-8");
	startTime = startTime.replace("T", URLEncoder.encode(" ", "UTF-8"));
	startTime = startTime.replace("Z", "");
	endTime = endTime.replace("T", URLEncoder.encode(" ", "UTF-8"));
	endTime = endTime.replace("Z", "");

	String request = getDownloadRequest(//
		Arrays.asList(DATASET_IDENTIFIER, START_TIME, END_TIME), //
		Arrays.asList(datasetIdentifier, startTime, endTime));

	return new URL(request);
    }

    /**
     * @param parameters
     * @param values
     * @return
     * @throws IOException
     */
    private String getDataListRequest(List<String> parameters, List<String> values) throws IOException {

	return getRequest(DATA_LIST_REQUEST_FILE, parameters, values);
    }

    /**
     * The "UnitID" parameter is omitted, so the values are always expressed with the default unit of measure.
     * The list of available unit of measure is in the "Conversion Option" menu, but the content is in the HTML page,
     * there is no API
     * to get the values
     * 
     * @param parameters
     * @param values
     * @return
     * @throws IOException
     */
    private static String getDownloadRequest(List<String> parameters, List<String> values) throws IOException {

	return getRequest(DOWNLOAD_REQUEST_FILE, parameters, values);
    }

    /**
     * @return
     * @throws IOException
     */
    private String getDataListRequest() throws IOException {

	return getDataListRequest(null, null);
    }

    /**
     * @param parameters
     * @param values
     * @return
     * @throws IOException
     */
    private static String getRequest(String requestTemplate, List<String> parameters, List<String> values) throws IOException {

	String request = IOStreamUtils.asUTF8String(NIWAClient.class.getClassLoader().getResourceAsStream(requestTemplate));

	request = request.replaceAll("\r\n", "");
	request = request.replaceAll("\n\r", "");
	request = request.replaceAll("\r", "");
	request = request.replaceAll("\n", "");

	if (parameters != null) {

	    for (int i = 0; i < parameters.size(); i++) {

		request = request.replace(parameters.get(i), values.get(i));
	    }
	}

	return request;
    }

}
