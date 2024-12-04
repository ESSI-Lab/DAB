package eu.essi_lab.profiler.wof.discovery.variables;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.wof.HydroServerProfiler;
import eu.essi_lab.profiler.wof.HydroServerProfilerSetting;
import eu.essi_lab.profiler.wof.WOFRequest;
import eu.essi_lab.profiler.wof.WOFRequestValidator;
import eu.essi_lab.profiler.wof.WOFRequest.Parameter;

/**
 * HYDRO Server GetVariables/GetVariablesObject request transformer
 *
 * @author boldrini
 */
public class GetVariablesTransformer extends DiscoveryRequestTransformer {

    public GetVariablesTransformer() {
    }

    public WOFRequest getWOFRequest(WebRequest request) {
	return new GetVariablesRequest(request);
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
    public DiscoveryMessage transform(WebRequest request) throws GSException {
	DiscoveryMessage message = super.transform(request);
	message.setRequestTimeout(12000); // 200 minutes
	return message;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WOFRequestValidator validator = new WOFRequestValidator() {

	    @Override
	    public WOFRequest getWOFRequest(WebRequest request) {
		return GetVariablesTransformer.this.getWOFRequest(request);
	    }
	};
	return validator.validate(request);

    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {
	List<Bond> operands = new ArrayList<>();

	// we are interested only on downloadable datasets
	ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	operands.add(accessBond);

	// we are interested only on downloadable datasets
	ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	operands.add(downBond);

	// we are interested only on TIME SERIES datasets
	ResourcePropertyBond timeSeriesBond = BondFactory.createIsTimeSeriesBond(true);
	operands.add(timeSeriesBond);

	WOFRequest wofRequest = getWOFRequest(webRequest);
	String uniqueVariableCode = wofRequest.getParameterValue(Parameter.VARIABLE);

	if (uniqueVariableCode != null) {
	    if (uniqueVariableCode.contains(":")) {
		uniqueVariableCode = uniqueVariableCode.substring(uniqueVariableCode.indexOf(":") + 1);
	    }
	    Bond variableBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER,
		    uniqueVariableCode);
	    operands.add(variableBond);
	}

	return BondFactory.createAndBond(operands);

    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	return Optional.of(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);

    }

    @Override
    protected Page getPage(WebRequest webRequest) throws GSException {

	WOFRequest request = getWOFRequest(webRequest);
	int start = 1;
	int count = 1000000000;
	String startValue = request.getParameterValue(Parameter.START);
	if (startValue != null && !startValue.equals("")) {
	    start = Integer.parseInt(startValue);
	}
	String countValue = request.getParameterValue(Parameter.COUNT);
	if (countValue != null && !countValue.equals("")) {
	    count = Integer.parseInt(countValue);
	}

	return new Page(start, count);
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
