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

import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataDescriptor;

/**
 * This access augmenter asks only for remote descriptor, then sets a valid execution report.
 * 
 * @author boldrini
 */
public class EasyAccessAugmenter extends AccessAugmenter {

    /**
     * 
     */
    public EasyAccessAugmenter() {

    }

    @Override
    protected String initName() {

	return "Easy access augmenter";
    }

    /**
     * @param setting
     */
    public EasyAccessAugmenter(AugmenterSetting setting) {

	super(setting);
    }

    @Override
    public String getType() {

	return "EasyAccessAugmenter";
    }

    @Override
    public DataComplianceReport getExecutionReport(DataComplianceTester tester, DataDescriptor preview, DataDescriptor full,
	    DataComplianceLevel level) throws GSException {
	String dateTime = ISO8601DateTimeUtils.getISO8601DateTime();
	DataDescriptor targetDescriptor = level.getTargetDescriptor(preview);

	DataComplianceReport report = new DataComplianceReport(tester.getDataDownloader().getOnline().getIdentifier(), targetDescriptor,
		dateTime);
	report.setTargetTest(DataComplianceTest.EXECUTION);
	report.setTargetComplianceLevel(level);
	report.setLastSucceededTest(DataComplianceTest.EXECUTION);
	report.setExecutionTime(1000);
	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	report.setExecutionResult(message);
	report.setDownloadable(true);
	report.setValidationMessage(message);
	report.setDownloadTime(1000);
	report.setDescriptors(preview, full);
	return report;
    }
}
