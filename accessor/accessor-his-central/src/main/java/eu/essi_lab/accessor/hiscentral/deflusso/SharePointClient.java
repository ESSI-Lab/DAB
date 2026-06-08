package eu.essi_lab.accessor.hiscentral.deflusso;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Client for browsing a publicly shared Microsoft SharePoint / OneDrive folder via a sharing link
 * ("anyone with the link" access), with no credentials required.
 * <p>
 * The sharing link is followed (collecting the anonymous guest session cookie that SharePoint issues
 * during the redirect chain) to resolve the underlying site and server-relative folder path. Folder
 * contents are then read through the anonymous SharePoint REST API
 * ({@code _api/web/GetFolderByServerRelativeUrl}).
 *
 * @author boldrini
 */
public class SharePointClient {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
	    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Safari/537.36";

    private final HttpClient client;

    private final String siteUrl;
    private final String rootFolderPath;
    private String currentFolderPath;

    /**
     * @param sharingUrl SharePoint folder sharing link (e.g. {@code https://tenant-my.sharepoint.com/:f:/g/...})
     *                   set to "anyone with the link can view"
     */
    public SharePointClient(String sharingUrl) throws IOException {

	CookieManager cookieManager = new CookieManager();
	cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

	this.client = HttpClient.newBuilder()//
		.cookieHandler(cookieManager)//
		.followRedirects(Redirect.NORMAL)//
		.connectTimeout(Duration.ofSeconds(30))//
		.build();

	URI resolved = resolveSharingLink(sharingUrl);
	this.rootFolderPath = extractFolderPath(resolved);
	this.siteUrl = extractSiteUrl(resolved);
	this.currentFolderPath = rootFolderPath;
    }

    /**
     * @return folder names in the current directory
     */
    public List<String> listFolders() throws IOException {

	List<String> folders = new ArrayList<>();
	for (JSONObject child : getChildren("Folders")) {
	    folders.add(child.getString("Name"));
	}
	return folders;
    }

    /**
     * Enters a sub-folder by name (relative to the current directory).
     */
    public void cd(String folderName) throws IOException {

	for (String folder : listFolders()) {
	    if (folder.equals(folderName)) {
		currentFolderPath = currentFolderPath + "/" + folderName;
		return;
	    }
	}
	throw new IOException("Folder not found: " + folderName);
    }

    /**
     * Returns to the folder referenced by the sharing link.
     */
    public void cdRoot() {

	currentFolderPath = rootFolderPath;
    }

    /**
     * @return the server-relative path of the current directory
     */
    public String getCurrentFolderPath() {

	return currentFolderPath;
    }

    /**
     * @param pattern simple glob, e.g. {@code *.xlsx} ({@code null} or {@code *} matches everything)
     * @return file names in the current directory matching the pattern
     */
    public List<String> listFiles(String pattern) throws IOException {

	List<String> files = new ArrayList<>();
	for (JSONObject child : getChildren("Files")) {
	    String name = child.getString("Name");
	    if (matchesPattern(name, pattern)) {
		files.add(name);
	    }
	}
	return files;
    }

    /**
     * Downloads and opens an Excel workbook from the current directory.
     */
    public XSSFWorkbook readXlsx(String fileName) throws IOException {

	String filePath = currentFolderPath + "/" + fileName;
	String url = siteUrl + "/_api/web/GetFileByServerRelativeUrl('" + encodePath(filePath) + "')/$value";
	try (InputStream in = get(url, null)) {
	    return new XSSFWorkbook(in);
	}
    }

    private URI resolveSharingLink(String sharingUrl) throws IOException {

	HttpResponse<Void> response = send(sharingUrl, null, HttpResponse.BodyHandlers.discarding());
	if (response.statusCode() >= 400) {
	    throw new IOException("Unable to open sharing link, HTTP " + response.statusCode() + ": " + sharingUrl);
	}
	URI resolved = response.uri();
	if (resolved.getRawQuery() == null || !resolved.getRawQuery().contains("id=")) {
	    throw new IOException("Sharing link did not resolve to a folder (no 'id' in " + resolved
		    + "). Make sure the link points to a folder and is shared with 'anyone with the link'.");
	}
	return resolved;
    }

    private static String extractFolderPath(URI resolved) throws IOException {

	for (String param : resolved.getRawQuery().split("&")) {
	    if (param.startsWith("id=")) {
		return URLDecoder.decode(param.substring(3), StandardCharsets.UTF_8);
	    }
	}
	throw new IOException("No folder id found in resolved URL: " + resolved);
    }

    private static String extractSiteUrl(URI resolved) throws IOException {

	String path = resolved.getPath();
	int layoutsIndex = path.indexOf("/_layouts");
	if (layoutsIndex < 0) {
	    throw new IOException("Unexpected resolved URL (no '/_layouts'): " + resolved);
	}
	String webPath = path.substring(0, layoutsIndex);
	return resolved.getScheme() + "://" + resolved.getAuthority() + webPath;
    }

    private List<JSONObject> getChildren(String childType) throws IOException {

	String url = siteUrl + "/_api/web/GetFolderByServerRelativeUrl('" + encodePath(currentFolderPath) + "')/"
		+ childType;
	String body = getString(url);
	JSONObject json = new JSONObject(body);
	JSONArray value = json.optJSONArray("value");
	List<JSONObject> children = new ArrayList<>();
	if (value != null) {
	    for (int i = 0; i < value.length(); i++) {
		children.add(value.getJSONObject(i));
	    }
	}
	return children;
    }

    private String getString(String url) throws IOException {

	HttpResponse<String> response = send(url, "application/json;odata=nometadata",
		HttpResponse.BodyHandlers.ofString());
	if (response.statusCode() >= 400) {
	    throw new IOException("SharePoint API error " + response.statusCode() + " for " + url + ": "
		    + response.body());
	}
	return response.body();
    }

    private InputStream get(String url, String accept) throws IOException {

	HttpResponse<InputStream> response = send(url, accept, HttpResponse.BodyHandlers.ofInputStream());
	if (response.statusCode() >= 400) {
	    throw new IOException("SharePoint API error " + response.statusCode() + " for " + url);
	}
	return response.body();
    }

    private <T> HttpResponse<T> send(String url, String accept, HttpResponse.BodyHandler<T> handler)
	    throws IOException {

	HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))//
		.header("User-Agent", USER_AGENT)//
		.timeout(Duration.ofSeconds(60))//
		.GET();
	if (accept != null) {
	    builder.header("Accept", accept);
	}
	try {
	    return client.send(builder.build(), handler);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    throw new IOException("Request interrupted: " + url, e);
	}
    }

    /**
     * URL-encodes a server-relative path for use inside an OData string literal, preserving slashes and
     * doubling single quotes as required by SharePoint.
     */
    static String encodePath(String path) {

	String escaped = path.replace("'", "''");
	String[] segments = escaped.split("/", -1);
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < segments.length; i++) {
	    if (i > 0) {
		sb.append('/');
	    }
	    sb.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
	}
	return sb.toString();
    }

    static boolean matchesPattern(String name, String pattern) {

	if (pattern == null || pattern.isEmpty() || "*".equals(pattern)) {
	    return true;
	}
	String n = name.toLowerCase(Locale.ROOT);
	String p = pattern.toLowerCase(Locale.ROOT);
	if (p.startsWith("*") && p.endsWith("*") && p.length() > 2) {
	    return n.contains(p.substring(1, p.length() - 1));
	}
	if (p.startsWith("*")) {
	    return n.endsWith(p.substring(1));
	}
	if (p.endsWith("*")) {
	    return n.startsWith(p.substring(0, p.length() - 1));
	}
	return n.equals(p);
    }

    /**
     * Example: lists every sub-folder of the shared folder and prints every {@code *.xlsx} file found.
     */
    public static void main(String[] args) throws Exception {

	String url = args.length > 0 ? args[0]
		: "https://cnrsc-my.sharepoint.com/:f:/g/personal/enrico_boldrini_cnr_it/IgDKzX2pfPsjSItF5ROJac6-AXIn4fWaJP6_1bGif-8w5hw?e=z5v4xO";

	SharePointClient client = new SharePointClient(url);

	System.out.println("Shared folder: " + client.getCurrentFolderPath());
	for (String xlsx : client.listFiles("*.xlsx")) {
	    System.out.println("  [root] " + xlsx);
	}

	for (String folder : client.listFolders()) {
	    System.out.println("Folder: " + folder);
	    client.cd(folder);
	    for (String xlsx : client.listFiles("*.xlsx")) {
		System.out.println("  " + folder + "/" + xlsx);
		try (XSSFWorkbook workbook = client.readXlsx(xlsx)) {
		    printWorkbook(workbook);
		}
	    }
	    client.cdRoot();
	}
    }

    /**
     * Prints the content of every sheet of a workbook as tab-separated rows.
     */
    private static void printWorkbook(XSSFWorkbook workbook) {

	DataFormatter formatter = new DataFormatter();
	for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
	    Sheet sheet = workbook.getSheetAt(s);
	    System.out.println("    --- sheet: " + sheet.getSheetName() + " ---");
	    for (Row row : sheet) {
		StringBuilder sb = new StringBuilder();
		int lastCell = row.getLastCellNum();
		for (int c = 0; c < lastCell; c++) {
		    if (c > 0) {
			sb.append('\t');
		    }
		    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		    sb.append(formatter.formatCellValue(cell));
		}
		System.out.println("    " + sb);
	    }
	}
    }
}
