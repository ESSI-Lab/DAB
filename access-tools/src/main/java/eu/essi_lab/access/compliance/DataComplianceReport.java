package eu.essi_lab.access.compliance;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.workflow.builder.Workflow;
public class DataComplianceReport {

    public static final String NONE = "none";

    private String timeStamp;
    private DataDescriptor previewDescriptor;
    private ValidationMessage message;
    private Long downloadTime;
    private Boolean isDownloadable;
    private String onlineId;
    private DataDescriptor targetDescriptor;
    private ValidationMessage executionResult;
    private List<Workflow> workflows;
    private Long executionTime;
    private DataComplianceLevel targetLevel;
    private DataComplianceTest lastSuccededTest;
    private DataComplianceTest targetTest;
    private DataObject downloadedData;

    private DataDescriptor fullDescriptor;

    /**
     * @param onlineId
     * @param targetDescriptor
     * @param timeStamp
     */
    public DataComplianceReport(String onlineId, DataDescriptor targetDescriptor, String timeStamp) {

	this.onlineId = onlineId;
	this.targetDescriptor = targetDescriptor;
	this.workflows = new ArrayList<>();
	this.timeStamp = timeStamp;
    }

    /**
     * @param onlineId
     * @param targetDescriptor
     */
    public DataComplianceReport(String onlineId, DataDescriptor targetDescriptor) {

	this(onlineId, targetDescriptor, ISO8601DateTimeUtils.getISO8601DateTime());
    }

    /**
     * @return
     */
    public String getOnlineId() {

	return onlineId;
    }

    /**
     * Returns the preview {@link DataDescriptor} related to this report
     */
    public DataDescriptor getPreviewDataDescriptor() {

	return previewDescriptor;
    }

    /**
     * Returns the full {@link DataDescriptor} related to this report
     */
    public DataDescriptor getFullDataDescriptor() {

	return fullDescriptor;
    }

    /**
     * @return
     */
    public DataDescriptor getTargetDescriptor() {

	return targetDescriptor;
    }

    /**
     * Returns the last succeeded {@link DataComplianceTest}
     */
    public DataComplianceTest getLastSucceededTest() {

	return lastSuccededTest;
    }

    /**
     * Set the last succeeded {@link DataComplianceTest}
     */
    public void setLastSucceededTest(DataComplianceTest lastTest) {

	this.lastSuccededTest = lastTest;
    }

    /**
     * @return
     */
    public DataComplianceTest getTargetTest() {

	return targetTest;
    }

    /**
     * @param targetTest
     */
    public void setTargetTest(DataComplianceTest targetTest) {

	this.targetTest = targetTest;
    }

    /**
     * @return
     */
    public List<Workflow> getWorkflows() {

	return workflows;
    }

    /**
     * Returns the number of {@link Workflow}s available for the target {@link DataDescriptor}.<br> Required test type: {@link
     * DataComplianceTest#BASIC}
     */
    public int getWorkflowsCount() {

	return workflows.size();
    }

    /**
     * Returns the length of the {@link Workflow}s available for the target {@link DataDescriptor}.<br> Required test type: {@link
     * DataComplianceTest#BASIC}
     *
     * @param targetLevel
     * @return
     */
    public int getWorkflowsLength() {

	return workflows.isEmpty() ? 0 : workflows.get(0).getWorkblocks().size();
    }

    /**
     * Returns an {@link Optional} describing the validity of the downloaded data, or an empty {@link Optional} if the {@link
     * DataComplianceTest#VALIDATION} test has not been performed (because not requested or because the {@link DataComplianceTest#DOWNLOAD}
     * is failed).<br> Required test type: {@link DataComplianceTest#VALIDATION}
     */
    public Optional<ValidationMessage> getValidationMessage() {

	return Optional.ofNullable(message);
    }

    /**
     * Returns an {@link Optional} describing if the data is downloadable, or an empty {@link Optional} if the {@link
     * DataComplianceTest#DOWNLOAD} test has not been performed.<br> Required test type: {@link DataComplianceTest#DOWNLOAD}
     */
    public Optional<Boolean> isDownloadable() {

	return Optional.ofNullable(isDownloadable);
    }

    /**
     * Returns an {@link Optional} describing the amount of time (in milliseconds) required to download the data, or an empty {@link
     * Optional} if the {@link DataComplianceTest#DOWNLOAD} test has not been performed.<br> Required test type: {@link
     * DataComplianceTest#DOWNLOAD}
     */
    public Optional<Long> getDownloadTime() {

	return Optional.ofNullable(downloadTime);
    }

    /**
     * Returns an {@link Optional} describing the execution result of the tested data for the target {@link DataDescriptor}, or an empty
     * {@link Optional} if the {@link DataComplianceTest#EXECUTION} test has not been performed (because not requested of because the {@link
     * DataComplianceTest#VALIDATION} is failed).<br> The returned message result is {@link ValidationResult#VALIDATION_SUCCESSFUL} if at
     * the end of the workflow execution, the transformed data is valid according to its {@link DataFormat} and to the proper {@link
     * DataValidator}, {@link ValidationResult#VALIDATION_FAILED} otherwise.<br> Required test type: {@link DataComplianceTest#EXECUTION}
     */
    public Optional<ValidationMessage> getExecutionResult() {

	return Optional.ofNullable(this.executionResult);
    }

    /**
     * Returns an {@link Optional} describing the execution time of the tested data for the target {@link DataDescriptor}, or an empty
     * {@link Optional} if the {@link DataComplianceTest#EXECUTION} test has not been performed or if the data is not compliant to the
     * target {@link DataDescriptor}.<br> Required test type: {@link DataComplianceTest#EXECUTION}
     */
    public Optional<Long> getExecutionTime() {

	return Optional.ofNullable(this.executionTime);
    }

    /**
     * @param targetLevel
     */
    public void setTargetComplianceLevel(DataComplianceLevel targetLevel) {

	this.targetLevel = targetLevel;
    }

    /**
     * @return the level
     */
    public DataComplianceLevel getTargetComplianceLevel() {

	return targetLevel;
    }

    /**
     * @return
     */
    public Optional<Workflow> getPreferredWorkflow() {

	return workflows.stream().//
		max((w1, w2) -> Integer.compare(w1.getPreference(), w2.getPreference()));
    }

    /**
     * @param preview
     * @param full
     */
    public void setDescriptors(DataDescriptor preview, DataDescriptor full) {

	this.previewDescriptor = preview;
	this.fullDescriptor = full;
    }

    /**
     * @param message
     */
    public void setValidationMessage(ValidationMessage message) {

	this.message = message;
    }

    /**
     * @param downloadTime
     */
    public void setDownloadTime(long downloadTime) {

	this.downloadTime = downloadTime;
    }

    /**
     * @param isDownloadable
     */
    public void setDownloadable(boolean isDownloadable) {

	this.isDownloadable = isDownloadable;
    }

    /**
     * @param executionTime
     */
    public void setExecutionTime(long executionTime) {

	this.executionTime = executionTime;
    }

    /**
     * @param valid
     */
    public void setExecutionResult(ValidationMessage valid) {

	this.executionResult = valid;
    }

    /**
     * @return the downloadedData
     */
    public DataObject getDownloadedData() {

	return downloadedData;
    }

    /**
     * @param downloadedData the downloadedData to set
     */
    public void setDownloadedData(DataObject downloadedData) {

	this.downloadedData = downloadedData;
    }

    /**
     * @return the dateStamp
     */
    public String getTimeStamp() {

	return timeStamp;
    }

    /**
     * @return
     */
    public static String formatTime(long time) {

	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss.SSS", Locale.UK);

	Date date = new Date(time);
	return formatter.format(date);
    }

    @Override
    public String toString() {

	Optional<Boolean> id = isDownloadable();

	String out = "---\n";
	out += "Online id: " + getOnlineId() + "\n";
	out += "Descriptor : " + getPreviewDataDescriptor().toString() + "\n";
	out += "Last succeeded test : " + getLastSucceededTest() + "\n";
	if (targetLevel != null) {
	    out += "Compliance level: " + targetLevel.getLabel() + "\n";
	} else {
	    out += "Target descriptor: " + getTargetDescriptor();
	}
	out += "--- BASIC TEST ---\n";
	out += "Workflows count: " + getWorkflowsCount() + "\n";
	out += "Workflows length: " + getWorkflowsLength() + "\n";
	out += "--- DOWNLOAD TEST ---\n";
	out += "Is downloadable: " + (!id.isPresent() ? NONE : id.get()) + "\n";
	out += "--- VALIDATION TEST ---\n";

	Optional<ValidationMessage> vm = getValidationMessage();

	out += "Validation result: " + (!vm.isPresent() ? NONE : vm.get().getResult()) + "\n";
	if (vm.isPresent() && vm.get().getResult() == ValidationResult.VALIDATION_FAILED) {
	    out += "Validation message: " + vm.get().getError() + "\n";
	}

	String stringTime = NONE;
	Optional<Long> dt = getDownloadTime();

	if (dt.isPresent()) {
	    stringTime = formatTime(dt.get());
	}
	out += "Download time: " + stringTime + "\n";
	out += "--- EXECUTION TEST ---\n";

	Optional<Long> execTime = getExecutionTime();
	String time = !execTime.isPresent() ? NONE : formatTime(execTime.get());

	Optional<ValidationMessage> er = getExecutionResult();

	out += "Execution result: " + (!er.isPresent() ? NONE : er.get().getResult()) + "\n";
	if (er.isPresent() && er.get().getResult() == ValidationResult.VALIDATION_FAILED) {
	    out += "Execution message: " + er.get().getError() + "\n";
	}

	out += "Execution time: " + time + "\n";

	out += "---";

	return out;
    }

}
