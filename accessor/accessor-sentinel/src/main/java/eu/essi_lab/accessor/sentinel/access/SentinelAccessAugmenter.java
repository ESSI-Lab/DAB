/**
 * 
 */
package eu.essi_lab.accessor.sentinel.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.access.augmenter.DescriptorComparator;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourcePropertyHandler;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * @author Fabrizio
 */
public class SentinelAccessAugmenter extends AccessAugmenter {

    public SentinelAccessAugmenter() {

    }

    @Override
    protected String initName() {

	return "Sentinel Access augmenter";
    }

    /**
     * @param setting
     */
    public SentinelAccessAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public String getType() {

	return "SentinelAccessAugmenter";
    }

    /**
     * 
     */
    protected void augment(GSResource resource, String onlineId, ReportsMetadataHandler handler) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("Augmenting resource with original id: {}", resource.getOriginalId());
	GSLoggerFactory.getLogger(getClass()).debug("Current online id: {}", onlineId);

	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);

	if (downloader == null) {

	    GSLoggerFactory.getLogger(getClass()).error("No DataDownloader found");
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Static augmentation STARTED");

	//
	// get the descriptors
	//
	List<DataDescriptor> fullList = downloader.getRemoteDescriptors();
	List<DataDescriptor> previewList = downloader.getPreviewRemoteDescriptors(fullList);

	fullList.sort(new DescriptorComparator());
	previewList.sort(new DescriptorComparator());

	DataDescriptor full = fullList.get(0);
	DataDescriptor preview = previewList.get(0);

	//
	// creates the static report
	//
	DataComplianceReport report = new DataComplianceReport(onlineId, null);
	report.setDescriptors(preview, full);

	report.setTargetComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setTargetTest(DataComplianceTest.EXECUTION);

	report.setDownloadable(true);
	report.setDownloadTime(1000);

	ValidationMessage validationMessage = new ValidationMessage();
	validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	report.setExecutionResult(validationMessage);
	report.setExecutionTime(1000);

	//
	// set the properties
	//
	String timeStamp = report.getTimeStamp();

	ResourcePropertyHandler propertyHandler = resource.getPropertyHandler();
	propertyHandler.setTestTimeStamp(timeStamp);

	resource.getPropertyHandler().setIsTransformable(true);
	resource.getPropertyHandler().setIsDownloadable(true);
	resource.getPropertyHandler().setIsExecutable(true);

	resource.getPropertyHandler().addDownloadTime(report.getDownloadTime().get());
	resource.getPropertyHandler().addExecutionTime(report.getExecutionTime().get());

	resource.getPropertyHandler().addComplianceLevel(DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getLabel());

	resource.getPropertyHandler().setIsGrid(true);

	//
	// adds the report
	//
	handler.addReport(report);

	GSLoggerFactory.getLogger(getClass()).debug("Static augmentation ENDED");
    }
}
