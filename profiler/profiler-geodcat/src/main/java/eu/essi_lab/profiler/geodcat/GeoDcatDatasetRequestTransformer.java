package eu.essi_lab.profiler.geodcat;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * Discovery by public metadata identifier for a single dataset description.
 */
public class GeoDcatDatasetRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	String id = GeoDcatRequestPaths.extractDatasetIdentifier(request);
	if (id == null || id.isEmpty()) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError("Dataset identifier missing (path segment or identifier query parameter)");
	    message.setLocator("identifier");
	}

	return message;
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	String id = GeoDcatRequestPaths.extractDatasetIdentifier(request);

	return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id);
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setIncludeOriginal(false);

	return selector;
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	return new Page(1, 1);
    }

    @Override
    public String getProfilerType() {

	return new GeoDcatProfilerSetting().getServiceType();
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }
}
