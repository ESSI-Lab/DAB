package eu.essi_lab.accessor.cehq;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.*;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import com.sun.xml.bind.*;
import eu.essi_lab.lib.xml.*;
import org.apache.commons.io.IOUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class CEHQClient {
    private static final String CEHQ_CLIENT_ERROR = "CEHQ_CLIENT_ERROR";
    private String endpoint = "https://www.cehq.gouv.qc.ca/hydrometrie/historique_donnees/default.asp";

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    public List<String> getStationIdentifiers() throws IOException {
	byte[] response = downloadBytes(getEndpoint());
	String str = new String(response, Charset.forName("windows-1252"));
	List<String> ret = new ArrayList<>();
	str = str.substring(str.indexOf("id=\"lstStation\""));
	str = str.substring(0, str.indexOf("select"));
	String[] split = str.split("value=");
	for (int i = 1; i < split.length; i++) {
	    String valueString = split[i];
	    valueString = valueString.substring(valueString.indexOf("\"") + 1);
	    valueString = valueString.substring(0, valueString.indexOf("\""));
	    valueString = valueString.trim();
	    ret.add(valueString);
	}

	return ret;
    }

    public enum CEHQProperty {
	ID, // Numéro de la station : 080718
	STATION_NAME, // Nom de la station : Waswanipi
	DESCRIPTION, // Description : À la tête de la chute Rouge
	STATE, // État : Station ouverte
	PERIOD, // Période(s) d'activité : De mai 1966 à aujourd'hui
	MUNICIPALITY, // Municipalité : Gouvernement régional d'Eeyou Istchee Baie-James
	REGION, // Région administrative : Nord-du-Québec
	RIVER, // Lac ou cours d'eau : Waswanipi, Rivière
	BASIN, // Région hydrographique : Baies de Hannah et de Rupert
	DRAINAGE_AREA, // Bassin versant à la station : 31 964 km²
	FLOW_REGIME, // Régime d'écoulement : Naturel
	FEDERAL_ID, // Numéro fédéral de la station : 03AB002
	REFERENCE_SYSTEM, // Système de référence : Arbitraire

	// CALCULATED
	LAT, //
	LON, //
	TIME_START, //
	TIME_END, //

    }

    public Map<CEHQProperty, String> getStationProperties(String stationId) throws Exception {
	String url = "https://www.cehq.gouv.qc.ca/hydrometrie/historique_donnees/fiche_station.asp?NoStation=" + stationId;
	XMLDocumentReader reader = readHTMLPage(url);

	Map<CEHQProperty, String> ret = new HashMap<>();

	Node[] nodes = reader.evaluateNodes("//*:tr[count(*:td)=2]");
	for (Node node : nodes) {
	    String key = reader.evaluateString(node, "*:td[1]").trim();
	    CEHQProperty property = null;
	    if (key.contains("Numéro de la station")) {
		property = CEHQProperty.ID;
	    } else if (key.contains("Nom de la station")) {
		property = CEHQProperty.STATION_NAME;
	    } else if (key.contains("Description")) {
		property = CEHQProperty.DESCRIPTION;
	    } else if (key.contains("État")) {
		property = CEHQProperty.STATE;
	    } else if (key.contains("Période(s) d'activité")) {
		property = CEHQProperty.PERIOD;
	    } else if (key.contains("Municipalité")) {
		property = CEHQProperty.MUNICIPALITY;
	    } else if (key.contains("Région administrative")) {
		property = CEHQProperty.REGION;
	    } else if (key.contains("Lac ou cours d'eau")) {
		property = CEHQProperty.RIVER;
	    } else if (key.contains("Région administrative")) {
		property = CEHQProperty.REGION;
	    } else if (key.contains("Région hydrographique")) {
		property = CEHQProperty.BASIN;
	    } else if (key.contains("Bassin versant à la station")) {
		property = CEHQProperty.DRAINAGE_AREA;
	    } else if (key.contains("Régime d'écoulement")) {
		property = CEHQProperty.FLOW_REGIME;
	    } else if (key.contains("Numéro fédéral")) {
		property = CEHQProperty.FEDERAL_ID;
	    }
	    String value = reader.evaluateString(node, "*:td[2]").trim();
	    if (property != null) {
		ret.put(property, value);
	    }
	}

	Map<CEHQVariable, SimpleEntry<Integer, Integer>> tmp = getAvailableYearsByVariable(stationId);

	if (!tmp.isEmpty()) {
	    CEHQVariable var = tmp.keySet().iterator().next();
	    SimpleEntry<Integer, Integer> years = tmp.get(var);
	    Integer min = years.getKey();
	    Integer max = years.getValue();
	    CEHQData minData = getData(stationId, var, min);
	    CEHQData maxData = getData(stationId, var, max);

	    ret.put(CEHQProperty.TIME_START, ISO8601DateTimeUtils.getISO8601DateTime(minData.getFirstDate()));
	    ret.put(CEHQProperty.TIME_END, ISO8601DateTimeUtils.getISO8601DateTime(maxData.getLastDate()));

	    ret.put(CEHQProperty.LAT, "" + maxData.getLatitude());
	    ret.put(CEHQProperty.LON, "" + maxData.getLongitude());

	}

	return ret;
    }

    public enum CEHQVariable {
	Q("Débit", "m³/s"), N("Niveau", "m");

	String label;

	public String getLabel() {
	    return label;
	}

	public String getUnits() {
	    return units;
	}

	String units;

	CEHQVariable(String label, String units) {
	    this.label = label;
	    this.units = units;
	}

	public static CEHQVariable decode(String parameterCode) {
	    for (CEHQVariable var : values()) {
		if (parameterCode.equals(var.name())) {
		    return var;
		}
	    }
	    return null;
	}

    }

    public Map<CEHQVariable, SimpleEntry<Date, Date>> getTimeSeriesTemporalExtent(String stationId) throws Exception {

	Map<CEHQVariable, SimpleEntry<Integer, Integer>> tmp = getAvailableYearsByVariable(stationId);
	Set<Entry<CEHQVariable, SimpleEntry<Integer, Integer>>> entrySet = tmp.entrySet();
	Map<CEHQVariable, SimpleEntry<Date, Date>> ret = new HashMap<>();
	for (Entry<CEHQVariable, SimpleEntry<Integer, Integer>> entry : entrySet) {
	    CEHQVariable var = entry.getKey();
	    SimpleEntry<Integer, Integer> range = entry.getValue();
	    Integer minYear = range.getKey();
	    SimpleEntry<Date, Date> minRange = getRangebyYear(stationId, var, minYear);
	    Integer maxYear = range.getValue();
	    SimpleEntry<Date, Date> maxRange = getRangebyYear(stationId, var, maxYear);
	    SimpleEntry<Date, Date> dateRange = new SimpleEntry<>(minRange.getKey(), maxRange.getValue());
	    ret.put(var, dateRange);
	}

	return ret;
    }

    public Map<CEHQVariable, SimpleEntry<Integer, Integer>> getAvailableYearsByVariable(String stationId) throws Exception {

	String url = "https://www.cehq.gouv.qc.ca/hydrometrie/historique_donnees/fiche_instantanee.asp?NoStation=" + stationId;
	XMLDocumentReader reader = readHTMLPage(url);
	Node[] nodes = reader.evaluateNodes("//*:a[contains(@href,'historique_donnees_instantanees')]");

	Map<CEHQVariable, SimpleEntry<Integer, Integer>> tmp = new HashMap<>(); // first year, last year for each
	// available variable
	for (Node node : nodes) {
	    String href = reader.evaluateString(node, "@href");
	    href = href.substring(href.lastIndexOf("/") + 1, href.lastIndexOf(".")); // 080718_N_1997
	    String[] split = href.split("_");
	    String variable = split[1];
	    Integer year = Integer.parseInt(split[2]);

	    CEHQVariable var = null;
	    switch (variable) {
	    case "Q":
		var = CEHQVariable.Q;
		break;
	    case "N":
		var = CEHQVariable.N;
		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).error("Unexpected variable: {}", variable);
		break;
	    }
	    if (var != null) {
		SimpleEntry<Integer, Integer> existing = tmp.get(var);
		if (existing == null) {
		    existing = new SimpleEntry<>(year, year);
		}
		Integer min = existing.getKey();
		Integer max = existing.getValue();
		if (year < min) {
		    min = year;
		}
		if (year > max) {
		    max = year;
		}
		existing = new SimpleEntry<>(min, max);
		tmp.put(var, existing);
	    }
	}

	Set<CEHQVariable> vars = tmp.keySet();
	for (CEHQVariable var : vars) {
	    SimpleEntry<Integer, Integer> years = tmp.get(var);
	    Integer min = years.getKey();
	    Integer max = years.getValue();

	    while (true) {
		CEHQData data = getData(stationId, var, min);
		if (data.getFirstDate() != null) {
		    break;
		}
		min++;
		if (min > max) {
		    break;
		}
	    }
	    while (true) {
		CEHQData data = getData(stationId, var, max);
		if (data.getLastDate() != null) {
		    break;
		}
		max--;
		if (min > max) {
		    break;
		}
	    }
	    tmp.put(var, new SimpleEntry<>(min, max));

	}

	return tmp;
    }

    public SimpleEntry<Date, Date> getRangebyYear(String stationId, CEHQVariable var, Integer year) throws GSException {
	List<SimpleEntry<Date, BigDecimal>> data = getData(stationId, var, year).getValues();
	Date min = data.get(0).getKey();
	Date max = data.get(data.size() - 1).getKey();
	SimpleEntry<Date, Date> ret = new SimpleEntry<>(min, max);
	return ret;
    }

    public CEHQData getData(String stationId, CEHQVariable var, Integer year) throws GSException {

	String url = "https://www.cehq.gouv.qc.ca/depot/historique_donnees_instantanees/" + stationId + "_" + var.name() + "_" + year
		+ ".txt";

	try {
	    byte[] bytes = downloadBytes(url);
	    String string = new String(bytes, Charset.forName("windows-1252"));
	    return parseData(string);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CEHQ_CLIENT_ERROR, //
		    e//
	    );
	}

    }

    private CEHQData parseData(String str) {

	CEHQData data = new CEHQData();

	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();

	String[] split = str.split("\n");
	boolean dataStart = false;
	for (int i = 0; i < split.length; i++) {
	    String string = split[i];
	    if (dataStart) {
		string = string.substring(string.indexOf(" ")).trim();
		String date = string.substring(0, 16).trim();
		String value = string.substring(17).trim();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d;
		try {
		    d = df.parse(date);
		    BigDecimal v = null;
		    try {
			v = new BigDecimal(value);
			ret.add(new SimpleEntry<>(d, v));
		    } catch (Exception e) {
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    if (string.startsWith("Station    ")) {
		dataStart = true;
	    }
	    if (string.startsWith("Coordonnées:")) {
		string = string.replace("Coordonnées:", "").trim();
		if (string.contains(")")) {
		    string = string.substring(string.indexOf(")") + 1).trim();
		}
		if (string.contains("//")) {
		    String[] split2 = string.split("//");
		    Double lat = toDecimal(split2[0]);
		    Double lon = toDecimal(split2[1]);
		    data.setLatitude(lat);
		    data.setLongitude(lon);
		}
	    }
	}

	ret.sort(new Comparator<SimpleEntry<Date, BigDecimal>>() {

	    @Override
	    public int compare(SimpleEntry<Date, BigDecimal> arg0, SimpleEntry<Date, BigDecimal> arg1) {
		return arg0.getKey().compareTo(arg1.getKey());
	    }
	});

	data.setValues(ret);

	return data;
    }

    private Double toDecimal(String dms) {
	// 49° 51' 27"
	dms = dms.replace("°", "").replace("'", "").replace("\"", "").trim();
	String[] split = dms.split(" ");
	double d = Double.parseDouble(split[0]);
	Integer minutes = Integer.parseInt(split[1]);
	Integer seconds = Integer.parseInt(split[2]) + minutes * 60;
	Double remainder = seconds.doubleValue() / 3600.0;
	return d + remainder;
    }

    @XmlTransient

    private static ExpiringCache<byte[]> downloadCache;

    static {
	downloadCache = new ExpiringCache<>();
	downloadCache.setDuration(240000);
	downloadCache.setMaxSize(50);
    }

    private synchronized XMLDocumentReader readHTMLPage(String url) throws Exception {
	byte[] response = downloadBytes(url);

	Parser parser = new Parser();

	parser.setFeature(Parser.namespacesFeature, false);
	parser.setFeature(Parser.namespacePrefixesFeature, false);

	TransformerFactory factory = XMLFactories.newTransformerFactory();

	Transformer transformer = factory.newTransformer();
	DOMResult result = new DOMResult();
	InputStream s = new ByteArrayInputStream(response);
	Reader rdr = new InputStreamReader(s, Charset.forName("windows-1252"));
	transformer.transform(new SAXSource(parser, new InputSource(rdr)), result);
	s.close();
	XMLDocumentReader reader = new XMLDocumentReader((Document) result.getNode());
	return reader;
    }

    private byte[] downloadBytes(String url) throws IOException {
	GSLoggerFactory.getLogger(getClass()).info("Checking cache for url: {}", url);
	byte[] ret = downloadCache.get(url);
	if (ret != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Found in cache url: {}", url);
	    return ret;
	}
	GSLoggerFactory.getLogger(getClass()).info("NOT found in cache url: {}", url);
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(url);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	InputStream ss = stream.get();
	IOUtils.copy(ss, baos);
	ss.close();
	ret = baos.toByteArray();
	baos.close();
	GSLoggerFactory.getLogger(getClass()).info("Put in cache url: {}", url);
	downloadCache.put(url, ret);
	return ret;
    }

}
