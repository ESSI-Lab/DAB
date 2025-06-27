package eu.essi_lab.profiler.os.handler.discover.semantics.connectors;

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

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.profiler.os.handler.discover.semantics.SemanticSource;
import eu.essi_lab.profiler.os.handler.discover.semantics.expander.SemanticExpansion;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Mattia Santoro
 */
public class GemetWebApiConnector implements SemanticSource {

    private final String base_url;

    public GemetWebApiConnector() {

	this.base_url = "http://www.eionet.europa.eu/gemet/";

    }

    @Override
    public List<URI> expandURI(URI uri, SemanticExpansion expansion) {
	String url = base_url + "getRelatedConcepts?";

	if (expansion == null)
	    expansion = SemanticExpansion.NARROWER;

	switch (expansion) {
	case NARROWER -> {
	    try {
		return expandURI(uri, new URI("http://www.w3.org/2004/02/skos/core#narrower"));
	    } catch (URISyntaxException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}
	case NARROWER_CLOSE_MATCH -> {
	    try {
		List<URI> uris = expandURI(uri, new URI("http://www.w3.org/2004/02/skos/core#narrower"));
		List<URI> closeuris = expandURI(uri, new URI("http://www.w3.org/2004/02/skos/core#closeMatch"));

		closeuris.forEach(curi -> {
		    if (!uris.contains(curi))
			uris.add(curi);
		});

		return uris;
	    } catch (URISyntaxException e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	}
	default -> {

	    GSLoggerFactory.getLogger(getClass()).error("Unknown expansion " + expansion.toString());

	}
	}

	return new ArrayList<>();
    }

    public List<URI> expandURI(URI uri, URI relation) {
	List<URI> uris = new ArrayList<>();

	String url = base_url + "getRelatedConcepts?";

	try {
	    String query = "concept_uri=" + URLEncoder.encode(uri.toString(), "UTF-8") + "&relation_uri=" + URLEncoder.encode(
		    relation.toString(), "UTF-8");

	    String str = executeRequest(url, query);
	    JSONArray array = new JSONArray(str);

	    Iterator<Object> it = array.iterator();

	    while (it.hasNext()) {
		JSONObject obj = (JSONObject) it.next();
		uris.add(new URI(obj.getString("uri")));

	    }

	} catch (UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return uris;
    }

    @Override
    public List<String> translateURI(URI uri, List<String> trLangs) {

	List<String> translations = new ArrayList<>();

	String url = base_url + "getAllTranslationsForConcept?";

	try {
	    String query = "concept_uri=" + URLEncoder.encode(uri.toString(), "UTF-8") + "&property_uri=" + URLEncoder.encode(
		    "http://www.w3.org/2004/02/skos/core#prefLabel",
		    "UTF-8");
	    String str = executeRequest(url, query);
	    JSONArray array = new JSONArray(str);

	    Iterator<Object> it = array.iterator();

	    while (it.hasNext()) {
		JSONObject obj = (JSONObject) it.next();

		if (trLangs.contains(obj.getString("language")))
		    translations.add(obj.getString("string"));
	    }

	} catch (UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return translations;
    }

    private List<URI> getConceptsMatchingKeyword(String searchTerm, String lang) {

	List<URI> uris = new ArrayList<>();

	String url = base_url + "getConceptsMatchingKeyword?";

	String query = "keyword=" + searchTerm + "&search_mode=4&language=" + lang;
	try {

	    String str = executeRequest(url, query);
	    JSONArray array = new JSONArray(str);

	    Iterator<Object> it = array.iterator();

	    while (it.hasNext()) {
		JSONObject obj = (JSONObject) it.next();
		uris.add(new URI(obj.getString("uri")));
	    }

	} catch (UnsupportedEncodingException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);

	}

	return uris;

    }

    @Override
    public List<URI> getConceptsMatchingKeyword(String searchTerm) {

	List<String> languages = Arrays.asList("it", "en", "fr");

	for (String l : languages) {
	    List<URI> concepts = getConceptsMatchingKeyword(searchTerm, l);

	    if (concepts.size() > 0)
		return concepts;
	}

	return new ArrayList<>();

    }

    private String executeRequest(String url, String query) throws IOException {

	Downloader d = new Downloader();
	d.setConnectionTimeout(TimeUnit.SECONDS, 20);

	String request_url = url + query;

	InputStream stream = d.downloadOptionalStream(request_url).get();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	IOUtils.copy(stream, baos);
	stream.close();
	baos.close();
	String str = new String(baos.toByteArray());

	return str;

    }
}
