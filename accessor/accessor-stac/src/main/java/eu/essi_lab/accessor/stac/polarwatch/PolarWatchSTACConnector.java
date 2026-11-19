package eu.essi_lab.accessor.stac.polarwatch;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.stac.harvested.STACConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the static STAC catalog published by NOAA PolarWatch (https://polarwatch.noaa.gov/stac.html).<br>
 * <br>
 * Unlike a STAC API, the PolarWatch catalog is a static tree: the root <code>catalog.json</code> lists one
 * <code>child</code> link per collection, and each <code>collection.json</code> has to be fetched separately. One
 * record is emitted per collection.<br>
 * <br>
 * Most collections are gridded datasets served by an ERDDAP <code>griddap</code> endpoint, whose metadata is much
 * richer than the STAC JSON (geographic bounds, actual time coverage, variables with units). When such an endpoint is
 * found among the collection links or assets, its <code>/info/{datasetID}/index.json</code> document is fetched too.
 * The record metadata is an envelope JSON like:
 *
 * <pre>
 * {
 *   "catalogUrl": "https://polarwatch.noaa.gov/stac/catalog.json",
 *   "collectionUrl": "https://polarwatch.noaa.gov/stac/collections/{id}/collection.json",
 *   "collection": { ...STAC collection... },
 *   "erddap": { "base": "https://coastwatch.noaa.gov/erddap", "protocol": "griddap", "datasetId": "...", "info": { ... } }
 * }
 * </pre>
 *
 * The <code>erddap</code> member is present only when an ERDDAP dataset is referenced; its <code>info</code> member
 * only when the info document could be downloaded.
 *
 * @author Enrico Boldrini
 */
public class PolarWatchSTACConnector extends STACConnector {

    /**
     *
     */
    public static final String TYPE = "PolarWatch STAC Connector";

    /**
     *
     */
    static final String DEFAULT_CATALOG_PATH = "/stac/catalog.json";

    private static final String POLARWATCH_CATALOG_ERROR = "POLARWATCH_CATALOG_ERROR";

    /**
     * Matches ERDDAP dataset URLs, e.g. https://coastwatch.noaa.gov/erddap/griddap/noaacwVIIRSn20iceconcNP06Daily.html
     */
    private static final Pattern ERDDAP_DATASET_PATTERN = Pattern
	    .compile("^(https?://.+?/erddap)/(griddap|tabledap)/([A-Za-z0-9_\\-]+?)(\\.html)?/?$");

    private List<String> collectionURLs;
    private String catalogURL;
    private Downloader downloader;

    public PolarWatchSTACConnector() {

	super();
	this.downloader = new Downloader();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	if (collectionURLs == null) {

	    catalogURL = resolveCatalogURL(getSourceURL());

	    Optional<String> catalog = downloader.downloadOptionalString(catalogURL);

	    if (catalog.isEmpty()) {

		throw GSException.createException(//
			getClass(), //
			"Unable to download PolarWatch STAC catalog: " + catalogURL, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			POLARWATCH_CATALOG_ERROR);
	    }

	    collectionURLs = extractCollectionURLs(new JSONObject(catalog.get()), catalogURL);

	    GSLoggerFactory.getLogger(getClass()).info("Found {} collections in {}", collectionURLs.size(), catalogURL);
	}

	int start = 0;
	String token = request.getResumptionToken();
	if (token != null) {
	    start = Integer.parseInt(token);
	}

	int limit = collectionURLs.size();
	if (!getSetting().isMaxRecordsUnlimited() && getSetting().getMaxRecords().isPresent()) {
	    limit = Math.min(limit, getSetting().getMaxRecords().get());
	}

	int pageSize = Math.max(1, getSetting().getPageSize());
	int end = Math.min(start + pageSize, limit);

	for (int i = start; i < end; i++) {

	    String collectionURL = collectionURLs.get(i);

	    try {

		createRecord(collectionURL).ifPresent(response::addRecord);

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn("Unable to harvest collection {}: {}", collectionURL, e.getMessage());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Harvested collections {}-{} of {}", start, end, limit);

	if (end < limit) {

	    response.setResumptionToken(String.valueOf(end));
	}

	return response;
    }

    /**
     * @param collectionURL
     * @return
     */
    private Optional<OriginalMetadata> createRecord(String collectionURL) {

	Optional<String> collection = downloader.downloadOptionalString(collectionURL);

	if (collection.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to download collection {}", collectionURL);
	    return Optional.empty();
	}

	JSONObject collectionObject = new JSONObject(collection.get());

	JSONObject envelope = new JSONObject();
	envelope.put(PolarWatchSTACMapper.CATALOG_URL, catalogURL);
	envelope.put(PolarWatchSTACMapper.COLLECTION_URL, collectionURL);
	envelope.put(PolarWatchSTACMapper.COLLECTION, collectionObject);

	findERDDAPDataset(collectionObject).ifPresent(erddap -> {

	    String infoURL = erddap.getString(PolarWatchSTACMapper.ERDDAP_BASE) + "/info/"
		    + erddap.getString(PolarWatchSTACMapper.ERDDAP_DATASET_ID) + "/index.json";

	    try {

		downloader.downloadOptionalString(infoURL).//
			ifPresent(info -> erddap.put(PolarWatchSTACMapper.ERDDAP_INFO, new JSONObject(info)));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn("Unable to read ERDDAP info {}: {}", infoURL, e.getMessage());
	    }

	    envelope.put(PolarWatchSTACMapper.ERDDAP, erddap);
	});

	OriginalMetadata metadata = new OriginalMetadata();
	metadata.setSchemeURI(PolarWatchSTACMapper.POLARWATCH_STAC_SCHEME_URI);
	metadata.setMetadata(envelope.toString());

	return Optional.of(metadata);
    }

    /**
     * Accepts either the catalog JSON URL or any PolarWatch page URL (e.g. https://polarwatch.noaa.gov/stac.html), in
     * which case the default catalog location is used
     *
     * @param endpoint
     * @return
     */
    static String resolveCatalogURL(String endpoint) {

	String url = endpoint.trim();

	int query = url.indexOf('?');
	if (query > 0) {
	    url = url.substring(0, query);
	}

	if (url.endsWith(".json")) {
	    return url;
	}

	int stac = url.indexOf("/stac/");
	if (stac > 0) {
	    return url.substring(0, stac) + DEFAULT_CATALOG_PATH;
	}

	if (url.endsWith("/stac")) {
	    return url + "/catalog.json";
	}

	URI uri = URI.create(url);
	return uri.getScheme() + "://" + uri.getRawAuthority() + DEFAULT_CATALOG_PATH;
    }

    /**
     * @param catalog
     * @param catalogURL
     * @return the absolute URLs of the catalog children
     */
    static List<String> extractCollectionURLs(JSONObject catalog, String catalogURL) {

	List<String> out = new ArrayList<>();

	JSONArray links = catalog.optJSONArray("links");
	if (links == null) {
	    return out;
	}

	URI base = URI.create(catalogURL);

	for (int i = 0; i < links.length(); i++) {

	    JSONObject link = links.optJSONObject(i);
	    if (link != null && "child".equals(link.optString("rel")) && !link.optString("href").isEmpty()) {

		out.add(base.resolve(link.getString("href").trim()).toString());
	    }
	}

	return out;
    }

    /**
     * Looks for an ERDDAP dataset URL among the collection links (<code>service</code> links first) and assets
     *
     * @param collection
     * @return an object with the ERDDAP base URL, protocol and dataset id
     */
    static Optional<JSONObject> findERDDAPDataset(JSONObject collection) {

	List<String> hrefs = new ArrayList<>();

	JSONArray links = collection.optJSONArray("links");
	if (links != null) {

	    for (String rel : Arrays.asList("service", "about", "describedby")) {

		for (int i = 0; i < links.length(); i++) {

		    JSONObject link = links.optJSONObject(i);
		    if (link != null && rel.equals(link.optString("rel"))) {
			hrefs.add(link.optString("href"));
		    }
		}
	    }
	}

	JSONObject assets = collection.optJSONObject("assets");
	if (assets != null) {

	    for (String key : assets.keySet()) {

		JSONObject asset = assets.optJSONObject(key);
		if (asset != null) {
		    hrefs.add(asset.optString("href"));
		}
	    }
	}

	for (String href : hrefs) {

	    Matcher matcher = ERDDAP_DATASET_PATTERN.matcher(href.trim());

	    if (matcher.matches()) {

		JSONObject erddap = new JSONObject();
		erddap.put(PolarWatchSTACMapper.ERDDAP_BASE, matcher.group(1));
		erddap.put(PolarWatchSTACMapper.ERDDAP_PROTOCOL, matcher.group(2));
		erddap.put(PolarWatchSTACMapper.ERDDAP_DATASET_ID, matcher.group(3));

		return Optional.of(erddap);
	    }
	}

	return Optional.empty();
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint != null && endpoint.contains("polarwatch.noaa.gov");
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(PolarWatchSTACMapper.POLARWATCH_STAC_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
