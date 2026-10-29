package eu.essi_lab.accessor.rarcticnet;

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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class RArcticNetExternalTestIT {

    private static final String KOLYMA_MONTHLY = "5855|MONTHLY";
    private static final String KOLYMA_DAILY = "5855|DAILY";

    @Test
    public void testSeries() throws Exception {

	RArcticNetClient client = new RArcticNetClient(System.getProperty("rArcticNetEndpoint", RArcticNetClient.DEFAULT_ENDPOINT));

	long start = System.currentTimeMillis();
	List<RArcticNetEntity> series = client.retrieveSeries(Arrays.asList(RArcticNetDataset.values()));
	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} series in {} ms", series.size(), System.currentTimeMillis() - start);

	long monthly = series.stream().filter(s -> s.getDataset().get() == RArcticNetDataset.MONTHLY).count();
	long daily = series.stream().filter(s -> s.getDataset().get() == RArcticNetDataset.DAILY).count();
	GSLoggerFactory.getLogger(getClass()).info("Monthly series: {}, daily series: {}", monthly, daily);

	assertTrue(monthly > 5000);
	assertTrue(daily > 100);

	RArcticNetEntity kolyma = series.stream().filter(s -> s.getSeriesId().equals(KOLYMA_MONTHLY)).findFirst().get();
	assertEquals("Kolyma At Orotuk", kolyma.getName());
	assertEquals("1957-01-01T00:00:00Z", kolyma.getString(RArcticNetClient.BEGIN));
	assertTrue(kolyma.getString(RArcticNetClient.END).startsWith("1997-"));

	RArcticNetEntity kolymaDaily = series.stream().filter(s -> s.getSeriesId().equals(KOLYMA_DAILY)).findFirst().get();
	assertEquals("1968-01-01T00:00:00Z", kolymaDaily.getString(RArcticNetClient.BEGIN));
	GSLoggerFactory.getLogger(getClass()).info("Daily series {}: {} - {}", KOLYMA_DAILY, kolymaDaily.getString(RArcticNetClient.BEGIN),
		kolymaDaily.getString(RArcticNetClient.END));

	// the second time the cached files are used
	start = System.currentTimeMillis();
	assertEquals(series.size(), client.retrieveSeries(Arrays.asList(RArcticNetDataset.values())).size());
	GSLoggerFactory.getLogger(getClass()).info("Retrieved again in {} ms", System.currentTimeMillis() - start);
    }

    @Test
    public void testObservations() throws Exception {

	RArcticNetClient client = new RArcticNetClient();

	TreeMap<Date, BigDecimal> monthly = client.retrieveObservations(RArcticNetClient.parseSeriesId(KOLYMA_MONTHLY), null, null);
	assertEquals(date("1957-01-01T00:00:00Z"), monthly.firstKey());
	assertEquals(new BigDecimal("3.810"), monthly.firstEntry().getValue());

	// the code of the daily series is read from the daily site attributes
	Date begin = date("1968-02-01T00:00:00Z");
	Date end = date("1968-02-29T00:00:00Z");
	TreeMap<Date, BigDecimal> daily = client.retrieveObservations(RArcticNetClient.parseSeriesId(KOLYMA_DAILY), begin, end);
	assertEquals(29, daily.size());
	assertEquals(new BigDecimal("3.07"), daily.firstEntry().getValue());
    }

    @Test
    public void testConnectorAndDownloader() throws Exception {

	RArcticNetConnector connector = new RArcticNetConnector();
	connector.setSourceURL(RArcticNetClient.DEFAULT_ENDPOINT);
	connector.getSetting().setSites("1,5855");
	connector.getSetting().setMaxRecords(10);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();

	List<String> ids = records.stream().map(r -> new RArcticNetEntity(r.getMetadata()).getSeriesId()).collect(Collectors.toList());
	assertEquals(Arrays.asList("1|MONTHLY", KOLYMA_MONTHLY, KOLYMA_DAILY), ids);

	GSSource source = new GSSource();
	source.setEndpoint(RArcticNetClient.DEFAULT_ENDPOINT);

	GSResource resource = new RArcticNetMapper().map(records.get(2), source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	online.setIdentifier("online");

	RArcticNetDownloader downloader = new RArcticNetDownloader();
	downloader.setOnlineResource(resource, "online");
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	List<DataDescriptor> descriptors = downloader.getRemoteDescriptors();
	assertFalse(descriptors.isEmpty());

	DataDescriptor descriptor = descriptors.get(0);
	descriptor.setTemporalDimension(date("1968-01-01T00:00:00Z"), date("1968-12-31T00:00:00Z"));

	File file = downloader.download(descriptor);
	assertNotNull(file);
	String wml = new String(Files.readAllBytes(file.toPath()));
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split("dateTimeUTC=").length - 1);
	assertTrue(wml.contains("1968-01-01T00:00:00"));
    }

    /**
     * @param text
     * @return
     */
    private static Date date(String text) {

	return ISO8601DateTimeUtils.parseISO8601ToDate(text).get();
    }
}
