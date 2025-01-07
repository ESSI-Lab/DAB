package eu.essi_lab.accessor.ana.sar;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class ANASARClient {
    static String locationEndpoint = "https://www.ana.gov.br/sar0/Home/CarregaMapa";
    static String serviceEndpoint = "http://sarws.ana.gov.br/SarWebService.asmx";
    private static LinkedHashMap<String, JSONObject> stations = new LinkedHashMap<>();
    private Logger logger;

    public List<JSONObject> getStations() {
	return new ArrayList<>(stations.values());
    }

    public ANASARClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());
	if (!stations.isEmpty()) {
	    return;
	}
	synchronized (stations) {
	    if (!stations.isEmpty()) {
		return;
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Brazil ANA SAR: Downloading reservoir information");
	    Downloader downloader = new Downloader();
	    Optional<InputStream> optional = downloader.downloadOptionalStream(locationEndpoint);
	    if (optional.isPresent()) {
		InputStream stream = optional.get();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
		    IOUtils.copy(stream, baos);
		    stream.close();
		    JSONArray json = new JSONArray(new String(baos.toByteArray()));
		    baos.close();
		    for (int i = 0; i < json.length(); i++) {
			JSONObject obj = json.getJSONObject(i);
			String reservoirID = obj.get("res_id").toString();
			stations.put(reservoirID, obj);
		    }
		    GSLoggerFactory.getLogger(getClass()).info("Brazil ANA SAR: Downloaded.");
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	}
    }

    public JSONObject getStation(String reservoirId) {
	return stations.get(reservoirId);
    }

    private static HashMap<String, ParameterInfo> seriesInfo = new HashMap<>();

    public ParameterInfo getParameterInfo(String reservoirId, String variable) throws Exception {

	String key = reservoirId + ":" + variable;

	synchronized (seriesInfo) {
	    if (seriesInfo.containsKey(key)) {
		return seriesInfo.get(key);
	    }

	    long oneWeek = 1000 * 60 * 60 * 24 * 7l;

	    Date start = ISO8601DateTimeUtils.parseISO8601ToDate("1900-01-01T00:00:00Z").get();
	    Date end = new Date();

	    long halfSpan = (3 * oneWeek);
	    long desiredGAP = halfSpan * 2;

	    Date test = new Date(end.getTime() - halfSpan);

	    long tmpSpan = halfSpan;
	    while (true) {
		logger.info("Detecting end of series for reservoir: " + reservoirId + " Variable: " + variable);

		List<SimpleEntry<Date, BigDecimal>> data = getData(reservoirId, variable, test, tmpSpan);
		Date maxDate = null;
		for (SimpleEntry<Date, BigDecimal> entry : data) {
		    if (entry.getValue() != null) {
			if (maxDate == null || entry.getKey().after(maxDate)) {
			    maxDate = entry.getKey();
			}
		    }
		}
		if (maxDate != null) {
		    end = maxDate;
		    break;
		} else {
		    tmpSpan = (long) (tmpSpan * 2);
		    test = new Date(test.getTime() - tmpSpan * 2);
		}

		if (test.before(start)) {
		    ParameterInfo ret = new ParameterInfo();
		    return ret;
		}
	    }

	    Date t0 = start;
	    Date t1 = end;

	    int loops = 0;
	    while (true) {
		if (loops++ > 100) {
		    logger.info("Too many iterations detecting start time");
		    ParameterInfo ret = new ParameterInfo();
		    return ret;
		}
		logger.info("Detecting start of series for reservoir: " + reservoirId + " Variable: " + variable);

		Date middle = new Date((t0.getTime() + t1.getTime()) / 2);

		List<SimpleEntry<Date, BigDecimal>> data = getData(reservoirId, variable, middle, halfSpan);
		long gap = t1.getTime() - t0.getTime();

		String percent = "100";
		if (gap != 0) {
		    percent = "" + (((double) desiredGAP / (double) gap) * 100);
		}

		logger.info(" GAP: " + gap + " desired: " + desiredGAP + " " + percent + "%");
		if (gap <= desiredGAP) {
		    if (data.isEmpty()) {
			seriesInfo.put(key, null);
		    } else {
			Long resolution = null;
			Date tmp = null;
			Date minDate = null;
			for (SimpleEntry<Date, BigDecimal> entry : data) {
			    if (entry.getValue() != null) {
				if (minDate == null || entry.getKey().before(minDate)) {
				    minDate = entry.getKey();
				}
			    }
			    if (tmp != null) {
				resolution = Math.abs(entry.getKey().getTime() - tmp.getTime());
			    }
			    tmp = entry.getKey();
			}
			if (minDate == null) {
			    minDate = middle;
			}
			ParameterInfo info = new ParameterInfo();
			info.setBegin(minDate);
			info.setEnd(end);
			info.setResolution(resolution);
			logger.info("Parameter info for " + variable + ": " + ISO8601DateTimeUtils.getISO8601DateTime(minDate) + " "
				+ ISO8601DateTimeUtils.getISO8601DateTime(end) + " " + resolution);
			seriesInfo.put(key, info);
		    }
		    return seriesInfo.get(key);
		}

		Date foundDate = null;
		for (SimpleEntry<Date, BigDecimal> entry : data) {
		    if (entry.getValue() != null) {
			if (foundDate == null || foundDate.after(entry.getKey())) {
			    foundDate = entry.getKey();
			}
		    }
		}
		if (foundDate != null) {
		    logger.info("Found");
		    t1 = foundDate;
		} else {
		    logger.info("Not found");
		    t0 = middle;
		}

	    }
	}

    }

    public List<SimpleEntry<Date, BigDecimal>> getData(String reservoirId, String variable, Date date, long halfSpan) throws Exception {
	long oneWeek = 1000 * 60 * 60 * 24 * 7l;
	Date start = new Date(date.getTime() - halfSpan);
	Date end = new Date(date.getTime() + halfSpan);
	return getData(reservoirId, variable, start, end);
    }

    public List<SimpleEntry<Date, BigDecimal>> getData(String reservoirId, String variable, Date dateBegin, Date dateEnd) throws Exception {

	JSONObject json = getStation(reservoirId);
	String network = json.get("tsi_id").toString();

	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	long oneDay = 1000 * 60 * 60 * 24l;
	String start = sdf.format(new Date(dateBegin.getTime() - oneDay));
	String end = sdf.format(new Date(dateEnd.getTime() + oneDay));

	String request = null;
	// HttpPost post = new HttpPost(serviceEndpoint);

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("Accept", "*/*");

	if (network.equals("2") || // SIN network
		variable.equals(ANASARVariable.VOLUME_UTIL.getName()) || //
		variable.equals(ANASARVariable.AFLUENCIA.getName()) || //
		variable.equals(ANASARVariable.DEFLUENCIA.getName()) //
	) {
	    // SIN network
	    request = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sar=\"http://sarws.ana.gov.br\">\n" + //
		    "   <soap:Header/>\n" + //
		    "   <soap:Body>\n" + //
		    "      <sar:DadosHistoricosSIN>\n" + //
		    "         <sar:CodigoReservatorio>" + reservoirId + "</sar:CodigoReservatorio>\n" + //
		    "         <sar:DataInicial>" + start + "</sar:DataInicial>\n" + //
		    "         <sar:DataFinal>" + end + "</sar:DataFinal>\n" + //
		    "      </sar:DadosHistoricosSIN>\n" + //
		    "   </soap:Body>\n" + //
		    "</soap:Envelope>";
	    headers.put("Content-Type", "application/soap+xml;charset=UTF-8;action=\"http://sarws.ana.gov.br/DadosHistoricosSIN\"");
	} else {
	    // general network
	    request = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sar=\"http://sarws.ana.gov.br\">\n" + //
		    "   <soap:Header/>\n" + //
		    "   <soap:Body>\n" + //
		    "      <sar:DadosHistoricosReservatorios>\n" + //
		    "         <sar:CodigoReservatorio>" + reservoirId + "</sar:CodigoReservatorio>\n" + //
		    "         <sar:DataInicial>" + start + "</sar:DataInicial>\n" + //
		    "         <sar:DataFinal>" + end + "</sar:DataFinal>\n" + //
		    "      </sar:DadosHistoricosReservatorios>\n" + //
		    "   </soap:Body>\n" + //
		    "</soap:Envelope>";
	    headers.put("Content-Type",
		    "application/soap+xml;charset=UTF-8;action=\"http://sarws.ana.gov.br/DadosHistoricosReservatorios\"");
	}

	// post.setHeader("Accept", "*/*");

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, serviceEndpoint, request, HttpHeaderUtils.build(headers));

	InputStream body = sendRequestWithRetry(postRequest, 3);

	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();

	try {
	    XMLDocumentReader reader = new XMLDocumentReader(body);
	    Node[] nodes = reader.evaluateNodes("//*:DadosHistoricosReservatorios | //*:DadoHistoricoSIN");
	    for (Node node : nodes) {
		String dateString = reader.evaluateString(node, "*:data_medicao");
		Date date = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();
		if (date.before(dateBegin) || date.after(dateEnd)) {
		    continue;
		}
		String valueString = reader.evaluateString(node, "*:" + variable);
		BigDecimal value = null;
		try {
		    if (valueString != null && !valueString.trim().isEmpty()) {
			value = new BigDecimal(valueString);
		    }
		} catch (NumberFormatException e) {
		    logger.info("Not parsable: " + valueString);
		}
		SimpleEntry<Date, BigDecimal> entry = new SimpleEntry<>(date, value);
		ret.add(entry);
	    }
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (XPathExpressionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	GSLoggerFactory.getLogger(getClass()).trace("Downloading from ANA SAR ENDED");

	ret.sort(new Comparator<SimpleEntry<Date, BigDecimal>>() {

	    @Override
	    public int compare(SimpleEntry<Date, BigDecimal> o1, SimpleEntry<Date, BigDecimal> o2) {
		return o2.getKey().compareTo(o1.getKey());
	    }
	});

	return ret;

    }

    private InputStream sendRequestWithRetry(HttpRequest post, int retry) throws Exception {
	Exception e = null;
	for (int j = 0; j < retry; j++) {

	    try {
		// HttpEntity entity = new StringEntity(request, StandardCharsets.UTF_8);
		// post.setEntity(entity);

		GSLoggerFactory.getLogger(getClass()).trace("Downloading from ANA SAR STARTED");
		GSLoggerFactory.getLogger(getClass()).trace(post.uri().toString());

		Downloader executor = new Downloader();

		HttpResponse<InputStream> response = executor.downloadResponse(post);

		int statusCode = response.statusCode();
		if (statusCode != 200) {

		    logger.warn("Status code {}: ", statusCode);
		    logger.warn("Status code not 200, continue");
		    throw new RuntimeException("Status code " + statusCode);

		}
		InputStream ret = response.body();
		return ret;
	    } catch (Exception tmp) {
		logger.info("retrying because: " + tmp.getMessage());
		e = tmp;
	    }

	    Thread.sleep(TimeUnit.SECONDS.toMillis(15));
	}
	if (e != null) {
	    throw e;
	} else {
	    throw new RuntimeException();
	}

    }

    public List<JSONObject> getSeriesInformation(String resId) {
	JSONObject station = getStation(resId);
	Integer tsiId = station.getInt("tsi_id");
	List<ANASARVariable> variables = new ArrayList<>();
	switch (tsiId) {
	case 1:
	case 4:
	case 5:
	    // from reservoir service
	    variables.add(ANASARVariable.COTA);
	    variables.add(ANASARVariable.VOLUME);
	    break;
	case 2:
	    // from SIN service
	    variables.add(ANASARVariable.VOLUME_UTIL);
	    variables.add(ANASARVariable.COTA);
	    variables.add(ANASARVariable.AFLUENCIA);
	    variables.add(ANASARVariable.DEFLUENCIA);
	    break;
	case 3:
	    // from reservoir service
	    variables.add(ANASARVariable.COTA);
	    variables.add(ANASARVariable.VOLUME);
	    // from SIN service
	    variables.add(ANASARVariable.VOLUME_UTIL);
	    variables.add(ANASARVariable.AFLUENCIA);
	    variables.add(ANASARVariable.DEFLUENCIA);
	    break;
	default:
	    break;
	}
	if (station.has("res_capacidade")) {
	    variables.add(ANASARVariable.CAPACIDADE);
	}
	List<JSONObject> ret = new ArrayList<>();

	for (ANASARVariable variable : variables) {
	    Optional<JSONObject> clone = getSeriesInformation(resId, ANASARVariable.decode(variable.getName()));
	    clone.ifPresent(o -> ret.add(o));
	}
	return ret;
    }

    public Optional<JSONObject> getSeriesInformation(String resId, String variable) {
	return getSeriesInformation(resId, variable, true);
    }

    public Optional<JSONObject> getSeriesInformation(String resId, String variable, boolean full) {
	return getSeriesInformation(resId, ANASARVariable.decode(variable), full);
    }

    private Optional<JSONObject> getSeriesInformation(String resId, ANASARVariable variable) {
	return getSeriesInformation(resId, variable, true);
    }

    private Optional<JSONObject> getSeriesInformation(String resId, ANASARVariable variable, boolean full) {

	JSONObject station = getStation(resId);
	if (station == null) {
	    GSLoggerFactory.getLogger(getClass()).warn("No station found for resource id: {}", resId);
	    return Optional.empty();
	}

	JSONObject clone = new JSONObject(station.toString());
	clone.put("variable", variable.getName());
	clone.put("variable_units", variable.getUnits());

	if (full) {
	    ParameterInfo info = null;
	    try {
		info = getParameterInfo(resId, variable.getName());
		if (info == null || info.getBegin() == null) {
		    System.out.println("error");
		}
		String date = ISO8601DateTimeUtils.getISO8601DateTime(info.getBegin());
		clone.put("start_date", date);
		String date2 = ISO8601DateTimeUtils.getISO8601DateTime(info.getEnd());
		clone.put("end_date", date2);
		clone.put("resolution_ms", info.getResolution());
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    clone.put("start_date", "1900-01-01T00:00:00Z");
	    clone.put("end_date", ISO8601DateTimeUtils.getISO8601DateTime(new Date()));
	}
	return Optional.of(clone);
    }
}
