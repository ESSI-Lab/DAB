package eu.essi_lab.lib.net.nvs;

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

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class NVSClient {

    private static ExpiringCache<String> cache = new ExpiringCache<>();
    static {
	cache.setMaxSize(500);
	cache.setDuration(TimeUnit.MINUTES.toMillis(30));
    }

    public String getLabel(String urn) {
	String ret = cache.get(urn);
	if (ret != null) {
	    return ret;
	}
	// transformation
	// http://www.seadatanet.org/urnurl/SDN:L05::111
	// http://vocab.nerc.ac.uk/collection/L05/current/111/
	if (urn.contains("www.seadatanet.org/urnurl/SDN:")) {
	    String rest = urn.split("www.seadatanet.org/urnurl/SDN:")[1];
	    String[] terms = rest.split("::");
	    urn = "http://vocab.nerc.ac.uk/collection/" + terms[0] + "/current/" + terms[1] + "/";
	}

	Downloader d = new Downloader();
	Optional<InputStream> stream = d.downloadOptionalStream(urn + "?_profile=nvs&_mediatype=application/rdf+xml");
	if (stream.isPresent()) {
	    XMLDocumentReader reader;
	    try {
		reader = new XMLDocumentReader(stream.get());
		ret = reader.evaluateString("/*:RDF/*:Description/*:prefLabel");
		cache.put(urn, ret);
		return ret;
	    } catch (Exception e) {
		return null;
	    }

	} else {
	    GSLoggerFactory.getLogger(getClass()).error("NVS not found: {}", urn);
	}
	return null;

    }

}
