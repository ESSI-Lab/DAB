package eu.essi_lab.access.augmenter;

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

import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourcePropertyHandler;

/**
 * @author Fabrizio
 */
public class AccessQualifier {

    private ResourcePropertyHandler resourcePropertyHandler;
    private ReportsMetadataHandler reportsMetadataHandler;

    /**
     * @param resource
     */
    public AccessQualifier(GSResource resource) {

	resourcePropertyHandler = resource.getPropertyHandler();
	reportsMetadataHandler = new ReportsMetadataHandler(resource);
    }

    /**
     * 
     */
    public void setQuality() {

	List<DataComplianceReport> reports = reportsMetadataHandler.getReports();
	boolean match = reports.stream().anyMatch(r -> r.getLastSucceededTest() == DataComplianceTest.EXECUTION);

	if (match) {

	    resourcePropertyHandler.setAccessQuality(10);
	} else {

	    resourcePropertyHandler.setAccessQuality(0);
	}
    }
}
