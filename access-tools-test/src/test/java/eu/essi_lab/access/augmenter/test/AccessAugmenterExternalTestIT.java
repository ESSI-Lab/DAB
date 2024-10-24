package eu.essi_lab.access.augmenter.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.accessor.wof.CUAHSIHISServerAccessor;
import eu.essi_lab.accessor.wof.CUAHSIHISServerConnector;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.workflow.builder.Workflow;

/**
 * @author Fabrizio
 */
public class AccessAugmenterExternalTestIT {

    private List<String> resourceNamesToBeTested = new ArrayList<>();

    public List<String> getResourceNamesToBeTested() {
	return resourceNamesToBeTested;
    }

    @Test
    public void cuahsiTest() throws GSException, ParserConfigurationException, JAXBException, SAXException, IOException {

	GSSource source = new GSSource();
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	source.setEndpoint("http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL");
	source.setUniqueIdentifier("lbr");
	source.setLabel("Little Bear River");

	CUAHSIHISServerAccessor accessor = new CUAHSIHISServerAccessor();
	accessor.getSetting().getGSSourceSetting().setSource(source);

	@SuppressWarnings("rawtypes")
	CUAHSIHISServerConnector wofConnector = new CUAHSIHISServerConnector();

	wofConnector.setFirstSiteOnly(true);

	ListRecordsRequest request = new ListRecordsRequest();

	String resumptionToken = null;
	do {

	    request.setResumptionToken(resumptionToken);

	    ListRecordsResponse<GSResource> response = accessor.listRecords(request);
	    Iterator<GSResource> records = response.getRecords();
	    resources: while (records.hasNext()) {

		GSResource res = records.next();

		if (!resourceNamesToBeTested.isEmpty()) {
		    Iterator<Online> onlines = res.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution()
			    .getDistributionOnlines();
		    while (onlines.hasNext()) {
			Online online = (Online) onlines.next();
			String name = online.getName();
			if (!resourceNamesToBeTested.contains(name)) {
			    continue resources;
			}
		    }
		}

		res.setPrivateId(UUID.randomUUID().toString());

		AccessAugmenter augmenter = createAugmenter();
		augmenter.augment(res);

		// System.err.println(res.asString(true));

		ReportsMetadataHandler handler = new ReportsMetadataHandler(res);
		List<DataComplianceReport> reports = handler.getReports();

		for (DataComplianceReport report : reports) {

		    DataDescriptor dataDescriptor = report.getPreviewDataDescriptor();
		    Assert.assertNotNull(dataDescriptor);

		    String onlineId = report.getOnlineId();
		    Assert.assertNotNull(onlineId);

		    DataComplianceLevel targetComplianceLevel = report.getTargetComplianceLevel();
		    Assert.assertTrue(targetComplianceLevel == DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);

		    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
		    switch (lastSucceededTest) {

		    case NONE:

			testNone(report, res);

			break;

		    case BASIC:

			testBasic(report, res, 0);

			break;

		    case DOWNLOAD:

			testBasic(report, res, 0);

			testDownload(report, res, 0);

			break;

		    case VALIDATION:

			testBasic(report, res, 0);

			testDownload(report, res, 0);

			testValidation(report, res, 0);

			break;

		    case EXECUTION:

			testBasic(report, res, 10);

			testDownload(report, res, 10);

			testValidation(report, res, 10);

			testExecution(report, res, 10);

			break;
		    }
		}
	    }

	    resumptionToken = response.getResumptionToken();

	} while (resumptionToken != null);
    }

    private void testNone(DataComplianceReport report, GSResource res) {

	List<Workflow> workflows = report.getWorkflows();
	Assert.assertTrue(workflows.isEmpty());

	Assert.assertTrue(report.getWorkflowsCount() == 0);
	Assert.assertTrue(report.getWorkflowsLength() == 0);

	ResourcePropertyHandler handler = res.getPropertyHandler();
	int accessQuality = handler.getAccessQuality().get();
	Assert.assertEquals(0, accessQuality);
    }

    private void testBasic(DataComplianceReport report, GSResource res, int val) {

	List<Workflow> workflows = report.getWorkflows();
	Assert.assertFalse(workflows.isEmpty());

	Assert.assertTrue(report.getWorkflowsCount() > 0);
	Assert.assertTrue(report.getWorkflowsLength() > 0);

	ResourcePropertyHandler handler = res.getPropertyHandler();
	int accessQuality = handler.getAccessQuality().get();
	Assert.assertEquals(val, accessQuality);
    }

    private void testDownload(DataComplianceReport report, GSResource res, int val) {

	Assert.assertTrue(report.getDownloadTime().isPresent());

	// the stream is not serialized
	Assert.assertNull(report.getDownloadedData());

	ResourcePropertyHandler handler = res.getPropertyHandler();
	int accessQuality = handler.getAccessQuality().get();
	Assert.assertEquals(val, accessQuality);
    }

    private void testValidation(DataComplianceReport report, GSResource res, int val) {

	Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	Assert.assertTrue(validationMessage.isPresent());

	String error = validationMessage.get().getError();
	Assert.assertNull(error);

	ResourcePropertyHandler handler = res.getPropertyHandler();
	int accessQuality = handler.getAccessQuality().get();
	Assert.assertEquals(val, accessQuality);
    }

    private void testExecution(DataComplianceReport report, GSResource res, int val) {

	Optional<ValidationMessage> executionResult = report.getExecutionResult();
	Assert.assertTrue(executionResult.isPresent());

	String error = executionResult.get().getError();
	Assert.assertNull(error);

	Optional<Long> executionTime = report.getExecutionTime();
	Assert.assertTrue(executionTime.isPresent());

	ResourcePropertyHandler handler = res.getPropertyHandler();
	int accessQuality = handler.getAccessQuality().get();
	Assert.assertEquals(val, accessQuality);
    }

    protected AccessAugmenter createAugmenter() {

	return new AccessAugmenter();
    }
}
