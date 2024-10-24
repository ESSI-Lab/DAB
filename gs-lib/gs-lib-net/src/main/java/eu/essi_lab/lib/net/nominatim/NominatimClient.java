/**
 * 
 */
package eu.essi_lab.lib.net.nominatim;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONArray;

import dev.failsafe.FailsafeException;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.nominatim.query.FreeFormQuery;
import eu.essi_lab.lib.net.nominatim.query.NominatimQuery;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.JSONUtils;

/**
 * <a href="https://nominatim.org/release-docs/develop/api/Search/">OSM Nominatim Client<a/>
 * 
 * @author Fabrizio
 */
public class NominatimClient {

    /**
     * 
     */
    private static final String BASE_ENDPOINT = "https://nominatim.openstreetmap.org/search?";

    /**
     * @param query
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     * @throws FailsafeException
     */
    public List<NominatimResponse> search(NominatimQuery query)
	    throws FailsafeException, IOException, InterruptedException, URISyntaxException {

	GSLoggerFactory.getLogger(getClass()).debug("Searching for query '{}' STARTED", query.compose());

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 5);

	String url = BASE_ENDPOINT + "format=jsonv2&" + query.compose();

	HttpResponse<InputStream> response = downloader.downloadResponse(url);

	int statusCode = response.statusCode();
	InputStream body = response.body();

	if (statusCode == 200 && body != null) {

	    String stringResponse = IOStreamUtils.asUTF8String(body);

	    JSONArray array = new JSONArray(stringResponse);

	    GSLoggerFactory.getLogger(getClass()).debug("Searching for query '{}' ENDED", query.compose());

	    return JSONUtils.map(array).//
		    stream().//
		    map(o -> new NominatimResponse(o)).//
		    collect(Collectors.toList());
	}

	throw new IOException("Unable to perform query, unexpecter error occurred");
    }

    /**
     * @param args
     * @throws FailsafeException
     * @throws IOException
     * @throws InterruptedException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws FailsafeException, IOException, InterruptedException, URISyntaxException {

	FreeFormQuery query = new FreeFormQuery();
	query.setQuery("rome,italy");

	NominatimClient client = new NominatimClient();
	List<NominatimResponse> response = client.search(query);
	System.out.println(response);
    }
}
