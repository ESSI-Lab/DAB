package eu.essi_lab.authentication.util;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class provides an helper to encode strings in format who will be
 * understood by twitter'oauth signature phase. I pick this class directly from
 * gi-suite.
 * 
 * @author pezzati
 */
public class RFC3986Encoder {

    public static String encode(String s, String encoding) throws UnsupportedEncodingException {
	if (s == null) {
	    return "";
	}
	return URLEncoder.encode(s, encoding).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }
}
