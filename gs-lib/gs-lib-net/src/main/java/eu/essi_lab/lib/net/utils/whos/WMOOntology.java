package eu.essi_lab.lib.net.utils.whos;

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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;

public class WMOOntology {

    public enum CommonWMOUnit {

	ACCELERATION_DUE_TO_GRAVITY("http://codes.wmo.int/common/unit/g", "acceleration due to gravity", "g"), //
	AMPERE("http://codes.wmo.int/common/unit/A", "ampere", "A"), //
	ASTRONOMIC_UNIT("http://codes.wmo.int/common/unit/AU", "astronomic unit", "AU"), //
	ATOMIC_MASS_UNIT("http://codes.wmo.int/common/unit/u", "atomic mass unit", "u"), //
	ATTO("http://codes.wmo.int/common/unit/a_pref", "atto", "a"), //
	BECQUEREL("http://codes.wmo.int/common/unit/Bq", "becquerel", "Bq"), //
	BECQUERELS_PER_CUBIC_METRE("http://codes.wmo.int/common/unit/Bq_m-3", "becquerels per cubic metre", "Bq m-3"), //
	BECQUERELS_PER_LITRE("http://codes.wmo.int/common/unit/Bq_l-1", "becquerels per litre", "Bq/l"), //
	BECQUERELS_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/Bq_m-2", "becquerels per square metre", "Bq m-2"), //
	BECQUEREL_SECONDS_PER_CUBIC_METRE("http://codes.wmo.int/common/unit/Bq_s_m-3", "becquerel seconds per cubic metre", "Bq s m-3"), //
	CANDELA("http://codes.wmo.int/common/unit/cd", "candela", "cd"), //
	CENTI("http://codes.wmo.int/common/unit/c_pref", "centi", "c"), //
	CENTIBARS_PER_12_HOURS("http://codes.wmo.int/common/unit/cb_-1", "centibars per 12 hours", "cb/12 h"), //
	CENTIBARS_PER_SECOND("http://codes.wmo.int/common/unit/cb_s-1", "centibars per second", "cb/s"), //
	CENTIMETRE("http://codes.wmo.int/common/unit/cm", "centimetre", "cm", "centimeter"), //
	CENTIMETRES_PER_HOUR("http://codes.wmo.int/common/unit/cm_h-1", "centimetres per hour", "cm/h"), //
	CENTIMETRES_PER_SECOND("http://codes.wmo.int/common/unit/cm_s-1", "centimetres per second", "cm/s"), //
	COULOMB("http://codes.wmo.int/common/unit/C", "coulomb", "C"), //
	CUBIC_METRES("http://codes.wmo.int/common/unit/m3", "cubic metres", "m3"), //
	CUBIC_METRES_PER_CUBIC_METRE("http://codes.wmo.int/common/unit/m3_m-3", "cubic metres per cubic metre", "m3 m-3"), //
	CUBIC_METRES_PER_SECOND("http://codes.wmo.int/common/unit/m3_s-1", "cubic metres per second", "m3/s", "m³/s", "cms",
		"cubic meters per second", "cubic metres per second", "cubic metre per second", "cubic meter per second",
		"metros cúbicos por segundo"), //
	DAY("http://codes.wmo.int/common/unit/d", "day", "d"), //
	DECA("http://codes.wmo.int/common/unit/da_pref", "deca", "da"), //
	DECI("http://codes.wmo.int/common/unit/d_pref", "deci", "d"), //
	DECIBELS_PER_DEGREE("http://codes.wmo.int/common/unit/dB_deg-1", "decibels per degree", "dB/deg"), //
	DECIBELS_PER_METRE("http://codes.wmo.int/common/unit/dB_m-1", "decibels per metre", "dB/m"), //
	DECIBEL_6("http://codes.wmo.int/common/unit/dB", "decibel (6)", "dB"), //
	DECIMETRE("http://codes.wmo.int/common/unit/dm", "decimetre", "dm"), //
	DECIPASCALS_PER_SECOND_MICROBAR_PER_SECOND("http://codes.wmo.int/common/unit/dPa_s-1",
		"decipascals per second (microbar per second)", "dPa/s"), //
	DEGREES_CELSIUS_8("http://codes.wmo.int/common/unit/degC", "degrees Celsius (8)", "C"), //
	DEGREES_CELSIUS_PER_100_METRES("http://codes.wmo.int/common/unit/C_-1", "degrees Celsius per 100 metres", "C/100 m"), //
	DEGREES_CELSIUS_PER_METRE("http://codes.wmo.int/common/unit/C_m-1", "degrees Celsius per metre", "C/m"), //
	DEGREES_PER_SECOND("http://codes.wmo.int/common/unit/deg_s-1", "degrees per second", "deg/s"), //
	DEGREES_TRUE("http://codes.wmo.int/common/unit/degrees_true", "degrees true", "deg"), //
	DEGREE_ANGLE("http://codes.wmo.int/common/unit/degree_(angle)", "degree (angle)", "deg", "grados", "° (gr)"), //
	DEGREE_CELSIUS("http://codes.wmo.int/common/unit/Cel", "degree Celsius", "Cel", "˚C", "°C", "degrees Celsius",
		"grados centígrados"), //
	DEKAPASCAL("http://codes.wmo.int/common/unit/daPa", "dekapascal", "daPa"), //
	DIMENSIONLESS("http://codes.wmo.int/common/unit/1", "Dimensionless", "1"), //
	DOBSON_UNIT_9("http://codes.wmo.int/common/unit/DU", "Dobson Unit (9)", "DU"), //
	EIGHTHS_OF_CLOUD("http://codes.wmo.int/common/unit/okta", "eighths of cloud", "okta"), //
	ELECTRON_VOLT("http://codes.wmo.int/common/unit/eV", "electron volt", "eV"), //
	EXA("http://codes.wmo.int/common/unit/E_pref", "exa", "E"), //
	FARAD("http://codes.wmo.int/common/unit/F", "farad", "F"), //
	FEMTO("http://codes.wmo.int/common/unit/f_pref", "femto", "f"), //
	FOOT("http://codes.wmo.int/common/unit/ft", "foot", "ft"), //
	GEOPOTENTIAL_METRE("http://codes.wmo.int/common/unit/gpm", "geopotential metre", "gpm"), //
	GIGA("http://codes.wmo.int/common/unit/G_pref", "giga", "G"), //
	GRAMS_PER_KILOGRAM("http://codes.wmo.int/common/unit/g_kg-1", "grams per kilogram", "g/kg"), //
	GRAMS_PER_KILOGRAM_PER_SECOND("http://codes.wmo.int/common/unit/g_kg-1_s-1", "grams per kilogram per second", "g kg-1 s-1"), //
	GRAY("http://codes.wmo.int/common/unit/Gy", "gray", "Gy"), //
	HECTARE("http://codes.wmo.int/common/unit/ha", "hectare", "ha"), //
	HECTO("http://codes.wmo.int/common/unit/h_pref", "hecto", "h"), //
	HECTOPASCAL("http://codes.wmo.int/common/unit/hPa", "hectopascal", "hPa", "hectoPascales", "miliBares", "mB"), //
	HECTOPASCALS_PER_3_HOURS("http://codes.wmo.int/common/unit/hPa_-1", "hectopascals per 3 hours", "hPa/3 h"), //
	HECTOPASCALS_PER_HOUR("http://codes.wmo.int/common/unit/hPa_h-1", "hectopascals per hour", "hPa/h"), //
	HECTOPASCALS_PER_SECOND("http://codes.wmo.int/common/unit/hPa_s-1", "hectopascals per second", "hPa/s"), //
	HENRY("http://codes.wmo.int/common/unit/H", "henry", "H"), //
	HERTZ("http://codes.wmo.int/common/unit/Hz", "hertz", "Hz"), //
	HOUR("http://codes.wmo.int/common/unit/h", "hour", "h"), //
	JOULE("http://codes.wmo.int/common/unit/J", "joule", "J"), //
	JOULES_PER_KILOGRAM("http://codes.wmo.int/common/unit/J_kg-1", "joules per kilogram", "J/kg"), //
	JOULES_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/J_m-2", "joules per square metre", "J m-2"), //
	KELVIN("http://codes.wmo.int/common/unit/K", "kelvin", "K"), //
	KELVINS_PER_METRE("http://codes.wmo.int/common/unit/K_m-1", "kelvins per metre", "K/m"), //
	KELVIN_METRES_PER_SECOND("http://codes.wmo.int/common/unit/K_m_s-1", "kelvin metres per second", "K m s-1"), //
	KELVIN_SQUARE_METRES_PER_KILOGRAM_PER_SECOND("http://codes.wmo.int/common/unit/K_m2_kg-1_s-1",
		"kelvin square metres per kilogram per second", "K m2 kg-1 s-1"), //
	KILO("http://codes.wmo.int/common/unit/k_pref", "kilo", "k"), //
	KILOGRAM("http://codes.wmo.int/common/unit/kg", "kilogram", "kg"), //
	KILOGRAMS_PER_CUBIC_METRE("http://codes.wmo.int/common/unit/kg_m-3", "kilograms per cubic metre", "kg m-3"), //
	KILOGRAMS_PER_KILOGRAM("http://codes.wmo.int/common/unit/kg_kg-1", "kilograms per kilogram", "kg/kg"), //
	KILOGRAMS_PER_KILOGRAM_PER_SECOND("http://codes.wmo.int/common/unit/kg_kg-1_s-1", "kilograms per kilogram per second",
		"kg kg-1 s-1"), //
	KILOGRAMS_PER_METRE("http://codes.wmo.int/common/unit/kg_m-1", "kilograms per metre", "kg/m"), //
	KILOGRAMS_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/kg_m-2", "kilograms per square metre", "kg m-2"), //
	KILOGRAMS_PER_SQUARE_METRE_PER_SECOND("http://codes.wmo.int/common/unit/kg_m-2_s-1", "kilograms per square metre per second",
		"kg m-2 s-1"), //
	KILOMETRE("http://codes.wmo.int/common/unit/km", "kilometre", "km"), //
	KILOMETRES_PER_DAY("http://codes.wmo.int/common/unit/km_d-1", "kilometres per day", "km/d"), //
	KILOMETRES_PER_HOUR("http://codes.wmo.int/common/unit/km_h-1", "kilometres per hour", "km/h", "kilómetros por hora"), //
	KILOPASCAL("http://codes.wmo.int/common/unit/kPa", "kilopascal", "kPa"), //
	KNOT("http://codes.wmo.int/common/unit/kt", "knot", "kt"), //
	KNOTS_PER_1000_METRES("http://codes.wmo.int/common/unit/kt_km-1", "knots per 1000 metres", "kt/km"), //
	LITRE("http://codes.wmo.int/common/unit/l", "litre", "l"), //
	LOGARITHM_PER_METRE("http://codes.wmo.int/common/unit/log_(m-1)", "logarithm per metre", "log (m-1)"), //
	LOGARITHM_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/log_(m-2)", "logarithm per square metre", "log (m-2)"), //
	LUMEN("http://codes.wmo.int/common/unit/lm", "lumen", "lm"), //
	LUX("http://codes.wmo.int/common/unit/lx", "lux", "lx"), //
	MEGA("http://codes.wmo.int/common/unit/M_pref", "mega", "M"), //
	METRE("http://codes.wmo.int/common/unit/m", "metre", "m", "metros", "meters"), //
	METRES_PER_SECOND("http://codes.wmo.int/common/unit/m_s-1", "metres per second", "m/s", "metros por segundo"), //
	METRES_PER_SECOND_PER_1000_METRES("http://codes.wmo.int/common/unit/m_s-1_km-1", "metres per second per 1000 metres", "m s-1/km"), //
	METRES_PER_SECOND_PER_METRE("http://codes.wmo.int/common/unit/m_s-1_m-1", "metres per second per metre", "m s-1/m"), //
	METRES_PER_SECOND_SQUARED("http://codes.wmo.int/common/unit/m_s-2", "metres per second squared", "m s-2"), //
	METRES_TO_THE_FOURTH_POWER("http://codes.wmo.int/common/unit/m4", "metres to the fourth power", "m4"), //
	METRES_TO_THE_TWO_THIRDS_POWER_PER_SECOND("http://codes.wmo.int/common/unit/m2_-1", "metres to the two thirds power per second",
		"m2/3 s-1"), //
	MICRO("http://codes.wmo.int/common/unit/u_pref", "micro", "u"), //
	MILLI("http://codes.wmo.int/common/unit/m_pref", "milli", "m"), //
	MILLIMETRE("http://codes.wmo.int/common/unit/mm", "millimetre", "mm", "milímetros", "millimeters", "millimetres", "millimeter",
		"millimetre"), //
	MILLIMETRES_PER_HOUR("http://codes.wmo.int/common/unit/mm_h-1", "millimetres per hour", "mm/h"), //
	MILLIMETRES_PER_SECONDS("http://codes.wmo.int/common/unit/mm_s-1", "millimetres per seconds", "mm/s"), //
	MILLIMETRES_PER_THE_SIXTH_POWER_PER_CUBIC_METRE("http://codes.wmo.int/common/unit/mm6_m-3",
		"millimetres per the sixth power per cubic metre", "mm6 m-3"), //
	MILLISIEVERT("http://codes.wmo.int/common/unit/mSv", "millisievert", "mSv"), //
	MINUTE_ANGLE("http://codes.wmo.int/common/unit/'", "minute (angle)", "'"), //
	MINUTE_TIME("http://codes.wmo.int/common/unit/min", "minute (time)", "min"), //
	MOLE("http://codes.wmo.int/common/unit/mol", "mole", "mol"), //
	MOLES_PER_MOLE("http://codes.wmo.int/common/unit/mol_mol-1", "moles per mole", "mol/mol"), //
	MONTH("http://codes.wmo.int/common/unit/mon", "month", "mon"), //
	NANO("http://codes.wmo.int/common/unit/n_pref", "nano", "n"), //
	NANOBAR_HPA_106("http://codes.wmo.int/common/unit/nbar", "nanobar = hPa 10^-6", "nbar"), //
	NEWTON("http://codes.wmo.int/common/unit/N", "newton", "N"), //
	NEWTONS_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/N_m-2", "newtons per square metre", "N m-2"), //
	N_UNITS("http://codes.wmo.int/common/unit/N_units", "N units", "N units"), //
	OHM("http://codes.wmo.int/common/unit/Ohm", "ohm", "Ohm"), //
	PARSEC("http://codes.wmo.int/common/unit/pc", "parsec", "pc"), //
	PARTS_PER_THOUSAND("http://codes.wmo.int/common/unit/0.001", "parts per thousand", "0/00"), //
	PASCAL("http://codes.wmo.int/common/unit/Pa", "pascal", "Pa"), //
	PASCALS_PER_SECOND("http://codes.wmo.int/common/unit/Pa_s-1", "pascals per second", "Pa/s"), //
	PER_CENT("http://codes.wmo.int/common/unit/percent", "per cent", "%", "porcentaje"), //
	PER_METRE("http://codes.wmo.int/common/unit/m-1", "per metre", "m-1"), //
	PER_SECOND_SAME_AS_HERTZ("http://codes.wmo.int/common/unit/s-1", "per second (same as hertz)", "/s"), //
	PER_SECOND_SQUARED("http://codes.wmo.int/common/unit/s-2", "per second squared", "s-2"), //
	PER_SQUARE_KILOGRAM_PER_SECOND("http://codes.wmo.int/common/unit/kg-2_s-1", "per square kilogram per second", "kg-2 s-1"), //
	PETA("http://codes.wmo.int/common/unit/P_pref", "peta", "P"), //
	PH_UNIT("http://codes.wmo.int/common/unit/pH_unit", "pH unit", "pH unit"), //
	PICO("http://codes.wmo.int/common/unit/p_pref", "pico", "p"), //
	RADIAN("http://codes.wmo.int/common/unit/rad", "radian", "rad"), //
	RADIANS_PER_METRE("http://codes.wmo.int/common/unit/rad_m-1", "radians per metre", "rad/m"), //
	SECOND("http://codes.wmo.int/common/unit/s", "second", "s"), //
	SECONDS_PER_METRE("http://codes.wmo.int/common/unit/s_m-1", "seconds per metre", "s/m"), //
	SECOND_ANGLE("http://codes.wmo.int/common/unit/''", "second (angle)", "''"), //
	SIEMENS("http://codes.wmo.int/common/unit/S", "siemens", "S"), //
	SIEMENS_PER_METRE("http://codes.wmo.int/common/unit/S_m-1", "siemens per metre", "S/m"), //
	SIEVERT("http://codes.wmo.int/common/unit/Sv", "sievert", "Sv"), //
	SQUARE_DEGREES("http://codes.wmo.int/common/unit/deg2", "square degrees", "deg^2"), //
	SQUARE_METRES("http://codes.wmo.int/common/unit/m2", "square metres", "m2"), //
	SQUARE_METRES_PER_HERTZ("http://codes.wmo.int/common/unit/m2_Hz-1", "square metres per hertz", "m2/Hz"), //
	SQUARE_METRES_PER_RADIAN_SQUARED("http://codes.wmo.int/common/unit/m2_rad-1_s", "square metres per radian squared", "m2 rad-1 s"), //
	SQUARE_METRES_PER_SECOND("http://codes.wmo.int/common/unit/m2_s-1", "square metres per second", "m2/s"), //
	SQUARE_METRES_PER_SECOND_SQUARED("http://codes.wmo.int/common/unit/m2_s-2", "square metres per second squared", "m2 s-2"), //
	SQUARE_METRES_SECOND("http://codes.wmo.int/common/unit/m2_s", "square metres second", "m2 s"), //
	STERADIAN("http://codes.wmo.int/common/unit/sr", "steradian", "sr"), //
	TERA("http://codes.wmo.int/common/unit/T_pref", "tera", "T"), //
	TESLA("http://codes.wmo.int/common/unit/T", "tesla", "T"), //
	TONNE("http://codes.wmo.int/common/unit/t", "tonne", "t"), //
	VOLT("http://codes.wmo.int/common/unit/V", "volt", "V"), //
	WATT("http://codes.wmo.int/common/unit/W", "watt", "W"), //
	WATTS_PER_CUBIC_METRE_PER_STERADIAN("http://codes.wmo.int/common/unit/W_m-3_sr-1", "watts per cubic metre per steradian",
		"W m-3 sr-1"), //
	WATTS_PER_METRE_PER_STERADIAN("http://codes.wmo.int/common/unit/W_m-1_sr-1", "watts per metre per steradian", "W m-1 sr-1"), //
	WATTS_PER_SQUARE_METRE("http://codes.wmo.int/common/unit/W_m-2", "watts per square metre", "W m-2", "Watts por metro cuadrado"), //
	WATTS_PER_SQUARE_METRE_PER_STERADIAN("http://codes.wmo.int/common/unit/W_m-2_sr-1", "watts per square metre per steradian",
		"W m-2 sr-1"), //
	WATTS_PER_SQUARE_METRE_PER_STERADIAN_CENTIMETRE("http://codes.wmo.int/common/unit/W_m-2_sr-1_cm",
		"watts per square metre per steradian centimetre", "W m-2 sr-1 cm"), //
	WATTS_PER_SQUARE_METRE_PER_STERADIAN_METRE("http://codes.wmo.int/common/unit/W_m-2_sr-1_m",
		"watts per square metre per steradian metre", "W m-2 sr-1 m"), //
	WEBER("http://codes.wmo.int/common/unit/Wb", "weber", "Wb"), //
	YEAR("http://codes.wmo.int/common/unit/a", "year", "a"), //
	YOCTO("http://codes.wmo.int/common/unit/(y)_pref", "(yocto)", "(y)"), //
	YOTTA("http://codes.wmo.int/common/unit/(Y)_pref", "(yotta)", "(Y)"), //
	ZEPTO("http://codes.wmo.int/common/unit/(z)_pref", "(zepto)", "(z)"), //
	ZETTA("http://codes.wmo.int/common/unit/(Z)_pref", "(zetta)", "(Z)");//

	private String uri;
	private String[] synonyms = null;

	public String getUri() {
	    return uri;
	}

	CommonWMOUnit(String uri, String... synonyms) {
	    this.uri = uri;
	    this.synonyms = synonyms;
	}

	public static CommonWMOUnit decode(String label) {
	    for (CommonWMOUnit unit : values()) {
		if (unit.uri != null) {
		    if (unit.uri.equals(label)) {
			return unit;
		    }
		}
	    }
	    // first considering cases
	    for (CommonWMOUnit unit : values()) {
		if (unit.synonyms != null) {
		    for (String synonym : unit.synonyms) {
			if (synonym.equals(label)) {
			    return unit;
			}
		    }
		}
	    }
	    // then ignoring cases
	    for (CommonWMOUnit unit : values()) {
		if (unit.synonyms != null) {
		    for (String synonym : unit.synonyms) {
			if (synonym.equalsIgnoreCase(label)) {
			    return unit;
			}
		    }
		}
	    }
	    return null;
	}

    }

    public WMOOntology() throws Exception {
    }

    private static ExpiringCache<SKOSConcept> variableCache = new ExpiringCache<>();
    private static ExpiringCache<WMOUnit> unitCache = new ExpiringCache<>();

    static {
	unitCache.setDuration(1000 * 60 * 60l);
	unitCache.setMaxSize(1000);
	variableCache.setDuration(1000 * 60 * 60l);
	variableCache.setMaxSize(1000);
    }

    private void refreshCaches() {
	if (unitCache.entrySet().isEmpty()) {
	    try {
		Downloader d = new Downloader();
		String url = "http://codes.wmo.int/system/query?output=json&format=application/json&timeout=0&query=";
		InputStream queryStream = WMOOntology.class.getClassLoader().getResourceAsStream("whos/wmo-codes-units.sparql");
		String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
		query = URLEncoder.encode(query, "UTF-8");
		url = url + query;
		InputStream stream = d.downloadOptionalStream(url).get();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		stream.close();
		baos.close();
		JSONObject jo = new JSONObject(new String(baos.toByteArray()));
		JSONObject results = jo.getJSONObject("results");
		JSONArray bindings = results.getJSONArray("bindings");
		for (int i = 0; i < bindings.length(); i++) {
		    JSONObject binding = bindings.getJSONObject(i);
		    JSONObject a = binding.getJSONObject("a");
		    String uri = a.getString("value");
		    String label = binding.getJSONObject("label").getString("value");
		    String abbreviation = binding.getJSONObject("abbreviation").getString("value");
		    WMOUnit unit = new WMOUnit(uri);
		    unit.setPreferredLabel(new SimpleEntry<>(label, null));
		    unit.setAbbreviation(abbreviation);
		    unitCache.put(uri, unit);
		}
	    } catch (Exception e) {
		unitCache.clear();
	    } finally {

	    }
	}
	if (variableCache.entrySet().isEmpty()) {
	    try {
		Downloader d = new Downloader();
		String url = "http://codes.wmo.int/system/query?output=json&format=application/json&timeout=0&query=";
		InputStream queryStream = WMOOntology.class.getClassLoader()
			.getResourceAsStream("whos/wmo-codes-get-concepts-by-type.sparql");
		String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
//		query = query.replace("${TYPE}", "http://codes.wmo.int/wmdr/ObservedVariableTerrestrial");
		query = URLEncoder.encode(query, "UTF-8");
		url = url + query;
		InputStream stream = d.downloadOptionalStream(url).get();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		stream.close();
		baos.close();
		JSONObject jo = new JSONObject(new String(baos.toByteArray()));
		JSONObject results = jo.getJSONObject("results");
		JSONArray bindings = results.getJSONArray("bindings");
		for (int i = 0; i < bindings.length(); i++) {
		    JSONObject binding = bindings.getJSONObject(i);
		    JSONObject a = binding.getJSONObject("a");
		    String uri = a.getString("value");
		    int lastSlashIndex = uri.lastIndexOf("/");
		    String last = uri.substring(lastSlashIndex+1);
		    if (last.startsWith("_")) {
			last = last.substring(1);
			uri = uri.substring(0,lastSlashIndex)+"/"+last;
		    }
		    
		    String label = binding.getJSONObject("label").getString("value");
		    String definition = binding.getJSONObject("description").getString("value");
		    SKOSConcept concept = new SKOSConcept(uri);
		    concept.setPreferredLabel(new SimpleEntry<>(label, null));
		    concept.setDefinition(new SimpleEntry<>(definition, null));
		    variableCache.put(uri, concept);
		}
	    } catch (Exception e) {
		variableCache.clear();
	    } finally {

	    }
	}
    }

    public synchronized SKOSConcept getVariable(String uri) {
	refreshCaches();
	return variableCache.get(uri);
    }

    public synchronized WMOUnit getUnit(String uri) {
	refreshCaches();
	return unitCache.get(uri);
    }

    public synchronized Set<String> getUnitsURI() {
	refreshCaches();
	return new HashSet<String>(unitCache.keySet());
    }

    /**
     * This is used to download a list of
     * 
     * @param type
     * @param file
     * @throws Exception
     */
    public void downloadCodes(String type, File file) throws Exception {
	Downloader d = new Downloader();
	String url = "http://codes.wmo.int/system/query?output=json&format=application/json&timeout=0&query=";
	InputStream queryStream = WMOOntology.class.getClassLoader().getResourceAsStream("whos/wmo-codes-get-concepts-by-type.sparql");
	String query = IOUtils.toString(queryStream, StandardCharsets.UTF_8);
//	query = query.replace("${TYPE}", type);
	query = URLEncoder.encode(query, "UTF-8");
	url = url + query;
	InputStream stream = d.downloadOptionalStream(url).get();
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	baos.close();
	JSONObject jo = new JSONObject(new String(baos.toByteArray()));
	JSONObject results = jo.getJSONObject("results");
	JSONArray bindings = results.getJSONArray("bindings");
	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	writer.write("Concept URI\tLabel\tDescription\n");
	for (int i = 0; i < bindings.length(); i++) {
	    JSONObject binding = bindings.getJSONObject(i);
	    JSONObject a = binding.getJSONObject("a");
	    String uri = a.getString("value");
	    String label = binding.getJSONObject("label").getString("value");
	    String description = binding.getJSONObject("description").getString("value");
	    writer.write(uri + "\t" + label + "\t" + description + "\n");
	}
	writer.close();

    }

    public void printEnumCode() {
	Set<String> units = getUnitsURI();
	TreeSet<String> enums = new TreeSet<String>();
	for (String uri : units) {
	    WMOUnit unit = getUnit(uri);
	    List<String> syns = new ArrayList<>();
	    syns.add(unit.getPreferredLabel().getKey());
	    syns.add(unit.getAbbreviation());

	    if (uri.equals(CommonWMOUnit.CUBIC_METRES_PER_SECOND.getUri())) {
		syns.add("m³/s");
		syns.add("cms");
		syns.add("cubic meters per second");
		syns.add("cubic metres per second");
		syns.add("cubic metre per second");
		syns.add("cubic meter per second");
		syns.add("metros cúbicos por segundo");
	    } else if (uri.equals(CommonWMOUnit.CENTIMETRE.getUri())) {
		syns.add("centimeter");
	    } else if (uri.equals(CommonWMOUnit.MILLIMETRE.getUri())) {
		syns.add("milímetros");
		syns.add("millimeters");
		syns.add("millimetres");
		syns.add("millimeter");
		syns.add("millimetre");
	    } else if (uri.equals(CommonWMOUnit.DEGREE_CELSIUS.getUri())) {
		syns.add("˚C");
		syns.add("°C");
		syns.add("degrees Celsius");
		syns.add("grados centígrados");
	    } else if (uri.equals(CommonWMOUnit.HECTOPASCAL.getUri())) {
		syns.add("hectoPascales");
		syns.add("miliBares");
		syns.add("mB");
	    } else if (uri.equals(CommonWMOUnit.METRE.getUri())) {
		syns.add("metros");
		syns.add("meters");
	    } else if (uri.equals(CommonWMOUnit.DEGREE_ANGLE.getUri())) {
		syns.add("grados");
		syns.add("° (gr)");
	    } else if (uri.equals(CommonWMOUnit.KILOMETRES_PER_HOUR.getUri())) {
		syns.add("kilómetros por hora");
	    } else if (uri.equals(CommonWMOUnit.METRES_PER_SECOND.getUri())) {
		syns.add("metros por segundo");
	    } else if (uri.equals(CommonWMOUnit.WATTS_PER_SQUARE_METRE.getUri())) {
		syns.add("Watts por metro cuadrado");
	    } else if (uri.equals(CommonWMOUnit.PER_CENT.getUri())) {
		syns.add("porcentaje");
	    }

	    String synonyms = "";
	    for (String syn : syns) {
		synonyms += ",\"" + syn + "\"";
	    }

	    String e = unit.getPreferredLabel().getKey().toUpperCase().replace(" ", "_").replace("(", "").replace(")", "").replace("-", "")
		    .replace("^", "").replace("=", "").replace("__", "_") + "(\"" + uri + "\"" + synonyms + "),//";
	    enums.add(e);
	}
	for (String e : enums) {
	    System.out.println(e);
	}
    }

    public static void main(String[] args) throws Exception {
	WMOOntology wmo = new WMOOntology();
	wmo.printEnumCode();
	// wmo.downloadCodes("http://codes.wmo.int/wmdr/ObservedVariableTerrestrial", new
	// File("/home/boldrini/ontologia/wmoCodes-terrestrial.csv"));
	// wmo.downloadCodes("http://codes.wmo.int/wmdr/ObservedVariableAtmosphere", new
	// File("/home/boldrini/ontologia/wmoCodes-atmoshpere.csv"));
	// wmo.downloadCodes("http://codes.wmo.int/wmdr/ObservedVariableEarth", new
	// File("/home/boldrini/ontologia/wmoCodes-earth.csv"));
	// wmo.downloadCodes("http://codes.wmo.int/wmdr/ObservedVariableOcean", new
	// File("/home/boldrini/ontologia/wmoCodes-ocean.csv"));
    }

    public static WMOUnit decodeUnit(String units) throws Exception {
	CommonWMOUnit commonUnit = CommonWMOUnit.decode(units);
	if (commonUnit == null) {
	    return null;
	}
	WMOOntology codes = new WMOOntology();
	return codes.getUnit(commonUnit.getUri());
    }
}
