package eu.essi_lab.access.compliance.wrapper;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.model.resource.data.dimension.FiniteDimension;

/**
 * @author Fabrizio
 */
public class ReportsMetadataHandlerTest {

    // @Test
    public void JSONTest() throws UnsupportedEncodingException, JAXBException {

	DataDescriptor descriptor = createGridDescriptor();
	DataDescriptorWrapper wrapper = new DataDescriptorWrapper(descriptor);
	String string = wrapper.asString(true);

	JSONObject jsonObject = XML.toJSONObject(string);
	String jsonPrettyPrintString = jsonObject.toString(4);

	System.out.println(jsonPrettyPrintString);

	String string2 = XML.toString(jsonObject);
	System.out.println(string2);
    }

    @Test
    public void test1() throws UnsupportedEncodingException, JAXBException, GSException, ParseException {

	Dataset dataset = new Dataset();

	ReportsMetadataHandler handler = new ReportsMetadataHandler(dataset);

	// --------------------------------------------

	DataDescriptor gridDescriptor = createGridDescriptor();
	DataDescriptor tsDescriptor = createTimeSeriesDescriptor();

	// --------------------------------------------

	DataDescriptor gridTargetDescriptor = DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getTargetDescriptor(gridDescriptor);
	DataDescriptor tsTargetDescriptor = DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE.getTargetDescriptor(tsDescriptor);

	// --------------------------------------------

	DataComplianceReport report1 = null;

	{
	    report1 = createReport("randomId1", gridTargetDescriptor);

	    report1.setTargetTest(DataComplianceTest.EXECUTION);
	    report1.setLastSucceededTest(DataComplianceTest.EXECUTION);
	    report1.setTargetComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE);
	    report1.setDescriptors(gridDescriptor, tsDescriptor);

	    report1.setDownloadable(true);
	    report1.setDownloadTime(6000);

	    ValidationMessage validationMessage = new ValidationMessage();
	    validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    report1.setValidationMessage(validationMessage);

	    ValidationMessage execMessage = new ValidationMessage();
	    execMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    report1.setExecutionResult(execMessage);
	    report1.setExecutionTime(8000);

	    handler.addReport(report1);
	}

	{
	    DataComplianceReport report = createReport("randomId2", tsTargetDescriptor);

	    report.setTargetTest(DataComplianceTest.EXECUTION);
	    report.setLastSucceededTest(DataComplianceTest.VALIDATION);

	    report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	    report.setDescriptors(tsDescriptor, gridDescriptor);

	    report.setDownloadable(true);
	    report.setDownloadTime(7000);

	    ValidationMessage validationMessage = new ValidationMessage();
	    validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    report.setValidationMessage(validationMessage);

	    ValidationMessage execMessage = new ValidationMessage();
	    execMessage.setResult(ValidationResult.VALIDATION_FAILED);
	    execMessage.setError("Execution error");

	    report.setExecutionTime(3000);
	    report.setExecutionResult(execMessage);

	    handler.addReport(report);
	}

	{
	    DataComplianceReport report = createReport("randomId3", tsTargetDescriptor);

	    report.setTargetTest(DataComplianceTest.EXECUTION);
	    report.setLastSucceededTest(DataComplianceTest.DOWNLOAD);

	    report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	    report.setDescriptors(tsDescriptor, gridDescriptor);
	    report.setDownloadable(true);
	    report.setDownloadTime(5000);

	    ValidationMessage validationMessage = new ValidationMessage();
	    validationMessage.setResult(ValidationResult.VALIDATION_FAILED);
	    validationMessage.setError("Validation error");

	    report.setValidationMessage(validationMessage);

	    handler.addReport(report);
	}

	{
	    DataComplianceReport report = createReport("randomId4", tsTargetDescriptor);

	    report.setTargetTest(DataComplianceTest.EXECUTION);
	    report.setLastSucceededTest(DataComplianceTest.BASIC);

	    report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	    report.setDescriptors(tsDescriptor, gridDescriptor);

	    report.setDownloadable(false);

	    handler.addReport(report);
	}

	{
	    DataComplianceReport report = createReport("randomId5", tsTargetDescriptor);

	    report.setTargetTest(DataComplianceTest.EXECUTION);
	    report.setLastSucceededTest(DataComplianceTest.BASIC);

	    report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	    report.setDescriptors(tsDescriptor, gridDescriptor);

	    handler.addReport(report);
	}

	{
	    DataComplianceReport report = new DataComplianceReport("randomId6", tsTargetDescriptor);

	    report.setTargetTest(DataComplianceTest.EXECUTION);
	    report.setLastSucceededTest(DataComplianceTest.NONE);

	    report.setTargetComplianceLevel(DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);
	    report.setDescriptors(tsDescriptor, gridDescriptor);

	    handler.addReport(report);
	}

	// --------------------------------------------

	System.out.println(dataset.asString(false));

	// --------------------------------------------

	test(handler, report1);
    }

    @Test
    public void test2() throws UnsupportedEncodingException, JAXBException, GSException, ParseException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("dataset.xml");
	Dataset dataset = Dataset.create(stream);

	ReportsMetadataHandler handler = new ReportsMetadataHandler(dataset);
	DataComplianceReport report = handler.getReports().get(0);

	test(handler, report);
    }

    private void test(ReportsMetadataHandler handler, DataComplianceReport report1)
	    throws UnsupportedEncodingException, JAXBException, GSException, ParseException {

	List<DataComplianceReport> reports = handler.getReports();
	Assert.assertEquals(6, reports.size());

	{
	    DataComplianceReport report = reports.get(0);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId1", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.EXECUTION);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkGridDescriptor(report);

	    DataDescriptor preDescriptor = report.getPreviewDataDescriptor();
	    DataDescriptorWrapper dataDescriptorWrapper = new DataDescriptorWrapper(preDescriptor);
	    DataDescriptor wrap = DataDescriptorWrapper.wrap(dataDescriptorWrapper);

	    checkGridDescriptor(wrap);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.TIME_SERIES, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(2, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(3, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertTrue(downloadable.get());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertTrue(Objects.equals(downloadTime.get(), 6000l));

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, validationMessage.get().getResult());

	    String error = validationMessage.get().getError();
	    Assert.assertNull(error);

	    // ----- EXECUTION

	    Optional<ValidationMessage> execResult = report.getExecutionResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, execResult.get().getResult());

	    error = execResult.get().getError();
	    Assert.assertNull(error);

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertTrue(Objects.equals(executionTime.get(), 8000l));

	}

	{
	    DataComplianceReport report = reports.get(1);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId2", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.VALIDATION);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkTimeSeriesDescriptor(report);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.GRID, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(2, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(3, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertTrue(downloadable.get());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertTrue(Objects.equals(downloadTime.get(), 7000l));

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, validationMessage.get().getResult());

	    String error = validationMessage.get().getError();
	    Assert.assertNull(error);

	    // ----- EXECUTION

	    Optional<ValidationMessage> execResult = report.getExecutionResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, execResult.get().getResult());

	    Assert.assertEquals("Execution error", execResult.get().getError());

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertTrue(Objects.equals(executionTime.get(), 3000l));
	}

	{
	    DataComplianceReport report = reports.get(2);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId3", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.DOWNLOAD);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkTimeSeriesDescriptor(report);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.GRID, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(2, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(3, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertTrue(downloadable.get());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertTrue(Objects.equals(downloadTime.get(), 5000l));

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, validationMessage.get().getResult());

	    Assert.assertEquals("Validation error", validationMessage.get().getError());

	    // ----- EXECUTION

	    Optional<ValidationMessage> executionResult = report.getExecutionResult();
	    Assert.assertTrue(!executionResult.isPresent());

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertTrue(!executionTime.isPresent());
	}

	{
	    DataComplianceReport report = reports.get(3);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId4", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.BASIC);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkTimeSeriesDescriptor(report);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.GRID, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(2, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(3, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertFalse(downloadable.get());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertFalse(downloadTime.isPresent());

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertFalse(validationMessage.isPresent());

	    // ----- EXECUTION

	    Optional<ValidationMessage> executionResult = report.getExecutionResult();
	    Assert.assertFalse(executionResult.isPresent());

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertFalse(executionTime.isPresent());
	}

	{
	    DataComplianceReport report = reports.get(4);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId5", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.BASIC);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkTimeSeriesDescriptor(report);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.GRID, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(2, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(3, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertFalse(downloadable.isPresent());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertFalse(downloadTime.isPresent());

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertFalse(validationMessage.isPresent());

	    // ----- EXECUTION

	    Optional<ValidationMessage> executionResult = report.getExecutionResult();
	    Assert.assertFalse(executionResult.isPresent());

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertFalse(executionTime.isPresent());
	}

	{
	    DataComplianceReport report = reports.get(5);

	    String onlineId = report.getOnlineId();
	    Assert.assertEquals("randomId6", onlineId);

	    DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
	    Assert.assertEquals(lastSucceededTest, DataComplianceTest.NONE);

	    // -------------------------------------------------------------
	    // at the moment no target descriptor is serialized.
	    // not strictly required since AccessAugmenter uses a compliance level
	    //
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    Assert.assertNull(targetDescriptor);

	    // -------------------------------------------------------------
	    //
	    //
	    checkTimeSeriesDescriptor(report);

	    // quick test on the full descriptor
	    DataDescriptor fullDataDescriptor = report.getFullDataDescriptor();
	    Assert.assertEquals(DataType.GRID, fullDataDescriptor.getDataType());

	    // ----- BASIC

	    int workflowsCount = report.getWorkflowsCount();
	    Assert.assertEquals(0, workflowsCount);

	    int workflowsLength = report.getWorkflowsLength();
	    Assert.assertEquals(0, workflowsLength);

	    // ----- DOWNLOAD

	    Optional<Boolean> downloadable = report.isDownloadable();
	    Assert.assertFalse(downloadable.isPresent());

	    Optional<Long> downloadTime = report.getDownloadTime();
	    Assert.assertFalse(downloadTime.isPresent());

	    // ----- VALIDATION

	    Optional<ValidationMessage> validationMessage = report.getValidationMessage();
	    Assert.assertFalse(validationMessage.isPresent());

	    // ----- EXECUTION

	    Optional<ValidationMessage> executionResult = report.getExecutionResult();
	    Assert.assertFalse(executionResult.isPresent());

	    Optional<Long> executionTime = report.getExecutionTime();
	    Assert.assertFalse(executionTime.isPresent());
	}

	handler.clearReports();
	Assert.assertEquals(0, handler.getReports().size());

	handler.addReport(report1);
	handler.addReport(report1);

	Assert.assertEquals(2, handler.getReports().size());
    }

    private void checkTimeSeriesDescriptor(DataComplianceReport report) {

	Assert.assertTrue(!report.getTimeStamp().equals(""));

	DataFormat dataFormat = report.getPreviewDataDescriptor().getDataFormat();
	Assert.assertEquals(DataFormat.NETCDF_3(), dataFormat);

	DataType dataType = report.getPreviewDataDescriptor().getDataType();
	Assert.assertEquals(DataType.TIME_SERIES, dataType);

	CRS crs = report.getPreviewDataDescriptor().getCRS();
	Assert.assertEquals(CRS.EPSG_4326(), crs);

	List<DataDimension> otherDimensions = report.getPreviewDataDescriptor().getOtherDimensions();
	Assert.assertEquals(2, otherDimensions.size());

	List<DataDimension> spatialDimensions = report.getPreviewDataDescriptor().getSpatialDimensions();
	Assert.assertEquals(3, spatialDimensions.size());

	DataDimension temporalDimension = report.getPreviewDataDescriptor().getTemporalDimension();
	Assert.assertNotNull(temporalDimension);

	Assert.assertTrue(temporalDimension != null);
    }

    private void checkGridDescriptor(DataComplianceReport report) {

	Assert.assertTrue(!report.getTimeStamp().equals(""));

	DataFormat dataFormat = report.getPreviewDataDescriptor().getDataFormat();
	Assert.assertEquals(DataFormat.NETCDF_3(), dataFormat);

	checkGridDescriptor(report.getPreviewDataDescriptor());
    }

    private void checkGridDescriptor(DataDescriptor desc) {

	DataFormat dataFormat = desc.getDataFormat();
	Assert.assertEquals(DataFormat.NETCDF_3(), dataFormat);

	DataType dataType = desc.getDataType();
	Assert.assertEquals(DataType.GRID, dataType);

	CRS crs = desc.getCRS();
	Assert.assertEquals(CRS.EPSG_4326(), crs);

	List<DataDimension> otherDimensions = desc.getOtherDimensions();
	Assert.assertEquals(2, otherDimensions.size());

	List<DataDimension> spatialDimensions = desc.getSpatialDimensions();
	Assert.assertEquals(3, spatialDimensions.size());

	DataDimension temporalDimension = desc.getTemporalDimension();
	Assert.assertNotNull(temporalDimension);

	Assert.assertTrue(temporalDimension != null);
    }

    private DataDescriptor createGridDescriptor() {

	DataDescriptor descriptor = new DataDescriptor();

	descriptor.setDataFormat(DataFormat.NETCDF_3());
	descriptor.setDataType(DataType.GRID);
	descriptor.setCRS(CRS.EPSG_4326());

	// ---------------------------------------------------------

	List<DataDimension> otherDimensions = Lists.newArrayList();

	ContinueDimension vertical = new ContinueDimension("verticalNoType");
	vertical.setDatum(new Datum("NGVD29"));
	vertical.setLower(1345);
	vertical.setUpper(134);

	otherDimensions.add(vertical);

	FiniteDimension verticalDiscrete = new FiniteDimension("other");
	verticalDiscrete.setPoints(Arrays.asList("1", "2", "3"));

	otherDimensions.add(verticalDiscrete);

	descriptor.setOtherDimensions(otherDimensions);

	// ---------------------------------------------------------

	List<DataDimension> spatialDimensions = Lists.newArrayList();

	ContinueDimension lat = new ContinueDimension("Latitude");
	lat.setLower(41.718473);
	lat.setType(DimensionType.COLUMN);
	lat.setUom(Unit.DEGREE);
	lat.setUpper(41.718473);
	lat.setResolution(1000d);

	ContinueDimension lon = new ContinueDimension("Longitude");
	lon.setLower(-111.946402);
	lon.setType(DimensionType.ROW);
	lon.setUom(Unit.DEGREE);
	lon.setUpper(-111.946402);
	lon.setResolution(1000l);
	lon.setSize(500l);
	lon.setDatum(Datum.SEA_LEVEL_DATUM_1929());

	spatialDimensions.add(lat);
	spatialDimensions.add(lon);

	FiniteDimension discreteX = new FiniteDimension("X");
	discreteX.setType(DimensionType.ROW);
	discreteX.setPoints(Arrays.asList("1", "2", "3"));

	spatialDimensions.add(discreteX);

	descriptor.setSpatialDimensions(spatialDimensions);

	// -----------------------------------------------------------

	ContinueDimension time = new ContinueDimension("timeResolutionInt");
	time.setDatum(Datum.UNIX_EPOCH_TIME());
	time.setLower(1123180200000l);
	time.setResolution((int) 1800000);
	time.setType(DimensionType.TIME);
	time.setUom(Unit.MILLI_SECOND);
	time.setUpper(1123216200000l);
	time.setResolution(1234);
	time.setSize(500l);
	time.setDatum(Datum.UNIX_EPOCH_TIME());

	descriptor.setTemporalDimension(time);

	return descriptor;
    }

    private DataDescriptor createTimeSeriesDescriptor() {

	DataDescriptor descriptor = createGridDescriptor();

	FiniteDimension discreteTime = new FiniteDimension("time");
	discreteTime.setType(DimensionType.TIME);
	discreteTime.setPoints(Arrays.asList("1", "2", "3"));

	DataDimension dataDimension = discreteTime;
	descriptor.setTemporalDimension(dataDimension);

	descriptor.setDataType(DataType.TIME_SERIES);

	return descriptor;
    }

    private DataComplianceReport createReport(String onlineId, DataDescriptor target) {

	return new DataComplianceReport(onlineId, target) {

	    @Override
	    public int getWorkflowsCount() {

		return 2;
	    }

	    @Override
	    public int getWorkflowsLength() {

		return 3;
	    }
	};
    }
}
