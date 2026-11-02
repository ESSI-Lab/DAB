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

import java.util.Arrays;

import org.junit.Test;

import eu.essi_lab.model.GSSource;

/**
 * @author boldrini
 */
public class KISTERSConnectorTest {

    @Test
    public void parameterLongNameFromGroupTest() {

	KISTERSEntity station = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_STATION_RESPONSE);
	KISTERSEntity series = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_GROUP_RESPONSE);

	// the joined long names of the station are replaced by the long name of the station parameter
	series.getObject().put(KISTERSClient.PARAM_TYPE_LONG_NAME, station.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME));

	KISTERSConnector.resolveParameterLongName(station, series);

	assertEquals("Water Level", series.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME));
    }

    @Test
    public void parameterLongNameFromStationTest() {

	// legacy per station mode (e.g. SEPA): no station parameter long name in the time series
	KISTERSEntity station = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_STATION_RESPONSE);
	KISTERSEntity series = KISTERSClientTest.firstEntity(KISTERSClientTest.SPW_GROUP_RESPONSE);
	series.getObject().remove(KISTERSClient.STATION_PARAM_LONG_NAME);
	series.getObject().put(KISTERSClient.PARAM_TYPE_ID, "558");
	series.getObject().put(KISTERSClient.PARAM_TYPE_LONG_NAME, station.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME));

	KISTERSConnector.resolveParameterLongName(station, series);

	assertEquals("River Discharge", series.getObject().getString(KISTERSClient.PARAM_TYPE_LONG_NAME));
    }

    @Test
    public void settingTest() {

	KISTERSConnectorSetting setting = new KISTERSConnectorSetting();

	// defaults: all the time series of each station
	assertTrue(setting.getTimeSeriesGroupIds().isEmpty());
	assertFalse(setting.getTimeSeriesNameRegex().isPresent());

	setting.setTimeSeriesGroupIds(" 7255523, 7255151 ,,");
	assertEquals(Arrays.asList("7255523", "7255151"), setting.getTimeSeriesGroupIds());

	setting.setTimeSeriesNameRegex("05-.*");
	assertEquals("05-.*", setting.getTimeSeriesNameRegex().get());

	// the options are kept by the JSON serialization
	KISTERSConnectorSetting copy = new KISTERSConnectorSetting(setting.getObject().toString());
	assertEquals(Arrays.asList("7255523", "7255151"), copy.getTimeSeriesGroupIds());
    }

    @Test
    public void supportsTest() {

	KISTERSConnector connector = new KISTERSConnector();

	for (String endpoint : Arrays.asList(//
		"https://portal.grdc.bafg.de/KiWIS/KiWIS?", //
		"https://timeseries.sepa.org.uk/KiWIS/KiWIS?datasource=0", //
		"https://download.waterinfo.be/tsmdownload/KiWIS/KiWIS?datasource=1", //
		"https://hicws.vlaanderen.be/KiWIS/KiWIS", //
		"https://hydrometrie.wallonie.be/services/KiWIS/KiWIS")) {

	    GSSource source = new GSSource();
	    source.setEndpoint(endpoint);
	    assertTrue(connector.supports(source));
	}
    }
}
