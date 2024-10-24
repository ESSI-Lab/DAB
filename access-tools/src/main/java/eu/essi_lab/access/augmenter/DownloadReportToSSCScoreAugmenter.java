package eu.essi_lab.access.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class DownloadReportToSSCScoreAugmenter extends ResourceAugmenter<DownloadReportToSSCScoreAugmenterSetting> {

    /**
     *  
     */
    public DownloadReportToSSCScoreAugmenter() {

    }

    /**
     * @param setting
     */
    public DownloadReportToSSCScoreAugmenter(DownloadReportToSSCScoreAugmenterSetting setting) {

	super(setting);
    }

    @Override
    public String getType() {

	return "DownloadReportToSSCScoreAugmenter";
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	List<DataComplianceReport> reports = handler.getReports();

	if (reports.isEmpty()) {

	    return Optional.empty();
	}

	int score = 0;

	int value = getSetting().getDownloadTimeTreshold();
	long downloadTreshold = value * 1000;

	value = getSetting().getExecutionTimeTreshold();
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
    protected String initName() {

	return "Download report to SSCScore augmenter";
    }

    @Override
    protected DownloadReportToSSCScoreAugmenterSetting initSetting() {

	return new DownloadReportToSSCScoreAugmenterSetting();
    }
}
