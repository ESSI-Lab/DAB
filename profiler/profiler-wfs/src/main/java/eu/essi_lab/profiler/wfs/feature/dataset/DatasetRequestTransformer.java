package eu.essi_lab.profiler.wfs.feature.dataset;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * @author boldrini
 */
public class DatasetRequestTransformer extends DiscoveryRequestTransformer {

    public static final String FEATURE_QUERY_ERROR = "FEATURE_QUERY_ERROR";

    public DatasetRequestTransformer() {

    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();

	selector.setSubset(ResourceSubset.CORE_EXTENDED);

	selector.setIndexesPolicy(IndexesPolicy.NONE);

	selector.setIncludeOriginal(false);

	return selector;
    }

    private ResourceSubset getResourceSubset(WebRequest request) {
	return ResourceSubset.CORE_EXTENDED;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {

	ResourceSubset subset = getResourceSubset(request);

	if (subset.equals(ResourceSubset.EXTENDED)) {
	    return new Page(1, 100);
	}

	return new Page(1, 10);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return "WFS_SERVER";
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

	// operands.add(BondFactory.createSpatialExtentBond(BondOperator.CONTAINS, extent));
	return null;

    }

}
