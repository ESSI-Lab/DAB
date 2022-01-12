package eu.essi_lab.shared.driver.es.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.sparql.function.library.localname;
import org.json.JSONObject;

import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.rip.RuntimeInfoProvider;
import eu.essi_lab.rip.RuntimeInfoPublisher;

public class ElasticsearchInfoPublisher extends RuntimeInfoPublisher {

    private static boolean enabled = true;

    public static final String RUNTIME_FOLDER = "runtime-info";

    private static ExpiringCache<TreeMap<String, List<Object>>> cache;

    static {
	cache = new ExpiringCache<TreeMap<String, List<Object>>>();
	cache.setDuration(6000000);
    }
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new ThreadFactory() {

	@Override
	public Thread newThread(Runnable r) {

	    Thread thread = new Thread(r);
	    thread.setPriority(Thread.MIN_PRIORITY);
	    thread.setName(thread.getName() + "_ES_INFO_PUBLISHER");

	    return thread;
	}
    });

    private String dbname = null;

    // private ESConnector connector;

    private ElasticsearchClient client;
    public ElasticsearchInfoPublisher(String endpoint, String dbname, String user, String password, String runtimeId, String context)
	    throws GSException {

	super(runtimeId, context);

	this.dbname = dbname;

	if (enabled && endpoint != null && dbname != null && user != null && password != null) {

	    
	    this.client = new ElasticsearchClient(endpoint, user, password);
	    client.setDbName(dbname);
	    try {
		client.init();
	    } catch (IOException e) {
		e.printStackTrace();
		throw new GSException();
	    }

	    // try {
	    // connector.initializePersistentStorage();
	    // } catch (GSException e) {
	    // DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	    // e.printStackTrace();
	    // }

	}
    }

    /**
     * This publisher must be enabled by the GI-Suite starter
     */
    public static void enable() {

	enabled = true;
    }

    @Override
    public void publish(RuntimeInfoProvider provider) throws GSException {

	if (provider == null || !enabled) {
	    return;
	}

	String id = getRuntimeId();

	TreeMap<String, List<Object>> properties = cache.get(id);

	if (properties == null) {
	    properties = new TreeMap<>();
	    cache.put(id, properties);
	}

	HashMap<String, List<String>> map = provider.provideInfo();

	map.put(RuntimeInfoElement.RUNTIME_ID.getName(), Arrays.asList(id));
	map.put(RuntimeInfoElement.RUNTIME_CONTEXT.getName(), Arrays.asList(getContext()));

	TreeMap<String, List<Object>> newMap = convertMap(map);

	for (String key : newMap.keySet()) {
	    List<Object> newValues = newMap.get(key);
	    List<Object> values = properties.get(key);
	    if (values == null) {
		values = new ArrayList<Object>();
		properties.put(key, values);
	    }
	    values.addAll(newValues);
	}

    }

    private TreeMap<String, List<Object>> convertMap(HashMap<String, List<String>> map) {
	TreeMap<String, List<Object>> ret = new TreeMap<>();
	for (String key : map.keySet()) {
	    List<String> list = map.get(key);
	    List<Object> objects = null;
	    if (list != null) {
		objects = new ArrayList<Object>();
		for (String item : list) {
		    objects.add(item);
		}
	    }
	    ret.put(key, objects);
	}
	return ret;
    }

    public static JSONObject createJsonObject(TreeMap<String, List<Object>> properties) {
	JSONObject ret = new JSONObject();
	for (String key : properties.keySet()) {
	    List<Object> values = properties.get(key);
	    if (values.size() == 0) {
		// nothing to do
	    } else if (values.size() == 1) {
		Object v = values.get(0);
		if (v instanceof Double) {
		    Double d = (Double) v;
		    ret.put(key, d);
		} else if (v instanceof Long) {
		    Long l = (Long) v;
		    ret.put(key, l);
		} else {
		    ret.put(key, v.toString());
		}
	    } else {
		ret.put(key, values);
	    }

	}
	return ret;
    }

    public static TreeMap<String, List<Object>> refineProperties(Map<String, List<Object>> properties) {
	TreeMap<String, List<Object>> ret = new TreeMap<>();

	String type = null;

	HashMap<String, String> synonyms = new HashMap<>();
	synonyms.put("DISCOVERY_MESSAGE_VIEW_ID", "VIEW_ID");
	synonyms.put("ACCESS_MESSAGE_VIEW_ID", "VIEW_ID");

	List<String> toDelete = new ArrayList<>();
	toDelete.add("DISCOVERY_MESSAGE_BBOX_se");
	toDelete.add("DISCOVERY_MESSAGE_BBOX_ne");
	toDelete.add("DISCOVERY_MESSAGE_BBOX_sw");
	toDelete.add("DISCOVERY_MESSAGE_BBOX_nw");

	toDelete.add("DISCOVERY_MESSAGE_TIME_STAMP_MILLIS");
	toDelete.add("DISCOVERY_MESSAGE_TIME_STAMP");

	toDelete.add("WEB_REQUEST_TIME_STAMP_MILLIS");
	toDelete.add("WEB_REQUEST_TIME_STAMP");
	toDelete.add("WEB_REQUEST_VIEW_ID");

	toDelete.add("RESULT_SET_TIME_STAMP_MILLIS");
	toDelete.add("RESULT_SET_TIME_STAMP");

	toDelete.add("CHRONOMETER_TIME_STAMP_MILLIS");
	toDelete.add("PROFILER_TIME_STAMP_MILLIS");
	toDelete.add("RESPONSE_Date");

	List<String> dates = new ArrayList<>();
	dates.add("CHRONOMETER_TIME_STAMP");
	dates.add("DISCOVERY_MESSAGE_tmpExtentBegin");
	dates.add("DISCOVERY_MESSAGE_tmpExtentEnd");

	// toDelete.add("runtimeContext");

	List<String> unique = new ArrayList<>();
	unique.add("runtimeId");
	unique.add("runtimeContext");
	unique.add("RESULT_SET_DATA_type");
	unique.add("RESULT_SET_crs");
	unique.add("RESULT_SET_RETURNED");
	unique.add("ACCESS_MESSAGE_SPATIAL_DIMENSION_name");
	unique.add("RESULT_SET_SPATIAL_DIMENSION_name");
	unique.add("RESULT_SET_MATCHED");
	unique.add("RESULT_SET_SPATIAL_DIMENSION_id");
	unique.add("RESULT_SET_TEMPORAL_DIMENSION_name");
	unique.add("RESULT_SET_TEMPORAL_DIMENSION_id");
	unique.add("RESULT_SET_DATA_format");
	String s = null;
	String e = null;
	String n = null;
	String w = null;
	String q = null;
	for (String localName : properties.keySet()) {

	    
	    
	    if (type == null) {
		if (localName.startsWith("DISCOVERY_MESSAGE")) {
		    type = "DiscoveryMessage";
		} else if (localName.startsWith("ACCESS_MESSAGE")) {
		    type = "AccessMessage";
		} else if (localName.startsWith("BULK_DOWNLOAD")) {
		    type = "BulkDownloadMessage";
		} else if (localName.equals("SCHEME")) {
		    type = "SemanticMessage";
		}

	    }

	    if (toDelete.contains(localName)) {
		continue;
	    }
	    String synonym = synonyms.get(localName);
	    if (synonym != null) {
		localName = synonym;
	    }
	    List<Object> values = properties.get(localName);
	    if (values == null || values.isEmpty()) {
		continue;
	    }
	    List<Object> refinedValues = new ArrayList<>();
	    Object firstValue = values.get(0);
	    if (firstValue != null) {
		String stringValue = firstValue.toString();

		
		
		if (localName.equals("DISCOVERY_MESSAGE_tmpExtentBegin")) {
		    System.out.println();
		}
		if (dates.contains(localName)) {
		    Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(stringValue);
		    if (parsed.isPresent()) {
			if (parsed.get().after(ISO8601DateTimeUtils.parseISO8601ToDate("3000-01-01").get())) {
			    continue;
			}
			if (parsed.get().before(ISO8601DateTimeUtils.parseISO8601ToDate("1000-01-01").get())) {
			    continue;
			}
			values.set(0, ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(parsed.get()));
		    } else {
			continue;
		    }
		}

		switch (localName) {
		case "DISCOVERY_MESSAGE_BBOX_EAST":
		    e = stringValue;
		    break;
		case "DISCOVERY_MESSAGE_BBOX_WEST":
		    w = stringValue;
		    break;
		case "DISCOVERY_MESSAGE_BBOX_SOUTH":
		    s = stringValue;
		    break;
		case "DISCOVERY_MESSAGE_BBOX_NORTH":
		    n = stringValue;
		    break;
		case "WEB_REQUEST_QUERY_STRING":
		    q = stringValue;
		    break;
		default:
		    break;
		}
	    }
	    for (Object v : values) {
		if (v == null) {
		    continue;
		}
		String stringValue = v.toString();
		if (stringValue.toLowerCase().equals("null") || stringValue.toLowerCase().equals("(null)")) {
		    continue;
		}
		Object refined = null;
		if (values.size() == 1 && !stringValue.isEmpty()
			&& org.apache.commons.lang3.StringUtils.isNumeric("" + stringValue.charAt(0))) {
		    try {
			refined = Double.parseDouble(stringValue);
			if (!Double.isFinite((double) refined)) {
			    refined = null;
			} else {
			    refined = Long.parseLong(stringValue);
			}
		    } catch (Exception e2) {
		    }
		}
		if (refined == null) {
		    refined = stringValue;
		}
		refinedValues.add(refined);

	    }
	    if (!refinedValues.isEmpty()) {
		if (localName.equals("ACCESS_MESSAGE_TIME_STAMP_MILLIS")||localName.equals("BULK_DOWNLOAD_MESSAGE_TIME_STAMP_MILLIS")) {
		    String rv = refinedValues.get(0).toString();
		    long l = Long.parseLong(rv);
		    refinedValues.clear();
		    refinedValues.add(ISO8601DateTimeUtils.getISO8601DateTime(new Date(l)));
		    localName = "CHRONOMETER_TIME_STAMP";
		}
		if (unique.contains(localName)) {
		    HashSet<Object> uniques = new HashSet<Object>(refinedValues);
		    ret.put(localName, Arrays.asList(uniques.toArray(new Object[] {})));
		} else {
		    ret.put(localName, refinedValues);
		}
	    }
	}
	if (w != null && s != null && n != null && e != null) {
	    String es = e + " " + s;
	    String ws = w + " " + s;
	    String en = e + " " + n;
	    String wn = w + " " + n;
	    String polygon = "POLYGON ((" + ws + "," + wn + "," + en + "," + es + "," + ws + "))";
	    ret.put("DISCOVERY_MESSAGE_POLYGON", Arrays.asList(polygon));
	}
	if (q != null && !q.isEmpty()) {
	    List<Object> array = new ArrayList<>();
	    if (q.contains("&")) {
		String[] split = q.split("&");
		array = Arrays.asList(split);
	    } else {
		array.add(q);
	    }
	    ret.put("WEB_REQUEST_QUERY_PARAMETERS", array);
	}

	List<Object> messageType = ret.get("MESSAGE_TYPE");
	if (messageType == null || messageType.isEmpty()) {

	    if (type != null) {
		ret.put("MESSAGE_TYPE", Arrays.asList(type));
	    }
	}

	return ret;
    }

    /**
     * @param providerName
     * @return
     */
    private String createURI(String providerName) {

	return "/" + RUNTIME_FOLDER + "/" + getRuntimeId() + "_" + UUID.randomUUID().toString().substring(0, 4) + "_" + providerName;
    }

    /**
     * @param name
     * @param value
     * @return
     */
    private String createDocument(String name, String value, boolean escape) {

	if (escape) {
	    value = StringEscapeUtils.escapeXml11(value);
	}

	String ns = !escape ? " xmlns:gs='" + NameSpace.GS_DATA_MODEL_SCHEMA_URI + "'" : "";

	return "<gs:" + name + ns + ">" + value + "</gs:" + name + ">";
    }

    /**
     * @param name
     * @param value
     * @return
     */
    private String createTextNode(String name, String value) {

	return createDocument(name, value, true);
    }

    public void write() {

	THREAD_POOL.execute(new Runnable() {

	    @Override
	    public void run() {
		RequestManager.getInstance().addThreadName(ElasticsearchInfoPublisher.this.getRuntimeId());

		String id = getRuntimeId();

		int size1 = getSize(id);

		try {
		    // let's sleep a bit before writing, in order to be sure that all the info have been published
		    // this is for extra safety, as they should be sent in a synchronous way
		    Thread.sleep(5000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}

		int size2 = getSize(id);

		if (size2 > size1) {
		    GSLoggerFactory.getLogger(getClass()).error("STATISTICS LOGGER GOT UNEXPECTED RESULTS: CHECK AS SOON AS POSSIBLE!");
		}

		TreeMap<String, List<Object>> properties = cache.get(id);

		if (properties == null) {
		    GSLoggerFactory.getLogger(getClass()).error("No statistics found for request id: {}", id);
		}

		TreeMap<String, List<Object>> refinedProperties = refineProperties(properties);

		JSONObject json = createJsonObject(refinedProperties);

		if (enabled && client != null) {

		    String index = "request";

		    client.write(index, json);

		}

		cache.remove(id);
	    }

	    private int getSize(String id) {
		TreeMap<String, List<Object>> ret = cache.get(id);
		if (ret == null) {
		    return 0;
		}
		return ret.size();
	    }
	});
    }
}
