package eu.essi_lab.accessor.dws.client;

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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.w3c.dom.Node;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class DWSStationList {

    private HashMap<String, DWSStation> stations = new HashMap<>();

    public HashMap<String, DWSStation> getStations() {
	return stations;
    }

    public DWSStationList(InputStream stream) throws Exception {
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	Node[] nodes = reader.evaluateNodes("//*:tr[../../*:table/@id='tableStations']");
	// from row 2, because 0 and 1 rows are headers
	for (int i = 2; i < nodes.length; i++) {
	    Node node = nodes[i];
	    String stationNumber = reader.evaluateString(node, "*:td[1]/*:a");
	    String stationName = reader.evaluateString(node, "*:td[2]");
	    String catchmentArea = reader.evaluateString(node, "*:td[3]");
	    String stationLatitude = reader.evaluateString(node, "*:td[4]");
	    String stationLongitude = reader.evaluateString(node, "*:td[5]");
	    String stationTemporalAvailability = reader.evaluateString(node, "*:td[6]");
	    DWSStation station = new DWSStation();
	    station.setStationCode(stationNumber);
	    station.setStationName(stationName);
	    try {
		station.setCatchmentAreaKm2(Integer.parseInt(catchmentArea));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to parse catchment area for station {}: {}", stationNumber,
			catchmentArea);
	    }
	    try {
		station.setLatitude(new BigDecimal(stationLatitude));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to parse station latitude for station {}: {}", stationNumber,
			stationLatitude);
	    }
	    try {
		station.setLongitude(new BigDecimal(stationLongitude));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to parse station longitude for station {}: {}", stationNumber,
			stationLongitude);
	    }
	    String[] split = stationTemporalAvailability.split(" to ");
	    station.setBeginDate(parseDate(split[0]));
	    station.setEndDate(parseDate(split[1]));
	    stations.put(stationNumber, station);
	}
    }

    private Date parseDate(String date) {
	return ISO8601DateTimeUtils.parseISO8601ToDate(date).get();
    }
}
