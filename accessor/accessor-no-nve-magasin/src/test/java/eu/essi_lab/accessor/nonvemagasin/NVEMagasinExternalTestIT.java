package eu.essi_lab.accessor.nonvemagasin;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author boldrini
 */
public class NVEMagasinExternalTestIT {

    private static final String FILLING_RATIO = "NO|0|fyllingsgrad";

    @Test
    public void testSeries() throws GSException {

	NVEMagasinClient client = new NVEMagasinClient(System.getProperty("nveMagasinEndpoint", NVEMagasinClient.DEFAULT_ENDPOINT));

	List<NVEMagasinEntity> series = client.retrieveSeries(Arrays.asList(NVEMagasinProperty.values()), Collections.emptyList());
	System.out.println("Series: " + series.size());
	// 9 areas with values (the watercourse region 4 has none) and 3 properties
	assertTrue(series.size() >= 27);

	NVEMagasinEntity ratio = series.stream().filter(s -> s.getSeriesId().equals(FILLING_RATIO)).findFirst().get();
	assertEquals("Norge", ratio.getString(NVEMagasinClient.AREA_NAME));
	assertEquals("1995-01-08T00:00:00Z", ratio.getString(NVEMagasinClient.BEGIN));
	System.out.println(ratio);

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(ratio.getString(NVEMagasinClient.END)).get();
	// weekly series, published with some days of delay
	assertTrue(System.currentTimeMillis() - end.getTime() < TimeUnit.DAYS.toMillis(21));

	assertEquals(end, client.retrieveLatestObservation(ratio).get());
    }

    @Test
    public void testObservations() throws GSException {

	NVEMagasinEntity ratio = NVEMagasinClient.parseSeriesId(FILLING_RATIO);

	Date begin = ISO8601DateTimeUtils.parseISO8601ToDate("2020-01-01T00:00:00Z").get();
	Date end = ISO8601DateTimeUtils.parseISO8601ToDate("2020-12-31T00:00:00Z").get();

	TreeMap<Date, BigDecimal> values = new NVEMagasinClient().retrieveObservations(ratio, begin, end);
	assertEquals(52, values.size());
	assertEquals("2020-01-05T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	assertTrue(values.values().stream().allMatch(v -> v.doubleValue() > 0 && v.doubleValue() <= 1));
    }

    @Test
    public void testConnectorAndDownloader() throws Exception {

	NVEMagasinConnector connector = new NVEMagasinConnector();
	connector.setSourceURL(NVEMagasinClient.DEFAULT_ENDPOINT);
	connector.getSetting().setAreaTypes("NO,EL");
	connector.getSetting().setMagasinProperties("fyllingsgrad,fylling_TWh");
	connector.getSetting().setMaxRecords(5);

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(new ListRecordsRequest());
	List<OriginalMetadata> records = response.getRecordsAsList();
	assertEquals(5, records.size());
	assertNull(response.getResumptionToken());
	assertEquals(FILLING_RATIO, new NVEMagasinEntity(records.get(0).getMetadata()).getSeriesId());

	GSSource source = new GSSource();
	source.setEndpoint(NVEMagasinClient.DEFAULT_ENDPOINT);
	assertTrue(connector.supports(source));

	GSResource resource = new NVEMagasinMapper().map(records.get(0), source);
	System.out.println(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	NVEMagasinDownloader downloader = new NVEMagasinDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	long end = descriptor.getTemporalDimension().getContinueDimension().getUpper().longValue();
	descriptor.setTemporalDimension(new Date(end - TimeUnit.DAYS.toMillis(365)), new Date(end));

	File file = downloader.download(descriptor);
	assertNotNull(file);

	String wml = Files.readString(file.toPath());
	assertTrue(wml.contains(":value"));
	System.out.println("WML size: " + wml.length() + " values: " + (wml.split(":value ").length - 1));
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }

    @Test
    public void testFullHarvest() throws GSException {

	NVEMagasinConnector connector = new NVEMagasinConnector();
	connector.setSourceURL(NVEMagasinClient.DEFAULT_ENDPOINT);

	ListRecordsRequest request = new ListRecordsRequest();
	int count = 0;
	int pages = 0;
	do {
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    count += response.getRecordsAsList().size();
	    request.setResumptionToken(response.getResumptionToken());
	    pages++;
	} while (request.getResumptionToken() != null);

	System.out.println("Harvested " + count + " records in " + pages + " pages");
	assertFalse(count < 27);
    }
}
