package eu.essi_lab.profiler.sos.sensor;

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

import java.util.HashSet;
import java.util.Set;

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
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.sos.SOSProfiler;
import eu.essi_lab.profiler.sos.SOSProfilerSetting;
import eu.essi_lab.profiler.sos.SOSUtils;
import eu.essi_lab.profiler.sos.SOSRequest.Parameter;

/**
 * @author boldrini
 */
public class DescribeSensorTransformer extends DiscoveryRequestTransformer {

    public DescribeSensorTransformer() {
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.FULL);
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	return selector;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	WebRequestValidator validator = getValidator();

	return validator.validate(request);

    }

    public WebRequestValidator getValidator() {
	return new DescribeSensorValidator();
    }

    @Override
    protected Bond getUserBond(WebRequest webRequest) throws GSException {

	DescribeSensorRequest request = new DescribeSensorRequest(webRequest);

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

	String procedure = request.getParameterValue(Parameter.PROCEDURE);
	if (procedure != null) {
	    String uniqueAttributeId = procedure.replace(SOSUtils.PROCEDURE_PREFIX, "");
	    operands.add(
		    BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, uniqueAttributeId));
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
	int count = 10000; // in order to prevent timeouts from the Helgoland SOS client
	// keep it under 200 or increase timeout in restful.war Helgoland configuration

	return new Page(start, count);
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    @Override
    public String getProfilerType() {

	return new SOSProfilerSetting().getServiceType();
    }

}
