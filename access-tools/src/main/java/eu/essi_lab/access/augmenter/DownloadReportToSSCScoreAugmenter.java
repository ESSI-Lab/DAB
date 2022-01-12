package eu.essi_lab.access.augmenter;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionInteger;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
public class DownloadReportToSSCScoreAugmenter extends ResourceAugmenter {

    /**
     * 
     */
    private static final long serialVersionUID = 4029571299312795867L;

    private static final int DOWNLOAD_TIME_TRESHOLD = 15;
    private static final int EXECUTION_TIME_TRESHOLD = 5;

    private static final String DOWNLOAD_TIME_TRESHOLD_KEY = "DOWNLOAD_TIME_TRESHOLD_KEY";
    private static final String EXECUTION_TIME_TRESHOLD_KEY = "EXECUTION_TIME_TRESHOLD_KEY";

    public DownloadReportToSSCScoreAugmenter() {

	setLabel("Download report to SSCScore augmenter");

	GSConfOptionInteger downOption = new GSConfOptionInteger();
	downOption.setLabel("Download time (seconds) sometimes/frequently unavailable treshold");
	downOption.setValue(DOWNLOAD_TIME_TRESHOLD);
	downOption.setKey(DOWNLOAD_TIME_TRESHOLD_KEY);

	getSupportedOptions().put(DOWNLOAD_TIME_TRESHOLD_KEY, downOption);

	GSConfOptionInteger execOption = new GSConfOptionInteger();
	execOption.setLabel("Execution time (seconds) mostly/very reliable treshold");
	execOption.setValue(EXECUTION_TIME_TRESHOLD);
	execOption.setKey(EXECUTION_TIME_TRESHOLD_KEY);

	getSupportedOptions().put(EXECUTION_TIME_TRESHOLD_KEY, execOption);
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	List<DataComplianceReport> reports = handler.getReports();

	if (reports.isEmpty()) {

	    return Optional.empty();
	}

	int score = 0;

	int value = (Integer) getSupportedOptions().get(DOWNLOAD_TIME_TRESHOLD_KEY).getValue();
	long downloadTreshold = value * 1000;

	value = (Integer) getSupportedOptions().get(EXECUTION_TIME_TRESHOLD_KEY).getValue();
	long execTreshold = value * 1000;

	for (DataComplianceReport report : reports) {

	    DataComplianceTest test = report.getLastSucceededTest();

	    switch (test) {
	    case NONE:
	    case BASIC:
	    case DOWNLOAD:
		score += 0; // VERY UNRELIABLE
		break;

	    case VALIDATION:

		long downloadTime = report.getDownloadTime().get();

		if (downloadTime < downloadTreshold) {

		    score += 50; // SOMETIMES UNAVAILABLE (fast download)

		} else {

		    score += 30; // FREQUENTLY UNAVAILABLE (slow download)
		}

		break;

	    case EXECUTION:

		long execTest = report.getExecutionTime().get();

		if (execTest < execTreshold) {

		    score += 100; // VERY RELIABLE (fast execution)

		} else {

		    score += 70; // MOSTLY RELIABLE (slow execution)
		}
	    }
	}

	score = score / reports.size();

	GSLoggerFactory.getLogger(getClass()).trace("sscScore value: {}", score);

	Optional<Integer> sscScore = resource.getPropertyHandler().getSSCScore();

	if (!sscScore.isPresent() || (sscScore.isPresent() && sscScore.get() != score)) {

	    resource.getPropertyHandler().setSSCSCore(score);

	    return Optional.of(resource);
	}

	return Optional.empty();
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
    }

    @Override
    public void onFlush() throws GSException {
    }

}
