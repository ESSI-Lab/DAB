package eu.essi_lab.accessor.esaca;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class ACAExternalTestIT {

    private static final String DISCHARGE = "AFORAMENT-EST|CALC001658";
    private static final String RESERVOIR_LEVEL = "EMBASSAMENT-EST|080581-002-ANA006";

    @Test
    public void testSeries() throws GSException {

	ACAClient client = new ACAClient(System.getProperty("acaEndpoint", ACAClient.DEFAULT_ENDPOINT));

	List<ACAEntity> series = client.retrieveSeries(Arrays.asList("aforament", "embassament"));
	assertTrue(series.size() > 150);

	Map<String, Long> properties = series.stream()
		.collect(Collectors.groupingBy(s -> s.getProperty().map(ACAProperty::name).orElse(s.getDescription()), Collectors.counting()));
	GSLoggerFactory.getLogger(getClass()).info("Series per property: {}", properties);

	assertTrue(properties.containsKey(ACAProperty.RIVER_LEVEL.name()));
	assertTrue(properties.containsKey(ACAProperty.RIVER_DISCHARGE.name()));
	assertTrue(properties.containsKey(ACAProperty.RESERVOIR_VOLUME.name()));
	assertTrue(properties.containsKey(ACAProperty.RESERVOIR_PERCENTAGE.name()));

	ACAEntity discharge = series.stream().filter(s -> s.getSeriesId().equals(DISCHARGE)).findFirst().get();
	assertEquals("Arenys de Munt (riera d'Arenys)", discharge.getComponentName());

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(discharge.getString(ACAClient.END)).get();
	// real time series
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void testLatestObservation() throws GSException {

	Date latest = new ACAClient().retrieveLatestObservation(ACAClient.parseSeriesId(RESERVOIR_LEVEL)).get();

	assertTrue(System.currentTimeMillis() - latest.getTime() < TimeUnit.DAYS.toMillis(2));
    }

    @Test
    public void testObservations() throws GSException {

	ACAClient client = new ACAClient();
	ACAEntity reservoirLevel = ACAClient.parseSeriesId(RESERVOIR_LEVEL);

	// one day: about 288 values (5 minutes), read with 2 requests
	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(1));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(reservoirLevel, begin, end);
	GSLoggerFactory.getLogger(getClass()).info("Values of the last day: {}", values.size());
	assertTrue(values.size() > ACAClient.MAX_LIMIT);
	assertFalse(values.firstKey().before(begin));

	// the begin of the rolling window
	begin = new Date(end.getTime() - ACAClient.ROLLING_WINDOW);
	Date windowEnd = new Date(begin.getTime() + TimeUnit.DAYS.toMillis(1));

	values = client.retrieveObservations(reservoirLevel, begin, windowEnd);
	GSLoggerFactory.getLogger(getClass()).info("Values of the first day of the rolling window: {} [{}/{}]", values.size(),
		values.isEmpty() ? null : values.firstKey(), values.isEmpty() ? null : values.lastKey());
	assertTrue(values.size() > ACAClient.MAX_LIMIT);
	assertTrue(values.firstKey().getTime() - begin.getTime() < TimeUnit.HOURS.toMillis(1));
    }

    @Test
    public void testConnector() throws GSException {

	ACAConnector connector = new ACAConnector();
	connector.setSourceURL(ACAClient.DEFAULT_ENDPOINT);
	connector.getSetting().setComponentTypes("aforament,embassament");
	connector.getSetting().setComponents("080060-001,080581-002");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertFalse(records.isEmpty());

	List<String> ids = records.stream().map(r -> new ACAEntity(r.getMetadata())).//
		map(ACAEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Harvested series: {}", ids);

	assertTrue(ids.contains(DISCHARGE));
	assertTrue(ids.contains(RESERVOIR_LEVEL));

	GSSource source = new GSSource();
	source.setEndpoint(ACAClient.DEFAULT_ENDPOINT);

	for (OriginalMetadata record : records) {
	    GSResource resource = new ACAMapper().map(record, source);
	    GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
	}
    }
}
