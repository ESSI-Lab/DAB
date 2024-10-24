package eu.essi_lab.downloader;

import java.util.Date;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.request.executor.access.AccessExecutor;
import eu.essi_lab.validator.wof.WML_1_1Validator;

public class DownloaderTest {

    public static void main(String[] args) throws Exception {

	Dataset resource = new Dataset();
	String name = "method;1;parameter;INA:2;platform;sat2:6360;quality;;source;27";
	String linkage = "https://alerta.ina.gob.ar/wml";
	String protocol = "urn:ogc:serviceType:WaterOneFlow:1.1:HTTP";
	String functionCode = "download";
	GSSource source = new GSSource();
	source.setEndpoint(linkage);
	resource.setSource(source);
	resource.getHarmonizedMetadata().getCoreMetadata().addDistributionOnlineResource(name, linkage, protocol, functionCode);
	String onlineId = "xxx";
	resource.getHarmonizedMetadata().getCoreMetadata().getOnline().setIdentifier(onlineId);
	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataType(DataType.TIME_SERIES);
	DataComplianceReport report = new DataComplianceReport(onlineId, descriptor);
	report.setDescriptors(descriptor, descriptor);
	ValidationMessage valid = new ValidationMessage();
	valid.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	report.setExecutionResult(valid);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setTargetTest(DataComplianceTest.EXECUTION);
	report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_DATA_COMPLIANCE_1);
	handler.addReport(report);
	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);
	System.out.println(downloader.getClass().getSimpleName());
	// DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);
	AccessExecutor executor = new AccessExecutor();

	Date begin = new Date(new Date().getTime() - 1000 * 60 * 60 * 24l);
	Date end = new Date();
	Date lastDate = null;
	while (true) {
	    DataDescriptor targetDescriptor = new DataDescriptor();
	    targetDescriptor.setDataFormat(DataFormat.WATERML_1_1());
	    targetDescriptor.setTemporalDimension(begin, end);
	    ResultSet<DataObject> ret = executor.retrieve(resource, onlineId, targetDescriptor);
	    WML_1_1Validator v = new WML_1_1Validator();
	    DataDescriptor attributes = v.readDataAttributes(ret.getResultsList().get(0));
	    long b = attributes.getTemporalDimension().getContinueDimension().getLower().longValue();
	    Date actualBegin = new Date(b);
	    long e = attributes.getTemporalDimension().getContinueDimension().getLower().longValue();
	    Date actualEnd = new Date(e);
	    String newString = "";
	    if (lastDate == null || !lastDate.equals(actualEnd)) {
		newString = "NEW ";
		lastDate = actualEnd;
	    }
	    GSLoggerFactory.getLogger(DownloaderTest.class).info(newString + "DATA RETRIEVED: "
		    + ISO8601DateTimeUtils.getISO8601DateTime(actualBegin) + " - " + ISO8601DateTimeUtils.getISO8601DateTime(actualEnd));
	    Thread.sleep(10000);
	    begin = actualEnd;
	    end = new Date();
	}

    }
}
