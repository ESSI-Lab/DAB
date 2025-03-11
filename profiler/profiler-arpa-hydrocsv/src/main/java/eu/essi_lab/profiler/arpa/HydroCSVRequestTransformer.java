package eu.essi_lab.profiler.arpa;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.profiler.arpa.HydroCSVParameters.HydroCSVParameter;

public class HydroCSVRequestTransformer extends DiscoveryRequestTransformer {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();

	try {
	    new HydroCSVParameters(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (IllegalArgumentException e) {
	    ret.setError(e.getMessage());
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    @Override
    protected Bond getUserBond(WebRequest request) throws GSException {

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

	// optional parameters
	HydroCSVParameters parameters = new HydroCSVParameters(request);

	String siteCode = parameters.getParameter(HydroCSVParameter.SITE_CODE);
	if (siteCode != null && !siteCode.equals("")) {
	    SimpleValueBond platformBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.UNIQUE_PLATFORM_IDENTIFIER,
		    siteCode);
	    operands.add(platformBond);
	}

	String variableCode = parameters.getParameter(HydroCSVParameter.VARIABLE_CODE);
	if (variableCode != null && !variableCode.equals("")) {
	    SimpleValueBond parameterBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL,
		    MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, variableCode);
	    operands.add(parameterBond);
	}

	String timeseriesCode = parameters.getParameter(HydroCSVParameter.TIMESERIES_CODE);
	if (timeseriesCode != null && !timeseriesCode.equals("")) {
	    SimpleValueBond timeseriesBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID,
		    timeseriesCode);
	    operands.add(timeseriesBond);
	}

	return BondFactory.createAndBond(operands);

    }

    @Override
    protected Optional<Queryable> getOrderingProperty() {
	return Optional.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
    }

    @Override
    protected Optional<SortOrder> getOrderingDirection() {
	return Optional.of(SortOrder.ASCENDING);
    }

    @Override
    protected ResourceSelector getSelector(WebRequest request) {

	ResourceSelector selector = new ResourceSelector();
	selector.setSubset(ResourceSubset.CORE_EXTENDED);
	selector.setIndexesPolicy(IndexesPolicy.NONE);

	return selector;
    }

    @Override
    public String getProfilerType() {
	return HydroCSVProfilerSetting.ARPA_HYDROCSV_PROFILER_TYPE;
    }

    @Override
    protected Optional<Queryable> getDistinctElement(WebRequest request) {
	HydroCSVParameters parameters = new HydroCSVParameters(request);
	String distinct = parameters.getParameter(HydroCSVParameter.DISTINCT_BY);
	if (distinct != null) {
	    switch (distinct) {
	    case "site":
	    case "sites":
		return Optional.of(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	    case "variable":
	    case "variables":
		return Optional.of(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	    default:
		break;
	    }
	}
	return Optional.empty();
    }

    @Override
    protected Page getPage(WebRequest request) throws GSException {
	Page ret;
	int start = 1;
	int count = 100;
	HydroCSVParameters parameters = new HydroCSVParameters(request);
	try {
	    start = Integer.parseInt(parameters.getParameter(HydroCSVParameter.START));
	} catch (Exception e) {
	    //
	}
	try {
	    count = Integer.parseInt(parameters.getParameter(HydroCSVParameter.COUNT));
	} catch (Exception e) {
	    //
	}
	ret = new Page(start, count);
	return ret;

    }

}
