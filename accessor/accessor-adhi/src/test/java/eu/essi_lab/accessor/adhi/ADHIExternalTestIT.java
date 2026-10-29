package eu.essi_lab.accessor.adhi;

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

import org.json.JSONObject;
import org.junit.Test;

import eu.essi_lab.accessor.adhi.ADHIClient.DatasetInfo;
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
public class ADHIExternalTestIT {

    @Test
    public void testDatasetAndStations() throws GSException {

	ADHIClient client = new ADHIClient(System.getProperty("adhiEndpoint", ADHIClient.DEFAULT_ENDPOINT));

	DatasetInfo info = client.retrieveDatasetInfo();
	assertNotNull(info.getVersion());
	assertTrue(info.getLicense().contains("CC BY 4.0"));
	GSLoggerFactory.getLogger(getClass()).info("Dataset version {}, files {}", info.getVersion(), info.getFileNames());

	for (ADHIVariable variable : ADHIVariable.values()) {
	    assertTrue(info.getFile(variable.getFileName()).get().getMd5() != null);
	}

	List<JSONObject> stations = client.retrieveStations(info);
	assertTrue(stations.size() > 1400);
	assertEquals("ADHI_1", stations.get(0).getString(ADHIClient.STATION_ID));
    }

    @Test
    public void testObservations() throws GSException {

	ADHIClient client = new ADHIClient();
	DatasetInfo info = client.retrieveDatasetInfo();

	TreeMap<Date, BigDecimal> values = client.retrieveObservations(info, "ADHI_1", ADHIVariable.MONTHLY_MEAN, null, null);
	assertEquals("1963-05-01T00:00:00Z", ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	assertEquals(new BigDecimal("10.31"), values.firstEntry().getValue());

	values = client.retrieveObservations(info, "ADHI_100", ADHIVariable.ANNUAL_MAX, null, null);
	assertEquals(new BigDecimal("3370"), values.get(ISO8601DateTimeUtils.parseISO8601ToDate("1953-01-01T00:00:00Z").get()));
    }

    @Test
    public void testConnectorAndDownloader() throws Exception {

	ADHIConnector connector = new ADHIConnector();
	connector.setSourceURL(ADHIClient.DEFAULT_ENDPOINT);
	connector.getSetting().setStations("ADHI_1,ADHI_100");
	connector.getSetting().setVariables(ADHIConnectorSetting.DEFAULT_VARIABLES + ",ANNUAL_Q50");
	connector.getSetting().setMaxRecords(20);

	List<OriginalMetadata> records = connector.listRecords(new ListRecordsRequest()).getRecordsAsList();
	List<String> ids = records.stream().map(r -> new ADHIEntity(r.getMetadata()).getSeriesId()).collect(Collectors.toList());
	GSLoggerFactory.getLogger(getClass()).info("Harvested {} records: {}", records.size(), ids);

	assertEquals(14, records.size());
	assertTrue(ids.containsAll(Arrays.asList("ADHI_1:MONTHLY_MEAN", "ADHI_100:ANNUAL_Q50")));

	OriginalMetadata record = records.get(0);
	ADHIEntity entity = new ADHIEntity(record.getMetadata());
	assertEquals("ADHI_1:MONTHLY_MEAN", entity.getSeriesId());
	assertEquals("1963-05-01T00:00:00Z", entity.getString(ADHIClient.BEGIN));
	assertEquals("1980-12-01T00:00:00Z", entity.getString(ADHIClient.END));

	GSSource source = new GSSource();
	source.setEndpoint(ADHIClient.DEFAULT_ENDPOINT);
	GSResource resource = new ADHIMapper().map(record, source);
	GSLoggerFactory.getLogger(getClass()).info("Title: {}", resource.getHarmonizedMetadata().getCoreMetadata().getTitle());

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getOnline();
	// during harvesting the identifier is assigned by the identifier decorator
	online.setIdentifier();

	ADHIDownloader downloader = new ADHIDownloader();
	downloader.setOnlineResource(resource, online.getIdentifier());
	assertTrue(downloader.canDownload());
	assertTrue(downloader.canConnect());

	DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	assertNotNull(descriptor.getTemporalDimension());

	File file = downloader.download(descriptor);
	assertNotNull(file);
	String wml = Files.readString(file.toPath());
	int count = wml.split(":value ").length - 1;
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), count);
	assertEquals(entity.getObject().getInt(ADHIClient.VALUES), count);

	// one year
	descriptor.setTemporalDimension(ISO8601DateTimeUtils.parseISO8601ToDate("1970-01-01T00:00:00Z").get(),
		ISO8601DateTimeUtils.parseISO8601ToDate("1970-12-31T00:00:00Z").get());
	wml = Files.readString(downloader.download(descriptor).toPath());
	assertFalse(wml.isEmpty());
	GSLoggerFactory.getLogger(getClass()).info("Downloaded {} bytes, {} values", wml.length(), wml.split(":value ").length - 1);
	System.out.println(wml.substring(wml.indexOf(":value") - 4, Math.min(wml.length(), wml.indexOf(":value") + 300)));
    }
}
