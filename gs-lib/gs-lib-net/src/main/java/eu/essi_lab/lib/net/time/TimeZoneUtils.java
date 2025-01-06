package eu.essi_lab.lib.net.time;

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
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;

import org.xml.sax.SAXException;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class TimeZoneUtils {

    private ExpiringCache<TimeZoneInfo> cache = new ExpiringCache<TimeZoneInfo>();

    public TimeZoneUtils() {
	cache.setMaxSize(1000);
	cache.setDuration(1000 * 60 * 60 * 24l);
    }

    public TimeZoneInfo getTimeZoneInfo(BigDecimal latitude, BigDecimal longitude) throws SAXException, IOException {
	String id = latitude + "-" + longitude;
	TimeZoneInfo ret = cache.get(id);
	if (ret != null) {
	    return ret;
	}
	Downloader downloader = new Downloader();
	Optional<InputStream> optionalStream = downloader
		.downloadOptionalStream("http://api.geonames.org/timezone?lat=" + latitude + "&lng=" + longitude + "&username=myuser");
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    XMLDocumentReader reader = new XMLDocumentReader(stream);
	    ret = new TimeZoneInfo(reader);
	    cache.put(id, ret);
	    return ret;
	}
	return null;
    }

}
