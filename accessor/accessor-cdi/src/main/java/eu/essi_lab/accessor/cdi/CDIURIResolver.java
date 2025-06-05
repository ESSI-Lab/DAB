package eu.essi_lab.accessor.cdi;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class CDIURIResolver implements javax.xml.transform.URIResolver {

    Logger logger = GSLoggerFactory.getLogger(getClass());

    private static ExpiringCache<byte[]> cache = new ExpiringCache<>();

    public CDIURIResolver() {
	cache.setDuration(600000);
	cache.setMaxSize(100);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
	if (base == null || base.isEmpty()) {
	    Downloader downloader = new Downloader();
	    byte[] cachedSource = cache.get(href);
	    if (cachedSource != null) {
		ByteArrayInputStream bais = new ByteArrayInputStream(cachedSource);
		return new StreamSource(bais);
	    }
	    logger.info("Resolving href: " + href + "  base: " + base);
	    String redirect = HttpConnectionUtils.resolveRedirect(href); // this is a needed workaround for https ->
									 // http
	    // redirects
	    Optional<InputStream> optionalStream = downloader.downloadOptionalStream(redirect);
	    if (optionalStream.isPresent()) {
		InputStream stream = optionalStream.get();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
		    IOUtils.copy(stream, baos);
		    stream.close();
		    baos.close();
		    byte[] bytes = baos.toByteArray();
		    cache.put(href, bytes);
		    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		    return new StreamSource(bais);
		} catch (IOException e) {
		    e.printStackTrace();
		}

	    }
	}
	return null;
    }

}
