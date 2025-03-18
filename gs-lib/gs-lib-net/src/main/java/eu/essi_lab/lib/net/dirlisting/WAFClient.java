/**
 * 
 */
package eu.essi_lab.lib.net.dirlisting;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class WAFClient {

    private URL url;
    private boolean absolutePathReference;

    /**
     * 
     */
    public WAFClient() {

    }

    /**
     * @param url
     * @throws IOException
     */
    public WAFClient(URL url) {

	this.url = url;
    }

    /**
     * Set to <code>true</code> if the server uses 'absolute path reference' instead of 'relative path reference'. <br>
     * According to <a href="https://www.rfc-editor.org/rfc/rfc3986#section-4.2">RFC 3986 section 4.2</a> <i>A relative
     * reference that begins with a single slash character
     * is termed an absolute-path reference</i>
     * 
     * @see <a href=
     *      "https://webmasters.stackexchange.com/questions/56840/what-is-the-purpose-of-leading-slash-in-html-urls">Purpose
     *      of leading slash</a>
     * @param absolutePathReference
     */
    public void setUseAbsolutePathReference(boolean absPathRef) {

	this.absolutePathReference = absPathRef;
    }

    /**
     * @return
     * @throws Exception
     */
    public List<URL> deepListFiles() throws Exception {

	return deepListFiles(new WAF_URL(this.url), null, null, this.absolutePathReference);
    }

    /**
     * @param filesFilter
     * @return
     * @throws Exception
     */
    public List<URL> deepListFiles(Predicate<? super URL> filesFilter) throws Exception {

	return deepListFiles(new WAF_URL(this.url), filesFilter, null, this.absolutePathReference);
    }

    /**
     * @param filesFilter
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public List<URL> deepListFiles(Predicate<? super URL> filesFilter, Predicate<? super URL> foldersFilter) throws Exception {

	return deepListFiles(new WAF_URL(this.url), filesFilter, foldersFilter, this.absolutePathReference);
    }

    /**
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public List<WAF_URL> deepListFolders(Predicate<? super URL> foldersFilter) throws Exception {

	return deepListFolders(new WAF_URL(this.url), foldersFilter, this.absolutePathReference);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<WAF_URL> deepListFolders() throws Exception {

	return deepListFolders(new WAF_URL(this.url), null, this.absolutePathReference);
    }

    /**
     * @param fileFilter
     * @return
     * @throws Exception
     */
    public List<URL> listFiles(Predicate<? super URL> fileFilter) throws Exception {

	return findURLs(true, fileFilter, null, this.absolutePathReference);
    }

    /**
     * @return
     * @throws IOException
     */
    public List<URL> listFiles() throws Exception {

	return findURLs(this.url, true, null, null, this.absolutePathReference, null, null);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<WAF_URL> listFolders() throws Exception {

	return listFolders(new WAF_URL(this.url), null, this.absolutePathReference);
    }

    /**
     * @param directoryFilter
     * @return
     * @throws Exception
     */
    public List<WAF_URL> listFolders(Predicate<? super URL> directoryFilter) throws Exception {

	return listFolders(new WAF_URL(this.url), directoryFilter, this.absolutePathReference);
    }

    /**
     * @param url
     * @return
     */
    public static List<WAF_URL> listFolders(WAF_URL url) throws Exception {

	return listFolders(url, null);
    }

    /**
     * @param url
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> listFolders(WAF_URL url, boolean absolutePathReference) throws Exception {

	return listFolders(url, null, absolutePathReference);
    }

    /**
     * @param dirURL
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(WAF_URL dirURL) throws Exception {

	return deepListFiles(dirURL, null, null, false);
    }

    /**
     * @param dirURL
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(WAF_URL dirURL, boolean absolutePathReference) throws Exception {

	return deepListFiles(dirURL, null, null, absolutePathReference);
    }

    /**
     * @param dirURL
     * @param filesFilter
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(WAF_URL dirURL, Predicate<? super URL> filesFilter) throws Exception {

	return deepListFiles(dirURL, filesFilter, null, false);
    }

    /**
     * @param dirURL
     * @param filesFilter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(WAF_URL dirURL, Predicate<? super URL> filesFilter, boolean absolutePathReference)
	    throws Exception {

	return deepListFiles(dirURL, filesFilter, null, absolutePathReference);
    }

    /***
     * @param dirURL
     * @param filesFilter
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(//
	    WAF_URL dirURL, //
	    Predicate<? super URL> filesFilter, //
	    Predicate<? super URL> foldersFilter//
    ) throws Exception {

	return deepListFiles(dirURL, filesFilter, foldersFilter, false);
    }

    /***
     * @param dirURL
     * @param filesFilter
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public static List<URL> deepListFiles(//
	    WAF_URL dirURL, //
	    Predicate<? super URL> filesFilter, //
	    Predicate<? super URL> foldersFilter, //
	    boolean absolutePathReference) throws Exception {

	List<URL> out = listFiles(dirURL, filesFilter, foldersFilter, absolutePathReference);
	List<WAF_URL> folders = listFolders(dirURL, foldersFilter, absolutePathReference);

	for (WAF_URL wAF_URL : folders) {
	    add(out, wAF_URL, filesFilter, foldersFilter, absolutePathReference);
	}

	return out;
    }

    /**
     * @param dirURL
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> deepListFolders(WAF_URL dirURL) throws Exception {

	return deepListFolders(dirURL, null, false);
    }

    /**
     * @param dirURL
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> deepListFolders(WAF_URL dirURL, boolean absolutePathReference) throws Exception {

	return deepListFolders(dirURL, null, absolutePathReference);
    }

    /**
     * @param dirURL
     * @param foldersFilter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> deepListFolders(//
	    WAF_URL dirURL, //
	    Predicate<? super URL> foldersFilter) throws Exception {

	return deepListFolders(dirURL, foldersFilter, false);
    }

    /**
     * @param dirURL
     * @param foldersFilter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> deepListFolders(//
	    WAF_URL dirURL, //
	    Predicate<? super URL> foldersFilter, //
	    boolean absolutePathReference) throws Exception {

	List<WAF_URL> out = listFolders(dirURL, foldersFilter, absolutePathReference);

	ArrayList<WAF_URL> arrayList = new ArrayList<WAF_URL>();

	for (WAF_URL waf_URL : out) {
	    add(arrayList, waf_URL, foldersFilter, absolutePathReference);
	}

	out.addAll(arrayList);

	return out;
    }

    /**
     * @param url
     * @return
     */
    public static List<URL> listFiles(WAF_URL url) throws Exception {

	return findURLs(url.getURL(), true, null, null, false, null, null);
    }

    /**
     * @param url
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<URL> listFiles(WAF_URL url, boolean absolutePathReference) throws Exception {

	return findURLs(url.getURL(), true, null, null, absolutePathReference, null, null);
    }
    
    /**
     * @param url
     * @param absolutePathReference
     * @param user
     * @param password
     * @return
     * @throws Exception
     */
    public static List<URL> listFiles(WAF_URL url, boolean absolutePathReference, String user, String password) throws Exception {

	return findURLs(url.getURL(), true, null, null, absolutePathReference, user, password);
    }

    /**
     * @param url
     * @param filter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<URL> listFiles(WAF_URL url, Predicate<? super URL> filter, boolean absolutePathReference) throws Exception {

	return findURLs(url.getURL(), true, filter, null, absolutePathReference, null, null);
    }

    /**
     * @param url
     * @return
     */
    public static List<URL> listFiles(WAF_URL url, Predicate<? super URL> filter) throws Exception {

	return findURLs(url.getURL(), true, filter, null, false, null, null);
    }

    /**
     * @param url
     * @param filter
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public static List<URL> listFiles(WAF_URL url, Predicate<? super URL> filter, Predicate<? super URL> foldersFilter) throws Exception {

	return findURLs(url.getURL(), true, filter, foldersFilter, false, null, null);
    }

    /**
     * @param url
     * @param filter
     * @param foldersFilter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<URL> listFiles(//
	    WAF_URL url, //
	    Predicate<? super URL> filter, //
	    Predicate<? super URL> foldersFilter, //
	    boolean absolutePathReference) throws Exception {

	return findURLs(url.getURL(), true, filter, foldersFilter, absolutePathReference, null, null);
    }

    /**
     * @param url
     * @param foldersFilter
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> listFolders(WAF_URL url, Predicate<? super URL> foldersFilter) throws Exception {

	return findURLs(url.getURL(), false, null, foldersFilter, false, null, null).//
		stream().//
		map(WAF_URL::new).//
		collect(Collectors.toList());
    }

    /**
     * @param url
     * @param foldersFilter
     * @param absolutePathReference
     * @return
     * @throws Exception
     */
    public static List<WAF_URL> listFolders(WAF_URL url, Predicate<? super URL> foldersFilter, boolean absolutePathReference)
	    throws Exception {

	return findURLs(url.getURL(), false, null, foldersFilter, absolutePathReference, null, null).stream().//
		map(WAF_URL::new).//
		collect(Collectors.toList());
    }

    /**
     * @param out
     * @param url
     * @param filesFilter
     * @param foldersFilter
     * @param absolutePathReference
     * @throws Exception
     */
    private static void add(//
	    List<URL> out, //
	    WAF_URL url, //
	    Predicate<? super URL> filesFilter, //
	    Predicate<? super URL> foldersFilter, //
	    boolean absolutePathReference) throws Exception {

	List<URL> listFiles = listFiles(url, filesFilter, absolutePathReference);

	out.addAll(listFiles);

	List<WAF_URL> folders = listFolders(url, foldersFilter, absolutePathReference);

	for (WAF_URL wafURL : folders) {
	    add(out, wafURL, filesFilter, foldersFilter, absolutePathReference);
	}
    }

    /**
     * @param out
     * @param url
     * @param foldersFilter
     * @throws Exception
     */
    private static void add(//
	    List<WAF_URL> out, //
	    WAF_URL url, //
	    Predicate<? super URL> foldersFilter, boolean absolutePathReference) throws Exception {

	List<WAF_URL> folders = listFolders(url, foldersFilter, absolutePathReference);

	out.addAll(folders);

	for (WAF_URL wafURL : folders) {
	    add(out, wafURL, foldersFilter, absolutePathReference);
	}
    }

    /**
     * @param link
     * @return
     */
    private static URL toURL(String link) {

	try {
	    return new URL(link);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(WAFClient.class).error(e.getMessage(), e);
	}

	return null;
    }

    /**
     * @param files
     * @param fileFilter
     * @param dirFilter
     * @return
     * @throws Exception
     */
    private List<URL> findURLs(boolean files, Predicate<? super URL> fileFilter, Predicate<? super URL> dirFilter,
	    boolean absolutePathReference) throws Exception {

	return findURLs(this.url, files, fileFilter, dirFilter, absolutePathReference, null, null);
    }

    
    
    /**
     * @param url
     * @param files
     * @param fileFilter
     * @param dirFilter
     * @param absPathRef
     * @param username
     * @param password
     * @return
     */
    private static List<URL> findURLs(//
	    URL url, //
	    boolean files, //
	    Predicate<? super URL> fileFilter, //
	    Predicate<? super URL> dirFilter, //
	    boolean absPathRef, String username, String password) throws Exception {

	if (fileFilter == null) {

	    fileFilter = u -> true;
	}

	if (dirFilter == null) {

	    dirFilter = u -> true;
	}

	HREFGrabberClient client = new HREFGrabberClient();
	if(username != null && password != null) {
	    client.setUsername(username);
	    client.setPassword(password);
	}

	List<String> links = client.grabLinks(url, null);

	return links.//
		stream().//

		// if present, removes initial '/' from the link
		map(l -> absPathRef && l.startsWith("/") ? l.substring(1, l.length()) : l).
		// filter(l -> !l.isEmpty()).//

		filter(l -> !l.startsWith("?")).// excluding ?C=N;O=D, ?C=M;O=A, ?C=S;O=A, ?C=D;O=A

		// excluding parent directory
		filter(l -> (!absPathRef && !l.startsWith("/")) || (absPathRef && !l.equals(getParentOfAbsoluteUrl(l, url)))).

		// excluding files OR excluding folders
		filter(l -> (files && !l.endsWith("/")) || (!files && l.endsWith("/"))).

		// externalizes the link
		map(l -> client.externalizeLink(url, l, absPathRef)).

		map(l -> toURL(l)).// mapping to URL
		filter(Objects::nonNull).// excluding invalid URLs
		filter(fileFilter).// optional file filter
		filter(dirFilter).// optional directory filter
		collect(Collectors.toList());

    }

    /**
     * @param url
     * @return
     */
    private static String getParentOfAbsoluteUrl(String link, URL url) {

	String path = url.getPath();
	path = path.substring(1, path.length());
	if (path.endsWith("/")) {
	    path = path.substring(0, path.length() - 1);
	}

	String[] split = path.split("/");

	String parent = "";
	for (int i = 0; i < split.length - 1; i++) {
	    parent += split[i] + "/";
	}

	return parent;
    }
}
