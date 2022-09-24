package eu.essi_lab.lib.net.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A mocked download, useful for tests, that downloads only what indicated in the constructor.
 * 
 * @author boldrini
 */
public class MockedDownloader extends Downloader {

    private String[] urlContentPairs;

    /**
     * Creates a mocked downloader that will reply with the specified content.
     * 
     * @param urlContentPairs pairs of url and correspondent content, in order to reply with the correspondent content
     *        for a request for a specific url. if a single string is passed to the constructor, then all requests will
     *        reply with the given string.
     */
    public MockedDownloader(String... urlContentPairs) {
	this.urlContentPairs = urlContentPairs;
    }

    @Override
    public Optional<InputStream> downloadStream(String url) {
	String response = getResponse(url);
	return Optional.of(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Optional<String> downloadString(String url) {
	return Optional.ofNullable(getResponse(url));
    }

    private String getResponse(String requestUrl) {
	switch (urlContentPairs.length) {
	case 0:
	    return "";
	case 1:
	    return urlContentPairs[0];
	default:
	    for (int i = 0; i < urlContentPairs.length; i += 2) {
		if (i + 1 < urlContentPairs.length) {
		    String url = urlContentPairs[i];
		    String content = urlContentPairs[i + 1];
		    if (url.equals(requestUrl)) {
			return content;
		    }
		}
	    }
	    return "";
	}
    }

}
