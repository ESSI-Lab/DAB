package eu.essi_lab.accessor.kisters;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.junit.Test;

/**
 * @author boldrini
 */
public class KISTERSClientTest {

    /**
     * A time series of the "SPW-WS-HauteurHR" group of Wallonia, as returned by getTimeseriesList with timeseriesgroup_id
     */
    static final String SPW_GROUP_RESPONSE = "[[\"ts_id\",\"station_id\",\"site_id\",\"ts_unitsymbol\",\"ts_name\",\"ts_unitname\",\"ts_spacing\","
	    + "\"parametertype_id\",\"parametertype_name\",\"station_no\",\"station_name\",\"station_latitude\",\"station_longitude\",\"site_name\","
	    + "\"stationparameter_longname\",\"from\",\"to\",\"GRDCCOUNTRY\"],"
	    + "[\"356958010\",\"3640093\",\"13910\",\"m\",\"05-Hauteur.Complet\",\"meter\",\"\",\"11547\",\"H\",\"L8710\",\"Seilles\","
	    + "\"50.5041390070031\",\"5.08946204910302\",\"Direction des Cours d'Eau Non Navigables\",\"Water Level\","
	    + "\"2021-04-13T17:20:00.000+02:00\",\"2026-10-27T15:00:00.000+01:00\",\"\"]]";

    /**
     * The station of the series above, as returned by getStationList
     */
    static final String SPW_STATION_RESPONSE = "[[\"station_name\",\"station_id\",\"station_latitude\",\"station_longitude\",\"site_id\","
	    + "\"site_name\",\"site_longname\",\"parametertype_id\",\"parametertype_name\",\"parametertype_longname\",\"river_name\",\"object_type\"],"
	    + "[\"Seilles\",\"3640093\",\"50.5041390070031\",\"5.08946204910302\",\"13910\",\"Direction des Cours d'Eau Non Navigables\",\"\","
	    + "\"11547;519;558\",\"H;T;Q\",\"Water Level;Temperature;River Discharge\",\"Velaine\",\"General\"]]";

    /**
     * @param response
     * @return the first entity of a KiWIS table response
     */
    static KISTERSEntity firstEntity(String response) {

	JSONArray array = new JSONArray(response);

	return new KISTERSEntity(array.getJSONArray(0), array.getJSONArray(1).toList().stream().map(Object::toString).toList());
    }

    @Test
    public void normalizeEndpointTest() {

	assertEquals("https://hicws.vlaanderen.be/KiWIS/KiWIS?",
		KISTERSClient.normalizeEndpoint("https://hicws.vlaanderen.be/KiWIS/KiWIS"));

	assertEquals("https://download.waterinfo.be/tsmdownload/KiWIS/KiWIS?datasource=1&",
		KISTERSClient.normalizeEndpoint("https://download.waterinfo.be/tsmdownload/KiWIS/KiWIS?datasource=1"));

	// already ready endpoints are unchanged
	assertEquals("https://portal.grdc.bafg.de/KiWIS/KiWIS?", KISTERSClient.normalizeEndpoint("https://portal.grdc.bafg.de/KiWIS/KiWIS?"));
	assertEquals("https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0&",
		KISTERSClient.normalizeEndpoint("https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0&"));
    }

    @Test
    public void errorResponseTest() {

	// returned with HTTP status 200 by HIC Flanders when a return field is not supported
	assertTrue(KISTERSClient.isError("{\"type\": \"error\", \"code\": \"InvalidParameterValue\", "
		+ "\"message\": \"Parameter 'returnfields' contains at least one invalid field.\"}"));

	assertTrue(KISTERSClient.isError("<html><body>Forbidden</body></html>"));

	assertFalse(KISTERSClient.isError(SPW_GROUP_RESPONSE));
    }

    @Test
    public void entityTest() {

	KISTERSEntity entity = firstEntity(SPW_GROUP_RESPONSE);

	assertEquals("356958010", entity.getObject().getString(KISTERSClient.TS_ID));
	assertEquals("Water Level", entity.getObject().getString(KISTERSClient.STATION_PARAM_LONG_NAME));
    }
}
