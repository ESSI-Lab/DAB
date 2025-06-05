package eu.essi_lab.accessor.dws.client;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.InterpolationType;

public class DWSClient {

    private String endpoint;

    private Downloader downloader = new Downloader();

    public DWSClient(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * @return
     */
    public String getEndpoint() {

	return endpoint;
    }

    public List<String> getRegions() {
	List<String> regionsList = new ArrayList<String>();
	// hard coded list string --
	regionsList = Stream.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S",
				     "T", "U", "V", "W", "X")
		.collect(Collectors.toList());
	return regionsList;

    }

    public InputStream getStations(String region) {
	InputStream is = null;

	Optional<InputStream> res = downloader.downloadOptionalStream(getEndpoint() + "HyStations.aspx?Region=" + region + "&StationType=rbRiver");
	if (res.isPresent()) {
	    is = res.get();
	}

	return is;

    }

    public InterpolationType[] interpolations = new InterpolationType[] { InterpolationType.CONTINUOUS, InterpolationType.AVERAGE,
	    InterpolationType.MAX, InterpolationType.MIN };

    public InterpolationType[] getInterpolations() {
	return interpolations;
    }

    public InputStream getData(String stationId, String dataType, Date begin, Date end) throws Exception {
	InputStream is = null;
	String start = ISO8601DateTimeUtils.getISO8601Date(begin);
	String finish = ISO8601DateTimeUtils.getISO8601Date(end);
	Optional<InputStream> res = downloader.downloadOptionalStream(getEndpoint() + "HyData.aspx?Station=" + stationId + "100.00&DataType="
		+ dataType + "&StartDT=" + start + "&EndDT=" + finish + "&SiteType=RIV");
	
	//Optional<SimpleEntry<Header[], InputStream>> response = downloader.downloadHeadersAndBody(getEndpoint() + "HyDataSets.aspx?Station=" + stationId + "&SiteDesc=RIV");// + "&DataType="
		//+ dataType + "&StartDT=" + start + "&EndDT=" + finish + "&SiteType=RIV");
	if (res.isPresent()) {
	    is = res.get();
	}

	return is;

    }

}
