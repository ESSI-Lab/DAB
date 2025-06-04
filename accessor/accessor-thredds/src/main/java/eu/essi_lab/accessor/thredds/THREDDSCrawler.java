package eu.essi_lab.accessor.thredds;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.List;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author boldrini
 */
public class THREDDSCrawler {

    private String endpoint;

    public THREDDSCrawler(String endpoint) {
	this.endpoint = endpoint;
    }

    public THREDDSPage crawl(THREDDSReference reference) throws Exception {
	THREDDSPage ret = crawl(null, new URL(endpoint), new THREDDSReference(), reference);
	return ret;
    }

    private static ExpiringCache<XMLDocumentReader> pageCache;
    static {
	pageCache = new ExpiringCache<>();
	pageCache.setDuration(1000 * 60 * 60l);
	pageCache.setMaxSize(100);
    }

    private THREDDSPage crawl(THREDDSPage father, URL baseURL, THREDDSReference currentReference, THREDDSReference targetReference)
	    throws Exception {
	XMLDocumentReader reader = pageCache.get(baseURL.toExternalForm());
	if (reader == null) {
	    Downloader downloader = new Downloader();
	    HttpResponse<InputStream> response = downloader.downloadResponse(baseURL.toExternalForm());
	    if (response.statusCode() == 403 || response.statusCode() == 401) {
		// skip
		return father;
	    } else {
		InputStream stream = response.body();
		reader = new XMLDocumentReader(stream);
		pageCache.put(baseURL.toExternalForm(), reader);
	    }
	}
	THREDDSPage page = new THREDDSPage(father, baseURL, reader);
	page.setReference(currentReference);
	List<String> links = page.getCatalogRefs();
	Integer target = targetReference.getFirstTarget();
	if (target == null) {
	    return page;
	}
	String link = links.get(target);
	URL childURL = new URL(baseURL, link);
	THREDDSReference nextReference = currentReference.clone();
	nextReference.addTarget(target);
	return crawl(page, childURL, nextReference, targetReference.getRestTarget());
    }
}
