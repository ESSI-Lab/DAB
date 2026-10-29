package eu.essi_lab.accessor.ittrentinofloods;

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

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class TrentinoFloodsExternalTestIT {

    @Test
    public void testSeries() throws GSException {

	TrentinoFloodsClient client = new TrentinoFloodsClient(
		System.getProperty("trentinoFloodsEndpoint", TrentinoFloodsClient.DEFAULT_ENDPOINT));

	List<TrentinoFloodsEntity> series = client.retrieveSeries();
	assertEquals(20, series.size());
	assertEquals(10, series.stream().filter(s -> s.getProperty().get() == TrentinoFloodsProperty.LEVEL).count());
	assertEquals(10, series.stream().filter(s -> s.getProperty().get() == TrentinoFloodsProperty.DISCHARGE).count());

	TrentinoFloodsEntity trento = series.get(0);
	assertEquals("1|1", trento.getSeriesId());
	assertEquals("Trento", trento.getStationName());
    }

    @Test
    public void testObservations() throws GSException {

	TrentinoFloodsClient client = new TrentinoFloodsClient();
	TrentinoFloodsEntity level = TrentinoFloodsClient.parseSeriesId("1|1");

	SimpleEntry<Date, Date> extent = client.retrieveTemporalExtent(level).get();
	GSLoggerFactory.getLogger(getClass()).info("Current extent: {} - {}", extent.getKey(), extent.getValue());

	// rolling window of 7 days
	long width = extent.getValue().getTime() - extent.getKey().getTime();
	assertTrue(width <= TrentinoFloodsClient.WINDOW + TimeUnit.HOURS.toMillis(2));

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(level, extent.getKey(), extent.getValue());
	// hourly values
	assertTrue(values.size() > 100);

	Date middle = new Date(extent.getKey().getTime() + TimeUnit.DAYS.toMillis(1));
	TreeMap<Date, BigDecimal> subset = client.retrieveObservations(level, extent.getKey(), middle);
	assertTrue(subset.size() >= 24 && subset.size() <= 25);
	assertFalse(subset.lastKey().after(middle));
    }

    @Test
    public void testConnector() throws GSException {

	TrentinoFloodsConnector connector = new TrentinoFloodsConnector();
	connector.setSourceURL(TrentinoFloodsClient.DEFAULT_ENDPOINT);
	connector.getSetting().setSensors("1,11,5");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	assertEquals(3, records.size());

	List<String> ids = records.stream().map(r -> new TrentinoFloodsEntity(r.getMetadata())).//
		peek(e -> assertTrue(e.getObject().has(TrentinoFloodsClient.BEGIN))).//
		peek(e -> assertTrue(e.getObject().has(TrentinoFloodsClient.END))).//
		map(TrentinoFloodsEntity::getSeriesId).//
		collect(Collectors.toList());

	assertTrue(ids.contains("1|1"));
	assertTrue(ids.contains("2|11"));
	assertTrue(ids.contains("1|5"));
    }

    @Test
    public void testDownload() throws Exception {

	TrentinoFloodsConnector connector = new TrentinoFloodsConnector();
	connector.setSourceURL(TrentinoFloodsClient.DEFAULT_ENDPOINT);
	connector.getSetting().setSensors("11");

	OriginalMetadata record = connector.listRecords(new ListRecordsRequest()).getRecordsAsList().get(0);

	GSSource source = new GSSource();
	source.setEndpoint(TrentinoFloodsClient.DEFAULT_ENDPOINT);
	GSResource resource = new TrentinoFloodsMapper().map(record, source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();

	// the identifier is assigned by the broker after the mapping
	online.setIdentifier("trentino-floods-test");

	TrentinoFloodsDownloader downloader = new TrentinoFloodsDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	File file = downloader.download(descriptor);
	String wml = new String(Files.readAllBytes(file.toPath()));
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes", wml.length());
	assertTrue(wml.contains("<value"));
    }
}
