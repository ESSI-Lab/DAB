package eu.essi_lab.access.compliance;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataValidator;
import eu.essi_lab.access.DataValidatorFactory;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.Chronometer.TimeFormat;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.builder.Workflow;
import eu.essi_lab.workflow.builder.WorkflowBuilder;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * A tool which executes tests in order to determine if a data described by a {@link DataDescriptor} can be transformed
 * in a data described
 * by another {@link DataDescriptor}. The {@link DataDescriptor}s used to execute the tests are provided by the {@link
 * DataDownloader#getPreviewRemoteDescriptors()} method
 *
 * @author Fabrizio
 */
public class DataComplianceTester {

    private static final String EXECUTION_TEST_ERROR = "EXECUTION_TEST_ERROR";
    private static final String DATA_COMPLIANCE_TEST_NO_DATA_VALIDATOR_ERROR = "DATA_COMPLIANCE_TEST_NO_DATA_VALIDATOR_ERROR";
    private static final String EXECUTION_TIME = "execTime";
    public static final String EXECUTION_OUTPUT = "executionOutput";

    private DataDownloader dataDownloader;
    public DataDownloader getDataDownloader() {
        return dataDownloader;
    }

    private Logger logger = GSLoggerFactory.getLogger(DataComplianceTester.class);

    /**
     * Enumeration of available compliance tests
     *
     * @author Fabrizio
     */
    public enum DataComplianceTest {
	/**
	 * Used to set {@link DataComplianceReport#getLastSucceededTest()} when none of the compliance tests have
	 * succeeded
	 */
	NONE(0),
	/**
	 * Tests if is possible to transform a data described by a {@link DataDescriptor} to a data described by another
	 * {@link
	 * DataDescriptor}. The test succeeds if there is at least one {@link Workflow} which can perform such
	 * transformation
	 */
	BASIC(1),
	/**
	 * Executes the {@link #BASIC} test, and if succeeded, tests if a data described by a {@link DataDescriptor} can
	 * be downloaded by a
	 * {@link DataDownloader}. The test succeeds if the data is correctly downloaded
	 */
	DOWNLOAD(2),
	/**
	 * Executes the {@link #BASIC} and the {@link #DOWNLOAD} test, and if succeeded, tests if the downloaded data is
	 * valid according to
	 * a proper {@link DataValidator}
	 */
	VALIDATION(3),
	/**
	 * Executes the {@link #BASIC}, {@link #DOWNLOAD} and {@link #VALIDATION} tests, and if succeeded, executes a
	 * {@link Workflow}
	 * created in the {@link #BASIC} test on the data downloaded in the {@link #DOWNLOAD} test. The test succeeds if
	 * at the end of a
	 * preferred {@link Workflow} execution, the data is valid according to a proper {@link DataValidator}
	 */
	EXECUTION(4);

	private int order;

	/**
	 * @param order
	 */
	private DataComplianceTest(int order) {

	    this.order = order;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {

	    return order;
	}
    }

    /**
     * Creates a new instance with the supplied <code>dataDownloader</code>
     *
     * @param dataDownloader
     */
    public DataComplianceTester(DataDownloader dataDownloader) {

	this.dataDownloader = dataDownloader;
    }

    /**
     * Executes the supplied <code>test</code> in order to determine if the supplied <code>dataDescriptor</code> which
     * must be one of the
     * {@link DataDownloader#getPreviewRemoteDescriptors()}, describes a data that can be transformed according to the
     * given
     * <code>targetDescriptor</code>.
     *
     * @param test the {@link DataComplianceTest} to perform
     * @param previewDescriptor the preview {@link DataDescriptor} to test
     * @param fullDescriptor full version of the <code>preview</code> {@link DataDescriptor}
     * @param targetDescriptor the target {@link DataDescriptor}
     * @return
     * @throws GSException if errors occur during the test execution
     * @throws IllegalArgumentException if the supplied <code>dataDescriptor</code> is not included in the {@link
     *         DataDownloader#getPreviewRemoteDescriptors()} list
     */
    public DataComplianceReport test(//
	    DataComplianceTest test, //
	    DataDescriptor previewDescriptor, //
	    DataDescriptor fullDescriptor, //
	    DataDescriptor targetDescriptor) throws GSException {

	if (!dataDownloader.getPreviewRemoteDescriptors().contains(previewDescriptor)) {
	    throw new IllegalArgumentException("Invalid data descriptor");
	}

	return test(//
		Arrays.asList(previewDescriptor), //
		Arrays.asList(fullDescriptor), //
		test, //
		targetDescriptor, //
		null).get(0);
    }

    /**
     * Executes the supplied <code>test</code> in order to determine if the data described by
     * <code>dataDescriptor</code> is compliant with the given <code>level</code>
     *
     * @param test the {@link DataComplianceTest} to perform
     * @param previewDescriptor the preview {@link DataDescriptor} to test
     * @param fullDescriptor full version of the <code>preview</code> {@link DataDescriptor}
     * @param level the {@link DataComplianceLevel} to test
     * @return
     * @throws GSException if errors occur during the test execution
     * @throws IllegalArgumentException if the supplied <code>dataDescriptor</code> is not included in the {@link
     *         DataDownloader#getPreviewRemoteDescriptors()} list
     */
    public DataComplianceReport test(DataComplianceTest test, DataDescriptor previewDescriptor, DataDescriptor fullDescriptor,
	    DataComplianceLevel level) throws GSException {

	// if (!dataDownloader.getPreviewRemoteDescriptors().contains(previewDescriptor)) {
	// throw new IllegalArgumentException("Invalid data descriptor");
	// }

	List<DataDescriptor> previewDescriptors = Arrays.asList(previewDescriptor);

	List<DataDescriptor> fullDescriptors = Arrays.asList(fullDescriptor);

	List<DataComplianceReport> reports = test(previewDescriptors, fullDescriptors, test, null, level);

	return reports.get(0);
    }

    /**
     * Executes the supplied <code>test</code> in order to determine if the {@link DataDescriptor}s provided by {@link
     * DataDownloader#getPreviewRemoteDescriptors()} describe a data that can be transformed according to the given
     * <code>targetDescriptor</code>
     *
     * @param test the {@link DataComplianceTest} to perform
     * @param targetDescriptor the target {@link DataDescriptor}
     * @return a list of {@link DataComplianceReport}, one for each {@link DataDescriptor} provided by {@link
     *         DataDownloader#getPreviewRemoteDescriptors()}
     * @throws GSException if errors occur during the test execution
     */
    public List<DataComplianceReport> test(DataComplianceTest test, DataDescriptor targetDescriptor) throws GSException {

	List<DataDescriptor> fullDescriptors = dataDownloader.getRemoteDescriptors();

	List<DataDescriptor> previewDescriptors = dataDownloader.getPreviewRemoteDescriptors(fullDescriptors);

	return test(previewDescriptors, fullDescriptors, test, targetDescriptor, null);
    }

    /**
     * Executes the supplied <code>test</code> in order to determine if the {@link DataDescriptor}s provided by {@link
     * DataDownloader#getPreviewRemoteDescriptors()} describe a data compliant with the given
     * <code>level</code>
     *
     * @param test the {@link DataComplianceTest} to perform
     * @param targetLevel the {@link DataComplianceLevel} to test
     * @return a list of {@link DataComplianceReport}, one for each {@link DataDescriptor} provided by {@link
     *         DataDownloader#getPreviewRemoteDescriptors()}
     * @throws GSException if errors occur during the test execution
     */
    public List<DataComplianceReport> test(DataComplianceTest test, DataComplianceLevel targetLevel) throws GSException {

	List<DataDescriptor> fullRemoteDescriptors = dataDownloader.getRemoteDescriptors();

	List<DataDescriptor> previewRemoteDescriptors = dataDownloader.getPreviewRemoteDescriptors(fullRemoteDescriptors);

	return test(previewRemoteDescriptors, fullRemoteDescriptors, test, null, targetLevel);
    }

    @SuppressWarnings("incomplete-switch")
    private List<DataComplianceReport> test(//
	    List<DataDescriptor> previewDescriptors, //
	    List<DataDescriptor> fullDescriptors, //
	    DataComplianceTest test, //
	    DataDescriptor targetDescriptor, //
	    DataComplianceLevel level)

	    throws GSException {

	ArrayList<DataComplianceReport> out = new ArrayList<>();

	String dateTime = ISO8601DateTimeUtils.getISO8601DateTime();

	for (int i = 0; i < previewDescriptors.size(); i++) {

	    DataDescriptor previewDescriptor = previewDescriptors.get(i);
	    DataDescriptor fullDescriptor = fullDescriptors.get(i);

	    if (level != null) {

		targetDescriptor = level.getTargetDescriptor(previewDescriptor);
	    }

	    DataComplianceReport report = new DataComplianceReport(//
		    dataDownloader.getOnline().getIdentifier(), //
		    targetDescriptor, //
		    dateTime);

	    report.setTargetTest(test);

	    if (level != null) {

		report.setTargetComplianceLevel(level);
	    }

	    switch (test) {
	    case BASIC:

		basicTest(previewDescriptor, fullDescriptor, report, targetDescriptor);
		break;

	    case DOWNLOAD:

		basicTest(previewDescriptor, fullDescriptor, report, targetDescriptor);
		downloadTest(previewDescriptor, report);
		break;

	    case VALIDATION:

		basicTest(previewDescriptor, fullDescriptor, report, targetDescriptor);
		downloadTest(previewDescriptor, report);
		validationTest(previewDescriptor, report);

		break;
	    case EXECUTION:

		basicTest(previewDescriptor, fullDescriptor, report, targetDescriptor);
		downloadTest(previewDescriptor, report);
		validationTest(previewDescriptor, report);
		executionTest(previewDescriptor, report);

		break;
	    }

	    out.add(report);
	}

	return out;
    }

    private void basicTest(//
	    DataDescriptor previewDescriptor, //
	    DataDescriptor fullDescriptor, //

	    DataComplianceReport report, //
	    DataDescriptor targetDescriptor) {

	logger.info("Basic test STARTED");

	report.setDescriptors(previewDescriptor, fullDescriptor);

	WorkflowBuilder builder = WorkflowBuilder.createLoadedBuilder();

	List<Workflow> workflows = builder.build(null, previewDescriptor, targetDescriptor);
	report.getWorkflows().addAll(workflows);

	if (!workflows.isEmpty()) {

	    report.setLastSucceededTest(DataComplianceTest.BASIC);

	} else {

	    report.setLastSucceededTest(DataComplianceTest.NONE);
	}

	logger.info("Basic test ENDED");
    }

    private void downloadTest(//
	    DataDescriptor descriptor, //
	    DataComplianceReport report) {

	if (dataDownloader.canDownload() && report.getWorkflowsCount() > 0) {

	    logger.info("Download test STARTED with current descriptor: {}", descriptor);

	    Chronometer chronometer = new Chronometer(TimeFormat.MIN_SEC_MLS);
	    chronometer.start();

	    File dataFile;
	    try {
		dataFile = dataDownloader.download(descriptor);
		dataFile.deleteOnExit();
	    } catch (GSException e) {

		handleException(e, "Error occurred during download test");

		return;
	    }

	    // set the download time
	    long elapsedTime = chronometer.getElapsedTimeMillis();
	    report.setDownloadTime(elapsedTime);

	    // data is downloadable
	    report.setDownloadable(true);

	    DataObject dataObject = new DataObject();
	    dataObject.setFile(dataFile);
	    dataObject.setDataDescriptor(descriptor);

	    // set the downloaded data object
	    report.setDownloadedData(dataObject);

	    report.setLastSucceededTest(DataComplianceTest.DOWNLOAD);

	    logger.info("Download test ENDED");

	    return;
	}

	logger.info("Download test SKIPPED");
    }

    private void validationTest(//
	    DataDescriptor descriptor, //
	    DataComplianceReport report) throws GSException {

	Optional<Boolean> rid = report.isDownloadable();

	if (rid.isPresent() && rid.get()) {

	    logger.info("Validation test STARTED with current descriptor: {}", descriptor);
	    DataObject data = report.getDownloadedData();

	    // validates the downloaded data
	    DataValidator validator = createValidator(descriptor.getDataFormat(), descriptor.getDataType());

	    data.setDataDescriptor(descriptor);

	    ValidationMessage message = validator.validate(data);

	    if (message.getResult() == ValidationResult.VALIDATION_SUCCESSFUL) {

		report.setLastSucceededTest(DataComplianceTest.VALIDATION);
	    }

	    report.setValidationMessage(message);

	    logger.info("Validation test ENDED");

	    return;
	}

	logger.info("Validation test SKIPPED");
    }

    private void executionTest(//
	    DataDescriptor descriptor, //
	    DataComplianceReport report) {

	Optional<ValidationMessage> rvm = report.getValidationMessage();

	if (rvm.isPresent() && rvm.get().getResult() == ValidationResult.VALIDATION_SUCCESSFUL) {

	    logger.info("Execution test STARTED with current descriptor: {}", descriptor);

	    Optional<Workflow> preferred = report.getPreferredWorkflow();

	    if (!preferred.isPresent()) {

		logger.warn("Can't find preferred workflow for {}, returning", descriptor);

		return;

	    }

	    Workflow preferredWorkflow = preferred.get();
	    DataDescriptor targetDescriptor = report.getTargetDescriptor();
	    DataObject downloadedData = report.getDownloadedData();

	    ValidationMessage message = new ValidationMessage();

	    try {
		message = doExecutionTest(descriptor, preferredWorkflow, targetDescriptor, downloadedData);
	    } catch (GSException ex) {

		message.setError(ex.getMessage());
		message.setResult(ValidationResult.VALIDATION_FAILED);

		GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    }

	    DataObject executionOutput = message.getPayload().get(DataComplianceTester.EXECUTION_OUTPUT, DataObject.class);

	    if (executionOutput != null) {
		DataFormat format = executionOutput.getDataDescriptor().getDataFormat();
		File file = executionOutput.getFile();
		if (format.equals(DataFormat.NETCDF()) || format.isSubTypeOf(DataFormat.NETCDF())) {
		    try {
			Number min = null;
			Number max = null;
			NetcdfDataset dataset = NetcdfDataset.openDataset(file.getAbsolutePath());
			for (Variable variable : dataset.getVariables()) {
			    List<Attribute> attributes = variable.getAttributes();
			    for (Attribute attribute : attributes) {
				String attributeName = attribute.getShortName();
				switch (attributeName) {
				case "valid_min":
				    min = attribute.getNumericValue();
				    break;
				case "valid_max":
				    max = attribute.getNumericValue();
				    break;
				default:
				    break;
				}
			    }
			}
			dataset.close();
			if (min != null && max != null) {
			    report.getFullDataDescriptor().setRangeMinimum(min);
			    report.getFullDataDescriptor().setRangeMaximum(max);
			    report.getPreviewDataDescriptor().setRangeMinimum(min);
			    report.getPreviewDataDescriptor().setRangeMaximum(max);

			}

		    } catch (IOException e) {
			e.printStackTrace();
		    }

		}
		// delete file after validation
		if (file.exists()) {
		    file.delete();
		}

	    }

	    if (message.getResult() == ValidationResult.VALIDATION_SUCCESSFUL) {

		report.setLastSucceededTest(DataComplianceTest.EXECUTION);

		// set the execution time
		report.setExecutionTime(message.getPayload().get(EXECUTION_TIME, Long.class));
	    }

	    // set the validity result
	    report.setExecutionResult(message);

	    logger.info("Execution test ENDED");

	    return;
	}

	logger.info("Execution test SKIPPED");

    }

    private ValidationMessage doExecutionTest(//
	    DataDescriptor descriptor, //
	    Workflow preferred, //
	    DataDescriptor targetDescriptor, //
	    DataObject dataObject) throws GSException {

	Chronometer chronometer = new Chronometer(TimeFormat.MIN_SEC_MLS);
	chronometer.start();

	// executes the workflow
	DataObject object = execute(preferred, dataObject, targetDescriptor);

	long timeMillis = chronometer.getElapsedTimeMillis();

	// transformed descriptor
	DataDescriptor resultDescriptor = object.getDataDescriptor();

	// -----------------------
	//
	// data description test
	//
	boolean equals = Objects.equals(//
		resultDescriptor, //
		targetDescriptor); //

	// ----------------------------
	//
	// data validation test
	//
	DataValidator validator = createValidator(resultDescriptor.getDataFormat(), resultDescriptor.getDataType());

	object.setDataDescriptor(resultDescriptor);

	ValidationMessage validationMessage = validator.validate(object);

	validationMessage.getPayload().add(new GSProperty<Long>(EXECUTION_TIME, timeMillis));

	validationMessage.getPayload().add(new GSProperty<DataObject>(EXECUTION_OUTPUT, object));

	// set the validity result
	validationMessage.setResult( //
		validationMessage.getResult() == ValidationResult.VALIDATION_SUCCESSFUL && equals ? //
			ValidationResult.VALIDATION_SUCCESSFUL : //
			ValidationResult.VALIDATION_FAILED);

	return validationMessage;
    }

    private DataObject execute(//
	    Workflow preferred, //
	    DataObject dataObject, //
	    DataDescriptor targetDescriptor) throws GSException {

	try {
	    return preferred.execute(null, dataObject, targetDescriptor);

	} catch (Exception e) {

	    logger.error("Exception executing workflow", e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    EXECUTION_TEST_ERROR, //
		    e);
	}
    }

    private DataValidator createValidator(DataFormat format, DataType dataType) throws GSException {

	Optional<DataValidator> opt = DataValidatorFactory.create(format, dataType);

	if (opt.isPresent()) {

	    return opt.get();

	} else {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to find DataValidator for format: " + format, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATA_COMPLIANCE_TEST_NO_DATA_VALIDATOR_ERROR);
	}
    }

    /**
     * @param e
     * @param message
     */
    private String handleException(GSException e, String message) {

	logger.error(message);

	List<ErrorInfo> infoList = e.getErrorInfoList();

	if (!infoList.isEmpty()) {

	    ErrorInfo errorInfo = infoList.get(0);
	    String errorDescription = errorInfo.getErrorDescription();

	    if (errorDescription != null) {

		logger.error("Error message: {}", errorDescription);
		return errorDescription;
	    }
	}

	return null;
    }
}
