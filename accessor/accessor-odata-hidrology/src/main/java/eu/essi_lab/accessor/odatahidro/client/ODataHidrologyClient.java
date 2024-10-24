/**
 * 
 */
package eu.essi_lab.accessor.odatahidro.client;

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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class ODataHidrologyClient {

    /**
     * @author Fabrizio
     */
    public enum Variable {
	/**
	 * 
	 */
	WATER_LEVEL("1", "W", "Vedenkorkeus", "Water level W [cm]"),

	/**
	 * 
	 */
	DISCHARGE("2", "Q", "Virtaama", "Discharge Q [m3/s]"),

	/**
	 * draining away of water (or substances carried in it) from the surface of an area of land, a building or
	 * structure, etc..)
	 */
	RUNOFF("3", "R", "Valuma", "Runoff R [l/s/km2]"),
	/**
	 * 
	 */
	EVAPORATION("4", "E", "Haihdunta", "Evaporation (mm)"),
	/**
	 * 
	 */
	AREAL_PREC("5", "PA", "SadantaAlue", "Areal Precipitation [mm]"),

	/**
	 * The correct id is 6, but the server returns a 404 error for this variable,
	 * so it is excluded
	 * http://rajapinnat.ymparisto.fi/api/Hydrologiarajapinta/1.0/odata/SadantaPiste?$top=1&$filter=Paikka_Id+eq+544&$orderby=Aika+asc&$select=Arvo,Aika&$skip=0
	 */
	POINT_PREC("NONE", "P", "SadantaPiste", "Point Precipitation [mm]"),

	/**
	 * 
	 */
	AREA_SNOW_COVER("7", "SA", "LumiAlue", "Areal Water Equivalent of Snow Cover [mm]"),
	/**
	 * 
	 */
	SNOW_COURSE("8", "S", "LumiPiste", "Snow course measurement [mm]"),
	/**
	 * 
	 */
	ICE_THICKNESS("9", "I", "Jaanpaksuus", "Ice thickness (cm)"),

	/**
	 * Only one column:
	 * orderby=Arvo ORD&$select=Arvo
	 */
	FREEZING("10", "IB", "JaatJaanlahto", "Freezing (date)"),
	/**
	 * 
	 */
	SURFACE_WATER_TEMP("11", "T", "LampoPintavesi", "Surface Water Temperature T [°C]"),
	/**
	 * 
	 */
	WATER_TEMP_PROFILE("12", "TS", "LampoLuotaus", "Water temperature profile TS [°C]");

	private String variableId;
	private String variableCode;
	private String variableName;
	private String desc;
	private String orderBy;
	private List<String> columns;

	/**
	 * @param name
	 */
	private Variable(String id, String code, String name, String desc) {

	    this.variableId = id;
	    this.variableCode = code;
	    this.variableName = name;
	    this.desc = desc;
	    this.columns = new ArrayList<String>();
	    this.columns.add("Arvo"); // Value

	    if (!name.equals("JaatJaanlahto")) {
		this.orderBy = "Aika";
		this.columns.add("Aika"); // Date
	    } else {
		this.orderBy = "Arvo";
	    }
	}

	/**
	 * @param id
	 * @return
	 */
	public static Optional<Variable> fromId(String id) {

	    return Arrays.asList(Variable.values()).//
		    stream().//
		    filter(v -> v.getVariableId().equals(id)).//
		    findFirst();
	}

	/**
	 * @return
	 */
	public String getVariableId() {

	    return variableId;
	}

	/**
	 * @return
	 */
	public String getVariableCode() {

	    return variableCode;
	}

	/**
	 * @return
	 */
	public String getVariableName() {

	    return this.variableName;
	}

	/**
	 * @return
	 */
	public String getDescription() {

	    return desc;
	}

	/**
	 * @return
	 */
	public String getUnits() {

	    if (desc.contains("[")) {
		return desc.substring(desc.indexOf("[")).replace("[", "").replace("]", "").trim();
	    }

	    if (desc.contains("(")) {
		return desc.substring(desc.indexOf("(")).replace("(", "").replace(")", "").trim();
	    }

	    return desc;
	}

	/**
	 * @return
	 */
	public List<String> getColumns() {

	    return columns;
	}

	/**
	 * @return
	 */
	public String getOrderBy() {

	    return orderBy;
	}
    }

    /**
     * @author Fabrizio
     */
    public enum Status {

	/**
	 * 
	 */
	CONTINUOUS("1", "Jatkuva", "Continuous"),
	/**
	 * 
	 */
	DISCONTINUED("2", "Lopetettu", "Discontinued"),
	/**
	 * 
	 */
	RANDOM("3", "Satunnainen", "Random"),
	/**
	 * 
	 */
	NOT_PICKING("4", "Ei poiminnassa", "Not picking"),
	/**
	 * 
	 */
	TEMPORARY("5", "Määräaikainen", "Temporary");

	private String id;
	private String finnishName;
	private String englishName;

	/**
	 * @param id
	 * @param finName
	 * @param engName
	 */
	private Status(String id, String finName, String engName) {
	    this.id = id;
	    this.finnishName = finName;
	    this.englishName = engName;
	}

	/**
	 * @param id
	 * @return
	 */
	public static Optional<Status> fromFinName(String finName) {

	    return Arrays.asList(Status.values()).//
		    stream().//
		    filter(v -> v.getFinnishName().equals(finName)).//
		    findFirst();
	}

	/**
	 * @param id
	 * @return
	 */
	public static Optional<Status> fromId(String id) {

	    return Arrays.asList(Status.values()).//
		    stream().//
		    filter(v -> v.getStatusId().equals(id)).//
		    findFirst();
	}

	/**
	 * @return
	 */
	public String getStatusId() {

	    return id;
	}

	/**
	 * @return
	 */
	public String getFinnishName() {
	    return finnishName;
	}

	/**
	 * @return
	 */
	public String getEnglishName() {
	    return englishName;
	}
    }

    /**
     * @author Fabrizio
     */
    public enum InformationTarget {

	/**
	 * 
	 */
	PLACE("Paikka", "Paikka_Id"),

	/**
	 * 
	 */
	OWNER("Omistaja", "Omistaja_Id"),
	/**
	 * 
	 */
	HARDWARE("Laitteisto", "Laitteisto_Id"),
	/**
	 * Status translations:
	 * Jatkuva -> Continuous
	 * Lopetettu -> Discontinued
	 * Satunnainen -> Random
	 * Ei poiminnassa -> Not picking
	 * Määräaikainen -> Temporary
	 */
	STATUS("Tila", "Tila_Id");

	private String targetName;
	private String columnName;

	/**
	 * @param targetName
	 * @param columnName
	 */
	private InformationTarget(String targetName, String columnName) {
	    this.targetName = targetName;
	    this.columnName = columnName;
	}

	/**
	 * @return
	 */
	public String getTargetName() {
	    return targetName;
	}

	/**
	 * @return
	 */
	public String getColumnName() {
	    return columnName;
	}

    }

    /**
     * Paikka --------> Place
     * -
     * KoordLat ------> Place Lat
     * KoordLong -----> Place Lon
     * Paikka_Id -----> Place id
     * Suure_Id ------> Variable id for what ??? (NOT USED)
     * Tila_Id -------> Status id
     * Omistaja_Id ---> Owner id
     * Laitteisto_Id -> Hardware id
     * JarviNimi -----> Lake name
     * Nimi ----------> Place name
     * H_Kunta_Id ----> Municipality id, NOT USED: USING KuntNimi instead
     * KuntaNimi -----> Municipality Name
     * VesalNime -----> Lake name
     * Suure_Id ------> Variable id
     * -
     * Tokens: SRV_URL, MAX_RESULTS, SKIP
     */
    private static final String PLACE_TABLE_QUERY_TEMPLATE = "SRV_URL/Paikka?$top=MAX_RESULTS&$select=KoordErTmPohj,KoordErTmIta,Paikka_Id,Tila_Id,Omistaja_Id,Laitteisto_Id,Nimi,KuntaNimi,JarviNimi,VesalNimi,Suure_Id,Nro&$skip=SKIP";

    private static final String FILTER = "&$filter=";

    private static final String PLACE_FILTER = "Paikka_Id eq PLACE_ID";

    private static final String STATION_FILTER = "Nro eq 'STATION_ID'";

    private static final String MAX_RESULTS = "&$top=MAX_RESULTS";
    /**
     * 
     */

    private static final String SKIP = "&$skip=SKIP";

    private static final String VAR_QUERY_TEMPLATE = "SRV_URL/VARIABLE?$orderby=ORDER_BY ORD&$select=COLUMNS";

    /**
     * 
     */
    private static final String INFO_QUERY_TEMPLATE = "SRV_URL/TARGET?$filter=COL_NAME eq ID&$select=Nimi";

    /**
     * 
     */
    private static final int DEFAULT_MAX_RESULTS = 500;

    public static final String DEFAULT_URL = "http://rajapinnat.ymparisto.fi/api/Hydrologiarajapinta/1.0/odata/";

    private String url;

    private HashMap<String, JSONObject> ownersMap;
    private HashMap<String, JSONObject> hardwareMap;
    private HashMap<String, JSONObject> statusMap;

    public ODataHidrologyClient() {
	this(DEFAULT_URL);
    }

    /**
     * @param url
     * @param maxResults
     */
    public ODataHidrologyClient(String url) {

	this.url = url;
	if (this.url.endsWith("/")) {
	    this.url = url.substring(0, url.length() - 1);
	}

	this.ownersMap = new HashMap<>();
	this.hardwareMap = new HashMap<>();
	this.statusMap = new HashMap<>();
    }

    /**
     * @param skip
     * @return
     * @throws Exception
     */
    public JSONObject execPlaceQuery(int skip) throws Exception {

	return execPlaceQuery(skip, DEFAULT_MAX_RESULTS);
    }

    /**
     * @param skip
     * @param maxResults
     * @throws Exception
     */
    public JSONObject execPlaceQuery(int skip, int maxResults) throws Exception {

	String query = PLACE_TABLE_QUERY_TEMPLATE.replace("SRV_URL", this.url);
	query = query.replace("MAX_RESULTS", String.valueOf(maxResults));
	query = query.replace("SKIP", String.valueOf(skip));

	return execQuery(query);
    }

    /**
     * @param skip
     * @param maxResults
     * @throws Exception
     */
    public JSONObject execPlaceQuery(int skip, int maxResults, String placeId) throws Exception {

	String query = (PLACE_TABLE_QUERY_TEMPLATE + FILTER + PLACE_FILTER).replace("SRV_URL", this.url).replace("PLACE_ID", placeId);
	query = query.replace("MAX_RESULTS", String.valueOf(maxResults));
	query = query.replace("SKIP", String.valueOf(skip));

	return execQuery(query);
    }

    public JSONObject execStationQuery(int skip, int maxResults, String stationId) throws Exception {

	String query = (PLACE_TABLE_QUERY_TEMPLATE + FILTER + STATION_FILTER).replace("SRV_URL", this.url).replace("STATION_ID", stationId);
	query = query.replace("MAX_RESULTS", String.valueOf(maxResults));
	query = query.replace("SKIP", String.valueOf(skip));

	return execQuery(query);
    }

    /**
     * @param variable
     * @param placeId
     * @param sortingOrder
     * @param skip
     * @return
     * @throws Exception
     */
    public JSONObject execVariableQuery(Variable variable, String placeId, String sortingOrder, int skip) throws Exception {

	return execVariableQuery(variable, placeId, sortingOrder, skip, DEFAULT_MAX_RESULTS, null, null);
    }

    /**
     * @param variable
     * @param placeId
     * @param sortingOrder
     * @param skip
     * @return
     * @throws Exception
     */
    public JSONObject execVariableQuery(Variable variable, String placeId, String sortingOrder, Integer skip, Integer maxResults,
	    Date begin, Date end) throws Exception {

	// SRV_URL/VARIABLE?$top=MAX_RESULTS&$filter=Paikka_Id eq PLACE_ID&$orderby=ORDER_BY
	// ORD&$select=COLUMNS&$skip=SKIP

	String query = VAR_QUERY_TEMPLATE.replace("SRV_URL", this.url);
	query = query.replace("VARIABLE", variable.getVariableName());

	String filter = "";

	if (placeId != null) {

	    filter = PLACE_FILTER.replace("PLACE_ID", placeId);
	}

	if (begin != null && end != null) {
	    if (filter.length() > 0) {
		filter += " and ";
	    }
	    // such as 2019-03-13T16:28:02.462Z
	    filter += "Aika le datetime'" + ISO8601DateTimeUtils.getISO8601DateTime(end) + "' and Aika ge datetime'"
		    + ISO8601DateTimeUtils.getISO8601DateTime(begin) + "' ";
	}

	query = query.replace("ORDER_BY", variable.getOrderBy());

	query = query.replace("ORD", sortingOrder);

	String columns = variable.getColumns().stream().collect(Collectors.joining(","));
	query = query.replace("COLUMNS", columns);

	if (maxResults != null) {

	    query = query + MAX_RESULTS.replace("MAX_RESULTS", String.valueOf(maxResults));

	}
	if (skip != null) {
	    query = query += SKIP.replace("SKIP", String.valueOf(skip));
	}

	if (filter.length() > 0) {

	    query = query + FILTER + filter;
	}

	JSONObject ret = execQuery(query);

	// all results required
	if (skip == null && maxResults == null) {

	    JSONObject tmp = ret;
	    while (tmp.has("odata.nextLink")) {
		String next = tmp.getString("odata.nextLink");
		tmp = execQuery(next);
		if (ret.has("value") && tmp.has("value")) {
		    JSONArray value = ret.getJSONArray("value");
		    JSONArray nextValue = tmp.getJSONArray("value");
		    for (int i = 0; i < nextValue.length(); i++) {
			value.put(nextValue.getJSONObject(i));
		    }
		}
	    }
	    return ret;

	} else {

	    return ret;

	}
    }

    /**
     * @param target
     * @param targetId
     * @return
     * @throws Exception
     */
    public JSONObject execInformativeQuery(InformationTarget target, String targetId) throws Exception {

	switch (target) {
	case HARDWARE:
	    if (hardwareMap.containsKey(targetId)) {
		return hardwareMap.get(targetId);
	    }
	    break;
	case STATUS:
	    if (statusMap.containsKey(targetId)) {
		return statusMap.get(targetId);
	    }
	    break;
	case OWNER:
	    if (ownersMap.containsKey(targetId)) {
		return ownersMap.get(targetId);
	    }
	    break;
	}

	String query = INFO_QUERY_TEMPLATE.replace("SRV_URL", this.url);

	query = query.replace("TARGET", target.getTargetName());

	query = query.replace("ID", targetId);

	query = query.replace("COL_NAME", target.getColumnName());

	JSONObject out = execQuery(query);

	switch (target) {
	case HARDWARE:
	    hardwareMap.put(targetId, out);
	    break;
	case STATUS:
	    statusMap.put(targetId, out);
	    break;

	case OWNER:
	    ownersMap.put(targetId, out);
	    break;
	}

	return out;
    }

    /**
     * @param query
     * @return
     * @throws Exception
     */
    private JSONObject execQuery(String query) throws Exception {

	Downloader downloader = new Downloader();

	query = query.replace(" ", URLEncoder.encode(" ", "UTF-8"));

	Optional<String> optional = downloader.downloadOptionalString(query);

	if (optional.isPresent()) {

	    return new JSONObject(optional.get());
	}

	throw new Exception("Unable to exec query");
    }

    private static void print(Optional<String> what, String val) {

	if (what.isPresent()) {

	    System.out.println(val + ": " + what.get());
	} else {

	    System.out.println(val + " missing");
	}
    }

    public static void main(String[] args) throws Exception {

	ODataHidrologyClient client = new ODataHidrologyClient("http://rajapinnat.ymparisto.fi/api/Hydrologiarajapinta/1.0/odata/");

	JSONObject placeQueryResponse = client.execPlaceQuery(0, 10);

	ClientResponseWrapper placeQueryResponseWrapper = new ClientResponseWrapper(placeQueryResponse);

	int responseSize = placeQueryResponseWrapper.getResponseSize();

	for (int i = 0; i < responseSize; i++) {

	    Optional<String> name = placeQueryResponseWrapper.getName(i);
	    Optional<String> hardwareId = placeQueryResponseWrapper.getHardwareId(i);
	    Optional<String> ownerId = placeQueryResponseWrapper.getOwnerId(i);
	    Optional<String> statusId = placeQueryResponseWrapper.getStatusId(i);
	    Optional<String> lat = placeQueryResponseWrapper.getLat(i);
	    Optional<String> lon = placeQueryResponseWrapper.getLon(i);
	    Optional<String> municipalityName = placeQueryResponseWrapper.getMunicipalityName(i);
	    Optional<String> placeId = placeQueryResponseWrapper.getPlaceId(i);
	    Optional<String> lakeName = placeQueryResponseWrapper.getLakeName(i);

	    Optional<String> riverName = placeQueryResponseWrapper.getWaterAreaName(i);
	    Optional<String> variableId = placeQueryResponseWrapper.getVariableId(i);

	    System.out.println("###");
	    System.out.println("Current place: " + name.get() + "\n");

	    print(name, "Name");
	    print(variableId, "Variable id");

	    print(hardwareId, "Hard id");
	    print(ownerId, "Owner id");
	    print(statusId, "Status id");
	    print(lat, "Lat");
	    print(lon, "Lon");
	    print(municipalityName, "Municipality name");
	    print(placeId, "Place id");
	    print(lakeName, "Lake name");
	    print(riverName, "River name");

	    {
		if (hardwareId.isPresent()) {
		    JSONObject hardResponse = client.execInformativeQuery(InformationTarget.HARDWARE, hardwareId.get());
		    ClientResponseWrapper respWrapper = new ClientResponseWrapper(hardResponse);
		    Optional<String> value = respWrapper.getName(0);
		    print(value, "Hardware");
		}
	    }

	    {
		if (ownerId.isPresent()) {
		    JSONObject ownerResponse = client.execInformativeQuery(InformationTarget.OWNER, ownerId.get());
		    ClientResponseWrapper respWrapper = new ClientResponseWrapper(ownerResponse);
		    Optional<String> value = respWrapper.getName(0);
		    print(value, "Owner");
		}
	    }

	    {
		if (statusId.isPresent()) {

		    // Optional<Status> optionalStatus = Status.fromId(statusId.get());
		    // if (optionalStatus.isPresent()) {
		    //
		    // System.out.println("Status: " + optionalStatus.get().getEnglishName());
		    // }

		    JSONObject statusResponse = client.execInformativeQuery(InformationTarget.STATUS, statusId.get());
		    ClientResponseWrapper respWrapper = new ClientResponseWrapper(statusResponse);
		    Optional<String> value = respWrapper.getName(0);
		    print(value, "Status (finnish)");
		    // System.out.println("Status (eng): "+Status.fromFinName(value.get()).get().getEnglishName());
		}
	    }

	    Optional<Variable> fromId = Variable.fromId(variableId.get());

	    System.out.println("\n---");
	    System.out.println("Current variable: " + fromId.get());

	    JSONObject varAscQueryResponse = client.execVariableQuery(fromId.get(), placeId.get(), "asc", 0, 1, null, null);
	    JSONObject varDescQueryResponse = client.execVariableQuery(fromId.get(), placeId.get(), "desc", 0, 1, null, null);

	    ClientResponseWrapper varAscQueryResponseWrapper = new ClientResponseWrapper(varAscQueryResponse);
	    ClientResponseWrapper varDescQueryResponseWrapper = new ClientResponseWrapper(varDescQueryResponse);

	    Optional<String> minValue = varAscQueryResponseWrapper.getValue(0);
	    Optional<String> minDate = varAscQueryResponseWrapper.getDate(0);

	    print(minValue, "Value");
	    print(minDate, "Min Date");

	    Optional<String> maxValue = varDescQueryResponseWrapper.getValue(0);
	    Optional<String> maxDate = varDescQueryResponseWrapper.getDate(0);

	    print(maxValue, "Value");
	    print(maxDate, "Max Date");
	}

    }
}
