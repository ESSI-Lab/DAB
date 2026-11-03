package eu.essi_lab.accessor.datalakes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * Harvests the live Datalakes catalog and runs the same EXECUTION compliance test as the access augmenter on the
 * Figino probe water temperature (dataset 1399). Needs the WML validator and workflow engine on the test classpath,
 * e.g. via -Dmaven.test.additionalClasspath
 */
public class DatalakesAccessExternalTestIT {

    private static final int FIGINO = 1399;
    private static final int LEXPLORE_HEAT_FLUXES = 886;
    private static final int LAKE_THUN_CTD = 1336;

    @Test
    public void figinoTest() throws Exception {

	GSSource source = new GSSource();
	source.setEndpoint(DatalakesClient.DEFAULT_ENDPOINT);
	source.setUniqueIdentifier("che-datalakes");

	List<OriginalMetadata> records = harvest();

	GSResource figino = map(records, FIGINO, "y", source);
	GSResource figinoPH = map(records, FIGINO, "y1", source);
	GSResource heatFluxes = map(records, LEXPLORE_HEAT_FLUXES, "y", source);

	// series of the same dataset share the station, datasets with the same sensor type don't
	String figinoStation = figino.getExtensionHandler().getUniquePlatformIdentifier().get();
	Assert.assertEquals(figinoStation, figinoPH.getExtensionHandler().getUniquePlatformIdentifier().get());
	Assert.assertNotEquals(figinoStation, heatFluxes.getExtensionHandler().getUniquePlatformIdentifier().get());
	Assert.assertEquals("Figino Multiparameter Probe (1m)",
		figino.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getCitation().getTitle());

	assertExecution(figino);
    }

    @Test
    public void lakeThunGridTest() throws Exception {

	GSSource source = new GSSource();
	source.setEndpoint(DatalakesClient.DEFAULT_ENDPOINT);
	source.setUniqueIdentifier("che-datalakes");

	List<OriginalMetadata> records = harvest();

	// the y axis is the depth coordinate of the z profiles, y1..y9 are time series (e.g. mixed layer depth)
	Assert.assertFalse(find(records, LAKE_THUN_CTD, "y").isPresent());

	assertExecution(map(records, LAKE_THUN_CTD, "y1", source));
    }

    private void assertExecution(GSResource resource) throws Exception {

	String onlineId = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline()
		.getIdentifier();

	DatalakesDownloader downloader = createDownloader(resource, onlineId);
	Assert.assertTrue(downloader.canDownload());

	List<DataDescriptor> full = downloader.getRemoteDescriptors();
	List<DataDescriptor> preview = downloader.getPreviewRemoteDescriptors(full);

	DataComplianceTester tester = new DataComplianceTester(downloader);
	DataComplianceReport report = tester.test(DataComplianceTest.EXECUTION, preview.get(0), full.get(0),
		DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);

	Assert.assertEquals(report.toString(), DataComplianceTest.EXECUTION, report.getLastSucceededTest());
    }

    private List<OriginalMetadata> harvest() throws Exception {

	DatalakesConnector connector = new DatalakesConnector();
	connector.setSourceURL(DatalakesClient.DEFAULT_ENDPOINT);

	List<OriginalMetadata> records = new ArrayList<>();
	ListRecordsRequest request = new ListRecordsRequest();
	String token = null;
	do {
	    request.setResumptionToken(token);
	    ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	    records.addAll(response.getRecordsAsList());
	    token = response.getResumptionToken();
	} while (token != null);

	return records;
    }

    private GSResource map(List<OriginalMetadata> records, int datasetId, String axis, GSSource source) throws Exception {

	return new DatalakesMapper().map(find(records, datasetId, axis).get(), source);
    }

    private Optional<OriginalMetadata> find(List<OriginalMetadata> records, int datasetId, String axis) {

	return records.stream().filter(r -> {
	    JSONObject json = new JSONObject(r.getMetadata());
	    return json.getInt("datasetId") == datasetId && json.getString("axis").equals(axis);
	}).findFirst();
    }

    /**
     * Bypasses {@link DatalakesDownloader#setOnlineResource(GSResource, String)}, which reads the API key from the
     * harvesting configuration
     */
    private DatalakesDownloader createDownloader(GSResource resource, String onlineId) throws Exception {

	DatalakesDownloader downloader = new DatalakesDownloader();

	Field resourceField = DataDownloader.class.getDeclaredField("resource");
	resourceField.setAccessible(true);
	resourceField.set(downloader, resource);

	Field onlineField = DataDownloader.class.getDeclaredField("online");
	onlineField.setAccessible(true);
	onlineField.set(downloader,
		resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline(onlineId));

	Field clientField = DatalakesDownloader.class.getDeclaredField("client");
	clientField.setAccessible(true);
	clientField.set(downloader, new DatalakesClient(DatalakesClient.DEFAULT_ENDPOINT));

	return downloader;
    }
}
