package eu.essi_lab.accessor.debrandenburgpegel;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class BrandenburgPegelExternalTestIT {

    private static final String WATER_LEVEL = "0491200|W|1";
    private static final String DISCHARGE = "0491200|Q|1";

    @Test
    public void testSeries() throws GSException {

	BrandenburgPegelClient client = new BrandenburgPegelClient(
		System.getProperty("brandenburgPegelEndpoint", BrandenburgPegelClient.DEFAULT_ENDPOINT));

	List<BrandenburgPegelEntity> series = client.retrieveSeries(Arrays.asList(BrandenburgPegelParameter.values()));
	GSLoggerFactory.getLogger(getClass()).info("Series: {}", series.size());
	assertTrue(series.size() > 250);
	assertTrue(series.stream().anyMatch(s -> s.getParameter().get() == BrandenburgPegelParameter.Q));

	BrandenburgPegelEntity waterLevel = series.stream().filter(s -> s.getOnlineName().equals(WATER_LEVEL)).findFirst().get();
	assertEquals("Prenzlau, Wehr UP", waterLevel.getStationName());
	assertEquals(53.31, waterLevel.getObject().getDouble(BrandenburgPegelClient.LAT), 0.02);

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(waterLevel.getString(BrandenburgPegelClient.END)).get();
	// real time series
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(7));
    }

    @Test
    public void testRecentWaterLevel() throws GSException {

	BrandenburgPegelClient client = new BrandenburgPegelClient();
	BrandenburgPegelEntity waterLevel = BrandenburgPegelClient.parseSeriesId(WATER_LEVEL);

	Date latest = client.retrieveLatestObservation(waterLevel).get();
	assertTrue(System.currentTimeMillis() - latest.getTime() < TimeUnit.DAYS.toMillis(1));

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(2));

	// from the CSV of the last week
	TreeMap<Date, BigDecimal> values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 100);
	assertFalse(values.firstKey().before(begin));

	// from the static CSV of the last 30 days, completed with the CSV of the last week
	begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(20));
	values = client.retrieveObservations(waterLevel, begin, end);
	assertTrue(values.size() > 1500);
	assertTrue(end.getTime() - values.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));
    }

    @Test
    public void testPageObservations() throws GSException {

	BrandenburgPegelClient client = new BrandenburgPegelClient();

	Date end = new Date();
	Date begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(100));

	// from the gauge page (last year)
	TreeMap<Date, BigDecimal> waterLevels = client.retrieveObservations(BrandenburgPegelClient.parseSeriesId(WATER_LEVEL), begin,
		end);
	GSLoggerFactory.getLogger(getClass()).info("Water levels: {} {} {}", waterLevels.size(), waterLevels.firstKey(),
		waterLevels.lastKey());
	assertTrue(waterLevels.size() > 8000);

	TreeMap<Date, BigDecimal> discharges = client.retrieveObservations(BrandenburgPegelClient.parseSeriesId(DISCHARGE), begin,
		end);
	GSLoggerFactory.getLogger(getClass()).info("Discharges: {} {} {}", discharges.size(), discharges.firstKey(),
		discharges.lastKey());
	assertTrue(discharges.size() > 8000);
	assertTrue(discharges.values().stream().allMatch(v -> v.doubleValue() < 100));
	assertTrue(end.getTime() - discharges.lastKey().getTime() < TimeUnit.DAYS.toMillis(1));
    }

    @Test
    public void testConnector() throws GSException, IOException {

	BrandenburgPegelConnector connector = new BrandenburgPegelConnector();
	connector.setSourceURL(BrandenburgPegelClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("0491200,0491101");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	List<String> ids = records.stream().map(r -> new BrandenburgPegelEntity(r.getMetadata())).//
		map(BrandenburgPegelEntity::getSeriesId).//
		collect(Collectors.toList());

	GSLoggerFactory.getLogger(getClass()).info("Records: {}", ids);
	assertEquals(Arrays.asList("0491101|W", "0491200|Q", "0491200|W"), ids);

	GSSource source = new GSSource();
	source.setEndpoint(BrandenburgPegelClient.DEFAULT_ENDPOINT);
	GSResource resource = new BrandenburgPegelMapper().map(records.get(1), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	BrandenburgPegelDownloader downloader = new BrandenburgPegelDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	assertTrue(System.currentTimeMillis() - end < TimeUnit.DAYS.toMillis(1));

	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(2)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	GSLoggerFactory.getLogger(getClass()).info("WML size: {} values: {}", wml.length(), wml.split(":value ").length - 1);
	assertTrue(wml.contains(":value"));
    }
}
