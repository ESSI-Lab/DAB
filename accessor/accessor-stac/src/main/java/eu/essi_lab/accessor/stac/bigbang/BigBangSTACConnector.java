package eu.essi_lab.accessor.stac.bigbang;

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
import java.util.Locale;
import java.util.Optional;

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
 * Harvests the static STAC catalog of the ISPRA BIGBANG model outputs (e.g.
 * http://boldrini.essi-lab.eu:8080/stac/catalog.json).<br>
 * <br>
 * The catalog is a static tree with no STAC API: the root <code>catalog.json</code> lists one <code>child</code>
 * link per hydrological variable, and each <code>collection.json</code> lists one <code>item</code> per monthly or
 * annual grid.<br>
 * <br>
 * By default one record is emitted per collection (i.e. per variable), since the collection already exposes the
 * aggregated archives (all monthly grids, all annual grids, long term averages) as assets. If the full harvest
 * option is enabled ({@link #isHarvestItems()}, set from the STAC connector wrapper setting), each collection record
 * is followed by one child record per item (i.e. per monthly or annual grid).<br>
 * <br>
 * The record metadata is an envelope JSON like:
 *
 * <pre>
 * {
 *   "catalogUrl": "https://boldrini.essi-lab.eu/stac/catalog.json",
 *   "collectionUrl": "https://boldrini.essi-lab.eu/stac/bigbang100-total-precipitation/collection.json",
 *   "harvestItems": false,
 *   "catalog": { ...STAC catalog, without the child links... },
 *   "collection": { ...STAC collection... }
 * }
 * </pre>
 *
 * Item records also have the <code>itemUrl</code> and <code>item</code> members; in this case the collection is
 * provided without its item links.<br>
 * <br>
 * The child hrefs published by the catalog are absolute and may differ in scheme/port from the configured endpoint
 * (e.g. <code>https://host/stac/...</code> vs <code>http://host:8080/stac/...</code>). They are downloaded as
 * published; if that fails, they are rebased on the configured catalog URL.<br>
 * <br>
 * The resumption token has the form <code>collectionIndex:position:recordsCount</code>, where position 0 is the
 * collection record and position <i>k</i> is the <i>k</i>-th item.
 *
 * @author Enrico Boldrini
 */
public class BigBangSTACConnector extends STACConnector {

    /**
     *
     */
    public static final String TYPE = "BIGBANG STAC Connector";

    private static final String BIGBANG_CATALOG_ERROR = "BIGBANG_CATALOG_ERROR";

    private List<String> collectionURLs;
    private String catalogURL;
    private String publishedCatalogURL;
    private JSONObject catalogInfo;
    private Downloader downloader;

    //
    // the last downloaded collection
    //
    private int cachedIndex = -1;
    private JSONObject cachedCollection;
    private JSONObject cachedCollectionWithoutItems;
    private List<String> cachedItemURLs;

    public BigBangSTACConnector() {

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
			"Unable to download BIGBANG STAC catalog: " + catalogURL, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			BIGBANG_CATALOG_ERROR);
	    }

	    JSONObject catalogObject = new JSONObject(catalog.get());

	    publishedCatalogURL = findLink(catalogObject, "self").orElse(catalogURL);
	    collectionURLs = extractChildURLs(catalogObject, publishedCatalogURL);
	    catalogInfo = withoutLinks(catalogObject, "child");

	    GSLoggerFactory.getLogger(getClass()).info("Found {} collections in {}, full harvest: {}", collectionURLs.size(), catalogURL,
		    isHarvestItems());
	}

	int[] position = parseToken(request.getResumptionToken());
	int collection = position[0];
	int index = position[1];
	int count = position[2];

	int maxRecords = Integer.MAX_VALUE;
	if (!getSetting().isMaxRecordsUnlimited() && getSetting().getMaxRecords().isPresent()) {
	    maxRecords = getSetting().getMaxRecords().get();
	}

	int pageSize = Math.max(1, getSetting().getPageSize());
	int attempts = 0;

	while (collection < collectionURLs.size() && attempts < pageSize && count < maxRecords) {

	    Optional<JSONObject> collectionObject = loadCollection(collection);

	    if (collectionObject.isEmpty() || (index > 0 && (!isHarvestItems() || index > cachedItemURLs.size()))) {

		collection++;
		index = 0;
		continue;
	    }

	    attempts++;

	    Optional<OriginalMetadata> record = Optional.empty();

	    try {

		record = index == 0 ? Optional.of(createCollectionRecord(collection)) : createItemRecord(cachedItemURLs.get(index - 1));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).warn("Unable to harvest record {} of collection {}: {}", index,
			collectionURLs.get(collection), e.getMessage());
	    }

	    if (record.isPresent()) {
		response.addRecord(record.get());
		count++;
	    }

	    index++;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Harvested {} records, now at collection {}/{} position {}", count, collection,
		collectionURLs.size(), index);

	if (collection < collectionURLs.size() && count < maxRecords) {

	    response.setResumptionToken(collection + ":" + index + ":" + count);
	}

	return response;
    }

    /**
     * @param token
     * @return collection index, position in the collection, records count
     */
    static int[] parseToken(String token) {

	if (token == null || token.trim().isEmpty()) {
	    return new int[] { 0, 0, 0 };
	}

	String[] split = token.trim().split(":");
	if (split.length == 3) {
	    return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) };
	}

	// collections only token
	int collection = Integer.parseInt(split[0]);
	return new int[] { collection, 0, collection };
    }

    /**
     * @param collectionIndex
     * @return the collection, downloaded only if not already cached
     */
    private Optional<JSONObject> loadCollection(int collectionIndex) {

	if (cachedIndex == collectionIndex) {
	    return Optional.ofNullable(cachedCollection);
	}

	String collectionURL = collectionURLs.get(collectionIndex);

	cachedIndex = collectionIndex;
	cachedCollection = download(collectionURL).map(JSONObject::new).orElse(null);
	cachedCollectionWithoutItems = null;
	cachedItemURLs = new ArrayList<>();

	if (cachedCollection == null) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to download collection {}", collectionURL);
	    return Optional.empty();
	}

	cachedCollectionWithoutItems = withoutLinks(cachedCollection, "item");
	cachedItemURLs = extractLinkURLs(cachedCollection, "item", collectionURL);

	return Optional.of(cachedCollection);
    }

    /**
     * @param collectionIndex
     * @return
     */
    private OriginalMetadata createCollectionRecord(int collectionIndex) {

	JSONObject envelope = new JSONObject();
	envelope.put(BigBangSTACMapper.CATALOG_URL, publishedCatalogURL);
	envelope.put(BigBangSTACMapper.COLLECTION_URL, collectionURLs.get(collectionIndex));
	envelope.put(BigBangSTACMapper.HARVEST_ITEMS, isHarvestItems());
	envelope.put(BigBangSTACMapper.CATALOG, catalogInfo);
	envelope.put(BigBangSTACMapper.COLLECTION, cachedCollection);

	return toOriginalMetadata(envelope);
    }

    /**
     * @param itemURL
     * @return
     */
    private Optional<OriginalMetadata> createItemRecord(String itemURL) {

	Optional<String> item = download(itemURL);

	if (item.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to download item {}", itemURL);
	    return Optional.empty();
	}

	JSONObject envelope = new JSONObject();
	envelope.put(BigBangSTACMapper.CATALOG_URL, publishedCatalogURL);
	envelope.put(BigBangSTACMapper.COLLECTION_URL, collectionURLs.get(cachedIndex));
	envelope.put(BigBangSTACMapper.ITEM_URL, itemURL);
	envelope.put(BigBangSTACMapper.HARVEST_ITEMS, true);
	envelope.put(BigBangSTACMapper.CATALOG, catalogInfo);
	envelope.put(BigBangSTACMapper.COLLECTION, cachedCollectionWithoutItems);
	envelope.put(BigBangSTACMapper.ITEM, new JSONObject(item.get()));

	return Optional.of(toOriginalMetadata(envelope));
    }

    private static OriginalMetadata toOriginalMetadata(JSONObject envelope) {

	OriginalMetadata metadata = new OriginalMetadata();
	metadata.setSchemeURI(BigBangSTACMapper.BIGBANG_STAC_SCHEME_URI);
	metadata.setMetadata(envelope.toString());

	return metadata;
    }

    /**
     * Downloads the given URL as published by the catalog; if that fails, retries it rebased on the configured catalog
     * URL
     *
     * @param url
     * @return
     */
    private Optional<String> download(String url) {

	Optional<String> out = downloader.downloadOptionalString(url);

	if (out.isEmpty()) {

	    String rebased = rebase(url, publishedCatalogURL, catalogURL);

	    if (!rebased.equals(url)) {

		GSLoggerFactory.getLogger(getClass()).debug("Retrying {} as {}", url, rebased);
		out = downloader.downloadOptionalString(rebased);
	    }
	}

	return out;
    }

    /**
     * Accepts either the catalog JSON URL or the STAC folder URL (e.g. http://host:8080/stac/)
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

	return url.endsWith("/") ? url + "catalog.json" : url + "/catalog.json";
    }

    /**
     * @param catalog
     * @param catalogURL used to resolve relative hrefs
     * @return the absolute URLs of the catalog children
     */
    static List<String> extractChildURLs(JSONObject catalog, String catalogURL) {

	return extractLinkURLs(catalog, "child", catalogURL);
    }

    /**
     * @param object
     * @param rel
     * @param baseURL used to resolve relative hrefs
     * @return the absolute URLs of the links with the given relation
     */
    static List<String> extractLinkURLs(JSONObject object, String rel, String baseURL) {

	List<String> out = new ArrayList<>();

	JSONArray links = object.optJSONArray("links");
	if (links == null) {
	    return out;
	}

	URI base = URI.create(baseURL);

	for (int i = 0; i < links.length(); i++) {

	    JSONObject link = links.optJSONObject(i);
	    if (link != null && rel.equals(link.optString("rel")) && !link.optString("href").trim().isEmpty()) {

		out.add(base.resolve(link.getString("href").trim()).toString());
	    }
	}

	return out;
    }

    /**
     * If <code>url</code> lies under the folder of <code>publishedCatalogURL</code>, returns the same path under the
     * folder of <code>configuredCatalogURL</code>; otherwise returns <code>url</code> unchanged
     *
     * @param url
     * @param publishedCatalogURL
     * @param configuredCatalogURL
     * @return
     */
    static String rebase(String url, String publishedCatalogURL, String configuredCatalogURL) {

	String publishedBase = folder(publishedCatalogURL);
	String configuredBase = folder(configuredCatalogURL);

	if (url.startsWith(publishedBase)) {
	    return configuredBase + url.substring(publishedBase.length());
	}

	return url;
    }

    private static String folder(String url) {

	return url.substring(0, url.lastIndexOf('/') + 1);
    }

    private static Optional<String> findLink(JSONObject object, String rel) {

	JSONArray links = object.optJSONArray("links");
	if (links != null) {
	    for (int i = 0; i < links.length(); i++) {
		JSONObject link = links.optJSONObject(i);
		if (link != null && rel.equals(link.optString("rel")) && !link.optString("href").trim().isEmpty()) {
		    return Optional.of(link.getString("href").trim());
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * The catalog (resp. collection) is copied in every record for its license and documentation links; child
     * (resp. item) links are dropped since they are not needed by the mapper
     */
    static JSONObject withoutLinks(JSONObject object, String rel) {

	JSONObject out = new JSONObject(object.toString());

	JSONArray links = object.optJSONArray("links");
	if (links != null) {

	    JSONArray kept = new JSONArray();
	    for (int i = 0; i < links.length(); i++) {
		JSONObject link = links.optJSONObject(i);
		if (link != null && !rel.equals(link.optString("rel"))) {
		    kept.put(link);
		}
	    }
	    out.put("links", kept);
	}

	return out;
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	if (endpoint == null) {
	    return false;
	}

	String e = endpoint.toLowerCase(Locale.ROOT);
	return e.contains("bigbang") || (e.contains("boldrini.essi-lab.eu") && e.contains("/stac"));
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(BigBangSTACMapper.BIGBANG_STAC_SCHEME_URI);
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
