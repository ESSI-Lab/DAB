package eu.essi_lab.profiler.terms;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.terms.TermsRequest.APIParameters;

public class TermsTransformer extends DiscoveryRequestTransformer {

    public TermsTransformer() {
	// empty constructor for service loader
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);

	selector.setIncludeOriginal(false);
	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	Set<Bond> operands = new HashSet<>();
	TermsRequest request = new TermsRequest(webRequest);

	Optional<SpatialBond> areaBond = request.getSpatialBond();
	if (areaBond.isPresent()) {
	    areaBond.get().setOperator(BondOperator.INTERSECTS);
	    operands.add(areaBond.get());
	}

	Optional<SimpleValueBond> beginBond = request.getBeginBond();
	if (beginBond.isPresent()) {
	    operands.add(beginBond.get());
	}

	Optional<SimpleValueBond> endBond = request.getEndBond();
	if (endBond.isPresent()) {
	    operands.add(endBond.get());
	}

	String observedProperty = request.getParameterValue(APIParameters.OBSERVED_PROPERTY);
	if (observedProperty != null) {
	    SimpleValueBond b1 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ATTRIBUTE_TITLE, observedProperty);
	    operands.add(b1);
	}

	String providerCode = request.getParameterValue(APIParameters.SOURCE);
	if (providerCode != null) {
	    operands.add(BondFactory.createSourceIdentifierBond(providerCode));
	}

	switch (operands.size()) {
	case 0:
	    return null;
	case 1:
	    return operands.iterator().next();
	default:
	    return BondFactory.createAndBond(operands);
	}

    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	int start = 1;
	int max = 10;
	TermsRequest request = new TermsRequest(webRequest);

	String limit = request.getParameterValue(APIParameters.LIMIT);
	String offset = request.getParameterValue(APIParameters.OFFSET);

	if (offset != null) {
	    start = Integer.parseInt(offset);
	}
	if (limit != null) {
	    max = Integer.parseInt(limit);
	}

	Page userPage = new Page(start, max);
	return userPage;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new TermsProfilerSetting().getServiceType();
    }

}
