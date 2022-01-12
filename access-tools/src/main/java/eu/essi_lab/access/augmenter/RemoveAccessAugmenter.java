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

import java.util.Optional;

import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
public class RemoveAccessAugmenter extends ResourceAugmenter {

    /**
     *
     */
    private static final long serialVersionUID = 1925873267406623783L;

    public RemoveAccessAugmenter() {

	setLabel("Remove access augmenter");
    }

    public Optional<GSResource> augment(GSResource resource) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Remove access augmentation of current resource STARTED");

	ReportsMetadataHandler handler = new ReportsMetadataHandler(resource);

	if (handler.getReports().isEmpty()) {

	    return Optional.empty();

	} else {

	    resource.getIndexesMetadata().remove(ResourceProperty.IS_EXECUTABLE.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.IS_DOWNLOADABLE.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.IS_TRANSFORMABLE.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.DOWNLOAD_TIME.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.SUCCEEDED_TEST.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.TEST_TIME_STAMP.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.DOWNLOAD_TIME.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.EXECUTION_TIME.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.COMPLIANCE_LEVEL.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.ACCESS_QUALITY.getName());

	    resource.getIndexesMetadata().remove(ResourceProperty.SSC_SCORE.getName());

	    GSLoggerFactory.getLogger(getClass()).info("Removed access reports");

	    handler.clearReports();

	    return Optional.of(resource);
	}
    }

    @Override
    public void onOptionSet(GSConfOption<?> opt) throws GSException {
	// nothing to set
    }

    @Override
    public void onFlush() throws GSException {
	// nothing to flush
    }

}
