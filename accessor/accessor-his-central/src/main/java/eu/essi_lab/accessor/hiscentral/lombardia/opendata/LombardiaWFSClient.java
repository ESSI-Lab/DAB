package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.ratings.Formula;
import eu.essi_lab.model.ratings.FormulaRange;
import eu.essi_lab.model.ratings.RatingCurve;
import eu.essi_lab.model.ratings.RatingCurves;

/**
 * Client of the ARPA Lombardia hydrological WFS (QGIS Server), publishing the rating curves ("Scale di deflusso") and
 * the hydrometric zeros ("Zeri idrometrici") of the river gauges. Features are linked to the open data sensors by the
 * identifier of the river level sensor.
 *
 * @author boldrini
 */
public class LombardiaWFSClient {

    public static final String DEFAULT_ENDPOINT = "https://idro.arpalombardia.it/ows/2/qdjango/24/";

    public static final String RATING_CURVES_LAYER = "Scale_di_deflusso";
    public static final String RATING_CURVES_SENSOR_FIELD = "IdSensore";

    public static final String HYDROMETRIC_ZEROS_LAYER = "Zeri_idrometrici";
    public static final String HYDROMETRIC_ZEROS_SENSOR_FIELD = "ID";

    /**
     * Rating curves are published as piece-wise formulas; this is the number of points sampled from each curve
     */
    private static final int RATING_CURVE_POINTS = 50;

    /**
     * Marks a period between two curves, without a formula
     */
    private static final String TRANSITION = "transizione";

    private static final String UNTIL_TODAY = "ad oggi";

    private static final DateTimeFormatter PERIOD_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * e.g. "QUOTA ORTOMETRICA - ZERO IDROMETRICO 32,089 m s.l.m."
     */
    private static final Pattern HYDROMETRIC_ZERO = Pattern.compile("ZERO\\s+IDROMETRICO\\s*(-?[\\d.,]+)\\s*m", Pattern.CASE_INSENSITIVE);

    private final String endpoint;

    public LombardiaWFSClient() {
	this(DEFAULT_ENDPOINT);
    }

    public LombardiaWFSClient(String endpoint) {
	this.endpoint = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
    }

    public String getEndpoint() {
	return endpoint;
    }

    /**
     * @return the pieces of all the rating curves, as feature properties
     */
    public List<JSONObject> getRatingCurveFeatures() throws IOException {
	return getFeatures(getFeaturesURL(endpoint, RATING_CURVES_LAYER, null, null));
    }

    /**
     * @return the hydrometric zeros, as feature properties
     */
    public List<JSONObject> getHydrometricZeroFeatures() throws IOException {
	return getFeatures(getFeaturesURL(endpoint, HYDROMETRIC_ZEROS_LAYER, null, null));
    }

    /**
     * @param endpoint the WFS endpoint
     * @param sensorId the river level sensor identifier
     * @return the GetFeature request returning the rating curve pieces of the given sensor
     */
    public static String getRatingCurvesURL(String endpoint, String sensorId) {
	return getFeaturesURL(endpoint, RATING_CURVES_LAYER, RATING_CURVES_SENSOR_FIELD, sensorId);
    }

    private static String getFeaturesURL(String endpoint, String layer, String field, String value) {

	Map<String, String> params = new LinkedHashMap<>();
	params.put("service", "WFS");
	params.put("version", "1.1.0");
	params.put("request", "GetFeature");
	params.put("typename", layer);
	params.put("outputFormat", "application/json");
	if (field != null) {
	    // QGIS Server expression filter
	    params.put("EXP_FILTER", "\"" + field + "\" = " + value);
	}

	StringBuilder url = new StringBuilder(endpoint);
	String separator = endpoint.contains("?") ? "&" : "?";
	for (Map.Entry<String, String> param : params.entrySet()) {
	    url.append(separator).append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
	    separator = "&";
	}
	return url.toString();
    }

    /**
     * @param url a GetFeature request with GeoJSON output
     * @return the properties of the returned features, with the point coordinates added as {@code lon} and
     *         {@code lat}
     */
    public static List<JSONObject> getFeatures(String url) throws IOException {

	String body = new String(download(url), StandardCharsets.UTF_8);
	JSONArray features = new JSONObject(body).optJSONArray("features");

	List<JSONObject> ret = new ArrayList<>();
	if (features == null) {
	    return ret;
	}
	for (int i = 0; i < features.length(); i++) {
	    JSONObject feature = features.getJSONObject(i);
	    JSONObject properties = feature.optJSONObject("properties");
	    if (properties == null) {
		continue;
	    }
	    JSONObject geometry = feature.optJSONObject("geometry");
	    if (geometry != null && "Point".equals(geometry.optString("type"))) {
		JSONArray coordinates = geometry.getJSONArray("coordinates");
		properties.put("lon", coordinates.getBigDecimal(0));
		properties.put("lat", coordinates.getBigDecimal(1));
	    }
	    ret.add(properties);
	}
	return ret;
    }

    /**
     * Reads the hydrometric zero from the monograph of the gauge, the PDF linked by the {@code download} field of the
     * hydrometric zeros layer
     *
     * @param pdfURL the gauge monograph
     * @return the orthometric height of the hydrometric zero, in m a.s.l., if found
     */
    public static Optional<BigDecimal> readHydrometricZero(String pdfURL) {

	try (PDDocument document = Loader.loadPDF(download(pdfURL))) {

	    return parseHydrometricZero(new PDFTextStripper().getText(document));

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(LombardiaWFSClient.class).warn("Unable to read hydrometric zero from {}: {}", pdfURL, e.getMessage());
	}
	return Optional.empty();
    }

    static Optional<BigDecimal> parseHydrometricZero(String text) {

	Matcher matcher = HYDROMETRIC_ZERO.matcher(text);
	if (!matcher.find()) {
	    return Optional.empty();
	}
	String value = matcher.group(1);
	if (value.contains(",")) {
	    // Italian decimal separator, possibly with thousands separators
	    value = value.replace(".", "").replace(',', '.');
	}
	try {
	    return Optional.of(new BigDecimal(value));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * Builds the rating curves of a sensor from the pieces published by the WFS. Pieces sharing the validity period
     * and the curve code are the level ranges of the same curve; transition periods, which have no formula, are
     * skipped. Levels are in meters, discharges in m3/s.
     *
     * @param features the rating curve pieces of a single sensor
     */
    public static RatingCurves toRatingCurves(List<JSONObject> features) {

	Map<String, List<JSONObject>> piecesByCurve = new LinkedHashMap<>();
	String sensorId = null;
	String name = null;
	for (JSONObject feature : features) {
	    sensorId = feature.opt(RATING_CURVES_SENSOR_FIELD) == null ? sensorId : feature.get(RATING_CURVES_SENSOR_FIELD).toString();
	    name = feature.optString("Nome", name);
	    String equation = feature.optString("EQUATION").trim();
	    if (equation.isEmpty() || equation.equalsIgnoreCase(TRANSITION)) {
		continue;
	    }
	    String key = feature.optString("periodo") + "|" + feature.optString("codice");
	    piecesByCurve.computeIfAbsent(key, k -> new ArrayList<>()).add(feature);
	}

	List<RatingCurve> curves = new ArrayList<>();
	for (List<JSONObject> pieces : piecesByCurve.values()) {

	    String period = pieces.get(0).optString("periodo");
	    LocalDate[] dates = parsePeriod(period);

	    Formula formula = new Formula();
	    double min = Double.NaN;
	    double max = Double.NaN;
	    for (JSONObject piece : pieces) {
		FormulaRange range = parseRange(piece.optString("range"), piece.optString("EQUATION"));
		if (range == null) {
		    GSLoggerFactory.getLogger(LombardiaWFSClient.class).warn("Unable to parse rating curve piece of sensor {}: {} {}",
			    sensorId, piece.optString("range"), piece.optString("EQUATION"));
		    continue;
		}
		formula.addRange(range);
		min = Double.isNaN(min) ? range.getLowerBound() : Math.min(min, range.getLowerBound());
		max = Double.isNaN(max) ? range.getUpperBound() : Math.max(max, range.getUpperBound());
	    }
	    if (formula.isEmpty() || !Double.isFinite(min) || !Double.isFinite(max)) {
		continue;
	    }

	    RatingCurve curve = new RatingCurve(dates[0], dates[1]);
	    curve.setFormula(formula);
	    curve.setMinLevel(min);
	    curve.setMaxLevel(max);
	    curve.setNumberOfPoints(RATING_CURVE_POINTS);
	    curve.generatePoints();
	    curves.add(curve);
	}

	curves.sort(Comparator.comparing(RatingCurve::getBeginDate, Comparator.nullsFirst(Comparator.naturalOrder())));

	RatingCurves ret = new RatingCurves(sensorId, name);
	ret.getCurves().addAll(curves);
	return ret;
    }

    /**
     * @param range e.g. "0.2  < = h(m) < =  2.34"
     * @param equation e.g. "Q=36.092*(h-0.089)^1.45"
     */
    static FormulaRange parseRange(String range, String equation) {
	return FormulaRange.parse(range.replace("(m)", ""), equation);
    }

    /**
     * @param period e.g. "2017/06/13 - 2019/11/25" or "2021/01/01 - ad oggi"
     * @return begin and end dates, the end is null for curves still in use
     */
    static LocalDate[] parsePeriod(String period) {

	LocalDate[] ret = new LocalDate[2];
	String[] split = period.split(" - ");
	ret[0] = parseDate(split[0]);
	ret[1] = split.length > 1 ? parseDate(split[1]) : null;
	return ret;
    }

    private static LocalDate parseDate(String date) {
	String value = date.trim();
	if (value.isEmpty() || value.equalsIgnoreCase(UNTIL_TODAY)) {
	    return null;
	}
	try {
	    return LocalDate.parse(value, PERIOD_DATE);
	} catch (Exception e) {
	    return null;
	}
    }

    private static byte[] download(String url) throws IOException {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 60);
	downloader.setResponseTimeout(TimeUnit.SECONDS, 120);
	downloader.setRetryPolicy(r -> r.statusCode() >= 500, 3, TimeUnit.SECONDS, 10);

	try {
	    HttpResponse<InputStream> response = downloader.downloadResponse(url);
	    byte[] body = IOStreamUtils.getBytes(response.body());
	    if (response.statusCode() != 200) {
		throw new IOException("HTTP " + response.statusCode() + " from " + url);
	    }
	    return body;
	} catch (IOException e) {
	    throw e;
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    throw new IOException("Interrupted while getting " + url, e);
	} catch (Exception e) {
	    throw new IOException("Unable to get " + url + ": " + e.getMessage(), e);
	}
    }
}
