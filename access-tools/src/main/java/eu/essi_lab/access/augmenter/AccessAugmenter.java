package eu.essi_lab.access.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;

/**
 * @author Fabrizio
 */
public class AccessAugmenter extends ResourceAugmenter<AugmenterSetting> {

    /**
     * 
     */
    public AccessAugmenter() {
    }

    /**
     * @param setting
     */
    public AccessAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Access augmentation of current resource STARTED");

	Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();

	if (distribution == null) {
	    return Optional.of(resource);
	}

	List<Online> onlines = Lists.newArrayList(distribution.getDistributionOnlines());

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	handler.clearReports();

	for (Online online : onlines) {

	    String onlineId = online.getIdentifier();
	    if (onlineId == null) {
		onlineId = online.setIdentifier();
	    }

	    if (online.getFunctionCode() != null) {
		if (online.getFunctionCode().equals("download")) {
		    try {
			augment(resource, onlineId, handler);
		    } catch (GSException gse) {
			throw gse;
		    } catch (Exception e) {
			e.printStackTrace();
			throw GSException.createException(//
				getClass(), //
				"Error during access augmentation. Resource id: " + resource.getPublicId() + " Online id: " + onlineId, //
				null, //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_ERROR, //
				"ACCESS_AUGMENTER_ERROR", //
				e);
		    }
		}
	    }
	}

	//
	// set the access quality
	//
	GSLoggerFactory.getLogger(getClass()).trace("Setting access quality");

	AccessQualifier qualifier = new AccessQualifier(resource);
	qualifier.setQuality();

	//
	// set the sscScore according to the reports
	//
	DownloadReportToSSCScoreAugmenter sscScoreAugmenter = new DownloadReportToSSCScoreAugmenter();
	Optional<GSResource> optional = sscScoreAugmenter.augment(resource);

	if (optional.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).trace("Resource updated with sscScore");

	    resource = optional.get();
	}

	GSLoggerFactory.getLogger(getClass()).info("Access augmentation of current resource ENDED");

	return Optional.of(resource);
    }

    @Override
    public String getType() {

	return "AccessAugmenter";
    }

    /**
     * Tests a single online resource, according to its identifier. If at least one descriptor passes the {@link
     * DataComplianceTest#EXECUTION} test, so the resource is marked as compliant with the {@link
     * DataComplianceLevel#GRID_BASIC_DATA_COMPLIANCE} or {@link DataComplianceLevel#TIME_SERIES_BASIC_DATA_COMPLIANCE}
     * (the only 2
     * supported compliance levels at the moment)
     *
     * @param resource
     * @param online
     * @param handler
     * @throws GSException
     */
    protected void augment(GSResource resource, String onlineId, ReportsMetadataHandler handler) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Augmenting resource with original id {}", resource.getOriginalId());
	GSLoggerFactory.getLogger(getClass()).debug("Current online id: {}", onlineId);

	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);

	if (downloader == null) {

	    GSLoggerFactory.getLogger(getClass()).error("No DataDownloader found");
	    return;
	}

	DataComplianceTester tester = new DataComplianceTester(downloader);

	List<DataDescriptor> fullList = downloader.getRemoteDescriptors();

	List<DataDescriptor> previewList = downloader.getPreviewRemoteDescriptors(fullList);

	DataComplianceLevel level = null;

	HashMap<DataComplianceTest, DataComplianceReport> testResults = new HashMap<>();

	DataType dataType = null;
	GSLoggerFactory.getLogger(getClass()).info("");
	GSLoggerFactory.getLogger(getClass()).info("Starting tests. Descriptors#: " + fullList.size());

	fullList.sort(new DescriptorComparator());
	previewList.sort(new DescriptorComparator());

	for (int i = 0; i < fullList.size(); i++) {

	    DataDescriptor full = fullList.get(i);

	    GSLoggerFactory.getLogger(getClass()).info("Descriptor #" + i + ":");
	    GSLoggerFactory.getLogger(getClass()).info(full.toString());
	    GSLoggerFactory.getLogger(getClass()).info("");

	    DataDescriptor preview = previewList.get(i);

	    dataType = preview.getDataType();
	    if (dataType == null) {
		GSLoggerFactory.getLogger(getClass()).error("Null data type. Skipping descriptor.");
		continue;
	    }
	    switch (dataType) {
	    case GRID:
		level = DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE;
		break;
	    case TIME_SERIES:
		level = DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE;
		break;
	    case TRAJECTORY:
		level = DataComplianceLevel.TRAJECTORY_BASIC_DATA_COMPLIANCE;
		break;	    
	    case VECTOR:
	    case POINT:
	    case PROFILE:
	    case TIME_SERIES_PROFILE:
	    case TRAJECTORY_PROFILE:
	    default:
		GSLoggerFactory.getLogger(getClass()).error("Unexpected data type: " + dataType + " Skipping descriptor.");
		continue;
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Data compliance test STARTED");

	    DataComplianceReport report = null;
	    try {
		report = getExecutionReport(tester, preview, full, level);
		GSLoggerFactory.getLogger(getClass()).debug("Data compliance test ENDED succesfully");

		DataComplianceTest lastSucceededTest = report.getLastSucceededTest();
		testResults.put(report.getLastSucceededTest(), report);
		if (lastSucceededTest.equals(DataComplianceTest.EXECUTION)) {
		    // already at the highest level!
		    break;
		}
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).debug("Data compliance test ENDED unexpectedly");
	    } finally {
		if (report != null) {
		    DataObject downloadedData = report.getDownloadedData();
		    if (downloadedData != null) {
			File downloadedFile = downloadedData.getFile();
			if (downloadedFile != null && downloadedFile.exists()) {
			    downloadedFile.delete();
			}
		    }
		}
	    }
	}

	DataComplianceReport report = chooseBestResult(testResults);

	DataComplianceTest lastSucceededTest = null;

	if (report != null) {
	    lastSucceededTest = report.getLastSucceededTest();
	    String timeStamp = report.getTimeStamp();

	    // set the test time stamp
	    ResourcePropertyHandler propertyHandler = resource.getPropertyHandler();
	    propertyHandler.setTestTimeStamp(timeStamp);
	}

	if (lastSucceededTest == null) {
	    setNotTransformable(resource);
	    setNotDownloadable(resource);
	    setNotExecutable(resource);

	    GSLoggerFactory.getLogger(getClass()).warn("Unmanageable resource");
	    return;
	}

	// set the higher order test
	setSucceededTest(resource, lastSucceededTest);

	switch (lastSucceededTest) {
	case NONE:
	case BASIC:

	    setNotTransformable(resource);
	    setNotDownloadable(resource);
	    resource.getPropertyHandler().setLastFailedDownloadDate();
	    setNotExecutable(resource);

	    GSLoggerFactory.getLogger(getClass()).warn("Unmanageable resource");
	    return;

	case DOWNLOAD:

	    resource.getPropertyHandler().setIsTransformable(true);
	    // this property is true only if the data can be downloaded and is valid
	    setNotDownloadable(resource);
	    resource.getPropertyHandler().setLastFailedDownloadDate();
	    setNotExecutable(resource);

	    GSLoggerFactory.getLogger(getClass()).warn("Execution test failed (download only)");
	    GSLoggerFactory.getLogger(getClass()).warn("{}", report);

	    break;
	case VALIDATION:

	    resource.getPropertyHandler().setIsTransformable(true);
	    // this property is true only if the data can be downloaded and is valid
	    resource.getPropertyHandler().setIsDownloadable(true);
	    resource.getPropertyHandler().setLastDownloadDate();
	    setNotExecutable(resource);
	    report.getDownloadTime().ifPresent(time -> resource.getPropertyHandler().addDownloadTime(time));

	    GSLoggerFactory.getLogger(getClass()).warn("Execution test failed (validation only)");
	    GSLoggerFactory.getLogger(getClass()).warn("{}", report);

	    break;

	case EXECUTION:

	    resource.getPropertyHandler().setIsTransformable(true);
	    resource.getPropertyHandler().setIsDownloadable(true);
	    resource.getPropertyHandler().setLastDownloadDate();
	    resource.getPropertyHandler().setIsExecutable(true);

	    report.getDownloadTime().ifPresent(time -> resource.getPropertyHandler().addDownloadTime(time));

	    report.getExecutionTime().ifPresent(time -> resource.getPropertyHandler().addExecutionTime(time));

	    resource.getPropertyHandler().addComplianceLevel(level.getLabel());

	    // -------------------------------------------
	    //
	    // adds the EXECUTION report
	    // for each online, only one EXECUTION report
	    //
	    handler.addReport(report);

	    switch (dataType) {
	    case GRID:
		resource.getPropertyHandler().setIsGrid(true);
		break;
	    case TIME_SERIES:
		resource.getPropertyHandler().setIsTimeseries(true);
		break;
	    case VECTOR:
		break;
	    case POINT:
		break;
	    case PROFILE:
		break;
	    case TIME_SERIES_PROFILE:
		break;
	    case TRAJECTORY:
		resource.getPropertyHandler().setIsTrajectory(true);
		break;
	    case TRAJECTORY_PROFILE:
		break;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Execution test succeeded");

	}

    }

    public DataComplianceReport getExecutionReport(DataComplianceTester tester, DataDescriptor preview, DataDescriptor full,
	    DataComplianceLevel level) throws GSException {
	return tester.test(DataComplianceTest.EXECUTION, preview, full, level);

    }

    private DataComplianceReport chooseBestResult(HashMap<DataComplianceTest, DataComplianceReport> testResults) {
	DataComplianceReport result;
	result = testResults.get(DataComplianceTest.EXECUTION);
	if (result != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Best result: " + DataComplianceTest.EXECUTION);
	    return result;
	}
	result = testResults.get(DataComplianceTest.VALIDATION);
	if (result != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Best result: " + DataComplianceTest.VALIDATION);
	    return result;
	}
	result = testResults.get(DataComplianceTest.DOWNLOAD);
	if (result != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Best result: " + DataComplianceTest.DOWNLOAD);
	    return result;
	}
	result = testResults.get(DataComplianceTest.BASIC);
	if (result != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Best result: " + DataComplianceTest.BASIC);
	    return result;
	}
	result = testResults.get(DataComplianceTest.NONE);
	if (result != null) {
	    GSLoggerFactory.getLogger(getClass()).info("Best result: " + DataComplianceTest.NONE);
	    return result;
	}
	GSLoggerFactory.getLogger(getClass()).info("No results");
	return null;
    }

    /**
     * Set the succeeded test with the higher order
     */
    protected void setSucceededTest(GSResource resource, DataComplianceTest lastSucceededTest) {

	Optional<String> opt = resource.getPropertyHandler().getSucceededTest();
	if (opt.isPresent()) {

	    DataComplianceTest test = DataComplianceTest.valueOf(opt.get());
	    if (lastSucceededTest.getOrder() > test.getOrder()) {

		resource.getPropertyHandler().setSucceededTest(lastSucceededTest.name());
	    }
	} else {

	    resource.getPropertyHandler().setSucceededTest(lastSucceededTest.name());
	}
    }

    private void setNotTransformable(GSResource resource) {

	if (!resource.getPropertyHandler().isTransformable().isPresent()) {

	    resource.getPropertyHandler().setIsTransformable(false);
	}
    }

    private void setNotDownloadable(GSResource resource) {

	if (!resource.getPropertyHandler().isDownloadable().isPresent()) {

	    resource.getPropertyHandler().setIsDownloadable(false);
	}
    }

    private void setNotExecutable(GSResource resource) {

	if (!resource.getPropertyHandler().isExecutable().isPresent()) {

	    resource.getPropertyHandler().setIsExecutable(false);
	}
    }

    @Override
    protected String initName() {

	return "Access augmenter";
    }

    @Override
    protected AugmenterSetting initSetting() {

	return new AugmenterSetting();
    }
}
