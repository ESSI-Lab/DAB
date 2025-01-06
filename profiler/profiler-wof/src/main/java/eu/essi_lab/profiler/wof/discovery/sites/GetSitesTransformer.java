package eu.essi_lab.profiler.wof.discovery.sites;

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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wof.HydroServerProfiler;
import eu.essi_lab.profiler.wof.HydroServerProfilerSetting;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFRequestValidator;
import eu.essi_lab.profiler.wof.WOFRequest.Parameter;

/**
 * HYDRO Server GetSites/GetSitesObject request transformer
 *
 * @author boldrini
 */
public class GetSitesTransformer extends DiscoveryRequestTransformer {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 1000000;

    public GetSitesTransformer() {
	// empty constructor for service loader
    }

    @Override
    public DiscoveryMessage transform(WebRequest request) throws GSException {

	DiscoveryMessage message = super.transform(request);

	WOFRequest wofRequest = getWOFRequest(request);
	String countValue = wofRequest.getParameterValue(Parameter.COUNT);

	if (countValue == null || countValue.isEmpty()) {

	    message.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	}

	return message;
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.FULL); // because of link to source is needed
	selector.setIndexesPolicy(IndexesPolicy.NONE);
	selector.setIncludeOriginal(false);
	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WebRequestValidator validator = getValidator();

	return validator.validate(request);

    }

    public WebRequestValidator getValidator() {
	return new WOFRequestValidator() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return GetSitesTransformer.this.getWOFRequest(request);
	    }
	};
    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	Set<Bond> operands = new HashSet<>();

	// we are interested only on downloadable datasets
	ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	operands.add(accessBond);

	// we are interested only on downloadable datasets
	ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	operands.add(downBond);

	// we are interested only on TIME SERIES datasets
	ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	operands.add(timeSeriesBond);

	WOFRequest request = getWOFRequest(webRequest);

	Optional<SpatialBond> areaBond = request.getSpatialBond();
	if (areaBond.isPresent()) {
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

	Optional<SimpleValueBond> keywordBond = request.getKeywordBond();
	if (keywordBond.isPresent()) {
	    operands.add(keywordBond.get());
	}

	Optional<Bond> sourcesBond = request.getSourcesBond();
	if (sourcesBond.isPresent()) {
	    operands.add(sourcesBond.get());
	}
	
	String variableCode = request.getParameterValue(Parameter.VARIABLE);
	if (variableCode!=null) {
	    operands.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL,MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, variableCode));
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
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);

    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	WOFRequest request = getWOFRequest(webRequest);
	int start = 1;
	int count = DEFAULT_PAGE_SIZE;

	String startValue = request.getParameterValue(Parameter.START);
	if (startValue != null && !startValue.isEmpty()) {
	    start = Integer.parseInt(startValue);
	}

	String countValue = request.getParameterValue(Parameter.COUNT);
	if (countValue != null && !countValue.isEmpty()) {
	    count = Integer.parseInt(countValue);
	}

	return new Page(start, count);
    }

    public WOFRequest getWOFRequest(WebRequest webRequest) {
	return new GetSitesRequest(webRequest);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new HydroServerProfilerSetting().getServiceType();
    }

}
