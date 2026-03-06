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

package eu.essi_lab.profiler.sta;

import java.util.HashSet;
import java.util.Set;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.sta.STARequest.EntitySet;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;

/**
 * Transforms OGC STA WebRequest to DiscoveryMessage.
 */
public abstract class STATransformer extends DiscoveryRequestTransformer {

    private static final int DEFAULT_TOP = 100;

    public STATransformer() {
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	Set<Bond> operands = new HashSet<>();

	operands.add(BondFactory.createIsExecutableBond(true));

	String filter = staRequest.getFilter();
	if (filter != null && !filter.isEmpty()) {
	    // Minimal $filter support - defer full OData parsing to later
	    // For now we pass through common query params
	}

	String platformCode = request.extractQueryParameter("platformCode").orElse(null);
	if (platformCode != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, platformCode));
	}
	staRequest.getEntityIdNormalized().ifPresent(id -> {
	    if (staRequest.getEntitySet().orElse(null) == EntitySet.Observations
		    || staRequest.getEntitySet().orElse(null) == EntitySet.Datastreams) {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, id));
	    } else {
		operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, id));
	    }
	});
	String platformName = request.extractQueryParameter("platformName").orElse(null);
	if (platformName != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.PLATFORM_TITLE, platformName));
	}
	String observedProperty = request.extractQueryParameter("observedProperty").orElse(null);
	if (observedProperty != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.ATTRIBUTE_TITLE, observedProperty));
	}
	String begin = request.extractQueryParameter("begin").orElse(null);
	if (begin != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, begin));
	}
	String end = request.extractQueryParameter("end").orElse(null);
	if (end != null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.LESS_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, end));
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
    protected Page getPage(WebRequest request) throws GSException {
	STARequest staRequest = new STARequest(request);
	int skip = staRequest.getSkip() != null ? staRequest.getSkip() : 0;
	int top = staRequest.getTop() != null ? staRequest.getTop() : DEFAULT_TOP;
	if (staRequest.getEntityId().isPresent() && !staRequest.getNavigationProperty().isPresent()) {
	    top = 1;
	    skip = 0;
	}
	return new Page(skip + 1, top);
    }

    @Override
    protected DiscoveryMessage refineMessage(DiscoveryMessage message) throws GSException {
	DiscoveryMessage refined = super.refineMessage(message);
	STARequest staRequest = new STARequest(refined.getWebRequest());
	String resumptionToken = staRequest.getResumptionToken();
	if (resumptionToken != null && !resumptionToken.isEmpty()) {
	    refined.setSearchAfter(SearchAfter.of(resumptionToken));
	}
	return refined;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {
	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.ALL);
	selector.setIncludeOriginal(false);
	selector.addIndex(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	selector.addIndex(MetadataElement.PLATFORM_TITLE);
	selector.addIndex(MetadataElement.PLATFORM_IDENTIFIER);
	selector.addIndex(ResourceProperty.SOURCE_ID);
	selector.addIndex(MetadataElement.BOUNDING_BOX);
	selector.addIndex(MetadataElement.COUNTRY);
	selector.addIndex(MetadataElement.ONLINE_ID);
	return selector;
    }

    @Override
    public String getProfilerType() {
	return "STA";
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage vm = new ValidationMessage();
	vm.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return vm;
    }
}
