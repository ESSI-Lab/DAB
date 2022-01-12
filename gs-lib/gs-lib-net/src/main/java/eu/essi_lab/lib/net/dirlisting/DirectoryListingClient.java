package eu.essi_lab.lib.net.dirlisting;

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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class DirectoryListingClient extends HREFGrabberClient {

    /**
     * @param url
     * @throws IOException
     */
    public DirectoryListingClient(URL url) {

	super(url);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<URL> listAllFiles() throws Exception {

	List<URL> out = listFiles();
	List<DirectoryURL> directories = listDirectories();

	for (DirectoryURL directoryURL : directories) {
	    add(out, directoryURL);
	}

	return out;
    }

    /**
     * @return
     * @throws IOException
     */
    public List<URL> listFiles() throws Exception {

	return findURLs(this.url, true);
    }

    /**
     * @param url
     * @return
     */
    public List<URL> listFiles(DirectoryURL url) throws Exception {

	return findURLs(url.getURL(), true);
    }

    /**
     * @return
     * @throws Exception
     */
    public List<DirectoryURL> listDirectories() throws Exception {

	return findURLs(this.url, false).//
		stream().//
		map(DirectoryURL::new).//
		collect(Collectors.toList());
    }

    /**
     * @param url
     * @return
     */
    public List<DirectoryURL> listDirectories(DirectoryURL url) throws Exception {

	return findURLs(url.getURL(), false).//
		stream().//
		map(DirectoryURL::new).//
		collect(Collectors.toList());
    }

    /**
     * @param out
     * @param url
     * @throws Exception
     */
    private void add(List<URL> out, DirectoryURL url) throws Exception {

	out.addAll(listFiles(url));

	List<DirectoryURL> directories = listDirectories(url);
	for (DirectoryURL directoryURL : directories) {
	    add(out, directoryURL);
	}
    }

    /**
     * @param link
     * @return
     */
    private URL toURL(String link) {

	try {
	    return new URL(link);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return null;
    }

    /**
     * @param url
     * @param files
     * @return
     * @throws Exception
     */
    private List<URL> findURLs(URL url, boolean files) throws Exception {

	return grabLinks_(url, null).//
		stream().//
		filter(l -> !l.startsWith("?")).// excluding ?C=N;O=D, ?C=M;O=A, ?C=S;O=A, ?C=D;O=A
		filter(l -> !l.startsWith("/")).// excluding parent directory
		filter(l -> (files && !l.endsWith("/")) // excluding files OR
			|| (!files && l.endsWith("/"))) // excluding directories
		.map(l -> externalizeLink(url, l)).// externalize the links
		map(l -> toURL(l)).// mapping to URL
		filter(Objects::nonNull).// excluding invalid URLs
		collect(Collectors.toList());
    }
}
