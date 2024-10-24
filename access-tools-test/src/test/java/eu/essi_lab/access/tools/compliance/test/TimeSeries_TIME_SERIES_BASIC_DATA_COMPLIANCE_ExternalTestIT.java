package eu.essi_lab.access.tools.compliance.test;

import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;

public class TimeSeries_TIME_SERIES_BASIC_DATA_COMPLIANCE_ExternalTestIT {

    @Test
    public void test() {

	{
	    String onlineResourceId = "id1";

	    Dataset dataset = new Dataset();
	    Online online = new Online();
	    online.setIdentifier(onlineResourceId);
	    online.setLinkage("http://icewater.usu.edu/littlebearriverwof/cuahsi_1_1.asmx?WSDL");
	    online.setName("platform;LBR:USU-LBR-Mendon;parameter;LBR:USU3");
	    online.setProtocol(NetProtocols.CUAHSI_WATER_ONE_FLOW_1_1.getCommonURN());
	    dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().addDistributionOnline(online);

	    // WorkflowBuilder.enableLogs(true);
	    // WorkflowBuilder.enableDeepLogs(true);

	    DataDownloader downloader = DataDownloaderFactory.getDataDownloader(dataset, onlineResourceId);

	    DataComplianceTester tester = new DataComplianceTester(downloader);

	    try {

		DataDescriptor descriptor = downloader.getPreviewRemoteDescriptors().get(0);

		DataComplianceReport report = tester.test(//
			DataComplianceTest.EXECUTION, //
			descriptor, //
			descriptor,//
			DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);

		// -------------------------------
		//
		// BASIC
		//
		Assert.assertTrue(report.getWorkflowsCount()>0);
		Assert.assertEquals(2, report.getWorkflowsLength());

		// -------------------------------
		//
		// DOWNLOAD
		//
		Assert.assertTrue(report.isDownloadable().get());

		// -------------------------------
		//
		// VALIDATION
		//
		Assert.assertTrue(report.getValidationMessage().get().getResult() == ValidationResult.VALIDATION_SUCCESSFUL);

		// -------------------------------
		//
		// EXECUTION
		//
		Optional<ValidationMessage> executionResult = report
			.getExecutionResult();

		Assert.assertTrue(executionResult.isPresent());
		Assert.assertTrue(executionResult.get().getResult() == ValidationResult.VALIDATION_SUCCESSFUL);

		GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    } catch (GSException ex) {

		if (!ex.getErrorInfoList().isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).error(ex.getErrorInfoList().get(0).getErrorDescription());
		}

		fail("Exception thrown");
	    }
	}
    }
}
